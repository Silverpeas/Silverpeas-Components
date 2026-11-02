/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.components.discussion.notification;

import java.util.Collection;
import java.util.Set;
import org.silverpeas.core.notification.user.UserSubscriptionNotificationBehavior;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.subscription.constant.SubscriberType;
import org.silverpeas.core.subscription.service.ResourceSubscriptionProvider;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberMapBySubscriberType;

/** Uses native component subscriptions, receiver filtering and user delivery channels. */
public class DiscussionSubscriptionNotification extends DiscussionMentionNotification
    implements UserSubscriptionNotificationBehavior {
  private final NotifAction action;
  private final SubscriptionSubscriberMapBySubscriberType subscribers =
      new SubscriptionSubscriberMapBySubscriberType();

  public DiscussionSubscriptionNotification(DiscussionSubscriptionEvent event) {
    super(event.instanceId(), event.message(), event.sender(), Set.of());
    action = event.action();
  }

  @Override
  protected void initialize() {
    super.initialize();
    subscribers.addAll(ResourceSubscriptionProvider.getSubscribersOfComponent(getComponentInstanceId()));
  }

  @Override
  protected NotifAction getAction() { return action; }

  @Override
  protected String getBundleSubjectKey() {
    return action == NotifAction.CREATE ? "discussion.subscriptionCreated" : "discussion.subscriptionUpdated";
  }

  @Override
  protected String getTemplateFileName() {
    return action == NotifAction.CREATE ? "subscriptionCreateNotification" : "subscriptionUpdateNotification";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return subscribers.get(SubscriberType.USER).getAllIds();
  }

  @Override
  protected Collection<String> getGroupIdsToNotify() {
    return subscribers.get(SubscriberType.GROUP).getAllIds();
  }
}
