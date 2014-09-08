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
package com.silverpeas.gallery.model;

import com.silverpeas.gallery.constant.MediaType;
import org.silverpeas.media.Definition;

/**
 * This class represents a Video.
 */
public class Video extends InternalMedia {
  private static final long serialVersionUID = 5772513957256327862L;

  private Definition definition = Definition.fromZero();
  private long bitrate = 0;
  private long duration = 0;

  @Override
  public MediaType getType() {
    return MediaType.Video;
  }

  /**
   * Gets the definition of the resolution.
   * @return the definition of the resolution.
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
   * Gets the number of bytes delivered per seconds.
   * @return number of byte delivered per seconds.
   */
  public long getBitrate() {
    return bitrate;
  }

  /**
   * Sets the number of bytes delivered per seconds.
   * @param bitrate the number of bytes delivered per seconds.
   */
  public void setBitrate(final long bitrate) {
    this.bitrate = bitrate;
  }

  /**
   * Gets the duration in milliseconds.
   * @return the duration in milliseconds.
   */
  public long getDuration() {
    return duration;
  }

  /**
   * Sets the duration in milliseconds.
   * @param durationInMs the duration in milliseconds.
   */
  public void setDuration(final long durationInMs) {
    this.duration = durationInMs;
  }

  /**
   * The type of this resource
   * @return the same value returned by getContributionType()
   */
  public static String getResourceType() {
    return MediaType.Video.name();
  }
}