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
import org.silverpeas.util.StringUtil;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * @author: Yohann Chastagnier
 */
public enum MediaResolution {
  TINY(true, "66x50", 66, 50, "66x50"),
  SMALL(true, "133x100", 133, 100, "133x100"),
  MEDIUM(true, "266x150", 266, 150, "266x150"),
  LARGE(false, "600x400", 600, 400, null),
  PREVIEW(false, "preview", 600, 400, "600x400"),
  WATERMARK(false, "watermark", null, null, null),
  ORIGINAL(false, "original", null, null, null);

  @SuppressWarnings("unchecked")
  public final static Set<MediaResolution> ALL =
      Collections.unmodifiableSet(EnumSet.allOf(MediaResolution.class));

  @JsonCreator
  public static MediaResolution fromNameOrLabel(String nameOrLabel) {
    MediaResolution result = null;
    for (MediaResolution mediaResolution : values()) {
      if (mediaResolution.name().toLowerCase().equals(nameOrLabel.toLowerCase()) ||
          (StringUtil.isDefined(mediaResolution.getLabel()) &&
              nameOrLabel.contains(mediaResolution.getLabel()))) {
        result = mediaResolution;
        break;
      }
    }
    return result;
  }

  @JsonValue
  public String getName() {
    return name();
  }

  private final boolean displayed;
  private final String label;
  private final Integer width;
  private final Integer height;
  private final String thumbnailSuffix;
  private final Integer watermarkSize;

  private MediaResolution(final boolean displayed, final String label, final Integer width,
      final Integer height, final String bundlePartOfWaterwarkSizeLabel) {
    this.displayed = displayed;
    this.label = label;
    this.width = width;
    this.height = height;
    this.watermarkSize = GalleryComponentSettings.getWatermarkSize(bundlePartOfWaterwarkSizeLabel);
    this.thumbnailSuffix = "original".equals(label) ? "" : "_" + label + ".jpg";
  }

  /**
   * Indicates if the definition can be displayed to the user.
   * @return true if the definition can be displayed, false otherwise.
   */
  public boolean isDisplayed() {
    return displayed;
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
