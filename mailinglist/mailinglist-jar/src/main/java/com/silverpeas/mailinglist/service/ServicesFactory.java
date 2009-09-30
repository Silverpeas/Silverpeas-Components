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
    return (NotificationHelper) getInstance().context
        .getBean(NOTIFICATION_HELPER);
  }

  public static NotificationFormatter getNotificationFormatter() {
    return (NotificationFormatter) getInstance().context
        .getBean(NOTIFICATION_FORMATTER);
  }

}
