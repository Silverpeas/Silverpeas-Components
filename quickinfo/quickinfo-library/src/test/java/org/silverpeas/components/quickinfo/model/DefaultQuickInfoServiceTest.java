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
package org.silverpeas.components.quickinfo.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.components.delegatednews.service.DelegatedNewsService;
import org.silverpeas.components.quickinfo.repository.NewsRepository;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.exception.NotFoundException;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Unit tests on the access to the news of a given QuickInfo instance: a news is always looked for
 * in the instance it belongs to, so the news of another instance are never served nor removed.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class DefaultQuickInfoServiceTest {

  private static final String QUICKINFO_ID = "quickinfo1";
  private static final String OTHER_QUICKINFO_ID = "quickinfo2";
  private static final String NEWS_ID = "news_1";

  @TestManagedMock
  private NewsRepository newsRepository;
  @TestManagedMock
  private PublicationService publicationService;
  @TestManagedMock
  private DelegatedNewsService delegatedNewsService;
  @TestedBean
  private DefaultQuickInfoService service;

  private News news;

  @BeforeEach
  void setUp() {
    news = News.builder().setTitleAndDescription("title", "description").build();
    news.setComponentInstanceId(QUICKINFO_ID);
    news.setPublicationId("128");
    when(newsRepository.getByIdAndInstanceId(NEWS_ID, QUICKINFO_ID)).thenReturn(news);
  }

  @Test
  void getANewsOfTheInstance() {
    News actual = service.getNews(new ResourceReference(NEWS_ID, QUICKINFO_ID));

    assertThat(actual, sameInstance(news));
  }

  @Test
  void getANewsOfAnotherInstanceFindsNothing() {
    News actual = service.getNews(new ResourceReference(NEWS_ID, OTHER_QUICKINFO_ID));

    assertThat(actual, nullValue());
  }

  @Test
  void getANewsOfAnotherInstanceAsContributionFindsNothing() {
    ContributionIdentifier contributionId =
        ContributionIdentifier.from(OTHER_QUICKINFO_ID, NEWS_ID, News.CONTRIBUTION_TYPE);

    assertThat(service.getContributionById(contributionId).isPresent(), is(false));
  }

  @Test
  void removeANewsOfAnotherInstanceIsNotFound() {
    ResourceReference newsRef = new ResourceReference(NEWS_ID, OTHER_QUICKINFO_ID);

    assertThrows(NotFoundException.class, () -> service.removeNews(newsRef));

    verify(newsRepository).getByIdAndInstanceId(NEWS_ID, OTHER_QUICKINFO_ID);
    verifyNoMoreInteractions(newsRepository);
    verify(publicationService, never()).removePublication(any());
  }

  @Test
  void publishANewsOfAnotherInstanceIsNotFound() {
    ResourceReference newsRef = new ResourceReference(NEWS_ID, OTHER_QUICKINFO_ID);

    assertThrows(NotFoundException.class, () -> service.publish(newsRef, "1"));

    verify(newsRepository).getByIdAndInstanceId(NEWS_ID, OTHER_QUICKINFO_ID);
    verifyNoMoreInteractions(newsRepository);
  }

  @Test
  void submitANewsOfAnotherInstanceOnHomepageIsNotFound() {
    ResourceReference newsRef = new ResourceReference(NEWS_ID, OTHER_QUICKINFO_ID);

    assertThrows(NotFoundException.class, () -> service.submitNewsOnHomepage(newsRef, "1"));

    verify(delegatedNewsService, never()).submitNews(any(), any(), any());
  }
}
