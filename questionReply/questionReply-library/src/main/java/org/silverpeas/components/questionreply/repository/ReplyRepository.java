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

import org.silverpeas.components.questionreply.model.Reply;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.datasource.repository.jpa.BasicJpaEntityRepository;
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;

import java.util.Collection;
import java.util.List;

/**
 * Repository of the replies to the questions of the FAQs. Any access to the replies is done
 * through their question.
 */
@Repository
public class ReplyRepository extends BasicJpaEntityRepository<Reply> {

  private static final String QUESTION_ID = "questionId";
  private static final String INSTANCE_ID = "instanceId";

  /**
   * Gets the specified reply to a question of the given FAQ.
   * @param replyId the unique identifier of the reply.
   * @param instanceId the unique identifier of the FAQ.
   * @return the reply or null if no question of the FAQ has such a reply.
   */
  public Reply getById(long replyId, String instanceId) {
    return getFromNamedQuery("Reply.findById",
        newNamedParameters().add("id", (int) replyId).add(INSTANCE_ID, instanceId));
  }

  /**
   * Gets the specified replies to the given question. The replies to other questions are ignored.
   * @param replyIds the unique identifiers of the replies.
   * @param questionId the unique identifier of the question.
   * @return the replies to the question among the specified ones.
   */
  public List<Reply> getById(Collection<Integer> replyIds, long questionId) {
    if (replyIds.isEmpty()) {
      return List.of();
    }
    return listFromNamedQuery("Reply.findByIdsOfQuestion",
        newNamedParameters().add("ids", replyIds).add(QUESTION_ID, (int) questionId));
  }

  /**
   * Finds all the replies to the specified question of the given FAQ.
   * @param questionId the unique identifier of the question.
   * @param instanceId the unique identifier of the FAQ.
   * @return the replies to the question.
   */
  public List<Reply> findByQuestion(long questionId, String instanceId) {
    return listFromNamedQuery("Reply.findByQuestion", questionOf(questionId, instanceId));
  }

  /**
   * Finds the public replies to the specified question of the given FAQ.
   * @param questionId the unique identifier of the question.
   * @param instanceId the unique identifier of the FAQ.
   * @return the public replies to the question.
   */
  public List<Reply> findPublicByQuestion(long questionId, String instanceId) {
    return listFromNamedQuery("Reply.findPublicByQuestion", questionOf(questionId, instanceId));
  }

  /**
   * Finds the private replies to the specified question of the given FAQ.
   * @param questionId the unique identifier of the question.
   * @param instanceId the unique identifier of the FAQ.
   * @return the private replies to the question.
   */
  public List<Reply> findPrivateByQuestion(long questionId, String instanceId) {
    return listFromNamedQuery("Reply.findPrivateByQuestion", questionOf(questionId, instanceId));
  }

  /**
   * Counts the replies to the specified question.
   * @param questionId the unique identifier of the question.
   * @return the number of replies.
   */
  public int countByQuestion(long questionId) {
    return count("Reply.countByQuestion", questionId);
  }

  /**
   * Counts the public replies to the specified question.
   * @param questionId the unique identifier of the question.
   * @return the number of public replies.
   */
  public int countPublicByQuestion(long questionId) {
    return count("Reply.countPublicByQuestion", questionId);
  }

  /**
   * Counts the private replies to the specified question.
   * @param questionId the unique identifier of the question.
   * @return the number of private replies.
   */
  public int countPrivateByQuestion(long questionId) {
    return count("Reply.countPrivateByQuestion", questionId);
  }

  private int count(String namedQuery, long questionId) {
    Long count = getFromNamedQuery(namedQuery,
        newNamedParameters().add(QUESTION_ID, (int) questionId), Long.class);
    return count == null ? 0 : count.intValue();
  }

  private NamedParameters questionOf(long questionId, String instanceId) {
    return newNamedParameters().add(QUESTION_ID, (int) questionId).add(INSTANCE_ID, instanceId);
  }
}
