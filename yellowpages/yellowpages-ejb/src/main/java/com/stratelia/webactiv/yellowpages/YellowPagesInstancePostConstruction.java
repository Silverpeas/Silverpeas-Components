/**
 * Copyright (C) 2000 - 2015 Silverpeas
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
package com.stratelia.webactiv.yellowpages;

import com.silverpeas.admin.components.ComponentInstancePostConstruction;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.node.control.NodeService;
import com.stratelia.webactiv.node.model.NodeDetail;
import com.stratelia.webactiv.node.model.NodePK;

import javax.inject.Named;
import javax.transaction.Transactional;

/**
 * Once an instance of the YellowPages application is created, creates for it an entry with the
 * NodeService to gathers the contacts and within it two special areas: the bin and the
 * declassified.
 * @author mmoquillon
 */
@Named
public class YellowPagesInstancePostConstruction implements ComponentInstancePostConstruction {

  @Transactional
  @Override
  public void postConstruct(final String componentInstanceId) {
    NodeService nodeService = NodeService.get();

    NodeDetail contacts = getContactsNodeFor(componentInstanceId);
    NodeDetail bin = getBinNodeFor(componentInstanceId, contacts);
    NodeDetail dz = getDzNodeFor(componentInstanceId, contacts);

    nodeService.createNode(contacts);
    nodeService.createNode(bin);
    nodeService.createNode(dz);
  }

  private NodeDetail getContactsNodeFor(String componentInstanceId) {
    NodeDetail root = new NodeDetail();
    root.setNodePK(new NodePK("0", componentInstanceId));
    root.setUseId(true);
    root.setName("Accueil");
    root.setDescription("");
    root.setCreatorId(UserDetail.getCurrentRequester().getId());
    root.setLevel(1);
    return root;
  }

  private NodeDetail getBinNodeFor(String componentInstanceId, NodeDetail root) {
    NodeDetail bin = new NodeDetail();
    bin.setNodePK(new NodePK("1", componentInstanceId));
    bin.setUseId(true);
    bin.setName("Corbeille");
    bin.setDescription("Vous trouvez ici les contacts que vous avez supprimé");
    bin.setCreatorId(UserDetail.getCurrentRequester().getId());
    bin.setLevel(2);
    bin.setFatherPK(root.getFatherPK());
    return bin;
  }

  private NodeDetail getDzNodeFor(String componentInstanceId, NodeDetail root) {
    NodeDetail dz = new NodeDetail();
    dz.setNodePK(new NodePK("2", componentInstanceId));
    dz.setUseId(true);
    dz.setName("Déclassées");
    dz.setDescription("Vos contacts inaccessibles se retrouvent ici");
    dz.setCreatorId(UserDetail.getCurrentRequester().getId());
    dz.setLevel(2);
    dz.setFatherPK(root.getFatherPK());
    return dz;
  }
}
