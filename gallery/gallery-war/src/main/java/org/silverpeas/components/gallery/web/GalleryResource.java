/*
 * Copyright (C) 2000 - 2026 Silverpeas
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.jboss.resteasy.plugins.providers.html.View;
import org.silverpeas.components.gallery.constant.MediaResolution;
import org.silverpeas.components.gallery.model.AlbumDetail;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.model.MediaPK;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.web.rs.annotation.Authorized;
import org.silverpeas.core.web.rs.annotation.doc.NotFound;
import org.silverpeas.kernel.util.StringUtil;

import static org.silverpeas.components.gallery.constant.GalleryResourceURIs.*;
import static org.silverpeas.components.gallery.constant.MediaType.*;

/**
 * A REST Web resource giving gallery data.
 *
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
   *
   * @param albumId the identifier of the album
   * @return the response to the HTTP GET request with the JSON representation of the asked album.
   */
  @Operation(summary = "Gets an album of media.")
  @ApiResponse(responseCode = "200", description = "The asked album.",
      content = @Content(schema = @Schema(implementation = AlbumEntity.class)))
  @NotFound
  @GET
  @Path(GALLERY_ALBUMS_URI_PART + "/{albumId}")
  @Produces(MediaType.APPLICATION_JSON)
  public AlbumEntity getAlbum(@PathParam("albumId") final String albumId,
      @QueryParam("sort") final MediaSort sort) {
    try {
      final AlbumDetail album = getGalleryService().getAlbum(new NodePK(albumId, getComponentId()));
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
   *
   * @param photoId the identifier of the photo
   * @return the response to the HTTP GET request with the JSON representation of the asked photo.
   */
  @Operation(summary = "Gets a photo of the given album.")
  @ApiResponse(responseCode = "200", description = "The asked photo.",
      content = @Content(schema = @Schema(implementation = PhotoEntity.class)))
  @NotFound
  @GET
  @Path(GALLERY_ALBUMS_URI_PART + "/{albumId}/" + GALLERY_PHOTOS_PART + "/{photoId}")
  @Produces(MediaType.APPLICATION_JSON)
  public <T extends AbstractMediaEntity<T>> T getPhoto(@PathParam("albumId") final String albumId,
      @PathParam("photoId") final String photoId) {
    return getMediaEntity(Photo, albumId, photoId);
  }

  /**
   * Gets the JSON representation of a video. If it doesn't exist, a 404 HTTP code is returned. If
   * the user isn't authentified, a 401 HTTP code is returned. If a problem occurs when processing
   * the request, a 503 HTTP code is returned.
   *
   * @param videoId the identifier of the video
   * @return the response to the HTTP GET request with the JSON representation of the asked video.
   */
  @Operation(summary = "Gets a video of the given album.")
  @ApiResponse(responseCode = "200", description = "The asked video.",
      content = @Content(schema = @Schema(implementation = VideoEntity.class)))
  @NotFound
  @GET
  @Path(GALLERY_ALBUMS_URI_PART + "/{albumId}/" + GALLERY_VIDEOS_PART + "/{videoId}")
  @Produces(MediaType.APPLICATION_JSON)
  public <T extends AbstractMediaEntity<T>> T getVideo(@PathParam("albumId") final String albumId,
      @PathParam("videoId") final String videoId) {
    return getMediaEntity(Video, albumId, videoId);
  }

  /**
   * Gets the JSON representation of a sound. If it doesn't exist, a 404 HTTP code is returned. If
   * the user isn't authentified, a 401 HTTP code is returned. If a problem occurs when processing
   * the request, a 503 HTTP code is returned.
   *
   * @param soundId the identifier of the sound
   * @return the response to the HTTP GET request with the JSON representation of the asked sound.
   */
  @Operation(summary = "Gets a sound of the given album.")
  @ApiResponse(responseCode = "200", description = "The asked sound.",
      content = @Content(schema = @Schema(implementation = SoundEntity.class)))
  @NotFound
  @GET
  @Path(GALLERY_ALBUMS_URI_PART + "/{albumId}/" + GALLERY_SOUNDS_PART + "/{soundId}")
  @Produces(MediaType.APPLICATION_JSON)
  public <T extends AbstractMediaEntity<T>> T getSound(@PathParam("albumId") final String albumId,
      @PathParam("soundId") final String soundId) {
    return getMediaEntity(Sound, albumId, soundId);
  }

  /**
   * Gets the JSON representation of a streaming. If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned. If a problem occurs when
   * processing the request, a 503 HTTP code is returned.
   *
   * @param streamingId the identifier of the streaming
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * streaming.
   */
  @Operation(summary = "Gets a streaming of the given album.")
  @ApiResponse(responseCode = "200", description = "The asked streaming.",
      content = @Content(schema = @Schema(implementation = StreamingEntity.class)))
  @NotFound
  @GET
  @Path(GALLERY_ALBUMS_URI_PART + "/{albumId}/" + GALLERY_STREAMINGS_PART + "/{streamingId}")
  @Produces(MediaType.APPLICATION_JSON)
  public <T extends AbstractMediaEntity<T>> T getStreaming(@PathParam("albumId") final String albumId,
      @PathParam("streamingId") final String streamingId) {
    return getMediaEntity(Streaming, albumId, streamingId);
  }

  /**
   * Gets the content of a photo. If it doesn't exist, a 404 HTTP code is returned. If the user
   * isn't authentified, a 401 HTTP code is returned. If a problem occurs when processing the
   * request, a 503 HTTP code is returned.
   *
   * @param photoId the identifier of the photo
   * @param size not used for the moment
   * @return the response to the HTTP GET request content of the asked photo.
   */
  @Operation(summary = "Downloads the content of a photo.",
      description = "The resolution query parameter selects the rendition of the photo to " +
          "download; by default the original one is returned.")
  @ApiResponse(responseCode = "200", description = "The content of the asked photo.",
      content = @Content(mediaType = "image/*"))
  @NotFound
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
   *
   * @param videoId the identifier of the video
   * @return the response to the HTTP GET request content of the asked video.
   */
  @Operation(summary = "Downloads the content of a video.")
  @ApiResponse(responseCode = "200", description = "The content of the asked video.",
      content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM))
  @NotFound
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
   *
   * @param videoId the identifier of the video
   * @param size the expected size of the thumbnail, in the WIDTHxHEIGHT format
   * @return the response to the HTTP GET request content of the asked thumbnail.
   */
  @Operation(summary = "Downloads the thumbnail of a video.",
      description = "The size path segment, in the WIDTHxHEIGHT format, is optional; without " +
          "it the thumbnail is returned in its original size.")
  @ApiResponse(responseCode = "200", description = "The content of the asked thumbnail.",
      content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM))
  @NotFound
  @GET
  @Path(GALLERY_VIDEOS_PART + "/{videoId}/" +
        GALLERY_MEDIA_THUMBNAIL_PART + "/{size:([0-9]*x[0-9]*/)?}{thumbnailId}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response getVideoThumbnail(@PathParam("videoId") final String videoId,
      @PathParam("size") final String size, @PathParam("thumbnailId") final String thumbnailId) {
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
   *
   * @param soundId the identifier of the sound
   * @return the response to the HTTP GET request content of the asked sound.
   */
  @Operation(summary = "Downloads the content of a sound.")
  @ApiResponse(responseCode = "200", description = "The content of the asked sound.",
      content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM))
  @NotFound
  @GET
  @Path(GALLERY_SOUNDS_PART + "/{soundId}/" + GALLERY_MEDIA_CONTENT_PART)
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response getSoundContent(@PathParam("soundId") final String soundId) {
    return getMediaContent(Sound, soundId, MediaResolution.ORIGINAL, null);
  }

  /**
   * Gets the provider data of a streaming from its url. If it doesn't exist, a 404 HTTP code is
   * returned. If the user isn't authentified, a 401 HTTP code is returned. If a problem occurs when
   * processing the request, a 503 HTTP code is returned.
   *
   * @param streamingId the identifier of the streaming
   * @return the response to the HTTP GET request content of the asked streaming.
   */
  @Operation(summary = "Gets the data of the provider hosting the given streaming.",
      description = "The request is redirected to the resource serving the data of the provider " +
          "for the home page URL of the streaming.")
  @ApiResponse(responseCode = "303",
      description = "The URI of the resource serving the data of the provider.")
  @NotFound
  @GET
  @Path(GALLERY_STREAMINGS_PART + "/{streamingId}/" + GALLERY_STREAMING_PROVIDER_DATA_PART)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getStreamingProviderDataFromStreamingId(
      @PathParam("streamingId") final String streamingId) {
    try {
      final Media media = getGalleryService().getMedia(new MediaPK(streamingId, getComponentId()));
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
   * Gets the embed content of a video. If it doesn't exist, a 404 HTTP code is returned. If the
   * user isn't authentified, a 401 HTTP code is returned. If a problem occurs when processing the
   * request, a 503 HTTP code is returned.
   *
   * @param videoId the identifier of the video
   * @return the response to the HTTP GET request embed content of the asked video.
   */
  @Operation(summary = "Gets the player of a video, ready to be embedded into a web page.")
  @ApiResponse(responseCode = "200", description = "The web page playing the asked video.",
      content = @Content(mediaType = MediaType.TEXT_HTML))
  @NotFound
  @GET
  @Path(GALLERY_VIDEOS_PART + "/{videoId}/" + GALLERY_MEDIA_EMBED_PART)
  public View getVideoEmbed(@PathParam("videoId") final String videoId) {
    return getMediaEmbed(Video, videoId);
  }

  /**
   * Gets the embed content of a sound. If it doesn't exist, a 404 HTTP code is returned. If the
   * user isn't authentified, a 401 HTTP code is returned. If a problem occurs when processing the
   * request, a 503 HTTP code is returned.
   *
   * @param soundId the identifier of the sound
   * @return the response to the HTTP GET request embed content of the asked sound.
   */
  @Operation(summary = "Gets the player of a sound, ready to be embedded into a web page.")
  @ApiResponse(responseCode = "200", description = "The web page playing the asked sound.",
      content = @Content(mediaType = MediaType.TEXT_HTML))
  @NotFound
  @GET
  @Path(GALLERY_SOUNDS_PART + "/{soundId}/" + GALLERY_MEDIA_EMBED_PART)
  public View getSoundEmbed(@PathParam("soundId") final String soundId) {
    return getMediaEmbed(Sound, soundId);
  }
}
