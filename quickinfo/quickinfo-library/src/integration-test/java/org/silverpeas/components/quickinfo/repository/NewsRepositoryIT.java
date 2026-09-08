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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.quickinfo.repository;

import org.hamcrest.Matchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.test.BasicWarBuilder;
import org.silverpeas.core.test.integration.rule.DbSetupRule;
import org.silverpeas.core.util.ServiceProvider;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@RunWith(Arquillian.class)
public class NewsRepositoryIT {

  private static final int ROW_COUNT = 3;
  private NewsRepository newsRepository;

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom("create-database.sql")
      .loadInitialDataSetFrom("quickinfo-dataset.sql");

  @Deployment
  public static WebArchive createTestArchive() {
    return BasicWarBuilder.onWarForTestClass(NewsRepositoryIT.class).testFocusedOn(warBuilder -> {
      warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core");
      warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core.services:silverpeas-core-pdc");
      warBuilder.addMavenDependenciesWithPersistence(
          "org.silverpeas.components.delegatednews:silverpeas-delegatednews");
      warBuilder.addMavenDependencies("org.silverpeas.core.services:silverpeas-core-silverstatistics");
      warBuilder.addMavenDependencies("org.silverpeas.core.services:silverpeas-core-comment");
      warBuilder.addAsResource("org/silverpeas/util/logging/silverpeasLogging.properties");
      warBuilder.addAsResource(
          "org/silverpeas/jobStartPagePeas/settings/jobStartPagePeasSettings.properties");
      warBuilder.addAsResource("org/silverpeas/components/quickinfo/repository");
      warBuilder.addPackages(true, "org.silverpeas.components.quickinfo");
    }).build();
  }

  @Before
  public void setUp() {
    newsRepository = ServiceProvider.getService(NewsRepository.class);
  }

  @Test
  public void testLoading() {
    List<News> allNews = newsRepository.getAll();
    assertThat(allNews, hasSize(ROW_COUNT));
  }

  @Test
  public void testLoadingANews() {
    News news = newsRepository.getById("news_1");
    assertThat(news.isImportant(), Matchers.is(true));
  }

  @Test
  public void testLoadingFromComponentId() {
    List<News> allNews = newsRepository.getByComponentId("quickinfo1");
    assertThat(allNews, hasSize(2));
    assertThat(allNews.get(0).getPublicationId(), Matchers.is("128"));
  }

  @Test
  public void testLoadingFromForeignId() {
    News news = newsRepository.getByForeignId("256");
    assertThat(news, Matchers.notNullValue());
    assertThat(news.getComponentInstanceId(), Matchers.is("quickinfo2"));
  }

  @Test
  public void testSaving() {
    final String userId = "1";
    UserDetail user = new UserDetail();
    user.setId(userId);
    OperationContext.fromUser(user);
    News savedNews = Transaction.performInOne(() -> {
      Period visibility = Period.between(LocalDate.now(), LocalDate.now().plusMonths(1));
      News news = News.builder()
          .setTitleAndDescription("test", "test")
          .setVisibilityPeriod(visibility)
          .setImportant(true)
          .setTicker(true)
          .setMandatory(false)
          .build();
      news.setComponentInstanceId("quickinfo1");
      news.setPublicationId("45789");
      news.createdBy(user);
      return newsRepository.save(news);
    });
    assertThat(savedNews.getLastUpdateDate(), Matchers.notNullValue());
    assertThat(savedNews.getLastUpdaterId(), Matchers.is(userId));
    assertThat(savedNews.isImportant(), Matchers.is(true));
  }
}
