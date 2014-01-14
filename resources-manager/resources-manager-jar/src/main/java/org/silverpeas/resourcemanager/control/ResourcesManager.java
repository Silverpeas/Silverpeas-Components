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
package org.silverpeas.resourcemanager.control;

import java.util.Date;
import java.util.List;

import org.silverpeas.date.Period;
import org.silverpeas.date.PeriodType;
import org.silverpeas.resourcemanager.model.Category;
import org.silverpeas.resourcemanager.model.Reservation;
import org.silverpeas.resourcemanager.model.Resource;
import org.silverpeas.resourcemanager.model.ResourceValidator;

/**
 *
 * @author ehugonnet
 */
public interface ResourcesManager {

  public List<Category> getCategories(String instanceId);

  public void createCategory(Category category);

  public void deleteCategory(Long id, String componentId);

  public Category getCategory(Long id);

  public void updateCategory(Category category);

  public void createResource(Resource resource);

  public List<Resource> getResourcesByCategory(Long categoryId);

  public void deleteResource(Long id, String componentId);

  public Resource getResource(Long id);
  
  public void updateResource(Resource resource, List<Long> managerIds);

  public List<Resource> getResourcesReservable(String instanceId, Date startDate, Date endDate);

  public List<Resource> getReservedResources(String instanceId, List<Long> resources,
      Date startDate, Date endDate);

  public void saveReservation(Reservation reservation, List<Long> resourceIds);

  public List<Reservation> getReservations(String instanceId);

  public List<Resource> getResourcesOfReservation(String instanceId, Long reservationId);

  public void deleteReservation(Long id, String componentId);

  public Reservation getReservation(String instanceId, Long reservationId);

  public void updateReservation(Reservation reservation, List<Long> resourceIds,
      boolean updateDate);

  public List<Resource> getReservedResources(String instanceId, List<Long> aimedResourceIds,
      Date startDate, Date endDate, Long reservationIdToSkip);

  public List<Reservation> getUserReservations(String instanceId, String userId);
  
  public List<Reservation> getReservationOfUser(String instanceId, Integer userId,
      final Period period);

  public List<Reservation> getReservationForValidation(String instanceId, String userId,
      final Period period);

  public List<Reservation> getReservationWithResourcesOfCategory(final String instanceId,
      Integer userId, final Period period, Long categoryId);

  public List<Reservation> getReservationWithResource(final String instanceId, Integer userId,
      Period period, Long resourceId);

  public void indexResourceManager(String instanceId);

  public List<ResourceValidator> getManagers(long resourceId);

  public String getResourceOfReservationStatus(Long resourceId, Long reservationId);

  public void updateReservedResourceStatus(long reservationId, long resourceId, String status);

  public boolean isManager(long userId, long resourceId);
}
