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
package org.silverpeas.components.whitepages.service;

import org.silverpeas.components.whitepages.model.SearchField;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.BasicWarBuilder;
import org.silverpeas.core.test.integration.rule.DbUnitLoadingRule;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Set;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests on the services provided by the WhitePagesService objects.
 */
@RunWith(Arquillian.class)
public class WhitePagesServiceIT {

  private WhitePagesService whitePagesService;

  public WhitePagesServiceIT() {
  }

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule("create-database.sql", "whitePages-dataset.xml");

  @PersistenceContext
  private EntityManager entityManager;

  @Deployment
  public static Archive<?> createTestArchive() {
    return BasicWarBuilder.onWarForTestClass(WhitePagesServiceIT.class)
        .testFocusedOn(warBuilder -> {
          warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core");
          warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core.services:silverpeas-core-pdc");
          warBuilder.addMavenDependencies("org.silverpeas.core.services:silverpeas-core-tagcloud");
          warBuilder.addAsResource("org/silverpeas/classifyEngine/ClassifyEngine.properties");
          warBuilder.addPackages(true, "org.silverpeas.components.whitepages");
        }).build();
  }

  @Before
  public void loadTestContext() throws Exception {
    whitePagesService = WhitePagesService.get();
    assertThat(whitePagesService, notNullValue());
  }

  /**
   * Tests the creation of several fields belonging to an application instance.
   */
  @Test
  public void testCreateSearchFields() throws Exception {
    final String instanceId = "whitePages32";
    String[] fields = new String[]{"field10", "field11", "field12"};
    whitePagesService.createSearchFields(fields, instanceId);

    Set<SearchField> searchFields = whitePagesService.getSearchFields(instanceId);
    for (SearchField searchField : searchFields) {
      assertThat(searchField.getFieldId(), isIn(fields));
      assertThat(searchField.getInstanceId(), is(instanceId));
    }
  }

  /**
   * Tests the getting of the fields in a given application instance.
   */
  @Test
  public void testGetSearchFields() throws Exception {
    Set<SearchField> searchFields = whitePagesService.getSearchFields("whitePages1");
    assertThat(searchFields.size(), is(1));
    SearchField actualSearchField = searchFields.iterator().next();
    assertThat(actualSearchField.getId(), is("0"));
    assertThat(actualSearchField.getInstanceId(), is("whitePages1"));
    assertThat(actualSearchField.getFieldId(), is("field1"));
  }

  /**
   * Tests the deletion of the fields in a given instance.
   */
  @Test
  public void testDeleteFields() throws Exception {
    whitePagesService.deleteFields("whitePages1");
    SearchField actual = entityManager.find(SearchField.class, "0");
    assertThat(actual, nullValue());
  }
}
