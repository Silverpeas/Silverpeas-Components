/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.resourcemanager.services;

import com.stratelia.webactiv.util.DBUtil;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.resourcemanager.model.Category;
import org.silverpeas.resourcemanager.model.Resource;
import org.silverpeas.resourcemanager.model.ResourceStatus;
import org.silverpeas.resourcemanager.model.ResourceValidator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author ehugonnet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-resource-manager-datasource.xml",
  "/spring-resource-manager.xml"})
@Transactional
@TransactionConfiguration(transactionManager = "jpaTransactionManager")
public class ResourceServiceTest {

  private static ReplacementDataSet dataSet;
  private static Category firstCategory = new Category(1L, "resourcesManager42", "Salles", true, "model1.xml", "5", "5",
      "Salles de réunion");
  private static Category secondCategory = new Category(2L, "resourcesManager42", "Voitures", true, null, "6", "6",
      "Véhicules utilitaires");

  @Inject
  private ResourceService service;

  @Inject
  @Named("jpaDataSource")
  private DataSource dataSource;

  public ResourceServiceTest() {
  }

  @BeforeClass
  public static void prepareDataset() throws Exception {
    FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
    dataSet = new ReplacementDataSet(builder.build(ResourceServiceTest.class.getClassLoader().
        getResourceAsStream("org/silverpeas/resourcemanager/services/resources_dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
  }

  @Before
  public void generalSetUp() throws Exception {
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
    DBUtil.getInstanceForTest(dataSource.getConnection());
  }

  /**
   * Test of createResource method, of class ResourceService.
   */
  @Test
  public void testCreateResource() {
    Long id = 20L;
    Resource resource = new Resource(null, firstCategory, "Salle Vercors",
        "Salle de réunion jusqu'à 4 personnes avec vidéoprojecteur", "5", "5",
        "resourcesManager42", true);
    assertThat(resource.getCreationDate(), nullValue());
    assertThat(resource.getUpdateDate(), nullValue());
    service.createResource(resource);
    assertThat(resource.getId(), is(id));
    assertThat(resource.getCreationDate(), notNullValue());
    assertThat(resource.getUpdateDate(), is(resource.getCreationDate()));
    resource.setId(id);
    Resource savedResource = service.getResource(id);
    assertThat(savedResource, is(resource));
  }

  /**
   * Test of updateResource method, of class ResourceService.
   */
  @Test
  public void testUpdateResource() {
    int id = 1;
    Resource expected = new Resource(1L, firstCategory, "Salle Chartreuse",
        "Salle de réunion jusqu'à 4 personnes",
        "5", "5", "resourcesManager42", true);
    Resource test = service.getResource(id);
    assertThat(test, is(expected));
    expected.setCreationDate(test.getCreationDate());
    expected.setUpdateDate(test.getUpdateDate());
    expected.setBookable(false);
    expected.setName("Salle Vercors");
    expected.setDescription("Salle de réunion jusqu'à 4 personnes avec vidéoprojecteur");
    Date oldUpdateDate = test.getUpdateDate();
    service.updateResource(expected);
    assertThat(test.getUpdateDate(), greaterThan(oldUpdateDate));
    test = service.getResource(id);
    assertThat(test, is(expected));
  }

  /**
   * Test of getResources method, of class ResourceService.
   */
  @Test
  public void testGetResources() {
    List<Resource> result = service.getResources();
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(3));
  }

  /**
   * Test of getResource method, of class ResourceService.
   */
  @Test
  public void testGetResource() {
    int id = 1;
    Resource expResult = new Resource(1L, firstCategory, "Salle Chartreuse",
        "Salle de réunion jusqu'à 4 personnes", "5", "5", "resourcesManager42", true);
    Resource result = service.getResource(id);
    assertThat(result, is(expResult));
  }

  /**
   * Test of deleteResource method, of class ResourceService.
   */
  @Test
  public void testDeleteResource() {
    int id = 1;
    Resource expResult = new Resource(1L, firstCategory, "Salle Chartreuse",
        "Salle de réunion jusqu'à 4 personnes", "5", "5", "resourcesManager42", true);
    Resource result = service.getResource(id);
    assertThat(result, is(expResult));
    service.deleteResource(id);
    result = service.getResource(id);
    assertThat(result, is(nullValue()));
  }

  /**
   * Test of deleteResourcesFromCategory method, of class ResourceService.
   */
  @Test
  public void testDeleteResourcesFromCategory() {
    long categoryId = 1L;
    List<Resource> result = service.getResourcesByCategory(categoryId);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(2));
    assertThat(result, contains(new Resource(1L, firstCategory, "Salle Chartreuse",
        "Salle de réunion jusqu'à 4 personnes", "5", "5", "resourcesManager42", true),
        new Resource(2L, firstCategory, "Salle Belledonne", "Salle de réunion jusqu'à 12 personnes", "5", "5", "resourcesManager42",
        true)));
    service.deleteResourcesFromCategory(categoryId);
    result = service.getResourcesByCategory(categoryId);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(0));
  }

  /**
   * Test of addManagers method, of class ResourceService.
   */
  @Test
  public void testAddManagers() {
    int id = 3;
    List<ResourceValidator> result = service.getManagers(id);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(1));
    assertThat(result, containsInAnyOrder(new ResourceValidator(id, 0)));
    service.addManagers(id, Arrays.asList(new ResourceValidator(id, 1), new ResourceValidator(id, 5),
        new ResourceValidator(id, 10)));
    result = service.getResource(id).getManagers();
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(4));
    assertThat(result, containsInAnyOrder(new ResourceValidator(id, 0), new ResourceValidator(id, 1),
        new ResourceValidator(id, 5), new ResourceValidator(id, 10)));
  }

  /**
   * Test of addManagers method, of class ResourceService.
   */
  @Test
  public void testIsManager() {
    long resourceId = 1L;
    long userId = 2L;
    boolean isManager = service.isManager(userId, resourceId);
    assertThat(isManager, is(true));
    resourceId = 3L;
    isManager = service.isManager(userId, resourceId);
    assertThat(isManager, is(false));
    resourceId = 1L;
    userId = 5L;
    isManager = service.isManager(userId, resourceId);
    assertThat(isManager, is(false));
  }

  /**
   * Test of addManager method, of class ResourceService.
   */
  @Test
  public void testAddManager() {
    int id = 3;
    List<ResourceValidator> result = service.getManagers(id);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(1));
    assertThat(result, containsInAnyOrder(new ResourceValidator(id, 0)));
    service.addManager(new ResourceValidator(id, 6));
    result = service.getResource(id).getManagers();
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(2));
    assertThat(result, containsInAnyOrder(new ResourceValidator(id, 0),
        new ResourceValidator(id, 6)));
  }

  /**
   * Test of removeAllManagers method, of class ResourceService.
   */
  @Test
  public void testRemoveAllManagers() {
    int id = 1;
    List<ResourceValidator> result = service.getManagers(id);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(3));
    assertThat(result, containsInAnyOrder(new ResourceValidator(id, 0), new ResourceValidator(id, 1),
        new ResourceValidator(id, 2)));
    service.removeAllManagers(id);
    result = service.getResource(id).getManagers();
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(0));
  }

  /**
   * Test of removeManager method, of class ResourceService.
   */
  @Test
  public void testRemoveManager() {
    int id = 1;
    List<ResourceValidator> result = service.getManagers(id);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(3));
    assertThat(result, containsInAnyOrder(new ResourceValidator(id, 0), new ResourceValidator(id, 1),
        new ResourceValidator(id, 2)));
    service.removeManager(new ResourceValidator(id, 2));
    result = service.getResource(id).getManagers();
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(2));
    assertThat(result,
        containsInAnyOrder(new ResourceValidator(id, 0), new ResourceValidator(id, 1)));
  }

  /**
   * Test of getResourcesByCategory method, of class ResourceService.
   */
  @Test
  public void testGetResourcesByCategory() {
    long categoryId = 1L;
    List<Resource> result = service.getResourcesByCategory(categoryId);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(2));
    assertThat(result, contains(new Resource(1L, firstCategory, "Salle Chartreuse",
        "Salle de réunion jusqu'à 4 personnes", "5", "5", "resourcesManager42", true),
        new Resource(2L, firstCategory, "Salle Belledonne", "Salle de réunion jusqu'à 12 personnes", "5", "5", "resourcesManager42",
        true)));
  }

  /**
   * Test of listAvailableResources method, of class ResourceService.
   */
  @Test
  public void testListAvailableResourcesWithReservationOutOfRange() {
    String instanceId = "resourcesManager42";
    String startDate = String.valueOf(1320134400000L - 3600000L - 3600000L);
    String endDate = String.valueOf(1320134400000L - 3600000L);
    List<Resource> result = service.listAvailableResources(instanceId, startDate, endDate);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(3));
    assertThat(result, contains(new Resource(1L, firstCategory, "Salle Chartreuse",
        "Salle de réunion jusqu'à 4 personnes", "5", "5", "resourcesManager42", true),
        new Resource(2L, firstCategory, "Salle Belledonne", "Salle de réunion jusqu'à 12 personnes", "5", "5", "resourcesManager42",
        true), new Resource(3L, secondCategory, "Twingo verte - 156 VV 38",
        "Twingo verte 4 places 5 portes",
        "5", "5", "resourcesManager42", true)));


  }

  @Test
  public void testListAvailableResourcesJustBeforeReservation() {
    String instanceId = "resourcesManager42";
    //Just before reservation range
    String startDate = String.valueOf(1320134400000L - 3600000L);
    String endDate = String.valueOf(1320134400000L);
    List<Resource> result = service.listAvailableResources(instanceId, startDate, endDate);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(3));
    assertThat(result, contains(new Resource(1L, firstCategory, "Salle Chartreuse",
        "Salle de réunion jusqu'à 4 personnes", "5", "5", "resourcesManager42", true),
        new Resource(2L, firstCategory, "Salle Belledonne", "Salle de réunion jusqu'à 12 personnes", "5", "5", "resourcesManager42", true),
        new Resource(3L, secondCategory, "Twingo verte - 156 VV 38",
            "Twingo verte 4 places 5 portes", "5", "5", "resourcesManager42", true)));

  }

  @Test
  public void testListAvailableResourcesInReservationRange() {
    String instanceId = "resourcesManager42";
    String startDate = String.valueOf(1320134400000L);
    String endDate = String.valueOf(1320134400000L + 3600000L);
    List<Resource> result = service.listAvailableResources(instanceId, startDate, endDate);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(1));
    assertThat(result, contains(new Resource(2L, firstCategory, "Salle Belledonne",
        "Salle de réunion jusqu'à 12 personnes", "5", "5", "resourcesManager42", true)));
  }

  @Test
  public void testListAvailableResourcesAfterReservationRange() {
    String instanceId = "resourcesManager42";
    String startDate = String.valueOf(1320163200000L);
    String endDate = String.valueOf(1320163200000L + 3600000L);
    List<Resource> result = service.listAvailableResources(instanceId, startDate, endDate);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(3));
    assertThat(result, contains(new Resource(1L, firstCategory, "Salle Chartreuse",
        "Salle de réunion jusqu'à 4 personnes", "5", "5", "resourcesManager42", true),
        new Resource(2L, firstCategory, "Salle Belledonne", "Salle de réunion jusqu'à 12 personnes", "5", "5", "resourcesManager42", true),
        new Resource(3L, secondCategory, "Twingo verte - 156 VV 38",
            "Twingo verte 4 places 5 portes", "5", "5", "resourcesManager42", true)));
  }

  /**
   * Test of listAvailableResources method, of class ResourceService.
   */
  @Test
  public void testListAvailableResourcesWithReservationOverlappingRange() {
    String instanceId = "resourcesManager42";
    String startDate = String.valueOf(1320134400000L - 7200000L);
    String endDate = String.valueOf(1320163200000L + 7200000L);
    List<Resource> result = service.listAvailableResources(instanceId, startDate, endDate);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(1));
    assertThat(result, contains(new Resource(2L, firstCategory, "Salle Belledonne",
        "Salle de réunion jusqu'à 12 personnes", "5",
        "5", "resourcesManager42", true)));


  }
}
