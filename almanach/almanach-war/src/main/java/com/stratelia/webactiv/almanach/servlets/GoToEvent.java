package com.stratelia.webactiv.almanach.servlets;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachBm;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachBmHome;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.almanach.model.EventPK;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.silverpeas.peasUtil.GoTo;

public class GoToEvent extends GoTo {
  public String getDestination(String eventId, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    EventPK eventPK = new EventPK(eventId);
    EventDetail event = getAlmanachBm().getEventDetail(eventPK);
    String componentId = event.getPK().getInstanceId();

    SilverTrace.info("gallery", "GoToImage.doPost", "root.MSG_GEN_PARAM_VALUE",
        "componentId = " + componentId);

    String gotoURL = URLManager.getURL(null, componentId) + event.getURL();

    return "goto=" + URLEncoder.encode(gotoURL, "UTF-8");
  }

  private AlmanachBm getAlmanachBm() {
    AlmanachBm currentAlmanachBm = null;
    try {
      AlmanachBmHome almanachBmHome = (AlmanachBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.ALMANACHBM_EJBHOME, AlmanachBmHome.class);
      currentAlmanachBm = almanachBmHome.create();
    } catch (Exception e) {
      displayError(null);
    }
    return currentAlmanachBm;
  }
}