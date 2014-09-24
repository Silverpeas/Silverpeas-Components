package com.silverpeas.silvercrawler.servlets.handlers;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;
import com.silverpeas.silvercrawler.model.SilverCrawlerForbiddenActionException;
import org.silverpeas.util.exception.SilverpeasException;

/**
 * Handler for use case : admin request new folder creation.
 *
 * @author Ludovic Bertin
 *
 */
public class CreateFolderFormHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController, HttpServletRequest request)
      throws Exception {

    // Is User has admin profile
    String userHisghestRole = getUserHighestRole(sessionController);
    boolean isAdmin = (userHisghestRole.equals("admin"));

    if (!isAdmin) {
      throw new SilverCrawlerForbiddenActionException("CreateFolderFormHandler.getDestination", SilverpeasException.ERROR, "user has not admin rights");
    }

     // store objects in request as attributes
    request.setAttribute("currentFolder", sessionController.getCurrentPath());

    // returns page to redirect to
    return "folderCreationForm.jsp";
  }

}
