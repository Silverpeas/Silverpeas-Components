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
package org.silverpeas.components.discussion.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.silverpeas.components.discussion.model.DiscussionLike;
import org.silverpeas.components.discussion.service.LikeState;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.datasource.repository.jpa.SilverpeasJpaEntityRepository;

@Repository
public class DiscussionLikeRepository extends SilverpeasJpaEntityRepository<DiscussionLike> {
  public Optional<DiscussionLike> find(String messageId, String userId) {
    return getEntityManager().createQuery("""
        select l from DiscussionLike l where l.messageId = :message and l.userId = :user
        """, DiscussionLike.class).setParameter("message", messageId).setParameter("user", userId)
        .getResultStream().findFirst();
  }

  public Map<String, LikeState> states(String conversationId, List<String> messageIds, String userId) {
    if (messageIds.isEmpty()) {
      return Map.of();
    }
    return getEntityManager().createQuery("""
        select l.messageId, count(l), sum(case when l.userId = :user then 1 else 0 end)
        from DiscussionLike l where l.conversationId = :conversation and l.messageId in :messages
        group by l.messageId
        """, Object[].class).setParameter("user", userId).setParameter("conversation", conversationId)
        .setParameter("messages", messageIds).getResultStream().collect(Collectors.toMap(
            row -> (String) row[0], row -> new LikeState(((Number) row[1]).longValue(),
                ((Number) row[2]).longValue() > 0)));
  }

  public void deleteMessage(String messageId) {
    getEntityManager().createQuery("delete from DiscussionLike l where l.messageId = :message")
        .setParameter("message", messageId).executeUpdate();
  }

  public void deleteConversation(String conversationId) {
    getEntityManager().createQuery("delete from DiscussionLike l where l.conversationId = :conversation")
        .setParameter("conversation", conversationId).executeUpdate();
  }
}
