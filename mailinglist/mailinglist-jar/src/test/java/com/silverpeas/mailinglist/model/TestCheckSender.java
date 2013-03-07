/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.silverpeas.mailinglist.model;

import com.silverpeas.mailinglist.AbstractSilverpeasDatasourceSpringContextTests;
import com.silverpeas.mailinglist.service.ServicesFactory;
import com.silverpeas.mailinglist.service.model.beans.InternalUser;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.jvnet.mock_javamail.Mailbox;

import java.io.IOException;
import java.sql.SQLException;

import javax.jms.QueueConnectionFactory;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.mockrunner.mock.jms.MockQueue;
import org.dbunit.database.DatabaseConnection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import com.silverpeas.jndi.SimpleMemoryContextFactory;
import com.silverpeas.mailinglist.jms.MockObjectFactory;
import com.silverpeas.mailinglist.service.model.MailingListService;

import com.stratelia.webactiv.beans.admin.AdminReference;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import static org.junit.Assert.*;

public class TestCheckSender  {

  private static final String ArchivageNotModeratedOpen_ID = "101";
  private static final String ArchivageNotModeratedClosed_ID = "102";
  
  private static DataSource dataSource;
  private static ClassPathXmlApplicationContext context;

  @BeforeClass
  public static void setUpClass() throws Exception {
    SimpleMemoryContextFactory.setUpAsInitialContext();
    context = new ClassPathXmlApplicationContext(new String[]{"/spring-checker.xml",
      "/spring-notification.xml", "/spring-jpa-hibernate.xml", "/spring-embedded-datasource.xml",
      "/spring-personalization.xml"});
    dataSource = context.getBean("jpaDataSource", DataSource.class);
    InitialContext ic = new InitialContext();
    ic.rebind("jdbc/Silverpeas", dataSource);
    DBUtil.getInstanceForTest(dataSource.getConnection());
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    DBUtil.clearTestInstance();
    SimpleMemoryContextFactory.tearDownAsInitialContext();
    context.close();
  }

  @Before
  public void init() throws Exception {
    IDatabaseConnection connection = getConnection();
    DatabaseOperation.CLEAN_INSERT.execute(connection, getDataSet());
    connection.close();
    AdminReference.getAdminService().reloadCache();
    registerMockJMS();
    Mailbox.clearAll();
  }

  @After
  public void after() throws Exception {
    IDatabaseConnection connection = getConnection();
    DatabaseOperation.DELETE_ALL.execute(connection, getDataSet());
    connection.close();
    MockObjectFactory.clearAll();
    Mailbox.clearAll();
  }

  private IDatabaseConnection getConnection() throws Exception {
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    return connection;
  }

  protected void registerMockJMS() throws Exception {
    InitialContext ic = new InitialContext();
    // Construct BasicDataSource reference
    QueueConnectionFactory refFactory = MockObjectFactory.getQueueConnectionFactory();
    ic.rebind(JNDINames.JMS_FACTORY, refFactory);
    ic.rebind(JNDINames.JMS_QUEUE, MockObjectFactory.createQueue(JNDINames.JMS_QUEUE));
    QueueConnectionFactory qconFactory = (QueueConnectionFactory) ic.lookup(JNDINames.JMS_FACTORY);
    assertNotNull(qconFactory);
    MockQueue queue = (MockQueue) ic.lookup(JNDINames.JMS_QUEUE);
    queue.clear();
  }

  protected IDataSet getDataSet() throws DataSetException, IOException {
    FlatXmlDataSet dataSet = new FlatXmlDataSetBuilder().build(TestCheckSender.class.
        getResourceAsStream("test-check-sender-dataset.xml"));;
    return dataSet;
  }

  @Test
  public void testArchivageNotModeratedOpen() {
    String email = "maggie.simpson@silverpeas.com";
    MailingList list = ServicesFactory.getMailingListService().findMailingList(
        ArchivageNotModeratedOpen_ID);
    assertNotNull(list);
    assertFalse(list.isModerated());
    assertTrue(list.isOpen());
    assertFalse(list.isNotify());
    assertEquals(
        "Liste archivage non modérée et ouverte avec un lecteur abonné", list.getName());
    assertEquals("thesimpsons@silverpeas.com", list.getSubscribedAddress());
    assertNotNull(list.getInternalSubscribers());
    assertEquals(1, list.getInternalSubscribers().size());
    assertNotNull(list.getReaders());
    assertEquals(1, list.getReaders().size());
    InternalUser reader = list.getReaders().iterator().next();
    assertEquals(email, reader.getEmail());
    assertNotNull(list.getModerators());
    assertEquals(0, list.getModerators().size());
    assertNotNull(list.getExternalSubscribers());
    assertEquals(0, list.getExternalSubscribers().size());
    assertNotNull(list.getGroupSubscribers());
    assertEquals(0, list.getGroupSubscribers().size());
    MailingListComponent component = new MailingListComponent(
        ArchivageNotModeratedOpen_ID);
    assertTrue(component.checkSender(email));
    assertTrue(list.isEmailAuthorized(email));
  }

  @Test
  public void testArchivageNotModeratedClosed() {
    String email = "lisa.simpson@silverpeas.com";
    String spammer = "joe.theplumber@spam.com";
    MailingList list = ServicesFactory.getMailingListService().findMailingList(
        ArchivageNotModeratedClosed_ID);
    assertNotNull(list);
    assertFalse(list.isModerated());
    assertFalse(list.isOpen());
    assertFalse(list.isNotify());
    assertEquals(
        "Liste archivage non modérée et fermée avec un lecteur abonné", list.getName());
    assertEquals("thesimpsons@silverpeas.com", list.getSubscribedAddress());
    assertNotNull(list.getInternalSubscribers());
    assertEquals(1, list.getInternalSubscribers().size());
    assertNotNull(list.getReaders());
    assertEquals(1, list.getReaders().size());
    assertNotNull(list.getModerators());
    assertEquals(0, list.getModerators().size());
    InternalUser reader = list.getReaders().iterator().next();
    assertEquals(email, reader.getEmail());
    assertNotNull(list.getExternalSubscribers());
    assertEquals(0, list.getExternalSubscribers().size());
    assertNotNull(list.getGroupSubscribers());
    assertEquals(0, list.getGroupSubscribers().size());
    MailingListComponent component = new MailingListComponent(
        ArchivageNotModeratedClosed_ID);
    assertTrue(component.checkSender(email));
    assertTrue(list.isEmailAuthorized(email));
    assertFalse(component.checkSender(spammer));
    assertFalse(list.isEmailAuthorized(spammer));
  }
}
