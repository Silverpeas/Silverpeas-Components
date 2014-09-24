package com.silverpeas.silvercrawler.servlets.handlers;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;
import com.silverpeas.silvercrawler.model.SilverCrawlerForbiddenActionException;
import org.silverpeas.util.exception.SilverpeasException;

/**
 * Handler for use case : admin/publisher request new file upload.
 *
 * @author Ludovic Bertin
 *
 */
public class UploadFileFormHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController, HttpServletRequest request)
      throws Exception {

    // Is User has admin or publisher profile
    String userHisghestRole = getUserHighestRole(sessionController);
    boolean isAdminOrPublisher = (userHisghestRole.equals("admin") || userHisghestRole.equals("publisher"));

    if (!isAdminOrPublisher) {
        throw new SilverCrawlerForbiddenActionException("UploadFileFormHandler.getDestination", SilverpeasException.ERROR, "user has not admin rights");
    }

     // store objects in request as attributes
    request.setAttribute("currentFolder", sessionController.getCurrentPath());

    // returns page to redirect to
    return "fileUploadForm.jsp";
  }

}
