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

import org.silverpeas.util.DateUtil;
import org.silverpeas.resourcemanager.model.Reservation;
import org.silverpeas.resourcemanager.model.Resource;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yohann Chastagnier
 */
public class ReservationBuilder {

  public static ReservationBuilder getReservationBuilder() {
    return new ReservationBuilder();
  }

  public ReservationMock buildReservation(final String instanceId, final Long reservationId,
      final String status) {
    return new ReservationMock(instanceId, reservationId, status);
  }

  private ReservationBuilder() {
    // Nothing to do
  }

  protected class ReservationMock extends Reservation {

    List<Resource> resources = new ArrayList<Resource>();

    public ReservationMock(final String instanceId, final Long reservationId, final String status) {
      super();
      setInstanceId(instanceId);
      setId(reservationId);
      setEvent("reservation-title" + reservationId);
      setPlace("reservation-place" + reservationId);
      setReason("reservation-reason" + reservationId);
      try {
        setBeginDate(DateUtil.parseDateTime("2013/01/01 08:00"));
        setEndDate(DateUtil.parseDateTime("2013/01/01 12:00"));
      } catch (ParseException e) {
        // Nothing to do.
      }
      setStatus(status);
    }

    public List<Resource> getResources() {
      return resources;
    }

    public ReservationMock addResource(final Resource resource) {
      getResources().add(resource);
      return this;
    }
  }
}
