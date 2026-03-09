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

import org.silverpeas.components.gallery.constant.MediaMimeType;
import org.silverpeas.components.gallery.media.DrewMediaMetadataExtractor;
import org.silverpeas.components.gallery.media.MediaMetadataException;
import org.silverpeas.components.gallery.media.MediaMetadataExtractor;
import org.silverpeas.components.gallery.model.InternalMedia;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.model.Photo;
import org.silverpeas.core.notification.message.MessageManager;
import org.silverpeas.core.process.ProcessProvider;
import org.silverpeas.core.process.io.file.FileHandler;
import org.silverpeas.core.process.io.file.HandledFile;
import org.silverpeas.core.process.management.AbstractFileProcess;
import org.silverpeas.core.process.management.ProcessExecutionContext;
import org.silverpeas.core.process.session.ProcessSession;
import org.silverpeas.kernel.logging.SilverLogger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Process to load metadata of a photo
 * @author Yohann Chastagnier
 */
public class GalleryLoadMetaDataProcess extends AbstractFileProcess<ProcessExecutionContext> {

  private final InternalMedia media;

  private GalleryLoadMetaDataProcess(final InternalMedia media) {
    this.media = media;
  }

  public static void load(final InternalMedia media) throws Exception {
    ProcessProvider.getProcessManagement().execute(new GalleryLoadMetaDataProcess(media),
        new ProcessExecutionContext(null, null));
  }

  @Override
  public void processFiles(final ProcessExecutionContext context, final ProcessSession session,
      final FileHandler fileHandler) throws MediaMetadataException {
    if (media.getType().isPhoto()) {
      setMetaData(fileHandler, media.getPhoto());
    }
  }

  private static void setMetaData(final FileHandler fileHandler, final Photo photo)
      throws MediaMetadataException {
    if (MediaMimeType.JPG == photo.getFileMimeType()) {
      final HandledFile handledFile = fileHandler
          .getHandledFile(Media.BASE_PATH, photo.getInstanceId(), photo.getWorkspaceSubFolderName(),
              photo.getFileName());
      if (handledFile.exists()) {
        try {
          MediaMetadataExtractor extractor = new DrewMediaMetadataExtractor(photo.getInstanceId());
          String lang = MessageManager.getLanguage();
          extractor.extractImageExifMetaData(handledFile.getFile(), lang)
              .forEach(photo::addMetaData);
          extractor.extractImageIptcMetaData(handledFile.getFile(), lang)
              .forEach(photo::addMetaData);
        } catch (UnsupportedEncodingException e) {
          SilverLogger.getLogger(GalleryLoadMetaDataProcess.class).silent(e)
              .error("Bad metadata encoding in image " + photo.getTitle() + ": " + e.getMessage());
        } catch (IOException e) {
          throw new MediaMetadataException(e);
        }
      }
    }
  }
}
