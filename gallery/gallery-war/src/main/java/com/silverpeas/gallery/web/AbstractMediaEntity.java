/*
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
package com.silverpeas.gallery.web;

import com.silverpeas.gallery.constant.MediaResolution;
import com.silverpeas.gallery.constant.MediaType;
import com.silverpeas.gallery.model.Media;
import com.silverpeas.web.WebEntity;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
 * Web entity abstraction which provides common media informations of the entity
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractMediaEntity<T extends AbstractMediaEntity<T>> implements WebEntity {
  private static final long serialVersionUID = -5619051121965308574L;

  @XmlElement(required = true, defaultValue = "")
  private MediaType type;

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

  @XmlElement(defaultValue = "")
  private String author;

  @XmlElement(defaultValue = "")
  private URI thumbUrl;

  @XmlElement(defaultValue = "")
  private URI url;

  @XmlElement(defaultValue = "0")
  private int width = MediaResolution.MEDIUM.getWidth();

  @XmlElement(defaultValue = "0")
  private int height = MediaResolution.MEDIUM.getHeight();

  /**
   * Sets a URI to this entity. With this URI, it can then be accessed through the Web.
   * @param uri the web entity URI.
   * @return itself.
   */
  @SuppressWarnings("unchecked")
  public T withURI(final URI uri) {
    this.uri = uri;
    return (T) this;
  }

  /**
   * Sets a parentURI to this entity.
   * @param parentURI the parent web entity URI.
   * @return itself.
   */
  @SuppressWarnings("unchecked")
  public T withParentURI(final URI parentURI) {
    this.parentURI = parentURI;
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T withOriginalUrl(final URI originalUrl) {
    url = originalUrl;
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T withThumbUrl(final URI thumbUrl) {
    this.thumbUrl = thumbUrl;
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T withWidth(final int width) {
    this.width = width;
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T withHeight(final int height) {
    this.height = height;
    return (T) this;
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

  public void setType(final MediaType type) {
    this.type = type;
  }

  public MediaType getType() {
    return type;
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

  protected void setAuthor(final String author) {
    this.author = author;
  }

  protected String getAuthor() {
    return author;
  }

  /**
   * @return the thumbUrl
   */
  public URI getThumbUrl() {
    return thumbUrl;
  }

  /**
   * @return the url
   */
  public URI getUrl() {
    return url;
  }

  /**
   * @return the height
   */
  public int getHeight() {
    return height;
  }

  /**
   * @return the width
   */
  public int getWidth() {
    return width;
  }

  /**
   * Instantiating a new web entity from the corresponding data
   * @param media the media
   */
  protected AbstractMediaEntity(final Media media) {
    this.type = media != null ? media.getType() : MediaType.Unknown;
    this.id = media == null || media.getId() == null ? "" : media.getId();
    this.title = media == null || media.getTitle() == null ? "" : media.getTitle();
    this.description =
        media == null || media.getDescription() == null ? "" : media.getDescription();
    this.author = media == null || media.getAuthor() == null ? "" : media.getAuthor();
  }

  protected AbstractMediaEntity() {
    this(null);
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(getType()).append(getId()).toHashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj.getClass() != getClass()) {
      return false;
    }
    final AbstractMediaEntity<?> other = (AbstractMediaEntity<?>) obj;
    return new EqualsBuilder().append(getType(), other.getType()).append(getId(), other.getId())
        .isEquals();
  }
}
