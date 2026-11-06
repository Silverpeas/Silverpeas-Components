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

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.silverpeas.components.questionreply.QuestionReplyException;
import org.silverpeas.components.questionreply.index.QuestionIndexer;
import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.components.questionreply.model.QuestionDetail;
import org.silverpeas.components.questionreply.model.Recipient;
import org.silverpeas.components.questionreply.model.Reply;
import org.silverpeas.components.questionreply.repository.QuestionRepository;
import org.silverpeas.components.questionreply.repository.RecipientRepository;
import org.silverpeas.components.questionreply.repository.ReplyRepository;
import org.silverpeas.components.questionreply.service.notification.SubscriptionNotifier;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.i18n.I18n;
import org.silverpeas.core.pdc.pdc.model.PdcPosition;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.kernel.util.StringUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper.buildAndSend;
import static org.silverpeas.core.pdc.pdc.model.PdcClassification.aPdcClassificationOfContent;

@Service
public class SilverpeasQuestionManager implements QuestionManager {

  @Inject
  private QuestionRepository questionRepository;
  @Inject
  private ReplyRepository replyRepository;
  @Inject
  private RecipientRepository recipientRepository;
  @Inject
  private QuestionIndexer questionIndexer;
  @Inject
  private QuestionReplyContentManager contentManager;
  @Inject
  private OrganizationController controller;
  @Inject
  private I18n i18n;

  /**
   * Create and persist a question with targeted recipient (be careful recipient doesn't have
   * question identifier set)
   * @param question the question to create
   * @return the generated question identifier
   * @throws QuestionReplyException if an error occurs
   */
  @Override
  @Transactional
  public long createQuestion(Question question) throws QuestionReplyException {
    Collection<Recipient> recipients = question.readRecipients();
    questionRepository.save(question);
    long questionId = question.getIdAsLong();
    questionIndexer.createIndex(question, List.of());
    for (Recipient recipient : recipients) {
      recipient.setQuestionId(questionId);
      recipientRepository.save(recipient);
    }
    try (Connection con = DBUtil.openConnection()) {
      contentManager.createSilverContent(con, question);
    } catch (SQLException | ContentManagerException e) {
      throw new QuestionReplyException(e);
    }
    return questionId;
  }

  /*
   * enregistre une réponse à une question : met à jour publicReplyNumber et/ou privateReplyNumber
   * et replyNumber de la question ainsi que le status à 1.
   */
  @Override
  @Transactional
  public long createReply(Reply reply, Question question) throws QuestionReplyException {
    reply.setQuestion(question);
    replyRepository.save(reply);
    WysiwygController.createFileAndAttachment(reply.readCurrentWysiwygContent(),
        reply.toResourceReference(), reply.getCreatorId(), i18n.getDefaultLanguage());
    if (question.hasNewStatus()) {
      question.waitForAnswer();
    }
    updateQuestion(question);
    notifySubscribers(question, reply);
    return reply.getIdAsLong();
  }

  @Override
  @Transactional
  public void closeQuestions(Collection<Long> questionIds, String instanceId)
      throws QuestionReplyException {
    for (Question question : getQuestionsOfFaq(questionIds, instanceId)) {
      question.close();
      updateQuestion(question);
    }
  }

  @Override
  @Transactional
  public void openQuestions(Collection<Long> questionIds, String instanceId)
      throws QuestionReplyException {
    for (Question question : getQuestionsOfFaq(questionIds, instanceId)) {
      question.waitForAnswer();
      updateQuestion(question);
    }
  }

  /*
   * Affecte le status public à 0 d'une liste de réponses : updateReply décrémente le nombre de
   * réponses publiques de la question d'autant : updateQuestion
   */
  @Override
  @Transactional
  public void updateRepliesPublicStatus(Collection<Long> replyIds, Question question)
      throws QuestionReplyException {
    if (replyIds != null) {
      for (Reply reply : getReplies(replyIds, question)) {
        reply.setPublicReply(0);
        updateReplyStatus(reply);
      }
      updateQuestion(question);
    }
  }

  /*
   * Affecte le status private à 0 d'une liste de réponses : updateReply décrémente le nombre de
   * réponses privées de la question d'autant : updateQuestion
   */
  @Override
  @Transactional
  public void updateRepliesPrivateStatus(Collection<Long> replyIds, Question question)
      throws QuestionReplyException {
    if (replyIds != null) {
      for (Reply reply : getReplies(replyIds, question)) {
        reply.setPrivateReply(0);
        updateReplyStatus(reply);
      }
      updateQuestion(question);
    }
  }

  /*
   * Modifie une question. La question est supprimée si publicReplyNumber et privateReplyNumber
   * sont à 0 et que la question est fermée. Met à jour publicReplyNumber et/ou privateReplyNumber
   * et replyNumber de la question
   */
  @Override
  @Transactional
  public void updateQuestion(Question question) throws QuestionReplyException {
    try {
      long questionId = question.getIdAsLong();
      question.setReplyNumber(replyRepository.countByQuestion(questionId));
      question.setPublicReplyNumber(replyRepository.countPublicByQuestion(questionId));
      question.setPrivateReplyNumber(replyRepository.countPrivateByQuestion(questionId));
      if ((question.getReplyNumber() == 0) && question.hasClosedStatus()) {
        deleteQuestion(question);
      } else {
        questionRepository.save(question);
        questionIndexer.updateIndex(question, getAllReplies(questionId, question.getInstanceId()));
        contentManager.updateSilverContentVisibility(question);
      }
    } catch (ContentManagerException e) {
      throw new QuestionReplyException(e);
    }
  }

  /*
   * Modifie une réponse : la réponse est supprimée si le status public et le status private sont à
   * 0.
   */
  @Override
  @Transactional
  public void updateReply(Reply reply) throws QuestionReplyException {
    Question question = reply.getQuestion();
    if ((reply.getPublicReply() == 0) && (reply.getPrivateReply() == 0)) {
      replyRepository.delete(List.of(reply));
    } else {
      replyRepository.save(reply);
      updateWysiwygContent(reply);
    }
    questionIndexer.updateIndex(question,
        getAllReplies(question.getIdAsLong(), question.getInstanceId()));
  }

  @Override
  @Transactional
  public void deleteQuestionAndReplies(Collection<Long> questionIds, String instanceId)
      throws QuestionReplyException {
    for (Question question : getQuestionsOfFaq(questionIds, instanceId)) {
      long questionId = question.getIdAsLong();
      recipientRepository.deleteByQuestion(questionId);
      replyRepository.delete(getAllReplies(questionId, instanceId));
      questionIndexer.deleteIndex(question);
      questionRepository.delete(List.of(question));
      deleteSilverContent(question);
    }
  }

  @Override
  public List<Reply> getAllReplies(long questionId, String instanceId)
      throws QuestionReplyException {
    List<Reply> allReplies = new ArrayList<>(getQuestionPrivateReplies(questionId, instanceId));
    allReplies.addAll(getQuestionPublicReplies(questionId, instanceId));
    return allReplies;
  }

  @Override
  public Optional<String> getInstanceIdOfQuestion(long questionId) {
    return questionRepository.getInstanceIdOf(questionId);
  }

  @Override
  public Question getQuestion(long questionId, String instanceId) {
    return questionRepository.getById(questionId, instanceId);
  }

  @Override
  public List<Question> getQuestionsByIds(List<String> ids, String instanceId) {
    return questionRepository.getById(
        ids.stream().map(Integer::parseInt).collect(Collectors.toSet()), instanceId);
  }

  /*
   * récupère la liste des réponses d'une question
   */
  @Override
  public List<Reply> getQuestionReplies(long questionId, String instanceId) {
    return withWysiwygContent(replyRepository.findByQuestion(questionId, instanceId));
  }

  /*
   * récupère la liste des réponses publiques d'une question
   */
  @Override
  public List<Reply> getQuestionPublicReplies(long questionId, String instanceId) {
    return withWysiwygContent(replyRepository.findPublicByQuestion(questionId, instanceId));
  }

  /*
   * récupère la liste des réponses privées d'une question
   */
  @Override
  public List<Reply> getQuestionPrivateReplies(long questionId, String instanceId) {
    return withWysiwygContent(replyRepository.findPrivateByQuestion(questionId, instanceId));
  }

  /*
   * récupère la liste des destinataires d'une question
   */
  @Override
  public List<Recipient> getQuestionRecipients(long questionId) {
    return recipientRepository.findByQuestion(questionId);
  }

  /*
   * récupère une réponse
   */
  @Override
  public Reply getReply(long replyId, String instanceId) {
    Reply reply = replyRepository.getById(replyId, instanceId);
    if (reply != null) {
      reply.loadWysiwygContent();
    }
    return reply;
  }

  /*
   * Récupère la liste des questions émises par un utilisateur et qui ne soient pas fermées ou
   * fermées avec réponses privées
   */
  @Override
  public List<Question> getSendQuestions(String userId, String instanceId) {
    return questionRepository.findSentBy(userId, instanceId);
  }

  /*
   * Récupère la liste des questions recues par un utilisateur, et qui ne sont pas fermées
   */
  @Override
  public List<Question> getReceiveQuestions(String userId, String instanceId) {
    return questionRepository.findReceivedBy(userId, instanceId);
  }

  /*
   * Récupère la liste des questions qui ne sont pas closes ou closes avec réponses publiques
   */
  @Override
  public List<Question> getQuestions(String instanceId) {
    return questionRepository.findNotClosedOrWithPublicReplies(instanceId);
  }

  /*
   * Récupère la liste de toutes les questions avec toutes ses réponses
   */
  @Override
  public List<Question> getAllQuestions(String instanceId) {
    List<Question> questions = getQuestions(instanceId).stream()
        .map(this::withReplies)
        .collect(Collectors.toList());
    if (isSortable(instanceId)) {
      questions.sort(QuestionRegexpComparator.getInstance());
    }
    return questions;
  }

  @Override
  public List<Question> getAllQuestionsByCategory(String instanceId, String categoryId) {
    List<Question> questions = getQuestions(instanceId).stream()
        .filter(question -> isInCategory(question, categoryId))
        .map(this::withReplies)
        .collect(Collectors.toList());
    if (isSortable(instanceId)) {
      questions.sort(QuestionRegexpComparator.getInstance());
    }
    return questions;
  }

  /*
   * Récupère la liste des questions publiques avec réponses
   */
  @Override
  public List<Question> getPublicQuestions(String instanceId) {
    return questionRepository.findWithPublicReplies(instanceId);
  }

  /**
   * Save and persist question and reply given in parameter
   * @param question the new question
   * @param reply the answer linked to the given question
   * @return the created question identifier
   * @throws QuestionReplyException if an error occurs
   */
  @Override
  public long createQuestionReply(Question question, Reply reply) throws QuestionReplyException {
    return createQuestionReply(question, reply, List.of());
  }

  @Override
  @Transactional
  public long createQuestionReply(Question question, Reply reply, List<PdcPosition> positions)
      throws QuestionReplyException {
    questionRepository.save(question);
    reply.setQuestion(question);
    replyRepository.save(reply);
    WysiwygController.createFileAndAttachment(reply.readCurrentWysiwygContent(),
        reply.toResourceReference(), reply.getCreatorId(), i18n.getDefaultLanguage());
    questionIndexer.createIndex(question, List.of(reply));
    try (Connection con = DBUtil.openConnection()) {
      contentManager.createSilverContent(con, question);
    } catch (SQLException | ContentManagerException e) {
      throw new QuestionReplyException(e);
    }
    classify(question, positions);
    notifySubscribers(question, reply);
    return question.getIdAsLong();
  }

  protected void updateWysiwygContent(Reply reply) {
    if (WysiwygController.haveGotWysiwyg(reply.getInstanceId(), reply.getId(),
        i18n.getDefaultLanguage())) {
      WysiwygController.updateFileAndAttachment(reply.readCurrentWysiwygContent(),
          reply.getInstanceId(), reply.getId(), reply.getCreatorId(), i18n.getDefaultLanguage());
    } else {
      WysiwygController.createUnindexedFileAndAttachment(reply.readCurrentWysiwygContent(),
          reply.toResourceReference(), reply.getCreatorId(), i18n.getDefaultLanguage());
    }
  }

  protected boolean isSortable(String instanceId) {
    return StringUtil
        .getBooleanValue(controller.getComponentParameterValue(instanceId, "sortable"));
  }

  private List<Question> getQuestionsOfFaq(Collection<Long> questionIds, String instanceId) {
    if (questionIds == null) {
      return List.of();
    }
    return questionRepository.getById(
        questionIds.stream().map(Long::intValue).collect(Collectors.toSet()), instanceId);
  }

  private List<Reply> getReplies(Collection<Long> replyIds, Question question) {
    return replyRepository.getById(
        replyIds.stream().map(Long::intValue).collect(Collectors.toSet()),
        question.getIdAsLong());
  }

  private List<Reply> withWysiwygContent(List<Reply> replies) {
    replies.forEach(Reply::loadWysiwygContent);
    return replies;
  }

  private Question withReplies(Question question) {
    question.writeReplies(getQuestionReplies(question.getIdAsLong(), question.getInstanceId()));
    return question;
  }

  private static boolean isInCategory(Question question, String categoryId) {
    return (StringUtil.isNotDefined(question.getCategoryId()) &&
        StringUtil.isNotDefined(categoryId)) ||
        (StringUtil.isDefined(categoryId) && categoryId.equals(question.getCategoryId()));
  }

  /*
   * Modifie le statut d'une réponse : la réponse est supprimée si le status public et le status
   * private sont à 0.
   */
  private void updateReplyStatus(Reply reply) throws QuestionReplyException {
    Question question = reply.getQuestion();
    if ((reply.getPublicReply() == 0) && (reply.getPrivateReply() == 0)) {
      replyRepository.delete(List.of(reply));
      WysiwygController.deleteFile(reply.getInstanceId(), reply.getId(),
          i18n.getDefaultLanguage());
    } else {
      replyRepository.save(reply);
      updateWysiwygContent(reply);
    }
    questionIndexer.updateIndex(question,
        getAllReplies(question.getIdAsLong(), question.getInstanceId()));
  }

  private void deleteQuestion(Question question) throws QuestionReplyException {
    recipientRepository.deleteByQuestion(question.getIdAsLong());
    questionRepository.delete(List.of(question));
    questionIndexer.deleteIndex(question);
    deleteSilverContent(question);
  }

  private void deleteSilverContent(Question question) throws QuestionReplyException {
    try (Connection con = DBUtil.openConnection()) {
      contentManager.deleteSilverContent(con, question);
    } catch (SQLException | ContentManagerException e) {
      throw new QuestionReplyException(e);
    }
  }

  /**
   * Classifies the specified question on the PdC. The subscribers on the PdC aren't alerted about
   * the classification: they are notified, with the other subscribers, about the replies to the
   * question.
   */
  private void classify(Question question, List<PdcPosition> positions) {
    if (!positions.isEmpty()) {
      final QuestionDetail questionDetail = new QuestionDetail(question);
      aPdcClassificationOfContent(questionDetail).withPositions(positions)
          .classifyContent(questionDetail, false);
    }
  }

  private void notifySubscribers(Question question, Reply reply) {
    if (reply.getPublicReply() == 1) {
      final User sender = reply.readAuthor();
      buildAndSend(new SubscriptionNotifier(sender, question, reply));
    }
  }
}
