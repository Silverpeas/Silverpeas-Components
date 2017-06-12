/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia;

import org.silverpeas.components.kmelia.model.KmeliaRuntimeException;
import org.silverpeas.core.contribution.contentcontainer.content.ContentInterface;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManager;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerProvider;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentInterface;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentVisibility;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.pdc.classification.ClassifyEngine;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Singleton;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The kmelia implementation of ContentInterface.
 */
@Singleton
public class KmeliaContentManager implements ContentInterface, java.io.Serializable {
  private static final long serialVersionUID = 3525407153404515235L;

  /**
   * Hidden constructor as this implementation must be GET by CDI mechanism.
   */
  protected KmeliaContentManager() {
  }

  /**
   * Find all the SilverContent with the given list of SilverContentId
   *
   * @param ids list of silverContentId to retrieve
   * @param peasId the id of the instance
   * @param userId the id of the user who wants to retrieve silverContent
   * @return a List of SilverContent
   */
  @Override
  public List<SilverContentInterface> getSilverContentById(List<Integer>  ids, String peasId, String userId) {
    if (getContentManager() == null) {
      return new ArrayList<>();
    }

    return getHeaders(makePKArray(ids, peasId), peasId, userId);
  }

  public int getSilverObjectId(String pubId, String peasId) {

    try {
      return getContentManager().getSilverContentId(pubId, peasId);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(
          "KmeliaContentManager.getSilverObjectId()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  /**
   * add a new content. It is registered to contentManager service
   *
   * @param con a Connection
   * @param pubDetail the content to register
   * @param userId the creator of the content
   * @return the unique silverObjectId which identified the new content
   */
  public int createSilverContent(Connection con, PublicationDetail pubDetail,
      String userId) throws ContentManagerException {
    SilverContentVisibility scv = new SilverContentVisibility(pubDetail
        .getBeginDate(), pubDetail.getEndDate(), isVisible(pubDetail));
    return getContentManager().addSilverContent(con, pubDetail.getPK().getId(),
        pubDetail.getPK().getComponentName(), userId, scv);
  }

  /**
   * update the visibility attributes of the content. Here, the type of content is a
   * PublicationDetail
   *
   * @param pubDetail the content
   */
  public void updateSilverContentVisibility(PublicationDetail pubDetail)
      throws ContentManagerException {
    updateSilverContentVisibility(pubDetail, isVisible(pubDetail));
  }

  private void updateSilverContentVisibility(SilverContentVisibility scv,
      PublicationDetail pubDetail, int silverContentId)
      throws ContentManagerException {
    if (silverContentId == -1) {
      createSilverContent(null, pubDetail, pubDetail.getUpdaterId());
    } else {
      getContentManager().updateSilverContentVisibilityAttributes(scv, silverContentId);
    }
    ClassifyEngine.clearCache();
  }

  /**
   * update the visibility attributes of the content. Here, the type of content is a
   * PublicationDetail
   *
   * @param pubDetail the content
   * @param isVisible
   */
  public void updateSilverContentVisibility(PublicationDetail pubDetail,
      boolean isVisible) throws ContentManagerException {
    int silverContentId = getContentManager().getSilverContentId(
        pubDetail.getPK().getId(), pubDetail.getPK().getComponentName());
    SilverContentVisibility scv = new SilverContentVisibility(pubDetail
        .getBeginDate(), pubDetail.getEndDate(), isVisible);
    updateSilverContentVisibility(scv, pubDetail, silverContentId);
  }

  /**
   * delete a content. It is registered to contentManager service
   *
   * @param con a Connection
   * @param pubPK the identifiant of the content to unregister
   */
  public void deleteSilverContent(Connection con, PublicationPK pubPK)
      throws ContentManagerException {
    int contentId = getContentManager().getSilverContentId(pubPK.getId(),
        pubPK.getComponentName());
    if (contentId != -1) {
      getContentManager().removeSilverContent(con, contentId);
    }
  }

  private boolean isVisible(PublicationDetail pubDetail) {
    return PublicationDetail.VALID.equals(pubDetail.getStatus());
  }

  /**
   * return a list of publicationPK according to a list of silverContentId
   *
   * @param idList a list of silverContentId
   * @param peasId the id of the instance
   * @return a list of publicationPK
   */
  private List<PublicationPK> makePKArray(List<Integer> idList, String peasId) {
    List<PublicationPK> pks = new ArrayList<PublicationPK>();
    // for each silverContentId, we get the corresponding publicationId
    for (int contentId : idList) {
      try {
        String id = getContentManager().getInternalContentId(contentId);
        PublicationPK pubPK = new PublicationPK(id, peasId);
        pks.add(pubPK);
      } catch (ClassCastException | ContentManagerException e) {
        // ignore unknown item
        SilverLogger.getLogger(this).debug(e.getMessage(), e);
      }
    }
    return pks;
  }

  /**
   * return a list of silverContent according to a list of publicationPK
   *
   * @param ids a list of publicationPK
   * @return a list of publicationDetail
   */
  private List<SilverContentInterface> getHeaders(List<PublicationPK> ids, String componentId,
      String userId) {
    List<SilverContentInterface> headers = new ArrayList<>();
    KmeliaAuthorization security = new KmeliaAuthorization();
    boolean checkRights = security.isRightsOnTopicsEnabled(componentId);

    Collection<PublicationDetail> publicationDetails = getPublicationService().getPublications(ids);
    for (PublicationDetail pubDetail : publicationDetails) {
      if (!checkRights || security.isPublicationAvailable(pubDetail.getPK(), userId)) {
        pubDetail.setIconUrl("kmeliaSmall.gif");
        headers.add(pubDetail);
      }
    }
    return headers;
  }

  private ContentManager getContentManager() {
    return ContentManagerProvider.getContentManager();
  }

  private PublicationService getPublicationService() {
    return PublicationService.get();
  }
}