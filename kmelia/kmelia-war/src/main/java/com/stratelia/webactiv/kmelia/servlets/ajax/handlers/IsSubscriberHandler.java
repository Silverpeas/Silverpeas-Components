package com.stratelia.webactiv.kmelia.servlets.ajax.handlers;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import com.stratelia.webactiv.kmelia.servlets.ajax.AjaxHandler;
import com.stratelia.webactiv.util.node.model.NodePK;

public class IsSubscriberHandler implements AjaxHandler {

  @Override
  public String handleRequest(HttpServletRequest request, ComponentSessionController controller) {
    KmeliaSessionController kmelia = ((KmeliaSessionController) controller);

    String topicId = request.getParameter("Id");
    
    try {
      // check if user is subscriber of given topic
      if (kmelia.getKmeliaBm().checkSubscription(new NodePK(topicId, kmelia.getComponentId()), kmelia.getUserId())) {
        return "false";
      }
      return "true";
    } catch (Exception e) {
      SilverTrace.error("kmelia", "IsSubscriberHandler.handleRequest", "root.MSG_GEN_PARAM_VALUE", e);
      return e.getMessage();
    }
  }

}
