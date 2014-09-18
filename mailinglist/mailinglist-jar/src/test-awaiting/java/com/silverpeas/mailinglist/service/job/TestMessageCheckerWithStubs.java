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
package com.silverpeas.mailinglist.service.job;

import com.silverpeas.mailinglist.service.event.MessageEvent;
import com.silverpeas.mailinglist.service.model.beans.Attachment;
import com.silverpeas.mailinglist.service.model.beans.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.lang3.CharEncoding;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.jvnet.mock_javamail.Mailbox;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import static com.silverpeas.mailinglist.PathTestUtil.BUILD_PATH;
import static com.silverpeas.mailinglist.PathTestUtil.SEPARATOR;

public class TestMessageCheckerWithStubs {

  private static final String theSimpsonsAttachmentPath = BUILD_PATH + SEPARATOR + "uploads"
      + SEPARATOR + "thesimpsons@silverpeas.com" + SEPARATOR + "{0}" + SEPARATOR + "lemonde.html";
  private static final String textEmailContent =
      "Bonjour famille Simpson, j'espère que vous allez bien. "
      + "Ici tout se passe bien et Krusty est très sympathique. Surtout "
      + "depuis que Tahiti Bob est retourné en prison. Je dois remplacer"
      + "l'homme canon dans la prochaine émission.\r\nBart";
  private static ConfigurableApplicationContext applicationContext;

  @BeforeClass
  public static void loadContext() {
    applicationContext = new ClassPathXmlApplicationContext("/spring-checker.xml",
        "/spring-notification.xml", "/spring-fake-services.xml");
  }

  @AfterClass
  public static void unloadContext() {
    applicationContext.close();
  }

  protected String loadHtml() throws IOException {
    StringWriter buffer = null;
    BufferedReader reader = null;
    try {
      buffer = new StringWriter();
      reader = new BufferedReader(new InputStreamReader(
          TestMessageCheckerWithStubs.class.getResourceAsStream("lemonde.html"), CharEncoding.UTF_8));
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

    assertThat(org.jvnet.mock_javamail.Mailbox.get("thesimpsons@silverpeas.com").size(), is(3));

    messageChecker.checkNewMessages(new Date());
    assertThat(mockListener2.getMessageEvent(), is(nullValue()));
    MessageEvent event = mockListener1.getMessageEvent();
    assertThat(event, is(notNullValue()));
    assertThat(event.getMessages(), is(notNullValue()));
    assertThat(event.getMessages(), hasSize(2));
    Message message = event.getMessages().get(0);
    assertThat(message.getSender(), is("bart.simpson@silverpeas.com"));
    assertThat(message.getTitle(), is("Plain text Email test with attachment"));
    assertThat(message.getBody(), is(textEmailContent));
    assertThat(message.getSummary(), is(textEmailContent.substring(0, 200)));
    assertThat(message.getSentDate().getTime(), is(sentDate1.getTime()));
    assertThat(message.getAttachmentsSize(), greaterThan(0L));
    assertThat(message.getAttachments(), hasSize(1));
    String path = MessageFormat.format(theSimpsonsAttachmentPath,
        new Object[]{messageChecker.getMailProcessor().replaceSpecialChars(
      message.getMessageId())});
    Attachment attached = message.getAttachments().iterator().next();
    assertThat(attached.getPath(), is(path));
    assertThat(message.getComponentId(), is("thesimpsons@silverpeas.com"));

    message = event.getMessages().get(1);
    assertThat(message.getSender(), is("bart.simpson@silverpeas.com"));
    assertThat(message.getTitle(), is("Plain text Email test"));
    assertThat(message.getBody(), is(textEmailContent));
    assertThat(message.getSummary(), is(textEmailContent.substring(0, 200)));
    assertThat(message.getAttachmentsSize(), is(0L));
    assertThat(message.getAttachments(), hasSize(0));
    assertThat(message.getComponentId(), is("thesimpsons@silverpeas.com"));
    assertThat(message.getSentDate().getTime(), is(sentDate2.getTime()));
  }

  protected MessageChecker getMessageChecker() {
    return (MessageChecker) applicationContext.getBean("messageChecker");
  }

  @After
  public void cleaAll() {
    Mailbox.clearAll();
  }
}
