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
import org.silverpeas.components.resourcesmanager.service.ResourceService;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.notification.GroupUserLink;
import org.silverpeas.core.admin.user.notification.GroupUserLinkEvent;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.notification.system.CDIResourceEventListener;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Listens for removing of users in groups in order to remove also them from the validators of all
 * of the resources managed by the Resource Manager applications. The event is listening once the
 * transaction within which the user is removed from the group has been successfully commited. This
 * is to ensure the user doesn't belong anymore to a group or directly in the Manager role profile
 * of the Resources Manager applications when checked.
 *
 * @author mmoquillon
 */
@Service
public class GroupUserLinkEventListener extends CDIResourceEventListener<GroupUserLinkEvent> {

  @Inject
  private OrganizationController organization;
  @Inject
  private ResourceService resourceService;
  @Inject
  private ResourcesManagersSynchronizer synchronizer;

  @Override
  public void onDeletion(GroupUserLinkEvent event) {
    GroupUserLink link = event.getTransition().getBefore();
    Set<String> instancesIds =
        getResourcesManagersInWhichUserIsManagerOnlyByGroup(link.getUserId(), link.getGroupId());
    Set<String> userIdToRemove = Set.of(link.getUserId());
    instancesIds.forEach(id -> synchronizer.synchronize(id, userIdToRemove));
  }

  private Set<String> getResourcesManagersInWhichUserIsManagerOnlyByGroup(String userId,
      String groupId) {
    return resourceService.getResources().stream()
        .filter(r -> r.getManagers().stream()
            .anyMatch(m -> m.getManagerId() == Integer.parseInt(userId)))
        .map(Resource::getInstanceId)
        .distinct()
        .filter(id -> organization.getComponentInst(id).getAllProfilesInst().stream()
            .filter(p -> p.getName().equalsIgnoreCase("responsable"))
            .reduce(this::merge)
            .filter(p -> !p.getAllUsers().contains(userId))
            .filter(p -> isInGroups(groupId, p.getAllGroups()))
            .filter(p -> isNotInOthersGroups(userId, groupId, p.getAllGroups()))
            .isPresent())
        .collect(Collectors.toSet());
  }

  private boolean isInGroups(String groupId, List<String> groups) {
    return groups.contains(groupId)
        || groups.stream()
        .flatMap(g -> Stream.of(organization.getRecursivelyAllSubgroups(g)))
        .map(Group.class::cast)
        .anyMatch(g -> g.getId().equalsIgnoreCase(groupId));
  }

  private boolean isNotInOthersGroups(String userId, String groupId, List<String> groups) {
    return groups.stream()
        .filter(g -> !g.equalsIgnoreCase(groupId))
        .map(g -> organization.getGroup(g))
        .map(Group.class::cast)
        .noneMatch(g -> List.of(g.getUserIds()).contains(userId));
  }

  // merge two similar profiles, one being specific to the Resources Manager app, the other
  // inherited from the workspace
  private ProfileInst merge(ProfileInst profileInst1, ProfileInst profileInst2) {
    ProfileInst profileInst = new ProfileInst();
    profileInst.setGroups(profileInst1.getAllGroups());
    profileInst.addGroups(profileInst2.getAllGroups());
    profileInst.setUsers(profileInst1.getAllUsers());
    profileInst.addUsers(profileInst2.getAllUsers());
    return profileInst;
  }
}