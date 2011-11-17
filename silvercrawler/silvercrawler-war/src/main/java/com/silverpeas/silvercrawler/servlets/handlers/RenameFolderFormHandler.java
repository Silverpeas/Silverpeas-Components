package com.silverpeas.silvercrawler.servlets.handlers;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;
import com.silverpeas.silvercrawler.model.SilverCrawlerForbiddenActionException;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Handler for use case : admin request folder renaming.
 *
 * @author Ludovic Bertin
 *
 */
public class RenameFolderFormHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController, HttpServletRequest request)
      throws Exception {

    // Retrieves folder's name to be removed
    String folderName = (String) request.getParameter("folderName");

    // Is User has admin profile
    String userHisghestRole = getUserHighestRole(sessionController);
    boolean isAdmin = (userHisghestRole.equals("admin"));

    if (!isAdmin) {
      throw new SilverCrawlerForbiddenActionException("RenameFolderFormHandler.getDestination", SilverpeasException.ERROR, "user has not admin rights");
    }

     // store objects in request as attributes
    request.setAttribute("currentFolder", sessionController.getCurrentPath());
    request.setAttribute("folderName", folderName);

    // returns page to redirect to
    return "folderRenameForm.jsp";
  }

}
