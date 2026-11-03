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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.discussion.service;

import java.util.List;
import jakarta.transaction.Transactional;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerDetail;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerHeader;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerPK;
import org.silverpeas.core.questioncontainer.container.service.QuestionContainerService;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class DiscussionVotePlannerTest {
  @TestManagedMock private QuestionContainerService containers;
  @TestedBean private DiscussionVotePlanner planner;

  @Test
  void nativeCreationHasExactlyOneQuestionAndPreservesRequiredAnonymity() {
    var user = mock(User.class);
    when(user.getId()).thenReturn("actor");
    var pk = new QuestionContainerPK("123", "space1", "pollingStation1");
    when(containers.createQuestionContainer(any(), any(), eq("actor"))).thenReturn(pk);
    try (var users = mockStatic(User.class)) {
      users.when(User::getCurrentRequester).thenReturn(user);
      assertSame(pk, planner.create(new DiscussionVoteInput("pollingStation1", "Quand ?", List.of("Mardi", "Jeudi"), true, false), "space1", true));
      var capture = ArgumentCaptor.forClass(QuestionContainerDetail.class);
      verify(containers).createQuestionContainer(any(), capture.capture(), eq("actor"));
      var detail = capture.getValue();
      assertEquals(1, detail.getQuestions().size());
      assertEquals("checkbox", detail.getFirstQuestion().getStyle());
      assertEquals(List.of("Mardi", "Jeudi"), detail.getFirstQuestion().getAnswers().stream().map(a -> a.getLabel()).toList());
      assertTrue(detail.getHeader().isAnonymous());
      assertFalse(detail.getHeader().isClosed());
      assertEquals(QuestionContainerHeader.IMMEDIATE_RESULTS, detail.getHeader().getResultMode());
      assertEquals("actor", detail.getHeader().getCreatorId());
      clearInvocations(containers);
      planner.create(new DiscussionVoteInput("pollingStation1", "Question", List.of("Oui", "Non"), false, false), "space1", false);
      verify(containers).createQuestionContainer(any(), capture.capture(), eq("actor"));
      assertEquals("radio", capture.getValue().getFirstQuestion().getStyle());
      assertFalse(capture.getValue().getHeader().isAnonymous());
    }
  }

  @Test
  void rollbackDispatcherSchedulesTheNativeIdentityBeforeTransactionCompletion() {
    @SuppressWarnings("unchecked")
    jakarta.enterprise.event.Event<DiscussionVoteRollback> events = mock(jakarta.enterprise.event.Event.class);
    var pk = new QuestionContainerPK("123", "space1", "pollingStation1");
    new DiscussionVoteRollbackDispatcher(events).schedule(pk);
    verify(events).fire(new DiscussionVoteRollback(pk));
  }

  @Test
  void partiallyCreatedNativeVoteIsRemovedOnFailureAndRollbackCleanupRunsOnlyAfterFailure() throws Exception {
    var user = mock(User.class);
    when(user.getId()).thenReturn("actor");
    var pk = new QuestionContainerPK("123", "space1", "pollingStation1");
    when(containers.createQuestionContainer(any(), any(), anyString())).thenAnswer(invocation -> {
      invocation.getArgument(1, QuestionContainerDetail.class).getHeader().setPK(pk);
      throw new IllegalStateException("Native answer creation failed");
    });
    var input = new DiscussionVoteInput("pollingStation1", "Question", List.of("Oui", "Non"), false, false);
    try (var users = mockStatic(User.class)) {
      users.when(User::getCurrentRequester).thenReturn(user);
      assertThrows(IllegalStateException.class, () -> planner.create(input, "space1", false));
      verify(containers).deleteQuestionContainer(pk);
      planner.rollback(new DiscussionVoteRollback(pk));
      verify(containers, times(2)).deleteQuestionContainer(pk);
    }
    var method = DiscussionVotePlanner.class.getMethod("rollback", DiscussionVoteRollback.class);
    assertEquals(TransactionPhase.AFTER_FAILURE, method.getParameters()[0].getAnnotation(Observes.class).during());
    assertEquals(Transactional.TxType.REQUIRES_NEW, method.getAnnotation(Transactional.class).value());
  }
}
