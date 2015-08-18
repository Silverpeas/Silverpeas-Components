/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.servlets;

import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.delegate.MediaDataCreateDelegate;
import com.silverpeas.gallery.model.GalleryRuntimeException;
import com.stratelia.silverpeas.peasCore.servlets.SilverpeasAuthenticatedHttpServlet;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import org.silverpeas.cache.service.CacheServiceProvider;
import org.silverpeas.core.admin.OrganizationController;
import org.silverpeas.servlet.HttpRequest;
import org.silverpeas.upload.UploadSession;
import org.silverpeas.util.error.SilverpeasTransverseErrorUtil;

import javax.ejb.EJBException;
import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * Class declaration
 * @author
 */
public class GalleryDragAndDrop extends SilverpeasAuthenticatedHttpServlet {

  private static final long serialVersionUID = -3063286463794353943L;

  @Inject
  private OrganizationController organizationController;

  @Override
  public void init(ServletConfig config) {
    try {
      super.init(config);
    } catch (ServletException se) {
      SilverTrace.fatal("importExportPeas", "ImportDragAndDrop.init",
          "peasUtil.CANNOT_ACCESS_SUPERCLASS");
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
    SilverTrace.info("gallery", "GalleryDragAndDrop.doPost", "root.MSG_GEN_ENTER_METHOD");
    UserDetail userDetail = UserDetail.getCurrentRequester();
    HttpRequest request = HttpRequest.decorate(req);
    UploadSession uploadSession = UploadSession.from(request);
    try {
      request.setCharacterEncoding("UTF-8");
      String componentId = request.getParameter("ComponentId");
      String albumId = request.getParameter("AlbumId");

      if (!uploadSession.isUserAuthorized(componentId)) {
        throwHttpForbiddenError();
      }

      SilverTrace.info("gallery", "GalleryDragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE",
          "componentId = " + componentId + " albumId = " + albumId + " userId = " +
              userDetail.getId());

      for (File filePath : uploadSession.getRootFolderFiles()) {

        SilverTrace.info("gallery", "GalleryDragAndDrop.doPost.doPost", "root.MSG_GEN_PARAM_VALUE",
            "full path = " + filePath);

        // Cas du zip
        if (FileUtil.isArchive(filePath.getName())) {
          ZipManager.extract(filePath, filePath.getParentFile());
        }
      }

      importRepository(uploadSession.getRootFolder(), userDetail.getId(), componentId, albumId);

    } catch (Exception e) {
      SilverTrace
          .debug("gallery", "GalleryDragAndDrop.doPost.doPost", "root.MSG_GEN_PARAM_VALUE", e);
      SilverpeasTransverseErrorUtil
          .throwTransverseErrorIfAny(e, userDetail.getUserPreferences().getLanguage());
    } finally {
      uploadSession.clear();
    }
  }

  private void importRepository(final File repository, final String userId,
      final String componentId, final String albumId) throws Exception {
    boolean watermark = "yes".equalsIgnoreCase(
        organizationController.getComponentParameterValue(componentId, "watermark"));
    boolean download = !"no".equalsIgnoreCase(
        organizationController.getComponentParameterValue(componentId, "download"));
    String watermarkHD =
        organizationController.getComponentParameterValue(componentId, "WatermarkHD");
    if (!StringUtil.isInteger(watermarkHD)) {
      watermarkHD = "";
    }
    String watermarkOther =
        organizationController.getComponentParameterValue(componentId, "WatermarkOther");
    if (!StringUtil.isInteger(watermarkOther)) {
      watermarkOther = "";
    }

    try {

      final UserDetail user = UserDetail.getById(userId);
      CacheServiceProvider.getSessionCacheService().put(UserDetail.CURRENT_REQUESTER_KEY, user);
      final MediaDataCreateDelegate delegate =
          new MediaDataCreateDelegate(null, user.getUserPreferences().getLanguage(), albumId);
      delegate.getHeaderData().setDownloadAuthorized(download);
      getGalleryBm().importFromRepository(user, componentId, repository, watermark, watermarkHD,
          watermarkOther, delegate);

    } catch (Exception e) {
      SilverTrace
          .info("gallery", "GalleryDragAndDrop.importRepository", "gallery.MSG_NOT_ADD_METADATA",
              "message = " + e.getMessage());
      if (e instanceof EJBException) {
        throw e;
      }
    }
  }

  /**
   * Gets the GalleryBm Service
   * @return a GalleryBm
   */
  private static GalleryBm getGalleryBm() {
    try {
      return ServiceProvider.getService(GalleryBm.class);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryProcessBuilder.getGalleryBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }
}