/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.component.kmelia;

import com.silverpeas.admin.components.ComponentPasteInterface;
import com.silverpeas.admin.components.PasteDetail;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBm;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.node.control.NodeService;
import com.stratelia.webactiv.node.model.NodeDetail;
import com.stratelia.webactiv.node.model.NodePK;
import org.silverpeas.util.ServiceProvider;
import org.silverpeas.util.exception.SilverpeasRuntimeException;
import org.silverpeas.wysiwyg.control.WysiwygController;

import java.rmi.RemoteException;
import java.util.List;

public class KmeliaPaste implements ComponentPasteInterface {

  public KmeliaPaste() {
  }

  @Override
  public void paste(PasteDetail pasteDetail) throws RemoteException {
    SilverTrace.debug("kmelia", "KmeliaPaste.paste()", "root.MSG_GEN_ENTER_METHOD");
    String fromComponentId = pasteDetail.getFromComponentId();
    String toComponentId = pasteDetail.getToComponentId();

    KmeliaCopyDetail copyDetail = new KmeliaCopyDetail(pasteDetail);
    copyDetail.addOption(KmeliaCopyDetail.NODE_RIGHTS, "true");

    NodePK rootPK = new NodePK(NodePK.ROOT_NODE_ID, fromComponentId);
    NodePK targetPK = new NodePK(NodePK.ROOT_NODE_ID, toComponentId);

    if (copyDetail.isPublicationHeaderMustBeCopied()) {
      // copy publications on root
      copyDetail.setFromNodePK(rootPK);
      copyDetail.setToNodePK(targetPK);
      getKmeliaBm().copyPublications(copyDetail);
    }
    
    // copy Wysiwyg of root
    WysiwygController.copy(fromComponentId, "Node_" + NodePK.ROOT_NODE_ID, toComponentId,
        "Node_" + NodePK.ROOT_NODE_ID, pasteDetail.getUserId());

    // copy first level of nodes
    List<NodeDetail> firstLevelNodes = getNodeBm().getHeadersByLevel(rootPK, 2);
    for (NodeDetail nodeToPaste : firstLevelNodes) {
      if (nodeToPaste.getId() > 2) {
        // Don't take unbalanced and basket nodes
        copyDetail.setFromNodePK(nodeToPaste.getNodePK());
        copyDetail.setToNodePK(targetPK);
        getKmeliaBm().copyNode(copyDetail);
      }
    }
    SilverTrace.debug("kmelia", "KmeliaPaste.paste()", "root.MSG_GEN_EXIT_METHOD");
  }

  private NodeService getNodeBm() {
    try {
      return ServiceProvider.getService(NodeService.class);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("PasteDetail.getNodeService()",
          SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_NODEBM_HOME", e);
    }
  }

  private KmeliaBm getKmeliaBm() {
    try {
      return ServiceProvider.getService(KmeliaBm.class);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("SendInKmelia.getKmeliaBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

}
