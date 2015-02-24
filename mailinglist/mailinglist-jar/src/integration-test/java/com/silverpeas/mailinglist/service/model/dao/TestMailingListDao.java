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
package com.silverpeas.mailinglist.service.model.dao;

import com.silverpeas.mailinglist.MailingListWarBuilder;
import com.silverpeas.mailinglist.service.model.TestMailingListService;
import com.silverpeas.mailinglist.service.model.beans.ExternalUser;
import com.silverpeas.mailinglist.service.model.beans.InternalGroupSubscriber;
import com.silverpeas.mailinglist.service.model.beans.InternalUserSubscriber;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import org.dbunit.DataSourceDatabaseTester;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.test.BasicWarBuilder;
import org.silverpeas.test.rule.DbUnitLoadingRule;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class TestMailingListDao {

  private DataSourceDatabaseTester databaseTester;

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule("create-database.sql", "mailinglist-dataset.xml");

  @Deployment
  public static Archive<?> createTestArchive() {
    return MailingListWarBuilder.onWarForTestClass(TestMailingListDao.class).build();
  }

  @Resource(lookup = "java:/datasources/silverpeas")
  private DataSource dataSource;

  @Before
  public void setUpTest() throws Exception {
    databaseTester = new DataSourceDatabaseTester(dataSource);
  }

  @After
  public void tearDownTest() throws Exception {
  }

  @Test
  public void testCreateMailingList() throws Exception {
    MailingListDao mailingListDao = getMailingListDAO();
    MailingList mailingList = new MailingList();
    mailingList.setComponentId("componentId");
    ExternalUser user = new ExternalUser();
    user.setEmail("bart.simpson@gmail.com");
    user.setComponentId("componentId");
    mailingList.getExternalSubscribers().add(user);
    InternalUserSubscriber bart = new InternalUserSubscriber();
    bart.setExternalId("bart");
    mailingList.getInternalSubscribers().add(bart);
    InternalGroupSubscriber simpsonsFamily = new InternalGroupSubscriber();
    simpsonsFamily.setExternalId("Simpsons");
    mailingList.getGroupSubscribers().add(simpsonsFamily);
    String id = mailingListDao.createMailingList(mailingList);
    assertNotNull(id);
    MailingList saved = mailingListDao.findById(id);
    assertNotNull(saved);
    assertEquals("componentId", saved.getComponentId());
    assertEquals(1, countRowsInTable("SC_MAILINGLIST_LIST"));
    assertEquals(1, countRowsInTable("SC_MAILINGLIST_EXTERNAL_USER"));
    assertNotNull(saved.getExternalSubscribers());
    assertEquals(1, saved.getExternalSubscribers().size());
    ExternalUser savedUser = saved.getExternalSubscribers().iterator().next();
    assertNotNull(savedUser.getId());
    assertNotNull(saved.getInternalSubscribers());
    assertEquals(1, saved.getInternalSubscribers().size());
    InternalUserSubscriber userSubscriber = saved.getInternalSubscribers().iterator().next();
    assertNotNull(userSubscriber.getId());
    assertEquals("bart", userSubscriber.getExternalId());
    assertNotNull(saved.getGroupSubscribers());
    assertEquals(1, saved.getGroupSubscribers().size());
    InternalGroupSubscriber groupSubscriber = saved.getGroupSubscribers().iterator().next();
    assertNotNull(groupSubscriber.getId());
    assertEquals("Simpsons", groupSubscriber.getExternalId());
    assertNotNull("componentId", savedUser.getComponentId());
  }

  @Test
  public void testUpdateMailingList() throws Exception {
    MailingListDao mailingListDao = getMailingListDAO();
    MailingList mailingList = new MailingList();
    mailingList.setComponentId("componentId");
    ExternalUser user = new ExternalUser();
    user.setEmail("bart.simpson@silverpeas.com");
    user.setComponentId("componentId");
    mailingList.getExternalSubscribers().add(user);
    String id = mailingListDao.createMailingList(mailingList);
    assertNotNull(id);
    mailingList = mailingListDao.findById(id);
    assertNotNull(mailingList);
    assertEquals("componentId", mailingList.getComponentId());
    assertEquals(1, countRowsInTable("SC_MAILINGLIST_LIST"));
    assertEquals(1, countRowsInTable("SC_MAILINGLIST_EXTERNAL_USER"));
    assertNotNull(mailingList.getExternalSubscribers());
    assertEquals(1, mailingList.getExternalSubscribers().size());
    ExternalUser savedUser = mailingList.getExternalSubscribers().iterator().next();
    assertNotNull(savedUser.getId());
    assertNotNull("componentId", savedUser.getComponentId());
    user = new ExternalUser();
    user.setEmail("lisa.simpson@silverpeas.com");
    user.setComponentId("componentId");
    mailingList.getExternalSubscribers().add(user);
    mailingListDao.updateMailingList(mailingList);
    mailingList = mailingListDao.findById(id);
    assertNotNull(mailingList);
    assertEquals("componentId", mailingList.getComponentId());
    assertEquals(1, countRowsInTable("SC_MAILINGLIST_LIST"));
    assertEquals(2, countRowsInTable("SC_MAILINGLIST_EXTERNAL_USER"));
    assertNotNull(mailingList.getExternalSubscribers());
    assertEquals(2, mailingList.getExternalSubscribers().size());
    user.setEmail("bart.simpson@silverpeas.com");
    user.setComponentId("componentId");
    mailingList.getExternalSubscribers().add(user);
    mailingListDao.updateMailingList(mailingList);
    mailingList = mailingListDao.findById(id);
    assertNotNull(mailingList);
    assertEquals("componentId", mailingList.getComponentId());
    assertEquals(1, countRowsInTable("SC_MAILINGLIST_LIST"));
    assertEquals(2, countRowsInTable("SC_MAILINGLIST_EXTERNAL_USER"));
    assertNotNull(mailingList.getExternalSubscribers());
    assertEquals(2, mailingList.getExternalSubscribers().size());
  }

  @Test
  public void testDeleteMailingList() throws Exception {
    MailingListDao mailingListDao = getMailingListDAO();
    MailingList mailingList = new MailingList();
    mailingList.setComponentId("componentId");
    ExternalUser user = new ExternalUser();
    user.setEmail("bart.simpson@silverpeas.com");
    user.setComponentId("componentId");
    mailingList.getExternalSubscribers().add(user);
    user = new ExternalUser();
    user.setEmail("lisa.simpson@silverpeas.com");
    user.setComponentId("componentId");
    mailingList.getExternalSubscribers().add(user);
    String id = mailingListDao.createMailingList(mailingList);
    assertNotNull(id);
    mailingList = mailingListDao.findById(id);
    assertNotNull(mailingList);
    assertEquals("componentId", mailingList.getComponentId());
    assertEquals(1, countRowsInTable("SC_MAILINGLIST_LIST"));
    assertEquals(2, countRowsInTable("SC_MAILINGLIST_EXTERNAL_USER"));
    assertNotNull(mailingList.getExternalSubscribers());
    assertEquals(2, mailingList.getExternalSubscribers().size());
    mailingListDao.deleteMailingList(mailingList);
    mailingList = mailingListDao.findById(id);
    assertNull(mailingList);
    assertEquals(0, countRowsInTable("SC_MAILINGLIST_LIST"));
    assertEquals(0, countRowsInTable("SC_MAILINGLIST_EXTERNAL_USER"));
  }

  @Test
  public void testFindByComponentId() throws Exception {
    MailingListDao mailingListDao = getMailingListDAO();
    MailingList mailingList = new MailingList();
    mailingList.setComponentId("componentId");
    ExternalUser user = new ExternalUser();
    user.setEmail("bart.simpson@silverpeas.com");
    user.setComponentId("componentId");
    mailingList.getExternalSubscribers().add(user);
    user = new ExternalUser();
    user.setEmail("lisa.simpson@silverpeas.com");
    user.setComponentId("componentId");
    mailingList.getExternalSubscribers().add(user);
    String id = mailingListDao.createMailingList(mailingList);
    assertNotNull(id);
    mailingList = mailingListDao.findByComponentId("componentId");
    assertNotNull(mailingList);
    assertEquals("componentId", mailingList.getComponentId());
    assertEquals(1, countRowsInTable("SC_MAILINGLIST_LIST"));
    assertEquals(2, countRowsInTable("SC_MAILINGLIST_EXTERNAL_USER"));
    assertNotNull(mailingList.getExternalSubscribers());
    assertEquals(2, mailingList.getExternalSubscribers().size());
  }

  @Test
  public void testListMailingList() throws Exception {
    MailingListDao mailingListDao = getMailingListDAO();
    MailingList mailingList = new MailingList();
    mailingList.setComponentId("componentId1");
    ExternalUser user = new ExternalUser();
    user.setEmail("bart.simpson@gmail.com");
    user.setComponentId("componentId1");
    mailingList.getExternalSubscribers().add(user);
    String id1 = mailingListDao.createMailingList(mailingList);
    assertNotNull(id1);
    mailingList = new MailingList();
    mailingList.setComponentId("componentId2");
    user = new ExternalUser();
    user.setEmail("bart.simpson@gmail.com");
    user.setComponentId("componentId2");
    mailingList.getExternalSubscribers().add(user);
    String id2 = mailingListDao.createMailingList(mailingList);
    assertNotNull(id2);
    List mailingLists = mailingListDao.listMailingLists();
    assertEquals(2, countRowsInTable("SC_MAILINGLIST_LIST"));
    assertEquals(2, countRowsInTable("SC_MAILINGLIST_EXTERNAL_USER"));
    assertNotNull(mailingLists);
    assertEquals(2, mailingLists.size());
  }

  private int countRowsInTable(String table) throws Exception {
    return databaseTester.getConnection().getRowCount(table);
  }

  private MailingListDao getMailingListDAO() {
    return MailingListDao.get();
  }

}
