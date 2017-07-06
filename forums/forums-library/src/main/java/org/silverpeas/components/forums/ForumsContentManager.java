/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.forums;

import org.silverpeas.components.forums.model.ForumDetail;
import org.silverpeas.components.forums.model.ForumPK;
import org.silverpeas.components.forums.service.ForumsRuntimeException;
import org.silverpeas.components.forums.service.ForumsServiceProvider;
import org.silverpeas.core.contribution.contentcontainer.content.ContentInterface;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManager;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerProvider;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentInterface;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentVisibility;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.pdc.classification.ClassifyEngine;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Singleton;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The forums implementation of ContentInterface.
 */
@Singleton
public class ForumsContentManager implements ContentInterface {

  /**
   * Hidden constructor as this implementation must be GET by CDI mechanism.
   */
  protected ForumsContentManager() {
  }

  @Override
  public List<SilverContentInterface> getSilverContentById(List<Integer> alSilverContentId,
      String sComponentId, String sUserId) {
    if (getContentManager() == null) {
      return new ArrayList<>();
    }
    return getHeaders(makePKArray(alSilverContentId, sComponentId));
  }

  /**
   * @param pubId
   * @param componentId
   * @return
   */
  public int getSilverObjectId(String pubId, String componentId) {
    try {
      return getContentManager().getSilverContentId(pubId, componentId);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsContentManager.getSilverObjectId()",
          SilverpeasRuntimeException.ERROR, "forums.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  /**
   * add a new content. It is registered to contentManager service
   * @param con a Connection
   * @param forumPK the content to register
   * @param userId the creator of the content
   * @return the unique silverObjectId which identified the new content
   */
  public int createSilverContent(Connection con, ForumPK forumPK, String userId)
      throws ContentManagerException {
    SilverContentVisibility scv = new SilverContentVisibility(true);
    return getContentManager()
        .addSilverContent(con, forumPK.getId(), forumPK.getComponentName(), userId, scv);
  }

  /**
   * update the visibility attributes of the content. Here, the type of content is a ForumDetail
   * @param forumPK the content
   * @param userId the unique identifier of the user
   */
  public void updateSilverContentVisibility(ForumPK forumPK, String userId)
      throws ContentManagerException {
    int silverContentId =
        getContentManager().getSilverContentId(forumPK.getId(), forumPK.getComponentName());
    if (silverContentId == -1) {
      createSilverContent(null, forumPK, userId);
    } else {
      SilverContentVisibility scv = new SilverContentVisibility(true);

      getContentManager().updateSilverContentVisibilityAttributes(scv, silverContentId);
      ClassifyEngine.clearCache();
    }
  }

  /**
   * delete a content. It is registered to contentManager service
   * @param con a Connection
   * @param forumPK the identifiant of the content to unregister
   */
  public void deleteSilverContent(Connection con, ForumPK forumPK) throws ContentManagerException {
    int contentId =
        getContentManager().getSilverContentId(forumPK.getId(), forumPK.getComponentName());
    if (contentId != -1) {
      getContentManager().removeSilverContent(con, contentId);
    }
  }

  /**
   * return a list of forumPK according to a list of silverContentId
   * @param idList a list of silverContentId
   * @param componentId the id of the instance
   * @return a list of forumPK
   */
  private List<ForumPK> makePKArray(List<Integer> idList, String componentId) {
    List<ForumPK> fks = new ArrayList<>();
    // for each silverContentId, we get the corresponding forumId
    for (final Integer contentId : idList) {
      try {
        String id = getContentManager().getInternalContentId(contentId);
        ForumPK forumPK = new ForumPK(componentId, id);
        fks.add(forumPK);
      } catch (ClassCastException | ContentManagerException e) {
        // ignore unknown item
        SilverLogger.getLogger(this).debug(e.getMessage(), e);
      }
    }
    return fks;
  }

  /**
   * return a list of silverContent according to a list of ForumPK
   * @param ids a list of ForumPK
   * @return a list of ForumDetail
   */
  private List<SilverContentInterface> getHeaders(List<ForumPK> ids) {
    Collection<ForumDetail> forumDetails = ForumsServiceProvider.getForumsService().getForums(ids);
    List<SilverContentInterface> headers = new ArrayList<>(forumDetails.size());
    for (ForumDetail forumDetail : forumDetails) {
      forumDetail.setIconUrl("forumsSmall.gif");
      headers.add(forumDetail);
    }

    return headers;
  }

  private ContentManager getContentManager() {
    return ContentManagerProvider.getContentManager();
  }
}
