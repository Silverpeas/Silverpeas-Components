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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.silverpeas.components.discussion.model.Conversation;
import org.silverpeas.components.discussion.model.DiscussionMessage;
import org.silverpeas.components.discussion.notification.DiscussionChangeDispatcher;
import org.silverpeas.components.discussion.repository.ConversationRepository;
import org.silverpeas.components.discussion.repository.DiscussionMessageRepository;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.security.authorization.ComponentAccessController;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.wbe.WbeHostManager;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class DiscussionDocumentEditionServiceTest {
  @TestManagedMock private AttachmentService attachments;
  @TestManagedMock private WbeHostManager editor;
  @TestManagedMock private ComponentAccessController access;
  @TestManagedMock private DiscussionMessageRepository messages;
  @TestManagedMock private ConversationRepository conversations;
  @TestManagedMock private DiscussionUploadSettings limits;
  @TestManagedMock private DiscussionChangeDispatcher changes;
  @TestedBean private DiscussionDocumentEditionService service;
  @TempDir private Path temporary;
  private SimpleDocument document;
  private DiscussionMessage message;
  private Conversation conversation;
  private User user;

  @BeforeEach
  void prepare() throws Exception {
    user = mock(User.class);
    when(user.getId()).thenReturn("writer");
    document = mock(SimpleDocument.class);
    when(document.getId()).thenReturn("file1");
    when(document.getInstanceId()).thenReturn("discussion1");
    when(document.getForeignId()).thenReturn("message1");
    when(document.getLanguage()).thenReturn("fr");
    when(document.getFilename()).thenReturn("budget.docx");
    when(document.getContentType()).thenReturn("application/octet-stream");
    when(document.isOpenOfficeCompatible()).thenReturn(true);
    when(document.getSize()).thenReturn(3L);
    when(document.getCreationDate()).thenReturn(new Date(1));
    var path = temporary.resolve("budget.docx");
    Files.write(path, new byte[]{1, 2, 3});
    when(document.getAttachmentPath()).thenReturn(path.toString());
    when(attachments.searchDocumentById(any(SimpleDocumentPK.class), eq("fr"))).thenReturn(document);
    message = mock(DiscussionMessage.class);
    when(message.getConversationId()).thenReturn("conversation1");
    when(messages.getById("message1")).thenReturn(message);
    conversation = mock(Conversation.class);
    when(conversation.getComponentInstanceId()).thenReturn("discussion1");
    when(conversations.getById("conversation1")).thenReturn(conversation);
    when(conversations.forMessage("discussion1", "message1")).thenReturn(conversation);
    when(editor.isEnabled()).thenReturn(true);
    when(editor.isHandled(any())).thenReturn(true);
    when(access.isUserAuthorized("writer", "discussion1")).thenReturn(true);
    when(access.getUserRoles(eq("writer"), eq("discussion1"), any())).thenReturn(Set.of(SilverpeasRole.WRITER));
    when(limits.forInstance("discussion1")).thenReturn(new DiscussionUploadSettings.Limits(4, 5, 1));
  }

  private DiscussionWbeFile open() {
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      var document = service.prepare("discussion1", "file1", "fr");
      return new DiscussionWbeFile(service, document.getInstanceId(), document.getId(), document.getLanguage());
    }
  }

  @Test
  void directEditingHasStableSharedIdentityAndNeverReservesOrUnlocksTheAttachment() {
    var file = open();
    assertEquals(file.id(), open().id());
    assertEquals("file1", file.silverpeasId());
    assertEquals("budget.docx", file.name());
    assertEquals(3, file.size());
    assertEquals("docx", file.ext());
    assertEquals(1, file.lastModificationDate().toInstant().toEpochMilli());
    assertTrue(file.linkedToResource().isPresent());
    verify(attachments, never()).lock(anyString(), anyString(), anyString());
    verify(attachments, never()).unlock(any());
  }

  @Test
  void moduleDisabledUnsupportedFormatOrExistingLegacyReservationHidesAndDeniesEditing() {
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      when(editor.isEnabled()).thenReturn(false);
      assertFalse(service.isEditable(document));
      assertThrows(SecurityException.class, () -> service.prepare("discussion1", "file1", "fr"));
      verify(editor, never()).isHandled(any());
      when(editor.isEnabled()).thenReturn(true);
      when(editor.isHandled(any())).thenReturn(false);
      assertFalse(service.isEditable(document));
      when(editor.isHandled(any())).thenReturn(true);
      when(document.isOpenOfficeCompatible()).thenReturn(false);
      assertFalse(service.isEditable(document));
      when(document.isOpenOfficeCompatible()).thenReturn(true);
      when(document.isEdited()).thenReturn(true);
      assertFalse(service.isEditable(document));
    }
  }

  @Test
  void callbackReadsNativeContentAndCannotSaveWithoutAnAuthenticatedEditionContext() throws Exception {
    var file = open();
    assertThrows(SecurityException.class, () -> file.updateFrom(new ByteArrayInputStream(new byte[]{4})));
    assertTrue(file.canBeAccessedBy(user));
    var output = new ByteArrayOutputStream();
    file.loadInto(output);
    assertArrayEquals(new byte[]{1, 2, 3}, output.toByteArray());
    assertThrows(SecurityException.class, () -> file.loadInto(output));
  }

  @Test
  void saveUpdatesTheOriginalThroughNativeAttachmentServicesAndPublishesAChange() throws Exception {
    var file = open();
    var futureDate = new Date(System.currentTimeMillis() + 10000);
    when(document.getLastUpdateDate()).thenReturn(futureDate);
    doAnswer(invocation -> {
      var content = invocation.getArgument(1, java.io.File.class);
      assertArrayEquals(new byte[]{4, 5, 6, 7}, Files.readAllBytes(content.toPath()));
      return null;
    }).when(attachments).updateAttachment(eq(document), any(java.io.File.class), eq(true), eq(false));
    assertTrue(file.canBeModifiedBy(user));
    file.updateFrom(new ByteArrayInputStream(new byte[]{4, 5, 6, 7}));
    verify(document).setSize(4);
    verify(document).setUpdatedBy("writer");
    verify(document).setLastUpdateDate(new Date(futureDate.getTime() + 1));
    verify(changes).changed("discussion1");
    verify(attachments, never()).lock(anyString(), anyString(), anyString());
    verify(attachments, never()).unlock(any());
  }

  @Test
  void oversizedSavedContentLeavesTheOriginalUntouched() {
    var file = open();
    assertTrue(file.canBeModifiedBy(user));
    assertThrows(IOException.class, () -> file.updateFrom(new ByteArrayInputStream(new byte[1024 * 1024 + 1])));
    verify(attachments, never()).updateAttachment(any(), any(java.io.File.class), anyBoolean(), anyBoolean());
    verifyNoInteractions(changes);
  }

  @Test
  void deletedForeignAndArchivedMessagesAndRevokedAccessAreCheckedAgainForCallbacks() throws Exception {
    var file = open();
    assertTrue(file.canBeModifiedBy(user));
    when(conversation.isArchived()).thenReturn(true);
    assertThrows(SecurityException.class, () -> file.updateFrom(new ByteArrayInputStream(new byte[]{4})));
    assertFalse(file.canBeModifiedBy(user));
    assertTrue(file.canBeAccessedBy(user));
    when(conversation.isArchived()).thenReturn(false);
    when(access.isUserAuthorized("writer", "discussion1")).thenReturn(false);
    assertFalse(file.canBeAccessedBy(user));
    when(access.isUserAuthorized("writer", "discussion1")).thenReturn(true);
    when(message.isDeleted()).thenReturn(true);
    assertFalse(file.canBeModifiedBy(user));
    assertThrows(SecurityException.class, this::open);
    when(message.isDeleted()).thenReturn(false);
    when(conversation.getComponentInstanceId()).thenReturn("discussion2");
    assertThrows(SecurityException.class, this::open);
    when(document.getInstanceId()).thenReturn("discussion2");
    assertThrows(SecurityException.class, this::open);
  }
}
