package com.stratelia.webactiv.kmelia.servlets.ajax.handlers;

import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import com.stratelia.webactiv.kmelia.servlets.ajax.AjaxHandler;

public class UpdateTopicStatusHandler implements AjaxHandler {

  @Override
  public String handleRequest(HttpServletRequest request, ComponentSessionController controller) {
    KmeliaSessionController kmelia = ((KmeliaSessionController) controller);

    String subTopicId = request.getParameter("Id");
    String newStatus = request.getParameter("Status");
    String recursive = request.getParameter("Recursive");

    try {
      kmelia.changeTopicStatus(newStatus, subTopicId, "1".equals(recursive));
      return "ok";
    } catch (RemoteException e) {
      SilverTrace.error("kmelia", "UpdateTopicStatusHandler.handleRequest",
          "root.MSG_GEN_PARAM_VALUE", e);
      return e.getMessage();
    }
  }

}
