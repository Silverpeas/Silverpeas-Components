/*
 * Copyright (C) 2000 - 2024 Silverpeas
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

import javax.servlet.http.HttpServletRequest;

import org.silverpeas.components.scheduleevent.control.ScheduleEventSessionController;
import org.silverpeas.components.scheduleevent.service.model.ScheduleEventBean;

public class ScheduleEventDescriptionNextRequestHandler implements ScheduleEventRequestHandler {

  private ScheduleEventRequestHandler firstStepRequestHandler = null;
  private ScheduleEventRequestHandler forwardRequestHandler = null;

  public void setFirstStepRequestHandler(ScheduleEventRequestHandler forwardRequestHandler) {
    this.firstStepRequestHandler = forwardRequestHandler;
  }

  public void setForwardRequestHandler(ScheduleEventRequestHandler forwardRequestHandler) {
    this.forwardRequestHandler = forwardRequestHandler;
  }

  @Override
  public String getDestination(String function, ScheduleEventSessionController scheduleeventSC,
      HttpServletRequest request) throws Exception {
    if (scheduleeventSC.isCurrentScheduleEventDefined()) {
      return saveGeneralInformationAndForwardRequestHandler(function, scheduleeventSC, request);
    } else {
      return getFirstStepDestination(function, scheduleeventSC, request);
    }
  }

  private String saveGeneralInformationAndForwardRequestHandler(String function,
      ScheduleEventSessionController scheduleeventSC,
      HttpServletRequest request) throws Exception {
    saveGeneralInformation(request, scheduleeventSC.getCurrentScheduleEvent());
    return forwardRequestHandler.getDestination(function, scheduleeventSC, request);
  }

  private void saveGeneralInformation(HttpServletRequest request, ScheduleEventBean current) {
    current.setTitle(request.getParameter("title"));
    current.setDescription(request.getParameter("description"));
  }

  private String getFirstStepDestination(String function,
      ScheduleEventSessionController scheduleeventSC,
      HttpServletRequest request) throws Exception {
    return firstStepRequestHandler.getDestination(function, scheduleeventSC, request);
  }

}
