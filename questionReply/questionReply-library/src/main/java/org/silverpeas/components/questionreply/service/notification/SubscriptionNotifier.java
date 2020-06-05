/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.components.questionreply.service.notification;

import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.components.questionreply.model.Reply;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.user.UserSubscriptionNotificationBehavior;
import org.silverpeas.core.subscription.constant.SubscriberType;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberMapBySubscriberType;

import java.util.Collection;
import java.util.MissingResourceException;

import static org.silverpeas.core.subscription.service.ResourceSubscriptionProvider.getSubscribersOfComponent;

/**
 * @author ehugonnet
 */
public class SubscriptionNotifier extends AbstractReplyNotifier
    implements UserSubscriptionNotificationBehavior {

  private final SubscriptionSubscriberMapBySubscriberType subscriberIdsByTypes;

  public SubscriptionNotifier(User sender, Question question, Reply reply) {
    super(question, reply, sender);
    this.subscriberIdsByTypes = getSubscribersOfComponent(question.getInstanceId()).indexBySubscriberType();
  }

  @Override
  protected String getTemplateFileName() {
    return "reply_subscription";
  }

  @Override
  protected String getBundleSubjectKey() {
    return "questionReply.subscription.title";
  }

  @Override
  protected String getTitle(final String language) {
    String translation;
    try {
      translation = getBundle(language).getString(getBundleSubjectKey());
    } catch (MissingResourceException ex) {
      translation = "Answer to %1$s";
    }
    return String.format(translation, getResource().getTitle());
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
