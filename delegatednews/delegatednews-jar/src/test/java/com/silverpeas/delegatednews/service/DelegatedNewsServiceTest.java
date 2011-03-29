/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.delegatednews.service;

import javax.sql.DataSource;

import com.silverpeas.delegatednews.model.DelegatedNew;
import com.silverpeas.delegatednews.dao.DelegatedNewsDaoTest;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import org.junit.AfterClass;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DelegatedNewsServiceTest {

  private static DelegatedNewsService service;
  private static DataSource ds;
  private static ClassPathXmlApplicationContext context;

  public DelegatedNewsServiceTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    context = new ClassPathXmlApplicationContext(
        "spring-delegatednews.xml");
    service = (DelegatedNewsService) context.getBean("delegatedNewsService");
    ds = (DataSource) context.getBean("dataSource");
    cleanDatabase();
  }
  
  @AfterClass
  public static void tearDownClass() throws Exception {
    context.close();
  }

  protected static void cleanDatabase() throws IOException, SQLException, DatabaseUnitException {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSet(
        DelegatedNewsDaoTest.class.getClassLoader().getResourceAsStream(
        "com/silverpeas/delegatednews/dao/delegatednews-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    IDatabaseConnection connection = new DatabaseConnection(ds.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
  }

  @Before
  public void setUp() throws Exception {
    cleanDatabase();
  }

  @Test
  public void testAddDelegatedNew() throws Exception {
	int pubId = 4;
	String instanceId = "kmelia1";  
    String contributorId = "1";
    service.addDelegatedNew(pubId, instanceId, contributorId);
  }
}
