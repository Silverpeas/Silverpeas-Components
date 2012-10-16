/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.mydb.model;

import java.io.Serializable;

import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * Primary key for a MyDB database connection.
 * @author Antoine HEDIN
 */
public class MyDBConnectionInfoPK extends WAPrimaryKey implements Serializable {

  private static final long serialVersionUID = 1L;

  public MyDBConnectionInfoPK(String id) {
    super(id);
  }

  public MyDBConnectionInfoPK(String id, WAPrimaryKey pk) {
    super(id, pk);
  }

  public MyDBConnectionInfoPK(String id, String space, String componentName) {
    super(id, space, componentName);
  }

  public String getRootTableName() {
    return "MyDBConnectionInfo";
  }

  public String getTableName() {
    return "SC_MyDB_ConnectInfo";
  }

  public boolean equals(Object other) {
    return ((other instanceof MyDBConnectionInfoPK)
        && (id.equals(((MyDBConnectionInfoPK) other).getId()))
        && (space.equals(((MyDBConnectionInfoPK) other).getSpace())) && (componentName
        .equals(((MyDBConnectionInfoPK) other).getComponentName())));
  }

  public int hashCode() {
    return toString().hashCode();
  }

}