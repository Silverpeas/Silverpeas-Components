/**
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

package com.silverpeas.gallery.constant;

import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import org.apache.commons.collections.set.UnmodifiableSet;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Enumeration for all supported media types in the gallery component.
 */
public enum MediaMimeType {
  BMP, GIF, PNG, JPG("jpeg", "pjpeg"), TIFF("tif"),
  MOV, MP4, FLV,
  MP3,
  ERROR;

  @SuppressWarnings("unchecked")
  public static final Set<MediaMimeType> PHOTOS =
      UnmodifiableSet.decorate(EnumSet.of(BMP, GIF, PNG, JPG, TIFF));
  @SuppressWarnings("unchecked")
  public static final Set<MediaMimeType> VIDEOS =
      UnmodifiableSet.decorate(EnumSet.of(MOV, MP4, FLV));
  @SuppressWarnings("unchecked")
  public static final Set<MediaMimeType> SOUNDS = UnmodifiableSet.decorate(EnumSet.of(MP3));

  private static final Set<MediaMimeType> ALL_VALIDS = EnumSet.allOf(MediaMimeType.class);

  static {
    ALL_VALIDS.remove(ERROR);
  }

  private final String mimeType;
  private final List<String> extensions;

  MediaMimeType(String... otherExtensions) {
    mimeType = FileUtil.getMimeType("file." + this.name().toLowerCase());
    extensions = new ArrayList<String>();
    extensions.add(name().toLowerCase());
    Collections.addAll(extensions, otherExtensions);
  }

  /**
   * Finds from the given file the corresponding {@link MediaMimeType}
   * @param file
   * @return {@link MediaMimeType}
   */
  public static MediaMimeType fromFile(File file) {
    return fromMimeType(file != null ? FileUtil.getMimeType(file.getPath()) : null);
  }

  /**
   * Gets the corresponding {@link MediaMimeType} of specified one as string.
   * @param mimeType
   * @return {@link MediaMimeType}
   */
  public static MediaMimeType fromMimeType(String mimeType) {
    if (StringUtil.isDefined(mimeType)) {
      // This test is to keep compatibility with existent galleries.
      // With IE7 uploading, it was possible to obtain this mime-type...
      if ("image/pjpeg".equals(mimeType)) {
        return JPG;
      }
      for (MediaMimeType mediaMimeType : ALL_VALIDS) {
        if (mediaMimeType.mimeType.equals(mimeType)) {
          return mediaMimeType;
        }
      }
    }
    return ERROR;
  }

  /**
   * Gets the name (useful for JSTL use)
   * @return
   */
  public String getName() {
    return name();
  }

  /**
   * Gets the mime type as string.
   * @return
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * Gets supported extensions.
   * @return
   */
  public List<String> getExtensions() {
    return extensions;
  }

  /**
   * Indicates if the mime type is one of handled photo types.
   * @return true if it is a handled one, false otherwise.
   */
  public boolean isSupportedPhotoType() {
    return PHOTOS.contains(this);
  }

  /**
   * Indicates if the mime type is one of handled photo types.
   * @return true if it is a handled one, false otherwise.
   */
  public boolean isSupportedVideoType() {
    return VIDEOS.contains(this);
  }

  /**
   * Indicates if the mime type is one of handled photo types.
   * @return true if it is a handled one, false otherwise.
   */
  public boolean isSupportedSoundType() {
    return SOUNDS.contains(this);
  }

  /**
   * Indicates if the mime type is a photo one and if it is readable by ImageIo.
   * @return true if it is a photo media type that is readable by ImageIo
   * @see http://docs.oracle.com/javase/6/docs/api/javax/imageio/package-summary.html
   */
  public boolean isReadablePhoto() {
    return this == GIF || this == JPG || this == PNG || this == BMP;
  }

  /**
   * Indicates if the mime type is one of photo and if it is previewable.
   * @return true if the mime type is one of a photo and if it is previewable, false otherwise.
   */
  public boolean isPreviewablePhoto() {
    return isReadablePhoto();
  }

  /**
   * Indicates if from the mime type can be read IPTC metadata.
   * @return true if IPTC can be read, false otherwise.
   */
  public boolean isIPTCCompliant() {
    return this == GIF || this == JPG || this == TIFF;
  }
}
