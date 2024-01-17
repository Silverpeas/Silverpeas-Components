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
import org.silverpeas.components.silvercrawler.model.SilverCrawlerForbiddenActionException;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collection;

/**
 * Handler for use case : admin request indexation of selected files.
 * @author Ludovic Bertin
 */
public class IndexSelectedFilesHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController,
      HttpServletRequest request) throws Exception {

    // checks that users has admin profile
    String userHighestRole = getUserHighestRole(sessionController);
    if (!userHighestRole.equals("admin")) {
      throw new SilverCrawlerForbiddenActionException("User is not manager of this component");
    }

    // retrieves file list
    String[] selectedFiles = request.getParameterValues("checkedFile");
    if (selectedFiles != null) {
      Collection<String> listFilesToIndex = Arrays.asList(selectedFiles);


      // index selected folders
      sessionController.indexSelectedFiles(listFilesToIndex);
    }

    // redirect to "ViewDirectory" use case
    return HandlerProvider.getHandler("ViewDirectory")
        .computeDestination(sessionController, request);
  }
}
