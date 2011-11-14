package com.silverpeas.silvercrawler.servlets.handlers;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;
import com.silverpeas.silvercrawler.control.UploadItem;
import com.silverpeas.silvercrawler.control.UploadReport;

/**
 * Handler for use case : some conflicts have been detected and user make a choice for each conflict.
 *
 * @author Ludovic Bertin
 *
 */
public class ResolveConflictsHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController, HttpServletRequest request)
      throws Exception {

    UploadReport report = sessionController.getLastUploadReport();

    // Retrieve user's choice for each conflict
    for (UploadItem item : report.getItems()) {
      if (item.isItemAlreadyExists()) {
        String choice = request.getParameter("choice"+item.getId());
        if ("replace".equals(choice)) {
          item.setReplace(true);
        }
      }
    }

    // Process upload
    report = sessionController.processLastUpload();

    // Generates messages to display
    request.setAttribute("errorMessage", report.displayErrors());
    request.setAttribute("successMessage", report.displaySuccess());

    // Go back to main page
    return HandlerProvider.getHandler("ViewDirectory").getDestination(sessionController, request);
  }

}
