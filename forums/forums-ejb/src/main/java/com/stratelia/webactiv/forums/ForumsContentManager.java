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
package com.stratelia.webactiv.forums;

import com.stratelia.silverpeas.classifyEngine.ClassifyEngine;
import com.stratelia.silverpeas.contentManager.ContentInterface;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.silverpeas.contentManager.SilverContentVisibility;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.forums.forumsException.ForumsRuntimeException;
import com.stratelia.webactiv.forums.models.ForumDetail;
import com.stratelia.webactiv.forums.models.ForumPK;
import org.silverpeas.util.ServiceProvider;
import org.silverpeas.util.exception.SilverpeasRuntimeException;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.stratelia.webactiv.forums.forumsManager.ejb.ForumsServiceProvider
    .getForumsService;

/**
 * The forums implementation of ContentInterface.
 */
public class ForumsContentManager implements ContentInterface {

  @Override
  public List<SilverContentInterface> getSilverContentById(List<Integer> alSilverContentId,
      String sComponentId, String sUserId, List<String> alContentUserRoles) {
    if (getContentManager() == null) {
      return new ArrayList<SilverContentInterface>();
    }
    return getHeaders(makePKArray(alSilverContentId, sComponentId));
  }

  /**
   * Method declaration
   *
   * @param pubId
   * @param peasId
   * @return
   * @see
   */
  public int getSilverObjectId(String pubId, String peasId) {
    SilverTrace.info("forums", "ForumsContentManager.getSilverObjectId()",
        "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubId);
    try {
      return getContentManager().getSilverContentId(pubId, peasId);
    } catch (Exception e) {
      throw new ForumsRuntimeException(
          "ForumsContentManager.getSilverObjectId()",
          SilverpeasRuntimeException.ERROR,
          "forums.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  /**
   * add a new content. It is registered to contentManager service
   *
   * @param con a Connection
   * @param forumPK the content to register
   * @param userId the creator of the content
   * @return the unique silverObjectId which identified the new content
   */
  public int createSilverContent(Connection con, ForumPK forumPK, String userId)
      throws ContentManagerException {
    SilverContentVisibility scv = new SilverContentVisibility(true);
    SilverTrace.info("forums", "ForumsContentManager.createSilverContent()",
        "root.MSG_GEN_ENTER_METHOD", "SilverContentVisibility = " + scv.toString());
    return getContentManager().addSilverContent(con, forumPK.getId(), forumPK.getComponentName(),
        userId, scv);
  }

  /**
   * update the visibility attributes of the content. Here, the type of content is a ForumDetail
   *
   * @param forumPK the content
   * @param userId the unique identifier of the user
   */
  public void updateSilverContentVisibility(ForumPK forumPK, String userId)
      throws ContentManagerException {
    int silverContentId = getContentManager().getSilverContentId(
        forumPK.getId(), forumPK.getComponentName());
    if (silverContentId == -1) {
      createSilverContent(null, forumPK, userId);
    } else {
      SilverContentVisibility scv = new SilverContentVisibility(
          isVisible(forumPK));
      SilverTrace.info("forums",
          "ForumsContentManager.updateSilverContentVisibility()",
          "root.MSG_GEN_ENTER_METHOD", "SilverContentVisibility = "
          + scv.toString());
      getContentManager().updateSilverContentVisibilityAttributes(scv, forumPK.getComponentName(),
          silverContentId);
      ClassifyEngine.clearCache();
    }
  }

  /**
   * delete a content. It is registered to contentManager service
   *
   * @param con a Connection
   * @param forumPK the identifiant of the content to unregister
   */
  public void deleteSilverContent(Connection con, ForumPK forumPK)
      throws ContentManagerException {
    int contentId = getContentManager().getSilverContentId(forumPK.getId(),
        forumPK.getComponentName());
    if (contentId != -1) {
      SilverTrace.info("forums", "ForumsContentManager.deleteSilverContent()",
          "root.MSG_GEN_ENTER_METHOD", "pubId = " + forumPK.getId()
          + ", contentId = " + contentId);
      getContentManager().removeSilverContent(con, contentId,
          forumPK.getComponentName());
    }
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  private boolean isVisible(ForumPK forumPK) {
    return true;
  }

  /**
   * return a list of forumPK according to a list of silverContentId
   *
   * @param idList a list of silverContentId
   * @param componentId the id of the instance
   * @return a list of forumPK
   */
  private List<ForumPK> makePKArray(List<Integer> idList, String componentId) {
    List<ForumPK> fks = new ArrayList<ForumPK>();
    ForumPK forumPK = null;
    Iterator<Integer> iter = idList.iterator();
    String id = null;

    // for each silverContentId, we get the corresponding forumId
    while (iter.hasNext()) {
      int contentId = iter.next().intValue();

      try {
        id = getContentManager().getInternalContentId(contentId);
        forumPK = new ForumPK(componentId, id);
        fks.add(forumPK);
      } catch (ClassCastException ignored) {
        // ignore unknown item
      } catch (ContentManagerException ignored) {
        // ignore unknown item
      }
    }
    return fks;
  }

  /**
   * return a list of silverContent according to a list of ForumPK
   *
   * @param ids a list of ForumPK
   * @return a list of ForumDetail
   */
  private List getHeaders(List<ForumPK> ids) {
    Collection<ForumDetail> forumDetails = getForumsService().getForums(ids);
    List<ForumDetail> headers = new ArrayList<ForumDetail>(forumDetails.size());
    for (ForumDetail forumDetail : forumDetails) {
      forumDetail.setIconUrl("forumsSmall.gif");
      headers.add(forumDetail);
    }

    return headers;
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  private ContentManager getContentManager() {
    if (contentManager == null) {
      contentManager = ServiceProvider.getService(ContentManager.class);
    }
    return contentManager;
  }

  private ContentManager contentManager = null;
}
