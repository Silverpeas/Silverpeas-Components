/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.kmelia.notification;

import static com.stratelia.webactiv.util.JNDINames.NODEBM_EJBHOME;
import static com.stratelia.webactiv.util.exception.SilverpeasRuntimeException.ERROR;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.notification.builder.AbstractTemplateUserNotificationBuilder;
import com.silverpeas.subscribe.SubscriptionService;
import com.silverpeas.subscribe.SubscriptionServiceFactory;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.kmelia.model.KmaxRuntimeException;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import org.silverpeas.core.admin.OrganisationController;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractKmeliaUserNotification<T> extends AbstractTemplateUserNotificationBuilder<T> {

  public AbstractKmeliaUserNotification(final T resource) {
    super(resource);
  }

  public AbstractKmeliaUserNotification(final T resource, final String title, final String fileName) {
    super(resource, title, fileName);
  }

  @Override
  protected String getMultilangPropertyFile() {
    return "com.stratelia.webactiv.kmelia.multilang.kmeliaBundle";
  }

  @Override
  protected String getTemplatePath() {
    return "kmelia";
  }

  protected OrganisationController getOrganisationController() {
    // Must return a new instance each time.
    // This is to resolve Serializable problems
    return new OrganizationController();
  }

  protected SubscriptionService getSubscribeBm() {
    return SubscriptionServiceFactory.getFactory().getSubscribeService();
  }

  protected NodeBm getNodeBm() {
    NodeBm nodeBm = null;
    try {
      final NodeBmHome nodeBmHome = EJBUtilitaire.getEJBObjectRef(NODEBM_EJBHOME, NodeBmHome.class);
      nodeBm = nodeBmHome.create();
    } catch (final Exception e) {
      throw new KmeliaRuntimeException("AbstractKmeliaNotificationBuilder.getNodeBm()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_NODEBM_HOME", e);
    }
    return nodeBm;
  }

  protected NodeDetail getNodeHeader(final NodePK pk) {
    NodeDetail nodeDetail = null;
    try {
      nodeDetail = getNodeBm().getHeader(pk);
    } catch (final Exception e) {
      throw new KmaxRuntimeException("AbstractKmeliaNotificationBuilder.getNodeHeader()", ERROR,
          "kmax.EX_IMPOSSIBLE_DOBTENIR_LE_NOEUD", e);
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
      try {
        final List<NodeDetail> path = (List<NodeDetail>) getNodeBm().getPath(nodePK);
        if (path.size() > 0) {
          // remove root topic "Accueil"
          path.remove(path.size() - 1);
        }
        htmlPath = getSpacesPath(nodePK.getInstanceId(), language)
            + getComponentLabel(nodePK.getInstanceId(), language);
        if (!path.isEmpty()) {
          htmlPath += " > " + displayPath(path, 10, language);
        }
      } catch (final RemoteException re) {
        throw new KmeliaRuntimeException("AbstractKmeliaNotificationBuilder.getHTMLNodePath()", ERROR,
            "kmelia.EX_IMPOSSIBLE_DOBTENIR_LES_EMPLACEMENTS_DE_LA_PUBLICATION",
            re);
      }
    }
    return htmlPath;
  }

  private String getSpacesPath(final String componentId, final String language) {
    String spacesPath = "";
    final List<SpaceInst> spaces = getOrganisationController().getSpacePathToComponent(
        componentId);
    final Iterator<SpaceInst> iSpaces = spaces.iterator();
    SpaceInst spaceInst = null;
    while (iSpaces.hasNext()) {
      spaceInst = iSpaces.next();
      spacesPath += spaceInst.getName(language);
      spacesPath += " > ";
    }
    return spacesPath;
  }

  private String getComponentLabel(final String componentId, final String language) {
    final ComponentInstLight component = getOrganisationController().getComponentInstLight(componentId);
    String componentLabel = "";
    if (component != null) {
      componentLabel = component.getLabel(language);
    }
    return componentLabel;
  }

  private String displayPath(final Collection<NodeDetail> path, final int beforeAfter, final String language) {
    final StringBuilder pathString = new StringBuilder();
    boolean first = true;

    final List<NodeDetail> pathAsList = new ArrayList<NodeDetail>(path);
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
