/**
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

package com.silverpeas.gallery;

import com.stratelia.webactiv.util.FileRepositoryManager;

import java.util.Locale;

/**
 * Enumeration for all supported image types.
 */
public
enum ImageType {
  JPEG, BMP, GIF, PNG, JPG, TIF, TIFF, ERROR;

  public static ImageType findType(String type) {
    if (type != null) {
      try {
      return ImageType.valueOf(type.toUpperCase(Locale.getDefault()));
      }catch(IllegalArgumentException ex) {
         return ERROR;
      }
    }
    return ERROR;
  }


  public static boolean isImage(String name) {
    ImageType type = findType(FileRepositoryManager.getFileExtension(name));
    return type.isValid();
  }

  public static boolean isValidExtension(String name) {
    ImageType type = findType(FileRepositoryManager.getFileExtension(name));
    return type.isJpeg() || type == TIF|| type == TIFF || type == PNG;
  }
  
  public static boolean isPreviewable(String name) {
    ImageType type = findType(FileRepositoryManager.getFileExtension(name));
    return type.isJpeg();
  }

  public static boolean isJpeg(String type) {
    return findType(type).isJpeg();
  }

  protected boolean isJpeg() {
    return this == GIF || this == JPEG || this == JPG;
  }

  protected boolean isValid() {
    return this != ERROR;
  }

}
