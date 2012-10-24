/*
 * Copyright (C) 2000 - 2012 Silverpeas <p/> This program is free software: you can redistribute
 * it and/or modify it under the terms of the GNU Affero General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version. <p/> As a special exception to the terms and conditions of version 3.0 of the GPL, you
 * may redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of
 * the text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html" <p/> This program is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * <p/> You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.questionReply.web;

import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.personalization.service.PersonalizationService;
import com.silverpeas.questionReply.control.QuestionManager;
import com.silverpeas.questionReply.model.Question;
import com.silverpeas.questionReply.model.Reply;
import static com.silverpeas.questionReply.web.QuestionReplyTestResources.COMPONENT_INSTANCE_ID;
import static com.silverpeas.questionReply.web.QuestionReplyTestResources.REPLY_RESOURCE_PATH;
import com.silverpeas.web.RESTWebServiceTest;
import static com.silverpeas.web.UserPriviledgeValidation.HTTP_SESSIONKEY;
import com.silverpeas.web.mock.UserDetailWithProfiles;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.util.attachment.control.AttachmentBm;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.anyString;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests on the comment getting by the CommentResource web service.
 */
public class ReplyGettingTest extends RESTWebServiceTest<QuestionReplyTestResources> {
  private UserDetailWithProfiles creator;
  private String creatorSessionKey;

  public ReplyGettingTest() {
    super("com.silverpeas.questionReply.web", "spring-questionreply-webservice.xml");
  }

  @Test
  public void getAllRepliesByANonAuthenticatedUser() {
    WebResource resource = resource();
    try {
      resource.path(REPLY_RESOURCE_PATH + "/question/3").accept(MediaType.APPLICATION_JSON).get(
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
    AttachmentBm mockAttachmentBm = mock(AttachmentBm.class);
    when(mockAttachmentBm.getAttachmentsByPKAndContext(Mockito.any(AttachmentPK.class),
            Mockito.matches("Images"), Mockito.any(java.sql.Connection.class))).thenReturn(
            new Vector<AttachmentDetail>());
    AttachmentBm oldBm = AttachmentController.changeAttachmentControllerForTests(mockAttachmentBm);
    try {
      WebResource resource = resource();

      UserDetailWithProfiles publisher = getTestResources().aUserNamed("Maud", "Simpson");
      publisher.addProfile(COMPONENT_INSTANCE_ID, SilverpeasRole.publisher);
      String publisherSessionKey = authenticate(publisher);

      UserDetailWithProfiles user = getTestResources().aUserNamed("Bart", "Simpson");
      user.addProfile(COMPONENT_INSTANCE_ID, SilverpeasRole.writer);
      String sessionKey = authenticate(user);
      
      QuestionManager mockedQuestionManager = mock(QuestionManager.class);
      Question question = getNewSimpleQuestion(creator.getId(), 3);
      when(mockedQuestionManager.getQuestion(3L)).thenReturn(question);
      List<Reply> replies = getPrivateAndPublicReplies(publisher.getId(), 3L);
      when(mockedQuestionManager.getAllReplies(3L, COMPONENT_INSTANCE_ID)).thenReturn(replies);
      getTestResources().getMockableQuestionManager().setQuestionManager(mockedQuestionManager);
      ReplyEntity[] entities = resource.path(REPLY_RESOURCE_PATH + "/question/3").header(
              HTTP_SESSIONKEY,
              sessionKey).accept(MediaType.APPLICATION_JSON).get(ReplyEntity[].class);
      assertNotNull(entities);
      assertThat(entities.length, is(2));
      assertThat(entities[0], ReplyEntityMatcher.matches(replies.get(0)));
      assertThat(entities[1], ReplyEntityMatcher.matches(replies.get(1)));
      entities = resource.path(REPLY_RESOURCE_PATH + "/question/3").header(HTTP_SESSIONKEY,
              creatorSessionKey).accept(MediaType.APPLICATION_JSON).get(ReplyEntity[].class);
      assertNotNull(entities);
      assertThat(entities.length, is(2));
      assertThat(entities[0], ReplyEntityMatcher.matches(replies.get(0)));
      assertThat(entities[1], ReplyEntityMatcher.matches(replies.get(1)));
      entities = resource.path(REPLY_RESOURCE_PATH + "/question/3").header(HTTP_SESSIONKEY,
              publisherSessionKey).accept(MediaType.APPLICATION_JSON).get(ReplyEntity[].class);
      assertNotNull(entities);
      assertThat(entities.length, is(1));
      assertThat(entities[0], ReplyEntityMatcher.matches(replies.get(0)));
    } finally {
      AttachmentController.changeAttachmentControllerForTests(oldBm);
    }
  }

  @Test
  public void getAnInvisibleQuestionByAnAuthenticatedUser() throws Exception {
    WebResource resource = resource();

    UserDetailWithProfiles user = getTestResources().aUserNamed("Bart", "Simpson");
    user.addProfile(COMPONENT_INSTANCE_ID, SilverpeasRole.user);
    String sessionKey = authenticate(user);
    
    QuestionManager mockedQuestionManager = mock(QuestionManager.class);
    Question question = getNewSimpleQuestion(creator.getId(), 3);
    question.waitForAnswer();
    when(mockedQuestionManager.getQuestion(3L)).thenReturn(question);
    getTestResources().getMockableQuestionManager().setQuestionManager(mockedQuestionManager);
    try {
      resource.path(REPLY_RESOURCE_PATH + "/question/3").header(HTTP_SESSIONKEY, sessionKey).
              accept(MediaType.APPLICATION_JSON).get(ReplyEntity[].class);
    } catch (UniformInterfaceException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int unauthorized = Status.FORBIDDEN.getStatusCode();
      assertThat(recievedStatus, is(unauthorized));
    }
  }

  private Question getNewSimpleQuestion(String creatorId, int id) {
    Question question = new Question(creatorId, COMPONENT_INSTANCE_ID);
    question.setPK(new IdPK(id));
    question.getPK().setId(COMPONENT_INSTANCE_ID);
    question.setTitle("Test");
    question.setContent("Hello world question");
    question.setCategoryId("");
    return question;
  }

  private List<Reply> getPrivateAndPublicReplies(String creatorId, long questionId) {
    List<Reply> replies = new ArrayList<Reply>();
    Reply reply = new Reply(questionId, creatorId);
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

  @Before
  public void preparePersonalizationAndQuestionCreator() {
    PersonalizationService myPersonalizationService = getTestResources().getPersonalizationServiceMock();
    UserPreferences prefs = mock(UserPreferences.class);
    when(prefs.getLanguage()).thenReturn("en");
    when(myPersonalizationService.getUserSettings(anyString())).thenReturn(prefs);

    creator = getTestResources().aUserNamed("Lisa", "Simpson");
    creator.addProfile(COMPONENT_INSTANCE_ID, SilverpeasRole.publisher);
    creatorSessionKey = authenticate(creator);
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{COMPONENT_INSTANCE_ID};
  }
}
