package com.silverpeas.scheduleevent.servlets.handlers;

import javax.servlet.http.HttpServletRequest;

import org.silverpeas.util.NotifierUtil;

import com.silverpeas.scheduleevent.control.ScheduleEventSessionController;

public class ScheduleEventCallAgainRequestHandler implements ScheduleEventRequestHandler {

  private String jspDestination;

  private ScheduleEventDetailRequestHandler detailHandler;

  public ScheduleEventCallAgainRequestHandler(String jspDestination) {
    this.jspDestination = jspDestination;
  }

  @Override
  public String getDestination(String function, ScheduleEventSessionController scheduleeventSC,
      HttpServletRequest request) throws Exception {
    scheduleeventSC.sendCallAgainNotification("callagain");
    NotifierUtil.addSuccess(scheduleeventSC.getString("scheduleevent.callagain.ok"));
    return detailHandler.getDestination(function, scheduleeventSC, request);
  }

  public void setDetailHandler(ScheduleEventDetailRequestHandler detailHandler) {
    this.detailHandler = detailHandler;
  }

}
