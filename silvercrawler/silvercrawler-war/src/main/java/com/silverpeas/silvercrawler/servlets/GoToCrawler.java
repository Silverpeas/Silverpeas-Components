package com.silverpeas.silvercrawler.servlets;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.peasUtil.GoTo;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class GoToCrawler extends GoTo {

  public String getDestination(String objectId, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    String componentId = objectId;
    String path = req.getParameter("Path");
    SilverTrace.info("formManager", "GoToCrawler.getDestination",
        "root.MSG_GEN_PARAM_VALUE", "componentId = " + componentId);
    String gotoURL = URLManager.getURL(null, componentId)
        + "SubDirectoryFromResult?DirectoryPath=" + path;
    return "goto=" + URLEncoder.encode(gotoURL, "UTF-8");
  }

  public GoToCrawler() {
  }
}