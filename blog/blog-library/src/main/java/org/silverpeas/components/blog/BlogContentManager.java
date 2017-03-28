/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
package org.silverpeas.components.blog;

import org.silverpeas.components.blog.model.BlogRuntimeException;
import org.silverpeas.core.contribution.contentcontainer.content.ContentInterface;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManager;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentVisibility;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.pdc.classification.ClassifyEngine;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The blog implementation of ContentInterface.
 */
@Singleton
public class BlogContentManager implements ContentInterface, Serializable {
  private static final long serialVersionUID = 8619139224896358447L;

  @Inject
  private ContentManager contentManager;
  @Inject
  private PublicationService currentPublicationService;

  /**
   * Hidden constructor as this implementation must be GET by CDI mechanism.
   */
  protected BlogContentManager() {
  }

  /**
   * Find all the SilverContent with the given list of SilverContentId
   * @param ids list of silverContentId to retrieve
   * @param instanceId the id of the instance
   * @param userId the id of the user who wants to retrieve silverContent
   * @return a List of SilverContent
   */
  @Override
  public List getSilverContentById(List<Integer> ids, String instanceId, String userId) {
    return getHeaders(getContentManager().getResourcesMatchingContents(ids), instanceId);
  }

  public int getSilverObjectId(String postId, String peasId) {

    try {
      return getContentManager().getSilverContentId(postId, peasId);
    } catch (Exception e) {
      throw new BlogRuntimeException("BlogContentManager.getSilverObjectId()",
          SilverpeasRuntimeException.ERROR, "blog.EX_GET_CONTENT_PDC", e);
    }
  }

  /**
   * return true if the publication is in Valid status
   * @param pubDetail the pubDetail
   * @return boolean
   */
  private boolean isVisible(PublicationDetail pubDetail) {
    return PublicationDetail.VALID.equals(pubDetail.getStatus());
  }

  /**
   * add a new content. It is registered to contentManager service
   * @param con a Connection
   * @param pubDetail the content to register
   * @param userId the creator of the content
   * @return the unique silverObjectId which identified the new content
   */
  public int createSilverContent(Connection con, PublicationDetail pubDetail, String userId)
      throws ContentManagerException {
    SilverContentVisibility scv =
        new SilverContentVisibility(pubDetail.getBeginDate(), pubDetail.getEndDate(),
            isVisible(pubDetail));
    return getContentManager()
        .addSilverContent(con, pubDetail.getPK().getId(), pubDetail.getPK().getComponentName(),
            userId, scv);
  }

  /**
   * delete a content. It is registered to contentManager service
   * @param con a Connection
   * @param pubPK the identifiant of the content to unregister
   */
  public void deleteSilverContent(Connection con, PublicationPK pubPK)
      throws ContentManagerException {
    int contentId = getContentManager().getSilverContentId(pubPK.getId(), pubPK.getComponentName());
    if (contentId != -1) {
      getContentManager().removeSilverContent(con, contentId, pubPK.getComponentName());
    }
  }

  /**
   * return a list of silverContent according to a list of publicationPK
   * @param ids a list of identifiers of publication.
   * @param instanceId identifier of the component instance.
   * @return a list of publicationDetail
   */
  private List<PublicationDetail> getHeaders(List<String> ids, String instanceId) {
    List<PublicationPK> pks = ids.stream()
        .map(id -> new PublicationPK(id, "useles", instanceId))
        .collect(Collectors.toList());
    List<PublicationDetail> headers = new ArrayList<>(ids.size());
    Collection<PublicationDetail> publicationDetails = getPublicationService().getPublications(pks);
    for (PublicationDetail pubDetail : publicationDetails) {
      pubDetail.setIconUrl("blogSmall.gif");
      headers.add(pubDetail);
    }
    return headers;
  }

  /**
   * update the visibility attributes of the content.
   * @param scv the silvercontentvisibility to update
   * @param pubDetail the pubDetail
   * @param silverContentId the silver content identifier to update, -1 if creation
   */
  private void updateSilverContentVisibility(SilverContentVisibility scv,
      PublicationDetail pubDetail, int silverContentId) throws ContentManagerException {
    if (silverContentId == -1) {
      createSilverContent(null, pubDetail, pubDetail.getUpdaterId());
    } else {
      getContentManager()
          .updateSilverContentVisibilityAttributes(scv, silverContentId);
    }
    ClassifyEngine.clearCache();
  }

  /**
   * update the visibility attributes of the content. Here, the type of content is a
   * PublicationDetail
   * @param pubDetail the content
   * @param isVisible is the publication visible
   */
  public void updateSilverContentVisibility(PublicationDetail pubDetail, boolean isVisible)
      throws ContentManagerException {
    int silverContentId = getContentManager()
        .getSilverContentId(pubDetail.getPK().getId(), pubDetail.getPK().getComponentName());
    SilverContentVisibility scv =
        new SilverContentVisibility(pubDetail.getBeginDate(), pubDetail.getEndDate(), isVisible);

    updateSilverContentVisibility(scv, pubDetail, silverContentId);
  }

  /**
   * update the visibility attributes of the content. Here, the type of content is a
   * PublicationDetail
   * @param pubDetail the content
   */
  public void updateSilverContentVisibility(PublicationDetail pubDetail)
      throws ContentManagerException {
    updateSilverContentVisibility(pubDetail, isVisible(pubDetail));
  }

  private ContentManager getContentManager() {
    return contentManager;
  }

  private PublicationService getPublicationService() {
    return currentPublicationService;
  }
}