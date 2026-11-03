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
import java.util.Map;
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
  @TestManagedMock
  private DiscussionAttachmentService imageService;
  @TestManagedMock
  private org.silverpeas.components.discussion.notification.DiscussionSubscriptionDispatcher subscriptions;
  @TestManagedMock
  private org.silverpeas.components.discussion.search.DiscussionMessageIndex messageIndex;
  @TestManagedMock
  private DiscussionLinkedEventService linkedEvents;
  @TestManagedMock
  private DiscussionLinkedIdeaService linkedIdeas;
  @TestManagedMock
  private DiscussionLinkedVoteService linkedVotes;
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
    when(conversations.current(eq("discussion1"), anyBoolean())).thenReturn(conversation);
    user = mock(User.class);
    when(user.getId()).thenReturn("user");
    when(access.require(eq("discussion1"), any())).thenReturn(SilverpeasRole.WRITER);
  }

  @Test
  void repliesShareTheConversationSequenceAndNativeMessageFeatures() {
    var root = existingMessage("other-author");
    when(conversation.nextSequence()).thenReturn(5L);
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      var image = mock(DiscussionImageUpload.class);
      var file = mock(DiscussionFileUpload.class);
      service.reply("discussion1", "message", "Une réponse", List.of(image), List.of(file));
      verify(imageService).store(eq("discussion1"), any(), eq(List.of(image)));
      verify(imageService).storeFiles(eq("discussion1"), any(), eq(List.of(file)));
      var saved = org.mockito.ArgumentCaptor.forClass(DiscussionMessage.class);
      verify(messages).save(saved.capture());
      assertEquals("message", saved.getValue().getThreadRootId());
      assertEquals("conversation", saved.getValue().getConversationId());
      assertEquals(5, saved.getValue().getSequenceNumber());
      verify(conversations).current("discussion1", true);
      verify(mentions).notifyNewMentions("discussion1", saved.getValue(), "");
      verify(messageIndex).schedule(eq("discussion1"), any());
      verify(subscriptions).schedule(argThat(event -> "message".equals(event.message().getThreadRootId())));
      verify(changes).published(eq("discussion1"), any(), eq("user"), any(), eq("Une réponse"));
    }
  }

  @Test
  void threadOperationsRequireTheSameWriterAccessAsTheMainConversation() {
    when(access.require("discussion1", SilverpeasRole.WRITER)).thenThrow(SecurityException.class);
    assertThrows(SecurityException.class,
        () -> service.reply("discussion1", "root", "reply", List.of(), List.of()));
    assertThrows(SecurityException.class,
        () -> service.threadRoot("discussion1", "reply"));
    assertThrows(SecurityException.class,
        () -> service.threadHistory("discussion1", "root", Long.MAX_VALUE, 30));
    assertThrows(SecurityException.class,
        () -> service.threadUpdates("discussion1", "root", 0, List.of()));
    verifyNoInteractions(conversations, messages);
  }

  @Test
  void foreignAndNestedThreadRootsCannotBeUsedToPublishReplies() {
    var root = existingMessage("other-author");
    when(root.getConversationId()).thenReturn("foreign-conversation");
    assertThrows(IllegalArgumentException.class,
        () -> service.reply("discussion1", "message", "reply", List.of(), List.of()));
    when(root.getConversationId()).thenReturn("conversation");
    when(root.getThreadRootId()).thenReturn("actual-root");
    assertThrows(IllegalArgumentException.class,
        () -> service.reply("discussion1", "message", "reply", List.of(), List.of()));
    assertThrows(IllegalArgumentException.class,
        () -> service.reply("discussion1", "", "reply", List.of(), List.of()));
    verify(messages, never()).save(any(DiscussionMessage.class));
    verifyNoInteractions(imageService, messageIndex, subscriptions);
  }

  @Test
  void replyPermalinksResolveTheirRootAndLoadTheThreadAroundTheReply() {
    var root = existingMessage("other-author");
    var reply = mock(DiscussionMessage.class);
    when(reply.getId()).thenReturn("reply");
    when(reply.getConversationId()).thenReturn("conversation");
    when(reply.getThreadRootId()).thenReturn("message");
    when(reply.getSequenceNumber()).thenReturn(5L);
    when(messages.getById("reply")).thenReturn(reply);
    when(messages.threadHistory("conversation", "message", 6, 31)).thenReturn(List.of(reply, root));
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      assertSame(root, service.threadRoot("discussion1", "reply"));
      assertEquals(List.of(reply, root), service.historyAround("discussion1", "reply").messages());
      verify(messages, never()).history(anyString(), anyLong(), anyInt());
    }
  }

  @Test
  void timelineUpdatesSkipRepliesAndAdvanceCursorAcrossOtherThreads() {
    var root = existingMessage("other-author");
    var reply = mock(DiscussionMessage.class);
    when(reply.getId()).thenReturn("reply");
    when(reply.getThreadRootId()).thenReturn("message");
    when(messages.selected("conversation", List.of("message", "reply"))).thenReturn(List.of(root, reply));
    when(messages.threadStatistics("conversation", List.of("message"), "user", 0))
        .thenReturn(Map.of("message", new long[]{3, 2}));
    when(conversation.getLastSequence()).thenReturn(99L);
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      var page = service.updates("discussion1", 4, List.of("message", "reply"));
      assertEquals(List.of(root), page.messages());
      assertEquals(99, page.getSyncSequence());
      verify(root).setThreadStatistics(3, 2);
    }
  }

  @Test
  void threadUpdatesFilterOtherThreadsAndHistoryRetainsPagination() {
    var root = existingMessage("other-author");
    var reply = mock(DiscussionMessage.class);
    when(reply.getId()).thenReturn("reply");
    when(reply.getThreadRootId()).thenReturn("message");
    when(reply.getSequenceNumber()).thenReturn(5L);
    var other = mock(DiscussionMessage.class);
    when(other.getId()).thenReturn("foreign-reply");
    when(other.getThreadRootId()).thenReturn("other-root");
    when(messages.selected("conversation", List.of("reply", "foreign-reply"))).thenReturn(List.of(reply, other));
    when(messages.threadNewer("conversation", "message", 4)).thenReturn(List.of(reply));
    when(messages.threadHistory("conversation", "message", 6, 2)).thenReturn(List.of(reply, root));
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      var updates = service.threadUpdates("discussion1", "message", 4, List.of("reply", "foreign-reply"));
      assertEquals(List.of(reply), updates.messages());
      var history = service.threadHistory("discussion1", "message", 6, 1);
      assertEquals(List.of(reply), history.messages());
      assertTrue(history.hasMore());
      assertEquals(5, history.nextBefore());
      when(root.getThreadRootId()).thenReturn("nested-root");
      assertThrows(IllegalArgumentException.class,
          () -> service.threadHistory("discussion1", "message", 6, 30));
    }
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
      verify(message).edit("updated", false);
      verifyNoInteractions(subscriptions); // The mock did not change its body.

      when(message.getCreatorId()).thenReturn("another-user");
      when(access.require("discussion1", SilverpeasRole.WRITER)).thenReturn(SilverpeasRole.ADMIN);
      service.delete("discussion1", "message", 0);
      verify(changes, times(2)).changed("discussion1");
      verify(message).delete();
      verify(messageIndex, times(2)).schedule("discussion1", "message");
    }
  }

  @Test
  void changedMessageSchedulesUpdateButUnchangedMessageDoesNot() {
    var message = existingMessage("user");
    when(message.getBody()).thenReturn("old", "updated");
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      service.edit("discussion1", "message", 0, "updated");
      verify(subscriptions).schedule(argThat(event -> event.action() ==
          org.silverpeas.core.notification.user.client.constant.NotifAction.UPDATE
          && event.message() == message && event.sender() == user));
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
      verify(changes).published(eq("discussion1"), any(), eq("user"), any(), any());
      verify(subscriptions).schedule(argThat(event -> event.action() ==
          org.silverpeas.core.notification.user.client.constant.NotifAction.CREATE
          && event.instanceId().equals("discussion1") && event.sender() == user));
      verify(changes).changed("discussion1");
      var captured = org.mockito.ArgumentCaptor.forClass(DiscussionMessage.class);
      verify(conversations).current("discussion1", true);
      verify(messages).save(captured.capture());
      verify(messageIndex).schedule(eq("discussion1"), any());
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
      when(message.getId()).thenReturn("message");
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

  @Test
  void pinningIsSharedIdempotentAndLimitedToTheAuthorizedConversation() {
    var message = existingMessage("other");
    service.setPinned("discussion1", "message", true);
    verify(message).setPinned(true);
    verify(messages).flush();
    verify(changes).changed("discussion1");
    clearInvocations(messages, changes);
    when(message.isPinned()).thenReturn(true);
    service.setPinned("discussion1", "message", true);
    verify(messages, never()).save(message);
    verifyNoInteractions(changes);
    service.setPinned("discussion1", "message", false);
    verify(message).setPinned(false);
    when(message.getConversationId()).thenReturn("other-conversation");
    assertThrows(IllegalArgumentException.class, () -> service.setPinned("discussion1", "message", true));
    when(message.getId()).thenReturn("message");
    when(message.getConversationId()).thenReturn("conversation");
    when(message.isDeleted()).thenReturn(true);
    assertThrows(IllegalArgumentException.class, () -> service.setPinned("discussion1", "message", true));
    when(access.require("discussion1", SilverpeasRole.WRITER)).thenThrow(SecurityException.class);
    clearInvocations(conversations, messages);
    assertThrows(SecurityException.class, () -> service.setPinned("discussion1", "message", true));
    assertThrows(SecurityException.class, () -> service.pinnedMessages("discussion1"));
    verifyNoInteractions(conversations, messages);
  }

  @Test
  void pinnedExcerptsHideMentionIdsAndDoNotSplitEmoji() {
    var message = mock(DiscussionMessage.class);
    when(message.getId()).thenReturn("old-message");
    when(message.getCreator()).thenReturn(user);
    when(user.getDisplayedName()).thenReturn("Alice");
    when(message.getBody()).thenReturn("@[Bernard](2) " + "🚀".repeat(180));
    when(messages.pinned("conversation")).thenReturn(List.of(message));
    var pin = service.pinnedMessages("discussion1").getFirst();
    assertEquals("old-message", pin.id());
    assertEquals("Alice", pin.author());
    assertTrue(pin.excerpt().startsWith("@Bernard 🚀"));
    assertFalse(pin.excerpt().contains("(2)"));
    assertEquals(181, pin.excerpt().codePointCount(0, pin.excerpt().length()));
    assertTrue(pin.excerpt().endsWith("…"));
    when(message.getCreator()).thenReturn(null);
    assertEquals("", service.pinnedMessages("discussion1").getFirst().author());
    var actual = new DiscussionMessage("conversation", 1, "Message");
    actual.setPinned(true);
    actual.delete();
    assertFalse(actual.isPinned());
    assertThrows(IllegalArgumentException.class, () -> actual.setPinned(true));
  }

  @Test
  void imagesAreLimitedToVisibleMessagesInTheAuthorizedConversation() {
    var message = existingMessage("other");
    when(message.getId()).thenReturn("message");
    var deleted = mock(DiscussionMessage.class);
    when(deleted.isDeleted()).thenReturn(true);
    when(messages.selected("conversation", List.of("message", "deleted", "foreign"))).thenReturn(List.of(message, deleted));
    var image = new DiscussionImage("photo", "/protected-image", "image.png");
    when(imageService.list("discussion1", "message")).thenReturn(List.of(image));
    assertEquals(java.util.Map.of("message", List.of(image)), service.images("discussion1", List.of("message", "deleted", "foreign")));
    verify(imageService, never()).list("discussion1", "foreign");
    when(access.require("discussion1", SilverpeasRole.WRITER)).thenThrow(SecurityException.class);
    clearInvocations(messages, imageService);
    assertThrows(SecurityException.class, () -> service.images("discussion1", List.of("message")));
    verifyNoInteractions(messages, imageService);
  }

  @Test
  void imageOnlyMessagesKeepValidationAndStoreImagesBeforeAnnouncingPublication() throws Exception {
    var image = DiscussionImageUpload.read(DiscussionImageUploadTest.image("png"));
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      service.publish("discussion1", "", List.of(image));
      var captured = org.mockito.ArgumentCaptor.forClass(DiscussionMessage.class);
      verify(messages).save(captured.capture());
      assertTrue(captured.getValue().getBody().isBlank());
      assertFalse(captured.getValue().getBody().isEmpty());
      var ordered = inOrder(messages, imageService, changes);
      ordered.verify(messages).flush();
      ordered.verify(imageService).store(eq("discussion1"), any(), eq(List.of(image)));
      ordered.verify(changes).changed("discussion1");
      assertThrows(IllegalArgumentException.class, () -> service.publish("discussion1", "", List.of()));
      assertThrows(IllegalArgumentException.class, () -> service.publish("discussion1", "too many", java.util.Collections.nCopies(5, image)));
    }
    var message = new DiscussionMessage("conversation", 1, "", true);
    message.edit(" ", true);
    assertThrows(IllegalArgumentException.class, () -> message.edit(" ", false));
    message.delete();
    assertThrows(IllegalArgumentException.class, () -> message.edit("text", true));
  }

  @Test
  void fileOnlyPublicationAndTheSharedIndexStayWithinTheAuthorizedConversation() throws Exception {
    var upload = DiscussionFileUpload.read(DiscussionFileUploadTest.file("budget.pdf", new byte[]{1}));
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      service.publish("discussion1", "", List.of(), List.of(upload));
      var captured = org.mockito.ArgumentCaptor.forClass(DiscussionMessage.class);
      verify(messages).save(captured.capture());
      assertTrue(captured.getValue().isFilesAttached());
      assertTrue(captured.getValue().getBody().isBlank());
      verify(imageService).storeFiles(eq("discussion1"), any(), eq(List.of(upload)));
      assertThrows(IllegalArgumentException.class, () -> service.publish("discussion1", "text", List.of(), java.util.Collections.nCopies(5, upload)));
    }
    var file = new DiscussionFile("file", "budget.pdf", "/silverpeas/attached_file/file", 1, "old-message");
    when(messages.fileMessageIds("conversation")).thenReturn(List.of("old-message"));
    when(imageService.files("discussion1", "old-message")).thenReturn(List.of(file));
    assertEquals(List.of(file), service.sharedFiles("discussion1"));
    when(access.require("discussion1", SilverpeasRole.WRITER)).thenThrow(SecurityException.class);
    clearInvocations(messages, imageService);
    assertThrows(SecurityException.class, () -> service.sharedFiles("discussion1"));
    assertThrows(SecurityException.class, () -> service.files("discussion1", List.of("foreign")));
    verifyNoInteractions(messages, imageService);
  }

  @Test
  void archivedChannelCannotAcceptNewMessages() {
    var channel = mock(Conversation.class);
    when(channel.isArchived()).thenReturn(true);
    when(conversations.current("discussion1", true)).thenReturn(channel);
    assertThrows(IllegalArgumentException.class, () -> service.publish("discussion1", "Message"));
    verify(messages, never()).save(any(DiscussionMessage.class));
  }

  @Test
  void eventDeletionIsOptionalAndFailureNeverDeletesTheMessage() {
    var message = existingMessage("user");
    when(message.getBody()).thenReturn("event body");
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      service.delete("discussion1", "message", 0);
      verifyNoInteractions(linkedEvents);
      clearInvocations(message, messages, likes);
      doThrow(SecurityException.class).when(linkedEvents).delete("event body");
      assertThrows(SecurityException.class, () -> service.delete("discussion1", "message", 0, true));
      verify(message, never()).delete();
      verify(messages, never()).save(any(DiscussionMessage.class));
      doNothing().when(linkedEvents).delete("event body");
      service.delete("discussion1", "message", 0, true);
      var order = inOrder(linkedEvents, message);
      order.verify(linkedEvents, atLeastOnce()).delete("event body");
      order.verify(message).delete();
    }
  }

  @Test
  void deletionInformationRequiresTheSameAuthorAndVersionChecksAsDeletion() {
    var message = existingMessage("another");
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      assertThrows(SecurityException.class, () -> service.deletion("discussion1", "message", 0));
      verifyNoInteractions(linkedEvents);
      when(message.getCreatorId()).thenReturn("user");
      when(message.getVersion()).thenReturn(1L);
      assertThrows(jakarta.persistence.OptimisticLockException.class, () -> service.deletion("discussion1", "message", 0));
      verifyNoInteractions(linkedEvents);
      when(message.getBody()).thenReturn("body");
      when(linkedEvents.describe("body")).thenReturn(DiscussionEventDeletion.none());
      when(linkedIdeas.describe("body")).thenReturn(DiscussionMessageDeletion.messageOnly());
      when(linkedVotes.describe("body")).thenReturn(DiscussionMessageDeletion.messageOnly());
      assertEquals(DiscussionMessageDeletion.messageOnly(), service.deletion("discussion1", "message", 1));
      var idea = new DiscussionMessageDeletion("idea", true, "Une idée", false);
      when(linkedIdeas.describe("body")).thenReturn(idea);
      assertEquals(idea, service.deletion("discussion1", "message", 1));
      when(linkedIdeas.describe("body")).thenReturn(DiscussionMessageDeletion.messageOnly());
      var vote = new DiscussionMessageDeletion("vote", true, "Question", false);
      when(linkedVotes.describe("body")).thenReturn(vote);
      assertEquals(vote, service.deletion("discussion1", "message", 1));
      when(linkedEvents.describe("body")).thenReturn(new DiscussionEventDeletion(true, true, "Réunion", true));
      assertEquals(new DiscussionMessageDeletion("event", true, "Réunion", true), service.deletion("discussion1", "message", 1));
    }
  }

  @Test
  void linkedIdeaDeletionIsExplicitAndDeniedIdeaDeletionKeepsTheMessage() {
    var message = existingMessage("user");
    when(message.getBody()).thenReturn("idea body");
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      service.delete("discussion1", "message", 0);
      verifyNoInteractions(linkedIdeas);
      clearInvocations(message, messages);
      doThrow(SecurityException.class).when(linkedIdeas).delete("idea body");
      assertThrows(SecurityException.class, () -> service.delete("discussion1", "message", 0, false, true));
      verify(message, never()).delete();
      verify(messages, never()).save(any(DiscussionMessage.class));
      doNothing().when(linkedIdeas).delete("idea body");
      service.delete("discussion1", "message", 0, false, true);
      var order = inOrder(linkedIdeas, message);
      order.verify(linkedIdeas, atLeastOnce()).delete("idea body");
      order.verify(message).delete();
      assertThrows(IllegalArgumentException.class, () -> service.delete("discussion1", "message", 0, true, true));
    }
  }

  @Test
  void nativeVoteDeletionMustBeExplicitAndCannotBeCombinedWithAnotherContribution() {
    var message = existingMessage("user");
    when(message.getBody()).thenReturn("vote body");
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      doThrow(SecurityException.class).when(linkedVotes).delete("vote body");
      assertThrows(SecurityException.class, () -> service.delete("discussion1", "message", 0, false, false, true));
      verify(message, never()).delete();
      doNothing().when(linkedVotes).delete("vote body");
      service.delete("discussion1", "message", 0, false, false, true);
      verify(linkedVotes, times(2)).delete("vote body");
      verify(message).delete();
      assertThrows(IllegalArgumentException.class, () -> service.delete("discussion1", "message", 0, false, true, true));
    }
  }

  private DiscussionMessage existingMessage(String author) {
    var message = mock(DiscussionMessage.class);
    when(messages.getById("message")).thenReturn(message);
    when(message.getId()).thenReturn("message");
    when(message.getConversationId()).thenReturn("conversation");
    when(message.getCreatorId()).thenReturn(author);
    when(message.getVersion()).thenReturn(0L);
    return message;
  }
}
