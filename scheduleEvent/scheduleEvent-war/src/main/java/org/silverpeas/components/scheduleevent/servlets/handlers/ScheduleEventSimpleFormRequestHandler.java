package org.silverpeas.components.scheduleevent.servlets.handlers;

import javax.servlet.http.HttpServletRequest;

import org.silverpeas.components.scheduleevent.control.ScheduleEventSessionController;
import org.silverpeas.components.scheduleevent.control.ScheduleEventSessionController;

public class ScheduleEventSimpleFormRequestHandler implements ScheduleEventBackableRequestHandler {
  private String jspDestination;

  public ScheduleEventSimpleFormRequestHandler(String jspDestination) {
    this.jspDestination = jspDestination;
  }

  @Override
  public String getDestination(String function, ScheduleEventSessionController scheduleeventSC,
      HttpServletRequest request) throws Exception {
    request.setAttribute(CURRENT_SCHEDULE_EVENT, scheduleeventSC.getCurrentScheduleEventVO());
    return jspDestination;
  }

  @Override
  public String getBackDestination(String function, ScheduleEventSessionController scheduleeventSC,
      HttpServletRequest request) throws Exception {
    return getDestination(function, scheduleeventSC, request);
  }

}
