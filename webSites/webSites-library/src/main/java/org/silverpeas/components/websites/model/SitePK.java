/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * SitePK.java
 *
 * Created on 9 Avril 2001, 16:40
 */
package org.silverpeas.components.websites.model;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.core.ResourceReference;

import java.io.Serializable;

/**
 * Primary key of a site.
 */
public class SitePK extends ResourceReference implements Serializable {

  private static final long serialVersionUID = 3986172113206842129L;

  public SitePK(String id, String componentName) {
    super(id, componentName);
  }

  @Override
  public String getRootTableName() {
    return "site";
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof SitePK)) {
      return false;
    }
    return (id.equals(((SitePK) other).getId())) && (space.equals(((SitePK) other).getSpace())) &&
        (componentName.equals(((SitePK) other).getComponentName()));
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(7, 13).append(getId()).append(getSpace()).append(getComponentName())
        .toHashCode();
  }
}