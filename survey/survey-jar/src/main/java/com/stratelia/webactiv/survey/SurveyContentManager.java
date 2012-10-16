/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.stratelia.silverpeas.contentManager.ContentInterface;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
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

  private ContentManager contentManager = null;
  private QuestionContainerBm currentQuestionContainerBm = null;

  /**
   * Find all the SilverContent with the given list of SilverContentId
   * @param ids list of silverContentId to retrieve
   * @param peasId the id of the instance
   * @param userId the id of the user who wants to retrieve silverContent
   * @param userRoles the roles of the user
   * @return a List of SilverContent
   */
  public List getSilverContentById(List<Integer> ids, String peasId, String userId, List<String> userRoles) {
    if (getContentManager() == null) {
      return new ArrayList<SilverContentInterface>();
    }

    return getHeaders(makePKArray(ids, peasId));
  }


  /**
   * return a list of silverContentId according to a list of publicationPK
   * @param idList a list of silverContentId
   * @param instanceId the id of the instance
   * @return a list of publicationPK
   */
  private List<QuestionContainerPK> makePKArray(List<Integer> idList, String instanceId) {
    List<QuestionContainerPK> pks = new ArrayList<QuestionContainerPK>();
    QuestionContainerPK pk = null;
    Iterator<Integer> iter = idList.iterator();
    String id = null;
    // for each silverContentId, we get the corresponding questionContainerId
    while (iter.hasNext()) {
      int contentId = (iter.next()).intValue();
      try {
        id = getContentManager().getInternalContentId(contentId);
        pk = new QuestionContainerPK(id, "useless", instanceId);
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
   * @param ids a list of publicationPK
   * @return a list of publicationDetail
   */
  private List<QuestionContainerHeader> getHeaders(List<QuestionContainerPK> pks) {
    List<QuestionContainerHeader> headers = new ArrayList<QuestionContainerHeader>();
    try {
      Collection<QuestionContainerHeader> questionContainerHeaders = getQuestionContainerBm()
          .getQuestionContainerHeaders((ArrayList) pks);
      for (QuestionContainerHeader qC : questionContainerHeaders) {
        qC.setIconUrl("surveySmall.gif");
        if (qC.getPK().getInstanceId().startsWith("pollingStation")) {
          qC.setIconUrl("pollingStationSmall.gif");
        }
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

}