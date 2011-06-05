/*
 * To change this template, choose Tools | Templates
 * and wait the template in the editor.
 */
package com.silverpeas.questionReply.control;

import com.silverpeas.questionReply.model.Question;
import com.silverpeas.questionReply.model.Reply;
import com.stratelia.webactiv.SilverpeasRole;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author ehugonnet
 */
public class QuestionReplyExportTest {

  public QuestionReplyExportTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of isReplyVisible method, of class QuestionReplyExport.
   */
  @Test
  public void testIsReplyVisible() {
    Question question = new Question("10", "questionReply12");
    Reply privateReply = new Reply();
    privateReply.setPublicReply(0);
    Reply publicReply = new Reply();
    publicReply.setPublicReply(1);
    QuestionReplySessionController adminScc = mock(QuestionReplySessionController.class);
    when(adminScc.getUserRole()).thenReturn(SilverpeasRole.admin);
    QuestionReplySessionController writerScc = mock(QuestionReplySessionController.class);
    when(writerScc.getUserRole()).thenReturn(SilverpeasRole.writer);
    QuestionReplySessionController publisherCreatorScc = mock(QuestionReplySessionController.class);
    when(publisherCreatorScc.getUserRole()).thenReturn(SilverpeasRole.publisher);
    when(publisherCreatorScc.getUserId()).thenReturn("10");
    QuestionReplySessionController publisherScc = mock(QuestionReplySessionController.class);
    when(publisherScc.getUserRole()).thenReturn(SilverpeasRole.publisher);
    when(publisherScc.getUserId()).thenReturn("20");
    QuestionReplySessionController userScc = mock(QuestionReplySessionController.class);
    when(userScc.getUserRole()).thenReturn(SilverpeasRole.user);
    when(userScc.getUserId()).thenReturn("20");
    QuestionReplyExport instance = new QuestionReplyExport(null, null);
    assertTrue("Admin should see everything", instance.isReplyVisible(question, privateReply,
        adminScc));
    assertTrue("Admin should see everything", instance.isReplyVisible(question, publicReply,
        adminScc));
    assertTrue("Writer should see everything", instance.isReplyVisible(question, privateReply,
        writerScc));
    assertTrue("Writer should see everything", instance.isReplyVisible(question, publicReply,
        writerScc));

    assertTrue("User should see everything public", instance.isReplyVisible(question, publicReply,
        userScc));
    assertFalse("User should not see everything private", instance.isReplyVisible(question,
        privateReply, userScc));
    assertTrue("Publisher should see everything public", instance.isReplyVisible(question,
        publicReply, publisherCreatorScc));
    assertTrue("Publisher should see everything it has created", instance.isReplyVisible(question,
        privateReply, publisherCreatorScc));
    assertTrue("Publisher should see everything public", instance.isReplyVisible(question,
        publicReply, publisherScc));
    assertFalse("Publisher should not see everything", instance.isReplyVisible(question, privateReply,
        publisherScc));
  }
}
