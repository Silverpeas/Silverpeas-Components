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

import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.notification.ProfileInstEvent;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.notification.system.CDIResourceEventListener;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Listens for update or creation of a profile instances for the Resource Manager applications. Its
 * goal is to be notified when a user doesn't play anymore the responsible role in a given Resource
 * Manager application and hence cannot be a validator of reservable resources.
 *
 * @author mmoquillon
 */
@Service
public class ProfileInstEventListener extends CDIResourceEventListener<ProfileInstEvent> {

  private static final String COMPONENT_NAME = "resourcesManager";
  private static final String MANAGER_ROLE_NAME = "responsable";

  @Inject
  private OrganizationController organization;
  @Inject
  private ResourcesManagersSynchronizer synchronizer;

  /**
   * one or more users have been removed, either directly or through groups removing, from the
   * specified profile. Only the publisher and writer profiles are taken in charge as these profiles
   * are those which feeds the responsible role in the application.
   *
   * @param event the event on the update of a resource.
   */
  @Override
  @Transactional
  public void onUpdate(ProfileInstEvent event) {
    ProfileInst before = event.getTransition().getBefore();
    ProfileInst after = event.getTransition().getAfter();
    if (isOnResponsibleRole(before)) {
      String instanceId = COMPONENT_NAME + after.getComponentFatherId();
      Set<String> removedUsers = findRemovedUsersId(instanceId, before, after);
      synchronizer.synchronize(instanceId, removedUsers);
    }
  }

  /**
   * Finds the users that were removed, either directly or through groups, from the specified role
   * profile of the related Resources Manager application. The users or the groups that are removed
   * by the profile instance update are those present in the role profile before the update and that
   * aren't anymore present in the role profile after the update.
   *
   * @param instanceId the unique identifier of the Resources Manager application.
   * @param before the state of the role profile before update.
   * @param after the state of the role profile after update.
   * @return a set with the unique identifiers of all of the users that were removed by the profile
   * instance update.
   */
  private Set<String> findRemovedUsersId(String instanceId, ProfileInst before, ProfileInst after) {
    List<String> usersAfter = after.getAllUsers();
    List<String> groupsAfter = after.getAllGroups();
    // get all the users directly removed from the profile instance and who's not anymore
    // declared among the Manager role profile of the application
    Stream<String> removedUsers = before.getAllUsers().stream()
        .filter(user -> !usersAfter.contains(user))
        .filter(u -> Stream.of(organization.getUserProfiles(u, instanceId))
            .noneMatch(p -> p.equalsIgnoreCase(MANAGER_ROLE_NAME)));


    // get all the users belonging to the groups removed from the profile instance and who's not
    // anymore declared among the Manager role profile of the application
    Stream<String> removedUsersInGroups = before.getAllGroups().stream()
        .filter(group -> !groupsAfter.contains(group))
        .map(g -> organization.getGroup(g))
        .flatMap(g -> Stream.of(((Group) g).getUserIds()))
        .distinct()
        .filter(u -> Stream.of(organization.getUserProfiles(u, instanceId))
            .noneMatch(p -> p.equalsIgnoreCase(MANAGER_ROLE_NAME)));

    return Stream.concat(removedUsers, removedUsersInGroups).collect(Collectors.toSet());
  }

  private boolean isOnResponsibleRole(ProfileInst profileInst) {
    return profileInst.getName().equalsIgnoreCase(MANAGER_ROLE_NAME);
  }
}
  