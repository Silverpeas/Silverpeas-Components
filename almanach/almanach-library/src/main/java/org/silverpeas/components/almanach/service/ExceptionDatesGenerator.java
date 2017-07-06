/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
package org.silverpeas.components.almanach.service;

import org.silverpeas.core.date.Temporal;
import static org.silverpeas.core.util.StringUtil.isDefined;

import org.silverpeas.components.almanach.model.EventDetail;
import org.silverpeas.components.almanach.model.Periodicity;
import org.silverpeas.components.almanach.model.PeriodicityException;
import org.silverpeas.core.persistence.jdbc.bean.IdPK;
import org.silverpeas.core.persistence.jdbc.bean.PersistenceException;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAO;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAOFactory;
import static org.silverpeas.core.util.DateUtil.extractHour;
import static org.silverpeas.core.util.DateUtil.extractMinutes;

import org.silverpeas.core.date.DateTime;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import java.util.*;
import java.util.Date;

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
    Set<Date> exceptionDates = new HashSet<>();
    if (event.isPeriodic()) {
      Collection<PeriodicityException> exceptions = getPeriodicityExceptions(event.getPeriodicity());
      java.util.Calendar exceptionsStartDate = java.util.Calendar.getInstance();
      java.util.Calendar exceptionsEndDate = java.util.Calendar.getInstance();
      for (PeriodicityException periodicityException : exceptions) {
        Temporal<?> temporal = toTemporal(periodicityException.getBeginDateException(), event.
                getStartHour());
        exceptionsStartDate.setTime(temporal.asDate());
        if (!isDefined(event.getEndHour()) && isDefined(event.getStartHour())) {
          temporal = toTemporal(periodicityException.getEndDateException(), event.getStartHour());
        } else {
          temporal = toTemporal(periodicityException.getEndDateException(), event.getEndHour());
        }
        exceptionsEndDate.setTime(temporal.asDate());
        while (exceptionsStartDate.before(exceptionsEndDate)
                || exceptionsStartDate.equals(exceptionsEndDate)) {
          exceptionDates.add(exceptionsStartDate.getTime());
          exceptionsStartDate.add(Calendar.DATE, 1);
        }
      }
    }
    return exceptionDates;
  }

  private Temporal<?> toTemporal(final java.util.Date date, String time) {
    Temporal<?> temporal;
    TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
    SettingBundle almanachSettings =
            ResourceLocator.getSettingBundle("org.silverpeas.almanach.settings.almanachSettings");
    TimeZone timeZone = registry.getTimeZone(almanachSettings.getString("almanach.timezone"));
    if (isDefined(time)) {
      java.util.Calendar calendarDate = java.util.Calendar.getInstance();
      calendarDate.setTime(date);
      calendarDate.set(java.util.Calendar.HOUR_OF_DAY, extractHour(time));
      calendarDate.set(java.util.Calendar.MINUTE, extractMinutes(time));
      calendarDate.set(java.util.Calendar.SECOND, 0);
      calendarDate.set(java.util.Calendar.MILLISECOND, 0);
      temporal = new DateTime(calendarDate.getTime()).inTimeZone(timeZone);
    } else {
      temporal = new org.silverpeas.core.date.Date(date).inTimeZone(timeZone);
    }
    return temporal;
  }

  private Collection<PeriodicityException> getPeriodicityExceptions(final Periodicity periodicity) {
    try {
      IdPK pk = new IdPK();
      SilverpeasBeanDAO<PeriodicityException> dao = SilverpeasBeanDAOFactory.getDAO(
              "org.silverpeas.components.almanach.model.PeriodicityException");
      return dao.findByWhereClause(pk, "periodicityId = " + periodicity.getPK().getId());
    } catch (PersistenceException e) {
      throw new AlmanachRuntimeException(
              "DefaultAlmanachService.getListPeriodicityException()",
              SilverpeasRuntimeException.ERROR,
              "almanach.EX_GET_PERIODICITY_EXCEPTION", e);
    }
  }
}
