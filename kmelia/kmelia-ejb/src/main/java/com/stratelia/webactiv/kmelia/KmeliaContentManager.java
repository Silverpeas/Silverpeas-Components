/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.stratelia.webactiv.kmelia;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.stratelia.silverpeas.classifyEngine.ClassifyEngine;
import com.stratelia.silverpeas.contentManager.ContentInterface;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.contentManager.SilverContentVisibility;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

/**
 * The kmelia implementation of ContentInterface.
 */
public class KmeliaContentManager implements ContentInterface, java.io.Serializable {

  private static final long serialVersionUID = 3525407153404515235L;

  /**
   * Find all the SilverContent with the given list of SilverContentId
   *
   * @param ids list of silverContentId to retrieve
   * @param peasId the id of the instance
   * @param userId the id of the user who wants to retrieve silverContent
   * @param userRoles the roles of the user
   * @return a List of SilverContent
   */
  @Override
  public List getSilverContentById(List ids, String peasId, String userId,
      List userRoles) {
    if (getContentManager() == null) {
      return new ArrayList();
    }

    return getHeaders(makePKArray(ids, peasId), peasId, userId);
  }

  public int getSilverObjectId(String pubId, String peasId) {
    SilverTrace.info("kmelia", "KmeliaContentManager.getSilverObjectId()",
        "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubId);
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
    SilverTrace.info("kmelia", "KmeliaContentManager.createSilverContent()",
        "root.MSG_GEN_ENTER_METHOD", "SilverContentVisibility = "
        + scv.toString());
    return getContentManager().addSilverContent(con, pubDetail.getPK().getId(),
        pubDetail.getPK().getComponentName(), userId, scv);
  }

  /**
   * update the visibility attributes of the content. Here, the type of content is a
   * PublicationDetail
   *
   * @param pubDetail the content
   * @param silverObjectId the unique identifier of the content
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
      getContentManager().updateSilverContentVisibilityAttributes(scv,
          pubDetail.getPK().getComponentName(), silverContentId);
    }
    ClassifyEngine.clearCache();
  }

  /**
   * update the visibility attributes of the content. Here, the type of content is a
   * PublicationDetail
   *
   * @param pubDetail the content
   * @param silverObjectId the unique identifier of the content
   */
  public void updateSilverContentVisibility(PublicationDetail pubDetail,
      boolean isVisible) throws ContentManagerException {
    int silverContentId = getContentManager().getSilverContentId(
        pubDetail.getPK().getId(), pubDetail.getPK().getComponentName());
    SilverContentVisibility scv = new SilverContentVisibility(pubDetail
        .getBeginDate(), pubDetail.getEndDate(), isVisible);
    SilverTrace.info("kmelia",
        "KmeliaContentManager.updateSilverContentVisibility()",
        "root.MSG_GEN_ENTER_METHOD", "SilverContentVisibility = "
        + scv.toString());
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
      SilverTrace.info("kmelia", "KmeliaContentManager.deleteSilverContent()",
          "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubPK.getId()
          + ", contentId = " + contentId);
      getContentManager().removeSilverContent(con, contentId,
          pubPK.getComponentName());
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
  private ArrayList<PublicationPK> makePKArray(List<Integer> idList, String peasId) {
    ArrayList<PublicationPK> pks = new ArrayList<PublicationPK>();
    // for each silverContentId, we get the corresponding publicationId
    for (int contentId : idList) {
      try {
        String id = getContentManager().getInternalContentId(contentId);
        PublicationPK pubPK = new PublicationPK(id, peasId);
        pks.add(pubPK);
      } catch (ClassCastException ignored) {
        // ignore unknown item
      } catch (ContentManagerException ignored) {
        // ignore unknown item
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
  private List<PublicationDetail> getHeaders(List<PublicationPK> ids, String componentId,
      String userId) {
    List<PublicationDetail> headers = new ArrayList<PublicationDetail>();
    KmeliaSecurity security = new KmeliaSecurity();
    boolean checkRights = security.isRightsOnTopicsEnabled(componentId);

    Collection<PublicationDetail> publicationDetails = getPublicationBm().getPublications(ids);
    for (PublicationDetail pubDetail : publicationDetails) {
      if (!checkRights || security.isPublicationAvailable(pubDetail.getPK(), userId)) {
        pubDetail.setIconUrl("kmeliaSmall.gif");
        headers.add(pubDetail);
      }
    }
    return headers;
  }

  private ContentManager getContentManager() {
    if (contentManager == null) {
      try {
        contentManager = new ContentManager();
      } catch (Exception e) {
        SilverTrace.fatal("kmelia", "KmeliaContentManager",
            "root.EX_UNKNOWN_CONTENT_MANAGER", e);
      }
    }
    return contentManager;
  }

  private PublicationBm getPublicationBm() {
    if (currentPublicationBm == null) {
      try {
        currentPublicationBm = EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
            PublicationBm.class);
      } catch (Exception e) {
        throw new KmeliaRuntimeException("KmeliaContentManager.getPublicationBm()",
            SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_PUBLICATIONBM_HOME",
            e);
      }
    }
    return currentPublicationBm;
  }
  private ContentManager contentManager = null;
  private PublicationBm currentPublicationBm = null;
}