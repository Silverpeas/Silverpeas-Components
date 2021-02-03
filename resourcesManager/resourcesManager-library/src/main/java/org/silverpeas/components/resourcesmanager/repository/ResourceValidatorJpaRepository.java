/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

package org.silverpeas.components.resourcesmanager.repository;

import org.silverpeas.components.resourcesmanager.model.ResourceValidator;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.datasource.repository.jpa.BasicJpaEntityRepository;

/**
 * @author ebonnet
 */
@Repository
public class ResourceValidatorJpaRepository extends BasicJpaEntityRepository<ResourceValidator>
    implements ResourceValidatorRepository {

  @Override
  public ResourceValidator getResourceValidator(final Long currentResourceId,
      final Long currentUserId) {
    return getFromNamedQuery("resourceValidator.getResourceValidator", newNamedParameters()
        .add("resourceId", currentResourceId).add("currentUserId", currentUserId));
  }

  /**
   * Deletes all entities belonging to the specified component instance.
   * @param instanceId the unique instance identifier.
   * @return the number of deleted entities.
   */
  @Override
  public long deleteByComponentInstanceId(final String instanceId) {
    return deleteFromNamedQuery("resourceValidator.deleteAllResourceValidatorsForComponentInstance",
        newNamedParameters().add("instanceId", instanceId));
  }
}
