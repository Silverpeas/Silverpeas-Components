package com.silverpeas.external.mailinglist.servlets;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.mailinglist.service.ServicesFactory;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.silverpeas.peasUtil.GoTo;
import com.stratelia.silverpeas.peasCore.URLManager;

public class GoToMessage extends GoTo {

  public String getDestination(String id, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    Message message = ServicesFactory.getMessageService().getMessage(id);
    String baseUrl = URLManager.getURL(null, message.getComponentId());
    if (!baseUrl.endsWith("/")) {
      baseUrl = baseUrl + '/';
    }
    String gotoURL = baseUrl + "message/" + id;
    return "goto=" + URLEncoder.encode(gotoURL, "UTF-8");
  }

}
