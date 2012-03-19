package com.silverpeas.kmelia.dao;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.silverpeas.kmelia.model.MostInterestedQueryVO;
import com.silverpeas.kmelia.repository.TopicSearchRepositoryTest;

/**
 *
 * @author ebonnet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-kmelia-search.xml", "/spring-kmelia-search-embbed-datasource.xml"})
@TransactionConfiguration(transactionManager = "jpaTransactionManager")
public class TopicSearchDaoTest {
  
  @Autowired
  private TopicSearchDao dao;

  @Inject
  private DataSource dataSource;

  
  public TopicSearchDaoTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }
  
  @Before
  public void generalSetUp() throws Exception {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(
        TopicSearchRepositoryTest.class.getClassLoader().getResourceAsStream(
        "com/silverpeas/kmelia/model/kmelia-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
  }
  
  @Before
  public void setUp() {
  }
  
  @After
  public void tearDown() {
  }

  /**
   * Test of getMostInterestedSearch method, of class TopicSearchDao.
   */
  @Test
  public void testGetMostInterestedSearch() {
    //System.out.println("getMostInterestedSearch");
    String instanceId = "kmelia111";
    List<MostInterestedQueryVO> result = dao.getMostInterestedSearch(instanceId);
    assertEquals(4, result.size());
    assertEquals("ma recherche", result.get(0).getQuery());
    assertEquals(2, result.get(0).getOccurrences().intValue());
  }

}
