/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.components.mailinglist.service.model.dao;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.components.mailinglist.MailingListWarBuilder;
import org.silverpeas.components.mailinglist.service.model.beans.Activity;
import org.silverpeas.components.mailinglist.service.model.beans.Attachment;
import org.silverpeas.components.mailinglist.service.model.beans.Message;
import org.silverpeas.components.mailinglist.service.util.OrderBy;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.test.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.util.file.FileFolderManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class MessageDaoIT {

  private static final int ATT_SIZE = 86199;
  private static final OrderBy orderByDate = new OrderBy("sentDate", false);
  private static final String textEmailContent =
      "Bonjour famille Simpson, j'espère que vous allez bien. " +
          "Ici tout se passe bien et Krusty est très sympathique. Surtout " +
          "depuis que Tahiti Bob est retourné en prison. Je dois remplacer" +
          "l'homme canon dans la prochaine émission.\nBart";
  private static final String attachmentPath = getAttachmentPath();
  private static final Path COPY_PATH =
      Paths.get("org/silverpeas/components/mailinglist/service/job/lemonde.html");

  private String copyPath;


  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom("create-database.sql");

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectoryRule = new MavenTargetDirectoryRule(this);

  @Deployment
  public static Archive<?> createTestArchive() {
    return MailingListWarBuilder.onWarForTestClass(MessageDaoIT.class).build();
  }

  @Before
  public void setUpTest() {
    copyPath =
        Paths.get(mavenTargetDirectoryRule.getResourceTestDirFile().getPath(), COPY_PATH.toString())
            .toString();
  }

  @After
  public void tearDownTest() {
    FileFolderManager.deleteFolder(getUploadPath());
  }


  private MessageDao getMessageDAO() {
    return MessageDao.get();
  }

  protected void copyFile(String filePath, String targetPath) throws IOException {
    FileUtils.copyFile(new File(filePath), new File(targetPath));
  }

  @Test
  public void createSimpleMessage() {
    Calendar sentDate = Calendar.getInstance();
    String id = Transaction.performInOne(() -> {
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
      return getMessageDAO().saveMessage(message);
    });
    assertNotNull(id);
    Message savedMessage = getMessageDAO().findMessageById(id);
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
    assertEquals("text/plain", savedMessage.getContentType());
  }

  @Test
  public void createSimpleMessageWithCompleteId() {
    MessageDao messageDao = getMessageDAO();
    Calendar sentDate = Calendar.getInstance();
    String id = Transaction.performInOne(() -> {
      Message message = new Message();
      message.setBody(textEmailContent);
      message.setComponentId("componentId");
      message.setModerated(true);
      message.setSummary(textEmailContent.substring(0, 200));
      message.setSender("bart.simpson@ilverpeas.com");
      message.setMessageId("<510A99E9.5060702@silverpeas.com>");
      message.setSentDate(sentDate.getTime());
      message.setTitle("Simple text message");
      message.setContentType("text/plain");
      return messageDao.saveMessage(message);
    });
    assertNotNull(id);
    Message savedMessage = messageDao.findMessageById(id);
    assertNotNull(savedMessage);
    assertEquals(textEmailContent, savedMessage.getBody());
    assertEquals("componentId", savedMessage.getComponentId());
    assertTrue(savedMessage.isModerated());
    assertEquals(textEmailContent.substring(0, 200), savedMessage.getSummary());
    assertEquals("bart.simpson@ilverpeas.com", savedMessage.getSender());
    assertEquals("<510A99E9.5060702@silverpeas.com>", savedMessage.getMessageId());
    assertEquals(sentDate.getTime(), savedMessage.getSentDate());
    assertEquals("Simple text message", savedMessage.getTitle());
    assertEquals(sentDate.get(Calendar.YEAR), savedMessage.getYear());
    assertEquals(sentDate.get(Calendar.MONTH), savedMessage.getMonth());
    assertEquals(id, savedMessage.getId());
    assertEquals(0, savedMessage.getVersion());
    assertEquals("text/plain", savedMessage.getContentType());
  }

  @Test
  public void recreateSimpleMessage() {
    MessageDao messageDao = getMessageDAO();
    Calendar sentDate = Calendar.getInstance();
    String id = Transaction.performInOne(() -> {
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
      return messageDao.saveMessage(message);
    });

    assertNotNull(id);
    Message savedMessage = messageDao.findMessageById(id);
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
    assertEquals("text/plain", savedMessage.getContentType());
    String newId = Transaction.performInOne(() -> {
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
      return messageDao.saveMessage(message);
    });
    assertNotNull(newId);
    assertEquals(id, newId);
  }

  @Test
  public void createSimpleHtmlMessage() {
    MessageDao messageDao = getMessageDAO();
    Calendar sentDate = Calendar.getInstance();
    Message message = new Message();
    String html = "<html><head></head><body>Here is a simple HTML content</body></html>";
    String id = Transaction.performInOne(() -> {
      message.setBody(html);
      message.setComponentId("componentId");
      message.setModerated(true);
      message.setSummary(textEmailContent.substring(0, 200));
      message.setSender("bart.simpson@ilverpeas.com");
      message.setMessageId("0000001747b40c10");
      message.setSentDate(sentDate.getTime());
      message.setContentType("text/html");
      message.setTitle("Simple html message");
      return messageDao.saveMessage(message);
    });
    assertNotNull(id);
    Message savedMessage = messageDao.findMessageById(id);
    assertNotNull(savedMessage);
    assertEquals(html, savedMessage.getBody());
    assertEquals("componentId", savedMessage.getComponentId());
    assertTrue(savedMessage.isModerated());
    assertEquals(textEmailContent.substring(0, 200), savedMessage.getSummary());
    assertEquals("bart.simpson@ilverpeas.com", savedMessage.getSender());
    assertEquals("0000001747b40c10", savedMessage.getMessageId());
    assertEquals(sentDate.getTime(), savedMessage.getSentDate());
    assertEquals("Simple html message", savedMessage.getTitle());
    assertEquals(sentDate.get(Calendar.YEAR), savedMessage.getYear());
    assertEquals(sentDate.get(Calendar.MONTH), savedMessage.getMonth());
    assertEquals(id, savedMessage.getId());
    assertEquals(0, savedMessage.getVersion());
    assertEquals("text/html", savedMessage.getContentType());
  }

  @Test
  public void createMessageWithAttachments() {
    MessageDao messageDao = getMessageDAO();
    Calendar sentDate = Calendar.getInstance();
    Message message = new Message();
    String id = Transaction.performInOne(() -> {
      message.setBody(textEmailContent);
      message.setComponentId("componentId");
      message.setModerated(true);
      message.setSummary(textEmailContent.substring(0, 200));
      message.setSender("bart.simpson@ilverpeas.com");
      message.setMessageId("0000001747b40c11");
      message.setSentDate(sentDate.getTime());
      message.setTitle("Simple text message");
      message.setContentType("text/plain");
      Attachment attachment = new Attachment();
      attachment.setPath(attachmentPath + "lemonde.html");
      attachment.setFileName("lemonde.html");
      attachment.setSize(10000);
      message.getAttachments().add(attachment);
      return messageDao.saveMessage(message);
    });
    assertNotNull(id);
    Message savedMessage = messageDao.findMessageById(id);
    assertNotNull(savedMessage);
    assertEquals(textEmailContent, savedMessage.getBody());
    assertEquals("componentId", savedMessage.getComponentId());
    assertTrue(savedMessage.isModerated());
    assertEquals(textEmailContent.substring(0, 200), savedMessage.getSummary());
    assertEquals("bart.simpson@ilverpeas.com", savedMessage.getSender());
    assertEquals("0000001747b40c11", savedMessage.getMessageId());
    assertEquals(sentDate.getTime(), savedMessage.getSentDate());
    assertEquals("Simple text message", savedMessage.getTitle());
    assertEquals(sentDate.get(Calendar.YEAR), savedMessage.getYear());
    assertEquals(sentDate.get(Calendar.MONTH), savedMessage.getMonth());
    assertEquals("text/plain", savedMessage.getContentType());
    assertEquals(id, savedMessage.getId());
    assertEquals(0, savedMessage.getVersion());
    assertEquals(10000, savedMessage.getAttachmentsSize());
    assertNotNull(savedMessage.getAttachments());
    assertEquals(1, savedMessage.getAttachments().size());
    Attachment attached = savedMessage.getAttachments().iterator().next();
    assertNotNull(attached);
    assertEquals(0, attached.getVersion());
    assertNotNull(attached.getId());
    assertEquals(10000, attached.getSize());
    assertEquals("lemonde.html", attached.getFileName());
    assertEquals(attachmentPath + "lemonde.html", attached.getPath());
  }

  @Test
  public void updateMessage() {
    MessageDao messageDao = getMessageDAO();
    Calendar sentDate = Calendar.getInstance();
    String id = Transaction.performInOne(() -> {
      Message message = new Message();
      message.setBody(textEmailContent);
      message.setComponentId("componentId");
      message.setModerated(true);
      message.setSummary(textEmailContent.substring(0, 200));
      message.setSender("bart.simpson@ilverpeas.com");
      message.setMessageId("0000001747b40c13");
      message.setSentDate(sentDate.getTime());
      message.setTitle("Simple text message");
      return messageDao.saveMessage(message);
    });
    assertNotNull(id);
    Transaction.performInOne(() -> {
      Message savedMessage = messageDao.findMessageById(id);
      assertNotNull(savedMessage);
      assertEquals(textEmailContent, savedMessage.getBody());
      assertEquals("componentId", savedMessage.getComponentId());
      assertTrue(savedMessage.isModerated());
      assertEquals(textEmailContent.substring(0, 200), savedMessage.getSummary());
      assertEquals("bart.simpson@ilverpeas.com", savedMessage.getSender());
      assertEquals("0000001747b40c13", savedMessage.getMessageId());
      assertEquals(sentDate.getTime(), savedMessage.getSentDate());
      assertEquals("Simple text message", savedMessage.getTitle());
      assertEquals(sentDate.get(Calendar.YEAR), savedMessage.getYear());
      assertEquals(sentDate.get(Calendar.MONTH), savedMessage.getMonth());
      assertEquals(id, savedMessage.getId());
      savedMessage.setModerated(false);
      messageDao.updateMessage(savedMessage);
      return null;
    });
    Message updatedMessage = messageDao.findMessageById(id);
    assertNotNull(updatedMessage);
    assertEquals(1, updatedMessage.getVersion());
    assertEquals(textEmailContent, updatedMessage.getBody());
    assertEquals("componentId", updatedMessage.getComponentId());
    assertFalse(updatedMessage.isModerated());
    assertEquals(textEmailContent.substring(0, 200), updatedMessage.getSummary());
    assertEquals("bart.simpson@ilverpeas.com", updatedMessage.getSender());
    assertEquals("0000001747b40c13", updatedMessage.getMessageId());
    assertEquals(sentDate.getTime(), updatedMessage.getSentDate());
    assertEquals("Simple text message", updatedMessage.getTitle());
    assertEquals(sentDate.get(Calendar.YEAR), updatedMessage.getYear());
    assertEquals(sentDate.get(Calendar.MONTH), updatedMessage.getMonth());
    assertEquals(id, updatedMessage.getId());
  }

  @Test
  public void updateMessageWithAttachment() {
    MessageDao messageDao = getMessageDAO();
    Calendar sentDate = Calendar.getInstance();
    String id = Transaction.performInOne(() -> {
      Message message = new Message();
      message.setBody(textEmailContent);
      message.setComponentId("componentId");
      message.setModerated(true);
      message.setSummary(textEmailContent.substring(0, 200));
      message.setSender("bart.simpson@ilverpeas.com");
      message.setMessageId("0000001747b40c14");
      message.setSentDate(sentDate.getTime());
      message.setTitle("Simple text message");
      Attachment attachment = new Attachment();
      attachment.setPath(attachmentPath + "lemonde.html");
      attachment.setFileName("lemonde.html");
      attachment.setSize(10000);
      message.getAttachments().add(attachment);
      return messageDao.saveMessage(message);
    });
    assertNotNull(id);
    Transaction.performInOne(() -> {
      Message savedMessage = messageDao.findMessageById(id);
      assertNotNull(savedMessage);
      assertEquals(textEmailContent, savedMessage.getBody());
      assertEquals("componentId", savedMessage.getComponentId());
      assertTrue(savedMessage.isModerated());
      assertEquals(textEmailContent.substring(0, 200), savedMessage.getSummary());
      assertEquals("bart.simpson@ilverpeas.com", savedMessage.getSender());
      assertEquals("0000001747b40c14", savedMessage.getMessageId());
      assertEquals(sentDate.getTime(), savedMessage.getSentDate());
      assertEquals("Simple text message", savedMessage.getTitle());
      assertEquals(sentDate.get(Calendar.YEAR), savedMessage.getYear());
      assertEquals(sentDate.get(Calendar.MONTH), savedMessage.getMonth());
      assertEquals(id, savedMessage.getId());
      assertEquals(10000, savedMessage.getAttachmentsSize());
      assertNotNull(savedMessage.getAttachments());
      assertEquals(1, savedMessage.getAttachments().size());
      Attachment attached = savedMessage.getAttachments().iterator().next();
      assertNotNull(attached);
      assertEquals(0, attached.getVersion());
      assertNotNull(attached.getId());
      assertEquals(10000, attached.getSize());
      assertEquals("lemonde.html", attached.getFileName());
      assertEquals(attachmentPath + "lemonde.html", attached.getPath());
      attached.setSize(20000);
      savedMessage.setModerated(false);
      messageDao.updateMessage(savedMessage);
      return null;
    });
    Message updatedMessage = messageDao.findMessageById(id);
    assertEquals(1, updatedMessage.getVersion());
    assertNotNull(updatedMessage);
    assertEquals(textEmailContent, updatedMessage.getBody());
    assertEquals("componentId", updatedMessage.getComponentId());
    assertFalse(updatedMessage.isModerated());
    assertEquals(textEmailContent.substring(0, 200), updatedMessage.getSummary());
    assertEquals("bart.simpson@ilverpeas.com", updatedMessage.getSender());
    assertEquals("0000001747b40c14", updatedMessage.getMessageId());
    assertEquals(sentDate.getTime(), updatedMessage.getSentDate());
    assertEquals("Simple text message", updatedMessage.getTitle());
    assertEquals(sentDate.get(Calendar.YEAR), updatedMessage.getYear());
    assertEquals(sentDate.get(Calendar.MONTH), updatedMessage.getMonth());
    assertEquals(id, updatedMessage.getId());
    assertEquals(20000, updatedMessage.getAttachmentsSize());
    assertNotNull(updatedMessage.getAttachments());
    assertEquals(1, updatedMessage.getAttachments().size());
    Attachment attached = updatedMessage.getAttachments().iterator().next();
    assertNotNull(attached);
    assertEquals(1, attached.getVersion());
    assertNotNull(attached.getId());
    assertEquals(20000, attached.getSize());
    assertEquals("lemonde.html", attached.getFileName());
    assertEquals(attachmentPath + "lemonde.html", attached.getPath());
  }

  @Test
  public void deleteMessageWithAttachments() throws Exception {
    MessageDao messageDao = getMessageDAO();
    copyFile(copyPath, attachmentPath + "lemonde2.html");
    copyFile(copyPath, attachmentPath + "lemonde.html");
    Calendar sentDate = Calendar.getInstance();
    String id = Transaction.performInOne(() -> {
      Message message = new Message();
      message.setBody(textEmailContent);
      message.setComponentId("componentId");
      message.setModerated(true);
      message.setSummary(textEmailContent.substring(0, 200));
      message.setSender("bart.simpson@ilverpeas.com");
      message.setMessageId("0000001747b40c15");
      message.setSentDate(sentDate.getTime());
      message.setTitle("Simple text message");

      Attachment attachment = new Attachment();
      attachment.setPath(attachmentPath + "lemonde.html");
      attachment.setFileName("lemonde.html");
      attachment.setSize(10000);
      message.getAttachments().add(attachment);
      attachment = new Attachment();
      attachment.setPath(attachmentPath + "lemonde2.html");
      attachment.setFileName("lemonde2.html");
      attachment.setSize(20000);
      message.getAttachments().add(attachment);
      return messageDao.saveMessage(message);
    });

    assertNotNull(id);
    Message msg = Transaction.performInOne(() -> {
      Message savedMessage = messageDao.findMessageById(id);
      assertEquals(1, countRowsInTable("SC_MAILINGLIST_MESSAGE"));
      assertEquals(2, countRowsInTable("SC_MAILINGLIST_ATTACHMENT"));
      assertNotNull(savedMessage);
      assertEquals(textEmailContent, savedMessage.getBody());
      assertEquals("componentId", savedMessage.getComponentId());
      assertTrue(savedMessage.isModerated());
      assertEquals(textEmailContent.substring(0, 200), savedMessage.getSummary());
      assertEquals("bart.simpson@ilverpeas.com", savedMessage.getSender());
      assertEquals("0000001747b40c15", savedMessage.getMessageId());
      assertEquals(sentDate.getTime(), savedMessage.getSentDate());
      assertEquals("Simple text message", savedMessage.getTitle());
      assertEquals(sentDate.get(Calendar.YEAR), savedMessage.getYear());
      assertEquals(sentDate.get(Calendar.MONTH), savedMessage.getMonth());
      assertEquals(id, savedMessage.getId());
      assertEquals(0, savedMessage.getVersion());
      assertNotNull(savedMessage.getAttachments());
      assertEquals(2, savedMessage.getAttachments().size());
      assertEquals(172398, savedMessage.getAttachmentsSize());
      messageDao.deleteMessage(savedMessage);
      return messageDao.findMessageById(id);
    });
    assertNull(msg);
    assertEquals(0, countRowsInTable("SC_MAILINGLIST_MESSAGE"));
    assertEquals(0, countRowsInTable("SC_MAILINGLIST_ATTACHMENT"));
    File deletedAttachement = new File(attachmentPath + "lemonde.html");
    assertFalse(deletedAttachement.exists());
    deletedAttachement = new File(attachmentPath + "lemonde2.html");
    assertFalse(deletedAttachement.exists());
  }

  @Test
  public void deleteMessageWithAttachmentShared() throws Exception {
    MessageDao messageDao = getMessageDAO();
    copyFile(copyPath, attachmentPath + "lemonde2.html");
    copyFile(copyPath, attachmentPath + "toto\\lemonde2.html");
    copyFile(copyPath, attachmentPath + "lemonde.html");

    String id = Transaction.performInOne(() -> {
      Calendar sentDate = Calendar.getInstance();
      Message message = new Message();
      message.setBody(textEmailContent);
      message.setComponentId("componentId");
      message.setModerated(true);
      message.setSummary(textEmailContent.substring(0, 200));
      message.setSender("bart.simpson@ilverpeas.com");
      message.setMessageId("0000001747b40c15");
      message.setSentDate(sentDate.getTime());
      message.setTitle("Simple text message");

      Message message2 = new Message();
      message2.setBody(textEmailContent);
      message2.setComponentId("componentId");
      message2.setModerated(true);
      message2.setSummary(textEmailContent.substring(0, 200));
      message2.setSender("bart.simpson@ilverpeas.com");
      message2.setMessageId("0000001747b40e15");
      message2.setSentDate(sentDate.getTime());
      message2.setTitle("Simple text message");

      Attachment attachment = new Attachment();
      attachment.setPath(attachmentPath + "lemonde.html");
      attachment.setFileName("lemonde.html");
      attachment.setSize(10000);
      message.getAttachments().add(attachment);
      attachment = new Attachment();
      attachment.setPath(attachmentPath + "lemonde2.html");
      attachment.setFileName("lemonde2.html");
      attachment.setSize(20000);
      message.getAttachments().add(attachment);

      attachment = new Attachment();
      attachment.setPath(attachmentPath + "toto\\lemonde2.html");
      attachment.setFileName("lemonde2.html");
      attachment.setSize(20000);
      message2.getAttachments().add(attachment);

      String tempId = messageDao.saveMessage(message);
      assertNotNull(tempId);
      String id2 = messageDao.saveMessage(message2);
      assertNotNull(id2);
      assertNotEquals(id2, tempId);
      return tempId;
    });

    Transaction.performInOne(() -> {
      Message savedMessage = messageDao.findMessageById(id);
      assertEquals(2, countRowsInTable("SC_MAILINGLIST_MESSAGE"));
      assertEquals(3, countRowsInTable("SC_MAILINGLIST_ATTACHMENT"));
      messageDao.deleteMessage(savedMessage);
      return null;
    });
    assertEquals(1, countRowsInTable("SC_MAILINGLIST_MESSAGE"));
    assertEquals(1, countRowsInTable("SC_MAILINGLIST_ATTACHMENT"));
    File deletedAttachement = new File(attachmentPath + "lemonde.html");
    assertFalse(deletedAttachement.exists());
    deletedAttachement = new File(attachmentPath + "lemonde2.html");
    assertTrue(deletedAttachement.exists());
    deletedAttachement = new File(attachmentPath + "toto\\lemonde2.html");
    assertFalse(deletedAttachement.exists());
  }

  @Test
  public void listMessagesOfMailingList() throws Exception {
    MessageDao messageDao = getMessageDAO();
    Calendar sentDate = Calendar.getInstance();
    Transaction.performInOne(() -> {

      sentDate.set(Calendar.YEAR, 2008);
      sentDate.set(Calendar.MONTH, Calendar.MARCH);
      sentDate.set(Calendar.DAY_OF_MONTH, 15);
      sentDate.set(Calendar.HOUR_OF_DAY, 9);
      sentDate.set(Calendar.MINUTE, 0);
      sentDate.set(Calendar.SECOND, 0);
      sentDate.set(Calendar.MILLISECOND, 0);
      Message message = new Message();
      message.setBody(textEmailContent);
      message.setComponentId("componentId");
      message.setModerated(true);
      message.setSummary(textEmailContent.substring(0, 200));
      message.setSender("bart.simpson@ilverpeas.com");
      message.setMessageId("0000001747b40c16");
      message.setSentDate(sentDate.getTime());
      message.setTitle("Simple text message");
      Attachment attachment = new Attachment();
      attachment.setPath(attachmentPath + "lemonde.html");
      attachment.setFileName("lemonde.html");
      attachment.setSize(10000);
      message.getAttachments().add(attachment);
      attachment = new Attachment();
      attachment.setPath(attachmentPath + "lemonde2.html");
      attachment.setFileName("lemonde2.html");
      attachment.setSize(20000);
      message.getAttachments().add(attachment);
      messageDao.saveMessage(message);

      message = new Message();
      message.setBody(textEmailContent);
      message.setComponentId("componentId");
      message.setModerated(false);
      message.setSummary(textEmailContent.substring(0, 200));
      message.setSender("bart.simpson@ilverpeas.com");
      message.setMessageId("0000001747b40c17");
      message.setSentDate(sentDate.getTime());
      message.setTitle("Simple text message 2");
      messageDao.saveMessage(message);

      sentDate.set(Calendar.YEAR, 2008);
      sentDate.set(Calendar.MONTH, Calendar.FEBRUARY);
      sentDate.set(Calendar.DAY_OF_MONTH, 17);
      sentDate.set(Calendar.HOUR_OF_DAY, 9);
      sentDate.set(Calendar.MINUTE, 0);
      sentDate.set(Calendar.SECOND, 0);
      sentDate.set(Calendar.MILLISECOND, 0);
      message = new Message();
      message.setBody(textEmailContent);
      message.setComponentId("componentId");
      message.setModerated(true);
      message.setSummary(textEmailContent.substring(0, 200));
      message.setSender("bart.simpson@ilverpeas.com");
      message.setMessageId("0000001747b40c18");
      message.setSentDate(sentDate.getTime());
      message.setTitle("Simple text message 3");
      messageDao.saveMessage(message);
      return null;
    });
    List<Message> messages =
        messageDao.listAllMessagesOfMailingList("componentId", 0, 10, orderByDate);
    assertNotNull(messages);
    assertEquals(3, messages.size());
    assertEquals(3, countRowsInTable("SC_MAILINGLIST_MESSAGE"));
    assertEquals(2, countRowsInTable("SC_MAILINGLIST_ATTACHMENT"));
    messages = messageDao.listAllMessagesOfMailingList("componentId", 0, 2, orderByDate);
    assertNotNull(messages);
    assertEquals(2, messages.size());
    messages = messageDao.listAllMessagesOfMailingList("componentId", 1, 2, orderByDate);
    assertNotNull(messages);
    assertEquals(1, messages.size());

    messages =
        messageDao.listDisplayableMessagesOfMailingList("componentId", -1, -1, 0, 10, orderByDate);
    assertNotNull(messages);
    assertEquals(2, messages.size());
    messages = messageDao.listDisplayableMessagesOfMailingList("componentId", -1, 2008, 0, 10,
        orderByDate);
    assertNotNull(messages);
    assertEquals(2, messages.size());
    messages = messageDao.listDisplayableMessagesOfMailingList("componentId", -1, 2007, 0, 10,
        orderByDate);
    assertNotNull(messages);
    assertEquals(0, messages.size());
    messages =
        messageDao.listDisplayableMessagesOfMailingList("componentId", Calendar.MARCH, 2008, 0, 10,
            orderByDate);
    assertNotNull(messages);
    assertEquals(1, messages.size());
    messages =
        messageDao.listDisplayableMessagesOfMailingList("componentId", Calendar.FEBRUARY, 2008, 0,
            10, orderByDate);
    assertNotNull(messages);
    assertEquals(1, messages.size());
    messages =
        messageDao.listDisplayableMessagesOfMailingList("componentId", -1, -1, 0, 1, orderByDate);
    assertNotNull(messages);
    assertEquals(1, messages.size());
    messages =
        messageDao.listDisplayableMessagesOfMailingList("componentId", -1, -1, 1, 1, orderByDate);
    assertNotNull(messages);
    assertEquals(1, messages.size());

    messages = messageDao.listUnmoderatedMessagesOfMailingList("componentId", 0, 10, orderByDate);
    assertNotNull(messages);
    assertEquals(1, messages.size());
    messages = messageDao.listUnmoderatedMessagesOfMailingList("componentId", 0, 1, orderByDate);
    assertNotNull(messages);
    assertEquals(1, messages.size());
    messages = messageDao.listUnmoderatedMessagesOfMailingList("componentId", 1, 1, orderByDate);
    assertNotNull(messages);
    assertEquals(0, messages.size());

    assertEquals(3, countRowsInTable("SC_MAILINGLIST_MESSAGE"));
    assertEquals(2, countRowsInTable("SC_MAILINGLIST_ATTACHMENT"));

    long unmoderatedMessages = messageDao.listTotalNumberOfUnmoderatedMessages("componentId");
    assertEquals(1, unmoderatedMessages);
    long displayabledMessages = messageDao.listTotalNumberOfDisplayableMessages("componentId");
    assertEquals(2, displayabledMessages);
    long totalMessages = messageDao.listTotalNumberOfMessages("componentId");
    assertEquals(3, totalMessages);

  }

  @Test
  public void listActivityMessages() throws Exception {
    MessageDao messageDao = getMessageDAO();
    Calendar sentDate = Calendar.getInstance();
    Date message1SentDate = sentDate.getTime();
    sentDate.add(Calendar.DAY_OF_MONTH, -1);
    Date message2SentDate = sentDate.getTime();
    sentDate.add(Calendar.DAY_OF_MONTH, -1);
    Date message3SentDate = sentDate.getTime();
    Transaction.performInOne(() -> {
      Message message = new Message();
      message.setBody(textEmailContent);
      message.setComponentId("componentId");
      message.setModerated(true);
      message.setSummary(textEmailContent.substring(0, 200));
      message.setSender("bart.simpson@ilverpeas.com");
      message.setMessageId("0000001747b40c19");
      message.setSentDate(message1SentDate);
      message.setTitle("Simple text message");
      Attachment attachment = new Attachment();
      attachment.setPath(attachmentPath + "le_monde.html");
      attachment.setFileName("le_monde.html");
      attachment.setSize(10000);
      message.getAttachments().add(attachment);
      attachment = new Attachment();
      attachment.setPath(attachmentPath + "le_monde_2.html");
      attachment.setFileName("le_monde_2.html");
      attachment.setSize(20000);
      message.getAttachments().add(attachment);
      messageDao.saveMessage(message);

      message = new Message();
      message.setBody(textEmailContent);
      message.setComponentId("componentId");
      message.setModerated(false);
      message.setSummary(textEmailContent.substring(0, 200));
      message.setSender("bart.simpson@ilverpeas.com");
      message.setMessageId("0000001747b40c20");
      message.setSentDate(message2SentDate);
      message.setTitle("Simple text message 2");
      messageDao.saveMessage(message);

      message = new Message();
      message.setBody(textEmailContent);
      message.setComponentId("componentId");
      message.setModerated(true);
      message.setSummary(textEmailContent.substring(0, 200));
      message.setSender("bart.simpson@ilverpeas.com");
      message.setMessageId("0000001747b40c21");
      message.setSentDate(message3SentDate);
      message.setTitle("Simple text message 3");
      return messageDao.saveMessage(message);
    });

    List<Message> messages = messageDao.listActivityMessages("componentId", 5, orderByDate);
    assertNotNull(messages);
    assertEquals(2, messages.size());
    assertEquals(3, countRowsInTable("SC_MAILINGLIST_MESSAGE"));
    assertEquals(2, countRowsInTable("SC_MAILINGLIST_ATTACHMENT"));
    Message activityMessage = messages.get(0);
    assertEquals(textEmailContent, activityMessage.getBody());
    assertEquals("componentId", activityMessage.getComponentId());
    assertTrue(activityMessage.isModerated());
    assertEquals(textEmailContent.substring(0, 200), activityMessage.getSummary());
    assertEquals("bart.simpson@ilverpeas.com", activityMessage.getSender());
    assertEquals("0000001747b40c19", activityMessage.getMessageId());
    assertEquals(message1SentDate, activityMessage.getSentDate());
    assertEquals("Simple text message", activityMessage.getTitle());
    assertEquals(2, activityMessage.getAttachments().size());
    assertEquals(30000, activityMessage.getAttachmentsSize());

    activityMessage = messages.get(1);
    assertEquals(textEmailContent, activityMessage.getBody());
    assertEquals("componentId", activityMessage.getComponentId());
    assertTrue(activityMessage.isModerated());
    assertEquals(textEmailContent.substring(0, 200), activityMessage.getSummary());
    assertEquals("bart.simpson@ilverpeas.com", activityMessage.getSender());
    assertEquals("0000001747b40c21", activityMessage.getMessageId());
    assertEquals(message3SentDate, activityMessage.getSentDate());
    assertEquals("Simple text message 3", activityMessage.getTitle());
    assertEquals(0, activityMessage.getAttachments().size());
    assertEquals(0, activityMessage.getAttachmentsSize());
  }

  @Test
  public void listActivities() {
    MessageDao messageDao = getMessageDAO();
    Transaction.performInOne(() -> {
      Calendar sentDate = Calendar.getInstance();
      sentDate.set(Calendar.MILLISECOND, 0);
      sentDate.set(Calendar.SECOND, 10);
      sentDate.set(Calendar.MINUTE, 3);
      sentDate.set(Calendar.HOUR, 10);
      sentDate.set(Calendar.DAY_OF_MONTH, 22);
      sentDate.set(Calendar.MONTH, Calendar.FEBRUARY);
      sentDate.set(Calendar.YEAR, 2008);
      Message message = new Message();
      message.setBody(textEmailContent);
      message.setComponentId("componentId");
      message.setModerated(true);
      message.setSummary(textEmailContent.substring(0, 200));
      message.setSender("bart.simpson@ilverpeas.com");
      message.setMessageId("0000001747b40c22");
      Date message1SentDate = sentDate.getTime();
      message.setSentDate(message1SentDate);
      message.setTitle("Simple text message");
      Attachment attachment = new Attachment();
      attachment.setPath(attachmentPath + "lemonde.html");
      attachment.setFileName("lemonde.html");
      attachment.setSize(10000);
      message.getAttachments().add(attachment);
      attachment = new Attachment();
      attachment.setPath(attachmentPath + "lemonde2.html");
      attachment.setFileName("lemonde2.html");
      attachment.setSize(20000);
      message.getAttachments().add(attachment);
      messageDao.saveMessage(message);
      message = new Message();
      message.setBody(textEmailContent);
      message.setComponentId("componentId");
      message.setModerated(true);
      message.setSummary(textEmailContent.substring(0, 200));
      message.setSender("bart.simpson@ilverpeas.com");
      message.setMessageId("0000001747b40c23");
      sentDate.add(Calendar.MONTH, -1);
      Date message2SentDate = sentDate.getTime();
      message.setSentDate(message2SentDate);
      message.setTitle("Simple text message 2");
      messageDao.saveMessage(message);

      message = new Message();
      message.setBody(textEmailContent);
      message.setComponentId("componentId");
      message.setModerated(true);
      message.setSummary(textEmailContent.substring(0, 200));
      message.setSender("bart.simpson@ilverpeas.com");
      message.setMessageId("0000001747b40c24");
      sentDate.add(Calendar.DAY_OF_MONTH, -1);
      Date message3SentDate = sentDate.getTime();
      message.setSentDate(message3SentDate);
      message.setTitle("Simple text message 3");
      messageDao.saveMessage(message);

      message = new Message();
      message.setBody(textEmailContent);
      message.setComponentId("componentId");
      message.setModerated(false);
      message.setSummary(textEmailContent.substring(0, 200));
      message.setSender("bart.simpson@ilverpeas.com");
      message.setMessageId("0000001747b40c25");
      sentDate.add(Calendar.DAY_OF_MONTH, -1);
      message.setSentDate(message3SentDate);
      message.setTitle("Simple text message 4");
      return messageDao.saveMessage(message);
    });


    List<Activity> activities = messageDao.listActivity("componentId");
    assertNotNull(activities);
    assertEquals(2, activities.size());
    for (final Activity activity : activities) {
      assertEquals(2008, activity.getYear());
      if (activity.getMonth() == Calendar.FEBRUARY) {
        assertEquals(1, activity.getNbMessages());
      } else {
        assertEquals(Calendar.JANUARY, activity.getMonth());
        assertEquals(2, activity.getNbMessages());
      }
    }
  }

  @Test
  public void createMessagesWithSameAttachments() {
    MessageDao messageDao = getMessageDAO();
    Calendar sentDate = Calendar.getInstance();
    String id1 = Transaction.performInOne(() -> {
      Message message = new Message();
      message.setBody(textEmailContent);
      message.setComponentId("componentId");
      message.setModerated(true);
      message.setSummary(textEmailContent.substring(0, 200));
      message.setSender("bart.simpson@ilverpeas.com");
      message.setMessageId("0000001747b40c26");
      message.setSentDate(sentDate.getTime());
      message.setTitle("Simple text message");
      message.setContentType("text/plain");
      Attachment attachment = new Attachment();
      attachment.setPath(attachmentPath + "toto" + File.separator + "lemonde.html");
      attachment.setFileName("lemonde.html");
      attachment.setSize(10000);
      copyFile(copyPath, attachmentPath + "toto" + File.separator + "lemonde.html");
      message.getAttachments().add(attachment);
      return messageDao.saveMessage(message);
    });
    assertNotNull(id1);
    Message savedMessage = messageDao.findMessageById(id1);
    assertNotNull(savedMessage);
    assertEquals(textEmailContent, savedMessage.getBody());
    assertEquals("componentId", savedMessage.getComponentId());
    assertTrue(savedMessage.isModerated());
    assertEquals(textEmailContent.substring(0, 200), savedMessage.getSummary());
    assertEquals("bart.simpson@ilverpeas.com", savedMessage.getSender());
    assertEquals("0000001747b40c26", savedMessage.getMessageId());
    assertEquals(sentDate.getTime(), savedMessage.getSentDate());
    assertEquals("Simple text message", savedMessage.getTitle());
    assertEquals(sentDate.get(Calendar.YEAR), savedMessage.getYear());
    assertEquals(sentDate.get(Calendar.MONTH), savedMessage.getMonth());
    assertEquals(id1, savedMessage.getId());
    assertEquals(0, savedMessage.getVersion());
    assertEquals(86199, savedMessage.getAttachmentsSize());
    assertNotNull(savedMessage.getAttachments());
    assertEquals(1, savedMessage.getAttachments().size());
    assertEquals("text/plain", savedMessage.getContentType());
    Attachment attached = savedMessage.getAttachments().iterator().next();
    assertNotNull(attached);
    assertEquals(0, attached.getVersion());
    assertNotNull(attached.getId());
    assertEquals(86199, attached.getSize());
    assertEquals("lemonde.html", attached.getFileName());
    assertEquals(attachmentPath + "toto" + File.separator + "lemonde.html", attached.getPath());

    String id2 = Transaction.performInOne(() -> {
      Message message = new Message();
      message.setBody(textEmailContent);
      message.setComponentId("componentId");
      message.setModerated(true);
      message.setSummary(textEmailContent.substring(0, 200));
      message.setSender("bart.simpson@ilverpeas.com");
      message.setMessageId("0000001747b40c27");
      message.setSentDate(sentDate.getTime());
      message.setTitle("Simple text message 2");

      Attachment attachment = new Attachment();
      attachment.setPath(attachmentPath + "titi" + File.separator + "lemonde.html");
      attachment.setFileName("lemonde.html");
      attachment.setSize(10000);
      copyFile(copyPath, attachmentPath + "titi" + File.separator + "lemonde.html");
      message.getAttachments().add(attachment);
      return messageDao.saveMessage(message);
    });
    assertNotNull(id2);
    savedMessage = messageDao.findMessageById(id2);
    assertNotNull(savedMessage);
    assertEquals(textEmailContent, savedMessage.getBody());
    assertEquals("componentId", savedMessage.getComponentId());
    assertTrue(savedMessage.isModerated());
    assertEquals(textEmailContent.substring(0, 200), savedMessage.getSummary());
    assertEquals("bart.simpson@ilverpeas.com", savedMessage.getSender());
    assertEquals("0000001747b40c27", savedMessage.getMessageId());
    assertEquals(sentDate.getTime(), savedMessage.getSentDate());
    assertEquals("Simple text message 2", savedMessage.getTitle());
    assertEquals(sentDate.get(Calendar.YEAR), savedMessage.getYear());
    assertEquals(sentDate.get(Calendar.MONTH), savedMessage.getMonth());
    assertEquals(id2, savedMessage.getId());
    assertEquals(0, savedMessage.getVersion());
    assertEquals(ATT_SIZE, savedMessage.getAttachmentsSize());
    assertNotNull(savedMessage.getAttachments());
    assertEquals(1, savedMessage.getAttachments().size());
    attached = savedMessage.getAttachments().iterator().next();
    assertNotNull(attached);
    assertEquals(0, attached.getVersion());
    assertNotNull(attached.getId());
    assertEquals(ATT_SIZE, attached.getSize());
    assertEquals("lemonde.html", attached.getFileName());
    assertEquals(attachmentPath + "toto" + File.separator + "lemonde.html", attached.getPath());
  }

  private static String getAttachmentPath() {
    return getUploadPath() + File.separatorChar + "componentId" + File.separatorChar +
        "mailId@silverpeas.com" + File.separatorChar;
  }

  private static String getUploadPath() {
    Properties props = new Properties();
    try {
      props.load(MessageDaoIT.class.getClassLoader().getResourceAsStream("maven.properties"));
    } catch (IOException ex) {
      Logger.getLogger(MessageDaoIT.class.getName()).log(Level.SEVERE, null, ex);
    }
    return props.getProperty("upload.dir", "c:\\tmp\\uploads");
  }

  private long countRowsInTable(String table) throws Exception {
    try (Connection connection = dbSetupRule.getSafeConnectionFromDifferentThread()) {
      return JdbcSqlQuery.countAll().from(table).executeWith(connection);
    }
  }
}
