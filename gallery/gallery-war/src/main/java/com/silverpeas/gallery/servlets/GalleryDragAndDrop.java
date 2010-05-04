/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.servlets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.gallery.ImageHelper;
import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.control.ejb.GalleryBmHome;
import com.silverpeas.gallery.model.AlbumDetail;
import com.silverpeas.gallery.model.GalleryRuntimeException;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.model.PhotoPK;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.ZipManager;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

/**
 * Class declaration
 *
 *
 * @author
 */
public class GalleryDragAndDrop extends HttpServlet {

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
    SilverTrace.info("gallery", "GalleryDragAndDrop.doPost",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      String componentId = request.getParameter("ComponentId");
      String albumId = request.getParameter("AlbumId");
      String userId = request.getParameter("UserId");
      SilverTrace.info("gallery", "GalleryDragAndDrop.doPost",
          "root.MSG_GEN_PARAM_VALUE", "componentId = " + componentId
          + " albumId = " + albumId + " userId = " + userId);
      String savePath = FileRepositoryManager.getTemporaryPath() + File.separator + userId
          + new Date().getTime() + File.separator;

      List<FileItem> items = FileUploadUtil.parseRequest(request);

      String parentPath = getParameterValue(items, "userfile_parent");
      SilverTrace.info("gallery", "GalleryDragAndDrop.doPost.doPost",
          "root.MSG_GEN_PARAM_VALUE", "parentPath = " + parentPath);

      SilverTrace.info("gallery", "GalleryDragAndDrop.doPost.doPost",
          "root.MSG_GEN_PARAM_VALUE", "debut de la boucle");

      for (FileItem item : items) {
        if (!item.isFormField()) {
          String fileName = item.getName();
          SilverTrace.info("gallery", "GalleryDragAndDrop.doPost.doPost",
              "root.MSG_GEN_PARAM_VALUE", "item = "
              + item.getFieldName() + " - " + fileName);
          if (fileName != null) {
            fileName = fileName.replace('\\', File.separatorChar);
            fileName = fileName.replace('/', File.separatorChar);
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
            if (FileUtil.ARCHIVE_MIME_TYPE.equals(FileUtil.getMimeType(fileName))) {
              ZipManager.extract(f, parent);
            }
          }
        }
      }
      importRepository(new File(savePath), userId, componentId, albumId);
      FileFolderManager.deleteFolder(savePath);
    } catch (Exception e) {
      SilverTrace.debug("gallery", "GalleryDragAndDrop.doPost.doPost",
          "root.MSG_GEN_PARAM_VALUE", e);
      res.getOutputStream().println("ERROR");
    }
    res.getOutputStream().println("SUCCESS");
  }

  private void importRepository(File dir, String userId, String componentId, String albumId)
      throws Exception {
    OrganizationController orga = new OrganizationController();
    boolean watermark = "yes".equalsIgnoreCase(orga.getComponentParameterValue(componentId, "watermark"));
    boolean download = !"no".equalsIgnoreCase(orga.getComponentParameterValue(componentId, "download"));
    String watermarkHD = orga.getComponentParameterValue(componentId, "WatermarkHD");
    String watermarkOther = orga.getComponentParameterValue(componentId, "WatermarkOther");
    importRepository(dir, userId, componentId, albumId, watermark, watermarkHD,
        watermarkOther, download);
  }

  private void importRepository(File dir, String userId, String componentId,
      String albumId, boolean watermark, String watermarkHD,
      String watermarkOther, boolean download) throws Exception {
    Iterator itPathContent = getPathContent(dir);
    while (itPathContent.hasNext()) {
      File file = (File) itPathContent.next();
      if (file.isFile()) {
        if (ImageHelper.isImage(file.getName())) {
          try {
            createPhoto(file.getName(), userId, componentId, albumId, file,
                watermark, watermarkHD, watermarkOther, download);
          } catch (Exception e) {
            SilverTrace.info("gallery", "GalleryDragAndDrop.importRepository",
                "gallery.MSG_NOT_ADD_METADATA", "photo =  " + file.getName());
          }
        }
      } else if (file.isDirectory()) {
        String newAlbumId = createAlbum(file.getName(), userId, componentId,
            albumId);
        // Traitement récursif spécifique
        importRepository(file.getAbsoluteFile(), userId, componentId,
            newAlbumId, watermark, watermarkHD, watermarkOther, download);
      }
    }
  }

  private Iterator getPathContent(File path) {
    // Récupération du contenu du dossier
    List listFile = new ArrayList();

    String[] listFileName = path.list();
    for (int i = 0; i < listFileName.length; i++) {
      listFile.add(new File(path + File.separator + listFileName[i]));
    }

    return listFile.iterator();
  }

  private String createAlbum(String name, String userId, String componentId,
      String fatherId) throws Exception {
    SilverTrace.info("gallery", "GalleryDragAndDrop.createAlbum",
        "root.MSG_GEN_ENTER_METHOD", "name = " + name + ", fatherId = " + fatherId);

    // création de l'album (avec le nom du répertoire) une seule fois
    NodeDetail node = new NodeDetail("unknown", name, null, null, null, null, "0", "unknown");
    AlbumDetail album = new AlbumDetail(node);
    album.setCreationDate(DateUtil.date2SQLDate(new Date()));
    album.setCreatorId(userId);
    album.getNodePK().setComponentName(componentId);
    NodePK nodePK = new NodePK(fatherId, componentId);
    NodePK newNodePK = getGalleryBm().createAlbum(album, nodePK);
    String newAlbumId = newNodePK.getId();

    return newAlbumId;
  }

  private String createPhoto(String name, String userId, String componentId,
      String albumId, File file, boolean watermark, String watermarkHD,
      String watermarkOther, boolean download)
      throws Exception {
    SilverTrace.info("gallery", "GalleryDragAndDrop.createPhoto",
        "root.MSG_GEN_ENTER_METHOD", "name = " + name + ", fatherId = "
        + albumId);

    // création de la photo
    PhotoDetail newPhoto = new PhotoDetail(name, null, new Date(), null, null,
        null, download, false);
    newPhoto.setAlbumId(albumId);
    newPhoto.setCreatorId(userId);
    PhotoPK pk = new PhotoPK("unknown", componentId);
    newPhoto.setPhotoPK(pk);

    String photoId = getGalleryBm().createPhoto(newPhoto, albumId);
    newPhoto.getPhotoPK().setId(photoId);

    // Création de la preview et des vignettes sur disque
    ImageHelper.processImage(newPhoto, file, watermark, watermarkHD,
        watermarkOther);
    try {
      ImageHelper.setMetaData(newPhoto, "fr");
    } catch (Exception e) {
      SilverTrace.info("gallery", "GalleryDragAndDrop.createPhoto",
          "gallery.MSG_NOT_ADD_METADATA", "photoId =  " + photoId);
    }
    // Modification de la photo pour mise à jour dimension
    getGalleryBm().updatePhoto(newPhoto);
    return photoId;
  }

  private String getParameterValue(List items, String parameterName) {
    Iterator iter = items.iterator();
    while (iter.hasNext()) {
      FileItem item = (FileItem) iter.next();
      if (item.isFormField() && parameterName.equals(item.getFieldName())) {
        return item.getString();
      }
    }
    return null;
  }

  private GalleryBm getGalleryBm() {
    GalleryBm galleryBm = null;
    try {
      GalleryBmHome galleryBmHome = (GalleryBmHome) EJBUtilitaire.getEJBObjectRef(
          JNDINames.GALLERYBM_EJBHOME, GalleryBmHome.class);
      galleryBm = galleryBmHome.create();
    } catch (Exception e) {
      throw new GalleryRuntimeException(
          "GallerySessionController.getGalleryBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return galleryBm;
  }
}
