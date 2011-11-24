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
import java.util.Date;
import java.util.List;

public class CategoryDetail implements Serializable {

  private static final long serialVersionUID = 1L;
  private String id;
  private String instanceId;
  private String name;
  private Date creationDate;
  private Date updateDate;
  private boolean bookable;
  private String form;
  private String responsibleId;
  private String createrId;
  private String updaterId;
  private String description;
  private List<ResourceDetail> resources;

  public boolean getBookable() {
    return bookable;
  }

  public void setBookable(boolean bookable) {
    this.bookable = bookable;
  }

  public String getCreaterId() {
    return createrId;
  }

  public void setCreaterId(String createrId) {
    this.createrId = createrId;
  }

  public String getForm() {
    return form;
  }

  public void setForm(String form) {
    this.form = form;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<ResourceDetail> getResources() {
    return resources;
  }

  public void setResources(List<ResourceDetail> resources) {
    this.resources = resources;
  }

  public String getResponsibleId() {
    return responsibleId;
  }

  public void setResponsibleId(String responsibleId) {
    this.responsibleId = responsibleId;
  }

  public String getUpdaterId() {
    return updaterId;
  }

  public void setUpdaterId(String updaterId) {
    this.updaterId = updaterId;
  }

  public CategoryDetail(String id, String instanceId, String name,
          Date creationDate, Date updateDate, boolean bookable, String form,
          String responsibleId, String createrId, String updaterId,
          String description, List<ResourceDetail> resources) {
    super();
    this.id = id;
    this.instanceId = instanceId;
    this.name = name;
    this.creationDate = creationDate;
    this.updateDate = updateDate;
    this.bookable = bookable;
    this.form = form;
    this.responsibleId = responsibleId;
    this.createrId = createrId;
    this.updaterId = updaterId;
    this.description = description;
    this.resources = resources;
  }

  public CategoryDetail(String name, boolean bookable, String form,
          String responsibleId, String description) {
    super();
    this.name = name;
    this.bookable = bookable;
    this.form = form;
    this.responsibleId = responsibleId;
    this.description = description;
  }

  public CategoryDetail(String id, String instanceId, String name,
          Date creationDate, Date updateDate, boolean bookable, String form,
          String responsibleId, String createrId, String updaterId,
          String description) {
    super();
    this.id = id;
    this.instanceId = instanceId;
    this.name = name;
    this.creationDate = creationDate;
    this.updateDate = updateDate;
    this.bookable = bookable;
    this.form = form;
    this.responsibleId = responsibleId;
    this.createrId = createrId;
    this.updaterId = updaterId;
    this.description = description;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public Date getUpdateDate() {
    return updateDate;
  }

  public void setUpdateDate(Date updateDate) {
    this.updateDate = updateDate;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public CategoryDetail(String id, String name, boolean bookable, String form,
          String responsibleId, String description) {
    super();
    this.id = id;
    this.name = name;
    this.bookable = bookable;
    this.form = form;
    this.responsibleId = responsibleId;
    this.description = description;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CategoryDetail other = (CategoryDetail) obj;
    if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
      return false;
    }
    if ((this.instanceId == null) ? (other.instanceId != null) : !this.instanceId.equals(
            other.instanceId)) {
      return false;
    }
    if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
      return false;
    }
    if (this.creationDate != other.creationDate && (this.creationDate == null || !this.creationDate.
            equals(other.creationDate))) {
      return false;
    }
    if (this.updateDate != other.updateDate && (this.updateDate == null || !this.updateDate.equals(
            other.updateDate))) {
      return false;
    }
    if (this.bookable != other.bookable) {
      return false;
    }
    if ((this.form == null) ? (other.form != null) : !this.form.equals(other.form)) {
      return false;
    }
    if ((this.responsibleId == null) ? (other.responsibleId != null) : !this.responsibleId.equals(
            other.responsibleId)) {
      return false;
    }
    if ((this.createrId == null) ? (other.createrId != null) : !this.createrId.equals(
            other.createrId)) {
      return false;
    }
    if ((this.updaterId == null) ? (other.updaterId != null) : !this.updaterId.equals(
            other.updaterId)) {
      return false;
    }
    if ((this.description == null) ? (other.description != null) : !this.description.equals(
            other.description)) {
      return false;
    }
    return !(this.resources != other.resources && (this.resources == null || !this.resources.equals(
        other.resources)));
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
    hash = 53 * hash + (this.instanceId != null ? this.instanceId.hashCode() : 0);
    hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
    hash = 53 * hash + (this.creationDate != null ? this.creationDate.hashCode() : 0);
    hash = 53 * hash + (this.updateDate != null ? this.updateDate.hashCode() : 0);
    hash = 53 * hash + (this.bookable ? 1 : 0);
    hash = 53 * hash + (this.form != null ? this.form.hashCode() : 0);
    hash = 53 * hash + (this.responsibleId != null ? this.responsibleId.hashCode() : 0);
    hash = 53 * hash + (this.createrId != null ? this.createrId.hashCode() : 0);
    hash = 53 * hash + (this.updaterId != null ? this.updaterId.hashCode() : 0);
    hash = 53 * hash + (this.description != null ? this.description.hashCode() : 0);
    hash = 53 * hash + (this.resources != null ? this.resources.hashCode() : 0);
    return hash;
  }

  @Override
  public String toString() {
    return "CategoryDetail{" + "id=" + id + ", instanceId=" + instanceId + ", name=" + name 
            + ", creationDate=" + creationDate + ", updateDate=" + updateDate + ", bookable=" 
            + bookable + ", form=" + form + ", responsibleId=" + responsibleId + ", createrId=" 
            + createrId + ", updaterId=" + updaterId + ", description=" + description 
            + ", resources=" + resources + '}';
  }
}
