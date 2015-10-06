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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.silverpeas.gallery.model.MetaData;
import org.silverpeas.util.ArrayUtil;
import org.silverpeas.util.StringUtil;
import org.silverpeas.util.i18n.I18NHelper;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Descriptor;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.iptc.IptcDirectory;
import org.apache.commons.lang3.CharEncoding;

import static com.drew.metadata.iptc.IptcDirectory.*;

/**
 * @author ehugonnet
 */
public class DrewMediaMetadataExtractor extends AbstractMediaMetadataExtractor {

  public DrewMediaMetadataExtractor(String instanceId) {
    init(instanceId);
  }

  @Override
  public List<MetaData> extractImageExifMetaData(File image)
      throws MediaMetadataException, IOException {
    return extractImageExifMetaData(image, I18NHelper.defaultLanguage);
  }

  @Override
  public List<MetaData> extractImageExifMetaData(File image, String lang)
      throws MediaMetadataException, IOException {
    try {
      List<MetaData> result = new ArrayList<>();
      // lire le fichier des properties
      // 1. Traitement des metadata EXIF
      Metadata metadata = ImageMetadataReader.readMetadata(image);
      ExifIFD0Directory exifDirectory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
      ExifIFD0Descriptor descriptor = new ExifIFD0Descriptor(exifDirectory);
      String value;
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

            case ExifIFD0Directory.TAG_RESOLUTION_UNIT:
              value = exifDirectory.getString(currentMetadata);
              if("2".equals(value)) {//dots per inch
                value = "DPI";
              } else if ("3".equals(value)) {//dots per cm
                value = "DPC";
              }
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
                "root.MSG_GEN_ENTER_METHOD",
                "METADATA EXIF label = " + property.getLabel() + " value = " + value);
            result.add(metaData);
          }
        }
      }
      return result;
    } catch (IOException | ImageProcessingException ex) {
      throw new MediaMetadataException(ex);
    }
  }

  @Override
  public List<MetaData> extractImageIptcMetaData(File image)
      throws MediaMetadataException, IOException {
    return extractImageIptcMetaData(image, I18NHelper.defaultLanguage);
  }

  @Override
  public List<MetaData> extractImageIptcMetaData(File image, String lang)
      throws IOException, MediaMetadataException {
    try {
      List<MetaData> result = new ArrayList<>();
      Metadata metadata = ImageMetadataReader.readMetadata(image);
      ByteArrayOutputStream forEncodingDetection = new ByteArrayOutputStream(1024);
      IptcDirectory iptcDirectory = metadata.getFirstDirectoryOfType(IptcDirectory.class);
      String iptcCharset = getIptcCharset(iptcDirectory);
      for (IptcProperty iptcProperty : imageIptcProperties) {
        switch (iptcProperty.getProperty()) {
          case TAG_RELEASE_DATE:
          case TAG_DATE_CREATED:
          case TAG_KEYWORDS:
            addStringMetaData(result, iptcDirectory, iptcProperty, lang, forEncodingDetection);
            break;
          default:
            addMetaData(result, iptcDirectory, iptcProperty, lang, forEncodingDetection);
            break;
        }
      }
      String defaultEncoding = CharEncoding.UTF_8;
      if (iptcCharset != null) {
        defaultEncoding = iptcCharset;
      }
      String encoding =
          StringUtil.detectStringEncoding(forEncodingDetection.toByteArray(), defaultEncoding);

      for (MetaData metaData : result) {
        metaData.convert(encoding);
      }
      return result;
    } catch (ImageProcessingException ex) {
      throw new MediaMetadataException(ex);
    }
  }

  private void addStringMetaData(List<MetaData> metadata, IptcDirectory iptcDirectory,
      IptcProperty iptcProperty, String lang, ByteArrayOutputStream forEncodingDetection)
      throws IOException {
    String value = getIptcStringValue(iptcDirectory, iptcProperty.getProperty());
    if (value != null) {
      MetaData meta = new MetaData(value.getBytes());
      meta.setLabel(iptcProperty.getLabel(lang));
      meta.setProperty(iptcProperty.getProperty() + "");
      if (iptcProperty.isDate()) {
        meta.setDate(true);
        meta.setDateValue(iptcDirectory.getDate(iptcProperty.getProperty()));
      } else {
        forEncodingDetection.write(value.getBytes());
      }
      metadata.add(meta);
    }
  }

  private void addMetaData(List<MetaData> metadata, IptcDirectory iptcDirectory,
      IptcProperty iptcProperty, String lang, ByteArrayOutputStream forEncodingDetection)
      throws IOException {
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

  private byte[] getIptcValue(IptcDirectory iptcDirectory, int iptcTag) {
    if (iptcDirectory != null && iptcDirectory.containsTag(iptcTag)) {
      return iptcDirectory.getByteArray(iptcTag);
    }
    return null;
  }

  private String getIptcStringValue(IptcDirectory iptcDirectory, int iptcTag) {
    if (iptcDirectory != null && iptcDirectory.containsTag(iptcTag)) {
      return iptcDirectory.getString(iptcTag);
    }
    return null;
  }

  private String getIptcCharset(IptcDirectory iptcDirectory) throws UnsupportedEncodingException {
    if (iptcDirectory != null && iptcDirectory.containsTag(TAG_CODED_CHARACTER_SET)) {
      byte[] data = iptcDirectory.getByteArray(TAG_CODED_CHARACTER_SET);
      if (data != null) {
        String escapeCode = new String(data, CharEncoding.UTF_8);
        if ("%G".equals(escapeCode)) {
          return CharEncoding.UTF_8;
        }
      }
    }
    return null;
  }
}
