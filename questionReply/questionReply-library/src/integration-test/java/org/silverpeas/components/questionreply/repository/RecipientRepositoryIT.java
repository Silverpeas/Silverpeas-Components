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
import org.silverpeas.components.questionreply.model.Recipient;
import org.silverpeas.components.questionreply.test.QuestionReplyTestContext;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.test.integration.rule.DbSetupRule;

import jakarta.inject.Inject;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests on the persistence of the users to whom the questions are addressed.
 */
@RunWith(Arquillian.class)
public class RecipientRepositoryIT {

  @Rule
  public DbSetupRule dbSetupRule = QuestionReplyTestContext.twoFaqs();

  @Inject
  private RecipientRepository repository;

  @Deployment
  public static WebArchive createTestArchive() {
    return QuestionReplyTestContext.warForTestClass(RecipientRepositoryIT.class);
  }

  @Test
  public void findTheRecipientsOfAQuestion() {
    assertThat(usersOf(repository.findByQuestion(4)), contains("4", "5"));
    assertThat(repository.findByQuestion(2), empty());
  }

  @Test
  public void saveANewRecipient() {
    Transaction.performInOne(() -> repository.save(new Recipient(2, "6")));

    assertThat(usersOf(repository.findByQuestion(2)), contains("6"));
  }

  @Test
  public void deleteTheRecipientsOfAQuestion() {
    Transaction.performInOne(() -> {
      repository.deleteByQuestion(4);
      return null;
    });

    assertThat(repository.findByQuestion(4), empty());
    assertThat(usersOf(repository.findByQuestion(3)), contains("4"));
  }

  private static List<String> usersOf(List<Recipient> recipients) {
    return recipients.stream().map(Recipient::getUserId).toList();
  }
}
