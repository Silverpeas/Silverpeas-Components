/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.delegatednews.service;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.*;

import javax.sql.DataSource;

import com.silverpeas.delegatednews.dao.DelegatedNewsDaoTest;
import com.silverpeas.delegatednews.model.DelegatedNews;

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
import java.util.Date;
import java.util.List;

import org.junit.AfterClass;


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
    ds = (DataSource) context.getBean("jpaDataSource");
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
  public void testAddDelegatedNews() throws Exception {
    int pubId = 4;
	  String instanceId = "kmelia1";  
    String contributorId = "1";
    service.addDelegatedNews(pubId, instanceId, contributorId, new Date(), null, null);
  }
  
  @Test
  public void testGetDelegatedNews() throws Exception {
    int pubId = 1;
    DelegatedNews detail = service.getDelegatedNews(pubId);
    assertThat(detail, notNullValue());
    assertThat(detail.getInstanceId(), is("kmelia1"));
  }
  

  @Test
  public void testGetAllDelegatedNews() throws Exception {
    List<DelegatedNews> listDetail = service.getAllDelegatedNews();
    assertThat(listDetail, notNullValue());
    assertThat(listDetail.size(), is(3));
  }
  
  @Test
  public void testGetAllValidDelegatedNews() throws Exception {
    List<DelegatedNews> listDetail = service.getAllValidDelegatedNews();
    assertThat(listDetail, notNullValue());
    assertThat(listDetail.size(), is(2));
  }
  
  @Test
  public void testValidateDelegatedNews() throws Exception {
    int pubId = 1; 
    String validatorId = "2"; 
    service.validateDelegatedNews(pubId, validatorId);
    DelegatedNews detail = service.getDelegatedNews(pubId);
    assertThat(detail, notNullValue());
    assertThat(detail.getPubId(), is(1));
  }
  
}
