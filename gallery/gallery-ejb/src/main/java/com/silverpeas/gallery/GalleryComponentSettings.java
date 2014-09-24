/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package com.silverpeas.gallery;

import com.silverpeas.gallery.constant.MediaResolution;
import org.silverpeas.util.StringUtil;
import com.stratelia.webactiv.util.ResourceLocator;
import org.silverpeas.core.admin.OrganisationControllerFactory;

/**
 * It gathers all the settings and i18n relative to the Gallery component.
 * @author mmoquillon
 */
public final class GalleryComponentSettings {

  private GalleryComponentSettings() {
  }

  /**
   * The name of the Gallery component in Silverpeas.
   */
  public static final String COMPONENT_NAME = "gallery";

  /**
   * The relative path of the properties file containing the settings of the Gallery component.
   */
  public static final String SETTINGS_PATH = "com.silverpeas.gallery.settings.gallerySettings";

  /**
   * The relative path of the i18n bundle of the Gallery component.
   */
  public static final String MESSAGES_PATH = "com.silverpeas.gallery.multilang.galleryBundle";

  /**
   * The relative path of the properties file containing the references of the icons dedicated to
   * the Gallery component.
   */
  public static final String ICONS_PATH = "com.silverpeas.gallery.settings.galleryIcons";

  /**
   * Gets all the messages for the Gallery component and translated in the specified language.
   * @param language the language in which are written the messages.
   * @return the resource with the translated messages.
   */
  public static ResourceLocator getMessagesIn(String language) {
    return new ResourceLocator(MESSAGES_PATH, language);
  }

  /**
   * Gets all the settings of the Gallery component.
   * @return the resource with the different component settings.
   */
  public static ResourceLocator getSettings() {
    return new ResourceLocator(SETTINGS_PATH, "");
  }

  /**
   * Gets all the icons definitions particular to the Gallery component.
   * @return the resource with icons definition.
   */
  public static ResourceLocator getIcons() {
    return new ResourceLocator(ICONS_PATH, "");
  }

  /**
   * Gets the max number of media displayed on homepage.
   * @return
   */
  public static int getNbMediaDisplayedPerPage() {
    return getNbMediaDisplayedPerPageByResolution(null);
  }

  /**
   * Gets the max number of media displayed on homepage.
   * @return
   */
  public static int getNbMediaDisplayedPerPageByResolution(MediaResolution resolution) {
    int nbPhotosPerPage = 15;
    if (resolution != null) {
      if (resolution.isTiny()) {
        nbPhotosPerPage = 35;
      } else if (resolution.isSmall()) {
        nbPhotosPerPage = 15;
      } else if (resolution.isMedium()) {
        nbPhotosPerPage = 6;
      }
    }
    return nbPhotosPerPage;
  }

  public static boolean isDragAndDropEnabled(String componentInstanceId) {
    return StringUtil.getBooleanValue(OrganisationControllerFactory.getOrganisationController()
        .getComponentParameterValue(componentInstanceId, "dragAndDrop"));
  }

  public static boolean isPdcEnabled(String componentInstanceId) {
    return StringUtil.getBooleanValue(OrganisationControllerFactory.getOrganisationController()
        .getComponentParameterValue(componentInstanceId, "usePdc"));
  }

  public static boolean isViewMetadataEnabled(String componentInstanceId) {
    return StringUtil.getBooleanValue(OrganisationControllerFactory.getOrganisationController()
        .getComponentParameterValue(componentInstanceId, "viewMetadata"));
  }

  public static boolean isMakeWatermarkEnabled(String componentInstanceId) {
    return StringUtil.getBooleanValue(OrganisationControllerFactory.getOrganisationController()
        .getComponentParameterValue(componentInstanceId, "watermark"));
  }

  public static Integer getWatermarkSize(String bundlePartOfWaterwarkSizeLabel) {
    String tmpValue =
        getSettings().getString("sizeWatermark" + bundlePartOfWaterwarkSizeLabel, null);
    return (StringUtil.isInteger(tmpValue)) ? Integer.valueOf(tmpValue) : null;
  }
}
