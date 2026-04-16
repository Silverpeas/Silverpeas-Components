/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.gallery.model;

import org.silverpeas.components.gallery.process.media.GalleryLoadMetaDataProcess;
import org.silverpeas.kernel.logging.SilverLogger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * A set of metadata embedded in a media.
 *
 * @author mmoquillon
 */
public class MetaDataSet implements Serializable {

  private final LinkedHashMap<String, MetaData> metaData = new LinkedHashMap<>();

  MetaDataSet(InternalMedia media) {
    try {
      GalleryLoadMetaDataProcess.load(media);
    } catch (Exception e) {
      SilverLogger.getLogger(this).warn(e);
    }
  }

  MetaDataSet(MetaDataSet metaData) {
    this.metaData.putAll(metaData.metaData);
  }

  /**
   * Adds a metadata.
   *
   * @param data a metadata.
   */
  public void addMetaData(MetaData data) {
    metaData.put(data.getProperty(), data);
  }

  /**
   * Gets a metadata according to the specified property name.
   *
   * @param property the property name for which the metadata is requested.
   * @return the metadata if it exists, null otherwise.
   */
  public MetaData getMetaData(String property) {
    return metaData.get(property);
  }

  /**
   * Gets all metadata property names.
   *
   * @return the list of metadata property names, empty list if no metadata.
   */
  public Collection<String> getMetaDataProperties() {
    Collection<MetaData> values = metaData.values();
    Collection<String> properties = new ArrayList<>();
    for (MetaData meta : values) {
      if (meta != null) {
        properties.add(meta.getProperty());
      }
    }
    return properties;
  }
}
  