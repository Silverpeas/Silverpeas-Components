/*
 * Copyright (C) 2000 - 2020 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.resourcesmanager;

import org.silverpeas.core.admin.component.ComponentInstancePreDestruction;
import org.silverpeas.components.resourcesmanager.repository.ResourceRepository;
import org.silverpeas.components.resourcesmanager.repository.CategoryRepository;
import org.silverpeas.components.resourcesmanager.repository.ReservationRepository;
import org.silverpeas.components.resourcesmanager.repository.ReservedResourceRepository;
import org.silverpeas.components.resourcesmanager.repository.ResourceValidatorRepository;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

/**
 * Deletes all the resources and categories related to the ResourcesManager instance that is being
 * deleted.
 * @author mmoquillon
 */
@Named
public class ResourcesManagerInstancePreDestruction implements ComponentInstancePreDestruction {

  @Inject
  private CategoryRepository categoryRepository;
  @Inject
  private ReservationRepository reservationRepository;
  @Inject
  private ReservedResourceRepository reservedResourceRepository;
  @Inject
  private ResourceRepository resourceRepository;
  @Inject
  private ResourceValidatorRepository resourceValidatorRepository;

  /**
   * Performs pre destruction tasks in the behalf of the specified ResourcesManager instance.
   * @param componentInstanceId the unique identifier of the ResourcesManager instance.
   */
  @Override
  @Transactional
  public void preDestroy(final String componentInstanceId) {
    reservedResourceRepository.deleteByComponentInstanceId(componentInstanceId);
    reservationRepository.deleteByComponentInstanceId(componentInstanceId);
    resourceValidatorRepository.deleteByComponentInstanceId(componentInstanceId);
    resourceRepository.deleteByComponentInstanceId(componentInstanceId);
    categoryRepository.deleteByComponentInstanceId(componentInstanceId);
  }
}
