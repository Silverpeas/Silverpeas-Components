package org.silverpeas.components.scheduleevent.servlets.handlers;

import jakarta.servlet.http.HttpServletRequest;
import org.silverpeas.components.scheduleevent.control.ScheduleEventSessionController;
import org.silverpeas.kernel.SilverpeasException;

public class ScheduleEventSimpleFormRequestHandler implements ScheduleEventBackableRequestHandler {
  private final String jspDestination;

  public ScheduleEventSimpleFormRequestHandler(String jspDestination) {
    this.jspDestination = jspDestination;
  }

  @Override
  public String getDestination(String function, ScheduleEventSessionController scheduleeventSC,
      HttpServletRequest request) throws SilverpeasException {
    request.setAttribute(CURRENT_SCHEDULE_EVENT, scheduleeventSC.getCurrentScheduleEventVO());
    return jspDestination;
  }

  @Override
  public String getBackDestination(String function, ScheduleEventSessionController scheduleeventSC,
      HttpServletRequest request) throws SilverpeasException {
    return getDestination(function, scheduleeventSC, request);
  }

}
