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
package com.silverpeas.whitePages.service;

import com.silverpeas.whitePages.model.SearchField;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.test.BasicWarBuilder;
import org.silverpeas.test.rule.DbUnitLoadingRule;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.*;

/**
 * Tests on the services provided by the WhitePagesService objects.
 */
@RunWith(Arquillian.class)
public class WhitePagesServiceTest {

  private WhitePagesService whitePagesService;

  public WhitePagesServiceTest() {
  }

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule("create-database.sql", "whitePages-dataset.xml");

  @PersistenceContext
  private EntityManager entityManager;

  @Deployment
  public static Archive<?> createTestArchive() {
    return BasicWarBuilder.onWarForTestClass(WhitePagesServiceProviderTest.class)
        .testFocusedOn(warBuilder -> {
          warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core:lib-core");
          warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core.ejb-core:pdc");
          warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core.ejb-core:node");
          warBuilder.addMavenDependencies("org.silverpeas.core.ejb-core:tagcloud");
          warBuilder.addMavenDependencies("org.silverpeas.core.ejb-core:publication");
          warBuilder.addMavenDependencies("org.silverpeas.core.ejb-core:formtemplate");
          warBuilder.addMavenDependencies("org.silverpeas.core.ejb-core:searchengine");
          warBuilder.addMavenDependencies("org.apache.tika:tika-core");
          warBuilder.addMavenDependencies("org.apache.tika:tika-parsers");
          warBuilder.addAsResource("META-INF/test-MANIFEST.MF", "META-INF/MANIFEST.MF");
          warBuilder.addAsResource("org/silverpeas/classifyEngine/ClassifyEngine.properties");
          warBuilder.addPackages(true, "com.silverpeas.whitePages");
        }).build();
  }

  @Before
  public void loadTestContext() throws Exception {
    whitePagesService = WhitePagesService.get();
    assertNotNull(whitePagesService);
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
    assertNull(actual);
  }
}
