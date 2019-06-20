/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.components.gallery.process.media;

import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.process.AbstractGalleryFileProcess;
import org.silverpeas.core.process.io.file.FileHandler;
import org.silverpeas.core.process.management.ProcessExecutionContext;
import org.silverpeas.core.process.session.ProcessSession;

/**
 * Process to delete a media from file system
 * @author Yohann Chastagnier
 */
public class GalleryDeleteMediaFileProcess extends AbstractGalleryFileProcess {

  /**
   * Gets an instance
   * @param media
   * @return
   */
  public static GalleryDeleteMediaFileProcess getInstance(final Media media) {
    return new GalleryDeleteMediaFileProcess(media);
  }

  /**
   * Default hidden constructor
   * @param media
   */
  protected GalleryDeleteMediaFileProcess(final Media media) {
    super(media);
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

    // Deleting repository with old media
    fileHandler.getHandledFile(Media.BASE_PATH, context.getComponentInstanceId(),
        getMedia().getWorkspaceSubFolderName()).delete();
  }
}
