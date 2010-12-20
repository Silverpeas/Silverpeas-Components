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

import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.util.DateUtil;
import java.util.Date;
import java.util.List;

/**
 * A DTO on an event occurrence in a calendar.
 * This DTO carries data about an event occurrence for the view rendering. So it provides also
 * specific rendering information.
 */
public class EventOccurrenceDTO {

  /**
   * One of the ISO 8601 date format. The one use to represent an event date.
   */
  protected static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm";
  private EventDetail eventDetail;
  private String cssClass;
  private Date startDate;
  private Date endDate;

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
  public EventOccurrenceDTO(final EventDetail eventDetail, final Date startDate, final Date endDate) {
    this.eventDetail = eventDetail;
    this.startDate = startDate;
    this.endDate = endDate;
    this.cssClass = eventDetail.getInstanceId();
  }

  public Date getEndDate() {
    return new Date(endDate.getTime());
  }

  public void setEndDate(final Date endDate) {
    this.endDate = new Date(endDate.getTime());
  }

  public Date getStartDate() {
    return new Date(startDate.getTime());
  }

  public void setStartDate(final Date startDate) {
    this.startDate = new Date(startDate.getTime());
  }

  /**
   * Gets the event to which this occurrence comes from.
   * @return the detail of the event occurrence.
   */
  public EventDetail getEventDetail() {
    return this.eventDetail;
  }

  /**
   * Gets the CSS class to which the event belongs.
   * The CSS class is set from the almanach instance identifier, so that all events within this
   * almanach will be rendered in a coherent way.
   * @return the CSS class of this event.
   */
  public String getCSSClass() {
    return cssClass;
  }

  /**
   * Gets the start date and time of this event in the ISO 8601 format
   * For example: 2010-01-01T14:30:00
   * @return the ISO 8601 format of the start date and time of this event.
   */
  public String getStartDateTimeInISO() {
    return DateUtil.formatDate(getStartDate(), ISO_DATE_FORMAT);
  }

  /**
   * Gets the end date and time of this event in the ISO 8601 format
   * For example: 2010-01-01T14:30:00
   * @return the ISO 8601 format of the end date and time of this event.
   */
  public String getEndDateTimeInISO() {
    return DateUtil.formatDate(getEndDate(), ISO_DATE_FORMAT);
  }

  /**
   * Gets a JSON (JavaScript Object Notation) representation of this event.
   * @return a JSON representation of this event.
   */
  public String toJSON() {
    return "{ id: \"" + getEventDetail().getId() + "\", instanceId: \"" + getEventDetail().
        getInstanceId() + "\", className: \"" + getEventDetail().getInstanceId() + "\" , title: \""
        + getEventDetail().getTitle() + "\", start: \""
        + getStartDateTimeInISO() + "\", end: \"" + getEndDateTimeInISO() + "\" }";
  }

  /**
   * Gets a JSON (JavaScript Object Notation) representation of all the specified events.
   * @param events the events.
   * @return a JSON array with the JSON representation of all the specified events.
   */
  public static String toJSON(final List<EventOccurrenceDTO> events) {
    StringBuilder descBuilder = new StringBuilder("[");
    for (EventOccurrenceDTO event : events) {
      descBuilder.append(event.toJSON()).append(", ");
    }
    String desc = descBuilder.toString();
    if (desc.endsWith(", ")) {
      desc = desc.substring(0, desc.length() - 2);
    }
    return desc + " ]";
  }
}
