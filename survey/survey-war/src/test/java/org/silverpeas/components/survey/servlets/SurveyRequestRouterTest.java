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
package org.silverpeas.components.survey.servlets;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.components.survey.SurveyException;
import org.silverpeas.components.survey.control.SurveySessionController;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the routing of the requests about the creation of a survey.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class SurveyRequestRouterTest {

  private static final String SURVEY_DETAIL = "surveyDetail.jsp";
  private static final String SURVEY_UPDATE = "/survey/jsp/surveyUpdate.jsp";
  private static final String SURVEY_ID = "13";

  private final SurveyRequestRouter router = new SurveyRequestRouter();
  private final SurveySessionController controller = mock(SurveySessionController.class);
  private final HttpServletRequest servletRequest = mock(HttpServletRequest.class);
  private final HttpRequest request = HttpRequest.decorate(servletRequest);

  @BeforeEach
  void setUp() throws SurveyException {
    when(controller.getUserRoles()).thenReturn(new String[]{"admin"});
    when(servletRequest.getCookies()).thenReturn(new Cookie[0]);
    when(controller.createSurveyUnderConstruction()).thenReturn(SURVEY_ID);
  }

  @Test
  void submitAndUpdateSurveyCreatesTheSurveyAndForwardsToItsUpdate() throws SurveyException {
    when(servletRequest.getParameter("Action")).thenReturn("SubmitAndUpdateSurvey");

    String destination = router.getDestination(SURVEY_DETAIL, controller, request);

    assertThat(destination, is(SURVEY_UPDATE));
    verify(controller).createSurveyUnderConstruction();
    verify(servletRequest).setAttribute("Action", "UpdateSurveyHeader");
    verify(servletRequest).setAttribute("SurveyId", SURVEY_ID);
  }

  @Test
  void updateSurveyForwardsToTheUpdateOfTheRequestedSurvey() {
    when(servletRequest.getParameter("SurveyId")).thenReturn(SURVEY_ID);

    String destination = router.getDestination("UpdateSurvey", controller, request);

    assertThat(destination, is(SURVEY_UPDATE));
    verify(servletRequest).setAttribute("Action", "UpdateSurveyHeader");
    verify(servletRequest).setAttribute("SurveyId", SURVEY_ID);
  }

  @Test
  void otherActionsOnSurveyDetailDoNotCreateAnySurvey() throws SurveyException {
    when(servletRequest.getParameter("Action")).thenReturn("PreviewSurvey");

    String destination = router.getDestination(SURVEY_DETAIL, controller, request);

    assertThat(destination, is("/survey/jsp/" + SURVEY_DETAIL));
    verify(controller, never()).createSurveyUnderConstruction();
  }
}
