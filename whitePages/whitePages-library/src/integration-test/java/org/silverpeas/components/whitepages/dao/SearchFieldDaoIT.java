/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.components.whitepages.dao;

import org.silverpeas.components.whitepages.model.SearchField;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.test.BasicWarBuilder;
import org.silverpeas.core.test.rule.DbUnitLoadingRule;
import org.silverpeas.core.util.ServiceProvider;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Set;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(Arquillian.class)
public class SearchFieldDaoIT {

  private SearchFieldDao searchFieldDao;

  public SearchFieldDaoIT() {
  }

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule("create-database.sql", "whitePages-dataset.xml");

  @PersistenceContext
  private EntityManager entityManager;


  @Deployment
  public static Archive<?> createTestArchive() {
    return BasicWarBuilder.onWarForTestClass(SearchFieldDaoIT.class).testFocusedOn(warBuilder -> {
      warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core");
      warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core.services:silverpeas-core-pdc");
      warBuilder.addMavenDependencies("org.silverpeas.core.services:silverpeas-core-tagcloud");
      warBuilder.addPackages(true, "org.silverpeas.components.whitepages");
    }).build();
  }

  @Before
  public void generalSetup() {
    searchFieldDao = ServiceProvider.getService(SearchFieldDao.class);
    assertThat(searchFieldDao, notNullValue());
  }

  /**
   * Tests the creation of a search field.
   */
  @Test
  public void testCreateSearchField() throws Exception {
    String searchFieldId = Transaction.performInOne(() -> {
      SearchField searchField = new SearchField();
      searchField.setFieldId("field23");
      searchField.setInstanceId("whitePages32");
      searchFieldDao.createSearchField(searchField);
      assertThat(searchField.getId(), notNullValue());
      return searchField.getId();
    });
    SearchField actual = entityManager.find(SearchField.class, searchFieldId);
    assertThat(actual, notNullValue());
  }

  /**
   * Tests the deletion of the search fields belonging to an application instance.
   */
  @Test
  public void testDeleteSearchFieldsFor() throws Exception {
    Transaction.performInOne(() -> {
      searchFieldDao.deleteSearchFieldsFor("whitePages1");
      return null;
    });
    SearchField actual = entityManager.find(SearchField.class, "0");
    assertThat(actual, nullValue());
  }

  /**
   * Test the search fields getting by instance id.
   */
  @Test
  public void testGetSearchFields() {
    String instanceId = "whitePages1";
    Set<SearchField> actualSearchFields = searchFieldDao.getSearchFields(instanceId);
    assertThat(actualSearchFields.size(), is(1));
    SearchField actualSearchField = actualSearchFields.iterator().next();
    assertThat(actualSearchField.getFieldId(), is("field1"));
    assertThat(actualSearchField.getInstanceId(), is("whitePages1"));
    assertThat(actualSearchField.getId(), is("0"));
  }

}
