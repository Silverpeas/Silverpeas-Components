/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
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

import javax.ejb.EJBObject;

import com.silverpeas.resourcesmanager.model.CategoryDetail;
import com.silverpeas.resourcesmanager.model.ReservationDetail;
import com.silverpeas.resourcesmanager.model.ResourceDetail;

/**
 * @author
 */
public interface ResourcesManagerBm extends EJBObject {
  public List getCategories(String instanceId) throws RemoteException;

  public void createCategory(CategoryDetail category) throws RemoteException;

  public void deleteCategory(String id, String componentId)
      throws RemoteException;

  public CategoryDetail getCategory(String id) throws RemoteException;

  public void updateCategory(CategoryDetail category) throws RemoteException;

  public String createResource(ResourceDetail resource) throws RemoteException;

  public List getResourcesByCategory(String categoryId) throws RemoteException;

  public void deleteResource(String id, String componentId)
      throws RemoteException;

  public ResourceDetail getResource(String id) throws RemoteException;

  public void updateResource(ResourceDetail resource) throws RemoteException;

  public List getResourcesReservable(String instanceId, Date startDate,
      Date endDate) throws RemoteException;

  public List verificationReservation(String instanceId,
      String listeReservation, Date startDate, Date endDate)
      throws RemoteException;

  public void saveReservation(ReservationDetail reservation,
      String listReservationCurrent) throws RemoteException;

  public List getReservationUser(String instanceId, String userId)
      throws RemoteException;

  public List getReservations(String instanceId) throws RemoteException;

  public List getResourcesofReservation(String instanceId, String reservationId)
      throws RemoteException;

  public void deleteReservation(String id, String componentId)
      throws RemoteException;

  public ReservationDetail getReservation(String instanceId,
      String reservationId) throws RemoteException;

  public void updateReservation(String listReservation,
      ReservationDetail reservationCourante) throws RemoteException;

  public List verificationNewDateReservation(String instanceId,
      String listeReservation, Date startDate, Date endDate,
      String reservationId) throws RemoteException;

  public List getMonthReservation(String instanceId, Date MonthDate,
      String userId, String language) throws RemoteException;

  public List getMonthReservationOfCategory(String instanceId, Date MonthDate,
      String userId, String language, String idCategory) throws RemoteException;

  public void indexResourceManager(String instanceId) throws RemoteException;
}