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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.community.notification;

import org.silverpeas.core.notification.user.UserSubscriptionNotificationBehavior;
import org.silverpeas.core.subscription.constant.SubscriberType;
import org.silverpeas.core.subscription.service.ResourceSubscriptionProvider;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberMapBySubscriberType;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.builder.AbstractContributionTemplateUserNotificationBuilder;

import org.silverpeas.components.community.model.Community;

import java.util.Collection;

/**
 * A notification of users subscribed to the changes in the application. The text of the
 * notification is taken from a template that is built with the data of the Community
 * entity concerned by the change.
 */
public final class CommunitySubscribedUserNotificationBuilder
    extends AbstractContributionTemplateUserNotificationBuilder<Community>
    implements UserSubscriptionNotificationBehavior {

  private final NotifAction action;
  private final SubscriptionSubscriberMapBySubscriberType subscriberIdsByTypes =
      new SubscriptionSubscriberMapBySubscriberType();

  public static CommunitySubscribedUserNotificationBuilder aboutCreationOf(
      final Community contribution) {
    return new CommunitySubscribedUserNotificationBuilder(contribution, NotifAction.CREATE);
  }

  public static CommunitySubscribedUserNotificationBuilder aboutUpdateOf(
      final Community contribution) {
    return new CommunitySubscribedUserNotificationBuilder(contribution, NotifAction.UPDATE);
  }

  private CommunitySubscribedUserNotificationBuilder(final Community contribution,
      final NotifAction action) {
    super(contribution);
    this.action = action;
  }

  @Override
  protected void initialize() {
    super.initialize();
    subscriberIdsByTypes.addAll(ResourceSubscriptionProvider
        .getSubscribersOfComponent(getResource().getComponentInstanceId()));
  }

  @Override
  protected void perform(final Community resource) {
    super.perform(resource);
    getNotificationMetaData().displayReceiversInFooter();
  }

  @Override
  protected boolean stopWhenNoUserToNotify() {
    return (!NotifAction.REPORT.equals(action));
  }

  @Override
  protected String getBundleSubjectKey() {
    return "GML.subscription";
  }

  @Override
  protected String getTemplateFileName() {
    return "communitySubscribedUserNotification";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return subscriberIdsByTypes.get(SubscriberType.USER).getAllIds();
  }

  @Override
  protected Collection<String> getGroupIdsToNotify() {
    return subscriberIdsByTypes.get(SubscriberType.GROUP).getAllIds();
  }

  @Override
  protected NotifAction getAction() {
    return action;
  }

  @Override
  protected final String getSender() {
    return getResource().getLastUpdater().getId();
  }

  @Override
  protected String getLocalizationBundlePath() {
    return "org.silverpeas.components.community.multilang.communityBundle";
  }

  @Override
  protected String getTemplatePath() {
    return "community";
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "community.notifLinkLabel";
  }
}