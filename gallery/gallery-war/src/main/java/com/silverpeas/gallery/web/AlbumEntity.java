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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.web;

import com.silverpeas.gallery.model.AlbumDetail;
import com.silverpeas.web.WebEntity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AlbumEntity implements WebEntity {
  private static final long serialVersionUID = 9156616894162079317L;

  @XmlElement(defaultValue = "")
  private URI uri;

  @XmlElement(defaultValue = "")
  private URI parentURI = null;

  @XmlElement(required = true)
  @NotNull
  private String id;

  @XmlElement(required = true)
  @NotNull
  @Size(min = 1)
  private String title;

  @XmlElement(defaultValue = "")
  private String description;

  @XmlElement
  private final Map<String, AbstractMediaEntity> mediaList =
      new LinkedHashMap<String, AbstractMediaEntity>();

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
   * Sets a URI to this entity. With this URI, it can then be accessed through the Web.
   * @param uri the web entity URI.
   * @return itself.
   */
  public AlbumEntity withURI(final URI uri) {
    this.uri = uri;
    return this;
  }

  /**
   * Sets a parentURI to this entity.
   * @param parentURI the parent web entity URI.
   * @return itself.
   */
  public AlbumEntity withParentURI(final URI parentURI) {
    this.parentURI = parentURI;
    return this;
  }

  /*
     * (non-Javadoc)
     * @see com.silverpeas.web.WebEntity#getURI()
     */
  @Override
  public URI getURI() {
    return uri;
  }

  protected URI getParentURI() {
    return parentURI;
  }

  protected void setId(final String id) {
    this.id = id;
  }

  protected String getId() {
    return id;
  }

  protected void setTitle(final String title) {
    this.title = title;
  }

  protected String getTitle() {
    return title;
  }

  protected void setDescription(final String description) {
    this.description = description;
  }

  protected String getDescription() {
    return description;
  }

  /**
   * Default hidden constructor.
   */
  private AlbumEntity(final AlbumDetail album, final String language) {
    this.id = String.valueOf(album.getId());
    this.title = album.getName();
    this.description = album.getDescription(language);
  }

  protected AlbumEntity() {
  }

  protected Map<String, AbstractMediaEntity> getMediaList() {
    return mediaList;
  }

  /**
   * Adding a media the the album.
   * @param mediaEntity
   */
  public void addMedia(final AbstractMediaEntity mediaEntity) {
    mediaList.put(mediaEntity.getId(), mediaEntity);
    maxWidth = Math.max(maxWidth, mediaEntity.getWidth());
    maxHeight = Math.max(maxHeight, mediaEntity.getHeight());
  }
}
