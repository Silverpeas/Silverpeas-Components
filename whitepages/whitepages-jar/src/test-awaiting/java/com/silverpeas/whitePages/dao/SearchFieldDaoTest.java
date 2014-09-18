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
package com.silverpeas.whitePages.dao;

import com.silverpeas.whitePages.model.SearchField;
import com.silverpeas.whitePages.test.DataSourceAccessor;
import java.util.Set;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class SearchFieldDaoTest {

  private ConfigurableApplicationContext context;
  private DataSourceAccessor dataSourceAccessor;
  private SearchFieldDao searchFieldDao;

  public SearchFieldDaoTest() {
  }

  @Before
  public void loadTestContext() throws Exception {
    context = new ClassPathXmlApplicationContext("/spring-whitePages-dao.xml",
        "spring-whitePages-embbed-datasource.xml");
    searchFieldDao = context.getBean(SearchFieldDao.class);
    assertNotNull(searchFieldDao);
    DataSource dataSource = context.getBean(DataSource.class);
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
   * Tests the creation of a search field.
   */
  @Test
  public void testCreateSearchField() throws Exception {
    SearchField searchField = new SearchField();
    searchField.setFieldId("field23");
    searchField.setInstanceId("whitePages32");
    searchFieldDao.createSearchField(searchField);
    assertNotNull(searchField.getId());
    assertThat(dataSourceAccessor.isExists(searchField.getId()), is(true));
    SearchField createdSearchField = dataSourceAccessor.fetchSearchFieldById(searchField.getId());
    assertThat(createdSearchField, is(searchField));
  }

  /**
   * Tests the deletion of the search fields belonging to an application instance.
   */
  @Test
  public void testDeleteSearchFieldsFor() throws Exception {
    SearchField searchField = getExistingSearchField();
    searchFieldDao.deleteSearchFieldsFor(searchField.getInstanceId());
    assertThat(dataSourceAccessor.isExists(searchField.getId()), is(false));
  }

  /**
   * Test the search fields getting by instance id.
   */
  @Test
  public void testGetSearchFields() {
    SearchField expectedSearchField = getExistingSearchField();
    Set<SearchField> actualSearchFields = searchFieldDao.getSearchFields(expectedSearchField.
        getInstanceId());
    assertThat(actualSearchFields.size(), is(1));
    SearchField actualSearchField = actualSearchFields.iterator().next();
    assertThat(actualSearchField, is(expectedSearchField));
  }

  private SearchField getExistingSearchField() {
    SearchField searchField = new SearchField();
    ReflectionTestUtils.setField(searchField, "id", "0");
    searchField.setFieldId("field1");
    searchField.setInstanceId("whitePages1");
    return searchField;
  }
}
