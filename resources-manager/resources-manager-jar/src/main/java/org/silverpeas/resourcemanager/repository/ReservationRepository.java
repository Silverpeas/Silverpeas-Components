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

import org.silverpeas.resourcemanager.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 *
 * @author ehugonnet
 */
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

  @Query("from Reservation reservation WHERE reservation.instanceId = :instanceId AND reservation.userId= :userId " +
  "AND reservation.beginDate < :endPeriod " +
  "AND reservation.endDate > :startPeriod ")
  public List<Reservation> findAllReservationsForUserInRange(@Param("instanceId") String instanceId,
      @Param("userId") Integer userId, @Param("startPeriod") String startPeriod,
      @Param("endPeriod") String endPeriod);

  @Query("from Reservation reservation WHERE reservation.instanceId = :instanceId AND reservation.userId= :userId")
  public List<Reservation> findAllReservationsForUser(@Param("instanceId") String instanceId,
      @Param("userId") Integer userId);

  @Query("SELECT DISTINCT reservedResource.reservation FROM ReservedResource reservedResource " +
  "JOIN reservedResource.resource.managers manager WHERE reservedResource.status = 'A' " +
  "AND manager.resourceValidatorPk.managerId = :managerId AND reservedResource.reservation.instanceId = :instanceId " +
  "AND reservedResource.reservation.beginDate < :endPeriod " +
  "AND reservedResource.reservation.endDate > :startPeriod ")
  public List<Reservation> findAllReservationsForValidation(@Param("instanceId") String instanceId,
      @Param("managerId") Long managerId, @Param("startPeriod") String startPeriod,
      @Param("endPeriod") String endPeriod);
  
  @Query("SELECT DISTINCT reservedResource.reservation FROM ReservedResource reservedResource " +
  "WHERE reservedResource.resource.id = :resourceId AND reservedResource.status != 'R' " +
  "AND reservedResource.reservation.beginDate < :endPeriod " +
  "AND reservedResource.reservation.endDate > :startPeriod ")
  public List<Reservation> findAllReservationsNotRefusedForResourceInRange(
      @Param("resourceId") Long resourceId, @Param("startPeriod") String startPeriod,
      @Param("endPeriod") String endPeriod);

  @Query("SELECT DISTINCT reservedResource.reservation FROM ReservedResource reservedResource " +
  "WHERE reservedResource.resource.category.id = :categoryId " +
  "AND reservedResource.reservation.beginDate < :endPeriod " +
  "AND reservedResource.reservation.endDate > :startPeriod ")
  public List<Reservation> findAllReservationsForCategoryInRange(
      @Param("categoryId") Long categoryId, @Param("startPeriod") String startPeriod,
      @Param("endPeriod") String endPeriod);

  @Query(value = "SELECT DISTINCT reservation FROM Reservation reservation WHERE reservation.instanceId = :instanceId")
  public List<Reservation> findAllReservations(@Param("instanceId") String instanceId);
}
