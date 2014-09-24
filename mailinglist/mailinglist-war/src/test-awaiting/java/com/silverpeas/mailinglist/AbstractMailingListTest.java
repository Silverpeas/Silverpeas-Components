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
import org.silverpeas.util.DBUtil;
import org.silverpeas.util.JNDINames;
import java.sql.SQLException;
import javax.jms.QueueConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.dbunit.DataSourceDatabaseTester;
import org.dbunit.DatabaseUnitException;
import org.dbunit.IDatabaseTester;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.junit.After;
import org.junit.Before;
import org.silverpeas.core.admin.OrganisationController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public abstract class AbstractMailingListTest {

  protected Logger logger = LoggerFactory.getLogger(AbstractMailingListTest.class);
  private ConfigurableApplicationContext context;
  private IDatabaseTester databaseTester;

  protected abstract String[] getContextConfigurations();

  protected abstract IDataSet getDataSet() throws Exception;

  public DataSource getDataSource() {
    return context.getBean(DataSource.class);
  }

  public <T> T getManagedService(Class<T> beanType) {
    return context.getBean(beanType);
  }

  public OrganisationController getOrganisationController() {
    return context.getBean(OrganisationController.class);
  }

  protected void registerDatasource() {
    try {
      InitialContext ic = new InitialContext();
      ic.rebind("jdbc/Silverpeas", getDataSource());
      ic.rebind(JNDINames.DATABASE_DATASOURCE, getDataSource());
      ic.rebind(JNDINames.ADMIN_DATASOURCE, getDataSource());
      registerMockJMS(ic);
    } catch (Exception nex) {
      logger.error("Can't register datasource", nex);
    }
  }

  protected void registerMockJMS(InitialContext ic) throws NamingException {
    QueueConnectionFactory refFactory = MockObjectFactory.getQueueConnectionFactory();
    ic.rebind(JNDINames.JMS_FACTORY, refFactory);
    ic.rebind(JNDINames.JMS_QUEUE, MockObjectFactory.createQueue(JNDINames.JMS_QUEUE));
    QueueConnectionFactory qconFactory = (QueueConnectionFactory) ic.lookup(JNDINames.JMS_FACTORY);
    assertThat(qconFactory, is(notNullValue()));
    MockQueue queue = (MockQueue) ic.lookup(JNDINames.JMS_QUEUE);
    queue.clear();
  }

  protected IDatabaseConnection getConnection() throws SQLException, DatabaseUnitException {
    return new DatabaseConnection(getDataSource().getConnection());
  }

  @Before
  public void setUpTestContext() throws Exception {
    SimpleMemoryContextFactory.setUpAsInitialContext();
    context = new ClassPathXmlApplicationContext(getContextConfigurations());
    DBUtil.clearTestInstance();
    registerDatasource();

    databaseTester = new DataSourceDatabaseTester(getDataSource());
    databaseTester.setDataSet(getDataSet());
    databaseTester.onSetup();
  }

  @After
  public void tearDownTestContext() throws Exception {
    databaseTester.onTearDown();
    MockObjectFactory.clearAll();
    DBUtil.clearTestInstance();
    context.close();
    SimpleMemoryContextFactory.tearDownAsInitialContext();
  }
}
