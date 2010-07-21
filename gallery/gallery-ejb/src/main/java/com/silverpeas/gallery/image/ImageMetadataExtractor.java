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

import com.drew.imaging.ImageProcessingException;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.MetadataException;
import com.silverpeas.gallery.model.MetaData;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 *
 * @author ehugonnet
 */
public interface ImageMetadataExtractor {

  List<IptcProperty> defineImageIptcProperties();

  List<ExifProperty> defineImageProperties();

  List<MetaData> extractImageExifMetaData(File image) throws ImageMetadataException, UnsupportedEncodingException;

  List<MetaData> extractImageExifMetaData(File image, String lang) throws ImageMetadataException, UnsupportedEncodingException;

  List<MetaData> extractImageIptcMetaData(File image) throws ImageMetadataException, UnsupportedEncodingException;

  List<MetaData> extractImageIptcMetaData(File image, String lang) throws ImageMetadataException, UnsupportedEncodingException;

}
