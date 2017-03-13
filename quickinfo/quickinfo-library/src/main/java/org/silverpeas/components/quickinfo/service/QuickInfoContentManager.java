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

package org.silverpeas.components.quickinfo.service;

import org.silverpeas.core.contribution.contentcontainer.content.ContentInterface;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManager;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerProvider;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentInterface;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentVisibility;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.pdc.classification.ClassifyEngine;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuickInfoContentManager implements ContentInterface {

  private PublicationService publicationService;
  public final static String CONTENT_ICON = "quickinfoSmall.gif";

  /**
   * Find all the SilverContent with the given list of SilverContentId
   * @param ids list of silverContentId to retrieve
   * @param componentId the id of the instance
   * @param sUserId the id of the user who wants to retrieve silverContent
   * @return a List of SilverContent
   */
  @Override
  public List<SilverContentInterface> getSilverContentById(List<Integer> ids, String componentId,
      String sUserId) {
    return getHeaders(getContentManager().getResourcesMatchingContents(ids), componentId);
  }

  /**
   * add a new content. It is registered to contentManager service
   * @param con a Connection
   * @param pubDetail the content to register
   * @param userId the creator of the content
   * @return the unique silverObjectId which identified the new content
   */
  public int createSilverContent(Connection con, PublicationDetail pubDetail, String userId,
      boolean isVisible) throws ContentManagerException {

    SilverContentVisibility scv =
        new SilverContentVisibility(pubDetail.getBeginDate(), pubDetail.getEndDate(), isVisible);
    return getContentManager()
        .addSilverContent(con, pubDetail.getPK().getId(), pubDetail.getPK().getComponentName(),
            userId, scv);
  }

  /**
   * update the visibility attributes of the content. Here, the type of content is a
   * PublicationDetail
   * @param pubDetail the content
   */
  public void updateSilverContentVisibility(PublicationDetail pubDetail, boolean isVisible)
      throws ContentManagerException {
    int silverContentId = getContentManager()
        .getSilverContentId(pubDetail.getPK().getId(), pubDetail.getPK().getComponentName());
    if (silverContentId != -1) {
      SilverContentVisibility scv =
          new SilverContentVisibility(pubDetail.getBeginDate(), pubDetail.getEndDate(), isVisible);

      getContentManager()
          .updateSilverContentVisibilityAttributes(scv, silverContentId);
      ClassifyEngine.clearCache();
    } else {
      createSilverContent(null, pubDetail, pubDetail.getCreatorId(), isVisible);
    }
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
   * @param instanceId the unique identifier of the component instance.
   * @return a list of publicationDetail
   */
  private List getHeaders(List<String> ids, String instanceId) {
    List<PublicationPK> pks = ids.stream()
        .map(id -> new PublicationPK(id, "useles", instanceId))
        .collect(Collectors.toList());
    List<PublicationDetail> publicationDetails =
        new ArrayList<>(getPublicationService().getPublications(pks));
    List<PublicationDetail> headers = new ArrayList<>(publicationDetails.size());
    for (PublicationDetail pubDetail : publicationDetails) {
      pubDetail.setIconUrl(CONTENT_ICON);
      headers.add(pubDetail);
    }
    return headers;
  }

  private PublicationService getPublicationService() {
    if (publicationService == null) {
      publicationService = PublicationService.get();
    }
    return publicationService;
  }

  private ContentManager getContentManager() {
    return ContentManagerProvider.getContentManager();
  }
}
