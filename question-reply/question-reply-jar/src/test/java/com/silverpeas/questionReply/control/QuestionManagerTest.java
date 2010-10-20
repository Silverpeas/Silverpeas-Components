/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.questionReply.control;

import org.slf4j.Logger;
import com.silverpeas.questionReply.model.Question;
import javax.naming.NamingException;
import java.sql.Statement;
import com.silverpeas.util.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.dbunit.database.IDatabaseConnection;
import com.silverpeas.components.model.AbstractTestDao;
import com.silverpeas.jcrutil.RandomGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;

/**
 *
 * @author ehugonnet
 */
public class QuestionManagerTest extends AbstractTestDao {

  public QuestionManagerTest() {
  }
  private static final String INSTANCE_ID = "questionReply12";

  @BeforeClass
  public static void generalSetUp() throws IOException, NamingException {
    AbstractTestDao.configureJNDIDatasource();
  }

  @Before
  @Override
  public void setUp() throws Exception {
    configureJNDIDatasource();
    IDatabaseConnection databaseConnection = getDatabaseTester().getConnection();
    executeDDL(databaseConnection, "create-database.ddl");
    super.prepareData();
  }

  private void executeDDL(IDatabaseConnection databaseConnection, String filename) {
    try {
      Statement st = databaseConnection.getConnection().createStatement();
      st.execute(loadDDL(filename));
    } catch (Exception e) {
      LoggerFactory.getLogger(QuestionManagerTest.class).error("Error creating tables", e); 
    } finally {
      try {
        getDatabaseTester().closeConnection(databaseConnection);
      } catch (Exception e) {
        // ignore  
      }
    }
  }

  private String loadDDL(String filename) throws IOException {
    InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
    BufferedReader r = new BufferedReader(new InputStreamReader(in));
    StringBuilder buffer = new StringBuilder();
    String line = null;
    String EOL = System.getProperty("line.separator");
    while ((line = r.readLine()) != null) {
      if (StringUtil.isDefined(line) && !line.startsWith("#")) {
        buffer.append(line).append(EOL);
      }
    }
    in.close();
    return buffer.toString();
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of getInstance method, of class QuestionManager.
   */
  @Test
  public void testGetInstance() {
    System.out.println("getInstance");
    QuestionManager expResult = QuestionManager.getInstance();
    assertNotNull(expResult);
    QuestionManager result = QuestionManager.getInstance();
    assertEquals(expResult, result);
  }

  /**
   * Test of createQuestion method, of class QuestionManager.
   */
  /*@Test
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
  /* @Test
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
  /* @Test
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
  /* @Test
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
  /*  @Test
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
  /*  @Test
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
  /*  @Test
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
  /* @Test
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
  /* @Test
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
  /* @Test
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
   */
  /*  @Test
  public void testUpdateRepliesPublicStatus() throws Exception {
  System.out.println("updateRepliesPublicStatus");
  Collection replyIds = null;
  Question question = null;
  QuestionManager instance = null;
  instance.updateRepliesPublicStatus(replyIds, question);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  /**
   * Test of updateRepliesPrivateStatus method, of class QuestionManager.
   */
  /* @Test
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
  /* @Test
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
  /*@Test
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
  /* @Test
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
   */
  /* @Test
  public void testGetQuestion() throws Exception {
  System.out.println("getQuestion");
  long questionId = 0L;
  QuestionManager instance = null;
  Question expResult = null;
  Question result = instance.getQuestion(questionId);
  assertEquals(expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  /**
   * Test of getQuestionAndReplies method, of class QuestionManager.
   */
  /*  @Test
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
  /* @Test
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
  /*@Test
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
  /*@Test
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
  /* @Test
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
  /*@Test
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
  /*@Test
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
  /*@Test
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
  /* @Test
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
  /* @Test
  public void testGetQuestions() throws Exception {
  System.out.println("getQuestions");
  String instanceId = "";
  QuestionManager instance = null;
  Collection expResult = null;
  Collection result = instance.getQuestions(instanceId);
  assertEquals(expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  /**
   * Test of getAllQuestions method, of class QuestionManager.
   */
  /*@Test
  public void testGetAllQuestions() throws Exception {
  System.out.println("getAllQuestions");
  String instanceId = "";
  QuestionManager instance = null;
  Collection expResult = null;
  Collection result = instance.getAllQuestions(instanceId);
  assertEquals(expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  /**
   * Test of getAllQuestionsByCategory method, of class QuestionManager.
   */
  /*@Test
  public void testGetAllQuestionsByCategory() throws Exception {
  System.out.println("getAllQuestionsByCategory");
  String instanceId = "";
  String categoryId = "";
  QuestionManager instance = null;
  Collection expResult = null;
  Collection result = instance.getAllQuestionsByCategory(instanceId, categoryId);
  assertEquals(expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  /**
   * Test of getPublicQuestions method, of class QuestionManager.
   */
  /* @Test
  public void testGetPublicQuestions() throws Exception {
  System.out.println("getPublicQuestions");
  String instanceId = "";
  QuestionManager instance = null;
  Collection expResult = null;
  Collection result = instance.getPublicQuestions(instanceId);
  assertEquals(expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  /**
   * Test of createQuestionReply method, of class QuestionManager.
   */
  /*@Test
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
   */
  /*@Test
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
