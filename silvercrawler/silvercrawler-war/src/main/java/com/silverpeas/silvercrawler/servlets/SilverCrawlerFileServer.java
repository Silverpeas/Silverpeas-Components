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
package com.silverpeas.silvercrawler.servlets;

import com.silverpeas.silvercrawler.statistic.Statistic;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.SilverpeasAuthenticatedHttpServlet;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.silverpeas.core.admin.OrganizationController;
import org.silverpeas.core.admin.OrganizationControllerProvider;
import org.silverpeas.util.FileRepositoryManager;
import org.silverpeas.util.FileUtil;
import org.silverpeas.util.LocalizationBundle;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.util.exception.RelativeFileAccessException;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

/**
 * Class declaration
 * @author
 */
public class SilverCrawlerFileServer extends SilverpeasAuthenticatedHttpServlet {

  private static final long serialVersionUID = 4892517833096053490L;

  @Inject
  private OrganizationController organizationController;

  @Override
  public void init(ServletConfig config) {
    try {
      super.init(config);
    } catch (ServletException se) {
      SilverTrace.fatal("silverCrawler", "FileServer.init()", "peasUtil.CANNOT_ACCESS_SUPERCLASS");
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
    SilverTrace.info("silverCrawler", "FileServer.doPost()", "root.MSG_GEN_ENTER_METHOD");
    String sourceFile = req.getParameter("SourceFile");
    String componentId = req.getParameter("ComponentId");
    String typeUpload = req.getParameter("TypeUpload");
    String path = req.getParameter("Path");

    try {
      FileUtil.checkPathNotRelative(sourceFile);
    } catch (RelativeFileAccessException e) {
      throwHttpForbiddenError();
    }

    MainSessionController mainSessionCtrl = getMainSessionController(req);
    String userId = mainSessionCtrl.getUserId();

    // Check user rights on identified component
    if (!OrganizationControllerProvider.getOrganisationController()
        .isComponentAvailable(componentId, userId)) {
      throwHttpForbiddenError();
    }

    File rootPath = FileUtils
        .getFile(organizationController.getComponentParameterValue(componentId, "directory"));

    // 2 cas :
    // - téléchargement d'un zip dans répertoire temporaire
    // - téléchargement d'un fichier depuis le répertoire crawlé
    File fileToSend;
    String type;
    File fileStat;
    if ("link".equals(typeUpload)) {
      type = Statistic.FILE;
      fileStat = fileToSend = FileUtils.getFile(rootPath, sourceFile);
      SilverTrace.debug("silverCrawler", "FileServer", "root.MSG_GEN_PARAM_VALUE",
          "file, type = " + type + " file = " + fileStat);
    } else {
      type = Statistic.DIRECTORY;
      fileToSend =
          FileUtils.getFile(FileRepositoryManager.getTemporaryPath(null, componentId), sourceFile);
      fileStat = FileUtils.getFile(rootPath, path);
      SilverTrace.debug("silverCrawler", "FileServer", "root.MSG_GEN_PARAM_VALUE",
          "directory, type = " + type + " file = " + fileStat);
    }

    sendFile(res, fileToSend);

    // ajout dans la table des téléchargements
    Statistic.addStat(userId, fileStat, componentId, type);
    SilverTrace.info("silverCrawler", "FileServer.doPost()", "root.MSG_GEN_ENTER_METHOD",
        " addStat : fileStat = " + fileStat);
  }

  private void sendFile(HttpServletResponse response, File file) throws IOException {
    response.setContentType(FileUtil.getMimeType(file.getName()));
    response.setHeader("Content-Length", String.valueOf(file.length()));
    response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
    SilverTrace.debug("peasUtil", "SilverCrawlerFileServer.sendFile()", "root.MSG_GEN_ENTER_METHOD",
        " file: " + file.getAbsolutePath());
    try {
      FileUtils.copyFile(file, response.getOutputStream());
      response.getOutputStream().flush();
      SilverTrace
          .debug("peasUtil", "SilverCrawlerFileServer.sendFile()", "root.MSG_GEN_ENTER_METHOD",
              " File was sent");
    } catch (IOException e) {
      SilverTrace.error("peasUtil", "SilverCrawlerFileServer.sendFile", "root.EX_CANT_READ_FILE",
          " file: " + file.getAbsolutePath(), e);
      displayWarningHtmlCode(response);
    }
  }

  private void displayWarningHtmlCode(HttpServletResponse res) throws IOException {
    LocalizationBundle messages = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.util.peasUtil.multiLang.fileServerBundle");

    OutputStream out = res.getOutputStream();
    StringReader sr = new StringReader(messages.getString("warning"));
    try {
      IOUtils.copy(sr, out);
      out.flush();
    } catch (IOException e) {
      SilverTrace
          .warn("silverCrawler", "FileServer.displayWarningHtmlCode", "root.EX_CANT_READ_FILE",
              "warning properties");
    } finally {
      IOUtils.closeQuietly(sr);
      IOUtils.closeQuietly(out);
    }
  }
}