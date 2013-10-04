/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.mailinglist;

import com.mockrunner.mock.jms.MockQueue;
import com.silverpeas.jndi.SimpleMemoryContextFactory;
import com.silverpeas.mailinglist.jms.MockObjectFactory;
import com.stratelia.webactiv.util.JNDINames;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;

import javax.jms.QueueConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.StringTokenizer;
import javax.inject.Inject;
import javax.inject.Named;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
public abstract class AbstractSilverpeasDatasourceSpringContextTests {

  Logger logger = LoggerFactory.getLogger(AbstractSilverpeasDatasourceSpringContextTests.class);
  @Inject
  @Named("dataSource")
  private DataSource datasource;
  
  @Inject
  private DataSourceConfiguration config;

  public DataSource getDataSource() {
    return datasource;
  }

  public void setDataSourceConfiguration(DataSourceConfiguration config) {
    this.config = config;
  }

  public DataSourceConfiguration getDataSourceConfiguration(DataSourceConfiguration config) {
    return this.config;
  }

  public void setDataSource(DataSource datasource) {
    this.datasource = datasource;
  }

  protected void registerDatasource() {
    try {
      InitialContext ic = new InitialContext();
      rebind(ic, config.getJndiName(), datasource);
      ic.rebind(config.getJndiName(), datasource);
      rebind(ic, JNDINames.DATABASE_DATASOURCE, datasource);
      ic.rebind(JNDINames.DATABASE_DATASOURCE, datasource);
      rebind(ic, JNDINames.ADMIN_DATASOURCE, datasource);
      ic.rebind(JNDINames.ADMIN_DATASOURCE, datasource);
      registerMockJMS(ic);
    } catch (Exception nex) {
      logger.error("Can't register datasource", nex);
    }
  }

  protected void registerMockJMS(InitialContext ic) throws NamingException {
    QueueConnectionFactory refFactory = MockObjectFactory.getQueueConnectionFactory();
    rebind(ic, JNDINames.JMS_FACTORY, refFactory);
    rebind(ic, JNDINames.JMS_QUEUE, MockObjectFactory.createQueue(JNDINames.JMS_QUEUE));
    QueueConnectionFactory qconFactory = (QueueConnectionFactory) ic.lookup(JNDINames.JMS_FACTORY);
    assertThat(qconFactory, is(notNullValue()));
    MockQueue queue = (MockQueue) ic.lookup(JNDINames.JMS_QUEUE);
    queue.clear();
  }

  public boolean isOracle() {
    return (config.getSchema() != null && !"".equals(config.getSchema()));
  }

  protected IDatabaseConnection getConnection() throws SQLException, DatabaseUnitException {
    if (isOracle()) {
      try {
        Class.forName(config.getDriver()).newInstance();
      } catch (Exception ex) {
        logger.error("Can't load configuration", ex);
      }
      IDatabaseConnection connection = new DatabaseConnection(DriverManager.getConnection(config.
          getUrl(), config.getUsername(), config.getPassword()));
      connection.getConfig().setProperty(
          "http://www.dbunit.org/features/qualifiedTableNames", true);
      connection.getConfig().setProperty(
          "http://www.dbunit.org/properties/datatypeFactory",
          new org.dbunit.ext.oracle.OracleDataTypeFactory());
      return connection;
    } else {
      IDatabaseConnection connection = new DatabaseConnection(datasource.getConnection());
      connection.getConfig().setProperty(
          "http://www.dbunit.org/properties/datatypeFactory",
          new org.dbunit.ext.postgresql.PostgresqlDataTypeFactory());
      return connection;
    }
  }

  protected abstract IDataSet getDataSet() throws Exception;

  @Before
  public void onSetUp() {
    SimpleMemoryContextFactory.setUpAsInitialContext();
    registerDatasource();
    IDatabaseConnection connection = null;
    try {
      connection = getConnection();
      DatabaseOperation.CLEAN_INSERT.execute(connection, getDataSet());
    } catch (Exception ex) {
      logger.error("Can't load configuration", ex);
    } finally {
      if (connection != null) {
        try {
          connection.getConnection().close();
        } catch (SQLException e) {
          logger.error("Can't load configuration", e);
        }
      }
    }
  }

  @After
  public void onTearDown() throws Exception {
    IDatabaseConnection connection = null;
    try {
      connection = getConnection();
      DatabaseOperation.DELETE_ALL.execute(connection, getDataSet());
    } catch (Exception ex) {
      logger.error("Can't load configuration", ex);
    } finally {
      if (connection != null) {
        try {
          connection.getConnection().close();
        } catch (SQLException e) {
          logger.error("Can't load configuration", e);
        }
      }
    }
    MockObjectFactory.clearAll();
    SimpleMemoryContextFactory.setUpAsInitialContext();
  }

  /**
   * Workaround to be able to use Sun's JNDI file system provider on Unix
   *
   * @param ic : the JNDI initial context
   * @param jndiName : the binding name
   * @param ref : the reference to be bound
   * @throws NamingException
   */
  protected void rebind(InitialContext ic, String jndiName, Object ref) throws NamingException {
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