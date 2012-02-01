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
 * "http://www.silverpeas.com/legal/licensing"
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "sc_resources_category")
public class Category {

  @Id
  @TableGenerator(name = "UNIQUE_ID_GEN", table = "uniqueId", pkColumnName = "tablename",
      valueColumnName = "maxId", pkColumnValue = "SC_Resources_Category")
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "UNIQUE_ID_GEN")
  private Long id;
  @Column
  private String instanceId;
  @Column
  private String name;
  @Column
  private String creationDate;
  @Column
  private String updateDate;
  @Column
  private Integer bookable;
  @Column
  private String form;
  @Column
  private Integer responsibleId;
  @Column
  private String createrId;
  @Column
  private String updaterId;
  @Column
  private String description;
  @OneToMany(mappedBy="category", orphanRemoval=false)
  private List<Resource> resources = new ArrayList<Resource>();

  public boolean isBookable() {
    return 1 == bookable;
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

  public String getForm() {
    return form;
  }

  public void setForm(String form) {
    this.form = form;
  }

  public String getId() {
    return String.valueOf(id);
  }

  public final void setId(String id) {
    if (StringUtil.isLong(id)) {
      this.id = Long.parseLong(id);
    }
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

  public List<Resource> getResources() {
    return resources;
  }

  public void setResources(List<Resource> resources) {
    this.resources = resources;
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

  public Category() {
    
  }
  
  public Category(String id, String instanceId, String name,
      Date creationDate, Date updateDate, boolean bookable, String form,
      String responsibleId, String createrId, String updaterId,
      String description, List<Resource> resources) {
    setId(id);
    this.instanceId = instanceId;
    this.name = name;
    setCreationDate(creationDate);
    setUpdateDate(updateDate);
    setBookable(bookable);
    this.form = form;
    setResponsibleId(responsibleId);
    this.createrId = createrId;
    this.updaterId = updaterId;
    this.description = description;
    this.resources = resources;
  }

  public Category(String name, boolean bookable, String form,
      String responsibleId, String description) {
    this.name = name;
    setBookable(bookable);
    this.form = form;
    setResponsibleId(responsibleId);
    this.description = description;
  }

  /**
   * For tests purpose only.
   * @param id
   * @param instanceId
   * @param name
   * @param creationDate
   * @param updateDate
   * @param bookable
   * @param form
   * @param responsibleId
   * @param createrId
   * @param updaterId
   * @param description
   */
  public Category(String id, String instanceId, String name,
      Date creationDate, Date updateDate, boolean bookable, String form,
      String responsibleId, String createrId, String updaterId,
      String description) {
    setId(id);
    this.instanceId = instanceId;
    this.name = name;
    setCreationDate(creationDate);
    setUpdateDate(updateDate);
    setBookable(bookable);
    this.form = form;
    setResponsibleId(responsibleId);
    this.createrId = createrId;
    this.updaterId = updaterId;
    this.description = description;
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

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Category other = (Category) obj;
    if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
      return false;
    }
    if ((this.instanceId == null) ? (other.instanceId != null) : !this.instanceId.equals(other.instanceId)) {
      return false;
    }
    if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
      return false;
    }
    if ((this.creationDate == null) ? (other.creationDate != null) : !this.creationDate.equals(other.creationDate)) {
      return false;
    }
    if ((this.updateDate == null) ? (other.updateDate != null) : !this.updateDate.equals(other.updateDate)) {
      return false;
    }
    if (this.bookable != other.bookable && (this.bookable == null || !this.bookable.equals(other.bookable))) {
      return false;
    }
    if ((this.form == null) ? (other.form != null) : !this.form.equals(other.form)) {
      return false;
    }
    if (this.responsibleId != other.responsibleId && (this.responsibleId == null || !this.responsibleId.equals(other.responsibleId))) {
      return false;
    }
    if ((this.createrId == null) ? (other.createrId != null) : !this.createrId.equals(other.createrId)) {
      return false;
    }
    if ((this.updaterId == null) ? (other.updaterId != null) : !this.updaterId.equals(other.updaterId)) {
      return false;
    }
    if ((this.description == null) ? (other.description != null) : !this.description.equals(other.description)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
    hash = 97 * hash + (this.instanceId != null ? this.instanceId.hashCode() : 0);
    hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
    hash = 97 * hash + (this.creationDate != null ? this.creationDate.hashCode() : 0);
    hash = 97 * hash + (this.updateDate != null ? this.updateDate.hashCode() : 0);
    hash = 97 * hash + (this.bookable != null ? this.bookable.hashCode() : 0);
    hash = 97 * hash + (this.form != null ? this.form.hashCode() : 0);
    hash = 97 * hash + (this.responsibleId != null ? this.responsibleId.hashCode() : 0);
    hash = 97 * hash + (this.createrId != null ? this.createrId.hashCode() : 0);
    hash = 97 * hash + (this.updaterId != null ? this.updaterId.hashCode() : 0);
    hash = 97 * hash + (this.description != null ? this.description.hashCode() : 0);
    return hash;
  }

  
  @Override
  public String toString() {
    return "CategoryDetail{" + "id=" + id + ", instanceId=" + instanceId + ", name=" + name +
        ", creationDate=" + creationDate + ", updateDate=" + updateDate + ", bookable=" +
        bookable + ", form=" + form + ", responsibleId=" + responsibleId + ", createrId=" +
        createrId + ", updaterId=" + updaterId + ", description=" + description + '}';
  }
}
