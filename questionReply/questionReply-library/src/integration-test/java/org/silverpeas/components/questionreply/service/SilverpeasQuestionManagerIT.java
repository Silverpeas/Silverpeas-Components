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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.components.questionreply.QuestionReplyException;
import org.silverpeas.components.questionreply.index.QuestionIndexer;
import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.components.questionreply.model.Recipient;
import org.silverpeas.components.questionreply.model.Reply;
import org.silverpeas.components.questionreply.test.QuestionReplyTestContext;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.model.WysiwygContent;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygManager;
import org.silverpeas.core.contribution.model.LocalizedContribution;
import org.silverpeas.core.test.integration.rule.DbSetupRule;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;

import static jakarta.interceptor.Interceptor.Priority.APPLICATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests on the persistence of the questions and of the replies by the
 * {@link QuestionManager}: the changes are made in transactions, and only on the questions and on
 * the replies of the given FAQ.
 */
@RunWith(Arquillian.class)
public class SilverpeasQuestionManagerIT {

  private static final String FAQ = "questionReply5";
  private static final String OTHER_FAQ = "questionReply2";

  @Rule
  public DbSetupRule dbSetupRule = QuestionReplyTestContext.twoFaqs();

  @Inject
  private QuestionManager manager;

  @Deployment
  public static WebArchive createTestArchive() {
    return QuestionReplyTestContext.warForServiceTestClass(SilverpeasQuestionManagerIT.class);
  }

  @Test
  public void createAQuestionWithItsRecipients() throws QuestionReplyException {
    Question question = new Question("2", FAQ);
    question.setTitle("A new question");
    question.writeRecipients(List.of(new Recipient("4"), new Recipient("6")));

    long questionId = manager.createQuestion(question);

    assertThat(questionId, is(6L));
    assertThat(manager.getQuestion(6, FAQ).getTitle(), is("A new question"));
    assertThat(usersOf(manager.getQuestionRecipients(6)), contains("4", "6"));
  }

  @Test
  public void aReplyToANewQuestionPutsItInWaitingForAnswers() throws QuestionReplyException {
    Question question = manager.getQuestion(3, FAQ);
    Reply reply = new Reply(question, "4");
    reply.setTitle("A private reply");

    long replyId = manager.createReply(reply, question);

    assertThat(replyId, is(15L));
    Question actual = manager.getQuestion(3, FAQ);
    assertThat(actual.hasWaitingStatus(), is(true));
    assertThat(actual.getReplyNumber(), is(1));
    assertThat(actual.getPrivateReplyNumber(), is(1));
    assertThat(actual.getPublicReplyNumber(), is(0));
    assertThat(idsOf(manager.getAllReplies(3, FAQ)), contains("15"));
  }

  @Test
  public void aReplyWithoutAnyStatusIsDeleted() throws QuestionReplyException {
    Question question = manager.getQuestion(4, FAQ);

    manager.updateRepliesPrivateStatus(List.of(12L), question);

    assertThat(manager.getReply(12, FAQ), nullValue());
    Question actual = manager.getQuestion(4, FAQ);
    assertThat(actual, notNullValue());
    assertThat(actual.getReplyNumber(), is(0));
  }

  @Test
  public void aClosedQuestionWithoutAnyReplyIsDeleted() throws QuestionReplyException {
    Question question = manager.getQuestion(5, FAQ);

    manager.updateRepliesPublicStatus(List.of(13L), question);
    manager.updateRepliesPrivateStatus(List.of(14L), question);

    assertThat(manager.getQuestion(5, FAQ), nullValue());
    assertThat(manager.getQuestionRecipients(5), empty());
  }

  @Test
  public void theRepliesToAnotherQuestionAreLeftUnchanged() throws QuestionReplyException {
    Question question = manager.getQuestion(4, FAQ);

    manager.updateRepliesPrivateStatus(List.of(10L, 13L), question);

    assertThat(manager.getReply(10, OTHER_FAQ).getPrivateReply(), is(1));
    assertThat(manager.getReply(13, FAQ).getPublicReply(), is(1));
    assertThat(manager.getQuestion(4, FAQ).getReplyNumber(), is(1));
  }

  @Test
  public void onlyTheQuestionsOfTheFaqAreClosedOrOpened() throws QuestionReplyException {
    manager.closeQuestions(List.of(4L), FAQ);
    manager.openQuestions(List.of(1L, 2L), FAQ);

    assertThat(manager.getQuestion(4, FAQ).hasClosedStatus(), is(true));
    assertThat(manager.getQuestion(1, OTHER_FAQ).hasClosedStatus(), is(true));
    assertThat(manager.getQuestion(2, OTHER_FAQ).hasClosedStatus(), is(true));
  }

  @Test
  public void onlyTheQuestionsOfTheFaqAreDeletedWithTheirRepliesAndRecipients()
      throws QuestionReplyException {
    manager.deleteQuestionAndReplies(List.of(1L, 5L), FAQ);

    assertThat(manager.getQuestion(5, FAQ), nullValue());
    assertThat(manager.getReply(13, FAQ), nullValue());
    assertThat(manager.getReply(14, FAQ), nullValue());
    assertThat(manager.getQuestionRecipients(5), empty());
    assertThat(manager.getQuestion(1, OTHER_FAQ), notNullValue());
    assertThat(idsOf(manager.getAllReplies(1, OTHER_FAQ)), contains("10"));
    assertThat(usersOf(manager.getQuestionRecipients(1)), contains("4"));
  }

  private static List<String> idsOf(Collection<Reply> replies) {
    return replies.stream().map(Reply::getId).toList();
  }

  private static List<String> usersOf(Collection<Recipient> recipients) {
    return recipients.stream().map(Recipient::getUserId).toList();
  }

  /**
   * No indexation of the questions in the tests.
   */
  @Alternative
  @Priority(APPLICATION + 10)
  public static class QuestionIndexerStub extends QuestionIndexer {

    @Override
    public void createIndex(Question question, Collection<Reply> replies) {
      // nothing to index
    }

    @Override
    public void deleteIndex(Question question) {
      // nothing to unindex
    }
  }

  /**
   * No registering of the questions as Silverpeas contents in the tests.
   */
  @Service
  @Alternative
  @Priority(APPLICATION + 10)
  public static class QuestionReplyContentManagerStub extends QuestionReplyContentManager {

    @Override
    protected void createSilverContent(Connection con, Question question) {
      // nothing to register
    }

    @Override
    protected void updateSilverContentVisibility(Question question) {
      // nothing to update
    }

    @Override
    protected void deleteSilverContent(Connection con, Question question) {
      // nothing to unregister
    }
  }

  /**
   * No users in the tests: the authors of the contents aren't read from the data source.
   */
  @Provider
  @Alternative
  @Priority(APPLICATION + 10)
  public static class UserProviderStub implements UserProvider {

    @Override
    public User getUser(String userId) {
      return null;
    }

    @Override
    public User getUserByToken(@Nonnull String token) {
      return null;
    }

    @Override
    public User getUserByLoginAndDomainId(@Nonnull String login, @Nonnull String domainId) {
      return null;
    }

    @Override
    public User getSystemUser() {
      return null;
    }
  }

  /**
   * No WYSIWYG content for the replies in the tests.
   */
  @Service
  @Alternative
  @Priority(APPLICATION + 10)
  public static class WysiwygManagerStub extends WysiwygManager {

    @Override
    public void createFileAndAttachment(WysiwygContent content) {
      // no WYSIWYG content
    }

    @Override
    public void createUnindexedFileAndAttachment(WysiwygContent content) {
      // no WYSIWYG content
    }

    @Override
    public void updateFileAndAttachment(WysiwygContent content) {
      // no WYSIWYG content
    }

    @Override
    public void deleteFile(String componentId, String objectId, String language) {
      // no WYSIWYG content
    }

    @Override
    public boolean haveGotWysiwyg(LocalizedContribution contribution) {
      return false;
    }

    @Override
    public WysiwygContent getByContribution(LocalizedContribution contribution) {
      return new WysiwygContent(contribution);
    }
  }
}
