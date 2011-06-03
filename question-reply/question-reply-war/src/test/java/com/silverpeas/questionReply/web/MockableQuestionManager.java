/*
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
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.questionReply.web;

import com.silverpeas.questionReply.QuestionReplyException;
import com.silverpeas.questionReply.control.QuestionManager;
import com.silverpeas.questionReply.model.Question;
import com.silverpeas.questionReply.model.Recipient;
import com.silverpeas.questionReply.model.Reply;

import java.util.Collection;
import java.util.List;
import javax.inject.Named;

/**
 *
 * @author emmanuel.hugonnet@silverpeas.org
 */
@Named
public class MockableQuestionManager implements QuestionManager {

  private QuestionManager questionManager;

  void setQuestionManager(QuestionManager questionManager) {
    this.questionManager = questionManager;
  }

  public QuestionManager getQuestionManager() {
    return questionManager;
  }

  @Override
  public long createQuestion(Question question) throws QuestionReplyException {
    return questionManager.createQuestion(question);
  }

  @Override
  public long createReply(Reply reply, Question question) throws QuestionReplyException {
    return questionManager.createReply(reply, question);
  }

  @Override
  public void closeQuestions(Collection<Long> questionIds) throws QuestionReplyException {
    questionManager.closeQuestions(questionIds);
  }

  @Override
  public void openQuestions(Collection<Long> questionIds) throws QuestionReplyException {
    questionManager.openQuestions(questionIds);
  }

  @Override
  public void updateQuestionRecipients(Question question) throws QuestionReplyException {
    questionManager.updateQuestionRecipients(question);
  }

  @Override
  public void updateQuestionRepliesPublicStatus(Collection<Long> questionIds)
      throws QuestionReplyException {
    questionManager.updateQuestionRepliesPublicStatus(questionIds);
  }

  @Override
  public void updateQuestionRepliesPrivateStatus(Collection<Long> questionIds)
      throws QuestionReplyException {
    questionManager.updateQuestionRepliesPrivateStatus(questionIds);
  }

  @Override
  public void updateRepliesPublicStatus(Collection<Long> replyIds, Question question)
      throws QuestionReplyException {
    questionManager.updateRepliesPublicStatus(replyIds, question);
  }

  @Override
  public void updateRepliesPrivateStatus(Collection<Long> replyIds, Question question)
      throws QuestionReplyException {
    questionManager.updateRepliesPrivateStatus(replyIds, question);
  }

  @Override
  public void updateQuestion(Question question) throws QuestionReplyException {
    questionManager.updateQuestion(question);
  }

  @Override
  public void updateReply(Reply reply) throws QuestionReplyException {
    questionManager.updateReply(reply);
  }

  @Override
  public void deleteQuestionAndReplies(Collection<Long> questionIds)
      throws QuestionReplyException {
    questionManager.deleteQuestionAndReplies(questionIds);
  }

  @Override
  public List<Reply> getAllReplies(long questionId) throws QuestionReplyException {
    return questionManager.getAllReplies(questionId);
  }

  @Override
  public Question getQuestion(long questionId) throws QuestionReplyException {
    return questionManager.getQuestion(questionId);
  }

  @Override
  public Question getQuestionAndReplies(long questionId) throws QuestionReplyException {
    return questionManager.getQuestionAndReplies(questionId);
  }

  @Override
  public List<Question> getQuestionsByIds(List<String> ids) throws QuestionReplyException {
    return questionManager.getQuestionsByIds(ids);
  }

  @Override
  public List<Reply> getQuestionReplies(long questionId) throws QuestionReplyException {
    return questionManager.getQuestionReplies(questionId);
  }

  @Override
  public List<Reply> getQuestionPublicReplies(long questionId) throws QuestionReplyException {
    return questionManager.getQuestionPublicReplies(questionId);
  }

  @Override
  public List<Reply> getQuestionPrivateReplies(long questionId) throws QuestionReplyException {
    return questionManager.getQuestionPrivateReplies(questionId);
  }

  @Override
  public List<Recipient> getQuestionRecipients(long questionId) throws QuestionReplyException {
    return questionManager.getQuestionRecipients(questionId);
  }

  @Override
  public Reply getReply(long replyId) throws QuestionReplyException {
    return questionManager.getReply(replyId);
  }

  @Override
  public List<Question> getSendQuestions(String userId, String instanceId)
      throws QuestionReplyException {
    return questionManager.getSendQuestions(userId, instanceId);
  }

  @Override
  public List<Question> getReceiveQuestions(String userId, String instanceId)
      throws QuestionReplyException {
    return questionManager.getReceiveQuestions(userId, instanceId);
  }

  @Override
  public List<Question> getQuestions(String instanceId) throws QuestionReplyException {
    return questionManager.getQuestions(instanceId);
  }

  @Override
  public List<Question> getAllQuestions(String instanceId) throws QuestionReplyException {
    return questionManager.getAllQuestions(instanceId);
  }

  @Override
  public List<Question> getAllQuestionsByCategory(String instanceId, String categoryId)
      throws QuestionReplyException {
    return questionManager.getAllQuestionsByCategory(instanceId, categoryId);
  }

  @Override
  public List<Question> getPublicQuestions(String instanceId) throws QuestionReplyException {
    return questionManager.getPublicQuestions(instanceId);
  }

  @Override
  public long createQuestionReply(Question question, Reply reply) throws QuestionReplyException {
    return questionManager.createQuestionReply(question, reply);
  }
}