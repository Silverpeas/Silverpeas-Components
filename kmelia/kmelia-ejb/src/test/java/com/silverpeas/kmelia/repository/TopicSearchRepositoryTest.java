/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.kmelia.repository;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.silverpeas.kmelia.domain.TopicSearch;
import com.silverpeas.kmelia.repository.TopicSearchRepository;

/**
 *
 * @author ebonnet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-kmelia-search.xml", "/spring-kmelia-search-embbed-datasource.xml"})
@TransactionConfiguration(transactionManager = "jpaTransactionManager")
public class TopicSearchRepositoryTest {
  
  @Autowired
  private TopicSearchRepository repo;

  @Inject
  private DataSource dataSource;

  
  public TopicSearchRepositoryTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void generalSetUp() throws Exception {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(
        TopicSearchRepositoryTest.class.getClassLoader().getResourceAsStream(
        "com/silverpeas/kmelia/model/kmelia-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
  }
  
  @Before
  public void setUp() {
  }
  
  @After
  public void tearDown() {
  }

  /**
   * Test put TopicSearch inside repository.
   */
  @Test
  public void testSave() {
    // System.out.println("getMostInterestedSearch");
    String instanceId = "kmelia111";
    TopicSearch entity = new TopicSearch(instanceId, 0, 0, "fr", "ma nouvelle recherche", new Date());
    TopicSearch result = repo.saveAndFlush(entity);
    assertEquals(result, repo.findOne(result.getId()));
  }
  
  /**
   * Test retrieve element from database
   */
  @Test
  public void testFindAll() {
    List<TopicSearch> results = repo.findAll();
    assertEquals(6, results.size());
  }
  
  /**
   * 
   */
  @Test
  public void testFindByInstanceId() {
    List<TopicSearch> results = repo.findByInstanceId("kmelia111");
    assertEquals(5, results.size());
  }
  
}
