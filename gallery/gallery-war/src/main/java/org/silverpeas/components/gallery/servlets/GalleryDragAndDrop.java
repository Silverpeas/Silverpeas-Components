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
package org.silverpeas.components.gallery.servlets;

import org.silverpeas.components.gallery.delegate.MediaDataCreateDelegate;
import org.silverpeas.components.gallery.service.GalleryService;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.io.upload.UploadSession;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.ZipUtil;
import org.silverpeas.core.util.error.SilverpeasTransverseErrorUtil;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.webcomponent.SilverpeasAuthenticatedHttpServlet;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

/**
 * Class declaration
 */
public class GalleryDragAndDrop extends SilverpeasAuthenticatedHttpServlet {

  private static final long serialVersionUID = -3063286463794353943L;

  @Inject
  private OrganizationController organizationController;

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) {
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

      for (File filePath : uploadSession.getRootFolderFiles()) {
        // Cas du zip
        if (FileUtil.isArchive(filePath.getName())) {
          ZipUtil.extract(filePath, filePath.getParentFile());
        }
      }

      importRepository(uploadSession.getRootFolder(), userDetail.getId(), componentId, albumId);

    } catch (Exception e) {
      SilverpeasTransverseErrorUtil
          .throwTransverseErrorIfAny(e, userDetail.getUserPreferences().getLanguage());
    } finally {
      uploadSession.clear();
    }
  }

  private void importRepository(final File repository, final String userId,
      final String componentId, final String albumId)  {
    boolean download = !"no".equalsIgnoreCase(
        organizationController.getComponentParameterValue(componentId, "download"));

    try {

      final UserDetail user = UserDetail.getById(userId);
      final MediaDataCreateDelegate delegate =
          new MediaDataCreateDelegate(null, user.getUserPreferences().getLanguage(), albumId);
      delegate.getHeaderData().setDownloadAuthorized(download);
      getGalleryService()
          .importFromRepository(user, componentId, repository, delegate);

    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  /**
   * Gets the GalleryService Service
   * @return a GalleryService
   */
  private static GalleryService getGalleryService() {
    return ServiceProvider.getService(GalleryService.class);
  }
}