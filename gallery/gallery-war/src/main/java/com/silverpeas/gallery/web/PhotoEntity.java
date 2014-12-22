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

  /**
   * Creates a new photo entity from the specified photo.
   * @param photo
   * @return the entity representing the specified photo.
   */
  public static PhotoEntity createFrom(final Photo photo) {
    return new PhotoEntity(photo);
  }

  public PhotoEntity withPreviewUrl(final URI previewUrl) {
    this.previewUrl = previewUrl;
    return this;
  }

  /**
   * @return the previewUrl
   */
  public URI getPreviewUrl() {
    return previewUrl;
  }

  /**
   * Default hidden constructor.
   */
  private PhotoEntity(final Photo photo) {
    super(photo);
    withWidth(photo.getDefinition().getWidth());
    withHeight(photo.getDefinition().getHeight());
  }

  @SuppressWarnings("UnusedDeclaration")
  protected PhotoEntity() {
    super();
  }
}
