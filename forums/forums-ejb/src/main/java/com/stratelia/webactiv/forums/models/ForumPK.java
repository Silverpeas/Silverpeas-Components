/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.stratelia.webactiv.forums.models;

import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * Clé primaire associée à un forum.
 * @author frageade
 * @since November 2000
 */
public class ForumPK extends WAPrimaryKey {

  /**
   * Generated serial version identifier (Serializable class)
   */
  private static final long serialVersionUID = -6924058189303890284L;

  public ForumPK(String component, String id) {
    super(id, component);
  }

  public ForumPK(String component) {
    this(component, "0");
  }

  @Override
  public boolean equals(Object other) {
    return ((other instanceof ForumPK)
        && (getInstanceId().equals(((ForumPK) other).getInstanceId()))
        && (getId().equals(((ForumPK) other).getId())));
  }

  @Override
  public int hashCode() {
    int hash = 7;
    return hash * super.hashCode();
  }

}