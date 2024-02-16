/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery.socialnetwork;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.components.gallery.model.InternalMedia;
import org.silverpeas.components.gallery.model.MediaWithStatus;
import org.silverpeas.core.socialnetwork.model.AbstractSocialInformation;
import org.silverpeas.core.socialnetwork.model.SocialInformationType;
import org.silverpeas.kernel.util.StringUtil;

public class SocialInformationGallery extends AbstractSocialInformation {

  public SocialInformationGallery(MediaWithStatus picture) {
    super(picture.getMedia().getMediaPK().toResourceReference());
    setTitle(picture.getMedia().getTitle());
    setUpdated(picture.isUpdate());

    String description = picture.getMedia().getDescription();
    if (!StringUtil.isDefined(description)) {
      description = "";
    }
    setDescription(description);

    if (isUpdated()) {
      setAuthor(picture.getMedia().getLastUpdatedBy());
      setDate(picture.getMedia().getLastUpdateDate());
    } else {
      setAuthor(picture.getMedia().getCreatorId());
      setDate(picture.getMedia().getCreationDate());
    }
    setUrl("/Rgallery/" + picture.getMedia().getInstanceId() + "/" + picture.getMedia().getURL());
    String id = picture.getMedia().getId();
    String mimeType = "streaming";
    if (picture.getMedia() instanceof InternalMedia) {
      mimeType = ((InternalMedia) picture.getMedia()).getFileMimeType().getMimeType();
    }
    String icon =
        "/FileServer/" + id + "_preview.jpg?ComponentId=" + picture.getMedia().getInstanceId() +
            "&SourceFile=" + id + "_preview.jpg&MimeType=" + mimeType + "&Directory=image" + id;
    setIcon(icon);
    setType(SocialInformationType.MEDIA.toString());
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
    return this.url == null ? other.url == null : this.url.equals(other.url);
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(11, 19).append(getType()).append(getTitle())
        .append(getDescription()).append(getAuthor()).append(getDate()).append(getUrl())
        .toHashCode();
  }

}
