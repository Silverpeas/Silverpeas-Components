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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.mailinglist.service.notification;

import com.mockrunner.mock.jms.MockQueue;
import com.silverpeas.mailinglist.AbstractMailingListTest;
import com.silverpeas.mailinglist.jms.MockObjectFactory;
import com.silverpeas.mailinglist.service.ServicesFactory;
import com.silverpeas.mailinglist.service.model.beans.InternalUser;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.stratelia.silverpeas.notificationserver.NotificationData;
import com.stratelia.silverpeas.notificationserver.NotificationServerUtil;
import com.stratelia.webactiv.beans.admin.AdminReference;
import com.stratelia.webactiv.util.JNDINames;
import java.io.IOException;
import java.util.List;
import javax.jms.QueueConnectionFactory;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.mock_javamail.Mailbox;

import static org.junit.Assert.*;

public class TestCheckNotification extends AbstractMailingListTest {

  private NotificationHelper notificationHelper;

  @Before
  public void init() throws Exception {
    AdminReference.getAdminService().reloadCache();
    Mailbox.clearAll();
    notificationHelper = getManagedService(NotificationHelper.class);
  }

  @After
  public void after() throws Exception {
    Mailbox.clearAll();
  }

  protected void registerMockJMS() throws Exception {
    InitialContext ic = new InitialContext();
    // Construct BasicDataSource reference
    QueueConnectionFactory refFactory = MockObjectFactory.getQueueConnectionFactory();
    ic.rebind(JNDINames.JMS_FACTORY, refFactory);
    ic.rebind(JNDINames.JMS_QUEUE, MockObjectFactory.createQueue(JNDINames.JMS_QUEUE));
    QueueConnectionFactory qconFactory = (QueueConnectionFactory) ic.lookup(JNDINames.JMS_FACTORY);
    assertNotNull(qconFactory);
    MockQueue queue = (MockQueue) ic.lookup(JNDINames.JMS_QUEUE);
    queue.clear();
  }

  @Test
  public void testNotifyArchivageNotModeratedOpen() throws Exception {
    ServicesFactory servicesFactory = ServicesFactory.getFactory();
    MailingList list = servicesFactory.getMailingListService().findMailingList("101");
    Message message = servicesFactory.getMessageService().getMessage("701");
    assertNotNull(message);
    assertNotNull(list);
    assertFalse(list.isModerated());
    assertTrue(list.isOpen());
    assertFalse(list.isNotify());
    assertEquals("Liste archivage non modérée et ouverte avec un lecteur abonné", list.getName());
    assertEquals("thesimpsons@silverpeas.com", list.getSubscribedAddress());
    assertNotNull(list.getInternalSubscribers());
    assertEquals(1, list.getInternalSubscribers().size());
    assertNotNull(list.getReaders());
    assertEquals(1, list.getReaders().size());
    InternalUser reader = list.getReaders().iterator().next();
    assertEquals("maggie.simpson@silverpeas.com", reader.getEmail());
    assertNotNull(list.getModerators());
    assertEquals(1, list.getModerators().size());
    InternalUser moderator = list.getModerators().iterator().next();
    assertEquals("bart.simpson@silverpeas.com", moderator.getEmail());
    assertNotNull(list.getExternalSubscribers());
    assertEquals(0, list.getExternalSubscribers().size());
    assertNotNull(list.getGroupSubscribers());
    assertEquals(0, list.getGroupSubscribers().size());
    notificationHelper.notify(message, list);
    List<TextMessage> messages = MockObjectFactory.getMessages(JNDINames.JMS_QUEUE);
    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  public void testNotifyArchivageModeratedOpen() throws Exception {
    ServicesFactory servicesFactory = ServicesFactory.getFactory();
    MailingList list = servicesFactory.getMailingListService().findMailingList("102");
    Message message = servicesFactory.getMessageService().getMessage("702");
    assertNotNull(message);
    assertNotNull(list);
    assertTrue(list.isModerated());
    assertFalse(list.isOpen());
    assertFalse(list.isNotify());
    assertEquals("Liste archivage modérée et fermée avec un lecteur abonné", list.getName());
    assertEquals("thesimpsons@silverpeas.com", list.getSubscribedAddress());
    assertNotNull(list.getInternalSubscribers());
    assertEquals(1, list.getInternalSubscribers().size());
    assertNotNull(list.getReaders());
    assertEquals(1, list.getReaders().size());
    InternalUser reader = list.getReaders().iterator().next();
    assertEquals("lisa.simpson@silverpeas.com", reader.getEmail());
    assertNotNull(list.getModerators());
    assertEquals(1, list.getModerators().size());
    InternalUser moderator = list.getModerators().iterator().next();
    assertEquals("bart.simpson@silverpeas.com", moderator.getEmail());
    assertNotNull(list.getExternalSubscribers());
    assertEquals(0, list.getExternalSubscribers().size());
    assertNotNull(list.getGroupSubscribers());
    assertEquals(0, list.getGroupSubscribers().size());
    notificationHelper.notify(message, list);
    List<TextMessage> messages = MockObjectFactory.getMessages(JNDINames.JMS_QUEUE);
    assertNotNull(messages);
    assertEquals(1, messages.size());
    for (TextMessage alert : messages) {
      assertNotNull(alert.getText());
      NotificationData data = NotificationServerUtil
          .convertXMLToNotificationData(alert.getText());
      assertNotNull(data);
      String channel = data.getTargetChannel();
      assertEquals("SMTP", channel);
      String recipient = data.getTargetReceipt();
      assertNotNull(recipient);
      assertTrue("Erreur destinataire " + recipient,
          "lisa.simpson@silverpeas.com".equals(recipient));
      assertEquals(message.getSummary(), data.getMessage());
      String url = (String) data.getTargetParam().get("URL");
      assertNotNull(url);
      assertEquals(
          "http://localhost:8000/silverpeas//autoRedirect.jsp?domainId=0&"
          + "goto=%2FRmailinglist%2F102%2Fmessage%2F702", url);
      String source = (String) data.getTargetParam().get("SOURCE");
      assertNotNull(source);
      assertEquals("thesimpsons@silverpeas.com", source);
    }
  }

  @Override
  protected IDataSet getDataSet() throws DataSetException, IOException {
    return new FlatXmlDataSetBuilder().build(TestCheckNotification.class.getResourceAsStream(
        "test-check-notification-dataset.xml"));
  }

  @Override
  protected String[] getContextConfigurations() {
    return new String[]{"/spring-checker.xml", "/spring-notification.xml",
      "/spring-mailinglist-services-factory.xml", "/spring-mailinglist-personalization-dao.xml",
      "/spring-mailinglist-embbed-datasource.xml"};
  }
}
