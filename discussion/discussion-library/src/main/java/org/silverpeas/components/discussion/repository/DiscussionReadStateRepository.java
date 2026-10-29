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

import java.util.Optional;
import org.silverpeas.components.discussion.model.DiscussionReadState;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.datasource.repository.jpa.SilverpeasJpaEntityRepository;

@Repository
public class DiscussionReadStateRepository extends SilverpeasJpaEntityRepository<DiscussionReadState> {
  public Optional<DiscussionReadState> find(String conversationId, String userId) {
    return getEntityManager().createQuery("""
        select r from DiscussionReadState r where r.conversationId = :conversation and r.userId = :user
        """, DiscussionReadState.class).setParameter("conversation", conversationId)
        .setParameter("user", userId).getResultStream().findFirst();
  }

  public void deleteConversation(String conversationId) {
    getEntityManager().createQuery(
        "delete from DiscussionReadState r where r.conversationId = :conversation")
        .setParameter("conversation", conversationId).executeUpdate();
  }
}
