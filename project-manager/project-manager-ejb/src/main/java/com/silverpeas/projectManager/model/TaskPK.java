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
 * FLOSS exception.  You should have received a copy of the text describing
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

/*
 * Created on 13 avr. 2005
 *
 */
package com.silverpeas.projectManager.model;

import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * @author neysseri
 */
public class TaskPK extends WAPrimaryKey {

  private static final long serialVersionUID = 341665047380031916L;

  /**
   * TaskPK constructor
   * @param id the task identifier
   * @param componentId the component identifier
   */
  public TaskPK(String id, String componentId) {
    super(id, null, componentId);
  }

  public TaskPK(int id, String componentId) {
    super(Integer.toString(id), null, componentId);
  }

  /**
   * Implementation of equals method for TaskPK object
   */
  public boolean equals(Object other) {
    if (!(other instanceof TaskPK)) {
      return false;
    }
    return (id.equals(((TaskPK) other).getId()))
        && (componentName.equals(((TaskPK) other).getComponentName()));
  }

  /**
   * @return A hash code for this object
   */
  public int hashCode() {
    return super.hashCode();
  }
}