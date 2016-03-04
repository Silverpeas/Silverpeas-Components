/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery.web;

import org.silverpeas.components.gallery.constant.MediaResolution;
import org.silverpeas.components.gallery.constant.MediaType;
import org.silverpeas.components.gallery.service.GalleryService;
import org.silverpeas.components.gallery.service.MediaServiceProvider;
import org.silverpeas.components.gallery.model.AlbumDetail;
import org.silverpeas.components.gallery.model.InternalMedia;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.model.MediaPK;
import com.silverpeas.web.RESTWebService;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.node.model.NodePK;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.html.View;
import org.silverpeas.components.gallery.constant.GalleryResourceURIs;
import org.silverpeas.file.SilverpeasFile;
import org.silverpeas.file.SilverpeasFileProvider;
import org.silverpeas.media.Definition;
import org.silverpeas.media.video.ThumbnailPeriod;
import org.silverpeas.util.StringUtil;

import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.EnumSet;

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
        .withURI(getUriInfo().getRequestUri()).withParentURI(GalleryResourceURIs.buildAlbumURI(album.getFatherPK()));
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
            .withPreviewUrl(GalleryResourceURIs.buildMediaContentURI(media, MediaResolution.PREVIEW))
            .withThumbUrl(URI.create(media.getApplicationThumbnailUrl(MediaResolution.SMALL)));
        break;
      case Video:
        entity = VideoEntity.createFrom(media.getVideo())
            .withThumbUrl(URI.create(media.getApplicationThumbnailUrl(MediaResolution.MEDIUM)));
        break;
      case Sound:
        entity = SoundEntity.createFrom(media.getSound())
            .withThumbUrl(URI.create(media.getApplicationThumbnailUrl(MediaResolution.MEDIUM)));
        break;
      case Streaming:
        entity = StreamingEntity.createFrom(media.getStreaming())
            .withOriginalUrl(URI.create(media.getStreaming().getHomepageUrl()))
            .withThumbUrl(URI.create(media.getApplicationThumbnailUrl(MediaResolution.MEDIUM)));
        break;
      default:
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
    if (media.getInternalMedia() != null) {
      entity.withOriginalUrl(
          GalleryResourceURIs.buildMediaContentURI(media, MediaResolution.ORIGINAL));
    }
    return entity.withURI(GalleryResourceURIs.buildMediaInAlbumURI(album, media)).withParentURI(
        GalleryResourceURIs.buildAlbumURI(album));
  }

  /**
   * Centralization of getting of media data.
   * @param expectedMediaType expected media type
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
   * @param expectedMediaType expected media type
   * @param mediaId the media identifier
   * @param requestedMediaResolution requested media resolution
   * @return
   */
  protected Response getMediaContent(final MediaType expectedMediaType, final String mediaId,
      final MediaResolution requestedMediaResolution) {
    try {
      final Media media = getMediaService().getMedia(new MediaPK(mediaId, getComponentId()));
      checkNotFoundStatus(media);
      verifyUserMediaAccess(media);
      // Adjusting the resolution according to the user rights
      MediaResolution mediaResolution = MediaResolution.ORIGINAL;
      if (media.getType().isPhoto()) {
        mediaResolution = requestedMediaResolution;
        if (MediaResolution.ORIGINAL == requestedMediaResolution &&
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
          .header("Content-Disposition", "inline; filename=\"" + file.getName() + "\"").build();
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Centralization of getting of media embed.
   * @param expectedMediaType expected media type
   * @param mediaId the media identifier
   * @param requestedMediaResolution requested media resolution
   */
  protected View getMediaEmbed(final MediaType expectedMediaType, final String mediaId,
      final MediaResolution requestedMediaResolution) {
    try {
      final Media media = getMediaService().getMedia(new MediaPK(mediaId, getComponentId()));
      checkNotFoundStatus(media);
      verifyUserMediaAccess(media);
      // Adjusting the resolution according to the user rights
      MediaResolution mediaResolution = MediaResolution.PREVIEW;
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
      return new View("/gallery/jsp/embed.jsp");
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Centralization of getting video media thumbnail.
   * @param expectedMediaType the expected media type
   * @param mediaId the media identifier
   * @param thumbnailId the thumbnail identifier
   * @param sizeDirective the size directive with pattern (optional)
   * @return
   */
  protected Response getMediaThumbnail(final MediaType expectedMediaType, final String mediaId,
      final String thumbnailId, final String sizeDirective) {
    try {
      final Media media = getMediaService().getMedia(new MediaPK(mediaId, getComponentId()));
      checkNotFoundStatus(media);
      verifyUserMediaAccess(media);
      // Verifying the physical file exists
      String filename = ThumbnailPeriod.fromIndex(thumbnailId).getFilename();
      if (StringUtil.isDefined(sizeDirective)) {
        filename = sizeDirective + "/" + filename;
      }
      final SilverpeasFile thumbFile =
          SilverpeasFileProvider.getFile(FileUtils
              .getFile(Media.BASE_PATH.getPath(), media.getComponentInstanceId(),
                  media.getWorkspaceSubFolderName(), filename).getPath());
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
   * @return true if user has privileged
   */
  protected boolean isUserPrivileged() {
    Collection<SilverpeasRole> userRoles = getUserRoles();
    return EnumSet.of(SilverpeasRole.admin, SilverpeasRole.publisher, SilverpeasRole.writer,
        SilverpeasRole.privilegedUser).stream().filter(role -> userRoles.contains(role)).findFirst()
        .isPresent();
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
   * @param media the media to check access
   * @return true if user has media access
   */
  protected boolean hasUserMediaAccess(Media media) {
    return media.canBeAccessedBy(getUserDetail());
  }

  /**
   * Verifying that the authenticated user is authorized to view the given media.
   * @param media
   * @throws javax.ws.rs.WebApplicationException if user is not authorized to view the media
   */
  protected void verifyUserMediaAccess(Media media) {
    if (!hasUserMediaAccess(media)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * @return if the authenticated user is authorized to view all photos.
   */
  protected boolean isViewAllPhotoAuthorized() {
    return getUserRoles().contains(SilverpeasRole.admin);
  }

  /**
   * @throws javax.ws.rs.WebApplicationException if the given media is not included in the given
   * album.
   */
  protected void verifyMediaIsInAlbum(Media media, AlbumDetail album) {
    if (!album.getMedia().contains(media)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * @return gallery media service layer
   */
  protected GalleryService getMediaService() {
    return MediaServiceProvider.getMediaService();
  }
}
