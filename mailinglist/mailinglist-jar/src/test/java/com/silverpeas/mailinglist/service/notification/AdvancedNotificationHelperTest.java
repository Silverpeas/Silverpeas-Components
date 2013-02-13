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

import java.io.IOException;
import java.io.InputStream;
import com.mockrunner.mock.jms.MockQueue;
import com.silverpeas.mailinglist.jms.MockObjectFactory;
import com.silverpeas.mailinglist.service.ServicesFactory;
import com.silverpeas.mailinglist.service.model.beans.ExternalUser;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.stratelia.silverpeas.notificationserver.NotificationData;
import com.stratelia.silverpeas.notificationserver.NotificationServerUtil;
import com.stratelia.webactiv.beans.admin.AdminReference;
import com.stratelia.webactiv.util.JNDINames;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.mock_javamail.Mailbox;
import javax.jms.QueueConnectionFactory;
import javax.jms.TextMessage;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.InitialContext;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.silverpeas.jndi.SimpleMemoryContextFactory;

import com.stratelia.webactiv.util.DBUtil;
import static org.junit.Assert.*;

public class AdvancedNotificationHelperTest {

  private static final String textEmailContent =
      "Bonjour famille Simpson, j'espère que vous allez bien. "
      + "Ici tout se passe bien et Krusty est très sympathique. Surtout "
      + "depuis que Tahiti Bob est retourné en prison. Je dois remplacer "
      + "l'homme canon dans la prochaine émission.Bart";
  private AdvancedNotificationHelper notificationHelper;
  
  private static DataSource dataSource;
  private static ClassPathXmlApplicationContext context;

  @BeforeClass
  public static void setUpClass() throws Exception {
    SimpleMemoryContextFactory.setUpAsInitialContext();
    context = new ClassPathXmlApplicationContext(new String[]{"/spring-checker.xml",
      "/spring-advanced-notification.xml", "/spring-jpa-hibernate.xml", "/spring-embedded-datasource.xml",
      "/spring-personalization.xml"});
    dataSource = context.getBean("jpaDataSource", DataSource.class);
    InitialContext ic = new InitialContext();
    ic.rebind("jdbc/Silverpeas", dataSource);
    DBUtil.getInstanceForTest(dataSource.getConnection());
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    DBUtil.clearTestInstance();
    SimpleMemoryContextFactory.tearDownAsInitialContext();
    context.close();
  }

  @Before
  public void init() throws Exception {
    IDatabaseConnection connection = getConnection();
    DatabaseOperation.CLEAN_INSERT.execute(connection, getDataSet());
    connection.close();
     notificationHelper = context.getBean(AdvancedNotificationHelper.class);
    AdminReference.getAdminService().reloadCache();
    registerMockJMS();
    Mailbox.clearAll();
  }

  @After
  public void after() throws Exception {
    IDatabaseConnection connection = getConnection();
    DatabaseOperation.DELETE_ALL.execute(connection, getDataSet());
    connection.close();
    MockObjectFactory.clearAll();
    Mailbox.clearAll();
  }

  private IDatabaseConnection getConnection() throws Exception {
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    return connection;
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
  public void testNotifyInternals() throws Exception {
    Message message = ServicesFactory.getMessageService().getMessage("700");
    assertNotNull(message);
    MailingList list = ServicesFactory.getMailingListService().findMailingList("100");
    assertNotNull(list);
    assertNotNull(list.getModerators());
    assertEquals(3, list.getModerators().size());
    assertNotNull(list.getReaders());
    assertEquals(2, list.getReaders().size());
    List<String> userIds = Arrays.asList(new String[]{"200", "201", "202", "203", "204"});
    notificationHelper.notifyInternals(message, list, userIds, null, false);
    List<TextMessage> messages = MockObjectFactory.getMessages(JNDINames.JMS_QUEUE);
    assertNotNull(messages);
    assertEquals(5, messages.size());
    for (TextMessage alert : messages) {
      assertNotNull(alert.getText());
      NotificationData data = NotificationServerUtil.convertXMLToNotificationData(alert.getText());
      assertNotNull(data);
      String channel = data.getTargetChannel();
      assertEquals("SMTP", channel);
      String recipient = data.getTargetReceipt();
      assertNotNull(recipient);
      assertTrue("Erreur destinataire " + recipient,
          "homer.simpson@silverpeas.com".equals(recipient) || "marge.simpson@silverpeas.com".equals(
          recipient) || "lisa.simpson@silverpeas.com".equals(recipient)
          || "maggie.simpson@silverpeas.com".equals(recipient) || "bart.simpson@silverpeas.com".
          equals(recipient));
      String notificationMessage = "<html><head/><body>p><b>Message [" + message.getTitle()
          + "] :</b></p><p>" + message.getSummary()
          + " ...<br/><a href=\"/Rmailinglist/100/message/700\">Cliquer ici</a></p></body></html>";
      assertEquals(notificationMessage, data.getMessage());
      String url = (String) data.getTargetParam().get("URL");
      assertNotNull(url);
      assertEquals(
          "http://localhost:8000/silverpeas//autoRedirect.jsp?domainId=0&"
          + "goto=%2FRmailinglist%2F100%2Fmessage%2F700", url);
      String source = (String) data.getTargetParam().get("SOURCE");
      assertNotNull(source);
      assertEquals("thesimpsons@silverpeas.com", source);
    }

  }

  @Test
  public void testNotifyExternals() throws Exception {
    Message message = ServicesFactory.getMessageService().getMessage("700");
    message.setContentType("text/plain; charset=\"UTF-8\"");
    assertNotNull(message);
    MailingList list = ServicesFactory.getMailingListService().findMailingList(
        "100");
    assertNotNull(list);
    assertNotNull(list.getExternalSubscribers());
    assertEquals(12, list.getExternalSubscribers().size());
    notificationHelper.notifyExternals(message, list);
    Iterator<ExternalUser> iter = list.getExternalSubscribers().iterator();
    while (iter.hasNext()) {
      ExternalUser recipient = iter.next();
      checkSimpleEmail(recipient.getEmail(), "[Liste de diffusion de test] : Simple Message");
    }

  }

  public void testSimpleSendMail() throws Exception {
    MimeMessage mail = new MimeMessage(notificationHelper.getSession());
    InternetAddress theSimpsons = new InternetAddress(
        "thesimpsons@silverpeas.com");
    mail.addFrom(new InternetAddress[]{theSimpsons});
    mail.setSubject("Simple text Email test");
    mail.setText(textEmailContent);
    List<ExternalUser> externalUsers = new LinkedList<ExternalUser>();
    ExternalUser user = new ExternalUser();
    user.setComponentId("100");
    user.setEmail("bart.simpson@silverpeas.com");
    externalUsers.add(user);
    notificationHelper.sendMail(mail, externalUsers);
    checkSimpleEmail("bart.simpson@silverpeas.com", "Simple text Email test");
  }

  @Test
  public void testMultiSendMail() throws Exception {
    MimeMessage mail = new MimeMessage(notificationHelper.getSession());
    InternetAddress theSimpsons = new InternetAddress(
        "thesimpsons@silverpeas.com");
    mail.addFrom(new InternetAddress[]{theSimpsons});
    mail.setSubject("Simple text Email test");
    mail.setText(textEmailContent);
    List<ExternalUser> externalUsers = new LinkedList<ExternalUser>();
    ExternalUser user = new ExternalUser();
    user.setComponentId("100");
    user.setEmail("bart.simpson@silverpeas.com");
    externalUsers.add(user);
    user = new ExternalUser();
    user.setComponentId("100");
    user.setEmail("homer.simpson@silverpeas.com");
    externalUsers.add(user);
    user = new ExternalUser();
    user.setComponentId("100");
    user.setEmail("lisa.simpson@silverpeas.com");
    externalUsers.add(user);
    user = new ExternalUser();
    user.setComponentId("100");
    user.setEmail("marge.simpson@silverpeas.com");
    externalUsers.add(user);
    user = new ExternalUser();
    user.setComponentId("100");
    user.setEmail("maggie.simpson@silverpeas.com");
    externalUsers.add(user);
    user = new ExternalUser();
    user.setComponentId("100");
    user.setEmail("ned.flanders@silverpeas.com");
    externalUsers.add(user);
    user = new ExternalUser();
    user.setComponentId("100");
    user.setEmail("maude.flanders@silverpeas.com");
    externalUsers.add(user);
    user = new ExternalUser();
    user.setComponentId("100");
    user.setEmail("rod.flanders@silverpeas.com");
    externalUsers.add(user);
    user = new ExternalUser();
    user.setComponentId("100");
    user.setEmail("todd.flanders@silverpeas.com");
    externalUsers.add(user);
    user = new ExternalUser();
    user.setComponentId("100");
    user.setEmail("krusty.theklown@silverpeas.com");
    externalUsers.add(user);
    user = new ExternalUser();
    user.setComponentId("100");
    user.setEmail("selma.bouvier@silverpeas.com");
    externalUsers.add(user);
    user = new ExternalUser();
    user.setComponentId("100");
    user.setEmail("patty.bouvier@silverpeas.com");
    externalUsers.add(user);
    assertEquals(12, externalUsers.size());
    notificationHelper.sendMail(mail, externalUsers);
    Iterator<ExternalUser> iter = externalUsers.iterator();
    while (iter.hasNext()) {
      ExternalUser recipient = iter.next();
      checkSimpleEmail(recipient.getEmail(), "Simple text Email test");
    }
  }

  @SuppressWarnings(
      "unchecked")
  protected void checkSimpleEmail(String address, String subject)
      throws Exception {
    List inbox = Mailbox.get(address);
    assertNotNull(inbox);
    assertEquals("No message for " + address, 1, inbox.size());
    MimeMessage alert = (MimeMessage) inbox.iterator().next();
    assertNotNull(alert);
    assertEquals(subject, alert.getSubject());
    assertEquals(textEmailContent, alert.getContent());
  }

  public void testSpringLoading() {
    assertNotNull(notificationHelper);
    assertNotNull(notificationHelper.getSmtpConfig());
    assertEquals("localhost", notificationHelper.getSmtpConfig().getServer());
    assertEquals("bsimpson", notificationHelper.getSmtpConfig().getUsername());
    assertEquals("bart", notificationHelper.getSmtpConfig().getPassword());
    assertEquals(25, notificationHelper.getSmtpConfig().getPort());
    assertFalse(notificationHelper.getSmtpConfig().isAuthenticate());
  }

  @Test
  public void testGetUsersIds() {
    MailingList list = ServicesFactory.getMailingListService().findMailingList("100");
    list.setModerated(false);
    Collection<String> userIds = notificationHelper.getUsersIds(list);
    assertEquals(2, userIds.size());
    for (String userId : userIds) {
      assertTrue("201".equals(userId) || "204".equals(userId));
    }
    list.setModerated(true);
    userIds = notificationHelper.getUsersIds(list);
    assertEquals(2, userIds.size());
    for (String userId : userIds) {
      assertTrue("201".equals(userId) || "204".equals(userId));
    }
  }

  @Test
  public void testGetModeratorsIds() {
    MailingList list = ServicesFactory.getMailingListService().findMailingList(
        "100");
    Collection<String> userIds = notificationHelper.getModeratorsIds(list);
    assertEquals(3, userIds.size());
    for (String userId : userIds) {
      assertTrue("Erreur userid " + userId, "200".equals(userId) || "202".equals(userId) || "203".
          equals(userId));
    }
    list.setModerated(false);
    userIds = notificationHelper.getModeratorsIds(list);
    assertEquals(3, userIds.size());
    for (String userId : userIds) {
      assertTrue("Erreur userid " + userId, "200".equals(userId) || "202".equals(userId) || "203".
          equals(userId));
    }
  }
  
 
  protected IDataSet getDataSet() throws DataSetException, IOException {
    FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
    InputStream in = TestNotificationHelper.class.getResourceAsStream("test-notification-helper-dataset.xml");
    try {
      return new ReplacementDataSet(builder.build(in));
    } finally {
      IOUtils.closeQuietly(in);
    }
  }
}
