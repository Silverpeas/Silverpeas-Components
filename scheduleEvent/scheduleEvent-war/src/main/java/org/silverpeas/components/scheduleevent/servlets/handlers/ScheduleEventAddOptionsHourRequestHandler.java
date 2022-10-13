/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.scheduleevent.servlets.handlers;

import org.silverpeas.components.scheduleevent.control.ScheduleEventSessionController;
import org.silverpeas.components.scheduleevent.service.model.beans.DateOption;
import org.silverpeas.components.scheduleevent.service.model.beans.ScheduleEvent;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.Set;

public class ScheduleEventAddOptionsHourRequestHandler implements ScheduleEventRequestHandler {

  @Override
  public String getDestination(String function, ScheduleEventSessionController scheduleeventSC,
      HttpServletRequest request) throws Exception {
    ScheduleEvent current = scheduleeventSC.getCurrentScheduleEvent();
    Set<DateOption> dates = current.getDates();
    Iterator<DateOption> iter = dates.iterator();
    while (iter.hasNext()) {
      DateOption aDate = iter.next();
      String tmpId = FORMATTER_TMP_ID.format(aDate.getDay());
      String hourFromParameters = request.getParameter("hourFor" + tmpId);
      int hour;
      try {
        hour = Integer.parseInt(hourFromParameters);
      } catch (Exception e) {
        SilverLogger.getLogger(this).error("Hour is not a int = " + hourFromParameters, e);
        hour = 8; // morning by default
      }
      aDate.setHour(hour);
    }
    scheduleeventSC.setCurrentScheduleEvent(current);
    request.setAttribute(CURRENT_SCHEDULE_EVENT, current);
    return "form/notify.jsp";
  }

}
