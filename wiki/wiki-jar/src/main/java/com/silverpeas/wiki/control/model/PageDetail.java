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
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.wiki.control.model;

public class PageDetail {

  int id = -1;
  String pageName = null;
  String instanceId = null;

  public PageDetail() {
  }

  public PageDetail(int id, String pageName, String instanceId) {
    this.id = id;
    this.pageName = pageName;
    this.instanceId = instanceId;
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

  /**
   * @return the id
   */
  public int getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * @return the pageName
   */
  public String getPageName() {
    return pageName;
  }

  /**
   * @param pageName the pageName to set
   */
  public void setPageName(String pageName) {
    this.pageName = pageName;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 37 * hash + this.id;
    hash = 37 * hash + (this.pageName != null ? this.pageName.hashCode() : 0);
    hash = 37 * hash + (this.instanceId != null ? this.instanceId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final PageDetail other = (PageDetail) obj;
    if (this.id != other.id) {
      return false;
    }
    if ((this.pageName == null) ? (other.pageName != null) : !this.pageName.equals(other.pageName)) {
      return false;
    }
    if ((this.instanceId == null) ? (other.instanceId != null) : !this.instanceId
        .equals(other.instanceId)) {
      return false;
    }
    return true;
  }
}
