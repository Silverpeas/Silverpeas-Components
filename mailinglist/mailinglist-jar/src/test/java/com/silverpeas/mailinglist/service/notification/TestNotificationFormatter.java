package com.silverpeas.mailinglist.service.notification;

import org.springframework.test.AbstractSingleSpringContextTests;

import com.silverpeas.mailinglist.service.model.beans.Message;

public class TestNotificationFormatter extends AbstractSingleSpringContextTests {
  protected String[] getConfigLocations() {
    return new String[] { "spring-checker.xml", "spring-notification.xml",
        "spring-hibernate.xml", "spring-datasource.xml" };
  }

  protected NotificationFormatter getFormatter() {
    return (NotificationFormatter) this.applicationContext
        .getBean("notificationFormatter");
  }

  public void testFormatTitle() {
    Message message = new Message();
    message.setTitle("Hello World");
    NotificationFormatter formatter = getFormatter();
    String result = formatter.formatTitle(message, "Test", false);
    assertEquals("[Test] : Hello World", result);
  }

  public void testFormatModerationTitle() {
    Message message = new Message();
    message.setTitle("Hello World");
    NotificationFormatter formatter = getFormatter();
    String result = formatter.formatTitle(message, "Test", true);
    assertEquals("[Test] : Hello World à modérer", result);
  }

  public void testFormatMessage() {
    Message message = new Message();
    message.setBody("Hello World");
    message.setId("id");
    message.setComponentId("componentId");
    NotificationFormatter formatter = getFormatter();
    String result = formatter.formatMessage(message, "", false);
    assertEquals("<html><head/><body><p>[null]</p><a href=\"/Rmailinglist/" +
    		"componentId/message/id\">Cliquez ici</a></body></html>", result);
  }

  public void testFormatModerationMessage() {
    Message message = new Message();
    message.setBody("Hello World");
    message.setId("id");
    message.setTitle("title");
    message.setComponentId("componentId");
    NotificationFormatter formatter = getFormatter();
    String result = formatter.formatMessage(message, "", true);
    assertEquals(
        "<html><head/><body><p><b>Message [title] : </b></p><p>[null]"
            + "...</p><a href=\"/Rmailinglist/componentId/moderationList/componentId\">"
            + "Modérez ici</a></body></html>", result);
  }

  public void testPrepareModerationUrl() {
    Message message = new Message();
    message.setTitle("Hello World");
    message.setId("id");
    message.setComponentId("componentId");
    NotificationFormatter formatter = getFormatter();
    String result = formatter.prepareUrl("", message, true);
    assertEquals(
        "/Rmailinglist/componentId/moderationList/componentId",
        result);
  }

  public void testPrepareUrl() {
    Message message = new Message();
    message.setTitle("Hello World");
    message.setId("id");
    message.setComponentId("componentId");
    NotificationFormatter formatter = getFormatter();
    String result = formatter.prepareUrl("", message, false);
    assertEquals("/Rmailinglist/componentId/message/id", result);
  }
}
