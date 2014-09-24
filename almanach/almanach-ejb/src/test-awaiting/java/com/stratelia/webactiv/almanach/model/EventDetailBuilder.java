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
package com.stratelia.webactiv.almanach.model;

import static org.silverpeas.util.DateUtil.*;

/**
 * A builder of event details used in tests.
 * The events built by this builder are thoses expected in the database.
 * So the events built by this builder have to reflect any change performed in the test database.
 */
public class EventDetailBuilder {

  public static final String[] PERIODIC_EVENTS = { "1000", "1003" };
  public static final String[] NON_PERIODIC_EVENTS = { "1001", "1002" };

  /**
   * Gets a builder for the event detail identified by the specified unique identifier.
   * @param id the unique identifier of the event to build.
   * @return the event detail builder ready to build the asked event.
   * @throws Exception if an error occurs while preparing the builder.
   */
  public static EventDetailBuilder anEventDetailOfId(String id) throws Exception {
    EventDetailBuilder builder = new EventDetailBuilder();
    if ("1000".equals(id)) {
      EventDetail event = new EventDetail(new EventPK(id, null, "almanach272"), "Complete event",
          parseDate("2011/01/04"), parseDate("2011/01/04"));
      event.setDelegatorId("1298");
      event.setStartHour("09:30");
      event.setEndHour("12:00");
      event.setEventUrl("");
      event.setPlace("Eybens");
      event.setPriority(0);
      Periodicity periodicity = new Periodicity();
      periodicity.setPK(new EventPK("34", null, "almanach272"));
      periodicity.setDay(0);
      periodicity.setDaysWeekBinary("0100000");
      periodicity.setEventId(1000);
      periodicity.setFrequency(1);
      periodicity.setNumWeek(1);
      periodicity.setUnity(2);
      periodicity.setUntilDatePeriod(parseDate("2011/05/31"));
      event.setPeriodicity(periodicity);
      builder.setEventDetail(event);
    } else if ("1001".equals(id)) {
      EventDetail event = new EventDetail(new EventPK(id, null, "almanach701"),
          "Event without end date", parseDate("2011/04/13"), parseDate("2011/04/13"));
      event.setDelegatorId("861");
      event.setStartHour("09:30");
      event.setEventUrl("");
      event.setPlace("Eybens");
      event.setPriority(0);
      builder.setEventDetail(event);
    } else if ("1002".equals(id)) {
      EventDetail event = new EventDetail(new EventPK(id, null, "almanach509"),
          "All day event without end date", parseDate("2011/04/15"), parseDate("2011/04/15"));
      event.setDelegatorId("847");
      event.setEventUrl("");
      event.setPlace("Eybens");
      event.setPriority(0);
      builder.setEventDetail(event);
    } else if ("1003".equals(id)) {
      EventDetail event = new EventDetail(new EventPK(id, null, "almanach509"), "All day event",
          parseDate("2011/04/20"), parseDate("2011/04/20"));
      event.setDelegatorId("847");
      event.setStartHour("09:30");
      event.setEventUrl("");
      event.setPlace("Eybens");
      event.setPriority(1);
      Periodicity periodicity = new Periodicity();
      periodicity.setPK(new EventPK("35", null, "almanach509"));
      periodicity.setDay(0);
      periodicity.setDaysWeekBinary("0010000");
      periodicity.setEventId(1003);
      periodicity.setFrequency(1);
      periodicity.setNumWeek(1);
      periodicity.setUnity(2);
      periodicity.setUntilDatePeriod(parseDate("2011/05/31"));
      event.setPeriodicity(periodicity);
      builder.setEventDetail(event);
    } else if ("1004".equals(id)) {
      EventDetail event = new EventDetail(new EventPK(id, null, "almanach509"), "All day event 2",
          parseDate("2012/02/20"), parseDate("2012/02/20"));
      event.setDelegatorId("847");
      event.setStartHour("09:30");
      event.setEventUrl("");
      event.setPlace("Eybens");
      event.setPriority(1);
      builder.setEventDetail(event);
    }  else if ("1005".equals(id)) {
      EventDetail event = new EventDetail(new EventPK(id, null, "almanach509"), "Event without end date 2",
          parseDate("2013/06/20"), parseDate("2013/06/20"));
      event.setDelegatorId("847");
      event.setStartHour("09:30");
      event.setEventUrl("");
      event.setPlace("Eybens");
      event.setPriority(1);
      builder.setEventDetail(event);
    } else {
      throw new IllegalArgumentException("The event detail with id '" + id + "' is unknown!");
    }
    return builder;
  }

  /**
   * Builds an instance of the asked event detail.
   * @return an EventDetail instance ready to be used in tests.
   */
  public EventDetail build() {
    return this.eventDetail;
  }

  private EventDetail eventDetail;

  private void setEventDetail(final EventDetail event) {
    this.eventDetail = event;
  }
}
