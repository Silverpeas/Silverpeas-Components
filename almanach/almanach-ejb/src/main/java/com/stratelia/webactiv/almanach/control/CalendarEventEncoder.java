/*
 * Copyright (C) 2000 - 2013 Silverpeas
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

import static com.silverpeas.calendar.CalendarEvent.anEventAt;
import static com.silverpeas.calendar.CalendarEventRecurrence.every;
import static org.silverpeas.util.StringUtil.isDefined;
import static org.silverpeas.util.DateUtil.asDatable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;

import com.silverpeas.calendar.CalendarEvent;
import com.silverpeas.calendar.CalendarEventRecurrence;
import com.silverpeas.calendar.Datable;
import com.silverpeas.calendar.DateTime;
import com.silverpeas.calendar.DayOfWeek;
import com.silverpeas.calendar.DayOfWeekOccurrence;
import com.silverpeas.calendar.TimeUnit;
import org.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.almanach.model.Periodicity;
import org.silverpeas.util.ResourceLocator;

/**
 * An encoder of EventDetail instances to EventCalendar instances.
 */
public class CalendarEventEncoder {

  private static ResourceLocator settings = new ResourceLocator(
          "org.silverpeas.almanach.settings.almanachSettings", "");

  /**
   * Encodes the specified details on almanach events into a calendar event.
   *
   * @param eventDetails details about some events in one or several almanachs.
   * @return the calendar events corresponding to the almanach events.
   */
  public List<CalendarEvent> encode(final List<EventDetail> eventDetails) {
    List<CalendarEvent> events = new ArrayList<>();
    TimeZone timeZone = TimeZone.getTimeZone(settings.getString("almanach.timezone"));
    for (EventDetail eventDetail : eventDetails) {
      Datable<?> startDate = createDatable(eventDetail.getStartDate(), eventDetail.getStartHour()).
              inTimeZone(timeZone);
      String endTime = eventDetail.getEndHour();
      if (startDate instanceof com.silverpeas.calendar.Date) {
        endTime = "";
      } else if (!isDefined(endTime)) {
        endTime = eventDetail.getStartHour();
      }
      Datable<?> endDate = createDatable(eventDetail.getEndDate(), endTime).inTimeZone(timeZone);

      CalendarEvent event = anEventAt(startDate).
              endingAt(endDate).
              withTitle(eventDetail.getName()).
              withDescription(eventDetail.getWysiwyg()).
              withPriority(eventDetail.getPriority());
      if (isDefined(eventDetail.getPlace())) {
        event.withLocation(eventDetail.getPlace());
      }
      String url = eventDetail.getEventUrl();
      if (isDefined(url)) {
        if (!StringUtil.startsWithIgnoreCase(url, "http")) {
          url = "http://" + url;
        }
        try {
          event.withUrl(new URL(url));
        } catch (MalformedURLException e) {
          SilverTrace.warn("almanach", "CalendarEventEncoder.encode", "root.ERROR",
              "Following URL '" + url + "' is malformed !");
        }
      }
      if (eventDetail.getPeriodicity() != null) {
        event.recur(withTheRecurrenceRuleOf(eventDetail));
      }

      events.add(event);
    }
    return events;
  }

  private CalendarEventRecurrence withTheRecurrenceRuleOf(final EventDetail event) {
    CalendarEventRecurrence recurrence = asCalendarEventRecurrence(event.getPeriodicity());
    TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
    TimeZone timeZone = registry.getTimeZone(settings.getString("almanach.timezone"));
    ExceptionDatesGenerator generator = new ExceptionDatesGenerator();
    Set<Date> exceptionDates = generator.generateExceptionDates(event);
    for (Date anExceptionDate : exceptionDates) {
      recurrence.excludeEventOccurrencesStartingAt(new DateTime(anExceptionDate, timeZone));
    }
    return recurrence;
  }

  /**
   * Converts the specified almanach event periodicity into a calendar event recurrence.
   *
   * @param periodicity the periodicity to convert.
   * @return the event recurrence corresponding to the specified periodicity.
   */
  private CalendarEventRecurrence asCalendarEventRecurrence(final Periodicity periodicity) {
    TimeUnit timeUnit;
    List<DayOfWeekOccurrence> daysOfWeek = new ArrayList<>();
    switch (periodicity.getUnity()) {
      case Periodicity.UNIT_WEEK:
        timeUnit = TimeUnit.WEEK;
        daysOfWeek.addAll(extractWeeklyDaysOfWeek(periodicity));
        break;
      case Periodicity.UNIT_MONTH:
        timeUnit = TimeUnit.MONTH;
        daysOfWeek.addAll(extractMonthlyDaysOfWeek(periodicity));
        break;
      case Periodicity.UNIT_YEAR:
        timeUnit = TimeUnit.YEAR;
        break;
      default:
        timeUnit = TimeUnit.DAY;
        break;
    }
    CalendarEventRecurrence recurrence = every(periodicity.getFrequency(), timeUnit).on(daysOfWeek);
    if (periodicity.getUntilDatePeriod() != null) {
      recurrence.upTo(asDatable(periodicity.getUntilDatePeriod(), true));
    }

    return recurrence;
  }

  /**
   * Extracts from the specified periodicity the occurrences of day of week on which an event
   * monthly recurs.
   *
   * @param periodicity the periodicity of an event.
   * @return a list of day of week occurrences.
   */
  private List<DayOfWeekOccurrence> extractMonthlyDaysOfWeek(final Periodicity periodicity) {
    List<DayOfWeekOccurrence> daysOfWeek = new ArrayList<>();
    int nth = periodicity.getNumWeek();
    if (nth != 0) {
      if (periodicity.getDay() == java.util.Calendar.MONDAY) {
        daysOfWeek.add(DayOfWeekOccurrence.nthOccurrence(nth, DayOfWeek.MONDAY));
      } else if (periodicity.getDay() == java.util.Calendar.TUESDAY) {// Tuesday
        daysOfWeek.add(DayOfWeekOccurrence.nthOccurrence(nth, DayOfWeek.TUESDAY));
      } else if (periodicity.getDay() == java.util.Calendar.WEDNESDAY) {
        daysOfWeek.add(DayOfWeekOccurrence.nthOccurrence(nth, DayOfWeek.WEDNESDAY));
      } else if (periodicity.getDay() == java.util.Calendar.THURSDAY) {
        daysOfWeek.add(DayOfWeekOccurrence.nthOccurrence(nth, DayOfWeek.THURSDAY));
      } else if (periodicity.getDay() == java.util.Calendar.FRIDAY) {
        daysOfWeek.add(DayOfWeekOccurrence.nthOccurrence(nth, DayOfWeek.FRIDAY));
      } else if (periodicity.getDay() == java.util.Calendar.SATURDAY) {
        daysOfWeek.add(DayOfWeekOccurrence.nthOccurrence(nth, DayOfWeek.SATURDAY));
      } else if (periodicity.getDay() == java.util.Calendar.SUNDAY) {
        daysOfWeek.add(DayOfWeekOccurrence.nthOccurrence(nth, DayOfWeek.SUNDAY));
      }
    }
    return daysOfWeek;
  }

  /**
   * Extracts from the specified periodicity the days of week an event weekly recurs.
   *
   * @param periodicity the periodicity of an event.
   * @return a list of days of week.
   */
  private List<DayOfWeekOccurrence> extractWeeklyDaysOfWeek(final Periodicity periodicity) {
    List<DayOfWeekOccurrence> daysOfWeek = new ArrayList<>();
    String encodedDaysOfWeek = periodicity.getDaysWeekBinary();
    if (encodedDaysOfWeek.charAt(0) == '1') {// Monday
      daysOfWeek.add(DayOfWeekOccurrence.allOccurrences(DayOfWeek.MONDAY));
    }
    if (encodedDaysOfWeek.charAt(1) == '1') {// Tuesday
      daysOfWeek.add(DayOfWeekOccurrence.allOccurrences(DayOfWeek.TUESDAY));
    }
    if (encodedDaysOfWeek.charAt(2) == '1') {
      daysOfWeek.add(DayOfWeekOccurrence.allOccurrences(DayOfWeek.WEDNESDAY));
    }
    if (encodedDaysOfWeek.charAt(3) == '1') {
      daysOfWeek.add(DayOfWeekOccurrence.allOccurrences(DayOfWeek.THURSDAY));
    }
    if (encodedDaysOfWeek.charAt(4) == '1') {
      daysOfWeek.add(DayOfWeekOccurrence.allOccurrences(DayOfWeek.FRIDAY));
    }
    if (encodedDaysOfWeek.charAt(5) == '1') {
      daysOfWeek.add(DayOfWeekOccurrence.allOccurrences(DayOfWeek.SATURDAY));
    }
    if (encodedDaysOfWeek.charAt(6) == '1') {
      daysOfWeek.add(DayOfWeekOccurrence.allOccurrences(DayOfWeek.SUNDAY));
    }
    return daysOfWeek;
  }

  /**
   * Creates a Datable object from the specified date and time
   *
   * @param date the date (day in month in year).
   * @param time the time if any. If the time is null or empty, then no time is defined and the
   * returned datable is a Date.
   * @return a Datable object corresponding to the specified date and time.
   */
  private Datable<?> createDatable(final Date date, final String time) {
    Datable<?> datable;
    if (isDefined(time)) {
      String[] timeComponents = time.split(":");
      Calendar dateAndTime = Calendar.getInstance();
      dateAndTime.setTime(date);
      dateAndTime.set(Calendar.HOUR_OF_DAY, Integer.valueOf(timeComponents[0]));
      dateAndTime.set(Calendar.MINUTE, Integer.valueOf(timeComponents[1]));
      datable = asDatable(dateAndTime.getTime(), true);
    } else {
      datable = asDatable(date, false);
    }
    return datable;
  }
}