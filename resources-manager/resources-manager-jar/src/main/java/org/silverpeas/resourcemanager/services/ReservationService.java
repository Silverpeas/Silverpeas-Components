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
package org.silverpeas.resourcemanager.services;

import com.silverpeas.annotation.Service;
import org.silverpeas.resourcemanager.model.Reservation;
import org.silverpeas.resourcemanager.model.ReservedResource;
import org.silverpeas.resourcemanager.model.Resource;
import org.silverpeas.resourcemanager.model.ResourceStatus;
import org.silverpeas.resourcemanager.repository.ReservationRepository;
import org.silverpeas.resourcemanager.repository.ReservedResourceRepository;
import org.silverpeas.resourcemanager.repository.ResourceRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

/**
 * @author ehugonnet
 */
@Service
@Transactional
public class ReservationService {

  @Inject
  private ReservationRepository repository;

  @Inject
  private ReservedResourceRepository reservedResourceRepository;

  @Inject
  private ResourceRepository resourceRepository;

  public void createReservation(Reservation reservation, List<Long> resourceIds) {
    reservation.setStatus(ResourceStatus.STATUS_VALIDATE);
    repository.save(reservation);
    for (Long resourceId : resourceIds) {
      ReservedResource reservedResource = new ReservedResource();
      reservedResource.setResourceId(resourceId);
      reservedResource.setReservationId(reservation.getId());
      Resource resource = resourceRepository.findOne(resourceId);
      if (!resource.getManagers().isEmpty()) {
        if (resourceRepository
            .getResourceValidator(resourceId, Long.parseLong(reservation.getUserId())) ==
            null) {
          reservedResource.setStatus(ResourceStatus.STATUS_FOR_VALIDATION);
        } else {
          reservedResource.setStatus(ResourceStatus.STATUS_VALIDATE);
        }
      } else {
        reservedResource.setStatus(reservation.getStatus());
      }
      reservedResourceRepository.save(reservedResource);
    }
    reservation.setStatus(computeReservationStatus(reservation));
    repository.save(reservation);
  }

  public String computeReservationStatus(Reservation reservation) {
    boolean validated = true;
    String reservationStatus = ResourceStatus.STATUS_FOR_VALIDATION;
    List<ReservedResource> reservedResources = reservedResourceRepository.
        findAllReservedResourcesForReservation(reservation.getId());
    for (ReservedResource reservedResource : reservedResources) {
      String status = reservedResource.getStatus();
      if (ResourceStatus.STATUS_FOR_VALIDATION.equals(status)) {
        validated = false;
      }
      if (ResourceStatus.STATUS_REFUSED.equals(status)) {
        return ResourceStatus.STATUS_REFUSED;
      }
    }
    if (!validated) {
      return ResourceStatus.STATUS_FOR_VALIDATION;
    }
    return ResourceStatus.STATUS_VALIDATE;
  }

  public void updateReservation(Reservation reservation) {
    repository.saveAndFlush(reservation);
  }

  public Reservation getReservation(long id) {
    return repository.findOne(id);
  }

  public void deleteReservation(long id) {
    reservedResourceRepository.deleteAllReservedResourcesForReservation(id);
    repository.delete(id);
  }

  public List<Reservation> findAllReservations(String instanceId) {
    return repository.findAllReservations(instanceId);
  }

  public List<Reservation> findAllReservationsForUser(String instanceId, Integer userId) {
    return repository.findAllReservationsForUser(instanceId, userId);
  }

  public List<Reservation> findAllReservationsForValidation(String instanceId, Long userId,
      String startPeriod, String endPeriod) {
    return repository.findAllReservationsForValidation(instanceId, userId, startPeriod, endPeriod);
  }
  
  public List<Reservation> findAllReservationsForUserInRange(String instanceId, Integer userId,
      String startPeriod, String endPeriod) {
    return repository.findAllReservationsForUserInRange(instanceId, userId, startPeriod, endPeriod);
  }

  public List<Reservation> findAllReservationsForCategoryInRange(Long categoryId,
      String startPeriod, String endPeriod) {
    return repository.findAllReservationsForCategoryInRange(categoryId, startPeriod, endPeriod);
  }
}
