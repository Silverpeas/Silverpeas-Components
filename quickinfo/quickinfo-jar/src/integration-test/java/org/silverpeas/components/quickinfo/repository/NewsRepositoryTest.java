package org.silverpeas.components.quickinfo.repository;

import com.stratelia.webactiv.beans.admin.UserDetail;
import org.hamcrest.Matchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.date.Period;
import org.silverpeas.date.PeriodType;
import org.silverpeas.persistence.Transaction;
import org.silverpeas.persistence.repository.OperationContext;
import org.silverpeas.test.BasicWarBuilder;
import org.silverpeas.test.rule.DbUnitLoadingRule;
import org.silverpeas.util.ServiceProvider;

import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@RunWith(Arquillian.class)
public class NewsRepositoryTest {

  private final static int ROW_COUNT = 3;
  private NewsRepository newsRepository;

  public NewsRepositoryTest() {
  }

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule("create-database.sql", "quickinfo-dataset.xml");

  @Deployment
  public static Archive<?> createTestArchive() {
    return BasicWarBuilder.onWarForTestClass(NewsRepositoryTest.class).testFocusedOn(warBuilder -> {
      warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core:lib-core");
      warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core.ejb-core:node");
      warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core.ejb-core:pdc");
      warBuilder.addMavenDependenciesWithPersistence(
          "org.silverpeas.components.delegatednews:delegatednews-jar");
      warBuilder.addMavenDependencies("org.silverpeas.core.ejb-core:tagcloud");
      warBuilder.addMavenDependencies("org.silverpeas.core.ejb-core:publication");
      warBuilder.addMavenDependencies("org.silverpeas.core.ejb-core:statistic");
      warBuilder.addMavenDependencies("org.silverpeas.core.ejb-core:comment");
      warBuilder.addMavenDependencies("org.apache.tika:tika-core");
      warBuilder.addMavenDependencies("org.apache.tika:tika-parsers");
      warBuilder.addPackages(true, "org.silverpeas.components.quickinfo");
      warBuilder.addPackages(true, "com.stratelia.webactiv.quickinfo");
      warBuilder.addAsResource("META-INF/test-MANIFEST.MF", "META-INF/MANIFEST.MF");
    }).build();
  }

  @Before
  public void setUp() throws Exception {
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
  public void testSaving() throws Exception {
    final String userId = "1";
    UserDetail user = new UserDetail();
    user.setId(userId);
    News savedNews = Transaction.performInOne(() -> {
      News news =
          new News("test", "test", Period.from(new Date(), PeriodType.month), true, true, false);
      news.setComponentInstanceId("quickinfo1");
      news.setPublicationId("45789");
      assertThat(news.getLastUpdateDate(), Matchers.nullValue());
      return newsRepository.save(OperationContext.fromUser(userId), news);
    });
    assertThat(savedNews.getLastUpdateDate(), Matchers.notNullValue());
    assertThat(savedNews.getLastUpdatedBy(), Matchers.is(userId));
    assertThat(savedNews.isImportant(), Matchers.is(true));
  }
}
