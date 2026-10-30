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
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.SilverpeasJpaEntity;

/** Dedicated plain-text message. A deletion leaves a cursor-preserving tombstone. */
@Entity
@Table(name = "sc_discussion_message", uniqueConstraints =
    @UniqueConstraint(columnNames = {"conversationId", "sequenceNumber"}))
public class DiscussionMessage extends SilverpeasJpaEntity<DiscussionMessage, UuidIdentifier> {
  private static final long serialVersionUID = 1L;
  public static final int MAX_BODY_LENGTH = 2000;
  @Column(nullable = false, length = 40, updatable = false)
  private String conversationId;
  @Column(nullable = false, updatable = false)
  private long sequenceNumber;
  @Column(nullable = false, length = MAX_BODY_LENGTH)
  private String body;
  @Column(nullable = false)
  private boolean deleted;

  @Transient
  private boolean unread;

  public boolean isUnread() { return unread; }
  public void setUnread(boolean unread) { this.unread = unread; }

  protected DiscussionMessage() {
    // JPA constructor.
  }

  public DiscussionMessage(String conversationId, long sequenceNumber, String body) {
    this.conversationId = conversationId;
    this.sequenceNumber = sequenceNumber;
    edit(body);
  }

  public String getConversationId() { return conversationId; }
  public long getSequenceNumber() { return sequenceNumber; }
  public String getBody() { return body; }
  public boolean isDeleted() { return deleted; }

  public void edit(String text) {
    if (deleted || text == null || text.isBlank() || text.length() > MAX_BODY_LENGTH) {
      throw new IllegalArgumentException("Invalid discussion message");
    }
    body = text.strip();
  }

  public void delete() {
    body = " ";
    deleted = true;
  }
}
