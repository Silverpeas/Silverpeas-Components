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
package com.stratelia.webactiv.almanach.control;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import static com.stratelia.webactiv.almanach.control.CalendarViewType.*;
import static com.silverpeas.util.StringUtil.*;

/**
 * It defines a window in time of the calendar belonging to a given almanach instance.
 * The type of view can be a weekly or a monthly one and it renders the events between the two dates
 * of the window in time.
 */
public class AlmanachCalendarView {

  private AlmanachDTO almanach;
  private AlmanachDay currentDay;
  private List<DisplayableEventOccurrence> events = new ArrayList<DisplayableEventOccurrence>();
  private CalendarViewType type = MONTHLY;
  private String label = "";
  private boolean withWeekend = true;
  private Locale locale = null;

  /**
   * Constructs a new calendar view of the specified almanach.
   * By default, the week-end days are displayed.
   * @param almanach the DTO carrying information about the almanach instance this view is about.
   * @param currentDay the current day in this calendar view.
   * @param viewType the type of view the calendar should be rendered.
   */
  public AlmanachCalendarView(final AlmanachDTO almanach, final AlmanachDay currentDay,
      final CalendarViewType viewType) {
    this.almanach = almanach;
    this.currentDay = currentDay;
    this.type = viewType;
  }

  /**
   * Gets the first day of weeks of the calendar with 1 meaning for sunday, 2 meaning for monday,
   * and so on.
   * The first day of weeks depends on the locale; the first day of weeks is monday for french
   * whereas it is for sunday for US.
   * @return the first day of week.
   */
  public int getFirstDayOfWeek() {
    Calendar calendar;
    if (locale == null) {
      calendar = Calendar.getInstance();
    } else {
      calendar = Calendar.getInstance(locale);
    }
    return calendar.getFirstDayOfWeek();
  }

  /**
   * Gets the first day of this calendar view.
   * @return the first day of the window in time.
   */
  public AlmanachDay getFirstDay() {
    AlmanachDay firstDay = null;
    Calendar calendar = Calendar.getInstance();
    switch (type) {
      case MONTHLY:
        calendar.setTime(currentDay.getDate());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        firstDay = new AlmanachDay(calendar.getTime());
        break;
      case WEEKLY:
        calendar.setTime(currentDay.getDate());
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        firstDay = new AlmanachDay(calendar.getTime());
        break;
      default:
        throw new UnsupportedOperationException("The type " + type.toString()
            + " is not yet supported");
    }
    return firstDay;
  }

  /**
   * Gets the last day of this calendar view.
   * @return the last day of the window in time.
   */
  public AlmanachDay getLastDay() {
    AlmanachDay lastDay = null;
    Calendar calendar = Calendar.getInstance();
    switch (type) {
      case MONTHLY:
        calendar.setTime(currentDay.getDate());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        lastDay = new AlmanachDay(calendar.getTime());
        break;
      case WEEKLY:
        calendar.setTime(currentDay.getDate());
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.add(Calendar.WEEK_OF_YEAR, 1);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        lastDay = new AlmanachDay(calendar.getTime());
        break;
      default:
        throw new UnsupportedOperationException("The type " + type.toString()
            + " is not yet supported");
    }
    return lastDay;
  }

  /**
   * Unset the rendering of the week-end days.
   */
  public void unsetWeekendVisible() {
    withWeekend = false;
  }

  /**
   * Is the week-end visible?
   * @return true if the week-end days should be rendered, false otherwise.
   */
  public boolean isWeekendVisible() {
    return withWeekend;
  }

  /**
   * Gets the label of this calendar view to render.
   * @return the calendar view label.
   */
  public String getLabel() {
    return label;
  }

  /**
   * Sets a label to this calendar view.
   * @param label the label to set.
   */
  public void setLabel(final String label) {
    this.label = label;
  }

  /**
   * Gets the current day in this calendar view.
   * @return the current day.
   */
  public AlmanachDay getCurrentDay() {
    return currentDay;
  }

  /**
   * Gets a DTO on the almanach instance this calendar view belongs to.
   * @return the almanach DTO.
   */
  public AlmanachDTO getAlmanach() {
    return almanach;
  }

  /**
   * Gets all events defined in the window in time this calendar view defines.
   * @return a list with all DTO on the events planned in this calendar view.
   */
  public List<DisplayableEventOccurrence> getEvents() {
    return Collections.unmodifiableList(events);
  }

  /**
   * Gets the type of view of this calendar view.
   * @return the type of view.
   */
  public CalendarViewType getViewType() {
    return type;
  }

  /**
   * Sets the events that are defined in this calendar view.
   * @param events a list of event DTOs.
   */
  public void setEvents(final List<DisplayableEventOccurrence> events) {
    this.events.clear();
    this.events.addAll(events);
  }

  /**
   * Gets the JSON representation of the event occurrences.
   * @return a JSON representation of the list of event occurrences.
   */
  public String getEventsInJSON() {
    return DisplayableEventOccurrence.toJSON(events);
  }

  /**
   * Sets the locale of this calendar view. According to the locale, some calendar properties will
   * be set (for example, the first day of the week).
   * @param locale the locale to take into account (fr for the french locale (fr_FR) for example).
   */
  public void setLocale(final String locale) {
    if (isDefined(locale)) {
      this.locale = new Locale(locale);
    }
  }
}
