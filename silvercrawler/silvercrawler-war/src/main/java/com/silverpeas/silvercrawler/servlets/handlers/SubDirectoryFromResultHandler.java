package com.silverpeas.silvercrawler.servlets.handlers;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;

/**
 * Handler for use case : Go to Sub Directory From Result page.
 *
 * @author Ludovic Bertin
 *
 */
public class SubDirectoryFromResultHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController, HttpServletRequest request)
      throws Exception {
    // Update current Path
    String newPath = request.getParameter("DirectoryPath");
    sessionController.setCurrentPathFromResult(newPath);

    // redirect to "ViewDirectory" use case
    return HandlerProvider.getHandler("ViewDirectory").computeDestination(sessionController, request);
  }

}
