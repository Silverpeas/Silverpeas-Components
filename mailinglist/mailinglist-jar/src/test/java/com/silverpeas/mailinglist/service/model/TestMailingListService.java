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

package com.silverpeas.mailinglist.service.model;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;

import com.silverpeas.mailinglist.AbstractSilverpeasDatasourceSpringContextTests;
import com.silverpeas.mailinglist.service.model.beans.ExternalUser;
import com.silverpeas.mailinglist.service.model.beans.InternalUser;
import com.silverpeas.mailinglist.service.model.beans.InternalUserSubscriber;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

public class TestMailingListService extends
    AbstractSilverpeasDatasourceSpringContextTests {

  private MailingListService mailingListService;
  private OrganizationController organizationController;

  public OrganizationController getOrganizationController() {
    return organizationController;
  }

  public void setOrganizationController(OrganizationController organizationController) {
    this.organizationController = organizationController;
  }

  @Override
  protected String[] getConfigLocations() {
    return new String[] { "spring-checker.xml", "spring-notification.xml",
        "spring-hibernate.xml", "spring-datasource.xml" };
  }

  @Override
  protected void onTearDown() {
    IDatabaseConnection connection = null;
    try {
      connection = getConnection();
      DatabaseOperation.DELETE_ALL.execute(connection, getDataSet());
      FileFolderManager.deleteFolder("c:\\tmp\\uploads\\componentId", false);
      super.cleanJndi();
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
  protected void onSetUp() {
    registerDatasource();
    IDatabaseConnection connection = null;
    try {
      connection = getConnection();
      DatabaseOperation.DELETE_ALL.execute(connection, getDataSet());
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
    organizationController.reloadAdminCache();
  }

  protected IDataSet getDataSet() throws DataSetException, IOException {
    if (isOracle()) {
      return new ReplacementDataSet(
          new FlatXmlDataSet(
              TestMailingListService.class
                  .getResourceAsStream("test-mailinglist-service-oracle-dataset.xml")));
    }
    return new ReplacementDataSet(new FlatXmlDataSet(
        TestMailingListService.class
            .getResourceAsStream("test-mailinglist-service-dataset.xml")));
  }

  public void testCreateMailingList() {    
    MailingList mailingList = new MailingList();
    mailingList.setComponentId("100");
    ExternalUser tahiti = new ExternalUser();
    tahiti.setEmail("bob.tahiti@silverpeas.com");
    tahiti.setComponentId("100");
    mailingList.addExternalSubscriber(tahiti);
    ExternalUser skinner = new ExternalUser();
    skinner.setEmail("seymour.skinner@silverpeas.com");
    skinner.setComponentId("100");
    mailingList.addExternalSubscriber(skinner);
    mailingListService.createMailingList(mailingList);
    MailingList savedMailingList = mailingListService.findMailingList("100");
    assertNotNull(savedMailingList);
    assertEquals("100", savedMailingList.getComponentId());
    assertEquals("Liste de diffusion de test", savedMailingList.getName());
    assertEquals("Gestion d'une liste de diffusion", savedMailingList
        .getDescription());
    assertTrue(savedMailingList.isModerated());
    assertTrue(savedMailingList.isNotify());
    assertTrue(savedMailingList.isSupportRSS());
    assertFalse(savedMailingList.isOpen());
    assertEquals("thesimpsons@silverpeas.com", savedMailingList
        .getSubscribedAddress());
    assertNotNull(savedMailingList.getExternalSubscribers());
    assertEquals(2, savedMailingList.getExternalSubscribers().size());
    assertTrue(savedMailingList.getExternalSubscribers().contains(skinner));
    assertTrue(savedMailingList.getExternalSubscribers().contains(tahiti));
  }

  public void testAddExternalUser() {
    MailingList mailingList = new MailingList();
    mailingList.setComponentId("100");
    ExternalUser tahiti = new ExternalUser();
    tahiti.setEmail("bob.tahiti@silverpeas.com");
    tahiti.setComponentId("100");
    mailingList.addExternalSubscriber(tahiti);
    ExternalUser skinner = new ExternalUser();
    skinner.setEmail("seymour.skinner@silverpeas.com");
    skinner.setComponentId("100");
    mailingList.addExternalSubscriber(skinner);
    mailingListService.createMailingList(mailingList);
    mailingList = mailingListService.findMailingList("100");
    assertNotNull(mailingList);
    assertEquals("100", mailingList.getComponentId());
    assertEquals("Liste de diffusion de test", mailingList.getName());
    assertEquals("Gestion d'une liste de diffusion", mailingList
        .getDescription());
    assertTrue(mailingList.isModerated());
    assertTrue(mailingList.isNotify());
    assertTrue(mailingList.isSupportRSS());
    assertFalse(mailingList.isOpen());
    assertEquals("thesimpsons@silverpeas.com", mailingList
        .getSubscribedAddress());
    assertNotNull(mailingList.getExternalSubscribers());
    assertEquals(2, mailingList.getExternalSubscribers().size());
    assertTrue(mailingList.getExternalSubscribers().contains(skinner));
    assertTrue(mailingList.getExternalSubscribers().contains(tahiti));
    ExternalUser krusty = new ExternalUser();
    krusty.setEmail("krusty.theklown@silverpeas.com");
    krusty.setComponentId("100");
    mailingListService.addExternalUser("100", krusty);
    mailingList = mailingListService.findMailingList("100");
    assertNotNull(mailingList);
    assertNotNull(mailingList.getExternalSubscribers());
    assertEquals(3, mailingList.getExternalSubscribers().size());
    ExternalUser cheater = new ExternalUser();
    cheater.setEmail("seymour.skinner@silverpeas.com");
    cheater.setComponentId("100");
    mailingListService.addExternalUser("100", cheater);
    mailingList = mailingListService.findMailingList("100");
    assertNotNull(mailingList);
    assertNotNull(mailingList.getExternalSubscribers());
    assertEquals(3, mailingList.getExternalSubscribers().size());
  }

  public void testAddInternalSubscribers() {
    MailingList mailingList = new MailingList();
    mailingList.setComponentId("100");
    InternalUserSubscriber bart = new InternalUserSubscriber();
    bart.setExternalId("200");
    InternalUserSubscriber maggie = new InternalUserSubscriber();
    maggie.setExternalId("204");
    mailingList.getInternalSubscribers().add(bart);
    mailingList.getInternalSubscribers().add(maggie);
    mailingListService.createMailingList(mailingList);
    mailingList = mailingListService.findMailingList("100");
    assertNotNull(mailingList);
    assertEquals("100", mailingList.getComponentId());
    assertEquals("Liste de diffusion de test", mailingList.getName());
    assertEquals("Gestion d'une liste de diffusion", mailingList
        .getDescription());
    assertTrue(mailingList.isModerated());
    assertTrue(mailingList.isNotify());
    assertTrue(mailingList.isSupportRSS());
    assertFalse(mailingList.isOpen());
    assertEquals("thesimpsons@silverpeas.com", mailingList
        .getSubscribedAddress());
    assertNotNull(mailingList.getInternalSubscribers());
    assertEquals(2, mailingList.getInternalSubscribers().size());
    assertTrue(mailingList.getInternalSubscribers().contains(bart));
    assertTrue(mailingList.getInternalSubscribers().contains(maggie));
    assertNotNull(mailingList.getGroupSubscribers());
    assertEquals(0, mailingList.getGroupSubscribers().size());
    mailingListService.subscribe("100", "203");
    mailingList = mailingListService.findMailingList("100");
    InternalUserSubscriber marge = new InternalUserSubscriber();
    marge.setExternalId("203");
    assertNotNull(mailingList);
    assertEquals("100", mailingList.getComponentId());
    assertEquals("Liste de diffusion de test", mailingList.getName());
    assertEquals("Gestion d'une liste de diffusion", mailingList
        .getDescription());
    assertTrue(mailingList.isModerated());
    assertTrue(mailingList.isNotify());
    assertTrue(mailingList.isSupportRSS());
    assertFalse(mailingList.isOpen());
    assertEquals("thesimpsons@silverpeas.com", mailingList
        .getSubscribedAddress());
    assertNotNull(mailingList.getInternalSubscribers());
    assertEquals(3, mailingList.getInternalSubscribers().size());
    assertTrue(mailingList.getInternalSubscribers().contains(bart));
    assertTrue(mailingList.getInternalSubscribers().contains(maggie));
    assertTrue(mailingList.getInternalSubscribers().contains(marge));
    assertNotNull(mailingList.getGroupSubscribers());
    assertEquals(0, mailingList.getGroupSubscribers().size());
    mailingListService.subscribe("100", "200");
    mailingList = mailingListService.findMailingList("100");
    assertNotNull(mailingList);
    assertEquals("100", mailingList.getComponentId());
    assertEquals("Liste de diffusion de test", mailingList.getName());
    assertEquals("Gestion d'une liste de diffusion", mailingList
        .getDescription());
    assertTrue(mailingList.isModerated());
    assertTrue(mailingList.isNotify());
    assertTrue(mailingList.isSupportRSS());
    assertFalse(mailingList.isOpen());
    assertEquals("thesimpsons@silverpeas.com", mailingList
        .getSubscribedAddress());
    assertNotNull(mailingList.getInternalSubscribers());
    assertEquals(3, mailingList.getInternalSubscribers().size());
    assertTrue(mailingList.getInternalSubscribers().contains(bart));
    assertTrue(mailingList.getInternalSubscribers().contains(maggie));
    assertTrue(mailingList.getInternalSubscribers().contains(marge));
    assertNotNull(mailingList.getGroupSubscribers());
    assertEquals(0, mailingList.getGroupSubscribers().size());
  }

  public void testAddExternalUsers() {
    MailingList mailingList = new MailingList();
    mailingList.setComponentId("100");
    ExternalUser tahiti = new ExternalUser();
    tahiti.setEmail("bob.tahiti@silverpeas.com");
    tahiti.setComponentId("100");
    mailingList.addExternalSubscriber(tahiti);
    ExternalUser skinner = new ExternalUser();
    skinner.setEmail("seymour.skinner@silverpeas.com");
    skinner.setComponentId("100");
    mailingList.addExternalSubscriber(skinner);
    mailingListService.createMailingList(mailingList);
    mailingList = mailingListService.findMailingList("100");
    assertNotNull(mailingList);
    assertEquals("100", mailingList.getComponentId());
    assertEquals("Liste de diffusion de test", mailingList.getName());
    assertEquals("Gestion d'une liste de diffusion", mailingList
        .getDescription());
    assertTrue(mailingList.isModerated());
    assertTrue(mailingList.isNotify());
    assertTrue(mailingList.isSupportRSS());
    assertFalse(mailingList.isOpen());
    assertEquals("thesimpsons@silverpeas.com", mailingList
        .getSubscribedAddress());
    assertNotNull(mailingList.getExternalSubscribers());
    assertEquals(2, mailingList.getExternalSubscribers().size());
    assertTrue(mailingList.getExternalSubscribers().contains(skinner));
    assertTrue(mailingList.getExternalSubscribers().contains(tahiti));
    Set<ExternalUser> newUsers = new HashSet<ExternalUser>(3);
    ExternalUser krusty = new ExternalUser();
    krusty.setEmail("krusty.theklown@silverpeas.com");
    krusty.setComponentId("100");
    newUsers.add(krusty);
    ExternalUser cheater = new ExternalUser();
    cheater.setEmail("seymour.skinner@silverpeas.com");
    cheater.setComponentId("100");
    newUsers.add(cheater);
    ExternalUser flanders = new ExternalUser();
    flanders.setEmail("ted.flanders@silverpeas.com");
    flanders.setComponentId("100");
    newUsers.add(flanders);
    mailingListService.addExternalUsers("100", newUsers);
    mailingList = mailingListService.findMailingList("100");
    assertNotNull(mailingList);
    assertNotNull(mailingList.getExternalSubscribers());
    assertEquals(4, mailingList.getExternalSubscribers().size());
    assertTrue(mailingList.getExternalSubscribers().contains(flanders));
    assertTrue(mailingList.getExternalSubscribers().contains(tahiti));
    assertTrue(mailingList.getExternalSubscribers().contains(krusty));
    assertTrue(mailingList.getExternalSubscribers().contains(skinner));
    assertTrue(mailingList.getExternalSubscribers().contains(cheater));
  }

  public void testRemoveExternalUser() {
    MailingList mailingList = new MailingList();
    mailingList.setComponentId("100");
    ExternalUser tahiti = new ExternalUser();
    tahiti.setEmail("bob.tahiti@silverpeas.com");
    tahiti.setComponentId("100");
    mailingList.addExternalSubscriber(tahiti);
    ExternalUser skinner = new ExternalUser();
    skinner.setEmail("seymour.skinner@silverpeas.com");
    skinner.setComponentId("100");
    mailingList.addExternalSubscriber(skinner);
    mailingListService.createMailingList(mailingList);
    mailingList = mailingListService.findMailingList("100");
    assertNotNull(mailingList);
    assertEquals("100", mailingList.getComponentId());
    assertEquals("Liste de diffusion de test", mailingList.getName());
    assertEquals("Gestion d'une liste de diffusion", mailingList
        .getDescription());
    assertTrue(mailingList.isModerated());
    assertTrue(mailingList.isNotify());
    assertTrue(mailingList.isSupportRSS());
    assertFalse(mailingList.isOpen());
    assertEquals("thesimpsons@silverpeas.com", mailingList
        .getSubscribedAddress());
    assertNotNull(mailingList.getExternalSubscribers());
    assertEquals(2, mailingList.getExternalSubscribers().size());
    assertTrue(mailingList.getExternalSubscribers().contains(skinner));
    assertTrue(mailingList.getExternalSubscribers().contains(tahiti));
    mailingListService.removeExternalUser("100", tahiti);
    mailingList = mailingListService.findMailingList("100");
    assertNotNull(mailingList);
    assertNotNull(mailingList.getExternalSubscribers());
    assertEquals(1, mailingList.getExternalSubscribers().size());
    ExternalUser cheater = new ExternalUser();
    cheater.setEmail("seymour.skinner@silverpeas.com");
    cheater.setComponentId("100");
    mailingListService.removeExternalUser("100", cheater);
    mailingList = mailingListService.findMailingList("100");
    assertNotNull(mailingList);
    assertNotNull(mailingList.getExternalSubscribers());
    assertEquals(0, mailingList.getExternalSubscribers().size());
  }

  public void testDeleteMailingList() {
    MailingList mailingList = new MailingList();
    mailingList.setComponentId("100");
    ExternalUser tahiti = new ExternalUser();
    tahiti.setEmail("bob.tahiti@silverpeas.com");
    tahiti.setComponentId("100");
    mailingList.addExternalSubscriber(tahiti);
    ExternalUser skinner = new ExternalUser();
    skinner.setEmail("seymour.skinner@silverpeas.com");
    skinner.setComponentId("100");
    mailingList.addExternalSubscriber(skinner);
    mailingListService.createMailingList(mailingList);
    mailingList = mailingListService.findMailingList("100");
    assertNotNull(mailingList);
    assertEquals("100", mailingList.getComponentId());
    assertEquals("Liste de diffusion de test", mailingList.getName());
    assertEquals("Gestion d'une liste de diffusion", mailingList
        .getDescription());
    assertTrue(mailingList.isModerated());
    assertTrue(mailingList.isNotify());
    assertTrue(mailingList.isSupportRSS());
    assertFalse(mailingList.isOpen());
    assertEquals("thesimpsons@silverpeas.com", mailingList
        .getSubscribedAddress());
    assertNotNull(mailingList.getExternalSubscribers());
    assertEquals(2, mailingList.getExternalSubscribers().size());
    assertTrue(mailingList.getExternalSubscribers().contains(skinner));
    assertTrue(mailingList.getExternalSubscribers().contains(tahiti));
    mailingListService.deleteMailingList("100");
    mailingList = mailingListService.findMailingList("100");
    assertNull(mailingList);
  }

  public void testListMailingList() {
    MailingList mailingList = new MailingList();
    mailingList.setComponentId("100");
    ExternalUser tahiti = new ExternalUser();
    tahiti.setEmail("bob.tahiti@silverpeas.com");
    tahiti.setComponentId("100");
    mailingList.addExternalSubscriber(tahiti);
    ExternalUser skinner = new ExternalUser();
    skinner.setEmail("seymour.skinner@silverpeas.com");
    skinner.setComponentId("100");
    mailingList.addExternalSubscriber(skinner);
    mailingListService.createMailingList(mailingList);
    mailingList = mailingListService.findMailingList("100");
    assertNotNull(mailingList);
    assertEquals("100", mailingList.getComponentId());
    assertEquals("Liste de diffusion de test", mailingList.getName());
    assertEquals("Gestion d'une liste de diffusion", mailingList
        .getDescription());
    assertTrue(mailingList.isModerated());
    assertTrue(mailingList.isNotify());
    assertTrue(mailingList.isSupportRSS());
    assertFalse(mailingList.isOpen());
    assertEquals("thesimpsons@silverpeas.com", mailingList
        .getSubscribedAddress());
    assertNotNull(mailingList.getExternalSubscribers());
    assertEquals(2, mailingList.getExternalSubscribers().size());
    assertTrue(mailingList.getExternalSubscribers().contains(skinner));
    assertTrue(mailingList.getExternalSubscribers().contains(tahiti));
    assertNotNull(mailingList.getModerators());
    assertEquals(3, mailingList.getModerators().size());
    for (InternalUser user : mailingList.getModerators()) {
      assertEquals("http://localhost:8000", user.getDomain());
      assertTrue("200".equals(user.getId()) || "202".equals(user.getId())
          || "203".equals(user.getId()));
      assertTrue("homer.simpson@silverpeas.com".equals(user.getEmail())
          || "marge.simpson@silverpeas.com".equals(user.getEmail())
          || "bart.simpson@silverpeas.com".equals(user.getEmail()));
    }
    assertNotNull(mailingList.getReaders());
    assertEquals(2, mailingList.getReaders().size());
    for (InternalUser user : mailingList.getReaders()) {
      assertTrue("201".equals(user.getId()) || "204".equals(user.getId()));
      assertEquals("http://localhost:8000", user.getDomain());
      assertTrue("lisa.simpson@silverpeas.com".equals(user.getEmail())
          || "maggie.simpson@silverpeas.com".equals(user.getEmail()));
    }

  }

  @SuppressWarnings("unchecked")
  public void testGetMailingList() {
    MailingList mailingList = new MailingList();
    mailingList.setComponentId("100");
    ExternalUser tahiti = new ExternalUser();
    tahiti.setEmail("bob.tahiti@silverpeas.com");
    tahiti.setComponentId("100");
    mailingList.addExternalSubscriber(tahiti);
    ExternalUser skinner = new ExternalUser();
    skinner.setEmail("seymour.skinner@silverpeas.com");
    skinner.setComponentId("100");
    mailingList.addExternalSubscriber(skinner);
    mailingListService.createMailingList(mailingList);
    List mailingLists = mailingListService.listAllMailingLists();
    assertNotNull(mailingLists);
    assertEquals(1, mailingLists.size());
    mailingList = (MailingList) mailingLists.iterator().next();
    assertEquals("100", mailingList.getComponentId());
    assertEquals("Liste de diffusion de test", mailingList.getName());
    assertTrue(mailingList.isModerated());
    assertTrue(mailingList.isNotify());
    assertTrue(mailingList.isSupportRSS());
    assertFalse(mailingList.isOpen());
    assertEquals("thesimpsons@silverpeas.com", mailingList
        .getSubscribedAddress());
    assertNotNull(mailingList.getExternalSubscribers());
    assertEquals(2, mailingList.getExternalSubscribers().size());
    assertTrue(mailingList.getExternalSubscribers().contains(skinner));
    assertTrue(mailingList.getExternalSubscribers().contains(tahiti));
    assertNotNull(mailingList.getModerators());
    assertEquals(3, mailingList.getModerators().size());
    for (InternalUser user : mailingList.getModerators()) {
      assertEquals("http://localhost:8000", user.getDomain());
      assertTrue("200".equals(user.getId()) || "202".equals(user.getId())
          || "203".equals(user.getId()));
      assertTrue("homer.simpson@silverpeas.com".equals(user.getEmail())
          || "marge.simpson@silverpeas.com".equals(user.getEmail())
          || "bart.simpson@silverpeas.com".equals(user.getEmail()));
    }
    assertNotNull(mailingList.getReaders());
    assertEquals(2, mailingList.getReaders().size());
    for (InternalUser user : mailingList.getReaders()) {
      assertEquals("http://localhost:8000", user.getDomain());
      assertTrue("201".equals(user.getId()) || "204".equals(user.getId()));
      assertTrue("lisa.simpson@silverpeas.com".equals(user.getEmail())
          || "maggie.simpson@silverpeas.com".equals(user.getEmail()));
    }

  }

  public MailingListService getMailingListService() {
    return mailingListService;
  }

  public void setMailingListService(MailingListService mailingListService) {
    this.mailingListService = mailingListService;
  }
}
