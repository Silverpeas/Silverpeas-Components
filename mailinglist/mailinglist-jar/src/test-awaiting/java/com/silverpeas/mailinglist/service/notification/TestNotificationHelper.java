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
package com.silverpeas.mailinglist.service.notification;

import com.silverpeas.mailinglist.AbstractMailingListTest;
import com.silverpeas.mailinglist.jms.MockObjectFactory;
import com.silverpeas.mailinglist.service.ServicesFactory;
import com.silverpeas.mailinglist.service.model.beans.ExternalUser;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.stratelia.silverpeas.notificationserver.NotificationData;
import com.stratelia.silverpeas.notificationserver.NotificationServerUtil;
import org.silverpeas.util.JNDINames;
import org.apache.commons.io.IOUtils;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.mock_javamail.Mailbox;
import org.silverpeas.mail.MailSending;

import javax.jms.TextMessage;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.silverpeas.mail.MailAddress.eMail;

public class TestNotificationHelper extends AbstractMailingListTest {

  private static final String TECHNICAL_CONTENT =
      "<!--BEFORE_MESSAGE_FOOTER--><!--AFTER_MESSAGE_FOOTER-->";

  private static final String textEmailContent =
      "Bonjour famille Simpson, j'espère que vous allez bien. "
      + "Ici tout se passe bien et Krusty est très sympathique. Surtout "
      + "depuis que Tahiti Bob est retourné en prison. Je dois remplacer "
      + "l'homme canon dans la prochaine émission.Bart";
  private SimpleNotificationHelper notificationHelper;

  @After
  public void clearMailBox() throws Exception {
    Mailbox.clearAll();
  }

  @Before
  public void setupMailbox() throws Exception {
    Mailbox.clearAll();
    notificationHelper = getManagedService(SimpleNotificationHelper.class);
  }

  @Test
  public void testNotifyInternals() throws Exception {
    ServicesFactory servicesFactory = ServicesFactory.getFactory();
    Message message = servicesFactory.getMessageService().getMessage("700");
    assertThat(message, is(notNullValue()));
    MailingList list = servicesFactory.getMailingListService().findMailingList("100");
    assertThat(list, is(notNullValue()));
    assertThat(list.getModerators(), is(notNullValue()));
    assertThat(list.getModerators().size(), is(3));
    assertThat(list.getReaders(), is(notNullValue()));
    assertThat(list.getReaders().size(), is(2));
    List<String> userIds = Arrays.asList(new String[]{"200", "201", "202", "203", "204"});
    notificationHelper.notifyInternals(message, list, userIds, null, false);
    List<TextMessage> messages = MockObjectFactory.getMessages(JNDINames.JMS_QUEUE);
    assertThat(messages, is(notNullValue()));
    assertThat(messages.size(), is(5));
    for (TextMessage alert : messages) {
      assertThat(alert.getText(), is(notNullValue()));
      NotificationData data = NotificationServerUtil.convertXMLToNotificationData(alert.getText());
      assertThat(data, is(notNullValue()));
      String channel = data.getTargetChannel();
      assertThat(channel, is("SMTP"));
      String recipient = data.getTargetReceipt();
      assertThat(recipient, is(notNullValue()));
      assertThat("Erreur destinataire " + recipient, recipient, isOneOf(
          "homer.simpson@silverpeas.com", "marge.simpson@silverpeas.com",
          "lisa.simpson@silverpeas.com", "maggie.simpson@silverpeas.com",
          "bart.simpson@silverpeas.com"));
      assertThat(data.getMessage(), is(message.getSummary() + TECHNICAL_CONTENT));
      String url = (String) data.getTargetParam().get("URL");
      assertThat(url, is(notNullValue()));
      assertThat(url, is("http://localhost:8000/silverpeas//autoRedirect.jsp?domainId=0&"
          + "goto=%2FRmailinglist%2F100%2Fmessage%2F700"));
      String source = (String) data.getTargetParam().get("SOURCE");
      assertThat(source, is(notNullValue()));
      assertThat(source, is("thesimpsons@silverpeas.com"));
    }

  }

  @Test
  public void testNotifyExternals() throws Exception {
    ServicesFactory servicesFactory = ServicesFactory.getFactory();
    Message message = servicesFactory.getMessageService().getMessage("700");
    message.setContentType("text/plain; charset=\"UTF-8\"");
    assertThat(message, is(notNullValue()));
    MailingList list = servicesFactory.getMailingListService().findMailingList("100");
    assertThat(list, is(notNullValue()));
    assertThat(list.getExternalSubscribers(), is(notNullValue()));
    assertThat(list.getExternalSubscribers().size(), is(12));
    notificationHelper.notifyExternals(message, list);
    Iterator<ExternalUser> iter = list.getExternalSubscribers().iterator();
    while (iter.hasNext()) {
      ExternalUser recipient = iter.next();
      checkSimpleEmail(recipient.getEmail(), "[Liste de diffusion de test] : Simple Message");
    }

  }

  @Test
  public void testSimpleSendMail() throws Exception {
    MailSending mail = MailSending.from(eMail("thesimpsons@silverpeas.com"));
    mail.withSubject("Simple text Email test");
    mail.withContent(textEmailContent);
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
    MailSending mail = MailSending.from(eMail("thesimpsons@silverpeas.com"));
    mail.withSubject("Simple text Email test");
    mail.withContent(textEmailContent);
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
    assertThat(externalUsers.size(), is(12));
    notificationHelper.sendMail(mail, externalUsers);
    Iterator<ExternalUser> iter = externalUsers.iterator();
    while (iter.hasNext()) {
      ExternalUser recipient = iter.next();
      checkSimpleEmail(recipient.getEmail(), "Simple text Email test");
    }
  }

  protected void checkSimpleEmail(String address, String subject)
      throws Exception {
    Mailbox inbox = Mailbox.get(address);
    assertThat(inbox, is(notNullValue()));
    assertThat("No message for " + address, inbox.size(), is(1));
    MimeMessage alert = (MimeMessage) inbox.iterator().next();
    assertThat(alert, is(notNullValue()));
    assertThat(alert.getSubject(), is(subject));
    assertThat((String) alert.getContent(), is(textEmailContent));
  }

  @Test
  public void testSpringLoading() {
    SimpleNotificationHelper helper = notificationHelper;
    assertThat(helper, is(notNullValue()));
    assertThat(helper.getSmtpConfig(), is(notNullValue()));
    assertThat(helper.getSmtpConfig().getServer(), is("localhost"));
    assertThat(helper.getSmtpConfig().getUsername(), is("bsimpson"));
    assertThat(helper.getSmtpConfig().getPassword(), is("bart"));
    assertThat(helper.getSmtpConfig().getPort(), is(25));
    assertThat(helper.getSmtpConfig().isAuthenticate(), is(false));
  }

  @Test
  public void testGetUsersIds() {
    MailingList list = ServicesFactory.getFactory().getMailingListService().findMailingList("100");
    list.setModerated(false);
    Collection<String> userIds = notificationHelper.getUsersIds(list);
    assertThat(userIds.size(), is(2));
    for (String userId : userIds) {
      assertThat(userId, isOneOf("201", "204"));
    }
    list.setModerated(true);
    userIds = notificationHelper.getUsersIds(list);
    assertThat(userIds.size(), is(2));
    for (String userId : userIds) {
      assertThat(userId, isOneOf("201", "204"));
    }
  }

  @Test
  public void testGetModeratorsIds() {
    MailingList list = ServicesFactory.getFactory().getMailingListService().findMailingList("100");
    Collection<String> userIds = notificationHelper.getModeratorsIds(list);
    assertThat(userIds.size(), is(3));
    for (String userId : userIds) {
      assertThat("Erreur userid " + userId, userId, isOneOf("200", "202", "203"));
    }
    list.setModerated(false);
    userIds = notificationHelper.getModeratorsIds(list);
    assertThat(userIds.size(), is(3));
    for (String userId : userIds) {
      assertThat("Erreur userid " + userId, userId, isOneOf("200", "202", "203"));
    }
  }

  @Override
  protected IDataSet getDataSet() throws DataSetException, IOException {
    FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
    InputStream in = TestNotificationHelper.class.getResourceAsStream(
        "test-notification-helper-dataset.xml");
    try {
      return new ReplacementDataSet(builder.build(in));
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  @Override
  protected String[] getContextConfigurations() {
    return new String[]{"/spring-checker.xml", "/spring-notification.xml",
      "/spring-mailinglist-services-factory.xml", "/spring-mailinglist-personalization-dao.xml",
      "/spring-mailinglist-embbed-datasource.xml"};
  }
}
