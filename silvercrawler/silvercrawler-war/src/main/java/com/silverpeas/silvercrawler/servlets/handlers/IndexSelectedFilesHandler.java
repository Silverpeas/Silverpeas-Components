package com.silverpeas.silvercrawler.servlets.handlers;

import java.util.Arrays;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;
import com.silverpeas.silvercrawler.model.SilverCrawlerForbiddenActionException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Handler for use case : admin request indexation of selected files.
 *
 * @author Ludovic Bertin
 *
 */
public class IndexSelectedFilesHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController, HttpServletRequest request)
      throws Exception {

    SilverTrace.debug("silverCrawler", "SilverCrawlerRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "IndexSelectedFiles");

    // checks that users has admin profile
    String userHighestRole = getUserHighestRole(sessionController);
    if (!userHighestRole.equals("admin")) {
      throw new SilverCrawlerForbiddenActionException("IndexSelectedFilesHandler.getDestination", SilverpeasException.ERROR, "User is not manager of this component");
    }

    // retrieves file list
    String[] selectedFiles = request.getParameterValues("checkedFile");
    Collection<String> listFilesToIndex = Arrays.asList(selectedFiles);
    SilverTrace.info("silverCrawler", "IndexSelectedFilesHandler.getDestination()", "root.MSG_GEN_PARAM_VALUE", "listFilesToIndex = " + listFilesToIndex);

    // index selected folders
    sessionController.indexSelectedFiles(listFilesToIndex);

    // redirect to "ViewDirectory" use case
    return HandlerProvider.getHandler("ViewDirectory").computeDestination(sessionController, request);
  }

}
