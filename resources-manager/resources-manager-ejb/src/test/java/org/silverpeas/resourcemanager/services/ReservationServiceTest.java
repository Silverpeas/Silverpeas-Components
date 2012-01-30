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
import com.stratelia.webactiv.util.DBUtil;
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
    ReservedResource reservedResource = new ReservedResource();
    reservedResource.setReservation(reservation);
    reservedResource.setResource(resourceService.getResource(1));
    reservation.getListResourcesReserved().add(reservedResource);
    reservedResource.setStatus(ResourceStatus.STATUS_FOR_VALIDATION);
    reservedResource = new ReservedResource();
    reservedResource.setReservation(reservation);
    reservedResource.setResource(resourceService.getResource(2));
    reservedResource.setStatus(ResourceStatus.STATUS_FOR_VALIDATION);
    reservation.getListResourcesReserved().add(reservedResource);
    int id = Integer.parseInt(service.createReservation(reservation));


    Reservation createdReservation = service.getReservation(id);

    ReservedResource resource1 = new ReservedResource();
    resource1.setReservationId(id);
    resource1.setResourceId(1);
    resource1.setStatus(ResourceStatus.STATUS_FOR_VALIDATION);
    ReservedResource resource2 = new ReservedResource();
    resource2.setReservationId(2);
    resource2.setResourceId(3);
    resource2.setStatus(ResourceStatus.STATUS_FOR_VALIDATION);

    reservation.setId(String.valueOf(id));

    assertThat(createdReservation.getListResourcesReserved(), is(notNullValue()));
    assertThat(createdReservation.getListResourcesReserved(), hasSize(2));
    assertThat(createdReservation.getListResourcesReserved(), containsInAnyOrder(resource1,
        resource2));
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
    System.out.println("updateReservation");
    Reservation reservation = null;
    ReservationService instance = new ReservationService();
    instance.updateReservation(reservation);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getReservations method, of class ReservationService.
   */
  @Test
  public void testGetReservations() {
    System.out.println("getReservations");
    ReservationService instance = new ReservationService();
    List expResult = null;
    List result = instance.getReservations();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
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
    assertThat(reservation.getListResourcesReserved(), is(notNullValue()));
    assertThat(reservation.getListResourcesReserved(), hasSize(3));
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
    assertThat(reservation.getListResourcesReserved(), is(notNullValue()));
    assertThat(reservation.getListResourcesReserved(), hasSize(3));
    Resource resource = resourceService.getResource(1);
    assertThat(resource.getReservedResources(), is(notNullValue()));
    assertThat(resource.getReservedResources(), hasSize(2));
    service.deleteReservation(reservationId);
    reservation = service.getReservation(reservationId);
    assertThat(reservation, is(nullValue()));
    resource = resourceService.getResource(1);
    assertThat(resource.getReservedResources(), is(notNullValue()));
    assertThat(resource.getReservedResources(), hasSize(1));
  }

  /**
   * Test of findAllReservations method, of class ReservationService.
   */
  @Test
  public void testFindAllReservations() {
    List<Reservation> reservations = service.findAllReservations("resourcesManager42");
    assertThat(reservations, is(notNullValue()));
    assertThat(reservations, hasSize(4));
    assertThat(reservations, containsInAnyOrder(new Reservation("3", "Test de la Toussaint", new Date(1320134400000L),
        new Date(1320163200000L),"To test","at work", "2",new Date(1319811924467L), new Date(1319811924467L), "resourcesManager42")/*,
        new Reservation("4", "Test réservation 20/12/2011", new Date(1324368000000L),
        new Date(1324375200000L),"To test a reservzation","at work", "9",new Date(1320225012008L), new Date(1320225012008L), "resourcesManager42")*/));
/*
  <sc_resources_reservation id="5" instanceId="resourcesManager42" evenement="Test réservation validée 20/12/2011"
                            userId="2" creationdate="1319811924467" updatedate="1319811924467" 
                            begindate="1324368000000"  enddate="1324375200000"
                            reason="To test a reservzation validated" place="at work" status="V" />
  <sc_resources_reservation id="6" instanceId="resourcesManager42" evenement="Test réservation refusée 20/12/2011"
                            userId="2" creationdate="1319811924467" updatedate="1319811924467" 
                            begindate="1324375200000"  enddate="1324382400000"
                            reason="To test a reservzation refused" place="at work" status="R" />*/
  }

  /**
   * Test of findAllReservationsForValidation method, of class ReservationService.
   */
  @Test
  public void testFindAllReservationsForValidation() {
    System.out.println("findAllReservationsForValidation");
    String instanceId = "";
    Integer userId = null;
    String startPeriod = "";
    String endPeriod = "";
    ReservationService instance = new ReservationService();
    List expResult = null;
    List result = instance.findAllReservationsForValidation(instanceId, userId, startPeriod,
        endPeriod);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of findAllReservationsForCategoryInRange method, of class ReservationService.
   */
  @Test
  public void testFindAllReservationsForCategoryInRange() {
    System.out.println("findAllReservationsForCategoryInRange");
    Integer categoryId = null;
    String startPeriod = "";
    String endPeriod = "";
    ReservationService instance = new ReservationService();
    List expResult = null;
    List result = instance.findAllReservationsForCategoryInRange(categoryId, startPeriod, endPeriod);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }
}
