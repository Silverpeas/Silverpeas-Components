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

import com.silverpeas.annotation.Authorized;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.silverpeas.gallery.constant.MediaResolution;
import com.silverpeas.gallery.constant.StreamingProvider;
import com.silverpeas.gallery.model.AlbumDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.sun.jersey.api.view.Viewable;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static com.silverpeas.gallery.constant.GalleryResourceURIs.*;
import static com.silverpeas.gallery.constant.MediaType.*;

/**
 * A REST Web resource giving gallery data.
 * @author Yohann Chastagnier
 */
@Service
@RequestScoped
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
   * @return the response to the HTTP GET request content of the asked photo.
   */
  @GET
  @Path(GALLERY_PHOTOS_PART + "/{photoId}/" + GALLERY_MEDIA_CONTENT_PART)
  @Produces("image/*")
  public Response getPhotoContent(@PathParam("photoId") final String photoId,
      @QueryParam(GALLERY_PHOTO_RESOLUTION_PARAM) MediaResolution mediaResolution) {
    if (mediaResolution == null) {
      mediaResolution = MediaResolution.ORIGINAL;
    }
    return getMediaContent(Photo, photoId, mediaResolution);
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
    return getMediaContent(Video, videoId, MediaResolution.ORIGINAL);
  }

  /**
   * Get the video thumbnail. If it doesn't exist, a 404 HTTP code is returned. If the user isn't
   * authentified, a 401 HTTP code is returned. If a problem occurs when processing the request, a
   * 503 HTTP code is returned.
   * @param videoId the identifier of the video
   * @return the response to the HTTP GET request content of the asked video.
   */
  @GET
  @Path(GALLERY_VIDEOS_PART + "/{videoId}/" + GALLERY_MEDIA_THUMBNAIL_PART + "/{thumbnailId}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response getVideoThumbnail(@PathParam("videoId") final String videoId,
      @PathParam("thumbnailId") final String thumbnailId) {
    return getMediaThumbnail(Video, videoId, thumbnailId);
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
    return getMediaContent(Sound, soundId, MediaResolution.ORIGINAL);
  }

  /**
   * Gets the provider data of a streamin from its url. If it doesn't exist, a 404 HTTP code is
   * returned. If the user isn't authentified, a 401 HTTP code is returned. If a problem occurs when
   * processing the request, a 503 HTTP code is returned.
   * @param url the url of the streaming
   * @return the response to the HTTP GET request content of the asked streaming.
   */
  @GET
  @Path(GALLERY_STREAMINGS_PART + "/" + GALLERY_STREAMING_PROVIDER_DATA_PART)
  @Produces(MediaType.APPLICATION_JSON)
  public StreamingProviderDataEntity getStreamingProviderDataFromUrl(
      @QueryParam("url") final String url) {
    try {
      checkNotFoundStatus(url);
      StreamingProvider streamingProvider = StreamingProvider.fromUrl(url);
      StreamingProviderDataEntity entity = null;
      switch (streamingProvider) {
        case youtube:
          entity = YoutubeDataEntity.fromOembed(
              getJSonFromUrl("http://www.youtube.com/oembed?url=" + url + "&format=json"));
          break;
        case vimeo:
          entity = VimeoDataEntity.fromOembed(
              getJSonFromUrl("http://vimeo.com/api/oembed.json?url=" + "http://vimeo.com/" +
                  streamingProvider.extractStreamingId(url)));
          break;
      }
      checkNotFoundStatus(entity);
      // noinspection ConstantConditions
      return entity.withURI(getUriInfo().getRequestUri());
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the embed centent of a video. If it doesn't exist, a 404 HTTP code is returned. If the user
   * isn't authentified, a 401 HTTP code is returned. If a problem occurs when processing the
   * request, a 503 HTTP code is returned.
   * @param videoId the identifier of the video
   * @return the response to the HTTP GET request embed centent of the asked video.
   */
  @GET
  @Path(GALLERY_VIDEOS_PART + "/{videoId}/" + GALLERY_MEDIA_EMBED_PART)
  public Viewable getVideoEmbed(@PathParam("videoId") final String videoId) {
    return getMediaEmbed(Video, videoId, MediaResolution.ORIGINAL);
  }

  /**
   * Gets the embed content of a sound. If it doesn't exist, a 404 HTTP code is returned. If the user
   * isn't authentified, a 401 HTTP code is returned. If a problem occurs when processing the
   * request, a 503 HTTP code is returned.
   * @param soundId the identifier of the sound
   * @return the response to the HTTP GET request embed content of the asked sound.
   */
  @GET
  @Path(GALLERY_SOUNDS_PART + "/{soundId}/" + GALLERY_MEDIA_EMBED_PART)
  public Viewable getSoundEmbed(@PathParam("soundId") final String soundId) {
    return getMediaEmbed(Sound, soundId, MediaResolution.ORIGINAL);
  }
}
