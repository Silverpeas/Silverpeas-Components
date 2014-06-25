/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.constant;

import com.silverpeas.gallery.GalleryComponentSettings;

/**
 * @author: Yohann Chastagnier
 */
public enum MediaResolution {
  TINY("66x50", 66, 50, "66x50"),
  SMALL("133x100", 133, 100, "133x100"),
  MEDIUM("266x150", 266, 150, "266x150"),
  LARGE("600x400", 600, 400, null),
  PREVIEW("preview", 600, 400, "600x400"),
  WATERMARK("watermark", null, null, null);

  public static MediaResolution fromNameOrLabel(String nameOrLabel) {
    MediaResolution result = null;
    for (MediaResolution mediaResolution : values()) {
      if (mediaResolution.name().toLowerCase().equals(nameOrLabel.toLowerCase()) ||
          nameOrLabel.contains(mediaResolution.getLabel())) {
        result = mediaResolution;
        break;
      }
    }
    return result;
  }

  private final String label;
  private final Integer width;
  private final Integer height;
  private final String thumbnailSuffix;
  private final Integer watermarkSize;

  private MediaResolution(final String label, final Integer width, final Integer height,
      final String bundlePartOfWaterwarkSizeLabel) {
    this.label = label;
    this.width = width;
    this.height = height;
    this.watermarkSize = GalleryComponentSettings.getWatermarkSize(bundlePartOfWaterwarkSizeLabel);
    this.thumbnailSuffix = "_" + label + ".jpg";
  }

  public String getLabel() {
    return label;
  }

  public Integer getWidth() {
    return width;
  }

  public Integer getHeight() {
    return height;
  }

  public String getThumbnailSuffix() {
    return thumbnailSuffix;
  }

  public boolean isWatermarkApplicable() {
    return watermarkSize != null;
  }

  public Integer getWatermarkSize() {
    return watermarkSize;
  }

  public boolean isTiny() {
    return this == TINY;
  }

  public boolean isSmall() {
    return this == SMALL;
  }

  public boolean isMedium() {
    return this == MEDIUM;
  }

  public boolean isLarge() {
    return this == LARGE;
  }

  public boolean isPreview() {
    return this == PREVIEW;
  }

  public boolean isWatermark() {
    return this == WATERMARK;
  }
}
