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

package com.stratelia.webactiv.quizz;

import java.util.*;
import java.rmi.RemoteException;

import com.stratelia.silverpeas.contentManager.*;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

import com.stratelia.webactiv.util.*;
import com.stratelia.webactiv.util.questionContainer.control.QuestionContainerBmHome;
import com.stratelia.webactiv.util.questionContainer.control.QuestionContainerBm;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerHeader;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerPK;

import javax.ejb.EJBException;

/**
 * The kmelia implementation of ContentInterface.
 */
public class QuizzContentManager implements ContentInterface {

  private ContentManager contentManager = null;
  private QuestionContainerBm questionContainerBm = null;

  /**
   * Find all the SilverContent with the given list of SilverContentId
   * @param ids list of silverContentId to retrieve
   * @param peasId the id of the instance
   * @param userId the id of the user who wants to retrieve silverContent
   * @param userRoles the roles of the user
   * @return a List of SilverContent
   */
  public List getSilverContentById(List ids, String peasId, String userId,
      List userRoles) {
    if (getContentManager() == null)
      return new ArrayList();

    return getHeaders(makePKArray(ids, peasId));
  }

  /**
   * return a list of publicationPK according to a list of silverContentId
   * @param idList a list of silverContentId
   * @param peasId the id of the instance
   * @return a list of publicationPK
   */
  private ArrayList makePKArray(List idList, String peasId) {
    ArrayList pks = new ArrayList();
    QuestionContainerPK qcPK = null;
    Iterator iter = idList.iterator();
    String id = null;
    // for each silverContentId, we get the corresponding publicationId
    while (iter.hasNext()) {
      int contentId = ((Integer) iter.next()).intValue();
      try {
        id = getContentManager().getInternalContentId(contentId);
        qcPK = new QuestionContainerPK(id, "useless", peasId);
        pks.add(qcPK);
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
  private List getHeaders(List ids) {
    QuestionContainerHeader containerHeader = null;
    ArrayList headers = new ArrayList();
    try {
      ArrayList questionHeaders = (ArrayList) getQuestionBm()
          .getQuestionContainerHeaders((ArrayList) ids);
      for (int i = 0; i < questionHeaders.size(); i++) {
        containerHeader = (QuestionContainerHeader) questionHeaders.get(i);
        containerHeader.setIconUrl("quizzSmall.gif");
        headers.add(containerHeader);
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
        SilverTrace.fatal("quizz", "QuizzContentManager",
            "root.EX_UNKNOWN_CONTENT_MANAGER", e);
      }
    }
    return contentManager;
  }

  private QuestionContainerBm getQuestionBm() {
    if (questionContainerBm == null) {
      try {
        QuestionContainerBmHome questionContainerBmHome = (QuestionContainerBmHome) EJBUtilitaire
            .getEJBObjectRef(JNDINames.QUESTIONCONTAINERBM_EJBHOME,
            QuestionContainerBmHome.class);

        this.questionContainerBm = questionContainerBmHome.create();
      } catch (Exception e) {
        throw new EJBException(e.getMessage());
      }
    }
    return questionContainerBm;
  }

}