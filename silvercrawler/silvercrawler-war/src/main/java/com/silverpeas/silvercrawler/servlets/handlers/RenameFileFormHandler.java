package com.silverpeas.silvercrawler.servlets.handlers;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;
import com.silverpeas.silvercrawler.model.SilverCrawlerForbiddenActionException;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Handler for use case : admin request file renaming.
 *
 * @author Ludovic Bertin
 *
 */
public class RenameFileFormHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController, HttpServletRequest request)
      throws Exception {

    // Retrieves folder's name to be removed
    String fileName = (String) request.getParameter("fileName");

    // Is User has admin or publisher profile
    String userHisghestRole = getUserHighestRole(sessionController);
    boolean isAdminOrPublisher = (userHisghestRole.equals("admin") || userHisghestRole.equals("publisher"));

    if (!isAdminOrPublisher) {
      throw new SilverCrawlerForbiddenActionException("RenameFileFormHandler.getDestination", SilverpeasException.ERROR, "user has not admin or publisher rights");
    }

    // store objects in request as attributes
    request.setAttribute("currentFolder", sessionController.getCurrentPath());
    request.setAttribute("fileName", fileName);

    // returns page to redirect to
    return "fileRenameForm.jsp";
  }

}
