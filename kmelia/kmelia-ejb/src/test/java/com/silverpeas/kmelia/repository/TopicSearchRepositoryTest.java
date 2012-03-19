package com.silverpeas.kmelia.repository;

import static org.junit.Assert.assertEquals;

import java.util.Date;
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

import com.silverpeas.kmelia.domain.TopicSearch;
import com.silverpeas.kmelia.repository.TopicSearchRepository;

/**
 *
 * @author ebonnet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-kmelia-search.xml", "/spring-kmelia-search-embbed-datasource.xml"})
@TransactionConfiguration(transactionManager = "jpaTransactionManager")
public class TopicSearchRepositoryTest {
  
  @Autowired
  private TopicSearchRepository repo;

  @Inject
  private DataSource dataSource;

  
  public TopicSearchRepositoryTest() {
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
   * Test put TopicSearch inside repository.
   */
  @Test
  public void testSave() {
    // System.out.println("getMostInterestedSearch");
    String instanceId = "kmelia111";
    TopicSearch entity = new TopicSearch(instanceId, 0, 0, "fr", "ma nouvelle recherche", new Date());
    TopicSearch result = repo.save(entity);
    assertEquals(result, repo.findOne(result.getId()));
  }
  
  /**
   * Test retrieve element from database
   */
  @Test
  public void testFindAll() {
    List<TopicSearch> results = repo.findAll();
    assertEquals(6, results.size());
  }
  
  /**
   * 
   */
  @Test
  public void testFindByInstanceId() {
    List<TopicSearch> results = repo.findByInstanceId("kmelia111");
    assertEquals(5, results.size());
  }
  
}
