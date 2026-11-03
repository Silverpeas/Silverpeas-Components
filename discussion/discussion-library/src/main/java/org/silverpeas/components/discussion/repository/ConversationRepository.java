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

import jakarta.persistence.LockModeType;
import org.silverpeas.components.discussion.model.Conversation;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.datasource.repository.jpa.SilverpeasJpaEntityRepository;

@Repository
public class ConversationRepository extends SilverpeasJpaEntityRepository<Conversation> {
  @jakarta.inject.Inject
  private org.silverpeas.components.discussion.service.DiscussionChannelContext context;

  public Conversation current(String instanceId, boolean lock) {
    String scope = context.getScope();
    return scope == null || "main".equals(scope) ? main(instanceId, lock) : channel(instanceId, scope, lock);
  }

  public java.util.List<Conversation> channels(String instanceId) {
    return getEntityManager().createQuery(
        "select c from Conversation c where c.componentInstanceId = :instance order by c.scopeKey",
        Conversation.class).setParameter("instance", instanceId).getResultList();
  }

  public Conversation forMessage(String instanceId, String messageId) {
    return getEntityManager().createQuery("""
        select c from Conversation c, DiscussionMessage m where m.id.id = :message
        and m.conversationId = c.id.id and c.componentInstanceId = :instance
        """, Conversation.class).setParameter("message", messageId).setParameter("instance", instanceId)
        .getResultStream().findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown discussion message"));
  }

  public Conversation main(String instanceId, boolean lock) {
    return channel(instanceId, "main", lock);
  }

  public Conversation channel(String instanceId, String scope, boolean lock) {
    var query = getEntityManager().createQuery(
        "select c from Conversation c where c.componentInstanceId = :instance and c.scopeKey = :scope",
        Conversation.class).setParameter("instance", instanceId).setParameter("scope", scope);
    if (lock) {
      query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
    }
    return query.getResultStream().findFirst().orElseThrow(
        () -> new IllegalArgumentException("Discussion instance not initialized"));
  }
}
