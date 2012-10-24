/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;
import com.silverpeas.silvercrawler.control.UploadItem;
import com.silverpeas.silvercrawler.control.UploadReport;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.peasCore.SessionManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;

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
      SilverTrace.fatal("silverCrawler", "DragAndDrop.init",
          "peasUtil.CANNOT_ACCESS_SUPERCLASS");
    }


  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse res)
      throws ServletException, IOException {
    SilverTrace.info("silverCrawler", "DragAndDrop.doPost", "root.MSG_GEN_ENTER_METHOD");
    request.setCharacterEncoding("UTF-8");
    if (!FileUploadUtil.isRequestMultipart(request)) {
      res.getOutputStream().println("SUCCESS");
      return;
    }

    try {
      String sessionId = request.getParameter("SessionId");
      String instanceId = request.getParameter("ComponentId");
      String ignoreFolders = request.getParameter("IgnoreFolders");

      SessionInfo session = SessionManager.getInstance().getSessionInfo(sessionId);
      SilverCrawlerSessionController sessionController = session.getAttribute("Silverpeas_SilverCrawler_"+instanceId);


      String userId = sessionController.getUserId();

      // build report
      UploadReport report = sessionController.getLastUploadReport();
      if (report == null) {
        report = new UploadReport();
        sessionController.setLastUploadReport(report);
      }

      // if first part of upload, needs to generate temporary path
      String savePath = report.getRepositoryPath();
      if (savePath == null) {
        savePath = FileRepositoryManager.getTemporaryPath() + "tmpupload" + File.separator + "SilverWrawler_" + System.currentTimeMillis() + File.separator;
        report.setRepositoryPath(savePath);
      }

      // Loop items
      List<FileItem> items = FileUploadUtil.parseRequest(request);
      for (FileItem item : items) {
        if (!item.isFormField()) {
          String fileUploadId = item.getFieldName().substring(4);
          String parentPath = FileUploadUtil.getParameter(items, "relpathinfo" + fileUploadId, null);
          String fileName = FileUploadUtil.getFileName(item);

          // if ignoreFolder is activated, no folders are permitted
          if ( (StringUtil.isDefined(parentPath)) && ("1".equals(ignoreFolders)) ) {
            report.setFailed(true);
            report.setForbiddenFolderDetected(true);
            break;
          }

          if (StringUtil.isDefined(parentPath)) {
            if (parentPath.endsWith(":\\")) { // special case for file on root of disk
              parentPath = parentPath.substring(0, parentPath.indexOf(':') + 1);
            }
          }

          SilverTrace.info("importExportPeas", "Drop.doPost",
              "root.MSG_GEN_PARAM_VALUE", "fileName = " + fileName);

          if (fileName != null) {
            if (fileName.indexOf(File.separatorChar) >= 0) {
              fileName = fileName.substring(fileName.lastIndexOf(File.separatorChar));
              parentPath = parentPath + File.separatorChar +
                      fileName.substring(0, fileName.lastIndexOf(File.separatorChar));
            }
            SilverTrace.info("importExportPeas", "Drop.doPost",
                "root.MSG_GEN_PARAM_VALUE", "fileName on Unix = " + fileName);
          }

          if (!"1".equals(ignoreFolders)) {
            fileName = File.separatorChar + parentPath + File.separatorChar + fileName;
          }

          if (!"".equals(savePath)) {
            File f = new File(savePath + fileName);
            File parent = f.getParentFile();
            if (!parent.exists()) {
              parent.mkdirs();
            }
            item.write(f);

            // save info into report
            UploadItem uploadItem = new UploadItem();
            uploadItem.setFileName(fileName);
            uploadItem.setParentPath(parentPath);
            report.addItem(uploadItem);
          }
        } else {
          SilverTrace.info("importExportPeas", "Drop.doPost", "root.MSG_GEN_PARAM_VALUE", "item = "
              + item.getFieldName() + " - " + item.getString());
        }
      }

    }

    catch (Exception e) {
      SilverTrace.debug("importExportPeas", "FileUploader.doPost", "root.MSG_GEN_PARAM_VALUE", e);
      res.getOutputStream().println("ERROR");
      return;
    }
    res.getOutputStream().println("SUCCESS");
  }
}
