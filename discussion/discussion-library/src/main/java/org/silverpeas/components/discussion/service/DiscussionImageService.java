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

import java.util.Date;
import java.util.List;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.i18n.I18NHelper;

/** Native, private attachments owned by the message's UUID and component instance. */
@Service
public class DiscussionImageService {
  public record Cleanup(String instanceId, String messageId, boolean rollback) { }
  @Inject
  private AttachmentService attachments;
  @Inject
  private Event<Cleanup> cleanup;

  public DiscussionImageService() {
    // CDI constructor.
  }

  DiscussionImageService(AttachmentService attachments, Event<Cleanup> cleanup) {
    this.attachments = attachments;
    this.cleanup = cleanup;
  }

  public List<DiscussionImage> list(String instanceId, String messageId) {
    return attachments.listDocumentsByForeignKey(new ResourceReference(messageId, instanceId), null).stream()
        .filter(document -> java.util.Set.of("image/jpeg", "image/png", "image/gif").contains(document.getContentType()))
        .map(document -> new DiscussionImage(document.getId(), document.getAttachmentURL(), document.getFilename())).toList();
  }

  public void store(String instanceId, String messageId, List<DiscussionImageUpload> images) {
    if (images.isEmpty()) {
      return;
    }
    cleanup.fire(new Cleanup(instanceId, messageId, true));
    int order = 0;
    for (var image : images) {
      String name = image.filename();
      var file = SimpleAttachment.builder(I18NHelper.getDefaultLanguage()).setFilename(name).setTitle(name)
          .setSize(image.size()).setContentType(image.contentType())
          .setCreationData(User.getCurrentRequester().getId(), new Date()).build();
      var document = new SimpleDocument(new SimpleDocumentPK(null, instanceId), messageId, order++, false, file);
      document.setPublicDocument(false);
      attachments.createAttachment(document, image.openStream(), false, false);
    }
  }

  public void removeAfterCommit(String instanceId, String messageId) {
    cleanup.fire(new Cleanup(instanceId, messageId, false));
  }

  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public void rollback(@Observes(during = TransactionPhase.AFTER_FAILURE) Cleanup change) {
    if (change.rollback()) {
      attachments.deleteAllAttachments(change.messageId(), change.instanceId());
    }
  }

  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public void committed(@Observes(during = TransactionPhase.AFTER_SUCCESS) Cleanup change) {
    if (!change.rollback()) {
      attachments.deleteAllAttachments(change.messageId(), change.instanceId());
    }
  }
}
