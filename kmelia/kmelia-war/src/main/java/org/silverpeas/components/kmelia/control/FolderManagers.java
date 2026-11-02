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
package org.silverpeas.components.kmelia.control;

import org.silverpeas.components.kmelia.InstanceParameters;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.security.authorization.ForbiddenRuntimeException;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.kernel.util.StringUtil;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * The managers of the folders having their own access rights in a Kmelia instance. When it is
 * enabled in the Kmelia instance, the managers of the application are forced as managers of such
 * folders so that they keep the hand on them: they are set as managers of the folders and, once
 * they are, they cannot be removed from this role.
 * @author mmoquillon
 */
public class FolderManagers {

  private final boolean appManagersForced;
  private final Supplier<ProfileInst> appManagers;

  /**
   * Constructs the managers of the folders of a Kmelia instance.
   * @param appManagersForced are the application managers forced as managers of the folders
   * having their own access rights?
   * @param appManagers a supplier of the profile of the application managers. It is invoked only
   * when the application managers are forced as managers of the folders.
   */
  public FolderManagers(boolean appManagersForced, Supplier<ProfileInst> appManagers) {
    this.appManagersForced = appManagersForced;
    this.appManagers = appManagers;
  }

  /**
   * Are the application managers forced as managers of the folders having their own access rights
   * in the specified Kmelia instance?
   * @param instance a Kmelia instance.
   * @return true if the forcing of the application managers as folder managers is enabled in the
   * instance, false otherwise.
   */
  public static boolean areAppManagersForcedIn(ComponentInst instance) {
    return StringUtil.getBooleanValue(
        instance.getParameterValue(InstanceParameters.APP_MANAGERS_FORCED_AS_FOLDER_MANAGERS));
  }

  /**
   * Are the specific access rights on the folders enabled in the specified Kmelia instance? When
   * disabled, the access rights of the folders are kept as they are to be found again once
   * enabled.
   * @param instance a Kmelia instance.
   * @return true if the folders can have their own access rights in the instance, false otherwise.
   */
  public static boolean areRightsOnFoldersEnabledIn(ComponentInst instance) {
    return StringUtil.getBooleanValue(
        instance.getParameterValue(InstanceParameters.RIGHTS_ON_TOPICS));
  }

  /**
   * Gets the managers of the specified application: those explicitly set and those inherited
   * from the parent space.
   * @param instance a component instance.
   * @return the profile of the managers of the application.
   */
  public static ProfileInst applicationManagersOf(ComponentInst instance) {
    ProfileInst managers = new ProfileInst();
    managers.setName(SilverpeasRole.ADMIN.getName());
    instance.getAllProfilesInst().stream()
        .filter(FolderManagers::isManagers)
        .forEach(p -> {
          managers.addUsers(p.getAllUsers());
          managers.addGroups(p.getAllGroups());
        });
    return managers;
  }

  /**
   * Checks the update of the specified profile of a folder with the given users and groups
   * doesn't remove any manager of the application from the managers of the folder when the
   * application managers are forced as managers of the folders. Any other profile than the one of
   * the folder managers isn't concerned by this check.
   * @param folderProfile the actual profile of a folder having its own access rights.
   * @param userIds the identifiers of the users to set in the profile.
   * @param groupIds the identifiers of the user groups to set in the profile.
   * @throws ForbiddenRuntimeException if a manager of the application is removed from the
   * managers of the folder whereas the application managers are forced as folder managers.
   */
  public void checkNoApplicationManagerRemoved(ProfileInst folderProfile, String[] userIds,
      String[] groupIds) {
    if (appManagersForced && isManagers(folderProfile)) {
      ProfileInst managers = appManagers.get();
      if (isOneRemoved(managers.getAllUsers(), folderProfile.getAllUsers(), userIds) ||
          isOneRemoved(managers.getAllGroups(), folderProfile.getAllGroups(), groupIds)) {
        throw new ForbiddenRuntimeException(
            "The managers of the application cannot be removed from the managers of a folder");
      }
    }
  }

  /**
   * Locks the managers of the application in the specified selection of users and groups for
   * the given role of a folder if this role is the one of the folder managers and if the
   * application managers are forced as managers of the folders: they cannot be removed from the
   * selection. Nothing is locked otherwise.
   * @param selection the selection of users and groups to play the role in the folder.
   * @param role the name of the role.
   */
  public void lockApplicationManagers(Selection selection, String role) {
    if (appManagersForced && SilverpeasRole.ADMIN.getName().equals(role)) {
      ProfileInst managers = appManagers.get();
      selection.setLockedUsers(managers.getAllUsers());
      selection.setLockedGroups(managers.getAllGroups());
    }
  }

  /**
   * Adds the managers of the application not already in the specified profile of a folder if
   * this profile is the one of the folder managers and if the application managers are forced as
   * managers of the folders. Nothing is done otherwise.
   * @param folderProfile a profile of a folder having its own access rights.
   * @return true if some managers of the application have been added, false otherwise.
   */
  public boolean addApplicationManagers(ProfileInst folderProfile) {
    if (appManagersForced && isManagers(folderProfile)) {
      ProfileInst managers = appManagers.get();
      int count = folderProfile.getAllUsers().size() + folderProfile.getAllGroups().size();
      folderProfile.addUsers(managers.getAllUsers());
      folderProfile.addGroups(managers.getAllGroups());
      return folderProfile.getAllUsers().size() + folderProfile.getAllGroups().size() > count;
    }
    return false;
  }

  private static boolean isManagers(ProfileInst profile) {
    return SilverpeasRole.ADMIN.getName().equals(profile.getName());
  }

  private static boolean isOneRemoved(List<String> appManagerIds, List<String> actualIds,
      String[] newIds) {
    List<String> keptIds = Arrays.asList(newIds);
    return appManagerIds.stream()
        .anyMatch(id -> actualIds.contains(id) && !keptIds.contains(id));
  }
}
