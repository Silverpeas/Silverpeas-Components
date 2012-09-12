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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.delegate;

import java.util.LinkedHashMap;
import java.util.Map;

import com.silverpeas.gallery.model.AlbumDetail;
import com.silverpeas.gallery.model.PhotoDetail;

/**
 * @author Yohann Chastagnier
 */
public class GalleryPasteDelegate {

  private final AlbumDetail album;
  private final Map<PhotoDetail, Boolean> photosToPaste = new LinkedHashMap<PhotoDetail, Boolean>();
  private final Map<AlbumDetail, Boolean> albumsToPaste = new LinkedHashMap<AlbumDetail, Boolean>();

  /**
   *
   */
  public GalleryPasteDelegate(final AlbumDetail album) {
    this.album = album;
  }

  /**
   * Adds a photo to paste
   * @param photo
   * @param isCutted
   */
  public void addPhoto(final PhotoDetail photo, final boolean isCutted) {
    photosToPaste.put(photo, isCutted);
  }

  /**
   * Adds an album to paste
   * @param album
   * @param isCutted
   */
  public void addAlbum(final AlbumDetail album, final boolean isCutted) {
    albumsToPaste.put(album, isCutted);
  }

  /**
   * @return the album
   */
  public AlbumDetail getAlbum() {
    return album;
  }

  /**
   * @return the photosToPaste
   */
  public Map<PhotoDetail, Boolean> getPhotosToPaste() {
    return photosToPaste;
  }

  /**
   * @return the albumsToPaste
   */
  public Map<AlbumDetail, Boolean> getAlbumsToPaste() {
    return albumsToPaste;
  }
}
