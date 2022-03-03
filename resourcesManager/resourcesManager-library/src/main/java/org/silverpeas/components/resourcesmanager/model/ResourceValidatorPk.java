/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.components.resourcesmanager.model;

import org.silverpeas.core.persistence.datasource.model.CompositeEntityIdentifier;

import javax.persistence.Embeddable;

/**
 * @author ehugonnet
 */
@Embeddable
public class ResourceValidatorPk implements CompositeEntityIdentifier {
  private static final long serialVersionUID = 5687541398796291824L;

  public static final int RV_RESOURCE_POSITION = 0;
  public static final int RV_MANAGER_POSITION = 1;

  private long resourceId;
  private long managerId;

  public ResourceValidatorPk() {
  }

  public ResourceValidatorPk(long resourceId, long managerId) {
    this.resourceId = resourceId;
    this.managerId = managerId;
  }

  public long getManagerId() {
    return managerId;
  }

  public void setManagerId(long managerId) {
    this.managerId = managerId;
  }

  public long getResourceId() {
    return resourceId;
  }

  public void setResourceId(long resourceId) {
    this.resourceId = resourceId;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ResourceValidatorPk other = (ResourceValidatorPk) obj;
    if (this.resourceId != other.resourceId) {
      return false;
    }
    return this.managerId == other.managerId;
  }


  @Override
  public int hashCode() {
    int result = (int) (resourceId ^ (resourceId >>> 32));
    result = 31 * result + (int) (managerId ^ (managerId >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "ResourceValidatorPk{" + "resourceId=" + resourceId + ", managerId=" + managerId + '}';
  }

  @Override
  public ResourceValidatorPk fromString(final String... values) {
    this.resourceId = Long.parseLong(values[RV_RESOURCE_POSITION]);
    this.managerId = Long.parseLong(values[RV_MANAGER_POSITION]);
    return this;
  }

  @Override
  public String asString() {
    return resourceId + COMPOSITE_SEPARATOR + managerId;
  }
}
