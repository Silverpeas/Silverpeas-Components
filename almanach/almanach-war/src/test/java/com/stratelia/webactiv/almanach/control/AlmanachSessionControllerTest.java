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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.stratelia.webactiv.almanach.control;

import java.util.Date;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import java.util.Calendar;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

/**
 *
 * @author ehugonnet
 */
public class AlmanachSessionControllerTest {
  
  private MainSessionController mainController;
  private ComponentContext context;

  public AlmanachSessionControllerTest() {
  }
  
  @Before
  public void prepareMocksToUseInTests() {
    mainController = mock(MainSessionController.class);
    when(mainController.getComponentParameterValue(anyString(), anyString())).thenReturn("");
    context = mock(ComponentContext.class);
    when(context.getCurrentComponentId()).thenReturn("almanach121");
  }

  /**
   * Test of getCurrentDay method, of class AlmanachSessionController.
   */
  @Test
  public void testGetCurrentDay() {
    AlmanachSessionController instance = new AlmanachSessionController(mainController, context);
    Calendar expResult = Calendar.getInstance();
    Date result = instance.getCurrentDay();
    Assert.assertTrue(expResult.getTimeInMillis() - result.getTime() < 1000l);
  }

  /**
   * Test of setCurrentDay method, of class AlmanachSessionController.
   */
  @Test
  public void testSetCurrentDay() {
    AlmanachSessionController instance = new AlmanachSessionController(mainController, context);
    Calendar expResult = Calendar.getInstance();
    instance.setCurrentDay(expResult.getTime());
    Date result = instance.getCurrentDay();
    Assert.assertEquals(expResult.getTimeInMillis(), result.getTime());
  }

  /**
   * Test of nextView method, of class AlmanachSessionController.
   */
  @Test
  public void testNextMonth() {
    AlmanachSessionController instance = new AlmanachSessionController(mainController, context);
    Calendar today = Calendar.getInstance();
    instance.setCurrentDay(today.getTime());
    Calendar nextMonth = Calendar.getInstance();
    nextMonth.setTime(today.getTime());
    nextMonth.add(Calendar.MONTH, 1);
    instance.nextView();
    Assert.assertEquals(nextMonth.getTimeInMillis(), instance.getCurrentDay().getTime());
  }

  /**
   * Test of previousView method, of class AlmanachSessionController.
   */
  @Test
  public void testPreviousMonth() {
    AlmanachSessionController instance = new AlmanachSessionController(mainController, context);
    Calendar today = Calendar.getInstance();
    instance.setCurrentDay(today.getTime());
    Calendar previousMonth = Calendar.getInstance();
    previousMonth.setTime(today.getTime());
    previousMonth.add(Calendar.MONTH, -1);
    instance.previousView();
    Assert.assertEquals(previousMonth.getTimeInMillis(), instance.getCurrentDay().getTime());
  }

  /**
   * Test of today method, of class AlmanachSessionController.
   */
  @Test
  public void testToday() {
    AlmanachSessionController instance = new AlmanachSessionController(mainController, context);
    instance.today();
    Calendar expResult = Calendar.getInstance();
    instance.setCurrentDay(expResult.getTime());
    Date result = instance.getCurrentDay();
    Assert.assertEquals(expResult.getTimeInMillis(), result.getTime());
  }
}
