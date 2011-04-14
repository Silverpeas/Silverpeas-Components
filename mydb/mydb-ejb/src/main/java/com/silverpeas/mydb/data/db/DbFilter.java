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

package com.silverpeas.mydb.data.db;

import java.sql.Types;

/**
 * Database data filter.
 * @author Antoine HEDIN
 */
public class DbFilter {

  public static final String ALL = "*";
  public static final String CONTAINS = "CONTAINS";
  public static final String[] COMPARES_SYMBOLS = { "=", "<=", ">=", "!=", ">",
      "<" };

  private String column;
  private String compare;
  private String value;
  private boolean manualFilter;

  public DbFilter() {
    column = ALL;
    compare = ALL;
    value = "";
    manualFilter = false;
  }

  public DbFilter(String column, String compare, String value) {
    if (column.equals(ALL) || compare.equals(ALL) || value.equals("")) {
      this.column = ALL;
      this.compare = ALL;
      this.value = "";
    } else {
      this.column = column;
      this.compare = compare;
      this.value = value;
    }
    manualFilter = false;
  }

  public String getColumn() {
    return column;
  }

  public String getCompare() {
    return compare;
  }

  public String getValue() {
    return value;
  }

  public boolean isManualFilter() {
    return manualFilter;
  }

  public String getQueryFilter(DbTable dbTable) {
    String query = "";
    manualFilter = false;
    if ((!ALL.equals(column)) && (!ALL.equals(compare)) && (!"".equals(value))) {
      int dataType = dbTable.getColumn(column).getDataType();
      if (((dataType == Types.INTEGER) || (dataType == Types.DOUBLE) || (dataType == Types.FLOAT))
          && (compare.equals(CONTAINS))) {
        // Impossible de filtrer sous la forme 'like' avec un numerique : on
        // positionne l'indicateur de filtrage manuel.
        manualFilter = true;
      } else {
        StringBuffer queryFilter = new StringBuffer(20).append(" where ")
            .append(column);
        if (compare.equals(CONTAINS)) {
          queryFilter.append(" like '%").append(value).append("%'");
        } else {
          queryFilter.append(" ").append(compare).append(" '").append(value)
              .append("'");
        }
        query = queryFilter.toString();
      }
    }
    return query;
  }

}