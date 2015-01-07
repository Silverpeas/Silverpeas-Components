/*
 * Copyright (C) 2000 - 2013 Silverpeas
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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.persistence.Transaction;
import org.silverpeas.resourcemanager.model.Reservation;
import org.silverpeas.resourcemanager.model.ReservedResource;
import org.silverpeas.resourcemanager.model.Resource;
import org.silverpeas.resourcemanager.model.ResourceStatus;
import org.silverpeas.test.BasicWarBuilder;
import org.silverpeas.test.rule.DbUnitLoadingRule;
import org.silverpeas.util.DateUtil;
import org.silverpeas.util.ServiceProvider;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author ehugonnet
 */
@RunWith(Arquillian.class)
public class ReservationServiceTest {

  public ReservationServiceTest() {
  }

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule("create-database.sql", "reservations_validation_dataset.xml");

  @Deployment
  public static Archive<?> createTestArchive() {
    return BasicWarBuilder.onWarForTestClass(ReservationServiceTest.class)
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

  private ReservationService service;
  private ResourceService resourceService;
  private ReservedResourceService reservedResourceService;

  @Before
  public void generalSetUp() throws Exception {
    service = ServiceProvider.getService(ReservationService.class);
    resourceService = ServiceProvider.getService(ResourceService.class);
    reservedResourceService = ServiceProvider.getService(ReservedResourceService.class);
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
    String instanceId = "resourcesManager42";
    Reservation reservation = service.getReservation(reservationId);
    Reservation expectedResult =
        new Reservation(3L, "Test de la Toussaint", new Date(1320134400000L),
            new Date(1320163200000L), "To test", "at work", "2", new Date(1319811924467L),
            new Date(1319811924467L), instanceId, "test");
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
    Reservation expectedResult =
        new Reservation(3L, "Test de la Toussaint", new Date(1320134400000L),
            new Date(1320163200000L), "To test", "at work", "2", new Date(1319811924467L),
            new Date(1319811924467L), "resourcesManager42", "test");
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
    List<Reservation> reservations = service.findAllReservations("resourcesManager42");
    assertThat(reservations, is(notNullValue()));
    assertThat(reservations, hasSize(4));
    assertThat(reservations, containsInAnyOrder(
        new Reservation(3L, "Test de la Toussaint", new Date(1320134400000L),
            new Date(1320163200000L), "To test", "at work", "2", new Date(1319811924467L),
            new Date(1319811924467L), "resourcesManager42"),
        new Reservation(4L, "Test réservation 20/12/2011", new Date(1324368000000L),
            new Date(1324375200000L), "To test a reservzation", "at work", "9",
            new Date(1320225012008L), new Date(1320225012008L), "resourcesManager42"),
        new Reservation(5L, "Test réservation validée 20/12/2011", new Date(1324368000000L),
            new Date(1324375200000L), "To test a reservzation validated", "at work", "2",
            new Date(1319811924467L), new Date(1319811924467L), "resourcesManager42"),
        new Reservation(6L, "Test réservation refusée 20/12/2011", new Date(1324375200000L),
            new Date(1324382400000L), "To test a reservzation refused", "at work", "2",
            new Date(1319811924467L), new Date(1319811924467L), "resourcesManager42")));
  }

  /**
   * Test of findAllReservations method, of class ReservationService.
   */
  @Test
  public void testFindAllReservationsForUser() {
    List<Reservation> reservations = service.findAllReservationsForUser("resourcesManager42", 2);
    assertThat(reservations, is(notNullValue()));
    assertThat(reservations, hasSize(3));
    assertThat(reservations, containsInAnyOrder(
        new Reservation(3L, "Test de la Toussaint", new Date(1320134400000L),
            new Date(1320163200000L), "To test", "at work", "2", new Date(1319811924467L),
            new Date(1319811924467L), "resourcesManager42"),
        new Reservation(5L, "Test réservation validée 20/12/2011", new Date(1324368000000L),
            new Date(1324375200000L), "To test a reservzation validated", "at work", "2",
            new Date(1319811924467L), new Date(1319811924467L), "resourcesManager42"),
        new Reservation(6L, "Test réservation refusée 20/12/2011", new Date(1324375200000L),
            new Date(1324382400000L), "To test a reservzation refused", "at work", "2",
            new Date(1319811924467L), new Date(1319811924467L), "resourcesManager42")));
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

    assertThat(reservations, containsInAnyOrder(
        new Reservation(4L, "Test réservation 20/12/2011", new Date(1324368000000L),
            new Date(1324375200000L), "To test a reservzation", "at work", "9",
            new Date(1320225012008L), new Date(1320225012008L), "resourcesManager42")));
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
    assertThat(reservations, containsInAnyOrder(
        new Reservation(3L, "Test de la Toussaint", new Date(1320134400000L),
            new Date(1320163200000L), "To test", "at work", "2", new Date(1319811924467L),
            new Date(1319811924467L), "resourcesManager42")));

    calend = Calendar.getInstance();
    calend.set(Calendar.MONTH, Calendar.DECEMBER);
    calend.set(Calendar.YEAR, 2011);
    calend.set(Calendar.DAY_OF_MONTH, 10);
    endPeriod = String.valueOf(DateUtil.getEndDateOfMonth(calend.getTime()).getTime());
    startPeriod = String.valueOf(DateUtil.getFirstDateOfMonth(calend.getTime()).getTime());
    reservations = service.findAllReservationsInRange(instanceId, null, startPeriod, endPeriod);
    assertThat(reservations, is(notNullValue()));
    assertThat(reservations, hasSize(3));

    assertThat(reservations, containsInAnyOrder(
        new Reservation(4L, "Test réservation 20/12/2011", new Date(1324368000000L),
            new Date(1324375200000L), "To test a reservzation", "at work", "9",
            new Date(1320225012008L), new Date(1320225012008L), "resourcesManager42"),
        new Reservation(5L, "Test réservation validée 20/12/2011", new Date(1324368000000L),
            new Date(1324375200000L), "To test a reservzation validated", "at work", "2",
            new Date(1319811924467L), new Date(1319811924467L), "resourcesManager42"),
        new Reservation(6L, "Test réservation refusée 20/12/2011", new Date(1324375200000L),
            new Date(1324382400000L), "To test a reservzation refused", "at work", "2",
            new Date(1319811924467L), new Date(1319811924467L), "resourcesManager42")));
  }

  /**
   * Test of findAllReservationsForValidation method, of class ReservationService.
   */
  @Test
  public void testFindAllReservationsForUser2InRange() {
    String instanceId = "resourcesManager42";
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
    assertThat(reservations, containsInAnyOrder(
        new Reservation(3L, "Test de la Toussaint", new Date(1320134400000L),
            new Date(1320163200000L), "To test", "at work", "2", new Date(1319811924467L),
            new Date(1319811924467L), "resourcesManager42")));

    calend = Calendar.getInstance();
    calend.set(Calendar.MONTH, Calendar.DECEMBER);
    calend.set(Calendar.YEAR, 2011);
    calend.set(Calendar.DAY_OF_MONTH, 10);
    endPeriod = String.valueOf(DateUtil.getEndDateOfMonth(calend.getTime()).getTime());
    startPeriod = String.valueOf(DateUtil.getFirstDateOfMonth(calend.getTime()).getTime());
    reservations = service.findAllReservationsInRange(instanceId, userId, startPeriod, endPeriod);
    assertThat(reservations, is(notNullValue()));
    assertThat(reservations, hasSize(2));

    assertThat(reservations, containsInAnyOrder(
        new Reservation(5L, "Test réservation validée 20/12/2011", new Date(1324368000000L),
            new Date(1324375200000L), "To test a reservzation validated", "at work", "2",
            new Date(1319811924467L), new Date(1319811924467L), "resourcesManager42"),
        new Reservation(6L, "Test réservation refusée 20/12/2011", new Date(1324375200000L),
            new Date(1324382400000L), "To test a reservzation refused", "at work", "2",
            new Date(1319811924467L), new Date(1319811924467L), "resourcesManager42")));
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
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.MONTH, Calendar.DECEMBER);
    calend.set(Calendar.YEAR, 2011);
    String endPeriod = String.valueOf(DateUtil.getEndDateOfMonth(calend.getTime()).getTime());
    String startPeriod = String.valueOf(DateUtil.getFirstDateOfMonth(calend.getTime()).getTime());
    List<Reservation> reservations =
        service.findAllReservationsForCategoryInRange(instanceId, null, 1L, startPeriod, endPeriod);
    assertThat(reservations, is(notNullValue()));
    assertThat(reservations, hasSize(2));
    assertThat(reservations, containsInAnyOrder(
        new Reservation(5L, "Test réservation validée 20/12/2011", new Date(1324368000000L),
            new Date(1324375200000L), "To test a reservzation validated", "at work", "2",
            new Date(1319811924467L), new Date(1319811924467L), "resourcesManager42"),
        new Reservation(6L, "Test réservation refusée 20/12/2011", new Date(1324375200000L),
            new Date(1324382400000L), "To test a reservzation refused", "at work", "2",
            new Date(1319811924467L), new Date(1319811924467L), "resourcesManager42")));
  }


  /**
   * Test of findAllReservationsForCategoryInRange method, of class ReservationService.
   */
  @Test
  public void testFindAllReservationsForUser2AndCategoryInRange() {
    String instanceId = "resourcesManager42";
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.MONTH, Calendar.DECEMBER);
    calend.set(Calendar.YEAR, 2011);
    String endPeriod = String.valueOf(DateUtil.getEndDateOfMonth(calend.getTime()).getTime());
    String startPeriod = String.valueOf(DateUtil.getFirstDateOfMonth(calend.getTime()).getTime());
    List<Reservation> reservations =
        service.findAllReservationsForCategoryInRange(instanceId, 2, 1L, startPeriod, endPeriod);
    assertThat(reservations, is(notNullValue()));
    assertThat(reservations, hasSize(2));
    assertThat(reservations, containsInAnyOrder(
        new Reservation(5L, "Test réservation validée 20/12/2011", new Date(1324368000000L),
            new Date(1324375200000L), "To test a reservzation validated", "at work", "2",
            new Date(1319811924467L), new Date(1319811924467L), "resourcesManager42"),
        new Reservation(6L, "Test réservation refusée 20/12/2011", new Date(1324375200000L),
            new Date(1324382400000L), "To test a reservzation refused", "at work", "2",
            new Date(1319811924467L), new Date(1319811924467L), "resourcesManager42")));
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
    assertThat(reservations, containsInAnyOrder(
        new Reservation(5L, "Test réservation validée 20/12/2011", new Date(1324368000000L),
            new Date(1324375200000L), "To test a reservzation validated", "at work", "2",
            new Date(1319811924467L), new Date(1319811924467L), "resourcesManager42")));
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
    assertThat(reservations, containsInAnyOrder(
        new Reservation(5L, "Test réservation validée 20/12/2011", new Date(1324368000000L),
            new Date(1324375200000L), "To test a reservzation validated", "at work", "2",
            new Date(1319811924467L), new Date(1319811924467L), "resourcesManager42")));
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
