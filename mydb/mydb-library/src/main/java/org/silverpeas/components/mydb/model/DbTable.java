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

package org.silverpeas.components.mydb.model;

import org.silverpeas.components.mydb.model.predicates.AbstractColumnValuePredicate;
import org.silverpeas.components.mydb.model.predicates.ColumnValuePredicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Table loaded from the database referred by a {@link MyDBConnectionInfo} instance.
 * @author mmoquillon
 */
public class DbTable {

  private final String name;
  private final List<DbColumn> columns = new ArrayList<>();
  private JdbcRequester requester = null;

  /**
   * Loads the default table defined in the specified {@link MyDBConnectionInfo} instance.
   * If no default table is defined then nothing is returned.
   * @param dsInfo the {@link MyDBConnectionInfo} instance with information to access the
   * database and to get the name of the table load.
   * @return optionally a {@link DbTable} instance or nothing if no default table is set in the
   * specified {@link MyDBConnectionInfo} instance.
   */
  public static Optional<DbTable> defaultTable(final MyDBConnectionInfo dsInfo) {
    DbTable table = null;
    if (dsInfo.isDefaultTableNameDefined()) {
      table = new DbTable(dsInfo.getDefaultTableName(), dsInfo);
    }
    return Optional.ofNullable(table);
  }

  /**
   * Gets a listing of the names of all the business tables in the database. The database is
   * identified by the specified {@link MyDBConnectionInfo} instance.
   * (The system and technical tables aren't get.)
   * @param dsInfo the {@link MyDBConnectionInfo} instance with information to access the
   * database.
   * @return a list with the name of all the business tables in the database.
   */
  public static List<String> list(final MyDBConnectionInfo dsInfo) {
    final JdbcRequester requester = new JdbcRequester(dsInfo);
    return requester.getTableNames();
  }

  /**
   * Constructs a new instance for a table with the specified name and that is defined in the
   * database referenced by the specified {@link MyDBConnectionInfo} object.
   * @param name the name of the table
   * @param ds the information about the database in which is defined this table.
   */
  public DbTable(final String name, final MyDBConnectionInfo ds) {
    this.name = name;
    setJdbcRequester(new JdbcRequester(ds));
  }

  /**
   * Constructs a database table with the specified name. Its content isn't loaded.
   * @param name the name of the table.
   */
  public DbTable(final String name) {
    this.name = name;
    this.columns.clear();
  }

  /**
   * Sets a requester to access the database and performs JDBC operations for this table. Once
   * a requester set, the columns of the table are automatically fetched.
   * @param requester a {@link JdbcRequester} instance.
   */
  private void setJdbcRequester(final JdbcRequester requester) {
    this.requester = requester;
    this.requester.perform((r, c) -> {
      this.columns.clear();
      this.columns.addAll(r.getColumns(c, this.name));
    });
  }

  /**
   * Gets this table's name.
   * @return the name of this table.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets all the columns that made up this table. If no columns were specified for this table,
   * then an empty list is returned.
   * @return a list of columns.
   */
  public List<DbColumn> getColumns() {
    return this.columns;
  }

  /**
   * Gets the column with the specified name.
   * @param name the name of the column.
   * @return optionally the asked column. If no such column exists then an empty optional is
   * returned.
   */
  public Optional<DbColumn> getColumn(final String name) {
    return this.columns.stream().filter(c -> c.name.equals(name)).findFirst();
  }

  /**
   * Gets the contents of this table as a list of rows, each of them being a tuple valuing the
   * all the columns of this table. If the table is empty, then an empty list is returned.
   * If this table hasn't yet its columns defined, then they are defined from the specified rows.
   * @param filter a predicate to use for filtering the table content.
   * @return a list of table rows. If a filter is set, it is then applied when requesting the
   * content of this table. The number of table rows is limited by the
   * {@link MyDBConnectionInfo#getDataMaxNumber()} property.
   */
  public List<TableRow> getContent(final ColumnValuePredicate filter) {
    final List<TableRow> rows = new ArrayList<>();
    if (!(filter instanceof AbstractColumnValuePredicate)) {
      throw new IllegalArgumentException(
          "DbTable doesn't support predicate other than AbstractColumnValuePredicate objects");
    }
    requester.perform(
        (r, c) -> rows.addAll(r.request(c, this.name, (AbstractColumnValuePredicate) filter)));
    if (this.columns.isEmpty() && !rows.isEmpty()) {
      this.columns.addAll(rows.get(0)
          .getFields()
          .entrySet()
          .stream()
          .map(e -> new DbColumn(e.getValue().getType(), e.getKey()))
          .collect(Collectors.toList()));
    }
    return rows;
  }
}
  