/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.image;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Descriptor;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.iptc.IptcDirectory;
import com.silverpeas.gallery.model.MetaData;
import com.silverpeas.util.ArrayUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.CharEncoding;

import static com.drew.metadata.iptc.IptcDirectory.*;

/**
 *
 * @author ehugonnet
 */
public class DrewImageMetadataExtractor extends AbstractImageMetadataExtractor {

  public DrewImageMetadataExtractor(String instanceId) {
    init(instanceId);

  }

  @Override
  public List<MetaData> extractImageExifMetaData(File image) throws ImageMetadataException,
    IOException {
    return extractImageExifMetaData(image, I18NHelper.defaultLanguage);
  }

  @Override
  public List<MetaData> extractImageExifMetaData(File image, String lang) throws
    ImageMetadataException, IOException {
    try {
      List<MetaData> result = new ArrayList<MetaData>();
      // lire le fichier des properties
      // 1. Traitement des metadata EXIF
      Metadata metadata = ImageMetadataReader.readMetadata(image);
      ExifIFD0Directory exifDirectory = metadata.getDirectory(ExifIFD0Directory.class);
      ExifIFD0Descriptor descriptor = new ExifIFD0Descriptor(exifDirectory);
      String value = null;
      if (exifDirectory != null) {
        for (ExifProperty property : imageProperties) {
          // rechercher la valeur de la metadata "label"
          int currentMetadata = property.getProperty();
          switch (currentMetadata) {
            case ExifIFD0Directory.TAG_WIN_AUTHOR:
              value = descriptor.getWindowsAuthorDescription();
              break;
            case ExifIFD0Directory.TAG_WIN_COMMENT:
              value = descriptor.getWindowsCommentDescription();
              break;

            case ExifIFD0Directory.TAG_WIN_KEYWORDS:
              value = descriptor.getWindowsKeywordsDescription();
              break;

            case ExifIFD0Directory.TAG_WIN_SUBJECT:
              value = descriptor.getWindowsSubjectDescription();
              break;

            case ExifIFD0Directory.TAG_WIN_TITLE:
              value = descriptor.getWindowsTitleDescription();
              break;
            default:
              value = exifDirectory.getString(currentMetadata);
          }
          if (value != null) {
            // ajout de cette metadata à la photo
            MetaData metaData = new MetaData(value);
            metaData.setLabel(property.getLabel(lang));
            metaData.setProperty(property.getProperty() + "");
            SilverTrace.debug("gallery", "GallerySessionController.addMetaData()",
              "root.MSG_GEN_ENTER_METHOD", "METADATA EXIF label = " + property.getLabel()
              + " value = " + value);
            result.add(metaData);
          }
        }
      }
      return result;
    } catch (IOException ex) {
      throw new ImageMetadataException(ex);
    } catch (ImageProcessingException ex) {
      throw new ImageMetadataException(ex);
    }
  }

  @Override
  public List<MetaData> extractImageIptcMetaData(File image) throws ImageMetadataException,
    IOException {
    return extractImageIptcMetaData(image, I18NHelper.defaultLanguage);

  }

  @Override
  public List<MetaData> extractImageIptcMetaData(File image, String lang) throws
    IOException, ImageMetadataException {
    try {
      List<MetaData> result = new ArrayList<MetaData>();
      Metadata metadata = ImageMetadataReader.readMetadata(image);
      ByteArrayOutputStream forEncodingDetection = new ByteArrayOutputStream(1024);
      IptcDirectory iptcDirectory = metadata.getDirectory(IptcDirectory.class);
      String iptcCharset = getIptcCharset(iptcDirectory);
      for (IptcProperty iptcProperty : imageIptcProperties) {
        switch (iptcProperty.getProperty()) {
          case TAG_RELEASE_DATE:
          case TAG_DATE_CREATED:
          case TAG_KEYWORDS:
            addStringMetaData(result, iptcDirectory, iptcProperty, lang);
            break;
          case TAG_BY_LINE:
          case TAG_BY_LINE_TITLE:
          case TAG_CAPTION:
          case TAG_CATEGORY:
          case TAG_CITY:
          case TAG_COPYRIGHT_NOTICE:
          case TAG_COUNTRY_OR_PRIMARY_LOCATION_NAME:
          case TAG_COUNTRY_OR_PRIMARY_LOCATION_CODE:
          case TAG_CREDIT:
          case TAG_HEADLINE:
          case TAG_OBJECT_NAME:
          case TAG_ORIGINAL_TRANSMISSION_REFERENCE:
          case TAG_ORIGINATING_PROGRAM:
          case TAG_PROVINCE_OR_STATE:
          case TAG_RELEASE_TIME:
          case TAG_SOURCE:
          case TAG_SPECIAL_INSTRUCTIONS:
          case TAG_SUPPLEMENTAL_CATEGORIES:
          case TAG_TIME_CREATED:
          case TAG_URGENCY:
          case TAG_CAPTION_WRITER:
          default:
            addMetaData(result, iptcDirectory, iptcProperty, lang, forEncodingDetection);
            break;
        }
      }
      String defaultEncoding = CharEncoding.UTF_8;
      if (iptcCharset != null) {
        defaultEncoding = iptcCharset;
      }
      String encoding = StringUtil.detectStringEncoding(forEncodingDetection.toByteArray(),
        defaultEncoding);

      for (MetaData metaData : result) {
        metaData.convert(encoding);
      }
      return result;
    } catch (MetadataException ex) {
      throw new ImageMetadataException(ex);
    } catch (ImageProcessingException ex) {
      throw new ImageMetadataException(ex);
    }
  }

  private void addStringMetaData(List<MetaData> metadata, IptcDirectory iptcDirectory,
    IptcProperty iptcProperty, String lang) throws UnsupportedEncodingException, MetadataException {
    String value = getIptcStringValue(iptcDirectory, iptcProperty.getProperty());
    if (value != null) {
      MetaData meta = new MetaData(value);
      meta.setLabel(iptcProperty.getLabel(lang));
      meta.setProperty(iptcProperty.getProperty() + "");
      if (iptcProperty.isDate()) {
        meta.setDate(true);
        meta.setDateValue(iptcDirectory.getDate(iptcProperty.getProperty()));
      }
      metadata.add(meta);
    }
  }

  private void addMetaData(List<MetaData> metadata, IptcDirectory iptcDirectory,
    IptcProperty iptcProperty, String lang, ByteArrayOutputStream forEncodingDetection)
    throws IOException, UnsupportedEncodingException, MetadataException {
    byte[] data = getIptcValue(iptcDirectory, iptcProperty.getProperty());
    if (data != null && !ArrayUtil.isEmpty(data)) {
      MetaData meta = new MetaData(data);
      meta.setLabel(iptcProperty.getLabel(lang));
      meta.setProperty(iptcProperty.getProperty() + "");
      if (iptcProperty.isDate()) {
        meta.setDate(true);
        meta.setDateValue(iptcDirectory.getDate(iptcProperty.getProperty()));
      } else {
        forEncodingDetection.write(data);
      }
      metadata.add(meta);
    }
  }

  private byte[] getIptcValue(IptcDirectory iptcDirectory, int iptcTag) throws
    UnsupportedEncodingException, MetadataException {
    if (iptcDirectory != null && iptcDirectory.containsTag(iptcTag)) {
      return iptcDirectory.getByteArray(iptcTag);
    }
    return null;
  }

  private String getIptcStringValue(IptcDirectory iptcDirectory, int iptcTag) throws
    UnsupportedEncodingException, MetadataException {
    if (iptcDirectory != null && iptcDirectory.containsTag(iptcTag)) {
      return iptcDirectory.getString(iptcTag);
    }
    return null;
  }

  private String getIptcCharset(IptcDirectory iptcDirectory) throws UnsupportedEncodingException {
    if (iptcDirectory != null && iptcDirectory.containsTag(TAG_CODED_CHARACTER_SET)) {
      byte[] data = iptcDirectory.getByteArray(TAG_CODED_CHARACTER_SET);
      String escapeCode = new String(data, CharEncoding.UTF_8);
      if ("%G".equals(escapeCode)) {
        return CharEncoding.UTF_8;
      }
    }
    return null;
  }
}
