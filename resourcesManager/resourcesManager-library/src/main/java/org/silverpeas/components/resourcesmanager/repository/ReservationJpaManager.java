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

package org.silverpeas.components.resourcesmanager.repository;

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.datasource.model.identifier.UniqueLongIdentifier;
import org.silverpeas.core.persistence.datasource.repository.jpa.BasicJpaEntityRepository;
import org.silverpeas.components.resourcesmanager.model.Reservation;

import java.util.List;

/**
 * @author ebonnet
 */
@Repository
public class ReservationJpaManager extends BasicJpaEntityRepository<Reservation>
    implements ReservationRepository {

  @Override
  public List<Reservation> findAllReservationsInRange(final String instanceId,
      final String startPeriod, final String endPeriod) {
    return listFromNamedQuery("reservation.findAllReservationsInRange",
        newNamedParameters().add("instanceId", instanceId).add("startPeriod", startPeriod)
            .add("endPeriod", endPeriod));
  }

  @Override
  public List<Reservation> findAllReservationsForUserInRange(final String instanceId,
      final Integer userId, final String startPeriod, final String endPeriod) {
    return listFromNamedQuery("reservation.findAllReservationsForUserInRange",
        newNamedParameters().add("instanceId", instanceId).add("userId", userId)
            .add("startPeriod", startPeriod).add("endPeriod", endPeriod));
  }

  @Override
  public List<Reservation> findAllReservationsForUser(final String instanceId,
      final Integer userId) {
    return listFromNamedQuery("reservation.findAllReservationsForUser",
        newNamedParameters().add("instanceId", instanceId).add("userId", userId));
  }

  @Override
  public List<Reservation> findAllReservationsForValidation(final String instanceId,
      final Long managerId, final String startPeriod, final String endPeriod) {
    return listFromNamedQuery("reservation.findAllReservationsForValidation",
        newNamedParameters().add("instanceId", instanceId).add("managerId", managerId)
            .add("startPeriod", startPeriod).add("endPeriod", endPeriod));
  }

  @Override
  public List<Reservation> findAllReservationsNotRefusedForResourceInRange(final Long resourceId,
      final String startPeriod, final String endPeriod) {
    return listFromNamedQuery("reservation.findAllReservationsNotRefusedForResourceInRange",
        newNamedParameters().add("resourceId", UniqueLongIdentifier.from(resourceId))
            .add("startPeriod", startPeriod).add("endPeriod", endPeriod));
  }

  @Override
  public List<Reservation> findAllReservationsForCategoryInRange(final String instanceId,
      final Long categoryId, final String startPeriod, final String endPeriod) {
    return listFromNamedQuery("reservation.findAllReservationsForCategoryInRange",
        newNamedParameters().add("instanceId", instanceId)
            .add("categoryId", UniqueLongIdentifier.from(categoryId))
            .add("startPeriod", startPeriod).add("endPeriod", endPeriod));
  }

  @Override
  public List<Reservation> findAllReservationsForUserAndCategoryInRange(final String instanceId,
      final Integer userId, final Long categoryId, final String startPeriod,
      final String endPeriod) {
    return listFromNamedQuery("reservation.findAllReservationsForUserAndCategoryInRange",
        newNamedParameters().add("instanceId", instanceId).add("userId", userId)
            .add("categoryId", UniqueLongIdentifier.from(categoryId))
            .add("startPeriod", startPeriod).add("endPeriod", endPeriod));
  }

  @Override
  public List<Reservation> findAllReservationsForResourceInRange(final String instanceId,
      final Long resourceId, final String startPeriod, final String endPeriod) {
    return listFromNamedQuery("reservation.findAllReservationsForResourceInRange",
        newNamedParameters().add("instanceId", instanceId)
            .add("resourceId", UniqueLongIdentifier.from(resourceId))
            .add("startPeriod", startPeriod).add("endPeriod", endPeriod));
  }

  @Override
  public List<Reservation> findAllReservationsForUserAndResourceInRange(final String instanceId,
      final Integer userId, final Long resourceId, final String startPeriod,
      final String endPeriod) {
    return listFromNamedQuery("reservation.findAllReservationsForUserAndResourceInRange",
        newNamedParameters().add("instanceId", instanceId).add("userId", userId)
            .add("resourceId", UniqueLongIdentifier.from(resourceId))
            .add("startPeriod", startPeriod).add("endPeriod", endPeriod));
  }

  @Override
  public List<Reservation> findAllReservations(final String instanceId) {
    return listFromNamedQuery("reservation.findAllReservations",
        newNamedParameters().add("instanceId", instanceId));
  }
}
