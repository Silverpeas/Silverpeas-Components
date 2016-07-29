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
package org.silverpeas.components.almanach.service;

import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import org.silverpeas.components.almanach.model.EventDetail;
import org.silverpeas.components.almanach.model.Periodicity;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.Recurrence;
import org.silverpeas.core.calendar.DayOfWeekOccurrence;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import static org.silverpeas.core.calendar.Recurrence.every;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * An encoder of EventDetail instances to EventCalendar instances.
 */
public class CalendarEventEncoder {

  private static SettingBundle settings = ResourceLocator.getSettingBundle(
          "org.silverpeas.almanach.settings.almanachSettings");

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
      CalendarEvent event;
      LocalDate startDate =
          LocalDateTime.ofInstant(eventDetail.getStartDate().toInstant(), timeZone.toZoneId())
              .toLocalDate();
      LocalDate endDate =
          LocalDateTime.ofInstant(eventDetail.getStartDate().toInstant(), timeZone.toZoneId())
              .toLocalDate();
      if (isDefined(eventDetail.getStartHour()) && isDefined(eventDetail.getEndHour())) {
        OffsetDateTime startDateTime = startDate.atTime(LocalTime.parse(eventDetail.getStartHour()))
            .atZone(timeZone.toZoneId())
            .toOffsetDateTime();
        OffsetDateTime endDateTime = endDate.atTime(LocalTime.parse(eventDetail.getEndHour()))
            .atZone(timeZone.toZoneId())
            .toOffsetDateTime();
        event = CalendarEvent.on(Period.between(startDateTime, endDateTime));
      } else {
        event = CalendarEvent.on(Period.between(startDate, endDate));
      }
      event.identifiedBy(eventDetail.getComponentInstanceId(), eventDetail.getId())
          .withTitle(eventDetail.getTitle())
          .withDescription(eventDetail.getWysiwyg())
          .withPriority(eventDetail.getPriority());
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
          SilverLogger.getLogger(this).error("Following URL '" + url + "' is malformed !");
        }
      }
      if (eventDetail.getPeriodicity() != null) {
        event.recur(withTheRecurrenceRuleOf(eventDetail));
      }

      events.add(event);
    }
    return events;
  }

  private Recurrence withTheRecurrenceRuleOf(final EventDetail event) {
    Recurrence recurrence = asCalendarEventRecurrence(event.getPeriodicity());
    TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
    TimeZone timeZone = registry.getTimeZone(settings.getString("almanach.timezone"));
    ExceptionDatesGenerator generator = new ExceptionDatesGenerator();
    Set<Date> exceptionDates = generator.generateExceptionDates(event);
    for (Date anExceptionDate : exceptionDates) {
      OffsetDateTime excludedDateTime =
          anExceptionDate.toInstant().atZone(timeZone.toZoneId()).toOffsetDateTime();
      recurrence.excludeEventOccurrencesStartingAt(excludedDateTime);
    }
    return recurrence;
  }

  /**
   * Converts the specified almanach event periodicity into a calendar event recurrence.
   *
   * @param periodicity the periodicity to convert.
   * @return the event recurrence corresponding to the specified periodicity.
   */
  private Recurrence asCalendarEventRecurrence(final Periodicity periodicity) {
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
    Recurrence recurrence = every(periodicity.getFrequency(), timeUnit).on(daysOfWeek);
    if (periodicity.getUntilDatePeriod() != null) {
      OffsetDateTime endDateTime = periodicity.getUntilDatePeriod()
          .toInstant()
          .atZone(TimeZone.getDefault().toZoneId())
          .toOffsetDateTime();
      recurrence.upTo(endDateTime);
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
        daysOfWeek.add(DayOfWeekOccurrence.nth(nth, DayOfWeek.MONDAY));
      } else if (periodicity.getDay() == java.util.Calendar.TUESDAY) {// Tuesday
        daysOfWeek.add(DayOfWeekOccurrence.nth(nth, DayOfWeek.TUESDAY));
      } else if (periodicity.getDay() == java.util.Calendar.WEDNESDAY) {
        daysOfWeek.add(DayOfWeekOccurrence.nth(nth, DayOfWeek.WEDNESDAY));
      } else if (periodicity.getDay() == java.util.Calendar.THURSDAY) {
        daysOfWeek.add(DayOfWeekOccurrence.nth(nth, DayOfWeek.THURSDAY));
      } else if (periodicity.getDay() == java.util.Calendar.FRIDAY) {
        daysOfWeek.add(DayOfWeekOccurrence.nth(nth, DayOfWeek.FRIDAY));
      } else if (periodicity.getDay() == java.util.Calendar.SATURDAY) {
        daysOfWeek.add(DayOfWeekOccurrence.nth(nth, DayOfWeek.SATURDAY));
      } else if (periodicity.getDay() == java.util.Calendar.SUNDAY) {
        daysOfWeek.add(DayOfWeekOccurrence.nth(nth, DayOfWeek.SUNDAY));
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
      daysOfWeek.add(DayOfWeekOccurrence.all(DayOfWeek.MONDAY));
    }
    if (encodedDaysOfWeek.charAt(1) == '1') {// Tuesday
      daysOfWeek.add(DayOfWeekOccurrence.all(DayOfWeek.TUESDAY));
    }
    if (encodedDaysOfWeek.charAt(2) == '1') {
      daysOfWeek.add(DayOfWeekOccurrence.all(DayOfWeek.WEDNESDAY));
    }
    if (encodedDaysOfWeek.charAt(3) == '1') {
      daysOfWeek.add(DayOfWeekOccurrence.all(DayOfWeek.THURSDAY));
    }
    if (encodedDaysOfWeek.charAt(4) == '1') {
      daysOfWeek.add(DayOfWeekOccurrence.all(DayOfWeek.FRIDAY));
    }
    if (encodedDaysOfWeek.charAt(5) == '1') {
      daysOfWeek.add(DayOfWeekOccurrence.all(DayOfWeek.SATURDAY));
    }
    if (encodedDaysOfWeek.charAt(6) == '1') {
      daysOfWeek.add(DayOfWeekOccurrence.all(DayOfWeek.SUNDAY));
    }
    return daysOfWeek;
  }

}