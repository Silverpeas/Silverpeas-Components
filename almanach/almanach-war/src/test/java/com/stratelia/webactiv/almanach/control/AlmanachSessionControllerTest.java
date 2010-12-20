/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
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
import com.stratelia.webactiv.almanach.control.ejb.AlmanachBm;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.almanach.model.EventPK;
import com.stratelia.webactiv.almanach.model.Periodicity;
import java.util.Calendar;
import java.util.List;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Uid;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import static org.mockito.Mockito.*;

/**
 *
 * @author ehugonnet
 */
public class AlmanachSessionControllerTest {

  public AlmanachSessionControllerTest() {
  }

  /**
   * Test of getCurrentDay method, of class AlmanachSessionController.
   */
  @Test
  public void testGetCurrentDay() {
    MainSessionController mainController = mock(MainSessionController.class);
    ComponentContext context = mock(ComponentContext.class);
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
    MainSessionController mainController = mock(MainSessionController.class);
    ComponentContext context = mock(ComponentContext.class);
    when(context.getCurrentComponentId()).thenReturn("almanach121");
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
    MainSessionController mainController = mock(MainSessionController.class);
    ComponentContext context = mock(ComponentContext.class);
    when(context.getCurrentComponentId()).thenReturn("almanach121");
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
    MainSessionController mainController = mock(MainSessionController.class);
    ComponentContext context = mock(ComponentContext.class);
    when(context.getCurrentComponentId()).thenReturn("almanach121");
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
    MainSessionController mainController = mock(MainSessionController.class);
    ComponentContext context = mock(ComponentContext.class);
    when(context.getCurrentComponentId()).thenReturn("almanach121");
    AlmanachSessionController instance = new AlmanachSessionController(mainController, context);
    instance.today();
    Calendar expResult = Calendar.getInstance();
    instance.setCurrentDay(expResult.getTime());
    Date result = instance.getCurrentDay();
    Assert.assertEquals(expResult.getTimeInMillis(), result.getTime());
  }

  /**
   * Test of listCurrentMonthEvents method, of class AlmanachSessionController.
   * @throws Exception
   */
  @Test
  public void testListCurrentMonthEvents() throws Exception {
    MainSessionController mainController = mock(MainSessionController.class);
    ComponentContext context = mock(ComponentContext.class);
    when(context.getCurrentComponentId()).thenReturn("almanach121");

    Calendar recurDate = Calendar.getInstance();
    recurDate.set(Calendar.YEAR, 2010);
    recurDate.set(Calendar.MONTH, Calendar.JUNE);
    recurDate.set(Calendar.DAY_OF_MONTH, 30);
    recurDate.set(Calendar.HOUR_OF_DAY, 11);
    recurDate.set(Calendar.MINUTE, 0);
    recurDate.set(Calendar.SECOND, 0);
    recurDate.set(Calendar.MILLISECOND, 0);

    Periodicity periodicity = new Periodicity();
    periodicity.setFrequency(1);
    periodicity.setUnity(Periodicity.UNIT_DAY);
    periodicity.setUntilDatePeriod(recurDate.getTime());
    RRule rrule = periodicity.generateRecurrenceRule();

    Calendar eventDate = Calendar.getInstance();
    eventDate.set(Calendar.YEAR, 2010);
    eventDate.set(Calendar.MONTH, Calendar.JUNE);
    eventDate.set(Calendar.DAY_OF_MONTH, 1);
    eventDate.set(Calendar.HOUR_OF_DAY, 10);
    eventDate.set(Calendar.MINUTE, 0);
    eventDate.set(Calendar.SECOND, 0);
    eventDate.set(Calendar.MILLISECOND, 0);
    DateTime startDate = new DateTime(eventDate.getTimeInMillis());
    eventDate.set(Calendar.HOUR_OF_DAY, 11);
    DateTime endDate = new DateTime(eventDate.getTimeInMillis());

    EventDetail detail = new EventDetail();
    detail.setPK(new EventPK("10", "", "almanach121"));
    detail.setTitle("Daily recurence June Event");
    detail.setPriority(5);
    detail.setStartHour("10:00");
    detail.setEndHour("11:00");
    AlmanachBm almanach = mock(AlmanachBm.class);
    when(almanach.getEventDetail(Matchers.any(EventPK.class))).thenReturn(detail);

    net.fortuna.ical4j.model.Calendar icalCalendar = new net.fortuna.ical4j.model.Calendar();
    icalCalendar.getProperties().add(CalScale.GREGORIAN);
    VEvent event = new VEvent(startDate, endDate, "Daily recurence June Event");
    Uid uid = new Uid("10");
    event.getProperties().add(uid);
    event.getProperties().add(rrule);
    icalCalendar.getComponents().add(event);
    when(almanach.getICal4jCalendar(anyCollectionOf(EventDetail.class), anyString())).thenReturn(icalCalendar);

    Calendar currentDay = Calendar.getInstance();
    currentDay.set(Calendar.YEAR, 2010);
    currentDay.set(Calendar.MONTH, Calendar.JUNE);
    currentDay.set(Calendar.DAY_OF_MONTH, 15);
    currentDay.set(Calendar.HOUR_OF_DAY, 12);
    currentDay.set(Calendar.MINUTE, 0);
    currentDay.set(Calendar.SECOND, 0);
    currentDay.set(Calendar.MILLISECOND, 0);

    AlmanachSessionController instance = new AlmanachSessionController(mainController, context);
    instance.setCurrentDay(currentDay.getTime());
    instance.setAlmanachBm(almanach);

    List<EventOccurrenceDTO> events = instance.listCurrentMonthEvents();
    Assert.assertNotNull(events);
    Assert.assertEquals(30, events.size());

    events = instance.listCurrentWeekEvents();
    Assert.assertNotNull(events);
    Assert.assertEquals(7, events.size());
  }
}
