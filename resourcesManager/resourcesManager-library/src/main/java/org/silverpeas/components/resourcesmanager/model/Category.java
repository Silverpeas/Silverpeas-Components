/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.resourcesmanager.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.core.persistence.datasource.model.identifier.UniqueLongIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;
import org.silverpeas.core.util.StringUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "sc_resources_category")
@NamedQueries({@NamedQuery(name = "category.findByInstanceId",
    query = "SELECT category FROM Category category WHERE category.instanceId = :instanceId ORDER BY category.name")})
public class Category extends BasicJpaEntity<Category, UniqueLongIdentifier> {
  private static final long serialVersionUID = 4947144625712662946L;

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
  private List<Resource> resources = new ArrayList<>();

  public void performBeforePersist() {
    Date now = new Date();
    setCreationDate(now);
    setUpdateDate(now);
  }

  public void performBeforeUpdate() {
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

  public Long getIdAsLong() {
    return getNativeId().getId();
  }

  public String getIdAsString() {
    return getId();
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
   * For tests purpose only. TODO remove this constructor in V6
   * @param id
   * @param instanceId
   * @param name
   * @param bookable
   * @param form
   * @param createrId
   * @param updaterId
   * @param description
   */
  public Category(Long id, String instanceId, String name, boolean bookable, String form,
      String createrId, String updaterId, String description) {
    setId(Long.toString(id));
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
    return "Category{" + "id=" + getId() + ", instanceId=" + instanceId + ", name=" + name +
        ", creationDate=" + creationDate + ", updateDate=" + updateDate + ", bookable=" + bookable +
        ", form=" + form + ", createrId=" + createrId + ", updaterId=" + updaterId +
        ", description=" + description + '}';
  }
}
