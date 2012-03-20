package com.silverpeas.kmelia.stats;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.silverpeas.kmelia.model.StatsFilterVO;
import com.silverpeas.kmelia.model.TopicSearchStatsVO;

/**
 *
 * @author ebonnet
 */
public class StatisticServiceTest {
  
  public StatisticServiceTest() {
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
   * Test of getNbConsultedPublication method, of class StatisticService.
   */
  @Test
  public void testGetNbConsultedPublication() {
    System.out.println("getNbConsultedPublication");
    StatsFilterVO statFilter = null;
    StatisticService instance = new StatisticServiceImpl();
    Integer expResult = null;
    Integer result = instance.getNbConsultedPublication(statFilter);
    assertEquals(expResult, result);
  }

  /**
   * Test of getStatisticActivityByPeriod method, of class StatisticService.
   */
  @Test
  public void testGetStatisticActivityByPeriod() {
    System.out.println("getStatisticActivityByPeriod");
    StatsFilterVO statFilter = null;
    StatisticService instance = new StatisticServiceImpl();
    TopicSearchStatsVO expResult = null;
    Integer result = instance.getStatisticActivityByPeriod(statFilter);
    assertEquals(expResult, result);
  }

  public class StatisticServiceImpl implements StatisticService {

    public Integer getNbConsultedPublication(StatsFilterVO statFilter) {
      return null;
    }

    public Integer getStatisticActivityByPeriod(StatsFilterVO statFilter) {
      return null;
    }
  }
}
