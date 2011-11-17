package com.silverpeas.silvercrawler.servlets.handlers;

import java.util.Arrays;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;
import com.silverpeas.silvercrawler.model.SilverCrawlerForbiddenActionException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Handler for use case : admin request indexation of selected folders.
 *
 * @author Ludovic Bertin
 *
 */
public class IndexSelectedFoldersHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController, HttpServletRequest request)
      throws Exception {

    SilverTrace.debug("silverCrawler", "IndexSelectedFoldersHandler.getDestination()", "root.MSG_GEN_PARAM_VALUE", "IndexSelectedFolders");

    // checks that users has admin profile
    String userHighestRole = getUserHighestRole(sessionController);
    if (!userHighestRole.equals("admin")) {
      throw new SilverCrawlerForbiddenActionException("IndexSelectedFoldersHandler.getDestination", SilverpeasException.ERROR, "User is not manager of this component");
    }

    // retrieves folder list
    String[] selectedFolders = request.getParameterValues("checkedDir");
    Collection<String> listFolderToIndex = Arrays.asList(selectedFolders);
    SilverTrace.info("silverCrawler", "IndexSelectedFoldersHandler.getDestination()", "root.MSG_GEN_PARAM_VALUE", "listFolderToIndex = " + listFolderToIndex);

    // index selected folders
    sessionController.indexPathSelected(listFolderToIndex);

    // redirect to "ViewDirectory" use case
    return HandlerProvider.getHandler("ViewDirectory").computeDestination(sessionController, request);
  }

}
