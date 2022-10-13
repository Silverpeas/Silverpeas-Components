/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.components.silvercrawler.servlets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.silverpeas.components.silvercrawler.statistic.Statistic;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.exception.RelativeFileAccessException;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.webcomponent.SilverpeasAuthenticatedHttpServlet;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

import static org.silverpeas.core.web.http.FileResponse.encodeAttachmentFilenameAsUtf8;

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
      SilverLogger.getLogger(this).error("SilverCrawler File Server Initialization Failure", se);
    }
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
    String sourceFile = FileUtil.verifyTaintedData(req.getParameter("SourceFile"));
    String componentId = req.getParameter("ComponentId");
    String typeUpload = req.getParameter("TypeUpload");
    String path = FileUtil.verifyTaintedData(req.getParameter("Path"));

    try {
      FileUtil.assertPathNotRelative(sourceFile);
    } catch (RelativeFileAccessException e) {
      SilverLogger.getLogger(this).warn(e);
      throwHttpForbiddenError();
    }

    MainSessionController mainSessionCtrl = getMainSessionController(req);
    String userId = mainSessionCtrl.getUserId();

    // Check user rights on identified component
    if (!OrganizationControllerProvider.getOrganisationController()
        .isComponentAvailableToUser(componentId, userId)) {
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
      if (sourceFile.startsWith(FilenameUtils.separatorsToUnix(rootPath.getPath()))) {
        //Path into index is stored absolute and with Unix separators
        fileStat = fileToSend = FileUtils.getFile(sourceFile);
      } else {
        fileStat = fileToSend = FileUtils.getFile(rootPath, sourceFile);
      }
    } else {
      type = Statistic.DIRECTORY;
      fileToSend =
          FileUtils.getFile(FileRepositoryManager.getTemporaryPath(), sourceFile);
      fileStat = FileUtils.getFile(rootPath, path);
    }

    sendFile(res, fileToSend);

    // ajout dans la table des téléchargements
    Statistic.addStat(userId, fileStat, componentId, type);
  }

  private void sendFile(HttpServletResponse response, File file) throws IOException {
    response.setHeader("Content-Length", String.valueOf(file.length()));
    final String normalizedFilename = StringUtil.normalize(file.getName());
    response.setContentType(FileUtil.getMimeType(normalizedFilename));
    response.setHeader("Content-Disposition", encodeAttachmentFilenameAsUtf8(normalizedFilename));
    try {
      FileUtils.copyFile(file, response.getOutputStream());
      response.getOutputStream().flush();
    } catch (IOException e) {
      SilverLogger.getLogger(this)
          .error("Cannot send file {0}", new String[]{file.getAbsolutePath()}, e);
      displayWarningHtmlCode(response);
    }
  }

  private void displayWarningHtmlCode(HttpServletResponse res) throws IOException {
    LocalizationBundle messages = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.util.peasUtil.multiLang.fileServerBundle");

    try(OutputStream out = res.getOutputStream();
        StringReader sr = new StringReader(messages.getString("warning"))) {
      IOUtils.copy(sr, out, Charsets.UTF_8);
      out.flush();
    } catch (IOException e) {
      SilverLogger.getLogger(this).error("Error while displaying warning HTML Code", e);
    }
  }
}