/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.silvercrawler.servlets;

import com.silverpeas.session.SessionInfo;
import com.silverpeas.session.SessionManagement;
import com.silverpeas.session.SessionManagementFactory;
import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;
import com.silverpeas.silvercrawler.control.UploadItem;
import com.silverpeas.silvercrawler.control.UploadReport;
import com.silverpeas.util.StringUtil;
import org.silverpeas.servlet.FileUploadUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.silverpeas.servlet.HttpRequest;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Class declaration
 * @author
 */
public class DragAndDrop extends HttpServlet {

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

    if (!request.isContentInMultipart()) {
      res.getOutputStream().println("SUCCESS");
      return;
    }

    try {
      String sessionId = request.getParameter("SessionId");
      String instanceId = request.getParameter("ComponentId");
      String ignoreFolders = request.getParameter("IgnoreFolders");

      SessionManagementFactory factory = SessionManagementFactory.getFactory();
      SessionManagement sessionManagement = factory.getSessionManagement();
      SessionInfo session = sessionManagement.getSessionInfo(sessionId);

      SilverCrawlerSessionController sessionController =
          session.getAttribute("Silverpeas_SilverCrawler_" + instanceId);

      // build report
      UploadReport report = sessionController.getLastUploadReport();
      if (report == null) {
        report = new UploadReport();
        sessionController.setLastUploadReport(report);
      }

      // if first part of upload, needs to generate temporary path
      File savePath = report.getRepositoryPath();
      if (report.getRepositoryPath() == null) {
        savePath = FileUtils.getFile(FileRepositoryManager.getTemporaryPath(), "tmpupload",
            ("SilverWrawler_" + System.currentTimeMillis()));
        report.setRepositoryPath(savePath);
      }

      // Loop items
      List<FileItem> items = request.getFileItems();
      for (FileItem item : items) {
        if (!item.isFormField()) {
          String fileUploadId = item.getFieldName().substring(4);
          String unixParentPath = FilenameUtils.separatorsToUnix(
              FileUploadUtil.getParameter(items, "relpathinfo" + fileUploadId, null));
          File parentPath = FileUtils.getFile(unixParentPath);

          // if ignoreFolder is activated, no folders are permitted
          if (StringUtil.isDefined(parentPath.getName()) &&
              StringUtil.getBooleanValue(ignoreFolders)) {
            report.setFailed(true);
            report.setForbiddenFolderDetected(true);
            break;
          }

          // Get the file path and name
          File fileName = FileUtils.getFile(parentPath, FileUploadUtil.getFileName(item));

          // Logging the name of the file
          SilverTrace.info("importExportPeas", "Drop.doPost", "root.MSG_GEN_PARAM_VALUE",
              "fileName = " + fileName.getName());

          // Registering in the temporary location
          File fileToSave = FileUtils.getFile(savePath, fileName.getPath());
          fileToSave.getParentFile().mkdirs();
          item.write(fileToSave);

          // Save info into report
          UploadItem uploadItem = new UploadItem();
          uploadItem.setFileName(fileName.getName());
          uploadItem.setParentRelativePath(fileName.getParentFile());
          report.addItem(uploadItem);
        } else {
          SilverTrace.info("importExportPeas", "Drop.doPost", "root.MSG_GEN_PARAM_VALUE",
              "item = " + item.getFieldName() + " - " + item.getString());
        }
      }

    } catch (Exception e) {
      SilverTrace.debug("importExportPeas", "Drop.doPost", "root.MSG_GEN_PARAM_VALUE", e);
      res.getOutputStream().println("ERROR");
      return;
    }
    res.getOutputStream().println("SUCCESS");
  }
}
