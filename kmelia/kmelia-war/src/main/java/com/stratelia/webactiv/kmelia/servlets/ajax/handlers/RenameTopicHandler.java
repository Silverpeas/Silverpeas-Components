package com.stratelia.webactiv.kmelia.servlets.ajax.handlers;

import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import com.stratelia.webactiv.kmelia.servlets.ajax.AjaxHandler;
import com.stratelia.webactiv.util.node.model.NodeDetail;

public class RenameTopicHandler implements AjaxHandler {

  @Override
  public String handleRequest(HttpServletRequest request, ComponentSessionController controller) {
    KmeliaSessionController kmelia = ((KmeliaSessionController) controller);

    String topicId = request.getParameter("Id");
    String name = request.getParameter("Name");

    try {
      NodeDetail node = kmelia.getNodeHeader(topicId);
      node.setName(name);
      kmelia.updateTopicHeader(node, "NoAlert");
      return "ok";
    } catch (RemoteException e) {
      SilverTrace.error("kmelia", "RenameTopicHandler.handleRequest", "root.MSG_GEN_PARAM_VALUE", e);
      return e.getMessage();
    }
  }

}
