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

import org.silverpeas.components.mydb.model.DbColumn;
import org.silverpeas.components.mydb.model.DbTable;
import org.silverpeas.components.mydb.model.TableRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
   * Sets up the underlying filter rule to filter the specified column. If this table view isn't
   * defined or if there is no column with the given name, nothing is done.
   * @param name the name of a column.
   */
  void filterOnColumn(final String name) {
    getColumn(name).ifPresent(c -> getFilter().setColumn(c));
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
  public List<DbColumn> getColumns() {
    final List<DbColumn> columns = new ArrayList<>();
    table.ifPresent(t -> columns.addAll(t.getColumns()));
    return columns;
  }

  /**
   * Gets the names of the columns that made up the primary key (simple or composite) in this table.
   * @return a list with the primary key names or an empty list if there is no view on a given
   * database table or if there is no primary key.
   */
  public List<String> getPrimaryKeyNames() {
    final List<String> colNames = new ArrayList<>();
    table.ifPresent(t -> colNames.addAll(t.getColumns()
        .stream()
        .filter(DbColumn::isPrimaryKey)
        .map(DbColumn::getName)
        .collect(Collectors.toList())));
    return colNames;
  }

  /**
   * Gets the specified column in this table view. If the table view isn't defined or if the column
   * doesn't exist, then nothing is returned ({@link Optional#empty()}
   * @param name the name of a column.
   * @return optionally the {@link DbColumn} instance with the specified name. If no such column
   * exist or if this table view isn't defined, then nothing is returned.
   */
  public Optional<DbColumn> getColumn(final String name) {
    return table.flatMap(t -> t.getColumn(name));
  }

  /**
   * Gets the rows without applying any filter.
   * @return the rows (not filtered).
   */
  public List<TableRow> getRows() {
    final List<TableRow> rows = new ArrayList<>();
    table.ifPresent(t -> rows.addAll(applyFilter(t)));
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
   * Deletes the specified rows in the database table. The deletion is propagated to the wrapped
   * database table.
   * <p>
   * If this view in on no database table, then nothing is done.
   * </p>
   * @param rowIdx the index of the row in the list of the table's rows returned by the
   * {@link TableView#getRows()} method that takes into account the filtering criteria.
   */
  public void deleteRow(final int rowIdx) {
    table.ifPresent(t -> {
      TableRow row = applyFilter(t).get(rowIdx);
      t.delete(row);
    });
  }

  /**
   * Updates the row at the specified index with the given {@link TableRow} instance.
   * @param rowIdx the index of the row in the list of the table's row returned by the
   * {@link TableView#getRows()} method that takes into account the filtering criteria.
   * @param row the new row with which the table row at the given index has to be updated.
   */
  public void updateRow(final int rowIdx, final TableRow row) {
    table.ifPresent(t -> {
      TableRow previous = applyFilter(t).get(rowIdx);
      t.update(previous, row);
    });
  }

  private List<TableRow> applyFilter(final DbTable table) {
    return table.getRows(getFilter().getFilteringPredicate());
  }
}
