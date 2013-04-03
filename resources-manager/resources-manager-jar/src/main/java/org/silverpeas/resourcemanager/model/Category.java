/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
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
package org.silverpeas.resourcemanager.model;

import com.silverpeas.util.StringUtil;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "sc_resources_category")
public class Category {

  @Id
  @TableGenerator(name = "UNIQUE_ID_GEN", table = "uniqueId", pkColumnName = "tablename",
  valueColumnName = "maxId", pkColumnValue = "sc_resources_category", allocationSize=1)
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
  private String createrId;
  @Column
  private String updaterId;
  @Column
  private String description;
  @OneToMany(mappedBy = "category", orphanRemoval = false)
  private List<Resource> resources = new ArrayList<Resource>();

  @PrePersist
  public void beforePersist() {
    Date now = new Date();
    setCreationDate(now);
    setUpdateDate(now);
  }

  @PreUpdate
  public void beforeUpdate() {
    setUpdateDate(new Date());
  }

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

  public Long getId() {
    return id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public String getIdAsString() {
    return String.valueOf(id);
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

  public String getUpdaterId() {
    return updaterId;
  }

  public void setUpdaterId(String updaterId) {
    this.updaterId = updaterId;
  }

  public Category() {
  }

  public Category(String name, boolean bookable, String form, String description) {
    this.name = name;
    setBookable(bookable);
    this.form = form;
    this.description = description;
  }

  /**
   * For tests purpose only.
   *
   * @param id
   * @param instanceId
   * @param name
   * @param bookable
   * @param form
   * @param createrId
   * @param updaterId
   * @param description
   */
  public Category(Long id, String instanceId, String name, boolean bookable, String form, String
      createrId, String updaterId, String description) {
    setId(id);
    this.instanceId = instanceId;
    this.name = name;
    setBookable(bookable);
    this.form = form;
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
    EqualsBuilder matcher = new EqualsBuilder();
    matcher.append(getId(), other.getId());
    matcher.append(getInstanceId(), other.getInstanceId());
    matcher.append(getName(), other.getName());
    matcher.append(isBookable(), other.isBookable());
    matcher.append(getForm(), other.getForm());
    matcher.append(getCreaterId(), other.getCreaterId());
    matcher.append(getUpdaterId(), other.getUpdaterId());
    matcher.append(getDescription(), other.getDescription());
    return matcher.isEquals();
  }

  @Override
  public int hashCode() {
    HashCodeBuilder hash = new HashCodeBuilder();
    hash.append(getId());
    hash.append(getInstanceId());
    hash.append(getName());
    hash.append(isBookable());
    hash.append(getForm());
    hash.append(getCreaterId());
    hash.append(getUpdaterId());
    hash.append(getDescription());
    return hash.toHashCode();
  }

  @Override
  public String toString() {
    return "CategoryDetail{" + "id=" + id + ", instanceId=" + instanceId + ", name=" + name
            + ", creationDate=" + creationDate + ", updateDate=" + updateDate + ", bookable="
            + bookable + ", form=" + form + ", createrId=" + createrId + ", updaterId=" + updaterId 
            + ", description=" + description + '}';
  }
}
