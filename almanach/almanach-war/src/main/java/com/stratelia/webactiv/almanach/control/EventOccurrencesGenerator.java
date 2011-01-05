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

import com.stratelia.webactiv.almanach.control.ejb.AlmanachBm;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachBmHome;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachException;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachNoSuchFindEventException;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.almanach.model.EventPK;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;

/**
 * It is a generator of event occurrences in a given window in time.
 */
public class EventOccurrencesGenerator {

  private final Calendar calendar;
  private AlmanachBm almanachBm;
  private final String instanceId;

  /**
   * Creates a new event occurrrences generator for the specified iCal calendar.
   * @param calendar the iCal calendar containing the event definitions.
   * @param instanceId the identifier of the component instance to which the calendar belongs.
   */
  public EventOccurrencesGenerator(final Calendar calendar, final String instanceId) {
    this.calendar = calendar;
    this.instanceId = instanceId;
  }

  /**
   * Gets the event occurrences of the events defined in the underlying calendar in the specified
   * month.
   * @param month the month as a Calendar instance.
   * @return a list of event DTOs.
   * @throws AlmanachException if an error occurs while getting the list of events.
   * @throws AlmanachNoSuchFindEventException if a detail about an event in the underlying iCal
   * calendar cannot be found.
   * @throws RemoteException if the communication with the remote business object fails.
   */
  public List<EventOccurrenceDTO> getEventOccurrencesInMonth(final java.util.Calendar month)
      throws AlmanachException, AlmanachNoSuchFindEventException, RemoteException {
    java.util.Calendar firstDayMonth = java.util.Calendar.getInstance();
    firstDayMonth.set(java.util.Calendar.YEAR, month.get(java.util.Calendar.YEAR));
    firstDayMonth.set(java.util.Calendar.DAY_OF_MONTH, 1);
    firstDayMonth.set(java.util.Calendar.MONTH, month.get(java.util.Calendar.MONTH));
    firstDayMonth.set(java.util.Calendar.HOUR_OF_DAY, 0);
    firstDayMonth.set(java.util.Calendar.MINUTE, 0);
    firstDayMonth.set(java.util.Calendar.SECOND, 0);
    firstDayMonth.set(java.util.Calendar.MILLISECOND, 0);
    java.util.Calendar lastDayMonth = java.util.Calendar.getInstance();
    lastDayMonth.set(java.util.Calendar.YEAR, month.get(java.util.Calendar.YEAR));
    lastDayMonth.set(java.util.Calendar.DAY_OF_MONTH, 1);
    lastDayMonth.set(java.util.Calendar.MONTH, month.get(java.util.Calendar.MONTH));
    lastDayMonth.set(java.util.Calendar.HOUR_OF_DAY, 0);
    lastDayMonth.set(java.util.Calendar.MINUTE, 0);
    lastDayMonth.set(java.util.Calendar.SECOND, 0);
    lastDayMonth.set(java.util.Calendar.MILLISECOND, 0);
    lastDayMonth.add(java.util.Calendar.MONTH, 1);
    Period monthPeriod = new Period(new DateTime(firstDayMonth.getTime()),
        new DateTime(lastDayMonth.getTime()));

    return generateEventOccurrences(monthPeriod);
  }

  /**
   * Gets the event occurrences of the events defined in the underlying calendar in the specified
   * week.
   * @param week the week as a Calendar instance.
   * @return a list of event DTOs.
   * @throws AlmanachException if an error occurs while getting the list of events.
   * @throws AlmanachNoSuchFindEventException if a detail about an event in the underlying iCal
   * calendar cannot be found.
   * @throws RemoteException if the communication with the remote business object fails.
   */
  public List<EventOccurrenceDTO> getEventOccurrencesInWeek(final java.util.Calendar week)
      throws AlmanachException, AlmanachNoSuchFindEventException, RemoteException {
    java.util.Calendar firstDayWeek = java.util.Calendar.getInstance();
    firstDayWeek.setTime(week.getTime());
    firstDayWeek.set(java.util.Calendar.DAY_OF_WEEK, week.getFirstDayOfWeek());
    firstDayWeek.set(java.util.Calendar.HOUR_OF_DAY, 0);
    firstDayWeek.set(java.util.Calendar.MINUTE, 0);
    firstDayWeek.set(java.util.Calendar.SECOND, 0);
    firstDayWeek.set(java.util.Calendar.MILLISECOND, 0);
    java.util.Calendar lastDayWeek = java.util.Calendar.getInstance();
    lastDayWeek.setTime(week.getTime());
    lastDayWeek.set(java.util.Calendar.YEAR, week.get(java.util.Calendar.YEAR));
    lastDayWeek.set(java.util.Calendar.MONTH, week.get(java.util.Calendar.MONTH));
    lastDayWeek.set(java.util.Calendar.HOUR_OF_DAY, 0);
    lastDayWeek.set(java.util.Calendar.MINUTE, 0);
    lastDayWeek.set(java.util.Calendar.SECOND, 0);
    lastDayWeek.set(java.util.Calendar.MILLISECOND, 0);
    lastDayWeek.set(java.util.Calendar.DAY_OF_WEEK, week.getFirstDayOfWeek());
    lastDayWeek.add(java.util.Calendar.WEEK_OF_YEAR, 1);
    Period weekPeriod = new Period(new DateTime(firstDayWeek.getTime()),
        new DateTime(lastDayWeek.getTime()));

    return generateEventOccurrences(weekPeriod);
  }

  /**
   * Generates the occurrences of the events in the underlying calendar for the specified period.
   * @param period the period.
   * @return a list of event occurrences that occur in the specified period.
   * @throws AlmanachException if an error occurs while getting the list of events.
   * @throws AlmanachNoSuchFindEventException if a detail about an event in the underlying iCal
   * calendar cannot be found.
   * @throws RemoteException if the communication with the remote business object fails.
   */
  private List<EventOccurrenceDTO> generateEventOccurrences(final Period period) throws
      AlmanachException,
      AlmanachNoSuchFindEventException, RemoteException {
    List<EventOccurrenceDTO> events = new ArrayList<EventOccurrenceDTO>();
    ComponentList componentList = calendar.getComponents(Component.VEVENT);
    for (Object eventObject : componentList) {
      VEvent iCalEvent = (VEvent) eventObject;
      String idEvent = iCalEvent.getProperties().getProperty(Property.UID).getValue();
      EventDetail evtDetail = getEventDetail(idEvent);
      PeriodList periodList = iCalEvent.calculateRecurrenceSet(period);
      for (Object recurrencePeriodObject : periodList) {
        Period recurrencePeriod = (Period) recurrencePeriodObject;
        EventOccurrenceDTO event = new EventOccurrenceDTO(evtDetail,
            new DateDTO(DateUtil.formatAsISO8601Day(recurrencePeriod.getStart()), evtDetail.getStartHour()),
            new DateDTO(DateUtil.formatAsISO8601Day(recurrencePeriod.getEnd()), evtDetail.getEndHour()));
        event.setPriority(evtDetail.getPriority() > 0);
        events.add(event);
      }
    }
    return events;
  }

  /**
   * Gets the detail of the event identified by the specified identifier and defined in the calendar
   * identified by the specified identifier..
   * @param id the unique identifier of the event to get.
   * @return the detail of the event.
   * @throws AlmanachException if an error occurs while getting the detail of the event.
   * @throws AlmanachNoSuchFindEventException if no event exists with a such identifier.
   * @throws RemoteException if the communication with the remote business object fails.
   */
  protected EventDetail getEventDetail(final String id) throws AlmanachException,
      AlmanachNoSuchFindEventException, RemoteException {
    EventDetail detail = getAlmanachBm().getEventDetail(
        new EventPK(id, null, instanceId));
    if (detail != null) {
      return detail;
    } else {
      throw new AlmanachNoSuchFindEventException("almanach.ASC_NoSuchFindEvent");
    }
  }

  /**
   * Gets the remote business object for handling events details.
   * @return the remote business object.
   * @throws AlmanachException if an error occurs while getting the remote object.
   */
  protected AlmanachBm getAlmanachBm() throws AlmanachException {
    if (almanachBm == null) {
      try {
        almanachBm = ((AlmanachBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.ALMANACHBM_EJBHOME, AlmanachBmHome.class)).create();
      } catch (Exception e) {
        throw new AlmanachException("AlmanachSessionControl.getAlmanachBm()",
            SilverpeasException.ERROR, "almanach.EX_EJB_CREATION_FAIL", e);
      }
    }
    return almanachBm;
  }

  /**
   * Sets explictly the remote business object for getting event details.
   * @param almanachBm the remote business object to set.
   */
  protected void setAlmanachBm(final AlmanachBm almanachBm) {
    this.almanachBm = almanachBm;
  }
}
