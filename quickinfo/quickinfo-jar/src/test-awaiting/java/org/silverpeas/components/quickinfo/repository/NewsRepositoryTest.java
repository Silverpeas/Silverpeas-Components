package org.silverpeas.components.quickinfo.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.Date;
import java.util.List;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.silverpeas.components.quickinfo.mock.OrganisationControllerMockWrapper;
import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.core.admin.OrganizationController;
import org.silverpeas.date.Period;
import org.silverpeas.date.PeriodType;
import org.silverpeas.persistence.Transaction;
import org.silverpeas.persistence.jpa.RepositoryBasedTest;
import org.silverpeas.persistence.repository.OperationContext;

import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.util.DBUtil;


public class NewsRepositoryTest extends RepositoryBasedTest {

  private final static int ROW_COUNT = 3;

  private NewsRepository newsRepository;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    DBUtil.getInstanceForTest(getDataSource().getConnection());
    newsRepository = getApplicationContext().getBean(NewsRepository.class);
  }

  @Override
  public void tearDown() throws Exception {
    try {
      super.tearDown();
    } finally {
      DBUtil.clearTestInstance();
    }
  }

  @Override
  public String[] getApplicationContextPath() {
    return new String[]{"/spring-quickinfo.xml", "/spring-quickinfo-mock.xml", "/spring-quickinfo-embedded-datasource.xml"};
  }

  @Override
  public String getDataSetPath() {
    return "org/silverpeas/components/quickinfo/quickinfo-dataset.xml";
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
    IDataSet actualDataSet = getActualDataSet();
    ITable table = actualDataSet.getTable("sc_quickinfo_news");
    assertThat(table.getRowCount(), is(ROW_COUNT));

    final String userId = "1";
    UserDetail user = new UserDetail();
    user.setId(userId);
    Mockito.when(getOrganisationControllerMock().getUserDetail(userId)).thenReturn(user);

    News savedNews = Transaction.performInOne(new Transaction.Process<News>() {
      @Override
      public News execute() {
        News news =
            new News("test", "test", Period.from(new Date(), PeriodType.month), true, true, false);
        news.setComponentInstanceId("quickinfo1");
        news.setPublicationId("45789");
        assertThat(news.getLastUpdateDate(), Matchers.nullValue());
        return newsRepository.save(OperationContext.fromUser(userId), news);
      }
    });

    assertThat(savedNews.getLastUpdateDate(), Matchers.notNullValue());
    assertThat(savedNews.getLastUpdatedBy(), Matchers.is(userId));
    assertThat(savedNews.isImportant(), Matchers.is(true));

    actualDataSet = getActualDataSet();
    table = actualDataSet.getTable("sc_quickinfo_news");
    assertThat(table.getRowCount(), is(ROW_COUNT+1));
  }

  private OrganizationController getOrganisationControllerMock() {
    return getApplicationContext().getBean(OrganisationControllerMockWrapper.class).getMock();
  }


}
