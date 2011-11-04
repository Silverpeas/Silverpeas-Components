/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.resourcesmanager.model;

import java.io.Serializable;

public class ResourceReservableDetail implements Serializable {

  private static final long serialVersionUID = 1L;
  String categoryId;
  String resourceId;
  String categoryName;
  String resourceName;

  public String getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(String categoryId) {
    this.categoryId = categoryId;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public void setCategoryName(String categoryName) {
    this.categoryName = categoryName;
  }

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public String getResourceName() {
    return resourceName;
  }

  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }

  public ResourceReservableDetail(String categoryId, String resourceId,
          String categoryName, String resourceName) {
    super();
    this.categoryId = categoryId;
    this.resourceId = resourceId;
    this.categoryName = categoryName;
    this.resourceName = resourceName;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ResourceReservableDetail other = (ResourceReservableDetail) obj;
    if ((this.categoryId == null) ? (other.categoryId != null) : !this.categoryId.equals(
            other.categoryId)) {
      return false;
    }
    if ((this.resourceId == null) ? (other.resourceId != null) : !this.resourceId.equals(
            other.resourceId)) {
      return false;
    }
    if ((this.categoryName == null) ? (other.categoryName != null) : !this.categoryName.equals(
            other.categoryName)) {
      return false;
    }
    if ((this.resourceName == null) ? (other.resourceName != null) : !this.resourceName.equals(
            other.resourceName)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 43 * hash + (this.categoryId != null ? this.categoryId.hashCode() : 0);
    hash = 43 * hash + (this.resourceId != null ? this.resourceId.hashCode() : 0);
    hash = 43 * hash + (this.categoryName != null ? this.categoryName.hashCode() : 0);
    hash = 43 * hash + (this.resourceName != null ? this.resourceName.hashCode() : 0);
    return hash;
  }

  @Override
  public String toString() {
    return "ResourceReservableDetail{" + "categoryId=" + categoryId + ", resourceId=" + resourceId 
            + ", categoryName=" + categoryName + ", resourceName=" + resourceName + '}';
  }
}
