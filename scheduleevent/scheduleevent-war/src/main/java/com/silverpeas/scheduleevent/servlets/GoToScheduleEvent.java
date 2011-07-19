package com.silverpeas.scheduleevent.servlets;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.peasUtil.GoTo;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class GoToScheduleEvent extends GoTo {

  private static final long serialVersionUID = 8520721008123551592L;

  @Override
  public String getDestination(String objectId, HttpServletRequest req, HttpServletResponse res)
      throws Exception {

    SilverTrace.info("scheduleevent", "GoToScheduleEvent.getDestination",
        "root.MSG_GEN_PARAM_VALUE", "objectId = " + objectId);

    String gotoURL =  URLManager.getURL(URLManager.CMP_SCHEDULE_EVENT)
        + "Detail?scheduleEventId=" + objectId;
 
    return "goto=" + URLEncoder.encode(gotoURL, "UTF-8");
  }

}
