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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.components.quickinfo.repository.NewsRepository;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.subscription.ContributionSubscribersProvider;
import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.service.GroupSubscriptionSubscriber;
import org.silverpeas.core.subscription.service.UserSubscriptionSubscriber;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the subscribers concerned by a news when only its identifier is known, as when
 * the user is asked, before he validates the modification of a news, whether the subscribers have
 * to be notified about it. The contribution classified on the PdC being the publication behind
 * the news, the subscribers concerned by a news are the ones concerned by its publication.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class NewsSubscribersProviderTest {

  private static final String COMPONENT_ID = "quickinfo15";
  private static final String NEWS_ID = "c1a0e4f2-news";
  private static final String PUBLICATION_ID = "42";
  private static final ContributionIdentifier NEWS =
      ContributionIdentifier.from(COMPONENT_ID, NEWS_ID, News.CONTRIBUTION_TYPE);
  private static final ContributionIdentifier PUBLICATION =
      ContributionIdentifier.from(COMPONENT_ID, PUBLICATION_ID, PublicationDetail.getResourceType());
  private static final SubscriptionSubscriber A_SUBSCRIBER_ON_THE_PDC =
      UserSubscriptionSubscriber.from("21");
  private static final SubscriptionSubscriber A_SUBSCRIBED_GROUP_ON_THE_PDC =
      GroupSubscriptionSubscriber.from("12");

  @TestManagedMock
  NewsRepository newsRepository;
  @TestManagedMock
  ContributionSubscribersProvider pdcSubscribersProvider;
  @TestedBean
  NewsSubscribersProvider provider;

  @BeforeEach
  void theSubscribersOnThePdcAreConcernedByThePublicationOfTheNews() {
    final News news = mock(News.class);
    when(news.getComponentInstanceId()).thenReturn(COMPONENT_ID);
    when(news.getPublicationId()).thenReturn(PUBLICATION_ID);
    when(newsRepository.getById(NEWS_ID)).thenReturn(news);
    when(pdcSubscribersProvider.getSubscribersOf(any())).thenAnswer(
        invocation -> new SubscriptionSubscriberList());
    when(pdcSubscribersProvider.getSubscribersOf(PUBLICATION)).thenAnswer(
        invocation -> new SubscriptionSubscriberList(
            List.of(A_SUBSCRIBER_ON_THE_PDC, A_SUBSCRIBED_GROUP_ON_THE_PDC)));
  }

  @Test
  void theSubscribersConcernedByANewsAreTheOnesConcernedByItsPublication() {
    final SubscriptionSubscriberList subscribers = provider.getSubscribersOf(NEWS);

    assertThat(subscribers,
        containsInAnyOrder(A_SUBSCRIBER_ON_THE_PDC, A_SUBSCRIBED_GROUP_ON_THE_PDC));
  }

  @Test
  void aContributionOtherThanANewsIsNotConcerned() {
    final SubscriptionSubscriberList subscribers = provider.getSubscribersOf(PUBLICATION);

    assertThat(subscribers, is(empty()));
    verify(newsRepository, never()).getById(anyString());
    verify(pdcSubscribersProvider, never()).getSubscribersOf(any());
  }

  @Test
  void anUnknownNewsHasNoSubscribers() {
    when(newsRepository.getById(NEWS_ID)).thenReturn(null);

    final SubscriptionSubscriberList subscribers = provider.getSubscribersOf(NEWS);

    assertThat(subscribers, is(empty()));
    verify(pdcSubscribersProvider, never()).getSubscribersOf(any());
  }
}
