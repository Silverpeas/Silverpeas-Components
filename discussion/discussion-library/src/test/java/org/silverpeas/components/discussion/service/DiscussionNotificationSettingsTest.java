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

import java.util.Optional;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.silverpeas.components.discussion.model.Conversation;
import org.silverpeas.components.discussion.model.DiscussionReadState;
import org.silverpeas.components.discussion.notification.DiscussionNotificationPreferenceEvent;
import org.silverpeas.components.discussion.repository.ConversationRepository;
import org.silverpeas.components.discussion.repository.DiscussionReadStateRepository;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.sse.DefaultServerEventNotifier;
import org.silverpeas.core.security.authorization.ComponentAccessController;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class DiscussionNotificationSettingsTest {
  @TestManagedMock private DiscussionAccess access;
  @TestManagedMock private ConversationRepository conversations;
  @TestManagedMock private DiscussionReadStateRepository states;
  @TestManagedMock private DefaultServerEventNotifier notifier;
  @TestManagedMock private ComponentAccessController receivers;
  @org.silverpeas.core.persistence.datasource.PersistOperation
  @TestManagedMock private org.silverpeas.core.persistence.datasource.model.jpa.JpaPersistOperation persistOperation;
  @TestManagedMock private org.silverpeas.components.discussion.repository.DiscussionMessageRepository messages;
  @TestedBean private DiscussionNotificationSettings settings;

  @Test
  void channelModesFilterDesktopDeliveryUsingTheActualMessageAndRecipient() {
    var conversation = mock(Conversation.class);
    when(conversation.getId()).thenReturn("projects");
    when(conversations.forMessage("discussion1", "message")).thenReturn(conversation);
    var message = mock(org.silverpeas.components.discussion.model.DiscussionMessage.class);
    when(message.getBody()).thenReturn("Bonjour @[Bernard](2)");
    when(messages.getById("message")).thenReturn(message);
    var state = new org.silverpeas.components.discussion.model.DiscussionReadState("projects", "2");
    when(states.find("projects", "2")).thenReturn(Optional.of(state));
    assertFalse(settings.channelMutedForMessage("discussion1", "message", "2"));
    state.setNotificationMode("MENTIONS");
    assertFalse(settings.channelMutedForMessage("discussion1", "message", "2"));
    when(message.getBody()).thenReturn("Bonjour @[Autre utilisateur](12)");
    assertTrue(settings.channelMutedForMessage("discussion1", "message", "2"));
    state.setNotificationMode("NONE");
    assertTrue(settings.channelMutedForMessage("discussion1", "message", "2"));
    assertFalse(settings.channelMutedForMessage("discussion1", "message", "3"));
  }

  @Test
  void preferencesBelongToTheRequesterAndDoNotMarkMessagesRead() {
    var conversation = mock(Conversation.class);
    when(conversation.getId()).thenReturn("conversation");
    when(conversations.main(eq("discussion1"), anyBoolean())).thenReturn(conversation);
    var user = mock(User.class);
    when(user.getId()).thenReturn("writer");
    when(states.find("conversation", "writer")).thenReturn(Optional.empty());
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      assertEquals(new DesktopPreference(true, -1), settings.current("discussion1"));
      assertFalse(settings.save("discussion1", false).enabled());
      var state = ArgumentCaptor.forClass(DiscussionReadState.class);
      verify(states).save(state.capture());
      assertFalse(state.getValue().isDesktopNotifications());
      assertEquals(0, state.getValue().getLastReadSequence());
      verify(states, times(2)).find("conversation", "writer");
      verify(states).flush();
      verifyNoInteractions(notifier);
    }
  }

  @Test
  void existingReadingPositionIsPreservedAndUnauthorizedRequestsCannotWrite() {
    var conversation = mock(Conversation.class);
    when(conversation.getId()).thenReturn("conversation");
    when(conversations.main(eq("discussion1"), anyBoolean())).thenReturn(conversation);
    var state = mock(DiscussionReadState.class);
    when(state.getVersion()).thenReturn(7L);
    when(states.find("conversation", "writer")).thenReturn(Optional.of(state));
    var user = mock(User.class);
    when(user.getId()).thenReturn("writer");
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      assertEquals(new DesktopPreference(false, 7), settings.save("discussion1", false));
      verify(state, never()).advanceTo(anyLong(), anyLong());
      when(access.require("discussion1", SilverpeasRole.WRITER)).thenThrow(SecurityException.class);
      clearInvocations(states, conversations);
      assertThrows(SecurityException.class, () -> settings.save("discussion1", true));
      assertThrows(SecurityException.class, () -> settings.current("discussion1"));
      verifyNoInteractions(states, conversations);
    }
  }

  @Test
  void changesArePublishedAfterCommitAndOnlyToTheOwnersAuthorizedSessions() throws Exception {
    var change = new DiscussionNotificationSettings.Change("discussion1", "writer", new DesktopPreference(false, 3));
    settings.afterCommit(change);
    var capture = ArgumentCaptor.forClass(DiscussionNotificationPreferenceEvent.class);
    verify(notifier).notify(capture.capture());
    var event = capture.getValue();
    var receiver = mock(User.class);
    when(receiver.getId()).thenReturn("other");
    assertFalse(event.isConcerned("session", receiver));
    verifyNoInteractions(receivers);
    when(receiver.getId()).thenReturn("writer");
    when(receivers.isUserAuthorized("writer", "discussion1")).thenReturn(true);
    when(receivers.getUserRoles(eq("writer"), eq("discussion1"), any())).thenReturn(java.util.Set.of(SilverpeasRole.WRITER));
    assertTrue(event.isConcerned("session", receiver));
    assertTrue(event.getData("session", receiver).contains("\"enabled\":false"));
    assertTrue(event.getData("session", receiver).contains("\"revision\":3"));
    assertEquals(TransactionPhase.AFTER_SUCCESS, DiscussionNotificationSettings.class.getMethod("afterCommit", DiscussionNotificationSettings.Change.class)
        .getParameters()[0].getAnnotation(Observes.class).during());
    when(receivers.isUserAuthorized("writer", "discussion1")).thenReturn(false);
    assertFalse(event.isConcerned("session", receiver));
  }
}
