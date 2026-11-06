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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.questionreply.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.persistence.datasource.model.identifier.UniqueIntegerIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;

/**
 * A user to whom a question of a FAQ is addressed.
 */
@Entity
@Table(name = "SC_QuestionReply_Recipient")
@NamedQuery(name = "Recipient.findByQuestion",
    query = "SELECT r FROM Recipient r WHERE r.questionId = :questionId ORDER BY r.id.id")
@NamedQuery(name = "Recipient.deleteByQuestion",
    query = "DELETE FROM Recipient r WHERE r.questionId = :questionId")
@SuppressWarnings("unused")
public class Recipient extends BasicJpaEntity<Recipient, UniqueIntegerIdentifier> {
  private static final long serialVersionUID = 909658183117075174L;
  @Column(nullable = false)
  private long questionId;
  @Column(nullable = false)
  private String userId;

  public Recipient() {
    // required by JPA
  }

  public Recipient(String userId) {
    this.userId = userId;
  }

  public Recipient(long questionId, String userId) {
    this.questionId = questionId;
    this.userId = userId;
  }

  public long getQuestionId() {
    return questionId;
  }

  public String getUserId() {
    return this.userId;
  }

  public void setQuestionId(long questionId) {
    this.questionId = questionId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String readRecipientName() {
    String name = null;
    UserDetail userDetail = UserDetail.getById(String.valueOf(this.userId));
    if (userDetail != null) {
      name = userDetail.getLastName() + " " + userDetail.getFirstName();
    }
    return name;
  }
}
