package com.stratelia.webactiv.kmelia.servlets.ajax.handlers;

import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import com.stratelia.webactiv.kmelia.servlets.ajax.AjaxHandler;

public class DeleteHandler implements AjaxHandler{

  @Override
  public String handleRequest(HttpServletRequest request, ComponentSessionController controller) {
    String id = request.getParameter("Id");
    try {
      ((KmeliaSessionController)controller).deleteTopic(id);
      return "ok";
    } catch (RemoteException e) {
      SilverTrace.error("kmelia", "DeleteHandler.handleRequest", "root.MSG_GEN_PARAM_VALUE", e);
      return e.getMessage();
    }
  }

}
