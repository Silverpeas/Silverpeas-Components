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
 * FLOSS exception.  You should have received a copy of the text describing
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
package com.silverpeas.dataWarning;

public class DataWarningDBDriver {

  private String driverUniqueID = "";
  private String driverName = "";
  private String className = "";
  private String description = "";
  private String jdbcUrl = null;

  public DataWarningDBDriver(String dui, String dn, String cn, String d, String du) {
    driverUniqueID = dui;
    driverName = dn;
    className = cn;
    description = d;
    jdbcUrl = du;
  }

  public String getClassName() {
    return className;
  }

  public String getDescription() {
    return description;
  }

  public String getDriverName() {
    return driverName;
  }

  public String getDriverUniqueID() {
    return driverUniqueID;
  }

  public String getJdbcUrl() {
    return jdbcUrl;
  }
}
