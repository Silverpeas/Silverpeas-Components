/*
 *  Copyright (C) 2000 - 2011 Silverpeas
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 * 
 *  As a special exception to the terms and conditions of version 3.0 of
 *  the GPL, you may redistribute this Program in connection with Free/Libre
 *  Open Source Software ("FLOSS") applications as described in Silverpeas's
 *  FLOSS exception.  You should have recieved a copy of the text describing
 *  the FLOSS exception, and it is also available here:
 *  "http://www.silverpeas.com/legal/licensing"
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.silverpeas.resourcesmanager.model;

import com.google.common.collect.Collections2;
import com.silverpeas.util.ToStringFunction;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Named;

import static com.silverpeas.resourcesmanager.model.ResourceStatus.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author ehugonnet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-resource-manager-embbed-datasource.xml"})
public class ReservationsDaoTest {

  @Inject
  @Named("dataSource")
  
  private DataSource dataSource;

  private static ReplacementDataSet dataSet;

  public ReservationsDaoTest() {
  }

  public Connection getConnection() throws SQLException {
    return this.dataSource.getConnection();
  }

  @BeforeClass
  public static void prepareDataset() throws Exception {
    FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
    dataSet = new ReplacementDataSet(builder.build(ReservationsDaoTest.class.getClassLoader().getResourceAsStream(
                "com/silverpeas/resourcesmanager/model/reservations_validation_dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
  }

  @Before
  public void generalSetUp() throws Exception {
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
    DBUtil.getInstanceForTest(dataSource.getConnection());
  }

  /**
   * Test of deleteReservedResource method, of class ResourcesManagerDAO.
   */
  @Test
  public void testDeleteReservedResource() throws Exception {
    Connection con = getConnection();
    String resourceId = "3";
    String instanceId = "resourcesManager42";
    try {
      List<Reservation> result = ResourcesManagerDAO.getReservations(con, instanceId);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(4));
      Map<String, Reservation> reservations = new HashMap<String, Reservation>(4);
      for (Reservation reservation : result) {
        reservations.put(reservation.getId(), reservation);
      }
      assertThat(reservations.get("3"), is(notNullValue()));
      assertThat(reservations.get("3").getListResourcesReserved(), is(notNullValue()));
      assertThat(reservations.get("3").getListResourcesReserved(), hasSize(3));
      assertThat(reservations.get("4"), is(notNullValue()));
      assertThat(reservations.get("4").getListResourcesReserved(), is(notNullValue()));
      assertThat(reservations.get("4").getListResourcesReserved(), hasSize(1));
      assertThat(reservations.get("5"), is(notNullValue()));
      assertThat(reservations.get("5").getListResourcesReserved(), is(notNullValue()));
      assertThat(reservations.get("5").getListResourcesReserved(), hasSize(1));
      assertThat(reservations.get("6"), is(notNullValue()));
      assertThat(reservations.get("6").getListResourcesReserved(), is(notNullValue()));
      assertThat(reservations.get("6").getListResourcesReserved(), hasSize(1));
      ResourcesManagerDAO.deleteReservedResource(con, resourceId);
      con.commit();
      result = ResourcesManagerDAO.getReservations(con, instanceId);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(4));
      reservations = new HashMap<String, Reservation>(4);
      for (Reservation reservation : result) {
        reservations.put(reservation.getId(), reservation);
      }
      assertThat(reservations.get("3"), is(notNullValue()));
      assertThat(reservations.get("3").getListResourcesReserved(), is(notNullValue()));
      assertThat(reservations.get("3").getListResourcesReserved(), hasSize(2));
      assertThat(reservations.get("4"), is(notNullValue()));
      assertThat(reservations.get("4").getListResourcesReserved(), is(notNullValue()));
      assertThat(reservations.get("4").getListResourcesReserved(), hasSize(0));
      assertThat(reservations.get("5"), is(notNullValue()));
      assertThat(reservations.get("5").getListResourcesReserved(), is(notNullValue()));
      assertThat(reservations.get("5").getListResourcesReserved(), hasSize(1));
      assertThat(reservations.get("6"), is(notNullValue()));
      assertThat(reservations.get("6").getListResourcesReserved(), is(notNullValue()));
      assertThat(reservations.get("6").getListResourcesReserved(), hasSize(1));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of saveReservation method, of class ResourcesManagerDAO.
   */
  @Test
  public void testSaveReservation() throws Exception {
    Connection con = getConnection();
    String reservationId = "3";
    String instanceId = "resourcesManager42";
    try {
      Reservation result = ResourcesManagerDAO.getReservation(con, instanceId, reservationId);
      Reservation expectedResult = new Reservation(reservationId, "Test de la Toussaint",
              new Date(1320134400000L), new Date(1320163200000L), "To test", "at work", "2",
              new Date(1319811924467L), new Date(1319811924467L), instanceId, "test");
      assertThat(result, is(expectedResult));
      assertThat(result.getListResourcesReserved(), is(notNullValue()));
      assertThat(result.getListResourcesReserved(), hasSize(3));
      String newReservationId = ResourcesManagerDAO.saveReservation(con, result, "3,2,1,4");
      con.commit();
      result = ResourcesManagerDAO.getReservation(con, instanceId, reservationId);
      assertThat(result, is(expectedResult));
      assertThat(result.getListResourcesReserved(), is(notNullValue()));
      assertThat(result.getListResourcesReserved(), hasSize(3));
      result = ResourcesManagerDAO.getReservation(con, instanceId, newReservationId);
      expectedResult.setId(newReservationId);
      expectedResult.setStatus(STATUS_FOR_VALIDATION);
      assertThat(result, is(expectedResult));
      assertThat(result.getListResourcesReserved(), is(notNullValue()));
      assertThat(result.getListResourcesReserved(), hasSize(4));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of saveIntoReservedResource method, of class ResourcesManagerDAO.
   */
  @Test
  public void testSaveIntoReservedResource() throws Exception {
    Connection con = getConnection();
    String reservationId = "3";
    String userId = "2";
    String instanceId = "resourcesManager42";
    try {
      Reservation result = ResourcesManagerDAO.getReservation(con, instanceId, reservationId);
      Reservation expectedResult = new Reservation(reservationId, "Test de la Toussaint",
              new Date(1320134400000L), new Date(1320163200000L), "To test", "at work", "2",
              new Date(1319811924467L), new Date(1319811924467L), instanceId, "test");
      assertThat(result, is(expectedResult));
      assertThat(result.getListResourcesReserved(), is(notNullValue()));
      assertThat(result.getListResourcesReserved(), hasSize(3));
      ResourcesManagerDAO.saveIntoReservedResource(con, Integer.parseInt(reservationId), "4", userId);
      con.commit();
      result = ResourcesManagerDAO.getReservation(con, instanceId, reservationId);
      assertThat(result, is(expectedResult));
      assertThat(result.getListResourcesReserved(), is(notNullValue()));
      assertThat(result.getListResourcesReserved(), hasSize(4));
    } finally {
      DBUtil.close(con);
    }
  }

  @Test(expected = NullPointerException.class)
  public void testSaveIntoReservedResourceNonExistingResource() throws Exception {
    Connection con = getConnection();
    String reservationId = "3";
    String userId = "2";
    String instanceId = "resourcesManager42";
    try {
      Reservation result = ResourcesManagerDAO.getReservation(con, instanceId, reservationId);
      Reservation expectedResult = new Reservation(reservationId, "Test de la Toussaint",
              new Date(1320134400000L), new Date(1320163200000L), "To test", "at work", "2",
              new Date(1319811924467L), new Date(1319811924467L), instanceId, "test");
      assertThat(result, is(expectedResult));
      assertThat(result.getListResourcesReserved(), is(notNullValue()));
      assertThat(result.getListResourcesReserved(), hasSize(3));
      ResourcesManagerDAO.saveIntoReservedResource(con, Integer.parseInt(reservationId), "6,4",
              userId);
      con.commit();
      result = ResourcesManagerDAO.getReservation(con, instanceId, reservationId);
      assertThat(result, is(expectedResult));
      assertThat(result.getListResourcesReserved(), is(notNullValue()));
      assertThat(result.getListResourcesReserved(), hasSize(4));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of getResourceStatus method, of class ResourcesManagerDAO.
   */
  @Test
  public void testGetResourceStatusWithoutManagers() throws Exception {
    Connection con = getConnection();
    String resourceId = "4";
    try {
      String result = ResourcesManagerDAO.getResourceStatus(con, resourceId, "5");
      assertThat(result, is(STATUS_VALIDATE));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of getResourceStatus method, of class ResourcesManagerDAO.
   */
  @Test
  public void testGetResourceStatusForManager() throws Exception {
    Connection con = getConnection();
    String resourceId = "3";
    try {
      String result = ResourcesManagerDAO.getResourceStatus(con, resourceId, "0");
      assertThat(result, is(STATUS_VALIDATE));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of getResourceStatus method, of class ResourcesManagerDAO.
   */
  @Test
  public void testGetResourceStatus() throws Exception {
    Connection con = getConnection();
    String resourceId = "2";
    try {
      String result = ResourcesManagerDAO.getResourceStatus(con, resourceId, "5");
      assertThat(result, is(STATUS_FOR_VALIDATION));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of insertIntoReservedResource method, of class ResourcesManagerDAO.
   */
  @Test
  public void testInsertIntoReservedResource() throws Exception {
    Connection con = getConnection();
    String reservationId = "3";
    String userId = "2";
    String instanceId = "resourcesManager42";
    try {
      Reservation result = ResourcesManagerDAO.getReservation(con, instanceId, reservationId);
      Reservation expectedResult = new Reservation(reservationId, "Test de la Toussaint",
              new Date(1320134400000L), new Date(1320163200000L), "To test", "at work", "2",
              new Date(1319811924467L), new Date(1319811924467L), instanceId, "test");
      assertThat(result, is(expectedResult));
      assertThat(result.getListResourcesReserved(), is(notNullValue()));
      assertThat(result.getListResourcesReserved(), hasSize(3));
      ResourcesManagerDAO.insertIntoReservedResource(con, Integer.parseInt(reservationId), "4",
              STATUS_VALIDATE);
      con.commit();
      result = ResourcesManagerDAO.getReservation(con, instanceId, reservationId);
      assertThat(result, is(expectedResult));
      assertThat(result.getListResourcesReserved(), is(notNullValue()));
      assertThat(result.getListResourcesReserved(), hasSize(4));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of idReservation method, of class ResourcesManagerDAO.
   */
  @Test
  public void testIdReservation() throws Exception {
    Connection con = getConnection();
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, 2011);
    calendar.set(Calendar.MONTH, Calendar.OCTOBER);
    calendar.set(Calendar.DAY_OF_MONTH, 10);
    Date beginDate = calendar.getTime();
    calendar.set(Calendar.MONTH, Calendar.NOVEMBER);
    Date endDate = calendar.getTime();
    try {
      int idResource = 1;
      int result = ResourcesManagerDAO.idReservation(con, idResource, beginDate, endDate);
      assertThat(result, is(notNullValue()));
      assertThat(result, is(3));
      idResource = 3;
      beginDate = endDate;
      calendar.set(Calendar.MONTH, Calendar.DECEMBER);
      calendar.set(Calendar.DAY_OF_MONTH, 25);
      endDate = calendar.getTime();
      result = ResourcesManagerDAO.idReservation(con, idResource, beginDate, endDate);
      assertThat(result, is(notNullValue()));
      assertThat(result, is(4));

      ResourcesManagerDAO.idReservation(con, idResource, beginDate, endDate);
      assertThat(result, is(notNullValue()));
      assertThat(result, is(4));
    } finally {
      DBUtil.close(con);
    }
  }

  @Test
  public void testIdReservationOutOfDate() throws Exception {
    Connection con = getConnection();
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, 2011);
    calendar.set(Calendar.MONTH, Calendar.NOVEMBER);
    calendar.set(Calendar.DAY_OF_MONTH, 10);
    Date beginDate = calendar.getTime();
    calendar.set(Calendar.MONTH, Calendar.DECEMBER);
    calendar.set(Calendar.DAY_OF_MONTH, 25);
    Date endDate = calendar.getTime();
    try {
      int idResource = 5;
      int result = ResourcesManagerDAO.idReservation(con, idResource, beginDate, endDate);
      assertThat(result, is(notNullValue()));
      assertThat(result, is(0));
      idResource = 3;
      calendar.set(Calendar.YEAR, 2011);
      calendar.set(Calendar.MONTH, Calendar.DECEMBER);
      calendar.set(Calendar.DAY_OF_MONTH, 25);
      beginDate = calendar.getTime();
      calendar.set(Calendar.MONTH, Calendar.DECEMBER);
      calendar.set(Calendar.DAY_OF_MONTH, 31);
      endDate = calendar.getTime();
      result = ResourcesManagerDAO.idReservation(con, idResource, beginDate, endDate);
      assertThat(result, is(notNullValue()));
      assertThat(result, is(0));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of getResourcesReservable method, of class ResourcesManagerDAO.
   */
  @Test
  public void testGetResourcesReservable() throws Exception {
    Connection con = getConnection();
    String instanceId = "resourcesManager42";
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, 2010);
    Date beginDate = calendar.getTime();
    calendar.add(Calendar.HOUR, 1);
    Date endDate = calendar.getTime();
    try {
      List<ResourceReservableDetail> result = ResourcesManagerDAO.getResourcesReservable(con,
              instanceId, beginDate, endDate);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(3));
      assertThat(result, contains(new ResourceReservableDetail("1", "1", "Salles",
              "Salle Chartreuse"), new ResourceReservableDetail("1", "2", "Salles",
              "Salle Belledonne"), new ResourceReservableDetail("2", "3", "Voitures",
              "Twingo verte - 156 VV 38")));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of getResourcesReservable method, of class ResourcesManagerDAO.
   */
  @Test
  public void testGetResourcesReservableWithProblem() throws Exception {
    Connection con = getConnection();
    String instanceId = "resourcesManager42";
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, 2011);
    calendar.set(Calendar.MONTH, Calendar.DECEMBER);
    calendar.set(Calendar.DAY_OF_MONTH, 20);
    calendar.set(Calendar.HOUR_OF_DAY, 8);
    calendar.set(Calendar.MINUTE, 15);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Date beginDate = calendar.getTime();
    calendar.set(Calendar.HOUR_OF_DAY, 11);
    Date endDate = calendar.getTime();
    try {
      List<ResourceReservableDetail> result = ResourcesManagerDAO.getResourcesReservable(con,
              instanceId, beginDate, endDate);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(3));
      assertThat(result, contains(new ResourceReservableDetail("1", "0", "Salles", ""),
              new ResourceReservableDetail("1", "0", "Salles", ""), new ResourceReservableDetail("2",
              "0", "Voitures", "")));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of verificationReservation method, of class ResourcesManagerDAO.
   */
  @Test
  public void testVerificationReservation() throws Exception {
    Connection con = getConnection();
    String instanceId = "resourcesManager42";
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, 2011);
    calendar.set(Calendar.MONTH, Calendar.NOVEMBER);
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    calendar.set(Calendar.HOUR_OF_DAY, 10);
    calendar.set(Calendar.MINUTE, 15);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Date beginDate = calendar.getTime();
    calendar.set(Calendar.HOUR_OF_DAY, 11);
    Date endDate = calendar.getTime();
    String listeResources = "1,2,6,4";
    try {
      List<ResourceDetail> result = ResourcesManagerDAO.verificationReservation(con,
          listeResources, beginDate, endDate);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(2));
      assertThat(result, contains(new ResourceDetail("1", "1", "Salle Chartreuse",
              new Date(1315232852398L), new Date(1315232852398L),
              "Salle de réunion jusqu'à 4 personnes", "5", "5", "5", "resourcesManager42", true),
              new ResourceDetail("2", "1", "Salle Belledonne",
              new Date(1315232852398L), new Date(1315232852398L),
              "Salle de réunion jusqu'à 12 personnes", "5", "5", "5", "resourcesManager42", true)));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of IdReservationProblem method, of class ResourcesManagerDAO.
   */
  @Test
  public void testIdReservationProblem() throws Exception {
    Connection con = getConnection();
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, 2011);
    calendar.set(Calendar.MONTH, Calendar.NOVEMBER);
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    calendar.set(Calendar.HOUR_OF_DAY, 10);
    calendar.set(Calendar.MINUTE, 15);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Date beginDate = calendar.getTime();
    calendar.set(Calendar.HOUR_OF_DAY, 11);
    Date endDate = calendar.getTime();
    int resourceId = 3;
    try {
      int result = ResourcesManagerDAO.IdReservationProblem(con, resourceId, beginDate, endDate);
      assertThat(result, is(3));
      resourceId = 1;
      result = ResourcesManagerDAO.IdReservationProblem(con, resourceId, beginDate, endDate);
      assertThat(result, is(3));
      resourceId = 5;
      result = ResourcesManagerDAO.IdReservationProblem(con, resourceId, beginDate, endDate);
      assertThat(result, is(0));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of verificationNewDateReservation method, of class ResourcesManagerDAO.
   */
  @Test
  public void testVerificationNewDateReservation() throws Exception {
    Connection con = getConnection();
    String instanceId = "resourcesManager42";
    String listeReservation = "1,2,6,4";
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, 2011);
    calendar.set(Calendar.MONTH, Calendar.DECEMBER);
    calendar.set(Calendar.DAY_OF_MONTH, 20);
    calendar.set(Calendar.HOUR_OF_DAY, 8);
    calendar.set(Calendar.MINUTE, 15);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Date startDate = calendar.getTime();
    calendar.set(Calendar.HOUR_OF_DAY, 11);
    Date endDate = calendar.getTime();
    String reservationId = "3";
    try {
      List<ResourceDetail> result = ResourcesManagerDAO.verificationNewDateReservation(con,
          listeReservation, startDate, endDate, reservationId);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(2));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of IdReservationDateProblem method, of class ResourcesManagerDAO.
   */
  @Test
  public void testIdReservationDateProblem() throws Exception {
    Connection con = getConnection();
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, 2011);
    calendar.set(Calendar.MONTH, Calendar.DECEMBER);
    calendar.set(Calendar.DAY_OF_MONTH, 20);
    calendar.set(Calendar.HOUR_OF_DAY, 8);
    calendar.set(Calendar.MINUTE, 15);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Date beginDate = calendar.getTime();
    calendar.set(Calendar.HOUR_OF_DAY, 10);
    Date endDate = calendar.getTime();
    String reservationId = "4";
    try {
      int resourceId = 2;
      int result = ResourcesManagerDAO.IdReservationDateProblem(con, resourceId, beginDate, endDate,
              reservationId);
      assertThat(result, is(0));
      resourceId = 1;
      result = ResourcesManagerDAO.IdReservationDateProblem(con, resourceId, beginDate, endDate,
              reservationId);
      assertThat(result, is(5));
      resourceId = 3;
      result = ResourcesManagerDAO.IdReservationDateProblem(con, resourceId, beginDate, endDate,
              reservationId);
      assertThat(result, is(0));
      reservationId = "3";
      result = ResourcesManagerDAO.IdReservationDateProblem(con, resourceId, beginDate, endDate,
              reservationId);
      assertThat(result, is(4));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of getReservationUser method, of class ResourcesManagerDAO.
   */
  @Test
  public void testGetReservationUser() throws Exception {
    Connection con = getConnection();
    String instanceId = "resourcesManager42";
    try {
      String userid = "9";
      List<Reservation> result = ResourcesManagerDAO.getReservationUser(con, instanceId,
              userid);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(1));
      assertThat(result, contains(new Reservation("4", "Test réservation 20/12/2011",
              new Date(1324368000000L), new Date(1324375200000L), "To test a reservzation",
              "at work", "9", new Date(1320225012008L), new Date(1320225012008L), instanceId, "A")));
      userid = "2";
      result = ResourcesManagerDAO.getReservationUser(con, instanceId,
              userid);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(3));
      assertThat(result, contains(new Reservation("3", "Test de la Toussaint",
              new Date(1320134400000L), new Date(1320163200000L), "To test", "at work", "2",
              new Date(1319811924467L), new Date(1319811924467L), instanceId, "test"),
              new Reservation("5", "Test réservation validée 20/12/2011",
              new Date(1324368000000L), new Date(1324375200000L), "To test a reservzation validated",
              "at work", "2", new Date(1319811924467L), new Date(1319811924467L), instanceId, "V"),
              new Reservation("6", "Test réservation refusée 20/12/2011",
              new Date(1324375200000L), new Date(1324382400000L), "To test a reservzation refused",
              "at work", "2", new Date(1319811924467L), new Date(1319811924467L), instanceId, "R")));

    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of getReservations method, of class ResourcesManagerDAO.
   */
  @Test
  public void testGetReservations() throws Exception {
    Connection con = getConnection();
    String instanceId = "resourcesManager42";
    try {
      List<Reservation> result = ResourcesManagerDAO.getReservations(con, instanceId);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(4));
      assertThat(result, contains(new Reservation("3", "Test de la Toussaint",
              new Date(1320134400000L), new Date(1320163200000L), "To test", "at work", "2",
              new Date(1319811924467L), new Date(1319811924467L), instanceId, "test"),
              new Reservation("4", "Test réservation 20/12/2011",
              new Date(1324368000000L), new Date(1324375200000L), "To test a reservzation",
              "at work", "9", new Date(1320225012008L), new Date(1320225012008L), instanceId, "A"),
              new Reservation("5", "Test réservation validée 20/12/2011",
              new Date(1324368000000L), new Date(1324375200000L), "To test a reservzation validated",
              "at work", "2", new Date(1319811924467L), new Date(1319811924467L), instanceId, "V"),
              new Reservation("6", "Test réservation refusée 20/12/2011",
              new Date(1324375200000L), new Date(1324382400000L), "To test a reservzation refused",
              "at work", "2", new Date(1319811924467L), new Date(1319811924467L), instanceId, "R")));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of getReservation method, of class ResourcesManagerDAO.
   */
  @Test
  public void testGetReservation() throws Exception {
    Connection con = getConnection();
    String reservationId = "3";
    String instanceId = "resourcesManager42";
    try {
      Reservation result = ResourcesManagerDAO.getReservation(con, instanceId, reservationId);
      Reservation expectedResult = new Reservation(reservationId, "Test de la Toussaint",
              new Date(1320134400000L), new Date(1320163200000L), "To test", "at work", "2",
              new Date(1319811924467L), new Date(1319811924467L), instanceId, "test");
      assertThat(result, is(expectedResult));
      assertThat(result.getListResourcesReserved(), is(notNullValue()));
      assertThat(result.getListResourcesReserved(), hasSize(3));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of getMonthReservation method, of class ResourcesManagerDAO.
   */
  @Test
  public void testGetMonthReservation() throws Exception {
    Connection con = getConnection();
    String userId = "2";
    String instanceId = "resourcesManager42";
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.MONTH, Calendar.NOVEMBER);
    calend.set(Calendar.YEAR, 2011);
    try {
      List<Reservation> result = ResourcesManagerDAO.getMonthReservation(con, instanceId,
              calend.getTime(), userId);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(1));
      assertThat(result, contains(new Reservation("3", "Test de la Toussaint",
              new Date(1320134400000L), new Date(1320163200000L), "To test", "at work", "2",
              new Date(1319811924467L), new Date(1319811924467L), instanceId, "test")));
      assertThat(result.get(0).getListResourcesReserved(), is(notNullValue()));
      assertThat(result.get(0).getListResourcesReserved(), hasSize(3));
      userId = "3";
      result = ResourcesManagerDAO.getMonthReservation(con, instanceId, calend.getTime(), userId);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(0));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of getReservationForValidation method, of class ResourcesManagerDAO.
   */
  @Test
  public void testGetReservationForValidation() throws Exception {
    Connection con = getConnection();
    String userId = "9";
    String instanceId = "resourcesManager42";
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.MONTH, Calendar.DECEMBER);
    calend.set(Calendar.YEAR, 2011);
    try {
      List<Reservation> result = ResourcesManagerDAO.getReservationUser(con, instanceId,
              userId);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(1));
      result = ResourcesManagerDAO.getReservationForValidation(con, instanceId, calend.getTime(),
              userId);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(1));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of getMonthReservationOfCategory method, of class ResourcesManagerDAO.
   */
  @Test
  public void testGetMonthReservationOfCategory() throws Exception {
    Connection con = getConnection();
    String userId = "2";
    String instanceId = "resourcesManager42";
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.MONTH, Calendar.NOVEMBER);
    calend.set(Calendar.YEAR, 2011);
    String categoryId = "2";
    try {
      List<Reservation> result = ResourcesManagerDAO.getMonthReservationOfCategory(con,
              instanceId,
              calend.getTime(), categoryId);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(1));
      assertThat(result, contains(new Reservation("3", "Test de la Toussaint",
              new Date(1320134400000L), new Date(1320163200000L), "To test", "at work", "2",
              new Date(1319811924467L), new Date(1319811924467L), instanceId, "test")));
      assertThat(result.get(0).getListResourcesReserved(), is(notNullValue()));
      assertThat(result.get(0).getListResourcesReserved(), hasSize(3));
      categoryId = "5";
      result = ResourcesManagerDAO.getMonthReservationOfCategory(con, instanceId,
              calend.getTime(), categoryId);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(0));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of deleteReservation method, of class ResourcesManagerDAO.
   */
  @Test
  public void testDeleteReservation() throws Exception {
    Connection con = getConnection();
    String reservationId = "3";
    String instanceId = "resourcesManager42";
    try {
      Reservation result = ResourcesManagerDAO.getReservation(con, instanceId, reservationId);
      Reservation expectedResult = new Reservation(reservationId, "Test de la Toussaint",
              new Date(1320134400000L), new Date(1320163200000L), "To test", "at work", "2",
              new Date(1319811924467L), new Date(1319811924467L), instanceId, "test");
      assertThat(result, is(expectedResult));
      assertThat(result.getListResourcesReserved(), is(notNullValue()));
      assertThat(result.getListResourcesReserved(), hasSize(3));
      ResourcesManagerDAO.deleteReservation(con, reservationId);
      con.commit();
      result = ResourcesManagerDAO.getReservation(con, instanceId, reservationId);
      assertThat(result, is(nullValue()));
      ResourceDao dao = new ResourceDao();
      List<ResourceDetail> resources = dao.getResourcesofReservation(con, instanceId, reservationId);
      assertThat(resources, is(notNullValue()));
      assertThat(resources, hasSize(0));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of updateReservation method, of class ResourcesManagerDAO.
   */
  @Test
  public void testUpdateReservationWhithoutUpdatingdate() throws Exception {
    Connection con = getConnection();
    String reservationId = "3";
    String instanceId = "resourcesManager42";
    try {
      Reservation result = ResourcesManagerDAO.getReservation(con, instanceId, reservationId);
      assertThat(result, is(new Reservation(reservationId, "Test de la Toussaint",
              new Date(1320134400000L), new Date(1320163200000L), "To test", "at work", "2",
              new Date(1319811924467L), new Date(1319811924467L), instanceId, "test")));
      assertThat(result.getListResourcesReserved(), is(notNullValue()));
      assertThat(result.getListResourcesReserved(), hasSize(3));
      Calendar calend = Calendar.getInstance();
      calend.set(Calendar.MONTH, Calendar.NOVEMBER);
      calend.set(Calendar.YEAR, 2011);
      calend.set(Calendar.DAY_OF_MONTH, 5);
      calend.set(Calendar.HOUR_OF_DAY, 14);
      calend.set(Calendar.MINUTE, 30);
      calend.set(Calendar.SECOND, 0);
      calend.set(Calendar.SECOND, 0);
      calend.set(Calendar.MILLISECOND, 0);
      result.setBeginDate(calend.getTime());
      calend.set(Calendar.HOUR_OF_DAY, 18);
      calend.set(Calendar.MINUTE, 15);
      calend.set(Calendar.SECOND, 0);
      calend.set(Calendar.SECOND, 0);
      calend.set(Calendar.MILLISECOND, 0);
      result.setEndDate(calend.getTime());
      result.setEvent("Testing an update of my reservation");
      result.setPlace("At work from home");
      result.setReason("Testing is cool");
      ResourcesManagerDAO.updateReservation(con, "1,2,3", result, false);
      con.commit();
      Reservation updatedResult = ResourcesManagerDAO.getReservation(con, instanceId,
              reservationId);
      assertThat(updatedResult, is(notNullValue()));
      result.setUpdateDate(updatedResult.getUpdateDate());
      result.setStatus(STATUS_REFUSED);
      assertThat(updatedResult, is(result));
      assertThat(updatedResult.getListResourcesReserved(), is(notNullValue()));
      assertThat(updatedResult.getListResourcesReserved(), hasSize(3));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of updateReservation method, of class ResourcesManagerDAO.
   */
  @Test
  public void testUpdateReservationWhithUpdatingdate() throws Exception {
    Connection con = getConnection();
    String reservationId = "3";
    String instanceId = "resourcesManager42";
    try {
      Reservation result = ResourcesManagerDAO.getReservation(con, instanceId, reservationId);
      assertThat(result, is(new Reservation(reservationId, "Test de la Toussaint",
              new Date(1320134400000L), new Date(1320163200000L), "To test", "at work", "2",
              new Date(1319811924467L), new Date(1319811924467L), instanceId, "test")));
      assertThat(result.getListResourcesReserved(), is(notNullValue()));
      assertThat(result.getListResourcesReserved(), hasSize(3));
      Calendar calend = Calendar.getInstance();
      calend.set(Calendar.MONTH, Calendar.NOVEMBER);
      calend.set(Calendar.YEAR, 2011);
      calend.set(Calendar.DAY_OF_MONTH, 5);
      calend.set(Calendar.HOUR_OF_DAY, 14);
      calend.set(Calendar.MINUTE, 30);
      calend.set(Calendar.SECOND, 0);
      calend.set(Calendar.SECOND, 0);
      calend.set(Calendar.MILLISECOND, 0);
      result.setBeginDate(calend.getTime());
      calend.set(Calendar.HOUR_OF_DAY, 18);
      calend.set(Calendar.MINUTE, 15);
      calend.set(Calendar.SECOND, 0);
      calend.set(Calendar.SECOND, 0);
      calend.set(Calendar.MILLISECOND, 0);
      result.setEndDate(calend.getTime());
      result.setEvent("Testing an update of my reservation");
      result.setPlace("At work from home");
      result.setReason("Testing is cool");
      ResourcesManagerDAO.updateReservation(con, "1,2,3", result, true);
      con.commit();
      Reservation updatedResult = ResourcesManagerDAO.getReservation(con, instanceId,
              reservationId);
      assertThat(updatedResult, is(notNullValue()));
      result.setUpdateDate(updatedResult.getUpdateDate());
      result.setStatus(STATUS_FOR_VALIDATION);
      assertThat(updatedResult, is(result));
      assertThat(updatedResult.getListResourcesReserved(), is(notNullValue()));
      assertThat(updatedResult.getListResourcesReserved(), hasSize(3));
      assertThat(updatedResult.getListResourcesReserved(), contains(
              result.getListResourcesReserved().toArray(new ResourceDetail[3])));
      Collection<String> updatedResources = Collections2.transform(updatedResult.
              getListResourcesReserved(), new ToStringFunction<ResourceDetail>());
      for (ResourceDetail reservedResource : result.getListResourcesReserved()) {
        if (!STATUS_VALIDATE.equals(reservedResource.getStatus()))  {
          reservedResource.setStatus(STATUS_FOR_VALIDATION);
        }
      }
      Collection<String> expectedResources = Collections2.transform(
              result.getListResourcesReserved(), new ToStringFunction<ResourceDetail>());
      assertThat(updatedResources, containsInAnyOrder(expectedResources.toArray(new String[3])));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of updateReservation method, of class ResourcesManagerDAO.
   */
  @Test
  public void testUpdateReservationDetail() throws Exception {
    Connection con = getConnection();
    String reservationId = "3";
    String instanceId = "resourcesManager42";
    try {
      Reservation result = ResourcesManagerDAO.getReservation(con, instanceId, reservationId);
      assertThat(result, is(new Reservation(reservationId, "Test de la Toussaint",
              new Date(1320134400000L), new Date(1320163200000L), "To test", "at work", "2",
              new Date(1319811924467L), new Date(1319811924467L), instanceId, "test")));
      assertThat(result.getListResourcesReserved(), is(notNullValue()));
      assertThat(result.getListResourcesReserved(), hasSize(3));
      Calendar calend = Calendar.getInstance();
      calend.set(Calendar.MONTH, Calendar.NOVEMBER);
      calend.set(Calendar.YEAR, 2011);
      calend.set(Calendar.DAY_OF_MONTH, 5);
      calend.set(Calendar.HOUR_OF_DAY, 14);
      calend.set(Calendar.MINUTE, 30);
      calend.set(Calendar.SECOND, 0);
      calend.set(Calendar.SECOND, 0);
      calend.set(Calendar.MILLISECOND, 0);
      result.setBeginDate(calend.getTime());
      calend.set(Calendar.HOUR_OF_DAY, 18);
      calend.set(Calendar.MINUTE, 15);
      calend.set(Calendar.SECOND, 0);
      calend.set(Calendar.SECOND, 0);
      calend.set(Calendar.MILLISECOND, 0);
      result.setEndDate(calend.getTime());
      result.setEvent("Testing an update of my reservation");
      result.setPlace("At work from home");
      result.setReason("Testing is cool");
      ResourcesManagerDAO.updateReservation(con, result);
      con.commit();
      Reservation updatedResult = ResourcesManagerDAO.getReservation(con, instanceId,
              reservationId);
      assertThat(updatedResult, is(notNullValue()));
      result.setUpdateDate(updatedResult.getUpdateDate());
      result.setStatus(STATUS_REFUSED);
      assertThat(updatedResult, is(result));
      assertThat(updatedResult.getListResourcesReserved(), is(notNullValue()));
      assertThat(updatedResult.getListResourcesReserved(), hasSize(3));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of compute the status of a reservation from the status of its resources
   */
  @Test
  public void testComputeValidatedReservationStatus() {
    List<ResourceDetail> resources = new ArrayList<ResourceDetail>(3);
    ResourceDetail resource = new ResourceDetail("Test", "1", true);
    resource.setStatus(STATUS_VALIDATE);
    resources.add(resource);
    resource = new ResourceDetail("Test", "2", true);
    resource.setStatus(STATUS_VALIDATE);
    resources.add(resource);
    resource = new ResourceDetail("Test", "1", false);
    resource.setStatus(STATUS_VALIDATE);
    resources.add(resource);
    assertThat(ResourcesManagerDAO.computeReservationStatus(resources), is(
            STATUS_VALIDATE));
  }

  /**
   * Test of compute the status of a reservation from the status of its resources
   */
  @Test
  public void testComputeRefusedReservationStatus() {
    List<ResourceDetail> resources = new ArrayList<ResourceDetail>(3);
    ResourceDetail resource = new ResourceDetail("Test", "1", true);
    resource.setStatus(STATUS_VALIDATE);
    resources.add(resource);
    resource = new ResourceDetail("Test", "2", true);
    resource.setStatus(STATUS_FOR_VALIDATION);
    resources.add(resource);
    resource = new ResourceDetail("Test", "1", false);
    resource.setStatus(STATUS_REFUSED);
    resources.add(resource);
    assertThat(ResourcesManagerDAO.computeReservationStatus(resources), is(
            STATUS_REFUSED));
  }

  /**
   * Test of compute the status of a reservation from the status of its resources
   */
  @Test
  public void testComputeToValidateReservationStatus() {
    List<ResourceDetail> resources = new ArrayList<ResourceDetail>(3);
    ResourceDetail resource = new ResourceDetail("Test", "1", true);
    resource.setStatus(STATUS_VALIDATE);
    resources.add(resource);
    resource = new ResourceDetail("Test", "2", true);
    resource.setStatus(STATUS_FOR_VALIDATION);
    resources.add(resource);
    resource = new ResourceDetail("Test", "1", false);
    resource.setStatus(STATUS_VALIDATE);
    resources.add(resource);
    assertThat(ResourcesManagerDAO.computeReservationStatus(resources), is(
            STATUS_FOR_VALIDATION));
  }

  /**
   * Test of updateIntoReservation method, of class ResourcesManagerDAO.
   */
  @Test
  public void testUpdateIntoReservation() throws Exception {
    Connection con = getConnection();
    String reservationId = "3";
    String instanceId = "resourcesManager42";
    try {
      Reservation result = ResourcesManagerDAO.getReservation(con, instanceId, reservationId);
      assertThat(result, is(new Reservation(reservationId, "Test de la Toussaint",
              new Date(1320134400000L), new Date(1320163200000L), "To test", "at work", "2",
              new Date(1319811924467L), new Date(1319811924467L), instanceId, "test")));
      assertThat(result.getListResourcesReserved(), is(notNullValue()));
      assertThat(result.getListResourcesReserved(), hasSize(3));
      Calendar calend = Calendar.getInstance();
      calend.set(Calendar.MONTH, Calendar.NOVEMBER);
      calend.set(Calendar.YEAR, 2011);
      calend.set(Calendar.DAY_OF_MONTH, 5);
      calend.set(Calendar.HOUR_OF_DAY, 14);
      calend.set(Calendar.MINUTE, 30);
      calend.set(Calendar.SECOND, 0);
      calend.set(Calendar.SECOND, 0);
      calend.set(Calendar.MILLISECOND, 0);
      result.setBeginDate(calend.getTime());
      calend.set(Calendar.HOUR_OF_DAY, 18);
      calend.set(Calendar.MINUTE, 15);
      calend.set(Calendar.SECOND, 0);
      calend.set(Calendar.SECOND, 0);
      calend.set(Calendar.MILLISECOND, 0);
      result.setEndDate(calend.getTime());
      result.setEvent("Testing an update of my reservation");
      result.setPlace("At work from home");
      result.setReason("Testing is cool");
      ResourcesManagerDAO.updateIntoReservation(con, result);
      con.commit();
      Reservation updatedResult = ResourcesManagerDAO.getReservation(con, instanceId,
              reservationId);
      assertThat(updatedResult, is(notNullValue()));
      result.setUpdateDate(updatedResult.getUpdateDate());
      assertThat(updatedResult, is(result));
      assertThat(updatedResult.getListResourcesReserved(), is(notNullValue()));
      assertThat(updatedResult.getListResourcesReserved(), hasSize(3));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of updateResourceStatus method, of class ResourcesManagerDAO.
   */
  @Test
  public void testUpdateResourceStatus() throws Exception {
    Connection con = getConnection();
    int reservationId = 3;
    try {
      int resourceId = 2;
      String result = ResourcesManagerDAO.getStatusResourceOfReservation(con, resourceId,
              reservationId);
      assertThat(result, is(STATUS_REFUSED));
      ResourcesManagerDAO.updateResourceStatus(con, STATUS_VALIDATE, resourceId,
              reservationId);
      con.commit();
      result = ResourcesManagerDAO.getStatusResourceOfReservation(con, resourceId,
              reservationId);
      assertThat(result, is(STATUS_VALIDATE));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of getStatusResourceOfReservation method, of class ResourcesManagerDAO.
   */
  @Test
  public void testGetStatusResourceOfReservation() throws Exception {
    Connection con = getConnection();
    int reservationId = 3;
    try {
      int resourceId = 0;
      String result = ResourcesManagerDAO.getStatusResourceOfReservation(con, resourceId,
              reservationId);
      assertThat(result, is(nullValue()));
      resourceId = 1;
      result = ResourcesManagerDAO.getStatusResourceOfReservation(con, resourceId,
              reservationId);
      assertThat(result, is(STATUS_VALIDATE));
      resourceId = 2;
      result = ResourcesManagerDAO.getStatusResourceOfReservation(con, resourceId,
              reservationId);
      assertThat(result, is(STATUS_REFUSED));
      resourceId = 3;
      result = ResourcesManagerDAO.getStatusResourceOfReservation(con, resourceId,
              reservationId);
      assertThat(result, is("test"));
    } finally {
      DBUtil.close(con);
    }
  }
}
