/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.socialNetwork;

import com.silverpeas.gallery.model.PhotoWithStatus;
import com.silverpeas.socialNetwork.model.SocialInformation;
import com.silverpeas.socialNetwork.model.SocialInformationType;
import com.silverpeas.util.StringUtil;
import java.sql.Timestamp;
import java.util.Date;

public class SocialInformationGallery implements SocialInformation {

  private final SocialInformationType type = SocialInformationType.PHOTO;
  private String title;
  private String description;
  private boolean socialInformationWasupdated = false;
  private String author;
  private Timestamp date;
  private String url;
  private String icon;

  public SocialInformationGallery(PhotoWithStatus picture) {

    this.title = picture.getPhoto().getTitle();

    this.socialInformationWasupdated = picture.isUpdate();

    this.description = picture.getPhoto().getDescription();
    if (!StringUtil.isDefined(description));
    description = "";
    if (socialInformationWasupdated) {
      this.author = picture.getPhoto().getUpdateId();
      this.date = new java.sql.Timestamp(picture.getPhoto().getUpdateDate().getTime());
    } else {
      this.author = picture.getPhoto().getCreatorId();
      this.date = new java.sql.Timestamp(picture.getPhoto().getCreationDate().getTime());
    }
    // this.url = URLManager.getURL(picture.getPhoto().getInstanceId())+ picture.getPhoto().getURL();
    this.url = "/Rgallery/" + picture.getPhoto().getInstanceId() + "/" + picture.getPhoto().getURL();
    String id = picture.getPhoto().getId();
    this.icon = "/FileServer/" + id + "_preview.jpg?ComponentId=" + picture.getPhoto().getInstanceId() + "&SourceFile=" + id + "_preview.jpg&MimeType="
        + picture.getPhoto().getImageMimeType() + "&Directory=image" + id;


  }

  /**
   * return the Title of this SocialInformation
   * @return String
   */
  @Override
  public String getTitle() {
    return title;
  }

  /**
   * return the Description of this SocialInformation
   * @return String
   */
  @Override
  public String getDescription() {
    return description;
  }

  /**
   * return the Author of this SocialInfo
   * @return String
   */
  @Override
  public String getAuthor() {
    return author;
  }

  /**
   * return the Url of this SocialInfo
   * @return String
   */
  @Override
  public String getUrl() {
    return url;
  }

  /**
   * return the Date of this SocialInfo
   * @return
   */
  @Override
  public Date getDate() {
    return date;
  }

  /**
   * return the icon of this SocialInformation
   * @return String
   */
  @Override
  public String getIcon() {
    return icon;
  }

  /**
   * return the type of this SocialInformation
   * @return String
   */
  @Override
  public String getType() {
    // TODO Auto-generated method stub
    return type.toString();
  }

  /**
   * return if this socialInfo was updtated or not
   * @return boolean
   */
  @Override
  public boolean isUpdeted() {
    return socialInformationWasupdated;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
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
    return getDate().compareTo(o.getDate()) * -1;
  }
}
