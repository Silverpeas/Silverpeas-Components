package com.silverpeas.silvercrawler.servlets.handlers;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collection;

/**
 * Handler for use case : admin request removal of a set of folders.
 * @author Ludovic Bertin
 */
public class RemoveSelectedFoldersHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController,
      HttpServletRequest request) throws Exception {

    // Is User has admin profile
    String userHisghestRole = getUserHighestRole(sessionController);
    boolean isAdmin = (userHisghestRole.equals("admin"));

    // retrieves folder list
    String[] selectedFolders = request.getParameterValues("checkedDir");
    if (selectedFolders != null) {
      Collection<String> listFolderToRemove = Arrays.asList(selectedFolders);
      SilverTrace.info("silverCrawler", "RemoveSelectedFoldersHandler.getDestination()",
          "root.MSG_GEN_PARAM_VALUE", "listFolderToRemove = " + listFolderToRemove);

      // Un-index selected folders
      for (String folder : listFolderToRemove) {
        sessionController.unindexPath(folder);
      }

      // Remove folders physically
      for (String folder : listFolderToRemove) {
        sessionController.removeSubFolder(folder, isAdmin);
      }
    }

    // redirect to "ViewDirectory" use case
    return HandlerProvider.getHandler("ViewDirectory")
        .computeDestination(sessionController, request);
  }

}
