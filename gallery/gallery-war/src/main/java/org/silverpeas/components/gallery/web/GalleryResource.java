/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery.web;

import org.jboss.resteasy.plugins.providers.html.View;
import org.silverpeas.components.gallery.constant.MediaResolution;
import org.silverpeas.components.gallery.model.AlbumDetail;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.model.MediaPK;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.web.rs.annotation.Authorized;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static org.silverpeas.components.gallery.constant.GalleryResourceURIs.*;
import static org.silverpeas.components.gallery.constant.MediaType.*;

/**
 * A REST Web resource giving gallery data.
 * @author Yohann Chastagnier
 */
@WebService
@Path(GALLERY_BASE_URI + "/{componentInstanceId}")
@Authorized
public class GalleryResource extends AbstractGalleryResource {

  /**
   * Gets the JSON representation of an album. If it doesn't exist, a 404 HTTP code is returned. If
   * the user isn't authentified, a 401 HTTP code is returned. If a problem occurs when processing
   * the request, a 503 HTTP code is returned.
   * @param albumId the identifier of the photo
   * @return the response to the HTTP GET request with the JSON representation of the asked photo.
   */
  @GET
  @Path(GALLERY_ALBUMS_URI_PART + "/{albumId}")
  @Produces(MediaType.APPLICATION_JSON)
  public AlbumEntity getAlbum(@PathParam("albumId") final String albumId,
      @QueryParam("sort") final MediaSort sort) {
    try {
      final AlbumDetail album = getMediaService().getAlbum(new NodePK(albumId, getComponentId()));
      if (sort != null) {
        sort.perform(album.getMedia());
      }
      return asWebEntity(album);
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the JSON representation of a photo. If it doesn't exist, a 404 HTTP code is returned. If
   * the user isn't authentified, a 401 HTTP code is returned. If a problem occurs when processing
   * the request, a 503 HTTP code is returned.
   * @param photoId the identifier of the photo
   * @return the response to the HTTP GET request with the JSON representation of the asked photo.
   */
  @GET
  @Path(GALLERY_ALBUMS_URI_PART + "/{albumId}/" + GALLERY_PHOTOS_PART + "/{photoId}")
  @Produces(MediaType.APPLICATION_JSON)
  public AbstractMediaEntity getPhoto(@PathParam("albumId") final String albumId,
      @PathParam("photoId") final String photoId) {
    return getMediaEntity(Photo, albumId, photoId);
  }

  /**
   * Gets the JSON representation of a video. If it doesn't exist, a 404 HTTP code is returned. If
   * the user isn't authentified, a 401 HTTP code is returned. If a problem occurs when processing
   * the request, a 503 HTTP code is returned.
   * @param videoId the identifier of the video
   * @return the response to the HTTP GET request with the JSON representation of the asked video.
   */
  @GET
  @Path(GALLERY_ALBUMS_URI_PART + "/{albumId}/" + GALLERY_VIDEOS_PART + "/{videoId}")
  @Produces(MediaType.APPLICATION_JSON)
  public AbstractMediaEntity getVideo(@PathParam("albumId") final String albumId,
      @PathParam("videoId") final String videoId) {
    return getMediaEntity(Video, albumId, videoId);
  }

  /**
   * Gets the JSON representation of a sound. If it doesn't exist, a 404 HTTP code is returned. If
   * the user isn't authentified, a 401 HTTP code is returned. If a problem occurs when processing
   * the request, a 503 HTTP code is returned.
   * @param soundId the identifier of the sound
   * @return the response to the HTTP GET request with the JSON representation of the asked sound.
   */
  @GET
  @Path(GALLERY_ALBUMS_URI_PART + "/{albumId}/" + GALLERY_SOUNDS_PART + "/{soundId}")
  @Produces(MediaType.APPLICATION_JSON)
  public AbstractMediaEntity getSound(@PathParam("albumId") final String albumId,
      @PathParam("soundId") final String soundId) {
    return getMediaEntity(Sound, albumId, soundId);
  }

  /**
   * Gets the JSON representation of a streaming. If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned. If a problem occurs when
   * processing the request, a 503 HTTP code is returned.
   * @param streamingId the identifier of the streaming
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * streaming.
   */
  @GET
  @Path(GALLERY_ALBUMS_URI_PART + "/{albumId}/" + GALLERY_STREAMINGS_PART + "/{streamingId}")
  @Produces(MediaType.APPLICATION_JSON)
  public AbstractMediaEntity getStreaming(@PathParam("albumId") final String albumId,
      @PathParam("streamingId") final String streamingId) {
    return getMediaEntity(Streaming, albumId, streamingId);
  }

  /**
   * Gets the content of a photo. If it doesn't exist, a 404 HTTP code is returned. If the user
   * isn't authentified, a 401 HTTP code is returned. If a problem occurs when processing the
   * request, a 503 HTTP code is returned.
   * @param photoId the identifier of the photo
   * @param size not used for the moment
   * @return the response to the HTTP GET request content of the asked photo.
   */
  @GET
  @Path(GALLERY_PHOTOS_PART + "/{photoId}/{size:([0-9]*x[0-9]*/)?}" + GALLERY_MEDIA_CONTENT_PART)
  @Produces("image/*")
  public Response getPhotoContent(@PathParam("photoId") final String photoId,
      @PathParam("size") final String size,
      @QueryParam(GALLERY_PHOTO_RESOLUTION_PARAM) MediaResolution mediaResolution) {
    if (mediaResolution == null) {
      mediaResolution = MediaResolution.ORIGINAL;
    }
    return getMediaContent(Photo, photoId, mediaResolution, size);
  }

  /**
   * Gets the content of a video. If it doesn't exist, a 404 HTTP code is returned. If the user
   * isn't authentified, a 401 HTTP code is returned. If a problem occurs when processing the
   * request, a 503 HTTP code is returned.
   * @param videoId the identifier of the video
   * @return the response to the HTTP GET request content of the asked video.
   */
  @GET
  @Path(GALLERY_VIDEOS_PART + "/{videoId}/" + GALLERY_MEDIA_CONTENT_PART)
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response getVideoContent(@PathParam("videoId") final String videoId) {
    return getMediaContent(Video, videoId, MediaResolution.ORIGINAL, null);
  }

  /**
   * Get the video thumbnail. If it doesn't exist, a 404 HTTP code is returned. If the user isn't
   * authentified, a 401 HTTP code is returned. If a problem occurs when processing the request, a
   * 503 HTTP code is returned.
   * @param videoId the identifier of the video
   * @param size not used for the moment
   * @return the response to the HTTP GET request content of the asked video.
   */
  @GET
  @Path(GALLERY_VIDEOS_PART + "/{videoId}/" + GALLERY_MEDIA_THUMBNAIL_PART + "/{size:([0-9]*x[0-9]*/)?}{thumbnailId}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response getVideoThumbnail(@PathParam("videoId") final String videoId,
      @PathParam("size") final String size,
      @PathParam("thumbnailId") final String thumbnailId) {
    String sizeDirective = size;
    if (StringUtil.isDefined(sizeDirective)) {
      sizeDirective = sizeDirective.replaceAll("[^0-9x]*", "");
    }
    return getMediaThumbnail(videoId, thumbnailId, sizeDirective);
  }

  /**
   * Gets the content of a sound. If it doesn't exist, a 404 HTTP code is returned. If the user
   * isn't authentified, a 401 HTTP code is returned. If a problem occurs when processing the
   * request, a 503 HTTP code is returned.
   * @param soundId the identifier of the sound
   * @return the response to the HTTP GET request content of the asked sound.
   */
  @GET
  @Path(GALLERY_SOUNDS_PART + "/{soundId}/" + GALLERY_MEDIA_CONTENT_PART)
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response getSoundContent(@PathParam("soundId") final String soundId) {
    return getMediaContent(Sound, soundId, MediaResolution.ORIGINAL, null);
  }

  /**
   * Gets the provider data of a streaming from its url. If it doesn't exist, a 404 HTTP code is
   * returned. If the user isn't authentified, a 401 HTTP code is returned. If a problem occurs
   * when
   * processing the request, a 503 HTTP code is returned.
   * @param streamingId the identifier of the streaming
   * @return the response to the HTTP GET request content of the asked streaming.
   */
  @GET
  @Path(GALLERY_STREAMINGS_PART + "/{streamingId}/" + GALLERY_STREAMING_PROVIDER_DATA_PART)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getStreamingProviderDataFromStreamingId(
      @PathParam("streamingId") final String streamingId) {
    try {
      final Media media = getMediaService().getMedia(new MediaPK(streamingId, getComponentId()));
      checkNotFoundStatus(media);
      org.silverpeas.components.gallery.model.Streaming streaming = media.getStreaming();
      checkNotFoundStatus(streaming);
      verifyUserMediaAccess(streaming);
      return Response.seeOther(getUri().getBaseUriBuilder()
          .path("media/streaming/" + GALLERY_STREAMING_PROVIDER_DATA_PART)
          .queryParam("url", streaming.getHomepageUrl())
          .build()).build();
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the embed centent of a video. If it doesn't exist, a 404 HTTP code is returned. If the
   * user
   * isn't authentified, a 401 HTTP code is returned. If a problem occurs when processing the
   * request, a 503 HTTP code is returned.
   * @param videoId the identifier of the video
   * @return the response to the HTTP GET request embed centent of the asked video.
   */
  @GET
  @Path(GALLERY_VIDEOS_PART + "/{videoId}/" + GALLERY_MEDIA_EMBED_PART)
  public View getVideoEmbed(@PathParam("videoId") final String videoId) {
    return getMediaEmbed(Video, videoId, MediaResolution.ORIGINAL);
  }

  /**
   * Gets the embed content of a sound. If it doesn't exist, a 404 HTTP code is returned. If the
   * user
   * isn't authentified, a 401 HTTP code is returned. If a problem occurs when processing the
   * request, a 503 HTTP code is returned.
   * @param soundId the identifier of the sound
   * @return the response to the HTTP GET request embed content of the asked sound.
   */
  @GET
  @Path(GALLERY_SOUNDS_PART + "/{soundId}/" + GALLERY_MEDIA_EMBED_PART)
  public View getSoundEmbed(@PathParam("soundId") final String soundId) {
    return getMediaEmbed(Sound, soundId, MediaResolution.ORIGINAL);
  }
}
