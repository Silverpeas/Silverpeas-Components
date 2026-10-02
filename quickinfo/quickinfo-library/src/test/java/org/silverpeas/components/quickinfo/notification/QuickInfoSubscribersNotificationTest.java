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

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.silverpeas.components.quickinfo.model.DefaultQuickInfoService;
import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.components.quickinfo.repository.NewsRepository;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.notification.user.builder.UserNotificationBuilder;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.reminder.Reminder;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.silverpeas.components.quickinfo.notification.QuickInfoDelayedVisibilityUserNotificationReminder.QUICKINFO_DELAYED_VISIBILITY_USER_NOTIFICATION;

/**
 * Unit tests on the notifications the QuickInfo service asks to send to the subscribers when a
 * news is published: a single notification is sent, whatever the way the users are subscribed (to
 * the application or to a position on the PdC on which the news is classified).
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class QuickInfoSubscribersNotificationTest {

  private static final String COMPONENT_ID = "quickinfo15";
  private static final String NEWS_ID = "news-1";
  private static final String PUBLICATION_ID = "42";
  private static final String PUBLISHER = "1";
  private static final ContributionIdentifier PUBLICATION =
      ContributionIdentifier.from(COMPONENT_ID, PUBLICATION_ID, PublicationDetail.getResourceType());

  @TestManagedMock
  NewsRepository newsRepository;
  @TestManagedMock
  PdcManager pdcManager;
  @TestManagedMock
  QuickInfoDelayedVisibilityUserNotificationReminder reminder;
  @TestedBean
  DefaultQuickInfoService service;

  @Test
  void theSubscribersAreNotifiedOnceAboutThePublishingOfANews() {
    final News news = aNews(Status.PUBLISHED_AND_VISIBLE);

    final List<UserNotificationBuilder> notifications =
        notificationsSentBy(() -> service.publish(NEWS_ID, PUBLISHER));

    assertThat(notifications, hasSize(1));
    assertThat(notifications.get(0), instanceOf(QuickInfoSubscriptionUserNotification.class));
    final QuickInfoSubscriptionUserNotification notification =
        (QuickInfoSubscriptionUserNotification) notifications.get(0);
    assertThat(notification.getAction(), is(NotifAction.CREATE));
    assertThat(notification.getComponentInstanceId(), is(news.getComponentInstanceId()));
  }

  /**
   * The subscribers on the PdC are found from the classification of the publication behind the
   * news.
   */
  @Test
  void theNotificationIsAboutThePublicationBehindTheNews() {
    aNews(Status.PUBLISHED_AND_VISIBLE);

    final List<UserNotificationBuilder> notifications =
        notificationsSentBy(() -> service.publish(NEWS_ID, PUBLISHER));

    final QuickInfoSubscriptionUserNotification notification =
        (QuickInfoSubscriptionUserNotification) notifications.get(0);
    assertThat(notification.getSubscribedContribution(), is(Optional.of(PUBLICATION)));
  }

  /**
   * The subscribers on the PdC are notified with the other subscribers by the notification of the
   * application: the service has no more to ask the PdC for notifying them.
   */
  @Test
  void theServiceDoesNotAskThePdcForNotifyingItsSubscribers() throws Exception {
    aNews(Status.PUBLISHED_AND_VISIBLE);

    notificationsSentBy(() -> service.publish(NEWS_ID, PUBLISHER));

    verify(pdcManager, never()).getPositions(anyInt(), anyString());
  }

  @Test
  void theSubscribersAreNotifiedWhenANewsBecomesVisible() {
    aNews(Status.PUBLISHED_AND_VISIBLE);

    final List<UserNotificationBuilder> notifications =
        notificationsSentBy(() -> service.performReminder(aReminderOfVisibility()));

    assertThat(notifications, hasSize(1));
    assertThat(((QuickInfoSubscriptionUserNotification) notifications.get(0)).getAction(),
        is(NotifAction.CREATE));
  }

  @Test
  void nobodyIsNotifiedAboutADraft() {
    aNews(Status.DRAFT);

    final List<UserNotificationBuilder> notifications =
        notificationsSentBy(() -> service.performReminder(aReminderOfVisibility()));

    assertThat(notifications, is(empty()));
  }

  @Test
  void theNotificationOfANewsNotYetVisibleIsPostponed() {
    final News news = aNews(Status.PUBLISHED_BUT_NOT_YET_VISIBLE);

    final List<UserNotificationBuilder> notifications =
        notificationsSentBy(() -> service.publish(NEWS_ID, PUBLISHER));

    assertThat(notifications, is(empty()));
    verify(reminder).setAbout(news);
  }

  private enum Status {
    DRAFT, PUBLISHED_BUT_NOT_YET_VISIBLE, PUBLISHED_AND_VISIBLE
  }

  private News aNews(final Status status) {
    final PublicationDetail publication = mock(PublicationDetail.class);
    when(publication.getId()).thenReturn(PUBLICATION_ID);
    when(publication.getIdentifier()).thenReturn(PUBLICATION);
    final News news = mock(News.class);
    when(news.getId()).thenReturn(NEWS_ID);
    when(news.getComponentInstanceId()).thenReturn(COMPONENT_ID);
    when(news.getPublicationId()).thenReturn(PUBLICATION_ID);
    when(news.getPublication()).thenReturn(publication);
    when(news.isDraft()).thenReturn(status == Status.DRAFT);
    when(news.isVisible()).thenReturn(status == Status.PUBLISHED_AND_VISIBLE);
    when(newsRepository.getById(NEWS_ID)).thenReturn(news);
    return news;
  }

  private static Reminder aReminderOfVisibility() {
    final Reminder aReminder = mock(Reminder.class);
    when(aReminder.getProcessName()).thenReturn(
        QUICKINFO_DELAYED_VISIBILITY_USER_NOTIFICATION.asString());
    when(aReminder.getContributionId()).thenReturn(
        ContributionIdentifier.from(COMPONENT_ID, NEWS_ID, News.CONTRIBUTION_TYPE));
    return aReminder;
  }

  /**
   * Gets the notifications that were asked to be sent by the specified treatment.
   */
  private static List<UserNotificationBuilder> notificationsSentBy(final Runnable treatment) {
    final ArgumentCaptor<UserNotificationBuilder> builders =
        ArgumentCaptor.forClass(UserNotificationBuilder.class);
    try (MockedStatic<UserNotificationHelper> helper = mockStatic(UserNotificationHelper.class)) {
      treatment.run();
      helper.verify(() -> UserNotificationHelper.buildAndSend(builders.capture()), atLeast(0));
    }
    return builders.getAllValues();
  }
}
