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
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery;

import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.io.media.video.ThumbnailPeriod;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;

/**
 * It gathers all the settings and i18n relative to the Gallery component.
 * @author mmoquillon
 */
public final class GalleryComponentSettings {

  /**
   * The name of the Gallery component in Silverpeas.
   */
  public static final String COMPONENT_NAME = "gallery";

  /**
   * The relative path of the properties file containing the settings of the Gallery component.
   */
  public static final String SETTINGS_PATH = "org.silverpeas.gallery.settings.gallerySettings";

  /**
   * The relative path of the i18n bundle of the Gallery component.
   */
  public static final String MESSAGES_PATH = "org.silverpeas.gallery.multilang.galleryBundle";

  /**
   * The relative path of the properties file containing the references of the icons dedicated to
   * the Gallery component.
   */
  public static final String ICONS_PATH = "org.silverpeas.gallery.settings.galleryIcons";
  private static final int DEFAULT_NBMEDIAS_PER_PAGE = 15;

  private GalleryComponentSettings() {
  }

  /**
   * Gets all the messages for the Gallery component and translated in the specified language.
   * @param language the language in which are written the messages.
   * @return the resource with the translated messages.
   */
  public static LocalizationBundle getMessagesIn(String language) {
    return ResourceLocator.getLocalizationBundle(MESSAGES_PATH, language);
  }

  /**
   * Gets all the settings of the Gallery component.
   * @return the resource with the different component settings.
   */
  public static SettingBundle getSettings() {
    return ResourceLocator.getSettingBundle(SETTINGS_PATH);
  }

  /**
   * Gets all the icons definitions particular to the Gallery component.
   * @return the resource with icons definition.
   */
  public static LocalizationBundle getIcons() {
    return ResourceLocator.getLocalizationBundle(ICONS_PATH);
  }

  /**
   * Gets the max number of media displayed on homepage.
   * @return
   */
  public static int getNbMediaDisplayedPerPage() {
    return DEFAULT_NBMEDIAS_PER_PAGE;
  }

  public static boolean isDragAndDropEnabled(String componentInstanceId) {
    return StringUtil.getBooleanValue(OrganizationControllerProvider.getOrganisationController()
        .getComponentParameterValue(componentInstanceId, "dragAndDrop"));
  }

  public static boolean isPdcEnabled(String componentInstanceId) {
    return StringUtil.getBooleanValue(OrganizationControllerProvider.getOrganisationController()
        .getComponentParameterValue(componentInstanceId, "usePdc"));
  }

  public static boolean isViewMetadataEnabled(String componentInstanceId) {
    return StringUtil.getBooleanValue(OrganizationControllerProvider.getOrganisationController()
        .getComponentParameterValue(componentInstanceId, "viewMetadata"));
  }

  public static boolean isMakeWatermarkEnabled(String componentInstanceId) {
    return StringUtil.getBooleanValue(OrganizationControllerProvider.getOrganisationController()
        .getComponentParameterValue(componentInstanceId, "watermark"));
  }

  private static String getWatermarkIdForOriginalResolution(String componentInstanceId) {
    String watermarkHD = OrganizationControllerProvider.getOrganisationController()
        .getComponentParameterValue(componentInstanceId, "WatermarkHD");
    if (!StringUtil.isInteger(watermarkHD)) {
      watermarkHD = "";
    }
    return watermarkHD;
  }

  private static String getWatermarkIdForThumbnailResolution(String componentInstanceId) {
    String watermarkOther = OrganizationControllerProvider.getOrganisationController()
        .getComponentParameterValue(componentInstanceId, "WatermarkOther");
    if (!StringUtil.isInteger(watermarkOther)) {
      watermarkOther = "";
    }
    return watermarkOther;
  }

  public static Integer getWatermarkSize(String bundlePartOfWaterwarkSizeLabel) {
    String tmpValue =
        getSettings().getString("sizeWatermark" + bundlePartOfWaterwarkSizeLabel, null);
    return StringUtil.isInteger(tmpValue) ? Integer.valueOf(tmpValue) : null;
  }

  public static int getMaxNumberOfPreviewThumbnail() {
    return ThumbnailPeriod.ALL_VALIDS.size();
  }

  public static Watermark getWatermark(String componentInstanceId) {
    Watermark watermark = new Watermark();
    watermark.setEnabled(isMakeWatermarkEnabled(componentInstanceId));
    watermark.setIPTCPropertyForHD(getWatermarkIdForOriginalResolution(componentInstanceId));
    watermark
        .setIPTCPropertyForThumbnails(getWatermarkIdForThumbnailResolution(componentInstanceId));
    watermark.setTextForHD(OrganizationController.get()
        .getComponentParameterValue(componentInstanceId, "WatermarkTextHD"));
    watermark.setTextForThumbnails(OrganizationController.get()
        .getComponentParameterValue(componentInstanceId, "WatermarkTextOther"));
    watermark.setImageUrlForHD(componentInstanceId, OrganizationController.get()
        .getComponentParameterValue(componentInstanceId, "WatermarkImageUrlHD"));
    watermark.setImageUrlForThumbnails(componentInstanceId, OrganizationController.get()
        .getComponentParameterValue(componentInstanceId, "WatermarkImageUrlOther"));
    return watermark;
  }
}