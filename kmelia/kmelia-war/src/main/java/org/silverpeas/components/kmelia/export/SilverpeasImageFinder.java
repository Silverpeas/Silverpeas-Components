/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia.export;

import org.silverpeas.components.gallery.model.MediaPK;
import org.silverpeas.components.gallery.model.Photo;
import org.silverpeas.components.gallery.service.GalleryService;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileServerUtils;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;

import static java.util.regex.Pattern.compile;
import static org.silverpeas.core.contribution.attachment.AttachmentServiceProvider.getAttachmentService;

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

  private static final SilverpeasImageFinder instance = new SilverpeasImageFinder();
  private static final String ATTACHED_FILE = "attached_file";
  private static final String SERVED_FILE = "FileServer";
  private static final String IMAGE_IN_GALLERY = "GalleryInWysiwyg";
  private static final String ATTACHMENT_ID_KEY = "attachmentId";
  private static final String LANGUAGE_KEY = "lang";

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
   */
  public String findImageReferencedBy(String href) {
    String path;
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
    return Paths.get(path).toUri().toString();
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
    final UnaryOperator<String> extractor = p -> {
      final Matcher matcher = compile(".*/" + p + "/([^/]+).*").matcher(href);
      return matcher.matches() ? matcher.group(1) : StringUtil.EMPTY;
    };
    final String attachmentId = extractor.apply(ATTACHMENT_ID_KEY);
    final String lang = extractor.apply(LANGUAGE_KEY);
    final SimpleDocument attachment = getAttachmentService()
        .searchDocumentById(new SimpleDocumentPK(attachmentId), lang);
    return attachment.getAttachmentPath();
  }

  private String findWithFileService(String href) {
    Map<String, String> parameters = getQueryParameters(href);
    String componentId = parameters.get(FileServerUtils.COMPONENT_ID_PARAMETER);
    String directory = parameters.get(FileServerUtils.DIRECTORY_PARAMETER);
    String sourceFile = parameters.get(FileServerUtils.SOURCE_FILE_PARAMETER);
    return FileRepositoryManager.getAbsolutePath(componentId) + directory + "/" + sourceFile;
  }

  private String findWithGalleryComponent(String href) {
    Map<String, String> parameters = getQueryParameters(href);
    String imageId = parameters.get("ImageId");
    String componentId = parameters.get("ComponentId");
    Photo image = getGalleryService().getPhoto(new MediaPK(imageId, componentId));
    return FileRepositoryManager.getAbsolutePath(image.getMediaPK().getInstanceId()) + "image"
        + image.getId() + "/" + image.getFileName();
  }

  private Map<String, String> getQueryParameters(String href) {
    Map<String, String> queryParameters = new HashMap<>();
    String querySeparator = "%3F"; // the URL encoding of the ? token
    String queryPart = href.substring(href.indexOf(querySeparator) + querySeparator.length());
    String[] parameters = queryPart.split("&");
    for (String parameter : parameters) {
      String[] keyValue = parameter.split("=");
      queryParameters.put(keyValue[0], keyValue[1]);
    }
    return queryParameters;
  }

  private GalleryService getGalleryService() {
    return ServiceProvider.getService(GalleryService.class);
  }
}
