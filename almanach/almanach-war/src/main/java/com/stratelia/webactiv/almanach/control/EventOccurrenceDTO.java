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

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.util.DateUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A DTO on an event occurrence in a calendar.
 * This DTO carries data about an event occurrence for the view rendering. So it provides also
 * specific rendering information.
 */
public class EventOccurrenceDTO {

  private EventDetail eventDetail;
  private DateDTO startDate;
  private DateDTO endDate;
  private boolean priority = false;
  private boolean allDay = false;

  /**
   * Constructs a new DTO about the specified event occurring at the specified start date and ending
   * at the specified endDate.
   * @param eventDetail the detail about the event such as the almanach it belongs to and so on.
   * @param startDate the date at which the event occurrence starts. The start date depends on the
   * window in time in which the event should be rendered and on its datetime definition in the
   * eventDetail (periodicity, start date of the first occurrence, and so one).
   * @param endDate the date at which the event occurrence ends. The end date depends on the
   * window in time in which the event should be rendered and on its datetime definition in the
   * eventDetail (periodicity, final end date of the event, and so one).
   */
  public EventOccurrenceDTO(final EventDetail eventDetail, final DateDTO startDate,
      final DateDTO endDate) {
    this.eventDetail = eventDetail;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public DateDTO getEndDate() {
    return endDate;
  }

  public void setEndDate(final DateDTO endDate) {
    this.endDate = endDate;
  }

  public DateDTO getStartDate() {
    return startDate;
  }

  public void setStartDate(final DateDTO startDate) {
    this.startDate = startDate;
  }

  /**
   * Gets the event to which this occurrence comes from.
   * @return the detail of the event occurrence.
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
   */
  public void setPriority(boolean priority) {
    this.priority = priority;
  }

  /**
   * Is the event occurring all the day defined by its start and end date.
   * @return true if the event is occurring all the day.
   */
  public boolean isAllDay() {
    return ! StringUtil.isDefined(startDate.getTimeInDay()) ||
        ! StringUtil.isDefined(endDate.getTimeInDay());
  }

  /**
   * Specifies wether the event should occur all day.
   * @param allDay the all day property to set.
   */
  public void setAllDay(boolean allDay) {
    this.allDay = allDay;
  }

  /**
   * Gets the start date and time of this event in the ISO 8601 format
   * For example: 2010-01-01T14:30:00.
   * @return the ISO 8601 format of the start date and time of this event.
   */
  public String getStartDateTimeInISO() {
    return startDate.getISO8601Date();
  }

  /**
   * Gets the end date and time of this event in the ISO 8601 format
   * For example: 2010-01-01T14:30:00.
   * @return the ISO 8601 format of the end date and time of this event.
   */
  public String getEndDateTimeInISO() {
    return endDate.getISO8601Date();
  }

  /**
   * Gets the CSS class(es) that is applied to this event rendering.
   * @return a list with the CSS classes that are applied to this.
   */
  protected List<String> getCSSClasses() {
    List<String> cssClasses = new ArrayList<String>(2);
    cssClasses.add(getEventDetail().getInstanceId());
    if (isPriority()) {
      cssClasses.add("priority");
    }
    return cssClasses;
  }

  /**
   * Gets a JSON (JavaScript Object Notation) representation of this event.
   * @return a JSON representation of this event.
   */
  public String toJSON() {
    return toJSONObject().toString();
  }

  /**
   * Gets a JSON (JavaScript Object Notation) representation of all the specified events.
   * @param events the events.
   * @return a JSON array with the JSON representation of all the specified events.
   */
  public static String toJSON(final List<EventOccurrenceDTO> events) {
    JSONArray jsonArray = new JSONArray();
    for (EventOccurrenceDTO event : events) {
      jsonArray.put(event.toJSONObject());
    }
    return jsonArray.toString();
  }

  /**
   * Converts this event DTO into a JSON object.
   * @return a JSON object of this event DTO.
   */
  protected JSONObject toJSONObject() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("id", getEventDetail().getId());
    jsonObject.put("instanceId", getEventDetail().getInstanceId());
    jsonObject.put("title", getEventDetail().getTitle());
    jsonObject.put("start", getStartDateTimeInISO());
    jsonObject.put("end", getEndDateTimeInISO());
    jsonObject.put("className", new JSONArray(getCSSClasses()));
    jsonObject.put("allDay", isAllDay());
    return jsonObject;
  }
}
