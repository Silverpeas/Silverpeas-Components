/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.quickinfo;

import org.silverpeas.util.ResourceLocator;

public class QuickInfoComponentSettings {
  
  private String description;
  private boolean commentsEnabled = false;
  private boolean taxonomyEnabled = false;
  private boolean notificationAllowed = false;
  private boolean broadcastTicker = false;
  private boolean broadcastBlocking = false;
  private boolean delegatedNewsEnabled = false;
  
  public static final String PARAM_COMMENTS = "comments";
  public static final String PARAM_TAXONOMY = "usePdc";
  public static final String PARAM_DELEGATED = "delegatedNews";
  
  public static final String PARAM_BROADCAST = "broadcasting";
  public static final String VALUE_BROADCAST_TICKER = "ticker";
  public static final String VALUE_BROADCAST_BLOCKING = "blocking";
  public static final String VALUE_BROADCAST_BOTH = "both";
  
  /**
   * The name of the Quickinfo component in Silverpeas.
   */
  public static final String COMPONENT_NAME = "quickinfo";

  /**
   * The relative path of the properties file containing the settings of the component.
   */
  public static final String SETTINGS_PATH
      = "org.silverpeas.quickinfo.settings.quickInfoSettings";

  /**
   * The relative path of the i18n bundle of the component.
   */
  public static final String MESSAGES_PATH
      = "org.silverpeas.quickinfo.multilang.quickinfo";

  /**
   * The relative path of the properties file containing the references of the icons dedicated to
   * the component.
   */
  public static final String ICONS_PATH
      = "org.silverpeas.quickinfo.settings.quickinfoIcons";
  
  public QuickInfoComponentSettings(String desc) {
    this.description = desc;
  }
  
  public boolean isCommentsEnabled() {
    return commentsEnabled;
  }

  public void setCommentsEnabled(boolean commentsEnabled) {
    this.commentsEnabled = commentsEnabled;
  }

  public String getDescription() {
    return description;
  }

  /**
   * Gets all the messages for the Suggestion Box component and translated in the specified
   * language.
   * @param language the language in which are written the messages.
   * @return the resource with the translated messages.
   */
  public static ResourceLocator getMessagesIn(String language) {
    return new ResourceLocator(MESSAGES_PATH, language);
  }

  /**
   * Gets all the settings of the Suggestion Box component.
   * @return the resource with the different component settings.
   */
  public static ResourceLocator getSettings() {
    return new ResourceLocator(SETTINGS_PATH, "");
  }

  /**
   * Gets all the icons definitions particular to the Suggestion Box component.
   * @return the resource with icons definition.
   */
  public static ResourceLocator getIcons() {
    return new ResourceLocator(ICONS_PATH, "");
  }

  public void setTaxonomyEnabled(boolean taxonomyEnabled) {
    this.taxonomyEnabled = taxonomyEnabled;
  }

  public boolean isTaxonomyEnabled() {
    return taxonomyEnabled;
  }

  public void setNotificationAllowed(boolean notificationAllowed) {
    this.notificationAllowed = notificationAllowed;
  }

  public boolean isNotificationAllowed() {
    return notificationAllowed;
  }
  
  public void setBroadcastModes(String paramValue) {
    this.broadcastTicker =
        VALUE_BROADCAST_BOTH.equals(paramValue) || VALUE_BROADCAST_TICKER.equals(paramValue);
    this.broadcastBlocking =
        VALUE_BROADCAST_BOTH.equals(paramValue) || VALUE_BROADCAST_BLOCKING.equals(paramValue);
  }

  public boolean isBroadcastingByTicker() {
    return broadcastTicker;
  }

  public boolean isBroadcastingByBlockingNews() {
    return broadcastBlocking;
  }

  public void setDelegatedNewsEnabled(boolean delegatedNewsEnabled) {
    this.delegatedNewsEnabled = delegatedNewsEnabled;
  }

  public boolean isDelegatedNewsEnabled() {
    return delegatedNewsEnabled;
  }

}