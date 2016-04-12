/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.components.kmelia;

import org.silverpeas.core.admin.component.ComponentInstancePostConstruction;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;

import javax.inject.Named;
import javax.transaction.Transactional;

/**
 * Once an instance of the Kmelia application is created, creates for it the root folder, the bin
 * and the declassified child folders.
 * @author mmoquillon
 */
@Named
public class KmeliaInstancePostConstruction implements ComponentInstancePostConstruction {

  @Transactional
  @Override
  public void postConstruct(final String componentInstanceId) {
    NodeService nodeService = NodeService.get();

    NodeDetail rootFolder = getRootFolderNodeFor(componentInstanceId);
    NodeDetail bin = getBinNodeFor(componentInstanceId, rootFolder);
    NodeDetail dz = getDzNodeFor(componentInstanceId, rootFolder);

    nodeService.createNode(rootFolder);
    nodeService.createNode(bin);
    nodeService.createNode(dz);
  }

  private NodeDetail getRootFolderNodeFor(String componentInstanceId) {
    NodeDetail root = new NodeDetail();
    root.setNodePK(new NodePK("0", componentInstanceId));
    root.setFatherPK(null);
    root.setUseId(true);
    root.setName("Accueil");
    root.setDescription("La Racine");
    root.setCreatorId(UserDetail.getCurrentRequester().getId());
    root.setLevel(1);
    root.setStatus("Visible");
    return root;
  }

  private NodeDetail getBinNodeFor(String componentInstanceId, NodeDetail root) {
    NodeDetail bin = new NodeDetail();
    bin.setNodePK(new NodePK("1", componentInstanceId));
    bin.setFatherPK(root.getNodePK());
    bin.setUseId(true);
    bin.setName("Corbeille");
    bin.setDescription("Vous trouvez ici les publications que vous avez supprimé");
    bin.setCreatorId(UserDetail.getCurrentRequester().getId());
    bin.setLevel(2);
    bin.setStatus("Invisible");
    return bin;
  }

  private NodeDetail getDzNodeFor(String componentInstanceId, NodeDetail root) {
    NodeDetail dz = new NodeDetail();
    dz.setNodePK(new NodePK("2", componentInstanceId));
    dz.setFatherPK(root.getNodePK());
    dz.setUseId(true);
    dz.setName("Déclassées");
    dz.setDescription("Vos publications inaccessibles se retrouvent ici");
    dz.setCreatorId(UserDetail.getCurrentRequester().getId());
    dz.setLevel(2);
    dz.setStatus("Invisible");
    return dz;
  }
}
