/*
 * Copyright (C) 2000 - 2024 Silverpeas
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

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBean;
import org.silverpeas.kernel.annotation.NonNull;

@SuppressWarnings({"deprecation", "unused"})
public class Recipient extends SilverpeasBean {
  private static final long serialVersionUID = 909658183117075174L;
  private long questionId;
  private String userId;

  public Recipient() {
    // required by the org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAO
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

  @Override
  @NonNull
  protected String getTableName() {
    return "SC_QuestionReply_Recipient";
  }

}
