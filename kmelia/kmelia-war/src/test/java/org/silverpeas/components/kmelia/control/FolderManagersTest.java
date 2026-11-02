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
import org.silverpeas.components.kmelia.InstanceParameters;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.Parameter;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.security.authorization.ForbiddenRuntimeException;
import org.silverpeas.core.web.selection.Selection;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit tests on the forcing of the managers of a Kmelia instance as managers of the folders
 * having their own access rights.
 * @author mmoquillon
 */
class FolderManagersTest {

  private final ProfileInst appManagers =
      profile(SilverpeasRole.ADMIN, List.of("1", "2"), List.of("4"));

  private FolderManagers forced() {
    return new FolderManagers(true, () -> appManagers);
  }

  private FolderManagers notForced() {
    return new FolderManagers(false, () -> fail("The application managers shouldn't be fetched"));
  }

  @Test
  void removingAnApplicationManagerUserIsForbiddenWhenForced() {
    ProfileInst folderManagers =
        profile(SilverpeasRole.ADMIN, List.of("1", "2", "12"), List.of("4"));
    String[] userIds = {"2", "12"};
    String[] groupIds = {"4"};

    var forcedFolderManagers = forced();
    assertThrows(ForbiddenRuntimeException.class,
        () -> forcedFolderManagers.checkNoApplicationManagerRemoved(folderManagers, userIds, groupIds));
  }

  @Test
  void removingAnApplicationManagerGroupIsForbiddenWhenForced() {
    ProfileInst folderManagers =
        profile(SilverpeasRole.ADMIN, List.of("1", "2"), List.of("3", "4"));
    String[] userIds = {"1", "2"};
    String[] groupIds = {"3"};

    var forcedFolderManagers = forced();
    assertThrows(ForbiddenRuntimeException.class,
        () -> forcedFolderManagers.checkNoApplicationManagerRemoved(folderManagers, userIds, groupIds));
  }

  @Test
  void removingAllTheFolderManagersIsForbiddenWhenForced() {
    ProfileInst folderManagers =
        profile(SilverpeasRole.ADMIN, List.of("1", "2", "12"), List.of("4"));
    String[] userIds = {};
    String[] groupIds = {};

    var forcedFolderManagers = forced();
    assertThrows(ForbiddenRuntimeException.class,
        () -> forcedFolderManagers.checkNoApplicationManagerRemoved(folderManagers, userIds, groupIds));
  }

  @Test
  void removingAnApplicationManagerIsAllowedWhenNotForced() {
    ProfileInst folderManagers =
        profile(SilverpeasRole.ADMIN, List.of("1", "2", "12"), List.of("4"));
    String[] userIds = {"12"};
    String[] groupIds = {};

    assertDoesNotThrow(
        () -> notForced().checkNoApplicationManagerRemoved(folderManagers, userIds, groupIds));
  }

  @Test
  void removingOtherFolderManagersThanTheApplicationOnesIsAllowed() {
    ProfileInst folderManagers =
        profile(SilverpeasRole.ADMIN, List.of("1", "2", "12"), List.of("3", "4"));
    String[] userIds = {"1", "2"};
    String[] groupIds = {"4", "5"};

    assertDoesNotThrow(
        () -> forced().checkNoApplicationManagerRemoved(folderManagers, userIds, groupIds));
  }

  @Test
  void applicationManagersNotYetFolderManagersAreNotRemoved() {
    ProfileInst folderManagers = profile(SilverpeasRole.ADMIN, List.of("12"), List.of("3"));
    String[] userIds = {"13"};
    String[] groupIds = {};

    assertDoesNotThrow(
        () -> forced().checkNoApplicationManagerRemoved(folderManagers, userIds, groupIds));
  }

  @Test
  void settingTheFirstFolderManagersIsAllowed() {
    ProfileInst folderManagers = profile(SilverpeasRole.ADMIN, List.of(), List.of());
    String[] userIds = {"12"};
    String[] groupIds = {"3"};

    assertDoesNotThrow(
        () -> forced().checkNoApplicationManagerRemoved(folderManagers, userIds, groupIds));
  }

  @Test
  void removingApplicationManagersFromAnotherFolderRoleIsAllowed() {
    ProfileInst folderWriters = profile(SilverpeasRole.WRITER, List.of("1", "12"), List.of("4"));
    String[] userIds = {"12"};
    String[] groupIds = {};
    FolderManagers folderManagers =
        new FolderManagers(true, () -> fail("The application managers shouldn't be fetched"));

    assertDoesNotThrow(
        () -> folderManagers.checkNoApplicationManagerRemoved(folderWriters, userIds, groupIds));
  }

  @Test
  void applicationManagersAreLockedInTheSelectionOfTheFolderManagersWhenForced() {
    Selection selection = new Selection();

    forced().lockApplicationManagers(selection, SilverpeasRole.ADMIN.getName());

    assertThat(selection.getLockedUsers(), contains("1", "2"));
    assertThat(selection.getLockedGroups(), contains("4"));
  }

  @Test
  void nothingIsLockedInTheSelectionOfTheFolderManagersWhenNotForced() {
    Selection selection = new Selection();

    notForced().lockApplicationManagers(selection, SilverpeasRole.ADMIN.getName());

    assertThat(selection.getLockedUsers(), empty());
    assertThat(selection.getLockedGroups(), empty());
  }

  @Test
  void nothingIsLockedInTheSelectionOfAnotherFolderRole() {
    Selection selection = new Selection();
    FolderManagers folderManagers =
        new FolderManagers(true, () -> fail("The application managers shouldn't be fetched"));

    folderManagers.lockApplicationManagers(selection, SilverpeasRole.WRITER.getName());

    assertThat(selection.getLockedUsers(), empty());
    assertThat(selection.getLockedGroups(), empty());
  }

  @Test
  void applicationManagersAreAddedToTheFolderManagersWhenForced() {
    ProfileInst folderManagers = profile(SilverpeasRole.ADMIN, List.of("2", "12"), List.of("3"));

    boolean added = forced().addApplicationManagers(folderManagers);

    assertThat(added, is(true));
    assertThat(folderManagers.getAllUsers(), containsInAnyOrder("1", "2", "12"));
    assertThat(folderManagers.getAllGroups(), containsInAnyOrder("3", "4"));
  }

  @Test
  void nothingIsAddedToTheFolderManagersHavingAlreadyTheApplicationManagers() {
    ProfileInst folderManagers =
        profile(SilverpeasRole.ADMIN, List.of("1", "2", "12"), List.of("4"));

    boolean added = forced().addApplicationManagers(folderManagers);

    assertThat(added, is(false));
    assertThat(folderManagers.getAllUsers(), contains("1", "2", "12"));
    assertThat(folderManagers.getAllGroups(), contains("4"));
  }

  @Test
  void nothingIsAddedToTheFolderManagersWhenNotForced() {
    ProfileInst folderManagers = profile(SilverpeasRole.ADMIN, List.of("12"), List.of());

    boolean added = notForced().addApplicationManagers(folderManagers);

    assertThat(added, is(false));
    assertThat(folderManagers.getAllUsers(), contains("12"));
    assertThat(folderManagers.getAllGroups(), empty());
  }

  @Test
  void nothingIsAddedToAnotherFolderRole() {
    ProfileInst folderWriters = profile(SilverpeasRole.WRITER, List.of("12"), List.of());
    FolderManagers folderManagers =
        new FolderManagers(true, () -> fail("The application managers shouldn't be fetched"));

    boolean added = folderManagers.addApplicationManagers(folderWriters);

    assertThat(added, is(false));
    assertThat(folderWriters.getAllUsers(), contains("12"));
  }

  @Test
  void theApplicationManagersAreTheExplicitAndTheInheritedOnes() {
    ComponentInst instance = new ComponentInst();
    instance.addProfileInst(profile(SilverpeasRole.ADMIN, List.of("1"), List.of("4")));
    ProfileInst inheritedManagers = profile(SilverpeasRole.ADMIN, List.of("1", "2"), List.of("5"));
    inheritedManagers.setInherited(true);
    instance.addProfileInst(inheritedManagers);
    instance.addProfileInst(profile(SilverpeasRole.WRITER, List.of("12"), List.of("3")));

    ProfileInst managers = FolderManagers.applicationManagersOf(instance);

    assertThat(managers.getName(), is(SilverpeasRole.ADMIN.getName()));
    assertThat(managers.getAllUsers(), containsInAnyOrder("1", "2"));
    assertThat(managers.getAllGroups(), containsInAnyOrder("4", "5"));
  }

  @Test
  void anApplicationCanHaveNoManagers() {
    ComponentInst instance = new ComponentInst();

    ProfileInst managers = FolderManagers.applicationManagersOf(instance);

    assertThat(managers.getAllUsers(), empty());
    assertThat(managers.getAllGroups(), empty());
  }

  @Test
  void theForcingOfTheApplicationManagersIsReadFromTheInstanceParameters() {
    assertThat(FolderManagers.areAppManagersForcedIn(kmelia("yes")), is(true));
    assertThat(FolderManagers.areAppManagersForcedIn(kmelia("no")), is(false));
    ComponentInst instanceWithoutTheParameter = new ComponentInst();
    instanceWithoutTheParameter.setParameters(List.of());
    assertThat(FolderManagers.areAppManagersForcedIn(instanceWithoutTheParameter), is(false));
  }

  @Test
  void theRightsOnFoldersAreReadFromTheInstanceParameters() {
    assertThat(FolderManagers.areRightsOnFoldersEnabledIn(
        instanceWithAsRightsOnFolders("yes")), is(true));
    assertThat(FolderManagers.areRightsOnFoldersEnabledIn(
        instanceWithAsRightsOnFolders("no")), is(false));
  }

  private static ComponentInst instanceWithAsRightsOnFolders(String parameterValue) {
    ComponentInst instance = new ComponentInst();
    Parameter parameter = new Parameter();
    parameter.setName(InstanceParameters.RIGHTS_ON_TOPICS);
    parameter.setValue(parameterValue);
    instance.setParameters(List.of(parameter));
    return instance;
  }

  private static ComponentInst kmelia(String appManagersForced) {
    ComponentInst instance = new ComponentInst();
    Parameter parameter = new Parameter();
    parameter.setName(InstanceParameters.APP_MANAGERS_FORCED_AS_FOLDER_MANAGERS);
    parameter.setValue(appManagersForced);
    instance.setParameters(List.of(parameter));
    return instance;
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
