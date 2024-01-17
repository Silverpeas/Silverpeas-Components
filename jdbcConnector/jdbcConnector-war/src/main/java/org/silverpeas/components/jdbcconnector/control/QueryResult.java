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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.jdbcconnector.control;

import org.silverpeas.components.jdbcconnector.service.TableRow;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Handle a query result with more flexibility than handling directly a list of {@link TableRow}
 * instances.
 * @author silveryocha
 */
public class QueryResult {

  private List<TableRow> rows = Collections.emptyList();
  private TableRowsFilter filter = new TableRowsFilter();

  private List<String> fieldNames = Collections.emptyList();

  QueryResult() {
  }

  TableRowsFilter getFilter() {
    return filter;
  }

  void clear() {
    rows = Collections.emptyList();
    filter.clear();
    fieldNames = Collections.emptyList();
  }

  void setNewResult(final List<TableRow> rows) {
    this.rows = rows;
    if (existsRows() && fieldNames.isEmpty()) {
      this.fieldNames = this.rows.get(0).getFieldNames();
    }
  }

  /**
   * Gets field names extracted from last query result set.
   * @return the field names.
   */
  public List<String> getFieldNames() {
    return fieldNames;
  }

  /**
   * Gets the rows after the applying the filter {@link TableRowsFilter#filter(List)}.
   * @return the filtered rows.
   */
  public List<TableRow> getFilteredRows() {
    return filter.filter(rows);
  }

  /**
   * Gets the rows without applying any filter.
   * @return the rows (not filtered).
   */
  public List<TableRow> getRows() {
    return rows;
  }

  /**
   * Indicates if it exists rows without taking care of filtering.
   * @return true if it exists rows, false otherwise.
   */
  boolean existsRows() {
    return !rows.isEmpty();
  }

  /**
   * Gets the first non null value from rows of column represented by the given name.
   * @param fieldName the column field name.
   * @return an optional value.
   */
  Optional<Object> getFirstNonNullValueOfColumn(final String fieldName) {
    return rows.stream()
               .map(r -> (Object) r.getFieldValue(fieldName))
               .filter(Objects::nonNull)
               .findFirst();
  }
}
