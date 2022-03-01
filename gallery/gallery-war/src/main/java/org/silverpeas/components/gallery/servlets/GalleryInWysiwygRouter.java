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

import org.silverpeas.components.gallery.constant.MediaResolution;
import org.silverpeas.components.gallery.model.AlbumDetail;
import org.silverpeas.components.gallery.model.GalleryRuntimeException;
import org.silverpeas.components.gallery.model.MediaPK;
import org.silverpeas.components.gallery.model.Photo;
import org.silverpeas.components.gallery.service.GalleryService;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.contribution.content.LinkUrlDataSource;
import org.silverpeas.core.contribution.content.LinkUrlDataSourceScanner;
import org.silverpeas.core.contribution.content.wysiwyg.service.directive.ImageUrlAccordingToHtmlSizeDirective;
import org.silverpeas.core.io.file.SilverpeasFile;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.activation.FileDataSource;
import javax.inject.Singleton;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;
import static org.silverpeas.core.contribution.content.LinkUrlDataSourceScanner.extractUrlParameters;
import static org.silverpeas.core.util.StringDataExtractor.RegexpPatternDirective.regexp;
import static org.silverpeas.core.util.StringDataExtractor.from;
import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

/**
 * This servlet is used in order to list image media for other components which handle WYSIWYG
 * editing.
 */
public class GalleryInWysiwygRouter extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private static SilverpeasFile getSilverpeasFile(final Photo image, final String size,
      final boolean useOriginal) {
    final MediaResolution mediaResolution = useOriginal
        ? MediaResolution.ORIGINAL
        : MediaResolution.PREVIEW;
    return image.getFile(mediaResolution, size);
  }

  private static boolean isViewInWysiwyg(final String componentId) {
    return "yes".equalsIgnoreCase(OrganizationController.get()
        .getComponentParameterValue(componentId, "viewInWysiwyg"));
  }

  private static GalleryService getGalleryService() {
    return ServiceProvider.getService(GalleryService.class);
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String componentId = request.getParameter("ComponentId");
    String albumId = request.getParameter("AlbumId");
    String imageId = request.getParameter("ImageId");
    String language = request.getParameter("Language");
    String size = request.getParameter("Size");
    boolean useOriginal = Boolean.parseBoolean(request.getParameter("UseOriginal"));

    // Check component instance application parameter viewInWysiwyg (shared picture) is activated
    boolean isViewInWysiwyg = isViewInWysiwyg(componentId);
    if (isViewInWysiwyg) {


      String rootDest = "/gallery/jsp/";
      String destination = "";

      // regarder si on demande l'affichage de l'arborescence ou l'affichage du
      // contenu d'un album
      if (StringUtil.isDefined(imageId)) {
        // Display image
        Photo image = getGalleryService().getPhoto(new MediaPK(imageId, componentId));
        displayImage(response, image, size, useOriginal);
      } else if (!StringUtil.isDefined(albumId)) {
        // Display albums content
        request.setAttribute("Albums", viewAllAlbums(componentId));
        destination = rootDest + "wysiwygAlbums.jsp";
      } else {
        // Display album content
        request.setAttribute("MediaList", viewPhotosOfAlbum(componentId, albumId));
        destination = rootDest + "wysiwygImages.jsp";
      }
      if (StringUtil.isDefined(destination)) {
        request.setAttribute("Language", language);
        RequestDispatcher requestDispatcher =
            getServletConfig().getServletContext().getRequestDispatcher(destination);
        if (requestDispatcher != null) {
          requestDispatcher.forward(request, response);
        }
      }
    }
  }

  /**
   * Retrieve all albums from a multimedia application
   * @param componentId the component instance identifier
   * @return a collection of AlbumDetail
   */
  private Collection<AlbumDetail> viewAllAlbums(String componentId) {
    try {
      return getGalleryService().getAllAlbums(componentId);
    } catch (Exception e) {
      throw new GalleryRuntimeException(e);
    }

  }

  /**
   * Retrieve all photos from an album
   * @param componentId the component identifier
   * @param albumId the album identifier
   * @return a collection of Photo
   */
  private Collection<Photo> viewPhotosOfAlbum(String componentId, String albumId) {
    try {
      NodePK nodePK = new NodePK(albumId, componentId);
      return getGalleryService().getAllPhotos(nodePK);
    } catch (Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  private void displayImage(HttpServletResponse res, Photo image, String size, boolean useOriginal)
      throws IOException {
    res.setContentType(image.getFileMimeType().getMimeType());
    res.setHeader("Cache-Control", "no-store"); //HTTP 1.1
    res.setHeader("Pragma", "no-cache");
    res.setDateHeader("Expires", -1);
    OutputStream out2 = res.getOutputStream();
    int read;
    final SilverpeasFile imageFile = getSilverpeasFile(image, size, useOriginal);
    try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(imageFile))) {
      read = input.read();
      while (read != -1) {
        // writes bytes into the response
        out2.write(read);
        read = input.read();
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).warn(e);
    } finally {

      // we must close the in and out streams
      try {
        out2.close();
      } catch (Exception e) {
        SilverLogger.getLogger(this).warn(e);
      }
    }
  }

  @Singleton
  public static class ImageUrlToDataSourceScanner implements LinkUrlDataSourceScanner {

    private static final Pattern GALLERY_CONTENT_LINK_PATTERN =
        Pattern.compile("(?i)=\"([^\"]*/GalleryInWysiwyg/[^\"]+)");

    @Override
    public List<LinkUrlDataSource> scanHtml(final String htmlContent) {
      final List<LinkUrlDataSource> result = new ArrayList<>();
      from(htmlContent).withDirectives(singletonList(regexp(GALLERY_CONTENT_LINK_PATTERN, 1))).extract().forEach(l -> {
        final Map<String, String> params = extractUrlParameters(l);
        final String componentId = params.get("ComponentId");
        // Check component instance application parameter viewInWysiwyg (shared picture) is activated
        boolean isViewInWysiwyg = isViewInWysiwyg(componentId);
        if (isViewInWysiwyg) {
          final String imageId = params.get("ImageId");
          final String size = params.get("Size");
          final boolean useOriginal = Boolean.parseBoolean(params.get("UseOriginal"));
          final Photo image = getGalleryService().getPhoto(new MediaPK(imageId, componentId));
          final SilverpeasFile imageFile = getSilverpeasFile(image, size, useOriginal);
          if (imageFile.exists()) {
            result.add(new LinkUrlDataSource(l, () -> new FileDataSource(imageFile)));
          }
        }
      });
      return result;
    }
  }

  @Singleton
  public static class ImageUrlAccordingToHtmlSizeDirectiveTranslator extends
      ImageUrlAccordingToHtmlSizeDirective.SrcWithSizeParametersTranslator {

    @Override
    public boolean isCompliantUrl(final String url) {
      return defaultStringIfNotDefined(url).contains("/GalleryInWysiwyg/");
    }
  }
}