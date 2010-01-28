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
 * FLOSS exception.  You should have recieved a copy of the text describing
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

import com.silverpeas.mailinglist.PathTestUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.jvnet.mock_javamail.Mailbox;
import org.springframework.test.AbstractSingleSpringContextTests;

import com.silverpeas.mailinglist.service.event.MessageEvent;
import com.silverpeas.mailinglist.service.model.beans.Attachment;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import static com.silverpeas.mailinglist.PathTestUtil.*;

public class TestMessageCheckerWithStubs extends AbstractSingleSpringContextTests {

  private static final String theSimpsonsAttachmentPath = BUILD_PATH + SEPARATOR + "uploads" + SEPARATOR +
      "thesimpsons@silverpeas.com" + SEPARATOR + "{0}" + SEPARATOR +
      "lemonde.html";
  private static final String textEmailContent =
      "Bonjour famille Simpson, j'espère que vous allez bien. " +
      "Ici tout se passe bien et Krusty est très sympathique. Surtout " +
      "depuis que Tahiti Bob est retourné en prison. Je dois remplacer" +
      "l'homme canon dans la prochaine émission.\r\nBart";

  protected String loadHtml() throws IOException {
    StringWriter buffer = null;
    BufferedReader reader = null;
    try {
      buffer = new StringWriter();
      reader =
          new BufferedReader(new InputStreamReader(
          TestMessageCheckerWithStubs.class.getResourceAsStream("lemonde.html")));
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

  protected String[] getConfigLocations() {
    return new String[]{"spring-checker.xml",
          "spring-notification.xml", "spring-fake-services.xml"};
  }

  public void testCheckNewMessages() throws MessagingException, IOException {
    org.jvnet.mock_javamail.Mailbox.clearAll();
    MessageChecker messageChecker = getMessageChecker();
    messageChecker.removeListener("componentId");
    messageChecker.removeListener("thesimpsons@silverpeas.com");
    messageChecker.removeListener("theflanders@silverpeas.com");
    StubMessageListener mockListener1 = new StubMessageListener(
        "thesimpsons@silverpeas.com");
    StubMessageListener mockListener2 = new StubMessageListener(
        "theflanders@silverpeas.com");
    messageChecker.addMessageListener(mockListener1);
    messageChecker.addMessageListener(mockListener2);
    MimeMessage mail = new MimeMessage(messageChecker.getMailSession());
    InternetAddress bart = new InternetAddress("bart.simpson@silverpeas.com");
    InternetAddress theSimpsons = new InternetAddress(
        "thesimpsons@silverpeas.com");
    mail.addFrom(new InternetAddress[]{bart});
    mail.addRecipient(RecipientType.TO, theSimpsons);
    mail.setSubject("Plain text Email test with attachment");
    MimeBodyPart attachment = new MimeBodyPart(
        TestMessageCheckerWithStubs.class.getResourceAsStream("lemonde.html"));
    attachment.setDisposition(Part.INLINE);
    attachment.setFileName("lemonde.html");
    MimeBodyPart body = new MimeBodyPart();
    body.setText(textEmailContent);
    Multipart multiPart = new MimeMultipart();
    multiPart.addBodyPart(body);
    multiPart.addBodyPart(attachment);
    mail.setContent(multiPart);
    mail.setSentDate(new Date());
    Date sentDate1 = new Date(mail.getSentDate().getTime());
    Transport.send(mail);

    mail = new MimeMessage(messageChecker.getMailSession());
    bart = new InternetAddress("bart.simpson@silverpeas.com");
    theSimpsons = new InternetAddress("thesimpsons@silverpeas.com");
    mail.addFrom(new InternetAddress[]{bart});
    mail.addRecipient(RecipientType.TO, theSimpsons);
    mail.setSubject("Plain text Email test");
    mail.setText(textEmailContent);
    mail.setSentDate(new Date());
    Date sentDate2 = new Date(mail.getSentDate().getTime());
    Transport.send(mail);

    //Unauthorized email
    mail = new MimeMessage(messageChecker.getMailSession());
    bart = new InternetAddress("marge.simpson@silverpeas.com");
    theSimpsons = new InternetAddress("thesimpsons@silverpeas.com");
    mail.addFrom(new InternetAddress[]{bart});
    mail.addRecipient(RecipientType.TO, theSimpsons);
    mail.setSubject("Plain text Email test");
    mail.setText(textEmailContent);
    mail.setSentDate(new Date());
    Transport.send(mail);

    assertEquals(3, org.jvnet.mock_javamail.Mailbox.get(
        "thesimpsons@silverpeas.com").size());

    messageChecker.checkNewMessages(new Date());
    assertNull(mockListener2.getMessageEvent());
    MessageEvent event = mockListener1.getMessageEvent();
    assertNotNull(event);
    assertNotNull(event.getMessages());
    assertEquals(2, event.getMessages().size());
    Message message = (Message) event.getMessages().get(0);
    assertEquals("bart.simpson@silverpeas.com", message.getSender());
    assertEquals("Plain text Email test with attachment", message.getTitle());
    assertEquals(textEmailContent, message.getBody());
    assertEquals(textEmailContent.substring(0, 200), message.getSummary());
    assertEquals(sentDate1.getTime(), message.getSentDate().getTime());
    assertTrue(message.getAttachmentsSize() > 0l);
    assertEquals(1, message.getAttachments().size());
    String path = MessageFormat.format(theSimpsonsAttachmentPath,
        new String[]{messageChecker.getMailProcessor().replaceSpecialChars(
          message.getMessageId())});
    Attachment attached =
        (Attachment) message.getAttachments().iterator().next();
    assertEquals(path, attached.getPath());
    assertEquals("thesimpsons@silverpeas.com", message.getComponentId());

    message = (Message) event.getMessages().get(1);
    assertEquals("bart.simpson@silverpeas.com", message.getSender());
    assertEquals("Plain text Email test", message.getTitle());
    assertEquals(textEmailContent, message.getBody());
    assertEquals(textEmailContent.substring(0, 200), message.getSummary());
    assertEquals(0, message.getAttachmentsSize());
    assertEquals(0, message.getAttachments().size());
    assertEquals("thesimpsons@silverpeas.com", message.getComponentId());
    assertEquals(sentDate2.getTime(), message.getSentDate().getTime());
  }

  protected MessageChecker getMessageChecker() {
    return (MessageChecker) applicationContext.getBean("messageChecker");
  }

  @Override
  protected void onTearDown() {
    Mailbox.clearAll();
    try {
      FileFolderManager.deleteFolder("c:\\tmp\\uploads\\componentId", false);
    } catch (UtilException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
