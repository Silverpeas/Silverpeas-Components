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

import org.silverpeas.components.resourcesmanager.model.ReservedResource;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.datasource.model.identifier.UniqueLongIdentifier;
import org.silverpeas.core.persistence.datasource.repository.jpa.BasicJpaEntityRepository;

import java.util.List;

/**
 * @author ebonnet
 */
@Repository
public class ReservedResourceJpaRepository extends BasicJpaEntityRepository<ReservedResource>
    implements ReservedResourceRepository {
  @Override
  public List<ReservedResource> findAllReservedResourcesWithProblem(final Long currentReservationId,
      final List<Long> futureReservedResourceIds, final String startPeriod,
      final String endPeriod) {
    return listFromNamedQuery("reservedResource.findAllReservedResourcesWithProblem",
        newNamedParameters()
            .add("currentReservationId", UniqueLongIdentifier.from(currentReservationId))
            .add("futureReservedResourceIds", futureReservedResourceIds)
            .add("startPeriod", startPeriod).add("endPeriod", endPeriod));
  }

  @Override
  public List<ReservedResource> findAllReservedResourcesForReservation(
      final Long currentReservationId) {
    return listFromNamedQuery("reservedResource.findAllReservedResourcesForReservation",
        newNamedParameters()
            .add("currentReservationId", UniqueLongIdentifier.from(currentReservationId)));
  }

  @Override
  public void deleteAllReservedResourcesForReservation(final Long currentReservationId) {
    deleteFromNamedQuery("reservedResource.deleteAllReservedResourcesForReservation",
        newNamedParameters().add("currentReservationId", currentReservationId));
  }

  @Override
  public void deleteAllReservedResourcesForResource(final Long currentResourceId) {
    deleteFromNamedQuery("reservedResource.deleteAllReservedResourcesForResource",
        newNamedParameters().add("currentResourceId", currentResourceId));
  }

  @Override
  public List<ReservedResource> findAllReservedResourcesOfReservation(
      final Long currentReservationId) {
    return listFromNamedQuery("reservedResource.findAllReservedResourcesOfReservation",
        newNamedParameters().add("currentReservationId", currentReservationId));
  }

  /**
   * Deletes all entities belonging to the specified component instance.
   * @param instanceId the unique instance identifier.
   * @return the number of deleted entities.
   */
  @Override
  public long deleteByComponentInstanceId(final String instanceId) {
    return deleteFromNamedQuery("reservedResource.deleteAllReservedResourcesForComponentInstance",
        newNamedParameters().add("instanceId", instanceId));
  }
}
