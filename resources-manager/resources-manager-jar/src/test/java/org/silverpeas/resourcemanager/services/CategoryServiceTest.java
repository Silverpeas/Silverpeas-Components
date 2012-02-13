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

import com.stratelia.webactiv.util.DBUtil;
import java.sql.Connection;
import java.sql.SQLException;
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
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.resourcemanager.model.Category;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author ehugonnet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-resource-manager-datasource.xml",
  "/spring-resource-manager.xml"})
@Transactional
@TransactionConfiguration(transactionManager = "jpaTransactionManager")
public class CategoryServiceTest {

  public CategoryServiceTest() {
  }
  private static ReplacementDataSet dataSet;

  @BeforeClass
  public static void prepareDataSet() throws Exception {
    FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
    dataSet = new ReplacementDataSet(builder.build(CategoryServiceTest.class.getClassLoader().
            getResourceAsStream("org/silverpeas/resourcemanager/services/categories_dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
  }
  @Inject
  private CategoryService service;
  @Inject
  @Named("jpaDataSource")
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

  @Test
  public void testSimpleConversion() {
    Date date = new Date();
    String value = String.valueOf(date.getTime());
    Date result = new Date(Long.parseLong(value));
    assertThat(date, is(result));
  }

  /**
   * Test of createCategory method, of class ResourcesManagerDAO.
   */
  @Test
  public void testCreateCategory() throws Exception {
    Category category = new Category("Vidéoprojecteurs ", true, "my_form.xml",
            "Vidéoprojecteurs pour les salles de réunion");
    Date now = new Date();
    category.setCreationDate(now);
    category.setUpdateDate(now);
    category.setInstanceId("resourcesManager42");
    category.setCreaterId("10");
    category.setUpdaterId("10");

    String result = service.createCategory(category);
    assertThat(result, is("250"));
    category.setId("250");
    Category savedCategory = service.getCategory("250");
    assertThat(savedCategory, is(category));

  }

  /**
   * Test of updateCategory method, of class ResourcesManagerDAO.
   */
  @Test
  public void testUpdateCategory() throws Exception {
    String id = "1";
    Category expResult = new Category(id, "resourcesManager42", "Salles", new Date(
            1315232752398L), new Date(1315232752398L), true, "model1.xml", "5", "5",
            "Salles de réunion");
    Category result = service.getCategory(id);
    assertThat(result, is(expResult));
    Date now = new Date();
    result.setUpdateDate(now);
    result.setName("Véhicules");
    result.setForm("car_form.xml");
    result.setUpdaterId("1");
    result.setDescription("This is a test");
    result.setBookable(false);
    service.updateCategory(result);
    expResult = new Category(id, "resourcesManager42", "Véhicules", new Date(
            1315232752398L), now, false, "car_form.xml", "5", "1", "This is a test");
    result = service.getCategory(id);
    assertThat(result, is(new CategoryMatcher(expResult)));
  }

  /**
   * Test of getCategories method, of class ResourcesManagerDAO.
   */
  @Test
  public void testGetCategories() throws Exception {
    String instanceId = "resourcesManager42";
    List<Category> result = service.getCategories(instanceId);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(2));
    assertThat(result, contains(new Category("1", "resourcesManager42", "Salles", new Date(
            1315232752398L), new Date(1315232752398L), true, "model1.xml", "5", "5",
            "Salles de réunion"), new Category("2", "resourcesManager42", "Voitures",
            new Date(1315232752398L), new Date(1315232752398L), true, null,  "6", "6",
            "Véhicules utilitaires")));
  }

  /**
   * Test of getCategory method, of class ResourcesManagerDAO.
   */
  @Test
  public void testGetCategory() throws Exception {
    String id = "1";
    Category expResult = new Category("1", "resourcesManager42", "Salles", new Date(
            1315232752398L), new Date(1315232752398L), true, "model1.xml", "5", "5",
            "Salles de réunion");
    Category result = service.getCategory(id);
    assertThat(result, is(expResult));
  }

  /**
   * Test of deleteCategory method, of class ResourcesManagerDAO.
   */
  @Test
  public void testDeleteCategory() throws Exception {
    String id = "1";
    service.deleteCategory(id);
    Category result = service.getCategory(id);
    assertThat(result, is(nullValue()));
  }
}
