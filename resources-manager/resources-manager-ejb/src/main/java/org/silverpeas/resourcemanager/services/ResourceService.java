/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.resourcemanager.services;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.silverpeas.resourcemanager.model.Reservation;
import org.silverpeas.resourcemanager.model.ReservedResource;
import org.silverpeas.resourcemanager.model.Resource;
import org.silverpeas.resourcemanager.model.ResourceValidator;
import org.silverpeas.resourcemanager.repository.ReservationRepository;
import org.silverpeas.resourcemanager.repository.ReservedResourceRepository;
import org.silverpeas.resourcemanager.repository.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Named
@Service
@Transactional
public class ResourceService {

  @Inject
  ResourceRepository repository;
  @Inject
  ReservationRepository reservationRepository;
  @Inject
  private ReservedResourceRepository reservedResourceRepository;

  public String createResource(Resource resource) {
    Resource savedResource = repository.saveAndFlush(resource);
    return savedResource.getId();
  }

  public void updateResource(Resource resource) {
    repository.saveAndFlush(resource);
  }

  public List<Resource> getResources() {
    return repository.findAll();
  }

  public Resource getResource(long id) {
    return repository.findOne(id);
  }

  public void deleteResource(long id) {
    reservedResourceRepository.deleteAllReservedResourcesForResource(id);
    repository.delete(id);
  }

  public void deleteResourcesFromCategory(Long categoryId) {
    List<Resource> listOfResources = repository.findAllResourcesByCategory(categoryId);
    for (Resource resource : listOfResources) {
      deleteResource(resource.getIntegerId());
    }
  }

  public void addManagers(long resourceId, List<ResourceValidator> managerIds) {
    Resource resource = repository.findOne(resourceId);
    for (ResourceValidator manager : managerIds) {
      resource.getManagers().add(manager);
    }
    repository.saveAndFlush(resource);
  }

  public void addManager(ResourceValidator manager) {
    Resource resource = repository.findOne(manager.getResourceId());
    resource.getManagers().add(manager);
    repository.saveAndFlush(resource);
  }

  public List<ResourceValidator> getManagers(long resourceId) {
    Resource resource = repository.findOne(resourceId);
    return resource.getManagers();
  }

  public void removeAllManagers(long resourceId) {
    Resource resource = repository.findOne(resourceId);
    resource.getManagers().clear();
    repository.saveAndFlush(resource);
  }

  public void removeManager(ResourceValidator manager) {
    Resource resource = repository.findOne(manager.getResourceId());
    resource.getManagers().remove(manager);
    repository.saveAndFlush(resource);
  }

  public List<Resource> getResourcesByCategory(Long categoryId) {
    return repository.findAllResourcesByCategory(categoryId);
  }

  public List<Resource> listAvailableResources(String instanceId, String startDate, String endDate) {
    List<Resource> bookableResources = repository.findAllBookableResources(instanceId);
    List<Resource> availableBookableResources = new ArrayList<Resource>(bookableResources.size());
    for (Resource resource : bookableResources) {
      List<Reservation> reservations = reservationRepository.
          findAllReservationsForValidatedResourceInRange(resource.getIntegerId(),
          startDate, endDate);
      if (reservations == null || reservations.isEmpty()) {
        availableBookableResources.add(resource);
      }
    }
    return availableBookableResources;
  }
  
  public boolean isManager(Long userId, Long resourceId) {
    return repository.getResourceValidator(resourceId, userId) != null;
  }

  public List<Resource> listResourcesOfReservation(Long reservationId) {
    return repository.findAllResourcesForReservation(reservationId);
  }

  public List<Resource> findAllResourcesWithProblem(long currentReservationId,
      List<Long> futureReservedResourceIds, String startPeriod, String endPeriod) {
    return repository.findAllResourcesWithProblem(currentReservationId,
        futureReservedResourceIds, startPeriod, endPeriod);
  }
}
