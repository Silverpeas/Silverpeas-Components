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

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import org.silverpeas.components.discussion.notification.DiscussionChangeDispatcher;
import org.silverpeas.components.discussion.repository.ConversationRepository;
import org.silverpeas.components.discussion.repository.DiscussionMessageRepository;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.ComponentAccessController;
import org.silverpeas.core.wbe.WbeHostManager;

/** Direct WBE access to message attachments: no WebDAV checkout or persistent reservation. */
@Service
@Transactional
public class DiscussionDocumentEditionService {
  @Inject private AttachmentService attachments;
  @Inject private WbeHostManager editor;
  @Inject private ComponentAccessController access;
  @Inject private DiscussionMessageRepository messages;
  @Inject private ConversationRepository conversations;
  @Inject private DiscussionUploadSettings limits;
  @Inject private DiscussionChangeDispatcher changes;

  public boolean isEditable(SimpleDocument document) {
    return editor.isEnabled() && document.isOpenOfficeCompatible() && !document.isEdited()
        && canModify(document, User.getCurrentRequester())
        && editor.isHandled(file(document));
  }

  public SimpleDocument prepare(String instanceId, String id, String language) {
    var document = document(instanceId, id, language);
    if (!isEditable(document)) { throw new SecurityException("Browser editing unavailable"); }
    return document;
  }

  private DiscussionWbeFile file(SimpleDocument document) {
    return new DiscussionWbeFile(this, document.getInstanceId(), document.getId(), document.getLanguage());
  }

  public SimpleDocument document(String instanceId, String id, String language) {
    var document = attachments.searchDocumentById(new SimpleDocumentPK(id, instanceId), language);
    if (document == null || !instanceId.equals(document.getInstanceId())
        || !"application/octet-stream".equals(document.getContentType())) {
      throw new SecurityException("Unknown discussion attachment");
    }
    var message = messages.getById(document.getForeignId());
    var conversation = message == null ? null : conversations.getById(message.getConversationId());
    if (message == null || message.isDeleted() || conversation == null
        || !instanceId.equals(conversation.getComponentInstanceId())) {
      throw new SecurityException("Unknown discussion attachment message");
    }
    return document;
  }

  public boolean canAccess(SimpleDocument document, User user) {
    if (user == null || user.isAnonymous() || user.isAccessGuest() || user.isRemovedState()
        || user.isDeletedState() || user.isDeactivatedState()
        || !access.isUserAuthorized(user.getId(), document.getInstanceId())) { return false; }
    var role = SilverpeasRole.getHighestFrom(access.getUserRoles(user.getId(), document.getInstanceId(), AccessControlContext.init()));
    return role != null && role.isGreaterThanOrEquals(SilverpeasRole.WRITER);
  }

  public boolean canModify(SimpleDocument document, User user) {
    return canAccess(document, user) && !document.isEdited()
        && !conversations.forMessage(document.getInstanceId(), document.getForeignId()).isArchived();
  }

  public void save(DiscussionWbeFile file, User user, InputStream input) throws IOException {
    var document = document(file.instanceId(), file.silverpeasId(), file.language());
    if (!editor.isEnabled() || !canModify(document, user) || !editor.isHandled(file)) {
      throw new SecurityException("Discussion attachment is no longer editable");
    }
    // Validate the complete new content before replacing the native attachment.
    var temporary = Files.createTempFile("discussion-wbe-", ".tmp");
    try {
      long maximum = limits.forInstance(document.getInstanceId()).getFileBytes();
      try (var output = Files.newOutputStream(temporary);
          var bounded = org.apache.commons.io.input.BoundedInputStream.builder()
              .setInputStream(input).setMaxCount(maximum + 1).setPropagateClose(false).get()) {
        bounded.transferTo(output);
        if (bounded.getCount() > maximum) { throw new IOException("Edited attachment exceeds instance limit"); }
      }
      document.setSize(Files.size(temporary));
      document.setUpdatedBy(user.getId());
      long previous = document.getLastUpdateDate() == null ? 0 : document.getLastUpdateDate().getTime();
      document.setLastUpdateDate(new Date(Math.max(System.currentTimeMillis(), previous + 1)));
      attachments.updateAttachment(document, temporary.toFile(), true, false);
      changes.changed(document.getInstanceId());
    } finally {
      Files.deleteIfExists(temporary);
    }
  }

  public void read(DiscussionWbeFile file, User user, OutputStream output) throws IOException {
    var document = document(file.instanceId(), file.silverpeasId(), file.language());
    if (!editor.isEnabled() || !canAccess(document, user)) {
      throw new SecurityException("Discussion attachment access denied");
    }
    Files.copy(Path.of(document.getAttachmentPath()), output);
  }
}
