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

import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import org.silverpeas.components.discussion.model.DiscussionMessage;
import org.silverpeas.components.discussion.repository.ConversationRepository;
import org.silverpeas.components.discussion.repository.DiscussionMessageRepository;
import org.silverpeas.components.discussion.service.DiscussionMentionService;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;

/** Native index lifecycle, shared by incremental updates and administration reindexing. */
@Service
public class DiscussionMessageIndex {
  public static final String TYPE = "DiscussionMessage";
  private static final int BATCH_SIZE = 200;
  @Inject
  private Event<DiscussionIndexEvent> events;
  @Inject
  private ConversationRepository conversations;
  @Inject
  private DiscussionMessageRepository messages;
  @Inject
  private IndexEngineProxy index;

  public void schedule(String instanceId, String messageId) {
    events.fire(new DiscussionIndexEvent(instanceId, messageId));
  }

  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public void afterCommit(@Observes(during = TransactionPhase.AFTER_SUCCESS) DiscussionIndexEvent event) {
    var conversation = conversations.main(event.instanceId(), false);
    var found = messages.selected(conversation.getId(), List.of(event.messageId()));
    if (found.isEmpty()) {
      index.delete(new IndexEntryKey(event.instanceId(), TYPE, event.messageId()));
    } else {
      indexMessage(event.instanceId(), found.getFirst());
    }
  }

  /** Called by Silverpeas administration with its already checked component scope. */
  @Transactional
  public void reindex(String instanceId) {
    var conversation = conversations.main(instanceId, false);
    long before = Long.MAX_VALUE;
    List<DiscussionMessage> batch;
    do {
      batch = messages.allHistory(conversation.getId(), before, BATCH_SIZE);
      batch.forEach(message -> indexMessage(instanceId, message));
      if (!batch.isEmpty()) {
        before = batch.getLast().getSequenceNumber();
      }
    } while (batch.size() == BATCH_SIZE);
  }

  private void indexMessage(String instanceId, DiscussionMessage message) {
    var key = new IndexEntryKey(instanceId, TYPE, message.getId());
    if (message.isDeleted()) {
      index.delete(key);
      return;
    }
    String text = DiscussionMentionService.displayText(message.getBody());
    var entry = new FullIndexEntry(key);
    entry.setTitle(text.isBlank() ? TYPE : text.substring(0,
        text.offsetByCodePoints(0, Math.min(text.codePointCount(0, text.length()), 120))));
    entry.setPreview(text);
    entry.addTextContent(text);
    entry.setCreationDate(message.getCreationDate());
    entry.setCreationUser(message.getCreatorId());
    entry.setLastModificationDate(message.getLastUpdateDate());
    entry.setLastModificationUser(message.getLastUpdaterId());
    index.add(entry);
  }
}
