/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
package org.silverpeas.components.scheduleevent.service;

import org.silverpeas.components.scheduleevent.service.model.ScheduleEventStatus;
import org.silverpeas.components.scheduleevent.service.model.beans.DateOption;
import org.silverpeas.components.scheduleevent.service.model.beans.Response;
import org.silverpeas.components.scheduleevent.service.model.beans.ScheduleEvent;
import org.silverpeas.core.calendar.event.CalendarEvent;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

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
   * Encodes the specified detailed scheduleevent with the specified dates into calendar events.
   *
   * @param eventDetail detail.
   * @param listDateOption list of dates.
   * @return the list of calendar event corresponding to the schedule event.
   */
  public List<CalendarEvent> encode(final ScheduleEvent eventDetail, final List<DateOption> listDateOption) {
    List<CalendarEvent> events = new ArrayList<>();
    Set<Response> listResponse = eventDetail.getResponses();
    if(eventDetail.getStatus() == ScheduleEventStatus.CLOSED && listResponse.size() > 0) {
      TimeZone timeZone = TimeZone.getTimeZone(getSettings().getString("scheduleevent.timezone"));
      for(DateOption eventDateOption : listDateOption) {
        int endTime = DateOption.MORNING_END_HOUR;
        if(eventDateOption.getHour() == DateOption.MORNING_BEGIN_HOUR) {
          endTime = DateOption.MORNING_END_HOUR;
        } else if(eventDateOption.getHour() == DateOption.AFTERNOON_BEGIN_HOUR) {
          endTime = DateOption.AFTERNOON_END_HOUR;
        }
        OffsetDateTime startDateTime =
            LocalDateTime.ofInstant(eventDateOption.getDay().toInstant(), timeZone.toZoneId())
                .withHour(eventDateOption.getHour())
                .withMinute(0)
            .atZone(timeZone.toZoneId())
            .toOffsetDateTime();
        OffsetDateTime endDateTime =
            LocalDateTime.ofInstant(eventDateOption.getDay().toInstant(), timeZone.toZoneId())
                .withHour(endTime)
                .withMinute(0)
            .atZone(timeZone.toZoneId())
            .toOffsetDateTime();
        CalendarEvent calendarEvent = CalendarEvent.on(Period.between(startDateTime, endDateTime))
            .identifiedBy(eventDetail.getComponentInstanceId(), eventDetail.getId())
            .withTitle(eventDetail.getTitle())
            .withDescription(eventDetail.getDescription());
        events.add(calendarEvent);
      }
    }
    return events;
  }
}
