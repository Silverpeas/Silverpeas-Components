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
package com.silverpeas.yellowpages.dao;

import com.silverpeas.yellowpages.model.GenericContactTopicRelation;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertNotNull;

@Transactional
public class GenericContactTopicRelationDaoTest {

private static GenericContactTopicRelationDao genericContactTopicRelationDao;
    private static DataSource ds;
    private static ClassPathXmlApplicationContext context;

    @BeforeClass
    public static void setUpClass() throws Exception {
        context = new ClassPathXmlApplicationContext("spring-company.xml");
        genericContactTopicRelationDao = (GenericContactTopicRelationDao) context.getBean("genericContactTopicRelationDao");
        ds = (DataSource) context.getBean("jpaDataSource");
        cleanDatabase();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        context.close();
    }

    @Before
    public void setUp() throws Exception {
        cleanDatabase();
    }

    protected static void cleanDatabase() throws IOException, SQLException, DatabaseUnitException {
        IDataSet dataSet = new FlatXmlDataSetBuilder().setColumnSensing(true).build(CompanyDaoTest.class.getClassLoader().getResourceAsStream("com/silverpeas/yellowpages/dao/company-dataset.xml"));
        IDatabaseConnection connection = new DatabaseConnection(ds.getConnection());
        DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
    }

    @Test
    public void testSaveGenericContactTopicRelation() throws Exception {

        // Genericcontact contact = "Obama"
        int genContactId = 217;
        int nodeId = 10;

        GenericContactTopicRelation relation = new GenericContactTopicRelation(nodeId, genContactId);
        GenericContactTopicRelation result = genericContactTopicRelationDao.saveAndFlush(relation);
        assertNotNull(result);
    }

    @Test
    public void testDeleteGenericContactTopicRelation() {
    }

}
