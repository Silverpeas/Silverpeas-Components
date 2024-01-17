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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.gallery.web;

import org.silverpeas.components.gallery.constant.MediaMimeType;
import org.silverpeas.components.gallery.model.GalleryRuntimeException;
import org.silverpeas.core.notification.message.MessageManager;
import org.silverpeas.core.notification.message.MessageNotifier;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.webapi.upload.ComponentInstanceFileUploadVerification;
import org.silverpeas.core.webapi.upload.FileUploadVerifyData;

import javax.inject.Named;
import java.io.File;

/**
 * Verifies for the Gallery instance a new uploaded file which has not been yet registered into
 * component data.
 * @author Yohann Chastagnier
 */
@Named
public class GalleryInstanceFileUploadVerification
    implements ComponentInstanceFileUploadVerification {

  @Override
  public void verify(final String componentInstanceId,
      final FileUploadVerifyData fileUploadVerifyData) {
    checkMimeType(new File(fileUploadVerifyData.getName()));
  }

  @Override
  public void verify(final String componentInstanceId, final File uploadedFile) {
    checkMimeType(uploadedFile);
  }

  private void checkMimeType(File file) {
    MediaMimeType mediaMimeType = MediaMimeType.fromFile(file);
    if (!mediaMimeType.isSupportedMediaType() && !FileUtil.isArchive(file.getPath())) {
      LocalizationBundle galleryBundle = ResourceLocator
          .getLocalizationBundle("org.silverpeas.gallery.multilang.galleryBundle",
              MessageManager.getLanguage());
      final String message = galleryBundle.getStringWithParams("gallery.format", file.getName());
      MessageNotifier.addError(message);
      throw new GalleryRuntimeException(message);
    }
  }
}
