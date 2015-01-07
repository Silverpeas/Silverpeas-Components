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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.io.Serializable;

import static org.silverpeas.resourcemanager.model.ResourceStatus.*;

/**
 * @author ehugonnet
 */
@Entity
@Table(name = "sc_resources_reservedresource")
@NamedQueries({
    @NamedQuery(name = "reservedResource.findAllResourcesForReservation",
        query = "SELECT DISTINCT reservedResource.resource FROM ReservedResource reservedResource" +
            " WHERE reservedResource.id.reservationId = :reservationId"),
    @NamedQuery(name = "reservedResource.findAllReservedResources",
        query =
            "SELECT DISTINCT reservedResource.resource FROM ReservedResource reservedResource " +
                "WHERE reservedResource.reservation.id != :reservationIdToSkip AND " +
                "reservedResource.status != 'R'" +
                "AND reservedResource.resource.id IN :aimedResourceIds " +
                "AND reservedResource.reservation.beginDate < :endPeriod " +
                "AND reservedResource.reservation.endDate > :startPeriod "),
    @NamedQuery(name = "reservedResource.findAllReservedResourcesWithProblem",
    query = "SELECT DISTINCT reservedResource FROM ReservedResource reservedResource " +
        "WHERE reservedResource.reservation.id != :currentReservationId " +
        "AND reservedResource.status != 'R'" +
        "AND reservedResource.resource.id.id IN :futureReservedResourceIds " +
        "AND reservedResource.reservation.beginDate < :endPeriod " +
        "AND reservedResource.reservation.endDate > :startPeriod "),
    @NamedQuery(name = "reservedResource.findAllReservedResourcesForReservation",
        query = "SELECT DISTINCT reservedResource FROM ReservedResource reservedResource " +
            "WHERE reservedResource.reservation.id = :currentReservationId"),
    @NamedQuery(name = "reservedResource.deleteAllReservedResourcesForReservation",
        query = "DELETE ReservedResource reservedResource " +
            "WHERE reservedResource.id.reservationId = :currentReservationId"),
    @NamedQuery(name = "reservedResource.deleteAllReservedResourcesForResource",
        query = "DELETE ReservedResource reservedResource " +
            "WHERE reservedResource.id.resourceId = :currentResourceId"),
    @NamedQuery(name = "reservedResource.findAllReservedResourcesOfReservation",
        query = "SELECT DISTINCT reservedResource FROM ReservedResource reservedResource " +
            "WHERE reservedResource.id.reservationId = :currentReservationId")})
public class ReservedResource extends AbstractJpaCustomEntity<ReservedResource, ReservedResourcePk>
    implements Serializable {

  private static final long serialVersionUID = -4233541745596218664L;

  @Column(name = "status")
  private String status;
  @ManyToOne(optional = false)
  @JoinColumn(name = "resourceId", updatable = false, insertable = false, referencedColumnName =
      "id")
  private Resource resource;
  @ManyToOne(optional = false)
  @JoinColumn(name = "reservationId", updatable = false, insertable = false, referencedColumnName
      = "id")
  private Reservation reservation;

  public ReservedResource() {
  }

  public void setReservedResourceId(String resourceId, String reservationId) {
    setId(resourceId + ReservedResourcePk.COMPOSITE_SEPARATOR + reservationId);
  }

  public Reservation getReservation() {
    return reservation;
  }

  public void setReservation(Reservation reservation) {
    this.reservation = reservation;
//    if (reservation != null && reservation.getId() != null) {
//      this.reservedResourcePk.setReservationId(reservation.getIdAsLong());
//    }
  }

  private String[] getStringIds() {
    return getId().split(ReservedResourcePk.COMPOSITE_SEPARATOR);
  }

  public long getReservationId() {
    return Long.parseLong(getStringIds()[ReservedResourcePk.RR_RESERVATION_POSITION]);
  }

  public Resource getResource() {
    return resource;
  }

  public void setResource(Resource resource) {
    this.resource = resource;
//    if (resource != null && resource.getIdAsLong() != null) {
//      this.reservedResourcePk.setResourceId(resource.getIdAsLong());
//    }
  }

  public long getResourceId() {
    return Long.parseLong(getStringIds()[ReservedResourcePk.RR_RESOURCE_POSITION]);
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
    if (this.getId() != other.getId() && (this.getId() == null || !this.getId().
        equals(other.getId()))) {
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
    hash = 11 * hash + (this.getId() != null ? this.getId().hashCode() : 0);
    hash = 11 * hash + (this.status != null ? this.status.hashCode() : 0);
    return hash;
  }

  @Override
  public String toString() {
    return "ReservedResource{" + "reservedResourcePk=" + getId() + ", status=" + status + '}';
  }
}
