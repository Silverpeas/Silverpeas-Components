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
package org.silverpeas.components.infoletter;

import org.silverpeas.components.infoletter.model.InfoLetterPublicationPdC;
import org.silverpeas.components.infoletter.model.InfoLetterService;
import org.silverpeas.components.infoletter.service.InfoLetterServiceProvider;
import org.silverpeas.core.contribution.contentcontainer.content.ContentInterface;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManager;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerProvider;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentInterface;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentVisibility;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.pdc.classification.ClassifyEngine;
import org.silverpeas.core.persistence.jdbc.bean.IdPK;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Singleton;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static org.silverpeas.components.infoletter.model.InfoLetterPublication.PUBLICATION_VALIDEE;

/**
 * The infoletter implementation of ContentInterface.
 */
@Singleton
public class InfoLetterContentManager implements ContentInterface {

  private InfoLetterService dataInterface = null;

  /**
   * Hidden constructor as this implementation must be GET by CDI mechanism.
   */
  protected InfoLetterContentManager() {
  }

  /**
   * Find all the SilverContent with the given list of SilverContentId
   * @param ids list of silverContentId to retrieve
   * @param sComponentId the id of the instance
   * @param userId the id of the user who wants to retrieve silverContent
   * @return a List of SilverContent
   */
  @Override
  public List<SilverContentInterface> getSilverContentById(List<Integer> ids, String sComponentId,
      String userId) {
    if (getContentManager() == null) {
      return new ArrayList<>();
    }
    return getHeaders(makePKArray(ids), sComponentId);
  }

  public int getSilverObjectId(String pubId, String peasId) {

    try {
      return getContentManager().getSilverContentId(pubId, peasId);
    } catch (Exception e) {
      throw new InfoLetterException("InfoLetterContentManager.getSilverObjectId()",
          SilverpeasException.ERROR, "infoletter.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  /**
   * add a new content. It is registered to contentManager service
   * @param con a Connection
   * @param ilPub the content to register
   * @param userId the creator of the content
   * @return the unique silverObjectId which identified the new content
   */
  public int createSilverContent(Connection con, InfoLetterPublicationPdC ilPub, String userId)
      throws ContentManagerException {
    SilverContentVisibility scv = new SilverContentVisibility(isVisible(ilPub));

    return getContentManager()
        .addSilverContent(con, ilPub.getId(), ilPub.getInstanceId(), userId, scv);
  }

  /**
   * update the visibility attributes of the content. Here, the type of content is a
   * PublicationDetail
   * @param ilPub the content
   */
  public void updateSilverContentVisibility(InfoLetterPublicationPdC ilPub)
      throws ContentManagerException {
    int silverContentId =
        getContentManager().getSilverContentId(ilPub.getId(), ilPub.getInstanceId());
    SilverContentVisibility scv = new SilverContentVisibility(isVisible(ilPub));

    if (silverContentId == -1) {
      createSilverContent(null, ilPub, ilPub.getCreatorId());
    } else {
      getContentManager()
          .updateSilverContentVisibilityAttributes(scv, silverContentId);
      ClassifyEngine.clearCache();
    }

  }

  /**
   * delete a content. It is registered to contentManager service
   * @param con a Connection
   * @param pubId the identifiant of the content to unregister
   * @param componentId the identifiant of the component instance where the content to unregister
   * is
   */
  public void deleteSilverContent(Connection con, String pubId, String componentId)
      throws ContentManagerException {
    int contentId = getContentManager().getSilverContentId(pubId, componentId);
    if (contentId != -1) {

      getContentManager().removeSilverContent(con, contentId);
    }
  }

  private boolean isVisible(InfoLetterPublicationPdC ilPub) {
    return ilPub.getPublicationState() == PUBLICATION_VALIDEE;
  }

  /**
   * return a list of ids according to a list of silverContentId
   * @param idList a list of silverContentId
   * @return a list of ids
   */
  private List<String> makePKArray(List<Integer> idList) {
    List<String> pks = new ArrayList<>();
    // for each silverContentId, we get the corresponding infoLetterPublicationId
    for (Integer contentId : idList) {
      try {
        String id = getContentManager().getInternalContentId(contentId);
        pks.add(id);
      } catch (ClassCastException | ContentManagerException e) {
        // ignore unknown item
        SilverLogger.getLogger(this).debug(e.getMessage(), e);
      }
    }
    return pks;
  }

  /**
   * return a list of silverContent according to a list of publicationPK
   * @param ids a list of publicationPK
   * @param componentId the id of the instance
   * @return a list of publicationDetail
   */
  private List<SilverContentInterface> getHeaders(List<String> ids, String componentId) {
    List<SilverContentInterface> headers = new ArrayList<>();
    for (String pubId : ids) {
      IdPK pubPK = new IdPK();
      pubPK.setId(pubId);

      InfoLetterPublicationPdC ilPub = getDataInterface().getInfoLetterPublication(pubPK);
      ilPub.setInstanceId(componentId);
      headers.add(ilPub);
    }
    return headers;
  }

  private ContentManager getContentManager() {
    return ContentManagerProvider.getContentManager();
  }

  private InfoLetterService getDataInterface() {
    if (dataInterface == null) {
      dataInterface = InfoLetterServiceProvider.getInfoLetterData();
    }
    return dataInterface;
  }
}