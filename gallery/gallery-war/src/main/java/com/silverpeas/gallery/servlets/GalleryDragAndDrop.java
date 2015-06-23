/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.servlets;

import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.delegate.MediaDataCreateDelegate;
import com.silverpeas.gallery.model.GalleryRuntimeException;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.ZipManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.cache.service.CacheServiceFactory;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.servlet.FileUploadUtil;
import org.silverpeas.servlet.HttpRequest;
import org.silverpeas.web.util.SilverpeasTransverseWebErrorUtil;

import javax.ejb.EJBException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.silverpeas.util.StringUtil.isDefined;

/**
 * Class declaration
 *
 *
 * @author
 */
public class GalleryDragAndDrop extends HttpServlet {

  private static final long serialVersionUID = -3063286463794353943L;

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
    String userId = null;
    HttpRequest request = HttpRequest.decorate(req);
    try {
      request.setCharacterEncoding("UTF-8");
      String componentId = request.getParameter("ComponentId");
      String albumId = request.getParameter("AlbumId");
      userId = request.getParameter("UserId");
      SilverTrace.info("gallery", "GalleryDragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE",
        "componentId = " + componentId + " albumId = " + albumId + " userId = " + userId);

      String savePath = FileRepositoryManager.getTemporaryPath()  + "tmpupload"
          + File.separatorChar + albumId + System.currentTimeMillis() + File.separatorChar;

      List<FileItem> items = request.getFileItems();

      for (FileItem item : items) {
        if (!item.isFormField()) {
          String fileUploadId = item.getFieldName().substring(4);
          String parentPath = FileUploadUtil.getParameter(items, "relpathinfo" + fileUploadId, null);
          String fileName = FileUploadUtil.getFileName(item);
          if (StringUtil.isDefined(parentPath)) {
            if (parentPath.endsWith(":\\")) { // special case for file on root of disk
              parentPath = parentPath.substring(0, parentPath.indexOf(':') + 1);
            }
          }
          parentPath = FileUtil.convertPathToServerOS(parentPath);
          SilverTrace.info("gallery", "GalleryDragAndDrop.doPost.doPost",
            "root.MSG_GEN_PARAM_VALUE", "item = " + item.getFieldName() + " - " + fileName);
          if (fileName != null) {
            if (fileName.contains(File.separator)) {
              fileName = fileName.substring(fileName.lastIndexOf(File.separatorChar));
              parentPath = parentPath + File.separatorChar + fileName.substring(0, fileName.
                  lastIndexOf(File.separatorChar));
            }
            SilverTrace.info("gallery", "GalleryDragAndDrop.doPost.doPost",
              "root.MSG_GEN_PARAM_VALUE", "fileName on Unix = " + fileName);

            if (parentPath != null && parentPath.length() > 0) {
              fileName = File.separatorChar + parentPath + File.separatorChar + fileName;
            }

            if (!"".equals(savePath)) {
              File f = new File(savePath + fileName);
              File parent = f.getParentFile();
              if (!parent.exists()) {
                parent.mkdirs();
              }
              item.write(f);

              // Cas du zip
              if (FileUtil.isArchive(fileName)) {
                ZipManager.extract(f, parent);
              }
            }
          }
        }
      }
      importRepository(new File(savePath), userId, componentId, albumId);
      FileFolderManager.deleteFolder(savePath);
    } catch (Exception e) {
      SilverTrace.debug("gallery", "GalleryDragAndDrop.doPost.doPost", "root.MSG_GEN_PARAM_VALUE",
          e);
      final StringBuilder sb = new StringBuilder("ERROR");
      final String errorMessage = SilverpeasTransverseWebErrorUtil
          .performAppletAlertExceptionMessage(e,
              UserDetail.getById(userId).getUserPreferences().getLanguage());
      if (isDefined(errorMessage)) {
        sb.append(": ");
        sb.append(errorMessage);
      }
      res.getOutputStream().println(sb.toString());
    }
    res.getOutputStream().println("SUCCESS");
  }

  private void importRepository(final File repository, final String userId, final String componentId,
      final String albumId) throws Exception {
    OrganisationController orga = new OrganizationController();
    boolean watermark =
        "yes".equalsIgnoreCase(orga.getComponentParameterValue(componentId, "watermark"));
    boolean download =
        !"no".equalsIgnoreCase(orga.getComponentParameterValue(componentId, "download"));
    String watermarkHD = orga.getComponentParameterValue(componentId, "WatermarkHD");
    if (!StringUtil.isInteger(watermarkHD)) {
      watermarkHD = "";
    }
    String watermarkOther = orga.getComponentParameterValue(componentId, "WatermarkOther");
    if (!StringUtil.isInteger(watermarkOther)) {
      watermarkOther = "";
    }

    try {

      final UserDetail user = UserDetail.getById(userId);
      CacheServiceFactory.getSessionCacheService().put(UserDetail.CURRENT_REQUESTER_KEY, user);
      final MediaDataCreateDelegate delegate =
          new MediaDataCreateDelegate(null, user.getUserPreferences().getLanguage(),
              albumId);
      delegate.getHeaderData().setDownloadAuthorized(download);
      getGalleryBm().importFromRepository(user, componentId, repository, watermark, watermarkHD,
          watermarkOther, delegate);

    } catch (Exception e) {
      SilverTrace.info("gallery", "GalleryDragAndDrop.importRepository",
          "gallery.MSG_NOT_ADD_METADATA", "message = " + e.getMessage());
      if (e instanceof EJBException) {
        throw e;
      }
    }
  }

  private String getParameterValue(List<FileItem> items, String parameterName) {
    for (FileItem item : items) {
      if (item.isFormField() && parameterName.equals(item.getFieldName())) {
        return item.getString();
      }
    }
    return null;
  }

  /**
   * Gets the GalleryBm EJB proxy
   * @return
   */
  private static GalleryBm getGalleryBm() {
    try {
      return EJBUtilitaire.getEJBObjectRef(JNDINames.GALLERYBM_EJBHOME, GalleryBm.class);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryProcessBuilder.getGalleryBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }
}