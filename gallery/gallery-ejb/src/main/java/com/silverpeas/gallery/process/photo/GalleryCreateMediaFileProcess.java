/*
 * Copyright (C) 2000 - 2013 Silverpeas
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

import com.silverpeas.gallery.ImageHelper;
import com.silverpeas.gallery.model.Media;
import com.silverpeas.gallery.process.AbstractGalleryFileProcess;
import com.silverpeas.gallery.process.GalleryProcessExecutionContext;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.process.io.file.FileHandler;
import org.silverpeas.process.session.ProcessSession;

import java.io.File;

/**
 * Process to create a media on file system
 * @author Yohann Chastagnier
 */
public class GalleryCreateMediaFileProcess extends AbstractGalleryFileProcess {

  private final File file;
  private final FileItem fileItem;
  private final boolean watermark;
  private final String watermarkHD;
  private final String watermarkOther;

  /**
   * Gets an instance
   * @param media
   * @param file
   * @param watermark
   * @param watermarkHD
   * @param watermarkOther
   * @return
   */
  public static GalleryCreateMediaFileProcess getInstance(final Media media, final Object file,
      final boolean watermark, final String watermarkHD, final String watermarkOther) {
    return new GalleryCreateMediaFileProcess(media, file, watermark, watermarkHD, watermarkOther);
  }

  /**
   * Default hidden constructor
   * @param media
   * @param file
   * @param watermark
   * @param watermarkHD
   * @param watermarkOther
   */
  protected GalleryCreateMediaFileProcess(final Media media, final Object file,
      final boolean watermark, final String watermarkHD, final String watermarkOther) {
    super(media);
    if (file != null) {
      if (file instanceof FileItem) {
        fileItem = (FileItem) file;
        this.file = null;
      } else if (file instanceof File) {
        this.file = (File) file;
        fileItem = null;
      } else {
        throw new IllegalArgumentException(
            "GalleryCreateMediaFileProcess() - parameter 'file' has to be an FileItem or File " +
                "instance.");
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

    // Media
    if (getMedia().getType().isPhoto()) {
      if (fileItem != null) {
        ImageHelper
            .processImage(fileHandler, getMedia().getPhoto(), fileItem, watermark, watermarkHD,
                watermarkOther);
      } else {
        ImageHelper.processImage(fileHandler, getMedia().getPhoto(), file, watermark, watermarkHD,
            watermarkOther);
      }
    }
  }
}
