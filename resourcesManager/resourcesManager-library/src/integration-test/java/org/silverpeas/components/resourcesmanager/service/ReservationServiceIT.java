/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.resourcesmanager.service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.components.resourcesmanager.model.Reservation;
import org.silverpeas.components.resourcesmanager.model.ReservedResource;
import org.silverpeas.components.resourcesmanager.model.Resource;
import org.silverpeas.components.resourcesmanager.model.ResourceStatus;
import org.silverpeas.components.resourcesmanager.test.WarBuilder4ResourcesManager;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.identifier.UniqueLongIdentifier;
import org.silverpeas.core.test.integration.rule.DbSetupRule;
import org.silverpeas.core.test.unit.EntityIdSetter;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ServiceProvider;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author ehugonnet
 */
@RunWith(Arquillian.class)
public class ReservationServiceIT {

  private final EntityIdSetter idSetter = new EntityIdSetter(UniqueLongIdentifier.class);

  private final Map<String, Reservation> expectedReservations = new HashMap<>();

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom("create-database.sql")
      .loadInitialDataSetFrom("reservations_validation_dataset.sql");

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4ResourcesManager.onWarForTestClass(ReservationServiceIT.class).build();
  }

  private ReservationService service;
  private ResourceService resourceService;
  private ReservedResourceService reservedResourceService;

  @Before
  public void generalSetUp() throws Exception {
    service = ServiceProvider.getService(ReservationService.class);
    resourceService = ServiceProvider.getService(ResourceService.class);
    reservedResourceService = ServiceProvider.getService(ReservedResourceService.class);
  }

  @Before
  public void prepareExpectedReservations() {
    String instanceId = "resourcesManager42";

    Reservation expected1 = new Reservation("Test de la Toussaint", new Date(1320134400000L),
        new Date(1320163200000L), "To test", "at work");
    idSetter.setIdTo(expected1, "3");
    expected1.setInstanceId(instanceId);
    expected1.setUserId("2");
    expected1.setCreationDate(new Date(1319811924467L));
    expected1.setUpdateDate(new Date(1319811924467L));
    expectedReservations.put(expected1.getId(), expected1);

    Reservation expected2 = new Reservation("Test réservation 20/12/2011", new Date(1324368000000L),
        new Date(1324375200000L), "To test a reservzation", "at work");
    idSetter.setIdTo(expected2, "4");
    expected2.setInstanceId(instanceId);
    expected2.setUserId("9");
    expected2.setCreationDate(new Date(1320225012008L));
    expected2.setUpdateDate(new Date(1320225012008L));
    expectedReservations.put(expected2.getId(), expected2);

    Reservation expected3 = new Reservation("Test réservation validée 20/12/2011",
        new Date(1324368000000L),
        new Date(1324375200000L), "To test a reservzation validated", "at work");
    idSetter.setIdTo(expected3, "5");
    expected3.setInstanceId(instanceId);
    expected3.setUserId("2");
    expected3.setCreationDate(new Date(1319811924467L));
    expected3.setUpdateDate(new Date(1319811924467L));
    expectedReservations.put(expected3.getId(), expected3);

    Reservation expected4 = new Reservation("Test réservation refusée 20/12/2011",
        new Date(1324375200000L),
        new Date(1324382400000L), "To test a reservzation refused", "at work");
    idSetter.setIdTo(expected4, "6");
    expected4.setInstanceId(instanceId);
    expected4.setUserId("2");
    expected4.setCreationDate(new Date(1319811924467L));
    expected4.setUpdateDate(new Date(1319811924467L));
    expectedReservations.put(expected4.getId(), expected4);
  }

  /**
   * Test of createReservation method, of class ReservationService.
   */
  @Test
  public void testCreateReservation() {
    Transaction.performInOne(() -> {
      String instanceId = "resourcesManager42";
      Reservation reservation = new Reservation("Test de la Toussaint", new Date(1320134400000L),
          new Date(1320163200000L), "To test", "at work");
      reservation.setInstanceId(instanceId);
      service.createReservation(reservation, Collections.<Long>emptyList());
      Reservation createdReservation = service.getReservation(reservation.getIdAsLong());
      return null;
    });
  }

  /**
   * Test of createReservation method, of class ReservationService.
   */
  @Test
  public void testCreateReservationWithResources() {
    String instanceId = "resourcesManager42";
    Reservation reservation =
        new Reservation("Test de la Toussaint", new Date(1320134400000L), new Date(1320163200000L),
            "To test", "at work");
    reservation.setInstanceId(instanceId);
    service.createReservation(reservation, Arrays.asList(1L, 2L));
    Reservation createdReservation = service.getReservation(reservation.getIdAsLong());
    assertThat(reservation, is(createdReservation));
    List<Resource> resources =
        resourceService.listResourcesOfReservation(reservation.getIdAsLong());
    assertThat(resources, is(notNullValue()));
    assertThat(resources, hasSize(2));
  }

  /**
   * Test of createReservation method, of class ReservationService.
   */
  @Test
  public void testCreateReservationFromUserNotManagerOfResource() {
    String instanceId = "resourcesManager42";
    Reservation reservation =
        new Reservation("Test de Noel", new Date(1320134400000L), new Date(1320163200000L),
            "To test", "at work");
    reservation.setInstanceId(instanceId);
    reservation.setUserId("5");
    service.createReservation(reservation, Arrays.asList(5L));
    Reservation createdReservation = service.getReservation(reservation.getIdAsLong());
    assertThat(reservation, is(createdReservation));
    List<Resource> resources =
        resourceService.listResourcesOfReservation(reservation.getIdAsLong());
    assertThat(resources, is(notNullValue()));
    assertThat(resources, hasSize(1));
    List<ReservedResource> reservedResources =
        reservedResourceService.findAllReservedResourcesOfReservation(reservation.getIdAsLong());
    assertThat(reservedResources, is(notNullValue()));
    assertThat(reservedResources, hasSize(1));
    String status = reservedResources.get(0).getStatus();
    assertThat(status, is(ResourceStatus.STATUS_FOR_VALIDATION));
  }


  /**
   * Test of createReservation method, of class ReservationService.
   */
  @Test
  public void testCreateReservationWithResourceFromUserManagerOfResource() {
    String instanceId = "resourcesManager42";
    Reservation reservation =
        new Reservation("Autre Test de Noel", new Date(1320134400000L), new Date(1320163200000L),
            "To test", "at work");
    reservation.setInstanceId(instanceId);
    reservation.setUserId("3");
    service.createReservation(reservation, Arrays.asList(5L));
    Reservation createdReservation = service.getReservation(reservation.getIdAsLong());
    assertThat(reservation, is(createdReservation));
    List<Resource> resources =
        resourceService.listResourcesOfReservation(reservation.getIdAsLong());
    assertThat(resources, is(notNullValue()));
    assertThat(resources, hasSize(1));
    List<ReservedResource> reservedResources =
        reservedResourceService.findAllReservedResourcesOfReservation(reservation.getIdAsLong());
    assertThat(reservedResources, is(notNullValue()));
    assertThat(reservedResources, hasSize(1));
    String status = reservedResources.get(0).getStatus();
    assertThat(status, is(ResourceStatus.STATUS_VALIDATE));
  }


  /**
   * Test of computeReservationStatus method, of class ReservationService.
   */
  @Test
  public void testComputeReservationStatus() {
    int reservationId = 3;
    Reservation reservation = service.getReservation(reservationId);
    String status = service.computeReservationStatus(reservation);
    assertThat(status, is(ResourceStatus.STATUS_REFUSED));
  }

  /**
   * Test of updateReservation method, of class ReservationService.
   */
  @Test
  public void testUpdateReservation() {
    int reservationId = 3;
    Reservation reservation = service.getReservation(reservationId);
    reservation.setEvent("Updating event");
    service.updateReservation(reservation);
    Reservation updateReservation = service.getReservation(reservationId);
    assertThat(updateReservation, is(reservation));
  }

  /**
   * Test of getReservation method, of class ReservationService.
   */
  @Test
  public void testGetReservation() {
    long reservationId = 3L;
    Reservation reservation = service.getReservation(reservationId);
    Reservation expectedResult = expectedReservations.get(String.valueOf(reservationId));
    assertThat(reservation, is(expectedResult));
    List<Resource> resources = resourceService.listResourcesOfReservation(reservationId);
    assertThat(resources, is(notNullValue()));
    assertThat(resources, hasSize(3));
  }

  /**
   * Test of deleteReservation method, of class ReservationService.
   */
  @Test
  public void testDeleteReservation() {
    long reservationId = 3L;
    Reservation reservation = service.getReservation(reservationId);
    Reservation expectedResult = expectedReservations.get(String.valueOf(reservationId));
    assertThat(reservation, is(expectedResult));
    List<Resource> resources = resourceService.listResourcesOfReservation(reservationId);
    assertThat(resources, is(notNullValue()));
    assertThat(resources, hasSize(3));
    service.deleteReservation(reservationId);
    reservation = service.getReservation(reservationId);
    assertThat(reservation, is(nullValue()));
    resources = resourceService.listResourcesOfReservation(reservationId);
    assertThat(resources, is(notNullValue()));
    assertThat(resources, hasSize(0));
  }

  /**
   * Test of findAllReservations method, of class ReservationService.
   */
  @Test
  public void testFindAllReservations() {
    Reservation reservation3 = expectedReservations.get("3");
    Reservation reservation4 = expectedReservations.get("4");
    Reservation reservation5 = expectedReservations.get("5");
    Reservation reservation6 = expectedReservations.get("6");

    List<Reservation> reservations = service.findAllReservations("resourcesManager42");
    assertThat(reservations, is(notNullValue()));
    assertThat(reservations, hasSize(4));
    assertThat(reservations,
        containsInAnyOrder(reservation3, reservation4, reservation5, reservation6));
  }

  /**
   * Test of findAllReservations method, of class ReservationService.
   */
  @Test
  public void testFindAllReservationsForUser() {
    Reservation reservation3 = expectedReservations.get("3");
    Reservation reservation5 = expectedReservations.get("5");
    Reservation reservation6 = expectedReservations.get("6");

    List<Reservation> reservations = service.findAllReservationsForUser("resourcesManager42", 2);
    assertThat(reservations, is(notNullValue()));
    assertThat(reservations, hasSize(3));
    assertThat(reservations, containsInAnyOrder(reservation3, reservation5, reservation6));
  }

  /**
   * Test of findAllReservationsForValidation method, of class ReservationService.
   */
  @Test
  public void testFindAllReservationsForValidation() {
    String instanceId = "resourcesManager42";
    Long userId = 0L;
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.MONTH, Calendar.NOVEMBER);
    calend.set(Calendar.YEAR, 2011);
    calend.set(Calendar.DAY_OF_MONTH, 10);
    String endPeriod = String.valueOf(DateUtil.getEndDateOfMonth(calend.getTime()).getTime());
    String startPeriod = String.valueOf(DateUtil.getFirstDateOfMonth(calend.getTime()).getTime());
    List<Reservation> reservations =
        service.findAllReservationsForValidation(instanceId, userId, startPeriod, endPeriod);
    assertThat(reservations, is(notNullValue()));
    assertThat(reservations, hasSize(0));
    calend = Calendar.getInstance();
    calend.set(Calendar.MONTH, Calendar.DECEMBER);
    calend.set(Calendar.YEAR, 2011);
    calend.set(Calendar.DAY_OF_MONTH, 10);
    endPeriod = String.valueOf(DateUtil.getEndDateOfMonth(calend.getTime()).getTime());
    startPeriod = String.valueOf(DateUtil.getFirstDateOfMonth(calend.getTime()).getTime());
    reservations =
        service.findAllReservationsForValidation(instanceId, userId, startPeriod, endPeriod);
    assertThat(reservations, is(notNullValue()));
    assertThat(reservations, hasSize(1));

    assertThat(reservations, containsInAnyOrder(expectedReservations.get("4")));
    userId = 2L;
    reservations =
        service.findAllReservationsForValidation(instanceId, userId, startPeriod, endPeriod);
    assertThat(reservations, is(notNullValue()));
    assertThat(reservations, hasSize(0));
  }

  /**
   * Test of findAllReservationsForValidation method, of class ReservationService.
   */
  @Test
  public void testFindAllReservationsForAllUsersInRange() {
    Reservation reservation3 = expectedReservations.get("3");
    Reservation reservation4 = expectedReservations.get("4");
    Reservation reservation5 = expectedReservations.get("5");
    Reservation reservation6 = expectedReservations.get("6");
    String instanceId = "resourcesManager42";

    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.MONTH, Calendar.NOVEMBER);
    calend.set(Calendar.YEAR, 2011);
    calend.set(Calendar.DAY_OF_MONTH, 10);
    String endPeriod = String.valueOf(DateUtil.getEndDateOfMonth(calend.getTime()).getTime());
    String startPeriod = String.valueOf(DateUtil.getFirstDateOfMonth(calend.getTime()).getTime());
    List<Reservation> reservations =
        service.findAllReservationsInRange(instanceId, null, startPeriod, endPeriod);
    assertThat(reservations, is(notNullValue()));
    assertThat(reservations, hasSize(1));
    assertThat(reservations, containsInAnyOrder(reservation3));

    calend = Calendar.getInstance();
    calend.set(Calendar.MONTH, Calendar.DECEMBER);
    calend.set(Calendar.YEAR, 2011);
    calend.set(Calendar.DAY_OF_MONTH, 10);
    endPeriod = String.valueOf(DateUtil.getEndDateOfMonth(calend.getTime()).getTime());
    startPeriod = String.valueOf(DateUtil.getFirstDateOfMonth(calend.getTime()).getTime());
    reservations = service.findAllReservationsInRange(instanceId, null, startPeriod, endPeriod);
    assertThat(reservations, is(notNullValue()));
    assertThat(reservations, hasSize(3));

    assertThat(reservations, containsInAnyOrder(reservation4, reservation5, reservation6));
  }

  /**
   * Test of findAllReservationsForValidation method, of class ReservationService.
   */
  @Test
  public void testFindAllReservationsForUser2InRange() {
    String instanceId = "resourcesManager42";
    Reservation reservation3 = expectedReservations.get("3");
    Reservation reservation5 = expectedReservations.get("5");
    Reservation reservation6 = expectedReservations.get("6");

    int userId = 2;
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.MONTH, Calendar.NOVEMBER);
    calend.set(Calendar.YEAR, 2011);
    calend.set(Calendar.DAY_OF_MONTH, 10);
    String endPeriod = String.valueOf(DateUtil.getEndDateOfMonth(calend.getTime()).getTime());
    String startPeriod = String.valueOf(DateUtil.getFirstDateOfMonth(calend.getTime()).getTime());
    List<Reservation> reservations =
        service.findAllReservationsInRange(instanceId, userId, startPeriod, endPeriod);
    assertThat(reservations, is(notNullValue()));
    assertThat(reservations, hasSize(1));
    assertThat(reservations, containsInAnyOrder(reservation3));

    calend = Calendar.getInstance();
    calend.set(Calendar.MONTH, Calendar.DECEMBER);
    calend.set(Calendar.YEAR, 2011);
    calend.set(Calendar.DAY_OF_MONTH, 10);
    endPeriod = String.valueOf(DateUtil.getEndDateOfMonth(calend.getTime()).getTime());
    startPeriod = String.valueOf(DateUtil.getFirstDateOfMonth(calend.getTime()).getTime());
    reservations = service.findAllReservationsInRange(instanceId, userId, startPeriod, endPeriod);
    assertThat(reservations, is(notNullValue()));
    assertThat(reservations, hasSize(2));

    assertThat(reservations, containsInAnyOrder(reservation5, reservation6));
  }

  /**
   * Test of findAllReservationsForValidation method, of class ReservationService.
   */
  @Test
  public void testFindAllReservationsForUnknownUserInRange() {
    String instanceId = "resourcesManager42";
    int userId = 15;
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.MONTH, Calendar.NOVEMBER);
    calend.set(Calendar.YEAR, 2011);
    calend.set(Calendar.DAY_OF_MONTH, 10);
    String endPeriod = String.valueOf(DateUtil.getEndDateOfMonth(calend.getTime()).getTime());
    String startPeriod = String.valueOf(DateUtil.getFirstDateOfMonth(calend.getTime()).getTime());
    List<Reservation> reservations =
        service.findAllReservationsInRange(instanceId, userId, startPeriod, endPeriod);
    assertThat(reservations, is(notNullValue()));
    assertThat(reservations, hasSize(0));

    calend = Calendar.getInstance();
    calend.set(Calendar.MONTH, Calendar.DECEMBER);
    calend.set(Calendar.YEAR, 2011);
    calend.set(Calendar.DAY_OF_MONTH, 10);
    endPeriod = String.valueOf(DateUtil.getEndDateOfMonth(calend.getTime()).getTime());
    startPeriod = String.valueOf(DateUtil.getFirstDateOfMonth(calend.getTime()).getTime());
    reservations = service.findAllReservationsInRange(instanceId, userId, startPeriod, endPeriod);
    assertThat(reservations, is(notNullValue()));
    assertThat(reservations, hasSize(0));
  }

  /**
   * Test of findAllReservationsForCategoryInRange method, of class ReservationService.
   */
  @Test
  public void testFindAllReservationsForAllUsersAndCategoryInRange() {
    String instanceId = "resourcesManager42";
    Reservation reservation5 = expectedReservations.get("5");
    Reservation reservation6 = expectedReservations.get("6");

    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.MONTH, Calendar.DECEMBER);
    calend.set(Calendar.YEAR, 2011);
    String endPeriod = String.valueOf(DateUtil.getEndDateOfMonth(calend.getTime()).getTime());
    String startPeriod = String.valueOf(DateUtil.getFirstDateOfMonth(calend.getTime()).getTime());
    List<Reservation> reservations =
        service.findAllReservationsForCategoryInRange(instanceId, null, 1L, startPeriod, endPeriod);
    assertThat(reservations, is(notNullValue()));
    assertThat(reservations, hasSize(2));
    assertThat(reservations, containsInAnyOrder(reservation5, reservation6));
  }


  /**
   * Test of findAllReservationsForCategoryInRange method, of class ReservationService.
   */
  @Test
  public void testFindAllReservationsForUser2AndCategoryInRange() {
    String instanceId = "resourcesManager42";
    Reservation reservation5 = expectedReservations.get("5");
    Reservation reservation6 = expectedReservations.get("6");

    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.MONTH, Calendar.DECEMBER);
    calend.set(Calendar.YEAR, 2011);
    String endPeriod = String.valueOf(DateUtil.getEndDateOfMonth(calend.getTime()).getTime());
    String startPeriod = String.valueOf(DateUtil.getFirstDateOfMonth(calend.getTime()).getTime());
    List<Reservation> reservations =
        service.findAllReservationsForCategoryInRange(instanceId, 2, 1L, startPeriod, endPeriod);
    assertThat(reservations, is(notNullValue()));
    assertThat(reservations, hasSize(2));
    assertThat(reservations, containsInAnyOrder(reservation5, reservation6));
  }


  /**
   * Test of findAllReservationsForCategoryInRange method, of class ReservationService.
   */
  @Test
  public void testFindAllReservationsForUnknownUserAndCategoryInRange() {
    String instanceId = "resourcesManager42";
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.MONTH, Calendar.DECEMBER);
    calend.set(Calendar.YEAR, 2011);
    String endPeriod = String.valueOf(DateUtil.getEndDateOfMonth(calend.getTime()).getTime());
    String startPeriod = String.valueOf(DateUtil.getFirstDateOfMonth(calend.getTime()).getTime());
    List<Reservation> reservations =
        service.findAllReservationsForCategoryInRange(instanceId, 5, 1L, startPeriod, endPeriod);
    assertThat(reservations, is(notNullValue()));
    assertThat(reservations, hasSize(0));
  }

  /**
   * Test of findAllReservationsForCategoryInRange method, of class ReservationService.
   */
  @Test
  public void testFindAllReservationsForAllUsersAndResourceInRange() {
    String instanceId = "resourcesManager42";
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.MONTH, Calendar.DECEMBER);
    calend.set(Calendar.YEAR, 2011);
    String endPeriod = String.valueOf(DateUtil.getEndDateOfMonth(calend.getTime()).getTime());
    String startPeriod = String.valueOf(DateUtil.getFirstDateOfMonth(calend.getTime()).getTime());
    List<Reservation> reservations =
        service.findAllReservationsForResourceInRange(instanceId, null, 1L, startPeriod, endPeriod);
    assertThat(reservations, is(notNullValue()));
    assertThat(reservations, hasSize(1));
    assertThat(reservations, containsInAnyOrder(expectedReservations.get("5")));
  }

  /**
   * Test of findAllReservationsForCategoryInRange method, of class ReservationService.
   */
  @Test
  public void testFindAllReservationsForUser2AndResourceInRange() {
    String instanceId = "resourcesManager42";
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.MONTH, Calendar.DECEMBER);
    calend.set(Calendar.YEAR, 2011);
    String endPeriod = String.valueOf(DateUtil.getEndDateOfMonth(calend.getTime()).getTime());
    String startPeriod = String.valueOf(DateUtil.getFirstDateOfMonth(calend.getTime()).getTime());
    List<Reservation> reservations =
        service.findAllReservationsForResourceInRange(instanceId, 2, 1L, startPeriod, endPeriod);
    assertThat(reservations, is(notNullValue()));
    assertThat(reservations, hasSize(1));
    assertThat(reservations, containsInAnyOrder(expectedReservations.get("5")));
  }

  /**
   * Test of findAllReservationsForCategoryInRange method, of class ReservationService.
   */
  @Test
  public void testFindAllReservationsForUnknownUserAndResourceInRange() {
    String instanceId = "resourcesManager42";
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.MONTH, Calendar.DECEMBER);
    calend.set(Calendar.YEAR, 2011);
    String endPeriod = String.valueOf(DateUtil.getEndDateOfMonth(calend.getTime()).getTime());
    String startPeriod = String.valueOf(DateUtil.getFirstDateOfMonth(calend.getTime()).getTime());
    List<Reservation> reservations =
        service.findAllReservationsForResourceInRange(instanceId, 5, 1L, startPeriod, endPeriod);
    assertThat(reservations, is(notNullValue()));
    assertThat(reservations, hasSize(0));
  }


}
