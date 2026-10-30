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

import java.util.List;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class DiscussionImageServiceTest {
  @TestManagedMock
  private AttachmentService attachments;
  @TestManagedMock
  private Event<DiscussionImageService.Cleanup> cleanup;
  @TestedBean
  private DiscussionImageService service;

  @Test
  void imageUrlsIncludeTheSilverpeasContextPath() {
    var document = mock(SimpleDocument.class);
    when(document.getId()).thenReturn("photo");
    when(document.getFilename()).thenReturn("photo.png");
    when(document.getContentType()).thenReturn("image/png");
    when(document.getAttachmentURL()).thenReturn("/attached_file/componentId/discussion1/attachmentId/photo/lang/fr/name/photo.png?t_=1");
    when(attachments.listDocumentsByForeignKey(any(), isNull())).thenReturn(
        new org.silverpeas.core.contribution.attachment.util.SimpleDocumentList<>(List.of(document)));
    try (var urls = mockStatic(org.silverpeas.core.util.URLUtil.class)) {
      urls.when(org.silverpeas.core.util.URLUtil::getApplicationURL).thenReturn("/silverpeas");
      var image = service.list("discussion1", "message1").getFirst();
      assertEquals("/silverpeas/attached_file/componentId/discussion1/attachmentId/photo/lang/fr/name/photo.png?t_=1", image.url());
    }
  }

  @Test
  void imagesUsePrivateNativeAttachmentsAndScheduleRollbackCleanup() throws Exception {
    var user = mock(User.class);
    when(user.getId()).thenReturn("writer");
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      var image = DiscussionImageUpload.read(DiscussionImageUploadTest.image("png"));
      new DiscussionImageService(attachments, cleanup).store("discussion1", "message1", List.of(image));
      var document = ArgumentCaptor.forClass(SimpleDocument.class);
      verify(attachments).createAttachment(document.capture(), any(java.io.InputStream.class), eq(false), eq(false));
      assertEquals("discussion1", document.getValue().getInstanceId());
      assertEquals("message1", document.getValue().getForeignId());
      assertFalse(document.getValue().isPublic());
      assertEquals("image/png", document.getValue().getContentType());
      verify(cleanup).fire(new DiscussionImageService.Cleanup("discussion1", "message1", true));
    }
  }

  @Test
  void failedPublicationAndCommittedDeletionCleanOnlyTheirOwnAttachments() throws Exception {
    var created = new DiscussionImageService.Cleanup("discussion1", "message1", true);
    var deleted = new DiscussionImageService.Cleanup("discussion1", "message1", false);
    service.committed(created);
    service.rollback(deleted);
    verifyNoInteractions(attachments);
    service.rollback(created);
    service.committed(deleted);
    verify(attachments, times(2)).deleteAllAttachments("message1", "discussion1");
    assertEquals(TransactionPhase.AFTER_FAILURE, DiscussionImageService.class.getMethod("rollback", DiscussionImageService.Cleanup.class)
        .getParameters()[0].getAnnotation(Observes.class).during());
    assertEquals(TransactionPhase.AFTER_SUCCESS, DiscussionImageService.class.getMethod("committed", DiscussionImageService.Cleanup.class)
        .getParameters()[0].getAnnotation(Observes.class).during());
  }
}
