/*
 * Copyright (C) 2000-2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
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
package com.silverpeas.whitePages.service;

import com.silverpeas.whitePages.model.SearchField;
import com.silverpeas.whitePages.test.DataSourceAccessor;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Tests on the services provided by the WhitePagesService objects.
 */
public class WhitePagesServiceTest {

  private ConfigurableApplicationContext context;
  private DataSourceAccessor dataSourceAccessor;
  private WhitePagesService whitePagesService;

  public WhitePagesServiceTest() {
  }

  @Before
  public void loadTestContext() throws Exception {
    context = new ClassPathXmlApplicationContext("/spring-whitePages-services.xml",
        "spring-whitePages-embbed-datasource.xml");
    whitePagesService = context.getBean(WhitePagesService.class);
    assertNotNull(whitePagesService);
    DataSource dataSource = context.getBean(DataSource.class);
    assertNotNull(dataSource);
    assertNotNull(dataSource);
    dataSourceAccessor = DataSourceAccessor.getAccessorOnDataSource(dataSource).
        initDataSourceWithData("whitePages-dataset.xml");
  }

  @After
  public void unloadTestContext() throws Exception {
    dataSourceAccessor.cleanUpDataSource();
    context.close();
  }

  /**
   * Tests the creation of several fields belonging to an application instance.
   */
  @Test
  public void testCreateSearchFields() throws Exception {
    final String instanceId = "whitePages32";
    String[] fields = new String[]{"field10", "field11", "field12"};
    whitePagesService.createSearchFields(fields, instanceId);
    List<SearchField> searchFields = dataSourceAccessor.fetchSearchFieldByInstanceId(instanceId);
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
    SearchField expectedSearchField = dataSourceAccessor.fetchSearchFieldById("0");
    assertThat(actualSearchField, is(expectedSearchField));
  }

  /**
   * Tests the deletion of the fields in a given instance.
   */
  @Test
  public void testDeleteFields() throws Exception {
    whitePagesService.deleteFields("whitePages1");
    assertThat(dataSourceAccessor.isExists("0"), is(false));
  }
}
