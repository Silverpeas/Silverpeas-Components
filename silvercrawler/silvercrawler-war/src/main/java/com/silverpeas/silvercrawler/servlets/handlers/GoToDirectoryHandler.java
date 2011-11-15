package com.silverpeas.silvercrawler.servlets.handlers;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;

/**
 * Handler for use case : user clicks on a permalink and come into silvercrawler.
 *
 * @author Ludovic Bertin
 *
 */
public class GoToDirectoryHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController, HttpServletRequest request)
      throws Exception {

    // Update current Path
    String newPath = request.getParameter("DirectoryPath");
    sessionController.goToDirectory(newPath);

    // redirect to "ViewDirectory" use case
    return HandlerProvider.getHandler("ViewDirectory").computeDestination(sessionController, request);
  }

}
