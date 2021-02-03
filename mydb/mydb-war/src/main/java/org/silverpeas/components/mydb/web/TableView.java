/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.components.mydb.model.DbColumn;
import org.silverpeas.components.mydb.model.DbTable;
import org.silverpeas.components.mydb.model.TableRow;
import org.silverpeas.components.mydb.service.MyDBRuntimeException;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.SilverpeasArrayList;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.webcomponent.WebMessager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.text.MessageFormat.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.silverpeas.components.mydb.web.TableRowUIEntity.convertList;
import static org.silverpeas.core.util.Mutable.empty;

/**
 * A view on one given database table loaded from a data source. The view wraps the table and it
 * provides filtering operations on its content. It is a container on zero or one table and hence
 * its single item can be changed.
 * @author mmoquillon
 */
public class TableView {

  private final Map<Integer, Pair<String, String>> orderBies = new HashMap<>(50);
  private Optional<DbTable> table = Optional.empty();
  private TableRowsFilter filter = new TableRowsFilter();
  private String orderBy = null;
  private PaginationPage pagination = null;
  private SilverpeasList<TableRowUIEntity> lastRows = SilverpeasList.wrap(emptyList());

  /**
   * Constructs an empty table view. This view is on nothing.
   */
  TableView() {
  }

  public PaginationPage getPagination() {
    return pagination;
  }

  void setPagination(final PaginationPage pagination) {
    this.pagination = pagination;
  }

  Map<Integer, Pair<String, String>> getOrderBies() {
    return orderBies;
  }

  void setOrderBy(final String orderBy) {
    this.orderBy = orderBy;
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
    orderBies.clear();
    table = Optional.empty();
    filter.clear();
    orderBy = null;
    lastRows = SilverpeasList.wrap(emptyList());
  }

  /**
   * Sets the view on the specified database table.
   * @param table the table on which this view will be defined.
   */
  void setTable(final Optional<DbTable> table) {
    clear();
    this.table = table;
    int i = 1;
    for (final DbColumn column : getColumns()) {
      orderBies.put(i++, Pair.of(column.getName() + " asc", column.getName() + " desc"));
    }
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
   * Gets the specified column in this table view. If the table view isn't defined or if the column
   * doesn't exist, then nothing is returned ({@link Optional#empty()}
   * @param name the name of a column.
   * @return optionally the {@link DbColumn} instance with the specified name. If no such column
   * exist or if this table view isn't defined, then nothing is returned.
   */
  Optional<DbColumn> getColumn(final String name) {
    return table.flatMap(t -> t.getColumn(name));
  }

  /**
   * Gets the rows without applying any filter.
   * @return the rows.
   */
  public SilverpeasList<TableRowUIEntity> getRows() {
    final Mutable<SilverpeasList<TableRow>> rows = empty();
    try {
      table.ifPresent(t -> rows.set(applyFilter(t)));
    } catch (final MyDBRuntimeException e) {
      WebMessager.getInstance().addSevere(e.getMessage());
      SilverLogger.getLogger(this).error(e);
    }
    lastRows = convertList(this, rows.orElseGet(SilverpeasArrayList::new), emptySet());
    return lastRows;
  }

  /**
   * Gets the last rows loaded by {@link #getRows()}.
   * @return the last loaded rows.
   */
  public SilverpeasList<TableRowUIEntity> getLastRows() {
    return lastRows;
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
   * @param uiRowId the UI row id of the row in the list of the table's rows returned by the
   * {@link TableView#getRows()} method that takes into account the filtering criteria.
   */
  long deleteRow(final String uiRowId) {
    final Mutable<Long> result = empty();
    table.ifPresent(t -> {
      final TableRow previousRow = getTableRowFromUiId(uiRowId);
      result.set(t.delete(previousRow));
    });
    return result.orElse(0L);
  }

  /**
   * Updates the row at the specified index with the given {@link TableRow} instance.
   * @param uiRowId the UI row id of the row in the list of the table's row returned by the
   * {@link TableView#getRows()} method that takes into account the filtering criteria.
   * @param row the new row with which the table row at the given index has to be updated.
   */
  long updateRow(final String uiRowId, final TableRow row) {
    final Mutable<Long> result = empty();
    table.ifPresent(t -> {
      final TableRow previousRow = getTableRowFromUiId(uiRowId);
      if (previousRow == row) {
        throw new IllegalArgumentException("the row with new values must be a copy of previous row");
      }
      result.set(t.update(previousRow, row));
    });
    return result.orElse(0L);
  }

  private TableRow getTableRowFromUiId(final String uiRowId) {
    return lastRows.stream()
        .filter(r -> r.getId().equals(uiRowId))
        .map(TableRowUIEntity::getData)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
            format("previous row with UI ID {0} has not been found", uiRowId)));
  }

  /**
   * Adds the specified row into the database table on which this view is.
   * @param row the {@link TableRow} instance to add.
   */
  void addRow(final TableRow row) {
    table.ifPresent(t -> t.add(row));
  }

  private SilverpeasList<TableRow> applyFilter(final DbTable table) {
    return table.getRows(getFilter().getFilteringPredicate(), orderBy , pagination);
  }
}
