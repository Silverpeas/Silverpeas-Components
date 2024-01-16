/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.kmelia.search;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.components.kmelia.stats.StatisticService;
import org.silverpeas.components.kmelia.test.WarBuilder4Kmelia;

import static org.junit.Assert.assertNotNull;

/**
 * @author ebonnet
 */
@RunWith(Arquillian.class)
public class KmeliaSearchServiceProviderIT {

  public KmeliaSearchServiceProviderIT() {
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4Kmelia.onWarForTestClass(KmeliaSearchServiceProviderIT.class)
        .testFocusedOn(warBuilder -> {
          warBuilder.addPackages(true, "org.silverpeas.components.kmelia");
          warBuilder.addAsResource("org/silverpeas/publication/publicationSettings.properties");
        }).build();
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of getTopicSearchService method, of class KmeliaSearchServiceProvider.
   */
  @Test
  public void testGetTopicSearchService() {
    TopicSearchService result = KmeliaSearchServiceProvider.getTopicSearchService();
    assertNotNull(result);
  }

  /**
   * Test of getStatisticService method, of class KmeliaSearchServiceProvider.
   */
  @Test
  public void testGetStatisticService() {
    StatisticService result = KmeliaSearchServiceProvider.getStatisticService();
    assertNotNull(result);
  }
}
