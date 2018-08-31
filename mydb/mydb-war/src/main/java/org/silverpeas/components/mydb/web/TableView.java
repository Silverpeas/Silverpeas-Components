/*
 * Copyright (C) 2000 - 2018 Silverpeas
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

package org.silverpeas.components.mydb.web;

import org.silverpeas.components.mydb.model.DbTable;
import org.silverpeas.components.mydb.model.TableRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A view on one given database table loaded from a data source. The view wraps the table and it
 * provides filtering operations on its content. It is a container on zero or one table and hence
 * its single item can be changed.
 * @author mmoquillon
 */
public class TableView {

  private Optional<DbTable> table = Optional.empty();
  private TableRowsFilter filter = new TableRowsFilter();

  /**
   * Constructs an empty table view. This view is on nothing.
   */
  TableView() {
  }

  /**
   * Gets the filter to apply on the table rows.
   * @return a {@link TableRowsFilter} instance.
   */
  TableRowsFilter getFilter() {
    return filter;
  }

  /**
   * Clears this table view.
   */
  void clear() {
    table = Optional.empty();
    filter.clear();
  }

  /**
   * Sets the view on the specified database table.
   * @param table the table on which this view will be defined.
   */
  void setTable(final Optional<DbTable> table) {
    this.table = table;
  }

  /**
   * Gets the name of the table.
   * @return the name of the table.
   */
  public String getName() {
    String name = "";
    if (table.isPresent()) {
      name = table.get().getName();
    }
    return name;
  }

  /**
   * Gets field names extracted from last query result set.
   * @return the field names.
   */
  public List<String> getColumns() {
    final List<String> columnNames = new ArrayList<>();
    table.ifPresent(t -> columnNames.addAll(t.getColumns()));
    return columnNames;
  }

  /**
   * Gets the rows after the applying the filter {@link TableRowsFilter#filter(List)}.
   * @return the filtered rows.
   */
  public List<TableRow> getFilteredRows() {
    return filter.filter(getRows());
  }

  /**
   * Gets the rows without applying any filter.
   * @return the rows (not filtered).
   */
  public List<TableRow> getRows() {
    final List<TableRow> rows = new ArrayList<>();
    table.ifPresent(t -> rows.addAll(t.getContent()));
    return rows;
  }

  /**
   * Indicates if the wrapped table is empty without taking into account any filtering.
   * @return true if the table has rows, false otherwise.
   */
  public boolean isEmpty() {
    return getRows().isEmpty();
  }

  /**
   * Is this view is on an existing database table or the view is empty.
   * @return true if the database table wrapped by this view is defined. False otherwise.
   */
  public boolean isDefined() {
    return this.table.isPresent();
  }

  /**
   * Gets the first non null value of the column with the specified name from all the rows of the
   * table.
   * @param columnName a column name.
   * @return an optional value.
   */
  Optional<Object> getFirstNonNullValue(final String columnName) {
    return getRows().stream()
        .map(r -> (Object) r.getFieldValue(columnName))
        .filter(Objects::nonNull)
        .findFirst();
  }
}
