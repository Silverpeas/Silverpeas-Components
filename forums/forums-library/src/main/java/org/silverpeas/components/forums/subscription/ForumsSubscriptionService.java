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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.forums.subscription;

import org.silverpeas.components.forums.model.Forum;
import org.silverpeas.components.forums.model.ForumPK;
import org.silverpeas.components.forums.model.Message;
import org.silverpeas.components.forums.model.MessagePK;
import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.constant.SubscriptionResourceType;
import org.silverpeas.core.subscription.service.AbstractResourceSubscriptionService;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashSet;

import static org.silverpeas.components.forums.service.ForumsServiceProvider.getForumsService;
import static org.silverpeas.core.subscription.SubscriptionServiceProvider.getSubscribeService;
import static org.silverpeas.core.subscription.constant.SubscriptionResourceType.*;

/**
 * As the class is implementing {@link org.silverpeas.core.initialization.Initialization}, no
 * annotation appears in order to be taken into account by CDI.<br>
 * The service will be taken in charge by initialization treatments.
 * @author Yohann Chastagnier
 */
@Singleton
public class ForumsSubscriptionService extends AbstractResourceSubscriptionService {

  @Override
  protected String getHandledComponentName() {
    return "forums";
  }

  @Override
  public SubscriptionSubscriberList getSubscribersOfComponentAndTypedResource(
      final String componentInstanceId, final SubscriptionResourceType resourceType,
      final String resourceId) {
    SubscriptionResourceType nextTypeToHandle = resourceType;

    Collection<SubscriptionSubscriber> subscribers = new HashSet<>();
    ForumPK forumPK = new ForumPK(componentInstanceId, resourceId);

    if (nextTypeToHandle == FORUM_MESSAGE) {
      // In that case, subscribers of messages, parent messages, of linked forum and their
      // parents and of component must be verified.
      MessagePK messagePK = new MessagePK(componentInstanceId, resourceId);
      Message message = verifyMessage(messagePK, subscribers);
      // Creating the parent forum identifier
      forumPK = new ForumPK(messagePK.getInstanceId(), message.getForumIdAsString());
      nextTypeToHandle = FORUM;
    }

    if (nextTypeToHandle == FORUM) {
      // In that case, subscribers of forum and their parents and of component must be verified.
      verifyForum(forumPK, subscribers);
      nextTypeToHandle = COMPONENT;
    }

    if (nextTypeToHandle == COMPONENT) {
      // In that case, subscribers of component must be verified.
      subscribers.addAll(super.getSubscribersOfComponentAndTypedResource(componentInstanceId,
          SubscriptionResourceType.COMPONENT, resourceId));
    }

    return new SubscriptionSubscriberList(subscribers);
  }

  private void verifyForum(final ForumPK forumPK,
      final Collection<SubscriptionSubscriber> subscribers) {
    subscribers.addAll(
        getSubscribeService().getSubscribers(ForumSubscriptionResource.from(forumPK)));

    // Subscribers of parent forums if any
    Forum currentForum = getForumsService().getForum(forumPK);
    while (!currentForum.isRoot()) {
      currentForum = getForumsService().getForum(
          new ForumPK(forumPK.getInstanceId(), currentForum.getParentIdAsString()));
      subscribers.addAll(getSubscribeService().getSubscribers(
          ForumSubscriptionResource.from(currentForum.getPk())));
    }
  }

  private Message verifyMessage(final MessagePK messagePK,
      final Collection<SubscriptionSubscriber> subscribers) {
    subscribers.addAll(
        getSubscribeService().getSubscribers(ForumMessageSubscriptionResource.from(messagePK)));

    // Subscribers of parent forum messages if any
    Message message = getForumsService().getMessage(messagePK);
    Message currentMessage = message;
    while (!currentMessage.isSubject()) {
      currentMessage = getForumsService().getMessage(
          new MessagePK(messagePK.getInstanceId(), currentMessage.getParentIdAsString()));
      subscribers.addAll(getSubscribeService().getSubscribers(
          ForumMessageSubscriptionResource.from(currentMessage.getPk())));
    }

    return message;
  }
}
