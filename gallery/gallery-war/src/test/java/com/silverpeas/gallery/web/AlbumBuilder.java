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

import java.util.ArrayList;
import java.util.Collection;

import com.silverpeas.gallery.model.AlbumDetail;
import com.silverpeas.gallery.model.PhotoDetail;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

/**
 * @author Yohann Chastagnier
 */
public class AlbumBuilder {

  public static AlbumBuilder getAlbumBuilder() {
    return new AlbumBuilder();
  }

  public AlbumMock buildAlbum(final String albumId) {
    return new AlbumMock(albumId);
  }

  private AlbumBuilder() {
    // Nothing to do
  }

  protected class AlbumMock extends AlbumDetail {
    private static final long serialVersionUID = -1043834505447908434L;

    public AlbumMock(final String albumId) {
      super(new NodeDetail());
      setName("name" + albumId);
      setDescription("description" + albumId);
      setNodePK(new NodePK(albumId));
      final Collection<PhotoDetail> photos = new ArrayList<PhotoDetail>();
      setPhotos(photos);
    }

    public AlbumMock addPhoto(final PhotoDetail photo) {
      getPhotos().add(photo);
      return this;
    }
  }
}
