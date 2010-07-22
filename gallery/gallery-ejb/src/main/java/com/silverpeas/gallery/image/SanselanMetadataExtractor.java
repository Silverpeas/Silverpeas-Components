/**
 * Copyright (C) 2000 - 2009 Silverpeas
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

import com.drew.metadata.exif.ExifDirectory;
import com.silverpeas.gallery.model.MetaData;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.common.ImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.iptc.IPTCConstants;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.apache.sanselan.formats.tiff.constants.TiffFieldTypeConstants;

/**
 *
 * @author ehugonnet
 */
public class SanselanMetadataExtractor implements ImageMetadataExtractor {

  public static final int TAG_RECORD_VERSION = 0x0200;
  public static final int TAG_CAPTION = 0x0278;
  public static final int TAG_WRITER = 0x027a;
  public static final int TAG_HEADLINE = 0x0269;
  public static final int TAG_SPECIAL_INSTRUCTIONS = 0x0228;
  public static final int TAG_BY_LINE = 0x0250;
  public static final int TAG_BY_LINE_TITLE = 0x0255;
  public static final int TAG_CREDIT = 0x026e;
  public static final int TAG_SOURCE = 0x0273;
  public static final int TAG_OBJECT_NAME = 0x0205;
  public static final int TAG_DATE_CREATED = 0x0237;
  public static final int TAG_CITY = 0x025a;
  public static final int TAG_PROVINCE_OR_STATE = 0x025f;
  public static final int TAG_COUNTRY_OR_PRIMARY_LOCATION = 0x0265;
  public static final int TAG_ORIGINAL_TRANSMISSION_REFERENCE = 0x0267;
  public static final int TAG_CATEGORY = 0x020f;
  public static final int TAG_SUPPLEMENTAL_CATEGORIES = 0x0214;
  public static final int TAG_URGENCY = 0x0200 | 10;
  public static final int TAG_KEYWORDS = 0x0200 | 25;
  public static final int TAG_COPYRIGHT_NOTICE = 0x0274;
  public static final int TAG_RELEASE_DATE = 0x0200 | 30;
  public static final int TAG_RELEASE_TIME = 0x0200 | 35;
  public static final int TAG_TIME_CREATED = 0x0200 | 60;
  public static final int TAG_ORIGINATING_PROGRAM = 0x0200 | 65;
  private ResourceLocator settings;
  private Map<String, ResourceLocator> metaDataBundles;
  private List<ExifProperty> imageProperties;
  private List<IptcProperty> imageIptcProperties;

  public SanselanMetadataExtractor() {
    this.settings = new ResourceLocator("com.silverpeas.gallery.settings.metadataSettings",
        I18NHelper.defaultLanguage);
    this.metaDataBundles = new HashMap<String, ResourceLocator>(I18NHelper.allLanguages.size());
    for (String lang : I18NHelper.allLanguages.keySet()) {
      metaDataBundles.put(lang, new ResourceLocator(
          "com.silverpeas.gallery.multilang.metadataBundle",
          lang));
    }
    this.imageProperties = defineImageProperties();
    this.imageIptcProperties = defineImageIptcProperties();

  }

  @Override
  public final List<ExifProperty> defineImageProperties() {
    List<ExifProperty> properties = new ArrayList<ExifProperty>();
    int indice = 1;
    boolean hasMore = true;
    while (hasMore) {
      String property = settings.getString("METADATA_" + indice + "_TAG");
      String labelKey = settings.getString("METADATA_" + indice + "_LABEL");
      hasMore = StringUtil.isInteger(property);
      if (hasMore) {
        ExifProperty exifProperty = new ExifProperty(Integer.valueOf(property));
        for (Map.Entry<String, ResourceLocator> labels : metaDataBundles.entrySet()) {
          String label = labels.getValue().getString(labelKey);
          exifProperty.setLabel(labels.getKey(), label);
        }
        properties.add(exifProperty);
      }
      indice++;
    }
    return properties;
  }

  @Override
  public final List<IptcProperty> defineImageIptcProperties() {
    List<IptcProperty> properties = new ArrayList<IptcProperty>();
    int indice = 1 + imageProperties.size();
    boolean hasMore = true;
    while (hasMore) {
      String property = settings.getString("IPTC_" + indice + "_TAG");
      String labelKey = settings.getString("IPTC_" + indice + "_LABEL");
      boolean isDate = settings.getBoolean("IPTC_" + indice + "_DATE", false);
      hasMore = StringUtil.isInteger(property);
      if (hasMore) {
        IptcProperty iptcProperty = new IptcProperty(Integer.valueOf(property));
        for (Map.Entry<String, ResourceLocator> labels : metaDataBundles.entrySet()) {
          String label = labels.getValue().getString(labelKey);
          iptcProperty.setLabel(labels.getKey(), label);
        }
        iptcProperty.setDate(isDate);
        properties.add(iptcProperty);
      }
      indice++;
    }
    return properties;
  }

  @Override
  public List<MetaData> extractImageExifMetaData(File image) throws ImageMetadataException,
      UnsupportedEncodingException {
    return extractImageExifMetaData(image, I18NHelper.defaultLanguage);
  }

  @Override
  public List<MetaData> extractImageExifMetaData(File image, String lang) throws
      ImageMetadataException, UnsupportedEncodingException {
    try {
      List<MetaData> result = new ArrayList<MetaData>();
      IImageMetadata metadata = Sanselan.getMetadata(image);
      if (metadata instanceof JpegImageMetadata) {
        JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
        String value = null;
        for (ExifProperty property : imageProperties) {
          // rechercher la valeur de la metadata "label"
          int currentMetadata = property.getProperty();
          switch (currentMetadata) {
            case ExifDirectory.TAG_WIN_AUTHOR:
              value = getExifValue(jpegMetadata, TiffConstants.EXIF_TAG_XPAUTHOR);
              break;
            case ExifDirectory.TAG_WIN_COMMENT:
              value = getExifValue(jpegMetadata, TiffConstants.EXIF_TAG_XPCOMMENT);
              break;

            case ExifDirectory.TAG_WIN_KEYWORDS:
              value = getExifValue(jpegMetadata, TiffConstants.EXIF_TAG_XPKEYWORDS);
              break;

            case ExifDirectory.TAG_WIN_SUBJECT:
              value = getExifValue(jpegMetadata, TiffConstants.EXIF_TAG_XPSUBJECT);
              break;

            case ExifDirectory.TAG_WIN_TITLE:
              value = getExifValue(jpegMetadata, TiffConstants.EXIF_TAG_XPTITLE);
              break;
            default:
              value = getExifValue(jpegMetadata, new TagInfo("Specific Metadata", currentMetadata,
                  TiffFieldTypeConstants.FIELD_TYPE_BYTE));
          }
          if (value != null) {
            // ajout de cette metadata à la photo
            MetaData metaData = new MetaData();
            metaData.setLabel(property.getLabel(lang));
            metaData.setProperty(property.getProperty() + "");
            metaData.setValue(value.replaceAll("\\s", " ").trim());
            SilverTrace.debug("gallery", "GallerySessionController.addMetaData()",
                "root.MSG_GEN_ENTER_METHOD", "METADATA EXIF label = " + property.getLabel()
                + " value = " + value);
            result.add(metaData);
          }
        }
      }
      return result;
    } catch (ImageReadException ex) {
      throw new ImageMetadataException(ex);
    } catch (IOException ex) {
      throw new ImageMetadataException(ex);
    }
  }

  @Override
  public List<MetaData> extractImageIptcMetaData(File image) throws ImageMetadataException,
      UnsupportedEncodingException {
    return extractImageIptcMetaData(image, I18NHelper.defaultLanguage);
  }

  @Override
  public List<MetaData> extractImageIptcMetaData(File image, String lang) throws
      ImageMetadataException, UnsupportedEncodingException {
    try {
      List<MetaData> result = new ArrayList<MetaData>();
      IImageMetadata metadata = Sanselan.getMetadata(image);
      if (metadata instanceof JpegImageMetadata) {
        JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
        Map<String, String> iptcValues = getIPTCValues(jpegMetadata);
        for (IptcProperty iptcProperty : imageIptcProperties) {
          // rechercher la valeur de la metadata "label"
          String value = null;
          switch (iptcProperty.getProperty()) {
            case TAG_BY_LINE:
              value = iptcValues.get(IPTCConstants.IPTC_TYPE_BYLINE.name);
              break;
            case TAG_BY_LINE_TITLE:
              value = iptcValues.get(IPTCConstants.IPTC_TYPE_BYLINE_TITLE.name);
              break;
            case TAG_CAPTION:
              value = iptcValues.get(IPTCConstants.IPTC_TYPE_CAPTION_ABSTRACT.name);
              break;
            case TAG_CATEGORY:
              value = iptcValues.get(IPTCConstants.IPTC_TYPE_CATEGORY.name);
              break;
            case TAG_CITY:
              value = iptcValues.get(IPTCConstants.IPTC_TYPE_CITY.name);
              break;
            case TAG_COPYRIGHT_NOTICE:
              value = iptcValues.get(IPTCConstants.IPTC_TYPE_COPYRIGHT_NOTICE.name);
              break;
            case TAG_COUNTRY_OR_PRIMARY_LOCATION:
              value = iptcValues.get(IPTCConstants.IPTC_TYPE_COUNTRY_PRIMARY_LOCATION_NAME.name);
              break;
            case TAG_CREDIT:
              value = iptcValues.get(IPTCConstants.IPTC_TYPE_CREDIT.name);
              break;
            case TAG_DATE_CREATED:
              value = iptcValues.get(IPTCConstants.IPTC_TYPE_DATE_CREATED.name);
              break;
            case TAG_HEADLINE:
              value = iptcValues.get(IPTCConstants.IPTC_TYPE_HEADLINE.name);
              break;
            case TAG_KEYWORDS:
              value = iptcValues.get(IPTCConstants.IPTC_TYPE_KEYWORDS.name);
              break;
            case TAG_OBJECT_NAME:
              value = iptcValues.get(IPTCConstants.IPTC_TYPE_OBJECT_NAME.name);
              break;
            case TAG_ORIGINAL_TRANSMISSION_REFERENCE:
              value = iptcValues.get(IPTCConstants.IPTC_TYPE_ORIGINAL_TRANSMISSION_REFERENCE.name);
              break;
            case TAG_ORIGINATING_PROGRAM:
              value = iptcValues.get(IPTCConstants.IPTC_TYPE_ORIGINATING_PROGRAM.name);
              break;
            case TAG_PROVINCE_OR_STATE:
              value = iptcValues.get(IPTCConstants.IPTC_TYPE_PROVINCE_STATE.name);
              break;
            case TAG_RECORD_VERSION:
              value = iptcValues.get(IPTCConstants.IPTC_TYPE_RECORD_VERSION.name);
              break;
            case TAG_RELEASE_DATE:
              value = iptcValues.get(IPTCConstants.IPTC_TYPE_RELEASE_DATE.name);
              break;
            case TAG_RELEASE_TIME:
              value = iptcValues.get(IPTCConstants.IPTC_TYPE_RELEASE_TIME.name);
              break;
            case TAG_SOURCE:
              value = iptcValues.get(IPTCConstants.IPTC_TYPE_SOURCE.name);
              break;
            case TAG_SPECIAL_INSTRUCTIONS:
              value = iptcValues.get(IPTCConstants.IPTC_TYPE_SPECIAL_INSTRUCTIONS.name);
              break;
            case TAG_SUPPLEMENTAL_CATEGORIES:
              value = iptcValues.get(IPTCConstants.IPTC_TYPE_SUPPLEMENTAL_CATEGORY.name);
              break;
            case TAG_TIME_CREATED:
              value = iptcValues.get(IPTCConstants.IPTC_TYPE_TIME_CREATED.name);
              break;
            case TAG_URGENCY:
              value = iptcValues.get(IPTCConstants.IPTC_TYPE_URGENCY.name);
              break;
            case TAG_WRITER:
              value = iptcValues.get(IPTCConstants.IPTC_TYPE_WRITER_EDITOR.name);
              break;
            default:
              value = iptcValues.get(IPTCConstants.IPTC_TYPE_SPECIAL_INSTRUCTIONS.name);
              break;
          }
          if (value != null) {
            MetaData metaData = new MetaData();
            metaData.setLabel(iptcProperty.getLabel(lang));
            metaData.setProperty(iptcProperty.getProperty() + "");
            metaData.setValue(value);
            if (iptcProperty.isDate()) {
              metaData.setDate(true);
              metaData.setDateValue(getDateValue(value));
            }
            result.add(metaData);
            SilverTrace.debug("gallery",
                "GallerySessionController.addMetaData()",
                "root.MSG_GEN_ENTER_METHOD", "METADATA IPTC label = " + iptcProperty.getLabel()
                + " value = " + value);
          }
        }
      }
      return result;
    } catch (ImageReadException ex) {
      throw new ImageMetadataException(ex);
    } catch (IOException ex) {
      throw new ImageMetadataException(ex);
    }
  }

  private String getExifValue(JpegImageMetadata jpegMetadata, TagInfo tagInfo) throws
      ImageReadException, UnsupportedEncodingException {
    TiffField field = jpegMetadata.findEXIFValue(tagInfo);
    if (field == null) {
      return null;
    }
    try {
      return field.getStringValue();
    } catch (ImageReadException ex) {
    }
    return new String(field.getByteArrayValue(), "UTF-16LE");
  }

  private Map<String, String> getIPTCValues(JpegImageMetadata photoshopMetadata) {
    Map<String, String> result = new HashMap<String, String>();
    @SuppressWarnings("unchecked")
    List<ImageMetadata.Item> items = (List<ImageMetadata.Item>) photoshopMetadata.getItems();
    for (ImageMetadata.Item item : items) {
      result.put(item.getKeyword(), item.getText());
    }
    return result;
  }

  private Date getDateValue(String value) throws ImageMetadataException {
    String datePatterns[] = {"yyyyMMdd",
      "yyyy:MM:dd HH:mm:ss",
      "yyyy:MM:dd HH:mm",
      "yyyy-MM-dd HH:mm:ss",
      "yyyy-MM-dd HH:mm"};
    for (int i = 0; i < datePatterns.length; i++) {
      try {
        DateFormat parser = new java.text.SimpleDateFormat(datePatterns[i]);
        return parser.parse(value);
      } catch (java.text.ParseException ex) {
        // simply try the next pattern
      }
    }
    throw new ImageMetadataException("Value '" + value + "' cannot be cast to a java.util.Date.");
  }
}
