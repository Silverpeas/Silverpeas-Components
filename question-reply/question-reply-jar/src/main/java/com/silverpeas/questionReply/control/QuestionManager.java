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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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
import com.silverpeas.questionReply.model.Recipient;
import com.silverpeas.questionReply.model.Reply;

import java.util.Collection;
import java.util.List;

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

  /*
  * Clos une liste de questions : updateQuestion
  */
  void closeQuestions(Collection<Long> questionIds) throws QuestionReplyException;

  void openQuestions(Collection<Long> questionIds) throws QuestionReplyException;

  /*
  * Modifie les destinataires d'une question : deleteRecipients, createRecipient
  */
  void updateQuestionRecipients(Question question) throws QuestionReplyException;

  /*
  * Affecte le status public à 0 de toutes les réponses d'une liste de questions : updateReply
  * Affecte le nombre de réponses publiques de la question à 0 : updateQuestion si question en
  * attente, on a demandé à la supprimer : deleteQuestion
  */
  void updateQuestionRepliesPublicStatus(Collection<Long> questionIds)
      throws QuestionReplyException;

  /*
  * Affecte le status private à 0 de toutes les réponses d'une liste de questions : updateReply
  * Affecte le nombre de réponses privées de la question à 0 : updateQuestion
  */
  void updateQuestionRepliesPrivateStatus(Collection<Long> questionIds)
      throws QuestionReplyException;

  /*
  * Affecte le status public à 0 d'une liste de réponses : updateReply
  * Décremente le nombre de réponses publiques de la question d'autant : updateQuestion
  */
  void updateRepliesPublicStatus(Collection<Long> replyIds, Question question)
      throws QuestionReplyException;

  /*
  * Affecte le status private à 0 d'une liste de réponses : updateReply Décremente le nombre de
  * réponses privées de la question d'autant : updateQuestion
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

  void deleteQuestionAndReplies(Collection<Long> questionIds) throws QuestionReplyException;

  List<Reply> getAllReplies(long questionId, String instanceId) throws QuestionReplyException;

  /*
  * recupère une question
  */
  Question getQuestion(long questionId) throws QuestionReplyException;

  Question getQuestionAndReplies(long questionId) throws QuestionReplyException;

  List<Question> getQuestionsByIds(List<String> ids) throws QuestionReplyException;

  /*
  * recupère la liste des réponses d'une question
  */
  List<Reply> getQuestionReplies(long questionId, String instanceId) throws QuestionReplyException;

  /*
  * recupère la liste des réponses publiques d'une question
  */
  List<Reply> getQuestionPublicReplies(long questionId, String instanceId) throws QuestionReplyException;

  /*
  * recupère la liste des réponses privées d'une question
  */
  List<Reply> getQuestionPrivateReplies(long questionId, String instanceId) throws QuestionReplyException;

  /*
  * recupère la liste des destinataires d'une question
  */
  List<Recipient> getQuestionRecipients(long questionId) throws QuestionReplyException;

  /*
  * recupère une réponse
  */
  Reply getReply(long replyId) throws QuestionReplyException;

  /*
  * Recupère la liste des questions emises par un utilisateur => Q dont il est l'auteur qui ne sont
  * pas closes ou closes avec réponses privées
  */
  List<Question> getSendQuestions(String userId, String instanceId)
      throws QuestionReplyException;

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

  List<Question> getAllQuestionsByCategory(String instanceId, String categoryId) throws
      QuestionReplyException;

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
}
