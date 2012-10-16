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

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.scheduleevent.control.ScheduleEventSessionController;
import com.silverpeas.scheduleevent.view.OptionDateVO;

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
      HttpServletRequest request) throws Exception {
    if (scheduleeventSC.isCurrentScheduleEventDefined()) {
      return saveOptionDatesAndForwardRequestHandler(function, scheduleeventSC, request);
    } else {
      return getFirstStepDestination(function, scheduleeventSC, request);
    }
  }

  private String saveOptionDatesAndForwardRequestHandler(String function,
      ScheduleEventSessionController scheduleeventSC, HttpServletRequest request) throws Exception {
    saveOptionDatesInCurrentScheduleEvent(scheduleeventSC, request);
    return forwardRequestHandler.getDestination(function, scheduleeventSC, request);
  }

  private void saveOptionDatesInCurrentScheduleEvent(
      ScheduleEventSessionController scheduleeventSC, HttpServletRequest request) throws Exception {
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
      HttpServletRequest request) throws Exception {
    return firstStepRequestHandler.getDestination(function, scheduleeventSC, request);
  }

}
