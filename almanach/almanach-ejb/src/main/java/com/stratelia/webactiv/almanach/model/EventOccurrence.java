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

import com.silverpeas.calendar.Datable;
import com.silverpeas.calendar.Date;
import com.silverpeas.calendar.DateTime;
import java.util.Calendar;
import static com.silverpeas.util.StringUtil.*;
import static com.stratelia.webactiv.util.DateUtil.*;

/**
 * An occurrence of an event in the time. Periodic events are made up of several of occurrences in
 * the time up to their end date.
 * 
 * The occurrences can be compared among them by the start date.
 */
public class EventOccurrence implements Comparable<EventOccurrence> {

  private EventDetail eventDetail;
  private Datable<?> startDate;
  private Datable<?> endDate;
  private boolean priority = false;

  /**
   * Creates an occurrence of the specified event starting and ending at the specified date and
   * optionally times. If the specified datables are just Date instances, then the occurrence is
   * considered to occur all the day.
   * @param event the event.
   * @return an occurrence of the specified event.
   */
  public static EventOccurrence anOccurrenceOf(final EventDetail event, final Datable<?> startDate,
      final Datable<?> endDate) {
    return new EventOccurrence(event, startDate, endDate);
  }
  
  /**
   * Creates an occurrence of the specified event starting and ending at the specified dates. The
   * time at which the occurrences should occur is given by the specified event detail.
   * @param event the event.
   * @return an occurrence of the specified event.
   */
  public static EventOccurrence anOccurrenceOf(final EventDetail event, final Date startDate,
      final Date endDate) {
    Datable<?> startDateTime = startDate;
    Datable<?> endDateTime = endDate;
    String startTime = event.getStartHour();
    String endTime = event.getEndHour();
    if (isDefined(startTime)) {
      Calendar theDateTime = Calendar.getInstance();
      theDateTime.setTime(startDate);
      theDateTime.set(Calendar.HOUR_OF_DAY, extractHour(startTime));
      theDateTime.set(Calendar.MINUTE, extractMinutes(startTime));
      startDateTime = new DateTime(theDateTime.getTime());
    }
    if (isDefined(endTime)) {
      Calendar theDateTime = Calendar.getInstance();
      theDateTime.setTime(endDate);
      theDateTime.set(Calendar.HOUR_OF_DAY, extractHour(endTime));
      theDateTime.set(Calendar.MINUTE, extractMinutes(endTime));
      endDateTime = new DateTime(theDateTime.getTime());
    }
    return new EventOccurrence(event, startDateTime, endDateTime);
  }
  
  /**
   * Convenient method to improve the readability when calling the anOccurrenceOf method.
   * @param startingDate the start date of the occurrence.
   * @return the start date.
   */
  public static Datable<?> startingAt(final Datable<?> startingDate) {
    return startingDate;
  }
  
  /**
   * Convenient method to improve the readability when calling the anOccurrenceOf method.
   * @param endingDate the end date of the occurrence.
   * @return the end date.
   */
  public static Datable<?> endingAt(final Datable<?> endingDate) {
    return endingDate;
  }

  /**
   * Gets the end date of this occurrence.
   * @return the end date.
   */
  public Datable<?> getEndDate() {
    return endDate;
  }

  /**
   * Gets the start date of this occurrence.
   * @return the start date.
   */
  public Datable<?> getStartDate() {
    return startDate;
  }

  /**
   * Gets the details about the event this occurrence belongs to.
   * @return the detail of the event.
   */
  public EventDetail getEventDetail() {
    return this.eventDetail;
  }

  /**
   * Is this event has a priority?
   * @return true if this event has a priority over others, false otherwise.
   */
  public boolean isPriority() {
    return priority;
  }

  /**
   * Sets the priority of this event.
   * @param priority the event priority.
   * @return itself.
   */
  public EventOccurrence withPriority(boolean priority) {
    this.priority = priority;
    return this;
  }

  /**
   * Does this occurrence occur all the day defined?
   * An occurrence occurs all the day if its start or its end date is a single date instead of a
   * date time.
   * @return true if the event is occurring all the day.
   */
  public boolean isAllDay() {
    boolean allDay = startDate instanceof Date || endDate instanceof Date;
    if (!allDay) {
      Date startDay = new Date(startDate.asDate());
      Date endDay = new Date(endDate.asDate());
      allDay = startDay.isBefore(endDay);
    }
    return allDay;
  }

  /**
   * Gets the start date and time of this event in the ISO 8601 format (with minute precision).
   * For example: 2010-01-01T14:30.
   * @return the ISO 8601 format of the start date and time of this event.
   */
  public String getStartDateTimeInISO() {
    return startDate.toShortISO8601();
  }

  /**
   * Gets the end date and time of this event in the ISO 8601 format (with minute precision).
   * For example: 2010-01-01T14:30.
   * @return the ISO 8601 format of the end date and time of this event.
   */
  public String getEndDateTimeInISO() {
    return endDate.toShortISO8601();
  }
  
  /**
   * Constructs a new occurrence of the specified event.
   * @param event the detail about the event for which an occurrence is constructed.
   */
  protected EventOccurrence(final EventDetail event, final Datable<?> startDate,
      final Datable<?> endDate) {
    this.eventDetail = event;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  @Override
  public int compareTo(EventOccurrence otherOccurrence) {
    Datable<?> otherStartDate = otherOccurrence.getStartDate();
    int result;
    if (otherStartDate instanceof Date || getStartDate() instanceof Date) {
      Date date = new Date(getStartDate().asDate());
      Date otherDate = new Date(otherStartDate.asDate());
      result = date.compareTo(otherDate);
    } else {
      DateTime dateTime = new DateTime(getStartDate().asDate());
      DateTime otherDateTime = new DateTime(otherStartDate.asDate());
      result = dateTime.compareTo(otherDateTime);
    }
    return result;
  }
}
