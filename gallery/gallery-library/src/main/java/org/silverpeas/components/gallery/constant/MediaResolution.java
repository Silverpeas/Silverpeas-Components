/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.components.gallery.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.silverpeas.components.gallery.GalleryComponentSettings;
import org.silverpeas.kernel.util.StringUtil;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * @author Yohann Chastagnier
 */
public enum MediaResolution {
  TINY(true, "66x50", 66, 50, "66x50"),
  SMALL(true, "133x100", 133, 100, "133x100"),
  MEDIUM(true, "266x150", 266, 150, "266x150"),
  LARGE(false, "600x400", 600, 400, null),
  PREVIEW(false, "preview", 600, 400, "600x400"),
  NORMAL(false, "normal", null, null, null),
  WATERMARK(false, "watermark", null, null, null),
  ORIGINAL(false, "original", null, null, null);

  public static final Set<MediaResolution> ALL =
      Collections.unmodifiableSet(EnumSet.allOf(MediaResolution.class));

  private final boolean displayed;
  private final String label;
  private final Integer width;
  private final Integer height;
  private final String thumbnailSuffix;
  private final String watermarkSizeLabel;

  MediaResolution(final boolean displayed, final String label, final Integer width,
      final Integer height, final String bundlePartOfWatermarkSizeLabel) {
    this.displayed = displayed;
    this.label = label;
    this.width = width;
    this.height = height;
    this.watermarkSizeLabel = bundlePartOfWatermarkSizeLabel;
    this.thumbnailSuffix = "original".equals(label) ? "" : ("_" + label);
  }

  @JsonCreator
  public static MediaResolution fromNameOrLabel(String nameOrLabel) {
    MediaResolution result = null;
    for (MediaResolution mediaResolution : values()) {
      if (mediaResolution.name().equalsIgnoreCase(nameOrLabel) ||
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
    return getWatermarkSize() != null;
  }

  /**
   * Gets the size of the watermark to apply to a media of this resolution. The settings are read
   * on demand and not once for all at the initialization of this enumeration: the resources of
   * Silverpeas aren't necessarily available when the class is loaded (this is the case for
   * example when the class is loaded by a documentation or a code analysis tool).
   * @return the size of the watermark or null if no watermark is applicable.
   */
  public Integer getWatermarkSize() {
    return watermarkSizeLabel == null ? null :
        GalleryComponentSettings.getWatermarkSize(watermarkSizeLabel);
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
