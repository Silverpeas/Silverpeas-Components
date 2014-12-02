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
package com.stratelia.webactiv.almanach.control.ejb;

import com.silverpeas.calendar.Date;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.almanach.model.EventOccurrence;
import org.silverpeas.date.Period;

import java.util.List;

/**
 * Generator of occurrences of one or more events in a given window in time.
 */
public interface EventOccurrenceGenerator {

  /**
   * Generates the occurrences of events that occur in the specified period.
   * @param period the period in which occurrences of events occur.
   * @param events the events for which the occurrences have to be generated.
   * @return a list of occurrences occuring in the specified period
   */
  List<EventOccurrence> generateOccurrencesInPeriod(final Period period,
      final List<EventDetail> events);
  
  /**
   * Generates the occurrences of the specified events that occur between the two specified dates.
   * @param startDate the inclusive date from which the event occurrences occur.
   * @param endDate the date to which the event occurrences occur.
   * @param events the events for which the occurrences have to be generated.
   * @return a list of occurrences occuring from the specified date.
   */
  List<EventOccurrence> generateOccurrencesInRange(final Date startDate, final Date endDate,
          final List<EventDetail> events);
  
  /**
   * Generates the occurrences of the specified events that occur from the specified date with no
   * limit in the future.
   * @param date the inclusive date from which the event occurrences occur.
   * @param events the events for which the occurrences have to be generated.
   * @return a list of occurrences occuring from the specified date.
   */
  List<EventOccurrence> generateOccurrencesFrom(final Date date, final List<EventDetail> events);
}
