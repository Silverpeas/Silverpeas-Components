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
package org.silverpeas.components.survey.control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.components.survey.SurveyException;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerDetail;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerHeader;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerPK;
import org.silverpeas.core.questioncontainer.container.service.QuestionContainerService;
import org.silverpeas.core.questioncontainer.question.model.Question;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the check of the answer type of the questions when creating a survey or a poll.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class SurveyCreationTest {

  private static final String SURVEY_ID = "13";

  private final SurveySessionController controller = mock(SurveySessionController.class);
  private final QuestionContainerService questionContainerService =
      mock(QuestionContainerService.class);

  @BeforeEach
  void setUp() throws Exception {
    when(controller.getQuestionContainerService()).thenReturn(questionContainerService);
    when(controller.getSpaceId()).thenReturn("WA1");
    when(controller.getComponentId()).thenReturn("pollingStation2");
    when(controller.getUserId()).thenReturn("1");
    when(questionContainerService.createQuestionContainer(any(QuestionContainerPK.class),
        any(QuestionContainerDetail.class), anyString()))
        .thenReturn(new QuestionContainerPK(SURVEY_ID, "WA1", "pollingStation2"));
    doCallRealMethod().when(controller).getListQuestionStyle();
    doCallRealMethod().when(controller).createSurvey(any(QuestionContainerDetail.class));
  }

  @Test
  void aPollWithoutAnswerTypeIsRejected() throws Exception {
    when(controller.isPollingStationMode()).thenReturn(true);
    QuestionContainerDetail poll = surveyWithAQuestionOfStyle("null");

    assertThrows(SurveyException.class, () -> controller.createSurvey(poll));
    verify(questionContainerService, never()).createQuestionContainer(any(), any(), any());
  }

  @Test
  void aPollWithAnOpenQuestionIsRejected() throws Exception {
    when(controller.isPollingStationMode()).thenReturn(true);
    QuestionContainerDetail poll = surveyWithAQuestionOfStyle("open");

    assertThrows(SurveyException.class, () -> controller.createSurvey(poll));
    verify(questionContainerService, never()).createQuestionContainer(any(), any(), any());
  }

  @Test
  void aPollWithAValidAnswerTypeIsCreated() throws Exception {
    when(controller.isPollingStationMode()).thenReturn(true);
    QuestionContainerDetail poll = surveyWithAQuestionOfStyle("radio");

    QuestionContainerPK pk = controller.createSurvey(poll);

    assertThat(pk.getId(), is(SURVEY_ID));
    verify(questionContainerService).createQuestionContainer(any(), any(), any());
  }

  @Test
  void aSurveyWithAnOpenQuestionIsCreated() throws Exception {
    when(controller.isPollingStationMode()).thenReturn(false);
    QuestionContainerDetail survey = surveyWithAQuestionOfStyle("open");

    QuestionContainerPK pk = controller.createSurvey(survey);

    assertThat(pk.getId(), is(SURVEY_ID));
    verify(questionContainerService).createQuestionContainer(any(), any(), any());
  }

  @Test
  void aSurveyWithAQuestionWithoutAnswerTypeIsRejected() throws Exception {
    when(controller.isPollingStationMode()).thenReturn(false);
    QuestionContainerDetail survey = surveyWithAQuestionOfStyle(null);

    assertThrows(SurveyException.class, () -> controller.createSurvey(survey));
    verify(questionContainerService, never()).createQuestionContainer(any(), any(), any());
  }

  private static QuestionContainerDetail surveyWithAQuestionOfStyle(final String style) {
    QuestionContainerHeader header =
        new QuestionContainerHeader(null, "petit dej", "", null, null, null, null, false, 0, 1,
            false, QuestionContainerHeader.IMMEDIATE_RESULTS,
            QuestionContainerHeader.TWICE_DISPLAY_RESULTS);
    Question question = new Question(null, null, "Croissant ?", "", "", null, style, 0);
    return new QuestionContainerDetail(header, List.of(question), null, null);
  }
}
