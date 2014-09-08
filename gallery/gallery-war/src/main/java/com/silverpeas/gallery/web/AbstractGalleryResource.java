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

import com.silverpeas.gallery.constant.MediaResolution;
import com.silverpeas.gallery.constant.MediaType;
import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.control.ejb.MediaServiceFactory;
import com.silverpeas.gallery.model.AlbumDetail;
import com.silverpeas.gallery.model.InternalMedia;
import com.silverpeas.gallery.model.Media;
import com.silverpeas.gallery.model.MediaPK;
import com.silverpeas.web.RESTWebService;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.sun.jersey.api.view.Viewable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.silverpeas.file.SilverpeasFile;
import org.silverpeas.media.Definition;
import org.silverpeas.file.SilverpeasFileProvider;
import org.silverpeas.media.video.ThumbnailPeriod;

import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.EnumSet;

import static com.silverpeas.gallery.constant.GalleryResourceURIs.*;
import static com.silverpeas.gallery.constant.MediaResolution.*;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractGalleryResource extends RESTWebService {

  @PathParam("componentInstanceId")
  private String componentInstanceId;

  /*
   * (non-Javadoc)
   * @see com.silverpeas.web.RESTWebService#getComponentId()
   */
  @Override
  public String getComponentId() {
    return componentInstanceId;
  }

  /**
   * Converts the album into its corresponding web entity.
   * @param album the album.
   * @return the corresponding photo entity.
   */
  protected AlbumEntity asWebEntity(AlbumDetail album) {
    checkNotFoundStatus(album);
    AlbumEntity albumEntity = AlbumEntity.createFrom(album, getUserPreferences().getLanguage())
        .withURI(getUriInfo().getRequestUri()).withParentURI(buildAlbumURI(album.getFatherPK()));
    for (Media media : album.getMedia()) {
      if (hasUserMediaAccess(media)) {
        albumEntity.addMedia(asWebEntity(media, album));
      }
    }
    return albumEntity;
  }

  /**
   * Converts the photo into its corresponding web entity.
   * @param media the photo to convert.
   * @param album the album of the photo.
   * @return the corresponding photo entity.
   */
  private AbstractMediaEntity asWebEntity(Media media, AlbumDetail album) {
    final AbstractMediaEntity entity;
    switch (media.getType()) {
      case Photo:
        entity = PhotoEntity.createFrom(media.getPhoto())
            .withPreviewUrl(buildMediaContentURI(media, PREVIEW))
            .withThumbUrl(URI.create(media.getApplicationThumbnailUrl(SMALL)));
        break;
      case Video:
        entity = VideoEntity.createFrom(media.getVideo())
            .withThumbUrl(URI.create(media.getApplicationThumbnailUrl(MEDIUM)));
        break;
      case Sound:
        entity = SoundEntity.createFrom(media.getSound())
            .withThumbUrl(URI.create(media.getApplicationThumbnailUrl(MEDIUM)));
        break;
      case Streaming:
        entity = StreamingEntity.createFrom(media.getStreaming())
            .withOriginalUrl(URI.create(media.getStreaming().getHomepageUrl()))
            .withThumbUrl(URI.create(media.getApplicationThumbnailUrl(MEDIUM)));
        break;
      default:
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
    if (media.getInternalMedia() != null) {
      entity.withOriginalUrl(buildMediaContentURI(media, ORIGINAL));
    }
    return entity.withURI(buildMediaInAlbumURI(album, media)).withParentURI(buildAlbumURI(album));
  }

  /**
   * Centralization of getting of media data.
   * @param expectedMediaType
   * @param albumId the identifier of the album in which the media must exist
   * @param mediaId the identifier of the expected media
   * @return
   */
  protected AbstractMediaEntity getMediaEntity(final MediaType expectedMediaType,
      final String albumId, final String mediaId) {
    try {
      final AlbumDetail album = getMediaService().getAlbum(new NodePK(albumId, getComponentId()));
      final Media media = getMediaService().getMedia(new MediaPK(mediaId, getComponentId()));
      checkNotFoundStatus(media);
      verifyUserMediaAccess(media);
      verifyMediaIsInAlbum(media, album);
      // Verifying the physical file exists and that the type of media is the one expected
      if (media.getInternalMedia() != null) {
        final SilverpeasFile file = media.getFile(MediaResolution.PREVIEW);
        if (!file.exists() || expectedMediaType != media.getType()) {
          throw new WebApplicationException(Status.NOT_FOUND);
        }
      }
      // Getting the web entity
      return asWebEntity(media, album);
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Centralization of getting of media content.
   * @param expectedMediaType
   * @param mediaId
   * @param requestedMediaResolution
   * @return
   */
  protected Response getMediaContent(final MediaType expectedMediaType, final String mediaId,
      final MediaResolution requestedMediaResolution) {
    try {
      final Media media = getMediaService().getMedia(new MediaPK(mediaId, getComponentId()));
      checkNotFoundStatus(media);
      verifyUserMediaAccess(media);
      // Adjusting the resolution according to the user rights
      MediaResolution mediaResolution = ORIGINAL;
      if (media.getType().isPhoto()) {
        mediaResolution = requestedMediaResolution;
        if (ORIGINAL == requestedMediaResolution &&
            !isUserPrivileged() && !media.isDownloadable()) {
          mediaResolution = MediaResolution.PREVIEW;
        }
      }
      // Verifying the physical file exists and that the type of media is the one expected
      final SilverpeasFile file = media.getFile(mediaResolution);
      if (!file.exists() || expectedMediaType != media.getType()) {
        throw new WebApplicationException(Status.NOT_FOUND);
      }
      return Response.ok(new StreamingOutput() {
        @Override
        public void write(final OutputStream output) throws IOException, WebApplicationException {
          final InputStream mediaStream;
          try {
            mediaStream = FileUtils.openInputStream(file);
          } catch (IOException e) {
            throw new WebApplicationException(Status.NOT_FOUND);
          }
          try {
            IOUtils.copy(mediaStream, output);
          } finally {
            IOUtils.closeQuietly(mediaStream);
          }
        }
      }).header("Content-Type", ((InternalMedia) media).getFileMimeType().getMimeType())
          .header("Content-Length", file.length())
          .header("Content-Disposition", "inline; filename=\"" + file.getName() + "\"")
          .build();
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Centralization of getting of media embed.
   * @param expectedMediaType
   * @param mediaId
   * @param requestedMediaResolution
   * @return
   */
  protected Viewable getMediaEmbed(final MediaType expectedMediaType, final String mediaId,
      final MediaResolution requestedMediaResolution) {
    try {
      final Media media = getMediaService().getMedia(new MediaPK(mediaId, getComponentId()));
      checkNotFoundStatus(media);
      verifyUserMediaAccess(media);
      // Adjusting the resolution according to the user rights
      MediaResolution mediaResolution = PREVIEW;
      if (requestedMediaResolution.getWidth() != null) {
        mediaResolution = requestedMediaResolution;
      }
      // Verifying the physical file exists and that the type of media is the one expected
      final SilverpeasFile file = media.getFile(mediaResolution);
      if (!file.exists() || expectedMediaType != media.getType()) {
        throw new WebApplicationException(Status.NOT_FOUND);
      }

      final Definition definition;
      switch (media.getType()) {
        case Video:
          definition = media.getVideo().getDefinition();
          break;
        default:
          definition = Definition.of(mediaResolution.getWidth(), mediaResolution.getHeight());
          break;
      }

      // Set request attribute
      getHttpServletRequest().setAttribute("media", media);
      getHttpServletRequest().setAttribute("definition", definition);
      getHttpServletRequest().setAttribute("posterResolution", mediaResolution);

      // Handled parameters
      getHttpServletRequest()
          .setAttribute("backgroundColor", getHttpServletRequest().getParameter("backgroundColor"));
      getHttpServletRequest()
          .setAttribute("autoPlay", getHttpServletRequest().getParameter("autoPlay"));

      return new Viewable("/gallery/jsp/embed.jsp", null);
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }
  
  /**
   * Centralization of getting video media thumbnail.
   * @param expectedMediaType
   * @param mediaId
   * @param thumbnailId
   * @return
   */
  protected Response getMediaThumbnail(final MediaType expectedMediaType, final String mediaId,
      final String thumbnailId) {
    try {
      final Media media = getMediaService().getMedia(new MediaPK(mediaId, getComponentId()));
      checkNotFoundStatus(media);
      verifyUserMediaAccess(media);
      // Verifying the physical file exists
      final SilverpeasFile thumbFile =
          SilverpeasFileProvider.getFile(FileUtils
              .getFile(Media.BASE_PATH.getPath(), media.getComponentInstanceId(),
                  media.getWorkspaceSubFolderName(),
                  ThumbnailPeriod.fromIndex(thumbnailId).getFilename()).getPath());
      if (!thumbFile.exists()) {
        throw new WebApplicationException(Status.NOT_FOUND);
      }

      return Response.ok(new StreamingOutput() {
        @Override
        public void write(final OutputStream output) throws IOException, WebApplicationException {
          final InputStream mediaStream;
          try {
            mediaStream = FileUtils.openInputStream(thumbFile);
          } catch (IOException e) {
            throw new WebApplicationException(Status.NOT_FOUND);
          }
          try {
            IOUtils.copy(mediaStream, output);
          } finally {
            IOUtils.closeQuietly(mediaStream);
          }
        }
      }).header("Content-Type", thumbFile.getMimeType())
          .header("Content-Length", thumbFile.length())
          .header("Content-Disposition", "inline; filename=\"" + thumbFile.getName() + "\"")
          .build();
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }
  
  /**
   * Indicates if the current user is a privileged one.
   * @return
   */
  protected boolean isUserPrivileged() {
    return !CollectionUtils.intersection(EnumSet
        .of(SilverpeasRole.admin, SilverpeasRole.publisher, SilverpeasRole.writer,
            SilverpeasRole.privilegedUser), getUserRoles()).isEmpty();
  }

  /**
   * Centralization
   * @param object any object
   */
  protected void checkNotFoundStatus(Object object) {
    boolean isNotFound = false;
    if (object == null) {
      isNotFound = true;
    } else if (object instanceof Media) {
      isNotFound = true;
      Media media = ((Media) object);
      switch (media.getType()) {
        case Photo:
          isNotFound = media.getPhoto() == null;
          break;
        case Video:
          isNotFound = media.getVideo() == null;
          break;
        case Sound:
          isNotFound = media.getSound() == null;
          break;
        case Streaming:
          isNotFound = media.getStreaming() == null;
          break;
      }
    }
    if (isNotFound) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
  }

  /**
   * Centralization
   * @param media
   * @return
   */
  protected boolean hasUserMediaAccess(Media media) {
    return media.canBeAccessedBy(getUserDetail());
  }

  /**
   * Verifying that the authenticated user is authorized to view the given photo.
   * @param media
   * @return
   */
  protected void verifyUserMediaAccess(Media media) {
    if (!hasUserMediaAccess(media)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * Checking if the authenticated user is authorized to view all photos.
   * @return
   */
  protected boolean isViewAllPhotoAuthorized() {
    return getUserRoles().contains(SilverpeasRole.admin);
  }

  /**
   * Verifying that the given photo is included in the given album.
   * @return
   */
  protected void verifyMediaIsInAlbum(Media media, AlbumDetail album) {
    if (!album.getMedia().contains(media)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * Gets Gallery EJB.
   * @return
   */
  protected GalleryBm getMediaService() {
    return MediaServiceFactory.getMediaService();
  }

  /**
   * Perform an Http Get
   * @param url
   * @return a {@link JSONObject}
   */
  protected JSONObject getJSonFromUrl(String url) {
    GetMethod httpGet = new GetMethod(url);
    httpGet.setRequestHeader("User-Agent", getHttpRequest().getHeader("User-Agent"));
    httpGet.addRequestHeader("Accept", "application/json");
    try {
      HttpClient client = new HttpClient();
      int statusCode = client.executeMethod(httpGet);
      if (statusCode != HttpStatus.SC_OK) {
        throw new WebApplicationException(statusCode);
      }
      return new JSONObject(new JSONTokener(httpGet.getResponseBodyAsString()));
    } catch (WebApplicationException wae) {
      throw wae;
    } catch (Exception e) {
      throw new WebApplicationException(e);
    } finally {
      httpGet.releaseConnection();
    }
  }
}
