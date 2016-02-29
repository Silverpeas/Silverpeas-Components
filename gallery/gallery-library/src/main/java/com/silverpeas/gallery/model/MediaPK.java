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
package com.silverpeas.gallery.model;

import org.silverpeas.util.WAPrimaryKey;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * It's the Node PrimaryKey object It identify a Node
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class MediaPK extends WAPrimaryKey {

  private static final long serialVersionUID = -7700365133735243226L;

  /**
   * Constructor which set only the id
   * @since 1.0
   */
  public MediaPK(String id) {
    super(id);
  }

  /**
   * Constructor which set id, space and component name
   * @since 1.0
   */
  public MediaPK(String id, String space, String componentName) {
    super(id, space, componentName);
  }

  public MediaPK(String id, String componentId) {
    super(id, componentId);
  }

  /**
   * Constructor which set the id The WAPrimaryKey provides space and component
   * name
   * @since 1.0
   */
  public MediaPK(String id, WAPrimaryKey pk) {
    super(id, pk);
  }

  /**
   * Return the object root table name
   * @return the root table name of the object
   * @since 1.0
   */
  @Override
  public String getRootTableName() {
    return "Gallery";
  }

  /**
   * Return the object table name
   * @return the table name of the object
   * @since 1.0
   */
  @Override
  public String getTableName() {
    return "SC_Gallery_Media";
  }

  /**
   * Check if an another object is equal to this object
   * @param other the object to compare to this NodePK
   * @return true if other is equals to this object
   * @since 1.0
   */
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (getClass() != other.getClass()) {
      return false;
    }
    final MediaPK otherMediaPK = (MediaPK) other;
    EqualsBuilder matcher = new EqualsBuilder();
    matcher.append(getId(), otherMediaPK.getId());
    matcher.append(getInstanceId(), otherMediaPK.getInstanceId());
    return matcher.isEquals();
  }

  /**
   * Returns a hash code for the key
   * @return A hash code for this object
   */
  @Override
  public int hashCode() {
    HashCodeBuilder hash = new HashCodeBuilder();
    hash.append(getId());
    hash.append(getInstanceId());
    return hash.build();
  }
}