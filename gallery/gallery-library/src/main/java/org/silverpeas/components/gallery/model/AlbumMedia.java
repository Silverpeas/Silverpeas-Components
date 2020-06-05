/*
 * Copyright (C) 2000 - 2020 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.gallery.model;

import org.silverpeas.core.node.model.NodePK;

import java.io.Serializable;

import static org.silverpeas.components.gallery.service.MediaServiceProvider.getMediaService;

/**
 * Represents a couple of an album and a media.
 * <p>
 * It is useful when manipulating data that have just been created, updated or deleted.
 * </p>
 * @author silveryocha
 */
public class AlbumMedia implements Serializable {
  private static final long serialVersionUID = -8463214847935683882L;

  private final String albumId;
  private final Media media;

  private AlbumDetail album;

  public AlbumMedia(final String albumId, final Media media) {
    this.media = media;
    this.albumId = albumId;
  }

  /**
   * Gets the identifier of the album which has been concerned by the manipulation of the
   * {@link Media}.
   * @return a string.
   */
  public String getAlbumId() {
    return albumId;
  }

  /**
   * Gets the {@link AlbumDetail} which has been concerned by the manipulation of the {@link Media}.
   * @return the cached {@link AlbumDetail}.
   */
  public AlbumDetail getAlbum() {
    if (album == null) {
      album = getMediaService().getAlbum(new NodePK(albumId, media.getInstanceId()));
    }
    return album;
  }

  /**
   * The media.
   * @return a {@link Media}.
   */
  public Media getMedia() {
    return media;
  }
}
