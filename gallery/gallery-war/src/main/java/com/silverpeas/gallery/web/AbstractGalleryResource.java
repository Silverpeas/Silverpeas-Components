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
import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.control.ejb.MediaServiceFactory;
import com.silverpeas.gallery.model.AlbumDetail;
import com.silverpeas.gallery.model.InternalMedia;
import com.silverpeas.gallery.model.Media;
import com.silverpeas.gallery.model.MediaPK;
import com.silverpeas.web.RESTWebService;
import com.stratelia.webactiv.SilverpeasRole;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.silverpeas.file.SilverpeasFile;

import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
      if (media.getType().isPhoto() && hasUserMediaAccess(media.getPhoto())) {
        albumEntity.addPhoto(asWebEntity(media.getPhoto(), album));
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
  protected PhotoEntity asWebEntity(Media media, AlbumDetail album) {
    checkNotFoundStatus(media);
    checkNotFoundStatus(media.getPhoto());
    checkNotFoundStatus(album);
    verifyMediaIsInAlbum(media.getPhoto(), album);
    return PhotoEntity.createFrom(media.getPhoto(), getUserPreferences().getLanguage())
        .withURI(buildMediaInAlbumURI(album, media)).withParentURI(buildAlbumURI(album))
        .withOriginalUrl(buildMediaContentURI(media, ORIGINAL))
        .withSmallUrl(buildMediaContentURI(media, SMALL))
        .withPreviewUrl(buildMediaContentURI(media, PREVIEW));
  }

  /**
   * Centralization of getting of media content.
   * @param mediaId
   * @param requestedMediaResolution
   * @return
   */
  protected Response getMediaContent(final String mediaId,
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
      // Verifying the physical file exists
      final SilverpeasFile file = media.getFile(mediaResolution);
      if (!file.exists()) {
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
          .header("Content-Length", file.length()).build();
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
}
