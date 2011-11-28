package com.silverpeas.silvercrawler.servlets.handlers;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;

/**
 * Handler for use case : Main
 *
 * @author Ludovic Bertin
 *
 */
public class InitHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController, HttpServletRequest request)
      throws Exception {

    // Set root path
    sessionController.setRootPath();

    // redirect to "ViewDirectory" use case
    return HandlerProvider.getHandler("ViewDirectory").computeDestination(sessionController, request);
  }

}
