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
package org.silverpeas.components.gallery.model;

import org.silverpeas.components.gallery.constant.MediaResolution;
import org.silverpeas.components.gallery.constant.MediaType;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.io.media.Definition;

/**
 * This class represents a Photo.
 */
public class Photo extends InternalMedia {
  private static final long serialVersionUID = 262504401033860860L;

  private Definition definition = Definition.fromZero();

  public Photo() {
    super();
  }

  protected Photo(final Photo other) {
    super(other);
    this.definition = other.definition;
  }

  @Override
  public MediaType getType() {
    return MediaType.Photo;
  }

  /**
   * Gets the definition of the photo.
   * @return the definition of the photo.
   */
  public Definition getDefinition() {
    return definition;
  }

  /**
   * Sets the definition of the resolution.
   * @param definition the definition of the resolution.
   */
  public void setDefinition(Definition definition) {
    this.definition = definition != null ? definition : Definition.fromZero();
  }

  /**
   * The type of this resource
   * @return the same value returned by getContributionType()
   */
  public static String getResourceType() {
    return MediaType.Photo.name();
  }

  @Override
  public boolean isPreviewable() {
    return StringUtil.isDefined(getFileName()) && getFileMimeType().isPreviewablePhoto();
  }

  @Override
  public String getApplicationEmbedUrl(final MediaResolution mediaResolution) {
    return "";
  }

  @Override
  public Photo getCopy() {
    return new Photo(this);
  }
}