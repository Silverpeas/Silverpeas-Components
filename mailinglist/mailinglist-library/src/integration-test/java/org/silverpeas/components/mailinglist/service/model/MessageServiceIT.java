/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.mailinglist.service.model;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.components.mailinglist.MailingListWarBuilder;
import org.silverpeas.components.mailinglist.service.model.beans.Attachment;
import org.silverpeas.components.mailinglist.service.model.beans.MailingList;
import org.silverpeas.components.mailinglist.service.model.beans.MailingListActivity;
import org.silverpeas.components.mailinglist.service.model.beans.Message;
import org.silverpeas.components.mailinglist.service.util.OrderBy;
import org.silverpeas.core.test.integration.rule.DbSetupRule;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class MessageServiceIT {

  private static final OrderBy orderByDate = new OrderBy("sentDate", false);
  private static final String textEmailContent = "Bonjour famille Simpson, " +
      "j'espère que vous allez bien. Ici tout se passe bien et Krusty est très " +
      "sympathique. Surtout depuis que Tahiti Bob est retourné en prison. Je " +
      "dois remplacer l'homme canon dans la prochaine émission.\nBart";
  private static final String attachmentPath =
      "c:\\tmp\\uploads\\componentId\\mailId@silverpeas.com\\";
  private MessageService messageService;

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom("create-database.sql")
      .loadInitialDataSetFrom("test-message-service-dataset.sql");

  private static TimeZone defaultTimeZone;

  @Deployment
  public static Archive<?> createTestArchive() {
    return MailingListWarBuilder.onWarForTestClass(MessageServiceIT.class)
        .testFocusedOn(warBuilder -> warBuilder.addAsResource(
            "org/silverpeas/util/attachment/Attachment.properties"))
        .build();
  }

  @Test
  public void getMessage() {
    Message savedMessage = messageService.getMessage("1");
    assertNotNull(savedMessage);
    assertEquals(textEmailContent.replaceAll("\n", ""), savedMessage.getBody());
    assertEquals("componentId", savedMessage.getComponentId());
    assertTrue(savedMessage.isModerated());
    assertEquals(textEmailContent.substring(0, 200), savedMessage.getSummary());
    assertEquals("bart.simpson@silverpeas.com", savedMessage.getSender());
    assertEquals("0000001747b40c8d", savedMessage.getMessageId());
    assertThat(1204364055000L, is(lessThanOrEqualTo(savedMessage.getSentDate().getTime())));
    assertEquals("Simple database message", savedMessage.getTitle());
    assertEquals("text/plain", savedMessage.getContentType());
    assertEquals(2008, savedMessage.getYear());
    assertEquals(Calendar.MARCH, savedMessage.getMonth());
    assertEquals("1", savedMessage.getId());
    assertEquals(1, savedMessage.getVersion());
    assertEquals(10000, savedMessage.getAttachmentsSize());
    assertNotNull(savedMessage.getAttachments());
    assertEquals(1, savedMessage.getAttachments().size());
    Attachment attached = savedMessage.getAttachments().iterator().next();
    assertNotNull(attached);
    assertEquals("1", attached.getId());
    assertEquals(1, attached.getVersion());
    assertEquals(10000, attached.getSize());
    assertEquals("lemonde.html", attached.getFileName());
    assertEquals("text/html", attached.getContentType());
    assertEquals(attachmentPath + "lemonde.html", attached.getPath());
  }

  @Test
  public void saveMessage() {
    Calendar sentDate = Calendar.getInstance();
    sentDate.set(Calendar.MILLISECOND, 0);
    Message message = new Message();
    message.setBody(textEmailContent);
    message.setComponentId("componentId");
    message.setModerated(true);
    message.setSummary(textEmailContent.substring(0, 200));
    message.setSender("bart.simpson@ilverpeas.com");
    message.setMessageId("0000001747b40c85");
    message.setSentDate(sentDate.getTime());
    message.setTitle("Simple text message");
    message.setContentType("text/plain");

    Attachment attachment = new Attachment();
    attachment.setPath(attachmentPath + "lemonde.html");
    attachment.setFileName("lemonde.html");
    attachment.setContentType("text/html");
    attachment.setSize(10000);
    message.getAttachments().add(attachment);
    String id = messageService.saveMessage(message);
    assertNotNull(id);
    Message savedMessage = messageService.getMessage(id);
    assertNotNull(savedMessage);
    assertEquals(textEmailContent, savedMessage.getBody());
    assertEquals("componentId", savedMessage.getComponentId());
    assertTrue(savedMessage.isModerated());
    assertEquals(textEmailContent.substring(0, 200), savedMessage.getSummary());
    assertEquals("bart.simpson@ilverpeas.com", savedMessage.getSender());
    assertEquals("0000001747b40c85", savedMessage.getMessageId());
    assertEquals(sentDate.getTime(), savedMessage.getSentDate());
    assertEquals("Simple text message", savedMessage.getTitle());
    assertEquals(sentDate.get(Calendar.YEAR), savedMessage.getYear());
    assertEquals(sentDate.get(Calendar.MONTH), savedMessage.getMonth());
    assertEquals(id, savedMessage.getId());
    assertEquals(0, savedMessage.getVersion());
    assertEquals(10000, savedMessage.getAttachmentsSize());
    assertNotNull(savedMessage.getAttachments());
    assertEquals(1, savedMessage.getAttachments().size());
    assertEquals("text/plain", savedMessage.getContentType());
    Attachment attached = savedMessage.getAttachments().iterator().next();
    assertNotNull(attached);
    assertEquals(0, attached.getVersion());
    assertNotNull(attached.getId());
    assertEquals(10000, attached.getSize());
    assertEquals("lemonde.html", attached.getFileName());
    assertEquals(attachmentPath + "lemonde.html", attached.getPath());
  }

  @Test
  public void saveAgainMessage() {
    Calendar sentDate = Calendar.getInstance();
    sentDate.set(Calendar.MILLISECOND, 0);
    Message message = new Message();
    message.setBody(textEmailContent);
    message.setComponentId("componentId");
    message.setModerated(true);
    message.setSummary(textEmailContent.substring(0, 200));
    message.setSender("bart.simpson@ilverpeas.com");
    message.setMessageId("0000001747b40c85");
    message.setSentDate(sentDate.getTime());
    message.setTitle("Simple text message");
    message.setContentType("text/plain");

    Attachment attachment = new Attachment();
    attachment.setPath(attachmentPath + "lemonde.html");
    attachment.setFileName("lemonde.html");
    attachment.setContentType("text/html");
    attachment.setSize(10000);
    message.getAttachments().add(attachment);
    String id = messageService.saveMessage(message);
    assertNotNull(id);
    Message savedMessage = messageService.getMessage(id);
    assertNotNull(savedMessage);
    assertEquals(textEmailContent, savedMessage.getBody());
    assertEquals("componentId", savedMessage.getComponentId());
    assertTrue(savedMessage.isModerated());
    assertEquals(textEmailContent.substring(0, 200), savedMessage.getSummary());
    assertEquals("bart.simpson@ilverpeas.com", savedMessage.getSender());
    assertEquals("0000001747b40c85", savedMessage.getMessageId());
    assertEquals(sentDate.getTime(), savedMessage.getSentDate());
    assertEquals("Simple text message", savedMessage.getTitle());
    assertEquals(sentDate.get(Calendar.YEAR), savedMessage.getYear());
    assertEquals(sentDate.get(Calendar.MONTH), savedMessage.getMonth());
    assertEquals(id, savedMessage.getId());
    assertEquals(0, savedMessage.getVersion());
    assertEquals(10000, savedMessage.getAttachmentsSize());
    assertNotNull(savedMessage.getAttachments());
    assertEquals(1, savedMessage.getAttachments().size());
    assertEquals("text/plain", savedMessage.getContentType());
    Attachment attached = savedMessage.getAttachments().iterator().next();
    assertNotNull(attached);
    assertEquals(0, attached.getVersion());
    assertNotNull(attached.getId());
    assertEquals(10000, attached.getSize());
    assertEquals("lemonde.html", attached.getFileName());
    assertEquals(attachmentPath + "lemonde.html", attached.getPath());

    message = new Message();
    message.setComponentId("componentId");
    message.setMessageId("0000001747b40c85");
    message.getAttachments().add(attachment);
    String newId = messageService.saveMessage(message);
    assertNotNull(newId);
    assertEquals(id, newId);

    message = new Message();
    message.setComponentId("componentId2");
    message.setMessageId("0000001747b40c85");
    attachment = new Attachment();
    attachment.setPath(attachmentPath + "lemonde.html");
    attachment.setFileName("lemonde.html");
    attachment.setContentType("text/html");
    attachment.setSize(10000);
    message.getAttachments().add(attachment);
    String newId2 = messageService.saveMessage(message);
    assertNotNull(newId2);
    assertNotSame(id, newId2);
  }

  @Test
  public void listMessages() {
    MailingList mailingList = new MailingList();
    mailingList.setComponentId("componentId");
    List<Message> messages = messageService.listMessages(mailingList, 0, orderByDate);
    assertNotNull(messages);
    assertEquals(3, messages.size());
    assertEquals("3", messages.get(0).getId());
    assertEquals("1", messages.get(1).getId());
    assertEquals("2", messages.get(2).getId());
  }

  @Test
  public void getTotalNumberOfMessages() {
    MailingList mailingList = new MailingList();
    mailingList.setComponentId("componentId");
    assertEquals(3, messageService.getTotalNumberOfMessages(mailingList));
  }

  @Test
  public void listDisplayableMessages() {
    MailingList mailingList = new MailingList();
    mailingList.setComponentId("componentId");
    List<Message> messages =
        messageService.listDisplayableMessages(mailingList, -1, -1, 0, orderByDate);
    assertNotNull(messages);
    assertEquals(2, messages.size());
    assertEquals("1", messages.get(0).getId());
    assertEquals("2", messages.get(1).getId());
    messages = messageService.listDisplayableMessages(mailingList, -1, 2008, 0, orderByDate);
    assertNotNull(messages);
    assertEquals(2, messages.size());
    assertEquals("1", messages.get(0).getId());
    assertEquals("2", messages.get(1).getId());
    messages = messageService.listDisplayableMessages(mailingList, Calendar.FEBRUARY, 2008, 0,
        orderByDate);
    assertNotNull(messages);
    assertEquals(1, messages.size());
    assertEquals("2", messages.get(0).getId());
    messages =
        messageService.listDisplayableMessages(mailingList, Calendar.MARCH, 2008, 0, orderByDate);
    assertNotNull(messages);
    assertEquals(1, messages.size());
    assertEquals("1", messages.get(0).getId());
    messages = messageService.listDisplayableMessages(mailingList, -1, 2007, 0, orderByDate);
    assertNotNull(messages);
    assertEquals(0, messages.size());
    messages = messageService.listDisplayableMessages(mailingList, 2, orderByDate);
    assertNotNull(messages);
    assertEquals(2, messages.size());
    assertEquals("1", messages.get(0).getId());
    assertEquals("2", messages.get(1).getId());
  }

  @Test
  public void listUnmoderatedeMessages() {
    MailingList mailingList = new MailingList();
    mailingList.setComponentId("componentId");
    List<Message> messages = messageService.listUnmoderatedeMessages(mailingList, 0, orderByDate);
    assertNotNull(messages);
    assertEquals(1, messages.size());
    assertEquals("3", messages.get(0).getId());
  }

  @Test
  public void getNumberOfPagesForUnmoderatedMessages() {
    MailingList mailingList = new MailingList();
    mailingList.setComponentId("componentId");
    long pages = messageService.getNumberOfPagesForUnmoderatedMessages(mailingList);
    assertEquals(1, pages);
    messageService.setElementsPerPage(1);
    pages = messageService.getNumberOfPagesForUnmoderatedMessages(mailingList);
    assertEquals(1, pages);
    messageService.setElementsPerPage(10);
  }

  @Test
  public void getNumberOfPagesForDisplayableMessages() {
    MailingList mailingList = new MailingList();
    mailingList.setComponentId("componentId");
    long pages = messageService.getNumberOfPagesForDisplayableMessages(mailingList);
    assertEquals(1, pages);
    messageService.setElementsPerPage(1);
    pages = messageService.getNumberOfPagesForDisplayableMessages(mailingList);
    assertEquals(2, pages);
    messageService.setElementsPerPage(10);
  }

  @Test
  public void deleteMessage() {
    Message savedMessage = messageService.getMessage("1");
    assertNotNull(savedMessage);
    assertEquals(textEmailContent.replaceAll("\n", ""), savedMessage.getBody());
    assertEquals("componentId", savedMessage.getComponentId());
    assertTrue(savedMessage.isModerated());
    assertEquals(textEmailContent.substring(0, 200), savedMessage.getSummary());
    assertEquals("bart.simpson@silverpeas.com", savedMessage.getSender());
    assertEquals("0000001747b40c8d", savedMessage.getMessageId());
    assertThat(1204364055000L, is(lessThanOrEqualTo(savedMessage.getSentDate().getTime())));
    assertEquals("Simple database message", savedMessage.getTitle());
    assertEquals("text/plain", savedMessage.getContentType());
    assertEquals(2008, savedMessage.getYear());
    assertEquals(Calendar.MARCH, savedMessage.getMonth());
    assertEquals("1", savedMessage.getId());
    assertEquals(1, savedMessage.getVersion());
    assertEquals(10000, savedMessage.getAttachmentsSize());
    assertNotNull(savedMessage.getAttachments());
    assertEquals(1, savedMessage.getAttachments().size());
    Attachment attached = savedMessage.getAttachments().iterator().next();
    assertNotNull(attached);
    assertEquals("1", attached.getId());
    assertEquals(1, attached.getVersion());
    assertEquals(10000, attached.getSize());
    assertEquals("lemonde.html", attached.getFileName());
    assertEquals("text/html", attached.getContentType());
    assertEquals(attachmentPath + "lemonde.html", attached.getPath());
    messageService.deleteMessage("1");
    savedMessage = messageService.getMessage("1");
    assertNull(savedMessage);
  }

  @Test
  public void moderateMessage() {
    Message savedMessage = messageService.getMessage("3");
    assertNotNull(savedMessage);
    assertEquals(textEmailContent.replaceAll("\n", ""), savedMessage.getBody());
    assertEquals("componentId", savedMessage.getComponentId());
    assertFalse(savedMessage.isModerated());
    assertEquals(textEmailContent.substring(0, 200), savedMessage.getSummary());
    assertEquals("bart.simpson@silverpeas.com", savedMessage.getSender());
    assertEquals("0000001747b40c95", savedMessage.getMessageId());
    assertThat(1204450455000L, is(lessThanOrEqualTo(savedMessage.getSentDate().getTime())));
    assertEquals("Simple database message 3", savedMessage.getTitle());
    assertEquals("text/plain", savedMessage.getContentType());
    assertEquals(2008, savedMessage.getYear());
    assertEquals(Calendar.MARCH, savedMessage.getMonth());
    assertEquals("3", savedMessage.getId());
    assertEquals(1, savedMessage.getVersion());
    assertEquals(0, savedMessage.getAttachmentsSize());
    assertNotNull(savedMessage.getAttachments());
    messageService.moderateMessage("3");
    savedMessage = messageService.getMessage("3");
    assertNotNull(savedMessage);
    assertTrue(savedMessage.isModerated());
    assertEquals(textEmailContent.replaceAll("\n", ""), savedMessage.getBody());
    assertEquals("componentId", savedMessage.getComponentId());
    assertEquals(textEmailContent.substring(0, 200), savedMessage.getSummary());
    assertEquals("bart.simpson@silverpeas.com", savedMessage.getSender());
    assertEquals("0000001747b40c95", savedMessage.getMessageId());
    assertThat(1204450455000L, is(lessThanOrEqualTo(savedMessage.getSentDate().getTime())));
    assertEquals("Simple database message 3", savedMessage.getTitle());
    assertEquals("text/plain", savedMessage.getContentType());
    assertEquals(2008, savedMessage.getYear());
    assertEquals(Calendar.MARCH, savedMessage.getMonth());
    assertEquals("3", savedMessage.getId());
    assertEquals(2, savedMessage.getVersion());
    assertEquals(0, savedMessage.getAttachmentsSize());
    assertNotNull(savedMessage.getAttachments());
  }

  @Test
  public void getNumberOfPagesForAllMessages() {
    MailingList mailingList = new MailingList();
    mailingList.setComponentId("componentId");
    long pages = messageService.getNumberOfPagesForAllMessages(mailingList);
    assertEquals(1, pages);
    messageService.setElementsPerPage(1);
    pages = messageService.getNumberOfPagesForAllMessages(mailingList);
    assertEquals(3, pages);
    messageService.setElementsPerPage(10);
  }

  @Test
  public void getActivity() {
    MailingList mailingList = new MailingList();
    mailingList.setComponentId("componentId");
    MailingListActivity activity = messageService.getActivity(mailingList);
    assertNotNull(activity);
    assertNotNull(activity.getMessages());
    assertEquals(2, activity.getMessages().size());
    String[] messageIds = {"1", "2"};
    assertThat(activity.getMessages().get(0).getId(), is(in(messageIds)));
    assertThat(activity.getMessages().get(1).getId(), is(in(messageIds)));
  }

  @BeforeClass
  public static void setUpTimezone() {
    defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void onSetUp() {
    this.messageService = MessageService.get();
  }

  @AfterClass
  public static void restoreTimeZone() {
    TimeZone.setDefault(defaultTimeZone);
  }

}
