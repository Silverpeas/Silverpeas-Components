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
package com.silverpeas.scheduleevent.service;

import com.silverpeas.calendar.CalendarEvent;
import com.silverpeas.calendar.Datable;
import com.silverpeas.scheduleevent.service.model.ScheduleEventStatus;
import com.silverpeas.scheduleevent.service.model.beans.DateOption;
import com.silverpeas.scheduleevent.service.model.beans.Response;
import com.silverpeas.scheduleevent.service.model.beans.ScheduleEvent;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.util.SettingBundle;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import static com.silverpeas.calendar.CalendarEvent.anEventAt;
import static org.silverpeas.util.DateUtil.asDatable;

/**
 * An encoder of EventDetail instances to EventCalendar instances.
 */
public class CalendarEventEncoder {

  private static SettingBundle settings = ResourceLocator.getSettingBundle(
      "org.silverpeas.components.scheduleevent.settings.ScheduleEventSettings");

  private static SettingBundle getSettings() {
    return settings;
  }
  
  /**
   * Creates a Datable object from the specified date and time
   *
   * @param date the date (day in month in year).
   * @param time : the hour 8 if morning, 14 if after meridian.
   * @return a Datable object corresponding to the specified date and time.
   */
  private Datable<?> createDatable(final Date date, final int time) {
    Calendar dateAndTime = GregorianCalendar.getInstance();
    dateAndTime.setTime(date);
    dateAndTime.set(Calendar.HOUR_OF_DAY, time);
    dateAndTime.set(Calendar.MINUTE, 0);
    Datable<?> datable = asDatable(dateAndTime.getTime(), true);
    return datable;
  }

  /**
   * Encodes the specified detailed scheduleevent with the specified dates into calendar events.
   *
   * @param eventDetail detail.
   * @param listDateOption list of dates.
   * @return the list of calendar event corresponding to the schedule event.
   */
  public List<CalendarEvent> encode(final ScheduleEvent eventDetail, final List<DateOption> listDateOption) {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    Set<Response> listResponse = eventDetail.getResponses();
    if(eventDetail.getStatus() == ScheduleEventStatus.CLOSED && listResponse.size() > 0) {
      TimeZone timeZone = TimeZone.getTimeZone(getSettings().getString("scheduleevent.timezone"));
      for(DateOption eventDateOption : listDateOption) {
        Datable<?> startDate = createDatable(eventDateOption.getDay(), eventDateOption.getHour()).
            inTimeZone(timeZone);
        int endTime = DateOption.MORNING_END_HOUR;
        if(eventDateOption.getHour() == DateOption.MORNING_BEGIN_HOUR) {
          endTime = DateOption.MORNING_END_HOUR;
        } else if(eventDateOption.getHour() == DateOption.AFTERNOON_BEGIN_HOUR) {
          endTime = DateOption.AFTERNOON_END_HOUR;
        }
        Datable<?> endDate = createDatable(eventDateOption.getDay(), endTime).inTimeZone(timeZone);
      
        CalendarEvent calendarEvent = anEventAt(startDate).
                endingAt(endDate).
                withTitle(eventDetail.getTitle()).
                withDescription(eventDetail.getDescription());
        events.add(calendarEvent); 
      }
    }
    return events;
  }
}
