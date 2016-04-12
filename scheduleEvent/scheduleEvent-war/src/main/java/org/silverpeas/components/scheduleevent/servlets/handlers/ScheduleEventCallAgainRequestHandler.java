package org.silverpeas.components.scheduleevent.servlets.handlers;

import org.silverpeas.components.scheduleevent.control.ScheduleEventSessionController;
import org.silverpeas.components.scheduleevent.control.ScheduleEventSessionController;

import javax.servlet.http.HttpServletRequest;

public class ScheduleEventCallAgainRequestHandler implements ScheduleEventRequestHandler {

  private ScheduleEventDetailRequestHandler detailHandler;

  public ScheduleEventCallAgainRequestHandler() {
  }

  @Override
  public String getDestination(String function, ScheduleEventSessionController scheduleeventSC,
      HttpServletRequest request) throws Exception {
    String message = request.getParameter("message_content");
    scheduleeventSC.sendCallAgainNotification(message);
    return detailHandler.getDestination(function, scheduleeventSC, request);
  }

  public void setForwardRequestHandler(ScheduleEventDetailRequestHandler detailHandler) {
    this.detailHandler = detailHandler;
  }

}
