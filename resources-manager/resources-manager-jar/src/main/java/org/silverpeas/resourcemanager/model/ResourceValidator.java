/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.resourcemanager.model;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

/**
 *
 * @author ehugonnet
 */
@Entity
@Table(name = "sc_resources_managers")
public class ResourceValidator implements Serializable {

  private static final long serialVersionUID = -7310087626487651284L;
  @EmbeddedId
  private ResourceValidatorPk resourceValidatorPk = new ResourceValidatorPk();
  @ManyToOne
  @JoinColumn(name = "resourceId", updatable = false, insertable = false, nullable = false, referencedColumnName = "id")
  private Resource resource;

  public ResourceValidator() {
  }

  public ResourceValidator(long resourceId, long managerId) {
    this.resourceValidatorPk = new ResourceValidatorPk(resourceId, managerId);
  }

  public long getManagerId() {
    return this.resourceValidatorPk.getManagerId();
  }

  public void setManagerId(long managerId) {
    this.resourceValidatorPk.setManagerId(managerId);
  }

  public long getResourceId() {
    return this.resourceValidatorPk.getResourceId();
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
    if (this.resourceValidatorPk != other.resourceValidatorPk && (this.resourceValidatorPk == null || !this.resourceValidatorPk.
        equals(other.resourceValidatorPk))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 67 * hash + (this.resourceValidatorPk != null ? this.resourceValidatorPk.hashCode() : 0);
    return hash;
  }

  public void setResourceId(long resourceId) {
    this.resourceValidatorPk.setResourceId(resourceId);
  }

  public Resource getResource() {
    return resource;
  }

  public void setResource(Resource resource) {
    this.resource = resource;
    this.resourceValidatorPk.setResourceId(resource.getIntegerId());
  }

  @Override
  public String toString() {
    return "ResourceValidator{" + "resourceValidatorPk=" + resourceValidatorPk + '}';
  }
}
