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

import java.io.Serializable;

/**
 *
 * @author ehugonnet
 */
public class ReservedResourcePk implements Serializable {

  private static final long serialVersionUID = -5550864318148567106L;
  private int resourceId;
  private int reservationId;

  public ReservedResourcePk() {
  }

  public ReservedResourcePk(int resourceId, int reservationId) {
    this.resourceId = resourceId;
    this.reservationId = reservationId;
  }

  public int getReservationId() {
    return reservationId;
  }

  public void setReservationId(int reservationId) {
    this.reservationId = reservationId;
  }

  public int getResourceId() {
    return resourceId;
  }

  public void setResourceId(int resourceId) {
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
    final ReservedResourcePk other = (ReservedResourcePk) obj;
    if (this.resourceId != other.resourceId) {
      return false;
    }
    if (this.reservationId != other.reservationId) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 53 * hash + this.resourceId;
    hash = 53 * hash + this.reservationId;
    return hash;
  }

  @Override
  public String toString() {
    return "ReservedResourcePk{" + "resourceId=" + resourceId + ", reservationId=" + reservationId + '}';
  }
}
