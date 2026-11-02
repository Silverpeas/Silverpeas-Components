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
import org.silverpeas.core.admin.ProfiledObjectId;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the adding of application managers to the managers of the folders having their
 * own access rights in a Kmelia instance.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class FolderManagersUpdaterTest {

  private static final String INSTANCE_ID = "kmelia12";
  private static final String FOLDER_WITH_MANAGERS = "10";
  private static final String FOLDER_WITHOUT_MANAGERS = "11";
  private static final String FOLDER_WITH_ALL_MANAGERS = "12";
  private static final String FOLDER_WITH_INHERITED_RIGHTS = "13";
  private static final String FOLDER_WITHOUT_RIGHTS = "14";

  private final ProfileInst appManagers = profile(null, List.of("1", "2"), List.of("4"));

  @TestManagedMock
  private Administration administration;
  @TestManagedMock
  private NodeService nodeService;
  @TestedBean
  private FolderManagersUpdater updater;

  @BeforeEach
  void setUpFolders() throws Exception {
    when(nodeService.getAllNodes(new NodePK(NodePK.ROOT_NODE_ID, INSTANCE_ID))).thenReturn(
        List.of(folder(FOLDER_WITH_MANAGERS, FOLDER_WITH_MANAGERS),
            folder(FOLDER_WITHOUT_MANAGERS, FOLDER_WITHOUT_MANAGERS),
            folder(FOLDER_WITH_ALL_MANAGERS, FOLDER_WITH_ALL_MANAGERS),
            folder(FOLDER_WITH_INHERITED_RIGHTS, FOLDER_WITH_MANAGERS),
            folder(FOLDER_WITHOUT_RIGHTS, NodeDetail.NO_RIGHTS_DEPENDENCY)));
    when(administration.getProfilesByObject(ProfiledObjectId.fromNode(FOLDER_WITH_MANAGERS),
        INSTANCE_ID)).thenReturn(List.of(profile("100", List.of("2", "12"), List.of("3"))));
    when(administration.getProfilesByObject(ProfiledObjectId.fromNode(FOLDER_WITHOUT_MANAGERS),
        INSTANCE_ID)).thenReturn(List.of());
    when(administration.getProfilesByObject(ProfiledObjectId.fromNode(FOLDER_WITH_ALL_MANAGERS),
        INSTANCE_ID)).thenReturn(List.of(profile("102", List.of("1", "2"), List.of("4"))));
  }

  @Test
  void theApplicationManagersAreAddedToTheFolderManagers() throws Exception {
    updater.addApplicationManagers(INSTANCE_ID, appManagers);

    ArgumentCaptor<ProfileInst> updated = ArgumentCaptor.forClass(ProfileInst.class);
    verify(administration).updateProfileInst(updated.capture());
    assertThat(updated.getValue().getId(), is("100"));
    assertThat(updated.getValue().getAllUsers(), containsInAnyOrder("1", "2", "12"));
    assertThat(updated.getValue().getAllGroups(), containsInAnyOrder("3", "4"));
  }

  @Test
  void theApplicationManagersAreTheFolderManagersIfNone() throws Exception {
    updater.addApplicationManagers(INSTANCE_ID, appManagers);

    ArgumentCaptor<ProfileInst> added = ArgumentCaptor.forClass(ProfileInst.class);
    verify(administration).addProfileInst(added.capture());
    ProfileInst managers = added.getValue();
    assertThat(managers.getName(), is(SilverpeasRole.ADMIN.getName()));
    assertThat(managers.getObjectId(), is(ProfiledObjectId.fromNode(FOLDER_WITHOUT_MANAGERS)));
    assertThat(managers.getComponentFatherId(), is(12));
    assertThat(managers.getAllUsers(), containsInAnyOrder("1", "2"));
    assertThat(managers.getAllGroups(), containsInAnyOrder("4"));
  }

  @Test
  void onlyTheFoldersWithTheirOwnRightsAreConcerned() throws Exception {
    updater.addApplicationManagers(INSTANCE_ID, appManagers);

    verify(administration, never()).getProfilesByObject(
        eq(ProfiledObjectId.fromNode(FOLDER_WITH_INHERITED_RIGHTS)), any());
    verify(administration, never()).getProfilesByObject(
        eq(ProfiledObjectId.fromNode(FOLDER_WITHOUT_RIGHTS)), any());
  }

  private static NodeDetail folder(String id, String rightsDependsOn) {
    NodeDetail folder = new NodeDetail();
    folder.setNodePK(new NodePK(id, INSTANCE_ID));
    folder.setRightsDependsOn(rightsDependsOn);
    return folder;
  }

  private static ProfileInst profile(String id, List<String> userIds, List<String> groupIds) {
    ProfileInst profile = new ProfileInst();
    profile.setId(id);
    profile.setName(SilverpeasRole.ADMIN.getName());
    profile.setUsers(userIds);
    profile.setGroups(groupIds);
    return profile;
  }
}
