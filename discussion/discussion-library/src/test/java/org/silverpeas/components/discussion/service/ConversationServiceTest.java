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
import java.util.Optional;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.silverpeas.components.discussion.model.*;
import org.silverpeas.components.discussion.repository.*;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class ConversationServiceTest {
  @TestManagedMock
  private ConversationRepository conversations;
  @TestManagedMock
  private DiscussionMessageRepository messages;
  @TestManagedMock
  private DiscussionReadStateRepository readStates;
  @TestManagedMock
  private DiscussionAccess access;
  @org.silverpeas.core.persistence.datasource.PersistOperation
  @TestManagedMock
  private org.silverpeas.core.persistence.datasource.model.jpa.JpaPersistOperation persistOperation;
  @TestManagedMock
  private DiscussionLikeRepository likes;
  @TestManagedMock
  private DiscussionMentionService mentions;
  @TestManagedMock
  private org.silverpeas.components.discussion.notification.DiscussionChangeDispatcher changes;
  @TestManagedMock
  private org.silverpeas.components.discussion.repository.DiscussionSeenRepository seen;
  @TestedBean
  private DefaultConversationService service;
  private Conversation conversation;
  private User user;

  @BeforeEach
  void prepare() {
    when(mentions.normalize(anyString(), any())).thenAnswer(call -> call.getArgument(1));
    conversation = mock(Conversation.class);
    when(conversation.getId()).thenReturn("conversation");
    when(conversation.getLastSequence()).thenReturn(4L);
    when(conversations.main(eq("discussion1"), anyBoolean())).thenReturn(conversation);
    user = mock(User.class);
    when(user.getId()).thenReturn("user");
    when(access.require(eq("discussion1"), any())).thenReturn(SilverpeasRole.WRITER);
  }

  @Test
  void historyUsesAnExclusiveCursorAndRequestsOnlyOneExtraMessage() {
    var one = new DiscussionMessage("conversation", 4, "four");
    var two = new DiscussionMessage("conversation", 3, "three");
    var three = new DiscussionMessage("conversation", 2, "two");
    when(messages.history("conversation", 5, 3)).thenReturn(List.of(one, two, three));
    when(readStates.find("conversation", "user")).thenReturn(Optional.empty());
    when(messages.unread("conversation", "user", 0)).thenReturn(3L);
    try (MockedStatic<User> current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      var page = service.history("discussion1", 5, 2);
      assertEquals(List.of(one, two), page.messages());
      assertEquals(List.of(two, one), page.getChronologicalMessages());
      assertEquals(3, page.nextBefore());
      assertTrue(page.hasMore());
      assertEquals(3, page.unreadCount());
      assertThrows(UnsupportedOperationException.class, () -> page.messages().clear());
    }
  }

  @Test
  void invalidHistoryLimitsAreRejectedBeforeQuerying() {
    assertThrows(IllegalArgumentException.class, () -> service.history("discussion1", 0, 30));
    assertThrows(IllegalArgumentException.class, () -> service.history("discussion1", 10, 101));
    assertThrows(IllegalArgumentException.class, () -> service.history("discussion1", 10, 0));
    verifyNoInteractions(messages);
  }

  @Test
  void inaccessibleHistoryDoesNotReadTheDatabase() {
    when(access.require("discussion1", SilverpeasRole.WRITER)).thenThrow(SecurityException.class);
    assertThrows(SecurityException.class, () -> service.history("discussion1", 10, 30));
    verifyNoInteractions(conversations, messages);
  }

  @Test
  void aMessageFromAnotherInstanceCannotBeEdited() {
    var message = mock(DiscussionMessage.class);
    when(messages.getById("message")).thenReturn(message);
    when(message.getConversationId()).thenReturn("other-conversation");
    assertThrows(IllegalArgumentException.class, () -> service.edit("discussion1", "message", 0, "x"));
    verify(messages, never()).save(any(DiscussionMessage.class));
  }

  @Test
  void anotherAuthorsMessageCannotBeDeleted() {
    var message = existingMessage("other-author");
    try (MockedStatic<User> current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      assertThrows(SecurityException.class, () -> service.delete("discussion1", "message", 0));
      verify(message, never()).delete();
    }
  }

  @Test
  void staleEditsDoNotOverwriteAnotherEdit() {
    var message = existingMessage("user");
    when(message.getVersion()).thenReturn(2L);
    try (MockedStatic<User> current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      assertThrows(OptimisticLockException.class,
          () -> service.edit("discussion1", "message", 1, "stale"));
      verify(message, never()).edit(anyString());
    }
  }

  @Test
  void authorCanEditAndManagerCanDelete() {
    var message = existingMessage("user");
    try (MockedStatic<User> current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      service.edit("discussion1", "message", 0, "updated");
      verify(message).edit("updated");
      when(message.getCreatorId()).thenReturn("another-user");
      when(access.require("discussion1", SilverpeasRole.WRITER)).thenReturn(SilverpeasRole.ADMIN);
      service.delete("discussion1", "message", 0);
      verify(changes, times(2)).changed("discussion1");
      verify(message).delete();
    }
  }

  @Test
  void readPositionOnlyAdvancesToAnExplicitlyAcknowledgedSequence() {
    var state = mock(DiscussionReadState.class);
    when(readStates.find("conversation", "user")).thenReturn(Optional.of(state));
    try (MockedStatic<User> current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      service.markRead("discussion1", 3);
      verify(changes).read("discussion1", "user");
      verify(state).advanceTo(3, 4);
      verify(state, never()).createdBy(any(User.class));
      verify(readStates).save(state);
    }
  }

  @Test
  void readersCannotPublish() {
    when(access.require("discussion1", SilverpeasRole.WRITER)).thenThrow(SecurityException.class);
    assertThrows(SecurityException.class, () -> service.publish("discussion1", "body"));
    verifyNoInteractions(conversations, messages);
  }

  @Test
  void publicationAllocatesASequenceUnderTheConversationLock() {
    when(conversation.nextSequence()).thenReturn(5L);
    try (MockedStatic<User> current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      service.publish("discussion1", "new message");
      verify(changes).changed("discussion1");
      var captured = org.mockito.ArgumentCaptor.forClass(DiscussionMessage.class);
      verify(conversations).main("discussion1", true);
      verify(messages).save(captured.capture());
      assertEquals("conversation", captured.getValue().getConversationId());
      assertEquals(5, captured.getValue().getSequenceNumber());
      assertEquals("new message", captured.getValue().getBody());
    }
  }

  @Test
  void aNewReadPositionIsInitializedButGuestsDoNotCreateOne() {
    when(readStates.find("conversation", "user")).thenReturn(Optional.empty());
    try (MockedStatic<User> current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      service.markRead("discussion1", 2);
      var captured = org.mockito.ArgumentCaptor.forClass(DiscussionReadState.class);
      verify(readStates).save(captured.capture());
      assertEquals(2, captured.getValue().getLastReadSequence());
      clearInvocations(readStates);
      when(user.isAccessGuest()).thenReturn(true);
      service.markRead("discussion1", 4);
      verifyNoInteractions(readStates);
    }
  }

  @Test
  void participantListingIsScopedToTheAuthorizedConversation() {
    when(messages.authorIds("conversation")).thenReturn(List.of("a", "b"));
    assertEquals(List.of("a", "b"), service.participantIds("discussion1"));
    when(access.require("discussion1", SilverpeasRole.WRITER)).thenThrow(SecurityException.class);
    clearInvocations(conversations, messages);
    assertThrows(SecurityException.class, () -> service.participantIds("discussion1"));
    verifyNoInteractions(conversations, messages);
  }

  @Test
  void likesAreIdempotentAndCanBeRemovedByTheCurrentUser() {
    existingMessage("other");
    var reaction = mock(DiscussionLike.class);
    when(likes.find("message", "user")).thenReturn(Optional.empty(), Optional.of(reaction), Optional.of(reaction));
    when(likes.states("conversation", List.of("message"), "user"))
        .thenReturn(java.util.Map.of("message", new LikeState(1, true)));
    try (MockedStatic<User> current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      assertTrue(service.setLike("discussion1", "message", true).liked());
      service.setLike("discussion1", "message", true);
      verify(likes, times(1)).save(any(DiscussionLike.class));
      service.setLike("discussion1", "message", false);
      verify(changes, times(2)).changed("discussion1");
      verify(likes).delete(reaction);
      verify(access, times(3)).require("discussion1", SilverpeasRole.WRITER);
    }
  }

  @Test
  void reactionsRejectOtherInstancesDeletedMessagesAndGuests() {
    var message = existingMessage("other");
    try (MockedStatic<User> current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      when(message.getConversationId()).thenReturn("other-conversation");
      assertThrows(IllegalArgumentException.class, () -> service.setLike("discussion1", "message", true));
      when(message.getConversationId()).thenReturn("conversation");
      when(message.isDeleted()).thenReturn(true);
      assertThrows(IllegalArgumentException.class, () -> service.setLike("discussion1", "message", true));
      when(user.isAccessGuest()).thenReturn(true);
      assertThrows(SecurityException.class, () -> service.setLike("discussion1", "message", true));
      verifyNoInteractions(likes);
    }
  }

  @Test
  void reactionCountsAndNotificationDeepLinksStayWithinTheConversation() {
    var message = existingMessage("other");
    when(message.getSequenceNumber()).thenReturn(2L);
    when(messages.history("conversation", 3, 31)).thenReturn(List.of(message));
    when(likes.states("conversation", List.of("message"), "user"))
        .thenReturn(java.util.Map.of("message", new LikeState(3, false)));
    when(readStates.find("conversation", "user")).thenReturn(Optional.empty());
    try (MockedStatic<User> current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      assertEquals(3, service.likeStates("discussion1", List.of("message")).get("message").count());
      assertEquals(List.of(message), service.historyAround("discussion1", "message").messages());
      when(message.getConversationId()).thenReturn("another");
      assertThrows(IllegalArgumentException.class, () -> service.historyAround("discussion1", "message"));
    }
  }

  @Test
  void unreadFlagsUseTheReadingPositionAndExcludeOwnAndDeletedMessages() {
    var read = mock(DiscussionReadState.class);
    when(read.getLastReadSequence()).thenReturn(3L);
    when(readStates.find("conversation", "user")).thenReturn(Optional.of(read));
    var unseen = historyMessage(6, "other", false);
    var own = historyMessage(5, "user", false);
    var deleted = historyMessage(4, "other", true);
    var seen = historyMessage(3, "other", false);
    when(messages.history("conversation", Long.MAX_VALUE, 31))
        .thenReturn(List.of(unseen, own, deleted, seen));
    when(messages.unread("conversation", "user", 3)).thenReturn(1L);
    try (MockedStatic<User> current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      var page = service.history("discussion1", Long.MAX_VALUE, 30);
      assertEquals(List.of(seen, deleted, own, unseen), page.getChronologicalMessages());
      assertEquals(3, page.nextBefore());
      assertEquals(1, page.unreadCount());
      verify(unseen).setUnread(true);
      verify(own).setUnread(false);
      verify(deleted).setUnread(false);
      verify(seen).setUnread(false);
    }
  }

  private DiscussionMessage historyMessage(long sequence, String author, boolean deleted) {
    var message = mock(DiscussionMessage.class);
    when(message.getSequenceNumber()).thenReturn(sequence);
    when(message.getCreatorId()).thenReturn(author);
    when(message.isDeleted()).thenReturn(deleted);
    return message;
  }

  @Test
  void updatesCombineLoadedRowsWithNewRowsAndApplyReadState() {
    var existing = mock(DiscussionMessage.class);
    when(existing.getId()).thenReturn("m2");
    when(existing.getSequenceNumber()).thenReturn(2L);
    when(existing.getCreatorId()).thenReturn("other");
    var added = mock(DiscussionMessage.class);
    when(added.getId()).thenReturn("m5");
    when(added.getSequenceNumber()).thenReturn(5L);
    when(added.getCreatorId()).thenReturn("other");
    when(messages.selected("conversation", List.of("m2", "foreign"))).thenReturn(List.of(existing));
    when(messages.newer("conversation", 4)).thenReturn(List.of(added));
    when(readStates.find("conversation", "user")).thenReturn(Optional.empty());
    when(messages.unread("conversation", "user", 0)).thenReturn(2L);
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      var result = service.updates("discussion1", 4, List.of("m2", "foreign"));
      assertEquals(List.of(added, existing), result.messages());
      assertEquals(2, result.unreadCount());
      verify(existing).setUnread(true);
      verify(added).setUnread(true);
      verify(access).require("discussion1", SilverpeasRole.WRITER);
    }
  }

  @Test
  void updateRequestsAreBoundedAndRequireAccessBeforeQueries() {
    assertThrows(IllegalArgumentException.class, () -> service.updates("discussion1", -1, List.of()));
    assertThrows(IllegalArgumentException.class, () -> service.updates("discussion1", 0,
        java.util.Collections.nCopies(101, "m1")));
    when(access.require("discussion1", SilverpeasRole.WRITER)).thenThrow(SecurityException.class);
    assertThrows(SecurityException.class, () -> service.updates("discussion1", 0, List.of()));
    verifyNoInteractions(messages, conversations);
  }

  @Test
  void visibleMessagesAreAcknowledgedIndividuallyAndOnlyOnce() {
    var message = mock(DiscussionMessage.class);
    when(message.getId()).thenReturn("visible");
    when(message.getCreatorId()).thenReturn("other");
    when(message.getSequenceNumber()).thenReturn(4L);
    when(messages.selected("conversation", List.of("visible"))).thenReturn(List.of(message));
    when(seen.seen("conversation", "user", List.of("visible")))
        .thenReturn(List.of(), List.of("visible"));
    when(messages.unread("conversation", "user", 0)).thenReturn(2L);
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      assertEquals(2, service.markVisible("discussion1", List.of("visible")));
      service.markVisible("discussion1", List.of("visible"));
      verify(seen, times(1)).save(any(org.silverpeas.components.discussion.model.DiscussionSeen.class));
      verify(changes, times(1)).read("discussion1", "user");
      verify(readStates, never()).save(any(DiscussionReadState.class));
    }
  }

  @Test
  void acknowledgmentsRejectForeignMessagesAndOversizedRequests() {
    assertThrows(IllegalArgumentException.class, () -> service.markVisible("discussion1", List.of()));
    assertThrows(IllegalArgumentException.class, () -> service.markVisible("discussion1",
        java.util.Collections.nCopies(101, "x")));
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      assertThrows(IllegalArgumentException.class, () -> service.markVisible("discussion1", List.of("foreign")));
    }
    verify(seen, never()).save(any(org.silverpeas.components.discussion.model.DiscussionSeen.class));
  }

  private DiscussionMessage existingMessage(String author) {
    var message = mock(DiscussionMessage.class);
    when(messages.getById("message")).thenReturn(message);
    when(message.getConversationId()).thenReturn("conversation");
    when(message.getCreatorId()).thenReturn(author);
    when(message.getVersion()).thenReturn(0L);
    return message;
  }
}
