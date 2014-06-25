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
import com.silverpeas.gallery.model.Photo;
import org.silverpeas.process.ProcessFactory;
import org.silverpeas.process.io.file.FileHandler;
import org.silverpeas.process.management.AbstractFileProcess;
import org.silverpeas.process.management.ProcessExecutionContext;
import org.silverpeas.process.session.ProcessSession;

/**
 * Process to load metadata of a photo
 * @author Yohann Chastagnier
 */
public class GalleryLoadMetaDataProcess extends AbstractFileProcess<ProcessExecutionContext> {

  private final Photo photo;

  /**
   * Method to call to load MetaData
   * @param photo
   * @throws Exception
   */
  public static void load(final Photo photo) throws Exception {
    ProcessFactory.getProcessManagement().execute(new GalleryLoadMetaDataProcess(photo),
        new ProcessExecutionContext(null, null));
  }

  /**
   * Default hidden constructor
   * @param photo
   */
  private GalleryLoadMetaDataProcess(final Photo photo) {
    this.photo = photo;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.process.management.AbstractFileProcess#processFiles(org.silverpeas.process.
   * management.ProcessExecutionContext, org.silverpeas.process.session.ProcessSession,
   * org.silverpeas.process.io.file.FileHandler)
   */
  @Override
  public void processFiles(final ProcessExecutionContext context, final ProcessSession session,
      final FileHandler fileHandler) throws Exception {
    ImageHelper.setMetaData(fileHandler, photo);
  }
}
