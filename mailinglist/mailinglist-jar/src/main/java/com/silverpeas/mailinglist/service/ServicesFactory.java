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

package com.silverpeas.mailinglist.service;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.silverpeas.mailinglist.service.job.MessageChecker;
import com.silverpeas.mailinglist.service.model.MailingListService;
import com.silverpeas.mailinglist.service.model.MessageService;
import com.silverpeas.mailinglist.service.notification.NotificationFormatter;
import com.silverpeas.mailinglist.service.notification.NotificationHelper;

public class ServicesFactory implements ApplicationContextAware {
  public static final String MAILING_LIST_SERVICE = "mailingListService";
  public static final String MESSAGE_SERVICE = "messageService";
  public static final String MESSAGE_CHECKER = "messageChecker";
  public static final String NOTIFICATION_HELPER = "notificationHelper";
  public static final String NOTIFICATION_FORMATTER = "notificationFormatter";

  private ApplicationContext context;
  private static ServicesFactory instance;

  private ServicesFactory() {
  }

  public void setApplicationContext(ApplicationContext context)
      throws BeansException {
    this.context = context;
  }

  protected static ServicesFactory getInstance() {
    synchronized (ServicesFactory.class) {
      if (ServicesFactory.instance == null) {
        ServicesFactory.instance = new ServicesFactory();
      }
    }
    return ServicesFactory.instance;
  }

  public static MailingListService getMailingListService() {
    return (MailingListService) getInstance().context
        .getBean(MAILING_LIST_SERVICE);
  }

  public static MessageService getMessageService() {
    return (MessageService) getInstance().context.getBean(MESSAGE_SERVICE);
  }

  public static MessageChecker getMessageChecker() {
    return (MessageChecker) getInstance().context.getBean(MESSAGE_CHECKER);
  }

  public static NotificationHelper getNotificationHelper() {
    return (NotificationHelper) getInstance().context.getBean(NOTIFICATION_HELPER);
  }

  public static NotificationFormatter getNotificationFormatter() {
    return (NotificationFormatter) getInstance().context.getBean(NOTIFICATION_FORMATTER);
  }

}
