/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.connecteurJDBC.control;

import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.connecteurJDBC.model.DataSourceConnectionInfo;
import org.silverpeas.connecteurJDBC.model.DataSourceDefinition;
import org.silverpeas.util.DBUtil;
import org.silverpeas.util.StringUtil;
import org.silverpeas.util.exception.SilverpeasException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Vector;

/**
 * Title: Connecteur JDBC Description: Ce composant a pour objet de permettre de recuperer
 * rapidement et simplement des donnees du systeme d'information de l'entreprise.
 */
public class ConnecteurJDBCSessionController extends AbstractComponentSessionController {

  private String table = null;
  private String column = null;
  private String selected = null;
  private String lastColumns = "";
  private String columnReq = "*";
  private String compare = "*";
  private String columnValue = "";
  private String validreq = "";
  private String fullreq = "";
  private String sortType = "";
  private boolean connectionOpened = false;

  private DataSourceConnectionInfo currentConnectionInfo = null;
  private List<DataSourceDefinition> allDataSources = null;

  private String[] tableNames = null;
  private String[] columnNames = null;
  ResultSet tables_rs;
  ResultSetMetaData tables_rsmd;
  private ResultSet rs;
  private DatabaseMetaData dbMetaData;
  // Current row number
  private int rowNumber;
  // columns selected in the sql query
  private Vector<String> selectedColumn = new Vector<>();

  /**
   * Constructeur
   * @param mainSessionCtrl
   * @param componentContext
   * @throws ConnecteurJDBCRuntimeException
   */
  public ConnecteurJDBCSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.connecteurJDBC.multilang.connecteurJDBC");
    loadAllAvailableDataSources();
    loadCurrentConnectionInfo();
  }

  /**
   * Initialize connector
   */
  private void loadCurrentConnectionInfo() {
    if (currentConnectionInfo == null) {
      try {
        // load and return the current connection settings for the component
        List<DataSourceConnectionInfo> connectionInfos =
            DataSourceConnectionInfo.getFromComponentInstance(getComponentId());
        if (connectionInfos.isEmpty()) {
          currentConnectionInfo =
              new DataSourceConnectionInfo("", getComponentId()).withSqlRequest("");
        } else {
          if (connectionInfos.size() > 1) {
          throw new ConnecteurJDBCException("connecteurJDBCSessionControl.initConnecteur()",
              SilverpeasException.FATAL, "connecteurJDBC.EX_THERE_MUST_BE_ONLY_ONE_CONECTION");
          }
          currentConnectionInfo = connectionInfos.get(0);
        }
      } catch (Exception e) {
        throw new ConnecteurJDBCRuntimeException("connecteurJDBCSessionControl.initConnecteur()",
            SilverpeasException.FATAL, "connecteurJDBC.EX_INIT_CONNECTEUR_FAIL", e);
      }
    }
  }

  private void loadAllAvailableDataSources() {
    if (allDataSources == null || allDataSources.isEmpty()) {
      allDataSources = DataSourceDefinition.getAll();
    }
  }

  public void closeConnection() {
    if (connectionOpened) {
      try {
        if (rs != null) {
          rs.close();
        }
        connectionOpened = false;
      } catch (SQLException e) {
        SilverTrace.warn("connecteurJDBC", "ConnecteurJDBCSessionControl.closeConnection()",
            "connecteurJDBC.MSG_CONNECTION_NOT_CLOSED", null, e);

      }
    }

  }

  public void startConnection() {
    final String request = currentConnectionInfo.getSqlRequest();
    startConnection(request);
  }

  public void startConnection(String request) {
    SilverTrace.info("connecteurJDBC", "ConnecteurJDBCSessionController.startConnection()",
        "root.MSG_GEN_ENTER_METHOD", "temp : " + request);
    rowNumber = 0;

    if (!connectionOpened && (currentConnectionInfo != null)) {
      try {
        Connection connection = currentConnectionInfo.openConnection();
        dbMetaData = connection.getMetaData();
        Statement stmt = connection.createStatement();
        if (!StringUtil.isDefined(request)) {
          rs = null;
        } else {
          rs = stmt.executeQuery(request);
        }
        connectionOpened = true;
      } catch (Exception e) {
        SilverTrace.warn("connecteurJDBC", "ConnecteurJDBCSessionControl.startConnection()",
            "connecteurJDBC.MSG_CONNECTION_NOT_STARTED", "request : " + request, e);
      }
    }
  }

  /**
   * set the valid request
   * @param validreq
   */
  public void setValidRequest(String validreq) {
    SilverTrace.info("connecteurJDBC", "ConnecteurJDBCSessionController.setValidRequest()",
        "root.MSG_GEN_ENTER_METHOD", "validreq : " + validreq);
    this.validreq = validreq;
  }

  /**
   * get the last valid request
   * @return
   */
  public String getLastValidRequest() {
    return validreq;
  }

  /**
   * set the full request
   */
  public void setFullRequest(String fullreq) {
    SilverTrace.info("connecteurJDBC", "ConnecteurJDBCSessionController.setFullRequest()",
        "root.MSG_GEN_ENTER_METHOD", "fullreq : " + fullreq);
    this.fullreq = fullreq;
  }

  /**
   * get the full request
   */
  public String getFullRequest() {
    if (StringUtil.isNotDefined(fullreq)) {
      return currentConnectionInfo.getSqlRequest();
    }
    return fullreq;
  }

  /**
   * set the sort type (asc or desc) of the request
   */
  public void setSortType(String sortType) {
    this.sortType = sortType;
  }

  /**
   * get the sort type asc or desc
   */
  public String getSortType() {
    return sortType;
  }

  /**
   * get the liste of the table names
   */
  public String[] getTableNames() throws ConnecteurJDBCException {
    startConnection();
    List<String> tableVector = new ArrayList<>();
    try {
      tables_rs = dbMetaData.getTables(null, null, null, null);
      tables_rsmd = tables_rs.getMetaData();
      while (tables_rs.next()) {
        tableVector.add(tables_rs.getString("TABLE_NAME"));
      }
      int n = tableVector.size();
      tableNames = tableVector.toArray(new String[n]);
    } catch (Exception e) {
      throw new ConnecteurJDBCException("connecteurJDBCSessionControl.getTableNames()",
          SilverpeasException.ERROR, "connecteurJDBC.EX_CANT_GET_TABLES_NAMES", e);
    }
    closeConnection();
    return tableNames;
  }

  /**
   * get the table corresponding to the tableName
   */
  private ResultSet getTable(String tableName) throws ConnecteurJDBCException {
    ResultSet resultset;
    try {
      resultset = dbMetaData.getColumns(null, null, tableName, null);
    } catch (SQLException e) {
      throw new ConnecteurJDBCException("connecteurJDBCSessionControl.getTable()",
          SilverpeasException.ERROR, "connecteurJDBC.EX_GET_TABLE_FAIL",
          "from table : " + tableName, e);
    }
    return resultset;
  }

  /**
   * get the columns names corrsponding to a table name
   */
  public String[] getColumnNames(String tableName) throws ConnecteurJDBCException {
    ResultSet tableRS = getTable(tableName);
    List<String> columnVector = new ArrayList<>();
    try {
      while (tableRS.next()) {
        columnVector.add(tableRS.getString("COLUMN_NAME"));
      }
    } catch (SQLException e) {
      throw new ConnecteurJDBCException("connecteurJDBCSessionControl.getTable()",
          SilverpeasException.ERROR, "connecteurJDBC.EX_GET_COLUMNS_NAMES_FAIL",
          "from table : " + tableName, e);
    }
    int n = columnVector.size();
    columnNames = columnVector.toArray(new String[n]);
    return columnNames;
  }

  /**
   * set the table of the SQL request
   */
  public void setTable(String table) {
    this.table = table;
  }

  /**
   * get the table of the SQL request
   */
  public String getTable() {
    return table;
  }

  /**
   * set the column of the SQL request
   */
  public void setColumn(String column) {
    this.column = column;
  }

  /**
   * get the column of the SQL request
   */
  public String getColumn() {
    return column;
  }

  public void setSelectedColumn(Vector<String> selectedColumn) {
    this.selectedColumn = selectedColumn;
  }

  public Vector<String> getSelectedColumn() {
    return selectedColumn;
  }

  public void setSelected(String selected) {
    this.selected = selected;
  }

  public String getSelected() {
    return selected;
  }

  public void setLastColumn(String lastColumns) {
    this.lastColumns = lastColumns;
  }

  public String getLastColumn() {
    return lastColumns;
  }

  /**
   * get the column selected to restrict the request results in the page connecteurJDBC.jsp
   */
  public String getColumnReq() {
    return columnReq;
  }

  /**
   * set the column selected to restrict the request results
   */
  public void setColumnReq(String columnReq) {
    this.columnReq = columnReq;
  }

  /**
   * get the symbole of comparison
   * @return
   */
  public String getCompare() {
    return compare;
  }

  /**
   * set the symbole of comparaison
   */
  public void setCompare(String compare) {
    this.compare = compare;
  }

  /**
   * get the value with wich a column is compared to restrict the sql request results
   */
  public String getColumnValue() {
    return columnValue;
  }

  /**
   * set the value with wich a column is compared
   */
  public void setColumnValue(String columnValue) {
    this.columnValue = columnValue;
  }

  /**
   * get the number of the columns of the SQL request results
   */
  public int getColumnCount() {
    int ret = 0;
    if (rs != null) {
      try {
        ResultSetMetaData md = rs.getMetaData();
        if (md == null) {
          ret = 1; // I assume that there is at least 1 column.
        } else {
          ret = md.getColumnCount();
        }
      } catch (SQLException e) {
        SilverTrace.warn("connecteurJDBC", "ConnecteurJDBCSessionControl.getColumnCount()",
            "connecteurJDBC.MSG_CANT_GET_Column_Count", null, e);
      }
    }
    return ret;
  }

  /**
   * get the name of the column number i
   */
  public String getColumnName(int i) {
    String ret = "";
    if (rs != null) {
      try {
        ResultSetMetaData md = rs.getMetaData();
        if (md == null) {
          ret = "Col 1";
        } else {
          ret = md.getColumnName(i);
        }
      } catch (SQLException e) {
        SilverTrace.warn("connecteurJDBC", "ConnecteurJDBCSessionControl.getColumnName()",
            "connecteurJDBC.MSG_CANT_GET_COLUMN_NAME", "column number : " + i, e);
      }
    }
    return ret;
  }

  /**
   * get the type of the column number i
   */
  public String getColumnType(int i) {
    String ret = "";
    if (rs != null) {
      try {
        ResultSetMetaData md = rs.getMetaData();
        if (md == null) {
          ret = "Col 1";
        } else {
          ret = md.getColumnTypeName(i);
        }
      } catch (SQLException e) {
        SilverTrace.warn("connecteurJDBC", "ConnecteurJDBCSessionControl.getColumnType()",
            "connecteurJDBC.MSG_CANT_GET_COLUMN_TYPE", "column number : " + i, e);
      }
    }
    return ret;
  }

  public boolean getNext() {
    boolean ret = false;
    if (rs != null) {
      final int rowLimit =
          (StringUtil.isNotDefined(currentConnectionInfo.getDataSourceName()) ? -1 :
              currentConnectionInfo.getDataMaxNumber());
      if (rowLimit == 0 || rowNumber < rowLimit) {
        rowNumber++;
        try {
          ret = rs.next();
        } catch (SQLException e) {
          SilverTrace.warn("connecteurJDBC", "ConnecteurJDBCSessionControl.getNext()",
              "connecteurJDBC.MSG_CANT_GET_NEXT_ROW", null, e);
        }
      }
    }
    return ret;
  }

  public String getString(int i) {
    String ret = "";
    if (rs != null) {
      try {
        ret = rs.getString(i);
      } catch (SQLException e) {
        SilverTrace.warn("connecteurJDBC", "ConnecteurJDBCSessionControl.getString()",
            "connecteurJDBC.MSG_CANT_GET_COLUMN_VALUE", null, e);
      }
    }
    return ret;
  }

  public boolean isConnectionConfigured() {
    return (currentConnectionInfo != null);
  }

  public String checkRequest(String request) {
    SilverTrace.info("connecteurJDBC", "ConnecteurJDBCSessionController.checkRequest()",
        "root.MSG_GEN_ENTER_METHOD", "request : " + request);
    String temp = request.trim();

    try {
      if (!StringUtil.isDefined(currentConnectionInfo.getDataSourceName())) {
        return getString("erreurParametresConnectionIncorrects");
      } else if (!StringUtil.isDefined(request)) {
        return getString("erreurRequeteVide");
      } else if (!temp.trim().toLowerCase().startsWith("select")) {
        return getString("erreurModifTable");
      } else {
        Connection connection = null;
        try {
          connection = currentConnectionInfo.openConnection();
          if (connection == null) {
            return getString("erreurParametresConnectionIncorrects");
          } else {
            try (Statement stmt = connection.createStatement()) {
              rs = stmt.executeQuery(request);
            }
          }
        } finally {
          DBUtil.close(connection);
        }
      }
    } catch (ConnecteurJDBCException | SQLException e) {
      SilverTrace.warn("connecteurJDBC", "ConnecteurJDBCSessionControl.checkConnection()",
          "connecteurJDBC.MSG_CHECK_REQUEST_FAIL", "request : " + request, e);
      return e.getMessage();
    } catch (MissingResourceException e) {
      SilverTrace.warn("connecteurJDBC", "ConnecteurJDBCSessionControl.checkConnection()",
          "connecteurJDBC.MSG_MISSING_MESSAGE_ERROR", "request : " + request, e);
      return "can't find error Message";
    }
    return null;
  }

  public List<DataSourceDefinition> getAvailableDataSources() {
    return allDataSources;
  }

  public void updateSQLRequest(String sqlRequest) throws ConnecteurJDBCException {
    String error = checkRequest(sqlRequest);
    if (StringUtil.isDefined(error)) {
      throw new ConnecteurJDBCException(getClass().getSimpleName() + ".updateSQLRequest()",
          SilverpeasException.ERROR, "connecteurJDBC.MSG_CHECK_REQUEST_FAIL", error);
    }
    setValidRequest(sqlRequest);
    getCurrentConnectionInfo().withSqlRequest(sqlRequest).save();
  }

  public void updateConnectionInfo(String dataSource, String login, String password, int rowLimit)
      throws ConnecteurJDBCException {
    if (StringUtil.isDefined(dataSource)) {
      currentConnectionInfo = getCurrentConnectionInfo().withDataSourceName(dataSource)
          .withLoginAndPassword(login, password)
          .withDataMaxNumber(rowLimit);
      checkConnection(currentConnectionInfo);
      currentConnectionInfo.save();
    }
  }

  public DataSourceConnectionInfo getCurrentConnectionInfo() {
    return currentConnectionInfo;
  }

  private void checkConnection(DataSourceConnectionInfo connectionInfo)
      throws ConnecteurJDBCException {
    Connection connection = null;
    try {
      connection = connectionInfo.openConnection();
    } catch (Exception ex) {
      SilverTrace.warn("connecteurJDBC", "ConnecteurJDBCSessionControl.checkConnection()",
          "connecteurJDBC.MSG_CHECK_CONNECTION_FAIL",
          "data source: " + currentConnectionInfo.getDataSourceName() + ", login: " +
              currentConnectionInfo.getLogin() + ", password : " +
              currentConnectionInfo.getPassword());
      throw ex;
    } finally {
      DBUtil.close(connection);
    }
  }
}