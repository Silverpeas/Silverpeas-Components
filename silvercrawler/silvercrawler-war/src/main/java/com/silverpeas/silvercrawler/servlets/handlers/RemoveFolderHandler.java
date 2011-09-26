package com.silverpeas.silvercrawler.servlets.handlers;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;

/**
 * Handler for use case : admin request folder removal.
 *
 * @author Ludovic Bertin
 *
 */
public class RemoveFolderHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController, HttpServletRequest request)
      throws Exception {

    // Retrieves folder's name to be removed
    String folderName = (String) request.getParameter("FolderName");

    // Is User has admin profile
    String userHisghestRole = getUserHighestRole(sessionController);
    boolean isAdmin = (userHisghestRole.equals("admin"));

    // Un-index requested pathunindexPath(String folderName)
    sessionController.unindexPath(folderName);

    // Remove folder physically
    sessionController.removeSubFolder(folderName, isAdmin);

    // redirect to "ViewDirectory" use case
    return HandlerProvider.getHandler("ViewDirectory").computeDestination(sessionController, request);
  }

}
