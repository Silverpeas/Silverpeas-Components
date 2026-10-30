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
package org.silverpeas.components.discussion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.SilverpeasJpaEntity;

/** One persistent reaction per message and authenticated user. */
@Entity
@Table(name = "sc_discussion_like", uniqueConstraints =
    @UniqueConstraint(columnNames = {"messageId", "userId"}))
public class DiscussionLike extends SilverpeasJpaEntity<DiscussionLike, UuidIdentifier> {
  private static final long serialVersionUID = 1L;
  @Column(nullable = false, length = 40, updatable = false)
  private String conversationId;
  @Column(nullable = false, length = 40, updatable = false)
  private String messageId;
  @Column(nullable = false, length = 40, updatable = false)
  private String userId;

  protected DiscussionLike() {
    // JPA constructor.
  }

  public DiscussionLike(String conversationId, String messageId, String userId) {
    this.conversationId = conversationId;
    this.messageId = messageId;
    this.userId = userId;
  }
}
