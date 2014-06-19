/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.socialNetwork;

import com.silverpeas.gallery.model.InternalMedia;
import com.silverpeas.gallery.model.MediaWithStatus;
import com.silverpeas.socialnetwork.model.SocialInformation;
import com.silverpeas.socialnetwork.model.SocialInformationType;
import com.silverpeas.util.StringUtil;

import java.util.Date;

public class SocialInformationGallery implements SocialInformation {

  private final SocialInformationType type = SocialInformationType.MEDIA;
  private String title;
  private String description;
  private boolean socialInformationWasupdated = false;
  private String author;
  private long dateTime;
  private String url;
  private String icon;

  public SocialInformationGallery(MediaWithStatus picture) {

    this.title = picture.getMedia().getTitle();

    this.socialInformationWasupdated = picture.isUpdate();

    this.description = picture.getMedia().getDescription();
    if (!StringUtil.isDefined(description)) {
      description = "";
    }
    if (socialInformationWasupdated) {
      this.author = picture.getMedia().getLastUpdatedBy();
      this.dateTime = picture.getMedia().getLastUpdateDate().getTime();
    } else {
      this.author = picture.getMedia().getCreatorId();
      dateTime = picture.getMedia().getCreationDate().getTime();
    }
    this.url =
        "/Rgallery/" + picture.getMedia().getInstanceId() + "/" + picture.getMedia().getURL();
    String id = picture.getMedia().getId();
    String mimeType = "streaming";
    if (picture.getMedia() instanceof InternalMedia) {
      mimeType = ((InternalMedia) picture.getMedia()).getFileMimeType();
    }
    this.icon =
        "/FileServer/" + id + "_preview.jpg?ComponentId=" + picture.getMedia().getInstanceId() +
            "&SourceFile=" + id + "_preview.jpg&MimeType=" + mimeType + "&Directory=image" + id;
  }

  /**
   * return the Title of this SocialInformation
   *
   * @return String
   */
  @Override
  public String getTitle() {
    return title;
  }

  /**
   * return the Description of this SocialInformation
   *
   * @return String
   */
  @Override
  public String getDescription() {
    return description;
  }

  /**
   * return the Author of this SocialInfo
   *
   * @return String
   */
  @Override
  public String getAuthor() {
    return author;
  }

  /**
   * return the Url of this SocialInfo
   *
   * @return String
   */
  @Override
  public String getUrl() {
    return url;
  }

  /**
   * return the Date of this SocialInfo
   *
   * @return
   */
  @Override
  public Date getDate() {
    return new Date(dateTime);
  }

  /**
   * return the icon of this SocialInformation
   *
   * @return String
   */
  @Override
  public String getIcon() {
    return icon;
  }

  /**
   * return the type of this SocialInformation
   *
   * @return String
   */
  @Override
  public String getType() {
    return type.toString();
  }

  /**
   * return if this socialInfo was updtated or not
   *
   * @return boolean
   */
  @Override
  public boolean isUpdeted() {
    return socialInformationWasupdated;
  }

  /*
   * (non-Javadoc) @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof SocialInformationGallery)) {
      return false;
    }
    SocialInformationGallery other = (SocialInformationGallery) obj;
    if (type.toString() == null) {
      if (other.type.toString() != null) {
        return false;
      }
    } else if (!type.toString().equals(other.type.toString())) {
      return false;
    }

    return true;
  }

  @Override
  public int compareTo(SocialInformation o) {
    return o.getDate().compareTo(getDate());
  }
}
