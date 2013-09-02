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

package com.silverpeas.scheduleevent.servlets.handlers;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.scheduleevent.control.ScheduleEventSessionController;
import com.silverpeas.scheduleevent.service.model.beans.Response;
import com.silverpeas.scheduleevent.service.model.beans.ScheduleEvent;

public class ScheduleEventValidResponseRequestHandler implements ScheduleEventRequestHandler {

  private ScheduleEventRequestHandler forwardRequestHandler = null;

  public void setForwardRequestHandler(ScheduleEventRequestHandler forwardRequestHandler) {
    this.forwardRequestHandler = forwardRequestHandler;
  }

  @Override
  public String getDestination(String function, ScheduleEventSessionController scheduleeventSC,
      HttpServletRequest request) throws Exception {
    if (forwardRequestHandler != null) {
      return validUserAnswerAndForwardRequestHandler(function, scheduleeventSC, request);
    } else {
      throw UndefinedForwardRequestHandlerException();
    }
  }

  private String validUserAnswerAndForwardRequestHandler(String function,
      ScheduleEventSessionController scheduleeventSC, HttpServletRequest request) throws Exception {
    ScheduleEvent event = getEvent(scheduleeventSC, request);
    if (event != null) {
      String[] checkedAnswers = request.getParameterValues("userChoices");
      updateUserAvailabilities(scheduleeventSC, event, checkedAnswers);
      return forwardRequestHandler.getDestination(function, scheduleeventSC, request);
    } else {
      throw new Exception("validUserAnswerAndForwardRequestHandler: No valid event");
    }
  }
  
  private ScheduleEvent getEvent(ScheduleEventSessionController scheduleeventSC,
      HttpServletRequest request) {
    String scheduleEventId = request.getParameter("scheduleEventId");
    if (scheduleEventId != null) {
      return scheduleeventSC.getDetail(scheduleEventId);
    }
    return null;
  }

  private void updateUserAvailabilities(ScheduleEventSessionController sessionController, ScheduleEvent scheduleEvent, String[] validatedDatesId) {
      ScheduleEvent eventToUpdate = sessionController.purgeOldResponseForUserId(scheduleEvent);     
      setResponsesFromUserAvailabilities(sessionController, validatedDatesId, eventToUpdate);
      sessionController.updateUserAvailabilities(eventToUpdate);
  }

  private void setResponsesFromUserAvailabilities(ScheduleEventSessionController sessionController,
      String[] validatedDatesId, ScheduleEvent event) {
    if (hasSelectedOneOrMoreTime(validatedDatesId)) {
      setSelectedResponses(sessionController, validatedDatesId, event);
    }
  }

  private boolean hasSelectedOneOrMoreTime(String[] validatedDatesId) {
    return validatedDatesId != null;
  }
  
  private void setSelectedResponses(ScheduleEventSessionController sessionController,
      String[] validatedDatesId, ScheduleEvent event) {
    Set<Response> responses = event.getResponses();
    for (String dateId : validatedDatesId) {
      Response response = sessionController.makeReponseFor(event, dateId);
      responses.add(response);
    }
  }

  private Exception UndefinedForwardRequestHandlerException() {
    return new Exception(
        "No forward request defines for" + this.getClass());
  }

}
