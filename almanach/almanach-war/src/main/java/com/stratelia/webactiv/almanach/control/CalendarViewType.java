/*
 * Copyright (C) 2000 - 2009 Silverpeas
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

/**
 * It defines the type of view mode of a calendar.
 */
public enum CalendarViewType {

  /**
   * The calendar view is monthly.
   */
  MONTHLY("month"),
  /**
   * The calendar view is weekly.
   */
  WEEKLY("agendaWeek");

  /**
   * Converts this view type in a string representation.
   * The value of the string depends on the calendar view rendering engine. It should be a value
   * that matches the view mode supported by the underlying calendar renderer.
   * @return
   */
  @Override
  public String toString() {
    return calendarView;
  }

  /**
   * Is this view type is a monthly one.
   * @return true if this view type is for a monthly one, false otherwise.
   */
  public boolean isMonthlyView() {
    return this == MONTHLY;
  }

  /**
   * Is this view type is a weekly one.
   * @return true if this view type is for a weekly one, false otherwise.
   */
  public boolean isWeeklyView() {
    return this == WEEKLY;
  }

  /**
   * Constructs a view type with the specified view mode.
   * @param modeView the view mode as defined in the underlying calendar renderer.
   */
  private CalendarViewType(final String viewMode) {
    this.calendarView = viewMode;
  }
  private String calendarView;
}
