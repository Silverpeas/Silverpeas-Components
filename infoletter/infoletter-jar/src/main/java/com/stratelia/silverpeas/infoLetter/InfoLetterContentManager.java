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
package com.stratelia.silverpeas.infoLetter;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.stratelia.silverpeas.classifyEngine.ClassifyEngine;
import com.stratelia.silverpeas.contentManager.ContentInterface;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.silverpeas.contentManager.SilverContentVisibility;
import com.stratelia.silverpeas.infoLetter.control.InfoLetterServiceProvider;
import com.stratelia.silverpeas.infoLetter.model.InfoLetterDataInterface;
import com.stratelia.silverpeas.infoLetter.model.InfoLetterPublicationPdC;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.persistence.IdPK;
import org.silverpeas.util.exception.SilverpeasException;

/**
 * The infoletter implementation of ContentInterface.
 */
public class InfoLetterContentManager implements ContentInterface {
  /**
   * Find all the SilverContent with the given list of SilverContentId
   * @param ids list of silverContentId to retrieve
   * @param sComponentId the id of the instance
   * @param userId the id of the user who wants to retrieve silverContent
   * @param userRoles the roles of the user
   * @return a List of SilverContent
   */
  @Override
  public List<SilverContentInterface> getSilverContentById(List<Integer> ids, String sComponentId,
      String userId, List<String> userRoles) {
    if (getContentManager() == null) {
      return new ArrayList<>();
    }
    return getHeaders(makePKArray(ids), sComponentId);
  }

  public int getSilverObjectId(String pubId, String peasId) {
    SilverTrace.info("infoletter", "InfoLetterContentManager.getSilverObjectId()",
        "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubId);
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
    SilverTrace.info("infoletter", "InfoLetterContentManager.createSilverContent()",
        "root.MSG_GEN_ENTER_METHOD", "SilverContentVisibility = " + scv.toString());
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
    SilverTrace.info("infoletter", "InfoLetterContentManager.updateSilverContentVisibility()",
        "root.MSG_GEN_ENTER_METHOD", "SilverContentVisibility = " + scv.toString());
    if (silverContentId == -1) {
      createSilverContent(null, ilPub, ilPub.getCreatorId());
    } else {
      getContentManager()
          .updateSilverContentVisibilityAttributes(scv, ilPub.getInstanceId(), silverContentId);
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
      SilverTrace.info("infoletter", "InfoLetterContentManager.deleteSilverContent()",
          "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubId + ", contentId = " + contentId);
      getContentManager().removeSilverContent(con, contentId, componentId);
    }
  }

  private boolean isVisible(InfoLetterPublicationPdC ilPub) {
    return (ilPub.getPublicationState() == 2);
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
      } catch (ClassCastException | ContentManagerException ignored) {
        // ignore unknown item
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
  private List getHeaders(List<String> ids, String componentId) {
    List<InfoLetterPublicationPdC> headers = new ArrayList<>();
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
    if (contentManager == null) {
      try {
        contentManager = new ContentManager();
      } catch (Exception e) {
        SilverTrace
            .fatal("infoletter", "InfoLetterContentManager", "root.EX_UNKNOWN_CONTENT_MANAGER", e);
      }
    }
    return contentManager;
  }

  private InfoLetterDataInterface getDataInterface() {
    if (dataInterface == null) {
      dataInterface = InfoLetterServiceProvider.getInfoLetterData();
    }
    return dataInterface;
  }

  private ContentManager contentManager = null;
  private InfoLetterDataInterface dataInterface = null;
}