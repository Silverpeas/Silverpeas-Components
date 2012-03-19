package com.silverpeas.kmelia.search;

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.silverpeas.kmelia.stats.StatisticService;

/**
 *
 * @author ebonnet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-kmelia-search.xml", "/spring-kmelia-search-embbed-datasource.xml"})
public class KmeliaSearchServiceFactoryTest {
  
  public KmeliaSearchServiceFactoryTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }
  
  @Before
  public void setUp() {
  }
  
  @After
  public void tearDown() {
  }


  /**
   * Test of getInstance method, of class KmeliaSearchServiceFactory.
   */
  @Test
  public void testGetInstance() {
    //System.out.println("getInstance");
    KmeliaSearchServiceFactory result = KmeliaSearchServiceFactory.getInstance();
    assertNotNull(result);
  }

  /**
   * Test of getTopicSearchService method, of class KmeliaSearchServiceFactory.
   */
  @Test
  public void testGetTopicSearchService() {
    //System.out.println("getTopicSearchService");
    TopicSearchService result = KmeliaSearchServiceFactory.getTopicSearchService();
    assertNotNull(result);
  }

  /**
   * Test of getStatisticService method, of class KmeliaSearchServiceFactory.
   */
  @Test
  public void testGetStatisticService() {
    //System.out.println("getStatisticService");
    StatisticService result = KmeliaSearchServiceFactory.getStatisticService();
    assertNotNull(result);
  }
}
