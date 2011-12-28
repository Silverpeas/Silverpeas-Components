package com.silverpeas.silvercrawler.servlets.handlers;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;
import com.silverpeas.silvercrawler.control.UploadReport;

/**
 * Handler for use case : files/folder have been dropped into dragNDrop area.
 *
 * @author Ludovic Bertin
 *
 */
public class ProcessDragAndDropHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController, HttpServletRequest request)
      throws Exception {

    UploadReport report = sessionController.checkLastUpload();

    if (report.isForbiddenFolderDetected()) {
      request.setAttribute("errorMessage", sessionController.getString("silverCrawler.notAllowedToDropFolders"));
      return HandlerProvider.getHandler("ViewDirectory").getDestination(sessionController, request);
    }

    if (!report.isConflictous()) {
      report = sessionController.processLastUpload();

      request.setAttribute("errorMessage", report.displayErrors());
      request.setAttribute("successMessage", report.displaySuccess());

      return HandlerProvider.getHandler("ViewDirectory").getDestination(sessionController, request);
    }

    // redirect to "ViewDirectory" use case
    request.setAttribute("DnDReport", report);

    return "dragNDropConflicts.jsp";
  }

}
