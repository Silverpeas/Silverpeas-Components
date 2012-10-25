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
package com.silverpeas.gallery.process.photo;

import java.io.File;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.process.io.file.FileHandler;
import org.silverpeas.process.session.ProcessSession;

import com.silverpeas.gallery.ImageHelper;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.process.AbstractGalleryFileProcess;
import com.silverpeas.gallery.process.GalleryProcessExecutionContext;

/**
 * Process to create a photo on file system
 * @author Yohann Chastagnier
 */
public class GalleryCreatePhotoFileProcess extends AbstractGalleryFileProcess {

  private final File file;
  private final FileItem fileItem;
  private final boolean watermark;
  private final String watermarkHD;
  private final String watermarkOther;

  /**
   * Gets an instance
   * @param photo
   * @param file
   * @param watermark
   * @param watermarkHD
   * @param watermarkOther
   * @return
   */
  public static GalleryCreatePhotoFileProcess getInstance(final PhotoDetail photo,
      final Object file, final boolean watermark, final String watermarkHD,
      final String watermarkOther) {
    return new GalleryCreatePhotoFileProcess(photo, file, watermark, watermarkHD, watermarkOther);
  }

  /**
   * Default hidden constructor
   * @param photo
   * @param file
   * @param watermark
   * @param watermarkHD
   * @param watermarkOther
   * @param parameters
   */
  protected GalleryCreatePhotoFileProcess(final PhotoDetail photo, final Object file,
      final boolean watermark, final String watermarkHD, final String watermarkOther) {
    super(photo);
    if (file != null) {
      if (file instanceof FileItem) {
        fileItem = (FileItem) file;
        this.file = null;
      } else if (file instanceof File) {
        this.file = (File) file;
        fileItem = null;
      } else {
        throw new IllegalArgumentException(
            "GalleryPhotoIOProcess() - parameter 'file' has to be an FileItem or File instance.");
      }
    } else {
      fileItem = null;
      this.file = null;
    }
    this.watermark = watermark;
    this.watermarkHD = watermarkHD;
    this.watermarkOther = watermarkOther;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.process.management.AbstractFileProcess#processFiles(org.silverpeas.process.
   * management.ProcessExecutionContext, org.silverpeas.process.session.ProcessSession,
   * org.silverpeas.process.io.file.FileHandler)
   */
  @Override
  public void processFiles(final GalleryProcessExecutionContext context,
      final ProcessSession session, final FileHandler fileHandler) throws Exception {

    // Photo
    if (fileItem != null) {
      ImageHelper.processImage(fileHandler, getPhoto(), fileItem, watermark, watermarkHD,
          watermarkOther);
    } else {
      ImageHelper.processImage(fileHandler, getPhoto(), file, watermark, watermarkHD,
          watermarkOther);
    }
  }
}
