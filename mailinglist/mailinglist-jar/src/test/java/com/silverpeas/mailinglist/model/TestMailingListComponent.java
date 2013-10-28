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
package com.silverpeas.mailinglist.model;

import com.silverpeas.mailinglist.AbstractMailingListTest;
import com.silverpeas.mailinglist.jms.MockObjectFactory;
import com.silverpeas.mailinglist.service.ServicesFactory;
import com.silverpeas.mailinglist.service.event.MessageEvent;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.stratelia.silverpeas.notificationserver.NotificationData;
import com.stratelia.silverpeas.notificationserver.NotificationServerUtil;
import com.stratelia.webactiv.util.JNDINames;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.jms.TextMessage;
import javax.mail.internet.MimeMessage;
import org.apache.commons.io.IOUtils;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.mock_javamail.Mailbox;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class TestMailingListComponent extends AbstractMailingListTest {

  private MailingListComponent component = new MailingListComponent("100");
  private static final String textEmailContent =
      "Bonjour famille Simpson, j'espère que vous allez bien. "
      + "Ici tout se passe bien et Krusty est très sympathique. Surtout "
      + "depuis que Tahiti Bob est retourné en prison. Je dois remplacer "
      + "l'homme canon dans la prochaine émission.Bart";

  @After
  public void onTearDown() throws Exception {
    Mailbox.clearAll();
  }

  @Before
  public void onSetUp() {
    Mailbox.clearAll();
  }

  @Override
  protected IDataSet getDataSet() throws DataSetException, IOException {
    InputStream in = null;
    try {
      in = TestMailingListComponent.class.getResourceAsStream("test-component-dataset.xml");
      return new FlatXmlDataSetBuilder().build(in);
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  @Test
  public void testCheckSender() {
    assertThat(component.checkSender("bart.simpson@silverpeas.com"), is(true));
    assertTrue(component.checkSender("lisa.simpson@silverpeas.com"));
    assertTrue(component.checkSender("marge.simpson@silverpeas.com"));
    assertTrue(component.checkSender("maggie.simpson@silverpeas.com"));
    assertTrue(component.checkSender("homer.simpson@silverpeas.com"));
    assertFalse(component.checkSender("krusty.theklown@silverpeas.com"));
    assertTrue(component.checkSender("barney.gumble@silverpeas.com"));
    assertTrue(component.checkSender("julius.hibbert@silverpeas.com"));
    assertTrue(component.checkSender("carl.carlson@silverpeas.com"));
    assertTrue(component.checkSender("edna.krabappel@silverpeas.com"));
    assertTrue(component.checkSender("nelson.muntz@silverpeas.com"));
    assertTrue(component.checkSender("ned.flanders@silverpeas.com"));
    assertTrue(component.checkSender("maude.flanders@silverpeas.com"));
    assertTrue(component.checkSender("rod.flanders@silverpeas.com"));
    assertTrue(component.checkSender("todd.flanders@silverpeas.com"));
    assertTrue(component.checkSender("herschel.krustofski@silverpeas.com"));
    assertTrue(component.checkSender("selma.bouvier@silverpeas.com"));
    assertTrue(component.checkSender("patty.bouvier@silverpeas.com"));
    assertFalse(component.checkSender("bart.simpson@gmail.com"));
    assertFalse(component.checkSender("otto.mann@silverpeas.com"));
  }

  @Test
  public void testOnMessage() throws Exception {
    Message message = ServicesFactory.getFactory().getMessageService().getMessage("700");
    message.setContentType("text/plain; charset=\"UTF-8\"");
    MessageEvent event = new MessageEvent();
    event.addMessage(message);
    component.onMessage(event);
    List<TextMessage> jmsMessages = MockObjectFactory.getMessages(JNDINames.JMS_QUEUE);
    assertNotNull(jmsMessages);
    assertEquals(3, jmsMessages.size());
    for (TextMessage alert : jmsMessages) {
      assertNotNull(alert.getText());
      NotificationData data = NotificationServerUtil.convertXMLToNotificationData(alert.getText());
      assertNotNull(data);
      String channel = data.getTargetChannel();
      assertEquals("SMTP", channel);
      String recipient = data.getTargetReceipt();
      assertNotNull(recipient);
      assertTrue("Erreur destinataire " + recipient,
          "homer.simpson@silverpeas.com".equals(recipient) || "marge.simpson@silverpeas.com"
          .equals(recipient) || "bart.simpson@silverpeas.com".equals(recipient));
      assertEquals(message.getSummary(), data.getMessage());
      String url = (String) data.getTargetParam().get("URL");
      assertNotNull(url);
      assertEquals("http://localhost:8000/silverpeas//autoRedirect.jsp?domainId=0&"
          + "goto=%2FRmailinglist%2F100%2FmoderationList%2F100", url);
      String source = (String) data.getTargetParam().get("SOURCE");
      assertNotNull(source);
      assertEquals("thesimpsons@silverpeas.com", source);
    }
    checkNoMessage("barney.gumble@silverpeas.com");
    checkNoMessage("julius.hibbert@silverpeas.com");
    checkNoMessage("carl.carlson@silverpeas.com");
    checkNoMessage("edna.krabappel@silverpeas.com");
    checkNoMessage("nelson.muntz@silverpeas.com");
    checkNoMessage("ned.flanders@silverpeas.com");
    checkNoMessage("maude.flanders@silverpeas.com");
    checkNoMessage("rod.flanders@silverpeas.com");
    checkNoMessage("todd.flanders@silverpeas.com");
    checkNoMessage("herschel.krustofski@silverpeas.com");
    checkNoMessage("selma.bouvier@silverpeas.com");
    checkNoMessage("patty.bouvier@silverpeas.com");

    message = ServicesFactory.getFactory().getMessageService().getMessage(message.getId());
    message.setModerated(true);
    event = new MessageEvent();
    event.addMessage(message);
    component.onMessage(event);
    jmsMessages = MockObjectFactory.getMessages(JNDINames.JMS_QUEUE);
    assertNotNull(jmsMessages);
    assertEquals(6, jmsMessages.size());
    for (TextMessage alert : jmsMessages) {
      assertNotNull(alert.getText());
      NotificationData data = NotificationServerUtil.convertXMLToNotificationData(alert.getText());
      assertNotNull(data);
      String channel = data.getTargetChannel();
      assertEquals("SMTP", channel);
      String recipient = data.getTargetReceipt();
      assertNotNull(recipient);
      assertTrue("Erreur destinataire " + recipient,
          "bart.simpson@silverpeas.com".equals(recipient) || "homer.simpson@silverpeas.com"
          .equals(recipient) || "marge.simpson@silverpeas.com".equals(recipient));
      assertEquals(message.getSummary(), data.getMessage());
      String url = (String) data.getTargetParam().get("URL");
      assertNotNull(url);
      assertEquals(
          "http://localhost:8000/silverpeas//autoRedirect.jsp?domainId=0&"
          + "goto=%2FRmailinglist%2F100%2FmoderationList%2F100", url);
      String source = (String) data.getTargetParam().get("SOURCE");
      assertNotNull(source);
      assertEquals("thesimpsons@silverpeas.com", source);
    }
    checkNoMessage("barney.gumble@silverpeas.com");
    checkNoMessage("julius.hibbert@silverpeas.com");
    checkNoMessage("carl.carlson@silverpeas.com");
    checkNoMessage("edna.krabappel@silverpeas.com");
    checkNoMessage("nelson.muntz@silverpeas.com");
    checkNoMessage("ned.flanders@silverpeas.com");
    checkNoMessage("maude.flanders@silverpeas.com");
    checkNoMessage("rod.flanders@silverpeas.com");
    checkNoMessage("todd.flanders@silverpeas.com");
    checkNoMessage("herschel.krustofski@silverpeas.com");
    checkNoMessage("selma.bouvier@silverpeas.com");
    checkNoMessage("patty.bouvier@silverpeas.com");
  }

  @Test
  public void testOnMessageNotModeratedNotify() throws Exception {
    MailingListComponent componentNotModerated = new MailingListComponent("101");
    MailingList list = ServicesFactory.getFactory().getMailingListService().findMailingList("101");
    assertNotNull(list);
    assertNotNull(list.getModerators());
    assertEquals(3, list.getModerators().size());
    assertNotNull(list.getReaders());
    assertEquals(2, list.getReaders().size());
    assertFalse(list.isModerated());
    assertTrue(list.isNotify());
    Message message = ServicesFactory.getFactory().getMessageService().getMessage("701");
    assertEquals(textEmailContent, message.getBody());
    message.setContentType("text/plain; charset=\"UTF-8\"");
    MessageEvent event = new MessageEvent();
    event.addMessage(message);
    componentNotModerated.onMessage(event);
    List<TextMessage> jmsMessages = MockObjectFactory.getMessages(JNDINames.JMS_QUEUE);
    assertNotNull(jmsMessages);
    assertEquals(5, jmsMessages.size());
    for (TextMessage alert : jmsMessages) {
      assertNotNull(alert.getText());
      NotificationData data = NotificationServerUtil.convertXMLToNotificationData(alert.getText());
      assertNotNull(data);
      String channel = data.getTargetChannel();
      assertEquals("SMTP", channel);
      String recipient = data.getTargetReceipt();
      assertNotNull(recipient);
      assertTrue("Erreur destinataire " + recipient,
          "bart.simpson@silverpeas.com".equals(recipient) || "marge.simpson@silverpeas.com"
          .equals(
          recipient) || "homer.simpson@silverpeas.com".equals(recipient)
          || "lisa.simpson@silverpeas.com".equals(recipient) || "maggie.simpson@silverpeas.com"
          .equals(recipient));
      assertEquals(message.getSummary(), data.getMessage());
      String url = (String) data.getTargetParam().get("URL");
      assertNotNull(url);
      assertEquals(
          "http://localhost:8000/silverpeas//autoRedirect.jsp?domainId=0&"
          + "goto=%2FRmailinglist%2F101%2Fmessage%2F701", url);
      String source = (String) data.getTargetParam().get("SOURCE");
      assertNotNull(source);
      assertEquals("thesimpsons@silverpeas.com", source);
    }
    checkSimpleEmail("barney.gumble@silverpeas.com",
        "[Liste de diffusion de test non modérée] : Simple Message");
    checkSimpleEmail("julius.hibbert@silverpeas.com",
        "[Liste de diffusion de test non modérée] : Simple Message");
    checkSimpleEmail("carl.carlson@silverpeas.com",
        "[Liste de diffusion de test non modérée] : Simple Message");
    checkSimpleEmail("edna.krabappel@silverpeas.com",
        "[Liste de diffusion de test non modérée] : Simple Message");
    checkSimpleEmail("nelson.muntz@silverpeas.com",
        "[Liste de diffusion de test non modérée] : Simple Message");
    checkSimpleEmail("ned.flanders@silverpeas.com",
        "[Liste de diffusion de test non modérée] : Simple Message");
    checkSimpleEmail("maude.flanders@silverpeas.com",
        "[Liste de diffusion de test non modérée] : Simple Message");
    checkSimpleEmail("rod.flanders@silverpeas.com",
        "[Liste de diffusion de test non modérée] : Simple Message");
    checkSimpleEmail("todd.flanders@silverpeas.com",
        "[Liste de diffusion de test non modérée] : Simple Message");
    checkSimpleEmail("herschel.krustofski@silverpeas.com",
        "[Liste de diffusion de test non modérée] : Simple Message");
    checkSimpleEmail("selma.bouvier@silverpeas.com",
        "[Liste de diffusion de test non modérée] : Simple Message");
    checkSimpleEmail("patty.bouvier@silverpeas.com",
        "[Liste de diffusion de test non modérée] : Simple Message");
  }

  protected void checkNoMessage(String address) throws Exception {
    Mailbox inbox = Mailbox.get(address);
    assertNotNull(inbox);
    assertEquals(0, inbox.size());
  }

  protected void checkSimpleEmail(String address, String subject)
      throws Exception {
    Mailbox inbox = Mailbox.get(address);
    assertNotNull(inbox);
    assertEquals("No message for " + address, 1, inbox.size());
    MimeMessage alert = (MimeMessage) inbox.iterator().next();
    assertNotNull(alert);
    assertEquals(subject, alert.getSubject());
    assertEquals("text/plain; charset=\"UTF-8\"", alert.getContentType());
    assertEquals(textEmailContent, (String) alert.getContent());
  }

  @Override
  protected String[] getContextConfigurations() {
    return new String[]{"/spring-checker.xml", "/spring-notification.xml",
      "/spring-mailinglist-services-factory.xml", "/spring-mailinglist-personalization-dao.xml",
      "/spring-mailinglist-embbed-datasource.xml"};
  }
}
