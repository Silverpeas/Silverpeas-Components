/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.components.kmelia.notification;

import org.silverpeas.core.admin.ProfiledObjectId;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.notification.user.UserSubscriptionNotificationBehavior;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.subscription.constant.SubscriberType;
import org.silverpeas.core.subscription.constant.SubscriptionResourceType;
import org.silverpeas.core.subscription.service.ResourceSubscriptionProvider;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberMapBySubscriberType;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author Yohann Chastagnier
 */
public class KmeliaSubscriptionPublicationUserNotification
    extends AbstractKmeliaPublicationUserNotification implements
    UserSubscriptionNotificationBehavior {

  private SubscriptionSubscriberMapBySubscriberType subscriberIdsByTypes =
      new SubscriptionSubscriberMapBySubscriberType();
  private Collection<String> userIdsToExcludeFromNotifying = new HashSet<>();

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

    if (getNodePK().isRoot()) {
      subscriberIdsByTypes.addAll(ResourceSubscriptionProvider
          .getSubscribersOfComponent(getComponentInstanceId()));
    } else {
      subscriberIdsByTypes.addAll(ResourceSubscriptionProvider
          .getSubscribersOfComponentAndTypedResource(getComponentInstanceId(), SubscriptionResourceType.NODE, getNodePK().getId()));
    }

    Collection<String> allUserSubscriberIds = subscriberIdsByTypes.getAllUserIds();
    if (!allUserSubscriberIds.isEmpty()) {
      // Identifying users to be excluded from notifying
      final OrganizationController orgaController = getOrganisationController();
      // Get only subscribers who have sufficient rights to read pubDetail
      final NodeDetail node = getNodeHeader(getNodePK());
      for (final String userId : allUserSubscriberIds) {
        if (!orgaController.isComponentAvailable(getNodePK().getInstanceId(), userId) || (node.
            haveRights() && !orgaController.isObjectAvailable(
            ProfiledObjectId.fromNode(node.getRightsDependsOn()), getNodePK().getInstanceId(),
            userId))) {
          userIdsToExcludeFromNotifying.add(userId);
        }
      }
    }
  }

  @Override
  protected void perform(final PublicationDetail resource) {
    super.perform(resource);
    getNotificationMetaData().displayReceiversInFooter();
  }

  @Override
  protected String getBundleSubjectKey() {
    return "Subscription";
  }

  @Override
  protected String getTemplateFileName() {
    if (NotifAction.CREATE.equals(getAction())) {
      return "notificationSubscriptionCreate";
    } else if(NotifAction.UPDATE.equals(getAction())) {
      return "notificationSubscriptionUpdate";
    } else {
      // Draft out or Validate or ALIAS
      return "notificationSubscriptionOtherAction";
    }
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return subscriberIdsByTypes.get(SubscriberType.USER).getAllIds();
  }

  @Override
  protected Collection<String> getUserIdsToExcludeFromNotifying() {
    return userIdsToExcludeFromNotifying;
  }

  @Override
  protected Collection<String> getGroupIdsToNotify() {
    return subscriberIdsByTypes.get(SubscriberType.GROUP).getAllIds();
  }
}
