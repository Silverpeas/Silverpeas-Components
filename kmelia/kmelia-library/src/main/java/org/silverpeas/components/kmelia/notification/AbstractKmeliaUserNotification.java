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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia.notification;

import org.silverpeas.components.kmelia.model.KmaxRuntimeException;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractKmeliaUserNotification<T> extends AbstractTemplateUserNotificationBuilder<T> {

  public AbstractKmeliaUserNotification(final T resource) {
    super(resource);
  }

  @Override
  protected String getLocalizationBundlePath() {
    return "org.silverpeas.kmelia.multilang.kmeliaBundle";
  }

  @Override
  protected String getTemplatePath() {
    return "kmelia";
  }

  protected OrganizationController getOrganisationController() {
    return OrganizationControllerProvider.getOrganisationController();
  }

  protected NodeService getNodeService() {
    return NodeService.get();
  }

  protected NodeDetail getNodeHeader(final NodePK pk) {
    NodeDetail nodeDetail;
    try {
      nodeDetail = getNodeService().getHeader(pk);
    } catch (final Exception e) {
      throw new KmaxRuntimeException(e);
    }
    return nodeDetail;
  }

  /**
   * @param nodePK
   * @return a String like Space1 > SubSpace > Component2 > Topic1 > Topic2
   */
  protected String getHTMLNodePath(final NodePK nodePK, final String language) {
    // get the path of the topic where the publication is classified
    String htmlPath = "";
    if (nodePK != null) {
      htmlPath = getSpacesPath(nodePK.getInstanceId(), language)
          + getComponentLabel(nodePK.getInstanceId(), language);
      if (!nodePK.isRoot() && !nodePK.getId().equals("-1")) {
        final List<NodeDetail> path = getNodeService().getPath(nodePK);
        if (!path.isEmpty()) {
          // remove root topic "Accueil"
          path.remove(path.size() - 1);
        }
        if (!path.isEmpty()) {
          htmlPath += " > " + displayPath(path, language);
        }
      }
    }
    return htmlPath;
  }

  private String getSpacesPath(final String componentId, final String language) {
    StringBuilder spacesPath = new StringBuilder();
    final List<SpaceInstLight> spaces = getOrganisationController().getPathToComponent(componentId);
    final Iterator<SpaceInstLight> iSpaces = spaces.iterator();
    SpaceInstLight spaceInst;
    while (iSpaces.hasNext()) {
      spaceInst = iSpaces.next();
      spacesPath.append(spaceInst.getName(language));
      spacesPath.append(" > ");
    }
    return spacesPath.toString();
  }

  private String getComponentLabel(final String componentId, final String language) {
    final ComponentInstLight component = getOrganisationController().getComponentInstLight(
        componentId);
    String componentLabel = "";
    if (component != null) {
      componentLabel = component.getLabel(language);
    }
    return componentLabel;
  }

  private String displayPath(final Collection<NodeDetail> path, final String language) {
    final StringBuilder pathString = new StringBuilder();
    boolean first = true;

    final List<NodeDetail> pathAsList = new ArrayList<>(path);
    Collections.reverse(pathAsList); // reverse path from root to node
    for (final NodeDetail nodeInPath : pathAsList) {
      if (!first) {
        pathString.append(" > ");
      }
      first = false;
      pathString.append(nodeInPath.getName(language));
    }
    return pathString.toString();
  }
}
