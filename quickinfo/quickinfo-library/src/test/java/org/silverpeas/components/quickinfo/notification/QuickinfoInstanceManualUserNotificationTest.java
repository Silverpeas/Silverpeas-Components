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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.quickinfo.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.components.quickinfo.model.QuickInfoService;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.user.NotificationContext;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the check of a manual notification about a news: only a news of the QuickInfo
 * instance from which the notification is sent can be notified.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class QuickinfoInstanceManualUserNotificationTest {

  private static final String QUICKINFO_ID = "quickinfo1";
  private static final String OTHER_QUICKINFO_ID = "quickinfo2";
  private static final String NEWS_ID = "news_1";

  @TestManagedMock
  private QuickInfoService quickInfoService;
  @TestedBean
  private QuickinfoInstanceManualUserNotification notification;

  private final User sender = mock(User.class);

  @BeforeEach
  void setUp() {
    News news = mock(News.class);
    when(news.canBeAccessedBy(sender)).thenReturn(true);
    when(quickInfoService.getNews(new ResourceReference(NEWS_ID, QUICKINFO_ID))).thenReturn(news);
  }

  @Test
  void aNewsOfTheInstanceCanBeNotified() {
    assertThat(notification.check(aContextOn(NEWS_ID, QUICKINFO_ID)), is(true));
  }

  @Test
  void aNewsOfAnotherInstanceCannotBeNotified() {
    assertThat(notification.check(aContextOn(NEWS_ID, OTHER_QUICKINFO_ID)), is(false));
  }

  private NotificationContext aContextOn(String newsId, String instanceId) {
    NotificationContext context = new NotificationContext(sender);
    context.put(NotificationContext.CONTRIBUTION_ID, newsId);
    context.put(NotificationContext.COMPONENT_ID, instanceId);
    return context;
  }
}
