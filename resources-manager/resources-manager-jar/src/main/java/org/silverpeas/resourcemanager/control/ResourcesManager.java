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
package org.silverpeas.resourcemanager.control;

import java.util.Date;
import java.util.List;
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

  public void deleteCategory(String id, String componentId);

  public Category getCategory(String id);

  public void updateCategory(Category category);

  public String createResource(Resource resource);

  public List<Resource> getResourcesByCategory(String categoryId);

  public void deleteResource(String id, String componentId);

  public Resource getResource(String id);

  public void updateResource(Resource resource);
  
  public void updateResource(Resource resource, List<Long> managerIds);

  public List<Resource> getResourcesReservable(String instanceId, Date startDate, Date endDate);

  public List<Resource> verificationReservation(String instanceId, String listeReservation,
      Date startDate, Date endDate);

  public void saveReservation(Reservation reservation, String listReservationCurrent);

  public List<Reservation> getReservations(String instanceId);

  public List<Resource> getResourcesofReservation(String instanceId, String reservationId);

  public void deleteReservation(String id, String componentId);

  public Reservation getReservation(String instanceId, String reservationId);

  public void updateReservation(Reservation reservationCourante, String listReservation,
      boolean updateDate);

  public List<Resource> verificationNewDateReservation(String instanceId, String listeReservation,
      Date startDate, Date endDate, String reservationId);

  public List<Reservation> getMonthReservation(String instanceId, Date monthDate, String userId);

  public List<Reservation> getUserReservations(String instanceId, String userId);
  
  public List<Reservation> getMonthReservationOfUser(String instanceId, Date monthDate,
      Integer userId);

  public List<Reservation> getReservationForValidation(String instanceId, Date monthDate,
      String userId);

  public List<Reservation> getMonthReservationOfCategory(Date monthDate, String idCategory);

  public List<Reservation> listReservationsOfMonthInCategoryForUser(Date monthDate,
      String idCategory, String userId);

  public void indexResourceManager(String instanceId);

  public void addManager(long resourceId, long managerId);

  public void removeManager(long resourceId, long managerId);

  public List<ResourceValidator> getManagers(long resourceId);

  public String getResourceOfReservationStatus(String resourceId, String reservationId);

  public void updateReservedResourceStatus(long reservationId, long resourceId, String status);

  public boolean isManager(long userId, long resourceId);
}
