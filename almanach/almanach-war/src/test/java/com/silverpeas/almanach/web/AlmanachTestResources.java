/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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
package com.silverpeas.almanach.web;

import java.util.List;
import com.stratelia.webactiv.almanach.model.Periodicity;
import com.stratelia.webactiv.almanach.model.EventPK;
import com.silverpeas.almanach.service.AlmanachServiceProvider;
import com.silverpeas.calendar.DateTime;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachBm;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.almanach.model.EventOccurrence;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Named;
import static com.stratelia.webactiv.almanach.model.EventOccurrence.*;
import static com.stratelia.webactiv.util.DateUtil.*;
import static org.mockito.Mockito.*;

/**
 * Resources required for the unit tests on the REST-based Almanach web service.
 */
@Named("almanachTestResources")
public class AlmanachTestResources {

  public static final String COMPONENT_INSTANCE_ID = "almanach21";
  public static final String ALMANACH_PATH = "almanach/" + COMPONENT_INSTANCE_ID + "/nextevents";
  public static final String INVALID_ALMANACH_PATH = "almanach/almanach100/nextevents";
  @Inject
  private AlmanachServiceProvider serviceProvider;
  private AlmanachBm mockedAlmanachBm = null;
  
  /**
   * Initializes the resources required by the unit tests.
   */
  public void init() {
    mockAlmanachBm();
  }

  /**
   * Saves the specified count of event occurrences in the future.
   * @param count the count of event occurrences to save.
   */
  public void saveSomeEventsInTheFuture(int count) {
    try {
      EventDetail event = anEventDetailWithOccurrences(count);
      Calendar startDate = startDateOfTheFirstOccurrenceOf(event);
      Calendar endDate = endDateOfTheFirstOccurrenceOf(event);
      List<EventOccurrence> occurrences = new ArrayList<EventOccurrence>(count);
      for (int i = 0; i < count; i++) {
        EventOccurrence occurrence = anOccurrenceOf(event, new DateTime(startDate.getTime()),
                new DateTime(endDate.getTime()));
        occurrences.add(occurrence);
        startDate.add(Calendar.WEEK_OF_YEAR, 1);
        endDate.add(Calendar.WEEK_OF_YEAR, 1);
      }
      when(mockedAlmanachBm.getNextEventOccurrences(anyString())).thenReturn(occurrences);
    } catch (Exception ex) {
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }

  private void mockAlmanachBm() {
    try {
      mockedAlmanachBm = mock(AlmanachBm.class);
      when(mockedAlmanachBm.getNextEventOccurrences(anyString())).thenReturn(new ArrayList<EventOccurrence>());
      serviceProvider.setAlmanachBean(mockedAlmanachBm);
    } catch (RemoteException ex) {
      Logger.getLogger(AlmanachTestResources.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private EventDetail anEventDetailWithOccurrences(int occurrenceCount) throws Exception {
    Calendar date = Calendar.getInstance();
    EventDetail event = new EventDetail(new EventPK("1000", null, COMPONENT_INSTANCE_ID), "An event",
            date.getTime(), date.getTime());
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
    date.add(Calendar.WEEK_OF_YEAR, occurrenceCount);
    periodicity.setUntilDatePeriod(date.getTime());
    event.setPeriodicity(periodicity);
    return event;
  }

  private Calendar startDateOfTheFirstOccurrenceOf(final EventDetail event) {
    Calendar date = Calendar.getInstance();
    date.setTime(event.getStartDate());
    date.set(java.util.Calendar.HOUR_OF_DAY, extractHour(event.getStartHour()));
    date.set(java.util.Calendar.MINUTE, extractMinutes(event.getStartHour()));
    return date;
  }

  private Calendar endDateOfTheFirstOccurrenceOf(final EventDetail event) {
    Calendar date = Calendar.getInstance();
    date.setTime(event.getEndDate());
    date.set(java.util.Calendar.HOUR_OF_DAY, extractHour(event.getEndHour()));
    date.set(java.util.Calendar.MINUTE, extractMinutes(event.getEndHour()));
    return date;
  }
}
