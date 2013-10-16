/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

import static org.silverpeas.resourcemanager.model.ResourceStatus.*;

/**
 *
 * @author ehugonnet
 */
@Entity
@Table(name = "sc_resources_reservedresource")
public class ReservedResource implements Serializable {

  private static final long serialVersionUID = -4233541745596218664L;
  @EmbeddedId
  private ReservedResourcePk reservedResourcePk = new ReservedResourcePk();
  @Column(name = "status")
  private String status;
  @ManyToOne(optional = false)
  @JoinColumn(name = "resourceId", updatable = false, insertable = false, referencedColumnName = "id")
  private Resource resource;
  @ManyToOne(optional = false)
  @JoinColumn(name = "reservationId", updatable = false, insertable = false, referencedColumnName = "id")
  private Reservation reservation;

  public ReservedResource() {
  }

  public Reservation getReservation() {
    return reservation;
  }

  public void setReservation(Reservation reservation) {
    this.reservation = reservation;
    if (reservation != null && reservation.getId() != null) {
      this.reservedResourcePk.setReservationId(reservation.getId());
    }
  }

  public long getReservationId() {
    return reservedResourcePk.getReservationId();
  }

  public void setReservationId(long reservationId) {
    reservedResourcePk.setReservationId(reservationId);
  }

  public Resource getResource() {
    return resource;
  }

  public void setResource(Resource resource) {
    this.resource = resource;
    if (resource != null && resource.getId() != null) {
      this.reservedResourcePk.setResourceId(resource.getId());
    }
  }

  public long getResourceId() {
    return reservedResourcePk.getResourceId();
  }

  public void setResourceId(long resourceId) {
    reservedResourcePk.setResourceId(resourceId);
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public boolean isValidated() {
    return STATUS_VALIDATE.equals(status);
  }

  public boolean isRefused() {
    return STATUS_REFUSED.equals(status);
  }

  public boolean isValidationRequired() {
    return STATUS_FOR_VALIDATION.equals(status);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ReservedResource other = (ReservedResource) obj;
    if (this.reservedResourcePk != other.reservedResourcePk && (this.reservedResourcePk == null || !this.reservedResourcePk.
        equals(other.reservedResourcePk))) {
      return false;
    }
    if ((this.status == null) ? (other.status != null) : !this.status.equals(other.status)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 11 * hash + (this.reservedResourcePk != null ? this.reservedResourcePk.hashCode() : 0);
    hash = 11 * hash + (this.status != null ? this.status.hashCode() : 0);
    return hash;
  }

  @Override
  public String toString() {
    return "ReservedResource{" + "reservedResourcePk=" + reservedResourcePk + ", status=" + status + '}';
  }
}
