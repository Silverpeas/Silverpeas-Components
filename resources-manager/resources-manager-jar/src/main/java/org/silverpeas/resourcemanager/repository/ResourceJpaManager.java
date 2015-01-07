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
import org.silverpeas.resourcemanager.model.Resource;
import org.silverpeas.resourcemanager.model.ResourceValidator;

import java.util.List;

/**
 * @author ebonnet
 */
public class ResourceJpaManager extends JpaBasicEntityManager<Resource, UniqueLongIdentifier>
    implements ResourceRepository {

  @Override
  public List<Resource> findAllResourcesByCategory(final Long categoryId) {
    return listFromNamedQuery("resource.findAllResourcesByCategory",
        newNamedParameters().add("categoryId", UniqueLongIdentifier.from(categoryId)));
  }

  @Override
  public List<Resource> findAllBookableResources(final String instanceId) {
    return listFromNamedQuery("resource.findAllBookableResources",
        newNamedParameters().add("instanceId", instanceId));
  }

  @Override
  public List<Resource> findAllResourcesForReservation(final Long reservationId) {
    return listFromNamedQuery("reservedResource.findAllResourcesForReservation",
        newNamedParameters().add("reservationId", reservationId));
  }

  @Override
  public List<Resource> findAllReservedResources(final Long reservationIdToSkip,
      final List<Long> aimedResourceIds, final String startPeriod, final String endPeriod) {
    return listFromNamedQuery("reservedResource.findAllReservedResources",
        newNamedParameters().add("reservationIdToSkip", reservationIdToSkip)
            .add("aimedResourceIds", aimedResourceIds).add("startPeriod", startPeriod)
            .add("endPeriod", endPeriod));
  }

  @Override
  public void deleteResourcesFromCategory(final Long categoryId) {
    deleteFromNamedQuery("resource.deleteResourcesFromCategory",
        newNamedParameters().add("categoryId", UniqueLongIdentifier.from(categoryId)));
  }
}
