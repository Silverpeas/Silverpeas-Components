/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
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

import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.persistence.SilverpeasBean;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;

public class Recipient extends SilverpeasBean {
  private static final long serialVersionUID = 909658183117075174L;
  private long questionId;
  private String userId;
  private static OrganizationController organizationController = new OrganizationController();

  public Recipient() {
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
    UserDetail userDetail = organizationController.getUserDetail(String.valueOf(this.userId));
    if (userDetail != null) {
      name = userDetail.getLastName() + " " + userDetail.getFirstName();
    }
    return name;
  }

  @Override
  public String _getTableName() {
    return "SC_QuestionReply_Recipient";
  }

  @Override
  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }

}
