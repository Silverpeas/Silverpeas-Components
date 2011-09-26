package com.silverpeas.silvercrawler.servlets.handlers;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;

/**
 * Handler for use case : View history by user.
 *
 * @author Ludovic Bertin
 *
 */
public class ViewHistoryByUserHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController, HttpServletRequest request)
      throws Exception {

    // Retrieve parameters
    String userId = (String) request.getParameter("UserId");
    String userName = (String) request.getParameter("UserName");
    String folderName = (String) request.getParameter("FolderName");

    // stores objects as request attributes
    request.setAttribute("DownloadsByUser", sessionController.getHistoryByUser(folderName, userId));
    request.setAttribute("UserName", userName);
    request.setAttribute("UserId", userId);
    request.setAttribute("FolderName", folderName);

    // returns page to redirect to
    return "historyByUser.jsp";
  }

}
