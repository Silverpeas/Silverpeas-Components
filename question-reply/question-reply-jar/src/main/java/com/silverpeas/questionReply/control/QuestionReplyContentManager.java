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
package com.silverpeas.questionReply.control;

import com.silverpeas.questionReply.QuestionReplyException;
import com.silverpeas.questionReply.model.Question;
import com.stratelia.silverpeas.classifyEngine.ClassifyEngine;
import com.stratelia.silverpeas.contentManager.ContentInterface;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.contentManager.ContentManagerFactory;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.silverpeas.contentManager.SilverContentVisibility;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.util.exception.SilverpeasException;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The questionReply implementation of ContentInterface.
 */
public class QuestionReplyContentManager implements ContentInterface {

  ContentManager contentManager = ContentManagerFactory.getFactory().getContentManager();

  public QuestionReplyContentManager() {
  }

  /** Find all the SilverContent with the given SilverContentId
   * @param ids
   * @param userId
   * @param peasId
   * @param userRoles
   */
  @Override
  public List<SilverContentInterface> getSilverContentById(List<Integer> ids, String peasId,
      String userId, List<String> userRoles) {
    return getHeaders(makeIdArray(ids), peasId);
  }

  private List<String> makeIdArray(List<Integer> idList) {
    List<String> ids = new ArrayList<String>(idList.size());
    for (int contentId : idList) {
      try {
        String id = this.contentManager.getInternalContentId(contentId);
        ids.add(id);
      } catch (ClassCastException ignored) {
        // ignore unknown item
      } catch (ContentManagerException ignored) {
        // ignore unknown item
      }
    }
    return ids;
  }

  private List<SilverContentInterface> getHeaders(List<String> ids, String instanceId) {
    List<SilverContentInterface> headers = new ArrayList<SilverContentInterface>();
    try {
      Collection<Question> questions = QuestionManagerFactory.getQuestionManager().getQuestionsByIds(
          new ArrayList<String>(ids));
      for (Question question : questions) {
        headers.add(new QuestionHeader(question,
            instanceId, question.getCreationDate(), question.getCreatorId()));
      }
    } catch (QuestionReplyException e) {
      // skip unknown and ill formed id.
    }
    return headers;
  }

  public int getSilverObjectId(String id, String peasId) throws QuestionReplyException {
    SilverTrace.info("questionReply", "QuestionReplyContentManager.getSilverObjectId()",
        "root.MSG_GEN_ENTER_METHOD", "id = " + id);
    try {
      return this.contentManager.getSilverContentId(id, peasId);
    } catch (Exception e) {
      throw new QuestionReplyException("QuestionReplyContentManager.getSilverObjectId()",
          SilverpeasException.ERROR, "questionReply.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  /**
   * Add a new content. It is registered to contentManager service
   *
   * @param con
   * @param question
   * @return the unique silverObjectId which identified the new content
   * @throws ContentManagerException
   */
  public int createSilverContent(Connection con, Question question) throws ContentManagerException {
    SilverContentVisibility scv = new SilverContentVisibility(
        isVisible(question));
    SilverTrace.info("questionReply",
        "QuestionReplyContentManager.createSilverContent()",
        "root.MSG_GEN_ENTER_METHOD", "SilverContentVisibility = "
        + scv.toString());
    return this.contentManager.addSilverContent(con, question.getPK().getId(),
        question.getInstanceId(), question.getCreatorId(), scv);
  }

  /**
   * update the visibility attributes of the content. Here, the type of content is a Question.
   * @param question the content
   * @throws ContentManagerException
   */
  public void updateSilverContentVisibility(Question question) throws ContentManagerException {
    int silverContentId = this.contentManager.getSilverContentId(
        question.getPK().getId(), question.getPK().getComponentName());
    SilverContentVisibility scv = new SilverContentVisibility(
        isVisible(question));
    SilverTrace.info("questionReply",
        "QuestionReplyContentManager.updateSilverContentVisibility()",
        "root.MSG_GEN_ENTER_METHOD", "SilverContentVisibility = "
        + scv.toString());
    this.contentManager.updateSilverContentVisibilityAttributes(scv,
        question.getPK().getComponentName(), silverContentId);
    ClassifyEngine.clearCache();
  }

  /**
   * delete a content. It is registered to contentManager service
   * @param con a Connection
   * @param pk the identity of the content to unregister
   * @throws ContentManagerException
   */
  public void deleteSilverContent(Connection con, IdPK pk) throws ContentManagerException {
    int contentId = this.contentManager.getSilverContentId(pk.getId(), pk.getComponentName());
    SilverTrace.info("questionReply", "QuestionReplyContentManager.deleteSilverContent()",
        "root.MSG_GEN_ENTER_METHOD", "id = " + pk.getId() + ", contentId = " + contentId);
    this.contentManager.removeSilverContent(con, contentId, pk.getComponentName());
  }

  private boolean isVisible(Question question) {
    return (question.getPublicReplyNumber() != 0);
  }
}
