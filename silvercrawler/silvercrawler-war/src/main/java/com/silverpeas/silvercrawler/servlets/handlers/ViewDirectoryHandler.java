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
import com.silverpeas.silvercrawler.model.FileFolder;

import javax.servlet.http.HttpServletRequest;

/**
 * Handler for use case : View Directory.
 * @author Ludovic Bertin
 */
public class ViewDirectoryHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController,
      HttpServletRequest request) throws Exception {

    // Get current folder
    String userHighestRole = getUserHighestRole(sessionController);
    boolean isAdmin = (userHighestRole.equals("admin"));
    FileFolder currentFolder = sessionController.getCurrentFolder(isAdmin);

    // Special case: folder list is forbidden
    if (!currentFolder.isReadable()) {
      request.setAttribute("errorMessage",
          sessionController.getString("silverCrawler.notAllowedToReadFolderContent"));
    }

    // Reset UploadReport
    sessionController.resetLastUploadReport();

    // Store objects in request as attributes
    request.setAttribute("Folder", currentFolder);
    request.setAttribute("Path", sessionController.getPath());
    request.setAttribute("IsDownload", sessionController.isDownload());
    request.setAttribute("IsRootPath", sessionController.isRootPath());
    request.setAttribute("IsAllowedNav", sessionController.isAllowedNav());
    request.setAttribute("RootPath", sessionController.getRootPath());
    request.setAttribute("isReadWriteActivated", sessionController.isReadWriteActivated());
    request
        .setAttribute("userAllowedToSetRWAccess", sessionController.checkRWSettingsAccess(false));
    request.setAttribute("userAllowedToLANAccess",
        sessionController.checkUserLANAccess(request.getRemoteAddr()));

    // tables settings (nb files/folders per page)
    request.setAttribute("MaxDirectories", sessionController.getNbMaxDirectoriesByPage());
    request.setAttribute("MaxFiles", sessionController.getNbMaxFilesByPage());

    // return page to redirect to
    return "viewDirectory.jsp";
  }

}
