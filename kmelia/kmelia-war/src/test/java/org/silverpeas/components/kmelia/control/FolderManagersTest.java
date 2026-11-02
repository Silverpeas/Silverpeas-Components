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

import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.security.authorization.ForbiddenRuntimeException;
import org.silverpeas.core.web.selection.Selection;

import java.util.List;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit tests on the interdiction to remove the managers of a Kmelia instance from the managers
 * of a folder having its own access rights.
 * @author mmoquillon
 */
class FolderManagersTest {

  private final ProfileInst appManagers =
      profile(SilverpeasRole.ADMIN, List.of("1", "2"), List.of("4"));

  @Test
  void removingAnApplicationManagerUserIsForbidden() {
    ProfileInst folderManagers =
        profile(SilverpeasRole.ADMIN, List.of("1", "2", "12"), List.of("4"));
    String[] userIds = {"2", "12"};
    String[] groupIds = {"4"};

    assertThrows(ForbiddenRuntimeException.class,
        () -> FolderManagers.checkNoApplicationManagerRemoved(folderManagers, userIds, groupIds,
            () -> appManagers));
  }

  @Test
  void removingAnApplicationManagerGroupIsForbidden() {
    ProfileInst folderManagers =
        profile(SilverpeasRole.ADMIN, List.of("1", "2"), List.of("3", "4"));
    String[] userIds = {"1", "2"};
    String[] groupIds = {"3"};

    assertThrows(ForbiddenRuntimeException.class,
        () -> FolderManagers.checkNoApplicationManagerRemoved(folderManagers, userIds, groupIds,
            () -> appManagers));
  }

  @Test
  void removingAllTheFolderManagersIsForbidden() {
    ProfileInst folderManagers =
        profile(SilverpeasRole.ADMIN, List.of("1", "2", "12"), List.of("4"));
    String[] userIds = {};
    String[] groupIds = {};

    assertThrows(ForbiddenRuntimeException.class,
        () -> FolderManagers.checkNoApplicationManagerRemoved(folderManagers, userIds, groupIds,
            () -> appManagers));
  }

  @Test
  void removingOtherFolderManagersThanTheApplicationOnesIsAllowed() {
    ProfileInst folderManagers =
        profile(SilverpeasRole.ADMIN, List.of("1", "2", "12"), List.of("3", "4"));
    String[] userIds = {"1", "2"};
    String[] groupIds = {"4", "5"};

    assertDoesNotThrow(
        () -> FolderManagers.checkNoApplicationManagerRemoved(folderManagers, userIds, groupIds,
            () -> appManagers));
  }

  @Test
  void applicationManagersNotYetFolderManagersAreNotRemoved() {
    ProfileInst folderManagers = profile(SilverpeasRole.ADMIN, List.of("12"), List.of("3"));
    String[] userIds = {"13"};
    String[] groupIds = {};

    assertDoesNotThrow(
        () -> FolderManagers.checkNoApplicationManagerRemoved(folderManagers, userIds, groupIds,
            () -> appManagers));
  }

  @Test
  void settingTheFirstFolderManagersIsAllowed() {
    ProfileInst folderManagers = profile(SilverpeasRole.ADMIN, List.of(), List.of());
    String[] userIds = {"12"};
    String[] groupIds = {"3"};

    assertDoesNotThrow(
        () -> FolderManagers.checkNoApplicationManagerRemoved(folderManagers, userIds, groupIds,
            () -> appManagers));
  }

  @Test
  void removingApplicationManagersFromAnotherFolderRoleIsAllowed() {
    ProfileInst folderWriters = profile(SilverpeasRole.WRITER, List.of("1", "12"), List.of("4"));
    String[] userIds = {"12"};
    String[] groupIds = {};
    Supplier<ProfileInst> noAppManagers =
        () -> fail("The application managers shouldn't be fetched");

    assertDoesNotThrow(
        () -> FolderManagers.checkNoApplicationManagerRemoved(folderWriters, userIds, groupIds,
            noAppManagers));
  }

  @Test
  void applicationManagersAreLockedInTheSelectionOfTheFolderManagers() {
    Selection selection = new Selection();

    FolderManagers.lockApplicationManagers(selection, SilverpeasRole.ADMIN.getName(),
        () -> appManagers);

    assertThat(selection.getLockedUsers(), contains("1", "2"));
    assertThat(selection.getLockedGroups(), contains("4"));
  }

  @Test
  void nothingIsLockedInTheSelectionOfAnotherFolderRole() {
    Selection selection = new Selection();
    Supplier<ProfileInst> noAppManagers =
        () -> fail("The application managers shouldn't be fetched");

    FolderManagers.lockApplicationManagers(selection, SilverpeasRole.WRITER.getName(),
        noAppManagers);

    assertThat(selection.getLockedUsers(), empty());
    assertThat(selection.getLockedGroups(), empty());
  }

  private static ProfileInst profile(SilverpeasRole role, List<String> userIds,
      List<String> groupIds) {
    ProfileInst profile = new ProfileInst();
    profile.setName(role.getName());
    profile.setUsers(userIds);
    profile.setGroups(groupIds);
    return profile;
  }
}
