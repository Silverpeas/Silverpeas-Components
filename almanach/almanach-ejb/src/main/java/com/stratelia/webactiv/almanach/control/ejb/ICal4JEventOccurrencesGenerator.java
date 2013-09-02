/*
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
package com.stratelia.webactiv.almanach.control.ejb;

import com.silverpeas.calendar.Datable;
import com.silverpeas.calendar.Date;
import com.stratelia.webactiv.almanach.control.ExceptionDatesGenerator;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.almanach.model.EventOccurrence;
import com.stratelia.webactiv.util.ResourceLocator;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Categories;
import net.fortuna.ical4j.model.property.ExDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import static com.silverpeas.util.StringUtil.isDefined;
import static com.stratelia.webactiv.almanach.model.EventOccurrence.*;
import static com.stratelia.webactiv.util.DateUtil.extractHour;
import static com.stratelia.webactiv.util.DateUtil.extractMinutes;

/**
 * A generator of event occurrences built on the iCal4J library.
 */
public class ICal4JEventOccurrencesGenerator implements EventOccurrenceGenerator {

  @Override
  public List<EventOccurrence> generateOccurrencesInPeriod(org.silverpeas.date.Period period,
          List<EventDetail> events) {
    Period thePeriod =
        new Period(new DateTime(period.getBeginDate()), new DateTime(period.getEndDate()));
    return generateOccurrencesOf(events, occuringIn(thePeriod));
  }

  @Override
  public List<EventOccurrence> generateOccurrencesInRange(Date startDate,  Date endDate,
    List<EventDetail> events) {
    Period period = new Period(new DateTime(startDate), new DateTime(endDate));
    return generateOccurrencesOf(events, occuringIn(period));
  }
  
  @Override
  public List<EventOccurrence> generateOccurrencesFrom(Date date,  List<EventDetail> events) {
    java.util.Calendar endDate = java.util.Calendar.getInstance();
    // a hack as the iCal4J Period objects don't support null end date or infinite end date.
    endDate.add(java.util.Calendar.YEAR, 100);
    return generateOccurrencesInRange(date, new Date(endDate.getTime()), events);
  }

  /**
   * Generates the occurrences of the specified events that occur in the specified period.
   * @param events the events for which the occurrences has to be generated.
   * @param inPeriod the period.
   * @return a list of event occurrences that occur in the specified period.
   */
  private List<EventOccurrence> generateOccurrencesOf(final List<EventDetail> events,
          final Period inPeriod) {
    List<EventOccurrence> occurrences = new ArrayList<EventOccurrence>();
    Calendar iCal4JCalendar = anICalCalendarWith(events);
    ComponentList componentList = iCal4JCalendar.getComponents(Component.VEVENT);
    for (Object eventObject : componentList) {
      VEvent iCalEvent = (VEvent) eventObject;
      int index = Integer.parseInt(iCalEvent.getProperties().getProperty(Property.CATEGORIES).
              getValue());
      EventDetail event = events.get(index);
      PeriodList periodList = iCalEvent.calculateRecurrenceSet(inPeriod);
      for (Object recurrencePeriodObject : periodList) {
        Period recurrencePeriod = (Period) recurrencePeriodObject;
        Datable<?> startDate = toDatable(recurrencePeriod.getStart(), event.getStartHour());
        Datable<?> endDate = toDatable(recurrencePeriod.getEnd(), event.getEndHour());
        EventOccurrence occurrence = anOccurrenceOf(event, startingAt(startDate), endingAt(endDate)).
                withPriority(event.isPriority());
        occurrences.add(occurrence);
      }
    }
    Collections.sort(occurrences);
    return occurrences;
  }

  /**
   * Gets an iCal calendar with the specified events.
   * It uses ical4J to build the ical calendar.
   * @param events the events to register in the iCal4J calendar to return.
   * @return an iCal4J calendar instance with the events specified in parameter.
   */
  private Calendar anICalCalendarWith(final List<EventDetail> events) {
    Calendar calendarAlmanach = new Calendar();
    calendarAlmanach.getProperties().add(CalScale.GREGORIAN);
    for (int i = 0; i < events.size(); i++) {
      EventDetail event = events.get(i);
      ExDate exceptionDates = null;
      if (event.isPeriodic()) {
        exceptionDates = generateExceptionDates(event);
      }
      VEvent iCalEvent = event.icalConversion(exceptionDates);
      iCalEvent.getProperties().add(new Categories(String.valueOf(i)));
      calendarAlmanach.getComponents().add(iCalEvent);
    }
    return calendarAlmanach;
  }

  /**
   * Generates the dates at which it exist some exceptions in the periodicity of the specified event.
   * @param event the detail on the event for which it can exist some exceptions in his recurrence.
   * @return an ExDate instance with all of the exception dates.
   */
  private ExDate generateExceptionDates(final EventDetail event) {
    ExceptionDatesGenerator generator = new ExceptionDatesGenerator();
    Set<java.util.Date> exceptionDates = generator.generateExceptionDates(event);
    DateList exDateList = new DateList();
    for (java.util.Date anExceptionDate : exceptionDates) {
      exDateList.add(new DateTime(anExceptionDate));
    }
    return new ExDate(exDateList);
  }

  private Datable<?> toDatable(final java.util.Date date, String time) {
    Datable<?> datable;
    TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
    ResourceLocator almanachSettings =
            new ResourceLocator("com.stratelia.webactiv.almanach.settings.almanachSettings", "");
    TimeZone timeZone = registry.getTimeZone(almanachSettings.getString("almanach.timezone"));
    if (isDefined(time)) {
      java.util.Calendar calendarDate = java.util.Calendar.getInstance();
      calendarDate.setTime(date);
      calendarDate.set(java.util.Calendar.HOUR_OF_DAY, extractHour(time));
      calendarDate.set(java.util.Calendar.MINUTE, extractMinutes(time));
      calendarDate.set(java.util.Calendar.SECOND, 0);
      calendarDate.set(java.util.Calendar.MILLISECOND, 0);
      datable = new com.silverpeas.calendar.DateTime(calendarDate.getTime()).inTimeZone(timeZone);
    } else {
      datable = new Date(date).inTimeZone(timeZone);
    }
    return datable;
  }

  private static Period occuringIn(final Period period) {
    return period;
  }
}
