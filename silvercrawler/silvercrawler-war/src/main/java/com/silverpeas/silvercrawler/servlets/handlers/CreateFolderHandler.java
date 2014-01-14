package com.silverpeas.silvercrawler.servlets.handlers;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;

/**
 * Handler for use case : new folder name (for creation) has been submitted.
 *
 * @author Ludovic Bertin
 *
 */
public class CreateFolderHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController, HttpServletRequest request)
      throws Exception {

    // Retrieves parameters
    String newName = request.getParameter("newName");

    // Is User has admin profile
    String userHisghestRole = getUserHighestRole(sessionController);
    boolean isAdmin = (userHisghestRole.equals("admin"));

    if (!isAdmin) {
      request.setAttribute( "errorMessage", "User has not admin rights");
      return "operationFailed.jsp";
    }

    // Create folder
    try {
      sessionController.createFolder(newName);
    }
    catch (Exception e) {
      request.setAttribute( "errorMessage", e.getMessage());
      return "operationFailed.jsp";
    }

    // returns page to redirect to
    return "operationSucceeded.jsp";
  }

}
