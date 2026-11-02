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

import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.security.authorization.ForbiddenRuntimeException;
import org.silverpeas.core.web.selection.Selection;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * The managers of a folder having its own access rights. The managers of the application are
 * set as managers of such a folder so that they keep the hand on it: once they are managers of
 * the folder, they cannot be removed from this role.
 * @author mmoquillon
 */
final class FolderManagers {

  private FolderManagers() {
  }

  /**
   * Checks the update of the specified profile of a folder with the given users and groups
   * doesn't remove any manager of the application from the managers of the folder. Any other
   * profile than the one of the folder managers isn't concerned by this check.
   * @param folderProfile the actual profile of a folder having its own access rights.
   * @param userIds the identifiers of the users to set in the profile.
   * @param groupIds the identifiers of the user groups to set in the profile.
   * @param appManagers a supplier of the profile of the application managers. It is invoked only
   * for the folder managers' profile.
   * @throws ForbiddenRuntimeException if a manager of the application is removed from the
   * managers of the folder.
   */
  static void checkNoApplicationManagerRemoved(ProfileInst folderProfile, String[] userIds,
      String[] groupIds, Supplier<ProfileInst> appManagers) {
    if (SilverpeasRole.ADMIN.getName().equals(folderProfile.getName())) {
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
   * the given role of a folder if this role is the one of the folder managers: they cannot be
   * removed from the selection. Nothing is locked for any other role.
   * @param selection the selection of users and groups to play the role in the folder.
   * @param role the name of the role.
   * @param appManagers a supplier of the profile of the application managers. It is invoked only
   * for the folder managers' role.
   */
  static void lockApplicationManagers(Selection selection, String role,
      Supplier<ProfileInst> appManagers) {
    if (SilverpeasRole.ADMIN.getName().equals(role)) {
      ProfileInst managers = appManagers.get();
      selection.setLockedUsers(managers.getAllUsers());
      selection.setLockedGroups(managers.getAllGroups());
    }
  }

  private static boolean isOneRemoved(List<String> appManagerIds, List<String> actualIds,
      String[] newIds) {
    List<String> keptIds = Arrays.asList(newIds);
    return appManagerIds.stream()
        .anyMatch(id -> actualIds.contains(id) && !keptIds.contains(id));
  }
}
