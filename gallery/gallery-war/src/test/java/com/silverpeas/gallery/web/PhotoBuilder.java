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

import java.sql.Date;

import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.model.PhotoPK;

/**
 * @author Yohann Chastagnier
 */
public class PhotoBuilder {

  public static PhotoBuilder getPhotoBuilder() {
    return new PhotoBuilder();
  }

  public PhotoDetail buildPhoto(final String photoId, final String componentId) {
    return new PhotoMock(photoId, componentId);
  }

  private PhotoBuilder() {
    // Nothing to do
  }

  protected class PhotoMock extends PhotoDetail {
    private static final long serialVersionUID = -1043834505447908434L;

    public PhotoMock(final String photoId, final String componentId) {
      super("title" + photoId, "description" + photoId, Date.valueOf("2012-01-01"), null,
          "2012/01/01", "author", true, true);
      setPhotoPK(new PhotoPK(photoId, componentId));
      setImageName("imageName" + photoId);
    }
  }
}
