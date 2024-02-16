/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
import org.silverpeas.components.questionreply.index.QuestionIndexer;
import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.components.questionreply.model.Recipient;
import org.silverpeas.components.questionreply.model.Reply;
import org.silverpeas.components.questionreply.service.notification.SubscriptionNotifier;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.bean.IdPK;
import org.silverpeas.core.persistence.jdbc.bean.PersistenceException;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAO;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAOFactory;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper.buildAndSend;

@Service
public class SilverpeasQuestionManager implements QuestionManager {

  private static final String QUESTION_ID = " questionId = ";
  private static final String INSTANCE_ID = " instanceId = '";
  @Inject
  private QuestionIndexer questionIndexer;
  private SilverpeasBeanDAO<Question> questionDao = null;
  private SilverpeasBeanDAO<Reply> replyDao = null;
  private SilverpeasBeanDAO<Recipient> recipientDao = null;
  @Inject
  private QuestionReplyContentManager contentManager;
  @Inject
  private OrganizationController controller;

  SilverpeasQuestionManager() {
    try {
      questionDao = SilverpeasBeanDAOFactory.getDAO(Question.class.getName());
      replyDao = SilverpeasBeanDAOFactory.getDAO(Reply.class.getName());
      recipientDao = SilverpeasBeanDAOFactory.getDAO(Recipient.class.getName());
    } catch (PersistenceException ex) {
      SilverLogger.getLogger(this).error(ex);
    }
  }

  /**
   * Create and persist a question with targeted recipient (be careful recipient doesn't have
   * question identifier set)
   * @param question the question to create
   * @return the generated question identifier
   * @throws QuestionReplyException
   */
  @Override
  public long createQuestion(Question question) throws QuestionReplyException {
    try (Connection con = DBUtil.openConnection()) {
      Collection<Recipient> recipients = question.readRecipients();
      IdPK pkQ = (IdPK) questionDao.add(con, question);
      question.setPK(pkQ);
      questionIndexer.createIndex(question, Collections.<Reply>emptyList());
      long idQ = pkQ.getIdAsLong();
      if (recipients != null) {
        for (Recipient recipient : recipients) {
          recipient.setQuestionId(idQ);
          createRecipient(con, recipient);
        }
      }
      question.setPK(pkQ);
      contentManager.createSilverContent(con, question);
      return idQ;
    } catch (SQLException | ContentManagerException | PersistenceException e) {
      throw new QuestionReplyException(e);
    }
  }

  /*
   * enregistre une réponse à une question => met à jour publicReplyNumber et/ou privateReplyNumber
   * et replyNumber de la question ainsi que le status à 1
   */
  @Override
  public long createReply(Reply reply, Question question) throws QuestionReplyException {
    try (Connection con = DBUtil.openConnection()) {
      IdPK pkR = (IdPK) replyDao.add(con, reply);
      WysiwygController.createFileAndAttachment(reply.readCurrentWysiwygContent(),
          new ResourceReference(pkR), reply.getCreatorId(), I18NHelper.DEFAULT_LANGUAGE);
      long idR = pkR.getIdAsLong();
      if (question.hasNewStatus()) {
        question.waitForAnswer();
      }
      updateQuestion(con, question);
      questionIndexer.updateIndex(question, getAllReplies(reply.getQuestionId(), question.
          getInstanceId()));
      notifySubscribers(question, reply);
      return idR;
    } catch (SQLException | PersistenceException e) {
      throw new QuestionReplyException(e);
    }
  }

  /*
   * enregistre un destinataire
   */
  private void createRecipient(Connection con, Recipient recipient) throws QuestionReplyException {
    try {
      recipientDao.add(con, recipient);
    } catch (PersistenceException e) {
      throw new QuestionReplyException(e);
    }
  }

  /*
   * supprime tous les destinataires d'une question
   */
  private void deleteRecipients(Connection con, long questionId) throws QuestionReplyException {
    try {
      IdPK pk = new IdPK();
      recipientDao.removeWhere(con, pk, QUESTION_ID + questionId);
    } catch (PersistenceException e) {
      throw new QuestionReplyException(e);
    }
  }

  /*
   * Clos une liste de questions : updateQuestion
   */
  @Override
  public void closeQuestions(Collection<Long> questionIds) throws QuestionReplyException {
    if (questionIds != null) {
      try (Connection con = DBUtil.openConnection()) {
        for (Long idQ : questionIds) {
          Question question = getQuestion(idQ);
          question.close();
          updateQuestion(con, question);
        }
      } catch (SQLException e) {
        throw new QuestionReplyException(e);
      }
    }
  }

  @Override
  public void openQuestions(Collection<Long> questionIds) throws QuestionReplyException {
    if (questionIds != null) {
      try (Connection con = DBUtil.openConnection()) {
        for (Long idQ : questionIds) {
          Question question = getQuestion(idQ);
          question.waitForAnswer();
          updateQuestion(con, question);
        }
      } catch (SQLException e) {
        throw new QuestionReplyException(e);
      }
    }
  }

  /*
   * Modifie les destinataires d'une question : deleteRecipients, createRecipient
   */
  @Override
  public void updateQuestionRecipients(Question question) throws QuestionReplyException {
    try (Connection con = DBUtil.openConnection()) {
      Collection<Recipient> recipients = question.readRecipients();
      deleteRecipients(con, ((IdPK) question.getPK()).getIdAsLong());
      if (recipients != null) {
        for (Recipient recipient : recipients) {
          recipient.setQuestionId(((IdPK) question.getPK()).getIdAsLong());
          createRecipient(con, recipient);
        }
      }
    } catch (SQLException | QuestionReplyException e) {
      throw new QuestionReplyException(e);
    }
  }

  /*
   * Affecte le status public à 0 de toutes les réponses d'une liste de questions : updateReply
   * Affecte le nombre de réponses publiques de la question à 0 : updateQuestion si question en
   * attente, on a demandé à la supprimer : deleteQuestion
   */
  @Override
  public void updateQuestionRepliesPublicStatus(Collection<Long> questionIds)
      throws QuestionReplyException {
    try (Connection con = DBUtil.openConnection()) {
      if (questionIds != null) {
        for (Long idQ : questionIds) {
          Question question = getQuestion(idQ);
          Collection<Reply> replies = getQuestionPublicReplies(idQ, question.getInstanceId());
          if (replies != null) {
            for (Reply reply : replies) {
              reply.setPublicReply(0);
              addComponentId(reply, question.getPK().getInstanceId());
              updateReply(con, reply);
            }
            updateQuestion(con, question);
          }
          if (question.hasNewStatus()) {
            deleteQuestion(con, idQ);
          }
        }
      }
    } catch (SQLException | QuestionReplyException e) {
      throw new QuestionReplyException(e);
    }
  }

  /*
   * Affecte le status private à 0 de toutes les réponses d'une liste de questions : updateReply
   * Affecte le nombre de réponses privées de la question à 0 : updateQuestion
   */
  @Override
  public void updateQuestionRepliesPrivateStatus(Collection<Long> questionIds)
      throws QuestionReplyException {
    try (Connection con = DBUtil.openConnection()) {
      if (questionIds != null) {
        for (Long idQ : questionIds) {
          Question question = getQuestion(idQ);
          Collection<Reply> replies = getQuestionPrivateReplies(idQ, question.getInstanceId());
          if (replies != null) {
            for (Reply reply : replies) {
              reply.setPrivateReply(0);
              addComponentId(reply, question.getPK().getInstanceId());
              updateReply(con, reply);
            }
            updateQuestion(con, question);
          }
        }
      }
    } catch (SQLException | QuestionReplyException e) {
      throw new QuestionReplyException(e);
    }
  }

  private void addComponentId(Reply reply, String componentId) {
    reply.getPK().setComponentName(componentId);
  }

  /*
   * Affecte le status public à 0 d'une liste de réponses : updateReply Décremente le nombre de
   * réponses publiques de la question d'autant : updateQuestion
   */
  @Override
  public void updateRepliesPublicStatus(Collection<Long> replyIds, Question question)
      throws QuestionReplyException {
    try (Connection con = DBUtil.openConnection()) {
      if (replyIds != null) {
        for (Long idR : replyIds) {
          Reply reply = getReply(idR);
          if (reply != null) {
            reply.setPublicReply(0);
            addComponentId(reply, question.getPK().getInstanceId());
            updateReply(con, reply);
          }
        }
        updateQuestion(con, question);
      }
    } catch (SQLException | QuestionReplyException e) {
      throw new QuestionReplyException(e);
    }
  }

  /*
   * Affecte le status private à 0 d'une liste de réponses : updateReply Décremente le nombre de
   * réponses privées de la question d'autant : updateQuestion
   */
  @Override
  public void updateRepliesPrivateStatus(Collection<Long> replyIds, Question question)
      throws QuestionReplyException {
    try (Connection con = DBUtil.openConnection()) {
      if (replyIds != null) {
        for (Long idR : replyIds) {
          Reply reply = getReply(idR);
          if (reply != null) {
            reply.setPrivateReply(0);
            addComponentId(reply, question.getPK().getInstanceId());
            updateReply(con, reply);
          }
        }
        updateQuestion(con, question);
      }
    } catch (SQLException | QuestionReplyException e) {
      throw new QuestionReplyException(e);
    }
  }

  /*
   * Modifie une question => la question est supprimée si publicReplyNumber et privateReplyNumber
   * sont à 0 et que la question est close => met à jour publicReplyNumber et/ou privateReplyNumber
   * et replyNumber de la question
   */
  private void updateQuestion(Connection con, Question question) throws QuestionReplyException {
    try {
      long idQ = ((IdPK) question.getPK()).getIdAsLong();
      question.setReplyNumber(getQuestionRepliesNumber(idQ));
      question.setPublicReplyNumber(getQuestionPublicRepliesNumber(idQ));
      question.setPrivateReplyNumber(getQuestionPrivateRepliesNumber(idQ));
      if ((question.getReplyNumber() == 0) && question.hasClosedStatus()) {
        deleteQuestion(con, idQ);
      } else {
        questionDao.update(con, question);
        questionIndexer.updateIndex(question, getAllReplies(idQ, question.getInstanceId()));
        question.getPK().setComponentName(question.getInstanceId());
        contentManager.updateSilverContentVisibility(question);
      }
    } catch (Exception e) {
      throw new QuestionReplyException(e);
    }
  }

  /*
   * Modifie une question => la question est supprimée si publicReplyNumber et privateReplyNumber
   * sont à 0 et que la question est close => met à jour publicReplyNumber et/ou privateReplyNumber
   * et replyNumber de la question
   */
  @Override
  public void updateQuestion(Question question) throws QuestionReplyException {
    try {
      long idQ = ((IdPK) question.getPK()).getIdAsLong();
      question.setReplyNumber(getQuestionRepliesNumber(idQ));
      question.setPublicReplyNumber(getQuestionPublicRepliesNumber(idQ));
      question.setPrivateReplyNumber(getQuestionPrivateRepliesNumber(idQ));
      if ((question.getReplyNumber() == 0) && question.hasClosedStatus()) {
        deleteQuestion(idQ);
      } else {
        questionDao.update(question);
        questionIndexer.updateIndex(question, getAllReplies(idQ, question.getInstanceId()));
        question.getPK().setComponentName(question.getInstanceId());
        contentManager.updateSilverContentVisibility(question);
      }
    } catch (Exception e) {
      throw new QuestionReplyException(e);
    }
  }

  /*
   * Modifie une réponse => La réponse est supprimée si le status public et le status private sont à
   * 0
   */
  private void updateReply(Connection con, Reply reply) throws QuestionReplyException {
    try {
      Question question = getQuestion(reply.getQuestionId());
      reply.getPK().setComponentName(question.getInstanceId());
      if ((reply.getPublicReply() == 0) && (reply.getPrivateReply() == 0)) {
        deleteReply(con, reply.getPK());
      } else {
        replyDao.update(con, reply);
        updateWysiwygContent(reply);
      }
      questionIndexer.updateIndex(question, getAllReplies(reply.getQuestionId(), question.
          getInstanceId()));
    } catch (PersistenceException e) {
      throw new QuestionReplyException(e);
    }
  }

  /*
   * Modifie une réponse => La réponse est supprimée si le status public et le status private sont à
   * 0
   */
  @Override
  public void updateReply(Reply reply) throws QuestionReplyException {
    try {
      Question question = getQuestion(reply.getQuestionId());
      if ((reply.getPublicReply() == 0) && (reply.getPrivateReply() == 0)) {
        deleteReply(((IdPK) reply.getPK()).getIdAsLong());
      } else {
        replyDao.update(reply);
        updateReplyWysiwygContent(reply);
      }
      questionIndexer.updateIndex(question, getAllReplies(reply.getQuestionId(), question.
          getInstanceId()));
    } catch (PersistenceException e) {
      throw new QuestionReplyException(e);
    }
  }

  private void updateReplyWysiwygContent(final Reply reply) {
    updateWysiwygContent(reply);
  }

  /*
   * supprime une question
   */
  private void deleteQuestion(Connection con, long questionId) throws QuestionReplyException {
    try {
      deleteRecipients(con, questionId);
      IdPK pk = new IdPK();
      pk.setIdAsLong(questionId);
      Question question = getQuestion(questionId);
      String peasId = question.getInstanceId();
      questionDao.remove(con, pk);
      questionIndexer.deleteIndex(question);
      pk.setComponentName(peasId);
      contentManager.deleteSilverContent(con, pk);
    } catch (Exception e) {
      throw new QuestionReplyException(e);
    }
  }

  /*
   * supprime une question
   */
  private void deleteQuestion(long questionId) throws QuestionReplyException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();
      deleteRecipients(con, questionId);
      IdPK pk = new IdPK();
      pk.setIdAsLong(questionId);
      Question question = getQuestion(questionId);
      String peasId = question.getInstanceId();
      questionDao.remove(con, pk);
      questionIndexer.deleteIndex(question);
      pk.setComponentName(peasId);
      contentManager.deleteSilverContent(con, pk);
    } catch (Exception e) {
      throw new QuestionReplyException(e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void deleteQuestionAndReplies(Collection<Long> questionIds) throws QuestionReplyException {
    // pour chaque question
    for (Long questionId : questionIds) {
      Connection con = null;
      try {
        con = DBUtil.openConnection();
        deleteRecipients(con, questionId);
        IdPK pk = new IdPK();
        pk.setIdAsLong(questionId);
        Question question = getQuestion(questionId);
        String peasId = question.getInstanceId();
        // rechercher les réponses
        Collection<Reply> replies = getAllReplies(questionId, peasId);
        for (Reply reply : replies) {
          long replyId = Long.parseLong(reply.getPK().getId());
          addComponentId(reply, question.getInstanceId());
          // supprimer la réponse et son index
          deleteReply(replyId);
        }
        questionIndexer.deleteIndex(question);
        // supprimer la question
        questionDao.remove(con, pk);
        pk.setComponentName(peasId);
        contentManager.deleteSilverContent(con, pk);
      } catch (Exception e) {
        throw new QuestionReplyException(e);
      } finally {
        DBUtil.close(con);
      }

    }
  }

  @Override
  public List<Reply> getAllReplies(long questionId, String instanceId)
      throws QuestionReplyException {
    List<Reply> allReplies = new ArrayList<>();
    try {
      Collection<Reply> privateReplies = getQuestionPrivateReplies(questionId, instanceId);
      allReplies.addAll(privateReplies);
      Collection<Reply> publicReplies = getQuestionPublicReplies(questionId, instanceId);
      allReplies.addAll(publicReplies);
      return allReplies;
    } catch (Exception e) {
      throw new QuestionReplyException(e);
    }
  }

  /*
   * supprime une réponse
   */
  private void deleteReply(long replyId) throws QuestionReplyException {
    try (Connection con = DBUtil.openConnection()) {
      IdPK pk = new IdPK();
      pk.setIdAsLong(replyId);
      replyDao.remove(con, pk);
    } catch (SQLException | PersistenceException e) {
      throw new QuestionReplyException(e);
    }
  }

  /*
   * supprime une réponse
   */
  private void deleteReply(Connection con, WAPrimaryKey replyId) throws QuestionReplyException {
    try {
      replyDao.remove(con, replyId);
      WysiwygController
          .deleteFile(replyId.getInstanceId(), replyId.getId(), I18NHelper.DEFAULT_LANGUAGE);
    } catch (PersistenceException e) {

      throw new QuestionReplyException(e);
    }
  }

  /*
   * recupère une question
   */
  @Override
  public Question getQuestion(long questionId) throws QuestionReplyException {
    try {
      IdPK pk = new IdPK();
      pk.setIdAsLong(questionId);
      return questionDao.findByPrimaryKey(pk);
    } catch (PersistenceException e) {
      throw new QuestionReplyException(e);
    }
  }

  @Override
  public Question getQuestionAndReplies(long questionId) throws QuestionReplyException {
    try {
      IdPK pk = new IdPK();
      pk.setIdAsLong(questionId);
      Question question = questionDao.findByPrimaryKey(pk);
      Collection<Reply> replies = getQuestionReplies(questionId, question.getInstanceId());
      question.writeReplies(replies);
      return question;
    } catch (PersistenceException e) {
      throw new QuestionReplyException(e);
    }
  }

  @Override
  public List<Question> getQuestionsByIds(List<String> ids) throws QuestionReplyException {
    StringBuilder where = new StringBuilder();
    int sizeOfIds = ids.size();
    for (int i = 0; i < sizeOfIds - 1; i++) {
      where.append(" id = ").append(ids.get(i)).append(" or ");
    }
    if (sizeOfIds != 0) {
      where.append(" id = ").append(ids.get(sizeOfIds - 1));
    }
    try {
      IdPK pk = new IdPK();
      return new ArrayList<>(questionDao.findByWhereClause(pk, where.toString()));
    } catch (PersistenceException e) {
      throw new QuestionReplyException(e);
    }
  }

  /*
   * recupère la liste des réponses d'une question
   */
  @Override
  public List<Reply> getQuestionReplies(long questionId, String instanceId)
      throws QuestionReplyException {
    try {
      IdPK pk = new IdPK();
      List<Reply> replies = new ArrayList<>(
          replyDao.findByWhereClause(pk, QUESTION_ID + questionId));
      for (Reply reply : replies) {
        reply.getPK().setComponentName(instanceId);
        reply.loadWysiwygContent();
      }
      return replies;
    } catch (PersistenceException e) {
      throw new QuestionReplyException(e);
    }
  }

  /*
   * recupère la liste des réponses publiques d'une question
   */
  @Override
  public List<Reply> getQuestionPublicReplies(long questionId, String instanceId)
      throws QuestionReplyException {
    try {
      IdPK pk = new IdPK();
      List<Reply> replies = new ArrayList<>(replyDao.findByWhereClause(pk,
          " publicReply = 1 and questionId = " + questionId));
      for (Reply reply : replies) {
        reply.getPK().setComponentName(instanceId);
        reply.loadWysiwygContent();
      }
      return replies;
    } catch (PersistenceException e) {
      throw new QuestionReplyException(e);
    }
  }

  /*
   * recupère la liste des réponses privées d'une question
   */
  @Override
  public List<Reply> getQuestionPrivateReplies(long questionId, String instanceId)
      throws QuestionReplyException {
    try {
      IdPK pk = new IdPK();
      List<Reply> replies = new ArrayList<>(replyDao.findByWhereClause(pk,
          " privateReply = 1 and questionId = " + questionId));
      for (Reply reply : replies) {
        reply.getPK().setComponentName(instanceId);
        reply.loadWysiwygContent();
      }
      return replies;
    } catch (PersistenceException e) {
      throw new QuestionReplyException(e);
    }
  }

  /*
   * recupère la liste des destinataires d'une question
   */
  @Override
  public List<Recipient> getQuestionRecipients(long questionId) throws QuestionReplyException {
    try {
      IdPK pk = new IdPK();
      return new ArrayList<>(recipientDao.findByWhereClause(pk, QUESTION_ID + questionId));
    } catch (PersistenceException e) {
      throw new QuestionReplyException(e);
    }
  }

  /*
   * recupère une réponse
   */
  @Override
  public Reply getReply(long replyId) throws QuestionReplyException {
    try {
      IdPK pk = new IdPK();
      pk.setIdAsLong(replyId);
      Reply reply = replyDao.findByPrimaryKey(pk);
      if (reply != null) {
        reply.loadWysiwygContent();
      }
      return reply;
    } catch (PersistenceException e) {
      throw new QuestionReplyException(e);
    }
  }

  /*
   * Recupère la liste des questions emises par un utilisateur => Q dont il est l'auteur qui ne sont
   * pas closes ou closes avec réponses privées
   */
  @Override
  public List<Question> getSendQuestions(String userId, String instanceId)
      throws QuestionReplyException {
    try {
      IdPK pk = new IdPK();
      return new ArrayList<>(questionDao.findByWhereClause(pk, INSTANCE_ID + instanceId +
          "' and (status <> 2 or privateReplyNumber > 0) and creatorId = " + userId));
    } catch (PersistenceException e) {
      throw new QuestionReplyException(e);
    }
  }

  /*
   * Recupère la liste des questions recues par un utilisateur => Q dont il est le destinataire et
   * qui ne sont pas closes
   */
  @Override
  public List<Question> getReceiveQuestions(String userId, String instanceId)
      throws QuestionReplyException {
    try {
      IdPK pk = new IdPK();
      return new ArrayList<>(questionDao.findByWhereClause(pk, INSTANCE_ID + instanceId +
          "' and status <> 2 and id IN (select questionId from SC_QuestionReply_Recipient " +
          "where userId = " +
          userId + ")"));
    } catch (PersistenceException e) {
      throw new QuestionReplyException(e);
    }
  }

  /*
   * Recupère la liste des questions qui ne sont pas closes ou closes avec réponses publiques
   */
  @Override
  public List<Question> getQuestions(String instanceId) throws QuestionReplyException {
    try {
      IdPK pk = new IdPK();
      return new ArrayList<>(questionDao.findByWhereClause(pk, INSTANCE_ID + instanceId +
          "' and  (status <> 2 or publicReplyNumber > 0) order by creationdate desc, id desc"));
    } catch (PersistenceException e) {
      throw new QuestionReplyException(e);
    }
  }

  /*
   * Recupère la liste de toutes les questions avec toutes ses réponses
   */
  @Override
  public List<Question> getAllQuestions(String instanceId) throws QuestionReplyException {
    List<Question> allQuestions = getQuestions(instanceId);
    List<Question> questions = new ArrayList<>(allQuestions.size());
    for (Question question : allQuestions) {
      Question fullQuestion = getQuestionAndReplies(Long.parseLong(question.getPK().getId()));
      questions.add(fullQuestion);
    }
    if (isSortable(instanceId)) {
      Collections.sort(questions, QuestionRegexpComparator.getInstance());
    }
    return questions;
  }

  @Override
  public List<Question> getAllQuestionsByCategory(String instanceId, String categoryId)
      throws QuestionReplyException {
    List<Question> allQuestions = getQuestions(instanceId);
    List<Question> questions = new ArrayList<>(allQuestions.size());
    for (Question question : allQuestions) {
      if ((StringUtil.isNotDefined(question.getCategoryId()) &&
          StringUtil.isNotDefined(categoryId)) ||
          (StringUtil.isDefined(categoryId) && StringUtil.isDefined(question.getCategoryId()) &&
              question.getCategoryId().equals(categoryId))) {
        // la question est sans catégorie
        Question fullQuestion = getQuestionAndReplies(Long.parseLong(question.getPK().getId()));
        questions.add(fullQuestion);
      }
    }
    if (isSortable(instanceId)) {
      Collections.sort(questions, QuestionRegexpComparator.getInstance());
    }
    return questions;
  }

  /*
   * Recupère la liste des questions publiques avec réponses
   */
  @Override
  public List<Question> getPublicQuestions(String instanceId) throws QuestionReplyException {
    try {
      IdPK pk = new IdPK();
      return new ArrayList<>(questionDao.findByWhereClause(pk,
          INSTANCE_ID + instanceId + "' AND publicReplyNumber > 0 ORDER BY id"));
    } catch (PersistenceException e) {
      throw new QuestionReplyException(e);
    }
  }

  /**
   * Save and persist question and reply given in parameter
   * @param question the new question
   * @param reply the answer linked to the given question
   * @return the created question identifier
   * @throws QuestionReplyException
   */
  @Override
  public long createQuestionReply(Question question, Reply reply) throws QuestionReplyException {
    Connection con = null;
    long idQ = -1;
    try {
      con = DBUtil.openConnection();
      IdPK pkQ = (IdPK) questionDao.add(con, question);
      idQ = pkQ.getIdAsLong();
      reply.setQuestionId(idQ);
      WAPrimaryKey pkR = replyDao.add(con, reply);
      reply.getPK().setId(pkR.getId());
      WysiwygController.createFileAndAttachment(reply.readCurrentWysiwygContent(),
          new ResourceReference(pkR), reply.getCreatorId(), I18NHelper.DEFAULT_LANGUAGE);
      questionIndexer.createIndex(question, Collections.singletonList(reply));
      Question updatedQuestion = getQuestion(idQ);
      contentManager.createSilverContent(con, updatedQuestion);
      notifySubscribers(question, reply);
    } catch (Exception e) {
      throw new QuestionReplyException(e);
    } finally {
      DBUtil.close(con);
    }
    return idQ;
  }

  /*
   * recupère le nombre de réponses d'une question
   */
  private int getQuestionRepliesNumber(long questionId) throws QuestionReplyException {
    try {
      IdPK pk = new IdPK();
      Collection<Reply> replies = replyDao.findByWhereClause(pk, QUESTION_ID + questionId);
      return replies.size();
    } catch (PersistenceException e) {
      throw new QuestionReplyException(e);
    }
  }

  /*
   * recupère le nombre de réponses publiques d'une question
   */
  private int getQuestionPublicRepliesNumber(long questionId) throws QuestionReplyException {
    try {
      IdPK pk = new IdPK();
      Collection<Reply> replies =
          replyDao.findByWhereClause(pk, " publicReply = 1 and questionId = " + questionId);
      return replies.size();
    } catch (PersistenceException e) {
      throw new QuestionReplyException(e);
    }
  }

  /*
   * recupère le nombre de réponses privées d'une question
   */
  private int getQuestionPrivateRepliesNumber(long questionId) throws QuestionReplyException {
    try {
      IdPK pk = new IdPK();
      Collection<Reply> replies = replyDao.findByWhereClause(pk,
          " privateReply = 1 and questionId = " + questionId);
      return replies.size();
    } catch (PersistenceException e) {
      throw new QuestionReplyException(e);
    }
  }

  protected void updateWysiwygContent(Reply reply) {
    if (WysiwygController.haveGotWysiwyg(reply.getPK().getInstanceId(), reply.getPK().getId(),
        I18NHelper.DEFAULT_LANGUAGE)) {
      WysiwygController
          .updateFileAndAttachment(reply.readCurrentWysiwygContent(), reply.getPK().getInstanceId(),
              reply.getPK().getId(), reply.getCreatorId(), I18NHelper.DEFAULT_LANGUAGE);
    } else {
      WysiwygController.createUnindexedFileAndAttachment(reply.readCurrentWysiwygContent(),
          new ResourceReference(reply.getPK()),
              reply.getCreatorId(), I18NHelper.DEFAULT_LANGUAGE);
    }
  }

  protected boolean isSortable(String instanceId) {
    return StringUtil
        .getBooleanValue(controller.getComponentParameterValue(instanceId, "sortable"));
  }

  private void notifySubscribers(Question question, Reply reply) {
    if (reply.getPublicReply() == 1) {
      final User sender = reply.readAuthor();
      buildAndSend(new SubscriptionNotifier(sender, question, reply));
    }
  }
}
