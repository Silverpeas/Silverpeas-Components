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

import java.net.URL;
import java.util.Properties;

import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import junit.framework.TestCase;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class TestZimbraConnection extends TestCase {

  Properties props;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    props = new Properties();
    props.load(TestZimbraConnection.class
        .getResourceAsStream("notification_zimbra.properties"));
  }

  public void testOpenImapConnection() {
    URL url = this.getClass().getClassLoader().getResource("truststore.jks");
    String path = url.getPath();
    System.out.println(path);
    System.setProperty("javax.net.ssl.trustStore", path);
    System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
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
      Message[] msgs = inbox.getMessages();
        FetchProfile profile = new FetchProfile();
        profile.add(FetchProfile.Item.FLAGS);
        inbox.fetch(msgs, profile);
    } catch (MessagingException mex) {
      SilverTrace.error("mailingList", "MessageChecker.checkNewMessages",
          "mail.processing.error", mex);
    } catch (Exception mex) {
      SilverTrace.error("mailingList", "MessageChecker.checkNewMessages",
          "mail.processing.error", mex);
    } finally {
      // -- Close down nicely --
      try {
        if (inbox != null) {
          inbox.close(false);
        }
        if (mailAccount != null) {
          mailAccount.close();
        }
      } catch (Exception ex2) {
        SilverTrace.error("mailingList", "MessageChecker.checkNewMessages",
            "mail.processing.error", ex2);
      }
    }

  }
}
