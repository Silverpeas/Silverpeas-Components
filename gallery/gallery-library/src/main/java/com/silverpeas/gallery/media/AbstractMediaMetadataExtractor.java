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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.media;

import com.silverpeas.ui.DisplayI18NHelper;
import org.silverpeas.util.LocalizationBundle;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.util.SettingBundle;
import org.silverpeas.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractMediaMetadataExtractor implements MediaMetadataExtractor {

  private static final SettingBundle DEFAULT_SETTINGS =
      ResourceLocator.getSettingBundle("org.silverpeas.gallery.settings.metadataSettings");

  protected SettingBundle settings = DEFAULT_SETTINGS;
  protected Map<String, LocalizationBundle> metaDataBundles;
  protected List<ExifProperty> imageProperties;
  protected List<IptcProperty> imageIptcProperties;

  @Override
  public final List<ExifProperty> defineImageProperties(Iterable<String> propertyNames) {
    List<ExifProperty> properties = new ArrayList<ExifProperty>();
    for (String value : propertyNames) {
      if (value.startsWith("METADATA_")) {
        String property = settings.getString(value + "_TAG");
        if (property != null) {
          String labelKey = settings.getString(value + "_LABEL");
          ExifProperty exifProperty = new ExifProperty(Integer.valueOf(property));
          for (Map.Entry<String, LocalizationBundle> labels : metaDataBundles.entrySet()) {
            String label = labels.getValue().getString(labelKey);
            exifProperty.setLabel(labels.getKey(), label);
          }
          properties.add(exifProperty);
        }
      }
    }
    return properties;
  }

  @Override
  public final List<IptcProperty> defineImageIptcProperties(Iterable<String> propertyNames) {
    List<IptcProperty> properties = new ArrayList<IptcProperty>();
    for (String value : propertyNames) {
      if (value.startsWith("IPTC_")) {
        String property = settings.getString(value + "_TAG");
        if (property != null) {
          String labelKey = settings.getString(value + "_LABEL");
          boolean isDate = StringUtil.getBooleanValue(settings.getString(value + "_DATE"));
          IptcProperty iptcProperty = new IptcProperty(Integer.valueOf(property));
          for (Map.Entry<String, LocalizationBundle> labels : metaDataBundles.entrySet()) {
            String label = labels.getValue().getString(labelKey);
            iptcProperty.setLabel(labels.getKey(), label);
          }
          iptcProperty.setDate(isDate);
          properties.add(iptcProperty);
        }
      }
    }
    return properties;
  }

  final void init(String instanceId) {
    settings = ResourceLocator.getSettingBundle(
        "org.silverpeas.gallery.settings.metadataSettings_" + instanceId);
    if (!settings.exists()) {
      this.settings = DEFAULT_SETTINGS;
    }
    this.metaDataBundles = new HashMap<>(DisplayI18NHelper.getLanguages().size());
    for (String lang : DisplayI18NHelper.getLanguages()) {
      metaDataBundles.put(lang,
          ResourceLocator.getLocalizationBundle("org.silverpeas.gallery.multilang.metadataBundle",
              lang));
    }
    String display = settings.getString("display");
    Iterable<String> propertyNames = StringUtil.splitString(display, ',');
    this.imageProperties = defineImageProperties(propertyNames);
    this.imageIptcProperties = defineImageIptcProperties(propertyNames);
  }
}
