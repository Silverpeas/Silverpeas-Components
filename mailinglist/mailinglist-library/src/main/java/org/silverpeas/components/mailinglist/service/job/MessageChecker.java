/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.mailinglist.service.job;

import org.silverpeas.components.mailinglist.service.event.MessageEvent;
import org.silverpeas.components.mailinglist.service.event.MessageListener;
import org.silverpeas.components.mailinglist.service.model.MailingListService;
import org.silverpeas.components.mailinglist.service.model.beans.MailingList;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.mail.engine.SmtpConfiguration;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerEvent;
import org.silverpeas.core.scheduler.SchedulerEventListener;
import org.silverpeas.core.scheduler.SchedulerException;
import org.silverpeas.core.scheduler.SchedulerProvider;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.mail.FetchProfile;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Service
public class MessageChecker {

  private static final String MAILING_LIST_JOB_NAME = "mailingListScheduler";

  public static final String IMAP_PROTOCOL = "imap";
  public static final String IMAP_SSL_PROTOCOL = "imaps";
  public static final String POP3_PROTOCOL = "pop3";
  private Map<String, MessageListener> listeners;
  @Inject
  private MailProcessor processor;
  private String mailServer;
  private String login;
  private String password;
  private String protocol;
  private int port;
  private boolean leaveOnServer;
  private Session mailSession;
  @Inject
  private MailingListService mailingListService;


  public MailingListService getMailingListService() {
    return mailingListService;
  }

  public Session getMailSession() {
    return mailSession;
  }

  public void schedule(final JobTrigger trigger) throws SchedulerException {
    Scheduler scheduler = SchedulerProvider.getVolatileScheduler();
    scheduler.scheduleJob(MAILING_LIST_JOB_NAME, trigger, new MessageCheckingListener());
  }

  public void unschedule() throws SchedulerException {
    Scheduler scheduler = SchedulerProvider.getVolatileScheduler();
    if (scheduler.isJobScheduled(MAILING_LIST_JOB_NAME)) {
      scheduler.unscheduleJob(MAILING_LIST_JOB_NAME);
    }
  }

  /**
   * Default constructor
   */
  public MessageChecker() {
    this.listeners = new HashMap<>(10);
    SmtpConfiguration smtpConfig = SmtpConfiguration.fromDefaultSettings();
    SettingBundle notifConfig =
        ResourceLocator.getSettingBundle("org.silverpeas.mailinglist.notification");
    protocol = notifConfig.getString("mail.server.protocol");
    mailServer = notifConfig.getString("mail.server.host");
    login = notifConfig.getString("mail.server.login");
    password = notifConfig.getString("mail.server.password");
    port = notifConfig.getInteger("mail.server.port", smtpConfig.getPort());
    leaveOnServer = notifConfig.getBoolean("mail.server.leave", true);
    mailSession = Session.getInstance(new Properties());
  }

  public String getLogin() {
    return login;
  }

  public String getPassword() {
    return password;
  }

  public String getMailServer() {
    return mailServer;
  }

  public boolean isLeaveOnServer() {
    if (POP3_PROTOCOL.equalsIgnoreCase(this.protocol)) {
      this.leaveOnServer = false;
    }
    return leaveOnServer;
  }

  public int getPort() {
    if (port <= 0) {
      if (POP3_PROTOCOL.equals(getProtocol())) {
        setPort(110);
      } else if (IMAP_PROTOCOL.equals(getProtocol())) {
        setPort(143);
      } else if (IMAP_SSL_PROTOCOL.equals(getProtocol())) {
        setPort(993);
      }
      setPort(110);
    }
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getProtocol() {
    return this.protocol;
  }

  public void setProtocol(String protocol) {
    if (POP3_PROTOCOL.equalsIgnoreCase(protocol) || IMAP_PROTOCOL.equalsIgnoreCase(protocol) ||
        IMAP_SSL_PROTOCOL.equalsIgnoreCase(protocol)) {
      this.protocol = protocol.toLowerCase();
    } else {
      this.protocol = POP3_PROTOCOL;
    }
    if (isImap()) {
      System.setProperty("mail.imap.partialfetch", "false");
    }
  }

  /**
   * Adds a new listener to the list of listeners.
   * @param listener the listener to be added.
   */
  public synchronized void addMessageListener(MessageListener listener) {
    this.listeners.put(listener.getComponentId(), listener);
  }

  /**
   * Gets the new messages on the Mail Server and processes them.
   * @param date the date of the checking.
   */
  public void checkNewMessages(Date date) {
    Store mailAccount = null;
    Folder inbox = null;
    Map<String, MessageListener> listenersByEmail = prepareListeners();
    try {
      mailAccount = mailSession.getStore(getProtocol());
      mailAccount.connect(getMailServer(), getPort(), getLogin(), getPassword());
      inbox = mailAccount.getFolder("INBOX");
      if (inbox == null) {
        throw new MessagingException("No POP3 INBOX");
      }
      // -- Open the folder for read write --
      inbox.open(Folder.READ_WRITE);

      // -- Get the message wrappers and process them --
      Message[] msgs = inbox.getMessages();
      if (isImap()) {
        FetchProfile profile = new FetchProfile();
        profile.add(FetchProfile.Item.FLAGS);
        inbox.fetch(msgs, profile);
      }
      Map<MessageListener, MessageEvent> eventsMap = new HashMap<>();
      for (final Message msg : msgs) {
        processMessage(inbox, listenersByEmail, eventsMap, msg);
      }
      for (final Map.Entry<MessageListener, MessageEvent> entry : eventsMap.entrySet()) {
        MessageListener mailingList = entry.getKey();
        mailingList.onMessage(entry.getValue());
      }
    } catch (Exception mex) {
      SilverLogger.getLogger(this).error(mex);
    } finally {
      // -- Close down nicely --
      try {
        if (inbox != null) {
          inbox.close(!isLeaveOnServer());
        }
        if (mailAccount != null) {
          mailAccount.close();
        }
      } catch (Exception ex2) {
        SilverLogger.getLogger(this).error(ex2);
      }
    }

  }

  private void processMessage(final Folder inbox,
      final Map<String, MessageListener> listenersByEmail,
      final Map<MessageListener, MessageEvent> eventsMap, final Message msg) {
    try {
      MimeMessage message = (MimeMessage) msg;
      if (isImap()) {
        if (!message.isSet(Flag.SEEN) && !message.isSet(Flag.DELETED)) {
          message = new MimeMessage(message);
          processEmail(message, eventsMap, listenersByEmail);
        }
      } else {
        processEmail(message, eventsMap, listenersByEmail);
      }

      if (isLeaveOnServer() && inbox.getMode() == Folder.READ_WRITE) {
        msg.setFlag(Flag.SEEN, true);
      } else {
        msg.setFlag(Flag.DELETED, true);
      }
    } catch (MessagingException | IOException e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  /**
   * Process an email, building the events to be send when all email have been processed.
   * @param mail the mail to be processed
   * @param eventsMap the map of MessageEvents
   * @param listenersByEmail the map of MessageListners with their emil address as key
   * @throws MessagingException
   * @throws IOException
   */
  protected void processEmail(MimeMessage mail, Map<MessageListener, MessageEvent> eventsMap,
      Map<String, MessageListener> listenersByEmail) throws MessagingException, IOException {
    BetterMimeMessage email = new BetterMimeMessage(mail);
    if (email.isBounced() || email.isSpam()) {
      return;
    }
    Set<String> allRecipients = getAllRecipients(mail);
    Set<MessageListener> mailingLists = getRecipientMailingLists(allRecipients, listenersByEmail);
    for (final MessageListener mailingList : mailingLists) {
      MessageEvent event;
      if (!eventsMap.containsKey(mailingList)) {
        event = new MessageEvent();
        eventsMap.put(mailingList, event);
      } else {
        event = eventsMap.get(mailingList);
      }
      this.processor.prepareMessage(mail, mailingList, event);
    }
  }

  /**
   * Extracts all the recipients of an email.
   * @param mail the email whose recipients are extracted.
   * @return a list of InternetAdress.
   * @throws MessagingException
   * @see javax.mail.internet.InternetAddress
   */
  protected Set<String> getAllRecipients(MimeMessage mail) throws MessagingException {
    Set<String> allRecipients = new HashSet<>(10);
    InternetAddress[] addresses = (InternetAddress[]) mail.getRecipients(RecipientType.TO);
    if (addresses != null) {
      for (final InternetAddress address : addresses) {
        allRecipients.add(address.getAddress());
      }
    }
    addresses = (InternetAddress[]) mail.getRecipients(RecipientType.BCC);
    if (addresses != null) {
      for (final InternetAddress address : addresses) {
        allRecipients.add(address.getAddress());
      }
    }
    addresses = (InternetAddress[]) mail.getRecipients(RecipientType.CC);
    if (addresses != null) {
      for (final InternetAddress address : addresses) {
        allRecipients.add(address.getAddress());
      }
    }
    return allRecipients;
  }

  /**
   * Finds all the mailing lists recipients for an email.
   * @param recipients the recipients of the email.
   * @return the list of mailing lists (as MessageListener) for this email.
   */
  protected Set<MessageListener> getRecipientMailingLists(Collection<String> recipients,
      Map<String, MessageListener> listenersByEmail) {
    Set<MessageListener> mailingLists = new HashSet<>(recipients.size());
    for (final String email : recipients) {
      MessageListener mailingList = listenersByEmail.get(email.toLowerCase());
      if (mailingList != null) {
        mailingLists.add(mailingList);
      }
    }
    return mailingLists;
  }

  /**
   * Prepare a map of subscribed email addresses and their corresponding listeners.
   * @return a map of subscribed email addresses and their corresponding listeners.
   */
  public synchronized Map<String, MessageListener> prepareListeners() {
    Map<String, MessageListener> listenersByEmail = new HashMap<>(this.listeners.size());
    for (final MessageListener listener : this.listeners.values()) {
      MailingList list = mailingListService.findMailingList(listener.getComponentId());
      if (list != null && list.getSubscribedAddress() != null) {
        listenersByEmail.put(list.getSubscribedAddress().toLowerCase(), listener);
      }
    }
    return listenersByEmail;
  }

  /**
   * Removes a listener from the list of listeners.
   * @param componentId the unique id of the component.
   */
  public synchronized void removeListener(String componentId) {
    this.listeners.remove(componentId);
  }

  public MailProcessor getMailProcessor() {
    return processor;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((login == null) ? 0 : login.hashCode());
    result = prime * result + ((mailServer == null) ? 0 : mailServer.hashCode());
    result = prime * result + ((password == null) ? 0 : password.hashCode());
    result = prime * result + ((protocol == null) ? 0 : protocol.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    return isEqualTo((MessageChecker) obj);
  }

  private boolean isEqualTo(final MessageChecker obj) {
    final MessageChecker other = obj;
    if ((mailServer == null && other.mailServer != null) ||
        (mailServer != null && !mailServer.equals(other.mailServer))) {
      return false;
    }
    if ((password == null && other.password != null) ||
        (password != null && !password.equals(other.password))) {
      return false;
    }
    return (protocol == null && other.protocol == null) ||
        (protocol != null && protocol.equals(other.protocol));
  }

  protected boolean isImap() {
    return IMAP_PROTOCOL.equalsIgnoreCase(getProtocol()) ||
        IMAP_SSL_PROTOCOL.equalsIgnoreCase(getProtocol());
  }

  public class MessageCheckingListener implements SchedulerEventListener {

    @Override
    public void triggerFired(SchedulerEvent anEvent) {
      if (listeners != null && !listeners.isEmpty()) {
        checkNewMessages(new Date());
      }
    }

    @Override
    public void jobSucceeded(SchedulerEvent anEvent) {
      // nothing to do
    }

    @Override
    public void jobFailed(SchedulerEvent anEvent) {
      SilverLogger.getLogger(this)
          .error(
              "The job '" + anEvent.getJobExecutionContext().getJobName() + "' was not successfull",
              anEvent.getJobThrowable());
    }
  }
}
