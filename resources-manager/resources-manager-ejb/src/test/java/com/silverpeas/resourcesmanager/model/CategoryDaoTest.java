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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-resource-manager-embbed-datasource.xml"})
public class CategoryDaoTest {

  private static ReplacementDataSet dataSet;
  
  @BeforeClass
  public static void prepareDataSet() throws Exception {
    FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
    dataSet = new ReplacementDataSet(builder.build(
            ResourcesManagerDAOTest.class.getClassLoader().getResourceAsStream(
            "com/silverpeas/resourcesmanager/model/categories_dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
  }
  
  private CategoryDao dao = new CategoryDao();
  
  @Inject
  private DataSource dataSource;

  public Connection getConnection() throws SQLException {
    return this.dataSource.getConnection();
  }
  
  

  @Before
  public void generalSetUp() throws Exception {    
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
    DBUtil.getInstanceForTest(dataSource.getConnection());
  }

  /**
   * Test of createCategory method, of class ResourcesManagerDAO.
   */
  @Test
  public void testCreateCategory() throws Exception {
    Connection con = getConnection();
    CategoryDetail category = new CategoryDetail("Vidéoprojecteurs ", true, "my_form.xml", "2",
            "Vidéoprojecteurs pour les salles de réunion");
    Date now = new Date();
    category.setCreationDate(now);
    category.setUpdateDate(now);
    category.setInstanceId("resourcesManager42");
     category.setCreaterId("10");
     category.setUpdaterId("10");
    try {
      int result = dao.createCategory(con, category);
      assertThat(result, is(6));
      category.setId("6");
      CategoryDetail savedCategory = dao.getCategory(con, "6");
      assertThat(savedCategory, is(category));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of updateCategory method, of class ResourcesManagerDAO.
   */
  @Test
  public void testUpdateCategory() throws Exception {
    Connection con = getConnection();
    String id = "1";
    try {
      CategoryDetail expResult = new CategoryDetail(id, "resourcesManager42", "Salles", new Date(
              1315232752398L), new Date(1315232752398L), true, "model1.xml", "5", "5", "5",
              "Salles de réunion");
      CategoryDetail result = dao.getCategory(con, id);
      assertThat(result, is(expResult));
      Date now = new Date();
      result.setUpdateDate(now);
      result.setName("Véhicules");
      result.setForm("car_form.xml");
      result.setResponsibleId("12");
      result.setUpdaterId("1");
      result.setDescription("This is a test");
      result.setBookable(false);
      dao.updateCategory(con, result);
      con.commit();
      expResult = new CategoryDetail(id, "resourcesManager42", "Véhicules", new Date(
              1315232752398L), now, false, "car_form.xml", "12", "5", "1", "This is a test");
      result = dao.getCategory(con, id);
      assertThat(result, is(new CategoryDetailMatcher(expResult)));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of getCategories method, of class ResourcesManagerDAO.
   */
  @Test
  public void testGetCategories() throws Exception {
    Connection con = getConnection();
    String instanceId = "resourcesManager42";
    try {
      List<CategoryDetail> result = dao.getCategories(con, instanceId);
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(2));
      assertThat(result, contains(new CategoryDetail("1", "resourcesManager42", "Salles", new Date(
              1315232752398L), new Date(1315232752398L), true, "model1.xml", "5", "5", "5",
              "Salles de réunion"), new CategoryDetail("2", "resourcesManager42", "Voitures",
              new Date(
              1315232752398L), new Date(1315232752398L), true, null, "6", "6", "6",
              "Véhicules utilitaires")));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of getCategory method, of class ResourcesManagerDAO.
   */
  @Test
  public void testGetCategory() throws Exception {
    Connection con = getConnection();
    String id = "1";
    try {
      CategoryDetail expResult = new CategoryDetail("1", "resourcesManager42", "Salles", new Date(
              1315232752398L), new Date(1315232752398L), true, "model1.xml", "5", "5", "5",
              "Salles de réunion");
      CategoryDetail result = dao.getCategory(con, id);
      assertThat(result, is(expResult));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of deleteCategory method, of class ResourcesManagerDAO.
   */
  @Test
  public void testDeleteCategory() throws Exception {
    Connection con = getConnection();
    String id = "1";
    try {
      dao.deleteCategory(con, id);
      con.commit();
      CategoryDetail result = dao.getCategory(con, id);
      assertThat(result, is(nullValue()));
    } finally {
      DBUtil.close(con);
    }
  }
}
