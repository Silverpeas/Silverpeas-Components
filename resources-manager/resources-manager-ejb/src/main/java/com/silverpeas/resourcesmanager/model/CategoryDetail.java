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

}
