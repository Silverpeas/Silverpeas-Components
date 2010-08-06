package com.stratelia.webactiv.kmelia.servlets.ajax.handlers;

import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import com.stratelia.webactiv.kmelia.servlets.ajax.AjaxHandler;

public class EmptyTrashHandler implements AjaxHandler {

  @Override
  public String handleRequest(HttpServletRequest request, ComponentSessionController controller) {   
    KmeliaSessionController kmelia = ((KmeliaSessionController) controller);
    
    try {
      kmelia.flushTrashCan();
      return "ok";
    } catch (RemoteException e) {
      SilverTrace.error("kmelia", "EmptyTrashHandler.handleRequest", "root.MSG_GEN_PARAM_VALUE", e);
      return e.getMessage();
    }
  }

}
