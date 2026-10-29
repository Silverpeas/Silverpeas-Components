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

/** A conversation container; the application currently creates only the main one per instance. */
@Entity
@Table(name = "sc_discussion_conversation", uniqueConstraints =
    @UniqueConstraint(columnNames = {"instanceId", "scopeKey"}))
public class Conversation extends SilverpeasJpaEntity<Conversation, UuidIdentifier> {
  private static final long serialVersionUID = 1L;
  @Column(name = "instanceId", nullable = false, length = 30)
  private String componentInstanceId;
  @Column(nullable = false, length = 40)
  private String scopeKey;
  @Column(nullable = false)
  private long lastSequence;

  protected Conversation() {
    // JPA constructor.
  }

  public Conversation(String componentInstanceId) {
    this.componentInstanceId = componentInstanceId;
    this.scopeKey = "main";
  }

  public String getComponentInstanceId() { return componentInstanceId; }
  public long getLastSequence() { return lastSequence; }

  /** Must be called with the conversation locked in the current transaction. */
  public long nextSequence() { return ++lastSequence; }
}
