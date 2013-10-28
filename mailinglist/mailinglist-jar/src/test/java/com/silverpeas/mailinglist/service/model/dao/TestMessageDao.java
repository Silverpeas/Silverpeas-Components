/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.mailinglist.service.model.dao;

import com.silverpeas.mailinglist.service.job.TestMessageChecker;
import com.silverpeas.mailinglist.service.model.beans.Activity;
import com.silverpeas.mailinglist.service.model.beans.Attachment;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.silverpeas.mailinglist.service.util.OrderBy;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dbunit.DataSourceDatabaseTester;
import org.dbunit.IDatabaseTester;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.silverpeas.util.Charsets;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.*;

import static java.io.File.separatorChar;

public class TestMessageDao {

  private static final int ATT_SIZE = 86199;
  private static final OrderBy orderByDate = new OrderBy("sentDate", false);
  private static final String textEmailContent =
      "Bonjour famille Simpson, j'espère que vous allez bien. "
      + "Ici tout se passe bien et Krusty est très sympathique. Surtout "
      + "depuis que Tahiti Bob est retourné en prison. Je dois remplacer"
      + "l'homme canon dans la prochaine émission.\nBart";
  private static final String attachmentPath = getAttachmentPath();
  private static final String COPY_PATH = System.getProperty("basedir") + separatorChar + "target"
      + separatorChar + "test-classes" + separatorChar + "com" + separatorChar + "silverpeas"
      + separatorChar + "mailinglist" + separatorChar + "service" + separatorChar + "job"
      + separatorChar + "lemonde.html";
  private ConfigurableApplicationContext context;
  private IDatabaseTester databaseTester;

  @Before
  public void setUpTest() throws Exception {
    context = new ClassPathXmlApplicationContext(
        "/spring-mailinglist-dao.xml", "/spring-mailinglist-embbed-datasource.xml");

    databaseTester = new DataSourceDatabaseTester(getDataSource());
    databaseTester.setDataSet(getDataSet());
    databaseTester.onSetup();
  }

  @After
  public void tearDownTest() throws Exception {
    databaseTester.onTearDown();
    FileFolderManager.deleteFolder(getUploadPath(), false);
    context.close();
  }

  private IDataSet getDataSet() throws DataSetException {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(
        MessageDao.class.getClassLoader().getResourceAsStream(
        "com/silverpeas/mailinglist/service/model/dao/mailinglist-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    return dataSet;
  }

  private MessageDao getMessageDAO() {
    return context.getBean(MessageDao.class);
  }

  private DataSource getDataSource() {
    return context.getBean("jpaDataSource", DataSource.class);
  }

  protected String loadHtml() throws IOException {
    InputStream in = TestMessageChecker.class.getResourceAsStream("lemonde.html");
    try {
      return IOUtils.toString(in, Charsets.UTF_8);
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  protected void copyFile(String filePath, String targetPath) throws IOException {
    FileUtils.copyFile(new File(filePath), new File(targetPath));
  }

  @Test
  public void testCreateSimpleMessage() {
    Calendar sentDate = Calendar.getInstance();
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
    String id = getMessageDAO().saveMessage(message);
    assertNotNull(id);
    Message savedMessage = getMessageDAO().findMessageById(id);
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
    assertEquals("text/plain", savedMessage.getContentType());
  }

  @Test
  public void testCreateSimpleMessageWithCompleteId() {
    MessageDao messageDao = getMessageDAO();
    Calendar sentDate = Calendar.getInstance();
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
    String id = messageDao.saveMessage(message);
    assertNotNull(id);
    Message savedMessage = messageDao.findMessageById(id);
    assertNotNull(savedMessage);
    assertEquals(textEmailContent, savedMessage.getBody());
    assertEquals("componentId", savedMessage.getComponentId());
    assertEquals(true, savedMessage.isModerated());
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
  public void testRecreateSimpleMessage() {
    MessageDao messageDao = getMessageDAO();
    Calendar sentDate = Calendar.getInstance();
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
    String id = messageDao.saveMessage(message);
    assertNotNull(id);
    Message savedMessage = messageDao.findMessageById(id);
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
    assertEquals("text/plain", savedMessage.getContentType());
    message = new Message();
    message.setBody(textEmailContent);
    message.setComponentId("componentId");
    message.setModerated(true);
    message.setSummary(textEmailContent.substring(0, 200));
    message.setSender("bart.simpson@ilverpeas.com");
    message.setMessageId("0000001747b40c85");
    message.setSentDate(sentDate.getTime());
    message.setTitle("Simple text message");
    message.setContentType("text/plain");
    String newId = messageDao.saveMessage(message);
    assertNotNull(newId);
    assertEquals(id, newId);
  }

  @Test
  public void testCreateSimpleHtmlMessage() throws IOException {
    MessageDao messageDao = getMessageDAO();
    Calendar sentDate = Calendar.getInstance();
    Message message = new Message();
    String html = loadHtml();
    message.setBody(html);
    message.setComponentId("componentId");
    message.setModerated(true);
    message.setSummary(textEmailContent.substring(0, 200));
    message.setSender("bart.simpson@ilverpeas.com");
    message.setMessageId("0000001747b40c10");
    message.setSentDate(sentDate.getTime());
    message.setContentType("text/html");
    message.setTitle("Simple html message");
    String id = messageDao.saveMessage(message);
    assertNotNull(id);
    Message savedMessage = messageDao.findMessageById(id);
    assertNotNull(savedMessage);
    assertEquals(html, savedMessage.getBody());
    assertEquals("componentId", savedMessage.getComponentId());
    assertEquals(true, savedMessage.isModerated());
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
  public void testCreateMessageWithAttachments() {
    MessageDao messageDao = getMessageDAO();
    Calendar sentDate = Calendar.getInstance();
    Message message = new Message();
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
    String id = messageDao.saveMessage(message);
    assertNotNull(id);
    Message savedMessage = messageDao.findMessageById(id);
    assertNotNull(savedMessage);
    assertEquals(textEmailContent, savedMessage.getBody());
    assertEquals("componentId", savedMessage.getComponentId());
    assertEquals(true, savedMessage.isModerated());
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
    Attachment attached = (Attachment) savedMessage.getAttachments().iterator().next();
    assertNotNull(attached);
    assertEquals(0, attached.getVersion());
    assertNotNull(attached.getId());
    assertEquals(10000, attached.getSize());
    assertEquals("lemonde.html", attached.getFileName());
    assertEquals(attachmentPath + "lemonde.html", attached.getPath());
  }

  @Test
  public void testUpdateMessage() {
    MessageDao messageDao = getMessageDAO();
    Calendar sentDate = Calendar.getInstance();
    Message message = new Message();
    message.setBody(textEmailContent);
    message.setComponentId("componentId");
    message.setModerated(true);
    message.setSummary(textEmailContent.substring(0, 200));
    message.setSender("bart.simpson@ilverpeas.com");
    message.setMessageId("0000001747b40c13");
    message.setSentDate(sentDate.getTime());
    message.setTitle("Simple text message");
    String id = messageDao.saveMessage(message);
    assertNotNull(id);
    Message savedMessage = messageDao.findMessageById(id);
    assertNotNull(savedMessage);
    assertEquals(textEmailContent, savedMessage.getBody());
    assertEquals("componentId", savedMessage.getComponentId());
    assertEquals(true, savedMessage.isModerated());
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
    savedMessage = messageDao.findMessageById(id);
    assertEquals(1, savedMessage.getVersion());
    assertNotNull(savedMessage);
    assertEquals(textEmailContent, savedMessage.getBody());
    assertEquals("componentId", savedMessage.getComponentId());
    assertEquals(false, savedMessage.isModerated());
    assertEquals(textEmailContent.substring(0, 200), savedMessage.getSummary());
    assertEquals("bart.simpson@ilverpeas.com", savedMessage.getSender());
    assertEquals("0000001747b40c13", savedMessage.getMessageId());
    assertEquals(sentDate.getTime(), savedMessage.getSentDate());
    assertEquals("Simple text message", savedMessage.getTitle());
    assertEquals(sentDate.get(Calendar.YEAR), savedMessage.getYear());
    assertEquals(sentDate.get(Calendar.MONTH), savedMessage.getMonth());
    assertEquals(id, savedMessage.getId());
  }

  @Test
  public void testUpdateMessageWithAttachment() {
    MessageDao messageDao = getMessageDAO();
    Calendar sentDate = Calendar.getInstance();
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
    String id = messageDao.saveMessage(message);
    assertNotNull(id);
    Message savedMessage = messageDao.findMessageById(id);
    assertNotNull(savedMessage);
    assertEquals(textEmailContent, savedMessage.getBody());
    assertEquals("componentId", savedMessage.getComponentId());
    assertEquals(true, savedMessage.isModerated());
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
    Attachment attached = (Attachment) savedMessage.getAttachments().iterator().next();
    assertNotNull(attached);
    assertEquals(0, attached.getVersion());
    assertNotNull(attached.getId());
    assertEquals(10000, attached.getSize());
    assertEquals("lemonde.html", attached.getFileName());
    assertEquals(attachmentPath + "lemonde.html", attached.getPath());
    attached.setSize(20000);
    savedMessage.setModerated(false);
    messageDao.updateMessage(savedMessage);
    savedMessage = messageDao.findMessageById(id);
    assertEquals(1, savedMessage.getVersion());
    assertNotNull(savedMessage);
    assertEquals(textEmailContent, savedMessage.getBody());
    assertEquals("componentId", savedMessage.getComponentId());
    assertEquals(false, savedMessage.isModerated());
    assertEquals(textEmailContent.substring(0, 200), savedMessage.getSummary());
    assertEquals("bart.simpson@ilverpeas.com", savedMessage.getSender());
    assertEquals("0000001747b40c14", savedMessage.getMessageId());
    assertEquals(sentDate.getTime(), savedMessage.getSentDate());
    assertEquals("Simple text message", savedMessage.getTitle());
    assertEquals(sentDate.get(Calendar.YEAR), savedMessage.getYear());
    assertEquals(sentDate.get(Calendar.MONTH), savedMessage.getMonth());
    assertEquals(id, savedMessage.getId());
    assertEquals(20000, savedMessage.getAttachmentsSize());
    assertNotNull(savedMessage.getAttachments());
    assertEquals(1, savedMessage.getAttachments().size());
    attached = (Attachment) savedMessage.getAttachments().iterator().next();
    assertNotNull(attached);
    assertEquals(1, attached.getVersion());
    assertNotNull(attached.getId());
    assertEquals(20000, attached.getSize());
    assertEquals("lemonde.html", attached.getFileName());
    assertEquals(attachmentPath + "lemonde.html", attached.getPath());
  }

  @Test
  public void testDeleteMessageWithAttachments() throws Exception {
    MessageDao messageDao = getMessageDAO();
    copyFile(COPY_PATH, attachmentPath + "lemonde2.html");
    copyFile(COPY_PATH, attachmentPath + "lemonde.html");

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
    String id = messageDao.saveMessage(message);
    assertNotNull(id);
    Message savedMessage = messageDao.findMessageById(id);
    assertEquals(1, countRowsInTable("SC_MAILINGLIST_MESSAGE"));
    assertEquals(2, countRowsInTable("SC_MAILINGLIST_ATTACHMENT"));
    assertNotNull(savedMessage);
    assertEquals(textEmailContent, savedMessage.getBody());
    assertEquals("componentId", savedMessage.getComponentId());
    assertEquals(true, savedMessage.isModerated());
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
    savedMessage = messageDao.findMessageById(id);
    assertNull(savedMessage);
    assertEquals(0, countRowsInTable("SC_MAILINGLIST_MESSAGE"));
    assertEquals(0, countRowsInTable("SC_MAILINGLIST_ATTACHMENT"));
    File deletedAttachement = new File(attachmentPath + "lemonde.html");
    assertFalse(deletedAttachement.exists());
    deletedAttachement = new File(attachmentPath + "lemonde2.html");
    assertFalse(deletedAttachement.exists());
  }

  @Test
  public void testDeleteMessageWithAttachmentShared() throws Exception {
    MessageDao messageDao = getMessageDAO();
    copyFile(COPY_PATH, attachmentPath + "lemonde2.html");
    copyFile(COPY_PATH, attachmentPath + "toto\\lemonde2.html");
    copyFile(COPY_PATH, attachmentPath + "lemonde.html");

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

    String id = messageDao.saveMessage(message);
    assertNotNull(id);
    String id2 = messageDao.saveMessage(message2);
    assertNotNull(id2);
    assertFalse(id2.equals(id));

    Message savedMessage = messageDao.findMessageById(id);
    assertEquals(2, countRowsInTable("SC_MAILINGLIST_MESSAGE"));
    assertEquals(3, countRowsInTable("SC_MAILINGLIST_ATTACHMENT"));
    messageDao.deleteMessage(savedMessage);

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
  public void testListMessagesOfMailingList() throws Exception {
    MessageDao messageDao = getMessageDAO();
    Calendar sentDate = Calendar.getInstance();
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
    List messages = messageDao.listAllMessagesOfMailingList("componentId", 0,
        10, orderByDate);
    assertNotNull(messages);
    assertEquals(3, messages.size());
    assertEquals(3, countRowsInTable("SC_MAILINGLIST_MESSAGE"));
    assertEquals(2, countRowsInTable("SC_MAILINGLIST_ATTACHMENT"));
    messages = messageDao.listAllMessagesOfMailingList("componentId", 0, 2,
        orderByDate);
    assertNotNull(messages);
    assertEquals(2, messages.size());
    messages = messageDao.listAllMessagesOfMailingList("componentId", 1, 2,
        orderByDate);
    assertNotNull(messages);
    assertEquals(1, messages.size());

    messages = messageDao.listDisplayableMessagesOfMailingList("componentId",
        -1, -1, 0, 10, orderByDate);
    assertNotNull(messages);
    assertEquals(2, messages.size());
    messages = messageDao.listDisplayableMessagesOfMailingList("componentId",
        -1, 2008, 0, 10, orderByDate);
    assertNotNull(messages);
    assertEquals(2, messages.size());
    messages = messageDao.listDisplayableMessagesOfMailingList("componentId",
        -1, 2007, 0, 10, orderByDate);
    assertNotNull(messages);
    assertEquals(0, messages.size());
    messages = messageDao.listDisplayableMessagesOfMailingList("componentId",
        Calendar.MARCH, 2008, 0, 10, orderByDate);
    assertNotNull(messages);
    assertEquals(1, messages.size());
    messages = messageDao.listDisplayableMessagesOfMailingList("componentId",
        Calendar.FEBRUARY, 2008, 0, 10, orderByDate);
    assertNotNull(messages);
    assertEquals(1, messages.size());
    messages = messageDao.listDisplayableMessagesOfMailingList("componentId",
        -1, -1, 0, 1, orderByDate);
    assertNotNull(messages);
    assertEquals(1, messages.size());
    messages = messageDao.listDisplayableMessagesOfMailingList("componentId",
        -1, -1, 1, 1, orderByDate);
    assertNotNull(messages);
    assertEquals(1, messages.size());

    messages = messageDao.listUnmoderatedMessagesOfMailingList("componentId",
        0, 10, orderByDate);
    assertNotNull(messages);
    assertEquals(1, messages.size());
    messages = messageDao.listUnmoderatedMessagesOfMailingList("componentId",
        0, 1, orderByDate);
    assertNotNull(messages);
    assertEquals(1, messages.size());
    messages = messageDao.listUnmoderatedMessagesOfMailingList("componentId",
        1, 1, orderByDate);
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
  public void testListActivityMessages() throws Exception {
    MessageDao messageDao = getMessageDAO();
    Calendar sentDate = Calendar.getInstance();
    Message message = new Message();
    message.setBody(textEmailContent);
    message.setComponentId("componentId");
    message.setModerated(true);
    message.setSummary(textEmailContent.substring(0, 200));
    message.setSender("bart.simpson@ilverpeas.com");
    message.setMessageId("0000001747b40c19");
    Date message1SentDate = sentDate.getTime();
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
    sentDate.add(Calendar.DAY_OF_MONTH, -1);
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
    message.setMessageId("0000001747b40c21");
    sentDate.add(Calendar.DAY_OF_MONTH, -1);
    Date message3SentDate = sentDate.getTime();
    message.setSentDate(message3SentDate);
    message.setTitle("Simple text message 3");
    messageDao.saveMessage(message);
    List messages = messageDao.listActivityMessages("componentId", 5,
        orderByDate);
    assertNotNull(messages);
    assertEquals(2, messages.size());
    assertEquals(3, countRowsInTable("SC_MAILINGLIST_MESSAGE"));
    assertEquals(2, countRowsInTable("SC_MAILINGLIST_ATTACHMENT"));
    Message activityMessage = (Message) messages.get(0);
    assertEquals(textEmailContent, activityMessage.getBody());
    assertEquals("componentId", activityMessage.getComponentId());
    assertEquals(true, activityMessage.isModerated());
    assertEquals(textEmailContent.substring(0, 200), activityMessage.getSummary());
    assertEquals("bart.simpson@ilverpeas.com", activityMessage.getSender());
    assertEquals("0000001747b40c19", activityMessage.getMessageId());
    assertEquals(message1SentDate, activityMessage.getSentDate());
    assertEquals("Simple text message", activityMessage.getTitle());
    assertEquals(2, activityMessage.getAttachments().size());
    assertEquals(30000, activityMessage.getAttachmentsSize());

    activityMessage = (Message) messages.get(1);
    assertEquals(textEmailContent, activityMessage.getBody());
    assertEquals("componentId", activityMessage.getComponentId());
    assertEquals(true, activityMessage.isModerated());
    assertEquals(textEmailContent.substring(0, 200), activityMessage.getSummary());
    assertEquals("bart.simpson@ilverpeas.com", activityMessage.getSender());
    assertEquals("0000001747b40c21", activityMessage.getMessageId());
    assertEquals(message3SentDate, activityMessage.getSentDate());
    assertEquals("Simple text message 3", activityMessage.getTitle());
    assertEquals(0, activityMessage.getAttachments().size());
    assertEquals(0, activityMessage.getAttachmentsSize());
  }

  @Test
  public void testListActivities() {
    MessageDao messageDao = getMessageDAO();
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
    messageDao.saveMessage(message);
    List activities = messageDao.listActivity("componentId");
    assertNotNull(activities);
    assertEquals(2, activities.size());
    Iterator iter = activities.iterator();
    while (iter.hasNext()) {
      Activity activity = (Activity) iter.next();
      assertEquals(activity.getYear(), 2008);
      if (activity.getMonth() == Calendar.FEBRUARY) {
        assertEquals(1, activity.getNbMessages());
      } else {
        assertEquals(Calendar.JANUARY, activity.getMonth());
        assertEquals(2, activity.getNbMessages());
      }
    }
  }

  @Test
  public void testCreateMessagesWithSameAttachments() throws IOException {
    MessageDao messageDao = getMessageDAO();
    Calendar sentDate = Calendar.getInstance();
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
    copyFile(COPY_PATH, attachmentPath + "toto" + File.separator + "lemonde.html");
    message.getAttachments().add(attachment);
    String id1 = messageDao.saveMessage(message);
    assertNotNull(id1);
    Message savedMessage = messageDao.findMessageById(id1);
    assertNotNull(savedMessage);
    assertEquals(textEmailContent, savedMessage.getBody());
    assertEquals("componentId", savedMessage.getComponentId());
    assertEquals(true, savedMessage.isModerated());
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
    Attachment attached = (Attachment) savedMessage.getAttachments().iterator().next();
    assertNotNull(attached);
    assertEquals(0, attached.getVersion());
    assertNotNull(attached.getId());
    assertEquals(86199, attached.getSize());
    assertEquals("lemonde.html", attached.getFileName());
    assertEquals(attachmentPath + "toto" + File.separator + "lemonde.html",
        attached.getPath());

    message = new Message();
    message.setBody(textEmailContent);
    message.setComponentId("componentId");
    message.setModerated(true);
    message.setSummary(textEmailContent.substring(0, 200));
    message.setSender("bart.simpson@ilverpeas.com");
    message.setMessageId("0000001747b40c27");
    message.setSentDate(sentDate.getTime());
    message.setTitle("Simple text message 2");

    attachment = new Attachment();
    attachment.setPath(attachmentPath + "titi" + File.separator + "lemonde.html");
    attachment.setFileName("lemonde.html");
    attachment.setSize(10000);
    copyFile(COPY_PATH, attachmentPath + "titi" + File.separator + "lemonde.html");
    message.getAttachments().add(attachment);
    String id2 = messageDao.saveMessage(message);
    assertNotNull(id2);
    savedMessage = messageDao.findMessageById(id2);
    assertNotNull(savedMessage);
    assertEquals(textEmailContent, savedMessage.getBody());
    assertEquals("componentId", savedMessage.getComponentId());
    assertEquals(true, savedMessage.isModerated());
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
    attached = (Attachment) savedMessage.getAttachments().iterator().next();
    assertNotNull(attached);
    assertEquals(0, attached.getVersion());
    assertNotNull(attached.getId());
    assertEquals(ATT_SIZE, attached.getSize());
    assertEquals("lemonde.html", attached.getFileName());
    assertEquals(attachmentPath + "toto" + File.separator + "lemonde.html",
        attached.getPath());
  }

  private static String getAttachmentPath() {
    return getUploadPath() + File.separatorChar + "componentId" + File.separatorChar
        + "mailId@silverpeas.com" + File.separatorChar;
  }

  private static String getUploadPath() {
    Properties props = new Properties();
    try {
      props.load(TestMessageDao.class.getClassLoader().getResourceAsStream("maven.properties"));
    } catch (IOException ex) {
      Logger.getLogger(TestMessageDao.class.getName()).log(Level.SEVERE, null, ex);
    }
    return props.getProperty("upload.dir", "c:\\tmp\\uploads");
  }

  private int countRowsInTable(String table) throws Exception {
    return databaseTester.getConnection().getRowCount(table);
  }
}
