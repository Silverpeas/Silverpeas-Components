/*
 *  Copyright (C) 2000 - 2011 Silverpeas
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 * 
 *  As a special exception to the terms and conditions of version 3.0 of
 *  the GPL, you may redistribute this Program in connection with Free/Libre
 *  Open Source Software ("FLOSS") applications as described in Silverpeas's
 *  FLOSS exception.  You should have recieved a copy of the text describing
 *  the FLOSS exception, and it is also available here:
 *  "http://www.silverpeas.com/legal/licensing"
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.silverpeas.resourcemanager.repository;

import java.util.List;
import org.silverpeas.resourcemanager.model.Resource;
import org.silverpeas.resourcemanager.model.ResourceValidator;
import org.synyx.hades.dao.GenericDao;
import org.synyx.hades.dao.Modifying;
import org.synyx.hades.dao.Param;
import org.synyx.hades.dao.Query;

public interface ResourceDao extends GenericDao<Resource, Long> {

  @Query("from Resource resource WHERE resource.category.id = :categoryId")
  public List<Resource> findAllResourcesByCategory(@Param("categoryId") Long categoryId);

  @Query("from Resource resource WHERE resource.instanceId = :instanceId AND resource.bookable = 1 AND resource.category.bookable = 1")
  public List<Resource> findAllBookableResources(@Param("instanceId") String instanceId);

  @Query("SELECT DISTINCT reservedResource.resource FROM ReservedResource reservedResource WHERE reservedResource.reservedResourcePk.reservationId = :currentReservationId")
  public List<Resource> findAllResourcesForReservation(
      @Param("currentReservationId") Long currentReservationId);

  @Query("SELECT DISTINCT reservedResource.resource FROM ReservedResource reservedResource " +
  "WHERE reservedResource.reservation.id != :currentReservationId AND reservedResource.status != 'R'" +
  "AND reservedResource.resource.id IN :futureReservedResourceIds " +
  "AND (( reservedResource.reservation.endDate > :startPeriod AND  reservedResource.reservation.beginDate <= :startPeriod)" +
  "OR ( reservedResource.reservation.endDate >= :endPeriod  AND  reservedResource.reservation.beginDate < :endPeriod))")
  public List<Resource> findAllResourcesWithProblem(
      @Param("currentReservationId") Long currentReservationId,
      @Param("futureReservedResourceIds") List<Long> futureReservedResourceIds,
      @Param("startPeriod") String startPeriod, @Param("endPeriod") String endPeriod);
  
  
  @Query("SELECT DISTINCT resourceValidator FROM ResourceValidator resourceValidator " +
  "WHERE resourceValidator.resourceValidatorPk.managerId = :currentUserId AND resourceValidator.resourceValidatorPk.resourceId = :currentReservationId")
  public ResourceValidator getResourceValidator(
      @Param("currentReservationId") Long currentResourceId, @Param("currentUserId") Long currentUserId);
  
  @Modifying
  @Query("DELETE ResourceValidator resourceValidator WHERE resourceValidator.resourceValidatorPk.resourceId = :currentResourceId")
  public void deleteAllManagersOfResource(@Param("currentResourceId") Long currentResourceId);
  
  @Modifying
  @Query("DELETE Resource resource WHERE resource.category.id = :categoryId")
  public void deleteResourcesFromCategory(@Param("categoryId") Long categoryId);

}