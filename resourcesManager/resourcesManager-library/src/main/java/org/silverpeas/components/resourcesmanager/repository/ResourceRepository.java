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

import org.silverpeas.components.resourcesmanager.model.Resource;
import org.silverpeas.core.persistence.datasource.repository.EntityRepository;
import org.silverpeas.core.persistence.datasource.repository.WithSaveAndFlush;

import java.util.List;

public interface ResourceRepository extends EntityRepository<Resource>, WithSaveAndFlush<Resource> {

  List<Resource> findAllResourcesByCategory(Long categoryId);

  List<Resource> findAllBookableResources(String instanceId);

  List<Resource> findAllResources(String instanceId);

  List<Resource> findAllResourcesForReservation(Long reservationId);

  List<Resource> findAllReservedResources(Long reservationIdToSkip, List<Long> aimedResourceIds,
      String startPeriod, String endPeriod);

  void deleteResourcesFromCategory(Long categoryId);
}