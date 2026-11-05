/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.discussion.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.wbe.WbeFile;
import static org.silverpeas.core.cache.service.CacheAccessorProvider.getThreadCacheAccessor;

/** Shared stable WOPI file identity backed directly by a Discussion attachment. */
public class DiscussionWbeFile extends WbeFile {
  private final DiscussionDocumentEditionService service;
  private final String instanceId;
  private final String attachmentId;
  private final String language;

  public DiscussionWbeFile(DiscussionDocumentEditionService service, String instanceId, String attachmentId, String language) {
    this.service = service;
    this.instanceId = instanceId;
    this.attachmentId = attachmentId;
    this.language = language;
  }

  String instanceId() { return instanceId; }
  String language() { return language; }
  private SimpleDocument document() { return service.document(instanceId, attachmentId, language); }
  private String actorKey() { return getClass().getName() + ":actor:" + id(); }
  private User actor() {
    var user = getThreadCacheAccessor().getCache().get(actorKey(), User.class);
    if (user == null) { throw new SecurityException("Missing authenticated WBE request"); }
    return user;
  }

  @Override
  public String silverpeasId() { return attachmentId; }
  @Override
  public String id() { return "discussion-" + attachmentId + "-" + language; }
  @Override
  public Optional<ResourceReference> linkedToResource() { return Optional.of(new ResourceReference(attachmentId, instanceId)); }
  @Override
  public User owner() { return User.getById(document().getCreatedBy()); }
  @Override
  public String name() { return document().getFilename(); }
  @Override
  public String mimeType() { return FileUtil.getMimeType(document().getAttachmentPath()); }
  @Override
  public long size() { return document().getSize(); }
  @Override
  public OffsetDateTime lastModificationDate() {
    var document = document();
    var date = document.getLastUpdateDate() == null ? document.getCreationDate() : document.getLastUpdateDate();
    return date.toInstant().atOffset(ZoneOffset.UTC);
  }
  @Override
  public boolean canBeAccessedBy(User user) { return authorize(user, false); }
  @Override
  public boolean canBeModifiedBy(User user) { return authorize(user, true); }

  private boolean authorize(User user, boolean modification) {
    // WOPI callbacks authenticate through their edition context, not the browser session.
    // Keep the verified actor in request-local storage; never on this shared cached file.
    getThreadCacheAccessor().getCache().remove(actorKey());
    try {
      var document = document();
      boolean allowed = modification ? service.canModify(document, user) : service.canAccess(document, user);
      if (allowed) { getThreadCacheAccessor().getCache().put(actorKey(), user); }
      return allowed;
    } catch (SecurityException unavailable) { return false; }
  }
  @Override
  public synchronized void updateFrom(InputStream input) throws IOException {
    try { service.save(this, actor(), input); }
    finally { getThreadCacheAccessor().getCache().remove(actorKey()); }
  }
  @Override
  public String toString() { return "DiscussionWbeFile[" + id() + "]"; }

  @Override
  public void loadInto(OutputStream output) throws IOException {
    try { service.read(this, actor(), output); }
    finally { getThreadCacheAccessor().getCache().remove(actorKey()); }
  }
}
