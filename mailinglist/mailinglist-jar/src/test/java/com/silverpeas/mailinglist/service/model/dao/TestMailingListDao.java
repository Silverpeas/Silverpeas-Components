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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.mailinglist.service.model.dao;

import com.silverpeas.mailinglist.service.model.beans.ExternalUser;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-checker.xml", "/spring-notification.xml",
  "/spring-hibernate.xml", "/spring-datasource.xml"})
@Transactional
@TransactionConfiguration(defaultRollback=true,transactionManager="txManager")
public class TestMailingListDao extends AbstractTransactionalJUnit4SpringContextTests {

  @Inject
  private MailingListDao mailingListDao;

  @Test
  public void testCreateMailingList() {
    MailingList mailingList = new MailingList();
    mailingList.setComponentId("componentId");
    ExternalUser user = new ExternalUser();
    user.setEmail("bart.simpson@gmail.com");
    user.setComponentId("componentId");
    mailingList.getExternalSubscribers().add(user);
    String id = mailingListDao.createMailingList(mailingList);
    assertNotNull(id);
    MailingList saved = mailingListDao.findById(id);
    assertNotNull(saved);
    assertEquals("componentId", saved.getComponentId());
    assertEquals(1, countRowsInTable("SC_MAILINGLIST_LIST"));
    assertEquals(1, countRowsInTable("SC_MAILINGLIST_EXTERNAL_USER"));
    assertNotNull(saved.getExternalSubscribers());
    assertEquals(1, saved.getExternalSubscribers().size());
    ExternalUser savedUser = (ExternalUser) saved.getExternalSubscribers().iterator().next();
    assertNotNull(savedUser.getId());
    assertNotNull("componentId", savedUser.getComponentId());
  }

  @Test
  public void testUpdateMailingList() {
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
    ExternalUser savedUser = (ExternalUser) mailingList.getExternalSubscribers().iterator().next();
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
  public void testDeleteMailingList() {
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
  public void testFindByComponentId() {
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
  public void testListMailingList() {
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
    List mailingLists = this.mailingListDao.listMailingLists();
    assertEquals(2, countRowsInTable("SC_MAILINGLIST_LIST"));
    assertEquals(2, countRowsInTable("SC_MAILINGLIST_EXTERNAL_USER"));
    assertNotNull(mailingLists);
    assertEquals(2, mailingLists.size());
  }
}
