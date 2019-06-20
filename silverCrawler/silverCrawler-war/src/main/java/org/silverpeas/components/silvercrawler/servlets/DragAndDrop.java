/*
 * Copyright (C) 2000 - 2019 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.components.silvercrawler.servlets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.silverpeas.components.silvercrawler.control.SilverCrawlerSessionController;
import org.silverpeas.components.silvercrawler.control.UploadItem;
import org.silverpeas.components.silvercrawler.control.UploadReport;
import org.silverpeas.core.io.upload.UploadSession;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.webcomponent.SilverpeasAuthenticatedHttpServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Class declaration
 * @author
 */
public class DragAndDrop extends SilverpeasAuthenticatedHttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) {

    HttpRequest request = HttpRequest.decorate(req);
    try {
      request.setCharacterEncoding("UTF-8");
    } catch (UnsupportedEncodingException e) {
      SilverLogger.getLogger(this).error(e);
      try {
        res.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      } catch (IOException e1) {
        SilverLogger.getLogger(this).error(e1);
      }
    }

    UploadSession uploadSession = UploadSession.from(request);

    try {
      String componentId = request.getParameter("ComponentId");
      boolean ignoreFolders = request.getParameterAsBoolean("IgnoreFolders");

      if (!uploadSession.isUserAuthorized(componentId)) {
        throwHttpForbiddenError();
      }

      SilverCrawlerSessionController sessionController =
          getSessionInfo(request).getAttribute("Silverpeas_SilverCrawler_" + componentId);

      // build report
      UploadReport report = sessionController.getLastUploadReport();
      if (report == null) {
        report = new UploadReport();
        sessionController.setLastUploadReport(report);
      }

      File rootUploadFolder = new File(uploadSession.getRootFolder().getParentFile(),
          "SilverCrawler_" + uploadSession.getRootFolder().getName());
      int rootUploadFolderPathLength = rootUploadFolder.getPath().length();
      FileUtils.moveDirectory(uploadSession.getRootFolder(), rootUploadFolder);

      // Setting into report instance the path into which the files have been uploaded
      report.setRepositoryPath(rootUploadFolder);

      if (ignoreFolders) {
        // Verifying that is does not exist folders
        File[] folders =
            rootUploadFolder.listFiles((FileFilter) FileFilterUtils.directoryFileFilter());
        if (folders != null && folders.length > 0) {
          report.setFailed(true);
          report.setForbiddenFolderDetected(true);
          return;
        }
      }

      // Loop items
      for (File file : FileUtils.listFiles(rootUploadFolder, FileFilterUtils.fileFileFilter(),
          FileFilterUtils.trueFileFilter())) {

        // Get the file path and name
        File fileName = new File(file.getPath().substring(rootUploadFolderPathLength));

        // Logging the name of the file


        // Save info into report
        UploadItem uploadItem = new UploadItem();
        uploadItem.setFileName(fileName.getName());
        uploadItem.setParentRelativePath(fileName.getParentFile());
        report.addItem(uploadItem);
      }

    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
      try {
        res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
      } catch (IOException e1) {
        SilverLogger.getLogger(this).error(e1);
      }
    } finally {
      uploadSession.clear();
    }
  }
}
