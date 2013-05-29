/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.whitePages.model;

import com.silverpeas.form.*;
import com.silverpeas.whitePages.record.*;
import com.stratelia.webactiv.persistence.*;

public class Card extends SilverpeasBean {

  private static final long serialVersionUID = -3513309887697109085L;
  private String userId;
  private int hideStatus = 0;
  private String instanceId;
  private String creationDate;
  private int creatorId;
  private boolean readOnly = true;
  private UserRecord userRecord;
  private DataRecord cardRecord;
  private Form userForm;
  private Form cardViewForm;
  private Form cardUpdateForm;

  public Card() {
  }

  public Card(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getUserId() {
    return this.userId;
  }

  public int getHideStatus() {
    return this.hideStatus;
  }

  public String getInstanceId() {
    return this.instanceId;
  }

  public boolean readReadOnly() {
    return this.readOnly;
  }

  public UserRecord readUserRecord() {
    return this.userRecord;
  }

  public DataRecord readCardRecord() {
    return this.cardRecord;
  }

  public Form readUserForm() {
    return this.userForm;
  }

  public Form readCardViewForm() {
    return this.cardViewForm;
  }

  public Form readCardUpdateForm() {
    return this.cardUpdateForm;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public void setHideStatus(int hideStatus) {
    this.hideStatus = hideStatus;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public void writeReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  public void writeUserRecord(UserRecord userRecord) {
    this.userRecord = userRecord;
    setUserId(userRecord.getId());
  }

  public void writeCardRecord(DataRecord cardRecord) {
    this.cardRecord = cardRecord;
  }

  public void writeUserForm(Form userForm) {
    this.userForm = userForm;
  }

  public void writeCardViewForm(Form cardViewForm) {
    this.cardViewForm = cardViewForm;
  }

  public void writeCardUpdateForm(Form cardUpdateForm) {
    this.cardUpdateForm = cardUpdateForm;
  }

  @Override
  public String _getTableName() {
    return "SC_WhitePages_Card";
  }

  @Override
  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }

  public void setCreationDate(String date) {
    this.creationDate = date;
  }

  public void setCreatorId(int creatorId) {
    this.creatorId = creatorId;
  }

  public String getCreationDate() {
    return this.creationDate;
  }

  public int getCreatorId() {
    return this.creatorId;
  }
}
