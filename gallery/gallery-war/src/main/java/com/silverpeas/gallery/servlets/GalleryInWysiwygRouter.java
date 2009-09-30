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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
---*/

package com.silverpeas.gallery.servlets;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.control.ejb.GalleryBmHome;
import com.silverpeas.gallery.model.GalleryRuntimeException;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.model.PhotoPK;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodePK;

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class GalleryInWysiwygRouter extends HttpServlet {
  HttpSession session;

  PrintWriter out;

  public void init(ServletConfig config) {
    try {
      super.init(config);
    } catch (ServletException se) {
      SilverTrace.fatal("gallery", "GalleryInWysiwygRouter.init",
          "gallery.CANNOT_ACCESS_SUPERCLASS");
    }
  }

  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    SilverTrace.info("gallery", "GalleryInWysiwygRouter.doPost",
        "root.MSG_GEN_ENTER_METHOD");

    String componentId = request.getParameter("ComponentId");
    String albumId = request.getParameter("AlbumId");
    String imageId = request.getParameter("ImageId");
    String language = request.getParameter("Language");
    String size = request.getParameter("Size");

    // contrôle que "componentId" est bien une photothèque ayant le droit d'être
    // vu dans un Wysiwyg
    boolean isViewInWysiwyg = "yes"
        .equalsIgnoreCase(getOrganizationController()
            .getComponentParameterValue(componentId, "viewInWysiwyg"));
    if (isViewInWysiwyg) {
      SilverTrace.info("gallery", "GalleryInWysiwygRouter.doPost",
          "root.MSG_GEN_PARAM_VALUE", "componentId = " + componentId
              + " albumId = " + albumId);

      String rootDest = "/gallery/jsp/";
      String destination = "";

      // regarder si on demande l'affichage de l'arborescence ou l'affichage du
      // contenu d'un album
      if (isDefined(imageId)) {
        // affichage de l'image
        PhotoDetail image = getGalleryBm().getPhoto(
            new PhotoPK(imageId, componentId));
        displayImage(response, image, size);
      } else if (!isDefined(albumId)) {
        // affichage de l'arborescence des albums
        Collection albums = viewAllAlbums(componentId);
        request.setAttribute("Albums", albums);
        // appel jsp
        destination = rootDest + "wysiwygAlbums.jsp";
      } else {
        // affichage du contenu d'un album
        Collection photos = viewPhotosOfAlbum(componentId, albumId);
        request.setAttribute("Photos", photos);
        // appel jsp
        destination = rootDest + "wysiwygImages.jsp";
      }
      request.setAttribute("Language", language);
      RequestDispatcher requestDispatcher = getServletConfig()
          .getServletContext().getRequestDispatcher(destination);
      if (requestDispatcher != null)
        requestDispatcher.forward(request, response);
    }
  }

  private Collection viewAllAlbums(String componentId) {
    Collection albums = new ArrayList();

    // récupération des albums de la photothèque
    try {
      albums = getGalleryBm().getAllAlbums(componentId);
    } catch (Exception e) {
      throw new GalleryRuntimeException(
          "GalleryInWysiwygRouter.viewAllAlbums()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return albums;
  }

  private Collection viewPhotosOfAlbum(String componentId, String albumId) {
    // récupération de toutes les photos d'un album
    Collection photos = new ArrayList();
    try {
      NodePK nodePK = new NodePK(albumId, componentId);
      photos = getGalleryBm().getAllPhoto(nodePK, false);
    } catch (Exception e) {
      throw new GalleryRuntimeException(
          "GalleryInWysiwygRouter.viewPhotosOfAlbum()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return photos;
  }

  private void displayImage(HttpServletResponse res, PhotoDetail image,
      String size) throws IOException {
    res.setContentType(image.getImageMimeType());
    OutputStream out2 = res.getOutputStream();
    int read;
    BufferedInputStream input = null;

    String fileName = image.getId() + "_preview.jpg";
    if (isDefined(size))
      fileName = image.getId() + "_" + size + ".jpg";

    String filePath = FileRepositoryManager.getAbsolutePath(image.getPhotoPK()
        .getInstanceId())
        + "image" + image.getId() + File.separator + fileName;
    SilverTrace.info("gallery", "GalleryInWysiwygRouter.displayImage()",
        "root.MSG_GEN_ENTER_METHOD", "filePath = " + filePath);
    try {
      input = new BufferedInputStream(new FileInputStream(filePath));
      read = input.read();
      if (read == -1) {
        // displayWarningHtmlCode(res);
      } else {
        while (read != -1) {
          out2.write(read); // writes bytes into the response
          read = input.read();
        }
      }
    } catch (Exception e) {
      SilverTrace.warn("gallery", "GalleryInWysiwygRouter.doPost",
          "root.EX_CANT_READ_FILE", "filePath = " + filePath);
      // displayWarningHtmlCode(res);
    } finally {
      SilverTrace.info("gallery", "GalleryInWysiwygRouter.displayImage()", "",
          " finally ");
      // we must close the in and out streams
      try {
        if (input != null) {
          input.close();
        }
        out2.close();
      } catch (Exception e) {
        SilverTrace.warn("gallery", "GalleryInWysiwygRouter.displayImage",
            "root.EX_CANT_READ_FILE", "close failed");
      }
    }
  }

  private GalleryBm getGalleryBm() {
    GalleryBm galleryBm = null;
    try {
      GalleryBmHome galleryBmHome = (GalleryBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.GALLERYBM_EJBHOME, GalleryBmHome.class);
      galleryBm = galleryBmHome.create();
    } catch (Exception e) {
      throw new GalleryRuntimeException(
          "GalleryInWysiwygRouter.getGalleryBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return galleryBm;
  }

  private OrganizationController getOrganizationController() {
    OrganizationController orga = new OrganizationController();
    return orga;
  }

  private boolean isDefined(String param) {
    return (param != null && param.length() > 0 && !"".equals(param));
  }
}