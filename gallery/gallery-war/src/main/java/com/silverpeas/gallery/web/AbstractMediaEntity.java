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

import java.net.URI;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.silverpeas.web.Exposable;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * Web entity abstraction which provides common media informations of the entity
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractMediaEntity<T extends AbstractMediaEntity<T>> implements Exposable {
  private static final long serialVersionUID = -5619051121965308574L;

  protected final static ResourceLocator gallerySettings = new ResourceLocator(
      "com.silverpeas.gallery.settings.gallerySettings", "");

  @XmlElement(required = true, defaultValue = "")
  private final String type;

  @XmlElement(defaultValue = "")
  private URI uri;

  @XmlElement(defaultValue = "")
  private URI parentURI = null;

  @XmlElement(required = true)
  @NotNull
  @Pattern(regexp = "^[0-9]+$")
  private final String id;

  @XmlElement(required = true)
  @NotNull
  @Size(min = 1)
  private final String title;

  @XmlElement(defaultValue = "")
  private final String description;

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

  public String getType() {
    return type;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.web.Exposable#getURI()
   */
  @Override
  public URI getURI() {
    return uri;
  }

  protected URI getParentURI() {
    return parentURI;
  }

  protected String getId() {
    return id;
  }

  protected String getTitle() {
    return title;
  }

  protected String getDescription() {
    return description;
  }

  /**
   * Instantiating a new web entity from the corresponding data
   * @param component
   */
  protected AbstractMediaEntity(final String type, final String id, final String title,
      final String description) {
    this.type = type;
    this.id = id == null ? "" : id;
    this.title = title == null ? "" : title;
    this.description = description == null ? "" : description;
  }

  protected AbstractMediaEntity() {
    this("", "", "", "");
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(getType()).append(id).toHashCode();
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
    return new EqualsBuilder().append(getType(), other.getType()).append(id, other.getId())
        .isEquals();
  }
}
