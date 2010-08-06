package com.stratelia.webactiv.kmelia.servlets.ajax.handlers;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import com.stratelia.webactiv.kmelia.servlets.ajax.AjaxHandler;

public class GetTopicWysiwygHandler implements AjaxHandler {

  @Override
  public String handleRequest(HttpServletRequest request, ComponentSessionController controller) {   
    KmeliaSessionController kmelia = ((KmeliaSessionController) controller);
    
    return kmelia.getWysiwygOnTopic(request.getParameter("Id"));
  }

}
