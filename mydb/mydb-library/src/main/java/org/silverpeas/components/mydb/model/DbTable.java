/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
import org.silverpeas.components.mydb.service.MyDBRuntimeException;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

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
   * If no default table is defined then nothing is returned. If the default table doesn't
   * exist then a {@link org.silverpeas.components.mydb.service.MyDBRuntimeException} is thrown.
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
   * Gets the table with the specified name from the database referenced by the given connection
   * information. If no such a table doesn't exist, then nothing is returned.
   * @param tableName the name of a table in the database.
   * @param dsInfo the {@link MyDBConnectionInfo} instance with information to access the
   * database
   * @return optionally a {@link DbTable} instance or nothing if no such a table exist in the
   * database.
   */
  public static Optional<DbTable> table(final String tableName, final MyDBConnectionInfo dsInfo) {
    try {
      return Optional.of(new DbTable(tableName, dsInfo));
    } catch (MyDBRuntimeException e) {
      return Optional.empty();
    }
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
    Objects.requireNonNull(dsInfo);
    final JdbcRequester requester = new JdbcRequester(dsInfo);
    return requester.getTableNames();
  }

  /**
   * Constructs a new instance for a table with the specified name and that is defined in the
   * database referenced by the specified {@link MyDBConnectionInfo} object.
   * If the table doesn't exist then a
   * {@link org.silverpeas.components.mydb.service.MyDBRuntimeException} is thrown.
   * @param name the name of the table
   * @param ds the information about the database in which is defined this table.
   */
  private DbTable(final String name, final MyDBConnectionInfo ds) {
    StringUtil.requireDefined(name);
    Objects.requireNonNull(ds);
    this.name = name;
    setJdbcRequester(new JdbcRequester(ds));
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
      r.loadColumns(c, this.name, d -> this.columns.add(new DbColumn(d)));
      return null;
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
    return this.columns.stream().filter(c -> c.getName().equals(name)).findFirst();
  }

  /**
   * Gets the contents of this table as a list of rows, each of them being a tuple valuing
   * all the columns of this table. If the table is empty, then an empty list is returned.
   * @param filter a predicate to use for filtering the table content.
   * @param orderBy a order by directive already built (without the clause key words).
   * @param pagination a pagination in order to avoid bad performances.
   * @return a list of table rows. If a filter is set, it is then applied when requesting the
   * content of this table. The number of table rows is limited by the
   * {@link MyDBConnectionInfo#getDataMaxNumber()} property.
   */
  @SuppressWarnings("unchecked")
  public SilverpeasList<TableRow> getRows(final ColumnValuePredicate filter, final String orderBy,
      final PaginationPage pagination) {
    if (!(filter instanceof AbstractColumnValuePredicate)) {
      throw new IllegalArgumentException(
          "DbTable doesn't support predicate other than AbstractColumnValuePredicate objects");
    }
    return requester.perform((r, c) -> {
      final JdbcRequester.DataConverters<TableFieldValue, TableRow> converters =
          new JdbcRequester.DataConverters(TableFieldValue::new, TableRow::new);
      return r.request(c, this.name, (AbstractColumnValuePredicate) filter, orderBy, converters,
          pagination);
    });
  }

  /**
   * Deletes the specified row.
   * @param row the row to delete in this database table.
   */
  public long delete(final TableRow row) {
    return requester.perform((r, c) -> {
      final Map<String, Object> criteria = getCriteriaFrom(row);
      return r.delete(c, getName(), criteria);
    });
  }

  /**
   * Updates the specified row with the specified other row.
   * @param actualRow the row currently in this table.
   * @param updatedRow the row that will replace the actual one in this table.
   */
  public long update(final TableRow actualRow, final TableRow updatedRow) {
    return requester.perform((r, c) -> {
      final Map<String, Object> criteria = getCriteriaFrom(actualRow);
      final Map<String, Object> values = tableRowToMap(updatedRow);
      return r.update(c, getName(), values, criteria);
    });
  }

  /**
   * Adds the specified row into this table. The row will be inserted into the database table.
   * @param row the row to add into the database table.
   */
  public void add(final TableRow row) {
    requester.perform((r, c) -> {
      final Map<String, Object> values = tableRowToMap(row);
      r.insert(c, getName(), values);
      return null;
    });
  }

  private Map<String, Object> getCriteriaFrom(final TableRow row) {
    final List<DbColumn> pkColumns =
        columns.stream().filter(DbColumn::isPrimaryKey).collect(Collectors.toList());
    final Map<String, Object> criteria;
    if (!pkColumns.isEmpty()) {
      criteria = pkColumns.stream()
          .collect(toMap(DbColumn::getName,
              pk -> row.getFieldValue(pk.getName()).toSQLObject()));
    } else {
      criteria = tableRowToMap(row);
    }
    return criteria;
  }

  private Map<String, Object> tableRowToMap(final TableRow tableRow) {
    return tableRow.getFields()
        .entrySet()
        .stream()
        .filter(Predicate.not(e -> e.getKey().equalsIgnoreCase("SP_MAX_ROW_COUNT")))
        .collect(HashMap::new, (h, e) -> h.put(e.getKey(), e.getValue().toSQLObject()), HashMap::putAll);
  }
}
