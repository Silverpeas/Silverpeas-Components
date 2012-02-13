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

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 *
 * @author ehugonnet
 */
@Embeddable
public class ResourceValidatorPk  implements Serializable{
  private static final long serialVersionUID = 5687541398796291824L;

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
    if (this.managerId != other.managerId) {
      return false;
    }
    return true;
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
  
  
}

