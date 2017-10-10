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
import org.silverpeas.core.contribution.contentcontainer.content.AbstractContentInterface;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentVisibility;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.persistence.jdbc.bean.IdPK;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Singleton;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The questionReply implementation of ContentInterface.
 */
@Singleton
public class QuestionReplyContentManager extends AbstractContentInterface {

  private static final String CONTENT_ICON_FILE_NAME = "questionReplySmall.gif";

  /**
   * Hidden constructor as this implementation must be GET by CDI mechanism.
   */
  protected QuestionReplyContentManager() {
  }

  @Override
  protected String getContentIconFileName(final String componentInstanceId) {
    return CONTENT_ICON_FILE_NAME;
  }

  @Override
  protected Optional<Contribution> getContribution(final String resourceId,
      final String componentInstanceId) {
    final List<? extends Contribution> contributions =
        getAccessibleContributions(Collections.singletonList(resourceId), componentInstanceId,
            null);
    return contributions.isEmpty() ? Optional.empty() : Optional.of(contributions.get(0));
  }

  @Override
  protected List<Contribution> getAccessibleContributions(final List<String> resourceIds,
      final String componentInstanceId, final String currentUserId) {
    try {
      return QuestionManagerProvider.getQuestionManager().getQuestionsByIds(resourceIds).stream()
          .map(q -> new QuestionHeader(q, componentInstanceId, q.getCreationDate(),
              q.getCreatorId())).collect(Collectors.toList());
    } catch (QuestionReplyException e) {
      // skip unknown and ill formed id.
      SilverLogger.getLogger(this).error(e);
      return Collections.emptyList();
    }
  }

  /**
   * Add a new content. It is registered to contentManager service
   * @throws ContentManagerException on technical error.
   */
  void createSilverContent(Connection con, Question question) throws ContentManagerException {
    SilverContentVisibility scv = new SilverContentVisibility(isVisible(question));
    getContentManager().addSilverContent(con, question.getPK().getId(), question.getInstanceId(),
        question.getCreatorId(), scv);
  }

  /**
   * update the visibility attributes of the content. Here, the type of content is a Question.
   * @param question the content
   * @throws ContentManagerException on technical error.
   */
  void updateSilverContentVisibility(Question question) throws ContentManagerException {
    int silverContentId = getContentManager()
        .getSilverContentId(question.getPK().getId(), question.getPK().getComponentName());
    SilverContentVisibility scv = new SilverContentVisibility(isVisible(question));
    getContentManager().updateSilverContentVisibilityAttributes(scv, silverContentId);
  }

  /**
   * delete a content. It is registered to contentManager service
   * @param con a Connection
   * @param pk the identity of the content to unregister
   * @throws ContentManagerException on technical error.
   */
  void deleteSilverContent(Connection con, IdPK pk) throws ContentManagerException {
    deleteSilverContent(con, pk.getId(), pk.getComponentName());
  }

  private boolean isVisible(Question question) {
    return question.getPublicReplyNumber() != 0;
  }
}
