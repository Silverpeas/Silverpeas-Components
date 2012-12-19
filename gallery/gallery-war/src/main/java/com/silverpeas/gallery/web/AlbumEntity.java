/*
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
package com.silverpeas.gallery.web;

import com.silverpeas.gallery.model.AlbumDetail;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AlbumEntity extends AbstractMediaEntity<AlbumEntity> {
  private static final long serialVersionUID = 9156616894162079317L;

  @XmlElement
  private final Map<String, PhotoEntity> photos = new LinkedHashMap<String, PhotoEntity>();

  @XmlElement(defaultValue = "0")
  private int maxWidth = 0;

  @XmlElement(defaultValue = "0")
  private int maxHeight = 0;

  /**
   * Creates a new album entity from the specified album.
   * @param album
   * @param language
   * @return the entity representing the specified album.
   */
  public static AlbumEntity createFrom(final AlbumDetail album, final String language) {
    return new AlbumEntity(album, language);
  }

  /**
   * Default hidden constructor.
   */
  private AlbumEntity(final AlbumDetail album, final String language) {
    super("album", String.valueOf(album.getId()), album.getName(), album.getDescription(language));
  }

  protected AlbumEntity() {
    super();
  }

  protected Map<String, PhotoEntity> getPhotos() {
    return photos;
  }

  /**
   * Adding a photo the the album.
   * @param photo
   */
  public void addPhoto(final PhotoEntity photo) {
    photos.put(photo.getId(), photo);
    maxWidth = Math.max(maxWidth, photo.getWidth());
    maxHeight = Math.max(maxHeight, photo.getHeight());
  }
}
