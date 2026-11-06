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
package org.silverpeas.components.questionreply.control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.components.questionreply.model.Reply;
import org.silverpeas.components.questionreply.service.QuestionManager;
import org.silverpeas.core.persistence.jdbc.bean.IdPK;
import org.silverpeas.kernel.exception.NotFoundException;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests on the access to the questions and to the replies from the user session of a given
 * FAQ: the questions and the replies of another FAQ must be neither read nor modified.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class QuestionReplySessionControllerTest {

  private static final String FAQ_ID = "questionReply5";
  private static final String OTHER_FAQ_ID = "questionReply2";

  @TestManagedMock
  private QuestionManager questionManager;

  private final QuestionReplySessionController controller =
      mock(QuestionReplySessionController.class);

  @BeforeEach
  void setUp() throws Exception {
    when(controller.getComponentId()).thenReturn(FAQ_ID);
    when(controller.getRepliesForQuestion(anyLong())).thenReturn(List.of());
    doCallRealMethod().when(controller).getQuestion(anyLong());
    doCallRealMethod().when(controller).getReply(anyLong());
    doCallRealMethod().when(controller).deleteQuestions(anyCollection());
    doCallRealMethod().when(controller).closeQuestions(anyCollection());
    doCallRealMethod().when(controller).closeQuestion(anyLong());
    doCallRealMethod().when(controller).openQuestion(anyLong());
    doCallRealMethod().when(controller).deleteR(anyCollection());
    doCallRealMethod().when(controller).setCurrentQuestion(any());
    doCallRealMethod().when(controller).getCurrentQuestion();
    when(questionManager.getQuestion(1)).thenReturn(aQuestion(1, FAQ_ID));
    when(questionManager.getQuestion(2)).thenReturn(aQuestion(2, OTHER_FAQ_ID));
    when(questionManager.getReply(10)).thenReturn(aReply(10, 1));
    when(questionManager.getReply(20)).thenReturn(aReply(20, 2));
  }

  @Test
  void getAQuestionOfTheFAQ() throws Exception {
    Question question = controller.getQuestion(1);

    assertThat(controller.getCurrentQuestion(), sameInstance(question));
  }

  @Test
  void getAQuestionOfAnotherFAQIsNotFound() {
    assertThrows(NotFoundException.class, () -> controller.getQuestion(2));
    verify(controller, never()).setCurrentQuestion(any());
  }

  @Test
  void getAnUnknownQuestionIsNotFound() {
    assertThrows(NotFoundException.class, () -> controller.getQuestion(3));
  }

  @Test
  void getAReplyOfTheFAQ() throws Exception {
    Reply reply = controller.getReply(10);

    verify(controller).setCurrentReply(reply);
  }

  @Test
  void getAReplyOfAnotherFAQIsNotFound() {
    assertThrows(NotFoundException.class, () -> controller.getReply(20));
    verify(controller, never()).setCurrentReply(any());
  }

  @Test
  void getAnUnknownReplyIsNotFound() {
    assertThrows(NotFoundException.class, () -> controller.getReply(30));
  }

  @Test
  void deleteQuestionsOfTheFAQ() throws Exception {
    controller.deleteQuestions(List.of(1L));

    verify(questionManager).deleteQuestionAndReplies(List.of(1L));
  }

  @Test
  void deleteQuestionsAmongWhichOneOfAnotherFAQIsNotFound() throws Exception {
    assertThrows(NotFoundException.class,
        () -> controller.deleteQuestions(List.of(1L, 2L)));
    verify(questionManager, never()).deleteQuestionAndReplies(anyCollection());
  }

  @Test
  void closeQuestionsAmongWhichOneOfAnotherFAQIsNotFound() throws Exception {
    assertThrows(NotFoundException.class,
        () -> controller.closeQuestions(List.of(1L, 2L)));
    verify(questionManager, never()).closeQuestions(anyCollection());
  }

  @Test
  void closeAQuestionOfAnotherFAQIsNotFound() throws Exception {
    assertThrows(NotFoundException.class, () -> controller.closeQuestion(2));
    verify(questionManager, never()).closeQuestions(anyCollection());
  }

  @Test
  void openAQuestionOfAnotherFAQIsNotFound() throws Exception {
    assertThrows(NotFoundException.class, () -> controller.openQuestion(2));
    verify(questionManager, never()).openQuestions(anyCollection());
  }

  @Test
  void deleteRepliesOfTheCurrentQuestion() throws Exception {
    controller.getQuestion(1);

    controller.deleteR(List.of(10L));

    verify(questionManager).updateRepliesPrivateStatus(eq(List.of(10L)), any(Question.class));
    verify(questionManager).updateRepliesPublicStatus(eq(List.of(10L)), any(Question.class));
  }

  @Test
  void deleteRepliesAmongWhichOneOfAnotherQuestionIsNotFound() throws Exception {
    controller.getQuestion(1);

    assertThrows(NotFoundException.class, () -> controller.deleteR(List.of(10L, 20L)));
    verify(questionManager, never()).updateRepliesPrivateStatus(anyCollection(), any());
    verify(questionManager, never()).updateRepliesPublicStatus(anyCollection(), any());
  }

  private static Question aQuestion(int id, String instanceId) {
    Question question = new Question("0", instanceId);
    question.setPK(new IdPK(id));
    return question;
  }

  private static Reply aReply(int id, long questionId) {
    Reply reply = new Reply(questionId, "0");
    reply.setPK(new IdPK(id));
    return reply;
  }
}
