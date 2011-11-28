package com.silverpeas.silvercrawler.servlets.handlers;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;

/**
 * Handler for use case : file new name has been submitted.
 *
 * @author Ludovic Bertin
 *
 */
public class RenameFileHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController, HttpServletRequest request)
      throws Exception {

    // Retrieves parameters
    String fileName = (String) request.getParameter("fileName");
    String newName = (String) request.getParameter("newName");

    // Is User has admin or publisher profile
    String userHisghestRole = getUserHighestRole(sessionController);
    boolean isAdminOrPublisher = (userHisghestRole.equals("admin") || userHisghestRole.equals("publisher"));

    if (!isAdminOrPublisher) {
      request.setAttribute( "errorMessage", "User has not admin or publisher rights");
      return "fileRenameFailed.jsp";
    }

    // Rename file
    try {
      sessionController.renameFolder(fileName, newName);
    }
    catch (Exception e) {
      request.setAttribute( "errorMessage", e.getMessage());
      return "operationFailed.jsp";
    }

    // returns page to redirect to
    return "operationSucceeded.jsp";
  }

}
