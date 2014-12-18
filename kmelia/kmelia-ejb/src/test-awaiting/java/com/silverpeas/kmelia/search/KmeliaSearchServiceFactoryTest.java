/**
 * Copyright (C) 2000 - 2014 Silverpeas
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.kmelia.search;

import com.silverpeas.kmelia.stats.StatisticService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.test.BasicWarBuilder;

import static org.junit.Assert.assertNotNull;

/**
 * @author ebonnet
 */
@RunWith(Arquillian.class)
public class KmeliaSearchServiceFactoryTest {

  public KmeliaSearchServiceFactoryTest() {
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return BasicWarBuilder.onWarForTestClass(TopicSearchServiceTest.class)
        .testFocusedOn(warBuilder -> {
          warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core:lib-core");
          warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core.ejb-core:pdc");
          warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core.ejb-core:node");
          warBuilder.addMavenDependencies("org.silverpeas.core.ejb-core:statistic");
          warBuilder.addMavenDependencies("org.silverpeas.core.ejb-core:tagcloud");
          warBuilder.addMavenDependencies("org.silverpeas.core.ejb-core:publication");
          warBuilder.addMavenDependencies("org.silverpeas.core.ejb-core:calendar");
          warBuilder.addMavenDependencies("org.silverpeas.core.ejb-core:formtemplate");
          warBuilder.addMavenDependencies("org.silverpeas.core.ejb-core:searchengine");
          warBuilder.addMavenDependencies("org.silverpeas.core.ejb-core:comment");
          warBuilder.addMavenDependencies("org.apache.tika:tika-core");
          warBuilder.addMavenDependencies("org.apache.tika:tika-parsers");
          warBuilder.addAsResource("META-INF/test-MANIFEST.MF", "META-INF/MANIFEST.MF");
          warBuilder.addPackages(true, "com.silverpeas.kmelia");
          warBuilder.addAsResource("org/silverpeas/kmelia/settings/kmeliaSettings.properties");
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
    TopicSearchService result = KmeliaSearchServiceFactory.getTopicSearchService();
    assertNotNull(result);
  }

  /**
   * Test of getStatisticService method, of class KmeliaSearchServiceProvider.
   */
  @Test
  public void testGetStatisticService() {
    StatisticService result = KmeliaSearchServiceFactory.getStatisticService();
    assertNotNull(result);
  }
}
