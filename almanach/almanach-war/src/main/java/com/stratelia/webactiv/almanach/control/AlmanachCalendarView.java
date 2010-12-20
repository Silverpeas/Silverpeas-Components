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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * It defines a window in time of the calendar belonging to a given almanach instance.
 * The type of view can be a weekly or a monthly one and it renders the events between the two dates
 * of the window in time.
 */
public class AlmanachCalendarView {

  /**
   * The type of view of an AlmanachCalendarView.
   */
  public static enum ViewType {
    MONTHLY("month"),
    WEEKLY("agendaWeek");

    /**
     * Converts this view type in a string representation.
     * The value of the string depends on the calendar view rendering engine. It should be a value
     * that matches the view mode supported by the underlying calendar renderer.
     * @return
     */
    @Override
    public String toString() {
      return fullCalendarView;
    }

    /**
     * Constructs a view type with the specified view mode.
     * @param modeView the view mode as defined in the underlying calendar renderer.
     */
    private ViewType(String viewMode) {
      this.fullCalendarView = viewMode;
    }

    private String fullCalendarView;
  }

  private AlmanachDTO almanach;
  private List<EventOccurrenceDTO> events = new ArrayList<EventOccurrenceDTO>();
  private ViewType type = ViewType.MONTHLY;


  /**
   * Constructs a new calendar view of the specified almanach.
   * @param almanach the DTO carrying information about the almanach instance this view is about.
   * @param viewType the type of view the calendar should be rendered.
   */
  public AlmanachCalendarView(final AlmanachDTO almanach, final ViewType viewType) {
    this.almanach = almanach;
    this.type = viewType;
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
  public List<EventOccurrenceDTO> getEvents() {
    return Collections.unmodifiableList(events);
  }

  /**
   * Gets the type of view of this calendar view.
   * @return the type of view.
   */
  public ViewType getViewType() {
    return type;
  }

  /**
   * Adds an event to this calendar view.
   * @param event the event to add.
   */
  public void addEvent(final EventOccurrenceDTO event) {
    this.events.add(event);
  }

}
