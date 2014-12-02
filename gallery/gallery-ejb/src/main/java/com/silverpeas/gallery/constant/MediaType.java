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

import com.silverpeas.gallery.model.Media;
import org.silverpeas.util.StringUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author: Yohann Chastagnier
 */
public enum MediaType {
  Unknown(null), Photo(com.silverpeas.gallery.model.Photo.class),
  Video(com.silverpeas.gallery.model.Video.class),
  Sound(com.silverpeas.gallery.model.Sound.class),
  Streaming(com.silverpeas.gallery.model.Streaming.class);

  private final Class<? extends Media> mediaClass;
  private final String mediaWebUriPart;

  private MediaType(final Class<? extends Media> mediaClass) {
    this.mediaClass = mediaClass;
    mediaWebUriPart = name().toLowerCase() + "s";
  }

  /**
   * Gets the enum instance according to the specified type.
   * @param type
   * @return
   */
  @JsonCreator
  public static MediaType from(String type) {
    try {
      return valueOf(StringUtil.capitalize(type));
    } catch (Exception e) {
      return Unknown;
    }
  }

  @JsonValue
  public String getName() {
    return name();
  }

  /**
   * Instantiates a new model instance according to the media type.
   * @param <MEDIA>
   * @return
   */
  @SuppressWarnings("unchecked")
  public <MEDIA extends Media> MEDIA newInstance() {
    try {
      return (MEDIA) mediaClass.newInstance();
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Indicates if current type is the photo one.
   * @return
   */
  public boolean isPhoto() {
    return Photo == this;
  }

  /**
   * Indicates if current type is the video one.
   * @return
   */
  public boolean isVideo() {
    return Video == this;
  }

  /**
   * Indicates if current type is the sound one.
   * @return
   */
  public boolean isSound() {
    return Sound == this;
  }

  /**
   * Indicates if current type is the streaming one.
   * @return
   */
  public boolean isStreaming() {
    return Streaming == this;
  }

  /**
   * Gets the prefix folder name of a media on Silverpeas workspace.
   * @return
   */
  public String getTechnicalFolder() {
    switch (this) {
      case Photo:
        return "image";
      default:
        return this.name().toLowerCase();
    }
  }

  /**
   * Gets the media Web Uri part.
   * @return
   */
  public String getMediaWebUriPart() {
    return mediaWebUriPart;
  }
}