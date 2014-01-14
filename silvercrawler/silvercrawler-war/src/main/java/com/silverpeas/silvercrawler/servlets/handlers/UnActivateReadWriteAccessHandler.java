package com.silverpeas.silvercrawler.servlets.handlers;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;

/**
 * Handler for use case : admin request Read/Write access unactivation.
 *
 * @author Ludovic Bertin
 *
 */
public class UnActivateReadWriteAccessHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController, HttpServletRequest request)
      throws Exception {

    // turns RW access OFF
    sessionController.switchReadWriteAccess(false);

    // redirect to "ViewDirectory" use case
    return HandlerProvider.getHandler("ViewDirectory").computeDestination(sessionController, request);
  }

}
