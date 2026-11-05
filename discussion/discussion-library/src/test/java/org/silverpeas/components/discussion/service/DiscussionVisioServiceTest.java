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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.silverpeas.components.discussion.model.Conversation;
import org.silverpeas.components.discussion.notification.DiscussionVisioDispatcher;
import org.silverpeas.components.discussion.notification.DiscussionVisioEvent;
import org.silverpeas.components.discussion.repository.ConversationRepository;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.chat.ChatSettings;
import org.silverpeas.core.chat.servers.ChatServer;
import org.silverpeas.core.security.authorization.ComponentAccessController;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class DiscussionVisioServiceTest {
  @TestManagedMock private DiscussionAccess access;
  @TestManagedMock private ChatSettings settings;
  @TestManagedMock @org.silverpeas.core.chat.servers.DefaultChatServer private ChatServer chat;
  @TestManagedMock private ConversationRepository conversations;
  @TestManagedMock private ConversationService discussion;
  @TestManagedMock private OrganizationController organization;
  @TestManagedMock private ComponentAccessController componentAccess;
  @TestManagedMock private DiscussionVisioDispatcher invitations;
  @TestedBean private DiscussionVisioService service;
  private User actor;
  private Conversation conversation;

  @BeforeEach
  void prepare() {
    actor = user("actor");
    conversation = mock(Conversation.class);
    when(conversations.current(eq("discussion1"), anyBoolean())).thenReturn(conversation);
    when(settings.isChatEnabled()).thenReturn(true);
    when(settings.isVisioEnabled()).thenReturn(true);
    when(settings.getVisioUrl()).thenReturn("https://silverpeas.example/silverpeas/visio/");
    when(chat.isAllowed(actor)).thenReturn(true);
    var active = user("active");
    var removed = user("removed");
    when(removed.isRemovedState()).thenReturn(true);
    var deactivated = user("deactivated");
    when(deactivated.isDeactivatedState()).thenReturn(true);
    var guest = user("guest");
    when(guest.isAccessGuest()).thenReturn(true);
    var denied = user("denied");
    when(organization.<User>getAllUsers("discussion1")).thenReturn(new User[]{actor, active, active, removed, deactivated, guest, denied});
    when(componentAccess.isUserAuthorized("active", "discussion1")).thenReturn(true);
  }

  @Test
  void disabledImOrVideoOrUnauthorizedUserCannotCreateOrSeeTheAction() {
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(actor);
      when(settings.isChatEnabled()).thenReturn(false);
      assertFalse(service.options("discussion1").available());
      assertThrows(SecurityException.class, () -> service.create("discussion1", "", "Meeting"));
      when(settings.isChatEnabled()).thenReturn(true);
      when(settings.isVisioEnabled()).thenReturn(false);
      assertFalse(service.options("discussion1").available());
      when(settings.isVisioEnabled()).thenReturn(true);
      when(chat.isAllowed(actor)).thenReturn(false);
      assertFalse(service.options("discussion1").available());
      verifyNoInteractions(discussion, invitations);
    }
  }

  @Test
  void createsOneAnnouncementAndInvitesOnlyDistinctActiveAuthorizedMembers() {
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(actor);
      assertEquals(1, service.options("discussion1").invitees());
      var meeting = service.create("discussion1", "", "Design [review]");
      assertTrue(meeting.url().matches("https://silverpeas.example/silverpeas/visio/discussion-[0-9a-f-]{36}"));
      verify(discussion).publish(eq("discussion1"), contains("Design \\[review\\]"));
      var event = ArgumentCaptor.forClass(DiscussionVisioEvent.class);
      verify(invitations).schedule(event.capture());
      assertEquals(java.util.Set.of("active"), event.getValue().recipients());
      assertEquals(meeting, event.getValue().meeting());
      assertNotEquals(meeting.id(), service.create("discussion1", "", "Another meeting").id());
    }
  }

  @Test
  void threadAnnouncementPreservesTheCurrentThread() {
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(actor);
      var root = mock(org.silverpeas.components.discussion.model.DiscussionMessage.class);
      when(root.getId()).thenReturn("root");
      when(discussion.threadRoot("discussion1", "root")).thenReturn(root);
      service.create("discussion1", "root", "Meeting");
      verify(discussion).reply(eq("discussion1"), eq("root"), anyString(), eq(List.of()), eq(List.of()));
      verify(discussion, never()).publish(anyString(), anyString());
    }
  }

  @Test
  void rejectsArchivedChannelsInvalidTitlesAndUnsafeConfiguration() {
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(actor);
      for (String title : new String[]{"", " ", "x".repeat(161), "Line\nBreak"}) {
        assertThrows(IllegalArgumentException.class, () -> service.create("discussion1", "", title));
      }
      when(settings.getVisioUrl()).thenReturn("javascript:alert(1)");
      assertThrows(IllegalArgumentException.class, () -> service.create("discussion1", "", "Meeting"));
      when(conversation.isArchived()).thenReturn(true);
      assertThrows(IllegalArgumentException.class, () -> service.create("discussion1", "", "Meeting"));
      verifyNoInteractions(invitations);
      verify(discussion, never()).publish(anyString(), anyString());
    }
  }

  @Test
  void accessDenialOrForeignThreadCannotPublishOrInvite() {
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(actor);
      var root = mock(org.silverpeas.components.discussion.model.DiscussionMessage.class);
      when(root.getId()).thenReturn("different-root");
      when(discussion.threadRoot("discussion1", "reply")).thenReturn(root);
      assertThrows(IllegalArgumentException.class,
          () -> service.create("discussion1", "reply", "Meeting"));
      doThrow(new SecurityException("Denied")).when(access).require(anyString(), any());
      assertThrows(SecurityException.class, () -> service.create("discussion1", "", "Meeting"));
      verifyNoInteractions(invitations);
      verify(discussion, never()).publish(anyString(), anyString());
    }
  }

  private User user(String id) {
    var user = mock(User.class);
    when(user.getId()).thenReturn(id);
    return user;
  }
}
