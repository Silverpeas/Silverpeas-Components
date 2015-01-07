/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.resourcemanager.services;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.persistence.Transaction;
import org.silverpeas.resourcemanager.model.Category;
import org.silverpeas.resourcemanager.model.Resource;
import org.silverpeas.resourcemanager.model.ResourceValidator;
import org.silverpeas.test.BasicWarBuilder;
import org.silverpeas.test.rule.DbUnitLoadingRule;
import org.silverpeas.util.ServiceProvider;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author ehugonnet
 */
@RunWith(Arquillian.class)
public class ResourceServiceTest {

  private Category firstCategory;
  private Category secondCategory;

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule("create-database.sql", "resources_dataset.xml");

  @Deployment
  public static Archive<?> createTestArchive() {
    return BasicWarBuilder.onWarForTestClass(ResourceServiceTest.class)
        .testFocusedOn(warBuilder -> {
          warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core:lib-core");
          warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core.ejb-core:pdc");
          warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core.ejb-core:node");
          warBuilder.addMavenDependencies("org.silverpeas.core.ejb-core:tagcloud");
          warBuilder.addMavenDependencies("org.silverpeas.core.ejb-core:publication");
          warBuilder.addMavenDependencies("org.silverpeas.core.ejb-core:formtemplate");
          warBuilder.addMavenDependencies("org.silverpeas.core.ejb-core:calendar");
          warBuilder.addMavenDependencies("org.apache.tika:tika-core");
          warBuilder.addMavenDependencies("org.apache.tika:tika-parsers");
          warBuilder.addAsResource("META-INF/test-MANIFEST.MF", "META-INF/MANIFEST.MF");
          warBuilder.addPackages(true, "org.silverpeas.resourcemanager");
        }).build();
  }

  public ResourceServiceTest() {
  }

  private ResourceService service;

  @Before
  public void generalSetUp() throws Exception {
    service = ServiceProvider.getService(ResourceService.class);
    firstCategory = new Category(1L, "resourcesManager42", "Salles", true, "model1.xml", "5", "5",
        "Salles de réunion");
    secondCategory = new Category(2L, "resourcesManager42", "Voitures", true, null, "6", "6",
        "Véhicules utilitaires");
  }

  /**
   * Test of createResource method, of class ResourceService.
   */
  @Test
  public void testCreateResource() {
    Transaction.performInOne(() -> {
      Long id = 21L;
      Resource resource = new Resource(null, firstCategory, "Salle Vercors",
          "Salle de réunion jusqu'à 4 personnes avec vidéoprojecteur", "5", "5",
          "resourcesManager42", true);
      assertThat(resource.getCreationDate(), nullValue());
      assertThat(resource.getUpdateDate(), nullValue());
      service.createResource(resource);
      assertThat(resource.getIdAsLong(), is(id));
      assertThat(resource.getCreationDate(), notNullValue());
      assertThat(resource.getUpdateDate(), is(resource.getCreationDate()));
      resource.setId(Long.toString(id));
      Resource savedResource = service.getResource(id);
      assertThat(savedResource, is(resource));
      return null;
    });
  }

  /**
   * Test of updateResource method, of class ResourceService.
   */
  @Test
  public void testUpdateResource() {
    Transaction.performInOne(() -> {
      int id = 1;
      Resource expected = new Resource(1L, firstCategory, "Salle Chartreuse",
          "Salle de réunion jusqu'à 4 personnes", "5", "5", "resourcesManager42", true);
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
      return null;
    });
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
    Resource expResult =
        new Resource(1L, firstCategory, "Salle Chartreuse", "Salle de réunion jusqu'à 4 personnes",
            "5", "5", "resourcesManager42", true);
    Resource result = service.getResource(id);
    assertThat(result, is(expResult));
  }

  /**
   * Test of deleteResource method, of class ResourceService.
   */
  @Test
  public void testDeleteResource() {
    int id = 1;
    Resource expResult =
        new Resource(1L, firstCategory, "Salle Chartreuse", "Salle de réunion jusqu'à 4 personnes",
            "5", "5", "resourcesManager42", true);
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
    assertThat(result, contains(
        new Resource(1L, firstCategory, "Salle Chartreuse", "Salle de réunion jusqu'à 4 personnes",
            "5", "5", "resourcesManager42", true),
        new Resource(2L, firstCategory, "Salle Belledonne", "Salle de réunion jusqu'à 12 personnes",
            "5", "5", "resourcesManager42", true)));
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
    Transaction.performInOne(() -> {
      int id = 3;
      List<ResourceValidator> result = service.getManagers(id);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(1));
      assertThat(result, containsInAnyOrder(new ResourceValidator(id, 0)));
      service.addManagers(id, Arrays
          .asList(new ResourceValidator(id, 1), new ResourceValidator(id, 5),
              new ResourceValidator(id, 10)));
      result = service.getResource(id).getManagers();
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(4));
      assertThat(result,
          containsInAnyOrder(new ResourceValidator(id, 0), new ResourceValidator(id, 1),
              new ResourceValidator(id, 5), new ResourceValidator(id, 10)));
      return null;
    });
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
    Transaction.performInOne(() -> {
      int id = 3;
      List<ResourceValidator> result = service.getManagers(id);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(1));
      assertThat(result, containsInAnyOrder(new ResourceValidator(id, 0)));
      service.addManager(new ResourceValidator(id, 6));
      List<ResourceValidator> afterInsertResult = service.getResource(id).getManagers();
      assertThat(afterInsertResult, is(notNullValue()));
      assertThat(afterInsertResult, hasSize(2));
      assertThat(afterInsertResult,
          containsInAnyOrder(new ResourceValidator(id, 0), new ResourceValidator(id, 6)));
      return null;
    });


  }

  /**
   * Test of removeAllManagers method, of class ResourceService.
   */
  @Test
  public void testRemoveAllManagers() {
    Transaction.performInOne(() -> {
      int id = 1;
      List<ResourceValidator> result = service.getManagers(id);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(3));
      assertThat(result,
          containsInAnyOrder(new ResourceValidator(id, 0), new ResourceValidator(id, 1),
              new ResourceValidator(id, 2)));
      service.removeAllManagers(id);
      result = service.getResource(id).getManagers();
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(0));
      return null;
    });
  }

  /**
   * Test of removeManager method, of class ResourceService.
   */
  @Test
  public void testRemoveManager() {
    Transaction.performInOne(() -> {
      int id = 1;
      List<ResourceValidator> result = service.getManagers(id);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(3));
      assertThat(result,
          containsInAnyOrder(new ResourceValidator(id, 0), new ResourceValidator(id, 1),
              new ResourceValidator(id, 2)));
      service.removeManager(new ResourceValidator(id, 2));
      result = service.getResource(id).getManagers();
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(2));
      assertThat(result,
          containsInAnyOrder(new ResourceValidator(id, 0), new ResourceValidator(id, 1)));
      return null;
    });
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
    assertThat(result, contains(
        new Resource(1L, firstCategory, "Salle Chartreuse", "Salle de réunion jusqu'à 4 personnes",
            "5", "5", "resourcesManager42", true),
        new Resource(2L, firstCategory, "Salle Belledonne", "Salle de réunion jusqu'à 12 personnes",
            "5", "5", "resourcesManager42", true)));
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
    assertThat(result, contains(
        new Resource(1L, firstCategory, "Salle Chartreuse", "Salle de réunion jusqu'à 4 personnes",
            "5", "5", "resourcesManager42", true),
        new Resource(2L, firstCategory, "Salle Belledonne", "Salle de réunion jusqu'à 12 personnes",
            "5", "5", "resourcesManager42", true),
        new Resource(3L, secondCategory, "Twingo verte - 156 VV 38",
            "Twingo verte 4 places 5 portes", "5", "5", "resourcesManager42", true)));


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
    assertThat(result, contains(
        new Resource(1L, firstCategory, "Salle Chartreuse", "Salle de réunion jusqu'à 4 personnes",
            "5", "5", "resourcesManager42", true),
        new Resource(2L, firstCategory, "Salle Belledonne", "Salle de réunion jusqu'à 12 personnes",
            "5", "5", "resourcesManager42", true),
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
    assertThat(result, contains(
        new Resource(2L, firstCategory, "Salle Belledonne", "Salle de réunion jusqu'à 12 personnes",
            "5", "5", "resourcesManager42", true)));
  }

  @Test
  public void testListAvailableResourcesAfterReservationRange() {
    String instanceId = "resourcesManager42";
    String startDate = String.valueOf(1320163200000L);
    String endDate = String.valueOf(1320163200000L + 3600000L);
    List<Resource> result = service.listAvailableResources(instanceId, startDate, endDate);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(3));
    assertThat(result, contains(
        new Resource(1L, firstCategory, "Salle Chartreuse", "Salle de réunion jusqu'à 4 personnes",
            "5", "5", "resourcesManager42", true),
        new Resource(2L, firstCategory, "Salle Belledonne", "Salle de réunion jusqu'à 12 personnes",
            "5", "5", "resourcesManager42", true),
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
    assertThat(result, contains(
        new Resource(2L, firstCategory, "Salle Belledonne", "Salle de réunion jusqu'à 12 personnes",
            "5", "5", "resourcesManager42", true)));
  }
}
