/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.resourcesmanager.service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.components.resourcesmanager.model.Category;
import org.silverpeas.components.resourcesmanager.model.Resource;
import org.silverpeas.components.resourcesmanager.model.ResourceValidator;
import org.silverpeas.components.resourcesmanager.test.WarBuilder4ResourcesManager;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.identifier.UniqueLongIdentifier;
import org.silverpeas.core.test.integration.rule.DbUnitLoadingRule;
import org.silverpeas.core.test.unit.EntityIdSetter;
import org.silverpeas.core.util.ServiceProvider;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author ehugonnet
 */
@RunWith(Arquillian.class)
public class ResourceServiceIT {

  private final EntityIdSetter idSetter = new EntityIdSetter(UniqueLongIdentifier.class);
  private final Map<Long, Resource> expectedResources = new HashMap<>();
  private Category firstCategory;
  private Category secondCategory;

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule("create-database.sql", "resources_dataset.xml");

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4ResourcesManager.onWarForTestClass(ResourceServiceIT.class).build();
  }

  private ResourceService service;

  @Before
  public void generalSetUp() {
    service = ServiceProvider.getService(ResourceService.class);
    firstCategory = new Category("Salles", true, "model1.xml", "Salles de réunion");
    idSetter.setIdTo(firstCategory, "1");
    firstCategory.setInstanceId("resourcesManager42");
    firstCategory.setCreaterId("5");
    firstCategory.setUpdaterId("5");
    secondCategory = new Category("Voitures", true, null, "Véhicules utilitaires");
    idSetter.setIdTo(secondCategory, "2");
    secondCategory.setInstanceId("resourcesManager42");
    secondCategory.setCreaterId("6");
    secondCategory.setUpdaterId("6");

    Resource resource1 = new Resource();
    idSetter.setIdTo(resource1, "1");
    resource1.setInstanceId("resourcesManager42");
    resource1.setCategory(firstCategory);
    resource1.setName("Salle Chartreuse");
    resource1.setDescription("Salle de réunion jusqu'à 4 personnes");
    resource1.setCreaterId("5");
    resource1.setUpdaterId("5");
    resource1.setBookable(true);
    expectedResources.put(1L, resource1);

    Resource resource2 = new Resource();
    idSetter.setIdTo(resource2, "2");
    resource2.setInstanceId("resourcesManager42");
    resource2.setCategory(firstCategory);
    resource2.setName("Salle Belledonne");
    resource2.setDescription("Salle de réunion jusqu'à 12 personnes");
    resource2.setCreaterId("5");
    resource2.setUpdaterId("5");
    resource2.setBookable(true);
    expectedResources.put(2L, resource2);

    Resource resource3 = new Resource();
    idSetter.setIdTo(resource3, "3");
    resource3.setInstanceId("resourcesManager42");
    resource3.setCategory(secondCategory);
    resource3.setName("Twingo verte - 156 VV 38");
    resource3.setDescription("Twingo verte 4 places 5 portes");
    resource3.setCreaterId("5");
    resource3.setUpdaterId("5");
    resource3.setBookable(true);
    expectedResources.put(3L, resource3);
  }

  /**
   * Test of createResource method, of class ResourceService.
   */
  @Test
  public void testCreateResource() {
    Transaction.performInOne(() -> {
      long id = 21L;
      Resource resource = new Resource();
      resource.setInstanceId("resourcesManager42");
      resource.setCategory(firstCategory);
      resource.setName("Salle Vercors");
      resource.setDescription("Salle de réunion jusqu'à 4 personnes avec vidéoprojecteur");
      resource.setCreaterId("5");
      resource.setUpdaterId("5");
      resource.setBookable(true);

      assertThat(resource.getCreationDate(), nullValue());
      assertThat(resource.getUpdateDate(), nullValue());
      service.createResource(resource);
      assertThat(resource.getIdAsLong(), is(id));
      assertThat(resource.getCreationDate(), notNullValue());
      assertThat(resource.getUpdateDate(), is(resource.getCreationDate()));
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
      long id = 1L;
      Resource expected = expectedResources.get(id);

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

  @Test
  public void testGetResourcesOfGiveResourcesManager() {
    List<Resource> result = service.getResources("resourcesManager42");
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(3));
  }

  /**
   * Test of getResource method, of class ResourceService.
   */
  @Test
  public void testGetResource() {
    long id = 1L;
    Resource expected = expectedResources.get(id);
    Resource result = service.getResource(id);
    assertThat(result, is(expected));
  }

  /**
   * Test of deleteResource method, of class ResourceService.
   */
  @Test
  public void testDeleteResource() {
    long id = 1L;
    Resource expected = expectedResources.get(id);

    Resource result = service.getResource(id);
    assertThat(result, is(expected));
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
    Resource resource1 = expectedResources.get(1L);
    Resource resource2 = expectedResources.get(2L);

    List<Resource> result = service.getResourcesByCategory(categoryId);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(2));
    assertThat(result, contains(resource1, resource2));
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
    Resource resource1 = expectedResources.get(1L);
    Resource resource2 = expectedResources.get(2L);

    List<Resource> result = service.getResourcesByCategory(categoryId);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(2));
    assertThat(result, contains(resource1, resource2));
  }

  /**
   * Test of listAvailableResources method, of class ResourceService.
   */
  @Test
  public void testListAvailableResourcesWithReservationOutOfRange() {
    Resource resource1 = expectedResources.get(1L);
    Resource resource2 = expectedResources.get(2L);
    Resource resource3 = expectedResources.get(3L);
    String instanceId = "resourcesManager42";
    String startDate = String.valueOf(1320134400000L - 3600000L - 3600000L);
    String endDate = String.valueOf(1320134400000L - 3600000L);
    List<Resource> result = service.listAvailableResources(instanceId, startDate, endDate);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(3));
    assertThat(result, contains(resource2, resource1, resource3));
  }

  @Test
  public void testListAvailableResourcesJustBeforeReservation() {
    String instanceId = "resourcesManager42";
    Resource resource1 = expectedResources.get(1L);
    Resource resource2 = expectedResources.get(2L);
    Resource resource3 = expectedResources.get(3L);

    //Just before reservation range
    String startDate = String.valueOf(1320134400000L - 3600000L);
    String endDate = String.valueOf(1320134400000L);
    List<Resource> result = service.listAvailableResources(instanceId, startDate, endDate);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(3));
    assertThat(result, contains(resource2, resource1, resource3));
  }

  @Test
  public void testListAvailableResourcesInReservationRange() {
    String instanceId = "resourcesManager42";
    String startDate = String.valueOf(1320134400000L);
    String endDate = String.valueOf(1320134400000L + 3600000L);
    List<Resource> result = service.listAvailableResources(instanceId, startDate, endDate);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(1));
    assertThat(result, contains(expectedResources.get(2L)));
  }

  @Test
  public void testListAvailableResourcesAfterReservationRange() {
    Resource resource1 = expectedResources.get(1L);
    Resource resource2 = expectedResources.get(2L);
    Resource resource3 = expectedResources.get(3L);
    String instanceId = "resourcesManager42";
    String startDate = String.valueOf(1320163200000L);
    String endDate = String.valueOf(1320163200000L + 3600000L);

    List<Resource> result = service.listAvailableResources(instanceId, startDate, endDate);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(3));
    assertThat(result, contains(resource2, resource1, resource3));
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
    assertThat(result, contains(expectedResources.get(2L)));
  }


  /**
   * Test of findAllReservedResources method, of class ResourceService.
   */
  @Test
  public void testFindAllReservedResources() {
    Resource resource1 = expectedResources.get(1L);
    Resource resource3 = expectedResources.get(3L);
    long reservationIdToSkip = -1;
    String startDate = String.valueOf(1320134400000L - 7200000L);
    String endDate = String.valueOf(1320163200000L + 7200000L);
    List<Long> futureReservedResourceIds = Arrays.asList(1L, 2L, 3L, 5L, 8L);

    List<Resource> result = service
        .findAllReservedResources(reservationIdToSkip, futureReservedResourceIds, startDate,
            endDate);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(2));
    assertThat(result, contains(resource1, resource3));
  }

}
