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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.silverpeas.gallery.model.PhotoDetail;
import com.stratelia.webactiv.util.FileServerUtils;

/**
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PhotoEntity extends AbstractMediaEntity<PhotoEntity> {
  private static final long serialVersionUID = -4634076513167690314L;

  private final static String PREVIEW_SUFFIX = "_preview.jpg";

  @XmlElement(defaultValue = "")
  private String previewUrl;

  @XmlElement(defaultValue = "")
  private String url;

  @XmlElement(defaultValue = "0")
  private int width;

  @XmlElement(defaultValue = "0")
  private int height;

  @XmlTransient
  private final PhotoDetail photo;

  /**
   * Creates a new photo entity from the specified photo.
   * @param photo
   * @param language
   * @return the entity representing the specified photo.
   */
  public static PhotoEntity createFrom(final PhotoDetail photo, final String language) {
    return new PhotoEntity(photo, language);
  }

  /**
   * Default hidden constructor.
   */
  private PhotoEntity(final PhotoDetail photo, final String language) {
    super("photo", photo.getId(), photo.getTitle(), photo.getDescription(language));
    this.photo = photo;
    previewUrl = buildURL(new StringBuilder().append(photo.getId()).append(PREVIEW_SUFFIX).toString());
    url = photo.isDownloadable() ? buildURL(photo.getImageName()) : previewUrl;
    width = photo.getSizeL();
    height = photo.getSizeH();
  }

  protected PhotoEntity() {
    super();
    photo = null;
  }

  /**
   * @return the previewUrl
   */
  public String getPreviewUrl() {
    return previewUrl;
  }

  /**
   * @return the url
   */
  public String getUrl() {
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
   * Centralized method to build an Photo URL.
   * @param imageName
   * @return
   */
  private String buildURL(final String imageName) {
    return FileServerUtils.getUrl(null, photo.getInstanceId(), imageName, photo.getImageMimeType(),
        new StringBuilder(gallerySettings.getString("imagesSubDirectory")).append(photo.getId())
            .toString());
  }
}
