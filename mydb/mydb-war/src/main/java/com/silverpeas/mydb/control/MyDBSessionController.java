/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.mydb.control;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.mydb.control.ejb.MyDBBm;
import com.silverpeas.mydb.control.ejb.MyDBBmHome;
import com.silverpeas.mydb.data.datatype.DataTypeList;
import com.silverpeas.mydb.data.date.DateFormatter;
import com.silverpeas.mydb.data.db.DbColumn;
import com.silverpeas.mydb.data.db.DbFilter;
import com.silverpeas.mydb.data.db.DbForeignKey;
import com.silverpeas.mydb.data.db.DbLine;
import com.silverpeas.mydb.data.db.DbTable;
import com.silverpeas.mydb.data.db.DbUtil;
import com.silverpeas.mydb.data.index.IndexElement;
import com.silverpeas.mydb.data.index.IndexElementComparator;
import com.silverpeas.mydb.data.index.IndexInfo;
import com.silverpeas.mydb.data.index.IndexList;
import com.silverpeas.mydb.exception.MyDBException;
import com.silverpeas.mydb.model.MyDBConnectionInfoDetail;
import com.silverpeas.mydb.model.MyDBConnectionInfoPK;
import com.silverpeas.mydb.model.MyDBRuntimeException;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.selectionPeas.jdbc.JdbcConnectorSetting;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * MyDB session control.
 * 
 * @author Antoine HEDIN
 */
public class MyDBSessionController extends AbstractComponentSessionController {

  private MyDBBm myDBEjb = null;
  private MyDBConnectionInfoDetail myDBDetail = null;
  private DriverManager driverManager;
  private JdbcConnectorSetting jdbcConnectorSetting;
  private Connection connection;
  private PreparedStatement prepStmt;
  private ResultSet rs;
  private int lineIndex = -1;
  private DbTable dbTable;
  private DbFilter dbFilter = new DbFilter();
  private FormManager formManager;
  private String[][] formParameters;
  private TableManager tableManager;
  private ResourcesWrapper resources;
  private DateFormatter dateFormatter;

  /**
   * Standard Session Controller Constructor
   * 
   * @param mainSessionCtrl
   *          The user's profile
   * @param componentContext
   *          The component's profile
   */
  public MyDBSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.silverpeas.mydb.multilang.myDBBundle",
        "com.silverpeas.mydb.settings.myDBIcons");
    resources = new ResourcesWrapper(getMultilang(), getIcon(), getSettings(),
        getLanguage());
    driverManager = new DriverManager();
    dateFormatter = new DateFormatter(resources.getString("DatePattern"));
    formManager = new FormManager(dateFormatter);
    initMyDB();
  }

  public DriverManager getDriverManager() {
    return driverManager;
  }

  public void setLineIndex(int lineIndex) {
    this.lineIndex = lineIndex;
  }

  public int getLineIndex() {
    return lineIndex;
  }

  public void removeLineIndex() {
    lineIndex = -1;
  }

  public DbFilter getDbFilter() {
    return dbFilter;
  }

  /**
   * Initializes the database filter.
   */
  public void initDbFilter() {
    dbFilter = new DbFilter();
  }

  /**
   * Update the database filter.
   * 
   * @param column
   *          The name of the column.
   * @param compare
   *          The symbol which links the column and the value.
   * @param value
   *          The value.
   */
  public void updateDbFilter(String column, String compare, String value) {
    dbFilter = new DbFilter(column, compare, value);
  }

  /**
   * Update the table's name.
   * 
   * @param tableName
   *          The new name of the table.
   * @throws MyDBException
   */
  public void updateTableName(String tableName) throws MyDBException {
    if (tableName != null && tableName.length() > 0) {
      setTableName(tableName);
    }
  }

  /**
   * @return True if the actual name of the table corresponds to an existing
   *         table in database, else return false and reset the table's name.
   */
  public boolean checkTableName() {
    String tableName = getTableName();
    try {
      if (tableName.length() > 0) {
        String[] tableNames = getTableNames();
        for (int i = 0, n = tableNames.length; i < n; i++) {
          if (tableName.equals(tableNames[i])) {
            return true;
          }
        }
        // Reach this part of code means that the actual table name does not
        // correspond to an existing table.
        // The name of the table is then reset.
        setTableName("");
      }
    } catch (MyDBException e) {
      SilverTrace.warn("myDB", "MyDBSessionController.checkTableName()",
          "myDB.MSG_TABLE_NAME_CHECK_FAILED", "TableName=" + tableName, e);
    }
    return false;
  }

  /**
   * Initializes the database connection informations.
   * 
   * @throws MyDBRuntimeException
   */
  private void initMyDB() throws MyDBRuntimeException {
    if (myDBDetail == null) {
      try {
        // load and return the current jdbc settings for the component
        MyDBBm myDBBm = getMyDBBm();
        Collection c = myDBBm.getConnectionList(new MyDBConnectionInfoPK("",
            getSpaceId(), getComponentId()));
        if (c.size() > 1) {
          throw new MyDBRuntimeException("myDBSessionController.initMyDB()",
              SilverpeasException.FATAL, "myDB.EX_MUST_BE_ONLY_ONE_CONNECTION");
        }

        Iterator i = c.iterator();
        if (i.hasNext()) {
          myDBDetail = (MyDBConnectionInfoDetail) i.next();
        }
      } catch (Exception e) {
        SilverTrace.error("myDB", "myDBSessionController.initMyDB()",
            "myDB.EX_CONNECTION_SETTING_INIT_FAILED", e);
        throw new MyDBRuntimeException("myDBSessionController.initMyDB()",
            SilverpeasException.FATAL,
            "myDB.EX_CONNECTION_SETTING_INIT_FAILED", e);
      }
    }
  }

  /**
   * Update the table's name and the corresponding connection information.
   * 
   * @param tableName
   *          The new table's name.
   * @throws MyDBException
   */
  public void setTableName(String tableName) throws MyDBException {
    if (myDBDetail == null) {
      myDBDetail = new MyDBConnectionInfoDetail();
      myDBDetail.setPK(new MyDBConnectionInfoPK("", getSpaceId(),
          getComponentId()));
      newMyDB();
    }
    if (myDBDetail != null) {
      myDBDetail.setTableName(tableName);
    }
    updateMyDB();
  }

  /**
   * @return The bean component, after having initialized it if needed.
   * @throws MyDBException
   */
  private MyDBBm getMyDBBm() throws MyDBException {
    if (myDBEjb == null) {
      try {
        MyDBBmHome myDBEjbHome = (MyDBBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.MYDBBM_EJBHOME, MyDBBmHome.class);
        myDBEjb = myDBEjbHome.create();
      } catch (Exception e) {
        throw new MyDBException("myDBSessionController.getMyDBBm()",
            SilverpeasException.ERROR, "myDB.EX_EJB_CREATION_FAILED", e);
      }
    }
    return myDBEjb;
  }

  /**
   * Loads the connection informations.
   * 
   * @throws MyDBException
   */
  private void newMyDB() throws MyDBException {
    if (myDBDetail != null) {
      try {
        // load and return the current jdbc settings for the component
        MyDBBm myDBBm = getMyDBBm();
        myDBDetail.setPK(myDBBm.addConnection(myDBDetail));
      } catch (Exception e) {
        throw new MyDBException("myDBSessionController.newMyDB()",
            SilverpeasException.FATAL, "myDB.EX_NEW_CONNECTION_FAILED", e);
      }
    }
  }

  public TableManager getTableManager() {
    return tableManager;
  }

  /**
   * Initializes the table manager.
   * 
   * @param mode
   *          the table mode (creation or modification).
   * @param originPage
   *          The origin page. Indeed, table mode can be accessed from the table
   *          selection page or the table detail page. This information is also
   *          memorized to go back to the origin page when table mode is left.
   */
  public void initTableManager(int mode, String originPage) {
    String driverName = getJdbcDriverName();
    ArrayList keywords = driverManager
        .getDatabaseKeywordsListForDriver(driverName);
    DataTypeList dataTypeList = driverManager
        .getDataTypeListForDriver(driverName);
    tableManager = new TableManager(mode, originPage, keywords, dataTypeList);
  }

  /**
   * @return True if the table manager is initialized.
   */
  public boolean hasTableManager() {
    return (tableManager != null);
  }

  /**
   * Reset the table manager.
   */
  public void resetTableManager() {
    tableManager = null;
  }

  public ResourcesWrapper getResources() {
    return resources;
  }

  public DateFormatter getDateFormatter() {
    return dateFormatter;
  }

  /**
   * Updates the database connection informations.
   * 
   * @param JDBCdriverName
   *          The name of the driver.
   * @param JDBCurl
   *          The URL to access the database.
   * @param login
   *          The login to access the database.
   * @param password
   *          The password to access the database.
   * @param rowLimit
   *          The maximum number of elements to display in a response to a
   *          database request.
   * @throws MyDBException
   */
  public void updateConnection(String JDBCdriverName, String JDBCurl,
      String login, String password, int rowLimit) throws MyDBException {
    if (myDBDetail != null) {
      myDBDetail.setJdbcDriverName(JDBCdriverName);
      myDBDetail.setJdbcUrl(JDBCurl);
      myDBDetail.setLogin(login);
      myDBDetail.setPassword(password);
      myDBDetail.setRowLimit(rowLimit);
      updateMyDB();
    } else {
      myDBDetail = new MyDBConnectionInfoDetail();
      myDBDetail.setPK(new MyDBConnectionInfoPK("", getSpaceId(),
          getComponentId()));
      myDBDetail.setJdbcDriverName(JDBCdriverName);
      myDBDetail.setJdbcUrl(JDBCurl);
      myDBDetail.setLogin(login);
      myDBDetail.setPassword(password);
      myDBDetail.setRowLimit(rowLimit);
      newMyDB();
    }
  }

  /**
   * Initializes the JDBC connector setting.
   */
  public void initJdbcConnectorSetting() {
    String driverClassName = driverManager
        .getDriverClassName(getJdbcDriverName());
    jdbcConnectorSetting = new JdbcConnectorSetting(driverClassName,
        getJdbcUrl(), getLogin(), getPassword());
  }

  /**
   * @return the JDBC connector setting, after having initialized it if needed.
   */
  public JdbcConnectorSetting getJdbcConnectorSetting() {
    if (jdbcConnectorSetting == null) {
      initJdbcConnectorSetting();
    }
    return jdbcConnectorSetting;
  }

  /**
   * Updates the database connection informations.
   * 
   * @throws MyDBException
   */
  private void updateMyDB() throws MyDBException {
    if (myDBDetail != null) {
      try {
        // load and return the current jdbc settings for the component
        MyDBBm myDBBm = getMyDBBm();
        myDBBm.updateConnection(myDBDetail);
      } catch (Exception e) {
        throw new MyDBException("myDBSessionController.updateMyDB()",
            SilverpeasException.FATAL, "myDB.EX_UPDATE_CONNECTION_FAILED", e);
      }
    }
  }

  public String getJdbcDriverName() {
    return (myDBDetail != null ? myDBDetail.getJdbcDriverName() : null);
  }

  public String getJdbcUrl() {
    return (myDBDetail != null ? myDBDetail.getJdbcUrl() : null);
  }

  public String getLogin() {
    return (myDBDetail != null ? myDBDetail.getLogin() : "");
  }

  public String getPassword() {
    return (myDBDetail != null ? myDBDetail.getPassword() : "");
  }

  public String getTableName() {
    return (myDBDetail != null ? myDBDetail.getTableName() : "");
  }

  public int getRowLimit() {
    return (myDBDetail != null ? myDBDetail.getRowLimit() : -1);
  }

  /**
   * Starts the connection to the database.
   */
  private void startConnection() {
    if (myDBDetail != null) {
      try {
        connection = getConnection();
      } catch (SQLException e) {
        SilverTrace.warn("myDB", "MyDBSessionController.startConnection()",
            "myDB.MSG_CONNECTION_NOT_STARTED", e);
        connection = null;
      }
    }
  }

  /**
   * Closes the database connection and its associated elements (result set and
   * prepared statement).
   */
  private void closeConnection() {
    try {
      if (rs != null) {
        rs.close();
        rs = null;
      }
      if (prepStmt != null) {
        prepStmt.close();
        prepStmt = null;
      }
      if (connection != null) {
        connection.close();
        connection = null;
      }
    } catch (SQLException e) {
      SilverTrace.warn("myDB", "MyDBSessionController.closeConnection()",
          "myDB.MSG_CONNECTION_NOT_CLOSED", e);
    }
  }

  /**
   * @return True if the database connection is available.
   */
  public boolean checkConnection() {
    try {
      connection = getConnection();
      if (connection != null) {
        return true;
      } else {
        driverManager.resetDriver();
      }
    } catch (SQLException e) {
      SilverTrace.warn("myDB", "MyDBSessionController.checkConnection()",
          "myDB.MSG_CONNECTION_NOT_CHECKED", e);
    } finally {
      closeConnection();
    }
    return false;
  }

  /**
   * @return The list of tables names available in database.
   */
  public String[] getTableNames() {
    Vector tableVector = new Vector();
    startConnection();
    if (connection == null) {
      SilverTrace.warn("myDB", "MyDBSessionController.getTableNames()",
          "myDB.MSG_CONNECTION_NOT_STARTED");
    } else {
      try {
        DatabaseMetaData dbMetaData = connection.getMetaData();
        if (dbMetaData != null) {
          rs = dbMetaData.getTables(null, null, null, new String[] { "TABLE" });
          while (rs.next()) {
            tableVector.addElement(rs.getString("TABLE_NAME"));
          }
        }
      } catch (SQLException e) {
        SilverTrace.warn("myDBSessionController",
            "MyDBSessionController.getTableNames()",
            "myDB.MSG_CANNOT_GET_TABLES_NAMES");
      } finally {
        closeConnection();
      }
    }
    return (String[]) tableVector.toArray(new String[tableVector.size()]);
  }

  /**
   * @return the database table corresponding to the current informations
   *         (connection, table's name,...).
   * @throws MyDBException
   */
  public DbTable getDbTable() throws MyDBException {
    dbTable = null;
    String tableName = getTableName();
    if (tableName != null && tableName.length() > 0) {
      startConnection();
      if (connection != null) {
        StringBuffer columnNameSb = new StringBuffer(100);
        try {
          DatabaseMetaData dbMetaData = connection.getMetaData();

          rs = dbMetaData.getTables(null, "%", tableName,
              new String[] { "TABLE" });
          if (rs.next()) {
            // Table.
            dbTable = new DbTable(tableName);
            dbTable.setLineIndex(lineIndex);

            // Columns.
            rs = dbMetaData.getColumns(null, "%", tableName, "%");
            while (rs.next()) {
              String columnName = rs.getString(DbColumn.COLUMN_NAME);
              int dataType = rs.getInt(DbColumn.DATA_TYPE);
              int dataSize = rs.getInt(DbColumn.COLUMN_SIZE);
              int nullable = rs.getInt(DbColumn.NULLABLE);
              boolean isNull = (nullable == 1);
              String defaultValue = rs.getString(DbColumn.COLUMN_DEF);
              if (defaultValue != null && defaultValue.length() > 0) {
                int index = defaultValue.indexOf("::");
                if (index != -1) {
                  defaultValue = defaultValue.substring(0, index);
                  if (defaultValue.startsWith("'")
                      && defaultValue.endsWith("'")) {
                    defaultValue = defaultValue.substring(1, defaultValue
                        .length() - 1);
                  }
                }
              }
              DbColumn newColumn = new DbColumn(columnName, dataType, dataSize,
                  isNull, defaultValue);
              dbTable.addColumn(newColumn);
              columnNameSb.append(columnName).append(",");
            }

            // Primary keys.
            rs = dbMetaData.getPrimaryKeys(null, null, tableName);
            String primaryKey;
            while (rs.next()) {
              primaryKey = rs.getString("COLUMN_NAME");
              dbTable.getPrimaryKey().addColumn(primaryKey);
              dbTable.getColumn(primaryKey).setReadOnly(true);
            }

            String fkName;
            String fkTableName;
            String fkColumnName;
            String pkTableName;
            String pkColumnName;

            // Imported foreign keys.
            rs = dbMetaData.getImportedKeys(null, null, tableName);
            while (rs.next()) {
              fkName = rs.getString(DbColumn.FK_NAME);
              fkColumnName = rs.getString(DbColumn.FKCOLUMN_NAME);
              pkTableName = rs.getString(DbColumn.PKTABLE_NAME);
              pkColumnName = rs.getString(DbColumn.PKCOLUMN_NAME);
              dbTable.getColumn(fkColumnName).setImportedForeignKey(
                  new DbForeignKey(fkName, pkTableName, pkColumnName));
            }

            // Exported foreign keys.
            rs = dbMetaData.getExportedKeys(null, null, tableName);
            while (rs.next()) {
              fkName = rs.getString(DbColumn.FK_NAME);
              fkTableName = rs.getString(DbColumn.FKTABLE_NAME);
              fkColumnName = rs.getString(DbColumn.FKCOLUMN_NAME);
              pkColumnName = rs.getString(DbColumn.PKCOLUMN_NAME);
              dbTable.getColumn(pkColumnName).addExportedForeignKey(
                  new DbForeignKey(fkName, fkTableName, fkColumnName));
            }
          }
        } catch (SQLException e) {
          throw new MyDBException("myDBSessionController.getDbTable()",
              SilverpeasException.ERROR,
              "myDB.EX_CANNOT_GET_TABLE_DESCRIPTION", "TableName : "
                  + tableName, e);
        }
        closeConnection();

        if (dbTable != null) {
          final String query = new StringBuffer(100).append("select ").append(
              columnNameSb.substring(0, columnNameSb.length() - 1)).append(
              " from ").append(tableName).append(
              dbFilter.getQueryFilter(dbTable)).toString();
          startConnection();
          try {
            Statement stmt = connection.createStatement();
            int maxRows = getRowLimit();
            if (maxRows != -1) {
              stmt.setMaxRows(maxRows);
            }
            rs = stmt.executeQuery(query);

            // Auto-increment columns.
            ResultSetMetaData rsmd = rs.getMetaData();
            for (int i = 1, n = rsmd.getColumnCount(); i <= n; i++) {
              if (rsmd.isAutoIncrement(i)) {
                DbColumn dbColumn = dbTable.getColumn(rsmd.getColumnName(i));
                dbColumn.setAutoIncrement(true);
                dbColumn.setReadOnly(true);
                dbColumn.setDefaultValue(null);
              }
            }

            // Data.
            String[] columnsNames = dbTable.getColumnsNames();
            final int columnsNamesCount = columnsNames.length;
            int i;
            boolean manualFilter = dbFilter.isManualFilter();
            String filterColumn = dbFilter.getColumn();
            String filterValue = dbFilter.getValue().toLowerCase();
            String data;
            String value;
            while (rs.next()) {
              data = (!filterColumn.equals(DbFilter.ALL) ? rs
                  .getString(filterColumn) : null);
              if ((!manualFilter)
                  || ((data != null) && (data.toLowerCase()
                      .indexOf(filterValue) != -1))) {
                // A line is added into the table if :
                // - the filter is not manual (the initial request was completed
                // with the filter
                // criteria successfully)
                // - the filter is manual (example : like on a numeric column)
                DbLine dbLine = new DbLine();
                i = 0;
                while (i < columnsNamesCount) {
                  value = rs.getString(i + 1);
                  if (dbTable.getColumn(i).getDataType() == Types.DATE) {
                    try {
                      value = dateFormatter.sqlToString(value);
                    } catch (ParseException e) {
                      value = null;
                      SilverTrace.warn("myDBSessionController",
                          "MyDBSessionController.getDbTable()",
                          "myDB.MSG_CANNOT_GET_TABLE_LINE", "TableName="
                              + tableName, e);
                    }
                  }
                  dbLine.addData(columnsNames[i], value);
                  i++;
                }
                dbTable.addLine(dbLine);
              }
            }
          } catch (SQLException e) {
            throw new MyDBException("myDBSessionController.getDbTable()",
                SilverpeasException.ERROR, "myDB.EX_CANNOT_GET_TABLE_DATA",
                "TableName : " + tableName, e);
          }
          closeConnection();
        }
      }
    }
    if (dbTable == null) {
      setTableName("");
    }
    return dbTable;
  }

  /**
   * @param tableName
   *          The table's name.
   * @return The index informations about the table which allow to determinate
   *         foreign keys links between the different columns of the table.
   * @throws MyDBException
   */
  public IndexList getIndexInfo(String tableName) throws MyDBException {
    IndexList indexList = new IndexList();
    if (tableName != null && tableName.length() > 0) {
      startConnection();
      if (connection != null) {
        try {
          DatabaseMetaData dbMetaData = connection.getMetaData();
          ResultSet rs = dbMetaData.getIndexInfo(null, null, tableName, false,
              false);
          ArrayList indexElements = new ArrayList();
          String indexName;
          String columnName;
          short position;
          while (rs.next()) {
            indexName = rs.getString(IndexElement.INDEX_NAME);
            columnName = rs.getString(IndexElement.COLUMN_NAME);
            position = rs.getShort(IndexElement.ORDINAL_POSITION);
            indexElements
                .add(new IndexElement(indexName, columnName, position));

            if (!indexList.containsColumn(columnName)) {
              indexList.addColumn(new DbColumn(columnName));
            }
          }
          Collections.sort(indexElements, new IndexElementComparator());
          indexList.sortColumns();

          IndexElement indexElement;
          String name;
          for (int i = 0, n = indexElements.size(); i < n; i++) {
            indexElement = (IndexElement) indexElements.get(i);
            name = indexElement.getIndexName();
            if (!indexList.containsIndexInfo(name)) {
              indexList.addIndexInfo(new IndexInfo(name));
            }
            indexList.getIndexInfo(name).addColumn(indexElement.getColumn());
          }

          if (indexList.getColumnsCount() > 0) {
            DbColumn column;
            rs = dbMetaData.getColumns(null, "%", tableName, "%");
            while (rs.next()) {
              columnName = rs.getString(DbColumn.COLUMN_NAME);
              if (indexList.containsColumn(columnName)) {
                column = indexList.getColumn(columnName);
                column.setDataType(rs.getInt(DbColumn.DATA_TYPE));
                column.setDataSize(rs.getInt(DbColumn.COLUMN_SIZE));
              }
            }
          }

          indexList.check(tableManager.getDataTypeList());
        } catch (SQLException e) {
          throw new MyDBException("myDBSessionController.getIndexInfo()",
              SilverpeasException.ERROR, "myDB.EX_CANNOT_GET_TABLE_INDEX_INFO",
              "TableName : " + tableName, e);
        }
        closeConnection();
      }
    }
    return indexList;
  }

  /**
   * @return An error message if the content of the new line enters in conflict
   *         (respect of primary key) with data already present in database.
   *         Returns null if no error is detected.
   */
  public String getLineCreationErrorMessage() {
    if (!dbTable.getPrimaryKey().isEmpty()) {
      try {
        connection = getConnection();
        String[] primaryKeys = dbTable.getPrimaryKey().getColumns();
        StringBuffer query = new StringBuffer(100).append("select ").append(
            primaryKeys[0]).append(" from ").append(dbTable.getName()).append(
            " where ");
        final int n = primaryKeys.length;
        for (int i = 0; i < n; i++) {
          if (i > 0) {
            query.append(" and ");
          }
          query.append(primaryKeys[i]).append(" = ?");
        }
        prepStmt = connection.prepareStatement(query.toString());
        int dataType;
        String value;
        for (int i = 0; i < n; i++) {
          value = getFormParameter(primaryKeys[i]);
          dataType = dbTable.getColumn(primaryKeys[i]).getDataType();
          setValueByType(value, dataType, i + 1);
        }
        rs = prepStmt.executeQuery();

        if (rs.next()) {
          if (n == 1) {
            return MessageFormat.format(resources
                .getString("LineCreationNotAllowed1"), new String[] {
                primaryKeys[0], getFormParameter(primaryKeys[0]) });
          } else {
            StringBuffer columnsSb = new StringBuffer();
            StringBuffer valuesSb = new StringBuffer();
            for (int i = 0; i < n; i++) {
              if (i > 0) {
                columnsSb.append(" ; ");
                valuesSb.append(" ; ");
              }
              columnsSb.append(primaryKeys[i]);
              valuesSb.append(getFormParameter(primaryKeys[i]));
            }
            return MessageFormat.format(resources
                .getString("LineCreationNotAllowedN"), new String[] {
                columnsSb.toString(), valuesSb.toString() });
          }
        }
      } catch (SQLException e) {
        SilverTrace.warn("myDB",
            "MyDBSessionController.getLineCreationErrorMessage()",
            "myDB.MSG_CANNOT_CHECK_LINE_CREATION", e);
      } finally {
        closeConnection();
      }
    }
    return null;
  }

  /**
   * @return An error message caused by the line creation attempt which could
   *         not be anticipated by the method getLineCreationErrorMessage. The
   *         line is added to the table if no error occurred.
   */
  public String createDbLine() {
    try {
      connection = getConnection();

      String[] columnsNames = dbTable.getColumnsNames(true);

      StringBuffer query = new StringBuffer(100).append("insert into ").append(
          dbTable.getName()).append(" (").append(
          DbUtil.getListAsString(columnsNames)).append(") values (");
      for (int i = 0, n = columnsNames.length; i < n; i++) {
        if (i > 0) {
          query.append(", ");
        }
        query.append("?");
      }
      query.append(")");
      prepStmt = connection.prepareStatement(query.toString());

      DbColumn dbColumn;
      int dataType;
      String value;
      String columnName;
      int psIndex = 1;
      for (int i = 0, n = columnsNames.length; i < n; i++) {
        columnName = columnsNames[i];
        value = formParameters[i][1];
        dbColumn = dbTable.getColumn(columnName);
        dataType = dbColumn.getDataType();
        setValueByType(value, dataType, psIndex);
        psIndex++;
      }

      prepStmt.executeUpdate();
    } catch (SQLException e) {
      SilverTrace.warn("myDB", "MyDBSessionController.createDbLine()",
          "myDB.MSG_CANNOT_CREATE_LINE", e);
      return MessageFormat.format(resources.getString("LineCreationError"),
          new String[] { e.getMessage() });
    } finally {
      closeConnection();
    }
    return null;
  }

  /**
   * @return An error message caused by the line update attempt. The line is
   *         updated if no error occurred.
   */
  public String updateDbData() {
    try {
      connection = getConnection();

      StringBuffer query = new StringBuffer(100).append("update ").append(
          dbTable.getName()).append(" set ");
      String[] columnsNames = dbTable.getColumnsNames(true);
      for (int i = 0, n = columnsNames.length; i < n; i++) {
        if (i > 0) {
          query.append(", ");
        }
        query.append(columnsNames[i]).append(" = ?");
      }
      query.append(" where ");
      String[] primaryKeys = dbTable.getPrimaryKey().getColumns();
      for (int i = 0, n = primaryKeys.length; i < n; i++) {
        if (i > 0) {
          query.append(" and ");
        }
        query.append(primaryKeys[i]).append(" = ?");
      }

      prepStmt = connection.prepareStatement(query.toString());

      DbColumn dbColumn;
      int dataType;
      String value;
      String columnName;
      int psIndex = 1;
      for (int i = 0, n = columnsNames.length; i < n; i++) {
        columnName = columnsNames[i];
        value = formParameters[i][1];
        dbColumn = dbTable.getColumn(columnName);
        dataType = dbColumn.getDataType();
        setValueByType(value, dataType, psIndex);
        psIndex++;
      }

      DbLine dbLine = dbTable.getSelectedLine();
      for (int i = 0, n = primaryKeys.length; i < n; i++) {
        value = dbLine.getData(primaryKeys[i]);
        dbColumn = dbTable.getColumn(primaryKeys[i]);
        dataType = dbColumn.getDataType();
        setValueByType(value, dataType, psIndex);
        psIndex++;
      }
      prepStmt.executeUpdate();
    } catch (SQLException e) {
      SilverTrace.warn("myDB", "MyDBSessionController.updateDbData()",
          "myDB.MSG_CANNOT_UPDATE_LINE", e);
      return MessageFormat.format(resources.getString("LineUpdateError"),
          new String[] { e.getMessage() });
    } finally {
      closeConnection();
    }
    return null;
  }

  /**
   * @return An error message if the content of the line is linked to others
   *         data by foreign keys and makes the deletion impossible (ex : data
   *         present in this line and only in this line into the table but
   *         referenced by an other one).
   */
  public String getLineDeletionErrorMessage() {
    dbTable.setLineIndex(lineIndex);
    DbColumn[] columns = dbTable.getColumnsWithExportedForeignKeys();
    int columnsCount = columns.length;
    if (columnsCount > 0) {
      try {
        connection = getConnection();
        String query;
        int resultCount;

        String tableName = dbTable.getName();
        DbLine dbLine = dbTable.getSelectedLine();
        DbColumn column;
        String columnName;
        int dataType;
        DbForeignKey[] foreignKeys;
        String fkTableName;
        String fkColumnName;
        String value;
        for (int i = 0; i < columnsCount; i++) {
          column = columns[i];
          columnName = column.getName();
          value = dbLine.getData(columnName);
          if (value != null && value.length() > 0) {
            dataType = column.getDataType();

            query = new StringBuffer(100).append("select ").append(columnName)
                .append(" from ").append(tableName).append(" where ").append(
                    columnName).append(" = ?").toString();
            prepStmt = connection.prepareStatement(query);
            setValueByType(value, dataType, 1);
            rs = prepStmt.executeQuery();
            // Nombre de lignes dont la colonne traitée possède la même valeur
            // que la ligne courante.
            resultCount = 0;
            while (rs.next() && resultCount < 2) {
              resultCount++;
            }

            if (resultCount == 1) {
              // La ligne courante possède, pour la colonne traitée, une valeur
              // unique. On vérifie alors
              // si elle est référencée via ses clés étrangères, dans d'autres
              // tables.
              foreignKeys = column.getExportedForeignKeys();
              for (int j = 0, m = foreignKeys.length; j < m; j++) {
                fkTableName = foreignKeys[j].getTableName();
                fkColumnName = foreignKeys[j].getColumnName();
                query = new StringBuffer(100).append("select ").append(
                    fkColumnName).append(" from ").append(fkTableName).append(
                    " where ").append(fkColumnName).append(" = ?").toString();
                prepStmt = connection.prepareStatement(query);
                setValueByType(value, dataType, 1);
                rs = prepStmt.executeQuery();
                if (rs.next()) {
                  // Au moins une ligne de la table définie dans la clé
                  // étrangère contient la même
                  // valeur que la ligne en cours de suppression.
                  // envoi d'un message d'erreur.
                  return MessageFormat.format(resources
                      .getString("LineDeletionNotAllowed"), new String[] {
                      value, columnName, fkColumnName, fkTableName });
                }
              }
            }
          }
        }
      } catch (SQLException e) {
        SilverTrace.warn("myDB",
            "MyDBSessionController.getLineDeletionErrorMessage()",
            "myDB.MSG_CANNOT_CHECK_LINE_DELETION", e);
      } finally {
        closeConnection();
      }
    }
    return null;
  }

  /**
   * @return An error message caused by the line deletion attempt which could
   *         not be anticipated by the method getLineDeletionErrorMessage. The
   *         line is removed if no error occurred.
   */
  public String deleteDbData() {
    dbTable.setLineIndex(lineIndex);
    try {
      connection = getConnection();
      StringBuffer query = new StringBuffer(100).append("delete from ").append(
          dbTable.getName()).append(" where ");
      String[] primaryKeys = dbTable.getPrimaryKey().getColumns();
      final int n = primaryKeys.length;
      for (int i = 0; i < n; i++) {
        if (i > 0) {
          query.append(" and ");
        }
        query.append(primaryKeys[i]).append(" = ?");
      }
      prepStmt = connection.prepareStatement(query.toString());
      DbLine dbLine = dbTable.getSelectedLine();
      int dataType;
      String value;
      for (int i = 0; i < n; i++) {
        value = dbLine.getData(primaryKeys[i]);
        dataType = dbTable.getColumn(primaryKeys[i]).getDataType();
        setValueByType(value, dataType, i + 1);
      }
      prepStmt.executeUpdate();
      closeConnection();
    } catch (SQLException e) {
      SilverTrace.warn("myDB", "MyDBSessionController.deleteDbData()",
          "myDB.MSG_CANNOT_DELETE_LINE", e);
      return MessageFormat.format(resources.getString("LineDeletionError"),
          new String[] { e.getMessage() });
    } finally {
      closeConnection();
    }
    return null;
  }

  /**
   * @param consultation
   *          The flag indicating if data have to be displayed as labels or
   *          input fields.
   * @param newRecord
   *          The flag which indicates if the form describes a new record or a
   *          record to update.
   * @param beanName
   *          Le bean's name.
   * @return The form describing the record to create/modify.
   * @throws MyDBException
   */
  public Form getForm(boolean consultation, boolean newRecord, String beanName)
      throws MyDBException {
    dbTable.setLineIndex(lineIndex);
    return formManager.getForm(dbTable, resources, consultation, newRecord,
        beanName, getComponentId(), "getJdbcConnectorSetting");
  }

  /**
   * @param newRecord
   *          The flag which indicated if the form describes a new record or a
   *          record to update.
   * @return A data record representing the record to create/modify.
   * @throws FormException
   */
  public DataRecord getRecord(boolean newRecord) throws FormException {
    if (newRecord) {
      return formManager.getDataRecord(formParameters);
    } else {
      return formManager.getDataRecord(dbTable.getSelectedLine());
    }
  }

  /**
   * Loads in the form's parameters the values contained into the map.
   * 
   * @param parameterMap
   *          The map which contains the record's describing data.
   */
  public void initFormParameters(Map parameterMap) {
    DbColumn[] columns = dbTable.getColumns();
    final int n = columns.length;
    formParameters = new String[n][2];
    DbColumn column;
    String columnName;
    String fieldKey;
    String value;
    for (int i = 0; i < n; i++) {
      column = columns[i];
      columnName = column.getName();
      if (parameterMap != null) {
        fieldKey = FormManager.FIELD_PREFIX + columnName;
        if (parameterMap.containsKey(fieldKey)) {
          value = ((String[]) parameterMap.get(fieldKey))[0];
          if (value == null || value.length() == 0) {
            value = null;
          }
        } else {
          value = null;
        }
      } else {
        // Cas d'une première arrivée sur un formulaire de création :
        // utilisation de la valeur par défaut de la
        // colonne.
        value = column.getDefaultValue();
      }
      formParameters[i] = new String[] { columnName, value };
    }
  }

  /**
   * @param key
   *          The name of the searched parameter.
   * @return The value associated to the key, null if the key is not found in
   *         the form's parameters.
   */
  public String getFormParameter(String key) {
    for (int i = 0, n = formParameters.length; i < n; i++) {
      if (formParameters[i][0].equals(key)) {
        return formParameters[i][1];
      }
    }
    return null;
  }

  /**
   * Executes the different queries to create the table corresponding to the
   * current informations of the controller.
   * 
   * @return True if the creation ended successfully.
   */
  public boolean createTable() {
    String query = "";
    try {
      connection = getConnection();
      Statement stmt = connection.createStatement();

      query = tableManager.getTableCreationQuery();
      stmt.execute(query);

      String[] keysQueries = tableManager.getUnicityKeysQueries();
      for (int i = 0, n = keysQueries.length; i < n; i++) {
        query = keysQueries[i];
        stmt.executeUpdate(query);
      }

      keysQueries = tableManager.getForeignKeysQueries();
      for (int i = 0, n = keysQueries.length; i < n; i++) {
        query = keysQueries[i];
        stmt.executeUpdate(query);
      }

      return true;
    } catch (SQLException e) {
      SilverTrace.warn("myDB", "MyDBSessionController.createTable()",
          "myDB.MSG_CANNOT_CREATE_TABLE", e);
      tableManager.setErrorLabel(MessageFormat.format(resources
          .getString("TableCreationError"), new String[] { e.getMessage() }));
      return false;
    } finally {
      closeConnection();
    }
  }

  /**
   * Removes from the database the table corresponding to the current
   * informations of the controller.
   * 
   * @return True if the deletion ended successfully.
   */
  public boolean dropTable() {
    try {
      String tableName = tableManager.getTable().getName();
      connection = getConnection();
      DatabaseMetaData dbMetaData = connection.getMetaData();
      rs = dbMetaData.getTables(null, "%", tableName, new String[] { "TABLE" });
      if (rs.next()) {
        // Vérification de l'existence de la table.
        Statement stmt = connection.createStatement();
        String query = "DROP TABLE " + tableName;
        stmt.execute(query);
      }
      return true;
    } catch (SQLException e) {
      SilverTrace.warn("myDB", "MyDBSessionController.dropTable()",
          "myDB.MSG_CANNOT_DELETE_TABLE", e);
      tableManager.setErrorLabel(MessageFormat.format(resources
          .getString("TableDeletionError"), new String[] { e.getMessage() }));
      return false;
    } finally {
      closeConnection();
    }
  }

  /**
   * @return A database connection corresponding to JDBC properties of the
   *         controller.
   * @throws SQLException
   */
  private Connection getConnection() throws SQLException {
    Properties info = new Properties();
    info.setProperty("user", getLogin());
    info.setProperty("password", getPassword());
    return driverManager.getDriver(getJdbcDriverName()).connect(getJdbcUrl(),
        info);
  }

  /**
   * Completes the statement with the value by respecting its type.
   * 
   * @param value
   *          The value to store in database.
   * @param dataType
   *          The type of the value.
   * @param index
   *          The index of the data in the statement.
   * @throws NumberFormatException
   * @throws SQLException
   */
  private void setValueByType(String value, int dataType, int index)
      throws NumberFormatException, SQLException {
    if (value == null) {
      switch (dataType) {
        case Types.CHAR:
          prepStmt.setNull(index, Types.CHAR);
          break;

        case Types.VARCHAR:
          prepStmt.setNull(index, Types.VARCHAR);
          break;

        case Types.INTEGER:
          prepStmt.setNull(index, Types.INTEGER);
          break;

        case Types.DOUBLE:
          prepStmt.setNull(index, Types.DOUBLE);
          break;

        case Types.FLOAT:
          prepStmt.setNull(index, Types.FLOAT);
          break;

        case Types.REAL:
          prepStmt.setNull(index, Types.REAL);
          break;

        case Types.SMALLINT:
          prepStmt.setNull(index, Types.SMALLINT);
          break;

        case Types.BIGINT:
          prepStmt.setNull(index, Types.BIGINT);
          break;

        case Types.NUMERIC:
          prepStmt.setNull(index, Types.NUMERIC);
          break;

        case Types.DATE:
          prepStmt.setNull(index, Types.DATE);
          break;

        case Types.BOOLEAN:
          prepStmt.setNull(index, Types.BOOLEAN);
          break;

        case Types.TIME:
          prepStmt.setNull(index, Types.TIME);
          break;

        case Types.TIMESTAMP:
          prepStmt.setNull(index, Types.TIMESTAMP);
          break;

        default:
          prepStmt.setNull(index, Types.VARCHAR);
          break;
      }
    } else {
      switch (dataType) {
        case Types.CHAR:
        case Types.VARCHAR:
          prepStmt.setString(index, value);
          break;

        case Types.INTEGER:
          prepStmt.setInt(index, Integer.parseInt(value));
          break;

        case Types.DOUBLE:
          prepStmt.setDouble(index, Double.parseDouble(value));
          break;

        case Types.FLOAT:
        case Types.REAL:
          prepStmt.setFloat(index, Float.parseFloat(value));
          break;

        case Types.SMALLINT:
          prepStmt.setShort(index, Short.parseShort(value));
          break;

        case Types.BIGINT:
          prepStmt.setLong(index, Long.parseLong(value));
          break;

        case Types.NUMERIC:
          prepStmt.setBigDecimal(index, new BigDecimal(value));
          break;

        case Types.DATE:
          // Format dd/mm/yyyy
          try {
            prepStmt.setDate(index, dateFormatter.stringToSql(value));
          } catch (ParseException e) {
            SilverTrace.warn("myDB", "MyDBSessionController.setValueByType()",
                "myDB.MSG_CANNOT_PARSE_DATE", "Date=" + value, e);
            prepStmt.setNull(index, Types.DATE);
          }
          break;

        case Types.BOOLEAN:
          prepStmt.setBoolean(index, Boolean.getBoolean(value));
          break;

        case Types.TIME:
          prepStmt.setTime(index, Time.valueOf(value));
          break;

        case Types.TIMESTAMP:
          prepStmt.setTimestamp(index, Timestamp.valueOf(value));
          break;

        default:
          prepStmt.setString(index, value);
          break;
      }
    }
  }
}
