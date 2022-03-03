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
package org.silverpeas.components.resourcesmanager.service;

import org.silverpeas.components.resourcesmanager.model.Reservation;
import org.silverpeas.components.resourcesmanager.model.Resource;
import org.silverpeas.components.resourcesmanager.model.ResourceValidator;
import org.silverpeas.components.resourcesmanager.repository.ReservationRepository;
import org.silverpeas.components.resourcesmanager.repository.ReservedResourceRepository;
import org.silverpeas.components.resourcesmanager.repository.ResourceRepository;
import org.silverpeas.components.resourcesmanager.repository.ResourceValidatorRepository;
import org.silverpeas.core.annotation.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ResourceService {

  @Inject
  ResourceRepository repository;
  @Inject
  ReservationRepository reservationRepository;
  @Inject
  private ReservedResourceRepository reservedResourceRepository;
  @Inject
  private ResourceValidatorRepository resourceValidatorRepository;

  public void createResource(Resource resource) {
    repository.saveAndFlush(resource);
  }

  public void updateResource(Resource resource) {
    repository.saveAndFlush(resource);
  }

  public List<Resource> getResources() {
    return repository.getAll();
  }

  public Resource getResource(long id) {
    return repository.getById(Long.toString(id));
  }

  public void deleteResource(long id) {
    reservedResourceRepository.deleteAllReservedResourcesForResource(id);
    repository.deleteById(Long.toString(id));
  }

  public void deleteResourcesFromCategory(Long categoryId) {
    repository.deleteResourcesFromCategory(categoryId);
  }

  public void addManagers(long resourceId, List<ResourceValidator> managerIds) {
    Resource resource = repository.getById(Long.toString(resourceId));
    for (ResourceValidator manager : managerIds) {
      manager.setResource(resource);
      resource.getManagers().add(manager);
    }
    repository.saveAndFlush(resource);
  }

  public void addManager(ResourceValidator manager) {
    Resource resource = repository.getById(Long.toString(manager.getResourceId()));
    resource.getManagers().add(manager);
    repository.saveAndFlush(resource);
  }

  public List<ResourceValidator> getManagers(long resourceId) {
    Resource resource = repository.getById(Long.toString(resourceId));
    return new ArrayList<>(resource.getManagers());
  }

  public void removeAllManagers(long resourceId) {
    Resource resource =  repository.getById(Long.toString(resourceId));
    resource.getManagers().clear();
    repository.saveAndFlush(resource);
  }

  public void removeManager(ResourceValidator manager) {
    Resource resource = repository.getById(Long.toString(manager.getResourceId()));
    resource.getManagers().remove(manager);
    repository.saveAndFlush(resource);
  }

  public List<Resource> getResourcesByCategory(Long categoryId) {
    return repository.findAllResourcesByCategory(categoryId);
  }

  public List<Resource> listAvailableResources(String instanceId, String startDate,
      String endDate) {
    List<Resource> bookableResources = repository.findAllBookableResources(instanceId);
    List<Resource> availableBookableResources = new ArrayList<>(bookableResources.size());
    for (Resource resource : bookableResources) {
      List<Reservation> reservations = reservationRepository.
          findAllReservationsNotRefusedForResourceInRange(resource.getIdAsLong(), startDate, endDate);
      if (reservations == null || reservations.isEmpty()) {
        availableBookableResources.add(resource);
      }
    }
    return availableBookableResources;
  }

  public boolean isManager(Long userId, Long resourceId) {
    return resourceValidatorRepository.getResourceValidator(resourceId, userId) != null;
  }

  public List<Resource> listResourcesOfReservation(Long reservationId) {
    return repository.findAllResourcesForReservation(reservationId);
  }

  public List<Resource> findAllReservedResources(long reservationIdToSkip,
      List<Long> aimedResourceIds, String startPeriod, String endPeriod) {
    return repository
        .findAllReservedResources(reservationIdToSkip, aimedResourceIds, startPeriod, endPeriod);
  }
}
