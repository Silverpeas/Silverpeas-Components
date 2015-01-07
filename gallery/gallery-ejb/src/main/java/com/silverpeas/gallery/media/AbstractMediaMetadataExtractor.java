/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.media;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.silverpeas.ui.DisplayI18NHelper;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;

public abstract class AbstractMediaMetadataExtractor implements MediaMetadataExtractor {

  static final Properties DEFAULT_SETTINGS = new Properties();

  static {
    try {
      FileUtil.loadProperties(DEFAULT_SETTINGS,
          "org/silverpeas/gallery/settings/metadataSettings.properties");
    } catch (IOException e) {
      SilverTrace.error("gallery", AbstractMediaMetadataExtractor.class.getName(),
          "Problem loading metadata settings", e);
    }
  }
  protected Properties settings = new Properties(DEFAULT_SETTINGS);
  protected Map<String, ResourceLocator> metaDataBundles;
  protected List<ExifProperty> imageProperties;
  protected List<IptcProperty> imageIptcProperties;

  @Override
  public final List<ExifProperty> defineImageProperties(Iterable<String> propertyNames) {
    List<ExifProperty> properties = new ArrayList<ExifProperty>();
    for (String value : propertyNames) {
      if (value.startsWith("METADATA_")) {
        String property = settings.getProperty(value + "_TAG");
        if (property != null) {
          String labelKey = settings.getProperty(value + "_LABEL");
          ExifProperty exifProperty = new ExifProperty(Integer.valueOf(property));
          for (Map.Entry<String, ResourceLocator> labels : metaDataBundles.entrySet()) {
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
        String property = settings.getProperty(value + "_TAG");
        if (property != null) {
          String labelKey = settings.getProperty(value + "_LABEL");
          boolean isDate = StringUtil.getBooleanValue(settings.getProperty(value + "_DATE"));
          IptcProperty iptcProperty = new IptcProperty(Integer.valueOf(property));
          for (Map.Entry<String, ResourceLocator> labels : metaDataBundles.entrySet()) {
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
    try {
      FileUtil.loadProperties(settings,
          "org/silverpeas/gallery/settings/metadataSettings_" + instanceId + ".properties");
    } catch (Exception e) {
      this.settings = DEFAULT_SETTINGS;
    }
    this.metaDataBundles =
        new HashMap<String, ResourceLocator>(DisplayI18NHelper.getLanguages().size());
    for (String lang : DisplayI18NHelper.getLanguages()) {
      metaDataBundles
          .put(lang, new ResourceLocator("org.silverpeas.gallery.multilang.metadataBundle", lang));
    }
    String display = settings.getProperty("display");
    Iterable<String> propertyNames = StringUtil.splitString(display, ',');
    this.imageProperties = defineImageProperties(propertyNames);
    this.imageIptcProperties = defineImageIptcProperties(propertyNames);
  }
}
