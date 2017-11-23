/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.almanach;


import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

import java.time.ZoneId;
import java.util.Arrays;

import static org.silverpeas.core.util.StringUtil.*;


/**
 * It gathers all the settings and i18n relative to the Almanach component.
 * @author Yohann Chastagnier
 */
public final class AlmanachSettings {

  /**
   * The name of the Almanach component in Silverpeas.
   */
  public static final String COMPONENT_NAME = "almanach";
  /**
   * The relative path of the properties file containing the settings of the Almanach
   * component.
   */
  public static final String SETTINGS_PATH =
      "org.silverpeas.almanach.settings.almanachSettings";
  /**
   * The relative path of the i18n bundle of the Almanach component.
   */
  public static final String MESSAGES_PATH =
      "org.silverpeas.almanach.multilang.almanach";
  /**
   * The relative path of the properties file containing the references of the icons dedicated to
   * the Almanach component.
   */
  public static final String ICONS_PATH = "org.silverpeas.almanach.settings.almanachIcons";

  public static final String ALMANACH_IN_SUBSPACES = "0";
  public static final String ALMANACH_IN_SPACE_AND_SUBSPACES = "1";
  public static final String ALL_ALMANACHS = "2";

  /**
   * Hidden constructor.
   */
  private AlmanachSettings() {
  }

  /**
   * Gets all the messages for the Almanach component and translated in the specified
   * language.
   * @param language the language in which are written the messages.
   * @return the resource with the translated messages.
   */
  public static LocalizationBundle getMessagesIn(String language) {
    return ResourceLocator.getLocalizationBundle(MESSAGES_PATH, language);
  }

  /**
   * Gets all the settings of the Almanach component.
   * @return the resource with the different component settings.
   */
  public static SettingBundle getSettings() {
    return ResourceLocator.getSettingBundle(SETTINGS_PATH);
  }

  /**
   * Gets all the icons definitions particular to the Almanach component.
   * @return the resource with icons definition.
   */
  public static LocalizationBundle getIcons() {
    return ResourceLocator.getLocalizationBundle(ICONS_PATH);
  }

  /**
   * Gets the zone identifier of the platform for almanach components.
   * @return a {@link ZoneId} instance.
   */
  public static ZoneId getZoneId() {
    return ZoneId.of(getSettings().getString("almanach.timezone"));
  }

  /**
   * Indicates if the weekend is visible on calendar for a component instance.
   * @param componentInstanceId the identifier of component instance for parameter values.
   * @return true if weekend is visible, false otherwise.
   */
  public static boolean isCalendarWeekendVisible(String componentInstanceId) {
    return !getBooleanValue(OrganizationController.get()
        .getComponentParameterValue(componentInstanceId, "weekendNotVisible"));
  }

  /**
   * Gets the default view of the almanach calendar.
   * @param componentInstanceId the identifier of component instance for parameter values.
   * @return the default view as string.
   */
  public static String getDefaultCalendarView(String componentInstanceId) {
    final String defaultView =
        OrganizationController.get().getComponentParameterValue(componentInstanceId, "defaultView");
    return defaultStringIfNotDefined(defaultView, "MONTHLY");
  }

  /**
   * Indicates if the PDC is used into the context of the component instance represented bu the
   * given identifier.
   * @param componentInstanceId the identifier of component instance for parameter values.
   * @return true if pdc is used, false otherwise.
   */
  public static boolean isPdcUsed(String componentInstanceId) {
    return getBooleanValue(
        OrganizationController.get().getComponentParameterValue(componentInstanceId, "usePdc"));
  }

  /**
   * Indicates if the PDC filtering is activated into the context of the component instance
   * represented bu the given identifier.
   * @param componentInstanceId the identifier of component instance for parameter values.
   * @return true if pdc filtering is activated, false otherwise.
   */
  public static boolean isFilterOnPdcActivated(String componentInstanceId) {
    return getBooleanValue(OrganizationController.get()
        .getComponentParameterValue(componentInstanceId, "filterOnPdc"));
  }

  /**
   * Gets the limit number of occurrences the next event view has to display.
   * @return the limit as int.
   */
  public static int getNbOccurrenceLimitOfNextEventView() {
    int limit = getSettings().getInteger("almanach.nextEvents.limit", -1);
    if (limit == -1) {
      limit = getCommonSetting().getInteger("calendar.nextEvents.limit");
    }
    return limit;
  }

  /**
   * Gets the limit number of occurrences the short next event view has to display.
   * @return the limit as int.
   */
  public static int getNbOccurrenceLimitOfShortNextEventView() {
    int limit = getSettings().getInteger("almanach.nextEvents.short.limit", -1);
    if (limit == -1) {
      limit = getCommonSetting().getInteger("calendar.nextEvents.short.limit");
    }
    return limit;
  }

  /**
   * Gets the limit number of occurrences the next event view has to display.
   * @return the limit as int.
   */
  public static Integer[] getNextEventTimeWindows() {
    String timeWindows = getSettings().getString("almanach.nextEvents.time.windows", "");
    if (isNotDefined(timeWindows)) {
      timeWindows = getCommonSetting().getString("calendar.nextEvents.time.windows");
    }
    return Arrays.stream(timeWindows.split(",")).map(w -> Integer.parseInt(w.trim())).toArray(Integer[]::new);
  }

  /**
   * Gets the aggregation mode for almanachs.
   * @return the code of the mode as string.
   */
  public static String getAggregationMode() {
    return getSettings().getString("almanachAgregationMode", ALMANACH_IN_SUBSPACES);
  }

  private static SettingBundle getCommonSetting() {
    return ResourceLocator.getSettingBundle("org.silverpeas.calendar.settings.calendar");
  }
}
