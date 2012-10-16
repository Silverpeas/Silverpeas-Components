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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.mailinglist.service.notification;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.silverpeas.mailinglist.service.model.beans.Message;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
public class TestNotificationFormatter {

  private ApplicationContext applicationContext;

  @Before
  public void loadContext() {
    applicationContext = new ClassPathXmlApplicationContext("spring-checker.xml",
        "spring-notification.xml", "spring-hibernate.xml", "spring-datasource.xml");
  }

  protected NotificationFormatter getFormatter() {
    return (NotificationFormatter) this.applicationContext.getBean("notificationFormatter");
  }

  @Test
  public void testFormatTitle() {
    Message message = new Message();
    message.setTitle("Hello World");
    NotificationFormatter formatter = getFormatter();
    String result = formatter.formatTitle(message, "Test", "fr", false);
    assertThat(result, is("[Test] : Hello World"));
  }

  @Test
  public void testFormatModerationTitle() {
    Message message = new Message();
    message.setTitle("Hello World");
    NotificationFormatter formatter = getFormatter();
    String result = formatter.formatTitle(message, "Test", "fr", true);
    assertThat(result, is("[Test] : Hello World à modérer"));
  }

  @Test
  public void testFormatMessage() {
    Message message = new Message();
    message.setBody("Hello World");
    message.setId("id");
    message.setComponentId("componentId");
    NotificationFormatter formatter = getFormatter();
    String result = formatter.formatMessage(message, "", false);
    assertThat(result, is("<html><head/><body><p>[null]</p><a href=\"/Rmailinglist/"
        + "componentId/message/id\">Cliquez ici</a></body></html>"));
  }

  @Test
  public void testFormatModerationMessage() {
    Message message = new Message();
    message.setBody("Hello World");
    message.setId("id");
    message.setTitle("title");
    message.setComponentId("componentId");
    NotificationFormatter formatter = getFormatter();
    String result = formatter.formatMessage(message, "", true);
    assertThat(result, is("<html><head/><body><p><b>Message [title] : </b></p><p>[null]"
        + "...</p><a href=\"/Rmailinglist/componentId/moderationList/componentId\">"
        + "Modérez ici</a></body></html>"));
  }

  @Test
  public void testPrepareModerationUrl() {
    Message message = new Message();
    message.setTitle("Hello World");
    message.setId("id");
    message.setComponentId("componentId");
    NotificationFormatter formatter = getFormatter();
    String result = formatter.prepareUrl(message, true);
    assertThat(result, is("/Rmailinglist/componentId/moderationList/componentId"));
  }

  @Test
  public void testPrepareUrl() {
    Message message = new Message();
    message.setTitle("Hello World");
    message.setId("id");
    message.setComponentId("componentId");
    NotificationFormatter formatter = getFormatter();
    String result = formatter.prepareUrl(message, false);
    assertThat(result, is("/Rmailinglist/componentId/message/id"));
  }
}
