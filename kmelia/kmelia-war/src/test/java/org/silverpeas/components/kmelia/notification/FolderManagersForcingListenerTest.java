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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.silverpeas.components.kmelia.InstanceParameters;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.Parameter;
import org.silverpeas.core.admin.component.notification.ComponentInstanceEvent;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the setting of the application managers as managers of the folders having their
 * own access rights when the forcing of the application managers as folder managers is enabled in
 * a Kmelia instance.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class FolderManagersForcingListenerTest {

  private static final String INSTANCE_ID = "kmelia12";

  @TestManagedMock
  private Administration administration;
  @TestManagedMock
  private FolderManagersUpdater updater;
  @TestedBean
  private FolderManagersForcingListener listener;

  @BeforeEach
  void setUpInstance() throws Exception {
    ComponentInst instance = kmelia("yes", "yes");
    instance.addProfileInst(managers(List.of("1"), List.of("4")));
    ProfileInst inheritedManagers = managers(List.of("2"), List.of());
    inheritedManagers.setInherited(true);
    instance.addProfileInst(inheritedManagers);
    when(administration.getComponentInst(INSTANCE_ID)).thenReturn(instance);
  }

  @Test
  void enablingTheForcingAddsTheApplicationManagersToTheFolderManagers() throws Exception {
    listener.onUpdate(new ComponentInstanceEvent(ResourceEvent.Type.UPDATE, kmelia("yes", "no"),
        kmelia("yes", "yes")));

    ArgumentCaptor<ProfileInst> appManagers = ArgumentCaptor.forClass(ProfileInst.class);
    verify(updater).addApplicationManagers(eq(INSTANCE_ID), appManagers.capture());
    assertThat(appManagers.getValue().getAllUsers(), containsInAnyOrder("1", "2"));
    assertThat(appManagers.getValue().getAllGroups(), containsInAnyOrder("4"));
  }

  @Test
  void keepingTheForcingEnabledChangesNothing() throws Exception {
    listener.onUpdate(new ComponentInstanceEvent(ResourceEvent.Type.UPDATE, kmelia("yes", "yes"),
        kmelia("yes", "yes")));

    verify(updater, never()).addApplicationManagers(any(), any());
  }

  @Test
  void disablingTheForcingChangesNothing() throws Exception {
    listener.onUpdate(new ComponentInstanceEvent(ResourceEvent.Type.UPDATE, kmelia("yes", "yes"),
        kmelia("yes", "no")));

    verify(updater, never()).addApplicationManagers(any(), any());
  }

  @Test
  void enablingTheForcingWithoutRightsOnFoldersChangesNothing() throws Exception {
    listener.onUpdate(new ComponentInstanceEvent(ResourceEvent.Type.UPDATE, kmelia("no", "no"),
        kmelia("no", "yes")));

    verify(updater, never()).addApplicationManagers(any(), any());
  }

  @Test
  void enablingAgainTheRightsOnFoldersWithTheForcingEnabledChangesNothing() throws Exception {
    listener.onUpdate(new ComponentInstanceEvent(ResourceEvent.Type.UPDATE, kmelia("no", "yes"),
        kmelia("yes", "yes")));

    verify(updater, never()).addApplicationManagers(any(), any());
  }

  @Test
  void updatingAnotherApplicationChangesNothing() throws Exception {
    ComponentInst before = kmelia("yes", "no");
    before.setName("almanach");
    ComponentInst after = kmelia("yes", "yes");
    after.setName("almanach");

    listener.onUpdate(new ComponentInstanceEvent(ResourceEvent.Type.UPDATE, before, after));

    verify(updater, never()).addApplicationManagers(any(), any());
  }

  private static ComponentInst kmelia(String rightsOnFolders, String appManagersForced) {
    ComponentInst instance = new ComponentInst();
    instance.setName("kmelia");
    instance.setLocalId(12);
    instance.setParameters(List.of(
        parameter(InstanceParameters.RIGHTS_ON_TOPICS, rightsOnFolders),
        parameter(InstanceParameters.APP_MANAGERS_FORCED_AS_FOLDER_MANAGERS, appManagersForced)));
    return instance;
  }

  private static Parameter parameter(String name, String value) {
    Parameter parameter = new Parameter();
    parameter.setName(name);
    parameter.setValue(value);
    return parameter;
  }

  private static ProfileInst managers(List<String> userIds, List<String> groupIds) {
    ProfileInst profile = new ProfileInst();
    profile.setName(SilverpeasRole.ADMIN.getName());
    profile.setUsers(userIds);
    profile.setGroups(groupIds);
    return profile;
  }
}
