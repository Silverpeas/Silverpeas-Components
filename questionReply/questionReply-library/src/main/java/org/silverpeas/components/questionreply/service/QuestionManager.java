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
package org.silverpeas.components.questionreply.service;

import org.silverpeas.components.questionreply.QuestionReplyException;
import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.components.questionreply.model.Recipient;
import org.silverpeas.components.questionreply.model.Reply;
import org.silverpeas.core.pdc.pdc.model.PdcPosition;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * The service managing the questions and the replies of the FAQs, that is to say of the
 * QuestionReply application instances. Except for finding out the FAQ of a question, the
 * questions and the replies are always accessed within a given FAQ: those of other FAQs are
 * ignored.
 */
public interface QuestionManager {

  /*
   * enregistre une question et ses destinataires (attention les destinataires n'ont pas de
   * questionId)
   */
  long createQuestion(Question question) throws QuestionReplyException;

  /*
   * enregistre une réponse à une question => met à jour publicReplyNumber et/ou privateReplyNumber
   * et replyNumber de la question ainsi que le status à 1
   */
  long createReply(Reply reply, Question question) throws QuestionReplyException;

  /**
   * Closes the specified questions of the given FAQ. The questions of other FAQs are ignored.
   * @param questionIds the unique identifiers of the questions.
   * @param instanceId the unique identifier of the FAQ.
   * @throws QuestionReplyException if an error occurs.
   */
  void closeQuestions(Collection<Long> questionIds, String instanceId)
      throws QuestionReplyException;

  /**
   * Opens again the specified questions of the given FAQ. The questions of other FAQs are ignored.
   * @param questionIds the unique identifiers of the questions.
   * @param instanceId the unique identifier of the FAQ.
   * @throws QuestionReplyException if an error occurs.
   */
  void openQuestions(Collection<Long> questionIds, String instanceId)
      throws QuestionReplyException;

  /*
   * Affecte le status public à 0 d'une liste de réponses à la question : updateReply
   * Décremente le nombre de réponses publiques de la question d'autant : updateQuestion
   * Les réponses à d'autres questions sont ignorées.
   */
  void updateRepliesPublicStatus(Collection<Long> replyIds, Question question)
      throws QuestionReplyException;

  /*
   * Affecte le status private à 0 d'une liste de réponses à la question : updateReply
   * Décremente le nombre de réponses privées de la question d'autant : updateQuestion
   * Les réponses à d'autres questions sont ignorées.
   */
  void updateRepliesPrivateStatus(Collection<Long> replyIds, Question question)
      throws QuestionReplyException;

  /*
   * Modifie une question => la question est supprimée si publicReplyNumber et privateReplyNumber
   * sont à 0 et que la question est close => met à jour publicReplyNumber et/ou privateReplyNumber
   * et replyNumber de la question
   */
  void updateQuestion(Question question) throws QuestionReplyException;

  /*
   * Modifie une réponse => La réponse est supprimée si le status public et le status private sont à
   * 0
   */
  void updateReply(Reply reply) throws QuestionReplyException;

  /**
   * Deletes the specified questions of the given FAQ with their replies. The questions of other
   * FAQs are ignored.
   * @param questionIds the unique identifiers of the questions.
   * @param instanceId the unique identifier of the FAQ.
   * @throws QuestionReplyException if an error occurs.
   */
  void deleteQuestionAndReplies(Collection<Long> questionIds, String instanceId)
      throws QuestionReplyException;

  /**
   * Gets the private then the public replies to the specified question of the given FAQ.
   * @param questionId the unique identifier of the question.
   * @param instanceId the unique identifier of the FAQ.
   * @return the replies, or an empty list if the FAQ has no such question.
   * @throws QuestionReplyException if an error occurs.
   */
  List<Reply> getAllReplies(long questionId, String instanceId) throws QuestionReplyException;

  /**
   * Gets the unique identifier of the FAQ in which the specified question is asked. It is for the
   * permalinks of the questions, that don't refer the FAQ.
   * @param questionId the unique identifier of the question.
   * @return the unique identifier of the FAQ, or nothing if there is no such question.
   * @throws QuestionReplyException if an error occurs.
   */
  Optional<String> getInstanceIdOfQuestion(long questionId) throws QuestionReplyException;

  /**
   * Gets the specified question of the given FAQ.
   * @param questionId the unique identifier of the question.
   * @param instanceId the unique identifier of the FAQ.
   * @return the question, or null if the FAQ has no such question.
   * @throws QuestionReplyException if an error occurs.
   */
  Question getQuestion(long questionId, String instanceId) throws QuestionReplyException;

  /**
   * Gets the specified questions of the given FAQ. The questions of other FAQs are ignored.
   * @param ids the unique identifiers of the questions to get.
   * @param instanceId the unique identifier of the FAQ.
   * @return the questions found, or an empty list if no identifiers are given.
   * @throws QuestionReplyException if an error occurs while fetching the questions.
   */
  List<Question> getQuestionsByIds(List<String> ids, String instanceId)
      throws QuestionReplyException;

  /**
   * Gets all the replies to the specified question of the given FAQ.
   * @param questionId the unique identifier of the question.
   * @param instanceId the unique identifier of the FAQ.
   * @return the replies, or an empty list if the FAQ has no such question.
   * @throws QuestionReplyException if an error occurs.
   */
  List<Reply> getQuestionReplies(long questionId, String instanceId) throws QuestionReplyException;

  /**
   * Gets the public replies to the specified question of the given FAQ.
   * @param questionId the unique identifier of the question.
   * @param instanceId the unique identifier of the FAQ.
   * @return the public replies, or an empty list if the FAQ has no such question.
   * @throws QuestionReplyException if an error occurs.
   */
  List<Reply> getQuestionPublicReplies(long questionId, String instanceId)
      throws QuestionReplyException;

  /**
   * Gets the private replies to the specified question of the given FAQ.
   * @param questionId the unique identifier of the question.
   * @param instanceId the unique identifier of the FAQ.
   * @return the private replies, or an empty list if the FAQ has no such question.
   * @throws QuestionReplyException if an error occurs.
   */
  List<Reply> getQuestionPrivateReplies(long questionId, String instanceId)
      throws QuestionReplyException;

  /*
   * recupère la liste des destinataires d'une question
   */
  List<Recipient> getQuestionRecipients(long questionId) throws QuestionReplyException;

  /**
   * Gets the specified reply to a question of the given FAQ.
   * @param replyId the unique identifier of the reply.
   * @param instanceId the unique identifier of the FAQ.
   * @return the reply, or null if no question of the FAQ has such a reply.
   * @throws QuestionReplyException if an error occurs.
   */
  Reply getReply(long replyId, String instanceId) throws QuestionReplyException;

  /*
   * Recupère la liste des questions emises par un utilisateur => Q dont il est l'auteur qui ne sont
   * pas closes ou closes avec réponses privées
   */
  List<Question> getSendQuestions(String userId, String instanceId) throws QuestionReplyException;

  /*
   * Recupère la liste des questions recues par un utilisateur => Q dont il est le destinataire et
   * qui ne sont pas closes
   */
  List<Question> getReceiveQuestions(String userId, String instanceId)
      throws QuestionReplyException;

  /*
   * Recupère la liste des questions qui ne sont pas closes ou closes avec réponses publiques
   */
  List<Question> getQuestions(String instanceId) throws QuestionReplyException;

  /*
   * Recupère la liste de toutes les questions avec toutes ses réponses
   */
  List<Question> getAllQuestions(String instanceId) throws QuestionReplyException;

  List<Question> getAllQuestionsByCategory(String instanceId, String categoryId)
      throws QuestionReplyException;

  /*
   * Recupère la liste des questions publiques avec réponses
   */
  List<Question> getPublicQuestions(String instanceId) throws QuestionReplyException;

  /**
   * Create and persist a question reply
   * @param question the new question
   * @param reply the answer linked to the given question
   * @return long identifier of the created question
   * @throws QuestionReplyException
   */
  long createQuestionReply(Question question, Reply reply) throws QuestionReplyException;

  /**
   * Creates and persists a question with its reply, and classifies the question on the PdC on the
   * specified positions. The question is classified before the subscribers are notified about the
   * reply, and without alerting the subscribers on the PdC about the classification: they are
   * notified about the reply with the other subscribers.
   * @param question the new question
   * @param reply the answer linked to the given question
   * @param positions the positions on the PdC on which the question has to be classified. If
   * empty, the question isn't classified.
   * @return long identifier of the created question
   * @throws QuestionReplyException if an error occurs
   */
  long createQuestionReply(Question question, Reply reply, List<PdcPosition> positions)
      throws QuestionReplyException;
}
