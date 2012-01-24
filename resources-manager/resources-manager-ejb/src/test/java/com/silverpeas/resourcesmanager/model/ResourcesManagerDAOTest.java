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

import com.stratelia.webactiv.util.DBUtil;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.BeforeClass;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static com.silverpeas.resourcesmanager.model.ResourceStatus.*;

/**
 * @author ehugonnet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-resource-manager-embbed-datasource.xml"})
public class ResourcesManagerDAOTest {

  @Inject
  private DataSource dataSource;

  private static ReplacementDataSet dataSet;
  
  public ResourcesManagerDAOTest() {
  }

  public Connection getConnection() throws SQLException {
    return this.dataSource.getConnection();
  }
  
  @BeforeClass
  public static void prepareDataSet() throws Exception {
    FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
    dataSet = new ReplacementDataSet(builder.build(
            ResourcesManagerDAOTest.class.getClassLoader().getResourceAsStream(
            "com/silverpeas/resourcesmanager/model/reservations_dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
  }

  @Before
  public void generalSetUp() throws Exception {
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
    DBUtil.getInstanceForTest(dataSource.getConnection());
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
      assertThat(result, is(ResourceStatus.STATUS_FOR_VALIDATION));
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
      int idResource = 1;
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
      assertThat(result, hasSize(2));
      assertThat(result, contains(new ResourceReservableDetail("1", "1", "Salles",
              "Salle Chartreuse"), new ResourceReservableDetail("2", "3", "Voitures",
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
      assertThat(result, hasSize(2));
      assertThat(result, contains(new ResourceReservableDetail("1", "1", "Salles",
              "Salle Chartreuse"), new ResourceReservableDetail("2", "0", "Voitures", "")));
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
    calendar.set(Calendar.HOUR_OF_DAY, 11);
    Date endDate = calendar.getTime();
    String reservationId = "4";
    try {
      int resourceId = 1;
      int result = ResourcesManagerDAO.IdReservationDateProblem(con, resourceId, beginDate, endDate,
              reservationId);
      assertThat(result, is(0));
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
      assertThat(result, hasSize(1));
      assertThat(result, contains(new Reservation("3", "Test de la Toussaint",
              new Date(1320134400000L), new Date(1320163200000L), "To test", "at work", "2",
              new Date(1319811924467L), new Date(1319811924467L), instanceId, "test")));

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
      assertThat(result, hasSize(2));
      assertThat(result, contains(new Reservation("3", "Test de la Toussaint",
              new Date(1320134400000L), new Date(1320163200000L), "To test", "at work", "2",
              new Date(1319811924467L), new Date(1319811924467L), instanceId, "test"),
              new Reservation("4", "Test réservation 20/12/2011",
              new Date(1324368000000L), new Date(1324375200000L), "To test a reservzation",
              "at work", "9", new Date(1320225012008L), new Date(1320225012008L), instanceId, "A")));
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
