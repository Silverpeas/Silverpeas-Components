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
import org.silverpeas.components.discussion.model.DiscussionMessage;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.datasource.repository.jpa.SilverpeasJpaEntityRepository;

@Repository
public class DiscussionMessageRepository extends SilverpeasJpaEntityRepository<DiscussionMessage> {
  public List<DiscussionMessage> pinned(String conversationId) {
    return getEntityManager().createQuery("""
        select m from DiscussionMessage m where m.conversationId = :conversation
        and m.pinned = true and m.deleted = false order by m.sequenceNumber desc
        """, DiscussionMessage.class).setParameter("conversation", conversationId).getResultList();
  }

  public List<String> fileMessageIds(String conversationId) {
    return getEntityManager().createQuery("""
        select m.id.id from DiscussionMessage m where m.conversationId = :conversation
        and m.filesAttached = true and m.deleted = false order by m.sequenceNumber desc
        """, String.class).setParameter("conversation", conversationId).getResultList();
  }

  public List<String> authorIds(String conversationId) {
    return getEntityManager().createQuery("""
        select distinct m.creatorId from DiscussionMessage m
        where m.conversationId = :conversation and m.deleted = false order by m.creatorId
        """, String.class).setParameter("conversation", conversationId).getResultList();
  }

  public List<DiscussionMessage> history(String conversationId, long before, int limit) {
    return getEntityManager().createQuery("""
        select m from DiscussionMessage m where m.conversationId = :conversation
        and m.sequenceNumber < :before order by m.sequenceNumber desc
        """, DiscussionMessage.class).setParameter("conversation", conversationId)
        .setParameter("before", before).setMaxResults(limit).getResultList();
  }

  public List<DiscussionMessage> newer(String conversationId, long after) {
    return getEntityManager().createQuery("""
        select m from DiscussionMessage m where m.conversationId = :conversation
        and m.sequenceNumber > :after order by m.sequenceNumber asc
        """, DiscussionMessage.class).setParameter("conversation", conversationId)
        .setParameter("after", after).setMaxResults(100).getResultList();
  }

  public List<DiscussionMessage> selected(String conversationId, List<String> ids) {
    if (ids.isEmpty()) {
      return List.of();
    }
    return getEntityManager().createQuery("""
        select m from DiscussionMessage m where m.conversationId = :conversation
        and m.id.id in :ids
        """, DiscussionMessage.class).setParameter("conversation", conversationId)
        .setParameter("ids", ids).getResultList();
  }

  public java.util.stream.Stream<String> publicationLinkBodies(String conversationId) {
    return getEntityManager().createQuery("""
        select m.body from DiscussionMessage m where m.conversationId = :conversation
        and m.deleted = false and (lower(m.body) like '%/publication/%'
        or lower(m.body) like '%/rkmelia/%' or lower(m.body) like '%/rtoolbox/%'
        or lower(m.body) like '%/rkmax/%')
        """, String.class).setParameter("conversation", conversationId).getResultStream();
  }

  public long unread(String conversationId, String userId, long lastRead) {
    return getEntityManager().createQuery("""
        select count(m) from DiscussionMessage m where m.conversationId = :conversation
        and m.sequenceNumber > :lastRead and m.creatorId <> :user and m.deleted = false
        and not exists (select s from DiscussionSeen s where s.messageId = m.id.id and s.userId = :user)
        """, Long.class).setParameter("conversation", conversationId)
        .setParameter("lastRead", lastRead).setParameter("user", userId).getSingleResult();
  }

  public void deleteConversation(String conversationId) {
    getEntityManager().createQuery(
        "delete from DiscussionMessage m where m.conversationId = :conversation")
        .setParameter("conversation", conversationId).executeUpdate();
  }
}
