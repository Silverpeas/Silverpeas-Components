package com.stratelia.webactiv.kmelia.servlets.ajax.handlers;

import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.kmelia.KmeliaConstants;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.webactiv.kmelia.servlets.ajax.AjaxHandler;

public class UnbindToPubliHandler implements AjaxHandler {

  @Override
  public String handleRequest(HttpServletRequest request, ComponentSessionController controller) {

    if (StringUtil.isDefined(request.getParameter("TopicToLinkId"))) {

      HashSet<String> list =
          (HashSet) request.getSession().getAttribute(KmeliaConstants.PUB_TO_LINK_SESSION_KEY);
      if (list == null) {
        list = new HashSet<String>();
        request.getSession().setAttribute(KmeliaConstants.PUB_TO_LINK_SESSION_KEY, list);
      }

      list.remove(request.getParameter("TopicToLinkId"));
    }

    return "ok";
  }

}
