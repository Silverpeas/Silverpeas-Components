/*
 * To change this template, choose Tools | Templates
 * and wait the template in the editor.
 */
package org.silverpeas.components.questionreply.control;

import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.components.questionreply.model.Reply;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author ehugonnet
 */
public class QuestionReplyExportTest {

  public QuestionReplyExportTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
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
    when(adminScc.getUserRole()).thenReturn(SilverpeasRole.ADMIN);
    QuestionReplySessionController writerScc = mock(QuestionReplySessionController.class);
    when(writerScc.getUserRole()).thenReturn(SilverpeasRole.WRITER);
    QuestionReplySessionController publisherCreatorScc = mock(QuestionReplySessionController.class);
    when(publisherCreatorScc.getUserRole()).thenReturn(SilverpeasRole.PUBLISHER);
    when(publisherCreatorScc.getUserId()).thenReturn("10");
    QuestionReplySessionController publisherScc = mock(QuestionReplySessionController.class);
    when(publisherScc.getUserRole()).thenReturn(SilverpeasRole.PUBLISHER);
    when(publisherScc.getUserId()).thenReturn("20");
    QuestionReplySessionController userScc = mock(QuestionReplySessionController.class);
    when(userScc.getUserRole()).thenReturn(SilverpeasRole.USER);
    when(userScc.getUserId()).thenReturn("20");
    QuestionReplyExport instance = new QuestionReplyExport(null, null);
    assertThat("Admin should see everything", instance.isReplyVisible(question, privateReply,
        adminScc), is(true));
    assertThat("Admin should see everything", instance.isReplyVisible(question, publicReply,
        adminScc), is(true));
    assertThat("Writer should see everything", instance.isReplyVisible(question, privateReply,
        writerScc), is(true));
    assertThat("Writer should see everything", instance.isReplyVisible(question, publicReply,
        writerScc), is(true));

    assertThat("User should see everything public", instance.isReplyVisible(question, publicReply,
        userScc), is(true));
    assertThat("User should not see everything private", instance.isReplyVisible(question,
        privateReply, userScc), is(false));
    assertThat("Publisher should see everything public", instance.isReplyVisible(question,
        publicReply, publisherCreatorScc), is(true));
    assertThat("Publisher should see everything it has created", instance.isReplyVisible(question,
        privateReply, publisherCreatorScc), is(true));
    assertThat("Publisher should see everything public", instance.isReplyVisible(question,
        publicReply, publisherScc), is(true));
    assertThat("Publisher should not see everything", instance.isReplyVisible(question, privateReply,
        publisherScc), is(false));
  }
}
