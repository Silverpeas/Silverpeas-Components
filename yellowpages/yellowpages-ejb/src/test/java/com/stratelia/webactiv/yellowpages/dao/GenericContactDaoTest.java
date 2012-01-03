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
package com.stratelia.webactiv.yellowpages.dao;

import com.stratelia.webactiv.yellowpages.model.GenericContact;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class GenericContactDaoTest {

    private static GenericContactDao dao;
    private static DataSource ds;
    private static ClassPathXmlApplicationContext context;

    @BeforeClass
    public static void setUpClass() throws Exception {
        context = new ClassPathXmlApplicationContext(
                "spring-company.xml");
        dao = (GenericContactDao) context.getBean("genericContactDao");
        ds = (DataSource) context.getBean("dataSource");
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
        ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSet(CompanyDaoTest.class.getClassLoader().getResourceAsStream(
                "com/stratelia/webactiv/yellowpages/dao/company-dataset.xml")));
        dataSet.addReplacementObject("[NULL]", null);
        IDatabaseConnection connection = new DatabaseConnection(ds.getConnection());
        DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
    }

    @Test
    public void testSaveGenericContact() throws Exception {

        // Test sur compagnie existante : "RIM"
        int companyId = 6;
        GenericContact gc = new GenericContact(GenericContact.TYPE_COMPANY, null, companyId);

        // enregistrement
        GenericContact result = dao.save(gc);
        assertNotNull(result);

        // relecture de la company dans la base
        GenericContact gcFromDb = dao.findOne(result.getGenericcontactId());
        // comparaison des deux objets
        assertNotNull(gcFromDb);
        assertEquals(companyId, gcFromDb.getCompanyId().intValue());
        assertEquals(null, gcFromDb.getContactId());
    }

    @Test
    public void testDeleteGenericContact() throws Exception {
        // Generic contact à supprimer
        int id = 210;

        GenericContact gc = dao.findOne(id);
        dao.delete(gc);
        assertNotNull(gc);
        GenericContact gcResult = dao.findOne(id);
        assertNull(gcResult);
    }

    @Test
    public void testFindGenericContactFromContactId() {
        // Id du contact "Georges Washington"
        int idWashingTon = 10;
        // Generic contact correspondant au contact "Georges Washington"
        int idGCWashingTon = 210;

        GenericContact gc = dao.findGenericContactFromContactId(idWashingTon);
        assertNotNull(gc);
        assertEquals(idGCWashingTon, gc.getGenericcontactId());
    }

    @Test
    public void testFindGenericCompanyFromCompanyId() {
        // Id de la company "Boïng"
        int idBoing = 5;
        // Generic contact correspondant à la company "Boïng"
        int idGCBoing = 115;

        GenericContact gc = dao.findGenericContactFromCompanyId(idBoing);
        assertNotNull(gc);
        assertEquals(idGCBoing, gc.getGenericcontactId());
    }

}
