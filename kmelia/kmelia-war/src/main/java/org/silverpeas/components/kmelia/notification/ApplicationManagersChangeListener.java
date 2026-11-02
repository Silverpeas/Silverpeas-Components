/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.components.kmelia.notification;

import jakarta.inject.Inject;
import org.silverpeas.components.kmelia.control.FolderManagers;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.notification.ProfileInstEvent;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.notification.system.CDIResourceEventListener;
import org.silverpeas.kernel.SilverpeasRuntimeException;

import java.util.List;

/**
 * Listens for events about the change of the managers of component instances, either explicitly
 * set or inherited from the parent space. When this component instance is a Kmelia one in which
 * the application managers are forced as managers of the folders with their own access rights,
 * then the new managers of the application are added to the managers of all these folders.
 * @author mmoquillon
 */
@Bean
public class ApplicationManagersChangeListener
    extends CDIResourceEventListener<ProfileInstEvent> {

  @Inject
  private Administration administration;
  @Inject
  private FolderManagersUpdater updater;

  @Override
  public void onCreation(final ProfileInstEvent event) {
    ProfileInst managers = event.getTransition().getAfter();
    addNewApplicationManagers(managers, managers.getAllUsers(), managers.getAllGroups());
  }

  @Override
  public void onUpdate(final ProfileInstEvent event) {
    ProfileInst before = event.getTransition().getBefore();
    ProfileInst after = event.getTransition().getAfter();
    addNewApplicationManagers(before, newIn(after.getAllUsers(), before.getAllUsers()),
        newIn(after.getAllGroups(), before.getAllGroups()));
  }

  private void addNewApplicationManagers(ProfileInst role, List<String> newUserIds,
      List<String> newGroupIds) {
    if (isApplicationManagers(role) && (!newUserIds.isEmpty() || !newGroupIds.isEmpty())) {
      ComponentInst instance = getComponentInst(role.getComponentFatherId());
      // the access rights of the folders are kept as they are while the specific rights are
      // disabled
      if (instance != null && "kmelia".equalsIgnoreCase(instance.getName()) &&
          FolderManagers.areRightsOnFoldersEnabledIn(instance) &&
          FolderManagers.areAppManagersForcedIn(instance)) {
        ProfileInst newManagers = new ProfileInst();
        newManagers.setName(SilverpeasRole.ADMIN.getName());
        newManagers.setUsers(newUserIds);
        newManagers.setGroups(newGroupIds);
        updater.addApplicationManagers(instance.getId(), newManagers);
      }
    }
  }

  private ComponentInst getComponentInst(int instanceLocalId) {
    try {
      return administration.getComponentInst(String.valueOf(instanceLocalId));
    } catch (AdminException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  private static boolean isApplicationManagers(ProfileInst role) {
    return role.isOnComponentInstance() && role.getComponentFatherId() > 0 &&
        SilverpeasRole.ADMIN.getName().equals(role.getName());
  }

  private static List<String> newIn(List<String> actualIds, List<String> previousIds) {
    return actualIds.stream()
        .filter(id -> !previousIds.contains(id))
        .toList();
  }

}
