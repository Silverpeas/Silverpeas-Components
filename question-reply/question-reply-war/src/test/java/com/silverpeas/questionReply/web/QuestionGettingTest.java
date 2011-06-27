/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.questionReply.web;

import com.silverpeas.personalization.service.MockablePersonalizationService;
import com.silverpeas.personalization.service.PersonalizationService;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.silverpeas.questionReply.control.QuestionManager;
import com.silverpeas.questionReply.model.Question;
import com.silverpeas.rest.RESTWebServiceTest;
import com.silverpeas.rest.mock.UserDetailWithProfiles;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.persistence.IdPK;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static com.silverpeas.rest.RESTWebService.*;

/**
 * Tests on the comment getting by the CommentResource web service.
 */
public class QuestionGettingTest extends RESTWebServiceTest {

  @Inject
  private MockableQuestionManager questionManager;
  @Inject
  private MockablePersonalizationService personalisationService;
  protected static final String COMPONENT_INSTANCE_ID = "questionReply12";
  protected static final String RESOURCE_PATH = "questionreply/" + COMPONENT_INSTANCE_ID + "/questions";

  public QuestionGettingTest() {
    super("com.silverpeas.questionReply.web", "spring-questionreply-webservice.xml");
  }

  @Test
  public void getAQuestionByANonAuthenticatedUser() {
    WebResource resource = resource();
    try {
      resource.path(RESOURCE_PATH + "/3").accept(MediaType.APPLICATION_JSON).get(String.class);
      fail("A non authenticated user shouldn't access the comment");
    } catch (UniformInterfaceException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int unauthorized = Status.UNAUTHORIZED.getStatusCode();
      assertThat(recievedStatus, is(unauthorized));
    }
  }

  @Test
  public void getAQuestionByAnAuthenticatedUser() throws Exception {
    WebResource resource = resource();

    UserDetail creator = new UserDetail();
    creator.setFirstName("Lisa");
    creator.setLastName("Simpson");
    creator.setId("1");
    authenticate(creator);

    UserDetailWithProfiles user = new UserDetailWithProfiles();
    user.setFirstName("Bart");
    user.setLastName("Simpson");
    user.setId("10");
    user.addProfile(COMPONENT_INSTANCE_ID, SilverpeasRole.writer);
    user.addProfile(COMPONENT_INSTANCE_ID, SilverpeasRole.user);
    String sessionKey = authenticate(user);
    personalisationService.setPersonalizationService(mock(PersonalizationService.class));
    QuestionManager mockedQuestionManager = mock(QuestionManager.class);
    Question question = getNewSimpleQuestion(3);
    when(mockedQuestionManager.getQuestion(3L)).thenReturn(question);
    questionManager.setQuestionManager(mockedQuestionManager);
    QuestionEntity entity = resource.path(RESOURCE_PATH + "/3").header(HTTP_SESSIONKEY, sessionKey).
        accept(
        MediaType.APPLICATION_JSON).get(QuestionEntity.class);
    assertNotNull(entity);
    assertThat(entity, QuestionEntityMatcher.matches(question));
    assertThat(entity.getCreator(), is(notNullValue()));
    assertThat(entity.getCreator().getFullName(), is("Lisa Simpson"));
  }

  @Test
  public void getAnInvisibleQuestionByAnAuthenticatedUser() throws Exception {
    WebResource resource = resource();

    UserDetail creator = new UserDetail();
    creator.setFirstName("Lisa");
    creator.setLastName("Simpson");
    creator.setId("1");
    authenticate(creator);

    UserDetailWithProfiles user = new UserDetailWithProfiles();
    user.setFirstName("Bart");
    user.setLastName("Simpson");
    user.setId("10");
    user.addProfile(COMPONENT_INSTANCE_ID, SilverpeasRole.user);
    String sessionKey = authenticate(user);
    personalisationService.setPersonalizationService(mock(PersonalizationService.class));
    QuestionManager mockedQuestionManager = mock(QuestionManager.class);
    Question question = getNewSimpleQuestion(3);
    question.waitForAnswer();
    when(mockedQuestionManager.getQuestion(3L)).thenReturn(question);
    questionManager.setQuestionManager(mockedQuestionManager);
    try {
      resource.path(RESOURCE_PATH + "/3").header(HTTP_SESSIONKEY, sessionKey).
          accept(MediaType.APPLICATION_JSON).get(QuestionEntity.class);
      fail("A non authenticated user shouldn't access the comment");
    } catch (UniformInterfaceException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int unauthorized = Status.FORBIDDEN.getStatusCode();
      assertThat(recievedStatus, is(unauthorized));
    }
  }

  private Question getNewSimpleQuestion(int id) {
    Question question = new Question("1", COMPONENT_INSTANCE_ID);
    question.setPK(new IdPK(id));
    question.getPK().setId(COMPONENT_INSTANCE_ID);
    question.setTitle("Test");
    question.setContent("Hello world question");
    question.setCategoryId("");
    return question;
  }
}
