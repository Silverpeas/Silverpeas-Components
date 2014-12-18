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

import com.silverpeas.kmelia.domain.TopicSearch;
import com.silverpeas.kmelia.model.MostInterestedQueryVO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.test.BasicWarBuilder;
import org.silverpeas.test.rule.DbUnitLoadingRule;
import org.silverpeas.util.ServiceProvider;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author ebonnet
 */
@RunWith(Arquillian.class)
public class TopicSearchServiceIntegrationTest {

  public TopicSearchServiceIntegrationTest() {
  }

  private TopicSearchService searchService;

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule("create-database.sql", "kmelia-dataset.xml");

  @Deployment
  public static Archive<?> createTestArchive() {
    return BasicWarBuilder.onWarForTestClass(TopicSearchServiceIntegrationTest.class)
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
          warBuilder.addClasses(MostInterestedQueryVO.class, TopicSearch.class);
          warBuilder.addPackages(true, "com.silverpeas.kmelia.dao");
          warBuilder.addPackages(true, "com.silverpeas.kmelia.repository");
          warBuilder.addPackages(true, "com.silverpeas.kmelia.search");
          warBuilder.addAsResource("org/silverpeas/kmelia/settings/kmeliaSettings.properties");
        }).build();
  }

  @Before
  public void generalSetup() {
    searchService = ServiceProvider.getService(TopicSearchService.class);
  }

  /**
   * Test of getMostInterestedSearch method, of class TopicSearchService.
   */
  @Test
  public void testGetMostInterestedSearch() {
    String instanceId = "kmelia111";
    List<MostInterestedQueryVO> result = searchService.getMostInterestedSearch(instanceId);
    assertEquals(4, result.size());
    assertEquals("ma recherche", result.get(0).getQuery());
    assertEquals(2, result.get(0).getOccurrences().intValue());
  }

  /**
   * Test of getMostInterestedSearch method, of class TopicSearchService.
   */
  @Test
  public void testGetEmptyMostInterestedSearch() {
    String instanceId = "kmelia100";
    List<MostInterestedQueryVO> result = searchService.getMostInterestedSearch(instanceId);
    assertEquals(0, result.size());
  }

  /**
   * Test of getMostInterestedSearch method, of class TopicSearchService.
   */
  @Test
  public void testGetFullMostInterestedSearch() {
    String instanceId = "kmelia112";
    List<MostInterestedQueryVO> result = searchService.getMostInterestedSearch(instanceId);
    assertEquals(10, result.size());
  }

}
