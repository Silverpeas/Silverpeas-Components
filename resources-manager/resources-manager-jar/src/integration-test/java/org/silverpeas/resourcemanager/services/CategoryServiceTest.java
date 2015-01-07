/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
import org.silverpeas.resourcemanager.model.Category;
import org.silverpeas.test.BasicWarBuilder;
import org.silverpeas.test.rule.DbUnitLoadingRule;
import org.silverpeas.util.ServiceProvider;

import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author ehugonnet
 */
@RunWith(Arquillian.class)
public class CategoryServiceTest {

  public CategoryServiceTest() {
  }

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule("create-database.sql", "categories_dataset.xml");

  @Deployment
  public static Archive<?> createTestArchive() {
    return BasicWarBuilder.onWarForTestClass(CategoryServiceTest.class)
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

  @Before
  public void generalSetup() {
    service = ServiceProvider.getService(CategoryService.class);
  }

  private CategoryService service;

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

    Transaction.performInOne(() -> {
      Category category = new Category("Vidéoprojecteurs ", true, "my_form.xml",
          "Vidéoprojecteurs pour les salles de réunion");
      category.setInstanceId("resourcesManager42");
      category.setCreaterId("10");
      category.setUpdaterId("10");
      assertThat(category.getCreationDate(), nullValue());
      assertThat(category.getUpdateDate(), nullValue());
      service.createCategory(category);
      assertThat(category.getCreationDate(), notNullValue());
      assertThat(category.getUpdateDate(), is(category.getCreationDate()));
      assertThat(category.getIdAsLong(), is(6L));
      Category savedCategory = service.getCategory(6L);
      assertThat(savedCategory, is(category));
      return null;
    });
  }

  /**
   * Test of updateCategory method, of class ResourcesManagerDAO.
   */
  @Test
  public void testUpdateCategory() throws Exception {
    Transaction.performInOne(() -> {
      Long id = 1L;
      Category expResult =
          new Category(id, "resourcesManager42", "Salles", true, "model1.xml", "5", "5",
              "Salles de réunion");
      Category result = service.getCategory(id);
      assertThat(result, is(expResult));
      result.setName("Véhicules");
      result.setForm("car_form.xml");
      result.setUpdaterId("1");
      result.setDescription("This is a test");
      result.setBookable(false);
      Date oldUpdateDate = result.getUpdateDate();
      service.updateCategory(result);
      assertThat(result.getUpdateDate(), greaterThan(oldUpdateDate));
      expResult =
          new Category(id, "resourcesManager42", "Véhicules", false, "car_form.xml", "5", "1",
              "This is a test");
      result = service.getCategory(id);
      assertThat(result, is(new CategoryMatcher(expResult)));
      return null;
    });
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
    assertThat(result, contains(new Category(1L, "resourcesManager42", "Salles", true, "model1.xml", "5", "5",
            "Salles de réunion"), new Category(2L, "resourcesManager42", "Voitures", true, null,
        "6", "6",
            "Véhicules utilitaires")));
  }

  /**
   * Test of getCategory method, of class ResourcesManagerDAO.
   */
  @Test
  public void testGetCategory() throws Exception {
    Long id = 1L;
    Category expResult = new Category(1L, "resourcesManager42", "Salles", true, "model1.xml", "5", "5",
            "Salles de réunion");
    Category result = service.getCategory(id);
    assertThat(result, is(expResult));
  }

  /**
   * Test of deleteCategory method, of class ResourcesManagerDAO.
   */
  @Test
  public void testDeleteCategory() throws Exception {
    Long id = 1L;
    service.deleteCategory(id);
    Category result = service.getCategory(id);
    assertThat(result, is(nullValue()));
  }
}
