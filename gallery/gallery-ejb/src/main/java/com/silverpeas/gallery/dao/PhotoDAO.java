/**
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
package com.silverpeas.gallery.dao;

import static com.silverpeas.gallery.model.MediaCriteria.QUERY_ORDER_BY.CREATE_DATE_DESC;
import static com.silverpeas.gallery.model.MediaCriteria.QUERY_ORDER_BY.IDENTIFIER_DESC;
import static com.stratelia.webactiv.util.DBUtil.unique;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.silverpeas.persistence.repository.OperationContext;

import com.silverpeas.gallery.GalleryComponentSettings;
import com.silverpeas.gallery.constant.MediaType;
import com.silverpeas.gallery.model.Media;
import com.silverpeas.gallery.model.MediaCriteria;
import com.silverpeas.gallery.model.Photo;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.exception.UtilException;

public class PhotoDAO {

  public static Photo getPhoto(Connection con, String photoId) throws SQLException {
    Photo photo = getByCriteria(con, MediaCriteria.fromMediaId(photoId));
    if (photo == null) {
      // It is very ugly, but it was like it before the photo to media migration...
      photo = new Photo();
    }
    return photo;
  }

  public static Collection<Photo> getAllPhoto(Connection con, String albumId,
      String instanceId, MediaCriteria.VISIBILITY visibility) throws SQLException {
    return findByCriteria(con,
        MediaCriteria.fromComponentInstanceId(instanceId).albumIdentifierIsOneOf(albumId)
            .withVisibility(visibility));
  }

  public static Collection<Photo> getPhotoNotVisible(Connection con, String instanceId)
      throws SQLException {
    return findByCriteria(con, MediaCriteria.fromComponentInstanceId(instanceId)
        .withVisibility(MediaCriteria.VISIBILITY.HIDDEN_ONLY));
  }

  public static Collection<Photo> getAllPhotoEndVisible(Connection con, int nbDays)
      throws SQLException {
    return findByCriteria(con, MediaCriteria.fromNbDaysBeforeThatMediaIsNotVisible(nbDays)
        .withVisibility(MediaCriteria.VISIBILITY.FORCE_GET_ALL));
  }

  public static String createPhoto(Connection con, Photo photo) throws SQLException, UtilException {
    return MediaDAO.saveMedia(con, OperationContext.fromUser(UserDetail.getCurrentRequester()),
        photo.getPhoto());
  }

  public static void createPath(Connection con, Photo photo, String albumId) throws SQLException,
      UtilException {
    MediaDAO.saveMediaPath(con, photo, albumId);
  }

  public static void updatePhoto(Connection con, Photo photo)
      throws SQLException {
    MediaDAO.saveMedia(con, OperationContext.fromUser(UserDetail.getCurrentRequester()),
        photo.getPhoto());
  }

  public static void removePhoto(Connection con, String photoId) throws SQLException {
    Photo photo = getByCriteria(con, MediaCriteria.fromMediaId(photoId));
    if (photo != null) {
      MediaDAO.deleteMedia(con, photo);
    }
  }

  /**
   * Gets last uploaded media.
   * @param con a database connection
   * @param instanceId the gallery application instance identifier
   * @return a collection of last uploaded photos
   * @throws SQLException
   */
  public static Collection<Photo> getLastRegisteredMedia(Connection con, String instanceId)
      throws SQLException {
    return findByCriteria(con, MediaCriteria.fromComponentInstanceId(instanceId)
        .orderedBy(CREATE_DATE_DESC, IDENTIFIER_DESC)
        .limitResultTo(GalleryComponentSettings.getNbMediaDisplayedPerPage()));
  }

  public static void deletePhotoPath(Connection con, String photoId,
      String instanceId) throws SQLException {
    Photo photo = getByCriteria(con,
        MediaCriteria.fromComponentInstanceId(instanceId).identifierIsOneOf(photoId));
    if (photo != null) {
      MediaDAO.deleteAllMediaPath(con, photo.getPhoto());
    }
  }

  public static void addPhotoPath(Connection con, String photoId,
      String albumId, String instanceId) throws SQLException {
    Photo photo = getByCriteria(con,
        MediaCriteria.fromComponentInstanceId(instanceId).identifierIsOneOf(photoId));
    if (photo != null) {
      MediaDAO.saveMediaPath(con, photo.getPhoto(), albumId);
    }
  }

  /**
   * Method to call in order to retrieve a unique photo.
   * @param connection
   * @param criteria
   * @return
   * @throws SQLException
   */
  private static Photo getByCriteria(Connection connection, MediaCriteria criteria)
      throws SQLException {
    return unique(findByCriteria(connection,
        criteria.withVisibility(MediaCriteria.VISIBILITY.FORCE_GET_ALL)));
  }

  /**
   * Method to call in order to retrieve photos.
   * @param connection
   * @param criteria
   * @return
   * @throws SQLException
   */
  private static List<Photo> findByCriteria(Connection connection, MediaCriteria criteria)
      throws SQLException {
    criteria.mediaTypeIsOneOf(MediaType.Photo);
    List<Photo> photos = new ArrayList<Photo>();
    for (Media media : MediaDAO.findByCriteria(connection, criteria)) {
      photos.add(media.getPhoto());
    }
    return photos;
  }
}
