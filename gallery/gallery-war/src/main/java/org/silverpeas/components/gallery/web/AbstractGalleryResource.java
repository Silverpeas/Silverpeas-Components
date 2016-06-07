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
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.webapi.base.RESTWebService;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.StandardOpenOption.READ;
import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

/**
 * @author Yohann Chastagnier
 */
abstract class AbstractGalleryResource extends RESTWebService {

  private static final int BUFFER_LENGTH = 1024 * 16;
  private static final long EXPIRE_TIME = 1000 * 60 * 60 * 24;
  private static final Pattern RANGE_PATTERN = Pattern.compile("bytes=(?<start>\\d*)-(?<end>\\d*)");

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
  AlbumEntity asWebEntity(AlbumDetail album) {
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
   * Gets the media and verifies the user rights.
   * @param expectedMediaType expected media type
   * @param mediaId the media identifier
   * @param requestedMediaResolution requested media resolution
   * @return the requested media.
   */
  private Pair<Media, SilverpeasFile> getCheckedMedia(final MediaType expectedMediaType,
      final String mediaId, final MediaResolution requestedMediaResolution) {
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
      return Pair.of(media, file);
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
   * @return the response.
   */
  Response getMediaContent(final MediaType expectedMediaType, final String mediaId,
      final MediaResolution requestedMediaResolution) {
    try {
      final Pair<Media, SilverpeasFile> checkedMedia =
          getCheckedMedia(expectedMediaType, mediaId, requestedMediaResolution);
      final Media media = checkedMedia.getLeft();
      final SilverpeasFile file = checkedMedia.getRight();

      java.nio.file.Path mediaPath = Paths.get(file.toURI());
      String mediaMimeType = getHttpRequest().getParameter("forceMimeType");
      if (StringUtil.isNotDefined(mediaMimeType)) {
        mediaMimeType = ((InternalMedia) media).getFileMimeType().getMimeType();
      }

      int length = (int) Files.size(mediaPath);
      int start = 0;
      int end = length - 1;

      String range = defaultStringIfNotDefined(getHttpServletRequest().getHeader("Range"), "");
      Matcher matcher = RANGE_PATTERN.matcher(range);
      boolean isPartialRequest = matcher.matches();

      if (isPartialRequest) {
        String startGroup = matcher.group("start");
        start = startGroup.isEmpty() ? start : Integer.valueOf(startGroup);
        start = start < 0 ? 0 : start;

        String endGroup = matcher.group("end");
        end = endGroup.isEmpty() ? end : Integer.valueOf(endGroup);
        end = end > length - 1 ? length - 1 : end;
      }

      final int finalStart = start;
      final int finalContentLength = end - finalStart + 1;

      final Response.ResponseBuilder responseBuilder;
      if (isPartialRequest) {

        // Handling here a partial response (pseudo streaming)

        final HttpServletResponse response = getHttpServletResponse();
        response.setBufferSize(BUFFER_LENGTH);

        responseBuilder =
            Response.status(Status.PARTIAL_CONTENT).entity((StreamingOutput) output -> {

              int bytesRead;
              int bytesLeft = finalContentLength;
              ByteBuffer buffer = ByteBuffer.allocate(BUFFER_LENGTH);

              try (SeekableByteChannel input = Files.newByteChannel(mediaPath, READ)) {
                input.position(finalStart);
                while ((bytesRead = input.read(buffer)) != -1 && bytesLeft > 0) {
                  buffer.clear();
                  output.write(buffer.array(), 0, bytesLeft < bytesRead ? bytesLeft : bytesRead);
                  bytesLeft -= bytesRead;
                }
              } catch (IOException ioe) {
                SilverLogger.getLogger(AbstractGalleryResource.class).warn(
                    "client stopping the streaming HTTP Request of media content represented by " +
                        "''{0}'' identifier (original message ''{1}'')", media.getId(),
                    ioe.getMessage());
              }
            }).header("Accept-Ranges", "bytes")
              .header("ETag", media.getId())
              .lastModified(Date.from(Files.getLastModifiedTime(mediaPath).toInstant()))
              .expires(Date.from(Instant.ofEpochMilli(System.currentTimeMillis() + EXPIRE_TIME)))
              .header("Content-Range", String.format("bytes %s-%s/%s", start, end, length));

      } else {

        // Handling here a full response

        responseBuilder = Response.ok((StreamingOutput) output -> {
          try (final InputStream mediaStream = FileUtils.openInputStream(file)) {
            IOUtils.copy(mediaStream, output);
          } catch (IOException e) {
            throw new WebApplicationException(Status.NOT_FOUND);
          }
        });
      }

      return responseBuilder
          .type(mediaMimeType)
          .header("Content-Length", finalContentLength)
          .header("Content-Disposition", String.format("inline;filename=\"%s\"", file.getName()))
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
   * @return the response.
   */
  Response getMediaThumbnail(final MediaType expectedMediaType, final String mediaId,
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
  private boolean isUserPrivileged() {
    Collection<SilverpeasRole> userRoles = getUserRoles();
    return EnumSet.of(SilverpeasRole.admin, SilverpeasRole.publisher, SilverpeasRole.writer,
        SilverpeasRole.privilegedUser).stream().filter(role -> userRoles.contains(role)).findFirst()
        .isPresent();
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
  private boolean hasUserMediaAccess(Media media) {
    return media.canBeAccessedBy(getUserDetail());
  }

  /**
   * Verifying that the authenticated user is authorized to view the given media.
   * @param media
   * @throws javax.ws.rs.WebApplicationException if user is not authorized to view the media
   */
  void verifyUserMediaAccess(Media media) {
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
  private void verifyMediaIsInAlbum(Media media, AlbumDetail album) {
    if (!album.getMedia().contains(media)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * @return gallery media service layer
   */
  GalleryService getMediaService() {
    return MediaServiceProvider.getMediaService();
  }
}
