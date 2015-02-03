/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
package org.silverpeas.components.forum.subscription;

import com.silverpeas.subscribe.SubscriptionSubscriber;
import com.silverpeas.subscribe.constant.SubscriptionResourceType;
import com.silverpeas.subscribe.service.AbstractResourceSubscriptionService;
import com.silverpeas.subscribe.util.SubscriptionSubscriberList;
import com.stratelia.webactiv.forums.models.Forum;
import com.stratelia.webactiv.forums.models.ForumPK;
import com.stratelia.webactiv.forums.models.Message;
import com.stratelia.webactiv.forums.models.MessagePK;

import java.util.Collection;
import java.util.HashSet;

import static com.silverpeas.subscribe.SubscriptionServiceProvider.getSubscribeService;
import static com.stratelia.webactiv.forums.forumsManager.ejb.ForumsServiceProvider
    .getForumsService;

/**
 * As the class is implementing {@link org.silverpeas.initialization.Initialization}, no
 * annotation appears in order to be taken into account by CDI.<br/>
 * The service will be taken in charge by initialization treatments.
 * @author Yohann Chastagnier
 */
public class ForumsSubscriptionService extends AbstractResourceSubscriptionService {

  @Override
  protected String getHandledComponentName() {
    return "forums";
  }

  @Override
  public SubscriptionSubscriberList getSubscribersOfComponentAndTypedResource(
      final String componentInstanceId, final SubscriptionResourceType resourceType,
      final String resourceId) {

    Collection<SubscriptionSubscriber> subscribers = new HashSet<>();
    ForumPK forumPK = null;

    switch (resourceType) {

      case FORUM_MESSAGE:
        /**
         * In that case, subscribers of messages, parent messages, of linked forum and their
         * parents and of component must be verified.
         */
        MessagePK messagePK = new MessagePK(componentInstanceId, resourceId);
        subscribers.addAll(
            getSubscribeService().getSubscribers(ForumMessageSubscriptionResource.from(messagePK)));

        // Subscribers of parent forum messages if any
        Message message = getForumsService().getMessage(messagePK);
        Message currentMessage = message;
        while (!currentMessage.isSubject()) {
          currentMessage = getForumsService().getMessage(
              new MessagePK(messagePK.getInstanceId(), currentMessage.getParentIdAsString()));
          subscribers.addAll(getSubscribeService()
              .getSubscribers(ForumMessageSubscriptionResource.from(currentMessage.getPk())));
        }

        // Creating the parent forum identifier
        forumPK = new ForumPK(messagePK.getInstanceId(), message.getForumIdAsString());

      case FORUM:
        /**
         * In that case, subscribers of forum and their parents and of component must be verified.
         */
        if (forumPK == null) {
          forumPK = new ForumPK(componentInstanceId, resourceId);
        }
        subscribers
            .addAll(getSubscribeService().getSubscribers(ForumSubscriptionResource.from(forumPK)));


        // Subscribers of parent forums if any
        Forum currentForum = getForumsService().getForum(forumPK);
        while (!currentForum.isRoot()) {
          currentForum = getForumsService()
              .getForum(new ForumPK(forumPK.getInstanceId(), currentForum.getParentIdAsString()));
          subscribers.addAll(getSubscribeService()
              .getSubscribers(ForumSubscriptionResource.from(currentForum.getPk())));
        }
      case COMPONENT:
        /**
         * In that case, subscribers of component must be verified.
         */
        subscribers.addAll(super.getSubscribersOfComponentAndTypedResource(componentInstanceId,
            SubscriptionResourceType.COMPONENT, resourceId));
    }

    return new SubscriptionSubscriberList(subscribers);
  }
}
