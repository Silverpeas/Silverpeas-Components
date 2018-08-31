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
import org.silverpeas.components.mydb.service.MyDBRuntimeException;
import org.silverpeas.core.util.StringUtil;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A requester of a remote enterprise data source by using the yet configured
 * {@link MyDBConnectionInfo} instance. If
 * no {@link MyDBConnectionInfo} instance is
 * available, then no connection is established and no request can be done.
 * @author mmoquillon
 */
public class JdbcRequester {

  private static final MessageFormat TABLE_QUERY = new MessageFormat("SELECT * FROM {0}");

  private final MyDBConnectionInfo currentConnectionInfo;

  /**
   * Constructs a new JDBC requester with the specified {@link MyDBConnectionInfo} instance.
   * @param dsInfo a {@link MyDBConnectionInfo} instance.
   */
  public JdbcRequester(final MyDBConnectionInfo dsInfo) {
    this.currentConnectionInfo = dsInfo;
  }

  /**
   * Is there is a data source set with this requester? A data source is set when there is a
   * {@link MyDBConnectionInfo} instance defined for the component instance to which this
   * requester is related.
   * @return true if there is a {@link MyDBConnectionInfo} instance set with this requester.
   * False otherwise.
   */
  public boolean isDataSourceDefined() {
    return currentConnectionInfo.isDefined();
  }

  /**
   * Is there is any default database table set with this requester? A request is set when there
   * is a
   * {@link MyDBConnectionInfo} instance defined for the component instance to which this
   * request is related and a database table has been defined for that connection information.
   * @return true if there is a database table defined for the current underlying connection
   * information.
   */
  public boolean isDefaultTableNameDefined() {
    return StringUtil.isDefined(currentConnectionInfo.getDefaultTableName());
  }

  /**
   * Gets the name of the default database table set with this requester.
   * @return a name of the default table that was set in the underlying
   * {@link MyDBConnectionInfo} instance of this requester.
   */
  public String getDefaultTableName() {
    return currentConnectionInfo.getDefaultTableName();
  }

  /**
   * Gets all the tables in the data source (only public tables and views are fetched). If an error
   * occurs while requesting the data source, a {@link MyDBRuntimeException} is thrown.
   * @return a list of table names.
   */
  public List<String> getTableNames() {
    try (Connection connection = currentConnectionInfo.openConnection()) {
      return getTableNames(connection);
    } catch (SQLException | MyDBException e) {
      throw new MyDBRuntimeException(e);
    }
  }

  List<String> getTableNames(final Connection connection) throws SQLException {
    Objects.requireNonNull(connection);
    final List<String> tableNames = new ArrayList<>();
    DatabaseMetaData dbMetaData = connection.getMetaData();
    ResultSet tables = dbMetaData.getTables(null, null, null, new String[]{"TABLE", "VIEW"});
    while (tables.next()) {
      tableNames.add(tables.getString("TABLE_NAME"));
    }
    return tableNames;
  }


  /**
   * Gets all the columns in the specified table.
   * @param tableName a name of a table. This name must have been get by invoking
   * {@link #getTableNames()} method. If an error
   * occurs while requesting the data source, a {@link MyDBRuntimeException} is thrown.
   * @return a list of column names in the given table.
   */
  public List<String> getColumnNames(final String tableName) {
    try (Connection connection = currentConnectionInfo.openConnection()) {
      return getColumnNames(connection, tableName);
    } catch (SQLException | MyDBException e) {
      throw new MyDBRuntimeException(e);
    }
  }

  private List<String> getColumnNames(final Connection connection, final String tableName)
      throws SQLException {
    Objects.requireNonNull(connection);
    final List<String> columnNames = new ArrayList<>();
    DatabaseMetaData dbMetaData = connection.getMetaData();
    ResultSet columns = dbMetaData.getColumns(null, null, tableName, null);
    while (columns.next()) {
      columnNames.add(columns.getString("COLUMN_NAME"));
    }
    return columnNames;
  }

  private List<TableRow> request(final Connection connection, final String tableName)
      throws SQLException {
    Objects.requireNonNull(connection);
    final List<TableRow> rows = new ArrayList<>();
    try (PreparedStatement statement = connection.prepareStatement(
        TABLE_QUERY.format(new Object[]{tableName}))) {
      statement.setMaxRows(currentConnectionInfo.getDataMaxNumber());
      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          rows.add(new TableRow(rs));
        }
        return rows;
      }
    }
  }

  public Optional<DbTable> loadTable() throws MyDBException {
    DbTable table = null;
    try (final Connection connection = currentConnectionInfo.openConnection()) {
      final String tableName;
      if (isDefaultTableNameDefined()) {
        tableName = getDefaultTableName();
        table = loadTable(connection, tableName);
      }
      return Optional.ofNullable(table);
    } catch (SQLException e) {
      throw new MyDBException(e);
    }
  }

  private DbTable loadTable(final Connection connection, final String tableName)
      throws SQLException {
    Objects.requireNonNull(connection);
    if (StringUtil.isNotDefined(tableName)) {
      throw new MyDBRuntimeException("The name of the table to load isn't defined!");
    }
    if (!isDataSourceDefined()) {
      throw new MyDBRuntimeException("No data source defined!");
    }
    DbTable table;
    final List<String> columns = getColumnNames(connection, tableName);
    final List<TableRow> rows = request(connection, tableName);
    table = new DbTable(tableName, columns);
    table.setContent(rows);
    return table;
  }
}
