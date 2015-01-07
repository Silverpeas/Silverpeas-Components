/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.almanach.control;

import org.silverpeas.calendar.CalendarDay;
import org.silverpeas.calendar.CalendarViewContext;
import org.silverpeas.calendar.CalendarViewType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * It defines a window in time of the calendar belonging to a given almanach instance. The type of
 * view can be a weekly or a monthly one and it renders the events between the two dates of the
 * window in time.
 */
public class AlmanachCalendarView {

  private CalendarViewContext viewContext;
  private AlmanachDTO almanach;
  private List<DisplayableEventOccurrence> events = new ArrayList<>();
  private String label = "";

  /**
   * Constructs a new calendar view of the specified almanach. By default, the week-end days are
   * displayed.
   *
   * @param almanach the DTO carrying information about the almanach instance this view is about.
   * @param currentDay the current day in this calendar view.
   * @param viewType the type of view the calendar should be rendered.
   * @param locale the locale to take into account (fr for the french locale (fr_FR) for example).
   */
  public AlmanachCalendarView(final AlmanachDTO almanach, final Date currentDay,
      final CalendarViewType viewType, final String locale) {
    this.almanach = almanach;
    viewContext = new CalendarViewContext(null, locale);
    viewContext.setReferenceDay(currentDay);
    viewContext.setViewType(viewType);
    if (!CalendarViewType.NEXT_EVENTS.equals(viewContext.getViewType())) {
      label = viewContext.getReferencePeriodLabel();
    }
  }

  /**
   * Gets the first day of weeks of the calendar with 1 meaning for sunday, 2 meaning for monday,
   * and so on. The first day of weeks depends on the locale; the first day of weeks is monday for
   * french whereas it is for sunday for US.
   *
   * @return the first day of week.
   */
  public int getFirstDayOfWeek() {
    return viewContext.getFirstDayOfWeek();
  }

  /**
   * Unset the rendering of the week-end days.
   */
  public void unsetWeekendVisible() {
    viewContext.setWithWeekend(false);
  }

  /**
   * Is the week-end visible?
   *
   * @return true if the week-end days should be rendered, false otherwise.
   */
  public boolean isWeekendVisible() {
    return viewContext.isWithWeekend();
  }

  /**
   * Gets the label of this calendar view to render.
   *
   * @return the calendar view label.
   */
  public String getLabel() {
    return label;
  }

  /**
   * Sets a label to this calendar view.
   *
   * @param label the label to set.
   */
  public void setLabel(final String label) {
    this.label = label;
  }

  /**
   * Gets the current day in this calendar view.
   *
   * @return the current day.
   */
  public CalendarDay getCurrentDay() {
    return viewContext.getReferenceDay();
  }

  /**
   * Gets a DTO on the almanach instance this calendar view belongs to.
   *
   * @return the almanach DTO.
   */
  public AlmanachDTO getAlmanach() {
    return almanach;
  }

  /**
   * Gets all events defined in the window in time this calendar view defines.
   *
   * @return a list with all DTO on the events planned in this calendar view.
   */
  public List<DisplayableEventOccurrence> getEvents() {
    return Collections.unmodifiableList(events);
  }

  /**
   * Gets the type of view of this calendar view.
   *
   * @return the type of view.
   */
  public CalendarViewType getViewType() {
    return viewContext.getViewType();
  }

  /**
   * Sets the events that are defined in this calendar view.
   *
   * @param events a list of event DTOs.
   */
  public void setEvents(final List<DisplayableEventOccurrence> events) {
    this.events.clear();
    this.events.addAll(events);
  }

  /**
   * Gets the JSON representation of the event occurrences.
   *
   * @return a JSON representation of the list of event occurrences.
   */
  public String getEventsInJSON() {
    return DisplayableEventOccurrence.toJSON(events);
  }
}
