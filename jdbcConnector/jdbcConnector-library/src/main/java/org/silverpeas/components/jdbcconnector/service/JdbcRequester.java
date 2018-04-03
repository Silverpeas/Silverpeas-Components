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

package org.silverpeas.components.jdbcconnector.service;

import org.silverpeas.components.jdbcconnector.model.DataSourceConnectionInfo;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A requester of a remote enterprise data source by using the yet configured
 * {@link org.silverpeas.components.jdbcconnector.model.DataSourceConnectionInfo} instance. If
 * no {@link org.silverpeas.components.jdbcconnector.model.DataSourceConnectionInfo} instance is
 * available, then no connection is established and no request can be done.
 * @author mmoquillon
 */
public class JdbcRequester {

  private final String instanceId;
  private final DataSourceConnectionInfo currentConnectionInfo;

  /**
   * Constructs a new JDBC requester for the specified component instance. It loads the
   * {@link DataSourceConnectionInfo} instance that was persisted for the specified component
   * instance in order to establish a connection with a remote data source. If there is more than
   * one connection information registered, then we are in an unstable state for the given component
   * instance and a {@link JdbcConnectorRuntimeException} is then thrown.
   * @param componentInstanceId the unique identifier of a ConnecteurJDBC application instance.
   */
  public JdbcRequester(final String componentInstanceId) {
    this.instanceId = componentInstanceId;
    final List<DataSourceConnectionInfo> availableConnectionInfo =
        DataSourceConnectionInfo.getFromComponentInstance(this.instanceId);
    if (availableConnectionInfo.isEmpty()) {
      this.currentConnectionInfo =
          new DataSourceConnectionInfo("", this.instanceId).withSqlRequest("");
    } else {
      if (availableConnectionInfo.size() > 1) {
        throw new JdbcConnectorRuntimeException(
            "There is more than one defined data source for the JDBC connector " + this.instanceId +
                ": " + availableConnectionInfo.size() + " data sources found!");
      }
      this.currentConnectionInfo = availableConnectionInfo.get(0);
    }
  }

  /**
   * Gets information about the current JDBC connection used to request the data source.
   * @return a {@link DataSourceConnectionInfo} instance with all the information about the
   * connection with the remote data source
   */
  public DataSourceConnectionInfo getCurrentConnectionInfo() {
    return currentConnectionInfo;
  }

  /**
   * Checks the connection with the remote data source referred by the underlying
   * {@link DataSourceConnectionInfo} instance used by this requester.
   * <p>
   * If the connection cannot be established with the data source, a {@link JdbcConnectorException}
   * exception is thrown. This occurs when:
   * </p>
   * <ul>
   * <li>no connection information is set for the underlying component instance,</li>
   * <li>the connection information isn't correct,</li>
   * <li>the connection information is correct but the connection with the data source fails at
   * this time.</li>
   * </ul>
   * @throws JdbcConnectorException if the connection cannot be established with the data source
   * referred by the {@link DataSourceConnectionInfo} instance used by this requester.
   */
  public void checkConnection() throws JdbcConnectorException {
    try (Connection connection = currentConnectionInfo.openConnection()) {
      // we check the connection can be opened without raising any exception
    } catch (SQLException e) {
      throw new JdbcConnectorException(e);
    }
  }

  /**
   * Is there is a data source set with this requester? A data source is set when there is a
   * {@link DataSourceConnectionInfo} instance defined for the component instance to which this
   * requester is related.
   * @return true if there is a {@link DataSourceConnectionInfo} instance set with this requester.
   * False otherwise.
   */
  public boolean isDataSourceDefined() {
    return currentConnectionInfo.isDefined();
  }

  /**
   * Gets all the tables in the data source (only public tables and views are fetched). If an error
   * occurs while requesting the data source, a {@link JdbcConnectorRuntimeException} is thrown.
   * @return a list of table names.
   */
  public List<String> getTableNames() {
    final List<String> tableNames = new ArrayList<>();
    try (Connection connection = currentConnectionInfo.openConnection()) {
      DatabaseMetaData dbMetaData = connection.getMetaData();
      ResultSet tables = dbMetaData.getTables(null, null, null, new String[]{"TABLE", "VIEW"});
      while (tables.next()) {
        tableNames.add(tables.getString("TABLE_NAME"));
      }
      return tableNames;
    } catch (SQLException | JdbcConnectorException e) {
      throw new JdbcConnectorRuntimeException(e);
    }
  }

  /**
   * Gets all the columns in the specified table.
   * @param tableName a name of a table. This name must have been get by invoking
   * {@link #getTableNames()} method. If an error
   * occurs while requesting the data source, a {@link JdbcConnectorRuntimeException} is thrown.
   * @return a list of column names in the given table.
   */
  public List<String> getColumnNames(final String tableName) {
    final List<String> columnNames = new ArrayList<>();
    try (Connection connection = currentConnectionInfo.openConnection()) {
      DatabaseMetaData dbMetaData = connection.getMetaData();
      ResultSet columns = dbMetaData.getColumns(null, null, tableName, null);
      while (columns.next()) {
        columnNames.add(columns.getString("COLUMN_NAME"));
      }
      return columnNames;
    } catch (SQLException | JdbcConnectorException e) {
      throw new JdbcConnectorRuntimeException(e);
    }
  }

  /**
   * Requests the data source by using the SQL query that was set with the current underlying
   * {@link DataSourceConnectionInfo} instance. A {@link JdbcConnectorRuntimeException} is thrown
   * if the data returned by the requesting fails to be read.
   * @return a list of rows, each of them represented by a {@link TableRow} instance.
   * @throws JdbcConnectorException if either no connection can be established or the requesting
   * failed.
   */
  public List<TableRow> request() throws JdbcConnectorException {
    return request(currentConnectionInfo.getSqlRequest());
  }

  /**
   * Requests the data source with the specified SQL query. A
   * {@link JdbcConnectorRuntimeException} is thrown if the data returned by the requesting fails
   * to be read.
   * @return a list of rows, each of them represented by a {@link TableRow} instance.
   * @throws JdbcConnectorException if either no connection can be established or the requesting
   * failed.
   */
  public List<TableRow> request(final String sqlQuery) throws JdbcConnectorException {
    final List<TableRow> rows = new ArrayList<>();
    try (Connection connection = currentConnectionInfo.openConnection();
         PreparedStatement statement = connection.prepareStatement(sqlQuery);
         ResultSet rs = statement.executeQuery()) {
      while (rs.next()) {
        rows.add(new TableRow(rs));
      }
      return rows;
    } catch (SQLException e) {
      throw new JdbcConnectorException(e);
    }
  }
}
