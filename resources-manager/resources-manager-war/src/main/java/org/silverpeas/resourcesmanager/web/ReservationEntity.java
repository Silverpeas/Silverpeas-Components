/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.resourcesmanager.web;

import com.silverpeas.calendar.DateTime;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.calendar.web.AbstractEventEntity;
import org.silverpeas.resourcemanager.model.Reservation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ReservationEntity extends AbstractEventEntity<ReservationEntity> {
  private static final long serialVersionUID = -6782696118309356915L;

  @XmlElement(defaultValue = "")
  private String reason;

  @XmlElement(defaultValue = "")
  private String place;

  @XmlElement(defaultValue = "")
  private String status;

  @XmlElement(defaultValue = "")
  private String bookedBy;

  @XmlElement(defaultValue = "")
  private URI resourceURI;

  @XmlElement(defaultValue = "")
  private Collection<ReservedResourceEntity> resources = new ArrayList<ReservedResourceEntity>();


  @XmlTransient
  private final Reservation reservation;

  @Override
  public ReservationEntity withURI(final URI uri) {
    return super.withURI(uri);
  }

  public ReservationEntity withResourceURI(final URI resourceURI) {
    this.resourceURI = resourceURI;
    return this;
  }

  /**
   * Creates a new reservation entity from the specified reservation.
   * @param instanceId
   * @param reservation
   * @return the entity representing the specified reservation.
   */
  public static ReservationEntity createFrom(final String instanceId,
      final Reservation reservation) {
    return new ReservationEntity(instanceId, reservation);
  }

  /**
   * Default hidden constructor.
   */
  private ReservationEntity(final String instanceId, final Reservation reservation) {
    super("reservation", instanceId, reservation.getIdAsString(), reservation.getEvent(), null,
        new DateTime(reservation.getBeginDate()), new DateTime(reservation.getEndDate()), null);
    this.reservation = reservation;
    reason = reservation.getReason();
    place = reservation.getPlace();
    status = reservation.getStatus();
    UserDetail user = UserDetail.getById(reservation.getUserId());
    if (user != null) {
      bookedBy = UserDetail.getById(reservation.getUserId()).getDisplayedName();
    }
  }

  protected ReservationEntity() {
    super();
    reservation = null;
  }

  protected Reservation getReservation() {
    return reservation;
  }

  public String getReason() {
    return reason;
  }

  public String getPlace() {
    return place;
  }

  public String getStatus() {
    return status;
  }

  public String getBookedBy() {
    return bookedBy;
  }

  public URI getResourceURI() {
    return resourceURI;
  }

  public Collection<ReservedResourceEntity> getResources() {
    return resources;
  }

  public ReservationEntity add(ReservedResourceEntity reservedResourceEntity) {
    resources.add(reservedResourceEntity);
    return this;
  }

  public ReservationEntity addAll(Collection<ReservedResourceEntity> reservedResourceEntities) {
    resources.addAll(reservedResourceEntities);
    return this;
  }
}
