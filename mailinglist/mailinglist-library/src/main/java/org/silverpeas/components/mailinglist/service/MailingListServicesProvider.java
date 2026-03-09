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
package org.silverpeas.components.mailinglist.service;

import jakarta.inject.Inject;
import org.silverpeas.components.mailinglist.service.job.MessageChecker;
import org.silverpeas.components.mailinglist.service.model.MailingListService;
import org.silverpeas.components.mailinglist.service.model.MessageService;
import org.silverpeas.components.mailinglist.service.notification.NotificationFormatter;
import org.silverpeas.components.mailinglist.service.notification.NotificationHelper;
import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.util.ServiceProvider;

@Provider
public class MailingListServicesProvider {

  @Inject
  private MailingListService mailingListService;
  @Inject
  private MessageService messageService;
  @Inject
  private MessageChecker messageChecker;
  //@Inject
  private NotificationHelper notificationHelper;
  @Inject
  private NotificationFormatter notificationFormatter;

  public static MailingListServicesProvider get() {
    return ServiceProvider.getService(MailingListServicesProvider.class);
  }

  private MailingListServicesProvider() {
  }

  public MailingListService getMailingListService() {
    return mailingListService;
  }

  public MessageService getMessageService() {
    return messageService;
  }

  public MessageChecker getMessageChecker() {
    return messageChecker;
  }

  public NotificationHelper getNotificationHelper() {
    return notificationHelper;
  }

  public NotificationFormatter getNotificationFormatter() {
    return notificationFormatter;
  }
}
