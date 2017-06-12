/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery;

import org.silverpeas.components.gallery.model.GalleryRuntimeException;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.model.MediaCriteria;
import org.silverpeas.components.gallery.model.MediaPK;
import org.silverpeas.components.gallery.service.GalleryService;
import org.silverpeas.core.contribution.contentcontainer.content.ContentInterface;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManager;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerProvider;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentInterface;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The gallery implementation of ContentInterface.
 */
@Singleton
public class GalleryContentManager implements ContentInterface, java.io.Serializable {
  private static final long serialVersionUID = 1L;

  /**
   * Hidden constructor as this implementation must be GET by CDI mechanism.
   */
  protected GalleryContentManager() {
  }

  /**
   * Find all the SilverContent with the given list of SilverContentId
   * @param ids list of silverContentId to retrieve
   * @param peasId the id of the instance
   * @param userId the id of the user who wants to retrieve silverContent
   * @return a List of SilverContent
   */
  @Override
  public List<SilverContentInterface> getSilverContentById(List<Integer> ids, String peasId,
      String userId) {
    if (getContentManager() == null) {
      return new ArrayList<>();
    }

    return getHeaders(makePKArray(ids, peasId));
  }

  public int getSilverObjectId(String mediaId, String peasId) {
    try {
      return getContentManager().getSilverContentId(mediaId, peasId);
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryContentManager.getSilverObjectId()",
          SilverpeasRuntimeException.ERROR, "gallery.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  /**
   * add a new content. It is registered to contentManager service
   * @param media the content to register
   * @param userId the creator of the content
   * @return the unique silverObjectId which identified the new content
   * @throws ContentManagerException
   */
  public int createSilverContent(Media media, String userId)
      throws ContentManagerException {
    try (Connection con = DBUtil.openConnection()) {
      return getContentManager()
          .addSilverContent(con, media.getMediaPK().getId(), media.getMediaPK().getComponentName(),
              userId);
    } catch (SQLException e) {
      throw new ContentManagerException(e.getMessage(), e);
    }
  }

  /**
   * delete a content. It is registered to contentManager service
   * @param mediaPK the identifiant of the content to unregister
   * @throws ContentManagerException
   */
  public void deleteSilverContent(MediaPK mediaPK) throws ContentManagerException {
    try (Connection con = DBUtil.openConnection()) {
      int contentId = getContentManager().getSilverContentId(mediaPK.getId(), mediaPK.getComponentName());
      if (contentId != -1) {
        getContentManager().removeSilverContent(con, contentId);
      }
    } catch (SQLException e) {
      throw new ContentManagerException(e.getMessage(), e);
    }
  }

  /**
   * return a list of mediaPK according to a list of silverContentId
   * @param idList a list of silverContentId
   * @param peasId the id of the instance
   * @return a list of MediaPK
   */
  private List<MediaPK> makePKArray(List<Integer> idList, String peasId) {
    List<MediaPK> pks = new ArrayList<>();
    // for each silverContentId, we get the corresponding mediaId
    for (int contentId : idList) {
      try {
        String id = getContentManager().getInternalContentId(contentId);
        MediaPK mediaPK = new MediaPK(id, peasId);
        pks.add(mediaPK);
      } catch (ClassCastException | ContentManagerException e) {
        // ignore unknown item
        SilverLogger.getLogger(this).debug(e.getMessage(), e);
      }
    }
    return pks;
  }

  /**
   * return a list of silverContent according to a list of mediaPK
   * @param ids a list of mediaPK
   * @return a list of media
   */
  private List<SilverContentInterface> getHeaders(List<MediaPK> ids) {
    List<SilverContentInterface> headers = new ArrayList<>();
    // création de la liste "headers" avec toutes les media (format Media)
    // en fonction de la liste "ids" des media (format MediaPK)
    if (ids != null) {
      for (MediaPK mediaPK : ids) {
        Media media = getGalleryService().getMedia(mediaPK, MediaCriteria.VISIBILITY.FORCE_GET_ALL);
        media.setIconUrl("gallerySmall.gif");
        headers.add(media);
      }
    }
    return headers;
  }

  private ContentManager getContentManager() {
    return ContentManagerProvider.getContentManager();
  }

  private GalleryService getGalleryService() {
    return ServiceProvider.getService(GalleryService.class);
  }
}
