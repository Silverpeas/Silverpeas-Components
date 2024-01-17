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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.forums.subscription.bean;

import org.silverpeas.components.forums.model.ForumPK;
import org.silverpeas.components.forums.model.ForumPath;
import org.silverpeas.components.forums.model.MessagePK;
import org.silverpeas.components.forums.model.MessagePath;
import org.silverpeas.components.forums.service.ForumService;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.subscription.SubscriptionResource;
import org.silverpeas.core.subscription.SubscriptionResourceType;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.subscription.bean.AbstractSubscriptionBean;
import org.silverpeas.core.web.subscription.bean.AbstractSubscriptionBeanService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.silverpeas.components.forums.subscription.ForumSubscriptionConstants.FORUM;
import static org.silverpeas.components.forums.subscription.ForumSubscriptionConstants.FORUM_MESSAGE;

/**
 * @author silveryocha
 */
@Service
public class ForumsSubscriptionBeanService extends AbstractSubscriptionBeanService {

  @Override
  protected List<SubscriptionResourceType> getHandledSubscriptionResourceTypes() {
    return Stream.of(FORUM, FORUM_MESSAGE).collect(Collectors.toList());
  }

  @Override
  public String getSubscriptionTypeListLabel(final SubscriptionResourceType type,
      final String language) {
    final Optional<String> label;
    if (FORUM.equals(type)) {
      label = Optional.of(getBundle(language).getString("subscription.list.forums"));
    } else if (FORUM_MESSAGE.equals(type)) {
      label = Optional.of(getBundle(language).getString("subscription.list.forum_messages"));
    } else {
      // nothing is done here about other types, explicit component implementation MUST exist.
      label = Optional.empty();
    }
    return label.orElse(StringUtil.EMPTY);
  }

  @Override
  public List<AbstractSubscriptionBean> toSubscriptionBean(
      final Collection<Subscription> subscriptions, final String language) {
    final OrganizationController controller = OrganizationController.get();
    final List<AbstractSubscriptionBean> converted = new ArrayList<>();
    for (final Subscription subscription : subscriptions) {
      // Subscriptions managed at this level are only those of node subscription.
      final SubscriptionResource resource = subscription.getResource();
      final SubscriptionResourceType type = resource.getType();
      if (FORUM.equals(type)) {
        controller.getComponentInstance(resource.getInstanceId())
            .ifPresent(i -> {
              final ForumPath path = ForumService.get().getForumPath(toForumPK(resource));
              converted.add(new ForumSubscriptionBean(subscription, path, i, language));
            });
      } else if (FORUM_MESSAGE.equals(type)) {
        controller.getComponentInstance(resource.getInstanceId())
            .ifPresent(i -> {
              final MessagePath path = ForumService.get().getMessagePath(toMessagePK(resource));
              converted.add(new ForumMessageSubscriptionBean(subscription, path, i, language));
            });
      }
    }
    return converted;
  }

  private ForumPK toForumPK(final SubscriptionResource resource) {
    return new ForumPK(resource.getPK().getInstanceId(), resource.getPK().getId());
  }

  private MessagePK toMessagePK(final SubscriptionResource resource) {
    return new MessagePK(resource.getPK().getInstanceId(), resource.getPK().getId());
  }

  @Override
  protected LocalizationBundle getBundle(final String language) {
    return ResourceLocator
        .getLocalizationBundle("org.silverpeas.forums.multilang.forumsBundle", language);
  }
}
