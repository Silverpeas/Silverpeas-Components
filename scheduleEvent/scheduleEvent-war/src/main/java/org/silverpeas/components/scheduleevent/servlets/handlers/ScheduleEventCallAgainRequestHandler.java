package org.silverpeas.components.scheduleevent.servlets.handlers;

import jakarta.servlet.http.HttpServletRequest;
import org.silverpeas.components.scheduleevent.control.ScheduleEventSessionController;
import org.silverpeas.kernel.SilverpeasException;

public class ScheduleEventCallAgainRequestHandler implements ScheduleEventRequestHandler {

  private ScheduleEventDetailRequestHandler detailHandler;


  @Override
  public String getDestination(String function, ScheduleEventSessionController scheduleeventSC,
      HttpServletRequest request) throws SilverpeasException {
    String message = request.getParameter("message_content");
    scheduleeventSC.sendCallAgainNotification(message);
    return detailHandler.getDestination(function, scheduleeventSC, request);
  }

  public void setForwardRequestHandler(ScheduleEventDetailRequestHandler detailHandler) {
    this.detailHandler = detailHandler;
  }

}
