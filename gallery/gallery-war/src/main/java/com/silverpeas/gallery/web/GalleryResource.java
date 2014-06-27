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

import static com.silverpeas.gallery.web.GalleryResourceURIs.GALLERY_ALBUMS_URI_PART;
import static com.silverpeas.gallery.web.GalleryResourceURIs.GALLERY_BASE_URI;
import static com.silverpeas.gallery.web.GalleryResourceURIs.GALLERY_PHOTOS_PART;
import static com.silverpeas.gallery.web.GalleryResourceURIs.GALLERY_PHOTO_CONTENT_PART;
import static com.silverpeas.gallery.web.GalleryResourceURIs.GALLERY_PHOTO_PREVIEW_PART;
import static com.silverpeas.gallery.web.GalleryResourceURIs.GALLERY_VIDEOS_PART;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.silverpeas.servlets.OnlineFile;

import com.silverpeas.annotation.Authorized;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.silverpeas.gallery.model.AlbumDetail;
import com.silverpeas.gallery.model.InternalMedia;
import com.silverpeas.gallery.model.Media;
import com.silverpeas.gallery.model.MediaPK;
import com.silverpeas.gallery.model.Video;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.node.model.NodePK;

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
  public AlbumEntity getAlbum(@PathParam("albumId") final String albumId) {
    try {
      final AlbumDetail album = getGalleryBm().getAlbum(new NodePK(albumId, getComponentId()));
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
  public PhotoEntity getPhoto(@PathParam("albumId") final String albumId,
      @PathParam("photoId") final String photoId) {
    try {
      final AlbumDetail album = getGalleryBm().getAlbum(new NodePK(albumId, getComponentId()));
      final Media media = getGalleryBm().getMedia(new MediaPK(photoId, getComponentId()));
      verifyUserMediaAccess(media);
      return asWebEntity(media, album);
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the preview content of a photo. If it doesn't exist, a 404 HTTP code is returned. If the
   * user isn't authentified, a 401 HTTP code is returned. If a problem occurs when processing the
   * request, a 503 HTTP code is returned.
   * @param photoId the identifier of the photo
   * @return the response to the HTTP GET request preview content of the asked photo.
   */
  @GET
  @Path(GALLERY_ALBUMS_URI_PART + "/{albumId}/" + GALLERY_PHOTOS_PART + "/{photoId}/" +
      GALLERY_PHOTO_PREVIEW_PART)
  @Produces("image/*")
  public Response getPhotoPreviewContent(@PathParam("albumId") final String albumId,
      @PathParam("photoId") final String photoId) {
    return getPhotoContent(albumId, photoId, false);
  }

  /**
   * Gets the content of a photo. If it doesn't exist, a 404 HTTP code is returned. If the user
   * isn't authentified, a 401 HTTP code is returned. If a problem occurs when processing the
   * request, a 503 HTTP code is returned.
   * @param photoId the identifier of the photo
   * @return the response to the HTTP GET request content of the asked photo.
   */
  @GET
  @Path(GALLERY_ALBUMS_URI_PART + "/{albumId}/" + GALLERY_PHOTOS_PART + "/{photoId}/" +
      GALLERY_PHOTO_CONTENT_PART)
  @Produces("image/*")
  public Response getPhotoOriginalContent(@PathParam("albumId") final String albumId,
      @PathParam("photoId") final String photoId) {
    return getPhotoContent(albumId, photoId, true);
  }

  /**
   * Centralization of getting of photo content.
   * @param albumId
   * @param photoId
   * @param isOriginalRequired
   * @return
   */
  private Response getPhotoContent(final String albumId, final String photoId,
      final boolean isOriginalRequired) {
    try {
      final AlbumDetail album = getGalleryBm().getAlbum(new NodePK(albumId, getComponentId()));
      final Media media = getGalleryBm().getMedia(new MediaPK(photoId, getComponentId()));
      checkNotFoundStatus(media);
      checkNotFoundStatus(media.getPhoto());
      verifyUserMediaAccess(media);
      return Response.ok(new StreamingOutput() {
        @Override
        public void write(final OutputStream output) throws IOException, WebApplicationException {
          final InputStream photoStream = asInputStream(media.getPhoto(), album,
              (isOriginalRequired && (isUserPrivileged() || media.isDownloadable())));
          try {
            IOUtils.copy(photoStream, output);
          } finally {
            IOUtils.closeQuietly(photoStream);
          }
        }
      }).header("Content-Type", ((InternalMedia) media).getFileMimeType()).build();
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the content of a video. If it doesn't exist, a 404 HTTP code is returned. If the user
   * isn't authentified, a 401 HTTP code is returned. If a problem occurs when processing the
   * request, a 503 HTTP code is returned.
   * @param videoId the identifier of the video
   * @return the response to the HTTP GET request content of the asked video.
   */
  @GET
  @Path(GALLERY_ALBUMS_URI_PART + "/{albumId}/" + GALLERY_VIDEOS_PART + "/{videoId}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response getVideo(@PathParam("albumId") final String albumId,
      @PathParam("videoId") final String videoId) {
    final Media media = getGalleryBm().getMedia(new MediaPK(videoId, getComponentId()));
    checkNotFoundStatus(media);
    checkNotFoundStatus(media.getVideo());
    verifyUserMediaAccess(media);
    if (!com.silverpeas.gallery.constant.MediaType.Video.equals(media.getType())) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    return Response.ok(new StreamingOutput() {
      @Override
      public void write(final OutputStream output) throws IOException, WebApplicationException {
        final File videoFile = openVideoFile(media.getVideo());
        FileUtils.copyFile(videoFile, output);
        output.flush();
      }
    }).header("Content-Type", media.getVideo().getFileMimeType())
        .header("Content-Length", String.valueOf(media.getVideo().getFileSize())).build();

  }

  /**
   * Open a file of a video according to given details of a video.
   * @param video
   * @return
   * @throws IOException
   */
  private File openVideoFile(final Video video) {
    final String videoId = video.getMediaPK().getId();
    final String instanceId = video.getMediaPK().getInstanceId();
    if (StringUtil.isDefined(videoId) && StringUtil.isDefined(instanceId)) {
      String fileName = video.getFileName();
      OnlineFile onlineFile =
          new OnlineFile(video.getFileMimeType(), fileName, video.getType().getTechnicalFolder() +
              videoId, instanceId);
      try {
        return onlineFile.getContentFile();
      } catch (IOException e) {
        SilverTrace.error("gallery", "GalleryRessource", "gallery.ERR_CANT_GET_VIDEO_BYTES",
            "video = " + video.getTitle() + " (#" + video.getId() + ")");
        throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
      }
    }
    return null;
  }

}
