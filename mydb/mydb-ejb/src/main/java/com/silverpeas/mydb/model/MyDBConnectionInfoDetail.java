/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.mydb.model;

import com.stratelia.webactiv.persistence.SilverpeasBean;

/**
 * Description of a connection to a database.
 * @author Antoine HEDIN
 */
public class MyDBConnectionInfoDetail extends SilverpeasBean {

  private static final long serialVersionUID = 1L;
  private String jdbcDriverName = "";
  private String jdbcUrl = "";
  private String login = "";
  private String password = "";
  private String tableName = "";
  private String instanceId = "";
  private int rowLimit;

  public String getJdbcDriverName() {
    return jdbcDriverName;
  }

  public void setJdbcDriverName(String jdbcDriverName) {
    this.jdbcDriverName = jdbcDriverName;
  }

  public String getJdbcUrl() {
    return jdbcUrl;
  }

  public void setJdbcUrl(String jdbcUrl) {
    this.jdbcUrl = jdbcUrl;
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public void setInstanceId(String instanceId) {
    getPK().setComponentName(instanceId);
    this.instanceId = instanceId;
  }

  public String getInstanceId() {
    return getPK().getComponentName();
  }

  public int getRowLimit() {
    return rowLimit;
  }

  public void setRowLimit(int rowLimit) {
    this.rowLimit = rowLimit;
  }

}