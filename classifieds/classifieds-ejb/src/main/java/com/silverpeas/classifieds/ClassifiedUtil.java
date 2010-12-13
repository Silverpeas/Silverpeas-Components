/*
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
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.classifieds;

import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.util.template.SilverpeasTemplateFactory;
import com.stratelia.webactiv.util.ResourceLocator;
import java.util.Properties;

/**
 * Utilitary class providing useful operations for all of the objects behaving within the classified
 * context.
 */
public final class ClassifiedUtil {

  private static final ResourceLocator settings = new ResourceLocator(
      "com.silverpeas.classifieds.settings.classifiedsSettings", "");
  private static final ResourceLocator messages = new ResourceLocator(
      "com.silverpeas.classifieds.multilang.classifiedsBundle", "");

  /**
   * Gets the URL of the specified classified in Silverpeas.
   * @param classified the classified.
   * @return the URL of the classified.
   */
  public static String getClassifiedUrl(final ClassifiedDetail classified) {
    return "/Rclassifieds/" + classified.getInstanceId() + "/searchResult?Type=Classified&Id=" + classified.
        getClassifiedId();
  }

  /**
   * Creates a new template for the specified classified.
   * @param classified the classified to use in templating.
   * @return a SilverpeasTemplate instance.
   */
  public static SilverpeasTemplate newTemplate(final ClassifiedDetail classified) {
    Properties templateConfiguration = new Properties();
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR,
        getSettingsResource().getString("templatePath"));
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR,
        getSettingsResource().getString("customersTemplatePath"));

    SilverpeasTemplate template =
        SilverpeasTemplateFactory.createSilverpeasTemplate(templateConfiguration);
    template.setAttribute("classified", classified);
    template.setAttribute("classifiedName", classified.getTitle());
    template.setAttribute("silverpeasURL", getClassifiedUrl(classified));
    return template;
  }

  /**
   * Gets the message associated with the specified key.
   * @param messageKey the key of the message.
   * @return the message in the default language (platform language).
   */
  public static String getMessage(final String messageKey) {
    messages.setLanguage("");
    return messages.getString(messageKey);
  }

  /**
   * Gets the message associated with the specified key and in the specified language.
   * @param messageKey the key of the message.
   * @param language the language in which the message is written.
   * @return the message in the specified language.
   */
  public static String getMessage(final String messageKey, final String language) {
    messages.setLanguage(language);
    return messages.getString(messageKey);
  }

  /**
   * Gets the message associated with the specified key and in the specified language. If no such
   * key exists, then specified default message is taken.
   * @param messageKey the key of the message.
   * @param defaultMessage the default message to return if the the specified message key doesn't
   * exist.
   * @param language the language in which the message is written.
   * @return the message in the specified language, otherwise the specified default message.
   */
  public static String getMessage(final String messageKey, final String defaultMessage,
      final String language) {
    messages.setLanguage(language);
    return messages.getString(messageKey, defaultMessage);
  }

  /**
   * Gets the resource locator in which are defined all localized messages for the classifieds
   * module.
   * @return the locator of the resource with localized message.
   */
  protected static ResourceLocator getMessagesResource() {
    return messages;
  }

  /**
   * Gets the resource in which are defined all the setting parameters of the classifieds module.
   * @return the locator of the resource with setting parameters.
   */
  protected static ResourceLocator getSettingsResource() {
    return settings;
  }
}
