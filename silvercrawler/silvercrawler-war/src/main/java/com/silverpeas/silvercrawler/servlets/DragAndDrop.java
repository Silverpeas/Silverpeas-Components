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
package com.silverpeas.silvercrawler.servlets;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;
import com.silverpeas.silvercrawler.control.UploadItem;
import com.silverpeas.silvercrawler.control.UploadReport;
import com.stratelia.silverpeas.peasCore.servlets.SilverpeasAuthenticatedHttpServlet;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.silverpeas.servlet.FileUploadUtil;
import org.silverpeas.servlet.HttpRequest;
import org.silverpeas.upload.UploadSession;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * Class declaration
 * @author
 */
public class DragAndDrop extends SilverpeasAuthenticatedHttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  public void init(ServletConfig config) {
    try {
      super.init(config);
    } catch (ServletException se) {
      SilverTrace.fatal("silverCrawler", "DragAndDrop.init", "peasUtil.CANNOT_ACCESS_SUPERCLASS");
    }
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    SilverTrace.info("silverCrawler", "DragAndDrop.doPost", "root.MSG_GEN_ENTER_METHOD");
    HttpRequest request = HttpRequest.decorate(req);
    request.setCharacterEncoding("UTF-8");

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
        SilverTrace.info("importExportPeas", "Drop.doPost", "root.MSG_GEN_PARAM_VALUE",
            "fileName = " + fileName.getName());

        // Save info into report
        UploadItem uploadItem = new UploadItem();
        uploadItem.setFileName(fileName.getName());
        uploadItem.setParentRelativePath(fileName.getParentFile());
        report.addItem(uploadItem);
      }

    } catch (Exception e) {
      SilverTrace.debug("importExportPeas", "Drop.doPost", "root.MSG_GEN_PARAM_VALUE", e);
      throw new ServletException(e);
    } finally {
      uploadSession.clear();
    }
  }
}
