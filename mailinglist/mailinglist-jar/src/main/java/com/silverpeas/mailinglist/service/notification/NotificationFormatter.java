package com.silverpeas.mailinglist.service.notification;

import java.text.MessageFormat;

import com.silverpeas.mailinglist.service.model.beans.Message;
import com.stratelia.webactiv.util.ResourceLocator;

public class NotificationFormatter {
  public static final String TITLE_KEY = "mailinglist.notification.title";

  public static final String TITLE_MODERATION_KEY = "mailinglist.notification.moderation.title";

  public static final String BODY_KEY = "mailinglist.notification.body";

  public static final String BODY_MODERATION_KEY = "mailinglist.notification.moderation.body";

  private MessageFormat titleFormatter;

  private MessageFormat titleModerationFormatter;

  private MessageFormat bodyFormatter;

  private MessageFormat bodyModerationFormatter;

  public NotificationFormatter(String lang) {
    ResourceLocator resources = new ResourceLocator(
        "com.silverpeas.mailinglist.multilang.mailingListBundle", lang);
    titleFormatter = new MessageFormat(resources.getString(TITLE_KEY));
    titleModerationFormatter = new MessageFormat(resources
        .getString(TITLE_MODERATION_KEY));
    bodyFormatter = new MessageFormat(resources.getString(BODY_KEY));
    bodyModerationFormatter = new MessageFormat(resources
        .getString(BODY_MODERATION_KEY));
  }

  public String formatTitle(Message message, String mailingListName,
      boolean moderate) {
    if (moderate) {
      return titleModerationFormatter.format(new String[] { mailingListName,
          message.getTitle() });
    }
    return titleFormatter.format(new String[] { mailingListName,
        message.getTitle() });
  }

  public String formatMessage(Message message, String baseUrl, boolean moderate) {
    if (moderate) {
      return bodyModerationFormatter.format(new String[] {
          message.getSummary(), prepareUrl(baseUrl, message, moderate),
          message.getTitle() });
    }
    return bodyFormatter.format(new String[] { message.getSummary(),
        prepareUrl(baseUrl, message, moderate), message.getTitle() });
  }

  public String prepareUrl(String baseUrl, Message message, boolean moderate) {
    if (moderate) {
      return "/Rmailinglist/" + message.getComponentId() + "/moderationList/"
          + message.getComponentId();
    }
    return "/Rmailinglist/" + message.getComponentId() + "/message/"
        + message.getId();
  }
}
