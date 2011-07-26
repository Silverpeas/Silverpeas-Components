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
package com.stratelia.webactiv.almanach.model;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import com.stratelia.webactiv.almanach.BaseAlmanachTest;
import java.util.Calendar;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.stratelia.webactiv.almanach.model.EventDetailBuilder.*;
import static com.stratelia.webactiv.almanach.model.EventDetailMatcher.*;
import static com.stratelia.webactiv.util.DateUtil.*;

/**
 * Unit tests on the DAO of event details.
 */
public class EventDAOTest extends BaseAlmanachTest {

  private EventDAO eventDAO;

  public EventDAOTest() {
  }

  @Before
  public void prepareEventDAO() {
    this.eventDAO = new EventDAO();
  }

  @Test
  public void emptyTest() {
    assertTrue(true);
  }

  @Test
  public void fetchEventsDefinedInAYear() throws Exception {
    Date year = dateToUseInTests();
    List<EventDetail> events = new ArrayList<EventDetail>(eventDAO.findAllEventsInYear(year,
            almanachIds));
    assertThat(events.size(), is(4));
    assertThat(events.get(0), is(theEventDetail(anEventDetailOfId("1000").build())));
    assertThat(events.get(1), is(theEventDetail(anEventDetailOfId("1001").build())));
    assertThat(events.get(2), is(theEventDetail(anEventDetailOfId("1002").build())));
    assertThat(events.get(3), is(theEventDetail(anEventDetailOfId("1003").build())));
  }

  @Test
  public void fetchEventsDefinedInAYearForAGivenAlmanach() throws Exception {
    Date year = dateToUseInTests();
    List<EventDetail> events = new ArrayList<EventDetail>(eventDAO.findAllEventsInYear(year,
            almanachIds[0]));
    assertThat(events.size(), is(1));
    assertThat(events.get(0), is(theEventDetail(anEventDetailOfId("1000").build())));
  }

  @Test
  public void fetchEventsForAYearWithoutAnyEvents() throws Exception {
    Calendar year = Calendar.getInstance();
    year.add(Calendar.YEAR, 10);
    List<EventDetail> events = new ArrayList<EventDetail>(eventDAO.findAllEventsInYear(
            year.getTime(),
            almanachIds));
    assertThat(events.isEmpty(), is(true));
  }

  @Test
  public void fetchEventsDefinedInAYearForEmptyAlmanach() throws Exception {
    Date year = dateToUseInTests();
    List<EventDetail> events = new ArrayList<EventDetail>(eventDAO.findAllEventsInYear(year, "0"));
    assertThat(events.isEmpty(), is(true));
  }

  @Test
  public void fetchEventsDefinedInAMonth() throws Exception {
    Date month = dateToUseInTests();
    List<EventDetail> events = new ArrayList<EventDetail>(eventDAO.findAllEventsInMonth(month,
            almanachIds));
    assertThat(events.size(), is(4));
    assertThat(events.get(0), is(theEventDetail(anEventDetailOfId("1000").build())));
    assertThat(events.get(1), is(theEventDetail(anEventDetailOfId("1001").build())));
    assertThat(events.get(2), is(theEventDetail(anEventDetailOfId("1002").build())));
    assertThat(events.get(3), is(theEventDetail(anEventDetailOfId("1003").build())));
  }

  @Test
  public void fetchEventsDefinedInAMonthForAGivenAlmanach() throws Exception {
    Date month = dateToUseInTests();
    List<EventDetail> events = new ArrayList<EventDetail>(eventDAO.findAllEventsInMonth(month,
            almanachIds[0]));
    assertThat(events.size(), is(1));
    assertThat(events.get(0), is(theEventDetail(anEventDetailOfId("1000").build())));
  }

  @Test
  public void fetchEventsForAMonthWithoutAnyEvents() throws Exception {
    Calendar month = Calendar.getInstance();
    month.add(Calendar.YEAR, 1);
    List<EventDetail> events = new ArrayList<EventDetail>(eventDAO.findAllEventsInMonth(month.
            getTime(), almanachIds));
    assertThat(events.isEmpty(), is(true));
  }

  @Test
  public void fetchEventsDefinedInAMonthForEmptyAlmanach() throws Exception {
    Date month = dateToUseInTests();
    List<EventDetail> events = new ArrayList<EventDetail>(eventDAO.findAllEventsInMonth(month, "0"));
    assertThat(events.isEmpty(), is(true));
  }

  @Test
  public void fetchEventsForAWeekWithoutAnyEvents() throws Exception {
    Calendar week = Calendar.getInstance();
    week.add(Calendar.YEAR, 1);
    List<EventDetail> events = new ArrayList<EventDetail>(eventDAO.findAllEventsInWeek(
            week.getTime(),
            almanachIds));
    assertThat(events.isEmpty(), is(true));
  }

  @Test
  public void fetchEventsDefinedInAWeekForEmptyAlmanach() throws Exception {
    Date week = dateToUseInTests();
    List<EventDetail> events = new ArrayList<EventDetail>(eventDAO.findAllEventsInWeek(week, "0"));
    assertThat(events.isEmpty(), is(true));
  }

  @Test
  public void fetchEventsDefinedInAWeek() throws Exception {
    Date week = dateToUseInTests();
    List<EventDetail> events = new ArrayList<EventDetail>(eventDAO.findAllEventsInWeek(week,
            almanachIds));
    assertThat(events.size(), is(3));
    assertThat(events.get(0), is(theEventDetail(anEventDetailOfId("1000").build())));
    assertThat(events.get(1), is(theEventDetail(anEventDetailOfId("1001").build())));
    assertThat(events.get(2), is(theEventDetail(anEventDetailOfId("1002").build())));
  }

  @Test
  public void fetchEventsDefinedInAWeekForAGivenAlmanach() throws Exception {
    Date week = dateToUseInTests();
    List<EventDetail> events = new ArrayList<EventDetail>(eventDAO.findAllEventsInWeek(week,
            almanachIds[0]));
    assertThat(events.size(), is(1));
    assertThat(events.get(0), is(theEventDetail(anEventDetailOfId("1000").build())));
  }

  @Test
  public void fetchAPeriodicEvent() throws Exception {
    EventDetail expected = anEventDetailOfId("1000").build();
    assertThat(expected.getPeriodicity(), notNullValue());
    EventDetail actual = eventDAO.findEventByPK(expected.getPK());
    assertThat(actual, notNullValue());
    assertThat(actual.getPeriodicity(), notNullValue());
    assertThat(actual, is(expected));
  }

  @Test
  public void fetchANonPeriodicEvent() throws Exception {
    EventDetail expected = anEventDetailOfId("1001").build();
    assertThat(expected.getPeriodicity(), nullValue());
    EventDetail actual = eventDAO.findEventByPK(expected.getPK());
    assertThat(actual, notNullValue());
    assertThat(actual.getPeriodicity(), nullValue());
    assertThat(actual, is(expected));
  }

  @Test
  public void fetchANonExistingEvent() throws Exception {
    EventPK pk = new EventPK("10", null, almanachIds[0]);
    EventDetail actual = eventDAO.findEventByPK(pk);
    assertThat(actual, nullValue());
  }

  @Test
  public void fetchAllEventsOfAnAlmanach() throws Exception {
    List<EventDetail> allEvents = new ArrayList<EventDetail>(eventDAO.findAllEvents(almanachIds[1]));
    assertThat(allEvents.size(), is(4));
    assertThat(allEvents.get(0), is(theEventDetail(anEventDetailOfId("1002").build())));
    assertThat(allEvents.get(1), is(theEventDetail(anEventDetailOfId("1003").build())));
    assertThat(allEvents.get(2), is(theEventDetail(anEventDetailOfId("1004").build())));
    assertThat(allEvents.get(3), is(theEventDetail(anEventDetailOfId("1005").build())));
  }

  @Test
  public void fetchAllEventsOfAnUnexistingAlmanachs() throws Exception {
    Collection<EventDetail> allEvents = eventDAO.findAllEvents("almanach1");
    assertThat(allEvents.isEmpty(), is(true));
  }

  @Test
  public void fetchAllEventsOfSeveralAlmanachs() throws Exception {
    List<EventDetail> allEvents = new ArrayList<EventDetail>(eventDAO.findAllEvents(almanachIds));
    assertThat(allEvents.size(), is(6));
    assertThat(allEvents.get(0), is(theEventDetail(anEventDetailOfId("1000").build())));
    assertThat(allEvents.get(1), is(theEventDetail(anEventDetailOfId("1001").build())));
    assertThat(allEvents.get(2), is(theEventDetail(anEventDetailOfId("1002").build())));
    assertThat(allEvents.get(3), is(theEventDetail(anEventDetailOfId("1003").build())));
    assertThat(allEvents.get(4), is(theEventDetail(anEventDetailOfId("1004").build())));
    assertThat(allEvents.get(5), is(theEventDetail(anEventDetailOfId("1005").build())));
  }

  @Test
  public void fetchAllEventsOfSeveralUnexistingAlmanachs() throws Exception {
    Collection<EventDetail> allEvents =
            eventDAO.findAllEvents("almanach1", "almanach2", "almanach3");
    assertThat(allEvents.isEmpty(), is(true));
  }

  @Test
  public void fetchSomeEvents() throws Exception {
    EventDetail event1 = anEventDetailOfId("1000").build();
    EventDetail event2 = anEventDetailOfId("1003").build();
    List<EventPK> pks = new ArrayList<EventPK>();
    pks.add(event1.getPK());
    pks.add(event2.getPK());
    List<EventDetail> events = new ArrayList<EventDetail>(eventDAO.findAllEventsByPK(pks));
    assertThat(events.size(), is(2));
    assertThat(events.get(0), is(theEventDetail(event1)));
    assertThat(events.get(1), is(theEventDetail(event2)));
  }

  @Test
  public void fetchEventsInAGivenRange() throws Exception {
    Date dateToUseInTests = dateToUseInTests();
    String startDay = aStartDayIn(dateToUseInTests);
    String endDay = anEndDayIn(dateToUseInTests);
    List<EventDetail> events = new ArrayList<EventDetail>(eventDAO.findAllEventsInRange(startDay,
            endDay, almanachIds));
    assertThat(events.size(), is(5));
    assertThat(events.get(0), is(theEventDetail(anEventDetailOfId("1000").build())));
    assertThat(events.get(1), is(theEventDetail(anEventDetailOfId("1001").build())));
    assertThat(events.get(2), is(theEventDetail(anEventDetailOfId("1002").build())));
    assertThat(events.get(3), is(theEventDetail(anEventDetailOfId("1003").build())));
    assertThat(events.get(4), is(theEventDetail(anEventDetailOfId("1004").build())));
  }
  
  @Test
  public void fetchEventInARangeWithoutEndDay() throws Exception {
    String startDay = aStartDayIn(dateToUseInTests());
    List<EventDetail> events = new ArrayList<EventDetail>(eventDAO.findAllEventsInRange(startDay,
            null, almanachIds));
    assertThat(events.size(), is(6));
    assertThat(events.get(0), is(theEventDetail(anEventDetailOfId("1000").build())));
    assertThat(events.get(1), is(theEventDetail(anEventDetailOfId("1001").build())));
    assertThat(events.get(2), is(theEventDetail(anEventDetailOfId("1002").build())));
    assertThat(events.get(3), is(theEventDetail(anEventDetailOfId("1003").build())));
    assertThat(events.get(4), is(theEventDetail(anEventDetailOfId("1004").build())));
    assertThat(events.get(5), is(theEventDetail(anEventDetailOfId("1005").build())));
  }
  
  @Test
  public void fetchEventInARangeWithoutAnyEvents() throws Exception {
    Date dateToUseInTests = dateToUseInTests();
    String startDay = aStartDayIn10YearsAfter(dateToUseInTests);
    String endDay = anEndDayIn10YearsAfter(dateToUseInTests);
    List<EventDetail> events = new ArrayList<EventDetail>(eventDAO.findAllEventsInRange(startDay,
            endDay, almanachIds));
    assertThat(events.isEmpty(), is(true));
  }

  private String aStartDayIn(final Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(java.util.Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
    return date2SQLDate(calendar.getTime());
  }

  private String anEndDayIn(final Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.YEAR, 1);
    return date2SQLDate(calendar.getTime());
  }
  
  private String aStartDayIn10YearsAfter(final Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.YEAR, 10);
    calendar.set(java.util.Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
    return date2SQLDate(calendar.getTime());
  }
  
  private String anEndDayIn10YearsAfter(final Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.YEAR, 10);
    return date2SQLDate(calendar.getTime());
  }
}
