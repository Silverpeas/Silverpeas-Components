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
package com.silverpeas.resourcesmanager.control.ejb;

import java.rmi.RemoteException;
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

  public List<Category> getCategories(String instanceId) throws RemoteException;

  public void createCategory(Category category) throws RemoteException;

  public void deleteCategory(String id, String componentId)
      throws RemoteException;

  public Category getCategory(String id) throws RemoteException;

  public void updateCategory(Category category) throws RemoteException;

  public String createResource(Resource resource) throws RemoteException;

  public List<Resource> getResourcesByCategory(String categoryId) throws RemoteException;

  public void deleteResource(String id, String componentId)
      throws RemoteException;

  public Resource getResource(String id) throws RemoteException;

  public void updateResource(Resource resource) throws RemoteException;

  public List<Resource> getResourcesReservable(String instanceId, Date startDate, Date endDate)
      throws RemoteException;

  public List<Resource> verificationReservation(String instanceId, String listeReservation,
      Date startDate, Date endDate) throws RemoteException;

  public void saveReservation(Reservation reservation, String listReservationCurrent) throws
      RemoteException;

  public List<Reservation> getReservations(String instanceId) throws RemoteException;

  public List<Resource> getResourcesofReservation(String instanceId, String reservationId)
      throws RemoteException;

  public void deleteReservation(String id, String componentId) throws RemoteException;

  public Reservation getReservation(String instanceId, String reservationId) throws RemoteException;

  public void updateReservation(Reservation reservationCourante, String listReservation,
      boolean updateDate) throws RemoteException;

  public List<Resource> verificationNewDateReservation(String instanceId, String listeReservation,
      Date startDate, Date endDate, String reservationId) throws RemoteException;

  public List<Reservation> getMonthReservation(String instanceId, Date monthDate, String userId)
      throws RemoteException;

  public List<Reservation> getUserReservations(String instanceId, String userId)
      throws RemoteException;

  public List<Reservation> getReservationForValidation(String instanceId, Date monthDate,
      String userId) throws RemoteException;

  public List<Reservation> getMonthReservationOfCategory(Date monthDate, String idCategory) throws
      RemoteException;

  public List<Reservation> listReservationsOfMonthInCategoryForUser(Date monthDate, String idCategory, String userId) throws
        RemoteException;

  public void indexResourceManager(String instanceId) throws RemoteException;

  public void addManager(long resourceId, long managerId) throws RemoteException;

  public void addManagers(long resourceId, List<Long> managers) throws RemoteException;

  public void removeManager(long resourceId, long managerId) throws RemoteException;

  public List<ResourceValidator> getManagers(long resourceId) throws RemoteException;

  public String getResourceOfReservationStatus(String resourceId, String reservationId) throws
      RemoteException;

  public void updateReservedResourceStatus(long reservationId, long resourceId, String status) throws
      RemoteException;
}
