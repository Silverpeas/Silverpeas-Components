package org.silverpeas.components.scheduleevent.servlets.handlers;

import jakarta.servlet.http.HttpServletRequest;
import org.silverpeas.components.scheduleevent.control.ScheduleEventSessionController;
import org.silverpeas.kernel.SilverpeasException;

public interface ScheduleEventBackableRequestHandler extends ScheduleEventRequestHandler {

  String getBackDestination(String function, ScheduleEventSessionController scheduleeventSC,
      HttpServletRequest request) throws SilverpeasException;
}
