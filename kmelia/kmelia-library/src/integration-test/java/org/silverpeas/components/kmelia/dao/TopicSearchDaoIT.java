/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

package org.silverpeas.components.kmelia.dao;

import org.silverpeas.components.kmelia.model.MostInterestedQueryVO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.components.kmelia.test.WarBuilder4Kmelia;
import org.silverpeas.core.test.BasicWarBuilder;
import org.silverpeas.core.test.rule.DbUnitLoadingRule;
import org.silverpeas.core.util.ServiceProvider;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author ebonnet
 */
@RunWith(Arquillian.class)
public class TopicSearchDaoIT {

  private TopicSearchDao dao;

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule("create-database.sql", "kmelia-dataset.xml");

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4Kmelia.onWarForTestClass(TopicSearchDaoIT.class)
        .testFocusedOn(warBuilder -> {
          warBuilder.addClasses(MostInterestedQueryVO.class);
          warBuilder.addPackages(true, "org.silverpeas.components.kmelia.dao");
        }).build();
  }

  @Before
  public void generalSetup() {
    dao = ServiceProvider.getService(TopicSearchDao.class);
  }

  public TopicSearchDaoIT() {
  }

  /**
   * Test of getMostInterestedSearch method, of class TopicSearchDao.
   */
  @Test
  public void testGetMostInterestedSearch() {
    String instanceId = "kmelia111";
    List<MostInterestedQueryVO> result = dao.getMostInterestedSearch(instanceId);
    assertEquals(4, result.size());
    assertEquals("ma recherche", result.get(0).getQuery());
    assertEquals(2, result.get(0).getOccurrences().intValue());
  }

}
