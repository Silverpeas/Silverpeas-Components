/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.silvercrawler.servlets.handlers;

import org.silverpeas.components.silvercrawler.control.SilverCrawlerSessionController;
import org.silverpeas.core.util.file.FileUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * Handler for use case : new folder name (for creation) has been submitted.
 * @author Ludovic Bertin
 */
public class CreateFolderHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController,
      HttpServletRequest request) throws Exception {

    // Retrieves parameters
    String newName = FileUtil.checkTaintedData(request.getParameter("newName"));

    // Is User has admin profile
    String userHisghestRole = getUserHighestRole(sessionController);
    boolean isAdmin = (userHisghestRole.equals("admin"));

    if (!isAdmin) {
      request.setAttribute("errorMessage", "User has not admin rights");
      return "operationFailed.jsp";
    }

    // Create folder
    try {
      sessionController.createFolder(newName);
    } catch (Exception e) {
      request.setAttribute("errorMessage", e.getMessage());
      return "operationFailed.jsp";
    }

    // returns page to redirect to
    return "operationSucceeded.jsp";
  }

}
