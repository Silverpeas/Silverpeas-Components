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

package org.silverpeas.components.community.model;

import org.silverpeas.components.community.repository.CommunityOfUsersRepository;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.notification.SpaceProfileInstEvent;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.notification.system.CDIResourceEventListener;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.kernel.annotation.Technical;

import javax.inject.Inject;

/**
 * A listener of events about the removing of a group of users from a space role profile. The goal
 * is to detect any attempts to remove a group of community members from its community in order to
 * restore it automatically. It is a workaround until such removal becomes impossible in Silverpeas
 * Core. The group of members are put by default in the reader role of a community space and hence
 * the check is done for any update of this role for a community. Thus, any other roles aren't
 * considered even if the group of members has been added/removed in those roles.
 *
 * @author mmoquillon
 */
@Technical
@Bean
public class MembersGroupRemovalFromRoleListener extends CDIResourceEventListener<SpaceProfileInstEvent> {

  @Inject
  private Administration admin;

  @Inject
  private CommunityOfUsersRepository repository;

  @Override
  public void onUpdate(SpaceProfileInstEvent event) {
    var spaceProfileBefore = event.getTransition().getBefore();
    // nothing to do if the role concerned by the update isn't the reader one. Indeed, the group
    // of community members is put in this role by rule and it is from this role the group must
    // not to be removed
    if (!spaceProfileBefore.getName().equals(SilverpeasRole.READER.getName())) {
      return;
    }
    var spaceProfileAfter = event.getTransition().getAfter();
    String spaceId = SpaceInst.SPACE_KEY_PREFIX + spaceProfileBefore.getSpaceFatherId();
    var groupsBefore = spaceProfileBefore.getAllGroups();
    var groupsAfter = spaceProfileAfter.getAllGroups();
    repository.getBySpaceId(spaceId)
        .flatMap(communityOfUsers ->
            communityOfUsers.getCommunitySpace().getMembersGroup()
                .flatMap(g -> groupsBefore.stream()
                    .filter(id -> id.equals(g.getId()))
                    .filter(id -> !groupsAfter.contains(id))
                    .findFirst())).ifPresent(id -> execute(() -> {
          spaceProfileAfter.addGroup(id);
          admin.updateSpaceProfileInst(spaceProfileAfter, User.getSystemUser().getId());
        }));
  }

  private void execute(AdminOperation operation) {
    try {
      operation.perform();
    } catch (AdminException e) {
      throw new SilverpeasRuntimeException("Unexpected error: " + e.getMessage());
    }
  }

  @FunctionalInterface
  private interface AdminOperation {

    void perform() throws AdminException;
  }
}
  