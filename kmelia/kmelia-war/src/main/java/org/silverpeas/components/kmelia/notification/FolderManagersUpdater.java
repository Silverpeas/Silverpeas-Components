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

import org.silverpeas.components.kmelia.control.FolderManagers;
import org.silverpeas.core.admin.ProfiledObjectId;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.kernel.util.StringUtil;

import javax.inject.Inject;

/**
 * Updates the managers of the folders having their own access rights in a Kmelia instance with
 * some managers of the application, as required by the forcing of the application managers as
 * folder managers.
 * @author mmoquillon
 */
@Bean
public class FolderManagersUpdater {

  @Inject
  private Administration administration;
  @Inject
  private NodeService nodeService;

  /**
   * Adds the specified application managers to the managers of all the folders having their own
   * access rights in the given Kmelia instance, if they aren't already.
   * @param instanceId the unique identifier of a Kmelia instance.
   * @param appManagers the profile with the application managers to add.
   * @throws AdminException if the managers of a folder cannot be updated.
   */
  public void addApplicationManagers(String instanceId, ProfileInst appManagers)
      throws AdminException {
    FolderManagers folderManagers = new FolderManagers(true, () -> appManagers);
    for (NodeDetail folder : nodeService.getAllNodes(
        new NodePK(NodePK.ROOT_NODE_ID, instanceId))) {
      if (folder.haveLocalRights()) {
        addApplicationManagers(folder, instanceId, folderManagers);
      }
    }
  }

  private void addApplicationManagers(NodeDetail folder, String instanceId,
      FolderManagers folderManagers) throws AdminException {
    ProfiledObjectId folderId = ProfiledObjectId.fromNode(folder.getId());
    ProfileInst managers = administration.getProfilesByObject(folderId, instanceId).stream()
        .filter(p -> SilverpeasRole.ADMIN.getName().equals(p.getName()))
        .findFirst()
        .orElseGet(() -> {
          ProfileInst profile = new ProfileInst();
          profile.setName(SilverpeasRole.ADMIN.getName());
          profile.setObjectId(folderId);
          profile.setComponentFatherId(ComponentInst.getComponentLocalId(instanceId));
          return profile;
        });
    if (folderManagers.addApplicationManagers(managers)) {
      if (StringUtil.isDefined(managers.getId())) {
        administration.updateProfileInst(managers);
      } else {
        administration.addProfileInst(managers);
      }
    }
  }
}
