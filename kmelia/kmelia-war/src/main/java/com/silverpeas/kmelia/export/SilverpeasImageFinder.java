/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.kmelia.export;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.SystemUtils;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.model.MediaPK;
import com.silverpeas.gallery.model.Photo;
import org.silverpeas.util.EJBUtilitaire;
import org.silverpeas.util.FileRepositoryManager;
import org.silverpeas.util.FileServerUtils;
import org.silverpeas.util.JNDINames;

/**
 * A finder of images that were uploaded by a Silverpeas component instance, whatever this component
 * is.
 *
 * Images in Silverpeas can be uploaded by different Silverpeas components. So, as each component
 * has its own way to refer an image that was upload through it, there is several ways to access an
 * image in Silverpeas. So, in the case a publication embeds one or more images coming from
 * different components, it is required to support the different mechanisme of finding an image. The
 * purpose of this class is to wrap theses different mechanismes for the export of Kmelia
 * publications.
 */
public class SilverpeasImageFinder {

  private static SilverpeasImageFinder instance = new SilverpeasImageFinder();
  private static String ATTACHED_FILE = "attached_file";
  private static String SERVED_FILE = "FileServer";
  private static String IMAGE_IN_GALLERY = "GalleryInWysiwyg";
  private static String ATTACHMENT_ID_KEY = "attachmentId";
  private static String LANGUAGE_KEY = "lang";

  /**
   * Gets an instance of this class.
   *
   * @return a SilverpeasImageFinder instance.
   */
  public static SilverpeasImageFinder getImageFinder() {
    return instance;
  }

  /**
   * Finds the image having as unique identifier in Silverpeas the specified relative web reference.
   *
   * @param href the web reference of the image is the path of the image relative to the web context
   * of Silverpeas. This path is Silverpeas component dependent, so that only a given Silverpeas
   * component can process it.
   * @return the URI of the image in the server.
   * @throws Exception if an error occurs while finding the image referenced by the specified href.
   */
  public String findImageReferenceddBy(String href) throws Exception {
    String path = "";
    if (isAnAttachment(href)) {
      path = findWithAttachmentController(href);
    } else if (isServedByFileServer(href)) {
      // bad news, the image was uploaded by the old servlet
      path = findWithFileService(href);
    } else if (isInGallery(href)) {
      path = findWithGalleryComponent(href);
    } else {
      path = FileRepositoryManager.getUploadPath() + href;
    }
    if (SystemUtils.IS_OS_WINDOWS) {
      path = "file:/" + path.replaceAll("\\\\", "/");
    } else {
      path = "file://" + path;
    }
    return path.replaceAll(" ", "%20");
  }

  private boolean isAnAttachment(String href) {
    return href.contains(ATTACHED_FILE);
  }

  private boolean isServedByFileServer(String href) {
    return href.contains(SERVED_FILE);
  }

  private boolean isInGallery(String href) {
    return href.contains(IMAGE_IN_GALLERY);
  }

  private String findWithAttachmentController(String href) {
    String attachmentId = null;
    String lang = null;
    String[] tokens = href.split("/");
    for (int i = 0; i < tokens.length; i++) {
      if (ATTACHMENT_ID_KEY.equals(tokens[i])) {
        attachmentId = tokens[++i];
      }
      if (LANGUAGE_KEY.equals(tokens[i])) {
        lang = tokens[++i];
      }
      if (attachmentId != null && lang != null) {
        break;
      }
    }
    SimpleDocument attachment = AttachmentServiceFactory.getAttachmentService().searchDocumentById(
        new SimpleDocumentPK(attachmentId), lang);
    return attachment.getAttachmentPath();
  }

  private String findWithFileService(String href) {
    Map<String, String> parameters = getQueryParameters(href);
    String componentId = parameters.get(FileServerUtils.COMPONENT_ID_PARAMETER);
    String directory = parameters.get(FileServerUtils.DIRECTORY_PARAMETER);
    String sourceFile = parameters.get(FileServerUtils.SOURCE_FILE_PARAMETER);
    return FileRepositoryManager.getAbsolutePath(componentId) + directory + "/" + sourceFile;
  }

  private String findWithGalleryComponent(String href) throws Exception {
    Map<String, String> parameters = getQueryParameters(href);
    String imageId = parameters.get("ImageId");
    String componentId = parameters.get("ComponentId");
    Photo image = getGalleryBm().getPhoto(new MediaPK(imageId, componentId));
    return FileRepositoryManager.getAbsolutePath(image.getMediaPK().getInstanceId()) + "image"
        + image.getId() + "/" + image.getFileName();
  }

  private Map<String, String> getQueryParameters(String href) {
    Map<String, String> queryParameters = new HashMap<String, String>();
    String querySeparator = "%3F"; // the URL encoding of the ? token
    String queryPart = href.substring(href.indexOf(querySeparator) + querySeparator.length());
    String[] parameters = queryPart.split("&");
    for (String parameter : parameters) {
      String[] keyValue = parameter.split("=");
      queryParameters.put(keyValue[0], keyValue[1]);
    }
    return queryParameters;
  }

  private GalleryBm getGalleryBm() throws Exception {
    return EJBUtilitaire.getEJBObjectRef(JNDINames.GALLERYBM_EJBHOME, GalleryBm.class);
  }
}
