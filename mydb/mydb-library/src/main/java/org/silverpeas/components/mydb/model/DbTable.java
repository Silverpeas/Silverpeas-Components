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

import org.silverpeas.components.mydb.service.MyDBException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Table loaded from the database referred by a {@link MyDBConnectionInfo} instance.
 * @author mmoquillon
 */
public class DbTable {

  private final String name;
  private final List<String> columns;
  private List<TableRow> rows;

  /**
   * Loads the default table defined in the specified {@link MyDBConnectionInfo} instance.
   * If no default table is defined then nothing is returned.
   * @param dsInfo the {@link MyDBConnectionInfo} instance with information to access the
   * database and to get the name of the table load.
   * @return optionally a {@link DbTable} instance or nothing if no default table is set in the
   * specified {@link MyDBConnectionInfo} instance.
   * @throws MyDBException if an error occurs while requesting the data source.
   */
  public static Optional<DbTable> defaultTable(final MyDBConnectionInfo dsInfo) throws MyDBException {
    final JdbcRequester requester = new JdbcRequester(dsInfo);
    return requester.loadTable();
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
   * Constructs a database table with the specified name and with the names of the columns that
   * made up it.
   * @param name the name of the table.
   * @param columns the names of the columns of the table.
   */
  DbTable(final String name, final List<String> columns) {
    this.name = name;
    this.columns = columns;
  }

  /**
   * Constructs a database table with the specified name. Its content isn't loaded.
   * @param name the name of the table.
   */
  public DbTable(final String name) {
    this.name = name;
    this.columns = new ArrayList<>();
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
   * @return a list of column names.
   */
  public List<String> getColumns() {
    return this.columns;
  }

  /**
   * Gets the contents of this table as a list of rows, each of them being a tuple valuing the
   * all the columns of this table. If this content of this table wasn't set or if the table is
   * empty, then an empty list is returned.
   * @return a list of table rows.
   */
  public List<TableRow> getContent() {
    if (this.rows == null) {
      return Collections.emptyList();
    }
    return this.rows;
  }

  /**
   * Sets the content of this table with the specified list of rows. If this table hasn't its
   * columns defined, then they are defined from the specified rows.
   * @param rows a list of {@link TableRow} instances, each of them representing a row in the table.
   */
  void setContent(final List<TableRow> rows) {
    this.rows = rows;
    if (this.columns.isEmpty() && !this.rows.isEmpty()) {
      this.columns.addAll(this.rows.get(0).getFieldNames());
    }
  }
}
  