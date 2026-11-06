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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.components.questionreply.test.QuestionReplyTestContext;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.test.integration.rule.DbSetupRule;

import jakarta.inject.Inject;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests on the access to the questions of the FAQs: a question is always got within
 * its FAQ.
 */
@RunWith(Arquillian.class)
public class QuestionRepositoryIT {

  private static final String FAQ = "questionReply5";
  private static final String OTHER_FAQ = "questionReply2";

  @Rule
  public DbSetupRule dbSetupRule = QuestionReplyTestContext.twoFaqs();

  @Inject
  private QuestionRepository repository;

  @Deployment
  public static WebArchive createTestArchive() {
    return QuestionReplyTestContext.warForTestClass(QuestionRepositoryIT.class);
  }

  @Test
  public void getTheFaqOfAQuestion() {
    assertThat(repository.getInstanceIdOf(1).orElseThrow(), is(OTHER_FAQ));
    assertThat(repository.getInstanceIdOf(3).orElseThrow(), is(FAQ));
  }

  @Test
  public void getTheFaqOfAnUnknownQuestion() {
    assertThat(repository.getInstanceIdOf(100).isEmpty(), is(true));
  }

  @Test
  public void getAQuestionOfTheFaq() {
    Question question = repository.getById(5, FAQ);

    assertThat(question, notNullValue());
    assertThat(question.getId(), is("5"));
    assertThat(question.getIdAsLong(), is(5L));
    assertThat(question.getInstanceId(), is(FAQ));
    assertThat(question.getTitle(), is("A closed question"));
    assertThat(question.getCreatorId(), is("3"));
    assertThat(question.getCreationDate(), is("2026/03/10"));
    assertThat(question.hasClosedStatus(), is(true));
    assertThat(question.getPublicReplyNumber(), is(1));
    assertThat(question.getPrivateReplyNumber(), is(1));
    assertThat(question.getReplyNumber(), is(2));
    assertThat(question.getCategoryId(), is("12"));
  }

  @Test
  public void aQuestionOfAnotherFaqIsNotFound() {
    assertThat(repository.getById(1, FAQ), nullValue());
  }

  @Test
  public void getQuestionsByIdsServesOnlyTheQuestionsOfTheFaq() {
    List<Question> questions = repository.getById(List.of(1, 2, 3, 4), FAQ);

    assertThat(idsOf(questions), contains("3", "4"));
  }

  @Test
  public void getQuestionsByNoIdsReturnsNothing() {
    assertThat(repository.getById(List.of(), FAQ), empty());
  }

  @Test
  public void findTheQuestionsNotClosedOrWithPublicRepliesTheMoreRecentFirst() {
    List<Question> questions = repository.findNotClosedOrWithPublicReplies(FAQ);

    assertThat(idsOf(questions), contains("5", "4", "3"));
  }

  @Test
  public void findTheQuestionsWithPublicReplies() {
    assertThat(idsOf(repository.findWithPublicReplies(FAQ)), contains("5"));
    assertThat(idsOf(repository.findWithPublicReplies(OTHER_FAQ)), contains("2"));
  }

  @Test
  public void findTheQuestionsSentByAUserThatAreNotClosedOrWithPrivateReplies() {
    assertThat(idsOf(repository.findSentBy("2", FAQ)), contains("3", "4"));
    assertThat(idsOf(repository.findSentBy("3", FAQ)), contains("5"));
    assertThat(repository.findSentBy("1", FAQ), empty());
  }

  @Test
  public void findTheQuestionsNotClosedReceivedByAUserInTheFaq() {
    assertThat(idsOf(repository.findReceivedBy("4", FAQ)), contains("3", "4"));
    assertThat(idsOf(repository.findReceivedBy("5", FAQ)), contains("4"));
    assertThat(repository.findReceivedBy("4", OTHER_FAQ), empty());
  }

  @Test
  public void saveANewQuestion() {
    Question question = new Question("2", FAQ);
    question.setTitle("A new question");
    question.setContent("Its content");

    Question saved = Transaction.performInOne(() -> repository.save(question));

    assertThat(saved.getId(), is("6"));
    Question actual = repository.getById(6, FAQ);
    assertThat(actual, notNullValue());
    assertThat(actual.getTitle(), is("A new question"));
    assertThat(actual.hasNewStatus(), is(true));
  }

  @Test
  public void updateAQuestion() {
    Question question = repository.getById(3, FAQ);
    question.setTitle("Updated");
    question.waitForAnswer();

    Transaction.performInOne(() -> repository.save(question));

    Question actual = repository.getById(3, FAQ);
    assertThat(actual.getTitle(), is("Updated"));
    assertThat(actual.hasWaitingStatus(), is(true));
  }

  @Test
  public void deleteAQuestion() {
    Question question = repository.getById(3, FAQ);

    Transaction.performInOne(() -> {
      repository.delete(List.of(question));
      return null;
    });

    assertThat(repository.getById(3, FAQ), nullValue());
  }

  private static List<String> idsOf(List<Question> questions) {
    return questions.stream().map(Question::getId).toList();
  }
}
