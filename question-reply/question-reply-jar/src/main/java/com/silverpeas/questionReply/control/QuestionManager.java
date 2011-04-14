/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.questionReply.QuestionReplyException;
import com.silverpeas.questionReply.model.Question;
import com.silverpeas.questionReply.model.Recipient;
import com.silverpeas.questionReply.model.Reply;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.persistence.PersistenceException;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;

public class QuestionManager {

  private static QuestionManager instance;
  private SilverpeasBeanDAO questionDao = null;
  private SilverpeasBeanDAO replyDao = null;
  private SilverpeasBeanDAO recipientDao = null;
  private QuestionReplyContentManager contentManager = null;
  private OrganizationController controller = new OrganizationController();

  private QuestionManager() {
  }

  private QuestionReplyContentManager getQuestionReplyContentManager() {
    if (contentManager == null) {
      contentManager = new QuestionReplyContentManager();
    }
    return contentManager;
  }

  static public QuestionManager getInstance() {
    synchronized (QuestionManager.class) {
      if (instance == null) {
        instance = new QuestionManager();
      }
    }
    return instance;
  }

  private SilverpeasBeanDAO getQdao() throws PersistenceException {
    if (questionDao == null) {
      questionDao = SilverpeasBeanDAOFactory.getDAO("com.silverpeas.questionReply.model.Question");
    }
    return questionDao;
  }

  private SilverpeasBeanDAO getRdao() throws PersistenceException {
    if (replyDao == null) {
      replyDao = SilverpeasBeanDAOFactory.getDAO("com.silverpeas.questionReply.model.Reply");
    }
    return replyDao;
  }

  private SilverpeasBeanDAO getUdao() throws PersistenceException {
    if (recipientDao == null) {
      recipientDao =
          SilverpeasBeanDAOFactory.getDAO("com.silverpeas.questionReply.model.Recipient");
    }
    return recipientDao;
  }

  /*
   * enregistre une question et ses destinataires (attention les destinataires n'ont pas de
   * questionId)
   */
  public long createQuestion(Question question) throws QuestionReplyException {
    long idQ = -1;

    boolean succeed = false;
    Connection con = null;

    try {
      Collection<Recipient> recipients = question.readRecipients();
      SilverpeasBeanDAO daoQ = getQdao();
      con = DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);

      IdPK pkQ = (IdPK) daoQ.add(con, question);
      createQuestionIndex(question);
      idQ = pkQ.getIdAsLong();
      if (recipients != null) {
        Iterator<Recipient> it = recipients.iterator();
        while (it.hasNext()) {
          Recipient recipient = it.next();
          recipient.setQuestionId(idQ);
          createRecipient(con, recipient);
        }
      }
      question.setPK(pkQ);
      getQuestionReplyContentManager().createSilverContent(con, question);
      succeed = true;
    } catch (UtilException e) {
      throw new QuestionReplyException("QuestionManager.createQuestion",
          SilverpeasException.ERROR, "questionReply.EX_CREATE_QUESTION_FAILED",
          "", e);
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.createQuestion",
          SilverpeasException.ERROR, "questionReply.EX_CREATE_QUESTION_FAILED",
          "", e);
    } catch (ContentManagerException e) {
      throw new QuestionReplyException("QuestionManager.createQuestion",
          SilverpeasException.ERROR, "questionReply.EX_CREATE_QUESTION_FAILED",
          "", e);
    } finally {
      closeConnection(con, succeed);
    }
    return idQ;
  }

  public void createQuestionIndex(Question question) {
    SilverTrace.info("questionReply", "QuestionManager.createQuestionIndex()",
        "root.MSG_GEN_ENTER_METHOD", "Question = " + question.getTitle());
    FullIndexEntry indexEntry = null;

    if (question != null) {
      // Indexer la Question
      indexEntry = new FullIndexEntry(question.getInstanceId(), "Question",
          question.getPK().getId());
      indexEntry.setTitle(question.getTitle());
      indexEntry.setPreView(question.getContent());
      indexEntry.setCreationDate(question.getCreationDate());
      indexEntry.setCreationUser(question.getCreatorId());
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  public void deleteQuestionIndex(Question question) {
    SilverTrace.info("questionReply", "QuestionManager.deleteQuestionIndex()",
        "root.MSG_GEN_ENTER_METHOD", "Question = " + question.toString());
    IndexEntryPK indexEntry = new IndexEntryPK(question.getInstanceId(),
        "Question", question.getPK().getId());

    IndexEngineProxy.removeIndexEntry(indexEntry);

    // supprimer les index des réponses...
  }

  /*
   * enregistre une réponse à une question => met à jour publicReplyNumber et/ou privateReplyNumber
   * et replyNumber de la question ainsi que le status à 1
   */
  public long createReply(Reply reply, Question question)
      throws QuestionReplyException {
    long idR = -1;
    boolean succeed = false;
    Connection con = null;
    try {
      SilverpeasBeanDAO daoR = getRdao();
      con = DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
      IdPK pkR = (IdPK) daoR.add(con, reply);
      createReplyIndex(reply);
      idR = pkR.getIdAsLong();
      if (question.getStatus() == 0) {
        question.setStatus(1);
      }
      updateQuestion(con, question);
      succeed = true;
    } catch (UtilException e) {
      throw new QuestionReplyException("QuestionManager.createReply",
          SilverpeasException.ERROR, "questionReply.EX_CREATE_REPLY_FAILED",
          "", e);
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.createReply",
          SilverpeasException.ERROR, "questionReply.EX_CREATE_REPLY_FAILED",
          "", e);
    } finally {
      closeConnection(con, succeed);
    }
    return idR;
  }

  public void createReplyIndex(Reply reply) {
    SilverTrace.info("questionReply", "QuestionManager.createReplyIndex()",
        "root.MSG_GEN_ENTER_METHOD", "Reply = " + reply.getTitle());
    FullIndexEntry indexEntry = null;

    if (reply != null) {
      // Indexer la Réponse
      indexEntry = new FullIndexEntry(reply.getPK().getInstanceId(), "Reply",
          reply.getPK().getId());
      indexEntry.setTitle(reply.getTitle());
      indexEntry.setPreView(reply.getContent());
      indexEntry.setCreationDate(reply.getCreationDate());
      indexEntry.setCreationUser(reply.getCreatorId());
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  public void deleteReplyIndex(Reply reply) {
    SilverTrace.info("questionReply", "QuestionManager.deleteReplyIndex()",
        "root.MSG_GEN_ENTER_METHOD", "Reply = " + reply.toString());

    IndexEntryPK indexEntry = new IndexEntryPK(reply.getPK().getInstanceId(),
        "Reply", reply.getPK().getId());

    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  /*
   * enregistre un destinataire
   */
  private void createRecipient(Connection con, Recipient recipient)
      throws QuestionReplyException {
    try {
      SilverpeasBeanDAO daoU = getUdao();
      daoU.add(con, recipient);
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.createQuestion",
          SilverpeasException.ERROR,
          "questionReply.EX_CREATE_RECIPIENT_FAILED", "", e);
    }
  }

  /*
   * supprime tous les destinataires d'une question
   */
  private void deleteRecipients(Connection con, long questionId)
      throws QuestionReplyException {
    try {
      SilverpeasBeanDAO daoU = getUdao();
      IdPK pk = new IdPK();
      daoU.removeWhere(con, pk, " questionId = "
          + new Long(questionId).toString());
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.deleteRecipients",
          SilverpeasException.ERROR,
          "questionReply.EX_DELETE_RECIPIENTS_FAILED", "", e);
    }
  }

  /*
   * Clos une liste de questions : updateQuestion
   */
  public void closeQuestions(Collection<Long> questionIds)
      throws QuestionReplyException {
    if (questionIds != null) {
      boolean succeed = false;
      Connection con = null;
      try {
        con = DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
        Iterator<Long> it = questionIds.iterator();
        while (it.hasNext()) {
          long idQ = it.next();
          Question question = getQuestion(idQ);
          question.setStatus(2);
          updateQuestion(con, question);
        }
        succeed = true;
      } catch (UtilException e) {
        throw new QuestionReplyException("QuestionManager.closeQuestions",
            SilverpeasException.ERROR,
            "questionReply.EX_CLOSE_QUESTIONS_FAILED", "", e);
      } finally {
        closeConnection(con, succeed);
      }
    }
  }

  public void openQuestions(Collection<Long> questionIds)
      throws QuestionReplyException {
    if (questionIds != null) {
      boolean succeed = false;
      Connection con = null;
      try {
        con = DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
        Iterator<Long> it = questionIds.iterator();
        while (it.hasNext()) {
          long idQ = it.next();
          Question question = getQuestion(idQ);
          question.setStatus(1);
          updateQuestion(con, question);
        }
        succeed = true;
      } catch (UtilException e) {
        throw new QuestionReplyException("QuestionManager.openQuestions",
            SilverpeasException.ERROR,
            "questionReply.EX_OPEN_QUESTIONS_FAILED", "", e);
      } finally {
        closeConnection(con, succeed);
      }
    }
  }

  /*
   * Modifie les destinataires d'une question : deleteRecipients, createRecipient
   */
  public void updateQuestionRecipients(Question question)
      throws QuestionReplyException {
    boolean succeed = false;
    Connection con = null;
    try {
      Collection<Recipient> recipients = question.readRecipients();
      con = DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
      deleteRecipients(con, ((IdPK) question.getPK()).getIdAsLong());
      if (recipients != null) {
        Iterator<Recipient> it = recipients.iterator();
        while (it.hasNext()) {
          Recipient recipient = it.next();
          recipient.setQuestionId(((IdPK) question.getPK()).getIdAsLong());
          createRecipient(con, recipient);
        }
      }
      succeed = true;
    } catch (UtilException e) {
      throw new QuestionReplyException(
          "QuestionManager.updateQuestionRecipients",
          SilverpeasException.ERROR,
          "questionReply.EX_UPDATE_RECIPIENTS_FAILED", "", e);
    } catch (QuestionReplyException e) {
      throw new QuestionReplyException(
          "QuestionManager.updateQuestionRecipients",
          SilverpeasException.ERROR,
          "questionReply.EX_UPDATE_RECIPIENTS_FAILED", "", e);
    } finally {
      closeConnection(con, succeed);
    }
  }

  /*
   * Affecte le status public à 0 de toutes les réponses d'une liste de questions : updateReply
   * Affecte le nombre de réponses publiques de la question à 0 : updateQuestion si question en
   * attente, on a demandé à la supprimer : deleteQuestion
   */
  public void updateQuestionRepliesPublicStatus(Collection<Long> questionIds)
      throws QuestionReplyException {
    boolean succeed = false;
    Connection con = null;

    try {
      con = DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
      if (questionIds != null) {
        Iterator<Long> itQ = questionIds.iterator();
        while (itQ.hasNext()) {
          long idQ = itQ.next();
          Question question = getQuestion(idQ);
          Collection<Reply> replies = getQuestionPublicReplies(idQ);
          if (replies != null) {
            Iterator<Reply> itR = replies.iterator();
            while (itR.hasNext()) {
              Reply reply = itR.next();
              reply.setPublicReply(0);
              addComponentId(reply, question.getPK().getInstanceId());
              updateReply(con, reply);
            }
            updateQuestion(con, question);
          }

          if (question.getStatus() == 0) {
            deleteQuestion(con, idQ);
          }
        }
      }
      succeed = true;
    } catch (UtilException e) {
      throw new QuestionReplyException(
          "QuestionManager.updateQuestionRepliesPublicStatus",
          SilverpeasException.ERROR,
          "questionReply.EX_UPDATE_REPLYSTATUS_FAILED", "", e);
    } catch (QuestionReplyException e) {
      throw new QuestionReplyException(
          "QuestionManager.updateQuestionRepliesPublicStatus",
          SilverpeasException.ERROR,
          "questionReply.EX_UPDATE_REPLYSTATUS_FAILED", "", e);
    } finally {
      closeConnection(con, succeed);
    }
  }

  /*
   * Affecte le status private à 0 de toutes les réponses d'une liste de questions : updateReply
   * Affecte le nombre de réponses privées de la question à 0 : updateQuestion
   */
  public void updateQuestionRepliesPrivateStatus(Collection<Long> questionIds)
      throws QuestionReplyException {
    boolean succeed = false;
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
      if (questionIds != null) {
        for (Long idQ : questionIds) {
          Question question = getQuestion(idQ);
          Collection<Reply> replies = getQuestionPrivateReplies(idQ);
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
      succeed = true;
    } catch (UtilException e) {
      throw new QuestionReplyException(
          "QuestionManager.updateQuestionRepliesPrivateStatus",
          SilverpeasException.ERROR,
          "questionReply.EX_UPDATE_REPLYSTATUS_FAILED", "", e);
    } catch (QuestionReplyException e) {
      throw new QuestionReplyException(
          "QuestionManager.updateQuestionRepliesPrivateStatus",
          SilverpeasException.ERROR,
          "questionReply.EX_UPDATE_REPLYSTATUS_FAILED", "", e);
    } finally {
      closeConnection(con, succeed);
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
  public void updateRepliesPublicStatus(Collection<Long> replyIds, Question question)
      throws QuestionReplyException {
    boolean succeed = false;
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
      succeed = true;
    } catch (UtilException e) {
      throw new QuestionReplyException(
          "QuestionManager.updateRepliesPublicStatus",
          SilverpeasException.ERROR,
          "questionReply.EX_UPDATE_REPLYSTATUS_FAILED", "", e);
    } catch (QuestionReplyException e) {
      throw new QuestionReplyException(
          "QuestionManager.updateRepliesPublicStatus",
          SilverpeasException.ERROR,
          "questionReply.EX_UPDATE_REPLYSTATUS_FAILED", "", e);
    } finally {
      closeConnection(con, succeed);
    }
  }

  /*
   * Affecte le status private à 0 d'une liste de réponses : updateReply Décremente le nombre de
   * réponses privées de la question d'autant : updateQuestion
   */
  public void updateRepliesPrivateStatus(Collection<Long> replyIds, Question question)
      throws QuestionReplyException {
    boolean succeed = false;
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
      succeed = true;
    } catch (UtilException e) {
      throw new QuestionReplyException(
          "QuestionManager.updateRepliesPrivateStatus",
          SilverpeasException.ERROR,
          "questionReply.EX_UPDATE_REPLYSTATUS_FAILED", "", e);
    } catch (QuestionReplyException e) {
      throw new QuestionReplyException(
          "QuestionManager.updateRepliesPrivateStatus",
          SilverpeasException.ERROR,
          "questionReply.EX_UPDATE_REPLYSTATUS_FAILED", "", e);
    } finally {
      closeConnection(con, succeed);
    }
  }

  /*
   * Modifie une question => la question est supprimée si publicReplyNumber et privateReplyNumber
   * sont à 0 et que la question est close => met à jour publicReplyNumber et/ou privateReplyNumber
   * et replyNumber de la question
   */
  private void updateQuestion(Connection con, Question question)
      throws QuestionReplyException {
    try {
      long idQ = ((IdPK) question.getPK()).getIdAsLong();
      question.setReplyNumber(getQuestionRepliesNumber(idQ));
      question.setPublicReplyNumber(getQuestionPublicRepliesNumber(idQ));
      question.setPrivateReplyNumber(getQuestionPrivateRepliesNumber(idQ));

      if ((question.getReplyNumber() == 0) && (question.getStatus() == 2)) {
        deleteQuestion(con, idQ);
      } else {
        SilverpeasBeanDAO daoQ = getQdao();
        daoQ.update(con, question);
        createQuestionIndex(question);

        question.getPK().setComponentName(question.getInstanceId());
        getQuestionReplyContentManager().updateSilverContentVisibility(question);
      }
    } catch (Exception e) {
      throw new QuestionReplyException("QuestionManager.updateQuestion",
          SilverpeasException.ERROR, "questionReply.EX_UPDATE_QUESTION_FAILED",
          "", e);
    }
  }

  /*
   * Modifie une question => la question est supprimée si publicReplyNumber et privateReplyNumber
   * sont à 0 et que la question est close => met à jour publicReplyNumber et/ou privateReplyNumber
   * et replyNumber de la question
   */
  public void updateQuestion(Question question) throws QuestionReplyException {
    try {
      long idQ = ((IdPK) question.getPK()).getIdAsLong();
      question.setReplyNumber(getQuestionRepliesNumber(idQ));
      question.setPublicReplyNumber(getQuestionPublicRepliesNumber(idQ));
      question.setPrivateReplyNumber(getQuestionPrivateRepliesNumber(idQ));

      if ((question.getReplyNumber() == 0) && (question.getStatus() == 2)) {
        deleteQuestion(idQ);
      } else {
        SilverpeasBeanDAO daoQ = getQdao();
        daoQ.update(question);
        createQuestionIndex(question);
        question.getPK().setComponentName(question.getInstanceId());
        getQuestionReplyContentManager().updateSilverContentVisibility(question);
      }
    } catch (Exception e) {
      throw new QuestionReplyException("QuestionManager.updateQuestion",
          SilverpeasException.ERROR, "questionReply.EX_UPDATE_QUESTION_FAILED",
          "", e);
    }
  }

  /*
   * Modifie une réponse => La réponse est supprimée si le status public et le status private sont à
   * 0
   */
  private void updateReply(Connection con, Reply reply)
      throws QuestionReplyException {
    try {
      if ((reply.getPublicReply() == 0) && (reply.getPrivateReply() == 0)) {
        deleteReply(con, ((IdPK) reply.getPK()).getIdAsLong());
        deleteReplyIndex(reply);
      } else {
        SilverpeasBeanDAO daoR = getRdao();
        daoR.update(con, reply);
        createReplyIndex(reply);
      }
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.updateReply",
          SilverpeasException.ERROR, "questionReply.EX_UPDATE_REPLY_FAILED",
          "", e);
    }
  }

  /*
   * Modifie une réponse => La réponse est supprimée si le status public et le status private sont à
   * 0
   */
  public void updateReply(Reply reply) throws QuestionReplyException {
    try {
      if ((reply.getPublicReply() == 0) && (reply.getPrivateReply() == 0)) {
        deleteReply(((IdPK) reply.getPK()).getIdAsLong());
        deleteReplyIndex(reply);
      } else {
        SilverpeasBeanDAO daoR = getRdao();
        daoR.update(reply);
        createReplyIndex(reply);
      }
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.updateReply",
          SilverpeasException.ERROR, "questionReply.EX_UPDATE_REPLY_FAILED",
          "", e);
    }
  }

  /*
   * supprime une question
   */
  private void deleteQuestion(Connection con, long questionId)
      throws QuestionReplyException {
    try {
      deleteRecipients(con, questionId);
      SilverpeasBeanDAO daoQ = getQdao();
      IdPK pk = new IdPK();
      pk.setIdAsLong(questionId);
      Question question = getQuestion(questionId);
      String peasId = question.getInstanceId();

      daoQ.remove(con, pk);
      deleteQuestionIndex(question);

      pk.setComponentName(peasId);
      getQuestionReplyContentManager().deleteSilverContent(con, pk);
    } catch (Exception e) {
      throw new QuestionReplyException("QuestionManager.deleteQuestion",
          SilverpeasException.ERROR, "questionReply.EX_DELETE_QUESTION_FAILED",
          "", e);
    }
  }

  /*
   * supprime une question
   */
  private void deleteQuestion(long questionId) throws QuestionReplyException {
    boolean succeed = false;
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
      deleteRecipients(con, questionId);
      SilverpeasBeanDAO daoQ = getQdao();
      IdPK pk = new IdPK();
      pk.setIdAsLong(questionId);
      Question question = getQuestion(questionId);
      String peasId = question.getInstanceId();

      daoQ.remove(con, pk);
      deleteQuestionIndex(question);

      pk.setComponentName(peasId);
      getQuestionReplyContentManager().deleteSilverContent(con, pk);

      succeed = true;
    } catch (UtilException e) {
      throw new QuestionReplyException("QuestionManager.deleteQuestion",
          SilverpeasException.ERROR, "questionReply.EX_DELETE_QUESTION_FAILED",
          "", e);
    } catch (Exception e) {
      throw new QuestionReplyException("QuestionManager.deleteQuestion",
          SilverpeasException.ERROR, "questionReply.EX_DELETE_QUESTION_FAILED",
          "", e);
    } finally {
      closeConnection(con, succeed);
    }
  }

  public void deleteQuestionAndReplies(Collection<Long> questionIds)
      throws QuestionReplyException {
    // pour chaque question
    for (Long questionId : questionIds) {
      boolean succeed = false;
      Connection con = null;
      try {
        con = DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
        long qId = questionId.longValue();
        deleteRecipients(con, qId);
        SilverpeasBeanDAO daoQ = getQdao();
        IdPK pk = new IdPK();
        pk.setIdAsLong(qId);
        Question question = getQuestion(qId);
        String peasId = question.getInstanceId();

        // rechercher les réponses
        Collection<Reply> replies = getAllReplies(qId);
        for (Reply reply : replies) {
          long replyId = Long.parseLong(reply.getPK().getId());
          WAPrimaryKey pkR = reply.getPK();
          pkR.setComponentName(question.getInstanceId());
          reply.setPK(pkR);
          // supprimer la réponse et son index
          deleteReply(replyId);
          deleteReplyIndex(reply);
        }

        // supprimer la question
        daoQ.remove(con, pk);
        deleteQuestionIndex(question);

        pk.setComponentName(peasId);
        getQuestionReplyContentManager().deleteSilverContent(con, pk);

        succeed = true;
      } catch (UtilException e) {
        throw new QuestionReplyException(
            "QuestionManager.deleteQuestionAndReplies",
            SilverpeasException.ERROR,
            "questionReply.EX_DELETE_QUESTION_FAILED", "", e);
      } catch (Exception e) {
        throw new QuestionReplyException(
            "QuestionManager.deleteQuestionAndReplies",
            SilverpeasException.ERROR,
            "questionReply.EX_DELETE_QUESTION_FAILED", "", e);
      } finally {
        closeConnection(con, succeed);
      }

    }
  }

  private Collection<Reply> getAllReplies(long questionId)
      throws QuestionReplyException {
    Collection<Reply> allReplies = new ArrayList<Reply>();
    try {
      Collection<Reply> privateReplies = getQuestionPrivateReplies(questionId);
      allReplies.addAll(privateReplies);
      Collection<Reply> publicReplies = getQuestionPublicReplies(questionId);
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
    boolean succeed = false;
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
      SilverpeasBeanDAO daoR = getRdao();
      IdPK pk = new IdPK();
      pk.setIdAsLong(replyId);
      daoR.remove(con, pk);
      succeed = true;
    } catch (UtilException e) {
      throw new QuestionReplyException("QuestionManager.deleteReply",
          SilverpeasException.ERROR, "questionReply.EX_DELETE_REPLY_FAILED",
          "", e);
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.deleteReply",
          SilverpeasException.ERROR, "questionReply.EX_DELETE_REPLY_FAILED",
          "", e);
    } finally {
      closeConnection(con, succeed);
    }
  }

  /*
   * supprime une réponse
   */
  private void deleteReply(Connection con, long replyId)
      throws QuestionReplyException {
    try {
      SilverpeasBeanDAO daoR = getRdao();
      IdPK pk = new IdPK();
      pk.setIdAsLong(replyId);
      daoR.remove(con, pk);
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.deleteReply",
          SilverpeasException.ERROR, "questionReply.EX_DELETE_REPLY_FAILED",
          "", e);
    }
  }

  /*
   * recupère une question
   */
  public Question getQuestion(long questionId) throws QuestionReplyException {
    Question question = null;
    try {
      SilverpeasBeanDAO daoQ = getQdao();
      IdPK pk = new IdPK();
      pk.setIdAsLong(questionId);
      question = (Question) daoQ.findByPrimaryKey(pk);
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.getQuestion",
          SilverpeasException.ERROR, "questionReply.EX_CANT_GET_QUESTION", "",
          e);
    }
    return question;
  }

  public Question getQuestionAndReplies(long questionId)
      throws QuestionReplyException {
    Question question = null;
    try {
      SilverpeasBeanDAO daoQ = getQdao();
      IdPK pk = new IdPK();
      pk.setIdAsLong(questionId);
      question = (Question) daoQ.findByPrimaryKey(pk);
      Collection<Reply> replies = getQuestionReplies(questionId);
      question.writeReplies(replies);
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.getQuestion",
          SilverpeasException.ERROR, "questionReply.EX_CANT_GET_QUESTION", "",
          e);
    }
    return question;
  }

  public Collection<Question> getQuestionsByIds(ArrayList ids)
      throws QuestionReplyException {
    StringBuffer where = new StringBuffer();
    int sizeOfIds = ids.size();
    for (int i = 0; i < sizeOfIds - 1; i++) {
      where.append(" id = " + (String) ids.get(i) + " or ");
    }
    if (sizeOfIds != 0) {
      where.append(" id = " + (String) ids.get(sizeOfIds - 1));
    }

    Collection<Question> questions = new ArrayList<Question>();
    try {
      SilverpeasBeanDAO daoQ = getQdao();
      IdPK pk = new IdPK();
      questions = daoQ.findByWhereClause(pk, where.toString());
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.getQuestions",
          SilverpeasException.ERROR, "questionReply.EX_CANT_GET_QUESTION", "",
          e);
    }
    return questions;
  }

  /*
   * recupère la liste des réponses d'une question
   */
  public Collection<Reply> getQuestionReplies(long questionId)
      throws QuestionReplyException {
    Collection<Reply> replies = new ArrayList<Reply>();
    try {
      SilverpeasBeanDAO daoR = getRdao();
      IdPK pk = new IdPK();
      replies = daoR.findByWhereClause(pk, " questionId = "
          + new Long(questionId).toString());
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.getQuestionReplies",
          SilverpeasException.ERROR, "questionReply.EX_CANT_GET_REPLIES", "", e);
    }
    return replies;
  }

  /*
   * recupère la liste des réponses publiques d'une question
   */
  public Collection<Reply> getQuestionPublicReplies(long questionId)
      throws QuestionReplyException {
    Collection<Reply> replies = new ArrayList<Reply>();
    try {
      SilverpeasBeanDAO daoR = getRdao();
      IdPK pk = new IdPK();
      replies = (Collection<Reply>) daoR.findByWhereClause(pk, " publicReply = 1 and questionId = "
          + new Long(questionId).toString());
    } catch (PersistenceException e) {
      throw new QuestionReplyException(
          "QuestionManager.getQuestionPublicReplies",
          SilverpeasException.ERROR, "questionReply.EX_CANT_GET_REPLIES", "", e);
    }
    return replies;
  }

  /*
   * recupère la liste des réponses privées d'une question
   */
  public Collection<Reply> getQuestionPrivateReplies(long questionId)
      throws QuestionReplyException {
    Collection<Reply> replies = new ArrayList<Reply>();
    try {
      SilverpeasBeanDAO daoR = getRdao();
      IdPK pk = new IdPK();
      replies = daoR.findByWhereClause(pk,
          " privateReply = 1 and questionId = "
              + new Long(questionId).toString());
    } catch (PersistenceException e) {
      throw new QuestionReplyException(
          "QuestionManager.getQuestionPrivateReplies",
          SilverpeasException.ERROR, "questionReply.EX_CANT_GET_REPLIES", "", e);
    }
    return replies;
  }

  /*
   * recupère la liste des destinataires d'une question
   */
  public Collection<Recipient> getQuestionRecipients(long questionId)
      throws QuestionReplyException {
    Collection<Recipient> recipients = new ArrayList<Recipient>();
    try {
      SilverpeasBeanDAO daoU = getUdao();
      IdPK pk = new IdPK();
      recipients = daoU.findByWhereClause(pk, " questionId = "
          + new Long(questionId).toString());
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.getQuestionRecipients",
          SilverpeasException.ERROR, "questionReply.EX_CANT_GET_RECIPIENTS",
          "", e);
    }
    return recipients;
  }

  /*
   * recupère une réponse
   */
  public Reply getReply(long replyId) throws QuestionReplyException {
    Reply reply = null;
    try {
      SilverpeasBeanDAO daoR = getRdao();
      IdPK pk = new IdPK();
      pk.setIdAsLong(replyId);
      reply = (Reply) daoR.findByPrimaryKey(pk);
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.getReply",
          SilverpeasException.ERROR, "questionReply.EX_CANT_GET_REPLY", "", e);
    }
    return reply;
  }

  /*
   * Recupère la liste des questions emises par un utilisateur => Q dont il est l'auteur qui ne sont
   * pas closes ou closes avec réponses privées
   */
  public Collection<Question> getSendQuestions(String userId, String instanceId)
      throws QuestionReplyException {
    Collection<Question> questions = new ArrayList<Question>();
    try {
      SilverpeasBeanDAO daoQ = getQdao();
      IdPK pk = new IdPK();
      questions = daoQ.findByWhereClause(pk, " instanceId = '" + instanceId
          + "' and (status <> 2 or privateReplyNumber > 0) and creatorId = "
          + userId);
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.getSendQuestions",
          SilverpeasException.ERROR, "questionReply.EX_CANT_GET_QUESTIONS", "",
          e);
    }
    return questions;
  }

  /*
   * Recupère la liste des questions recues par un utilisateur => Q dont il est le destinataire et
   * qui ne sont pas closes
   */
  public Collection<Question> getReceiveQuestions(String userId, String instanceId)
      throws QuestionReplyException {
    Collection<Question> questions = new ArrayList<Question>();
    try {
      SilverpeasBeanDAO daoQ = getQdao();
      IdPK pk = new IdPK();
      questions =
          daoQ.findByWhereClause(
              pk,
              " instanceId = '"
                  + instanceId
                  +
                  "' and status <> 2 and id IN (select questionId from SC_QuestionReply_Recipient where userId = "
                  + userId + ")");
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.getReceiveQuestions",
          SilverpeasException.ERROR, "questionReply.EX_CANT_GET_QUESTIONS", "",
          e);
    }
    return questions;
  }

  /*
   * Recupère la liste des questions qui ne sont pas closes ou closes avec réponses publiques
   */
  public Collection<Question> getQuestions(String instanceId)
      throws QuestionReplyException {
    Collection<Question> questions = new ArrayList<Question>();
    try {
      SilverpeasBeanDAO daoQ = getQdao();
      IdPK pk = new IdPK();
      questions = daoQ.findByWhereClause(pk, " instanceId = '" + instanceId
          + "' and  (status <> 2 or publicReplyNumber > 0) order by creationdate desc, id desc");
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.getQuestions",
          SilverpeasException.ERROR, "questionReply.EX_CANT_GET_QUESTIONS", "", e);
    }
    return questions;
  }

  /*
   * Recupère la liste de toutes les questions avec toutes ses réponses
   */
  public Collection<Question> getAllQuestions(String instanceId)
      throws QuestionReplyException {
    List<Question> questions = new ArrayList<Question>();
    Collection<Question> allQuestions = getQuestions(instanceId);
    for (Question question : allQuestions) {
      question = getQuestionAndReplies(Long.parseLong(question.getPK().getId()));
      questions.add(question);
    }
    if (isSortable(instanceId)) {
      Collections.sort(questions, QuestionRegexpComparator.getInstance());
    }
    return questions;
  }

  public Collection<Question> getAllQuestionsByCategory(String instanceId,
      String categoryId) throws QuestionReplyException {
    List<Question> questions = new ArrayList<Question>();
    Collection<Question> allQuestions = getQuestions(instanceId);
    for (Question question : allQuestions) {
      if (!StringUtil.isDefined(question.getCategoryId()) && categoryId == null) {
        // la question est sans catégorie
        question = getQuestionAndReplies(Long.parseLong(question.getPK().getId()));
        questions.add(question);
      } else if (categoryId != null && StringUtil.isDefined(question.getCategoryId())) {
        if (question.getCategoryId().equals(categoryId)) {
          question = getQuestionAndReplies(Long.parseLong(question.getPK().getId()));
          questions.add(question);
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
  public Collection<Question> getPublicQuestions(String instanceId)
      throws QuestionReplyException {
    Collection<Question> questions = new ArrayList<Question>();
    try {
      SilverpeasBeanDAO daoQ = getQdao();
      IdPK pk = new IdPK();
      questions = daoQ.findByWhereClause(pk, " instanceId = '" + instanceId
          + "' and publicReplyNumber > 0 ");
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.getPublicQuestions",
          SilverpeasException.ERROR, "questionReply.EX_CANT_GET_QUESTIONS", "",
          e);
    }
    return questions;
  }

  /*
   * enregistre une question et une réponse
   */
  public long createQuestionReply(Question question, Reply reply)
      throws QuestionReplyException {
    boolean succeed = false;
    Connection con = null;
    long idQ = -1;
    try {
      con = DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
      SilverpeasBeanDAO daoQ = getQdao();
      IdPK pkQ = (IdPK) daoQ.add(con, question);
      createQuestionIndex(question);
      idQ = pkQ.getIdAsLong();
      SilverpeasBeanDAO daoR = getRdao();
      reply.setQuestionId(idQ);
      daoR.add(con, reply);
      createReplyIndex(reply);

      question = getQuestion(idQ);
      getQuestionReplyContentManager().createSilverContent(con, question);
      succeed = true;
    } catch (UtilException e) {
      throw new QuestionReplyException("QuestionManager.createQuestion",
          SilverpeasException.ERROR, "questionReply.EX_CREATE_QUESTION_FAILED",
          "", e);
    } catch (Exception e) {
      throw new QuestionReplyException("QuestionManager.createQuestion",
          SilverpeasException.ERROR, "questionReply.EX_CREATE_QUESTION_FAILED",
          "", e);
    } finally {
      closeConnection(con, succeed);
    }
    return idQ;
  }

  /*
   * recupère le nombre de réponses d'une question
   */
  private int getQuestionRepliesNumber(long questionId)
      throws QuestionReplyException {
    int nb = 0;
    try {
      SilverpeasBeanDAO daoR = getRdao();
      IdPK pk = new IdPK();
      Collection replies = daoR.findByWhereClause(pk, " questionId = "
          + new Long(questionId).toString());
      nb = replies.size();
    } catch (PersistenceException e) {
      throw new QuestionReplyException("QuestionManager.getQuestionReplies",
          SilverpeasException.ERROR, "questionReply.EX_CANT_GET_REPLIES", "", e);
    }
    return nb;
  }

  /*
   * recupère le nombre de réponses publiques d'une question
   */
  private int getQuestionPublicRepliesNumber(long questionId)
      throws QuestionReplyException {
    int nb = 0;
    try {
      SilverpeasBeanDAO daoR = getRdao();
      IdPK pk = new IdPK();
      Collection replies = daoR.findByWhereClause(pk,
          " publicReply = 1 and questionId = "
              + new Long(questionId).toString());
      nb = replies.size();
    } catch (PersistenceException e) {
      throw new QuestionReplyException(
          "QuestionManager.getQuestionPublicReplies",
          SilverpeasException.ERROR, "questionReply.EX_CANT_GET_REPLIES", "", e);
    }
    return nb;
  }

  /*
   * recupère le nombre de réponses privées d'une question
   */
  private int getQuestionPrivateRepliesNumber(long questionId)
      throws QuestionReplyException {
    int nb = 0;
    try {
      SilverpeasBeanDAO daoR = getRdao();
      IdPK pk = new IdPK();
      Collection replies = daoR.findByWhereClause(pk,
          " privateReply = 1 and questionId = "
              + new Long(questionId).toString());
      nb = replies.size();
    } catch (PersistenceException e) {
      throw new QuestionReplyException(
          "QuestionManager.getQuestionPrivateReplies",
          SilverpeasException.ERROR, "questionReply.EX_CANT_GET_REPLIES", "", e);
    }
    return nb;
  }

  private void closeConnection(Connection con, boolean succeed)
      throws QuestionReplyException {
    if (con != null) {
      try {
        con.close();
      } catch (SQLException e) {
        throw new QuestionReplyException("QuestionManager.closeConnection",
            SilverpeasException.ERROR,
            "questionReply.EX_CREATE_QUESTION_FAILED", "", e);
      }
    }
  }

  protected boolean isSortable(String instanceId) {
    return StringUtil
        .getBooleanValue(controller.getComponentParameterValue(instanceId, "sortable"));
  }
}
