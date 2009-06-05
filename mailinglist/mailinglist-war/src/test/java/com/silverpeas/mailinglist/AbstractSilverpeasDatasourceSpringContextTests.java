package com.silverpeas.mailinglist;

import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.sql.DataSource;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public abstract class AbstractSilverpeasDatasourceSpringContextTests extends
    AbstractDependencyInjectionSpringContextTests {
  private DataSource datasource;
  private DataSourceConfiguration config;
  private static boolean registred = false;

  public DataSource getDataSource() {
    return datasource;
  }

  public void setDataSourceConfiguration(DataSourceConfiguration config) {
    this.config = config;
  }

  public DataSourceConfiguration getDataSourceConfiguration(
      DataSourceConfiguration config) {
    return this.config;
  }

  public void setDataSource(DataSource datasource) {
    this.datasource = datasource;
  }

  protected void registerDatasource() {
    if (registred) {
      return;
    }
    try {
      InitialContext ic = new InitialContext();
      // Construct BasicDataSource reference
      Reference ref = new Reference("javax.sql.DataSource",
          "org.apache.commons.dbcp.BasicDataSourceFactory", null);
      ref.add(new StringRefAddr("driverClassName", config.getDriver()));
      ref.add(new StringRefAddr("url", config.getUrl()));
      ref.add(new StringRefAddr("username", config.getUsername()));
      ref.add(new StringRefAddr("password", config.getPassword()));
      ref.add(new StringRefAddr("maxActive", "4"));
      ref.add(new StringRefAddr("maxWait", "5000"));
      ref.add(new StringRefAddr("removeAbandoned", "true"));
      ref.add(new StringRefAddr("removeAbandonedTimeout", "5000"));
      ic.rebind(config.getJndiName(), ref);
      registred = true;
    } catch (NamingException nex) {

    }
  }

  public boolean isOracle() {
    return (config.getSchema() != null && !"".equals(config.getSchema()));
  }

  protected IDatabaseConnection getConnection() throws SQLException {
    if (isOracle()) {
      try {
      Class.forName(config.getDriver()).newInstance();
      }catch(Exception ex) {
        logger.error(ex);
      }
      IDatabaseConnection connection = new DatabaseConnection(DriverManager
          .getConnection(config.getUrl(), config.getUsername(), config
              .getPassword()));
      connection.getConfig().setFeature(
          "http://www.dbunit.org/features/qualifiedTableNames", true);
      connection.getConfig().setProperty(
          "http://www.dbunit.org/properties/datatypeFactory",
          new org.dbunit.ext.oracle.OracleDataTypeFactory());
      return connection;
    }
    return new DatabaseConnection(datasource.getConnection());
  }

  protected abstract IDataSet getDataSet() throws Exception;

  protected void onSetUp() {
    registerDatasource();
    IDatabaseConnection connection = null;
    try {
      connection = getConnection();
      DatabaseOperation.CLEAN_INSERT.execute(connection, getDataSet());
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      if (connection != null) {
        try {
          connection.getConnection().close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  protected void onTearDown() {
    IDatabaseConnection connection = null;
    try {
      connection = getConnection();
      DatabaseOperation.DELETE_ALL.execute(connection, getDataSet());
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      if (connection != null) {
        try {
          connection.getConnection().close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
