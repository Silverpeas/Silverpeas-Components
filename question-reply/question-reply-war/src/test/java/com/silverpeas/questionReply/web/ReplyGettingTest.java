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
import com.silverpeas.questionReply.control.QuestionManager;
import com.silverpeas.questionReply.model.Question;
import com.silverpeas.questionReply.model.Reply;
import com.silverpeas.rest.RESTWebServiceTest;
import com.silverpeas.rest.mock.UserDetailWithProfiles;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.persistence.IdPK;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import java.util.ArrayList;
import java.util.List;
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
public class ReplyGettingTest extends RESTWebServiceTest {

  @Inject
  private MockableQuestionManager questionManager;
  @Inject
  private MockablePersonalizationService personalisationService;
  protected static final String COMPONENT_INSTANCE_ID = "questionReply12";
  protected static final String RESOURCE_PATH = "questionreply/" + COMPONENT_INSTANCE_ID + "/replies";

  public ReplyGettingTest() {
    super("com.silverpeas.questionReply.web", "spring-questionreply-webservice.xml");
  }

  @Test
  public void getAllRepliesByANonAuthenticatedUser() {
    WebResource resource = resource();
    try {
      resource.path(RESOURCE_PATH + "/question/3").accept(MediaType.APPLICATION_JSON).get(
              String.class);
      fail("A non authenticated user shouldn't access the comment");
    } catch (UniformInterfaceException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int unauthorized = Status.UNAUTHORIZED.getStatusCode();
      assertThat(recievedStatus, is(unauthorized));
    }
  }

  @Test
  public void getAllRepliesByAnAuthenticatedUser() throws Exception {
    WebResource resource = resource();

    UserDetailWithProfiles creator = new UserDetailWithProfiles();
    creator.setFirstName("Lisa");
    creator.setLastName("Simpson");
    creator.setId("1");
    creator.addProfile(COMPONENT_INSTANCE_ID, SilverpeasRole.publisher);
    String creatorSessionKey = authenticate(creator);


    UserDetailWithProfiles publisher = new UserDetailWithProfiles();
    publisher.setFirstName("Maud");
    publisher.setLastName("Simpson");
    publisher.setId("5");
    publisher.addProfile(COMPONENT_INSTANCE_ID, SilverpeasRole.publisher);
    String publisherSessionKey = authenticate(publisher);

    UserDetailWithProfiles user = new UserDetailWithProfiles();
    user.setFirstName("Bart");
    user.setLastName("Simpson");
    user.setId("10");
    user.addProfile(COMPONENT_INSTANCE_ID, SilverpeasRole.writer);
    String sessionKey = authenticate(user);
    personalisationService.setPersonalizationService(mock(PersonalizationService.class));
    QuestionManager mockedQuestionManager = mock(QuestionManager.class);
    Question question = getNewSimpleQuestion(3);
    when(mockedQuestionManager.getQuestion(3L)).thenReturn(question);
    List<Reply> replies = getPrivateAndPublicReplies(3L);
    when(mockedQuestionManager.getAllReplies(3L, COMPONENT_INSTANCE_ID)).thenReturn(replies);
    questionManager.setQuestionManager(mockedQuestionManager);
    ReplyEntity[] entities = resource.path(RESOURCE_PATH + "/question/3").header(HTTP_SESSIONKEY,
            sessionKey).accept(MediaType.APPLICATION_JSON).get(ReplyEntity[].class);
    assertNotNull(entities);
    assertThat(entities.length, is(2));
    assertThat(entities[0], ReplyEntityMatcher.matches(replies.get(0)));
    assertThat(entities[1], ReplyEntityMatcher.matches(replies.get(1)));
    entities = resource.path(RESOURCE_PATH + "/question/3").header(HTTP_SESSIONKEY,
            creatorSessionKey).
            accept(MediaType.APPLICATION_JSON).get(ReplyEntity[].class);
    assertNotNull(entities);
    assertThat(entities.length, is(2));
    assertThat(entities[0], ReplyEntityMatcher.matches(replies.get(0)));
    assertThat(entities[1], ReplyEntityMatcher.matches(replies.get(1)));
    entities = resource.path(RESOURCE_PATH + "/question/3").header(HTTP_SESSIONKEY,
            publisherSessionKey).
            accept(MediaType.APPLICATION_JSON).get(ReplyEntity[].class);
    assertNotNull(entities);
    assertThat(entities.length, is(1));
    assertThat(entities[0], ReplyEntityMatcher.matches(replies.get(0)));
  }

  /* @Test
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
  }*/
  private Question getNewSimpleQuestion(int id) {
    Question question = new Question("1", COMPONENT_INSTANCE_ID);
    question.setPK(new IdPK(id));
    question.getPK().setId(COMPONENT_INSTANCE_ID);
    question.setTitle("Test");
    question.setContent("Hello world question");
    question.setCategoryId("");
    return question;
  }

  private List<Reply> getPrivateAndPublicReplies(long questionId) {
    List<Reply> replies = new ArrayList<Reply>();
    Reply reply = new Reply(questionId, "5");
    reply.setPK(new IdPK(100));
    reply.setTitle("Public reply");
    reply.writeWysiwygContent("This reply content should be visible for all");
    reply.setCreationDate("2011/06/03");
    reply.setContent("");
    reply.setPrivateReply(0);
    reply.setPublicReply(1);
    replies.add(reply);

    reply = new Reply(questionId, "6");
    reply.setPK(new IdPK(101));
    reply.setTitle("Private reply");
    reply.writeWysiwygContent(
            "This reply content should be visible for writers and question creator only");
    reply.setCreationDate("2011/06/03");
    reply.setPrivateReply(01);
    reply.setPublicReply(0);
    replies.add(reply);
    return replies;
  }
}
