/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.questionReply.model;

import com.stratelia.webactiv.persistence.*;
import com.stratelia.webactiv.beans.admin.*;

public class Recipient extends SilverpeasBean {
  private long questionId;
  private String userId;
  private static OrganizationController organizationController = new OrganizationController();

  public Recipient() {
  }

  public Recipient(String userId) {
    setUserId(userId);
  }

  public Recipient(long questionId, String userId) {
    setQuestionId(questionId);
    setUserId(userId);
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
    UserDetail userDetail = organizationController.getUserDetail(new Integer(
        getUserId()).toString());
    if (userDetail != null)
      name = userDetail.getLastName() + " " + userDetail.getFirstName();
    return name;
  }

  public String _getTableName() {
    return "SC_QuestionReply_Recipient";
  }

  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }

}
