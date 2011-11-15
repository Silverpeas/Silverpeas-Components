package com.silverpeas.silvercrawler.servlets.handlers;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;

/**
 * Handler for use case : main view for portlet usage.
 *
 * @author Ludovic Bertin
 *
 */
public class PortletHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController, HttpServletRequest request)
      throws Exception {

    // set root path
    sessionController.setRootPath();

    // Store objects in request as attributes
    request.setAttribute("Folder", sessionController.getCurrentFolder());
    request.setAttribute("Path", sessionController.getPath());
    request.setAttribute("IsDownload", sessionController.isDownload());
    request.setAttribute("IsRootPath", new Boolean(sessionController.isRootPath()));
    request.setAttribute("IsAllowedNav", sessionController.isAllowedNav());
    request.setAttribute("RootPath", sessionController.getRootPath());

    // return page to redirect to
    return "portlet.jsp";
  }

}
