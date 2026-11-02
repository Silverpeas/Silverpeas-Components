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

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.silverpeas.components.discussion.model.Conversation;
import org.silverpeas.components.discussion.model.DiscussionMessage;
import org.silverpeas.components.discussion.repository.ConversationRepository;
import org.silverpeas.components.discussion.repository.DiscussionMessageRepository;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class DiscussionMessageIndexTest {
  @TestManagedMock
  private ConversationRepository conversations;
  @TestManagedMock
  private DiscussionMessageRepository messages;
  @TestManagedMock
  private IndexEngineProxy index;

  @TestedBean
  private DiscussionMessageIndex service;

  @BeforeEach
  void prepare() {
    var conversation = mock(Conversation.class);
    when(conversation.getId()).thenReturn("c1");
    when(conversations.main("discussion1", false)).thenReturn(conversation);
  }

  @Test
  void committedMessageIsRereadAndIndexesReadableMentionsAndMetadata() {
    var message = message("m1", 1);
    when(message.getBody()).thenReturn("@[Alice](2) a corrigé <le projet>");
    when(message.getCreationDate()).thenReturn(new Date(1000));
    when(message.getLastUpdateDate()).thenReturn(new Date(2000));
    when(message.getCreatorId()).thenReturn("42");
    when(message.getLastUpdaterId()).thenReturn("43");
    when(messages.selected("c1", List.of("m1"))).thenReturn(List.of(message));
    service.afterCommit(new DiscussionIndexEvent("discussion1", "m1"));
    var entry = ArgumentCaptor.forClass(FullIndexEntry.class);
    verify(index).add(entry.capture());
    assertEquals(new IndexEntryKey("discussion1", "DiscussionMessage", "m1"), entry.getValue().getPK());
    assertEquals("@Alice a corrigé <le projet>", entry.getValue().getTextContentList().getFirst().getContent());
    assertEquals("@Alice a corrigé <le projet>", entry.getValue().getPreview());
    assertEquals("42", entry.getValue().getCreationUser());
    assertEquals("43", entry.getValue().getLastModificationUser());
  }

  @Test
  void deletedOrMissingMessageRemovesItsNativeIndexEntry() {
    var deleted = message("m1", 1);
    when(deleted.isDeleted()).thenReturn(true);
    when(messages.selected("c1", List.of("m1"))).thenReturn(List.of(deleted), List.of());
    service.afterCommit(new DiscussionIndexEvent("discussion1", "m1"));
    service.afterCommit(new DiscussionIndexEvent("discussion1", "m1"));
    verify(index, times(2)).delete(new IndexEntryKey("discussion1", "DiscussionMessage", "m1"));
    verify(index, never()).add(any());
  }

  @Test
  void nativeReindexTraversesOlderBatchesAndRemovesTombstones() {
    var batch = java.util.stream.IntStream.range(0, 200).mapToObj(i -> message("m" + i, 300 - i)).toList();
    var deleted = message("old", 99);
    when(deleted.isDeleted()).thenReturn(true);
    when(messages.history("c1", Long.MAX_VALUE, 200)).thenReturn(batch);
    when(messages.history("c1", 101, 200)).thenReturn(List.of(deleted));
    service.reindex("discussion1");
    verify(index, times(200)).add(any());
    verify(index).delete(new IndexEntryKey("discussion1", "DiscussionMessage", "old"));
  }

  @Test
  void indexObserverOnlyRunsAfterSuccessfulCommitInFreshTransaction() throws Exception {
    var method = DiscussionMessageIndex.class.getMethod("afterCommit", DiscussionIndexEvent.class);
    assertEquals(TransactionPhase.AFTER_SUCCESS, method.getParameters()[0].getAnnotation(Observes.class).during());
    assertEquals(Transactional.TxType.REQUIRES_NEW, method.getAnnotation(Transactional.class).value());
  }

  private DiscussionMessage message(String id, long sequence) {
    var message = mock(DiscussionMessage.class);
    when(message.getId()).thenReturn(id);
    when(message.getSequenceNumber()).thenReturn(sequence);
    when(message.getBody()).thenReturn("Hello");
    return message;
  }
}
