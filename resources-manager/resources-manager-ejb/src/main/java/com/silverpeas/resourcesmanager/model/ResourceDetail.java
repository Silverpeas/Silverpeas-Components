/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.silverpeas.resourcesmanager.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ResourceDetail implements Serializable {

  private static final long serialVersionUID = 1L;
  private String id;
  private String categoryId;
  private String name;
  private Date creationDate;
  private Date updateDate;
  private String description;
  private String responsibleId;
  private String createrId;
  private String updaterId;
  private String instanceId;
  private boolean bookable;
  
  private List<String> managers;
  private String status;        // pour les ressources associées à une réservation

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

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(String categoryId) {
    this.categoryId = categoryId;
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

  public String getResponsibleId() {
    return responsibleId;
  }

  public void setResponsibleId(String responsibleId) {
    this.responsibleId = responsibleId;
  }

  public Date getUpdateDate() {
    return updateDate;
  }

  public void setUpdateDate(Date updateDate) {
    this.updateDate = updateDate;
  }

  public String getUpdaterId() {
    return updaterId;
  }

  public void setUpdaterId(String updaterId) {
    this.updaterId = updaterId;
  }

  public List<String> getManagers() {
    return managers;
  }

  public void setManagers(List<String> managers) {
    this.managers = managers;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public ResourceDetail(String name, String categoryId, boolean bookable) {
    super();
    this.name = name;
    this.categoryId = categoryId;
    this.bookable = bookable;
  }

  public ResourceDetail(String name, String categoryId, String responsibleId,
      String description, boolean bookable) {
    super();
    this.categoryId = categoryId;
    this.name = name;
    this.description = description;
    this.responsibleId = responsibleId;
    this.bookable = bookable;
  }

  public ResourceDetail(String id, String categoryId, String name,
      Date creationDate, Date updateDate, String description,
      String responsibleId, String createrId, String updaterId,
      String instanceId, boolean bookable) {
    super();
    this.id = id;
    this.categoryId = categoryId;
    this.name = name;
    this.creationDate = creationDate;
    this.updateDate = updateDate;
    this.description = description;
    this.responsibleId = responsibleId;
    this.createrId = createrId;
    this.updaterId = updaterId;
    this.instanceId = instanceId;
    this.bookable = bookable;
  }

  public ResourceDetail(String id, String categoryId, String name,
      Date creationDate, Date updateDate, String description,
      String responsibleId, String createrId, String updaterId,
      String instanceId, boolean bookable, String status) {
    super();
    this.id = id;
    this.categoryId = categoryId;
    this.name = name;
    this.creationDate = creationDate;
    this.updateDate = updateDate;
    this.description = description;
    this.responsibleId = responsibleId;
    this.createrId = createrId;
    this.updaterId = updaterId;
    this.instanceId = instanceId;
    this.bookable = bookable;
    this.status = status;
  }
  
  public ResourceDetail(String id, String categoryId, String name,
      String description, String responsibleId, boolean bookable) {
    super();
    this.id = id;
    this.categoryId = categoryId;
    this.name = name;
    this.description = description;
    this.responsibleId = responsibleId;
    this.bookable = bookable;
  }

  public boolean equals(Object obj) {
    if (obj != null && obj instanceof ResourceDetail) {
      ResourceDetail r = (ResourceDetail) obj;
      return r.getId().equals(this.getId());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return 356 +(id != null ? id.hashCode() : 0);
  }
}
