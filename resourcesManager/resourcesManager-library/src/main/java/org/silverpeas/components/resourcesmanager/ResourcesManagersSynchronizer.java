/*
 * Copyright (C) 2000 - 2025 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.resourcesmanager;

import org.silverpeas.components.resourcesmanager.model.Resource;
import org.silverpeas.components.resourcesmanager.model.ResourceValidator;
import org.silverpeas.components.resourcesmanager.service.ResourceService;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.kernel.annotation.Defined;
import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.annotation.Technical;
import org.silverpeas.kernel.util.StringUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A synchronizer of the list of managers of all concerned resources with any modification of the
 * Manager role profile for a Resources Manager application.
 *
 * @author mmoquillon
 */
@Technical
@Service
@Singleton
public class ResourcesManagersSynchronizer {

  @Inject
  private ResourceService resourceService;

  /**
   * Synchronizes the list of managers of the resources managed by the specified Resources Manager
   * application and by taking into account the given users have been removed from the Manager role
   * profile for the application. The specified users could could have been explicitly removed or
   * have been in a group that has been removed from the Manager role profile.
   *
   * @param instanceId the unique identifier of a Resource Managers application.
   * @param removedUsersId a set with the unique identifiers of the removed users.
   */
  @Transactional
  public void synchronize(@Defined String instanceId, @NonNull Set<String> removedUsersId) {
    StringUtil.requireDefined(instanceId);
    Objects.requireNonNull(removedUsersId);
    List<Resource> resources = resourceService.getResources(instanceId);
    updateValidators(resources, removedUsersId);
  }

  private void updateValidators(List<Resource> resources, Set<String> removedUsersId) {
    resources.forEach(r -> {
      List<ResourceValidator> managerIds = r.getManagers().stream()
          .map(ResourceValidator::getManagerId)
          .filter(m -> !removedUsersId.contains(String.valueOf(m)))
          .map(m -> new ResourceValidator(r.getIdAsLong(), m))
          .collect(Collectors.toList());
      r.setManagers(managerIds);
      resourceService.updateResource(r);
    });
  }
}
  