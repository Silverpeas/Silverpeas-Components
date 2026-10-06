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
package org.silverpeas.components.quickinfo.notification;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.components.quickinfo.repository.NewsRepository;
import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.subscription.ContributionSubscribersProvider;
import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Provider of the subscribers concerned by a news when only the identifier of the news is known,
 * as when the user is asked, before he validates the modification of a news, whether the
 * subscribers have to be notified about it. The contribution known of the transverse services,
 * like the classification on the PdC, is the publication behind a news and not the news itself:
 * hence the subscribers concerned by a news are the ones concerned by its publication.
 * @author mmoquillon
 */
@Provider
public class NewsSubscribersProvider implements ContributionSubscribersProvider {

  @Inject
  private NewsRepository newsRepository;
  @Inject
  private Instance<ContributionSubscribersProvider> subscribersProviders;

  @Override
  public SubscriptionSubscriberList getSubscribersOf(final ContributionIdentifier contribution) {
    final Set<SubscriptionSubscriber> subscribers = new HashSet<>();
    if (News.CONTRIBUTION_TYPE.equals(contribution.getType())) {
      Optional.ofNullable(newsRepository.getById(contribution.getLocalId()))
          .map(NewsSubscribersProvider::getPublicationOf)
          .ifPresent(p -> subscribersProviders.forEach(
              provider -> subscribers.addAll(provider.getSubscribersOf(p))));
    }
    return new SubscriptionSubscriberList(subscribers);
  }

  private static ContributionIdentifier getPublicationOf(final News news) {
    return ContributionIdentifier.from(news.getComponentInstanceId(), news.getPublicationId(),
        PublicationDetail.getResourceType());
  }
}
