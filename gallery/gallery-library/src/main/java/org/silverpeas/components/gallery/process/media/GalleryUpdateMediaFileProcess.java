/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery.process.media;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.components.gallery.MediaUtil;
import org.silverpeas.components.gallery.Watermark;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.process.AbstractGalleryFileProcess;
import org.silverpeas.core.process.io.file.FileHandler;
import org.silverpeas.core.process.management.ProcessExecutionContext;
import org.silverpeas.core.process.session.ProcessSession;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.kernel.logging.SilverLogger;

/**
 * Process to update a media on file system
 * @author Yohann Chastagnier
 */
public class GalleryUpdateMediaFileProcess extends AbstractGalleryFileProcess {

  private final FileItem fileItem;
  private final Watermark watermark;

  /**
   * Gets an instance
   * @param media
   * @param fileItem
   * @param watermark
   * @return
   */
  public static GalleryUpdateMediaFileProcess getInstance(final Media media,
      final FileItem fileItem, final Watermark watermark) {
    return new GalleryUpdateMediaFileProcess(media, fileItem, watermark);
  }

  /**
   * Default hidden constructor
   * @param media
   * @param fileItem
   * @param watermark
   */
  protected GalleryUpdateMediaFileProcess(final Media media, final FileItem fileItem,
      final Watermark watermark) {
    super(media);
    this.fileItem = fileItem;
    this.watermark = watermark;
  }

  /*
   * (non-Javadoc)
   * @see AbstractFileProcess#processFiles(org.silverpeas.process.
   * management.ProcessExecutionContext, ProcessSession,
   * FileHandler)
   */
  @Override
  public void processFiles(final ProcessExecutionContext context,
      final ProcessSession session, final FileHandler fileHandler) throws Exception {

    boolean hasBeenProcessed = false;

    // Media
    if (fileItem != null && !getMedia().getType().isStreaming()) {
      final String name = fileItem.getName();
      if (StringUtil.isDefined(name)) {

        hasBeenProcessed = true;

        // Deleting repository with old media
        fileHandler.getHandledFile(Media.BASE_PATH, context.getComponentInstanceId(),
            getMedia().getWorkspaceSubFolderName()).delete();

        switch (getMedia().getType()) {
          case Photo:
            // Creating new images
            MediaUtil.processPhoto(fileHandler, getMedia().getPhoto(), fileItem, watermark);
            break;
          case Video:
            // Save new video
            MediaUtil.processVideo(fileHandler, getMedia().getVideo(), fileItem);
            break;
          case Sound:
            // Save new sound
            MediaUtil.processSound(fileHandler, getMedia().getSound(), fileItem);
            break;
          default:
            // In other cases, there is no file to manage.
            break;
        }
      }
    }

    if (!getMedia().getType().isStreaming() && !hasBeenProcessed) {
      SilverLogger.getLogger(this)
          .warn(getMedia().getType().name() + " media type is never processed");
    }
  }
}
