/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.silverpeas.mailinglist.service.notification;

import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.util.Collection;


import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.util.template.SilverpeasTemplateFactory;
import com.stratelia.silverpeas.notificationManager.GroupRecipient;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.webactiv.calendar.control.CalendarRuntimeException;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Utility class to send notifications.
 * @author Emmanuel Hugonnet
 * @version $revision$
 */
public class AdvancedNotificationHelper extends SimpleNotificationHelper {

  private static final Properties templateConfiguration = new Properties();
  public static final String MODERATION_TEMPLATE_FILE = "notificationMailinglistModeration";
  public static final String MESSAGE_TEMPLATE_FILE = "notificationMailinglistMessage";

  static {
    ResourceLocator settings = new ResourceLocator(
            "com.silverpeas.mailinglist.settings.mailinglistSettings", "");
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR, settings.getString(
            "templatePath"));
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR, settings.getString(
            "customersTemplatePath"));
  }

  public SilverpeasTemplate getTemplate(Message message, String mailingListName, boolean moderate) {
    Properties configuration = new Properties(templateConfiguration);
    SilverpeasTemplate template = SilverpeasTemplateFactory.createSilverpeasTemplate(configuration);
    template.setAttribute("title", message.getTitle());
    template.setAttribute("list", mailingListName);
    template.setAttribute("summary", message.getSummary());
    template.setAttribute("sentDate", message.getSentDate());
    template.setAttribute("sender", message.getSender());
    template.setAttribute("fullContent", message.getBody());
    if (moderate) {
      template.setAttribute("messageUrl",
              "/Rmailinglist/" + message.getComponentId() + "/moderationList/"
              + message.getComponentId());
    } else {
      template.setAttribute("messageUrl", "/Rmailinglist/" + message.getComponentId() + "/message/"
              + message.getId());
    }
    return template;
  }

  @Override
  public void notifyInternals(Message message, MailingList list,
          Collection<String> userIds, Collection<String> groupIds, boolean moderate)
          throws NotificationManagerException {
    Map<String, SilverpeasTemplate> templates = new HashMap<String, SilverpeasTemplate>();
    String subject = getNotificationFormatter().formatTitle(message, list.getName(),
            I18NHelper.defaultLanguage, moderate);
    for (String lang : I18NHelper.getAllSupportedLanguages()) {
      templates.put(lang, getTemplate(message, lang, moderate));
    }
    String templateFileName = MESSAGE_TEMPLATE_FILE;
    if (moderate) {
      templateFileName = MODERATION_TEMPLATE_FILE;
    }
    NotificationMetaData metadata = new NotificationMetaData(NotificationParameters.NORMAL, subject,
            templates, templateFileName);
    metadata.setAnswerAllowed(false);
    metadata.setDate(message.getSentDate());
    metadata.setSource(list.getSubscribedAddress());
    metadata.setSender(list.getSubscribedAddress());
    metadata.setComponentId(message.getComponentId());
    metadata.setLink(getNotificationFormatter().prepareUrl(message, moderate));
    for (String userId : userIds) {
      metadata.addUserRecipient(new UserRecipient(userId));
    }
    if (groupIds != null && !groupIds.isEmpty()) {
      for (String groupId : groupIds) {
        metadata.addGroupRecipient(new GroupRecipient(groupId));
      }
    }
    getNotificationSender().notifyUser(metadata);
    try {
      if (moderate) {
        createTask(message, subject, userIds);
      }
    } catch (CalendarRuntimeException e) {
      throw new NotificationManagerException("NotificationHelperImpl",
              SilverpeasException.ERROR, "calendar.MSG_CANT_CHANGE_TODO_ATTENDEES", e);
    } catch (RemoteException e) {
      throw new NotificationManagerException("NotificationHelperImpl",
              SilverpeasException.ERROR, "calendar.MSG_CANT_CHANGE_TODO_ATTENDEES", e);
    } catch (UnsupportedEncodingException e) {
      throw new NotificationManagerException("NotificationHelperImpl",
              SilverpeasException.ERROR, "calendar.MSG_CANT_CHANGE_TODO_ATTENDEES", e);
    }
  }
}
