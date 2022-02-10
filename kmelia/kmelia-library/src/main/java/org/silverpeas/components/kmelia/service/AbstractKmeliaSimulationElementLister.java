/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia.service;

import org.silverpeas.core.NotSupportedException;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.process.annotation.SimulationElementLister;

/**
 * User: Yohann Chastagnier
 * Date: 24/10/13
 */
public abstract class AbstractKmeliaSimulationElementLister extends SimulationElementLister {

  public AbstractKmeliaSimulationElementLister() {
    super();
  }

  public AbstractKmeliaSimulationElementLister(final SimulationElementLister parentElementLister) {
    super(parentElementLister);
  }

  private final NodeService nodeService = NodeService.get();
  private PublicationService publicationService;

  public NodeService getNodeService() {
    return nodeService;
  }

  public PublicationService getPublicationService() {
    if (publicationService == null) {
      publicationService = PublicationService.get();
    }
    return publicationService;
  }

  @Override
  public void listElements(final Object source, final String language,
      final ResourceReference targetPK) {
    throw new NotSupportedException("This method isn't supported by this class");
  }
}
