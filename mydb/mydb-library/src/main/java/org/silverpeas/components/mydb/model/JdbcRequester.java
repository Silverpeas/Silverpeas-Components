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
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.TransactionRuntimeException;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
  private String orderBy = "";

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
  private boolean isDataSourceDefined() {
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
   * Performs the specified database operations within the same connection with the data source and
   * within a same transaction. If no data source is defined, then a {@link MyDBRuntimeException}
   * is thrown. If the database * operations throw an exception, then a
   * {@link MyDBRuntimeException} is thrown.
   * @param operations the operation to execute on the database.
   */
  <T> T perform(final DbOperation<T> operations) {
    Objects.requireNonNull(operations);
    if (!isDataSourceDefined()) {
      throw new MyDBRuntimeException("No data source defined!");
    }
    try {
      return Transaction.performInOne(() -> {
        try (Connection connection = currentConnectionInfo.openConnection()) {
          return operations.execute(this, connection);
        }
      });
    } catch (TransactionRuntimeException e) {
      throw new MyDBRuntimeException(e);
    }
  }

  private List<String> getTableNames(final Connection connection) throws SQLException {
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
   * Gets the definition of all the columns of the specified database table and passes them to the
   * specified {@link ColumnConsumer} function.
   * @param connection a connection to the database.
   * @param tableName the name of the table from which the columns have to be get.
   * @param consumer the consumer of the column definitions.
   * @throws SQLException if an error occurs while requesting the database.
   */
  void loadColumns(final Connection connection, final String tableName,
      final ColumnConsumer consumer) throws SQLException {
    Objects.requireNonNull(connection);
    final List<String> columnPks = new ArrayList<>(2);
    DatabaseMetaData dbMetaData = connection.getMetaData();
    ResultSet primaryKeys =
        dbMetaData.getPrimaryKeys(connection.getCatalog(), connection.getSchema(), tableName);
    while (primaryKeys.next()) {
      final String pk = primaryKeys.getString("COLUMN_NAME");
      columnPks.add(pk);
    }
    ResultSet columns = dbMetaData.getColumns(connection.getCatalog(), null, tableName, null);
    while (columns.next()) {
      final String name = columns.getString("COLUMN_NAME");
      final int type = columns.getInt("DATA_TYPE");
      final int size = columns.getInt("COLUMN_SIZE");
      final boolean isPrimaryKey = columnPks.contains(name);
      consumer.accept(name, type, size, isPrimaryKey);
    }
  }

  /**
   * Requests the content of the specified table by applying the given predicate and uses the
   * specified converters to convert each row and each row's value to their corresponding business
   * object, ready to be handled by the caller.
   * @param connection a connection to the database.
   * @param tableName the name of the table to request.
   * @param predicate a predicate to use to filter the table's content.
   * @param converters the converters to use to convert each row and each row's value to a business
   * object.
   * @param <V> the type of the business objects representing the row's values.
   * @param <R> the type of the business objects representing the rows.
   * @return a list of rows, matching the given predicate, in their business representation. If no
   * rows match the specified predicate or if the table is empty, an empty list is returned.
   * @throws SQLException if an error occurs while requesting the database.
   */
  <V, R> List<R> request(final Connection connection, final String tableName,
      final AbstractColumnValuePredicate predicate, final DataConverters<V, R> converters)
      throws SQLException {
    Objects.requireNonNull(connection);
    Objects.requireNonNull(tableName);
    Objects.requireNonNull(predicate);
    JdbcSqlQuery query = JdbcSqlQuery.createSelect("*").from(tableName);
    query = predicate.apply(query).orderBy(this.orderBy);
    return query.executeWith(connection, rs -> {
      try {
        final Map<String, V> row = new LinkedHashMap<>();
        ResultSetMetaData rsMetaData = rs.getMetaData();
        for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
          V value = converters.getValueConverter()
              .convert(rs.getObject(i), rs.getMetaData().getColumnType(i));
          row.put(rsMetaData.getColumnName(i), value);
        }
        return converters.getRowConverter().convert(row);
      } catch (SQLException e) {
        throw new MyDBRuntimeException(e);
      }
    });
  }

  /**
   * Deletes all the rows in the specified table name that match the specified predicate. If no
   * criteria is given, then the table is emptied. If the table is already empty, nothing is done.
   * @param connection a connection to the database.
   * @param tableName the name of the table in which some of the rows have to be deleted.
   * @param criteria the criteria the rows to delete have to match.
   * @throws SQLException if an error occurs while deleting the rows in the specified table.
   */
  void delete(final Connection connection, final String tableName,
      final Map<String, Object> criteria) throws SQLException {
    final JdbcSqlQuery query = JdbcSqlQuery.createDeleteFor(tableName);
    applyCriteria(query, criteria)
        .executeWith(connection);
  }

  /**
   * Updates the rows in the specified table that match the given criteria with the values defined
   * in the specified dictionary <code>values</code>.
   * @param connection a connection to the database.
   * @param tableName the name of the table in which the rows will be updated.
   * @param values the values with which the rows will be updated.
   * @param criteria the criteria the rows have to match.
   * @throws SQLException if an error occurs while updating the rows in the specified table.
   */
  void update(final Connection connection, final String tableName, final Map<String, Object> values,
      Map<String, Object> criteria) throws SQLException {
    JdbcSqlQuery query = JdbcSqlQuery.createUpdateFor(tableName);
    values.forEach(query::addUpdateParam);
    applyCriteria(query, criteria).executeWith(connection);
  }

  /**
   * Inserts into the specified table a new row with the specified values.
   * @param connection a connection to the database.
   * @param tableName the name of the table into which the row will be inserted.
   * @param values the values of the row to insert as a map of column names to column values.
   */
  void insert(final Connection connection, final String tableName, final Map<String, Object> values)
      throws SQLException {
    JdbcSqlQuery query = JdbcSqlQuery.createInsertFor(tableName);
    values.forEach(query::addInsertParam);
    query.executeWith(connection);
  }

  private JdbcSqlQuery applyCriteria(final JdbcSqlQuery query, final Map<String, Object> criteria) {
    final StringBuilder clauses = new StringBuilder(criteria.size());
    final List<Object> values = new ArrayList<>(criteria.size());
    final String conjunction = " and ";
    criteria.forEach((key, value) -> {
      clauses.append(key).append(" = ?").append(conjunction);
      values.add(value);
    });
    clauses.setLength(clauses.length() - conjunction.length());
    return query.where(clauses.toString(), values);
  }

  /**
   * A provider of converters of data. They convert database data to their corresponding business
   * data.
   * @param <V> the business type representing a column's value in a given row of a database table.
   * @param <R> the business type representing a given row of a database table.
   */
  static class DataConverters<V, R> {
    private final ValueConverter<V> valueConverter;
    private final RowConverter<V, R> rowConverter;

    DataConverters(final ValueConverter<V> valueConverter, final RowConverter<V, R> rowConverter) {
      this.valueConverter = valueConverter;
      this.rowConverter = rowConverter;
    }

    /**
     * Converter of a database table row's value to a business object representing that value.
     * @return a {@link ValueConverter} instance.
     */
    ValueConverter<V> getValueConverter() {
      return valueConverter;
    }

    /**
     * Converter of a database table row to a business object representing that row.
     * @return a {@link RowConverter} instance.
     */
    RowConverter<V, R> getRowConverter() {
      return rowConverter;
    }
  }

  @FunctionalInterface
  interface DbOperation<T> {
    T execute(final JdbcRequester requester, final Connection connection) throws SQLException;
  }

  @FunctionalInterface
  interface ColumnConsumer {
    void accept(final String name, final int type, final int size, final boolean primaryKey);
  }

  @FunctionalInterface
  interface RowConverter<V, R> {
    R convert(final Map<String, V> row);
  }

  @FunctionalInterface
  interface ValueConverter<V> {
    V convert(final Object value, final int valueType);
  }
}
