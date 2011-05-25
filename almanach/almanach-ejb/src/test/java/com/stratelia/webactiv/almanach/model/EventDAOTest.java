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
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.stratelia.webactiv.almanach.model.EventDetailBuilder.*;
import static com.stratelia.webactiv.almanach.model.EventDetailMatcher.*;

/**
 * Unit tests on the DAO of event details.
 */
public class EventDAOTest extends BaseAlmanachTest {

  public EventDAOTest() {
  }

  @Test
  public void emptyTest() {
    assertTrue(true);
  }

  @Test
  public void fetchEventsDefinedInAMonth() throws Exception {
    Date month = dateToUseInTests();
    EventPK pk = new EventPK("", null, almanachIds[0]);
    List<EventDetail> events = new ArrayList<EventDetail>(EventDAO.getMonthEvents(getConnection(),
        pk,
        month, almanachIds));
    assertThat(events.size(), is(4));
    assertThat(events.get(0), is(theEventDetail(anEventDetailOfId("1000").build())));
    assertThat(events.get(1), is(theEventDetail(anEventDetailOfId("1001").build())));
    assertThat(events.get(2), is(theEventDetail(anEventDetailOfId("1002").build())));
    assertThat(events.get(3), is(theEventDetail(anEventDetailOfId("1003").build())));
  }
  
  @Test
  public void fetchEventsDefinedInAMonthForAGivenAlmanach() throws Exception {
    Date month = dateToUseInTests();
    EventPK pk = new EventPK("", null, almanachIds[0]);
    List<EventDetail> events = new ArrayList<EventDetail>(EventDAO.getMonthEvents(getConnection(),
        pk, month, null));
    assertThat(events.size(), is(1));
    assertThat(events.get(0), is(theEventDetail(anEventDetailOfId("1000").build())));
  }

  @Test
  public void fetchEventsForAMonthWithoutAnyEvents() throws Exception {
    Calendar month = Calendar.getInstance();
    month.add(Calendar.YEAR, 1);
    EventPK pk = new EventPK("", null, almanachIds[0]);
    List<EventDetail> events = new ArrayList<EventDetail>(EventDAO.getMonthEvents(getConnection(),
        pk,
        month.getTime(), almanachIds));
    assertThat(events.isEmpty(), is(true));
  }

  @Test
  public void fetchEventsDefinedInAMonthForEmptyAlmanach() throws Exception {
    Date month = dateToUseInTests();
    EventPK pk = new EventPK("", null, "0");
    List<EventDetail> events = new ArrayList<EventDetail>(EventDAO.getMonthEvents(getConnection(),
        pk,
        month, new String[]{"0"}));
    assertThat(events.isEmpty(), is(true));
  }

  @Test
  public void fetchEventsForAWeekWithoutAnyEvents() throws Exception {
    Calendar week = Calendar.getInstance();
    week.add(Calendar.YEAR, 1);
    EventPK pk = new EventPK("", null, almanachIds[0]);
    List<EventDetail> events = new ArrayList<EventDetail>(EventDAO.getWeekEvents(getConnection(), pk,
        week.getTime(), almanachIds));
    assertThat(events.isEmpty(), is(true));
  }

  @Test
  public void fetchEventsDefinedInAWeekForEmptyAlmanach() throws Exception {
    Date week = dateToUseInTests();
    EventPK pk = new EventPK("", null, "0");
    List<EventDetail> events = new ArrayList<EventDetail>(EventDAO.getWeekEvents(getConnection(), pk,
        week, new String[]{"0"}));
    assertThat(events.isEmpty(), is(true));
  }

  @Test
  public void fetchEventsDefinedInAWeek() throws Exception {
    Date week = dateToUseInTests();
    EventPK pk = new EventPK("", null, almanachIds[0]);
    List<EventDetail> events = new ArrayList<EventDetail>(EventDAO.getWeekEvents(getConnection(), pk,
        week, almanachIds));
    assertThat(events.size(), is(3));
    assertThat(events.get(0), is(theEventDetail(anEventDetailOfId("1000").build())));
    assertThat(events.get(1), is(theEventDetail(anEventDetailOfId("1001").build())));
    assertThat(events.get(2), is(theEventDetail(anEventDetailOfId("1002").build())));
  }
  
  @Test
  public void fetchEventsDefinedInAWeekForAGivenAlmanach() throws Exception {
    Date week = dateToUseInTests();
    EventPK pk = new EventPK("", null, almanachIds[0]);
    List<EventDetail> events = new ArrayList<EventDetail>(EventDAO.getWeekEvents(getConnection(),
        pk, week, null));
    assertThat(events.size(), is(1));
    assertThat(events.get(0), is(theEventDetail(anEventDetailOfId("1000").build())));
  }

  @Test
  public void fetchAPeriodicEvent() throws Exception {
    EventDetail expected = anEventDetailOfId("1000").build();
    assertThat(expected.getPeriodicity(), notNullValue());
    EventDetail actual = EventDAO.getEventDetail(getConnection(), expected.getPK());
    assertThat(actual, notNullValue());
    assertThat(actual.getPeriodicity(), notNullValue());
    assertThat(actual, is(expected));
  }

  @Test
  public void fetchANonPeriodicEvent() throws Exception {
    EventDetail expected = anEventDetailOfId("1001").build();
    assertThat(expected.getPeriodicity(), nullValue());
    EventDetail actual = EventDAO.getEventDetail(getConnection(), expected.getPK());
    assertThat(actual, notNullValue());
    assertThat(actual.getPeriodicity(), nullValue());
    assertThat(actual, is(expected));
  }

  @Test
  public void fetchANonExistingEvent() throws Exception {
    EventPK pk = new EventPK("10", null, almanachIds[0]);
    EventDetail actual = EventDAO.getEventDetail(getConnection(), pk);
    assertThat(actual, nullValue());
  }

  @Test
  public void fetchAllEventsOfAnAlmanach() throws Exception {
    EventPK pk = new EventPK("", null, almanachIds[1]);
    List<EventDetail> allEvents = new ArrayList<EventDetail>(EventDAO.getAllEvents(getConnection(),
        pk));
    assertThat(allEvents.size(), is(2));
    assertThat(allEvents.get(0), is(theEventDetail(anEventDetailOfId("1002").build())));
    assertThat(allEvents.get(1), is(theEventDetail(anEventDetailOfId("1003").build())));
  }

  @Test
  public void fetchAllEventsOfAnUnexistingAlmanachs() throws Exception {
    EventPK pk = new EventPK("", null, "almanach1");
    Collection<EventDetail> allEvents = EventDAO.getAllEvents(getConnection(), pk);
    assertThat(allEvents.isEmpty(), is(true));
  }

  @Test
  public void fetchAllEventsOfSeveralAlmanachs() throws Exception {
    EventPK pk = new EventPK("", null, almanachIds[0]);
    List<EventDetail> allEvents = new ArrayList<EventDetail>(EventDAO.getAllEvents(getConnection(),
        pk, almanachIds));
    assertThat(allEvents.size(), is(4));
    assertThat(allEvents.get(0), is(theEventDetail(anEventDetailOfId("1000").build())));
    assertThat(allEvents.get(1), is(theEventDetail(anEventDetailOfId("1001").build())));
    assertThat(allEvents.get(2), is(theEventDetail(anEventDetailOfId("1002").build())));
    assertThat(allEvents.get(3), is(theEventDetail(anEventDetailOfId("1003").build())));
  }

  @Test
  public void fetchAllEventsOfSeveralUnexistingAlmanachs() throws Exception {
    EventPK pk = new EventPK("", null, "almanach1");
    Collection<EventDetail> allEvents = EventDAO.getAllEvents(getConnection(), pk, new String[]{
          "almanach2", "almanach3"});
    assertThat(allEvents.isEmpty(), is(true));
  }

  @Test
  public void fetchSomeEvents() throws Exception {
    EventDetail event1 = anEventDetailOfId("1000").build();
    EventDetail event2 = anEventDetailOfId("1003").build();
    List<EventPK> pks = new ArrayList<EventPK>();
    pks.add(event1.getPK());
    pks.add(event2.getPK());
    List<EventDetail> events = new ArrayList<EventDetail>(EventDAO.getEvents(getConnection(), pks));
    assertThat(events.size(), is(2));
    assertThat(events.get(0), is(theEventDetail(event1)));
    assertThat(events.get(1), is(theEventDetail(event2)));
  }
}
