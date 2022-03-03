/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.components.gallery.constant;

import org.silverpeas.components.gallery.model.AlbumDetail;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.core.io.media.video.ThumbnailPeriod;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.web.SilverpeasWebResource;

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
  public static final String GALLERY_DOWNLOAD_CONTEXT_PARAM = "downloadContext";
  public static final String GALLERY_VIDEOS_PART = "videos";
  public static final String GALLERY_SOUNDS_PART = "sounds";
  public static final String GALLERY_MEDIA_CONTENT_PART = "content";
  public static final String GALLERY_MEDIA_EMBED_PART = "embed";
  public static final String GALLERY_STREAMINGS_PART = "streamings";
  public static final String GALLERY_MEDIA_THUMBNAIL_PART = "thumbnail";
  public static final String GALLERY_STREAMING_PROVIDER_DATA_PART = "providerData";

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
    return SilverpeasWebResource.getBasePathBuilder().path(GALLERY_BASE_URI)
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
    UriBuilder uriBuilder = SilverpeasWebResource.getBasePathBuilder().path(GALLERY_BASE_URI)
        .path(media.getComponentInstanceId()).path(media.getType().getMediaWebUriPart())
        .path(media.getId()).path(GALLERY_MEDIA_CONTENT_PART)
        .queryParam("_t", media.getLastUpdateDate().getTime());
    if (mediaResolution != null && mediaResolution != MediaResolution.ORIGINAL) {
      uriBuilder.queryParam(GALLERY_PHOTO_RESOLUTION_PARAM, mediaResolution);
    }
    return uriBuilder.build();
  }

  /**
   * Centralized the build of a media embed URI according to the specified resolution.
   * @param media the media.
   * @param mediaResolution the requested resolution.
   * @return the computed URI.
   */
  public static URI buildMediaEmbedURI(Media media, MediaResolution mediaResolution) {
    if (media == null) {
      return null;
    }
    UriBuilder uriBuilder = SilverpeasWebResource.getBasePathBuilder().path(GALLERY_BASE_URI)
        .path(media.getComponentInstanceId()).path(media.getType().getMediaWebUriPart())
        .path(media.getId()).path(GALLERY_MEDIA_EMBED_PART);
    if (mediaResolution != null && mediaResolution != MediaResolution.ORIGINAL) {
      uriBuilder.queryParam(GALLERY_PHOTO_RESOLUTION_PARAM, mediaResolution);
    }
    return uriBuilder.build();
  }

  /**
   * Centralized the build of a video thumbnail URI according to the given ThumbnailPeriod.
   * @param media the media.
   * @param thumbnail the thumbnail period.
   * @return the computed URI.
   */
  public static URI buildVideoThumbnailURI(Media media, ThumbnailPeriod thumbnail) {
    if (media == null) {
      return null;
    }
    UriBuilder uriBuilder =
        SilverpeasWebResource.getBasePathBuilder().path(GALLERY_BASE_URI)
            .path(media.getComponentInstanceId()).path(media.getType().getMediaWebUriPart())
            .path(media.getId()).path(GALLERY_MEDIA_THUMBNAIL_PART)
            .path(Integer.toString(thumbnail.getIndex()))
            .queryParam("_t", media.getLastUpdateDate().getTime());
    return uriBuilder.build();
  }

  private GalleryResourceURIs() {
  }
}
