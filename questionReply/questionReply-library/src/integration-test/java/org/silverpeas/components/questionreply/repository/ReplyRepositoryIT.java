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
import org.silverpeas.components.questionreply.model.Reply;
import org.silverpeas.components.questionreply.test.QuestionReplyTestContext;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.test.integration.rule.DbSetupRule;

import jakarta.inject.Inject;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests on the access to the replies to the questions of the FAQs: a reply is always
 * got through its question, and so within its FAQ.
 */
@RunWith(Arquillian.class)
public class ReplyRepositoryIT {

  private static final String FAQ = "questionReply5";
  private static final String OTHER_FAQ = "questionReply2";

  @Rule
  public DbSetupRule dbSetupRule = QuestionReplyTestContext.twoFaqs();

  @Inject
  private ReplyRepository repository;
  @Inject
  private QuestionRepository questionRepository;

  @Deployment
  public static WebArchive createTestArchive() {
    return QuestionReplyTestContext.warForTestClass(ReplyRepositoryIT.class);
  }

  @Test
  public void getAReplyToAQuestionOfTheFaq() {
    Reply reply = repository.getById(13, FAQ);

    assertThat(reply, notNullValue());
    assertThat(reply.getId(), is("13"));
    assertThat(reply.getQuestionId(), is(5L));
    assertThat(reply.getInstanceId(), is(FAQ));
    assertThat(reply.getTitle(), is("Public reply to 5"));
    assertThat(reply.getCreatorId(), is("4"));
    assertThat(reply.getCreationDateAsString(), is("2026/03/11"));
    assertThat(reply.getPublicReply(), is(1));
    assertThat(reply.getPrivateReply(), is(0));
  }

  @Test
  public void aReplyToAQuestionOfAnotherFaqIsNotFound() {
    assertThat(repository.getById(10, FAQ), nullValue());
    assertThat(repository.getById(10, OTHER_FAQ), notNullValue());
  }

  @Test
  public void getRepliesByIdsServesOnlyTheRepliesToTheQuestion() {
    List<Reply> replies = repository.getById(List.of(12, 13, 14, 10), 5);

    assertThat(idsOf(replies), contains("13", "14"));
  }

  @Test
  public void getRepliesByNoIdsReturnsNothing() {
    assertThat(repository.getById(List.of(), 5), empty());
  }

  @Test
  public void findTheRepliesToAQuestionOfTheFaq() {
    assertThat(idsOf(repository.findByQuestion(5, FAQ)), contains("13", "14"));
    assertThat(idsOf(repository.findPublicByQuestion(5, FAQ)), contains("13"));
    assertThat(idsOf(repository.findPrivateByQuestion(5, FAQ)), contains("14"));
  }

  @Test
  public void findTheRepliesToAQuestionOfAnotherFaqReturnsNothing() {
    assertThat(repository.findByQuestion(1, FAQ), empty());
    assertThat(repository.findPublicByQuestion(2, FAQ), empty());
    assertThat(repository.findPrivateByQuestion(1, FAQ), empty());
  }

  @Test
  public void countTheRepliesToAQuestion() {
    assertThat(repository.countByQuestion(5), is(2));
    assertThat(repository.countPublicByQuestion(5), is(1));
    assertThat(repository.countPrivateByQuestion(5), is(1));
    assertThat(repository.countByQuestion(3), is(0));
  }

  @Test
  public void saveANewReply() {
    Question question = questionRepository.getById(3, FAQ);
    Reply reply = new Reply(question, "4");
    reply.setTitle("A reply");
    reply.setPublicReply(1);
    reply.setPrivateReply(0);

    Reply saved = Transaction.performInOne(() -> repository.save(reply));

    assertThat(saved.getId(), is("15"));
    assertThat(idsOf(repository.findByQuestion(3, FAQ)), contains("15"));
    assertThat(repository.getById(15, FAQ).getInstanceId(), is(FAQ));
  }

  @Test
  public void deleteAReply() {
    Reply reply = repository.getById(14, FAQ);

    Transaction.performInOne(() -> {
      repository.delete(List.of(reply));
      return null;
    });

    assertThat(idsOf(repository.findByQuestion(5, FAQ)), contains("13"));
  }

  private static List<String> idsOf(List<Reply> replies) {
    return replies.stream().map(Reply::getId).toList();
  }
}
