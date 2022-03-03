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
package org.silverpeas.components.gallery.process.media;

import org.silverpeas.components.gallery.MediaUtil;
import org.silverpeas.components.gallery.model.InternalMedia;
import org.silverpeas.core.process.ProcessProvider;
import org.silverpeas.core.process.io.file.FileHandler;
import org.silverpeas.core.process.management.AbstractFileProcess;
import org.silverpeas.core.process.management.ProcessExecutionContext;
import org.silverpeas.core.process.session.ProcessSession;

/**
 * Process to load metadata of a photo
 * @author Yohann Chastagnier
 */
public class GalleryLoadMetaDataProcess extends AbstractFileProcess<ProcessExecutionContext> {

  private final InternalMedia media;

  /**
   * Default hidden constructor
   * @param media
   */
  private GalleryLoadMetaDataProcess(final InternalMedia media) {
    this.media = media;
  }

  /**
   * Method to call to load MetaData
   * @param media
   * @throws Exception
   */
  public static void load(final InternalMedia media) throws Exception {
    ProcessProvider.getProcessManagement().execute(new GalleryLoadMetaDataProcess(media),
        new ProcessExecutionContext(null, null));
  }

  /*
   * (non-Javadoc)
   * @see AbstractFileProcess#processFiles(org.silverpeas.process.
   * management.ProcessExecutionContext, ProcessSession,
   * FileHandler)
   */
  @Override
  public void processFiles(final ProcessExecutionContext context, final ProcessSession session,
      final FileHandler fileHandler) throws Exception {
    if (media.getType().isPhoto()) {
      MediaUtil.setMetaData(fileHandler, media.getPhoto());
    }
  }
}
