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

package org.silverpeas.components.jdbcconnector.model;

import org.silverpeas.components.jdbcconnector.service.JdbcConnectorException;
import org.silverpeas.components.jdbcconnector.service.DataSourceConnectionInfoService;
import org.silverpeas.core.persistence.datasource.model.identifier.UniqueIntegerIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;
import org.silverpeas.kernel.util.StringUtil;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Information about a JDBC connection to a data source. Such information is the name of the
 * data source and the credentials required to access that data source.
 * @author mmoquillon
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "DataSourceConnectionInfo.findByInstanceId", query = "from " +
        "DataSourceConnectionInfo where instanceId = :instanceId"),
    @NamedQuery(name = "DataSourceConnectionInfo.deleteByInstanceId", query =
        "delete DataSourceConnectionInfo where instanceId = :instanceId")})
@Table(name = "sc_connecteurjdbc_connectinfo")
public class DataSourceConnectionInfo
    extends BasicJpaEntity<DataSourceConnectionInfo, UniqueIntegerIdentifier> {

  @Column(length = 250, nullable = false)
  @NotNull
  private String dataSource;
  @Column(length = 250)
  private String login;
  @Column(length = 250)
  private String password;
  @Column(length = 4000)
  private String sqlreq;
  private int rowLimit = 0;
  @Column(length = 50, nullable = false)
  @NotNull
  private String instanceId;

  public static DataSourceConnectionInfo getById(String id) {
    return DataSourceConnectionInfoService.get().getConnectionInfo(id);
  }

  public static List<DataSourceConnectionInfo> getFromComponentInstance(String instanceId) {
    return DataSourceConnectionInfoService.get().getConnectionInfoList(instanceId);
  }

  public static void removeFromComponentInstance(String instanceId) {
    DataSourceConnectionInfoService.get().removeConnectionInfoOfComponentInstance(instanceId);
  }

  protected DataSourceConnectionInfo() {
  }

  public DataSourceConnectionInfo(String dataSource, String instanceId) {
    this.dataSource = dataSource;
    this.instanceId = instanceId;
  }

  /**
   * Is this connection information defined? Information about a connection to a data source is
   * defined if both it is related to a ConnecteurJDBC application instance and the name of the
   * data source is defined.
   * @return
   */
  public boolean isDefined() {
    return StringUtil.isDefined(this.dataSource) && StringUtil.isDefined(this.instanceId);
  }

  /**
   * Sets the SQL request to used when requesting the data source and returns this connection
   * information.
   * @param sqlRequest a SQL request.
   * @return itself.
   */
  public DataSourceConnectionInfo withSqlRequest(String sqlRequest) {
    setSqlRequest(sqlRequest);
    return this;
  }

  /**
   * Sets the maximum number of data to select when requesting the data source and returns this
   * connection information.
   * @param maxNumber the maximum number of data to return. 0 means all.
   * @return itself.
   */
  public DataSourceConnectionInfo withDataMaxNumber(int maxNumber) {
    setDataMaxNumber(maxNumber);
    return this;
  }

  public DataSourceConnectionInfo withLoginAndPassword(String login, String password) {
    setLoginAndPassword(login, password);
    return this;
  }

  /**
   * Sets a new data source by its JNDI name to this connection info.
   * @param dataSourceName the JNDI name of the data source to connect to.
   * @return itself.
   */
  public DataSourceConnectionInfo withDataSourceName(String dataSourceName) {
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
   * Gets the unique identifier of the component instance this connection info belongs to.
   * @return the unique identifier of the component instance.
   */
  public String getInstanceId() {
    return instanceId;
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
   * Gets the SQL request used to select data from the connected data source.
   * @return the SQL request used in this connection information.
   */
  public String getSqlRequest() {
    return sqlreq;
  }

  /**
   * Gets the maximum number of data to return when requesting the data source.
   * @return the maximum number of data to consider. 0 means all.
   */
  public int getDataMaxNumber() {
    return rowLimit;
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
   * Sets the SQL request to used when requesting the data source.
   * @param sqlRequest a SQL request.
   */
  public void setSqlRequest(String sqlRequest) {
    this.sqlreq = sqlRequest;
  }

  /**
   * Sets the maximum number of data to select when requesting the data source.
   * @param maxNumber the maximum number of data to return. 0 means all.
   */
  public void setDataMaxNumber(int maxNumber) {
    if (maxNumber <= 0) {
      this.rowLimit = 0;
    } else {
      this.rowLimit = maxNumber;
    }
  }

  /**
   * Saves or updates this connection information into the persistence context in order to be able
   * to retrieve it later.
   */
  public void save() {
    DataSourceConnectionInfoService.get().saveConnectionInfo(this);
  }

  /**
   * Removes this connection information from the persistence context.
   */
  public void remove() {
    DataSourceConnectionInfoService.get().removeConnectionInfo(this);
  }

  /**
   * Opens a connection to the data source targeted by this connection information.
   * @return a connection against the data source referred by this object.
   */
  public Connection openConnection() throws JdbcConnectorException {
    try {
      DataSource ds = InitialContext.doLookup(getDataSourceName());
      return ds.getConnection(getLogin(), getPassword());
    } catch (NamingException | SQLException ex) {
      throw new JdbcConnectorException(ex.getMessage());
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
}
