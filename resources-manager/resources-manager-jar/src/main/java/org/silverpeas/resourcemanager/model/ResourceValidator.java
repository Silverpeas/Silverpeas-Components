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

import org.silverpeas.persistence.model.jpa.AbstractJpaCustomEntity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author ehugonnet
 */
@Entity
@Table(name = "sc_resources_managers")
@NamedQueries({@NamedQuery(name = "resourceValidator.getResourceValidator",
    query = "SELECT DISTINCT resourceValidator FROM ResourceValidator resourceValidator " +
        "WHERE resourceValidator.id.managerId = :currentUserId AND " +
        "resourceValidator.id.resourceId = :resourceId")})
public class ResourceValidator
    extends AbstractJpaCustomEntity<ResourceValidator, ResourceValidatorPk>
    implements Serializable {

  private static final long serialVersionUID = -7310087626487651284L;
  @ManyToOne
  @JoinColumn(name = "resourceId", updatable = false, insertable = false, nullable = false,
      referencedColumnName = "id")
  private Resource resource;

  public ResourceValidator() {
  }

  public ResourceValidator(long resourceId, long managerId) {
    setId(resourceId + ResourceValidatorPk.COMPOSITE_SEPARATOR + managerId);
  }

  private String[] getStringIds() {
    return getId().split(ResourceValidatorPk.COMPOSITE_SEPARATOR);
  }

  public long getManagerId() {
    return Long.parseLong(getStringIds()[ResourceValidatorPk.RV_MANAGER_POSITION]);
  }

  public long getResourceId() {
    return Long.parseLong(getStringIds()[ResourceValidatorPk.RV_RESOURCE_POSITION]);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ResourceValidator other = (ResourceValidator) obj;
    if (this.getId() != other.getId() &&
        (this.getId() == null || !this.getId().
            equals(other.getId()))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 67 * hash + (this.getId() != null ? this.getId().hashCode() : 0);
    return hash;
  }

  public Resource getResource() {
    return resource;
  }

  public void setResource(Resource resource) {
    this.resource = resource;
    this.getNativeId().setResourceId(resource.getIdAsLong());
  }

  @Override
  public String toString() {
    return "ResourceValidator{" + "resourceValidatorPk=" + getId() + '}';
  }
}
