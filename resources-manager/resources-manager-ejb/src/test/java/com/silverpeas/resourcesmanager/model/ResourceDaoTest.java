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

import com.google.common.collect.Lists;
import com.stratelia.webactiv.util.DBUtil;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
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
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author ehugonnet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-resource-manager-embbed-datasource.xml"})
public class ResourceDaoTest {

  private final ResourceDao dao = new ResourceDao();
  @Inject
  private DataSource dataSource;

  public ResourceDaoTest() {
  }

  public Connection getConnection() throws SQLException {
    return this.dataSource.getConnection();
  }

  @Before
  public void generalSetUp() throws Exception {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSet(
            ResourceDaoTest.class.getClassLoader().getResourceAsStream(
            "com/silverpeas/resourcesmanager/model/resources_dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
    DBUtil.getInstanceForTest(dataSource.getConnection());
  }

  /**
   * Test of createResource method, of class ResourcesManagerDAO.
   */
  @Test
  public void testCreateResource() throws SQLException {
    Connection con = getConnection();
    String id = "21";
    try {
      ResourceDetail resource = new ResourceDetail(null, "1", "Salle Vercors",
              new Date(1315232852398L), new Date(1315232852398L),
              "Salle de réunion jusqu'à 4 personnes avec vidéoprojecteur", "5", "5", "5",
              "resourcesManager42", true);
      String result = dao.createResource(con, resource);
      assertThat(result, is(id));
      resource.setId(id);
      ResourceDetail savedResource = dao.getResource(con, id);
      assertThat(savedResource, is(resource));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of updateResource method, of class ResourcesManagerDAO.
   */
  @Test
  public void testUpdateResource() throws SQLException {
    Connection con = getConnection();
    try {
      String id = "1";
      ResourceDetail resource = new ResourceDetail(id, "1", "Salle Chartreuse",
              new Date(1315232852398L), new Date(1315232852398L),
              "Salle de réunion jusqu'à 4 personnes", "5", "5", "5", "resourcesManager42", true);
      ResourceDetail result = dao.getResource(con, id);
      assertThat(result, is(resource));
      resource.setBookable(false);
      resource.setName("Salle Vercors");
      resource.setDescription("Salle de réunion jusqu'à 4 personnes avec vidéoprojecteur");
      Date now = new Date();
      resource.setUpdateDate(now);
      dao.updateResource(con, resource);
      con.commit();
      result = dao.getResource(con, id);
      assertThat(result, is(resource));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of getResourcesByCategory method, of class ResourcesManagerDAO.
   */
  @Test
  public void testGetResourcesByCategory() throws SQLException {
    Connection con = getConnection();
    try {
      String categoryId = "1";
      List<ResourceDetail> result = dao.getResourcesByCategory(con, categoryId);
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
   * Test of getResource method, of class ResourcesManagerDAO.
   */
  @Test
  public void testGetResource() throws SQLException {
    Connection con = getConnection();
    try {
      String id = "1";
      ResourceDetail expResult = new ResourceDetail(id, "1", "Salle Chartreuse",
              new Date(1315232852398L), new Date(1315232852398L),
              "Salle de réunion jusqu'à 4 personnes", "5", "5", "5", "resourcesManager42", true);
      ResourceDetail result = dao.getResource(con, id);
      assertThat(result, is(expResult));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of deleteResource method, of class ResourcesManagerDAO.
   */
  @Test
  public void testDeleteResource() throws SQLException {
    Connection con = getConnection();
    try {
      String id = "1";
      ResourceDetail expResult = new ResourceDetail(id, "1", "Salle Chartreuse",
              new Date(1315232852398L), new Date(1315232852398L),
              "Salle de réunion jusqu'à 4 personnes", "5", "5", "5", "resourcesManager42", true);
      ResourceDetail result = dao.getResource(con, id);
      assertThat(result, is(expResult));
      dao.deleteResource(con, id);
      con.commit();
      result = dao.getResource(con, id);
      assertThat(result, is(nullValue()));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of deleteResourceFromCategory method, of class ResourcesManagerDAO.
   */
  @Test
  public void testDeleteResourceFromCategory() throws SQLException {
    Connection con = getConnection();
    try {
      String categoryId = "1";
      List<ResourceDetail> result = dao.getResourcesByCategory(con, categoryId);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(2));
      assertThat(result, contains(new ResourceDetail("1", "1", "Salle Chartreuse",
              new Date(1315232852398L), new Date(1315232852398L),
              "Salle de réunion jusqu'à 4 personnes", "5", "5", "5", "resourcesManager42", true),
              new ResourceDetail("2", "1", "Salle Belledonne",
              new Date(1315232852398L), new Date(1315232852398L),
              "Salle de réunion jusqu'à 12 personnes", "5", "5", "5", "resourcesManager42", true)));
      dao.deleteResourceFromCategory(con, categoryId);
      con.commit();
      result = dao.getResourcesByCategory(con, categoryId);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(0));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of getResourcesofReservation method, of class ResourcesManagerDAO.
   */
  @Test
  public void testGetResourcesofReservation() throws SQLException {
    Connection con = getConnection();
    String reservationId = "3";
    String instanceId = "resourcesManager42";
    try {
      List<ResourceDetail> result = dao.getResourcesofReservation(con,instanceId, reservationId);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(3));
       assertThat(result, contains(new ResourceDetail("1", "1", "Salle Chartreuse",
              new Date(1315232852398L), new Date(1315232852398L),
              "Salle de réunion jusqu'à 4 personnes", "5", "5", "5", "resourcesManager42", true),
              new ResourceDetail("2", "1", "Salle Belledonne",
              new Date(1315232852398L), new Date(1315232852398L),
              "Salle de réunion jusqu'à 12 personnes", "5", "5", "5", "resourcesManager42", true),
              new ResourceDetail("3", "2", "Twingo verte - 156 VV 38",
              new Date(1315232852398L), new Date(1315232852398L),
              "Twingo verte 4 places 5 portes", "5", "5", "5", "resourcesManager42", true)));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of addManagers method, of class ResourcesManagerDAO.
   */
  @Test
  public void testAddManagers() throws SQLException {
    Connection con = getConnection();
    int id = 3;
    try {
      List<String> result = dao.getManagers(con, id);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(1));
      assertThat(result, containsInAnyOrder("0"));
      dao.addManagers(con, id, Lists.newArrayList("1", "5", "10"));
      result = dao.getManagers(con, id);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(4));
      assertThat(result, containsInAnyOrder("0", "1", "5", "10"));
      con.commit();
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of addManager method, of class ResourcesManagerDAO.
   */
  @Test
  public void testAddManager() throws SQLException {
    Connection con = getConnection();
    int id = 3;
    try {
      List<String> result = dao.getManagers(con, id);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(1));
      assertThat(result, containsInAnyOrder("0"));
      dao.addManager(con, id, 6);
      result = dao.getManagers(con, id);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(2));
      assertThat(result, containsInAnyOrder("0", "6"));
      con.commit();
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of removeAllManagers method, of class ResourcesManagerDAO.
   */
  @Test
  public void testRemoveAllManagers() throws SQLException {
    Connection con = getConnection();
    int id = 1;
    try {
      List<String> result = dao.getManagers(con, id);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(3));
      assertThat(result, containsInAnyOrder("0", "1", "2"));
      dao.removeAllManagers(con, id);
      result = dao.getManagers(con, id);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(0));
      con.commit();
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of removeManager method, of class ResourcesManagerDAO.
   */
  @Test
  public void testRemoveManager() throws SQLException {
    Connection con = getConnection();
    int id = 1;
    try {
      List<String> result = dao.getManagers(con, id);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(3));
      assertThat(result, containsInAnyOrder("0", "1", "2"));
      dao.removeManager(con, id, 2);
      result = dao.getManagers(con, id);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(2));
      assertThat(result, containsInAnyOrder("0", "1"));
      con.commit();
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of getManagers method, of class ResourcesManagerDAO.
   */
  @Test
  public void testGetManagers() throws SQLException {
    Connection con = getConnection();
    int id = 1;
    try {
      List<String> result = dao.getManagers(con, id);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(3));
      assertThat(result, containsInAnyOrder("0", "1", "2"));
    } finally {
      DBUtil.close(con);
    }
  }
}
