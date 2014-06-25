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
package com.silverpeas.gallery.model;

import com.silverpeas.gallery.ImageType;
import com.silverpeas.gallery.constant.MediaType;
import com.silverpeas.gallery.process.photo.GalleryLoadMetaDataProcess;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class represents a Photo.
 */
public class Photo extends InternalMedia {
  private static final long serialVersionUID = 262504401033860860L;

  private int resolutionH = 0;
  private int resolutionW = 0;
  private LinkedHashMap<String, MetaData> metaData = null;

  @Override
  public MediaType getType() {
    return MediaType.Photo;
  }

  /**
   * Gets the height of the resolution.
   * @return the height of the resolution.
   */
  public int getResolutionH() {
    return resolutionH;
  }

  /**
   * Sets the height of the resolution.
   * @param resolutionH the height of the resolution.
   */
  public void setResolutionH(int resolutionH) {
    this.resolutionH = resolutionH;
  }

  /**
   * Gets the width of the resolution.
   * @return the width of the resolution.
   */
  public int getResolutionW() {
    return resolutionW;
  }

  /**
   * Sets the width of the resolution.
   * @param resolutionW the width of the resolution.
   */
  public void setResolutionW(int resolutionW) {
    this.resolutionW = resolutionW;
  }

  private Map<String, MetaData> getAllMetaData() {
    if (metaData == null) {
      metaData = new LinkedHashMap<String, MetaData>();
      try {
        GalleryLoadMetaDataProcess.load(this);
      } catch (Exception e) {
        SilverTrace.error("gallery", "Media.getAllMetaData", "gallery.MSG_NOT_ADD_METADATA",
            "photoId =  " + getId());
      }
    }
    return metaData;
  }

  /**
   * Adds a metadata.
   * @param data a metadata.
   */
  public void addMetaData(MetaData data) {
    getAllMetaData().put(data.getProperty(), data);
  }

  /**
   * Gets a metadata according to the specified property name.
   * @param property the property name for which the metadata is requested.
   * @return the metadata if it exists, null otherwise.
   */
  public MetaData getMetaData(String property) {
    return getAllMetaData().get(property);
  }

  /**
   * Gets all metadata property names.
   * @return the list of metadata property names, empty list if no metadata.
   */
  public Collection<String> getMetaDataProperties() {
    Collection<MetaData> values = getAllMetaData().values();
    Collection<String> properties = new ArrayList<String>();
    for (MetaData meta : values) {
      if (meta != null) {
        properties.add(meta.getProperty());
      }
    }
    return properties;
  }

  /**
   * The type of this resource
   * @return the same value returned by getContributionType()
   */
  public static String getResourceType() {
    return MediaType.Photo.name();
  }

  @Override
  public boolean isPreviewable() {
    return ImageType.isPreviewable(getFileName());
  }
}