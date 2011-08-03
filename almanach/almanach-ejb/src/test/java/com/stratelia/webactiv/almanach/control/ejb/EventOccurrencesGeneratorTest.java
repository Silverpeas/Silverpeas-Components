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

import com.silverpeas.calendar.Date;
import com.stratelia.webactiv.almanach.BaseAlmanachTest;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.almanach.model.EventOccurrence;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.stratelia.webactiv.almanach.model.EventDetailBuilder.*;
import static com.stratelia.webactiv.almanach.model.EventOccurrenceMatcher.*;

/**
 * Unit tests on the generation of occurrences of events in a given period.
 */
public class EventOccurrencesGeneratorTest extends BaseAlmanachTest {
  
  private EventOccurrenceGenerator generator;
  
  public EventOccurrencesGeneratorTest() {
  }
  
  @Before
  public void prepareOccurrenceGenerator() {
    EventOccurrenceGeneratorFactory generatorFactory = EventOccurrenceGeneratorFactory.getFactory();
    generator = generatorFactory.getEventOccurrenceGenerator();
    assertThat(generator, notNullValue());
  }
  
  @Test
  public void generateOccurrencesInYearWithNoEvents() {
    List<EventDetail> events = new ArrayList<EventDetail>();
    List<EventOccurrence> occurrences = generator.generateOccurrencesInYear(aPeriod(), events);
    assertThat(occurrences.isEmpty(), is(true));
  }
  
  @Test
  public void generateOccurrencesInYearWithNonPeriodicEvents() throws Exception {
    List<EventDetail> events = new ArrayList<EventDetail>();
    events.add(anEventDetailOfId(NON_PERIODIC_EVENTS[0]).build());
    events.add(anEventDetailOfId(NON_PERIODIC_EVENTS[1]).build());
    List<EventOccurrence> occurrences = generator.generateOccurrencesInYear(aPeriod(), events);
    assertThat(occurrences.size(), is(events.size()));
    assertThat(occurrences.get(0), is(anOccurrenceOfEvent(NON_PERIODIC_EVENTS[0],
        startingAt("2011-04-13T09:30"),
        endingAt("2011-04-13T09:30"))));
    assertThat(occurrences.get(1), is(anOccurrenceOfEvent(NON_PERIODIC_EVENTS[1],
        startingAt("2011-04-15"),
        endingAt("2011-04-15"))));
  }
  
  @Test
  public void generateOccurrencesInYearWithPeriodicEvents() throws Exception {
    List<EventDetail> events = new ArrayList<EventDetail>();
    events.add(anEventDetailOfId(PERIODIC_EVENTS[0]).build()); // it has 20 occurrences in the given year
    events.add(anEventDetailOfId(PERIODIC_EVENTS[1]).build()); // it has 5 occurrences in the given year
    List<EventOccurrence> occurrences = generator.generateOccurrencesInYear(aPeriod(), events);
    assertThat(occurrences.size(), is(25));
  }
  
  @Test
  public void generateOccurrencesInYearWithPeriodicAndNonPeriodicEvents() throws Exception {
    List<EventDetail> events = new ArrayList<EventDetail>();
    events.add(anEventDetailOfId(PERIODIC_EVENTS[0]).build()); // it has 20 occurrences in the given year
    events.add(anEventDetailOfId(NON_PERIODIC_EVENTS[0]).build());
    events.add(anEventDetailOfId(PERIODIC_EVENTS[1]).build()); // it has 5 occurrences in the given year
    events.add(anEventDetailOfId(NON_PERIODIC_EVENTS[1]).build());
    List<EventOccurrence> occurrences = generator.generateOccurrencesInYear(aPeriod(), events);
    assertThat(occurrences.size(), is(27));
  }

  @Test
  public void generateOccurrencesInMonthWithNoEvents() {
    List<EventDetail> events = new ArrayList<EventDetail>();
    List<EventOccurrence> occurrences = generator.generateOccurrencesInMonth(aPeriod(), events);
    assertThat(occurrences.isEmpty(), is(true));
  }
  
  @Test
  public void generateOccurrencesInMonthWithNonPeriodicEvents() throws Exception {
    List<EventDetail> events = new ArrayList<EventDetail>();
    events.add(anEventDetailOfId(NON_PERIODIC_EVENTS[0]).build());
    events.add(anEventDetailOfId(NON_PERIODIC_EVENTS[1]).build());
    List<EventOccurrence> occurrences = generator.generateOccurrencesInMonth(aPeriod(), events);
    assertThat(occurrences.size(), is(events.size()));
    assertThat(occurrences.get(0), is(anOccurrenceOfEvent(NON_PERIODIC_EVENTS[0],
        startingAt("2011-04-13T09:30"),
        endingAt("2011-04-13T09:30"))));
    assertThat(occurrences.get(1), is(anOccurrenceOfEvent(NON_PERIODIC_EVENTS[1],
        startingAt("2011-04-15"),
        endingAt("2011-04-15"))));
  }
  
  @Test
  public void generateOccurrencesInMonthWithPeriodicEvents() throws Exception {
    List<EventDetail> events = new ArrayList<EventDetail>();
    events.add(anEventDetailOfId(PERIODIC_EVENTS[0]).build()); // it has two occurrences in the given month
    events.add(anEventDetailOfId(PERIODIC_EVENTS[1]).build()); // it has only one occurrence in the given month
    List<EventOccurrence> occurrences = generator.generateOccurrencesInMonth(aPeriod(), events);
    assertThat(occurrences.size(), is(3));
    assertThat(occurrences.get(0), is(anOccurrenceOfEvent(PERIODIC_EVENTS[0],
        startingAt("2011-04-05T09:30"),
        endingAt("2011-04-05T12:00"))));
    assertThat(occurrences.get(1), is(anOccurrenceOfEvent(PERIODIC_EVENTS[0],
        startingAt("2011-04-12T09:30"),
        endingAt("2011-04-12T12:00"))));
    assertThat(occurrences.get(2), is(anOccurrenceOfEvent(PERIODIC_EVENTS[1],
        startingAt("2011-04-20T09:30"),
        endingAt("2011-04-20T09:30"))));
  }
  
  @Test
  public void generateOccurrencesInMonthWithPeriodicAndNonPeriodicEvents() throws Exception {
    List<EventDetail> events = new ArrayList<EventDetail>();
    events.add(anEventDetailOfId(PERIODIC_EVENTS[0]).build()); // it has two occurrences in the given month
    events.add(anEventDetailOfId(NON_PERIODIC_EVENTS[0]).build());
    events.add(anEventDetailOfId(PERIODIC_EVENTS[1]).build()); // it has only one occurrence in the given month
    events.add(anEventDetailOfId(NON_PERIODIC_EVENTS[1]).build());
    List<EventOccurrence> occurrences = generator.generateOccurrencesInMonth(aPeriod(), events);
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
  public void generateOccurrencesInWeekWithNoEvents() {
    List<EventDetail> events = new ArrayList<EventDetail>();
    List<EventOccurrence> occurrences = generator.generateOccurrencesInWeek(aPeriod(), events);
    assertThat(occurrences.isEmpty(), is(true));
  }

  @Test
  public void generateOccurrencesInWeekWithNonPeriodicEvents() throws Exception {
    List<EventDetail> events = new ArrayList<EventDetail>();
    events.add(anEventDetailOfId(NON_PERIODIC_EVENTS[0]).build());
    events.add(anEventDetailOfId(NON_PERIODIC_EVENTS[1]).build());
    List<EventOccurrence> occurrences = generator.generateOccurrencesInWeek(aPeriod(), events);
    assertThat(occurrences.size(), is(events.size()));
    assertThat(occurrences.get(0), is(anOccurrenceOfEvent(NON_PERIODIC_EVENTS[0],
        startingAt("2011-04-13T09:30"),
        endingAt("2011-04-13T09:30"))));
    assertThat(occurrences.get(1), is(anOccurrenceOfEvent(NON_PERIODIC_EVENTS[1],
        startingAt("2011-04-15"),
        endingAt("2011-04-15"))));
  }
  
  @Test
  public void generateOccurrencesInWeekWithPeriodicEvents() throws Exception {
    List<EventDetail> events = new ArrayList<EventDetail>();
    events.add(anEventDetailOfId(PERIODIC_EVENTS[0]).build()); // it has one occurrence in the given week
    events.add(anEventDetailOfId(PERIODIC_EVENTS[1]).build()); // it has no occurrence in the given week
    List<EventOccurrence> occurrences = generator.generateOccurrencesInWeek(aPeriod(), events);
    assertThat(occurrences.size(), is(1));
    assertThat(occurrences.get(0), is(anOccurrenceOfEvent(PERIODIC_EVENTS[0],
        startingAt("2011-04-12T09:30"),
        endingAt("2011-04-12T12:00"))));
  }
  
  @Test
  public void generateOccurrencesInWeekWithPeriodicAndNonPeriodicEvents() throws Exception {
    List<EventDetail> events = new ArrayList<EventDetail>();
    events.add(anEventDetailOfId(PERIODIC_EVENTS[0]).build()); // it has one occurrence in the given week
    events.add(anEventDetailOfId(NON_PERIODIC_EVENTS[0]).build());
    events.add(anEventDetailOfId(PERIODIC_EVENTS[1]).build()); // it has no occurrence in the given week
    events.add(anEventDetailOfId(NON_PERIODIC_EVENTS[1]).build());
    List<EventOccurrence> occurrences = generator.generateOccurrencesInWeek(aPeriod(), events);
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
  
  @Test
  public void generateOccurrencesFromAGivenDateWithNoEvents() throws Exception {
    List<EventDetail> events = new ArrayList<EventDetail>();
    List<EventOccurrence> occurrences = generator.generateOccurrencesFrom(aDate(), events);
    assertThat(occurrences.isEmpty(),  is(true));
  }
  
  @Test
  public void generateOccurrencesFromAGivenDateWithNonPeriodicEvents() throws Exception {
    List<EventDetail> events = new ArrayList<EventDetail>();
    events.add(anEventDetailOfId(NON_PERIODIC_EVENTS[0]).build()); // it has no occurrences from the given date
    events.add(anEventDetailOfId(NON_PERIODIC_EVENTS[1]).build());
    List<EventOccurrence> occurrences = generator.generateOccurrencesFrom(aDate(), events);
    assertThat(occurrences.size(), is(1));
    assertThat(occurrences.get(0), is(anOccurrenceOfEvent(NON_PERIODIC_EVENTS[1],
        startingAt("2011-04-15"),
        endingAt("2011-04-15"))));
  }
  
  @Test
  public void generateOccurrencesFromAGivenDateWithPeriodicEvents() throws Exception {
    List<EventDetail> events = new ArrayList<EventDetail>();
    events.add(anEventDetailOfId(PERIODIC_EVENTS[0]).build()); // it has 5 occurrences from the given date
    events.add(anEventDetailOfId(PERIODIC_EVENTS[1]).build()); // it has 5 occurrences from the given date
    List<EventOccurrence> occurrences = generator.generateOccurrencesFrom(aDate(), events);
    assertThat(occurrences.size(), is(10));
  }
  
  @Test
  public void generateOccurrencesFromAGivenDateWithPeriodicAndNonPeriodicEvents() throws Exception {
    List<EventDetail> events = new ArrayList<EventDetail>();
    events.add(anEventDetailOfId(PERIODIC_EVENTS[0]).build()); // it has 5 occurrences from the given date
    events.add(anEventDetailOfId(NON_PERIODIC_EVENTS[0]).build()); // it has no occurrences from the given date
    events.add(anEventDetailOfId(PERIODIC_EVENTS[1]).build()); // it has 5 occurrences from the given date
    events.add(anEventDetailOfId(NON_PERIODIC_EVENTS[1]).build());
    List<EventOccurrence> occurrences = generator.generateOccurrencesFrom(aDate(), events);
    assertThat(occurrences.size(), is(11));
  }
          
  @Test
  public void generateOccurrencesInRangeWithNoEvents() throws Exception {
    List<EventDetail> events = new ArrayList<EventDetail>();
    List<EventOccurrence> occurrences = generator.generateOccurrencesInRange(startDate(), endDate(), events);
    assertThat(occurrences.isEmpty(),  is(true));
  }
  
  @Test
  public void generateOccurrencesInRangeWithNonPeriodicEvents() throws Exception {
    List<EventDetail> events = new ArrayList<EventDetail>();
    events.add(anEventDetailOfId(NON_PERIODIC_EVENTS[0]).build()); // it has no occurrences from the given date
    events.add(anEventDetailOfId(NON_PERIODIC_EVENTS[1]).build());
    List<EventOccurrence> occurrences = generator.generateOccurrencesInRange(startDate(), endDate(), events);
    assertThat(occurrences.size(), is(1));
    assertThat(occurrences.get(0), is(anOccurrenceOfEvent(NON_PERIODIC_EVENTS[1],
        startingAt("2011-04-15"),
        endingAt("2011-04-15"))));
  }
  
  @Test
  public void generateOccurrencesInRangeWithPeriodicEvents() throws Exception {
    List<EventDetail> events = new ArrayList<EventDetail>();
    events.add(anEventDetailOfId(PERIODIC_EVENTS[0]).build()); // it has 5 occurrences from the given date
    events.add(anEventDetailOfId(PERIODIC_EVENTS[1]).build()); // it has 5 occurrences from the given date
    List<EventOccurrence> occurrences = generator.generateOccurrencesInRange(startDate(), endDate(), events);
    assertThat(occurrences.size(), is(10));
  }
  
  @Test
  public void generateOccurrencesInRangeWithPeriodicAndNonPeriodicEvents() throws Exception {
    List<EventDetail> events = new ArrayList<EventDetail>();
    events.add(anEventDetailOfId(PERIODIC_EVENTS[0]).build()); // it has 5 occurrences from the given date
    events.add(anEventDetailOfId(NON_PERIODIC_EVENTS[0]).build()); // it has no occurrences from the given date
    events.add(anEventDetailOfId(PERIODIC_EVENTS[1]).build()); // it has 5 occurrences from the given date
    events.add(anEventDetailOfId(NON_PERIODIC_EVENTS[1]).build());
    List<EventOccurrence> occurrences = generator.generateOccurrencesInRange(startDate(), endDate(), events);
    assertThat(occurrences.size(), is(11));
  }

  private Calendar aPeriod() {
    Calendar date = Calendar.getInstance();
    date.setTime(dateToUseInTests());
    return date;
  }
  
  private Date aDate() {
    return new Date(dateToUseInTests());
  }
  
  private Date startDate() {
    return aDate();
  }
  
  private Date endDate() {
    Calendar endDate = Calendar.getInstance();
    endDate.setTime(aDate());
    endDate.add(Calendar.MONTH, 3);
    return new Date(endDate.getTime());
  }
}
