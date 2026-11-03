/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.discussion.service;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.questioncontainer.answer.model.Answer;
import org.silverpeas.core.questioncontainer.question.model.Question;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerDetail;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerHeader;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerPK;
import org.silverpeas.core.questioncontainer.container.service.QuestionContainerService;

/** Adapter to the legacy Vote engine, with compensation if its announcement rolls back. */
@Service
public class DiscussionVotePlanner {
  @Inject private QuestionContainerService containers;

  @Transactional(Transactional.TxType.NOT_SUPPORTED)
  public QuestionContainerPK create(DiscussionVoteInput input, String spaceId, boolean forcedAnonymous) {
    var pk = new QuestionContainerPK(null, spaceId, input.instanceId());
    var header = new QuestionContainerHeader(pk, input.question(), "", User.getCurrentRequester().getId(),
        null, null, null, false, 0, 1, forcedAnonymous || input.anonymous(),
        QuestionContainerHeader.IMMEDIATE_RESULTS, QuestionContainerHeader.TWICE_DISPLAY_RESULTS);
    var question = new Question(null, null, input.question(), "", "", null, input.multiple() ? "checkbox" : "radio", 0);
    question.setAnswers(input.answers().stream().map(answer -> new Answer(null, null, answer, 0, false, "", 0, false, null, null)).toList());
    var detail = new QuestionContainerDetail(header, List.of(question), List.of(), List.of());
    try {
      return containers.createQuestionContainer(pk, detail, User.getCurrentRequester().getId());
    } catch (RuntimeException failure) {
      // The native service updates the header PK before saving questions/answers.
      if (header.getPK().getId() != null) {
        try { containers.deleteQuestionContainer(header.getPK()); }
        catch (RuntimeException cleanup) { failure.addSuppressed(cleanup); }
      }
      throw failure;
    }
  }

  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public void rollback(@Observes(during = TransactionPhase.AFTER_FAILURE) DiscussionVoteRollback event) {
    containers.deleteQuestionContainer(event.pk());
  }
}
