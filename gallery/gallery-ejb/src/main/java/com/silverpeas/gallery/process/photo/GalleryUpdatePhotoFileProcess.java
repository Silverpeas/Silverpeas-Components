/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.process.photo;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.process.io.file.FileHandler;
import org.silverpeas.process.session.Session;

import com.silverpeas.gallery.ImageHelper;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.process.AbstractGalleryFileProcess;
import com.silverpeas.gallery.process.GalleryProcessExecutionContext;
import com.silverpeas.util.StringUtil;

/**
 * Process to update a photo on file system
 * @author Yohann Chastagnier
 */
public class GalleryUpdatePhotoFileProcess extends AbstractGalleryFileProcess {

  private final FileItem fileItem;
  private final boolean watermark;
  private final String watermarkHD;
  private final String watermarkOther;

  /**
   * Gets an instance
   * @param photo
   * @param fileItem
   * @param watermark
   * @param watermarkHD
   * @param watermarkOther
   * @return
   */
  public static GalleryUpdatePhotoFileProcess getInstance(final PhotoDetail photo,
      final FileItem fileItem, final boolean watermark, final String watermarkHD,
      final String watermarkOther) {
    return new GalleryUpdatePhotoFileProcess(photo, fileItem, watermark, watermarkHD,
        watermarkOther);
  }

  /**
   * Default hidden constructor
   * @param photo
   * @param fileItem
   * @param watermark
   * @param watermarkHD
   * @param watermarkOther
   */
  protected GalleryUpdatePhotoFileProcess(final PhotoDetail photo, final FileItem fileItem,
      final boolean watermark, final String watermarkHD, final String watermarkOther) {
    super(photo);
    this.fileItem = fileItem;
    this.watermark = watermark;
    this.watermarkHD = watermarkHD;
    this.watermarkOther = watermarkOther;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.process.management.AbstractFileProcess#processFiles(org.silverpeas.process.
   * management.ProcessExecutionContext, org.silverpeas.process.session.Session,
   * org.silverpeas.process.io.file.FileHandler)
   */
  @Override
  public void processFiles(final GalleryProcessExecutionContext context, final Session session,
      final FileHandler fileHandler) throws Exception {
    if (fileItem != null) {
      final String name = fileItem.getName();
      if (StringUtil.isDefined(name)) {

        // Deleting repository with old photos
        fileHandler.getHandledFile(BASE_PATH, context.getComponentInstanceId(),
            gallerySettings.getString("imagesSubDirectory") + getPhoto().getId()).delete();

        // Creating new images
        ImageHelper.processImage(fileHandler, getPhoto(), fileItem, watermark, watermarkHD,
            watermarkOther);
      }
    }
  }
}
