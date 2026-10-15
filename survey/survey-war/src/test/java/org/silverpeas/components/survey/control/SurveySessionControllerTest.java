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
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerPK;
import org.silverpeas.core.questioncontainer.question.model.Question;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the creation of the survey under construction in the user session.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class SurveySessionControllerTest {

  private static final String SURVEY_ID = "13";

  private final SurveySessionController controller = mock(SurveySessionController.class);
  private final QuestionContainerDetail surveyUnderConstruction = new QuestionContainerDetail();
  private final List<Question> questions = new ArrayList<>();

  @BeforeEach
  void setUp() throws SurveyException {
    questions.add(new Question(null, null, "What?", "", "", null, "list", 0));
    questions.add(new Question(null, null, "Why?", "", "", null, "open", 0));
    when(controller.getSessionSurveyUnderConstruction()).thenReturn(surveyUnderConstruction);
    when(controller.getSessionQuestions()).thenReturn(questions);
    when(controller.createSurvey(surveyUnderConstruction))
        .thenReturn(new QuestionContainerPK(SURVEY_ID, "WA1", "survey2"));
    doCallRealMethod().when(controller).createSurveyUnderConstruction();
  }

  @Test
  void createSurveyUnderConstructionWithTheQuestionsInSession() throws SurveyException {
    String surveyId = controller.createSurveyUnderConstruction();

    assertThat(surveyId, is(SURVEY_ID));
    assertThat(surveyUnderConstruction.getQuestions(),
        contains(questions.toArray(new Question[0])));
    verify(controller).createSurvey(surveyUnderConstruction);
  }

  @Test
  void createSurveyUnderConstructionClearsTheConstructionContext() throws SurveyException {
    controller.createSurveyUnderConstruction();

    verify(controller).removeSessionSurveyUnderConstruction();
  }
}
