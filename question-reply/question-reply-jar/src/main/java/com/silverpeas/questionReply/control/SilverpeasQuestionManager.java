/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.questionReply.control;

import com.silverpeas.questionReply.QuestionReplyException;
import com.silverpeas.questionReply.control.notification.SubscriptionNotifier;
import com.silverpeas.questionReply.index.QuestionIndexer;
import com.silverpeas.questionReply.model.Question;
import com.silverpeas.questionReply.model.Recipient;
import com.silverpeas.questionReply.model.Reply;
import com.silverpeas.subscribe.SubscriptionServiceFactory;
import com.silverpeas.subscribe.service.ComponentSubscriptionResource;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.core.admin.OrganisationController;

import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.persistence.PersistenceException;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.stratelia.silverpeas.wysiwyg.WysiwygException;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;

@Named("questionManager")
public class SilverpeasQuestionManager implements QuestionManager {

  private final QuestionIndexer questionIndexer = new QuestionIndexer();
  private SilverpeasBeanDAO<Question> questionDao = null;
  private SilverpeasBeanDAO<Reply> replyDao = null;
  private SilverpeasBeanDAO<Recipient> recipientDao = null;
  private final QuestionReplyContentManager contentManager = new QuestionReplyContentManager();
  @Inject
  private OrganisationController controller;

  SilverpeasQuestionManager() {
    try {
      questionDao = SilverpeasBeanDAOFactory.<Question>getDAO(
              "com.silverpeas.questionReply.model.Question");
      replyDao = SilverpeasBeanDAOFactory.<Reply>getDAO("com.silverpeas.questionReply.model.Reply");
      recipientDao = SilverpeasBeanDAOFactory.<Recipient>getDAO(
              "com.silverpeas.questionReply.model.Recipient");
    } catch (PersistenceException ex) {
      SilverTrace.error("questionReply", "SilverpeasQuestionManager()",
              "root.EX_RESOURCE_CLOSE_FAILED", ex);
    }
  }

  QuestionReplyContentManager getContentManager() {
    return contentManager;
  }

  /*
   * enregistre une question et ses destinataires (attention les destinataires n'ont pas de
   * questionId)
   */
  @Override
  public long createQuestion(Question question) throws QuestionReplyException {
    Connection con = null;
    try {
      Collection<Recipient> recipients = question.readRecipients();
      con = DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
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
    } catch (UtilException e) {
      throw new QuestionReplyException("QuestionManager.createQuestion", SilverpeasException.ERROR,
              "questionReply.EX_CREATE_QUESTION_FAILED", "", e);
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.createQuestion", SilverpeasException.ERROR,
              "questionReply.EX_CREATE_QUESTION_FAILED", "", e);
    } catch (ContentManagerException e) {
      throw new QuestionReplyException("QuestionManager.createQuestion", SilverpeasException.ERROR,
              "questionReply.EX_CREATE_QUESTION_FAILED", "", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /*
   * enregistre une réponse à une question => met à jour publicReplyNumber et/ou privateReplyNumber
   * et replyNumber de la question ainsi que le status à 1
   */
  @Override
  public long createReply(Reply reply, Question question) throws QuestionReplyException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
      IdPK pkR = (IdPK) replyDao.add(con, reply);
      WysiwygController.createFileAndAttachment(reply.readCurrentWysiwygContent(), question.
              getInstanceId(), pkR.getId(), I18NHelper.defaultLanguage);
      long idR = pkR.getIdAsLong();
      if (question.hasNewStatus()) {
        question.waitForAnswer();
      }
      updateQuestion(con, question);
      questionIndexer.updateIndex(question, getAllReplies(reply.getQuestionId(), question.
              getInstanceId()));
      notifySubscribers(question, reply);
      return idR;
    } catch (UtilException e) {
      throw new QuestionReplyException("QuestionManager.createReply", SilverpeasException.ERROR,
              "questionReply.EX_CREATE_REPLY_FAILED", "", e);
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.createReply", SilverpeasException.ERROR,
              "questionReply.EX_CREATE_REPLY_FAILED", "", e);
    } catch (WysiwygException e) {
      throw new QuestionReplyException("QuestionManager.createReply", SilverpeasException.ERROR,
              "questionReply.EX_CREATE_REPLY_FAILED", "", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /*
   * enregistre un destinataire
   */
  private void createRecipient(Connection con, Recipient recipient) throws QuestionReplyException {
    try {
      recipientDao.add(con, recipient);
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.createQuestion",
              SilverpeasException.ERROR, "questionReply.EX_CREATE_RECIPIENT_FAILED", "", e);
    }
  }

  /*
   * supprime tous les destinataires d'une question
   */
  private void deleteRecipients(Connection con, long questionId) throws QuestionReplyException {
    try {
      IdPK pk = new IdPK();
      recipientDao.removeWhere(con, pk, " questionId = " + String.valueOf(questionId));
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.deleteRecipients", SilverpeasException.ERROR,
              "questionReply.EX_DELETE_RECIPIENTS_FAILED", "", e);
    }
  }

  /*
   * Clos une liste de questions : updateQuestion
   */
  @Override
  public void closeQuestions(Collection<Long> questionIds) throws QuestionReplyException {
    if (questionIds != null) {
      Connection con = null;
      try {
        con = DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
        for (Long idQ : questionIds) {
          Question question = getQuestion(idQ);
          question.close();
          updateQuestion(con, question);
        }
      } catch (UtilException e) {
        throw new QuestionReplyException("QuestionManager.closeQuestions", SilverpeasException.ERROR,
                "questionReply.EX_CLOSE_QUESTIONS_FAILED", "", e);
      } finally {
        DBUtil.close(con);
      }
    }
  }

  @Override
  public void openQuestions(Collection<Long> questionIds) throws QuestionReplyException {
    if (questionIds != null) {
      Connection con = null;
      try {
        con = DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
        for (Long idQ : questionIds) {
          Question question = getQuestion(idQ);
          question.waitForAnswer();
          updateQuestion(con, question);
        }
      } catch (UtilException e) {
        throw new QuestionReplyException("QuestionManager.openQuestions", SilverpeasException.ERROR,
                "questionReply.EX_OPEN_QUESTIONS_FAILED", "", e);
      } finally {
        DBUtil.close(con);
      }
    }
  }

  /*
   * Modifie les destinataires d'une question : deleteRecipients, createRecipient
   */
  @Override
  public void updateQuestionRecipients(Question question) throws QuestionReplyException {
    Connection con = null;
    try {
      Collection<Recipient> recipients = question.readRecipients();
      con = DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
      deleteRecipients(con, ((IdPK) question.getPK()).getIdAsLong());
      if (recipients != null) {
        for (Recipient recipient : recipients) {
          recipient.setQuestionId(((IdPK) question.getPK()).getIdAsLong());
          createRecipient(con, recipient);
        }
      }
    } catch (UtilException e) {
      throw new QuestionReplyException("QuestionManager.updateQuestionRecipients",
              SilverpeasException.ERROR, "questionReply.EX_UPDATE_RECIPIENTS_FAILED", "", e);
    } catch (QuestionReplyException e) {
      throw new QuestionReplyException("QuestionManager.updateQuestionRecipients",
              SilverpeasException.ERROR, "questionReply.EX_UPDATE_RECIPIENTS_FAILED", "", e);
    } finally {
      DBUtil.close(con);
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
    Connection con = null;

    try {
      con = DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
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
    } catch (UtilException e) {
      throw new QuestionReplyException("QuestionManager.updateQuestionRepliesPublicStatus",
              SilverpeasException.ERROR, "questionReply.EX_UPDATE_REPLYSTATUS_FAILED", "", e);
    } catch (QuestionReplyException e) {
      throw new QuestionReplyException("QuestionManager.updateQuestionRepliesPublicStatus",
              SilverpeasException.ERROR, "questionReply.EX_UPDATE_REPLYSTATUS_FAILED", "", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /*
   * Affecte le status private à 0 de toutes les réponses d'une liste de questions : updateReply
   * Affecte le nombre de réponses privées de la question à 0 : updateQuestion
   */
  @Override
  public void updateQuestionRepliesPrivateStatus(Collection<Long> questionIds)
          throws QuestionReplyException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
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
    } catch (UtilException e) {
      throw new QuestionReplyException("QuestionManager.updateQuestionRepliesPrivateStatus",
              SilverpeasException.ERROR, "questionReply.EX_UPDATE_REPLYSTATUS_FAILED", "", e);
    } catch (QuestionReplyException e) {
      throw new QuestionReplyException("QuestionManager.updateQuestionRepliesPrivateStatus",
              SilverpeasException.ERROR, "questionReply.EX_UPDATE_REPLYSTATUS_FAILED", "", e);
    } finally {
      DBUtil.close(con);
    }
  }

  private void addComponentId(Reply reply, String componentId) {
    WAPrimaryKey pk = reply.getPK();
    pk.setComponentName(componentId);
    reply.setPK(pk);
  }

  /*
   * Affecte le status public à 0 d'une liste de réponses : updateReply Décremente le nombre de
   * réponses publiques de la question d'autant : updateQuestion
   */
  @Override
  public void updateRepliesPublicStatus(Collection<Long> replyIds, Question question)
          throws QuestionReplyException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
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
    } catch (UtilException e) {
      throw new QuestionReplyException("QuestionManager.updateRepliesPublicStatus",
              SilverpeasException.ERROR, "questionReply.EX_UPDATE_REPLYSTATUS_FAILED", "", e);
    } catch (QuestionReplyException e) {
      throw new QuestionReplyException("QuestionManager.updateRepliesPublicStatus",
              SilverpeasException.ERROR, "questionReply.EX_UPDATE_REPLYSTATUS_FAILED", "", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /*
   * Affecte le status private à 0 d'une liste de réponses : updateReply Décremente le nombre de
   * réponses privées de la question d'autant : updateQuestion
   */
  @Override
  public void updateRepliesPrivateStatus(Collection<Long> replyIds, Question question)
          throws QuestionReplyException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
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
    } catch (UtilException e) {
      throw new QuestionReplyException("QuestionManager.updateRepliesPrivateStatus",
              SilverpeasException.ERROR, "questionReply.EX_UPDATE_REPLYSTATUS_FAILED", "", e);
    } catch (QuestionReplyException e) {
      throw new QuestionReplyException("QuestionManager.updateRepliesPrivateStatus",
              SilverpeasException.ERROR, "questionReply.EX_UPDATE_REPLYSTATUS_FAILED", "", e);
    } finally {
      DBUtil.close(con);
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
      throw new QuestionReplyException("QuestionManager.updateQuestion",
              SilverpeasException.ERROR, "questionReply.EX_UPDATE_QUESTION_FAILED", "", e);
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
      throw new QuestionReplyException("QuestionManager.updateQuestion",
              SilverpeasException.ERROR, "questionReply.EX_UPDATE_QUESTION_FAILED", "", e);
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
    } catch (WysiwygException e) {
      throw new QuestionReplyException("QuestionManager.updateReply",
              SilverpeasException.ERROR, "questionReply.EX_UPDATE_REPLY_FAILED", "", e);
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.updateReply",
              SilverpeasException.ERROR, "questionReply.EX_UPDATE_REPLY_FAILED", "", e);
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
        try {
          updateWysiwygContent(reply);
        } catch (WysiwygException e) {
          throw new QuestionReplyException("QuestionManager.updateReply",
                  SilverpeasException.ERROR, "questionReply.EX_UPDATE_REPLY_FAILED", "", e);
        }
      }
      questionIndexer.updateIndex(question, getAllReplies(reply.getQuestionId(), question.
              getInstanceId()));
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.updateReply",
              SilverpeasException.ERROR, "questionReply.EX_UPDATE_REPLY_FAILED", "", e);
    }
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
      throw new QuestionReplyException("QuestionManager.deleteQuestion",
              SilverpeasException.ERROR, "questionReply.EX_DELETE_QUESTION_FAILED", "", e);
    }
  }

  /*
   * supprime une question
   */
  private void deleteQuestion(long questionId) throws QuestionReplyException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
      deleteRecipients(con, questionId);
      IdPK pk = new IdPK();
      pk.setIdAsLong(questionId);
      Question question = getQuestion(questionId);
      String peasId = question.getInstanceId();
      questionDao.remove(con, pk);
      questionIndexer.deleteIndex(question);
      pk.setComponentName(peasId);
      contentManager.deleteSilverContent(con, pk);
    } catch (UtilException e) {
      throw new QuestionReplyException("QuestionManager.deleteQuestion",
              SilverpeasException.ERROR, "questionReply.EX_DELETE_QUESTION_FAILED", "", e);
    } catch (Exception e) {
      throw new QuestionReplyException("QuestionManager.deleteQuestion",
              SilverpeasException.ERROR, "questionReply.EX_DELETE_QUESTION_FAILED", "", e);
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
        con = DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
        deleteRecipients(con, questionId);
        IdPK pk = new IdPK();
        pk.setIdAsLong(questionId);
        Question question = getQuestion(questionId);
        String peasId = question.getInstanceId();
        // rechercher les réponses
        Collection<Reply> replies = getAllReplies(questionId, peasId);
        for (Reply reply : replies) {
          long replyId = Long.parseLong(reply.getPK().getId());
          WAPrimaryKey pkR = reply.getPK();
          pkR.setComponentName(question.getInstanceId());
          reply.setPK(pkR);
          // supprimer la réponse et son index
          deleteReply(replyId);
        }
        questionIndexer.deleteIndex(question);
        // supprimer la question
        questionDao.remove(con, pk);
        pk.setComponentName(peasId);
        contentManager.deleteSilverContent(con, pk);
      } catch (UtilException e) {
        throw new QuestionReplyException("QuestionManager.deleteQuestionAndReplies",
                SilverpeasException.ERROR, "questionReply.EX_DELETE_QUESTION_FAILED", "", e);
      } catch (Exception e) {
        throw new QuestionReplyException(
                "QuestionManager.deleteQuestionAndReplies", SilverpeasException.ERROR,
                "questionReply.EX_DELETE_QUESTION_FAILED", "", e);
      } finally {
        DBUtil.close(con);
      }

    }
  }

  @Override
  public List<Reply> getAllReplies(long questionId, String instanceId) throws QuestionReplyException {
    List<Reply> allReplies = new ArrayList<Reply>();
    try {
      Collection<Reply> privateReplies = getQuestionPrivateReplies(questionId, instanceId);
      allReplies.addAll(privateReplies);
      Collection<Reply> publicReplies = getQuestionPublicReplies(questionId, instanceId);
      allReplies.addAll(publicReplies);
      return allReplies;
    } catch (Exception e) {
      throw new QuestionReplyException("QuestionManager.getAllReplies", SilverpeasException.ERROR,
              "questionReply.EX_DELETE_QUESTION_FAILED", "", e);
    }
  }

  /*
   * supprime une réponse
   */
  private void deleteReply(long replyId) throws QuestionReplyException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
      IdPK pk = new IdPK();
      pk.setIdAsLong(replyId);
      replyDao.remove(con, pk);
    } catch (UtilException e) {
      throw new QuestionReplyException("QuestionManager.deleteReply",
              SilverpeasException.ERROR, "questionReply.EX_DELETE_REPLY_FAILED", "", e);
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.deleteReply",
              SilverpeasException.ERROR, "questionReply.EX_DELETE_REPLY_FAILED", "", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /*
   * supprime une réponse
   */
  private void deleteReply(Connection con, WAPrimaryKey replyId) throws QuestionReplyException {
    try {
      replyDao.remove(con, replyId);
      WysiwygController.deleteFileAndAttachment(replyId.getInstanceId(), replyId.getId());
    } catch (Exception e) {
      throw new QuestionReplyException("QuestionManager.deleteReply", SilverpeasException.ERROR,
              "questionReply.EX_DELETE_REPLY_FAILED", "", e);
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
      throw new QuestionReplyException("QuestionManager.getQuestion",
              SilverpeasException.ERROR, "questionReply.EX_CANT_GET_QUESTION", "", e);
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
      throw new QuestionReplyException("QuestionManager.getQuestion",
              SilverpeasException.ERROR, "questionReply.EX_CANT_GET_QUESTION", "", e);
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
      return new ArrayList<Question>(questionDao.findByWhereClause(pk, where.toString()));
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.getQuestions",
              SilverpeasException.ERROR, "questionReply.EX_CANT_GET_QUESTION", "", e);
    }
  }

  /*
   * recupère la liste des réponses d'une question
   */
  @Override
  public List<Reply> getQuestionReplies(long questionId, String instanceId) throws
          QuestionReplyException {
    try {
      IdPK pk = new IdPK();
      List<Reply> replies = new ArrayList<Reply>(replyDao.findByWhereClause(pk,
              " questionId = " + String.valueOf(questionId)));
      for (Reply reply : replies) {
        reply.getPK().setComponentName(instanceId);
        reply.loadWysiwygContent();
      }
      return replies;
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.getQuestionReplies",
              SilverpeasException.ERROR, "questionReply.EX_CANT_GET_REPLIES", "", e);
    }
  }

  /*
   * recupère la liste des réponses publiques d'une question
   */
  @Override
  public List<Reply> getQuestionPublicReplies(long questionId, String instanceId) throws
          QuestionReplyException {
    try {
      IdPK pk = new IdPK();
      List<Reply> replies = new ArrayList<Reply>(replyDao.findByWhereClause(pk,
              " publicReply = 1 and questionId = " + String.valueOf(questionId)));
      for (Reply reply : replies) {
        reply.getPK().setComponentName(instanceId);
        reply.loadWysiwygContent();
      }
      return replies;
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.getQuestionPublicReplies",
              SilverpeasException.ERROR, "questionReply.EX_CANT_GET_REPLIES", "", e);
    }
  }

  /*
   * recupère la liste des réponses privées d'une question
   */
  @Override
  public List<Reply> getQuestionPrivateReplies(long questionId, String instanceId) throws
          QuestionReplyException {
    try {
      IdPK pk = new IdPK();
      List<Reply> replies = new ArrayList<Reply>(replyDao.findByWhereClause(pk,
              " privateReply = 1 and questionId = " + String.valueOf(questionId)));
      for (Reply reply : replies) {
        reply.getPK().setComponentName(instanceId);
        reply.loadWysiwygContent();
      }
      return replies;
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.getQuestionPrivateReplies",
              SilverpeasException.ERROR, "questionReply.EX_CANT_GET_REPLIES", "", e);
    }
  }

  /*
   * recupère la liste des destinataires d'une question
   */
  @Override
  public List<Recipient> getQuestionRecipients(long questionId) throws QuestionReplyException {
    try {
      IdPK pk = new IdPK();
      return new ArrayList<Recipient>(recipientDao.findByWhereClause(pk, " questionId = " + String.
              valueOf(questionId)));
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.getQuestionRecipients",
              SilverpeasException.ERROR, "questionReply.EX_CANT_GET_RECIPIENTS", "", e);
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
      throw new QuestionReplyException("QuestionManager.getReply",
              SilverpeasException.ERROR, "questionReply.EX_CANT_GET_REPLY", "", e);
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
      return new ArrayList<Question>(questionDao.findByWhereClause(pk,
              " instanceId = '" + instanceId
              + "' and (status <> 2 or privateReplyNumber > 0) and creatorId = " + userId));
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.getSendQuestions",
              SilverpeasException.ERROR, "questionReply.EX_CANT_GET_QUESTIONS", "", e);
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
      return new ArrayList<Question>(questionDao.findByWhereClause(pk,
              " instanceId = '" + instanceId
              + "' and status <> 2 and id IN (select questionId from SC_QuestionReply_Recipient where userId = "
              + userId + ")"));
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.getReceiveQuestions",
              SilverpeasException.ERROR, "questionReply.EX_CANT_GET_QUESTIONS", "", e);
    }
  }

  /*
   * Recupère la liste des questions qui ne sont pas closes ou closes avec réponses publiques
   */
  @Override
  public List<Question> getQuestions(String instanceId) throws QuestionReplyException {
    try {
      IdPK pk = new IdPK();
      return new ArrayList<Question>(questionDao.findByWhereClause(pk,
              " instanceId = '" + instanceId
              + "' and  (status <> 2 or publicReplyNumber > 0) order by creationdate desc, id desc"));
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.getQuestions",
              SilverpeasException.ERROR, "questionReply.EX_CANT_GET_QUESTIONS", "", e);
    }
  }

  /*
   * Recupère la liste de toutes les questions avec toutes ses réponses
   */
  @Override
  public List<Question> getAllQuestions(String instanceId) throws QuestionReplyException {
    List<Question> allQuestions = getQuestions(instanceId);
    List<Question> questions = new ArrayList<Question>(allQuestions.size());
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
  public List<Question> getAllQuestionsByCategory(String instanceId, String categoryId) throws
          QuestionReplyException {
    List<Question> allQuestions = getQuestions(instanceId);
    List<Question> questions = new ArrayList<Question>(allQuestions.size());
    for (Question question : allQuestions) {
      if (!StringUtil.isDefined(question.getCategoryId()) && !StringUtil.isDefined(categoryId)) {
        // la question est sans catégorie
        Question fullQuestion = getQuestionAndReplies(Long.parseLong(question.getPK().getId()));
        questions.add(fullQuestion);
      } else if (StringUtil.isDefined(categoryId) && StringUtil.isDefined(question.getCategoryId())) {
        if (question.getCategoryId().equals(categoryId)) {
          Question fullQuestion = getQuestionAndReplies(Long.parseLong(question.getPK().getId()));
          questions.add(fullQuestion);
        }
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
      return new ArrayList<Question>(questionDao.findByWhereClause(pk,
              " instanceId = '" + instanceId + "' AND publicReplyNumber > 0 ORDER BY id"));
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.getPublicQuestions",
              SilverpeasException.ERROR, "questionReply.EX_CANT_GET_QUESTIONS", "", e);
    }
  }

  /*
   * enregistre une question et une réponse
   */
  @Override
  public long createQuestionReply(Question question, Reply reply) throws QuestionReplyException {
    Connection con = null;
    long idQ = -1;
    try {
      con = DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
      IdPK pkQ = (IdPK) questionDao.add(con, question);
      idQ = pkQ.getIdAsLong();
      reply.setQuestionId(idQ);
      WAPrimaryKey pkR = replyDao.add(con, reply);
      WysiwygController.createFileAndAttachment(reply.readCurrentWysiwygContent(), question.
              getInstanceId(), pkR.getId(), I18NHelper.defaultLanguage);
      questionIndexer.createIndex(question, Collections.singletonList(reply));
      Question updatedQuestion = getQuestion(idQ);
      contentManager.createSilverContent(con, updatedQuestion);
      notifySubscribers(question, reply);
    } catch (UtilException e) {
      throw new QuestionReplyException("QuestionManager.createQuestion",
              SilverpeasException.ERROR, "questionReply.EX_CREATE_QUESTION_FAILED", "", e);
    } catch (Exception e) {
      throw new QuestionReplyException("QuestionManager.createQuestion",
              SilverpeasException.ERROR, "questionReply.EX_CREATE_QUESTION_FAILED", "", e);
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
      Collection<Reply> replies = replyDao.findByWhereClause(pk, " questionId = " + String.valueOf(
              questionId));
      return replies.size();
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.getQuestionReplies",
              SilverpeasException.ERROR, "questionReply.EX_CANT_GET_REPLIES", "", e);
    }
  }

  /*
   * recupère le nombre de réponses publiques d'une question
   */
  private int getQuestionPublicRepliesNumber(long questionId) throws QuestionReplyException {
    try {
      IdPK pk = new IdPK();
      Collection<Reply> replies = replyDao.findByWhereClause(pk,
              " publicReply = 1 and questionId = " + String.valueOf(questionId));
      return replies.size();
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.getQuestionPublicReplies",
              SilverpeasException.ERROR, "questionReply.EX_CANT_GET_REPLIES", "", e);
    }
  }

  /*
   * recupère le nombre de réponses privées d'une question
   */
  private int getQuestionPrivateRepliesNumber(long questionId) throws QuestionReplyException {
    try {
      IdPK pk = new IdPK();
      Collection<Reply> replies = replyDao.findByWhereClause(pk,
              " privateReply = 1 and questionId = " + String.valueOf(questionId));
      return replies.size();
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.getQuestionPrivateReplies",
              SilverpeasException.ERROR, "questionReply.EX_CANT_GET_REPLIES", "", e);
    }
  }

  protected void updateWysiwygContent(Reply reply) throws WysiwygException {
    if (WysiwygController.haveGotWysiwyg(reply.getPK().getInstanceId(), reply.getPK().getId(),
        I18NHelper.defaultLanguage)) {
      WysiwygController.updateFileAndAttachment(reply.readCurrentWysiwygContent(),
          reply.getPK().getInstanceId(), reply.getPK().getId(), reply.getCreatorId(),
          I18NHelper.defaultLanguage);
    } else {
      WysiwygController.createFileAndAttachment(reply.readCurrentWysiwygContent(),
          reply.getPK().getInstanceId(), reply.getPK().getId(), I18NHelper.defaultLanguage);
    }
  }

  protected boolean isSortable(String instanceId) {
    return StringUtil.getBooleanValue(controller.getComponentParameterValue(instanceId, "sortable"));
  }

  void notifySubscribers(Question question, Reply reply) throws
          QuestionReplyException {
    if (reply.getPublicReply() == 1) {
      UserDetail sender = reply.readAuthor(controller);
      SubscriptionNotifier notifier = new SubscriptionNotifier(sender, URLManager.getServerURL(null),
              question, reply);
      Collection<String> subscribers =
          SubscriptionServiceFactory.getFactory().getSubscribeService().
              getUserSubscribers(ComponentSubscriptionResource.from(question.getInstanceId()));
      Set<UserRecipient> userRecipients = new HashSet<UserRecipient>();
      for (String subscriberId : subscribers) {
        userRecipients.add(new UserRecipient(subscriberId));
      }
      notifier.sendNotification(userRecipients);
    }
  }
}
