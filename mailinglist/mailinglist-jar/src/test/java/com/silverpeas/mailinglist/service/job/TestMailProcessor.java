/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.mailinglist.service.job;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.springframework.test.AbstractSingleSpringContextTests;

import com.silverpeas.mailinglist.service.event.MessageEvent;
import com.silverpeas.mailinglist.service.event.MessageListener;
import com.silverpeas.mailinglist.service.model.beans.Attachment;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import static com.silverpeas.util.PathTestUtil.*;
import static org.mockito.Mockito.*;

public class TestMailProcessor extends AbstractSingleSpringContextTests {

  private static int ATT_SIZE = 85922;

  @Override
  protected String[] getConfigLocations() {
    return new String[]{"spring-checker.xml", "spring-notification.xml",
          "spring-hibernate.xml", "spring-datasource.xml"};
  }
  private static final String attachmentPath = BUILD_PATH + SEPARATOR +
      "uploads" + SEPARATOR + "componentId" + SEPARATOR +
      "mailId@silverpeas.com" + SEPARATOR + "lemonde.html";
  private static final String textEmailContent =
      "Bonjour famille Simpson, j'espère que vous allez bien. " +
      "Ici tout se passe bien et Krusty est très sympathique. Surtout " +
      "depuis que Tahiti Bob est retourné en prison. Je dois remplacer" +
      "l'homme canon dans la prochaine émission.\nBart";
  private static final String htmlEmailSummary =
      "Politique A la Une Le Desk Vidéos International *Elections américaines Europe Politique " +
      "*Municipales & Cantonales 2008 Société Carnet Economie Médias Météo Rendez-vous Sports " +
      "*Tournoi des VI Nations E";

  @Override
  protected void onTearDown() {
    try {
      FileFolderManager.deleteFolder(BUILD_PATH + SEPARATOR +
          "uploads" + SEPARATOR + "componentId", false);
    } catch (UtilException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  protected String loadHtml() throws IOException {
    StringWriter buffer = null;
    BufferedReader reader = null;
    try {
      buffer = new StringWriter();
      reader = new BufferedReader(new InputStreamReader(
          TestMessageChecker.class.getResourceAsStream("lemonde.html"), "UTF-8"));
      String line = null;
      while ((line = reader.readLine()) != null) {
        buffer.write(line);
      }
      return buffer.toString();
    } finally {
      if (reader != null) {
        reader.close();
      }
      if (buffer != null) {
        buffer.close();
      }
    }
  }

  public void testProcessMailPartWithAttachment() throws IOException,
      MessagingException {
    MailProcessor processor = (MailProcessor) getApplicationContext().getBean("mailProcessor");
    MimeBodyPart part = new MimeBodyPart(TestMessageChecker.class.getResourceAsStream("lemonde.html"));
    part.setDisposition(Part.ATTACHMENT);
    part.setFileName("lemonde.html");
    part.setHeader("Content-Type", "text/html; charset=ISO-8859-1");
    Message message = new Message();
    message.setComponentId("componentId");
    message.setMessageId("mailId@silverpeas.com");
    processor.processMailPart(part, message);
    assertEquals(1, message.getAttachments().size());
    Attachment attachment = message.getAttachments().iterator().next();
    assertNotNull(attachment.getPath());
    assertEquals(attachment.getPath(), attachmentPath);
    assertEquals("lemonde.html", attachment.getFileName());
    assertEquals(85922, message.getAttachmentsSize());
    assertEquals(85922, attachment.getSize());
    assertEquals("lemonde.html", attachment.getFileName());
    File partFile = new File(attachment.getPath());
    assertTrue(partFile.exists());
    assertTrue(partFile.isFile());
    assertEquals(85922, partFile.length());
    partFile.delete();
  }

  public void testProcessMailPartWithHtml() throws MessagingException,
      IOException {
    MailProcessor processor = (MailProcessor) getApplicationContext().getBean(
        "mailProcessor");
    MimeBodyPart part = new MimeBodyPart();
    String html = loadHtml();
    part.setContent(html, "text/html");
    part.setHeader("Content-Type", "text/html; charset=UTF-8");
    Message message = new Message();
    message.setComponentId("componentId");
    message.setMessageId("mailId@silverpeas.com");
    processor.processMailPart(part, message);
    assertEquals(0, message.getAttachments().size());
    assertEquals(0, message.getAttachmentsSize());
    message = new Message();
    message.setComponentId("componentId");
    message.setMessageId("mailId@silverpeas.com");
    processor.processMailPart(part, message);
    assertEquals(0, message.getAttachments().size());
    assertEquals(0, message.getAttachmentsSize());
    assertEquals(html, message.getBody());
    assertEquals(htmlEmailSummary, message.getSummary());
    assertEquals("text/html", message.getContentType());
    File partFile = new File(attachmentPath);
    assertFalse(partFile.exists());
  }

  public void testProcessMailPartWithInlineAttachment()
      throws MessagingException, IOException {
    MimeBodyPart part = new MimeBodyPart(TestMessageChecker.class.
        getResourceAsStream("lemonde.html"));
    part.setFileName("lemonde.html");
    part.setDisposition(Part.INLINE);
    Message message = new Message();
    message.setComponentId("componentId");
    message.setMessageId("mailId@silverpeas.com");
    MailProcessor processor = (MailProcessor) getApplicationContext().getBean(
        "mailProcessor");
    processor.processMailPart(part, message);
    assertEquals(1, message.getAttachments().size());
    Attachment attachment = message.getAttachments().iterator().next();
    assertNotNull(attachment.getPath());
    assertEquals(attachment.getPath(), attachmentPath);
    assertEquals("lemonde.html", attachment.getFileName());
    assertEquals(ATT_SIZE, message.getAttachmentsSize());
    assertEquals(ATT_SIZE, attachment.getSize());
    File partFile = new File(attachment.getPath());
    assertTrue(partFile.exists());
    assertTrue(partFile.isFile());
    assertEquals(ATT_SIZE, partFile.length());
    partFile.delete();
  }

  public void testProcessMailPartWithText() throws MessagingException,
      IOException {
    MimeBodyPart part = new MimeBodyPart();
    part.setText("Bonjour famille Simpson");
    Message message = new Message();
    message.setComponentId("componentId");
    message.setMessageId("mailId@silverpeas.com");
    MailProcessor processor = (MailProcessor) getApplicationContext().getBean(
        "mailProcessor");
    processor.processMailPart(part, message);
    assertEquals(0, message.getAttachments().size());
    assertEquals(0, message.getAttachmentsSize());
    assertEquals("Bonjour famille Simpson", message.getBody());
    assertEquals("Bonjour famille Simpson", message.getSummary());
    File partFile = new File(attachmentPath);
    assertFalse(partFile.exists());
    part = new MimeBodyPart();
    part.setText(textEmailContent);
    message = new Message();
    message.setComponentId("componentId");
    message.setMessageId("mailId@silverpeas.com");
    processor.processMailPart(part, message);
    assertEquals(0, message.getAttachments().size());
    assertEquals(0, message.getAttachmentsSize());
    assertEquals(textEmailContent, message.getBody());
    assertEquals(textEmailContent.substring(0, 200), message.getSummary());
    assertEquals("text/plain", message.getContentType());
    partFile = new File(attachmentPath);
    assertFalse(partFile.exists());
  }

  public void testReplaceSpecialChars() {
    MailProcessor processor = (MailProcessor) getApplicationContext().getBean(
        "mailProcessor");
    String specialString =
        "Bart Simpson -<23_B blue Street> 73882 Springfield." +
        "'\\Tel:33#0476898967%Youhou/";
    String cleanString = processor.replaceSpecialChars(specialString);
    assertEquals("Bart_Simpson___23_B_blue_Street__73882_Springfield." +
        "__Tel_33_0476898967_Youhou_", cleanString);
    cleanString = processor.replaceSpecialChars(null);
    assertNotNull(cleanString);
    assertEquals("", cleanString);
  }

  public void testSaveAttachment() throws IOException, MessagingException {
    MimeBodyPart part = new MimeBodyPart(TestMessageChecker.class.
        getResourceAsStream("lemonde.html"));
    part.setFileName("lemonde.html");
    MailProcessor processor = (MailProcessor) getApplicationContext().getBean(
        "mailProcessor");
    String path = processor.saveAttachment(part, "componentId",
        "mailId@silverpeas.com");
    assertNotNull(path);
    assertEquals(path, attachmentPath);
    File partFile = new File(path);
    assertTrue(partFile.exists());
    assertTrue(partFile.isFile());
    assertEquals(ATT_SIZE, partFile.length());
    partFile.delete();
  }

  public void testPrepareMessageWithTextEmail() throws MessagingException,
      IOException {
    MailProcessor processor = (MailProcessor) getApplicationContext().getBean(
        "mailProcessor");
    MessageListener mailingList = mock(MessageListener.class);
    when(mailingList.getComponentId()).thenReturn("componentId");
    when(mailingList.checkSender("bart.simpson@silverpeas.com")).thenReturn(Boolean.TRUE);
    MimeMessage mail = new MimeMessage((Session) applicationContext.getBean("mailSession"));
    InternetAddress bart = new InternetAddress("bart.simpson@silverpeas.com");
    InternetAddress theSimpsons = new InternetAddress("thesimpsons@silverpeas.com");
    mail.addFrom(new InternetAddress[]{bart});
    mail.addRecipient(RecipientType.TO, theSimpsons);
    mail.setSubject("Simple text Email test");
    mail.setText(textEmailContent);
    MessageEvent event = new MessageEvent();
    processor.prepareMessage(mail, mailingList, event);
    assertNotNull(event);
    assertNotNull(event.getMessages());
    assertEquals(1, event.getMessages().size());
    Message message = event.getMessages().get(0);
    assertEquals("bart.simpson@silverpeas.com", message.getSender());
    assertEquals("Simple text Email test", message.getTitle());
    assertEquals(textEmailContent, message.getBody());
    assertEquals(textEmailContent.substring(0, 200), message.getSummary());
    assertEquals(0, message.getAttachmentsSize());
    assertEquals(0, message.getAttachments().size());
    assertEquals("componentId", message.getComponentId());
    verify(mailingList, times(1)).checkSender("bart.simpson@silverpeas.com");
    verify(mailingList, times(1)).getComponentId();
  }

  public void testPrepareMessageWithHtmlEmail() throws MessagingException,
      IOException {
    MailProcessor processor = (MailProcessor) getApplicationContext().getBean(
        "mailProcessor");
    MessageListener mailingList = mock(MessageListener.class);
    when(mailingList.getComponentId()).thenReturn("componentId");
    when(mailingList.checkSender("bart.simpson@silverpeas.com")).thenReturn(Boolean.TRUE);
    MimeMessage mail = new MimeMessage((Session) applicationContext.getBean(
        "mailSession"));
    InternetAddress bart = new InternetAddress("bart.simpson@silverpeas.com");
    InternetAddress theSimpsons = new InternetAddress(
        "thesimpsons@silverpeas.com");
    mail.addFrom(new InternetAddress[]{bart});
    mail.addRecipient(RecipientType.TO, theSimpsons);
    mail.setSubject("Simple HTML Email test");
    String html = loadHtml();
    mail.setContent(html, "text/html");
    mail.setHeader("Content-Type", "text/html");
    MessageEvent event = new MessageEvent();
    processor.prepareMessage(mail, mailingList, event);
    assertNotNull(event);
    assertNotNull(event.getMessages());
    assertEquals(1, event.getMessages().size());
    Message message = event.getMessages().get(0);
    assertEquals("bart.simpson@silverpeas.com", message.getSender());
    assertEquals("Simple HTML Email test", message.getTitle());
    assertEquals(html, message.getBody());
    assertEquals(htmlEmailSummary, message.getSummary());
    assertEquals(0, message.getAttachmentsSize());
    assertEquals(0, message.getAttachments().size());
    assertEquals("componentId", message.getComponentId());
    verify(mailingList, times(1)).checkSender("bart.simpson@silverpeas.com");
    verify(mailingList, times(1)).getComponentId();
  }

  public void testPrepareMessageWithHtmlEmailAndAttachment()
      throws MessagingException, IOException {
    MailProcessor processor = (MailProcessor) getApplicationContext().getBean("mailProcessor");
    MessageListener mailingList = mock(MessageListener.class);
    when(mailingList.getComponentId()).thenReturn("componentId");
    when(mailingList.checkSender("bart.simpson@silverpeas.com")).thenReturn(Boolean.TRUE);
    MimeMessage mail = new MimeMessage((Session) applicationContext.getBean("mailSession"));
    InternetAddress bart = new InternetAddress("bart.simpson@silverpeas.com");
    InternetAddress theSimpsons = new InternetAddress("thesimpsons@silverpeas.com");
    mail.addFrom(new InternetAddress[]{bart});
    mail.addRecipient(RecipientType.TO, theSimpsons);
    mail.setSubject("Attachment HTML Email test");
    Multipart multipart = new MimeMultipart();
    String html = loadHtml();
    MimeBodyPart body = new MimeBodyPart();
    body.setContent(html, "text/html");
    body.setHeader("Content-Type", "text/html");
    multipart.addBodyPart(body);
    MimeBodyPart attachment = new MimeBodyPart(TestMessageChecker.class.
        getResourceAsStream("lemonde.html"));
    attachment.setDisposition(Part.INLINE);
    attachment.setFileName("lemonde.html");
    multipart.addBodyPart(attachment);
    mail.setContent(multipart);
    Transport.send(mail);
    String mailId = processor.replaceSpecialChars(mail.getMessageID());
    MessageEvent event = new MessageEvent();
    processor.prepareMessage(mail, mailingList, event);
    assertNotNull(event);
    assertNotNull(event.getMessages());
    assertEquals(1, event.getMessages().size());
    Message message = event.getMessages().get(0);
    assertEquals("bart.simpson@silverpeas.com", message.getSender());
    assertEquals("Attachment HTML Email test", message.getTitle());
    assertEquals(html, message.getBody());
    assertEquals(htmlEmailSummary, message.getSummary());
    assertEquals(1, message.getAttachments().size());
    assertEquals(85922, message.getAttachmentsSize());
    assertEquals("componentId", message.getComponentId());
    Attachment attach = message.getAttachments().iterator().next();
    assertNotNull(attach.getPath());
    assertEquals(attach.getPath(), BUILD_PATH + SEPARATOR +
        "uploads" + SEPARATOR + "componentId" + SEPARATOR + mailId + SEPARATOR +
        "lemonde.html");
    File partFile = new File(attach.getPath());
    assertTrue(partFile.exists());
    assertTrue(partFile.isFile());
    verify(mailingList, times(1)).checkSender("bart.simpson@silverpeas.com");
    verify(mailingList, times(1)).getComponentId();
  }

  public void testProcessBodyPlainText() throws Exception {
    MailProcessor processor = (MailProcessor) getApplicationContext().getBean("mailProcessor");
    Message message = new Message();
    processor.processBody(textEmailContent, "text/plain", message);
    assertNotNull(message.getBody());
    assertEquals(textEmailContent, message.getBody());
    assertEquals(textEmailContent.substring(0, 200), message.getSummary());
    message = new Message();
    processor.processBody(textEmailContent, null, message);
    assertNotNull(message.getBody());
    assertEquals(textEmailContent, message.getBody());
    assertEquals(textEmailContent.substring(0, 200), message.getSummary());
    assertEquals("text/plain", message.getContentType());
  }

  public void testProcessBodyHtmlText() throws Exception {
    MailProcessor processor = (MailProcessor) getApplicationContext().getBean("mailProcessor");
    Message message = new Message();
    String content = loadHtml();
    processor.processBody(content, "text/html", message);
    assertNotNull(message.getBody());
    assertEquals(content, message.getBody());
    assertEquals(htmlEmailSummary, message.getSummary());
    assertEquals("text/html", message.getContentType());
  }

  public void testPrepareUnauthorizedMessageWithTextEmail()
      throws MessagingException, IOException {
    MailProcessor processor = (MailProcessor) getApplicationContext().getBean("mailProcessor");
    MessageListener mailingList = mock(MessageListener.class);
    when(mailingList.getComponentId()).thenReturn("componentId");
    when(mailingList.checkSender("bart.simpson@silverpeas.com")).thenReturn(Boolean.FALSE);
    MimeMessage mail = new MimeMessage((Session) applicationContext.getBean("mailSession"));
    InternetAddress bart = new InternetAddress("bart.simpson@silverpeas.com");
    InternetAddress theSimpsons = new InternetAddress("thesimpsons@silverpeas.com");
    mail.addFrom(new InternetAddress[]{bart});
    mail.addRecipient(RecipientType.TO, theSimpsons);
    mail.setSubject("Simple text Email test");
    mail.setText(textEmailContent);
    MessageEvent event = new MessageEvent();
    processor.prepareMessage(mail, mailingList, event);
    assertNotNull(event);
    assertNotNull(event.getMessages());
    assertEquals(0, event.getMessages().size());
    verify(mailingList, times(1)).checkSender("bart.simpson@silverpeas.com");
    verify(mailingList, times(0)).getComponentId();
  }
}
