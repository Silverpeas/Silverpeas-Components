/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.silverpeas.mailinglist.service.model;

import com.silverpeas.mailinglist.AbstractSilverpeasDatasourceSpringContextTests;
import com.silverpeas.mailinglist.service.model.beans.Attachment;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.silverpeas.mailinglist.service.model.beans.MailingListActivity;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.silverpeas.mailinglist.service.util.OrderBy;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;

@ContextConfiguration(locations = {"/spring-notification.xml", "/spring-checker.xml",
  "/spring-hibernate.xml", "/spring-datasource.xml"})
public class TestMessageService extends AbstractSilverpeasDatasourceSpringContextTests {

  private static final OrderBy orderByDate = new OrderBy("sentDate", false);
  private static final String textEmailContent = "Bonjour famille Simpson, " +
       "j'espère que vous allez bien. Ici tout se passe bien et Krusty est très " +
       "sympathique. Surtout depuis que Tahiti Bob est retourné en prison. Je " +
       "dois remplacer l'homme canon dans la prochaine émission.\nBart";
  private static final String attachmentPath = "c:\\tmp\\uploads\\componentId\\mailId@silverpeas.com\\";
  
  @Inject
  private MessageService messageService;

  @Test
  public void testGetMessage() {
    Message savedMessage = messageService.getMessage("1");
    assertNotNull(savedMessage);
    assertEquals(textEmailContent.replaceAll("\n", ""), savedMessage.getBody());
    assertEquals("componentId", savedMessage.getComponentId());
    assertEquals(true, savedMessage.isModerated());
    assertEquals(textEmailContent.substring(0, 200), savedMessage.getSummary());
    assertEquals("bart.simpson@silverpeas.com", savedMessage.getSender());
    assertEquals("0000001747b40c8d", savedMessage.getMessageId());
    assertEquals(1204364055000l, savedMessage.getSentDate().getTime());
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
  public void testSaveMessage() {
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
    assertEquals(true, savedMessage.isModerated());
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
  public void testResaveMessage() {
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
    assertEquals(true, savedMessage.isModerated());
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

    message.getAttachments().add(attachment);
    String newId2 = messageService.saveMessage(message);
    assertNotNull(newId2);
    assertNotSame(id, newId2);
  }

  @Test
  public void testListMessages() {
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
  public void testGetTotalNumberOfMessages() {
    MailingList mailingList = new MailingList();
    mailingList.setComponentId("componentId");
    assertEquals(3, messageService.getTotalNumberOfMessages(mailingList));
  }

  @Test
  public void testListDisplayableMessages() {
    MailingList mailingList = new MailingList();
    mailingList.setComponentId("componentId");
    List<Message> messages = messageService.listDisplayableMessages(mailingList, -1, -1,
        0, orderByDate);
    assertNotNull(messages);
    assertEquals(2, messages.size());
    assertEquals("1", messages.get(0).getId());
    assertEquals("2", messages.get(1).getId());
    messages = messageService.listDisplayableMessages(mailingList, -1, 2008, 0,
        orderByDate);
    assertNotNull(messages);
    assertEquals(2, messages.size());
    assertEquals("1", messages.get(0).getId());
    assertEquals("2", messages.get(1).getId());
    messages = messageService.listDisplayableMessages(mailingList,
        Calendar.FEBRUARY, 2008, 0, orderByDate);
    assertNotNull(messages);
    assertEquals(1, messages.size());
    assertEquals("2", messages.get(0).getId());
    messages = messageService.listDisplayableMessages(mailingList,
        Calendar.MARCH, 2008, 0, orderByDate);
    assertNotNull(messages);
    assertEquals(1, messages.size());
    assertEquals("1", messages.get(0).getId());
    messages = messageService.listDisplayableMessages(mailingList, -1, 2007, 0,
        orderByDate);
    assertNotNull(messages);
    assertEquals(0, messages.size());
    messages = messageService.listDisplayableMessages(mailingList, 2,
        orderByDate);
    assertNotNull(messages);
    assertEquals(2, messages.size());
    assertEquals("1", messages.get(0).getId());
    assertEquals("2", messages.get(1).getId());
  }

  @Test
  public void testListUnmoderatedeMessages() {
    MailingList mailingList = new MailingList();
    mailingList.setComponentId("componentId");
    List<Message> messages = messageService.listUnmoderatedeMessages(mailingList, 0,
        orderByDate);
    assertNotNull(messages);
    assertEquals(1, messages.size());
    assertEquals("3", messages.get(0).getId());
  }

  @Test
  public void testGetNumberOfPagesForUnmoderatedMessages() {
    MailingList mailingList = new MailingList();
    mailingList.setComponentId("componentId");
    int pages = messageService.getNumberOfPagesForUnmoderatedMessages(mailingList);
    assertEquals(1, pages);
    messageService.setElementsPerPage(1);
    pages = messageService.getNumberOfPagesForUnmoderatedMessages(mailingList);
    assertEquals(1, pages);
    messageService.setElementsPerPage(10);
  }

  @Test
  public void testGetNumberOfPagesForDisplayableMessages() {
    MailingList mailingList = new MailingList();
    mailingList.setComponentId("componentId");
    int pages = messageService.getNumberOfPagesForDisplayableMessages(mailingList);
    assertEquals(1, pages);
    messageService.setElementsPerPage(1);
    pages = messageService.getNumberOfPagesForDisplayableMessages(mailingList);
    assertEquals(2, pages);
    messageService.setElementsPerPage(10);
  }

  @Test
  public void testDeleteMessage() {
    Message savedMessage = messageService.getMessage("1");
    assertNotNull(savedMessage);
    assertEquals(textEmailContent.replaceAll("\n", ""), savedMessage.getBody());
    assertEquals("componentId", savedMessage.getComponentId());
    assertEquals(true, savedMessage.isModerated());
    assertEquals(textEmailContent.substring(0, 200), savedMessage.getSummary());
    assertEquals("bart.simpson@silverpeas.com", savedMessage.getSender());
    assertEquals("0000001747b40c8d", savedMessage.getMessageId());
    assertEquals(1204364055000l, savedMessage.getSentDate().getTime());
    assertEquals("Simple database message", savedMessage.getTitle());
    assertEquals("text/plain", savedMessage.getContentType());
    assertEquals(2008, savedMessage.getYear());
    assertEquals(Calendar.MARCH, savedMessage.getMonth());
    assertEquals("1", savedMessage.getId());
    assertEquals(1, savedMessage.getVersion());
    assertEquals(10000, savedMessage.getAttachmentsSize());
    assertNotNull(savedMessage.getAttachments());
    assertEquals(1, savedMessage.getAttachments().size());
    Attachment attached = (Attachment) savedMessage.getAttachments().iterator().next();
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
  public void testModerateMessage() {
    Message savedMessage = messageService.getMessage("3");
    assertNotNull(savedMessage);
    assertEquals(textEmailContent.replaceAll("\n", ""), savedMessage.getBody());
    assertEquals("componentId", savedMessage.getComponentId());
    assertEquals(false, savedMessage.isModerated());
    assertEquals(textEmailContent.substring(0, 200), savedMessage.getSummary());
    assertEquals("bart.simpson@silverpeas.com", savedMessage.getSender());
    assertEquals("0000001747b40c95", savedMessage.getMessageId());
    assertEquals(1204450455000l, savedMessage.getSentDate().getTime());
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
    assertEquals(true, savedMessage.isModerated());
    assertEquals(textEmailContent.replaceAll("\n", ""), savedMessage.getBody());
    assertEquals("componentId", savedMessage.getComponentId());
    assertEquals(textEmailContent.substring(0, 200), savedMessage.getSummary());
    assertEquals("bart.simpson@silverpeas.com", savedMessage.getSender());
    assertEquals("0000001747b40c95", savedMessage.getMessageId());
    assertEquals(1204450455000l, savedMessage.getSentDate().getTime());
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
  public void testGetNumberOfPagesForAllMessages() {
    MailingList mailingList = new MailingList();
    mailingList.setComponentId("componentId");
    int pages = messageService.getNumberOfPagesForAllMessages(mailingList);
    assertEquals(1, pages);
    messageService.setElementsPerPage(1);
    pages = messageService.getNumberOfPagesForAllMessages(mailingList);
    assertEquals(3, pages);
    messageService.setElementsPerPage(10);
  }

  @Test
  public void testGetActivity() {
    complexSetUp();
    MailingList mailingList = new MailingList();
    mailingList.setComponentId("componentId");
    MailingListActivity activity = messageService.getActivity(mailingList);
    assertNotNull(activity);
    assertNotNull(activity.getMessages());
    assertEquals(5, activity.getMessages().size());
    assertEquals("3", activity.getMessages().get(0).getId());
    assertEquals("4", activity.getMessages().get(1).getId());
    assertEquals("1", activity.getMessages().get(2).getId());
    assertEquals("10", activity.getMessages().get(3).getId());
    assertEquals("2", activity.getMessages().get(4).getId());
  }

  protected void complexSetUp() {
    IDatabaseConnection connection = null;
    try {
      connection = getConnection();
      DatabaseOperation.DELETE_ALL.execute(connection, getDataSet());
      DatabaseOperation.CLEAN_INSERT.execute(connection, getComplexDataSet());
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      if (connection != null) {
        try {
          connection.getConnection().close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Override
  protected IDataSet getDataSet() throws Exception {
    if (isOracle()) {
      return new ReplacementDataSet(new FlatXmlDataSet(TestMessageService.class.getResourceAsStream(
          "test-message-service-oracle-dataset.xml")));
    }
    return new ReplacementDataSet(new FlatXmlDataSet(TestMessageService.class.getResourceAsStream(
        "test-message-service-dataset.xml")));
  }

  protected IDataSet getComplexDataSet() throws DataSetException, IOException {
    if (isOracle()) {
      return new ReplacementDataSet(new FlatXmlDataSet(TestMessageService.class.getResourceAsStream(
          "test-message-service-complex-oracle-dataset.xml")));
    }
    return new ReplacementDataSet(new FlatXmlDataSet(TestMessageService.class.getResourceAsStream(
        "test-message-service-complex-dataset.xml")));
  }

  @After
  public void onTearDown() throws Exception {
    try {
      FileFolderManager.deleteFolder("c:\\tmp\\uploads\\componentId", false);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    super.onTearDown();
  }
}
