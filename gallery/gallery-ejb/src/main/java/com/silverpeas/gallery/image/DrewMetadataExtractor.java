/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.image;

import static com.drew.metadata.iptc.IptcDirectory.TAG_BY_LINE;
import static com.drew.metadata.iptc.IptcDirectory.TAG_BY_LINE_TITLE;
import static com.drew.metadata.iptc.IptcDirectory.TAG_CAPTION;
import static com.drew.metadata.iptc.IptcDirectory.TAG_CATEGORY;
import static com.drew.metadata.iptc.IptcDirectory.TAG_CITY;
import static com.drew.metadata.iptc.IptcDirectory.TAG_COPYRIGHT_NOTICE;
import static com.drew.metadata.iptc.IptcDirectory.TAG_COUNTRY_OR_PRIMARY_LOCATION;
import static com.drew.metadata.iptc.IptcDirectory.TAG_CREDIT;
import static com.drew.metadata.iptc.IptcDirectory.TAG_DATE_CREATED;
import static com.drew.metadata.iptc.IptcDirectory.TAG_HEADLINE;
import static com.drew.metadata.iptc.IptcDirectory.TAG_KEYWORDS;
import static com.drew.metadata.iptc.IptcDirectory.TAG_OBJECT_NAME;
import static com.drew.metadata.iptc.IptcDirectory.TAG_ORIGINAL_TRANSMISSION_REFERENCE;
import static com.drew.metadata.iptc.IptcDirectory.TAG_ORIGINATING_PROGRAM;
import static com.drew.metadata.iptc.IptcDirectory.TAG_PROVINCE_OR_STATE;
import static com.drew.metadata.iptc.IptcDirectory.TAG_RECORD_VERSION;
import static com.drew.metadata.iptc.IptcDirectory.TAG_RELEASE_DATE;
import static com.drew.metadata.iptc.IptcDirectory.TAG_RELEASE_TIME;
import static com.drew.metadata.iptc.IptcDirectory.TAG_SOURCE;
import static com.drew.metadata.iptc.IptcDirectory.TAG_SPECIAL_INSTRUCTIONS;
import static com.drew.metadata.iptc.IptcDirectory.TAG_SUPPLEMENTAL_CATEGORIES;
import static com.drew.metadata.iptc.IptcDirectory.TAG_TIME_CREATED;
import static com.drew.metadata.iptc.IptcDirectory.TAG_URGENCY;
import static com.drew.metadata.iptc.IptcDirectory.TAG_WRITER;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifDescriptor;
import com.drew.metadata.exif.ExifDirectory;
import com.drew.metadata.iptc.IptcDirectory;
import com.silverpeas.gallery.model.MetaData;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 *
 * @author ehugonnet
 */
public class DrewMetadataExtractor implements ImageMetadataExtractor {

  private ResourceLocator settings;
  private Map<String, ResourceLocator> metaDataBundles;
  private List<ExifProperty> imageProperties;
  private List<IptcProperty> imageIptcProperties;

  public
  DrewMetadataExtractor(String instanceId) {
    this.settings = new ResourceLocator("com.silverpeas.gallery.settings.metadataSettings_" + instanceId, "");
    this.metaDataBundles = new HashMap<String, ResourceLocator>(I18NHelper.allLanguages.size());
    for (String lang : I18NHelper.allLanguages.keySet()) {
      metaDataBundles.put(lang, new ResourceLocator("com.silverpeas.gallery.multilang.metadataBundle",
          lang));
    }
    String display = settings.getString("display");
    this.imageProperties = defineImageProperties(COMMA_SPLITTER.split(display));
    this.imageIptcProperties = defineImageIptcProperties(COMMA_SPLITTER.split(display));

  }

  @Override
  public final List<ExifProperty> defineImageProperties(Iterable<String> propertyNames) {
    List<ExifProperty> properties = new ArrayList<ExifProperty>();

    for (String value : propertyNames) {
      if (value.startsWith("METADATA_")) {
        String property = settings.getString(value + "_TAG");
        String labelKey = settings.getString(value + "_LABEL");
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
  
  @Override
  public final List<IptcProperty> defineImageIptcProperties(Iterable<String> propertyNames) {
    List<IptcProperty> properties = new ArrayList<IptcProperty>();
    for (String value : propertyNames) {
      if (value.startsWith("IPTC_")) {
        String property = settings.getString(value + "_TAG");
        String labelKey = settings.getString(value + "_LABEL");
        boolean isDate = settings.getBoolean(value + "_DATE", false);
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

  public List<MetaData> extractImageExifMetaData(File image) throws ImageMetadataException,
      UnsupportedEncodingException {
    return extractImageExifMetaData(image, I18NHelper.defaultLanguage);
  }

  public List<MetaData> extractImageExifMetaData(File image, String lang) throws
      ImageMetadataException, UnsupportedEncodingException {
    try {
      List<MetaData> result = new ArrayList<MetaData>();
      // lire le fichier des properties
      // 1. Traitement des metadata EXIF
      Metadata metadata = ImageMetadataReader.readMetadata(image);
      Directory exifDirectory = metadata.getDirectory(ExifDirectory.class);
      ExifDescriptor descriptor = new ExifDescriptor(exifDirectory);
      String value = null;
      for (ExifProperty property : imageProperties) {
        // rechercher la valeur de la metadata "label"
        int currentMetadata = property.getProperty();
        switch (currentMetadata) {
          case ExifDirectory.TAG_WIN_AUTHOR:
            value = descriptor.getWindowsAuthorDescription();
            break;
          case ExifDirectory.TAG_WIN_COMMENT:
            value = descriptor.getWindowsCommentDescription();
            break;

          case ExifDirectory.TAG_WIN_KEYWORDS:
            value = descriptor.getWindowsKeywordsDescription();
            break;

          case ExifDirectory.TAG_WIN_SUBJECT:
            value = descriptor.getWindowsSubjectDescription();
            break;

          case ExifDirectory.TAG_WIN_TITLE:
            value = descriptor.getWindowsTitleDescription();
            break;
          default:
            value = exifDirectory.getString(currentMetadata);
        }
        if (value != null) {
          // ajout de cette metadata à la photo
          MetaData metaData = new MetaData();
          metaData.setLabel(property.getLabel(lang));
          metaData.setProperty(property.getProperty() + "");
          metaData.setValue(value);
          SilverTrace.debug("gallery", "GallerySessionController.addMetaData()",
              "root.MSG_GEN_ENTER_METHOD", "METADATA EXIF label = " + property.getLabel()
              + " value = " + value);
          result.add(metaData);
        }
      }
      return result;
    } catch (MetadataException ex) {
      throw new ImageMetadataException(ex);
    } catch (ImageProcessingException ex) {
      throw new ImageMetadataException(ex);
    }
  }

  public List<MetaData> extractImageIptcMetaData(File image) throws ImageMetadataException,
      UnsupportedEncodingException {
    return extractImageIptcMetaData(image, I18NHelper.defaultLanguage);

  }

  public List<MetaData> extractImageIptcMetaData(File image, String lang) throws
      UnsupportedEncodingException, ImageMetadataException {
    try {
      List<MetaData> result = new ArrayList<MetaData>();
      // lire le fichier des properties
      // 1. Traitement des metadata EXIF
      Metadata metadata = ImageMetadataReader.readMetadata(image);
      IptcDirectory iptcDirectory = (IptcDirectory) metadata.getDirectory(IptcDirectory.class);
      for (IptcProperty iptcProperty : imageIptcProperties) {
        // rechercher la valeur de la metadata "label"
        String value = null;
        switch (iptcProperty.getProperty()) {
          case TAG_BY_LINE:
            value = getIptcValue(iptcDirectory, TAG_BY_LINE);
            break;
          case TAG_BY_LINE_TITLE:
            value = getIptcValue(iptcDirectory, TAG_BY_LINE_TITLE);
            break;
          case TAG_CAPTION:
            value = getIptcValue(iptcDirectory, TAG_CAPTION);
            break;
          case TAG_CATEGORY:
            value = getIptcValue(iptcDirectory, TAG_CATEGORY);
            break;
          case TAG_CITY:
            value = getIptcValue(iptcDirectory, TAG_CITY);
            break;
          case TAG_COPYRIGHT_NOTICE:
            value = getIptcValue(iptcDirectory, TAG_COPYRIGHT_NOTICE);
            break;
          case TAG_COUNTRY_OR_PRIMARY_LOCATION:
            value = getIptcValue(iptcDirectory, TAG_COUNTRY_OR_PRIMARY_LOCATION);
            break;
          case TAG_CREDIT:
            value = getIptcValue(iptcDirectory, TAG_CREDIT);
            break;
          case TAG_DATE_CREATED:
            value = getIptcStringValue(iptcDirectory, TAG_DATE_CREATED);
            break;
          case TAG_HEADLINE:
            value = getIptcValue(iptcDirectory, TAG_HEADLINE);
            break;
          case TAG_KEYWORDS:
            value = getIptcStringValue(iptcDirectory, TAG_KEYWORDS);
            break;
          case TAG_OBJECT_NAME:
            value = getIptcValue(iptcDirectory, TAG_OBJECT_NAME);
            break;
          case TAG_ORIGINAL_TRANSMISSION_REFERENCE:
            value = getIptcValue(iptcDirectory, TAG_ORIGINAL_TRANSMISSION_REFERENCE);
            break;
          case TAG_ORIGINATING_PROGRAM:
            value = getIptcValue(iptcDirectory, TAG_ORIGINATING_PROGRAM);
            break;
          case TAG_PROVINCE_OR_STATE:
            value = getIptcValue(iptcDirectory, TAG_PROVINCE_OR_STATE);
            break;
          case TAG_RECORD_VERSION:
            value = getIptcValue(iptcDirectory, TAG_RECORD_VERSION);
            break;
          case TAG_RELEASE_DATE:
            value = getIptcStringValue(iptcDirectory, TAG_RELEASE_DATE);
            break;
          case TAG_RELEASE_TIME:
            value = getIptcValue(iptcDirectory, TAG_RELEASE_TIME);
            break;
          case TAG_SOURCE:
            value = getIptcValue(iptcDirectory, TAG_SOURCE);
            break;
          case TAG_SPECIAL_INSTRUCTIONS:
            value = getIptcValue(iptcDirectory, TAG_SPECIAL_INSTRUCTIONS);
            break;
          case TAG_SUPPLEMENTAL_CATEGORIES:
            value = getIptcValue(iptcDirectory, TAG_SUPPLEMENTAL_CATEGORIES);
            break;
          case TAG_TIME_CREATED:
            value = getIptcValue(iptcDirectory, TAG_TIME_CREATED);
            break;
          case TAG_URGENCY:
            value = getIptcValue(iptcDirectory, TAG_URGENCY);
            break;
          case TAG_WRITER:
            value = getIptcValue(iptcDirectory, TAG_WRITER);
            break;
          default:
            value = getIptcValue(iptcDirectory, iptcProperty.getProperty());
            break;
        }
        if (value != null) {
          MetaData metaData = new MetaData();
          metaData.setLabel(iptcProperty.getLabel(lang));
          metaData.setProperty(iptcProperty.getProperty() + "");
          metaData.setValue(value);
          if (iptcProperty.isDate()) {
            metaData.setDate(true);
            metaData.setDateValue(iptcDirectory.getDate(iptcProperty.getProperty()));
          }
          result.add(metaData);
          SilverTrace.debug("gallery",
              "GallerySessionController.addMetaData()",
              "root.MSG_GEN_ENTER_METHOD", "METADATA IPTC label = " + iptcProperty.getLabel()
              + " value = " + value);
        }
      }
      return result;
    } catch (MetadataException ex) {
      throw new ImageMetadataException(ex);
    } catch (ImageProcessingException ex) {
      throw new ImageMetadataException(ex);
    }
  }

  private String getIptcValue(IptcDirectory iptcDirectory, int iptcTag) throws
      UnsupportedEncodingException, MetadataException {
    if (iptcDirectory.containsTag(iptcTag)) {
      byte[] data = iptcDirectory.getByteArray(iptcTag);
      String encoding = StringUtil.detectEncoding(data, "UTF-85");
      return new String(data, encoding);
    }
    return null;
  }

  private String getIptcStringValue(IptcDirectory iptcDirectory, int iptcTag) throws
      UnsupportedEncodingException, MetadataException {
    if (iptcDirectory.containsTag(iptcTag)) {
      return iptcDirectory.getString(iptcTag);
    }
    return null;
  }
}
