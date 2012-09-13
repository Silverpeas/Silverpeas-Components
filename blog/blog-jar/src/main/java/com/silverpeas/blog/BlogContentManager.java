/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.blog;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.silverpeas.blog.model.BlogRuntimeException;
import com.stratelia.silverpeas.contentManager.ContentInterface;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import java.util.Collection;

/**
 * The blog implementation of ContentInterface.
 */
public class BlogContentManager implements ContentInterface, java.io.Serializable {

  private static final long serialVersionUID = 8619139224896358447L;
  /**
   * Find all the SilverContent with the given list of SilverContentId
   * @param ids list of silverContentId to retrieve
   * @param peasId the id of the instance
   * @param userId the id of the user who wants to retrieve silverContent
   * @param userRoles the roles of the user
   * @return a List of SilverContent
   */
  @Override
  public List getSilverContentById(List<Integer> ids, String peasId, String userId,
      List<String> userRoles) {
    if (getContentManager() == null)
      return new ArrayList();

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
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  /**
   * add a new content. It is registered to contentManager service
   * @param con a Connection
   * @param pubDetail the content to register
   * @param userId the creator of the content
   * @return the unique silverObjectId which identified the new content
   */
  public int createSilverContent(Connection con, PublicationDetail pubDetail,
      String userId) throws ContentManagerException {
    // SilverTrace.info("blog","BlogContentManager.createSilverContent()",
    // "root.MSG_GEN_ENTER_METHOD",
    // "SilverContentVisibility = "+scv.toString());
    return getContentManager().addSilverContent(con, pubDetail.getPK().getId(),
        pubDetail.getPK().getComponentName(), userId);
  }

  /**
   * delete a content. It is registered to contentManager service
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
   * @param ids a list of publicationPK
   * @return a list of publicationDetail
   */
  private List<PublicationDetail> getHeaders(List<PublicationPK> ids) {
    ArrayList<PublicationDetail> headers = new ArrayList<PublicationDetail>();
    try {
      Collection<PublicationDetail> publicationDetails = getPublicationBm().getPublications(ids);
      for (PublicationDetail pubDetail : publicationDetails) {
        pubDetail.setIconUrl("blogSmall.gif");
        headers.add(pubDetail);
      }
    } catch (RemoteException e) {
      // skip unknown and ill formed id.
    }
    return headers;
  }

  private ContentManager getContentManager() {
    if (contentManager == null) {
      try {
        contentManager = new ContentManager();
      } catch (Exception e) {
        SilverTrace.fatal("blog", "blogContentManager",
            "root.EX_UNKNOWN_CONTENT_MANAGER", e);
      }
    }
    return contentManager;
  }

  private PublicationBm getPublicationBm() {
    if (currentPublicationBm == null) {
      try {
        PublicationBmHome publicationBmHome = (PublicationBmHome) EJBUtilitaire
            .getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
            PublicationBmHome.class);
        currentPublicationBm = publicationBmHome.create();
      } catch (Exception e) {
        throw new BlogRuntimeException("BlogContentManager.getPublicationBm()",
            SilverpeasRuntimeException.ERROR,
            "blog.EX_IMPOSSIBLE_DE_FABRIQUER_PUBLICATIONBM_HOME", e);
      }
    }
    return currentPublicationBm;
  }

  private ContentManager contentManager = null;
  private PublicationBm currentPublicationBm = null;
}