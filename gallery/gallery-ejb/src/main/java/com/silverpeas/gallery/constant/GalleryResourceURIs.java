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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.constant;


import com.silverpeas.gallery.model.AlbumDetail;
import com.silverpeas.gallery.model.Media;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.util.node.model.NodePK;
import org.silverpeas.settings.SilverpeasSettings;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * Base URIs from which the REST-based ressources representing gallery entities are defined.
 * @author Yohann Chastagnier
 */
public final class GalleryResourceURIs {

  public static final String GALLERY_BASE_URI = "gallery";
  public static final String GALLERY_ALBUMS_URI_PART = "albums";
  public static final String GALLERY_PHOTOS_PART = "photos";
  public static final String GALLERY_PHOTO_RESOLUTION_PARAM = "resolution";
  public static final String GALLERY_VIDEOS_PART = "videos";
  public static final String GALLERY_SOUNDS_PART = "sounds";
  public static final String GALLERY_MEDIA_CONTENT_PART = "content";

  /**
   * Centralized the build of a album URI.
   * @param album the album.
   * @return the computed URI.
   */
  public static URI buildAlbumURI(AlbumDetail album) {
    if (album == null) {
      return null;
    }
    return buildAlbumURI(album.getNodePK());
  }

  /**
   * Centralized the build of a album URI.
   * @param albumPk the album.
   * @return the computed URI.
   */
  public static URI buildAlbumURI(NodePK albumPk) {
    if (albumPk == null) {
      return null;
    }
    return UriBuilder.fromUri(URLManager.getApplicationURL())
        .path(SilverpeasSettings.getRestWebServicesUriBase()).path(GALLERY_BASE_URI)
        .path(albumPk.getInstanceId()).path(GALLERY_ALBUMS_URI_PART).path(albumPk.getId()).build();
  }

  /**
   * Centralized the build of a media URI contained in a specified album.
   * @param album the album that contains the media.
   * @param media the media.
   * @return the computed URI.
   */
  public static URI buildMediaInAlbumURI(AlbumDetail album, Media media) {
    if (album == null || media == null) {
      return null;
    }
    return UriBuilder.fromUri(buildAlbumURI(album)).path(media.getType().getMediaWebUriPart())
        .path(media.getId()).build();
  }

  /**
   * Centralized the build of a media content URI according to the specified resolution.
   * @param media the media.
   * @param mediaResolution the requested resolution.
   * @return the computed URI.
   */
  public static URI buildMediaContentURI(Media media, MediaResolution mediaResolution) {
    if (media == null) {
      return null;
    }
    UriBuilder uriBuilder = UriBuilder.fromUri(URLManager.getApplicationURL())
        .path(SilverpeasSettings.getRestWebServicesUriBase()).path(GALLERY_BASE_URI)
        .path(media.getComponentInstanceId()).path(media.getType().getMediaWebUriPart())
        .path(media.getId()).path(GALLERY_MEDIA_CONTENT_PART)
        .queryParam("_t", media.getLastUpdateDate().getTime());
    if (mediaResolution != null && mediaResolution != MediaResolution.ORIGINAL) {
      uriBuilder.queryParam(GALLERY_PHOTO_RESOLUTION_PARAM, mediaResolution);
    }
    return uriBuilder.build();
  }

  private GalleryResourceURIs() {
  }
}
