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
package org.silverpeas.resourcemanager.repository;

import org.silverpeas.resourcemanager.model.ReservedResource;
import org.silverpeas.resourcemanager.model.ReservedResourcePk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 *
 * @author ehugonnet
 */
public interface ReservedResourceRepository extends
    JpaRepository<ReservedResource, ReservedResourcePk> {

  @Query("SELECT DISTINCT reservedResource FROM ReservedResource reservedResource " +
  "WHERE reservedResource.reservation.id != :currentReservationId AND reservedResource.status != 'R'" +
  "AND reservedResource.resource.id IN :futureReservedResourceIds " +
  "AND (( reservedResource.reservation.endDate > :startPeriod AND  reservedResource.reservation.beginDate <= :startPeriod)" +
  "OR ( reservedResource.reservation.endDate >= :endPeriod  AND  reservedResource.reservation.beginDate < :endPeriod))")
  public List<ReservedResource> findAllReservedResourcesWithProblem(
      @Param("currentReservationId") Long currentReservationId,
      @Param("futureReservedResourceIds") List<Long> futureReservedResourceIds,
      @Param("startPeriod") String startPeriod, @Param("endPeriod") String endPeriod);

  @Query("SELECT DISTINCT reservedResource FROM ReservedResource reservedResource WHERE reservedResource.reservation.id = :currentReservationId")
  public List<ReservedResource> findAllReservedResourcesForReservation(
      @Param("currentReservationId") Long currentReservationId);

  @Modifying
  @Query("DELETE ReservedResource reservedResource WHERE reservedResource.reservedResourcePk.reservationId = :currentReservationId")
  public void deleteAllReservedResourcesForReservation(
      @Param("currentReservationId") Long currentReservationId);

  @Modifying
  @Query("DELETE ReservedResource reservedResource WHERE reservedResource.reservedResourcePk.resourceId = :currentResourceId")
  public void deleteAllReservedResourcesForResource(
      @Param("currentResourceId") Long currentResourceId);

  @Query("SELECT DISTINCT reservedResource FROM ReservedResource reservedResource WHERE reservedResource.reservedResourcePk.reservationId = :currentReservationId")
  public List<ReservedResource> findAllReservedResourcesOfReservation(
      @Param("currentReservationId") Long currentReservationId);
}
