/*
 * Copyright (C) 2000 - 2012 Silverpeas
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

import java.util.Collection;

import com.silverpeas.calendar.DateTime;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.almanach.model.EventOccurrence;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.silverpeas.attachment.model.SimpleDocument;
import static com.silverpeas.util.StringUtil.*;

/**
 * An occurrence of an event in the time and that can be rendered in a calendar view.
 *
 * A DisplayableEventOccurrence instance decorates an event occurrence by adding to it rendering
 * information so that the occurrence can be displayed into a calendar view.
 */
public class DisplayableEventOccurrence extends EventOccurrence {

  /**
   * Decorates the specified event occurrence with rendering features.
   * @param occurrence the event occurrence to decorate.
   * @return a DisplayableEventOccurrence instance.
   */
  public static DisplayableEventOccurrence decorate(final EventOccurrence occurrence) {
    return new DisplayableEventOccurrence(occurrence);
  }

  /**
   * Decorates the specified event occurrences with rendering features.
   * @param occurrences a list of event occurrences to decorate.
   * @return a list of DisplayableEventOccurrence instances, each of them decorating one of the
   * specified occurrences.
   */
  public static List<DisplayableEventOccurrence> decorate(final List<EventOccurrence> occurrences) {
    List<DisplayableEventOccurrence> decorators =
            new ArrayList<DisplayableEventOccurrence>(occurrences.size());
    for (EventOccurrence occurrence : occurrences) {
      decorators.add(decorate(occurrence));
    }
    return decorators;
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

  public boolean isStartTimeDefined() {
    return getStartDate() instanceof DateTime;
  }

  public boolean isEndTimeDefined() {
    return getEndDate() instanceof DateTime;
  }

  /**
   * Gets a JSON (JavaScript Object Notation) representation of all the specified events.
   * @param events the events.
   * @return a JSON array with the JSON representation of all the specified events.
   */
  public static String toJSON(final List<DisplayableEventOccurrence> events) {
    JSONArray jsonArray = new JSONArray();
    for (DisplayableEventOccurrence event : events) {
      jsonArray.put(event.toJSONObject());
    }
    return jsonArray.toString();
  }

  /**
   * Converts this event DTO into a JSON object.
   * @return a JSON object of this event DTO.
   */
  protected JSONObject toJSONObject() {
    EventDetail event = getEventDetail();
    String startDate = getStartDateTimeInISO();
    String endDate = getEndDateTimeInISO();
    String description  = event.getWysiwyg();
    Collection<SimpleDocument> attachments = event.getAttachments();
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("id", event.getId());
    jsonObject.put("instanceId", event.getInstanceId());
    jsonObject.put("title", event.getTitle());
    jsonObject.put("description", description);
    jsonObject.put("location", event.getPlace());
    jsonObject.put("hasAttachments", attachments != null && !attachments.isEmpty());
    jsonObject.put("start", startDate);
    jsonObject.put("end", endDate);
    jsonObject.put("className", new JSONArray(getCSSClasses()));
    jsonObject.put("allDay", isAllDay());
    jsonObject.put("startTimeDefined", isStartTimeDefined());
    jsonObject.put("endTimeDefined", isEndTimeDefined());
    jsonObject.put("priority", event.isPriority());
    if (isDefined(event.getEventUrl())) {
      jsonObject.put("eventURL", event.getEventUrl());
    }
    return jsonObject;
  }

  private DisplayableEventOccurrence(final EventOccurrence occurrence) {
    super(occurrence.getEventDetail(), occurrence.getStartDate(), occurrence.getEndDate());
    withPriority(occurrence.isPriority());
  }
}
