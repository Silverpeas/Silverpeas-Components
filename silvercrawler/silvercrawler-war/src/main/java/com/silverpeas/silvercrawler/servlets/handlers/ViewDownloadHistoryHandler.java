package com.silverpeas.silvercrawler.servlets.handlers;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;

/**
 * Handler for use case : View download history.
 *
 * @author Ludovic Bertin
 *
 */
public class ViewDownloadHistoryHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController, HttpServletRequest request)
      throws Exception {

    // Retrieve requested name
    String name = request.getParameter("Name");

    // stores objects as request attributes
    request.setAttribute("Downloads", sessionController.getHistoryByFolder(name));
    request.setAttribute("Name", name);

    // returns page to redirect to
    return "history.jsp";
  }

}
