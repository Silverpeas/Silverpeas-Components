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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.webpages.notification;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.notification.user.UserSubscriptionNotificationBehavior;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.subscription.constant.SubscriberType;
import org.silverpeas.core.subscription.service.ResourceSubscriptionProvider;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberMapBySubscriberType;
import org.silverpeas.kernel.logging.SilverLogger;

import java.util.Collection;

/**
 * @author Yohann Chastagnier
 */
public class WebPagesUserNotifier extends AbstractWebPagesNotification
    implements UserSubscriptionNotificationBehavior {

  private SubscriptionSubscriberMapBySubscriberType subscriberIdsByTypes;

  /**
   * Builds and sends a webpages notification. A warning message is logged when an exception is
   * catched.
   * @param resource
   * @param userId
   */
  public static void notify(final NodePK resource, final String userId) {
    try {
      UserNotificationHelper.buildAndSend(new WebPagesUserNotifier(resource, userId));
    } catch (final Exception e) {
      SilverLogger.getLogger(WebPagesUserNotifier.class).warn(e);
    }
  }

  /**
   * Default constructor
   * @param resource
   */
  public WebPagesUserNotifier(final NodePK resource, final String userId) {
    super(resource, User.getById(userId));
  }

  @Override
  protected void initialize() {
    super.initialize();

    // Subscribers
    subscriberIdsByTypes =
        ResourceSubscriptionProvider.getSubscribersOfComponent(getResource().getInstanceId())
            .indexBySubscriberType();
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.UPDATE;
  }

  @Override
  protected String getBundleSubjectKey() {
    return "webPages.subscription";
  }

  @Override
  protected String getTemplateFileName() {
    return "notificationUpdateContent";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return subscriberIdsByTypes.get(SubscriberType.USER).getAllIds();
  }

  @Override
  protected Collection<String> getGroupIdsToNotify() {
    return subscriberIdsByTypes.get(SubscriberType.GROUP).getAllIds();
  }

}