/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.delegatednews.dao;

import com.silverpeas.delegatednews.model.DelegatedNews;

import org.dbunit.operation.DatabaseOperation;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.naming.NamingException;
import java.io.IOException;
import javax.sql.DataSource;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.junit.AfterClass;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import java.lang.String;
import java.lang.Integer;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DelegatedNewsDaoTest {

  private static DelegatedNewsDao dao;
  private static ClassPathXmlApplicationContext context;
  
  public DelegatedNewsDaoTest() {
  }

  @BeforeClass
  public static void generalSetUp() throws IOException, NamingException, Exception {
    context = new ClassPathXmlApplicationContext("spring-delegatednews.xml");
    dao = (DelegatedNewsDao) context.getBean("delegatedNewsDao");
    DataSource ds = (DataSource) context.getBean("dataSource");
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSet(
    DelegatedNewsDaoTest.class.getClassLoader().getResourceAsStream(
    "com/silverpeas/delegatednews/dao/delegatednews-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    IDatabaseConnection connection = new DatabaseConnection(ds.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet); 
  }
  
   @AfterClass
  public static void tearDownClass() throws Exception {
    context.close();
  }

  @Test
  public void testInsertDelegatedNews() throws Exception {
    Integer pubId = new Integer("3");
    String instanceId = "kmelia1";  
    String contributorId = "1";
    DelegatedNews expectedDetail = new DelegatedNews(pubId.intValue(), instanceId, contributorId, new Date());
    expectedDetail = dao.save(expectedDetail);
    DelegatedNews detail = dao.readByPrimaryKey(pubId);
    assertThat(detail, notNullValue());
    assertThat(detail.getPubId(), is(expectedDetail.getPubId()));
    assertThat(detail.getInstanceId(), is(expectedDetail.getInstanceId()));
    assertThat(detail.getContributorId(), is(expectedDetail.getContributorId()));
    //assertThat(detail, is(expectedDetail));
  }
  
  @Test
  public void testGetDelegatedNews() throws Exception {
    Integer pubId = new Integer("1");
	  DelegatedNews detail = dao.readByPrimaryKey(pubId);
    assertThat(detail, notNullValue());

    pubId = new Integer("2");
    detail = dao.readByPrimaryKey(pubId);
    assertThat(detail, notNullValue());
  }
}
