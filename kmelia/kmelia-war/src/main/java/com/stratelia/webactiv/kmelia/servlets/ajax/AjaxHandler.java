package com.stratelia.webactiv.kmelia.servlets.ajax;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.peasCore.ComponentSessionController;

public interface AjaxHandler {

  public String handleRequest(HttpServletRequest request, ComponentSessionController controller);

}
