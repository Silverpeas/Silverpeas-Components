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
package org.silverpeas.components.forum.notification;

import com.silverpeas.usernotification.builder.UserSubscriptionNotificationBehavior;
import com.silverpeas.subscribe.constant.SubscriberType;
import com.silverpeas.subscribe.util.SubscriptionSubscriberMapBySubscriberType;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.webactiv.forums.models.ForumDetail;

import java.util.Collection;

/**
 * User: Yohann Chastagnier
 * Date: 10/06/13
 */
public class ForumsForumSubscriptionUserNotification extends AbstractForumsForumUserNotification
    implements UserSubscriptionNotificationBehavior {

  private SubscriptionSubscriberMapBySubscriberType subscriberIdsByTypes =
      new SubscriptionSubscriberMapBySubscriberType();

  /**
   * Default constructor.
   * @param resource
   */
  public ForumsForumSubscriptionUserNotification(final ForumDetail resource) {
    super(resource);
  }

  @Override
  protected void initialize() {
    super.initialize();
    subscriberIdsByTypes.addAll(getForumsService().listAllSubscribers(getResource().getPK()));
  }

  @Override
  protected String getBundleSubjectKey() {
    return "forums.forum.notification.subject";
  }

  @Override
  protected String getFileName() {
    return "forumNotification";
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.CREATE;
  }

  @Override
  protected String getSender() {
    return getResource().getCreatorId();
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
