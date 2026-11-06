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
package org.silverpeas.components.questionreply.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.components.questionreply.service.QuestionManager;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.persistence.jdbc.bean.IdPK;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.web.WebResourceUri;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Unit tests on the access to the questions of a given FAQ through the REST API: the questions of
 * another FAQ must never be served.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class QuestionResourceTest {

  private static final String FAQ_ID = "questionReply5";
  private static final String OTHER_FAQ_ID = "questionReply2";

  @TestManagedMock
  private OrganizationController organizationController;
  @TestManagedMock
  private QuestionManager questionManager;
  @TestedBean
  private QuestionResource resource;

  private QuestionResource rest;

  @BeforeEach
  void setUp() {
    resource.componentId = FAQ_ID;
    rest = spy(resource);
    doReturn(SilverpeasRole.PUBLISHER).when(rest).getUserProfile();
    doReturn(mock(WebResourceUri.class, RETURNS_DEEP_STUBS)).when(rest).getUri();
    doReturn(List.of()).when(rest).asWebEntities(anyList());
  }

  @Test
  void getAQuestionOfTheFAQ() throws Exception {
    Question question = aQuestion(1, FAQ_ID);
    when(questionManager.getQuestion(1)).thenReturn(question);
    QuestionEntity expected = mock(QuestionEntity.class);
    doReturn(expected).when(rest).asWebEntity(any(Question.class), any());

    QuestionEntity actual = rest.getQuestion("1");

    assertThat(actual, sameInstance(expected));
    verify(rest).asWebEntity(eq(question), any());
  }

  @Test
  void getAQuestionOfAnotherFAQIsNotFound() throws Exception {
    when(questionManager.getQuestion(1)).thenReturn(aQuestion(1, OTHER_FAQ_ID));

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> rest.getQuestion("1"));

    assertThat(ex.getResponse().getStatus(), is(404));
    verify(rest, never()).asWebEntity(any(Question.class), any());
  }

  @Test
  void getAnUnknownQuestionIsNotFound() throws Exception {
    when(questionManager.getQuestion(1)).thenReturn(null);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> rest.getQuestion("1"));

    assertThat(ex.getResponse().getStatus(), is(404));
  }

  @Test
  void getQuestionsByIdsServesOnlyTheQuestionsOfTheFAQ() throws Exception {
    Question ownQuestion = aQuestion(3, FAQ_ID);
    when(questionManager.getQuestionsByIds(anyList())).thenReturn(
        List.of(aQuestion(1, OTHER_FAQ_ID), ownQuestion, aQuestion(2, OTHER_FAQ_ID)));

    rest.getQuestions(Set.of("1", "2", "3"));

    assertThat(servedQuestions(), contains(ownQuestion));
  }

  @SuppressWarnings("unchecked")
  private List<Question> servedQuestions() {
    ArgumentCaptor<List<Question>> captor = ArgumentCaptor.forClass(List.class);
    verify(rest).asWebEntities(captor.capture());
    return captor.getValue();
  }

  private static Question aQuestion(int id, String instanceId) {
    Question question = new Question("0", instanceId);
    question.setPK(new IdPK(id));
    return question;
  }
}
