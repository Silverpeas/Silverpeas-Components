/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.resourcesmanager.service;

import org.silverpeas.components.resourcesmanager.model.Reservation;
import org.silverpeas.components.resourcesmanager.model.ReservedResource;
import org.silverpeas.components.resourcesmanager.model.Resource;
import org.silverpeas.components.resourcesmanager.model.ResourceStatus;
import org.silverpeas.components.resourcesmanager.repository.ReservationRepository;
import org.silverpeas.components.resourcesmanager.repository.ReservedResourceRepository;
import org.silverpeas.components.resourcesmanager.repository.ResourceRepository;
import org.silverpeas.components.resourcesmanager.repository.ResourceValidatorRepository;
import org.silverpeas.core.annotation.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
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

  @Inject
  private ResourceValidatorRepository resourceValidatorRepository;

  public void createReservation(Reservation reservation, List<Long> resourceIds) {
    reservation.setStatus(ResourceStatus.STATUS_VALIDATE);
    repository.save(reservation);
    for (Long resourceId : resourceIds) {
      ReservedResource reservedResource = new ReservedResource();
      reservedResource.setReservedResourceId(Long.toString(resourceId), reservation.getId());
      Resource resource = resourceRepository.getById(Long.toString(resourceId));
      if (!resource.getManagers().isEmpty()) {
        if (resourceValidatorRepository
            .getResourceValidator(resourceId, Long.parseLong(reservation.getUserId())) == null) {
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
    List<ReservedResource> reservedResources = reservedResourceRepository.
        findAllReservedResourcesForReservation(reservation.getIdAsLong());
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
    return repository.getById(Long.toString(id));
  }

  public void deleteReservation(long id) {
    reservedResourceRepository.deleteAllReservedResourcesForReservation(id);
    repository.deleteById(Long.toString(id));
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

  /**
   * Finds all reservations related to the given user on the given period.
   * If user parameter (userId) is not defined, the reservations returned are not filtered by user.
   * @param instanceId
   * @param userId
   * @param startPeriod
   * @param endPeriod
   * @return
   */
  public List<Reservation> findAllReservationsInRange(String instanceId, Integer userId,
      String startPeriod, String endPeriod) {
    if (userId == null) {
      return repository.findAllReservationsInRange(instanceId, startPeriod, endPeriod);
    }
    return repository.findAllReservationsForUserInRange(instanceId, userId, startPeriod, endPeriod);
  }

  /**
   * Finds all reservations related to the given user on the given period and for which at least
   * one
   * resource of the given category is attached.
   * If user parameter (userId) is not defined, the reservations returned are not filtered by user.
   * @param instanceId
   * @param userId
   * @param categoryId
   * @param startPeriod
   * @param endPeriod
   * @return
   */
  public List<Reservation> findAllReservationsForCategoryInRange(final String instanceId,
      Integer userId, Long categoryId, String startPeriod, String endPeriod) {
    if (userId == null) {
      return repository
          .findAllReservationsForCategoryInRange(instanceId, categoryId, startPeriod, endPeriod);
    }
    return repository
        .findAllReservationsForUserAndCategoryInRange(instanceId, userId, categoryId, startPeriod,
            endPeriod);
  }

  /**
   * Finds all reservations related to the given user on the given period and for which the given
   * resource is attached.
   * If user parameter (userId) is not defined, the reservations returned are not filtered by user.
   * @param instanceId
   * @param userId
   * @param resourceId
   * @param startPeriod
   * @param endPeriod
   * @return
   */
  public List<Reservation> findAllReservationsForResourceInRange(final String instanceId,
      Integer userId, Long resourceId, String startPeriod, String endPeriod) {
    if (userId == null) {
      return repository
          .findAllReservationsForResourceInRange(instanceId, resourceId, startPeriod, endPeriod);
    }
    return repository
        .findAllReservationsForUserAndResourceInRange(instanceId, userId, resourceId, startPeriod,
            endPeriod);
  }
}
