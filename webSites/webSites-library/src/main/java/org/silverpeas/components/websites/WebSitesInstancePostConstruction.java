/*
 * Copyright (C) 2000 - 2018 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.websites;

import org.silverpeas.core.admin.component.ComponentInstancePostConstruction;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.file.FileFolderManager;

import javax.inject.Named;
import javax.transaction.Transactional;
import java.io.File;

/**
 * Once an instance of the WebSites application is spawned, creates for it an entry with the Node
 * service to refer the web sites and a folder to store attachments and images of the future
 * web sites.
 * @author mmoquillon
 */
@Named
public class WebSitesInstancePostConstruction implements ComponentInstancePostConstruction {

  @Transactional
  @Override
  public void postConstruct(final String componentInstanceId) {
    NodeService nodeService = NodeService.get();
    NodeDetail websites = getWebSitesRootNodeFor(componentInstanceId);
    nodeService.createNode(websites);
    createAttachmentsAndImagesDirectory(componentInstanceId);
  }

  private NodeDetail getWebSitesRootNodeFor(String componentInstanceId) {
    NodeDetail webSites = new NodeDetail();
    webSites.setNodePK(new NodePK("0", componentInstanceId));
    webSites.setFatherPK(null);
    webSites.setUseId(true);
    webSites.setName("Accueil");
    webSites.setDescription("La Racine");
    webSites.setCreatorId(UserDetail.getCurrentRequester().getId());
    webSites.setLevel(1);
    webSites.setStatus("Visible");
    return webSites;
  }

  private void createAttachmentsAndImagesDirectory(String componentId) {
    SettingBundle uploadSettings =
        ResourceLocator.getSettingBundle("org.silverpeas.webSites.settings.webSiteSettings");
    File uploadFolder = new File(uploadSettings.getString("uploadsPath"));
    if (!uploadFolder.exists()) {
      FileFolderManager.createFolder(uploadFolder);
    }
    FileFolderManager.createFolder(new File(uploadFolder, componentId));
  }
}
