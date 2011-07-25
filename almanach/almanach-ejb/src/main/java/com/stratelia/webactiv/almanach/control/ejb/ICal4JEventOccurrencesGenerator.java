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

import java.util.ArrayList;
import java.util.TimeZone;
import com.stratelia.webactiv.util.ResourceLocator;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import com.silverpeas.calendar.Datable;
import com.silverpeas.calendar.Date;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.almanach.model.EventOccurrence;
import com.stratelia.webactiv.almanach.model.Periodicity;
import com.stratelia.webactiv.almanach.model.PeriodicityException;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.persistence.PersistenceException;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Categories;
import net.fortuna.ical4j.model.property.ExDate;
import static com.stratelia.webactiv.almanach.model.EventOccurrence.*;
import static com.silverpeas.util.StringUtil.*;
import static com.stratelia.webactiv.util.DateUtil.*;

/**
 * A generator of event occurrences built on the iCal4J library.
 */
public class ICal4JEventOccurrencesGenerator implements EventOccurrenceGenerator {

  @Override
  public List<EventOccurrence> generateOccurrencesInYear(java.util.Calendar year,
          List<EventDetail> events) {
    java.util.Calendar firstDayYear = java.util.Calendar.getInstance();
    firstDayYear.set(java.util.Calendar.YEAR, year.get(java.util.Calendar.YEAR));
    firstDayYear.set(java.util.Calendar.DAY_OF_MONTH, 1);
    firstDayYear.set(java.util.Calendar.MONTH, java.util.Calendar.JANUARY);
    firstDayYear.set(java.util.Calendar.HOUR_OF_DAY, 0);
    firstDayYear.set(java.util.Calendar.MINUTE, 0);
    firstDayYear.set(java.util.Calendar.SECOND, 0);
    firstDayYear.set(java.util.Calendar.MILLISECOND, 0);
    java.util.Calendar lastDayYear = java.util.Calendar.getInstance();
    lastDayYear.set(java.util.Calendar.YEAR, year.get(java.util.Calendar.YEAR));
    lastDayYear.set(java.util.Calendar.DAY_OF_MONTH, 1);
    lastDayYear.set(java.util.Calendar.MONTH, java.util.Calendar.JANUARY);
    lastDayYear.set(java.util.Calendar.HOUR_OF_DAY, 0);
    lastDayYear.set(java.util.Calendar.MINUTE, 0);
    lastDayYear.set(java.util.Calendar.SECOND, 0);
    lastDayYear.set(java.util.Calendar.MILLISECOND, 0);
    lastDayYear.add(java.util.Calendar.YEAR, 1);
    Period theYear = new Period(new DateTime(firstDayYear.getTime()),
            new DateTime(lastDayYear.getTime()));

    return generateOccurrencesOf(events, occuringIn(theYear));
  }

  @Override
  public List<EventOccurrence> generateOccurrencesInMonth(java.util.Calendar month,
          List<EventDetail> events) {
    java.util.Calendar firstDayMonth = java.util.Calendar.getInstance();
    firstDayMonth.set(java.util.Calendar.YEAR, month.get(java.util.Calendar.YEAR));
    firstDayMonth.set(java.util.Calendar.DAY_OF_MONTH, 1);
    firstDayMonth.set(java.util.Calendar.MONTH, month.get(java.util.Calendar.MONTH));
    firstDayMonth.set(java.util.Calendar.HOUR_OF_DAY, 0);
    firstDayMonth.set(java.util.Calendar.MINUTE, 0);
    firstDayMonth.set(java.util.Calendar.SECOND, 0);
    firstDayMonth.set(java.util.Calendar.MILLISECOND, 0);
    java.util.Calendar lastDayMonth = java.util.Calendar.getInstance();
    lastDayMonth.set(java.util.Calendar.YEAR, month.get(java.util.Calendar.YEAR));
    lastDayMonth.set(java.util.Calendar.DAY_OF_MONTH, 1);
    lastDayMonth.set(java.util.Calendar.MONTH, month.get(java.util.Calendar.MONTH));
    lastDayMonth.set(java.util.Calendar.HOUR_OF_DAY, 0);
    lastDayMonth.set(java.util.Calendar.MINUTE, 0);
    lastDayMonth.set(java.util.Calendar.SECOND, 0);
    lastDayMonth.set(java.util.Calendar.MILLISECOND, 0);
    lastDayMonth.add(java.util.Calendar.MONTH, 1);
    Period theMonth = new Period(new DateTime(firstDayMonth.getTime()),
            new DateTime(lastDayMonth.getTime()));

    return generateOccurrencesOf(events, occuringIn(theMonth));
  }

  @Override
  public List<EventOccurrence> generateOccurrencesInWeek(java.util.Calendar week,
          List<EventDetail> events) {
    java.util.Calendar firstDayWeek = java.util.Calendar.getInstance();
    firstDayWeek.setTime(week.getTime());
    firstDayWeek.set(java.util.Calendar.DAY_OF_WEEK, week.getFirstDayOfWeek());
    firstDayWeek.set(java.util.Calendar.HOUR_OF_DAY, 0);
    firstDayWeek.set(java.util.Calendar.MINUTE, 0);
    firstDayWeek.set(java.util.Calendar.SECOND, 0);
    firstDayWeek.set(java.util.Calendar.MILLISECOND, 0);
    java.util.Calendar lastDayWeek = java.util.Calendar.getInstance();
    lastDayWeek.setTime(week.getTime());
    lastDayWeek.set(java.util.Calendar.HOUR_OF_DAY, 0);
    lastDayWeek.set(java.util.Calendar.MINUTE, 0);
    lastDayWeek.set(java.util.Calendar.SECOND, 0);
    lastDayWeek.set(java.util.Calendar.MILLISECOND, 0);
    lastDayWeek.set(java.util.Calendar.DAY_OF_WEEK, week.getFirstDayOfWeek());
    lastDayWeek.add(java.util.Calendar.WEEK_OF_YEAR, 1);
    Period theWeek = new Period(new DateTime(firstDayWeek.getTime()),
            new DateTime(lastDayWeek.getTime()));

    return generateOccurrencesOf(events, occuringIn(theWeek));
  }

  @Override
  public List<EventOccurrence> generateOccurrencesFrom(Date date,  List<EventDetail> events) {
    // a hack as the iCal4J Period objects don't support null end date or infinite end date.
    java.util.Calendar rangeEndDate = java.util.Calendar.getInstance();
    rangeEndDate.setTime(date);
    rangeEndDate.add(java.util.Calendar.YEAR, 100);
    Period period = new Period(new DateTime(date), new DateTime(rangeEndDate.getTime()));
    return generateOccurrencesOf(events, occuringIn(period));
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
    Collection<PeriodicityException> periodicityExceptions =
            getPeriodicityExceptions(event.getPeriodicity());
    DateList exceptionDates = new DateList();
    java.util.Calendar exceptionsStartDate = java.util.Calendar.getInstance();
    java.util.Calendar exceptionsEndDate = java.util.Calendar.getInstance();
    for (PeriodicityException periodicityException : periodicityExceptions) {
      Datable<?> datable = toDatable(periodicityException.getBeginDateException(), event.
              getStartHour());
      exceptionsStartDate.setTime(datable.asDate());
      if (!isDefined(event.getEndHour()) && isDefined(event.getStartHour())) {
        datable = toDatable(periodicityException.getEndDateException(), event.getStartHour());
      } else {
        datable = toDatable(periodicityException.getEndDateException(), event.getEndHour());
      }
      exceptionsEndDate.setTime(datable.asDate());
      while (exceptionsStartDate.before(exceptionsEndDate)
              || exceptionsStartDate.equals(exceptionsEndDate)) {
        exceptionDates.add(new DateTime(exceptionsStartDate.getTime()));
        exceptionsStartDate.add(java.util.Calendar.DATE, 1);
      }
    }
    return new ExDate(exceptionDates);
  }

  /**
   * Gets all the exceptions of the specified periodicity.
   * @param periodicity an event periodicity
   * @return a collection of exceptions that were applied to the specified periodicity.
   */
  private Collection getPeriodicityExceptions(final Periodicity periodicity) {
    try {
      IdPK pk = new IdPK();
      return getPeriodicityExceptionDAO().findByWhereClause(pk, "periodicityId = " + periodicity.
              getPK().getId());
    } catch (PersistenceException e) {
      throw new AlmanachRuntimeException(
              "AlmanachBmEJB.getListPeriodicityException()",
              SilverpeasRuntimeException.ERROR,
              "almanach.EX_GET_PERIODICITY_EXCEPTION", e);
    }
  }

  private SilverpeasBeanDAO getPeriodicityExceptionDAO() {
    try {
      SilverpeasBeanDAO dao = SilverpeasBeanDAOFactory.getDAO(
              "com.stratelia.webactiv.almanach.model.PeriodicityException");
      return dao;
    } catch (PersistenceException pe) {
      throw new AlmanachRuntimeException(
              "AlmanachBmEJB.getPeriodicityExceptionDAO()",
              SilverpeasRuntimeException.ERROR,
              "almanach.EX_PERSISTENCE_PERIODICITY_EXCEPTION", pe);
    }
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
