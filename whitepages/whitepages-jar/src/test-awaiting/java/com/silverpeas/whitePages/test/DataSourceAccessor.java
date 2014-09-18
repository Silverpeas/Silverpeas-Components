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
package com.silverpeas.whitePages.test;

import com.silverpeas.whitePages.model.SearchField;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.dbunit.DataSourceDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * An object providing an access to the data in the underlying data source used in the tests.
 */
public class DataSourceAccessor {

  private DataSourceDatabaseTester databaseTester;

  private DataSourceAccessor(DataSource dataSource) {
    databaseTester = new DataSourceDatabaseTester(dataSource);
  }

  public static DataSourceAccessor getAccessorOnDataSource(DataSource dataSource) {
    return new DataSourceAccessor(dataSource);
  }

  public DataSourceAccessor initDataSourceWithData(String xmlDataFile) throws Exception {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(
        getClass().getClassLoader().getResourceAsStream("whitePages-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    databaseTester.setDataSet(dataSet);
    databaseTester.onSetup();
    return this;
  }

  public void cleanUpDataSource() throws Exception {
    IDataSet actualDataSet = databaseTester.getConnection().createDataSet();
    databaseTester.setDataSet(actualDataSet);
    databaseTester.onTearDown();
  }

  public SearchField fetchSearchFieldById(String id) throws Exception {
    SearchField searchField = new SearchField();
    ITable table = databaseTester.getConnection().createDataSet().getTable(
        "sc_whitepages_searchfields");
    int count = table.getRowCount();
    for (int i = 0; i < count; i++) {
      String actualId = (String) table.getValue(i, "id");
      if (actualId.equals(id)) {
        ReflectionTestUtils.setField(searchField, "id", actualId);
        String actualFieldId = (String) table.getValue(i, "fieldId");
        String actualInstanceId = (String) table.getValue(i, "instanceId");
        searchField.setFieldId(actualFieldId);
        searchField.setInstanceId(actualInstanceId);
        break;
      }
    }
    return searchField;
  }

  public List<SearchField> fetchSearchFieldByInstanceId(String id) throws Exception {
    List<SearchField> searchFields = new ArrayList<SearchField>();
    ITable table = databaseTester.getConnection().createDataSet().getTable(
        "sc_whitepages_searchfields");
    int count = table.getRowCount();
    for (int i = 0; i < count; i++) {
      String actualInstanceId = (String) table.getValue(i, "instanceId");
      if (actualInstanceId.equals(id)) {
        SearchField searchField = new SearchField();
        String actualFieldId = (String) table.getValue(i, "fieldId");
        String actualId = (String) table.getValue(i, "id");
        ReflectionTestUtils.setField(searchField, "id", actualId);
        searchField.setFieldId(actualFieldId);
        searchField.setInstanceId(actualInstanceId);
        searchFields.add(searchField);
      }
    }
    return searchFields;
  }

  public boolean isExists(String searchFieldId) throws Exception {
    int searchFieldsCount = databaseTester.getConnection().getRowCount("sc_whitepages_searchfields",
        "where id = '" + searchFieldId + "'");
    return searchFieldsCount == 1;
  }
}
