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

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Flags.Flag;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.silverpeas.mailinglist.service.event.MessageEvent;
import com.silverpeas.mailinglist.service.event.MessageListener;
import com.silverpeas.mailinglist.service.model.MailingListService;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.stratelia.silverpeas.scheduler.SchedulerEvent;
import com.stratelia.silverpeas.scheduler.SchedulerEventHandler;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class MessageChecker implements SchedulerEventHandler {

  public static final String IMAP_PROTOCOL = "imap";
  public static final String IMAP_SSL_PROTOCOL = "imaps";
  public static final String POP3_PROTOCOL = "pop3";
  private Map<String, MessageListener> listeners;
  private MailProcessor processor;
  private String mailServer;
  private String login;
  private String password;
  private String protocol;
  private int port;
  private boolean leaveOnServer;
  private Session mailSession;
  private MailingListService mailingListService;

  public MailingListService getMailingListService() {
    return mailingListService;
  }

  public void setMailingListService(MailingListService mailingListService) {
    this.mailingListService = mailingListService;
  }

  public Session getMailSession() {
    return mailSession;
  }

  public void setMailSession(Session mailSession) {
    this.mailSession = mailSession;
  }

  public MessageChecker() {
    this.listeners = new HashMap<String, MessageListener>(10);
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getMailServer() {
    return mailServer;
  }

  public void setMailServer(String mailServer) {
    this.mailServer = mailServer;
  }

  public boolean isLeaveOnServer() {
    if (POP3_PROTOCOL.equalsIgnoreCase(this.protocol)) {
      this.leaveOnServer = false;
    }
    return leaveOnServer;
  }

  public void setLeaveOnServer(boolean leaveOnServer) {
    this.leaveOnServer = leaveOnServer;
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
    if (POP3_PROTOCOL.equalsIgnoreCase(protocol)
        || IMAP_PROTOCOL.equalsIgnoreCase(protocol)
        || IMAP_SSL_PROTOCOL.equalsIgnoreCase(protocol)) {
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
      Map<MessageListener, MessageEvent> eventsMap = new HashMap<MessageListener, MessageEvent>();
      for (int msgNum = 0; msgNum < msgs.length; msgNum++) {
        try {
          MimeMessage message = (MimeMessage) msgs[msgNum];
          if (isImap()) {
            if (!message.getFlags().contains(Flag.SEEN)
                && !message.getFlags().contains(Flag.DELETED)) {
              message = new MimeMessage(message);
              processEmail(message, eventsMap, listenersByEmail);
            }
          } else {
            processEmail(message, eventsMap, listenersByEmail);
          }

          if (isLeaveOnServer() && inbox.getMode() == Folder.READ_WRITE) {
            msgs[msgNum].setFlag(Flag.SEEN, true);
          } else {
            msgs[msgNum].setFlag(Flag.DELETED, true);
          }
        } catch (MessagingException mex) {
          SilverTrace.error("mailingList", "MessageChecker.checkNewMessages",
              "mail.processing.error", mex);
        } catch (IOException ioex) {
          SilverTrace.error("mailingList", "MessageChecker.checkNewMessages",
              "mail.io.error", ioex);
        }
      }
      Iterator<Map.Entry<MessageListener, MessageEvent>> iter = eventsMap.entrySet().iterator();
      while (iter.hasNext()) {
        Map.Entry<MessageListener, MessageEvent> entry = iter.next();
        MessageListener mailingList = entry.getKey();
        mailingList.onMessage(entry.getValue());
      }
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
          inbox.close(!isLeaveOnServer());
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

  /**
   * Process an email, building the events to be send when all email have been processed.
   * @param mail the mail to be processed
   * @param eventsMap the map of MessageEvents
   * @param listenersByEmail the map of MessageListners with their emil address as key
   * @throws MessagingException
   * @throws IOException
   */
  void processEmail(MimeMessage mail,
      Map<MessageListener, MessageEvent> eventsMap,
      Map<String, MessageListener> listenersByEmail) throws MessagingException,
      IOException {
    BetterMimeMessage email = new BetterMimeMessage(mail);
    if(email.isBounced() || email.isSpam()) {
      return;
    }
    Set<String> allRecipients = getAllRecipients(mail);
    Set<MessageListener> mailingLists = getRecipientMailingLists(allRecipients,
        listenersByEmail);
    Iterator<MessageListener> iter = mailingLists.iterator();
    while (iter.hasNext()) {
      MessageEvent event;
      MessageListener mailingList = iter.next();
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
  Set<String> getAllRecipients(MimeMessage mail) throws MessagingException {
    Set<String> allRecipients = new HashSet<String>(10);
    InternetAddress[] addresses = (InternetAddress[]) mail.getRecipients(RecipientType.TO);
    if (addresses != null) {
      for (int i = 0; i < addresses.length; i++) {
        allRecipients.add(addresses[i].getAddress());
      }
    }
    addresses = (InternetAddress[]) mail.getRecipients(RecipientType.BCC);
    if (addresses != null) {
      for (int i = 0; i < addresses.length; i++) {
        allRecipients.add(addresses[i].getAddress());
      }
    }
    addresses = (InternetAddress[]) mail.getRecipients(RecipientType.CC);
    if (addresses != null) {
      for (int i = 0; i < addresses.length; i++) {
        allRecipients.add(addresses[i].getAddress());
      }
    }
    return allRecipients;
  }

  /**
   * Finds all the mailing lists recipients for an email.
   * @param recipients the recipients of the email.
   * @return the list of mailing lists (as MessageListener) for this email.
   */
  Set<MessageListener> getRecipientMailingLists(Collection<String> recipients,
      Map<String, MessageListener> listenersByEmail) {
    Set<MessageListener> mailingLists = new HashSet<MessageListener>(recipients.size());
    Iterator<String> recipientIter = recipients.iterator();
    while (recipientIter.hasNext()) {
      String email = recipientIter.next();
      MessageListener mailingList = listenersByEmail.get(email);
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
    Map<String, MessageListener> listenersByEmail = new HashMap<String, MessageListener>(
        this.listeners.size());
    Iterator<MessageListener> listenerIter = this.listeners.values().iterator();
    while (listenerIter.hasNext()) {
      MessageListener listener = listenerIter.next();
      MailingList list = mailingListService.findMailingList(listener.getComponentId());
      if (list != null && list.getSubscribedAddress() != null) {
        listenersByEmail.put(list.getSubscribedAddress(), listener);
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

  public void setMailProcessor(MailProcessor processor) {
    this.processor = processor;
  }

  @Override
  public void handleSchedulerEvent(SchedulerEvent event) {
    switch (event.getType()) {
      case SchedulerEvent.EXECUTION_NOT_SUCCESSFULL:
        SilverTrace.error("mailingList", "MessageChecker.handleSchedulerEvent",
            "The job '" + event.getJob().getJobName()
            + "' was not successfull");
        break;
      case SchedulerEvent.EXECUTION_SUCCESSFULL:
        SilverTrace.debug("mailingList", "MessageChecker.handleSchedulerEvent",
            "The job '" + event.getJob().getJobName() + "' was successfull");
        break;
      case SchedulerEvent.EXECUTION:
        if (this.listeners != null && !this.listeners.isEmpty()) {
          SilverTrace.info("mailingList",
              "MessageChecker.handleSchedulerEvent", "The job '"
              + event.getJob().getJobName() + "' executing ....");
          this.checkNewMessages(new Date());
          SilverTrace.info("mailingList",
              "MessageChecker.handleSchedulerEvent", "The job '"
              + event.getJob().getJobName() + "' done!!");
        } else {
          SilverTrace.info("mailingList",
              "MessageChecker.handleSchedulerEvent", "The job '"
              + event.getJob().getJobName() + "' has no listeners ....");
        }
        break;
      default:
        SilverTrace.error("mailingList", "MessageChecker.handleSchedulerEvent",
            "Illegal event type");
        break;
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((login == null) ? 0 : login.hashCode());
    result = prime * result
        + ((mailServer == null) ? 0 : mailServer.hashCode());
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
    final MessageChecker other = (MessageChecker) obj;
    if (login == null) {
      if (other.login != null) {
        return false;
      }
    } else if (!login.equals(other.login)) {
      return false;
    }
    if (mailServer == null) {
      if (other.mailServer != null) {
        return false;
      }
    } else if (!mailServer.equals(other.mailServer)) {
      return false;
    }
    if (password == null) {
      if (other.password != null) {
        return false;
      }
    } else if (!password.equals(other.password)) {
      return false;
    }
    if (protocol == null) {
      if (other.protocol != null) {
        return false;
      }
    } else if (!protocol.equals(other.protocol)) {
      return false;
    }
    return true;
  }

  protected boolean isImap() {
    return IMAP_PROTOCOL.equalsIgnoreCase(getProtocol())
        || IMAP_SSL_PROTOCOL.equalsIgnoreCase(getProtocol());
  }
}
