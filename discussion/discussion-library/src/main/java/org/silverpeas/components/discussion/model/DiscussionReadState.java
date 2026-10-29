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

/** Monotonic per-user reading position in a conversation. */
@Entity
@Table(name = "sc_discussion_read", uniqueConstraints =
    @UniqueConstraint(columnNames = {"conversationId", "userId"}))
public class DiscussionReadState extends SilverpeasJpaEntity<DiscussionReadState, UuidIdentifier> {
  private static final long serialVersionUID = 1L;
  @Column(nullable = false, length = 40, updatable = false)
  private String conversationId;
  @Column(nullable = false, length = 40, updatable = false)
  private String userId;
  @Column(nullable = false)
  private long lastReadSequence;

  protected DiscussionReadState() {
    // JPA constructor.
  }

  public DiscussionReadState(String conversationId, String userId) {
    this.conversationId = conversationId;
    this.userId = userId;
  }

  public long getLastReadSequence() { return lastReadSequence; }

  public void advanceTo(long sequence, long lastSequence) {
    if (sequence < 0 || sequence > lastSequence) {
      throw new IllegalArgumentException("Invalid reading position");
    }
    lastReadSequence = Math.max(lastReadSequence, sequence);
  }
}
