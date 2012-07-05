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
 * "http://www.silverpeas.org/legal/licensing"
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

import static com.stratelia.webactiv.util.exception.SilverpeasRuntimeException.ERROR;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.webactiv.beans.admin.ObjectType;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

/**
 * @author Yohann Chastagnier
 */
public class KmeliaSubscriptionPublicationUserNotification extends AbstractKmeliaPublicationUserNotification {

  private final NodePK nodePK;
  private final NotifAction action;
  private final String fileName;
  private final String subjectKey;
  private final String senderName;

  public KmeliaSubscriptionPublicationUserNotification(final NodePK nodePK, final PublicationDetail resource,
      final NotifAction action) {
    this(nodePK, resource, action, null);
  }

  public KmeliaSubscriptionPublicationUserNotification(final NodePK nodePK, final PublicationDetail resource,
      final NotifAction action, final String senderName) {
    super(resource, null, null);
    this.nodePK = nodePK;
    this.action = action;
    this.senderName = senderName;
    if (NotifAction.REPORT.equals(action)) {
      fileName = "notification";
      subjectKey = "Alert";
    } else if (NotifAction.CREATE.equals(action)) {
      fileName = "notificationSubscriptionCreate";
      subjectKey = "Subscription";
    } else if (NotifAction.UPDATE.equals(action)) {
      fileName = "notificationSubscriptionUpdate";
      subjectKey = "Subscription";
    } else {
      fileName = null;
      subjectKey = null;
    }
  }

  @Override
  protected String getBundleSubjectKey() {
    return subjectKey;
  }

  @Override
  protected String getFileName() {
    return fileName;
  }

  @Override
  protected boolean stopWhenNoUserToNotify() {
    return (!NotifAction.REPORT.equals(action));
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {

    if (NotifAction.REPORT.equals(action)) {
      // In the report case, users that have to be notified can't be known at this level
      return null;
    }

    final List<String> newSubscribers;

    Collection<NodeDetail> path = null;
    if (!"kmax".equals(getResource().getInstanceId())) {
      try {
        path = getNodeBm().getPath(nodePK);
      } catch (final RemoteException re) {
        throw new KmeliaRuntimeException("KmeliaBmEJB.sendSubscriptionsNotification()", ERROR,
            "kmelia.EX_IMPOSSIBLE_DE_PLACER_LA_PUBLICATION_DANS_LE_THEME", re);
      }
    }

    // build a Collection of nodePK which are the ascendants of fatherPK
    final Set<String> subscriberIds = new HashSet<String>();
    if (path != null) {
      for (final NodeDetail descendant : path) {
        subscriberIds.addAll(getSubscribeBm().getSubscribers(descendant.getNodePK()));
      }
    }
    final OrganizationController orgaController = getOrganizationController();
    if (subscriberIds != null && !subscriberIds.isEmpty()) {
      // get only subscribers who have sufficient rights to read pubDetail
      final NodeDetail node = getNodeHeader(nodePK);
      newSubscribers = new ArrayList<String>(subscriberIds.size());
      for (final String userId : subscriberIds) {
        if (orgaController.isComponentAvailable(nodePK.getInstanceId(), userId)) {
          if (!node.haveRights() || orgaController.isObjectAvailable(node.getRightsDependsOn(),
              ObjectType.NODE, nodePK.getInstanceId(), userId)) {
            newSubscribers.add(userId);
          }
        }
      }
    } else {
      newSubscribers = null;
    }

    return newSubscribers;
  }

  @Override
  protected final String getPath(final String language) {
    if (nodePK == null) {
      return "";
    }
    return getHTMLNodePath(nodePK, language);
  }

  @Override
  protected String getSenderName() {
    return senderName;
  }

  @Override
  protected NotifAction getAction() {
    return action;
  }

  @Override
  protected String getComponentInstanceId() {
    return getResource().getInstanceId();
  }

  @Override
  protected String getSender() {
    if (NotifAction.REPORT.equals(action)) {
      return null;
    } else if (NotifAction.CREATE.equals(action)) {
      return getResource().getCreatorId();
    }
    return getResource().getUpdaterId();
  }
}
