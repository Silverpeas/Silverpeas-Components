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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.questionreply.control;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.components.questionreply.model.QuestionDetail;
import org.silverpeas.components.questionreply.model.Reply;
import org.silverpeas.components.questionreply.service.QuestionManager;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.model.PdcPosition;
import org.silverpeas.core.pdc.pdc.service.PdcClassificationService;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.extension.LocalizationBundleStub;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the classification on the PdC of a question at its creation. The subscribers on
 * the PdC are never alerted about the classification of a new question: as the subscribers of the
 * application, they are notified about the public replies given to the question. For a FAQ, a
 * question created with its public reply, the classification is delegated to the creation of the
 * FAQ itself so that the question is classified before the subscribers are notified.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class QuestionClassificationAtCreationTest {

  private static final String COMPONENT_ID = "questionReply7";
  private static final long QUESTION_ID = 23L;
  private static final String AUTHOR = "1";
  private static final String BUNDLE = "org.silverpeas.questionReply.multilang.questionReplyBundle";
  private static final String A_POSITION_ON_THE_PDC =
      "{\"positions\":[{\"values\":[{\"id\":\"/0/12/\",\"axisId\":3,\"treeId\":\"1\"}]}]}";

  @RegisterExtension
  static LocalizationBundleStub questionReplyBundle =
      new LocalizationBundleStub(BUNDLE, LocalizationBundleStub.LANGUAGE_ALL);

  @TestManagedMock
  QuestionManager questionManager;
  @TestManagedMock
  PdcClassificationService pdcClassificationService;

  private QuestionReplySessionController controller;

  @BeforeEach
  void setUpTheController() throws Exception {
    CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
    final UserDetail author = mock(UserDetail.class);
    when(author.getId()).thenReturn(AUTHOR);
    final MainSessionController mainController = mock(MainSessionController.class);
    when(mainController.getFavoriteLanguage()).thenReturn("fr");
    when(mainController.getCurrentUserDetail()).thenReturn(author);
    final ComponentContext context = mock(ComponentContext.class);
    when(context.getCurrentComponentName()).thenReturn("questionReply");
    when(context.getCurrentComponentId()).thenReturn(COMPONENT_ID);
    when(context.getCurrentProfile()).thenReturn(new String[]{"admin"});
    controller = new QuestionReplySessionController(mainController, context, BUNDLE, null);

    final Question question = new Question(AUTHOR, COMPONENT_ID);
    question.getPK().setId(String.valueOf(QUESTION_ID));
    when(questionManager.getQuestion(QUESTION_ID)).thenReturn(question);
    when(questionManager.createQuestionReply(any(Question.class), any(Reply.class), any()))
        .thenReturn(QUESTION_ID);
  }

  @AfterEach
  void clearRequestContext() {
    CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
  }

  @Test
  void theClassificationOfANewQuestionDoesNotAlertTheSubscribersOnThePdc() {
    controller.classifyQuestionReply(QUESTION_ID, A_POSITION_ON_THE_PDC);

    verify(pdcClassificationService).classifyContent(any(QuestionDetail.class),
        any(PdcClassification.class), eq(false));
    verify(pdcClassificationService, never()).classifyContent(any(), any(), eq(true));
  }

  @Test
  void aNewQuestionWithoutAnyPositionIsNotClassified() {
    controller.classifyQuestionReply(QUESTION_ID, "");

    verify(pdcClassificationService, never()).classifyContent(any(), any(), anyBoolean());
  }

  /**
   * The question has to be classified before the subscribers are notified about its reply: this
   * is done by the creation of the FAQ and not, afterward, by the controller.
   */
  @Test
  void thePositionsOfANewFaqAreGivenToItsCreation() throws Exception {
    aNewFaqIsPrepared();

    final long questionId = controller.saveNewFAQ(List.of(), A_POSITION_ON_THE_PDC);

    assertThat(questionId, is(QUESTION_ID));
    assertThat(positionsGivenToTheCreationOfTheFaq(), hasSize(1));
    verify(pdcClassificationService, never()).classifyContent(any(), any(), anyBoolean());
  }

  @Test
  void aNewFaqWithoutAnyPositionIsCreatedWithoutClassification() throws Exception {
    aNewFaqIsPrepared();

    controller.saveNewFAQ(List.of(), "");

    assertThat(positionsGivenToTheCreationOfTheFaq(), is(empty()));
  }

  private void aNewFaqIsPrepared() {
    controller.getNewQuestion();
    controller.setNewQuestionContent("A question", "The content of a question", null);
    controller.getNewReply();
    controller.setNewReplyContent("A reply", "The content of a reply", 1, 0);
  }

  @SuppressWarnings("unchecked")
  private List<PdcPosition> positionsGivenToTheCreationOfTheFaq() throws Exception {
    final ArgumentCaptor<List<PdcPosition>> positions = ArgumentCaptor.forClass(List.class);
    verify(questionManager).createQuestionReply(any(Question.class), any(Reply.class),
        positions.capture());
    return positions.getValue();
  }
}
