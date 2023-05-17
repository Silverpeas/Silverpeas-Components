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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.kmelia.stats;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.components.kmelia.model.StatsFilterVO;
import org.silverpeas.core.test.unit.UnitTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author ebonnet
 */
@UnitTest
class StatisticServiceTest {

  public StatisticServiceTest() {
  }

  @BeforeEach
  public void setUp() {
  }

  @AfterEach
  public void tearDown() {
  }

  /**
   * Test of getNbConsultedPublication method, of class StatisticService.
   */
  @Test
  void testGetNbConsultedPublication() {
    //System.out.println("getNbConsultedPublication");
    StatsFilterVO statFilter = null;
    StatisticService instance = new StatisticServiceImpl();
    Integer expResult = -1;
    Integer result = instance.getNbConsultedPublication(statFilter);
    assertEquals(expResult, result);
  }

  /**
   * Test of getStatisticActivityByPeriod method, of class StatisticService.
   */
  @Test
  void testGetStatisticActivityByPeriod() {
    //System.out.println("getStatisticActivityByPeriod");
    StatsFilterVO statFilter = null;
    StatisticService instance = new StatisticServiceImpl();
    Integer expResult = -1;
    Integer result = instance.getNbStatisticActivityByPeriod(statFilter);
    assertEquals(expResult, result);
  }

}
