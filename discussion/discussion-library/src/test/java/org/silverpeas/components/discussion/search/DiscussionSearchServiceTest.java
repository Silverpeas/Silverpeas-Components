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
package org.silverpeas.components.discussion.search;

import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.silverpeas.components.discussion.model.Conversation;
import org.silverpeas.components.discussion.model.DiscussionMessage;
import org.silverpeas.components.discussion.repository.ConversationRepository;
import org.silverpeas.components.discussion.repository.DiscussionMessageRepository;
import org.silverpeas.components.discussion.service.DiscussionAccess;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.index.search.PlainSearchResult;
import org.silverpeas.core.index.search.SearchEngine;
import org.silverpeas.core.index.search.model.MatchingIndexEntry;
import org.silverpeas.core.index.search.model.ParseException;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class DiscussionSearchServiceTest {
  @TestManagedMock
  private DiscussionAccess access;
  @TestManagedMock
  private ConversationRepository conversations;
  @TestManagedMock
  private DiscussionMessageRepository messages;
  @TestManagedMock
  private SearchEngine engine;

  @TestedBean
  private DiscussionSearchService service;

  @BeforeEach
  void prepare() {
    var conversation = mock(Conversation.class);
    when(conversation.getId()).thenReturn("conversation1");
    when(conversations.main("discussion1", false)).thenReturn(conversation);
  }

  @Test
  void nativeSearchIsScopedToCurrentUserAndInstanceAndRechecksLiveRows() throws Exception {
    when(engine.search(any())).thenReturn(new PlainSearchResult(List.of(), List.of(
        hit("discussion1", "DiscussionMessage", "second"),
        hit("discussion2", "DiscussionMessage", "other-instance"),
        hit("discussion1", "attachment", "wrong-type"),
        hit("discussion1", "DiscussionMessage", "deleted"),
        hit("discussion1", "DiscussionMessage", "missing"),
        hit("discussion1", "DiscussionMessage", "first"),
        hit("discussion1", "DiscussionMessage", "second"))));
    var deleted = message("deleted");
    when(deleted.isDeleted()).thenReturn(true);
    var first = message("first");
    var second = message("second");
    when(messages.selected(eq("conversation1"), anyList())).thenReturn(List.of(first, deleted, second));
    try (var users = mockStatic(User.class)) {
      var user = mock(User.class);
      when(user.getId()).thenReturn("42");
      users.when(User::getCurrentRequester).thenReturn(user);
      var page = service.search("discussion1", "  projet  ", 0);
      assertEquals(List.of("second", "first"), page.messages().stream().map(DiscussionMessage::getId).toList());
      assertEquals(2, page.total());
      assertFalse(page.isHasMore());
      var query = ArgumentCaptor.forClass(QueryDescription.class);
      verify(engine).search(query.capture());
      assertEquals(java.util.Set.of("discussion1"), query.getValue().getWhereToSearch());
      assertEquals("42", query.getValue().getSearchingUser());
      assertEquals("projet", query.getValue().getQuery());
      assertFalse(query.getValue().isAdminScope());
      verify(messages).selected("conversation1", List.of("second", "deleted", "missing", "first"));
    }
  }

  @Test
  void resultsArePaginatedInNativeRelevanceOrderAndDatabaseQueriesAreBounded() throws Exception {
    var hits = IntStream.range(0, 205).mapToObj(i -> hit("discussion1", "DiscussionMessage", "m" + i)).toList();
    when(engine.search(any())).thenReturn(new PlainSearchResult(List.of(), hits));
    when(messages.selected(eq("conversation1"), anyList())).thenAnswer(call -> {
      List<String> ids = call.getArgument(1);
      assertTrue(ids.size() <= 200);
      return ids.stream().map(this::message).toList();
    });
    try (var users = mockStatic(User.class)) {
      var user = mock(User.class); when(user.getId()).thenReturn("42");
      users.when(User::getCurrentRequester).thenReturn(user);
      var page = service.search("discussion1", "projet", 20);
      assertEquals(205, page.getTotal());
      assertEquals(20, page.getMessages().size());
      assertEquals("m20", page.getMessages().getFirst().getId());
      assertEquals(40, page.getNextOffset());
      assertTrue(page.isHasMore());
      verify(messages, times(2)).selected(eq("conversation1"), anyList());
    }
  }

  @Test
  void permissionsAreCheckedBeforeSearchingAndInvalidQueriesAreRejected() throws Exception {
    doThrow(SecurityException.class).when(access).require("discussion2", SilverpeasRole.WRITER);
    assertThrows(SecurityException.class, () -> service.search("discussion2", "x", 0));
    assertThrows(IllegalArgumentException.class, () -> service.search("discussion1", "x".repeat(201), 0));
    assertThrows(IllegalArgumentException.class, () -> service.search("discussion1", "x", -1));
    assertEquals(0, service.search("discussion1", " ", 0).total());
    verifyNoInteractions(engine, messages);
    when(engine.search(any())).thenThrow(new ParseException("Invalid expression"));
    try (var users = mockStatic(User.class)) {
      var user = mock(User.class); when(user.getId()).thenReturn("42");
      users.when(User::getCurrentRequester).thenReturn(user);
      assertThrows(IllegalArgumentException.class, () -> service.search("discussion1", "\"", 0));
    }
  }

  private DiscussionMessage message(String id) {
    var message = mock(DiscussionMessage.class);
    when(message.getId()).thenReturn(id);
    return message;
  }

  private MatchingIndexEntry hit(String instance, String type, String id) {
    return new MatchingIndexEntry(new IndexEntryKey(instance, type, id));
  }
}
