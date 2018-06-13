/*
 * Copyright (C) 2000 - 2018 Silverpeas
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.resteasy.plugins.providers.html.View;
import org.silverpeas.components.gallery.constant.GalleryResourceURIs;
import org.silverpeas.components.gallery.constant.MediaResolution;
import org.silverpeas.components.gallery.constant.MediaType;
import org.silverpeas.components.gallery.model.AlbumDetail;
import org.silverpeas.components.gallery.model.InternalMedia;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.model.MediaPK;
import org.silverpeas.components.gallery.service.GalleryService;
import org.silverpeas.components.gallery.service.MediaServiceProvider;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.io.file.SilverpeasFile;
import org.silverpeas.core.io.file.SilverpeasFileProvider;
import org.silverpeas.core.io.media.Definition;
import org.silverpeas.core.io.media.video.ThumbnailPeriod;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.http.FileResponse;
import org.silverpeas.core.webapi.base.RESTWebService;

import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.EnumSet;

import static org.silverpeas.components.gallery.constant.GalleryResourceURIs.GALLERY_BASE_URI;

/**
 * @author Yohann Chastagnier
 */
abstract class AbstractGalleryResource extends RESTWebService {

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

  @Override
  protected String getResourceBasePath() {
    return GALLERY_BASE_URI;
  }

  /**
   * Converts the album into its corresponding web entity.
   * @param album the album.
   * @return the corresponding photo entity.
   */
  AlbumEntity asWebEntity(AlbumDetail album) {
    checkNotFoundStatus(album);
    AlbumEntity albumEntity = AlbumEntity.createFrom(album, getUserPreferences().getLanguage())
        .withURI(getUri().getRequestUri())
        .withParentURI(GalleryResourceURIs.buildAlbumURI(album.getFatherPK()));
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
   * @return the corresponding media entity.
   */
  AbstractMediaEntity getMediaEntity(final MediaType expectedMediaType, final String albumId,
      final String mediaId) {
    try {
      final AlbumDetail album = getMediaService().getAlbum(new NodePK(albumId, getComponentId()));
      final Media media = getMediaService().getMedia(new MediaPK(mediaId, getComponentId()));
      checkNotFoundStatus(media);
      verifyUserMediaAccess(media);
      verifyMediaIsInAlbum(media, album);
      // Verifying the physical file exists and that the type of media is the one expected
      if (media.getInternalMedia() != null) {
        checkMediaExistsWithRequestedMimeType(expectedMediaType, media, MediaResolution.PREVIEW);
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
   * Gets the media and verifies the user rights.
   * @param expectedMediaType expected media type
   * @param mediaId the media identifier
   * @param requestedMediaResolution requested media resolution
   * @param size a specific size applied on requested resolution
   * @return the requested media.
   */
  private Pair<Media, SilverpeasFile> getCheckedMedia(final MediaType expectedMediaType,
      final String mediaId, final MediaResolution requestedMediaResolution, final String size) {
    try {
      final Media media = getMediaService().getMedia(new MediaPK(mediaId, getComponentId()));
      checkNotFoundStatus(media);
      verifyUserMediaAccess(media);
      // Adjusting the resolution according to the user rights
      MediaResolution mediaResolution = getUserMediaResolution(requestedMediaResolution, media);
      // Verifying the physical file exists and that the type of media is the one expected
      final SilverpeasFile file = media.getFile(mediaResolution, size);
      if (!file.exists() || expectedMediaType != media.getType()) {
        throw new WebApplicationException(Status.NOT_FOUND);
      }
      return Pair.of(media, file);
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the user resolution according to its rights and the resolution requested.
   * @param requestedMediaResolution the requested media resolution.
   * @param media the media.
   * @return a {@link MediaResolution} instance.
   */
  private MediaResolution getUserMediaResolution(final MediaResolution requestedMediaResolution,
      final Media media) {
    MediaResolution mediaResolution = MediaResolution.ORIGINAL;
    if (media.getType().isPhoto()) {
      mediaResolution = requestedMediaResolution;
      if (MediaResolution.ORIGINAL == requestedMediaResolution && !isUserPrivileged() &&
          !media.isDownloadable()) {
        mediaResolution = MediaResolution.PREVIEW;
      }
    }
    return mediaResolution;
  }

  /**
   * Centralization of getting of media content.
   * @param expectedMediaType expected media type
   * @param mediaId the media identifier
   * @param requestedMediaResolution requested media resolution
   * @param size a specific size applied on requested resolution
   * @return the response.
   */
  Response getMediaContent(final MediaType expectedMediaType, final String mediaId,
      final MediaResolution requestedMediaResolution, final String size) {
    try {
      final Pair<Media, SilverpeasFile> checkedMedia =
          getCheckedMedia(expectedMediaType, mediaId, requestedMediaResolution, size);
      final Media media = checkedMedia.getLeft();
      final SilverpeasFile file = checkedMedia.getRight();
      return FileResponse.fromRest(getHttpServletRequest(), getHttpServletResponse())
          .forceMimeType(((InternalMedia) media).getFileMimeType().getMimeType())
          .forceFileId(mediaId)
          .silverpeasFile(file)
          .build();
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
  View getMediaEmbed(final MediaType expectedMediaType, final String mediaId,
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
      checkMediaExistsWithRequestedMimeType(expectedMediaType, media, mediaResolution);

      final Definition definition;
      MediaType mediaType = media.getType();
      if (mediaType == MediaType.Video) {
        definition = media.getVideo().getDefinition();
      } else {
        definition = Definition.of(mediaResolution.getWidth(), mediaResolution.getHeight());
      }

      getHttpServletRequest().setAttribute("mediaUrl", media.getApplicationOriginalUrl());
      getHttpServletRequest().setAttribute("posterUrl", media.getApplicationThumbnailUrl(mediaResolution));
      getHttpServletRequest().setAttribute("playerType", media.getType().getName());
      getHttpServletRequest().setAttribute("mimeType", media.getInternalMedia().getFileMimeType().getMimeType());
      getHttpServletRequest().setAttribute("definition", definition);
      getHttpServletRequest().setAttribute("backgroundColor", getHttpServletRequest().getParameter("backgroundColor"));
      getHttpServletRequest().setAttribute("autoPlay", getHttpServletRequest().getParameter("autoPlay"));

      return new View("/media/jsp/embed.jsp");
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Verifies the physical file exists and that the type of media is the one expected.
   * @param requestedMediaType the requested media type (from the request).
   * @param media the media to verify.
   * @param mediaResolution the media resolution to get (from the service).
   */
  private void checkMediaExistsWithRequestedMimeType(final MediaType requestedMediaType,
      final Media media, final MediaResolution mediaResolution) {
    final SilverpeasFile file = media.getFile(mediaResolution);
    if (!file.exists() || requestedMediaType != media.getType()) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
  }

  /**
   * Centralization of getting video media thumbnail.
   * @param mediaId the media identifier
   * @param thumbnailId the thumbnail identifier
   * @param sizeDirective the size directive with pattern (optional)
   * @return the response.
   */
  Response getMediaThumbnail(final String mediaId,
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

      return loadFileContent(thumbFile)
          .header("Content-Type", thumbFile.getMimeType())
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
  private boolean isUserPrivileged() {
    Collection<SilverpeasRole> userRoles = getUserRoles();
    return EnumSet.of(SilverpeasRole.admin, SilverpeasRole.publisher, SilverpeasRole.writer,
        SilverpeasRole.privilegedUser).stream().anyMatch(userRoles::contains);
  }

  /**
   * Centralization
   * @param object any object
   */
  void checkNotFoundStatus(Object object) {
    boolean isNotFound = false;
    if (object == null) {
      isNotFound = true;
    } else if (object instanceof Media) {
      Media media = (Media) object;
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
        default:
          isNotFound = true;
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
  private boolean hasUserMediaAccess(Media media) {
    return media.canBeAccessedBy(getUser());
  }

  /**
   * Verifying that the authenticated user is authorized to view the given media.
   * @param media a media for which the access has to be verified.
   * @throws javax.ws.rs.WebApplicationException if user is not authorized to view the media
   */
  void verifyUserMediaAccess(Media media) {
    if (!hasUserMediaAccess(media)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * @throws javax.ws.rs.WebApplicationException if the given media is not included in the given
   * album.
   */
  private void verifyMediaIsInAlbum(Media media, AlbumDetail album) {
    if (!album.getMedia().contains(media)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  private Response.ResponseBuilder loadFileContent(final File file) {
    StreamingOutput streamingOutput = output -> {
      try (final InputStream mediaStream = FileUtils.openInputStream(file)) {
        IOUtils.copy(mediaStream, output);
      } catch (IOException e) {
        throw new WebApplicationException(e, Status.NOT_FOUND);
      }
    };
    return Response.ok(streamingOutput);
  }

  /**
   * @return gallery media service layer
   */
  GalleryService getMediaService() {
    return MediaServiceProvider.getMediaService();
  }
}
