/**
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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.scheduleevent.servlets.handlers;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.scheduleevent.control.ScheduleEventSessionController;
import com.silverpeas.scheduleevent.service.model.ScheduleEventBean;

public class ScheduleEventAddRequestHandler implements ScheduleEventBackableRequestHandler {
  private String jspDestination;

  public ScheduleEventAddRequestHandler(String jspDestination) {
    this.jspDestination = jspDestination;
  }
  
  @Override
  public String getDestination(String function, ScheduleEventSessionController scheduleeventSC,
      HttpServletRequest request) {
    ScheduleEventBean current = createCurrentScheduleEvent(scheduleeventSC);
    return getGeneralInformationDestination(request, current);
  }

  private ScheduleEventBean createCurrentScheduleEvent(ScheduleEventSessionController scheduleeventSC) {
    scheduleeventSC.createCurrentScheduleEvent();
    scheduleeventSC.getCurrentScheduleEvent();
    return scheduleeventSC.getCurrentScheduleEventVO();
  }

  private String getGeneralInformationDestination(HttpServletRequest request, ScheduleEventBean current) {
    request.setAttribute(CURRENT_SCHEDULE_EVENT, current);
    return jspDestination;
  }

  @Override
  public String getBackDestination(String function, ScheduleEventSessionController scheduleeventSC,
      HttpServletRequest request) {
    ScheduleEventBean current = scheduleeventSC.getCurrentScheduleEventVO();
    return getGeneralInformationDestination(request, current);
  }

}
