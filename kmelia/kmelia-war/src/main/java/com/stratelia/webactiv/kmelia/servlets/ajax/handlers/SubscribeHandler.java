package com.stratelia.webactiv.kmelia.servlets.ajax.handlers;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.kmelia.KmeliaSecurity;
import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import com.stratelia.webactiv.kmelia.servlets.ajax.AjaxHandler;

public class SubscribeHandler implements AjaxHandler {

  @Override
  public String handleRequest(HttpServletRequest request, ComponentSessionController controller) {
    KmeliaSessionController kmelia = ((KmeliaSessionController) controller);

    String topicId = request.getParameter("Id");

    try {
      // check if user is allowed to access to given topic
      if (isNodeAvailable(kmelia, topicId)) {
        kmelia.addSubscription(topicId);
        return "ok";
      }
      return "nok";
    } catch (Exception e) {
      SilverTrace.error("kmelia", "SubscribeHandler.handleRequest", "root.MSG_GEN_PARAM_VALUE", e);
      return e.getMessage();
    }
  }

  private boolean isNodeAvailable(KmeliaSessionController kmelia, String nodeId) {
    KmeliaSecurity security = new KmeliaSecurity();
    return security.isObjectAvailable(kmelia.getComponentId(), kmelia.getUserId(), nodeId, "Node");
  }

}
