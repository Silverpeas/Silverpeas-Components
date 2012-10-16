/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.questionReply.control;

import com.google.common.collect.Lists;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;
import com.silverpeas.components.model.AbstractTestDao;
import com.silverpeas.questionReply.model.Question;
import com.silverpeas.questionReply.model.QuestionMatcher;
import com.silverpeas.questionReply.model.Reply;
import com.silverpeas.questionReply.model.ReplyMatcher;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.webactiv.persistence.IdPK;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.silverpeas.attachment.AttachmentService;
import org.silverpeas.attachment.mock.SimpleDocumentServiceWrapper;
import org.silverpeas.attachment.model.SimpleDocument;

import com.silverpeas.util.ForeignPK;
import com.stratelia.webactiv.util.WAPrimaryKey;
import static org.mockito.Mockito.*;

/**
 *
 * @author ehugonnet
 */
public class QuestionManagerTest extends AbstractTestDao {

  private ApplicationContext context;

  public QuestionManagerTest() {
    context = new ClassPathXmlApplicationContext("spring-questionreply.xml");
  }

  private SimpleDocumentServiceWrapper getAttachmentService() {
    return (SimpleDocumentServiceWrapper) context.getBean("simpleDocumentService");
  }

  /**
   * Test of getInstance method, of class QuestionManager.
   */
  public void testGetInstance() {
    QuestionManager expResult = QuestionManagerFactory.getQuestionManager();
    assertNotNull(expResult);
    QuestionManager result = QuestionManagerFactory.getQuestionManager();
    assertEquals(expResult, result);
  }

  /**
   * Test of createQuestion method, of class QuestionManager.
   */
  /*
   public void testCreateQuestion() throws Exception {
   System.out.println("createQuestion");
   QuestionManager instance = QuestionManager.getInstance();
   Question question = new Question("5", INSTANCE_ID);
   assertEquals(1, getConnection().getRowCount(question._getTableName()));
   question.setTitle(RandomGenerator.getRandomString());
   long expResult = 0L;
   long result = instance.createQuestion(question);
   assertEquals(expResult, result);
   assertEquals(2, getConnection().getRowCount(question._getTableName()));
   }*/
  /**
   * Test of deleteQuestionIndex method, of class QuestionManager.
   */
  /*
   public void testDeleteQuestionIndex() {
   System.out.println("deleteQuestionIndex");
   Question question = null;
   QuestionManager instance = null;
   instance.deleteQuestionIndex(question);
   // TODO review the generated test code and remove the default call to fail.
   fail("The test case is a prototype.");
   }
   */
  /**
   * Test of createReply method, of class QuestionManager.
   */
  /*
   public void testCreateReply() throws Exception {
   System.out.println("createReply");
   Reply reply = null;
   Question question = null;
   QuestionManager instance = null;
   long expResult = 0L;
   long result = instance.createReply(reply, question);
   assertEquals(expResult, result);
   // TODO review the generated test code and remove the default call to fail.
   fail("The test case is a prototype.");
   }*/
  /**
   * Test of createReplyIndex method, of class QuestionManager.
   */
  /*
   public void testCreateReplyIndex() {
   System.out.println("createReplyIndex");
   Reply reply = null;
   QuestionManager instance = null;
   instance.createReplyIndex(reply);
   // TODO review the generated test code and remove the default call to fail.
   fail("The test case is a prototype.");
   }*/
  /**
   * Test of deleteReplyIndex method, of class QuestionManager.
   */
  /*
   public void testDeleteReplyIndex() {
   System.out.println("deleteReplyIndex");
   Reply reply = null;
   QuestionManager instance = null;
   instance.deleteReplyIndex(reply);
   // TODO review the generated test code and remove the default call to fail.
   fail("The test case is a prototype.");
   }*/
  /**
   * Test of closeQuestions method, of class QuestionManager.
   */
  /*
   public void testCloseQuestions() throws Exception {
   System.out.println("closeQuestions");
   Collection questionIds = null;
   QuestionManager instance = null;
   instance.closeQuestions(questionIds);
   // TODO review the generated test code and remove the default call to fail.
   fail("The test case is a prototype.");
   }*/
  /**
   * Test of openQuestions method, of class QuestionManager.
   */
  /*
   public void testOpenQuestions() throws Exception {
   System.out.println("openQuestions");
   Collection questionIds = null;
   QuestionManager instance = null;
   instance.openQuestions(questionIds);
   // TODO review the generated test code and remove the default call to fail.
   fail("The test case is a prototype.");
   }*/
  /**
   * Test of updateQuestionRecipients method, of class QuestionManager.
   */
  /*
   public void testUpdateQuestionRecipients() throws Exception {
   System.out.println("updateQuestionRecipients");
   Question question = null;
   QuestionManager instance = null;
   instance.updateQuestionRecipients(question);
   // TODO review the generated test code and remove the default call to fail.
   fail("The test case is a prototype.");
   }*/
  /**
   * Test of updateQuestionRepliesPublicStatus method, of class QuestionManager.
   */
  /*
   public void testUpdateQuestionRepliesPublicStatus() throws Exception {
   System.out.println("updateQuestionRepliesPublicStatus");
   Collection questionIds = null;
   QuestionManager instance = null;
   instance.updateQuestionRepliesPublicStatus(questionIds);
   // TODO review the generated test code and remove the default call to fail.
   fail("The test case is a prototype.");
   }*/
  /**
   * Test of updateQuestionRepliesPrivateStatus method, of class QuestionManager.
   */
  /*
   public void testUpdateQuestionRepliesPrivateStatus() throws Exception {
   System.out.println("updateQuestionRepliesPrivateStatus");
   Collection questionIds = null;
   QuestionManager instance = null;
   instance.updateQuestionRepliesPrivateStatus(questionIds);
   // TODO review the generated test code and remove the default call to fail.
   fail("The test case is a prototype.");
   }*/
  /**
   * Test of updateRepliesPublicStatus method, of class QuestionManager.
   *
   * @throws Exception
   */
  public void testUpdateRepliesPublicStatusShouldDeleteQuestionWithNoResponse() throws Exception {
    SilverpeasQuestionManager manager = (SilverpeasQuestionManager) QuestionManagerFactory.
        getQuestionManager();
    ContentManager mockContentManager = Mockito.mock(ContentManager.class);
    manager.getContentManager().contentManager = mockContentManager;
    Question question = manager.getQuestion(100L);
    Question question1 = new Question();
    question1.setCategoryId("");
    question1.setContent("la mise en œuvre désigne la création");
    question1.setCreationDate("2010/09/14");
    question1.setCreatorId("0");
    question1.setInstanceId("questionReply12");
    question1.setPK(new IdPK("100"));
    question1.setPrivateReplyNumber(0);
    question1.setPublicReplyNumber(1);
    question1.setReplyNumber(1);
    question1.setStatus(2);
    question1.setTitle("Les accents ça fonctionne ïci ?");
    assertThat(question, is(notNullValue()));
    assertThat(question, QuestionMatcher.matches(question1));
    manager.updateRepliesPublicStatus(Collections.singletonList(200L), question);
    question = manager.getQuestion(100L);
    assertThat(question, is(nullValue()));
    verify(mockContentManager, times(1)).removeSilverContent(Mockito.any(Connection.class), anyInt(), anyString());
  }

  /**
   * Test of updateRepliesPublicStatus method, of class QuestionManager.
   *
   * @throws Exception
   */
  public void testUpdateRepliesPublicStatusShouldHaveNoEffectsOnPrivateReplies() throws Exception {
    SilverpeasQuestionManager manager = (SilverpeasQuestionManager) QuestionManagerFactory.
        getQuestionManager();
    ContentManager mockContentManager = Mockito.mock(ContentManager.class);
    manager.getContentManager().contentManager = mockContentManager;
    Question question = manager.getQuestion(103L);
    Question question0 = new Question();
    question0.setCategoryId("1");
    question0.setContent("This question has only private answers");
    question0.setCreationDate("2010/09/14");
    question0.setCreatorId("0");
    question0.setInstanceId("questionReply12");
    question0.setPK(new IdPK("103"));
    question0.setPrivateReplyNumber(1);
    question0.setPublicReplyNumber(0);
    question0.setReplyNumber(1);
    question0.setStatus(1);
    question0.setTitle("Question with private answers only");
    assertThat(question, is(notNullValue()));
    assertThat(question, QuestionMatcher.matches(question0));
    manager.updateRepliesPublicStatus(Collections.singletonList(203L), question);
    question = manager.getQuestion(103L);
    assertThat(question, is(notNullValue()));
    assertThat(question, QuestionMatcher.matches(question0));
  }

  /**
   * Test of updateRepliesPublicStatus method, of class QuestionManager.
   *
   * @throws Exception
   */
  public void testUpdateRepliesPublicStatus() throws Exception {
    SilverpeasQuestionManager manager = (SilverpeasQuestionManager) QuestionManagerFactory.
        getQuestionManager();
    ContentManager mockContentManager = mock(ContentManager.class);
    manager.getContentManager().contentManager = mockContentManager;
    AttachmentService realService = mock(AttachmentService.class);
    when(realService.searchAttachmentsByExternalObject(Mockito.any(ForeignPK.class), anyString())).
        thenReturn(new ArrayList<SimpleDocument>());
    getAttachmentService().setRealService(realService);
    Question question = manager.getQuestion(101L);
    Question question2 = new Question();
    question2.setCategoryId("3");
    question2.setContent("Les cœurs saignent Et s'accrochent en haut De tes bas");
    question2.setCreationDate("1993/06/14");
    question2.setCreatorId("0");
    question2.setInstanceId("questionReply12");
    question2.setPK(new IdPK("101"));
    question2.setPrivateReplyNumber(1);
    question2.setPublicReplyNumber(1);
    question2.setReplyNumber(2);
    question2.setStatus(1);
    question2.setTitle("Oh Marlène");
    assertThat(question, is(notNullValue()));
    assertThat(question, QuestionMatcher.matches(question2));
    manager.updateRepliesPublicStatus(Lists.asList(201L, new Long[]{202L}), question);
    question = manager.getQuestionAndReplies(101L);
    assertThat(question, is(notNullValue()));
    question2.setPublicReplyNumber(0);
    question2.setReplyNumber(1);
    assertThat(question, QuestionMatcher.matches(question2));
    Mockito.verify(mockContentManager, Mockito.times(0)).removeSilverContent(Mockito.any(
        Connection.class), Mockito.anyInt(), Mockito.anyString());
    List<Reply> replies = question.readReplies();
    Reply reply = new Reply();
    reply.setContent("Et quand ils meurent ou s'endorment C'est la chaleur de ta voix Qui les "
        + "apaise, et les traîne Jusqu'en dehors des combats");
    reply.setCreationDate("1993/11/15");
    reply.setCreatorId("2");
    reply.setQuestionId(101L);
    reply.setPK(new IdPK("202"));
    reply.setPrivateReply(1);
    reply.setPublicReply(0);
    reply.setTitle("Oh Marlène");
    assertThat(replies, is(notNullValue()));
    assertThat(replies, hasSize(1));
    assertThat(replies.get(0), ReplyMatcher.matches(reply));
  }

  /**
   * Test of updateRepliesPrivateStatus method, of class QuestionManager.
   */
  /*
   public void testUpdateRepliesPrivateStatus() throws Exception {
   System.out.println("updateRepliesPrivateStatus");
   Collection replyIds = null;
   Question question = null;
   QuestionManager instance = null;
   instance.updateRepliesPrivateStatus(replyIds, question);
   // TODO review the generated test code and remove the default call to fail.
   fail("The test case is a prototype.");
   }*/
  /**
   * Test of updateQuestion method, of class QuestionManager.
   */
  /*
   public void testUpdateQuestion() throws Exception {
   System.out.println("updateQuestion");
   Question question = null;
   QuestionManager instance = null;
   instance.updateQuestion(question);
   // TODO review the generated test code and remove the default call to fail.
   fail("The test case is a prototype.");
   }*/
  /**
   * Test of updateReply method, of class QuestionManager.
   */
  /*
   public void testUpdateReply() throws Exception {
   System.out.println("updateReply");
   Reply reply = null;
   QuestionManager instance = null;
   instance.updateReply(reply);
   // TODO review the generated test code and remove the default call to fail.
   fail("The test case is a prototype.");
   }*/
  /**
   * Test of deleteQuestionAndReplies method, of class QuestionManager.
   */
  /*
   public void testDeleteQuestionAndReplies() throws Exception {
   System.out.println("deleteQuestionAndReplies");
   Collection questionIds = null;
   QuestionManager instance = null;
   instance.deleteQuestionAndReplies(questionIds);
   // TODO review the generated test code and remove the default call to fail.
   fail("The test case is a prototype.");
   }*/
  /**
   * Test of getQuestion method, of class QuestionManager.
   *
   * @throws Exception
   */
  public void testGetSimpleQuestion() throws Exception {
    long questionId = 100L;
    Question question = QuestionManagerFactory.getQuestionManager().getQuestion(questionId);
    Question expectedResult = new Question();
    expectedResult.setCategoryId("");
    expectedResult.setContent("la mise en œuvre désigne la création");
    expectedResult.setCreationDate("2010/09/14");
    expectedResult.setCreatorId("0");
    expectedResult.setInstanceId("questionReply12");
    expectedResult.setPK(new IdPK("100"));
    expectedResult.setPrivateReplyNumber(0);
    expectedResult.setPublicReplyNumber(1);
    expectedResult.setReplyNumber(1);
    expectedResult.setStatus(2);
    expectedResult.setTitle("Les accents ça fonctionne ïci ?");
    assertThat(question.hasClosedStatus(), is(true));
    assertThat(question.hasWaitingStatus(), is(false));
    assertThat(question.hasNewStatus(), is(false));
    assertThat(question, QuestionMatcher.matches(expectedResult));
  }

  /**
   * Test of getQuestion method, of class QuestionManager.
   *
   * @throws Exception
   */
  public void testGetWrongQuestion() throws Exception {
    long questionId = 100000L;
    Question question = QuestionManagerFactory.getQuestionManager().getQuestion(questionId);
    assertThat(question, is(nullValue()));
  }

  /**
   * Test of getQuestionAndReplies method, of class QuestionManager.
   */
  /*
   public void testGetQuestionAndReplies() throws Exception {
   System.out.println("getQuestionAndReplies");
   long questionId = 0L;
   QuestionManager instance = null;
   Question expResult = null;
   Question result = instance.getQuestionAndReplies(questionId);
   assertEquals(expResult, result);
   // TODO review the generated test code and remove the default call to fail.
   fail("The test case is a prototype.");
   }*/
  /**
   * Test of getQuestionsByIds method, of class QuestionManager.
   */
  /*
   public void testGetQuestionsByIds() throws Exception {
   System.out.println("getQuestionsByIds");
   ArrayList ids = null;
   QuestionManager instance = null;
   Collection expResult = null;
   Collection result = instance.getQuestionsByIds(ids);
   assertEquals(expResult, result);
   // TODO review the generated test code and remove the default call to fail.
   fail("The test case is a prototype.");
   }*/
  /**
   * Test of getQuestionReplies method, of class QuestionManager.
   */
  /*
   public void testGetQuestionReplies() throws Exception {
   System.out.println("getQuestionReplies");
   long questionId = 0L;
   QuestionManager instance = null;
   Collection expResult = null;
   Collection result = instance.getQuestionReplies(questionId);
   assertEquals(expResult, result);
   // TODO review the generated test code and remove the default call to fail.
   fail("The test case is a prototype.");
   }*/
  /**
   * Test of getQuestionPublicReplies method, of class QuestionManager.
   */
  /*
   public void testGetQuestionPublicReplies() throws Exception {
   System.out.println("getQuestionPublicReplies");
   long questionId = 0L;
   QuestionManager instance = null;
   Collection expResult = null;
   Collection result = instance.getQuestionPublicReplies(questionId);
   assertEquals(expResult, result);
   // TODO review the generated test code and remove the default call to fail.
   fail("The test case is a prototype.");
   }*/
  /**
   * Test of getQuestionPrivateReplies method, of class QuestionManager.
   */
  /*
   public void testGetQuestionPrivateReplies() throws Exception {
   System.out.println("getQuestionPrivateReplies");
   long questionId = 0L;
   QuestionManager instance = null;
   Collection expResult = null;
   Collection result = instance.getQuestionPrivateReplies(questionId);
   assertEquals(expResult, result);
   // TODO review the generated test code and remove the default call to fail.
   fail("The test case is a prototype.");
   }*/
  /**
   * Test of getQuestionRecipients method, of class QuestionManager.
   */
  /*
   public void testGetQuestionRecipients() throws Exception {
   System.out.println("getQuestionRecipients");
   long questionId = 0L;
   QuestionManager instance = null;
   Collection expResult = null;
   Collection result = instance.getQuestionRecipients(questionId);
   assertEquals(expResult, result);
   // TODO review the generated test code and remove the default call to fail.
   fail("The test case is a prototype.");
   }*/
  /**
   * Test of getReply method, of class QuestionManager.
   */
  /*
   public void testGetReply() throws Exception {
   System.out.println("getReply");
   long replyId = 0L;
   QuestionManager instance = null;
   Reply expResult = null;
   Reply result = instance.getReply(replyId);
   assertEquals(expResult, result);
   // TODO review the generated test code and remove the default call to fail.
   fail("The test case is a prototype.");
   }*/
  /**
   * Test of getSendQuestions method, of class QuestionManager.
   */
  /*
   public void testGetSendQuestions() throws Exception {
   System.out.println("getSendQuestions");
   String userId = "";
   String instanceId = "";
   QuestionManager instance = null;
   Collection expResult = null;
   Collection result = instance.getSendQuestions(userId, instanceId);
   assertEquals(expResult, result);
   // TODO review the generated test code and remove the default call to fail.
   fail("The test case is a prototype.");
   }*/
  /**
   * Test of getReceiveQuestions method, of class QuestionManager.
   */
  /*
   public void testGetReceiveQuestions() throws Exception {
   System.out.println("getReceiveQuestions");
   String userId = "";
   String instanceId = "";
   QuestionManager instance = null;
   Collection expResult = null;
   Collection result = instance.getReceiveQuestions(userId, instanceId);
   assertEquals(expResult, result);
   // TODO review the generated test code and remove the default call to fail.
   fail("The test case is a prototype.");
   }*/
  /**
   * Test of getQuestions method, of class QuestionManager.
   */
  public void testGetQuestions() throws Exception {
    String instanceId = "questionReply12";
    Collection<Question> expectedResult = new ArrayList<Question>();
    Question question1 = new Question();
    question1.setCategoryId("");
    question1.setContent("la mise en œuvre désigne la création");
    question1.setCreationDate("2010/09/14");
    question1.setCreatorId("0");
    question1.setInstanceId("questionReply12");
    question1.setPK(new IdPK("100"));
    question1.setPrivateReplyNumber(0);
    question1.setPublicReplyNumber(1);
    question1.setReplyNumber(1);
    question1.setStatus(2);
    question1.setTitle("Les accents ça fonctionne ïci ?");
    expectedResult.add(question1);
    Question question2 = new Question();
    question2.setCategoryId("3");
    question2.setContent("Les cœurs saignent Et s'accrochent en haut De tes bas");
    question2.setCreationDate("1993/06/14");
    question2.setCreatorId("0");
    question2.setInstanceId("questionReply12");
    question2.setPK(new IdPK("101"));
    question2.setPrivateReplyNumber(1);
    question2.setPublicReplyNumber(1);
    question2.setReplyNumber(2);
    question2.setStatus(1);
    question2.setTitle("Oh Marlène");
    expectedResult.add(question2);
    Question question0 = new Question();
    question0.setCategoryId("1");
    question0.setContent("This question has only private answers");
    question0.setCreationDate("2010/09/14");
    question0.setCreatorId("0");
    question0.setInstanceId("questionReply12");
    question0.setPK(new IdPK("103"));
    question0.setPrivateReplyNumber(1);
    question0.setPublicReplyNumber(0);
    question0.setReplyNumber(1);
    question0.setStatus(1);
    question0.setTitle("Question with private answers only");
    expectedResult.add(question0);
    List< Question> questions = QuestionManagerFactory.getQuestionManager().getQuestions(
        instanceId);
    assertThat(questions, is(notNullValue()));
    assertThat(questions, hasSize(3));
    assertThat(questions.get(1), QuestionMatcher.matches(question1));
    assertThat(questions.get(2), QuestionMatcher.matches(question2));
    assertThat(questions.get(0), QuestionMatcher.matches(question0));
  }

  /**
   * Test of getAllQuestions method, of class QuestionManager.
   *
   * @throws Exception
   */
  public void testGetAllQuestions() throws Exception {
    String instanceId = "questionReply12";
    Collection<Question> expectedResult = new ArrayList<Question>();
    Question question1 = new Question();
    question1.setCategoryId("");
    question1.setContent("la mise en œuvre désigne la création");
    question1.setCreationDate("2010/09/14");
    question1.setCreatorId("0");
    question1.setInstanceId("questionReply12");
    question1.setPK(new IdPK("100"));
    question1.setPrivateReplyNumber(0);
    question1.setPublicReplyNumber(1);
    question1.setReplyNumber(1);
    question1.setStatus(2);
    question1.setTitle("Les accents ça fonctionne ïci ?");
    expectedResult.add(question1);
    Question question2 = new Question();
    question2.setCategoryId("3");
    question2.setContent("Les cœurs saignent Et s'accrochent en haut De tes bas");
    question2.setCreationDate("1993/06/14");
    question2.setCreatorId("0");
    question2.setInstanceId("questionReply12");
    question2.setPK(new IdPK("101"));
    question2.setPrivateReplyNumber(1);
    question2.setPublicReplyNumber(1);
    question2.setReplyNumber(2);
    question2.setStatus(1);
    question2.setTitle("Oh Marlène");
    expectedResult.add(question2);
    Question question0 = new Question();
    question0.setCategoryId("1");
    question0.setContent("This question has only private answers");
    question0.setCreationDate("2010/09/14");
    question0.setCreatorId("0");
    question0.setInstanceId("questionReply12");
    question0.setPK(new IdPK("103"));
    question0.setPrivateReplyNumber(1);
    question0.setPublicReplyNumber(0);
    question0.setReplyNumber(1);
    question0.setStatus(1);
    question0.setTitle("Question with private answers only");
    expectedResult.add(question0);
    List< Question> questions = QuestionManagerFactory.getQuestionManager().getAllQuestions(
        instanceId);
    assertThat(questions, is(notNullValue()));
    assertThat(questions, hasSize(3));
    assertThat(questions.get(0), QuestionMatcher.matches(question0));
    assertThat(questions.get(1), QuestionMatcher.matches(question1));
    assertThat(questions.get(2), QuestionMatcher.matches(question2));
    List<Reply> replies = questions.get(1).readReplies();
    assertThat(replies, is(notNullValue()));
    assertThat(replies, hasSize(1));
    Reply reply = new Reply();
    reply.setContent("Les accents fonctionnent correctement grâce à l'UTF-8");
    reply.setCreationDate("2010/10/02");
    reply.setCreatorId("1");
    reply.setQuestionId(100L);
    reply.setPK(new IdPK("200"));
    reply.setPrivateReply(0);
    reply.setPublicReply(1);
    reply.setTitle("Oui les accents fonctionnent");
    assertThat(replies.get(0), ReplyMatcher.matches(reply));
    assertThat(questions.get(2), QuestionMatcher.matches(question2));
    replies = questions.get(2).readReplies();
    reply = new Reply();
    reply.setContent("Dans tes veines Coule l'amour Des soldats");
    reply.setCreationDate("1993/10/02");
    reply.setCreatorId("1");
    reply.setQuestionId(101L);
    reply.setPK(new IdPK("201"));
    reply.setPrivateReply(0);
    reply.setPublicReply(1);
    reply.setTitle("Oh Marlène");
    assertThat(replies.get(0), ReplyMatcher.matches(reply));
    reply = new Reply();
    reply.setContent("Et quand ils meurent ou s'endorment C'est la chaleur de ta voix Qui les "
        + "apaise, et les traîne Jusqu'en dehors des combats");
    reply.setCreationDate("1993/11/15");
    reply.setCreatorId("2");
    reply.setQuestionId(101L);
    reply.setPK(new IdPK("202"));
    reply.setPrivateReply(1);
    reply.setPublicReply(0);
    reply.setTitle("Oh Marlène");

    assertThat(replies.get(1), ReplyMatcher.matches(reply));
  }

  /**
   * Test of getAllQuestionsByCategory method, of class QuestionManager.
   *
   * @throws Exception
   */
  public void testGetAllQuestionsByCategory() throws Exception {
    String instanceId = "questionReply12";
    String categoryId = "3";
    Question question2 = new Question();
    question2.setCategoryId(categoryId);
    question2.setContent("Les cœurs saignent Et s'accrochent en haut De tes bas");
    question2.setCreationDate("1993/06/14");
    question2.setCreatorId("0");
    question2.setInstanceId("questionReply12");
    question2.setPK(new IdPK("101"));
    question2.setPrivateReplyNumber(1);
    question2.setPublicReplyNumber(1);
    question2.setReplyNumber(2);
    question2.setStatus(1);
    question2.setTitle("Oh Marlène");
    List<Question> questions = QuestionManagerFactory.getQuestionManager().
        getAllQuestionsByCategory(
        instanceId, categoryId);
    assertThat(questions, is(notNullValue()));
    assertThat(questions, hasSize(1));
    assertThat(questions.get(0), QuestionMatcher.matches(question2));
  }

  /**
   * Test of getPublicQuestions method, of class QuestionManager.
   *
   * @throws Exception
   */
  public void testGetPublicQuestions() throws Exception {
    String instanceId = "questionReply12";
    Question question1 = new Question();
    question1.setCategoryId("");
    question1.setContent("la mise en œuvre désigne la création");
    question1.setCreationDate("2010/09/14");
    question1.setCreatorId("0");
    question1.setInstanceId("questionReply12");
    question1.setPK(new IdPK("100"));
    question1.setPrivateReplyNumber(0);
    question1.setPublicReplyNumber(1);
    question1.setReplyNumber(1);
    question1.setStatus(2);
    question1.setTitle("Les accents ça fonctionne ïci ?");
    List< Question> questions = QuestionManagerFactory.getQuestionManager().getPublicQuestions(
        instanceId);
    assertThat(questions, is(notNullValue()));
    assertThat(questions, hasSize(2));
    assertThat(questions.get(0), QuestionMatcher.matches(question1));
  }

  /**
   * Test of createQuestionReply method, of class QuestionManager.
   */
  /*
   public void testCreateQuestionReply() throws Exception {
   System.out.println("createQuestionReply");
   Question question = null;
   Reply reply = null;
   QuestionManager instance = null;
   long expResult = 0L;
   long result = instance.createQuestionReply(question, reply);
   assertEquals(expResult, result);
   // TODO review the generated test code and remove the default call to fail.
   fail("The test case is a prototype.");
   }*/
  /**
   * Test of isSortable method, of class QuestionManager.
   *
   * @return
   */
  /*
   public void testIsSortable() {
   System.out.println("isSortable");
   String instanceId = "";
   QuestionManager instance = null;
   boolean expResult = false;
   boolean result = instance.isSortable(instanceId);
   assertEquals(expResult, result);
   // TODO review the generated test code and remove the default call to fail.
   fail("The test case is a prototype.");
   }*/
  @Override
  protected String getDatasetFileName() {
    return "question-reply-dataset.xml";
  }
}
