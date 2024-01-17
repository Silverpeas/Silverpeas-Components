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

package org.silverpeas.components.mydb.model;

import org.silverpeas.components.mydb.model.predicates.AbstractColumnValuePredicate;
import org.silverpeas.components.mydb.service.MyDBException;
import org.silverpeas.components.mydb.service.MyDBRuntimeException;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.TransactionRuntimeException;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.annotation.Nonnull;
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
      String table = tables.getString("TABLE_NAME");
      if (isAuthorizedTable(connection, table)) {
        tableNames.add(table);
      }
    }
    return tableNames;
  }

  private boolean isAuthorizedTable(final Connection connection, String tableName) {
    try {
      JdbcSqlQuery.createCountFor(tableName).executeWith(connection);
      return true;
    } catch (SQLException e) {
      return false;
    }
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
    final DatabaseMetaData dbMetaData = connection.getMetaData();
    final List<String> columnPks = getPrimaryKeys(connection, tableName, dbMetaData);
    final Map<String, ForeignKeyDescriptor> columnFks =
        getForeignKeys(connection, tableName, dbMetaData);
    final ResultSet columns = dbMetaData.getColumns(connection.getCatalog(), null, tableName, null);
    while (columns.next()) {
      final String name = columns.getString("COLUMN_NAME");
      final int type = columns.getInt("DATA_TYPE");
      final int size = columns.getInt("COLUMN_SIZE");
      final boolean isAutoIncremented =
          StringUtil.getBooleanValue(columns.getString("IS_AUTOINCREMENT"));
      final boolean isNullable = StringUtil.getBooleanValue(columns.getString("IS_NULLABLE"));
      final boolean isPrimaryKey = columnPks.contains(name);
      final DefaultValue defaultValue = getDefaultValue(columns);
      consumer.accept(new ColumnDescriptor().withName(name)
          .withType(type)
          .withSize(size)
          .withPrimaryKey(isPrimaryKey)
          .withForeignKey(columnFks.get(name))
          .withNullable(isNullable)
          .withAutoIncrement(isAutoIncremented)
          .withDefaultValue(defaultValue));
    }
  }

  private Map<String, ForeignKeyDescriptor> getForeignKeys(final Connection connection,
      final String tableName, final DatabaseMetaData dbMetaData) throws SQLException {
    final Map<String, ForeignKeyDescriptor> columnFks = new LinkedHashMap<>(4);
    final ResultSet foreignKeys =
        dbMetaData.getImportedKeys(connection.getCatalog(), connection.getSchema(), tableName);
    while (foreignKeys.next()) {
      final String fkName = foreignKeys.getString("FK_NAME");
      final String columnName = foreignKeys.getString("FKCOLUMN_NAME");
      final String targetTableName = foreignKeys.getString("PKTABLE_NAME");
      final String targetColumnName = foreignKeys.getString("PKCOLUMN_NAME");
      columnFks.put(columnName,
          new ForeignKeyDescriptor(fkName, targetTableName, targetColumnName));
    }
    return columnFks;
  }

  private List<String> getPrimaryKeys(final Connection connection, final String tableName,
      final DatabaseMetaData dbMetaData) throws SQLException {
    final List<String> columnPks = new ArrayList<>(2);
    final ResultSet primaryKeys =
        dbMetaData.getPrimaryKeys(connection.getCatalog(), connection.getSchema(), tableName);
    while (primaryKeys.next()) {
      final String pk = primaryKeys.getString("COLUMN_NAME");
      columnPks.add(pk);
    }
    return columnPks;
  }

  /**
   * Requests the content of the specified table by applying the given predicate and uses the
   * specified converters to convert each row and each row's value to their corresponding business
   * object, ready to be handled by the caller.
   * @param <V> the type of the business objects representing the row's values.
   * @param <R> the type of the business objects representing the rows.
   * @param connection a connection to the database.
   * @param tableName the name of the table to request.
   * @param predicate a predicate to use to filter the table's content.
   * @param orderBy a order by directive already built (without the clause key words).
   * @param converters the converters to use to convert each row and each row's value to a business
   * object.
   * @param pagination a pagination in order to avoid bad performances.
   * @return a list of rows, matching the given predicate, in their business representation. If no
   * rows match the specified predicate or if the table is empty, an empty list is returned.
   * @throws SQLException if an error occurs while requesting the database.
   */
  <V, R> SilverpeasList<R> request(final Connection connection, final String tableName,
      final AbstractColumnValuePredicate predicate, final String orderBy, final DataConverters<V, R> converters,
      final PaginationPage pagination)
      throws SQLException {
    Objects.requireNonNull(connection);
    Objects.requireNonNull(tableName);
    Objects.requireNonNull(predicate);
    JdbcSqlQuery query = JdbcSqlQuery.createSelect("*").from(tableName);
    query = predicate.apply(query).orderBy(orderBy);
    if (pagination != null) {
      query.withPagination(pagination.asCriterion());
    }
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
  long delete(final Connection connection, final String tableName,
      final Map<String, Object> criteria) throws SQLException {
    final JdbcSqlQuery query = JdbcSqlQuery.createDeleteFor(tableName);
    return applyCriteria(query, criteria).executeWith(connection);
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
  long update(final Connection connection, final String tableName, final Map<String, Object> values,
      Map<String, Object> criteria) throws SQLException {
    final JdbcSqlQuery query = JdbcSqlQuery.createUpdateFor(tableName);
    values.forEach(query::addUpdateParam);
    return applyCriteria(query, criteria).executeWith(connection);
  }

  /**
   * Inserts into the specified table a new row with the specified values.
   * @param connection a connection to the database.
   * @param tableName the name of the table into which the row will be inserted.
   * @param values the values of the row to insert as a map of column names to column values.
   */
  void insert(final Connection connection, final String tableName, final Map<String, Object> values)
      throws SQLException {
    final JdbcSqlQuery query = JdbcSqlQuery.createInsertFor(tableName);
    values.forEach(query::addInsertParam);
    query.executeWith(connection);
  }

  private JdbcSqlQuery applyCriteria(final JdbcSqlQuery query, final Map<String, Object> criteria) {
    final StringBuilder clauses = new StringBuilder(criteria.size());
    final List<Object> values = new ArrayList<>(criteria.size());
    final String conjunction = " and ";
    criteria.forEach((key, value) -> {
      if (value == null) {
        clauses.append(key).append(" is null").append(conjunction);
      } else {
        clauses.append(key).append(" = ?").append(conjunction);
        values.add(value);
      }
    });
    clauses.setLength(clauses.length() - conjunction.length());
    return query.where(clauses.toString(), values);
  }

  private DefaultValue getDefaultValue(final ResultSet resultSet)
      throws SQLException {
    String defaultValue = resultSet.getString("COLUMN_DEF");
    return new DefaultValue(defaultValue);
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

  /**
   * Descriptor of a foreign key. It should be mapped to a column in a given table for which it
   * references another column of another table.
   */
  static class ForeignKeyDescriptor {
    private final String name;
    private final String targetTableName;
    private final String targetColumnName;

    private ForeignKeyDescriptor(final String name, final String targetTableName,
        final String targetColumnName) {
      this.name = name;
      this.targetTableName = targetTableName;
      this.targetColumnName = targetColumnName;
    }

    /**
     * Gets the name of the foreign key. Useful to retrieve the columns that made up a composite
     * foreign keys.
     * @return the name of the foreign key.
     */
    String getName() {
      return name;
    }

    /**
     * Gets the name of the table targeted by this key.
     * @return the name of the targeted table.
     */
    String getTargetTableName() {
      return targetTableName;
    }

    /**
     * Gets the name of the column in the targeted table that is targeted by this key.
     * @return the name of the targeted column.
     */
    String getTargetColumnName() {
      return targetColumnName;
    }
  }

  class DefaultValue {
    private final String pattern;

    public DefaultValue(final String valuePattern) {
      this.pattern = valuePattern;
    }

    public boolean isDefined() {
      return pattern != null;
    }

    public String get() {
      String value = null;
      if (isDefined()) {
        try (Connection connection = currentConnectionInfo.openConnection()) {
          value = computeSQLFunction(connection, pattern);
        } catch (MyDBException | SQLException e) {
          throw new MyDBRuntimeException(e);
        }
        int index = value.indexOf("::");
        if (index != -1) {
          value = parseDefaultValue(value, index);
        }
      }
      return value;
    }

    @Nonnull
    private String parseDefaultValue(String defaultValue, final int valueLength) {
      defaultValue = defaultValue.substring(0, valueLength);
      if (defaultValue.startsWith("'") && defaultValue.endsWith("'")) {
        defaultValue = defaultValue.substring(1, defaultValue.length() - 1);
      }
      return defaultValue;
    }

    @Nonnull
    private String computeSQLFunction(final Connection connection, final String function) {
      try {
        return JdbcSqlQuery.createSelect(function)
            .executeUniqueWith(connection, r -> r.getString(1));
      } catch (SQLException e) {
        // not a function or cannot be computed by the database
        SilverLogger.getLogger(this).silent(e);
        return function;
      }
    }
  }

  /**
   * A descriptor of a column in a database table.
   */
  class ColumnDescriptor {
    private String name;
    private int type;
    private int size;
    private boolean primaryKey;
    private ForeignKeyDescriptor foreignKey;
    private boolean nullable;
    private boolean autoIncrementable;
    private DefaultValue defaultValue;

    private ColumnDescriptor() {
    }

    /**
     * Gets the name of the column.
     * @return the unique name of the column in the table.
     */
    String getName() {
      return name;
    }

    private ColumnDescriptor withName(final String name) {
      this.name = name;
      return this;
    }

    /**
     * Gets the SQL type code of this column.
     * @return the code of the SQL type of the values that column accepts.
     * @see java.sql.Types
     */
    int getType() {
      return type;
    }

    private ColumnDescriptor withType(final int type) {
      this.type = type;
      return this;
    }

    /**
     * Gets the size of the column SQL type.
     * @return the size of the column type.
     */
    int getSize() {
      return size;
    }

    private ColumnDescriptor withSize(final int size) {
      this.size = size;
      return this;
    }

    /**
     * Is this column a primary key?
     * @return true if the values of this column are primary keys. False otherwise.
     */
    boolean isPrimaryKey() {
      return primaryKey;
    }

    private ColumnDescriptor withPrimaryKey(final boolean primaryKey) {
      this.primaryKey = primaryKey;
      return this;
    }

    /**
     * Is this column can be nullable.
     * @return true if this column can be valued with null, false otherwise.
     */
    boolean isNullable() {
      return nullable;
    }

    private ColumnDescriptor withNullable(final boolean nullable) {
      this.nullable = nullable;
      return this;
    }

    /**
     * Is the valuation of this column can be incrementable?
     * @return true of the valuation of this column is taken in charge by the database. False
     * otherwise.
     */
    boolean isAutoIncrementable() {
      return autoIncrementable;
    }

    private ColumnDescriptor withAutoIncrement(final boolean isAutoIncrement) {
      this.autoIncrementable = isAutoIncrement;
      return this;
    }

    /**
     * Gets the default value of this column if any.
     * @return the default value set with this column, null if no such a default value was set.
     */
    DefaultValue getDefaultValue() {
      return defaultValue;
    }

    private ColumnDescriptor withDefaultValue(final DefaultValue defaultValue) {
      this.defaultValue = defaultValue;
      return this;
    }

    /**
     * Gets a descriptor of the foreign key that is mapped with this column.
     * @return a foreign key descriptor or null if this column isn't a foreign key.
     */
    ForeignKeyDescriptor getForeignKey() {
      return this.foreignKey;
    }

    private ColumnDescriptor withForeignKey(final ForeignKeyDescriptor foreignKey) {
      this.foreignKey = foreignKey;
      return this;
    }
  }

  /**
   * An operation on the behalf of the database.
   * @param <T> the type of the result.
   */
  @FunctionalInterface
  interface DbOperation<T> {
    T execute(final JdbcRequester requester, final Connection connection) throws SQLException;
  }

  /**
   * A consumer of a database column description.
   */
  @FunctionalInterface
  interface ColumnConsumer {
    void accept(final ColumnDescriptor column);
  }

  /**
   * A converter of the database row to a business object.
   * @param <V> the type of the business value in a row.
   * @param <R> the type of the business row.
   */
  @FunctionalInterface
  interface RowConverter<V, R> {
    R convert(final Map<String, V> row);
  }

  /**
   * A converter of a value in a column to a business object.
   * @param <V> the type of the business value.
   */
  @FunctionalInterface
  interface ValueConverter<V> {
    V convert(final Object value, final int valueType);
  }
}
