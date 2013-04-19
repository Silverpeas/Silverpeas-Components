/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.mailinglist.service.notification;

import com.silverpeas.mailinglist.service.model.beans.Message;
import com.silverpeas.util.PathTestUtil;
import com.silverpeas.util.template.SilverpeasTemplate;
import java.io.File;
import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author ehugonnet
 */
public class AdvancedNotificationFormatterTest {

  private static String rootDir = PathTestUtil.TARGET_DIR
      + "test-classes" + File.separatorChar + "templates" + File.separatorChar;
  private static Properties configuration = new Properties();

  public AdvancedNotificationFormatterTest() {
  }

  @Before
  public void setUp() {
    configuration.setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR, rootDir);
    configuration.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR, rootDir);
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of formatTitle method, of class AdvancedNotificationFormatter.
   */
  @Test
  public void testFormatTitle() {
    Message message = new Message();
    message.setTitle("Hello World");
    String mailingListName = "My Simple Mailing List";
    String lang = "en";
    boolean moderate = false;
    AdvancedNotificationFormatter instance = new AdvancedNotificationFormatter();
    String expResult = "[My Simple Mailing List] : Hello World";
    String result = instance.formatTitle(message, mailingListName, lang, moderate);
    assertEquals(expResult, result);
  }

  @Test
  public void testFormatModerationTitle() {
    Message message = new Message();
    message.setTitle("Hello Moderated World");
    String mailingListName = "My Moderated Mailing List";
    String lang = "en";
    boolean moderate = true;
    AdvancedNotificationFormatter instance = new AdvancedNotificationFormatter();
    String expResult = "[My Moderated Mailing List] : Hello Moderated World - to be moderated";
    String result = instance.formatTitle(message, mailingListName, lang, moderate);
    assertEquals(expResult, result);
  }

  /**
   * Test of formatMessage method, of class AdvancedNotificationFormatter.
   */
  @Test
  public void testFormatMessage() {
    Message message = new Message();
    message.setTitle("Hello World");
    message.setSummary("This world is really cool !!!");
    message.setComponentId("componentId");
    String lang = "en";
    boolean moderate = false;
    AdvancedNotificationFormatter instance = new AdvancedNotificationFormatter();
    String expResult = "<html><head/><body>p><b>Message [Hello World] :</b></p><p>This world is "
        + "really cool !!! ...<br/><a href=\"/Rmailinglist/componentId/message/" + message.getId()
        + "\">Click here</a></p></body></html>";
    String result = instance.formatMessage(message, lang, moderate);
    assertEquals(expResult, result);
  }

  /**
   * Test of formatMessage method, of class AdvancedNotificationFormatter.
   */
  @Test
  public void testFormatModeratedMessage() {
    Message message = new Message();
    message.setTitle("Hello Moderated World");
    message.setSummary("This moderated world is really cool !!!");
    message.setComponentId("componentId");
    String lang = "en";
    boolean moderate = true;
    AdvancedNotificationFormatter instance = new AdvancedNotificationFormatter();
    String expResult = "<html><head/><body><p><b>Message [Hello Moderated World] - to be moderated "
        + ": </b></p><p>This moderated world is really cool !!! ...</p>"
        + "<a href=\"/Rmailinglist/componentId/moderationList/componentId\">Moderate here</a>"
        + "</body></html>";
    String result = instance.formatMessage(message, lang, moderate);
    assertEquals(expResult, result);
  }
}
