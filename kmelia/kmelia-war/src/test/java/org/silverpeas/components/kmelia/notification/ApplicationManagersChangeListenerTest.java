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

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.silverpeas.components.kmelia.InstanceParameters;
import org.silverpeas.core.admin.ProfiledObjectId;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.Parameter;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.notification.ProfileInstEvent;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the adding of the new application managers to the managers of the folders having
 * their own access rights when the forcing of the application managers as folder managers is
 * enabled in a Kmelia instance.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class ApplicationManagersChangeListenerTest {

  private static final int INSTANCE_LOCAL_ID = 12;
  private static final String INSTANCE_ID = "kmelia12";

  @TestManagedMock
  private Administration administration;
  @TestManagedMock
  private FolderManagersUpdater updater;
  @TestedBean
  private ApplicationManagersChangeListener listener;

  @Test
  void theNewApplicationManagersAreAddedToTheFolderManagers() throws Exception {
    givenAnInstance("kmelia", "yes", "yes");
    ProfileInst before = role(SilverpeasRole.ADMIN, List.of("1", "2"), List.of("4"));
    ProfileInst after = role(SilverpeasRole.ADMIN, List.of("2", "3"), List.of("4", "5"));

    listener.onUpdate(new ProfileInstEvent(ResourceEvent.Type.UPDATE, before, after));

    ArgumentCaptor<ProfileInst> newManagers = ArgumentCaptor.forClass(ProfileInst.class);
    verify(updater).addApplicationManagers(eq(INSTANCE_ID), newManagers.capture());
    assertThat(newManagers.getValue().getAllUsers(), containsInAnyOrder("3"));
    assertThat(newManagers.getValue().getAllGroups(), containsInAnyOrder("5"));
  }

  @Test
  void theNewInheritedApplicationManagersAreAddedToTheFolderManagers() throws Exception {
    givenAnInstance("kmelia", "yes", "yes");
    ProfileInst before = role(SilverpeasRole.ADMIN, List.of("1"), List.of());
    before.setInherited(true);
    ProfileInst after = role(SilverpeasRole.ADMIN, List.of("1", "3"), List.of());
    after.setInherited(true);

    listener.onUpdate(new ProfileInstEvent(ResourceEvent.Type.UPDATE, before, after));

    ArgumentCaptor<ProfileInst> newManagers = ArgumentCaptor.forClass(ProfileInst.class);
    verify(updater).addApplicationManagers(eq(INSTANCE_ID), newManagers.capture());
    assertThat(newManagers.getValue().getAllUsers(), containsInAnyOrder("3"));
    assertThat(newManagers.getValue().getAllGroups(), empty());
  }

  @Test
  void theFirstApplicationManagersAreAddedToTheFolderManagers() throws Exception {
    givenAnInstance("kmelia", "yes", "yes");
    ProfileInst managers = role(SilverpeasRole.ADMIN, List.of("1"), List.of("4"));

    listener.onCreation(new ProfileInstEvent(ResourceEvent.Type.CREATION, managers));

    ArgumentCaptor<ProfileInst> newManagers = ArgumentCaptor.forClass(ProfileInst.class);
    verify(updater).addApplicationManagers(eq(INSTANCE_ID), newManagers.capture());
    assertThat(newManagers.getValue().getAllUsers(), containsInAnyOrder("1"));
    assertThat(newManagers.getValue().getAllGroups(), containsInAnyOrder("4"));
  }

  @Test
  void removingApplicationManagersChangesNothing() throws Exception {
    givenAnInstance("kmelia", "yes", "yes");
    ProfileInst before = role(SilverpeasRole.ADMIN, List.of("1", "2"), List.of("4"));
    ProfileInst after = role(SilverpeasRole.ADMIN, List.of("1"), List.of());

    listener.onUpdate(new ProfileInstEvent(ResourceEvent.Type.UPDATE, before, after));

    verify(updater, never()).addApplicationManagers(any(), any());
  }

  @Test
  void addingApplicationManagersWithoutForcingChangesNothing() throws Exception {
    givenAnInstance("kmelia", "yes", "no");
    ProfileInst before = role(SilverpeasRole.ADMIN, List.of("1"), List.of());
    ProfileInst after = role(SilverpeasRole.ADMIN, List.of("1", "3"), List.of());

    listener.onUpdate(new ProfileInstEvent(ResourceEvent.Type.UPDATE, before, after));

    verify(updater, never()).addApplicationManagers(any(), any());
  }

  @Test
  void addingApplicationManagersWithoutRightsOnFoldersChangesNothing() throws Exception {
    givenAnInstance("kmelia", "no", "yes");
    ProfileInst before = role(SilverpeasRole.ADMIN, List.of("1"), List.of());
    ProfileInst after = role(SilverpeasRole.ADMIN, List.of("1", "3"), List.of());

    listener.onUpdate(new ProfileInstEvent(ResourceEvent.Type.UPDATE, before, after));

    verify(updater, never()).addApplicationManagers(any(), any());
  }

  @Test
  void addingUsersInAnotherApplicationRoleChangesNothing() throws Exception {
    givenAnInstance("kmelia", "yes", "yes");
    ProfileInst before = role(SilverpeasRole.WRITER, List.of("1"), List.of());
    ProfileInst after = role(SilverpeasRole.WRITER, List.of("1", "3"), List.of());

    listener.onUpdate(new ProfileInstEvent(ResourceEvent.Type.UPDATE, before, after));

    verify(updater, never()).addApplicationManagers(any(), any());
  }

  @Test
  void addingFolderManagersChangesNothing() throws Exception {
    givenAnInstance("kmelia", "yes", "yes");
    ProfileInst before = role(SilverpeasRole.ADMIN, List.of("1"), List.of());
    before.setObjectId(ProfiledObjectId.fromNode("10"));
    ProfileInst after = role(SilverpeasRole.ADMIN, List.of("1", "3"), List.of());
    after.setObjectId(ProfiledObjectId.fromNode("10"));

    listener.onUpdate(new ProfileInstEvent(ResourceEvent.Type.UPDATE, before, after));

    verify(updater, never()).addApplicationManagers(any(), any());
  }

  @Test
  void addingManagersInAnotherApplicationChangesNothing() throws Exception {
    givenAnInstance("almanach", "yes", "yes");
    ProfileInst before = role(SilverpeasRole.ADMIN, List.of("1"), List.of());
    ProfileInst after = role(SilverpeasRole.ADMIN, List.of("1", "3"), List.of());

    listener.onUpdate(new ProfileInstEvent(ResourceEvent.Type.UPDATE, before, after));

    verify(updater, never()).addApplicationManagers(any(), any());
  }

  private void givenAnInstance(String application, String rightsOnFolders,
      String appManagersForced) throws Exception {
    ComponentInst instance = new ComponentInst();
    instance.setName(application);
    instance.setLocalId(INSTANCE_LOCAL_ID);
    instance.setParameters(List.of(
        parameter(InstanceParameters.RIGHTS_ON_TOPICS, rightsOnFolders),
        parameter(InstanceParameters.APP_MANAGERS_FORCED_AS_FOLDER_MANAGERS, appManagersForced)));
    when(administration.getComponentInst(String.valueOf(INSTANCE_LOCAL_ID))).thenReturn(instance);
  }

  private static Parameter parameter(String name, String value) {
    Parameter parameter = new Parameter();
    parameter.setName(name);
    parameter.setValue(value);
    return parameter;
  }

  private static ProfileInst role(SilverpeasRole role, List<String> userIds,
      List<String> groupIds) {
    ProfileInst profile = new ProfileInst();
    profile.setId("100");
    profile.setName(role.getName());
    profile.setComponentFatherId(INSTANCE_LOCAL_ID);
    profile.setUsers(userIds);
    profile.setGroups(groupIds);
    return profile;
  }
}
