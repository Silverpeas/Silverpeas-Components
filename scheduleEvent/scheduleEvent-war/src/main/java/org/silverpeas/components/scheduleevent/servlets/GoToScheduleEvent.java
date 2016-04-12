package org.silverpeas.components.scheduleevent.servlets;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.util.servlet.GoTo;

public class GoToScheduleEvent extends GoTo {

  private static final long serialVersionUID = 8520721008123551592L;

  @Override
  public String getDestination(String objectId, HttpServletRequest req, HttpServletResponse res)
      throws Exception {



    String gotoURL =  URLUtil.getURL(URLUtil.CMP_SCHEDULE_EVENT)
        + "Detail?scheduleEventId=" + objectId;

    return "goto=" + URLEncoder.encode(gotoURL, "UTF-8");
  }

}
