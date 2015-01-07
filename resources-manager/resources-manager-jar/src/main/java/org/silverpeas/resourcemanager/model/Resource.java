/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.resourcemanager.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.persistence.model.identifier.UniqueLongIdentifier;
import org.silverpeas.persistence.model.jpa.AbstractJpaCustomEntity;
import org.silverpeas.util.StringUtil;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.silverpeas.resourcemanager.model.ResourceStatus.*;

@Entity
@Table(name = "sc_resources_resource")
@NamedQueries({@NamedQuery(name = "resource.findAllResourcesByCategory",
    query = "SELECT resource FROM Resource resource WHERE resource.category.id = :categoryId"),
    @NamedQuery(name = "resource.findAllBookableResources",
        query = "SELECT resource FROM Resource resource WHERE resource.instanceId = :instanceId " +
            "AND resource.bookable = 1 AND resource.category.bookable = 1"),
    @NamedQuery(name = "resource.deleteResourcesFromCategory",
        query = "DELETE Resource resource WHERE resource.category.id = :categoryId")
})
public class Resource extends AbstractJpaCustomEntity<Resource, UniqueLongIdentifier> {
  private static final long serialVersionUID = 3438059589840347315L;

  @ManyToOne(optional = true, fetch = FetchType.EAGER)
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
  private String createrId;
  @Column
  private String updaterId;
  @Column
  private String instanceId;
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "resource")
  private List<ResourceValidator> managers = new ArrayList<>();
  @Transient
  private String status;

  @Override
  protected void performBeforePersist() {
    Date now = new Date();
    setCreationDate(now);
    setUpdateDate(now);
  }

  @Override
  public void performBeforeUpdate() {
    setUpdateDate(new Date());
  }

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
    this.creationDate = String.valueOf(creationDate.getTime());
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
    this.updateDate = String.valueOf(updateDate.getTime());
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Long getIdAsLong() {
    return getNativeId().getId();
  }

  public String getIdAsString() {
    return getId();
  }

  public Long getCategoryId() {
    if (category != null) {
      return category.getIdAsLong();
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

  public void merge(Resource resource) {
    this.bookable = resource.bookable;
    this.category = resource.category;
    this.createrId = resource.createrId;
    this.creationDate = resource.creationDate;
    this.description = resource.description;
    this.instanceId = resource.instanceId;
    this.name = resource.name;
    this.updateDate = resource.updateDate;
    this.updaterId = resource.updaterId;
  }

  public Resource(Long id, Category category, String name, String description, String createrId,
      String updaterId, String instanceId, boolean bookable) {
    setId(id != null ? Long.toString(id): null);
    this.category = category;
    this.name = name;
    this.description = description;
    this.createrId = createrId;
    this.updaterId = updaterId;
    this.instanceId = instanceId;
    setBookable(bookable);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Resource other = (Resource) obj;
    EqualsBuilder matcher = new EqualsBuilder();
    matcher.append(getId(), other.getId());
    matcher.append(getCategory(), other.getCategory());
    matcher.append(getName(), other.getName());
    matcher.append(isBookable(), other.isBookable());
    matcher.append(getDescription(), other.getDescription());
    matcher.append(getCreaterId(), other.getCreaterId());
    matcher.append(getUpdaterId(), other.getUpdaterId());
    matcher.append(getInstanceId(), other.getInstanceId());
    return matcher.isEquals();
  }

  @Override
  public int hashCode() {
    HashCodeBuilder hash = new HashCodeBuilder();
    hash.append(getId());
    hash.append(getCategory());
    hash.append(getName());
    hash.append(isBookable());
    hash.append(getDescription());
    hash.append(getCreaterId());
    hash.append(getUpdaterId());
    hash.append(getInstanceId());
    return hash.toHashCode();
  }

  @Override
  public String toString() {
    return "Resource{" + "id=" + getId() + ", category= {" + category + "}, name=" + name +
        ", creationDate=" + creationDate + ", updateDate=" + updateDate + ", description=" +
        description + ", createrId=" + createrId + ", updaterId=" + updaterId +
        ", instanceId=" + instanceId + ", bookable=" + bookable + ", managers=" + managers +
        ", status=" + getStatus() + '}';
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
