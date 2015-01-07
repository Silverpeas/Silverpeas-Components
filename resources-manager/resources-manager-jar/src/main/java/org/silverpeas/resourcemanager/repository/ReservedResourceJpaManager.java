/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.resourcemanager.repository;

import org.silverpeas.persistence.model.identifier.UniqueLongIdentifier;
import org.silverpeas.persistence.repository.jpa.JpaBasicEntityManager;
import org.silverpeas.resourcemanager.model.ReservedResource;
import org.silverpeas.resourcemanager.model.ReservedResourcePk;

import java.util.List;

/**
 * @author ebonnet
 */
public class ReservedResourceJpaManager
    extends JpaBasicEntityManager<ReservedResource, ReservedResourcePk>
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
}
