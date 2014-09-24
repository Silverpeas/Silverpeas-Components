/*
 * Copyright (C) 2000 - 2013 Silverpeas
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

import com.silverpeas.calendar.Datable;
import static org.silverpeas.util.StringUtil.isDefined;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachRuntimeException;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.almanach.model.Periodicity;
import com.stratelia.webactiv.almanach.model.PeriodicityException;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.persistence.PersistenceException;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;
import static com.stratelia.webactiv.util.DateUtil.extractHour;
import static com.stratelia.webactiv.util.DateUtil.extractMinutes;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import java.util.*;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;

/**
 * A generator of exception dates from a given event periodicity.
 */
public class ExceptionDatesGenerator {

  /**
   * Generates the exception dates in the recurrence rule of the specified event.
   * @param event details about the event.
   * @return a set of exception dates.
   */
  public Set<Date> generateExceptionDates(final EventDetail event) {
    Set<Date> exceptionDates = new HashSet<Date>();
    if (event.isPeriodic()) {
      Collection<PeriodicityException> exceptions = getPeriodicityExceptions(event.getPeriodicity());
      java.util.Calendar exceptionsStartDate = java.util.Calendar.getInstance();
      java.util.Calendar exceptionsEndDate = java.util.Calendar.getInstance();
      for (PeriodicityException periodicityException : exceptions) {
        Datable<?> datable = toDatable(periodicityException.getBeginDateException(), event.
                getStartHour());
        exceptionsStartDate.setTime(datable.asDate());
        if (!isDefined(event.getEndHour()) && isDefined(event.getStartHour())) {
          datable = toDatable(periodicityException.getEndDateException(), event.getStartHour());
        } else {
          datable = toDatable(periodicityException.getEndDateException(), event.getEndHour());
        }
        exceptionsEndDate.setTime(datable.asDate());
        while (exceptionsStartDate.before(exceptionsEndDate)
                || exceptionsStartDate.equals(exceptionsEndDate)) {
          exceptionDates.add(exceptionsStartDate.getTime());
          exceptionsStartDate.add(Calendar.DATE, 1);
        }
      }
    }
    return exceptionDates;
  }

  private Datable<?> toDatable(final java.util.Date date, String time) {
    Datable<?> datable;
    TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
    ResourceLocator almanachSettings =
            new ResourceLocator("com.stratelia.webactiv.almanach.settings.almanachSettings", "");
    TimeZone timeZone = registry.getTimeZone(almanachSettings.getString("almanach.timezone"));
    if (isDefined(time)) {
      java.util.Calendar calendarDate = java.util.Calendar.getInstance();
      calendarDate.setTime(date);
      calendarDate.set(java.util.Calendar.HOUR_OF_DAY, extractHour(time));
      calendarDate.set(java.util.Calendar.MINUTE, extractMinutes(time));
      calendarDate.set(java.util.Calendar.SECOND, 0);
      calendarDate.set(java.util.Calendar.MILLISECOND, 0);
      datable = new com.silverpeas.calendar.DateTime(calendarDate.getTime()).inTimeZone(timeZone);
    } else {
      datable = new com.silverpeas.calendar.Date(date).inTimeZone(timeZone);
    }
    return datable;
  }

  private Collection<PeriodicityException> getPeriodicityExceptions(final Periodicity periodicity) {
    try {
      IdPK pk = new IdPK();
      SilverpeasBeanDAO<PeriodicityException> dao = SilverpeasBeanDAOFactory.getDAO(
              "com.stratelia.webactiv.almanach.model.PeriodicityException");
      return dao.findByWhereClause(pk, "periodicityId = " + periodicity.getPK().getId());
    } catch (PersistenceException e) {
      throw new AlmanachRuntimeException(
              "AlmanachBmEJB.getListPeriodicityException()",
              SilverpeasRuntimeException.ERROR,
              "almanach.EX_GET_PERIODICITY_EXCEPTION", e);
    }
  }
}
