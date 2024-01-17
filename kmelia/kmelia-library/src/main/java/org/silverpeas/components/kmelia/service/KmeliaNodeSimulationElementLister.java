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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia.service;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.contribution.attachment.process.AttachmentSimulationElementLister;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.process.annotation.SimulationElementLister;

import java.util.List;

/**
 * User: Yohann Chastagnier
 * Date: 24/10/13
 */
public class KmeliaNodeSimulationElementLister extends AbstractKmeliaSimulationElementLister {

  public KmeliaNodeSimulationElementLister() {
    super();
  }

  public KmeliaNodeSimulationElementLister(final SimulationElementLister parentElementLister) {
    super(parentElementLister);
  }

  @Override
  public void listElements(final ResourceReference sourcePK, final String language) {
    NodePK nodePK = new NodePK(sourcePK.getId(), sourcePK.getInstanceId());

    // Retrieving all nodes to copy or move
    List<NodeDetail> treeToPaste = getNodeService().getSubTree(nodePK);

    for (NodeDetail node : treeToPaste) {

      // Using also for the current node specifics
      for (String prefix : new String[]{"Node_"}) {
        ResourceReference resourceReference =
            new ResourceReference(prefix + node.getNodePK().getId(), node.getNodePK().getInstanceId());
        new AttachmentSimulationElementLister(this).listElements(resourceReference, language);
      }

      // Finally, using here the implementation of publication document lister
      new KmeliaPublicationSimulationElementLister(this).listElements(node.getNodePK(), language);
    }
  }
}
