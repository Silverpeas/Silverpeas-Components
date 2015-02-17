/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.silvercrawler.servlets.handlers;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;
import com.silverpeas.silvercrawler.control.UploadItem;
import com.silverpeas.silvercrawler.control.UploadReport;

import javax.servlet.http.HttpServletRequest;

/**
 * Handler for use case : some conflicts have been detected and user make a choice for each
 * conflict.
 * @author Ludovic Bertin
 */
public class ResolveConflictsHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController,
      HttpServletRequest request) throws Exception {

    UploadReport report = sessionController.getLastUploadReport();

    // Retrieve user's choice for each conflict
    for (UploadItem item : report.getItems()) {
      if (item.isItemAlreadyExists()) {
        String choice = request.getParameter("choice" + item.getId());
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
