/*
 * Copyright (C) 2000 - 2013 Silverpeas
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

import com.silverpeas.subscribe.constant.SubscriberType;
import com.silverpeas.subscribe.service.NodeSubscriptionResource;
import com.silverpeas.subscribe.util.SubscriptionUtil;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.webactiv.beans.admin.ObjectType;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.node.model.NodeDetail;
import com.stratelia.webactiv.node.model.NodePK;
import com.stratelia.webactiv.publication.model.PublicationDetail;
import org.silverpeas.core.admin.OrganizationController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * @author Yohann Chastagnier
 */
public class KmeliaSubscriptionPublicationUserNotification
    extends AbstractKmeliaPublicationUserNotification {

  private Map<SubscriberType, Collection<String>> subscriberIdsByTypes =
      SubscriptionUtil.indexSubscriberIdsByType(null);
  private Collection<String> userIdsToExcludeFromNotifying = new HashSet<String>();

  public KmeliaSubscriptionPublicationUserNotification(final NodePK nodePK,
      final PublicationDetail resource, final NotifAction action) {
    super(nodePK, resource, action);
  }

  @Override
  protected void initialize() {
    super.initialize();

    // ###########
    // Subscribers
    // ###########

    // In the report case, users that have to be notified can't be known at this level
    Collection<NodeDetail> path = null;
    if (!"kmax".equals(getResource().getInstanceId())) {
      path = getNodeBm().getPath(getNodePK());
    }
    // build a Collection of nodePK which are the ascendants of fatherPK
    if (path != null) {
      for (final NodeDetail descendant : path) {
        SubscriptionUtil.indexSubscriberIdsByType(subscriberIdsByTypes,
            getSubscribeBm().getSubscribers(NodeSubscriptionResource.from(descendant.getNodePK())));
      }
    }

    // Identifying users to be excluded from notifying
    final OrganizationController orgaController = getOrganisationController();
    final Collection<String> allUserSubscriberIds =
        new ArrayList<String>(subscriberIdsByTypes.get(SubscriberType.USER));
    for (String groupId : subscriberIdsByTypes.get(SubscriberType.GROUP)) {
      for (UserDetail user : orgaController.getAllUsersOfGroup(groupId)) {
        allUserSubscriberIds.add(user.getId());
      }
    }

    if (!allUserSubscriberIds.isEmpty()) {
      // get only subscribers who have sufficient rights to read pubDetail
      final NodeDetail node = getNodeHeader(getNodePK());
      for (final String userId : allUserSubscriberIds) {
        if (!orgaController.isComponentAvailable(getNodePK().getInstanceId(), userId) || (node.
            haveRights() && !orgaController
            .isObjectAvailable(node.getRightsDependsOn(), ObjectType.NODE,
                getNodePK().getInstanceId(), userId))) {
          userIdsToExcludeFromNotifying.add(userId);
        }
      }
    }
  }

  @Override
  protected String getBundleSubjectKey() {
    return "Subscription";
  }

  @Override
  protected String getFileName() {
    if (NotifAction.CREATE.equals(getAction())) {
      return "notificationSubscriptionCreate";
    }
    // Update
    return "notificationSubscriptionUpdate";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return subscriberIdsByTypes.get(SubscriberType.USER);
  }

  @Override
  protected Collection<String> getUserIdsToExcludeFromNotifying() {
    return userIdsToExcludeFromNotifying;
  }

  @Override
  protected Collection<String> getGroupIdsToNotify() {
    return subscriberIdsByTypes.get(SubscriberType.GROUP);
  }
}
