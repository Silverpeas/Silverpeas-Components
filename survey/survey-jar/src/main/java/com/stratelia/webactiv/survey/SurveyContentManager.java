/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
package com.stratelia.webactiv.survey;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.stratelia.silverpeas.contentManager.ContentInterface;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.questionContainer.control.QuestionContainerBm;
import com.stratelia.webactiv.util.questionContainer.control.QuestionContainerBmHome;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerHeader;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerPK;

/**
 * The survey implementation of ContentInterface
 */
public class SurveyContentManager implements ContentInterface {
  /**
   * Find all the SilverContent with the given list of SilverContentId
   * 
   * @param ids
   *          list of silverContentId to retrieve
   * @param peasId
   *          the id of the instance
   * @param userId
   *          the id of the user who wants to retrieve silverContent
   * @param userRoles
   *          the roles of the user
   * @return a List of SilverContent
   */
  public List getSilverContentById(List ids, String peasId, String userId,
      List userRoles) {
    if (getContentManager() == null)
      return new ArrayList();

    return getHeaders(makePKArray(ids, peasId));
  }

  /**
   * add a new content. It is registered to contentManager service
   * 
   * @param con
   *          a Connection
   * @param qC
   *          the content to register
   * @param userId
   *          the creator of the content
   * @return the unique silverObjectId which identified the new content
   */
  /*
   * public int createSilverContent(Connection con, QuestionContainerHeader qC,
   * String userId) throws ContentManagerException { SilverContentVisibility scv
   * = new SilverContentVisibility(isVisible(qC));
   * SilverTrace.info("survey","SurveyContentManager.createSilverContent()",
   * "root.MSG_GEN_ENTER_METHOD", "SilverContentVisibility = "+scv.toString());
   * return getContentManager().addSilverContent(con, qC.getPK().getId(),
   * qC.getPK().getComponentName(), userId, scv); }
   */

  /**
   * update the visibility attributes of the content. Here, the type of content
   * is a PublicationDetail
   * 
   * @param pubDetail
   *          the content
   * @param silverObjectId
   *          the unique identifier of the content
   */
  /*
   * public void updateSilverContentVisibility(QuestionContainerHeader qC, int
   * silverObjectId) throws ContentManagerException { SilverContentVisibility
   * scv = new SilverContentVisibility(isVisible(qC));
   * SilverTrace.info("survey",
   * "SurveyContentManager.updateSilverContentVisibility()",
   * "root.MSG_GEN_ENTER_METHOD", "SilverContentVisibility = "+scv.toString());
   * getContentManager().updateSilverContentVisibilityAttributes(scv,
   * qC.getPK().getComponentName(), silverObjectId);
   * ClassifyEngine.clearCache(); }
   */

  /**
   * delete a content. It is registered to contentManager service
   * 
   * @param con
   *          a Connection
   * @param pubPK
   *          the identifiant of the content to unregister
   */
  /*
   * public void deleteSilverContent(Connection con, QuestionContainerPK pk)
   * throws ContentManagerException { int contentId =
   * getContentManager().getSilverContentId(pk.getId(), pk.getComponentName());
   * SilverTrace.info("survey","SurveyContentManager.deleteSilverContent()",
   * "root.MSG_GEN_ENTER_METHOD",
   * "id = "+pk.getId()+", contentId = "+contentId);
   * getContentManager().removeSilverContent(con, contentId,
   * pk.getComponentName()); }
   */

  // this content is always visible
  /*
   * private boolean isVisible(QuestionContainerHeader qC) { return true; }
   */

  /**
   * return a list of silverContentId according to a list of publicationPK
   * 
   * @param idList
   *          a list of silverContentId
   * @param peasId
   *          the id of the instance
   * @return a list of publicationPK
   */
  private ArrayList makePKArray(List idList, String peasId) {
    ArrayList pks = new ArrayList();
    QuestionContainerPK pk = null;
    Iterator iter = idList.iterator();
    String id = null;
    // for each silverContentId, we get the corresponding questionContainerId
    while (iter.hasNext()) {
      int contentId = ((Integer) iter.next()).intValue();
      try {
        id = getContentManager().getInternalContentId(contentId);
        pk = new QuestionContainerPK(id, "useless", peasId);
        pks.add(pk);
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
   * @param ids
   *          a list of publicationPK
   * @return a list of publicationDetail
   */
  private List getHeaders(List pks) {
    QuestionContainerHeader qC = null;
    ArrayList headers = new ArrayList();
    try {
      ArrayList questionContainerHeaders = (ArrayList) getQuestionContainerBm()
          .getQuestionContainerHeaders((ArrayList) pks);
      for (int i = 0; i < questionContainerHeaders.size(); i++) {
        qC = (QuestionContainerHeader) questionContainerHeaders.get(i);
        qC.setIconUrl("surveySmall.gif");
        if (qC.getPK().getInstanceId().startsWith("pollingStation"))
          qC.setIconUrl("pollingStationSmall.gif");
        headers.add(qC);
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
        SilverTrace.fatal("survey", "SurveyContentManager.getContentManager()",
            "root.EX_UNKNOWN_CONTENT_MANAGER", e);
      }
    }
    return contentManager;
  }

  private QuestionContainerBm getQuestionContainerBm() {
    if (currentQuestionContainerBm == null) {
      try {
        QuestionContainerBmHome questionContainerBmHome = (QuestionContainerBmHome) EJBUtilitaire
            .getEJBObjectRef(JNDINames.QUESTIONCONTAINERBM_EJBHOME,
                QuestionContainerBmHome.class);
        currentQuestionContainerBm = questionContainerBmHome.create();
      } catch (Exception e) {
        SilverTrace.fatal("survey",
            "SurveyContentManager.getQuestionContainerBm()",
            "root.EX_UNKNOWN_CONTENT_MANAGER", e);
      }
    }
    return currentQuestionContainerBm;
  }

  private ContentManager contentManager = null;
  private QuestionContainerBm currentQuestionContainerBm = null;
}