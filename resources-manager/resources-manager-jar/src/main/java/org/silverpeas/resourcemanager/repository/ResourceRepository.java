/*
 *  Copyright (C) 2000 - 2012 Silverpeas
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
 *  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

  @Query("from Resource resource WHERE resource.category.id = :categoryId")
  public List<Resource> findAllResourcesByCategory(@Param("categoryId") Long categoryId);

  @Query("from Resource resource WHERE resource.instanceId = :instanceId AND resource.bookable = 1 AND resource.category.bookable = 1")
  public List<Resource> findAllBookableResources(@Param("instanceId") String instanceId);

  @Query("SELECT DISTINCT reservedResource.resource FROM ReservedResource reservedResource WHERE reservedResource.reservedResourcePk.reservationId = :reservationId")
  public List<Resource> findAllResourcesForReservation(
      @Param("reservationId") Long reservationId);

  @Query("SELECT DISTINCT reservedResource.resource FROM ReservedResource reservedResource " +
  "WHERE reservedResource.reservation.id != :reservationIdToSkip AND reservedResource.status != 'R'" +
  "AND reservedResource.resource.id IN :aimedResourceIds " +
  "AND reservedResource.reservation.beginDate < :endPeriod " +
  "AND reservedResource.reservation.endDate > :startPeriod ")
  public List<Resource> findAllReservedResources(
      @Param("reservationIdToSkip") Long reservationIdToSkip,
      @Param("aimedResourceIds") List<Long> aimedResourceIds,
      @Param("startPeriod") String startPeriod, @Param("endPeriod") String endPeriod);


  @Query("SELECT DISTINCT resourceValidator FROM ResourceValidator resourceValidator " +
  "WHERE resourceValidator.resourceValidatorPk.managerId = :currentUserId AND resourceValidator.resourceValidatorPk.resourceId = :reservationId")
  public ResourceValidator getResourceValidator(
      @Param("reservationId") Long currentResourceId, @Param("currentUserId") Long currentUserId);

  @Modifying
  @Query("DELETE Resource resource WHERE resource.category.id = :categoryId")
  public void deleteResourcesFromCategory(@Param("categoryId") Long categoryId);
}