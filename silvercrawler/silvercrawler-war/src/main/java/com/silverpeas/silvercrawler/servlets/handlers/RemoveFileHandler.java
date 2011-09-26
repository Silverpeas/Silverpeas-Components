package com.silverpeas.silvercrawler.servlets.handlers;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;

/**
 * Handler for use case : admin request file removal.
 *
 * @author Ludovic Bertin
 *
 */
public class RemoveFileHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController, HttpServletRequest request)
      throws Exception {

    // Retrieves files name to be removed
    String fileName = (String) request.getParameter("FileName");

    // Is User has admin or publisher profile
    String userHisghestRole = getUserHighestRole(sessionController);
    boolean isAdminOrPublisher = (userHisghestRole.equals("admin") || userHisghestRole.equals("publisher"));

    // Un-index requested file
    sessionController.unindexFile(fileName);

    // Remove folder physically
    sessionController.removeFile(fileName, isAdminOrPublisher);

    // redirect to "ViewDirectory" use case
    return HandlerProvider.getHandler("ViewDirectory").computeDestination(sessionController, request);
  }

}
