package com.silverpeas.silvercrawler.servlets.handlers;

import java.util.Arrays;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * Handler for use case : admin/publisher request removal of a set of files.
 *
 * @author Ludovic Bertin
 *
 */
public class RemoveSelectedFilesHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController, HttpServletRequest request)
      throws Exception {

    // Is User has admin or publisher profile
    String userHisghestRole = getUserHighestRole(sessionController);
    boolean isAdminOrPublisher = (userHisghestRole.equals("admin") || userHisghestRole.equals("publisher"));

    // retrieves file list
    String[] selectedFiles = request.getParameterValues("checkedFile");
    Collection<String> listFilesToRemove = Arrays.asList(selectedFiles);
    SilverTrace.info("silverCrawler", "RemoveSelectedFilesHandler.getDestination()", "root.MSG_GEN_PARAM_VALUE", "listFilesToRemove = " + listFilesToRemove);

    // Un-index requested file
    for (String fileName : listFilesToRemove) {
      sessionController.unindexFile(fileName);
    }

    // Remove folder physically
    for (String fileName : listFilesToRemove) {
      sessionController.removeFile(fileName, isAdminOrPublisher);
    }

    // redirect to "ViewDirectory" use case
    return HandlerProvider.getHandler("ViewDirectory").computeDestination(sessionController, request);
  }

}
