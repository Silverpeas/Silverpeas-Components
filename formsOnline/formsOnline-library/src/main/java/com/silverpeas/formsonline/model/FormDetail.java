/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.formsonline.model;

import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FormDetail {
  public static final int STATE_NOT_YET_PUBLISHED = 0;
  public static final int STATE_PUBLISHED = 1;
  public static final int STATE_UNPUBLISHED = 2;

  private int id = -1;
  private String xmlFormName = null;
  private String name = "";
  private String description = "";
  private String title = "";
  private String creatorId = null;
  private Date creationDate = new Date();
  private String instanceId = null;
  private boolean alreadyUsed = false;
  private int state = STATE_NOT_YET_PUBLISHED;

  private transient boolean sendable = true;

  List<UserDetail> sendersAsUsers;
  List<Group> sendersAsGroups;
  List<UserDetail> receiversAsUsers;
  List<Group> receiversAsGroups;

  /**
   * @return the id
   */
  public int getId() {
    return id;
  }

  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return the state
   */
  public int getState() {
    return state;
  }

  /**
   * @param state the state to set
   */
  public void setState(int state) {
    this.state = state;
  }

  /**
   * @return the creationDate
   */
  public Date getCreationDate() {
    return creationDate != null ? (Date) creationDate.clone() : null;
  }

  /**
   * @param creationDate the creationDate to set
   */
  public void setCreationDate(Date creationDate) {
    this.creationDate = (creationDate != null ? (Date) creationDate.clone() : null);
  }

  /**
   * @param id the id to set
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * @return the xmlFormName
   */
  public String getXmlFormName() {
    return xmlFormName;
  }

  /**
   * @param xmlFormName the xmlFormName to set
   */
  public void setXmlFormName(String xmlFormName) {
    this.xmlFormName = xmlFormName;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the creatorId
   */
  public String getCreatorId() {
    return creatorId;
  }

  /**
   * @param creatorId the creatorId to set
   */
  public void setCreatorId(String creatorId) {
    this.creatorId = creatorId;
  }

  /**
   * @return the instanceId
   */
  public String getInstanceId() {
    return instanceId;
  }

  /**
   * @param instanceId the instanceId to set
   */
  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public void setAlreadyUsed(boolean alreadyUsed) {
    this.alreadyUsed = alreadyUsed;
  }

  public boolean isAlreadyUsed() {
    return alreadyUsed;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final FormDetail other = (FormDetail) obj;
    if (this.id != other.id) {
      return false;
    }
    if ((this.instanceId == null) ? (other.instanceId != null) :
        !this.instanceId.equals(other.instanceId)) {
      return false;
    }
    if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
      return false;
    }
    if ((this.title == null) ? (other.title != null) : !this.title.equals(other.title)) {
      return false;
    }
    if (this.creationDate != other.creationDate &&
        (this.creationDate == null || !this.creationDate.equals(other.creationDate))) {
      return false;
    }
    if (this.alreadyUsed != other.alreadyUsed) {
      return false;
    }
    if ((this.xmlFormName == null) ? (other.xmlFormName != null) :
        !this.xmlFormName.equals(other.xmlFormName)) {
      return false;
    }
    if ((this.description == null) ? (other.description != null) :
        !this.description.equals(other.description)) {
      return false;
    }
    return this.state == other.state;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(7, 17).append(this.id).append(this.xmlFormName).append(this.name)
        .append(this.description).append(this.title).append(this.creatorId).append(this.instanceId)
        .append(this.alreadyUsed).append(this.state).toHashCode();
  }

  public boolean isPublished() {
    return getState() == STATE_PUBLISHED;
  }

  public boolean isUnpublished() {
    return getState() == STATE_UNPUBLISHED;
  }

  public boolean isNotYetPublished() {
    return getState() == STATE_NOT_YET_PUBLISHED;
  }

  public void setSendable(boolean sendable) {
    this.sendable = sendable;
  }

  public boolean isSendable() {
    return sendable;
  }

  public boolean isSender(String userId) {
    return isInList(userId, getAllSenders());
  }

  public boolean isValidator(String userId) {
    return isInList(userId, getAllReceivers());
  }

  public FormPK getPK() {
    return new FormPK(Integer.toString(getId()), getInstanceId());
  }

  private boolean isInList(String userId, List<UserDetail> users) {
    for (UserDetail user : users) {
      if (user.getId().equals(userId)) {
        return true;
      }
    }
    return false;
  }

  private List<UserDetail> getAllSenders() {
    List<UserDetail> senders = getSendersAsUsers();
    senders.addAll(getUsers(getSendersAsGroups()));
    return senders;
  }

  private List<UserDetail> getAllReceivers() {
    List<UserDetail> users = getReceiversAsUsers();
    users.addAll(getUsers(getReceiversAsGroups()));
    return users;
  }

  private List<UserDetail> getUsers(List<Group> groups) {
    List<UserDetail> users = new ArrayList<UserDetail>();
    for (Group group : groups) {
      for (UserDetail user : group.getAllUsers()) {
        users.add(user);
      }
    }
    return users;
  }

  public List<UserDetail> getSendersAsUsers() {
    if (sendersAsUsers == null) {
      return new ArrayList<UserDetail>();
    }
    return sendersAsUsers;
  }

  public void setSendersAsUsers(final List<UserDetail> sendersAsUsers) {
    this.sendersAsUsers = sendersAsUsers;
  }

  public List<Group> getSendersAsGroups() {
    if (sendersAsGroups == null) {
      return new ArrayList<Group>();
    }
    return sendersAsGroups;
  }

  public void setSendersAsGroups(final List<Group> sendersAsGroups) {
    this.sendersAsGroups = sendersAsGroups;
  }

  public List<UserDetail> getReceiversAsUsers() {
    if (receiversAsUsers == null) {
      return new ArrayList<UserDetail>();
    }
    return receiversAsUsers;
  }

  public void setReceiversAsUsers(final List<UserDetail> receiversAsUsers) {
    this.receiversAsUsers = receiversAsUsers;
  }

  public List<Group> getReceiversAsGroups() {
    if (receiversAsGroups == null) {
      return new ArrayList<Group>();
    }
    return receiversAsGroups;
  }

  public void setReceiversAsGroups(final List<Group> receiversAsGroups) {
    this.receiversAsGroups = receiversAsGroups;
  }
}