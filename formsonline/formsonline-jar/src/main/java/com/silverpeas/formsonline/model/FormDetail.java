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

package com.silverpeas.formsonline.model;

import java.util.Date;

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
    return creationDate;
  }

  /**
   * @param creationDate the creationDate to set
   */
  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
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
    if ((this.instanceId == null) ? (other.instanceId != null) : !this.instanceId
        .equals(other.instanceId)) {
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
    if ((this.xmlFormName == null) ? (other.xmlFormName != null) : !this.xmlFormName
        .equals(other.xmlFormName)) {
      return false;
    }
    if ((this.description == null) ? (other.description != null) : !this.description
        .equals(other.description)) {
      return false;
    }
    if (this.state != other.state) {
      return false;
    }

    return true;
  }
}
