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
package org.silverpeas.resourcemanager.model;

import com.silverpeas.util.StringUtil;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.silverpeas.resourcesmanager.model.ResourceStatus.*;

@Entity
@Table(name = "sc_resources_resource")
public class Resource {

  @Id
  @TableGenerator(name = "UNIQUE_RESOURCE_ID_GEN", table = "uniqueId", pkColumnName = "tablename",
  valueColumnName = "maxId", pkColumnValue = "sc_resources_resource")
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "UNIQUE_RESOURCE_ID_GEN")
  private Long id;
  @ManyToOne(optional = true)
  @JoinColumn(name = "categoryid", nullable = true, updatable = true)
  private Category category;
  @Column
  private String name;
  @Column
  private String creationDate;
  @Column
  private String updateDate;
  @Column
  private Integer bookable;
  @Column
  private String description;
  @Column
  private Integer responsibleId;
  @Column
  private String createrId;
  @Column
  private String updaterId;
  @Column
  private String instanceId;
  @OneToMany(cascade= CascadeType.ALL, orphanRemoval=true, mappedBy="resource")
  private List<ResourceValidator> managers = new ArrayList<ResourceValidator>();
  @Transient
  private String status;

  public boolean isBookable() {
    return bookable == 1;
  }

  public void setBookable(boolean bookable) {
    if (bookable) {
      this.bookable = 1;
    } else {
      this.bookable = 0;
    }
  }

  public String getCreaterId() {
    return createrId;
  }

  public void setCreaterId(String createrId) {
    this.createrId = createrId;
  }

  public Date getCreationDate() {
    if (StringUtil.isLong(creationDate)) {
      Date create = new Date();
      create.setTime(Long.parseLong(creationDate));
      return create;
    }
    return null;
  }

  public void setCreationDate(Date creationDate) {
    if (creationDate != null) {
      this.creationDate = String.valueOf(creationDate.getTime());
    } else {
      creationDate = null;
    }
  }

  public Date getUpdateDate() {
    if (StringUtil.isLong(updateDate)) {
      Date update = new Date();
      update.setTime(Long.parseLong(updateDate));
      return update;
    }
    return null;
  }

  public void setUpdateDate(Date updateDate) {
    if (updateDate != null) {
      this.updateDate = String.valueOf(updateDate.getTime());
    }
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getId() {
    return String.valueOf(id);
  }
  
  public Long getIntegerId() {
    return id;
  }

  public final void setId(String id) {
    if (StringUtil.isLong(id)) {
      this.id = Long.parseLong(id);
    }
  }

  public String getCategoryId() {
    if (category != null) {
      return category.getId();
    }
    return null;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

  public Category getCategory() {
    return category;
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
    return String.valueOf(responsibleId);
  }

  public void setResponsibleId(String responsibleId) {
    this.responsibleId = Integer.parseInt(responsibleId);
  }

  public String getUpdaterId() {
    return updaterId;
  }

  public void setUpdaterId(String updaterId) {
    this.updaterId = updaterId;
  }

  public List<ResourceValidator> getManagers() {
    return managers;
  }

  public void setManagers(List<ResourceValidator> managers) {
    this.managers = managers;
  }

  public Resource() {
  }

  public Resource(String name, Category category, boolean bookable) {
    this.name = name;
    this.category = category;
    setBookable(bookable);
  }

  public Resource(String name, Category category, String responsibleId,
      String description, boolean bookable) {
    this.category = category;
    this.name = name;
    this.description = description;
    setResponsibleId(responsibleId);
    setBookable(bookable);
  }

  public Resource(String id, Category category, String name,
      Date creationDate, Date updateDate, String description,
      String responsibleId, String createrId, String updaterId,
      String instanceId, boolean bookable) {
    setId(id);
    this.category = category;
    this.name = name;
    setCreationDate(creationDate);
    setUpdateDate(updateDate);
    this.description = description;
    setResponsibleId(responsibleId);
    this.createrId = createrId;
    this.updaterId = updaterId;
    this.instanceId = instanceId;
    setBookable(bookable);
  }

  public Resource(String id, Category category, String name,
      Date creationDate, Date updateDate, String description,
      String responsibleId, String createrId, String updaterId,
      String instanceId, boolean bookable, String status) {
    setId(id);
    this.category = category;
    this.name = name;
    setCreationDate(creationDate);
    setUpdateDate(updateDate);
    this.description = description;
    setResponsibleId(responsibleId);
    this.createrId = createrId;
    this.updaterId = updaterId;
    this.instanceId = instanceId;
    setBookable(bookable);
    this.setStatus(status);
  }

  public Resource(String id, Category category, String name,
      String description, String responsibleId, boolean bookable) {
    setId(id);
    this.category = category;
    this.name = name;
    this.description = description;
    setResponsibleId(responsibleId);
    setBookable(bookable);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Resource) {
      Resource r = (Resource) obj;
      return r.getId().equals(this.getId());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return 356 + (id != null ? id.hashCode() : 0);
  }

  @Override
  public String toString() {
    return "Resource{" + "id=" + id + ", category= {" + category + "}, name=" + name +
         ", creationDate=" + creationDate + ", updateDate=" + updateDate + ", description=" +
         description + ", responsibleId=" + responsibleId + ", createrId=" + createrId +
         ", updaterId=" + updaterId + ", instanceId=" + instanceId + ", bookable=" + bookable +
         ", managers=" + managers + ", status=" + getStatus() + '}';
  }

  public boolean isValidated() {
    return STATUS_VALIDATE.equals(getStatus());
  }

  public boolean isRefused() {
    return STATUS_REFUSED.equals(getStatus());
  }

  public boolean isValidationRequired() {
    return STATUS_FOR_VALIDATION.equals(getStatus());
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
