/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.control.ejb.GalleryBmHome;
import com.silverpeas.gallery.delegate.PhotoDataCreateDelegate;
import com.silverpeas.gallery.model.GalleryRuntimeException;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.ZipManager;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

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
  public void doPost(HttpServletRequest request, HttpServletResponse res)
    throws ServletException, IOException {
    SilverTrace.info("gallery", "GalleryDragAndDrop.doPost", "root.MSG_GEN_ENTER_METHOD");
    try {
      request.setCharacterEncoding("UTF-8");
      String componentId = request.getParameter("ComponentId");
      String albumId = request.getParameter("AlbumId");
      String userId = request.getParameter("UserId");
      SilverTrace.info("gallery", "GalleryDragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE",
        "componentId = " + componentId + " albumId = " + albumId + " userId = " + userId);
      String savePath = FileRepositoryManager.getTemporaryPath() + File.separatorChar + userId
        + System.currentTimeMillis() + File.separatorChar;
      List<FileItem> items = FileUploadUtil.parseRequest(request);
      String parentPath = getParameterValue(items, "userfile_parent");
      SilverTrace.info("gallery", "GalleryDragAndDrop.doPost.doPost",
        "root.MSG_GEN_PARAM_VALUE", "parentPath = " + parentPath);

      SilverTrace.info("gallery", "GalleryDragAndDrop.doPost.doPost",
        "root.MSG_GEN_PARAM_VALUE", "debut de la boucle");
      for (FileItem item : items) {
        if (!item.isFormField()) {
          String fileName = FileUploadUtil.getFileName(item);
          SilverTrace.info("gallery", "GalleryDragAndDrop.doPost.doPost",
            "root.MSG_GEN_PARAM_VALUE", "item = " + item.getFieldName() + " - " + fileName);
          if (fileName != null) {
            SilverTrace.info("gallery", "GalleryDragAndDrop.doPost.doPost",
              "root.MSG_GEN_PARAM_VALUE", "fileName = " + fileName);
            // modifier le nom avant de l'écrire
            File f = new File(savePath + File.separatorChar + fileName);
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
      importRepository(new File(savePath), userId, componentId, albumId);
      FileFolderManager.deleteFolder(savePath);
    } catch (Exception e) {
      SilverTrace.debug("gallery", "GalleryDragAndDrop.doPost.doPost", "root.MSG_GEN_PARAM_VALUE", e);
      res.getOutputStream().println("ERROR");
    }
    res.getOutputStream().println("SUCCESS");
  }

  private void importRepository(final File repository, final String userId, final String componentId,
      final String albumId) throws Exception {
    OrganizationController orga = new OrganizationController();
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
      final PhotoDataCreateDelegate delegate =
          new PhotoDataCreateDelegate(user.getUserPreferences().getLanguage(), albumId);
      delegate.getHeaderData().setDownload(download);
      getGalleryBm().importFromRepository(user, componentId, repository, watermark, watermarkHD,
          watermarkOther, delegate);

    } catch (Exception e) {
      SilverTrace.info("gallery", "GalleryDragAndDrop.importRepository",
          "gallery.MSG_NOT_ADD_METADATA", "message = " + e.getMessage());
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
      return EJBUtilitaire.getEJBObjectRef(JNDINames.GALLERYBM_EJBHOME, GalleryBmHome.class)
          .create();
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryProcessBuilder.getGalleryBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }
}