/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

import com.silverpeas.mailinglist.service.model.beans.Message;
import org.silverpeas.util.StringUtil;
import org.silverpeas.util.i18n.I18NHelper;
import org.silverpeas.util.template.SilverpeasTemplate;
import org.silverpeas.util.template.SilverpeasTemplateFactory;
import com.stratelia.webactiv.util.ResourceLocator;
import java.util.Properties;

/**
 *
 * @author ehugonnet
 */
public class AdvancedNotificationFormatter extends AbstractNotificationFormatter {

  private static final Properties templateConfiguration = new Properties();
  public static final String TITLE_KEY = "mailinglist.notification.template.title";
  public static final String TITLE_MODERATION_KEY = "mailinglist.notification.template.moderation.title";


  public static final String MODERATION_TEMPLATE_FILE = "mailinglistModerationMessage";
  public static final String SIMPLE_TEMPLATE_FILE = "mailinglistSimpleMessage";

  static {
    ResourceLocator settings = new ResourceLocator(
        "com.silverpeas.mailinglist.settings.mailinglistSettings", "");
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR, settings.getString("templatePath"));
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR, settings.getString("customersTemplatePath"));
  }

  public SilverpeasTemplate getTemplate(Message message, String mailingListName, boolean moderate) {
    Properties configuration = new Properties(templateConfiguration);
    SilverpeasTemplate template = SilverpeasTemplateFactory.createSilverpeasTemplate(configuration);
    template.setAttribute("title", message.getTitle());
    template.setAttribute("mailingListName", mailingListName);
    template.setAttribute("summary", message.getSummary());
    template.setAttribute("sentDate", message.getSentDate());
    template.setAttribute("sender", message.getSender());
    template.setAttribute("fullContent", message.getBody());
    template.setAttribute("messageUrl", prepareUrl(message, moderate));
    return template;
  }

  @Override
  public String formatTitle(Message message, String mailingListName, String lang, boolean moderate) {
    SilverpeasTemplate template = getTemplate(message, mailingListName, moderate);
    ResourceLocator resources = new ResourceLocator("com.silverpeas.mailinglist.multilang.mailinglistBundle", lang);
    if (moderate) {
      return template.applyStringTemplate(resources.getString(TITLE_MODERATION_KEY));
    }
    return template.applyStringTemplate(resources.getString(TITLE_KEY));
  }

  @Override
  public String formatMessage(Message message, String lang, boolean moderate) {
    SilverpeasTemplate template = getTemplate(message, "", moderate);
    String currentLanguage = I18NHelper.defaultLanguage;
    if(StringUtil.isDefined(lang)) {
      currentLanguage = lang;
    }
    String templateFileName = SIMPLE_TEMPLATE_FILE + '_' + currentLanguage;
    if(moderate) {
      templateFileName = MODERATION_TEMPLATE_FILE + '_' + currentLanguage;
    }
    return template.applyFileTemplate(templateFileName);
  }
}
