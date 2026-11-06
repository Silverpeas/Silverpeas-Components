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
package org.silverpeas.components.quickinfo.control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.components.quickinfo.model.QuickInfoService;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.silverstatistics.access.service.StatisticService;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.exception.NotFoundException;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests on the access to the news from the user session of a given QuickInfo instance: the
 * news of another instance must never be served.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class QuickInfoSessionControllerTest {

  private static final String QUICKINFO_ID = "quickinfo1";
  private static final String OTHER_QUICKINFO_ID = "quickinfo2";

  @TestManagedMock
  private QuickInfoService quickInfoService;
  @TestManagedMock
  private StatisticService statisticService;

  private final QuickInfoSessionController controller = mock(QuickInfoSessionController.class);

  @BeforeEach
  void setUp() {
    when(controller.getComponentId()).thenReturn(QUICKINFO_ID);
    when(controller.getUserId()).thenReturn("1");
    when(controller.getHighestSilverpeasUserRole()).thenReturn(SilverpeasRole.USER);
    doCallRealMethod().when(controller).getNews(anyString(), anyBoolean());
    doCallRealMethod().when(controller).getNewsByForeignId(anyString());
  }

  @Test
  void getANewsOfTheInstance() {
    News news = aNews(QUICKINFO_ID);
    when(quickInfoService.getNews(new ResourceReference("news_1", QUICKINFO_ID))).thenReturn(news);

    assertThat(controller.getNews("news_1", true), sameInstance(news));
    verify(statisticService).addStat("1", news);
  }

  @Test
  void getANewsOfAnotherInstanceIsNotFound() {
    assertThrows(NotFoundException.class, () -> controller.getNews("news_2", true));
    verify(quickInfoService).getNews(new ResourceReference("news_2", QUICKINFO_ID));
    verifyNoInteractions(statisticService);
  }

  @Test
  void getANewsOfTheInstanceByItsPublication() {
    News news = aNews(QUICKINFO_ID);
    when(quickInfoService.getNewsByForeignId("128")).thenReturn(news);

    assertThat(controller.getNewsByForeignId("128"), sameInstance(news));
  }

  @Test
  void getANewsOfAnotherInstanceByItsPublicationIsNotFound() {
    News news = aNews(OTHER_QUICKINFO_ID);
    when(quickInfoService.getNewsByForeignId("256")).thenReturn(news);

    assertThrows(NotFoundException.class, () -> controller.getNewsByForeignId("256"));
    verifyNoInteractions(statisticService);
  }

  private static News aNews(String instanceId) {
    News news = mock(News.class);
    when(news.getComponentInstanceId()).thenReturn(instanceId);
    return news;
  }
}
