/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

package org.silverpeas.connecteurJDBC.service;

import com.stratelia.webactiv.persistence.SilverpeasBean;

public class ConnecteurJDBCConnectionInfoDetail extends SilverpeasBean {

  private static final long serialVersionUID = 1L;
  private String JDBCdriverName = "";
  private String JDBCurl = "";
  private String login = "";
  private String password = "";
  private String SQLreq = "";
  private String instanceId = "";
  private int rowLimit;

  // getters
  public String getJDBCdriverName() {
    return JDBCdriverName;
  }

  public String getJDBCurl() {
    return JDBCurl;
  }

  public String getLogin() {
    return login;
  }

  public String getPassword() {
    return password;
  }

  public String getSQLreq() {
    return SQLreq;
  }

  public int getRowLimit() {
    return rowLimit;
  }

  public String getInstanceId() {
    return getPK().getComponentName();
  }

  // setters
  public void setJDBCdriverName(String JDBCdriverName) {
    this.JDBCdriverName = JDBCdriverName;
  }

  public void setJDBCurl(String JDBCurl) {
    this.JDBCurl = JDBCurl;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setSQLreq(String SQLreq) {
    this.SQLreq = SQLreq;
  }

  public void setRowLimit(int rowLimit) {
    this.rowLimit = rowLimit;
  }

  public void setInstanceId(String componentId) {
    this.instanceId = componentId;
  }
}