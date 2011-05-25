package com.silverpeas.gallery.image;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.common.base.Splitter;
import com.silverpeas.util.ConfigurationClassLoader;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.webactiv.util.ResourceLocator;

public abstract class AbstractMetadataExtractor implements ImageMetadataExtractor {

  static final Properties defaultSettings = new Properties();
  static final ConfigurationClassLoader loader = new ConfigurationClassLoader(
      ImageMetadataExtractor.class.getClassLoader());
  static {
    try {
      defaultSettings.load(loader
          .getResourceAsStream("/com/silverpeas/gallery/settings/metadataSettings.properties"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }  
  Properties settings = new Properties(defaultSettings);
  Map<String, ResourceLocator> metaDataBundles;
  List<ExifProperty> imageProperties;
  List<IptcProperty> imageIptcProperties;
  
  protected static final Splitter COMMA_SPLITTER = Splitter.on(',');
  
  public final List<ExifProperty> defineImageProperties(Iterable<String> propertyNames) {
    List<ExifProperty> properties = new ArrayList<ExifProperty>();
    for (String value : propertyNames) {
      if (value.startsWith("METADATA_")) {
        String property = settings.getProperty(value + "_TAG");
        String labelKey = settings.getProperty(value + "_LABEL");
        ExifProperty exifProperty = new ExifProperty(Integer.valueOf(property));
        for (Map.Entry<String, ResourceLocator> labels : metaDataBundles.entrySet()) {
          String label = labels.getValue().getString(labelKey);
          exifProperty.setLabel(labels.getKey(), label);
        }
        properties.add(exifProperty);
      }
    }
    return properties;
  }

  public final List<IptcProperty> defineImageIptcProperties(Iterable<String> propertyNames) {
    List<IptcProperty> properties = new ArrayList<IptcProperty>();
    for (String value : propertyNames) {
      if (value.startsWith("IPTC_")) {
        String property = settings.getProperty(value + "_TAG");
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
    return properties;
  }
  
  final void init(String instanceId) {
    try {
      this.settings.load(loader
          .getResourceAsStream("/com/silverpeas/gallery/settings/metadataSettings_" + instanceId +
              ".properties"));
    } catch (Exception e) {
      this.settings = defaultSettings;
    }
    this.metaDataBundles = new HashMap<String, ResourceLocator>(I18NHelper.allLanguages.size());
    for (String lang : I18NHelper.allLanguages.keySet()) {
      metaDataBundles.put(lang, new ResourceLocator("com.silverpeas.gallery.multilang.metadataBundle",
          lang));
    }
    String display = settings.getProperty("display");
    Iterable<String> propertyNames = COMMA_SPLITTER.split(display);
    this.imageProperties = defineImageProperties(propertyNames);
    this.imageIptcProperties = defineImageIptcProperties(propertyNames);
  }

}
