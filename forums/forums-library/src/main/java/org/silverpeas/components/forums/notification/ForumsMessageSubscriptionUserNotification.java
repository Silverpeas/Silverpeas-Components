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
package org.silverpeas.components.forums.notification;

import org.silverpeas.core.notification.user.UserSubscriptionNotificationBehavior;
import org.silverpeas.core.subscription.constant.SubscriberType;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberMapBySubscriberType;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.components.forums.model.Message;

import java.util.Collection;

/**
 * User: Yohann Chastagnier
 * Date: 10/06/13
 */
public class ForumsMessageSubscriptionUserNotification extends AbstractForumsMessageUserNotification
    implements UserSubscriptionNotificationBehavior {

  private SubscriptionSubscriberMapBySubscriberType subscriberIdsByTypes =
      new SubscriptionSubscriberMapBySubscriberType();

  /**
   * Default constructor.
   * @param resource
   * @param action
   */
  public ForumsMessageSubscriptionUserNotification(final Message resource, final NotifAction action) {
    super(resource, action);
  }

  @Override
  protected void initialize() {
    super.initialize();
    subscriberIdsByTypes.addAll(getForumsService().listAllSubscribers(getResource().getPk()));
  }

  @Override
  protected void perform(final Message resource) {
    super.perform(resource);
    getNotificationMetaData().displayReceiversInFooter();
  }

  @Override
  protected String getBundleSubjectKey() {
    return "GML.subscription";
  }

  @Override
  protected String getTemplateFileName() {
    if (NotifAction.CREATE.equals(getAction())) {
      return "subscriptionNotificationOnSubjectOrMessageCreate";
    }
    return "subscriptionNotificationOnSubjectOrMessageUpdate";
  }

  @Override
  protected String getSender() {
    return getResource().getAuthor();
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
