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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.datawarning.model;

import org.silverpeas.components.datawarning.DataWarningDBDriver;
import org.silverpeas.components.datawarning.DataWarningDBDrivers;
import org.silverpeas.components.datawarning.DataWarningException;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBean;
import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.logging.SilverLogger;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class DataWarning extends SilverpeasBean {

  private static final long serialVersionUID = 8814573887371294333L;
  public static final int INCONDITIONAL_QUERY = 0;
  public static final int TRIGGER_ANALYSIS = 1;
  private String description;
  private String jdbcDriverName;
  private String login;
  private String pwd;
  private int rowLimit;
  private String instanceId;
  private int analysisType;

  public DataWarning() {
    super();
    analysisType = INCONDITIONAL_QUERY;
  }

  public DataWarning copy()  {
    DataWarning newOne;
    try {
      newOne = (DataWarning) super.clone();
    } catch (CloneNotSupportedException e) {
      SilverLogger.getLogger(this).silent(e);
      newOne = new DataWarning();
    }

    newOne.description = description;
    newOne.jdbcDriverName = jdbcDriverName;
    newOne.login = login;
    newOne.pwd = pwd;
    newOne.rowLimit = rowLimit;
    newOne.instanceId = instanceId;
    newOne.analysisType = analysisType;
    newOne.setPK(getPK());

    return newOne;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescription() {
    return getNonNullString(description);
  }

  public void setJdbcDriverName(String jdbcDriverName) {
    this.jdbcDriverName = jdbcDriverName;
  }

  public String getJdbcDriverName() {
    return jdbcDriverName;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public String getLogin() {
    return getNonNullString(login);
  }

  public void setPwd(String password) {
    this.pwd = password;
  }

  public String getPwd() {
    return getNonNullString(pwd);
  }

  public void setRowLimit(int rowLimit) {
    this.rowLimit = rowLimit;
  }

  public int getRowLimit() {
    return rowLimit;
  }

  @SuppressWarnings("unused")
  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setAnalysisType(int analysisType) {
    this.analysisType = analysisType;
  }

  public int getAnalysisType() {
    return analysisType;
  }

  @Override
  @NonNull
  protected String getTableName() {
    return "SC_DataWarning";
  }

  public Connection openConnection() throws DataWarningException {
    DataWarningDBDrivers dataWarningDBDrivers = new DataWarningDBDrivers();

    DataWarningDBDriver dbDriver = dataWarningDBDrivers.getDBDriver(jdbcDriverName);
    Connection con;

    try {
      Class.forName(dbDriver.getClassName());
      con = DriverManager.getConnection(dbDriver.getJdbcUrl(), login, pwd);
    } catch (Exception e) {
      throw new DataWarningException(e);
    }

    return con;
  }

  public void closeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
  }

  /**
   * return un tableau de string contenant les noms de toutes les tables de la base
   */
  public String[] getAllTableNames() throws DataWarningException {
    String[] retour;
    Connection con = openConnection();
    ResultSet tablesRs = null;
    try {
      DatabaseMetaData dbMetaData = con.getMetaData();
      tablesRs = dbMetaData.getTables(null, null, null, null);
      List<String> tables = new ArrayList<>();
      while (tablesRs.next()) {
        tables.add(tablesRs.getString("TABLE_NAME"));
      }
      retour = new String[tables.size()];
      for (int i = 0; i < tables.size(); i++) {
        retour[i] = tables.get(i);
      }
    } catch (Exception e) {
      throw new DataWarningException(e);
    } finally {
      DBUtil.close(tablesRs);
      closeConnection(con);
    }
    return retour;
  }

  /**
   * return les noms des colonnes de la table passé en parametre
   */
  public String[] getColumnNames(String tableName) throws DataWarningException {
    String[] retour;
    Connection con = openConnection();
    ResultSet colonnesRs = null;
    try {
      DatabaseMetaData dbMetaData = con.getMetaData();
      colonnesRs = dbMetaData.getColumns(null, null, tableName, null);
      List<String> colonnes = new ArrayList<>();
      while (colonnesRs.next()) {
        colonnes.add(colonnesRs.getString("COLUMN_NAME"));
      }
      retour = new String[colonnes.size()];
      for (int i = 0; i < colonnes.size(); i++) {
        retour[i] = colonnes.get(i);
      }
    } catch (Exception e) {
      throw new DataWarningException(e);
    } finally {
      DBUtil.close(colonnesRs);
      closeConnection(con);
    }
    return retour;
  }
}
