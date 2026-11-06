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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.questionreply.repository;

import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.datasource.repository.jpa.BasicJpaEntityRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository of the questions asked in the FAQs. Any access to the questions is done within a
 * given FAQ, except for finding out the FAQ of a question.
 */
@Repository
public class QuestionRepository extends BasicJpaEntityRepository<Question> {

  private static final String INSTANCE_ID = "instanceId";
  private static final String CLOSED = "closed";

  /**
   * Gets the unique identifier of the FAQ in which is asked the specified question.
   * @param questionId the unique identifier of a question.
   * @return the unique identifier of the FAQ or nothing if there is no such question.
   */
  public Optional<String> getInstanceIdOf(long questionId) {
    return Optional.ofNullable(getFromNamedQuery("Question.findInstanceId",
        newNamedParameters().add("id", (int) questionId), String.class));
  }

  /**
   * Gets the specified question of the given FAQ.
   * @param questionId the unique identifier of the question.
   * @param instanceId the unique identifier of the FAQ.
   * @return the question or null if the FAQ has no such question.
   */
  public Question getById(long questionId, String instanceId) {
    return getFromNamedQuery("Question.findById",
        newNamedParameters().add("id", (int) questionId).add(INSTANCE_ID, instanceId));
  }

  /**
   * Gets the specified questions of the given FAQ. The questions of other FAQs are ignored.
   * @param questionIds the unique identifiers of the questions.
   * @param instanceId the unique identifier of the FAQ.
   * @return the questions of the FAQ among the specified ones.
   */
  public List<Question> getById(Collection<Integer> questionIds, String instanceId) {
    if (questionIds.isEmpty()) {
      return List.of();
    }
    return listFromNamedQuery("Question.findByIds",
        newNamedParameters().add("ids", questionIds).add(INSTANCE_ID, instanceId));
  }

  /**
   * Finds the questions of the given FAQ that aren't closed or that have at least one public
   * reply, the more recent first.
   * @param instanceId the unique identifier of the FAQ.
   * @return the questions found.
   */
  public List<Question> findNotClosedOrWithPublicReplies(String instanceId) {
    return listFromNamedQuery("Question.findNotClosedOrWithPublicReplies",
        newNamedParameters().add(INSTANCE_ID, instanceId).add(CLOSED, Question.CLOSED));
  }

  /**
   * Finds the questions of the given FAQ having at least one public reply.
   * @param instanceId the unique identifier of the FAQ.
   * @return the questions found.
   */
  public List<Question> findWithPublicReplies(String instanceId) {
    return listFromNamedQuery("Question.findWithPublicReplies",
        newNamedParameters().add(INSTANCE_ID, instanceId));
  }

  /**
   * Finds the questions asked by the specified user in the given FAQ that aren't closed or that
   * have at least one private reply.
   * @param userId the unique identifier of the user.
   * @param instanceId the unique identifier of the FAQ.
   * @return the questions found.
   */
  public List<Question> findSentBy(String userId, String instanceId) {
    return listFromNamedQuery("Question.findSentBy",
        newNamedParameters().add(INSTANCE_ID, instanceId)
            .add("userId", userId)
            .add(CLOSED, Question.CLOSED));
  }

  /**
   * Finds the questions of the given FAQ addressed to the specified user and that aren't closed.
   * @param userId the unique identifier of the user.
   * @param instanceId the unique identifier of the FAQ.
   * @return the questions found.
   */
  public List<Question> findReceivedBy(String userId, String instanceId) {
    return listFromNamedQuery("Question.findReceivedBy",
        newNamedParameters().add(INSTANCE_ID, instanceId)
            .add("userId", userId)
            .add(CLOSED, Question.CLOSED));
  }
}
