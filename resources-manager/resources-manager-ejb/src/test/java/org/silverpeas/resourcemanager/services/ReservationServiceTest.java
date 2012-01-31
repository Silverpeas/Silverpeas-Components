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
 * "http://www.silverpeas.org/legal/licensing"
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

import com.silverpeas.resourcesmanager.model.ResourceStatus;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.DBUtil;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.resourcemanager.model.Reservation;
import org.silverpeas.resourcemanager.model.ReservedResource;
import org.silverpeas.resourcemanager.model.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 *
 * @author ehugonnet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-resource-manager-datasource.xml",
  "/spring-resource-manager.xml"})
@Transactional
@TransactionConfiguration(transactionManager = "jpaTransactionManager")
public class ReservationServiceTest {

  public ReservationServiceTest() {
  }
  private static ReplacementDataSet dataSet;
  @Inject
  private ReservationService service;
  @Inject
  private ResourceService resourceService;
  @Inject
  @Named("jpaDataSource")
  private DataSource dataSource;

  @BeforeClass
  public static void prepareDataset() throws Exception {
    FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
    dataSet = new ReplacementDataSet(builder.build(ReservationServiceTest.class.getClassLoader().
        getResourceAsStream(
        "org/silverpeas/resourcemanager/services/reservations_validation_dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
  }

  @Before
  public void generalSetUp() throws Exception {
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
    DBUtil.getInstanceForTest(dataSource.getConnection());
  }

  /**
   * Test of createReservation method, of class ReservationService.
   */
  @Test
  public void testCreateReservation() {
    String instanceId = "resourcesManager42";
    Reservation reservation = new Reservation("Test de la Toussaint", new Date(1320134400000L),
        new Date(1320163200000L), "To test", "at work");
    reservation.setInstanceId(instanceId);
    int id = Integer.parseInt(service.createReservation(reservation));
    Reservation createdReservation = service.getReservation(id);
    reservation.setId(String.valueOf(id));
    assertThat(reservation, is(createdReservation));
  }

  /**
   * Test of computeReservationStatus method, of class ReservationService.
   */
  @Test
  public void testComputeReservationStatus() {
    int reservationId = 3;
    Reservation reservation = service.getReservation(reservationId);
    String status = service.computeReservationStatus(reservation);
    assertThat(status, is(ResourceStatus.STATUS_FOR_VALIDATION));
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
    int reservationId = 3;
    String instanceId = "resourcesManager42";
    Reservation reservation = service.getReservation(reservationId);
    Reservation expectedResult = new Reservation("3", "Test de la Toussaint",
        new Date(1320134400000L), new Date(1320163200000L), "To test", "at work", "2",
        new Date(1319811924467L), new Date(1319811924467L), instanceId, "test");
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
    int reservationId = 3;
    Reservation reservation = service.getReservation(reservationId);
    Reservation expectedResult = new Reservation("3", "Test de la Toussaint",
        new Date(1320134400000L), new Date(1320163200000L), "To test", "at work", "2",
        new Date(1319811924467L), new Date(1319811924467L), "resourcesManager42", "test");
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
    assertThat(reservations, containsInAnyOrder(new Reservation("3", "Test de la Toussaint",
        new Date(1320134400000L),
        new Date(1320163200000L), "To test", "at work", "2", new Date(1319811924467L), new Date(
        1319811924467L), "resourcesManager42"),
        new Reservation("4", "Test réservation 20/12/2011", new Date(1324368000000L),
        new Date(1324375200000L), "To test a reservzation", "at work", "9", new Date(1320225012008L),
        new Date(1320225012008L), "resourcesManager42"),
        new Reservation("5", "Test réservation validée 20/12/2011", new Date(1324368000000L),
        new Date(1324375200000L), "To test a reservzation validated", "at work", "2", new Date(
        1319811924467L), new Date(1319811924467L), "resourcesManager42"),
        new Reservation("6", "Test réservation refusée 20/12/2011", new Date(1324375200000L),
        new Date(1324382400000L), "To test a reservzation refused", "at work", "2", new Date(
        1319811924467L), new Date(1319811924467L), "resourcesManager42")));
  }

  /**
   * Test of findAllReservationsForValidation method, of class ReservationService.
   */
  @Test
  public void testFindAllReservationsForValidation() {
    String instanceId = "resourcesManager42";
    Integer userId = 2;
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.MONTH, Calendar.NOVEMBER);
    calend.set(Calendar.YEAR, 2011);
    calend.set(Calendar.DAY_OF_MONTH, 10);
    String endPeriod = String.valueOf(DateUtil.getEndDateOfMonth(calend.getTime()).getTime());
    String startPeriod = String.valueOf(DateUtil.getFirstDateOfMonth(calend.getTime()).getTime());
    List<Reservation> reservations = service.findAllReservationsForValidation(instanceId, userId,
        startPeriod, endPeriod);
    assertThat(reservations, is(notNullValue()));
    assertThat(reservations, hasSize(1));
    assertThat(reservations, containsInAnyOrder(new Reservation("3", "Test de la Toussaint",
        new Date(1320134400000L), new Date(1320163200000L), "To test", "at work", "2", new Date(
        1319811924467L), new Date(
        1319811924467L), "resourcesManager42")));

    calend = Calendar.getInstance();
    calend.set(Calendar.MONTH, Calendar.DECEMBER);
    calend.set(Calendar.YEAR, 2011);
    calend.set(Calendar.DAY_OF_MONTH, 10);
    endPeriod = String.valueOf(DateUtil.getEndDateOfMonth(calend.getTime()).getTime());
    startPeriod = String.valueOf(DateUtil.getFirstDateOfMonth(calend.getTime()).getTime());
    reservations = service.findAllReservationsForValidation(instanceId, userId,
        startPeriod, endPeriod);
    assertThat(reservations, is(notNullValue()));
    assertThat(reservations, hasSize(2));

    assertThat(reservations, containsInAnyOrder(new Reservation("5",
        "Test réservation validée 20/12/2011", new Date(1324368000000L), new Date(1324375200000L),
        "To test a reservzation validated", "at work", "2", new Date(1319811924467L), new Date(
        1319811924467L), "resourcesManager42"), new Reservation("6",
        "Test réservation refusée 20/12/2011", new Date(1324375200000L), new Date(1324382400000L),
        "To test a reservzation refused", "at work", "2", new Date(1319811924467L), new Date(
        1319811924467L), "resourcesManager42")));

  }

  /**
   * Test of findAllReservationsForCategoryInRange method, of class ReservationService.
   */
  @Test
  public void testFindAllReservationsForCategoryInRange() {
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.MONTH, Calendar.DECEMBER);
    calend.set(Calendar.YEAR, 2011);
    String endPeriod = String.valueOf(DateUtil.getEndDateOfMonth(calend.getTime()).getTime());
    String startPeriod = String.valueOf(DateUtil.getFirstDateOfMonth(calend.getTime()).getTime());
    List<Reservation> reservations = service.findAllReservationsForCategoryInRange(1, startPeriod,
        endPeriod);
    assertThat(reservations, is(notNullValue()));
    assertThat(reservations, hasSize(2));
    assertThat(reservations, containsInAnyOrder(new Reservation("5",
        "Test réservation validée 20/12/2011", new Date(1324368000000L), new Date(1324375200000L),
        "To test a reservzation validated", "at work", "2", new Date(1319811924467L), new Date(
        1319811924467L), "resourcesManager42"), new Reservation("6",
        "Test réservation refusée 20/12/2011", new Date(
        1324375200000L), new Date(1324382400000L), "To test a reservzation refused", "at work",
        "2", new Date(1319811924467L), new Date(1319811924467L), "resourcesManager42")));
  }
}
