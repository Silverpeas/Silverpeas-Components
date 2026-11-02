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

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Objects;
import java.util.HashMap;
import java.util.List;
import org.silverpeas.components.discussion.model.DiscussionMessage;
import org.silverpeas.components.discussion.repository.ConversationRepository;
import org.silverpeas.components.discussion.repository.DiscussionMessageRepository;
import org.silverpeas.components.discussion.service.DiscussionAccess;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.index.search.SearchEngine;
import org.silverpeas.core.index.search.model.ParseException;
import org.silverpeas.core.index.search.model.QueryDescription;

/** Search is always confined to the authenticated user's current discussion. */
@Service
@Transactional
public class DiscussionSearchService {
  private static final int PAGE_SIZE = 20;
  @Inject
  private DiscussionAccess access;
  @Inject
  private ConversationRepository conversations;
  @Inject
  private DiscussionMessageRepository messages;
  @Inject
  private SearchEngine engine;

  public DiscussionSearchPage search(String instanceId, String text, int offset) {
    access.require(instanceId, SilverpeasRole.WRITER);
    if (text == null || text.isBlank()) {
      return new DiscussionSearchPage(List.of(), 0, 0);
    }
    if (text.length() > 200 || offset < 0) {
      throw new IllegalArgumentException("Invalid discussion search");
    }
    var query = new QueryDescription(text.strip());
    query.addComponent(instanceId);
    query.setSearchingUser(User.getCurrentRequester().getId());
    try {
      var ids = engine.search(query).getEntries().stream()
          .filter(entry -> instanceId.equals(entry.getComponent())
              && DiscussionMessageIndex.TYPE.equals(entry.getObjectType()))
          .map(entry -> entry.getObjectId()).distinct().toList();
      var conversation = conversations.main(instanceId, false);
      var existing = new HashMap<String, DiscussionMessage>();
      // Bound SQL IN clauses, including on Oracle, and discard stale/deleted index hits.
      for (int start = 0; start < ids.size(); start += 200) {
        messages.selected(conversation.getId(), ids.subList(start, Math.min(start + 200, ids.size())))
            .stream().filter(message -> !message.isDeleted())
            .forEach(message -> existing.put(message.getId(), message));
      }
      var ordered = ids.stream().map(existing::get).filter(Objects::nonNull).toList();
      int from = Math.min(offset, ordered.size());
      return new DiscussionSearchPage(ordered.subList(from, Math.min(from + PAGE_SIZE, ordered.size())),
          ordered.size(), from);
    } catch (ParseException e) {
      throw new IllegalArgumentException("Invalid search expression", e);
    }
  }
}
