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

import org.silverpeas.core.date.period.Period;
import org.silverpeas.components.resourcesmanager.model.Category;
import org.silverpeas.components.resourcesmanager.model.Reservation;
import org.silverpeas.components.resourcesmanager.model.Resource;
import org.silverpeas.components.resourcesmanager.model.ResourceValidator;

import java.util.Date;
import java.util.List;

/**
 * Manager of the resources in a given Resource Manager application
 *
 * @author ehugonnet
 */
public interface ResourcesManager {

  List<Category> getCategories(String instanceId);

  void createCategory(Category category);

  void deleteCategory(Long id, String componentId);

  Category getCategory(Long id);

  void updateCategory(Category category);

  void createResource(Resource resource);

  List<Resource> getResourcesByCategory(Long categoryId);

  List<Resource> getBookableResources(String componentId);

  void deleteResource(Long id, String componentId);

  Resource getResource(Long id);

  void updateResource(Resource resource, List<Long> managerIds);

  List<Resource> getResourcesReservable(String instanceId, Date startDate, Date endDate);

  List<Resource> getReservedResources(String instanceId, List<Long> resources,
      Date startDate, Date endDate);

  void saveReservation(Reservation reservation, List<Long> resourceIds);

  List<Reservation> getReservations(String instanceId);

  List<Resource> getResourcesOfReservation(String instanceId, Long reservationId);

  void deleteReservation(Long id, String componentId);

  Reservation getReservation(String instanceId, Long reservationId);

  void updateReservation(Reservation reservation, List<Long> resourceIds,
      boolean updateDate);

  List<Resource> getReservedResources(String instanceId, List<Long> aimedResourceIds,
      Date startDate, Date endDate, Long reservationIdToSkip);

  List<Reservation> getUserReservations(String instanceId, String userId);

  List<Reservation> getReservationOfUser(String instanceId, Integer userId,
      final Period period);

  List<Reservation> getReservationForValidation(String instanceId, String userId,
      final Period period);

  List<Reservation> getReservationWithResourcesOfCategory(final String instanceId,
      Integer userId, final Period period, Long categoryId);

  List<Reservation> getReservationWithResource(final String instanceId, Integer userId,
      Period period, Long resourceId);

  void indexResourceManager(String instanceId);

  List<ResourceValidator> getManagers(long resourceId);

  String getResourceOfReservationStatus(Long resourceId, Long reservationId);

  void updateReservedResourceStatus(long reservationId, long resourceId, String status);

  boolean isManager(long userId, long resourceId);
}
