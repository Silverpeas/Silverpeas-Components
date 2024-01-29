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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.formsonline;

import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.ResourceLocator;

public class FormsOnlineComponentSettings {

  public static final String PARAM_WORKGROUP = "workgroup";

  /**
   * The name of the Almanach component in Silverpeas.
   */
  public static final String COMPONENT_NAME = "formsOnline";
  /**
   * The relative path of the properties file containing the settings of the Almanach
   * component.
   */
  public static final String SETTINGS_PATH =
      "org.silverpeas.formsonline.settings.formsOnlineSettings";
  /**
   * The relative path of the i18n bundle of the FormsOnline component.
   */
  public static final String MESSAGES_PATH =
      "org.silverpeas.formsonline.multilang.formsOnlineBundle";

  private FormsOnlineComponentSettings() {

  }

  /**
   * Gets all the messages for the FormsOnline component and translated in the specified language.
   * @param language the language in which are written the messages.
   * @return the resource with the translated messages.
   */
  public static LocalizationBundle getMessagesIn(String language) {
    return ResourceLocator.getLocalizationBundle(MESSAGES_PATH, language);
  }
}