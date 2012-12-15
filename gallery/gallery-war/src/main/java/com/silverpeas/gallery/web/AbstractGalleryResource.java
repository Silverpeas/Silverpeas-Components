/*
 * Copyright (C) 2000 - 2012 Silverpeas
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

import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.control.ejb.GalleryBmHome;
import com.silverpeas.gallery.model.AlbumDetail;
import com.silverpeas.gallery.model.GalleryRuntimeException;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.model.PhotoPK;
import com.silverpeas.web.RESTWebService;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodePK;
import org.apache.commons.collections.CollectionUtils;

import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;

import static com.silverpeas.gallery.web.GalleryResourceURIs.*;

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
   * Converts the given list of data into their corresponding web entities.
   * @param entityClass the entity class returned.
   * @param data data to convert.
   * @return an array with the corresponding web entities.
   */
  @SuppressWarnings("unchecked")
  protected <T, E extends AbstractMediaEntity<?>> Collection<E> asWebEntities(
      Class<E> entityClass, Collection<T> data) {
    Collection<E> entities = new ArrayList<E>(data.size());
    for (Object object : data) {
      if (object instanceof PhotoDetail) {
        entities.add((E) asWebEntity(object));
      } else if (object instanceof ComponentInstLight) {
        entities.add((E) asWebEntity(object));
      } else {
        asWebEntity(object);
      }
    }
    return entities;
  }

  /**
   * Converts the album into its corresponding web entity.
   * @param album the album.
   * @return the corresponding photo entity.
   */
  protected AlbumEntity asWebEntity(AlbumDetail album) {
    checkNotFoundStatus(album);
    AlbumEntity albumEntity =
        AlbumEntity.createFrom(album, getUserPreferences().getLanguage())
            .withURI(getUriInfo().getRequestUri())
            .withParentURI(buildAlbumURI(album.getFatherPK()));
    for (PhotoDetail photo : album.getPhotos()) {
      if (isViewAllPhotoAuthorized() || photo.isVisible(new Date())) {
        albumEntity.addPhoto(asWebEntity(photo, album));
      }
    }
    return albumEntity;
  }

  /**
   * Converts the photo into its corresponding web entity.
   * @param photo the photo to convert.
   * @param album the album of the photo.
   * @return the corresponding photo entity.
   */
  protected PhotoEntity asWebEntity(PhotoDetail photo, AlbumDetail album) {
    checkNotFoundStatus(photo);
    checkNotFoundStatus(album);
    verifyPhotoInAlbum(photo, album);
    if (!CollectionUtils.intersection(
        EnumSet.of(SilverpeasRole.admin, SilverpeasRole.publisher, SilverpeasRole.privilegedUser),
        getUserRoles()).isEmpty()) {
      photo.setDownload(true);
    }
    return PhotoEntity.createFrom(photo, getUserPreferences().getLanguage())
        .withURI(buildPhotoURI(photo.getPhotoPK(), album.getNodePK()))
        .withParentURI(buildAlbumURI(album.getNodePK()));
  }

  /**
   * Centralized build of album URI.
   * @param album
   * @return album URI
   */
  protected URI buildAlbumURI(NodePK album) {
    if (album == null) {
      return null;
    }
    return getUriInfo().getBaseUriBuilder().path(GALLERY_BASE_URI).path(getComponentId())
        .path(GALLERY_ALBUMS_URI_PART).path(album.getId()).build();
  }

  /**
   * Centralized build of album URI.
   * @param photo
   * @param album
   * @return album URI
   */
  protected URI buildPhotoURI(PhotoPK photo, NodePK album) {
    if (photo == null || album == null) {
      return null;
    }
    return getUriInfo().getBaseUriBuilder().path(GALLERY_BASE_URI).path(getComponentId())
        .path(GALLERY_ALBUMS_URI_PART).path(album.getId()).path(GALLERY_PHOTOS_PART)
        .path(photo.getId()).build();
  }

  /**
   * Converts the component into its corresponding web entity.
   * @param object the object to convert.
   * @return the corresponding component entity.
   */
  protected AbstractMediaEntity<?> asWebEntity(Object object) {
    throw new WebApplicationException(Status.NOT_FOUND);
  }

  /**
   * Centralization
   * @param object any object
   */
  private void checkNotFoundStatus(Object object) {
    if (object == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
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
   * Verifying that the authenticated user is authorized to view all photos.
   * @return
   */
  protected void verifyViewAllPhotoAuthorized(PhotoDetail photo) {
    if (!isViewAllPhotoAuthorized() || !photo.isVisible(new Date())) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * Verifying that the given photo is included in the given album.
   * @return
   */
  protected void verifyPhotoInAlbum(PhotoDetail photo, AlbumDetail album) {
    if (!album.getPhotos().contains(photo)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * Gets Gallery EJB.
   * @return
   */
  protected GalleryBm getGalleryBm() {
    GalleryBm galleryBm = null;
    try {
      final GalleryBmHome galleryBmHome =
          EJBUtilitaire.getEJBObjectRef(JNDINames.GALLERYBM_EJBHOME, GalleryBmHome.class);
      galleryBm = galleryBmHome.create();
    } catch (Exception e) {
      throw new GalleryRuntimeException("AbstractGalleryResource.getGalleryBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return galleryBm;
  }
}
