/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.silverpeas.mailinglist;

import com.mockrunner.mock.jms.MockQueue;
import com.silverpeas.mailinglist.jms.MockObjectFactory;
import com.stratelia.webactiv.util.JNDINames;
import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.Properties;
import java.util.StringTokenizer;
import javax.jms.QueueConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.sql.DataSource;
import org.apache.commons.io.FileUtils;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public abstract class AbstractSilverpeasDatasourceSpringContextTests extends AbstractDependencyInjectionSpringContextTests {

  private DataSource datasource;
  private DataSourceConfiguration config;

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
    try {
      prepareJndi();
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
      rebind(ic, config.getJndiName(), ref);
      ic.rebind(config.getJndiName(), ref);
      rebind(ic, JNDINames.DATABASE_DATASOURCE, ref);
      ic.rebind(JNDINames.DATABASE_DATASOURCE, ref);
      rebind(ic, JNDINames.ADMIN_DATASOURCE, ref);
       ic.rebind(JNDINames.ADMIN_DATASOURCE, ref);
      registerMockJMS(ic);
    } catch (NamingException nex) {
      logger.error(nex);
    } catch (IOException ex) {
      logger.error(ex);
    }
  }


  protected void registerMockJMS(InitialContext ic) throws NamingException {
   Reference refFactory = new Reference("javax.jms.QueueConnectionFactory",
        "com.silverpeas.mailinglist.jms.MockObjectFactory", null);
    rebind(ic, JNDINames.JMS_FACTORY, refFactory);
    Reference refQueue = new Reference("javax.jms.Queue",
        "com.silverpeas.mailinglist.jms.MockObjectFactory", null);
    rebind(ic, JNDINames.JMS_QUEUE, refQueue);
    QueueConnectionFactory qconFactory = (QueueConnectionFactory) ic.lookup(JNDINames.JMS_FACTORY);
    assertNotNull(qconFactory);
    MockQueue queue = (MockQueue) ic.lookup(JNDINames.JMS_QUEUE);
    queue.clear();
  }

  public boolean isOracle() {
    return (config.getSchema() != null && !"".equals(config.getSchema()));
  }

  protected IDatabaseConnection getConnection() throws SQLException {
    if (isOracle()) {
      try {
        Class.forName(config.getDriver()).newInstance();
      } catch (Exception ex) {
        logger.error(ex);
      }
      IDatabaseConnection connection = new DatabaseConnection(DriverManager.getConnection(config.getUrl(), config.getUsername(), config.getPassword()));
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

  @Override
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

  @Override
  protected void onTearDown() throws IOException {
    IDatabaseConnection connection = null;
    try {
      connection = getConnection();
      DatabaseOperation.DELETE_ALL.execute(connection, getDataSet());
      cleanJndi();
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
    MockObjectFactory.clearAll();
    cleanJndi();
  }

  /**
   * Creates the directory for JNDI files ystem provider
   * @throws IOException
   */
  protected void prepareJndi() throws IOException {
    Properties jndiProperties = new Properties();
    jndiProperties.load(PathTestUtil.class.getClassLoader().getResourceAsStream("jndi.properties"));
    String jndiDirectoryPath = jndiProperties.getProperty("java.naming.provider.url").substring(7);
    File jndiDirectory = new File(jndiDirectoryPath);
    if (!jndiDirectory.exists()) {
      jndiDirectory.mkdirs();
      jndiDirectory.mkdir();
    }
  }

  /**
   * Deletes the directory for JNDI file system provider
   * @throws IOException
   */
  protected void cleanJndi() throws IOException {
    Properties jndiProperties = new Properties();
    jndiProperties.load(PathTestUtil.class.getClassLoader().getResourceAsStream("jndi.properties"));
    String jndiDirectoryPath = jndiProperties.getProperty("java.naming.provider.url").substring(7);
    File jndiDirectory = new File(jndiDirectoryPath);
    if (jndiDirectory.exists()) {
      FileUtils.deleteQuietly(jndiDirectory);
    }
  }

  /**
   * Workaround to be able to use Sun's JNDI file system provider on Unix
   * @param ic : the JNDI initial context
   * @param jndiName : the binding name
   * @param ref : the reference to be bound
   * @throws NamingException
   */
  protected void rebind(InitialContext ic, String jndiName, Reference ref) throws NamingException {
    Context currentContext = ic;
    StringTokenizer tokenizer = new StringTokenizer(jndiName, "/", false);
    while (tokenizer.hasMoreTokens()) {
      String name = tokenizer.nextToken();
      if (tokenizer.hasMoreTokens()) {
        try {
          currentContext = (Context) currentContext.lookup(name);
        } catch (javax.naming.NameNotFoundException nnfex) {
          currentContext = currentContext.createSubcontext(name);
        }
      } else {
        currentContext.rebind(name, ref);
      }
    }
  }
}
