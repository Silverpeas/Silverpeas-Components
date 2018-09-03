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
import org.silverpeas.components.mydb.service.MyDBException;
import org.silverpeas.components.mydb.service.MyDBRuntimeException;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A requester of a remote enterprise data source by using the yet configured
 * {@link MyDBConnectionInfo} instance. If
 * no {@link MyDBConnectionInfo} instance is
 * available, then no connection is established and no request can be done.
 * @author mmoquillon
 */
class JdbcRequester {

  private final MyDBConnectionInfo currentConnectionInfo;

  /**
   * Constructs a new JDBC requester with the specified {@link MyDBConnectionInfo} instance.
   * @param dsInfo a {@link MyDBConnectionInfo} instance.
   */
  JdbcRequester(final MyDBConnectionInfo dsInfo) {
    this.currentConnectionInfo = dsInfo;
  }

  /**
   * Is there is a data source set with this requester? A data source is set when there is a
   * {@link MyDBConnectionInfo} instance defined for the component instance to which this
   * requester is related.
   * @return true if there is a {@link MyDBConnectionInfo} instance set with this requester.
   * False otherwise.
   */
  boolean isDataSourceDefined() {
    return currentConnectionInfo.isDefined();
  }

  /**
   * Gets all the tables in the data source (only public tables and views are fetched). If an error
   * occurs while requesting the data source, a {@link MyDBRuntimeException} is thrown.
   * @return a list of table names.
   */
  List<String> getTableNames() {
    try (Connection connection = currentConnectionInfo.openConnection()) {
      return getTableNames(connection);
    } catch (SQLException | MyDBException e) {
      throw new MyDBRuntimeException(e);
    }
  }

  /**
   * Performs the specified database operations within the same connection to the data source. If
   * no data source is defined, then a {@link MyDBRuntimeException} is thrown. If the database
   * operations throw an exception, then a {@link MyDBRuntimeException} is thrown.
   * @param operations the operation to execute on the database.
   */
  void perform(final DbOperation operations) {
    Objects.requireNonNull(operations);
    if (!isDataSourceDefined()) {
      throw new MyDBRuntimeException("No data source defined!");
    }
    try(Connection connection = currentConnectionInfo.openConnection()) {
      operations.execute(this, connection);
    } catch (Exception e) {
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

  List<DbColumn> getColumns(final Connection connection, final String tableName)
      throws SQLException {
    Objects.requireNonNull(connection);
    final List<DbColumn> dbColumns = new ArrayList<>();
    DatabaseMetaData dbMetaData = connection.getMetaData();
    ResultSet columns = dbMetaData.getColumns(null, null, tableName, null);
    while (columns.next()) {
      final String name = columns.getString("COLUMN_NAME");
      final int type = columns.getInt("DATA_TYPE");
      dbColumns.add(new DbColumn(type, name));
    }
    return dbColumns;
  }

  List<TableRow> request(final Connection connection, final String tableName, final
      AbstractColumnValuePredicate predicate) throws SQLException {
    Objects.requireNonNull(connection);
    Objects.requireNonNull(tableName);
    Objects.requireNonNull(predicate);
    JdbcSqlQuery query = JdbcSqlQuery.createSelect("*").from(tableName);
    query = predicate.apply(query);
    return query.executeWith(connection, TableRow::new);
  }

  @FunctionalInterface
  interface DbOperation {
    void execute(final JdbcRequester requester, final Connection connection) throws SQLException;
  }
}
