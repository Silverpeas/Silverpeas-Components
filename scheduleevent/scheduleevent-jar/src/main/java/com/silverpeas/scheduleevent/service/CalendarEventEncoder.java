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
package com.silverpeas.scheduleevent.service;

import static com.silverpeas.calendar.CalendarEvent.anEventAt;
import com.silverpeas.calendar.*;
import com.silverpeas.scheduleevent.service.model.ScheduleEventStatus;
import com.silverpeas.scheduleevent.service.model.beans.DateOption;
import com.silverpeas.scheduleevent.service.model.beans.Response;
import com.silverpeas.scheduleevent.service.model.beans.ScheduleEvent;

import static com.silverpeas.util.StringUtil.isDefined;
import org.silverpeas.wysiwyg.WysiwygException;
import static com.stratelia.webactiv.util.DateUtil.asDatable;
import com.stratelia.webactiv.util.ResourceLocator;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.*;

/**
 * An encoder of EventDetail instances to EventCalendar instances.
 */
public class CalendarEventEncoder {

  private static ResourceLocator settings = new ResourceLocator(
          "org.silverpeas.components.scheduleevent.settings.ScheduleEventSettings", "");

  private static ResourceLocator getSettings() {
    return settings;
  }

  /**
   * Encodes the specified scheduleevent detail into calendar events.
   *
   * @param scheduleevent detail.
   * @return the list of calendar event corresponding to the schedule event.
   * @throws MalformedURLException if the URL of an event is invalid.
   * @throws WysiwygException if an error occurs while fetching the WYSIWYG description of an event.
   */
  public List<CalendarEvent> encode(final ScheduleEvent eventDetail, final List<DateOption> listDateOption)
          throws MalformedURLException {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    Set<Response> listResponse = eventDetail.getResponses();
    if(eventDetail.getStatus() == ScheduleEventStatus.CLOSED && listResponse.size() > 0) {
      TimeZone timeZone = TimeZone.getTimeZone(getSettings().getString("scheduleevent.timezone"));
      for(DateOption eventDateOption : listDateOption) {
        Datable<?> startDate = createDatable(eventDateOption.getDay(), eventDateOption.getHour()).
            inTimeZone(timeZone);
        int endTime = 12;
        if(eventDateOption.getHour() == 8) {
          endTime = 12;
        } else if(eventDateOption.getHour() == 14) {
          endTime = 18;
        }
        Datable<?> endDate = createDatable(eventDateOption.getDay(), endTime).inTimeZone(timeZone);
      
        CalendarEvent calendarEvent = anEventAt(startDate).
                endingAt(endDate).
                withTitle(eventDetail.getTitle()).
                withDescription(eventDetail.getDescription());
        if (isDefined(eventDetail.getURL())) {
          calendarEvent.withUrl(new URL(eventDetail.getURL()));
        }
        events.add(calendarEvent); 
      }
    }
    return events;
  }

  /**
   * Creates a Datable object from the specified date and time
   *
   * @param date the date (day in month in year).
   * @param time : the hour 8 if morning, 14 if after meridian.
   * returned datable is a Date.
   * @return a Datable object corresponding to the specified date and time.
   */
  private Datable<?> createDatable(final Date date, final int time) {
    Calendar dateAndTime = Calendar.getInstance();
    dateAndTime.setTime(date);
    dateAndTime.set(Calendar.HOUR_OF_DAY, time);
    dateAndTime.set(Calendar.MINUTE, 0);
    Datable<?> datable = asDatable(dateAndTime.getTime(), true);
    return datable;
  }
}
