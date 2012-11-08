/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
