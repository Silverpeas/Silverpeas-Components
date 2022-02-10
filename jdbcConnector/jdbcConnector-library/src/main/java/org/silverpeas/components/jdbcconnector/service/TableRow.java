/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.jdbcconnector.service;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A row in a table in a data source. A row is a tuple whose each field matches a given column of
 * the requested table. Hence the field name in the row is the column name and its value the value
 * in the column.
 * @author mmoquillon
 */
public class TableRow {

  private final Map<String, Comparable> fields = new LinkedHashMap<>();

  /**
   * Constructs a {@link TableRow} from the specified {@link ResultSet}.
   * @param rs a {@link ResultSet} returned by a SQL query.
   */
  TableRow(final ResultSet rs) {
    try {
      ResultSetMetaData rsMetaData = rs.getMetaData();
      for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
        Object value = rs.getObject(i);
        Comparable valueToStore;
        if (value == null) {
          valueToStore = null;
        } else if (value instanceof Comparable) {
          valueToStore = (Comparable) value;
        } else {
          valueToStore = new TableFieldValue(value);
        }
        this.fields.put(rsMetaData.getColumnName(i), valueToStore);
      }
    } catch (SQLException e) {
      throw new JdbcConnectorRuntimeException(e);
    }
  }

  /**
   * Gets the name of all the fields in this table row.
   * @return a list with the name of all of the fields in this table row.
   */
  public List<String> getFieldNames() {
    return new ArrayList<>(fields.keySet());
  }

  /**
   * Gets all the fields of this table row.
   * @return a {@link Map} between a field name and its value.
   */
  public Map<String, Object> getFields() {
    return Collections.unmodifiableMap(fields);
  }

  /**
   * Gets the value of the specified field in this table row.
   * @param field the name of the field.
   * @return the value of the asked field.
   */
  public Comparable getFieldValue(final String field) {
    return fields.get(field);
  }
}
  