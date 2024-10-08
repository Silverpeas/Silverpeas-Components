/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.resourcesmanager.service;

import org.hamcrest.Matchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.components.resourcesmanager.model.Category;
import org.silverpeas.components.resourcesmanager.test.WarBuilder4ResourcesManager;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.identifier.UniqueLongIdentifier;
import org.silverpeas.core.test.integration.rule.DbUnitLoadingRule;
import org.silverpeas.core.test.unit.EntityIdSetter;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author ehugonnet
 */
@RunWith(Arquillian.class)
public class CategoryServiceIT {

  private final EntityIdSetter idSetter = new EntityIdSetter(UniqueLongIdentifier.class);

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule("create-database.sql", "categories_dataset.xml");

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4ResourcesManager.onWarForTestClass(CategoryServiceIT.class).build();
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
  public void testCreateCategory() {

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
  public void testUpdateCategory() {
    Transaction.performInOne(() -> {
      Long id = 1L;
      Category expResult = new Category("Salles", true, "model1.xml", "Salles de réunion");
      idSetter.setIdTo(expResult, String.valueOf(id));
      expResult.setInstanceId("resourcesManager42");
      expResult.setCreaterId("5");
      expResult.setUpdaterId("5");

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

      expResult = new Category("Véhicules", false, "car_form.xml", "This is a test");
      idSetter.setIdTo(expResult, String.valueOf(id));
      expResult.setInstanceId("resourcesManager42");
      expResult.setCreaterId("5");
      expResult.setUpdaterId("1");
      result = service.getCategory(id);
      assertThat(result, Matchers.is(new CategoryMatcher(expResult)));
      return null;
    });
  }

  /**
   * Test of getCategories method, of class ResourcesManagerDAO.
   */
  @Test
  public void testGetCategories() {
    String instanceId = "resourcesManager42";
    Category expCategory1 = new Category("Salles", true, "model1.xml", "Salles de réunion");
    idSetter.setIdTo(expCategory1, "1");
    expCategory1.setInstanceId(instanceId);
    expCategory1.setCreaterId("5");
    expCategory1.setUpdaterId("5");
    Category expCategory2 = new Category("Voitures", true, null, "Véhicules utilitaires");
    idSetter.setIdTo(expCategory2, "2");
    expCategory2.setInstanceId(instanceId);
    expCategory2.setCreaterId("6");
    expCategory2.setUpdaterId("6");

    List<Category> result = service.getCategories(instanceId);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(2));
    assertThat(result, contains(expCategory1, expCategory2));
  }

  /**
   * Test of getCategory method, of class ResourcesManagerDAO.
   */
  @Test
  public void testGetCategory() {
    Long id = 1L;
    Category expResult = new Category("Salles", true, "model1.xml", "Salles de réunion");
    idSetter.setIdTo(expResult, "1");
    expResult.setInstanceId("resourcesManager42");
    expResult.setCreaterId("5");
    expResult.setUpdaterId("5");
    Category result = service.getCategory(id);
    assertThat(result, is(expResult));
  }

  /**
   * Test of deleteCategory method, of class ResourcesManagerDAO.
   */
  @Test
  public void testDeleteCategory() {
    Long id = 1L;
    service.deleteCategory(id);
    Category result = service.getCategory(id);
    assertThat(result, is(nullValue()));
  }
}
