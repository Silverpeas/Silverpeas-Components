package com.silverpeas.mailinglist.service.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import com.silverpeas.mailinglist.service.model.beans.ExternalUser;
import com.silverpeas.mailinglist.service.notification.SmtpConfiguration;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * Utility class that sends the mails
 * @author Emmanuel Hugonnet
 * @version $revision$
 */
public class MailSender implements Runnable {

  public static final int BATCH_SIZE = 10;

  private Session session;
  private SmtpConfiguration config;
  private Collection<ExternalUser> externalUsers;
  private MimeMessage mail;

  public MailSender(Session session, MimeMessage mail,
      SmtpConfiguration config, Collection<ExternalUser> externalUsers)
      throws MessagingException {
    this.config = config;
    this.mail = new MimeMessage(mail);
    this.session = Session.getInstance(session.getProperties());
    this.externalUsers = new ArrayList<ExternalUser>(externalUsers);
  }

  public void run() {
    synchronized (this) {
      try {
        sendMail();
      } catch (MessagingException e) {
        SilverTrace.error("mailingList", "MailSender.sendMail",
            "mailinglist.external.notification.send", e);
      }
    }

  }

  private void sendMail() throws MessagingException {
    SilverTrace.debug("mailingList", this.getClass().getName(),
        "mailinglist.notification.external.sending", mail.getSubject());
    Transport transport = null;
    try {
      transport = session.getTransport("smtp");
      if (config.isAuthenticate()) {
        transport.connect(config.getServer(), config.getPort(), config
            .getUsername(), config.getPassword());
      } else {
        transport.connect(config.getServer(), config.getPort(), null, null);
      }
      List<InternetAddress> bcc = new ArrayList<InternetAddress>(BATCH_SIZE);
      Iterator<ExternalUser> iter = externalUsers.iterator();
      int i = 0;
      while (iter.hasNext()) {
        ExternalUser user = iter.next();
        bcc.add(new InternetAddress(user.getEmail()));
        if ((i + 1) % BATCH_SIZE == 0 || !iter.hasNext()) {
          mail.setRecipients(RecipientType.BCC, (InternetAddress[]) bcc
              .toArray(new InternetAddress[bcc.size()]));
          transport.sendMessage(mail, mail.getRecipients(RecipientType.BCC));
          SilverTrace.debug("mailingList", this.getClass().getName(),
              "mailinglist.notification.external.done", bcc.toString());
          bcc.removeAll(bcc);
        }
        i++;
      }
    } finally {
      transport.close();
    }
  }

}
