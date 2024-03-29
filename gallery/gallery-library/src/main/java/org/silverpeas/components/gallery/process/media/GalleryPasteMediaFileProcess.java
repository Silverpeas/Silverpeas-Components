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

import org.silverpeas.components.gallery.MediaUtil;
import org.silverpeas.components.gallery.model.InternalMedia;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.model.MediaPK;
import org.silverpeas.components.gallery.process.AbstractGalleryFileProcess;
import org.silverpeas.core.process.io.file.FileHandler;
import org.silverpeas.core.process.management.ProcessExecutionContext;
import org.silverpeas.core.process.session.ProcessSession;

/**
 * Process to paste a media on file system
 * @author Yohann Chastagnier
 */
public class GalleryPasteMediaFileProcess extends AbstractGalleryFileProcess {

  private final MediaPK fromMediaPk;
  private final boolean isCutted;

  /**
   * Default hidden constructor
   * @param media
   * @param fromMediaPk
   * @param isCutted
   */
  protected GalleryPasteMediaFileProcess(final Media media, final MediaPK fromMediaPk,
      final boolean isCutted) {
    super(media);
    this.fromMediaPk = fromMediaPk;
    this.isCutted = isCutted;
  }

  /**
   * Gets an instance
   * @param media
   * @param fromMediaPk
   * @param isCutted
   * @return
   */
  public static GalleryPasteMediaFileProcess getInstance(final Media media,
      final MediaPK fromMediaPk, final boolean isCutted) {
    return new GalleryPasteMediaFileProcess(media, fromMediaPk, isCutted);
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
    InternalMedia internalMedia = getMedia().getInternalMedia();
    if (internalMedia != null &&
        (!isCutted || !fromMediaPk.getInstanceId().equals(context.getComponentInstanceId()))) {
        MediaUtil.pasteInternalMedia(fileHandler, fromMediaPk, internalMedia, isCutted);
    }
  }
}
