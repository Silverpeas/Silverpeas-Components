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

import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.silverpeas.components.scheduleevent.control.ScheduleEventSessionController;
import org.silverpeas.components.scheduleevent.view.OptionDateVO;
import org.silverpeas.kernel.SilverpeasException;

public class ScheduleEventTimeNextRequestHandler implements ScheduleEventRequestHandler {

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
      HttpServletRequest request) throws SilverpeasException {
    if (scheduleeventSC.isCurrentScheduleEventDefined()) {
      return saveOptionDatesAndForwardRequestHandler(function, scheduleeventSC, request);
    } else {
      return getFirstStepDestination(function, scheduleeventSC, request);
    }
  }

  private String saveOptionDatesAndForwardRequestHandler(String function,
      ScheduleEventSessionController scheduleeventSC, HttpServletRequest request) throws SilverpeasException {
    saveOptionDatesInCurrentScheduleEvent(scheduleeventSC, request);
    return forwardRequestHandler.getDestination(function, scheduleeventSC, request);
  }

  private void saveOptionDatesInCurrentScheduleEvent(
      ScheduleEventSessionController scheduleeventSC, HttpServletRequest request) throws SilverpeasException {
    Set<OptionDateVO> optionalDays = scheduleeventSC.getCurrentOptionalDateIndexes();
    for (OptionDateVO date : optionalDays) {
      setPartOfDayFrom(request, date);
    }
    scheduleeventSC.setCurrentScheduleEventWith(optionalDays);
  }

  private void setPartOfDayFrom(HttpServletRequest request, OptionDateVO date) {
    date.setMorning(isMorningParameterChecked(request, date));
    date.setAfternoon(isAfernoonParameterChecked(request, date));
  }

  private boolean isMorningParameterChecked(HttpServletRequest request, OptionDateVO date) {
    return requestParameterExists(request, date.getMorningIndexFormat());
  }

  private boolean isAfernoonParameterChecked(HttpServletRequest request, OptionDateVO date) {
    return requestParameterExists(request, date.getAfternoonIndexFormat());
  }

  private boolean requestParameterExists(HttpServletRequest request, String parameter) {
    return request.getParameter(parameter) != null;
  }

  private String getFirstStepDestination(String function,
      ScheduleEventSessionController scheduleeventSC,
      HttpServletRequest request) throws SilverpeasException {
    return firstStepRequestHandler.getDestination(function, scheduleeventSC, request);
  }

}
