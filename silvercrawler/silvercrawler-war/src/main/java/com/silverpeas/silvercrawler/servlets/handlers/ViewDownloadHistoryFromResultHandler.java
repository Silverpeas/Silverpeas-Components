package com.silverpeas.silvercrawler.servlets.handlers;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;

/**
 * Handler for use case : View download history (from result page).
 *
 * @author Ludovic Bertin
 *
 */
public class ViewDownloadHistoryFromResultHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController, HttpServletRequest request)
      throws Exception {

    // Retrieve requested path
    String fullPath = request.getParameter("Name");

    // stores objects as request attributes
    request.setAttribute("Downloads", sessionController.getHistoryByFolderFromResult(fullPath));

    // as user comes from search result page, full path is given => extracts only name
    String name = sessionController.getNameFromPath(fullPath);
    request.setAttribute("Name", name);

    // returns page to redirect to
    return "history.jsp";
  }

}
