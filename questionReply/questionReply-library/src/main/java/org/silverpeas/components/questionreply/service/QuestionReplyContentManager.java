/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.questionreply.service;

import org.silverpeas.components.questionreply.QuestionReplyException;
import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.core.contribution.contentcontainer.content.ContentInterface;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManager;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentInterface;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentVisibility;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.pdc.classification.ClassifyEngine;
import org.silverpeas.core.persistence.jdbc.bean.IdPK;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The questionReply implementation of ContentInterface.
 */
@Singleton
public class QuestionReplyContentManager implements ContentInterface {

  @Inject
  private ContentManager contentManager;

  /**
   * Hidden constructor as this implementation must be GET by CDI mechanism.
   */
  protected QuestionReplyContentManager() {
  }

  /**
   * Find all the SilverContent with the given SilverContentId
   * @param ids
   * @param userId
   * @param peasId
   */
  @Override
  public List<SilverContentInterface> getSilverContentById(List<Integer> ids, String peasId,
      String userId) {
    return getHeaders(makeIdArray(ids), peasId);
  }

  private List<String> makeIdArray(List<Integer> idList) {
    List<String> ids = new ArrayList<>(idList.size());
    for (int contentId : idList) {
      try {
        String id = this.contentManager.getInternalContentId(contentId);
        ids.add(id);
      } catch (ClassCastException | ContentManagerException e) {
        // ignore unknown item
        SilverLogger.getLogger(this).debug(e.getMessage(), e);
      }
    }
    return ids;
  }

  private List<SilverContentInterface> getHeaders(List<String> ids, String instanceId) {
    List<SilverContentInterface> headers = new ArrayList<>();
    try {
      Collection<Question> questions =
          QuestionManagerProvider.getQuestionManager().getQuestionsByIds(new ArrayList<String>(ids));
      for (Question question : questions) {
        headers.add(new QuestionHeader(question, instanceId, question.getCreationDate(),
            question.getCreatorId()));
      }
    } catch (QuestionReplyException e) {
      // skip unknown and ill formed id.
      SilverLogger.getLogger(this).debug(e.getMessage(), e);
    }
    return headers;
  }

  public int getSilverObjectId(String id, String peasId) throws QuestionReplyException {

    try {
      return this.contentManager.getSilverContentId(id, peasId);
    } catch (Exception e) {
      throw new QuestionReplyException("QuestionReplyContentManager.getSilverObjectId()",
          SilverpeasException.ERROR, "questionReply.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  /**
   * Add a new content. It is registered to contentManager service
   * @param con
   * @param question
   * @return the unique silverObjectId which identified the new content
   * @throws ContentManagerException
   */
  public int createSilverContent(Connection con, Question question) throws ContentManagerException {
    SilverContentVisibility scv = new SilverContentVisibility(isVisible(question));

    return this.contentManager
        .addSilverContent(con, question.getPK().getId(), question.getInstanceId(),
            question.getCreatorId(), scv);
  }

  /**
   * update the visibility attributes of the content. Here, the type of content is a Question.
   * @param question the content
   * @throws ContentManagerException
   */
  public void updateSilverContentVisibility(Question question) throws ContentManagerException {
    int silverContentId = this.contentManager
        .getSilverContentId(question.getPK().getId(), question.getPK().getComponentName());
    SilverContentVisibility scv = new SilverContentVisibility(isVisible(question));

    this.contentManager
        .updateSilverContentVisibilityAttributes(scv, silverContentId);
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

    this.contentManager.removeSilverContent(con, contentId);
  }

  private boolean isVisible(Question question) {
    return question.getPublicReplyNumber() != 0;
  }
}
