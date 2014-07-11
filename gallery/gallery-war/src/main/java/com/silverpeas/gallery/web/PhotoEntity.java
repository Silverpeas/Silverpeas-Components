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

import com.silverpeas.gallery.model.Photo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PhotoEntity extends AbstractMediaEntity<PhotoEntity> {
  private static final long serialVersionUID = -4634076513167690314L;

  @XmlElement(defaultValue = "")
  private URI previewUrl;

  @XmlElement(defaultValue = "")
  private URI smallUrl;

  @XmlElement(defaultValue = "")
  private URI url;

  @XmlElement(defaultValue = "0")
  private int width;

  @XmlElement(defaultValue = "0")
  private int height;

  @Override
  public PhotoEntity withURI(final URI uri) {
    return super.withURI(uri);
  }

  public PhotoEntity withOriginalUrl(final URI originalUrl) {
    url = originalUrl;
    return this;
  }

  public PhotoEntity withSmallUrl(final URI smallUrl) {
    this.smallUrl = smallUrl;
    return this;
  }

  public PhotoEntity withPreviewUrl(final URI previewUrl) {
    this.previewUrl = previewUrl;
    return this;
  }

  /**
   * Creates a new photo entity from the specified photo.
   * @param photo
   * @param language
   * @return the entity representing the specified photo.
   */
  public static PhotoEntity createFrom(final Photo photo, final String language) {
    return new PhotoEntity(photo, language);
  }

  /**
   * Default hidden constructor.
   */
  private PhotoEntity(final Photo photo, final String language) {
    super("photo", photo.getId(), photo.getTitle(), photo.getDescription(language));
    width = photo.getDefinition().getWidth();
    height = photo.getDefinition().getHeight();
  }

  @SuppressWarnings("UnusedDeclaration")
  protected PhotoEntity() {
    super();
  }

  /**
   * @return the previewUrl
   */
  public URI getPreviewUrl() {
    return previewUrl;
  }

  /**
   * @return the smallUrl
   */
  public URI getSmallUrl() {
    return smallUrl;
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
}
