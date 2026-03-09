/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.mailinglist.service.notification;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.inject.Inject;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import org.silverpeas.components.mailinglist.service.model.beans.*;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.mail.MailSending;
import org.silverpeas.core.mail.ReceiverMailAddressSet;
import org.silverpeas.core.notification.NotificationException;
import org.silverpeas.core.notification.user.client.GroupRecipient;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.personalorganizer.model.ToDoHeader;
import org.silverpeas.core.personalorganizer.service.CalendarRuntimeException;
import org.silverpeas.core.personalorganizer.service.SilverpeasCalendar;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.Charsets;

import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.silverpeas.core.mail.MailAddress.eMail;
import static org.silverpeas.core.mail.MailContent.of;

/**
 * Utility class to send notifications.
 * @author Emmanuel Hugonnet
 */
public class SimpleNotificationHelper implements NotificationHelper {

  public static final int BATCH_SIZE = 10;

  @Inject
  private NotificationFormatter notificationFormatter;
  @Inject
  private SilverpeasCalendar calendar;
  @Inject
  private OrganizationController controller;

  public void notifyModerators(Message message, MailingList list)
      throws NotificationException {
    Set<String> userIds = getModeratorsIds(list);
    notifyInternals(message, list, userIds, null, true);
  }

  public void notifyUsers(Message message, MailingList list) throws NotificationException {
    Set<String> userIds = getUsersIds(list);
    Set<String> groupIds = getGroupIds(list);
    notifyInternals(message, list, userIds, groupIds, false);
  }

  public void notifyUsersAndModerators(Message message, MailingList list)
      throws NotificationException {
    Set<String> userIds = getUsersIds(list);
    Set<String> moderatorIds = getModeratorsIds(list);
    userIds.addAll(moderatorIds);
    Collection<String> groupIds = getGroupIds(list);
    notifyInternals(message, list, userIds, groupIds, false);
  }

  public void notifyInternals(Message message, MailingList list, Collection<String> userIds,
      Collection<String> groupIds, boolean moderate) throws NotificationException {
    String defaultTitle = notificationFormatter
        .formatTitle(message, list.getName(), DisplayI18NHelper.getDefaultLanguage(), moderate);
    NotificationMetaData metadata = new NotificationMetaData();
    metadata.setAnswerAllowed(false);
    metadata.setDate(message.getSentDate());
    metadata.setSource(list.getSubscribedAddress());
    metadata.setSender(list.getSubscribedAddress());
    metadata.setComponentId(message.getComponentId());
    String link = notificationFormatter.prepareUrl(message, moderate);
    for (String lang : DisplayI18NHelper.getLanguages()) {
      String title = notificationFormatter.formatTitle(message, list.getName(), lang, moderate);
      String content = notificationFormatter.formatMessage(message, lang, moderate);
      metadata.addLanguage(lang, title, content);
    }
    metadata.setTitle(defaultTitle);
    if (message.getSummary() != null) {
      metadata.setContent(message.getSummary());
    } else {
      metadata.setContent("");
    }
    metadata.setLink(link);
    sendNotification(message, list, userIds, groupIds, moderate, metadata, defaultTitle);
  }

  protected void sendNotification(Message message, MailingList list, Collection<String> userIds,
      Collection<String> groupIds, boolean moderate, NotificationMetaData metadata, String defaultTitle) throws NotificationException {
    for (String userId : userIds) {
      metadata.addUserRecipient(new UserRecipient(userId));
    }
    if (groupIds != null && !groupIds.isEmpty()) {
      for (String groupId : groupIds) {
        metadata.addGroupRecipient(new GroupRecipient(groupId));
      }
    }
    NotificationSender notificationSender = new NotificationSender(list.getComponentId());
    notificationSender.notifyUser(metadata);
    try {
      if (moderate) {
        createTask(message, defaultTitle, userIds);
      }
    } catch (CalendarRuntimeException e) {
      throw new NotificationException(e);
    }
  }

  public void notifyExternals(Message message, MailingList list) throws MessagingException {
    MailSending mail = MailSending.from(eMail(list.getSubscribedAddress()));
    String subject = notificationFormatter
        .formatTitle(message, list.getName(), DisplayI18NHelper.getDefaultLanguage(), false);
    mail.withSubject(subject);

    if (!message.getAttachments().isEmpty()) {
      Multipart multiPart = new MimeMultipart();
      for (Attachment attachment : message.getAttachments()) {
        MimeBodyPart part = new MimeBodyPart();
        DataSource source = new FileDataSource(attachment.getPath());
        part.setDataHandler(new DataHandler(source));
        part.setFileName(attachment.getFileName());
        multiPart.addBodyPart(part);
      }
      MimeBodyPart body = new MimeBodyPart();
      body.setContent(message.getBody(), message.getContentType());
      multiPart.addBodyPart(body);
      mail.withContent(multiPart);
    } else {
      mail.withContent(of(message.getBody()).withContentType(message.getContentType()));
    }
    sendMail(mail, list.getExternalSubscribers());
  }

  protected void sendMail(MailSending mail, Collection<ExternalUser> externalUsers) {
    ReceiverMailAddressSet receivers =
        ReceiverMailAddressSet.ofRecipientType(ReceiverMailAddressSet.MailRecipientType.BCC)
            .withReceiversBatchSizeOf(BATCH_SIZE);
    for (ExternalUser externalUser : externalUsers) {
      receivers.add(eMail(externalUser.getEmail()));
    }
    mail.to(receivers).send();
  }

  public NotificationFormatter getNotificationFormatter() {
    return notificationFormatter;
  }

  public Set<String> getModeratorsIds(MailingList list) {
    int size = list.getModerators().size();
    Set<String> result = new HashSet<>(size);
    for (InternalUser user : list.getModerators()) {
      result.add(user.getId());
    }
    return result;
  }

  public Set<String> getUsersIds(MailingList list) {
    int size = list.getInternalSubscribers().size();
    Set<String> result = new HashSet<>(size);
    for (InternalUserSubscriber subscriber : list.getInternalSubscribers()) {
      result.add(subscriber.getExternalId());
    }
    return result;
  }

  public Set<String> getGroupIds(MailingList list) {
    int size = list.getGroupSubscribers().size();
    Set<String> result = new HashSet<>(size);
    for (final InternalGroupSubscriber internalGroupSubscriber : list.getGroupSubscribers()) {
      result.add((internalGroupSubscriber).getExternalId());
    }
    return result;
  }

  public SilverpeasCalendar getCalendar() {
    return calendar;
  }

  protected void createTask(Message message, String title, Collection<String> userIds)
      throws CalendarRuntimeException {
    ToDoHeader todo = new ToDoHeader();
    todo.setDelegatorId(message.getComponentId());
    todo.setComponentId(message.getComponentId());
    todo.setName(title);
    todo.setStartDate(message.getSentDate());
    todo.setDescription(message.getId());
    String todoId = getCalendar().addToDo(todo);
    todo.setId(todoId);
    todo.setExternalId(
        "message/" + message.getId() + "?todoId=" + URLEncoder.encode(todoId, Charsets.UTF_8));
    getCalendar().updateToDo(todo);
    getCalendar().setToDoAttendees(todoId, userIds.toArray(new String[0]));
  }

  @Override
  public void notify(Message message, MailingList list)
      throws MessagingException, NotificationException {
    if (list.isNotify() || list.isModerated()) {
      if (message.isModerated()) {
        if (list.isModerated() && !list.isNotify()) {
          notifyUsers(message, list);
        } else {
          notifyUsersAndModerators(message, list);
          notifyExternals(message, list);
        }
      } else {
        notifyModerators(message, list);
      }
    }
  }

}
