/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery.dao;

import org.silverpeas.components.gallery.constant.MediaType;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.model.MediaCriteria;
import org.silverpeas.components.gallery.model.Photo;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery.unique;

public class PhotoDAO {

  private PhotoDAO() {
  }

  public static Photo getPhoto(String photoId) throws SQLException {
    Photo photo = getByCriteria(MediaCriteria.fromMediaId(photoId));
    if (photo == null) {
      // It is very ugly, but it was like it before the photo to media migration...
      photo = new Photo();
    }
    return photo;
  }

  public static Collection<Photo> getAllPhoto(String albumId, String instanceId,
      MediaCriteria.VISIBILITY visibility) throws SQLException {
    return findByCriteria(
        MediaCriteria.fromComponentInstanceId(instanceId).albumIdentifierIsOneOf(albumId)
            .withVisibility(visibility));
  }

  /**
   * Method to call in order to retrieve a unique photo.
   * @param criteria the media criteria.
   * @return the aimed photo if any, null otherwise.
   * @throws SQLException
   */
  private static Photo getByCriteria(MediaCriteria criteria) throws SQLException {
    return unique(findByCriteria(criteria.withVisibility(MediaCriteria.VISIBILITY.FORCE_GET_ALL)));
  }

  /**
   * Method to call in order to retrieve photos.
   * @param criteria the media criteria.
   * @return the list of photo media according to given criteria.
   * @throws SQLException
   */
  private static List<Photo> findByCriteria(MediaCriteria criteria) throws SQLException {
    criteria.mediaTypeIsOneOf(MediaType.Photo);
    return MediaDAO.findByCriteria(criteria).stream().map(Media::getPhoto)
        .collect(Collectors.toList());
  }
}
