/*
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.almanach.control.ejb;

import org.junit.Before;
import com.stratelia.webactiv.almanach.BaseAlmanachTest;
import com.stratelia.webactiv.almanach.model.EventOccurrence;
import com.stratelia.webactiv.almanach.model.Periodicity;
import java.util.Calendar;
import java.util.List;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.property.RRule;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.stratelia.webactiv.almanach.model.EventDetailBuilder.*;
import static com.stratelia.webactiv.almanach.model.EventOccurrenceMatcher.*;

/**
 * Unit tests on the implementation of some almanach EJB business operations.
 */
public class AlmanachBmEJBTest extends BaseAlmanachTest {

  private AlmanachBmEJB almanachBmEJB;

  public AlmanachBmEJBTest() {
  }

  @Before
  public void prepareAlmanachBmEJB() {
    almanachBmEJB = new AlmanachBmEJB();
  }

  /**
   * Test of generateRecurrenceRule method, of class AlmanachBmEJB.
   */
  @Test
  public void testGenerateRecurrenceRule() {
    Periodicity periodicity = new Periodicity();
    periodicity.setFrequency(1);
    periodicity.setUnity(Periodicity.UNIT_DAY);
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.DAY_OF_MONTH, 30);
    calend.set(Calendar.MONTH, Calendar.JUNE);
    calend.set(Calendar.YEAR, 2010);
    calend.set(Calendar.HOUR_OF_DAY, 10);
    calend.set(Calendar.MINUTE, 30);
    calend.set(Calendar.SECOND, 0);
    calend.set(Calendar.MILLISECOND, 0);
    periodicity.setUntilDatePeriod(calend.getTime());
    RRule result = almanachBmEJB.generateRecurrenceRule(periodicity);
    assertNotNull(result);
    assertFalse(result.isCalendarProperty());
    Recur recur = result.getRecur();
    assertNotNull(recur);
    assertNotNull(recur.getUntil());
    calend.set(Calendar.HOUR_OF_DAY, 23);
    calend.set(Calendar.MINUTE, 59);
    calend.set(Calendar.SECOND, 59);
    assertEquals(calend.getTimeInMillis(), recur.getUntil().getTime());
    assertEquals(Recur.DAILY, recur.getFrequency());
  }
  
  @Test
  public void eventOccurrencesInAGivenYearShouldBeCorrectlyObtained() throws Exception {
    List<EventOccurrence> occurrences = almanachBmEJB.getEventOccurrencesInYear(year2011(),
        almanachIds);
    assertThat(occurrences.size(), is(27));
  }

  @Test
  public void eventOccurrencesInAGivenMonthShouldBeCorrectlyObtained() throws Exception {
    List<EventOccurrence> occurrences = almanachBmEJB.getEventOccurrencesInMonth(april2011(),
        almanachIds);
    assertThat(occurrences.size(), is(5));
    assertThat(occurrences.get(0), is(anOccurrenceOfEvent(PERIODIC_EVENTS[0],
        startingAt("2011-04-05T09:30"),
        endingAt("2011-04-05T12:00"))));
    assertThat(occurrences.get(1), is(anOccurrenceOfEvent(PERIODIC_EVENTS[0],
        startingAt("2011-04-12T09:30"),
        endingAt("2011-04-12T12:00"))));
    assertThat(occurrences.get(2), is(anOccurrenceOfEvent(NON_PERIODIC_EVENTS[0],
        startingAt("2011-04-13T09:30"),
        endingAt("2011-04-13T09:30"))));
    assertThat(occurrences.get(3), is(anOccurrenceOfEvent(NON_PERIODIC_EVENTS[1],
        startingAt("2011-04-15"),
        endingAt("2011-04-15"))));
    assertThat(occurrences.get(4), is(anOccurrenceOfEvent(PERIODIC_EVENTS[1],
        startingAt("2011-04-20T09:30"),
        endingAt("2011-04-20T09:30"))));
  }

  @Test
  public void eventOccurrencesInAGivenWeekShouldBeCorrectlyObtained() throws Exception {
    List<EventOccurrence> occurrences = almanachBmEJB.getEventOccurrencesInWeek(week15In2011(),
        almanachIds);
    assertThat(occurrences.size(), is(3));
    assertThat(occurrences.get(0), is(anOccurrenceOfEvent(PERIODIC_EVENTS[0],
        startingAt("2011-04-12T09:30"),
        endingAt("2011-04-12T12:00"))));
    assertThat(occurrences.get(1), is(anOccurrenceOfEvent(NON_PERIODIC_EVENTS[0],
        startingAt("2011-04-13T09:30"),
        endingAt("2011-04-13T09:30"))));
    assertThat(occurrences.get(2), is(anOccurrenceOfEvent(NON_PERIODIC_EVENTS[1],
        startingAt("2011-04-15"),
        endingAt("2011-04-15"))));
  }
  
  private Calendar year2011() {
    Calendar year2011 = Calendar.getInstance();
    year2011.setTime(dateToUseInTests());
    return year2011;
  }

  private Calendar april2011() {
    Calendar april = Calendar.getInstance();
    april.setTime(dateToUseInTests());
    return april;
  }

  private Calendar week15In2011() {
    Calendar week15 = Calendar.getInstance();
    week15.setTime(dateToUseInTests());
    return week15;
  }
}
