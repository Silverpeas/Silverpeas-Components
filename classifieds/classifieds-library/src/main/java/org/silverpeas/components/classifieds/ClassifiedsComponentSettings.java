/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

package org.silverpeas.components.classifieds;

import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

import java.io.Serializable;

public class ClassifiedsComponentSettings implements Serializable {

  private boolean commentsEnabled = false;
  private boolean priceAllowed = true;
  private boolean photosAllowed = true;

  public static final String PARAM_COMMENTS = "comments";
  public static final String PARAM_PRICE = "usePrice";
  public static final String PARAM_PHOTOS = "usePhotos";

  /**
   * The name of the Classified component in Silverpeas.
   */
  public static final String COMPONENT_NAME = "classifieds";

  /**
   * The relative path of the properties file containing the settings of the component.
   */
  public static final String SETTINGS_PATH
      = "org.silverpeas.classifieds.settings.classifiedsSettings";

  /**
   * The relative path of the i18n bundle of the component.
   */
  public static final String MESSAGES_PATH
      = "org.silverpeas.classifieds.multilang.classifiedsBundle";

  /**
   * The relative path of the properties file containing the references of the icons dedicated to
   * the component.
   */
  public static final String ICONS_PATH
      = "org.silverpeas.classifieds.settings.classifiedsIcons";

  public boolean isCommentsEnabled() {
    return commentsEnabled;
  }

  public void setCommentsEnabled(boolean commentsEnabled) {
    this.commentsEnabled = commentsEnabled;
  }

  /**
   * Gets all the messages for the Suggestion Box component and translated in the specified
   * language.
   * @param language the language in which are written the messages.
   * @return the resource with the translated messages.
   */
  public static LocalizationBundle getMessagesIn(String language) {
    return ResourceLocator.getLocalizationBundle(MESSAGES_PATH, language);
  }

  /**
   * Gets all the settings of the Suggestion Box component.
   * @return the resource with the different component settings.
   */
  public static SettingBundle getSettings() {
    return ResourceLocator.getSettingBundle(SETTINGS_PATH);
  }

  /**
   * Gets all the icons definitions particular to the Suggestion Box component.
   * @return the resource with icons definition.
   */
  public static LocalizationBundle getIcons() {
    return ResourceLocator.getLocalizationBundle(ICONS_PATH);
  }

  public boolean isPriceAllowed() {
    return priceAllowed;
  }

  public void setPriceAllowed(final boolean priceAllowed) {
    this.priceAllowed = priceAllowed;
  }

  public boolean isPhotosAllowed() {
    return photosAllowed;
  }

  public void setPhotosAllowed(final boolean photosAllowed) {
    this.photosAllowed = photosAllowed;
  }
}