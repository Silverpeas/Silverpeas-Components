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
package com.silverpeas.mailinglist.service.job;

import java.io.InputStream;
import java.util.Properties;

import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.silverpeas.mailinglist.service.event.MessageEvent;
import com.silverpeas.mailinglist.service.event.MessageListener;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
public class TestYahooMailConnection {

  Properties props;

  @Before
  public void setUp() throws Exception {
    props = new Properties();
    InputStream in = this.getClass().getResourceAsStream(
        "/org/silverpeas/mailinglist/service/job/yahoomail.properties");
    try {
      props.load(in);
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  @Test
  public void testOpenImapConnection() throws Exception {
    Store mailAccount = null;
    Folder inbox = null;
    Session mailSession = Session.getInstance(System.getProperties());
    try {
      mailSession.setDebug(true);
      mailAccount = mailSession.getStore(props.getProperty("mail.server.protocol"));
      mailAccount.connect(props.getProperty("mail.server.host"),
          Integer.parseInt(props.getProperty("mail.server.port")),
          props.getProperty("mail.server.login"),
          props.getProperty("mail.server.password"));
      inbox = mailAccount.getFolder("INBOX");
      if (inbox == null) {
        throw new MessagingException("No POP3 INBOX");
      }
      // -- Open the folder for read write --
      inbox.open(Folder.READ_WRITE);

      // -- Get the message wrappers and process them --
      javax.mail.Message[] msgs = inbox.getMessages();
      FetchProfile profile = new FetchProfile();
      profile.add(FetchProfile.Item.FLAGS);
      inbox.fetch(msgs, profile);
      MailProcessor processor = new MailProcessor();
      MessageListener mailingList = mock(MessageListener.class);
      when(mailingList.checkSender(anyString())).thenReturn(Boolean.TRUE);
      when(mailingList.getComponentId()).thenReturn("mailingList38");
      MessageEvent event = new MessageEvent();
      for (javax.mail.Message message : msgs) {
        processor.prepareMessage((MimeMessage) message, mailingList, event);
      }
      assertThat(event.getMessages(), is(notNullValue()));
      assertThat(event.getMessages().size(), is(msgs.length));
      for (com.silverpeas.mailinglist.service.model.beans.Message message : event.getMessages()) {
        assertThat(message, is(notNullValue()));
        assertThat(message.getMessageId(), is(notNullValue()));
      }
    } finally {
      // -- Close down nicely --
      if (inbox != null) {
        inbox.close(false);
      }
      if (mailAccount != null) {
        mailAccount.close();
      }
    }
  }
}
