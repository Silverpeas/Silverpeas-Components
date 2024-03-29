/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia;

import org.silverpeas.components.kmelia.model.KmeliaRuntimeException;
import org.silverpeas.components.kmelia.service.KmeliaService;
import org.silverpeas.core.admin.component.ApplicationResourcePasting;
import org.silverpeas.core.admin.component.model.PasteDetail;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.UsedAxis;
import org.silverpeas.core.pdc.pdc.service.PdcClassificationService;
import org.silverpeas.core.pdc.pdc.service.PdcManager;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import java.util.List;

@Service
@Named("kmelia" + ApplicationResourcePasting.NAME_SUFFIX)
public class KmeliaResourcePasting implements ApplicationResourcePasting {

  @Inject
  private PdcManager pdcManager;

  @Inject
  private PdcClassificationService classificationService;

  @Inject
  private NodeService nodeService;

  @Inject
  private KmeliaService kmeliaService;

  @Override
  @Transactional
  public void paste(PasteDetail pasteDetail) {
    String fromComponentId = pasteDetail.getFromComponentId();
    String toComponentId = pasteDetail.getToComponentId();

    // copy the utilized part of the PdC if any
    try {
      List<UsedAxis> usedAxis = pdcManager.getUsedAxisByInstanceId(fromComponentId);
      for(UsedAxis axis: usedAxis) {
        UsedAxis copy = axis.copy();
        copy.setInstanceId(toComponentId);
        pdcManager.addUsedAxis(copy);
      }
    } catch (PdcException e) {
      throw new KmeliaRuntimeException(e.getMessage());
    }

    // copy the predefined classification on the PdC if any
    PdcClassification appClassification =
        classificationService.getPreDefinedClassification(fromComponentId);
    if (!appClassification.isEmpty() &&
        appClassification.isPredefinedForTheWholeComponentInstance()) {
      PdcClassification copy = appClassification.copy()
          .inComponentInstance(toComponentId);
      classificationService.savePreDefinedClassification(copy);
    }

    // copy nodes and publications
    KmeliaCopyDetail copyDetail = new KmeliaCopyDetail(pasteDetail);
    copyDetail.addOption(KmeliaCopyDetail.NODE_RIGHTS, "true");
    copyDetail.addOption(KmeliaCopyDetail.ADMINISTRATIVE_OPERATION, Boolean.TRUE.toString());

    NodePK rootPK = new NodePK(NodePK.ROOT_NODE_ID, fromComponentId);
    NodePK targetPK = new NodePK(NodePK.ROOT_NODE_ID, toComponentId);

    if (copyDetail.isPublicationHeaderMustBeCopied()) {
      // copy publications on root
      copyDetail.setFromNodePK(rootPK);
      copyDetail.setToNodePK(targetPK);
      kmeliaService.copyPublications(copyDetail);
    }

    // copy Wysiwyg of root
    WysiwygController.copy(fromComponentId, "Node_" + NodePK.ROOT_NODE_ID, toComponentId,
        "Node_" + NodePK.ROOT_NODE_ID, pasteDetail.getUserId());

    // copy recursively the first-level nodes (direct children of the root node)
    List<NodeDetail> firstLevelNodes = nodeService.getHeadersByLevel(rootPK, 2);
    for (NodeDetail nodeToPaste : firstLevelNodes) {
      if (nodeToPaste.isChild()) {
        // Don't take unclassified, root and basket nodes
        copyDetail.setFromNodePK(nodeToPaste.getNodePK());
        copyDetail.setToNodePK(targetPK);
        kmeliaService.copyNode(copyDetail);
      }
    }
  }

}
