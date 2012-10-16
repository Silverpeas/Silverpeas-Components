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
package com.silverpeas.mailinglist.service.job;

import static com.silverpeas.mailinglist.PathTestUtil.*;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.jvnet.mock_javamail.Mailbox;

import com.silverpeas.mailinglist.service.event.MessageEvent;
import com.silverpeas.mailinglist.service.event.MessageListener;
import com.silverpeas.mailinglist.service.model.beans.Attachment;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import java.io.*;
import javax.inject.Inject;
import org.junit.After;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-checker.xml", "/spring-notification.xml",
  "/spring-fake-services.xml"})
public class TestMessageChecker {

  @Inject
  MessageChecker messageChecker;
  private static int ATT_SIZE = 85922;
  private static final String attachmentPath = BUILD_PATH + SEPARATOR +
      "uploads" + SEPARATOR + "componentId" + SEPARATOR + "{0}" + SEPARATOR + "lemonde.html";
  private static final String textEmailContent =
      "Bonjour famille Simpson, j'espère que vous allez bien. " +
      "Ici tout se passe bien et Krusty est très sympathique. Surtout " +
      "depuis que Tahiti Bob est retourné en prison. Je dois remplacer" +
      "l'homme canon dans la prochaine émission.\r\nBart";
  private static final String htmlEmailSummary = "Politique Recherchez depuis sur Le Monde.fr A " +
      "la Une Le Desk Vidéos International *Elections américaines Europe Politique " +
      "*Municipales & Cantonales 2008 Société Carnet Economie Médias Météo Rendez-vou";

  protected String loadHtml() throws IOException {
    StringWriter buffer = null;
    BufferedReader reader = null;
    try {
      buffer = new StringWriter();
      reader = new BufferedReader(new InputStreamReader(
          TestMessageChecker.class.getResourceAsStream("lemonde.html"), "UTF-8"));
      String line;
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

  @Test
  public void testSpringLoading() {
    assertNotNull(messageChecker);
    assertEquals("thesimpsons", messageChecker.getLogin());
    assertEquals("simpson", messageChecker.getPassword());
    assertEquals("imap", messageChecker.getProtocol());
    assertEquals("silverpeas.com", messageChecker.getMailServer());
    assertEquals(143, messageChecker.getPort());
    assertTrue(messageChecker.isLeaveOnServer());
  }

  @Test
  public void testGetAllRecipients() throws AddressException, MessagingException {
    MimeMessage mail = new MimeMessage(messageChecker.getMailSession());
    mail.addFrom(new InternetAddress[]{new InternetAddress("bart.simpson@silverpeas.com")});
    mail.addRecipient(RecipientType.TO, new InternetAddress("lisa.simpson@silverpeas.com"));
    mail.addRecipient(RecipientType.TO, new InternetAddress("marge.simpson@silverpeas.com"));
    mail.addRecipient(RecipientType.CC, new InternetAddress("homer.simpson@silverpeas.com"));
    mail.addRecipient(RecipientType.CC, new InternetAddress("krusty.theklown@silverpeas.com"));
    mail.addRecipient(RecipientType.BCC, new InternetAddress("ned.flanders@silverpeas.com"));
    mail.addRecipient(RecipientType.BCC, new InternetAddress("ted.flanders@silverpeas.com"));
    Set<String> recipients = messageChecker.getAllRecipients(mail);
    assertNotNull(recipients);
    assertEquals(6, recipients.size());
    assertTrue(recipients.contains("lisa.simpson@silverpeas.com"));
    assertTrue(recipients.contains("marge.simpson@silverpeas.com"));
    assertTrue(recipients.contains("homer.simpson@silverpeas.com"));
    assertTrue(recipients.contains("krusty.theklown@silverpeas.com"));
    assertTrue(recipients.contains("ned.flanders@silverpeas.com"));
    assertTrue(recipients.contains("ted.flanders@silverpeas.com"));
  }

  @Test
  public void testRecipientMailingList() throws AddressException, MessagingException {
    MessageListener mockListener1 = mock(MessageListener.class);
    MessageListener mockListener2 = mock(MessageListener.class);
    Map<String, MessageListener> listenersByEmail = new HashMap<String, MessageListener>(2);
    listenersByEmail.put("bart.simpson@silverpeas.com", mockListener1);
    listenersByEmail.put("ned.flanders@silverpeas.com", mockListener2);
    List<String> allRecipients = Arrays.asList(new String[]{
          "lisa.simpson@silverpeas.com", "marge.simpson@silverpeas.com",
          "homer.simpson@silverpeas.com", "Bart.Simpson@silverpeas.com",
          "krusty.theklown@silverpeas.com", "ned.flanders@silverpeas.com",
          "ted.flanders@silverpeas.com"});
    Set<MessageListener> recipients = messageChecker.getRecipientMailingLists(allRecipients,
        listenersByEmail);
    assertNotNull(recipients);
    assertEquals(2, recipients.size());
    assertTrue(recipients.contains(mockListener1));
    assertTrue(recipients.contains(mockListener2));
  }

  @Test
  public void testProcessEmailSimpleText() throws MessagingException, IOException {
    MessageListener mockListener1 = mock(MessageListener.class);
    when(mockListener1.getComponentId()).thenReturn("componentId");
    when(mockListener1.checkSender("bart.simpson@silverpeas.com")).thenReturn(Boolean.TRUE);

    MessageListener mockListener2 = mock(MessageListener.class);
    Map<String, MessageListener> listenersByEmail = new HashMap<String, MessageListener>(2);
    listenersByEmail.put("thesimpsons@silverpeas.com", mockListener1);
    listenersByEmail.put("theflanders@silverpeas.com", mockListener2);
    MimeMessage mail = new MimeMessage(messageChecker.getMailSession());
    InternetAddress bart = new InternetAddress("bart.simpson@silverpeas.com");
    InternetAddress theSimpsons = new InternetAddress(
        "thesimpsons@silverpeas.com");
    mail.addFrom(new InternetAddress[]{bart});
    mail.addRecipient(RecipientType.TO, theSimpsons);
    mail.setSubject("Simple text Email test");
    mail.setText(textEmailContent);
    Map<MessageListener, MessageEvent> events = new HashMap<MessageListener, MessageEvent>();
    messageChecker.processEmail(mail, events, listenersByEmail);
    assertNotNull(events);
    assertEquals(1, events.size());
    assertNull(events.get(mockListener2));

    MessageEvent event = events.get(mockListener1);
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
    assertEquals("text/plain; charset=" + System.getProperty("file.encoding"), message.
        getContentType());
    verify(mockListener1, atLeastOnce()).checkSender("bart.simpson@silverpeas.com");
  }

  @Test
  public void testProcessEmailHtmlText() throws MessagingException, IOException {
    MessageListener mockListener1 = mock(MessageListener.class);
    when(mockListener1.getComponentId()).thenReturn("componentId");
    when(mockListener1.checkSender("bart.simpson@silverpeas.com")).thenReturn(Boolean.TRUE);
    MessageListener mockListener2 = mock(MessageListener.class);
    Map<String, MessageListener> listenersByEmail = new HashMap<String, MessageListener>(2);
    listenersByEmail.put("thesimpsons@silverpeas.com", mockListener1);
    listenersByEmail.put("theflanders@silverpeas.com", mockListener2);
    MimeMessage mail = new MimeMessage(messageChecker.getMailSession());
    InternetAddress bart = new InternetAddress("bart.simpson@silverpeas.com");
    InternetAddress theSimpsons = new InternetAddress(
        "thesimpsons@silverpeas.com");
    mail.addFrom(new InternetAddress[]{bart});
    mail.addRecipient(RecipientType.TO, theSimpsons);
    mail.setSubject("Simple html Email test");
    String html = loadHtml();
    mail.setText(html, "UTF-8", "html");
    mail.setSentDate(new Date());
    Date sentDate = new Date(mail.getSentDate().getTime());
    Transport.send(mail);
    Map<MessageListener, MessageEvent> events = new HashMap<MessageListener, MessageEvent>();
    messageChecker.processEmail(mail, events, listenersByEmail);
    assertNotNull(events);
    assertEquals(1, events.size());
    assertNull(events.get(mockListener2));

    MessageEvent event = events.get(mockListener1);
    assertNotNull(event);
    assertNotNull(event.getMessages());
    assertEquals(1, event.getMessages().size());
    Message message = event.getMessages().get(0);
    assertEquals("bart.simpson@silverpeas.com", message.getSender());
    assertEquals("Simple html Email test", message.getTitle());
    assertEquals(html, message.getBody());
    assertNotNull(message.getSentDate());
    assertEquals(sentDate.getTime(), message.getSentDate().getTime());
    assertEquals(htmlEmailSummary, message.getSummary());
    assertEquals(0, message.getAttachmentsSize());
    assertEquals(0, message.getAttachments().size());
    assertEquals("componentId", message.getComponentId());
    assertEquals("text/html; charset=UTF-8", message.getContentType());
    verify(mockListener1, atLeastOnce()).checkSender("bart.simpson@silverpeas.com");
  }

  @Test
  public void testProcessEmailHtmlTextWithAttachment() throws MessagingException, IOException {
    MessageListener mockListener1 = mock(MessageListener.class);
    when(mockListener1.getComponentId()).thenReturn("componentId");
    when(mockListener1.checkSender("bart.simpson@silverpeas.com")).thenReturn(Boolean.TRUE);
    MessageListener mockListener2 = mock(MessageListener.class);
    messageChecker.addMessageListener(mockListener1);
    messageChecker.addMessageListener(mockListener2);
    Map<String, MessageListener> listenersByEmail = new HashMap<String, MessageListener>(2);
    listenersByEmail.put("thesimpsons@silverpeas.com", mockListener1);
    listenersByEmail.put("theflanders@silverpeas.com", mockListener2);
    MimeMessage mail = new MimeMessage(messageChecker.getMailSession());
    InternetAddress bart = new InternetAddress("bart.simpson@silverpeas.com");
    InternetAddress theSimpsons = new InternetAddress(
        "thesimpsons@silverpeas.com");
    mail.addFrom(new InternetAddress[]{bart});
    mail.addRecipient(RecipientType.TO, theSimpsons);
    mail.setSubject("Html Email test with attachment");
    String html = loadHtml();
    MimeBodyPart attachment = new MimeBodyPart(TestMessageChecker.class.getResourceAsStream(
        "lemonde.html"));
    attachment.setDisposition(Part.ATTACHMENT);
    attachment.setFileName("lemonde.html");
    MimeBodyPart body = new MimeBodyPart();
    body.setContent(html, "text/html; charset=\"UTF-8\"");
    Multipart multiPart = new MimeMultipart();
    multiPart.addBodyPart(body);
    multiPart.addBodyPart(attachment);
    mail.setContent(multiPart);
    Transport.send(mail);
    Map<MessageListener, MessageEvent> events = new HashMap<MessageListener, MessageEvent>();
    messageChecker.processEmail(mail, events, listenersByEmail);
    assertNotNull(events);
    assertEquals(1, events.size());
    assertNull(events.get(mockListener2));

    MessageEvent event = events.get(mockListener1);
    assertNotNull(event);
    assertNotNull(event.getMessages());
    assertEquals(1, event.getMessages().size());
    Message message = event.getMessages().get(0);
    assertEquals("bart.simpson@silverpeas.com", message.getSender());
    assertEquals("Html Email test with attachment", message.getTitle());
    assertEquals(html, message.getBody());
    assertEquals(htmlEmailSummary, message.getSummary());
    assertEquals(ATT_SIZE, message.getAttachmentsSize());
    assertEquals(1, message.getAttachments().size());
    String path = MessageFormat.format(attachmentPath, messageChecker.getMailProcessor().
        replaceSpecialChars(message.getMessageId()));
    Attachment attached = message.getAttachments().iterator().next();
    assertEquals(path, attached.getPath());
    assertEquals("lemonde.html", attached.getFileName());
    assertEquals("componentId", message.getComponentId());
    assertEquals("text/html", message.getContentType());
    verify(mockListener1, atLeastOnce()).checkSender("bart.simpson@silverpeas.com");
  }

  @Test
  public void testProcessEmailTextWithAttachment() throws MessagingException, IOException {
    MessageListener mockListener1 = mock(MessageListener.class);
    when(mockListener1.getComponentId()).thenReturn("componentId");
    when(mockListener1.checkSender("bart.simpson@silverpeas.com")).thenReturn(Boolean.TRUE);
    MessageListener mockListener2 = mock(MessageListener.class);
    messageChecker.addMessageListener(mockListener1);
    messageChecker.addMessageListener(mockListener2);
    Map<String, MessageListener> listenersByEmail = new HashMap<String, MessageListener>(2);
    listenersByEmail.put("thesimpsons@silverpeas.com", mockListener1);
    listenersByEmail.put("theflanders@silverpeas.com", mockListener2);
    MimeMessage mail = new MimeMessage(messageChecker.getMailSession());
    InternetAddress bart = new InternetAddress("bart.simpson@silverpeas.com");
    InternetAddress theSimpsons = new InternetAddress(
        "thesimpsons@silverpeas.com");
    mail.addFrom(new InternetAddress[]{bart});
    mail.addRecipient(RecipientType.TO, theSimpsons);
    mail.setSubject("Plain text Email test with attachment");
    MimeBodyPart attachment = new MimeBodyPart(TestMessageChecker.class.getResourceAsStream(
        "lemonde.html"));
    attachment.setDisposition(Part.INLINE);
    attachment.setFileName("lemonde.html");
    MimeBodyPart body = new MimeBodyPart();
    body.setText(textEmailContent);
    Multipart multiPart = new MimeMultipart();
    multiPart.addBodyPart(body);
    multiPart.addBodyPart(attachment);
    mail.setContent(multiPart);
    mail.setSentDate(new Date());
    Date sentDate = new Date(mail.getSentDate().getTime());
    Transport.send(mail);
    Map<MessageListener, MessageEvent> events = new HashMap<MessageListener, MessageEvent>();
    messageChecker.processEmail(mail, events, listenersByEmail);
    assertNotNull(events);
    assertEquals(1, events.size());
    assertNull(events.get(mockListener2));

    MessageEvent event = events.get(mockListener1);
    assertNotNull(event);
    assertNotNull(event.getMessages());
    assertEquals(1, event.getMessages().size());
    Message message = event.getMessages().get(0);
    assertEquals("bart.simpson@silverpeas.com", message.getSender());
    assertEquals("Plain text Email test with attachment", message.getTitle());
    assertEquals(textEmailContent, message.getBody());
    assertEquals(textEmailContent.substring(0, 200), message.getSummary());
    assertEquals(ATT_SIZE, message.getAttachmentsSize());
    assertEquals(1, message.getAttachments().size());
    String path = MessageFormat.format(attachmentPath, messageChecker.getMailProcessor().
        replaceSpecialChars(
        message.getMessageId()));
    Attachment attached = message.getAttachments().iterator().next();
    assertEquals(path, attached.getPath());
    assertEquals("lemonde.html", attached.getFileName());
    assertEquals("componentId", message.getComponentId());
    assertEquals(sentDate.getTime(), message.getSentDate().getTime());
    assertEquals("text/plain", message.getContentType());
    org.jvnet.mock_javamail.Mailbox.clearAll();
    verify(mockListener1, atLeastOnce()).checkSender("bart.simpson@silverpeas.com");
    verify(mockListener1, times(2)).getComponentId();
  }

  @Test
  public void testProcessUnauthorizedEmailSimpleText() throws MessagingException, IOException {
    MessageListener mockListener1 = mock(MessageListener.class);
    when(mockListener1.getComponentId()).thenReturn("componentId");
    when(mockListener1.checkSender("bart.simpson@silverpeas.com")).thenReturn(Boolean.FALSE);
    MessageListener mockListener2 = mock(MessageListener.class);
    messageChecker.addMessageListener(mockListener1);
    messageChecker.addMessageListener(mockListener2);
    Map<String, MessageListener> listenersByEmail = new HashMap<String, MessageListener>(2);
    listenersByEmail.put("thesimpsons@silverpeas.com", mockListener1);
    listenersByEmail.put("theflanders@silverpeas.com", mockListener2);
    MimeMessage mail = new MimeMessage(messageChecker.getMailSession());
    InternetAddress bart = new InternetAddress("bart.simpson@silverpeas.com");
    InternetAddress theSimpsons = new InternetAddress("thesimpsons@silverpeas.com");
    mail.addFrom(new InternetAddress[]{bart});
    mail.addRecipient(RecipientType.TO, theSimpsons);
    mail.setSubject("Simple text Email test");
    mail.setText(textEmailContent);
    Map<MessageListener, MessageEvent> events = new HashMap<MessageListener, MessageEvent>();
    messageChecker.processEmail(mail, events, listenersByEmail);
    assertNotNull(events);
    assertEquals(1, events.size());
    assertNull(events.get(mockListener2));
    MessageEvent event = events.get(mockListener1);
    assertNotNull(event);
    assertNotNull(event.getMessages());
    assertEquals(0, event.getMessages().size());
    verify(mockListener1, atLeastOnce()).checkSender("bart.simpson@silverpeas.com");
  }

  @After
  public void onTearDown() {
    Mailbox.clearAll();
    FileFolderManager.deleteFolder(BUILD_PATH + File.separatorChar + "uploads", false);
  }
}
