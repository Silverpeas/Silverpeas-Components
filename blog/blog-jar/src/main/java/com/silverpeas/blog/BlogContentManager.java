/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
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
package com.silverpeas.blog;

import com.silverpeas.blog.model.BlogRuntimeException;
import com.stratelia.silverpeas.classifyEngine.ClassifyEngine;
import com.stratelia.silverpeas.contentManager.ContentInterface;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.contentManager.SilverContentVisibility;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.publication.control.PublicationService;
import com.stratelia.webactiv.publication.model.PublicationDetail;
import com.stratelia.webactiv.publication.model.PublicationPK;
import org.silverpeas.util.exception.SilverpeasRuntimeException;

import javax.inject.Inject;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The blog implementation of ContentInterface.
 */
public class BlogContentManager implements ContentInterface, java.io.Serializable {

  private static final long serialVersionUID = 8619139224896358447L;

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
  public List getSilverContentById(List<Integer> ids, String peasId, String userId,
      List<String> userRoles) {
    if (getContentManager() == null) {
      return new ArrayList();
    }

    return getHeaders(makePKArray(ids, peasId));
  }

  public int getSilverObjectId(String postId, String peasId) {
    SilverTrace.info("blog", "BlogContentManager.getSilverObjectId()",
        "root.MSG_GEN_ENTER_METHOD", "postId = " + postId);
    try {
      return getContentManager().getSilverContentId(postId, peasId);
    } catch (Exception e) {
      throw new BlogRuntimeException("BlogContentManager.getSilverObjectId()",
          SilverpeasRuntimeException.ERROR,
          "blog.EX_GET_CONTENT_PDC", e);
    }
  }
  
  /**
   * return true if the publication is in Valid status
   *
   * @param pubDetail the pubDetail
   * @return boolean
   */
  private boolean isVisible(PublicationDetail pubDetail) {
    return PublicationDetail.VALID.equals(pubDetail.getStatus());
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
    SilverTrace.info("blog","BlogContentManager.createSilverContent()",
     "root.MSG_GEN_ENTER_METHOD",
     "SilverContentVisibility = "+scv.toString());
    return getContentManager().addSilverContent(con, pubDetail.getPK().getId(),
        pubDetail.getPK().getComponentName(), userId, scv);
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
      SilverTrace.info("blog", "BlogContentManager.deleteSilverContent()",
          "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubPK.getId()
          + ", contentId = " + contentId);
      getContentManager().removeSilverContent(con, contentId,
          pubPK.getComponentName());
    }
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
    for (Integer contentId : idList) {
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
  private List<PublicationDetail> getHeaders(List<PublicationPK> ids) {
    List<PublicationDetail> headers = new ArrayList<PublicationDetail>(ids.size());
    Collection<PublicationDetail> publicationDetails = getPublicationService().getPublications(ids);
    for (PublicationDetail pubDetail : publicationDetails) {
      pubDetail.setIconUrl("blogSmall.gif");
      headers.add(pubDetail);
    }
    return headers;
  }
  
  /**
   * update the visibility attributes of the content.
   *
   * @param scv
   * @param pubDetail the pubDetail
   * @param silverContentId
   */
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
   * @param isVisible is the publication visible
   */
  public void updateSilverContentVisibility(PublicationDetail pubDetail,
      boolean isVisible) throws ContentManagerException {
    int silverContentId = getContentManager().getSilverContentId(
        pubDetail.getPK().getId(), pubDetail.getPK().getComponentName());
    SilverContentVisibility scv = new SilverContentVisibility(pubDetail
        .getBeginDate(), pubDetail.getEndDate(), isVisible);
    SilverTrace.info("blog",
        "BlogContentManager.updateSilverContentVisibility()",
        "root.MSG_GEN_ENTER_METHOD", "SilverContentVisibility = "
        + scv.toString());
    updateSilverContentVisibility(scv, pubDetail, silverContentId);
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

  private ContentManager getContentManager() {
    if (contentManager == null) {
      try {
        contentManager = new ContentManager();
      } catch (Exception e) {
        SilverTrace.fatal("blog", "BlogContentManager.getContentManager()",
            "root.EX_UNKNOWN_CONTENT_MANAGER", e);
      }
    }
    return contentManager;
  }

  private PublicationService getPublicationService() {
    if (currentPublicationService == null) {
        throw new BlogRuntimeException("BlogContentManager.getPublicationService()",
            SilverpeasRuntimeException.ERROR, "blog.EX_GET_PUBLICATIONBM_OBJECT");
    }
    return currentPublicationService;
  }

  private ContentManager contentManager = null;
  @Inject
  private PublicationService currentPublicationService;
}