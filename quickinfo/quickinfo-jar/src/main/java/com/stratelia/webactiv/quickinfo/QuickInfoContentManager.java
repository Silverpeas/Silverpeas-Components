/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.stratelia.webactiv.quickinfo;

import com.stratelia.silverpeas.classifyEngine.ClassifyEngine;
import com.stratelia.silverpeas.contentManager.ContentInterface;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.silverpeas.contentManager.SilverContentVisibility;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.publication.control.PublicationService;
import com.stratelia.webactiv.publication.model.PublicationDetail;
import com.stratelia.webactiv.publication.model.PublicationPK;
import org.silverpeas.util.ServiceProvider;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class QuickInfoContentManager implements ContentInterface {

  private ContentManager contentManager;
  private PublicationService currentPublicationService;
  public final static String CONTENT_ICON = "quickinfoSmall.gif";

  /**
   * Find all the SilverContent with the given list of SilverContentId
   *
   * @param ids list of silverContentId to retrieve
   * @param componentId the id of the instance
   * @param sUserId the id of the user who wants to retrieve silverContent
   * @param alContentUserRoles the roles of the user
   * @return a List of SilverContent
   */
  @Override
  public List<SilverContentInterface> getSilverContentById(List<Integer> ids,
      String componentId, String sUserId, List<String> alContentUserRoles) {
    if (getContentManager() == null) {
      return new ArrayList<SilverContentInterface>();
    }
    return getHeaders(makePKArray(ids, componentId));
  }

  public int getSilverObjectId(String pubId, String peasId) {
    SilverTrace.info("quickinfo", "QuickInfoContentManager.getSilverObjectId()",
        "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubId);
    try {
      return getContentManager().getSilverContentId(pubId, peasId);
    } catch (Exception e) {
      return 0;
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
      String userId, boolean isVisible) throws ContentManagerException {
    SilverTrace.info("quickinfo",
        "QuickInfoContentManager.createSilverContent()",
        "root.MSG_GEN_ENTER_METHOD", String.valueOf(pubDetail));
    SilverContentVisibility scv = new SilverContentVisibility(pubDetail
        .getBeginDate(), pubDetail.getEndDate(), isVisible);
    return getContentManager().addSilverContent(con, pubDetail.getPK().getId(),
        pubDetail.getPK().getComponentName(), userId, scv);
  }

  /**
   * update the visibility attributes of the content. Here, the type of content is a
   * PublicationDetail
   *
   * @param pubDetail the content
   */
  public void updateSilverContentVisibility(PublicationDetail pubDetail,
      boolean isVisible) throws ContentManagerException {
    int silverContentId = getContentManager().getSilverContentId(
        pubDetail.getPK().getId(), pubDetail.getPK().getComponentName());
    if (silverContentId != -1) {
      SilverContentVisibility scv =
          new SilverContentVisibility(pubDetail.getBeginDate(), pubDetail.getEndDate(), isVisible);
      SilverTrace.info("quickinfo", "QuickInfoContentManager.updateSilverContentVisibility()",
          "root.MSG_GEN_ENTER_METHOD", String.valueOf(scv));
      getContentManager().updateSilverContentVisibilityAttributes(scv,
          pubDetail.getPK().getComponentName(), silverContentId);
      ClassifyEngine.clearCache();
    } else {
      createSilverContent(null, pubDetail, pubDetail.getCreatorId(), isVisible);
    }
  }

  /**
   * delete a content. It is registered to contentManager service
   *
   * @param con a Connection
   * @param pubPK the identifiant of the content to unregister
   */
  public void deleteSilverContent(Connection con, PublicationPK pubPK)
      throws ContentManagerException {
    int contentId = getContentManager().getSilverContentId(pubPK.getId(), pubPK.getComponentName());
    if (contentId != -1) {
      SilverTrace.info("quickinfo", "QuickInfoContentManager.deleteSilverContent()",
          "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubPK.getId() + ", contentId = " + contentId);
      getContentManager().removeSilverContent(con, contentId, pubPK.getComponentName());
    }
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
    PublicationPK pubPK = null;
    Iterator<Integer> iter = idList.iterator();
    String id = null;
    // for each silverContentId, we get the corresponding publicationId
    while (iter.hasNext()) {
      int contentId = iter.next().intValue();
      try {
        id = getContentManager().getInternalContentId(contentId);
        pubPK = new PublicationPK(id, "useless", peasId);
        pks.add(pubPK);
      } catch (ClassCastException ignored) {
        ;// ignore unknown item
      } catch (ContentManagerException ignored) {
        ;// ignore unknown item
      }
    }
    return pks;
  }

  /**
   * return a list of silverContent according to a list of publicationPK
   *
   * @param pubPKs a list of publicationPK
   * @return a list of publicationDetail
   */
  private List getHeaders(List<PublicationPK> pubPKs) {
    List<PublicationDetail> publicationDetails = new ArrayList<PublicationDetail>(
        getPublicationService().getPublications(pubPKs));
    List<PublicationDetail> headers = new ArrayList<PublicationDetail>(publicationDetails.size());
    for (PublicationDetail pubDetail : publicationDetails) {
      pubDetail.setIconUrl(CONTENT_ICON);
      headers.add(pubDetail);
    }
    return headers;
  }

  private PublicationService getPublicationService() {
    if (currentPublicationService == null) {
        currentPublicationService = ServiceProvider.getService(PublicationService.class);
    }
    return currentPublicationService;
  }

  private ContentManager getContentManager() {
    if (contentManager == null) {
      try {
        contentManager = new ContentManager();
      } catch (Exception e) {
        SilverTrace.fatal("quickinfo", "QuickInfoContentManager.getContentManager()",
            "root.EX_UNKNOWN_CONTENT_MANAGER", e);
      }
    }
    return contentManager;
  }
}
