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
package org.silverpeas.resourcemanager.services;

import com.silverpeas.resourcesmanager.model.ResourceStatus;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.silverpeas.resourcemanager.model.Reservation;
import org.silverpeas.resourcemanager.model.ReservedResource;
import org.silverpeas.resourcemanager.repository.ReservationRepository;
import org.silverpeas.resourcemanager.repository.ReservedResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author ehugonnet
 */
@Named
@Service
@Transactional
public class ReservationService {

  @Inject
  private ReservationRepository repository;
  @Inject
  private ReservedResourceRepository reservedResourceRepository;

  public String createReservation(Reservation reservation) {
    reservation.setStatus(computeReservationStatus(reservation));
    Date now = new Date();
    reservation.setCreationDate(now);
    reservation.setUpdateDate(now);
    List<ReservedResource> reservedResources = reservedResourceRepository.save(reservation.getListResourcesReserved());
    return reservedResources.iterator().next().getReservation().getId();
  }

  String computeReservationStatus(Reservation reservation) {
    boolean refused = false;
    boolean validated = true;
    String reservationStatus = ResourceStatus.STATUS_FOR_VALIDATION;
    for (ReservedResource reservedResource : reservation.getListResourcesReserved()) {
      String status = reservedResource.getStatus();
      refused = false;
      if (ResourceStatus.STATUS_FOR_VALIDATION.equals(status)) {
        reservationStatus = status;
        validated = false;
      }
      if (ResourceStatus.STATUS_REFUSED.equals(status)) {
        refused = true;
        validated = false;
      }
    }
    if (refused) {
      reservationStatus = ResourceStatus.STATUS_REFUSED;
    }
    if (validated) {
      reservationStatus = ResourceStatus.STATUS_VALIDATE;
    }
    return reservationStatus;
  }

  public void updateReservation(Reservation reservation) {
    repository.saveAndFlush(reservation);
  }

  public Reservation getReservation(int id) {
    return repository.findOne(id);
  }

  public void deleteReservation(int id) {
    Reservation reservation = repository.findOne(id);
    reservedResourceRepository.delete(reservation.getListResourcesReserved());
    reservation.getListResourcesReserved().clear();
    repository.delete(id);
  }

  public List<Reservation> findAllReservations(String instanceId) {
    return repository.findAllReservations(instanceId);
  }

  public List<Reservation> findAllReservationsForValidation(String instanceId, Integer userId,
      String startPeriod, String endPeriod) {
    return repository.findAllReservationsForValidation(instanceId, userId, startPeriod, endPeriod);
  }

  public List<Reservation> findAllReservationsForCategoryInRange(Integer categoryId,
      String startPeriod, String endPeriod) {
    return repository.findAllReservationsForCategoryInRange(categoryId, startPeriod, endPeriod);
  }
}
