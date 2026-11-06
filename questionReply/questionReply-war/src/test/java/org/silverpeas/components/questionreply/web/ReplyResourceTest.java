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
import org.silverpeas.components.questionreply.model.Reply;
import org.silverpeas.components.questionreply.service.QuestionManager;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.web.rs.RESTRequestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import jakarta.ws.rs.WebApplicationException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests on the access to the replies of the questions of a given FAQ through the REST API:
 * the replies to a question of another FAQ must never be served.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class ReplyResourceTest {

  private static final String FAQ_ID = "questionReply5";
  private static final String OTHER_FAQ_ID = "questionReply2";

  @TestManagedMock
  private OrganizationController organizationController;
  @TestManagedMock
  private QuestionManager questionManager;
  @TestedBean
  private ReplyResource resource;

  private ReplyResource rest;

  @BeforeEach
  void setUp() {
    resource.componentId = FAQ_ID;
    rest = spy(resource);
    User user = mock(User.class);
    when(user.getId()).thenReturn("7");
    RESTRequestContext context = mock(RESTRequestContext.class);
    when(context.getUser()).thenReturn(user);
    doReturn(context).when(rest).getSilverpeasContext();
    doReturn(SilverpeasRole.ADMIN).when(rest).getUserProfile();
    doReturn(List.of()).when(rest).asWebEntities(anyList(), any());
  }

  @Test
  void getAllTheRepliesToAQuestionOfTheFAQ() throws Exception {
    when(questionManager.getQuestion(1, FAQ_ID)).thenReturn(aQuestion(1, FAQ_ID));
    Question question = aQuestion(1, FAQ_ID);
    Reply privateReply = aReply(question, 0);
    Reply publicReply = aReply(question, 1);
    when(questionManager.getAllReplies(1, FAQ_ID)).thenReturn(List.of(privateReply, publicReply));

    rest.getAllRepliesForQuestion("1");

    assertThat(servedReplies(), contains(privateReply, publicReply));
  }

  @Test
  void getAllTheRepliesToAQuestionOfAnotherFAQIsNotFound() throws Exception {
    when(questionManager.getQuestion(2, OTHER_FAQ_ID)).thenReturn(aQuestion(2, OTHER_FAQ_ID));

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> rest.getAllRepliesForQuestion("2"));

    assertThat(ex.getResponse().getStatus(), is(404));
    verify(questionManager, never()).getAllReplies(anyLong(), anyString());
  }

  @Test
  void getAllTheRepliesToAnUnknownQuestionIsNotFound() throws Exception {
    when(questionManager.getQuestion(2, FAQ_ID)).thenReturn(null);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> rest.getAllRepliesForQuestion("2"));

    assertThat(ex.getResponse().getStatus(), is(404));
  }

  @Test
  void getThePublicRepliesToAQuestionOfTheFAQ() throws Exception {
    when(questionManager.getQuestion(1, FAQ_ID)).thenReturn(aQuestion(1, FAQ_ID));
    Reply publicReply = aReply(aQuestion(1, FAQ_ID), 1);
    when(questionManager.getQuestionPublicReplies(1, FAQ_ID)).thenReturn(List.of(publicReply));

    rest.getPublicRepliesForQuestion("1");

    assertThat(servedReplies(), contains(publicReply));
  }

  @Test
  void getThePublicRepliesToAQuestionOfAnotherFAQIsNotFound() throws Exception {
    when(questionManager.getQuestion(2, OTHER_FAQ_ID)).thenReturn(aQuestion(2, OTHER_FAQ_ID));

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> rest.getPublicRepliesForQuestion("2"));

    assertThat(ex.getResponse().getStatus(), is(404));
    verify(questionManager, never()).getQuestionPublicReplies(anyLong(), anyString());
  }

  @SuppressWarnings("unchecked")
  private List<Reply> servedReplies() {
    ArgumentCaptor<List<Reply>> captor = ArgumentCaptor.forClass(List.class);
    verify(rest).asWebEntities(captor.capture(), any());
    return captor.getValue();
  }

  private static Question aQuestion(int id, String instanceId) {
    Question question = new Question("0", instanceId);
    question.setId(String.valueOf(id));
    return question;
  }

  private static Reply aReply(Question question, int publicReply) {
    Reply reply = new Reply(question, "0");
    reply.setPublicReply(publicReply);
    reply.setPrivateReply(1 - publicReply);
    return reply;
  }
}
