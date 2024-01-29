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
package org.silverpeas.components.community;

import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * It gathers all the settings and i18n relative to the community component.
 */
public final class CommunityComponentSettings {

  private CommunityComponentSettings() {
  }

  /**
   * The name of the application (id est component or peas) in Silverpeas.
   */
  public static final String COMPONENT_NAME = "community";

  /**
   * The relative path of the properties file containing the settings of the Silverpeas
   * component.
   */
  public static final String SETTINGS_PATH =
    "org.silverpeas.components.community.settings.communitySettings";

  /**
   * The relative path of the i18n bundle of the Silverpeas component.
   */
  public static final String MESSAGES_PATH =
    "org.silverpeas.components.community.multilang.communityBundle";

  /**
   * Gets all the messages for the Silverpeas component and translated in the specified language.
   * @param language the language in which are written the messages.
   * @return the resource with the translated messages.
   */
  public static LocalizationBundle getMessagesIn(String language) {
    return ResourceLocator.getLocalizationBundle(MESSAGES_PATH, language);
  }

  /**
   * Gets all the settings of the Silverpeas component.
   * @return the resource with the different component settings.
   */
  public static SettingBundle getSettings() {
    return ResourceLocator.getSettingBundle(SETTINGS_PATH);
  }

  /**
   * Gets the list of reasons a member can choose to explain its leaving.
   * @param language the language in which are written the messages.
   * @return a list of stings.
   */
  public static List<String> getLeaveReasons(final String language) {
    return Stream
        .of(getMessagesIn(language).getString("community.membership.leaving.reasons").split(";"))
        .map(String::trim)
        .collect(Collectors.toList());
  }
}