/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.mydb.model;

import org.silverpeas.components.mydb.service.MyDBConnectionInfoService;
import org.silverpeas.components.mydb.service.MyDBException;
import org.silverpeas.core.persistence.datasource.model.identifier.UniqueIntegerIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;
import org.silverpeas.core.util.StringUtil;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Information about a connexion to a data source. Such information is the name of the
 * data source and the credentials required to access that data source.
 * @author mmoquillon
 */
@Entity
@NamedQuery(name = "MyDBConnectionInfo.findByInstanceId",
    query = "select ds from MyDBConnectionInfo ds where ds.instanceId = :instanceId")
@NamedQuery(name = "MyDBConnectionInfo.deleteByInstanceId",
    query = "delete MyDBConnectionInfo where instanceId = :instanceId")
@Table(name = "sc_mydb_connectinfo")
public class MyDBConnectionInfo
    extends BasicJpaEntity<MyDBConnectionInfo, UniqueIntegerIdentifier> {

  @Column(length = 250, nullable = false)
  @NotNull
  private String dataSource;
  @Column(length = 250)
  private String login;
  @Column(length = 250)
  private String password;
  @Column(length = 100)
  private String tableName;
  private int rowLimit = 0;
  @Column(length = 50, nullable = false)
  @NotNull
  private String instanceId;

  public static List<MyDBConnectionInfo> getFromComponentInstance(String instanceId) {
    return MyDBConnectionInfoService.get().getConnectionInfoList(instanceId);
  }

  public static void removeFromComponentInstance(String instanceId) {
    MyDBConnectionInfoService.get().removeConnectionInfoOfComponentInstance(instanceId);
  }

  protected MyDBConnectionInfo() {
  }

  public MyDBConnectionInfo(String dataSource, String instanceId) {
    this.dataSource = dataSource;
    this.instanceId = instanceId;
  }

  /**
   * Is this connection information defined? Information about a connection to a data source is
   * defined if both it is related to a myDB application instance and the name of the
   * data source is defined.
   * @return true if the connection is defined. False otherwise.
   */
  public boolean isDefined() {
    return StringUtil.isDefined(this.dataSource) && StringUtil.isDefined(this.instanceId);
  }

  /**
   * Sets the default table to defaultTable from the database with this connection information
   * and returns
   * the later.
   * @param tableName the name of the table to load by default.
   * @return itself.
   */
  public MyDBConnectionInfo withDefaultTableName(String tableName) {
    setDefaultTableName(tableName.toLowerCase());
    return this;
  }

  public MyDBConnectionInfo withoutAnyDefaultTable() {
    setDefaultTableName(null);
    return this;
  }

  /**
   * Sets the maximum number of data to select when requesting the data source and returns this
   * connection information.
   * @param maxNumber the maximum number of data to return. 0 means all.
   * @return itself.
   */
  public MyDBConnectionInfo withDataMaxNumber(int maxNumber) {
    setDataMaxNumber(maxNumber);
    return this;
  }

  public MyDBConnectionInfo withLoginAndPassword(String login, String password) {
    setLoginAndPassword(login, password);
    return this;
  }

  /**
   * Sets a new data source by its JNDI name to this connection info.
   * @param dataSourceName the JNDI name of the data source to connect to.
   * @return itself.
   */
  public MyDBConnectionInfo withDataSourceName(String dataSourceName) {
    setDataSourceName(dataSourceName);
    return this;
  }

  /**
   * Gets the JNDI name of the data source targeted by this connection information.
   * @return the JNDI name of the data source.
   */
  public String getDataSourceName() {
    return dataSource;
  }

  /**
   * Gets the user identifier used in the data source authentication.
   * @return the login.
   */
  public String getLogin() {
    return (login == null ? "" : login);
  }

  /**
   * Gets the password associated with the login to connect the data source.
   * @return the password.
   */
  public String getPassword() {
    return (password == null ? "" : password);
  }

  /**
   * Gets the name of the table to load with this connexion information.
   * @return the name of the default table.
   */
  public String getDefaultTableName() {
    return tableName;
  }

  /**
   * Gets the maximum number of data to return when requesting the data source.
   * @return the maximum number of data to consider. 0 means all.
   */
  public int getDataMaxNumber() {
    return rowLimit;
  }

  /**
   * Is the name of the default table to load is defined?
   * @return true if the name of a default table is set, false otherwise.
   */
  public boolean isDefaultTableNameDefined() {
    return StringUtil.isDefined(tableName);
  }

  /**
   * Sets a new data source by its JNDI name to this connection info.
   * @param dataSourceName the JNDI name of the data source to connect to.
   */
  public void setDataSourceName(String dataSourceName) {
    this.dataSource = dataSourceName;
  }

  /**
   * Sets the login and the password required to open a connection to the targeted data source.
   * @param login a login.
   * @param password the password associated with the login.
   */
  public void setLoginAndPassword(String login, String password) {
    this.login = login;
    this.password = password;
  }

  /**
   * Sets the name of the default table to load with this connection information.
   * @param tableName the name of a table in the database.
   */
  public void setDefaultTableName(String tableName) {
    this.tableName = tableName;
  }

  /**
   * Sets the maximum number of data to select when requesting the data source.
   * @param maxNumber the maximum number of data to return. 0 means all.
   */
  public void setDataMaxNumber(int maxNumber) {
    this.rowLimit = Math.max(maxNumber, 0);
  }

  /**
   * Saves or updates this connection information into the persistence context in order to be able
   * to retrieve it later.
   */
  public void save() {
    MyDBConnectionInfoService.get().saveConnectionInfo(this);
  }

  /**
   * Opens a connection to the data source targeted by this connection information.
   * @return a connection against the data source referred by this object.
   */
  public Connection openConnection() throws MyDBException {
    try {
      DataSource ds = InitialContext.doLookup(getDataSourceName());
      return ds.getConnection(getLogin(), getPassword());
    } catch (NamingException | SQLException ex) {
      throw new MyDBException(ex.getMessage());
    }
  }

  @Override
  public boolean equals(final Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  /**
   * Checks the connection with the remote data source referred in this
   * {@link MyDBConnectionInfo} instance.
   * <p>
   * If the connection cannot be established with the data source, a
   * {@link org.silverpeas.components.mydb.service.MyDBException}
   * exception is thrown. This occurs when:
   * </p>
   * <ul>
   * <li>no connection information is set for the underlying component instance,</li>
   * <li>the connection information isn't correct,</li>
   * <li>the connection information is correct but the connection with the data source fails at
   * this time.</li>
   * </ul>
   * @throws MyDBException if the connection cannot be established with the data source
   * referred by this {@link MyDBConnectionInfo} instance.
   */
  public void checkConnection() throws MyDBException {
    try (Connection connection = openConnection()) {
      if (!connection.isValid(0)) {
        throw new MyDBException("No valid connexion with the data source " + getDataSourceName());
      }
    } catch (SQLException e) {
      throw new MyDBException(e);
    }
  }
}
