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

import com.silverpeas.yellowpages.model.Company;
import com.silverpeas.yellowpages.model.GenericContact;
import org.dbunit.DataSourceDatabaseTester;
import org.dbunit.IDatabaseTester;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.inject.Inject;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class CompanyDaoTest extends SpringDbTest {
    @Inject private CompanyDao dao;

    @Test
    public void testSearchByPatternWithSpacesAndMajMin() throws Exception {
        String searchPattern = "  wAl   "; // doit remonter 2 résultats : Walmart et Walt Disney
        List<Company> result = dao.findCompanyListByPattern(searchPattern);
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testSaveCompany() throws Exception {
        Company company = new Company("yellowpages6", "Apple", "steve.jobs@apple.test", "+ 00000000", "+ 11111111");

        // enregistrement
        Company result = dao.save(company);
        assertNotNull(result);

        // relecture de la company dans la base
        Company companyFromDb = dao.findOne(result.getCompanyId());
        // comparaison des deux objets
        assertNotNull(companyFromDb);
        assertEquals(company.getName(), companyFromDb.getName());
        assertEquals(company.getEmail(), companyFromDb.getEmail());
        assertEquals(company.getPhone(), companyFromDb.getPhone());
        assertEquals(company.getFax(), companyFromDb.getFax());
        assertEquals(result.getCompanyId(), companyFromDb.getCompanyId());
    }

    @Test
    public void testUpdateCompany() throws Exception {
        // Company à modifier : Walt Disney
        int id = 1;
        String newName = "Pixar";
        String newEmail = "pixar@test.com";

        Company company = dao.findOne(id);
        assertNotNull(company);

        company.setName(newName);
        company.setEmail(newEmail);
        dao.saveAndFlush(company);

        // relecture de la company dans la base
        Company companyFromDb = dao.findOne(id);
        // comparaison des deux objets
        assertNotNull(companyFromDb);
        assertEquals(newName, companyFromDb.getName());
        assertEquals(newEmail, companyFromDb.getEmail());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testDeleteCompanyShouldReturnException() throws Exception {
        // Tentative de suppression de la company : Microsoft
        // Doit générer une exception car des contacts sont lies à cette company
        int id = 2;
        Company company = dao.findOne(id);
        assertNotNull(company);
        dao.delete(company);
        assertTrue(false);
    }

    @Test
    public void testDeleteCompany() throws Exception {
        // Tentative de suppression de la company : Walmart
        // Ok car cette company n'est liée à aucun contact
        int id = 7;
        Company company = dao.findOne(id);
        assertNotNull(company);
        dao.delete(company);

        // relecture de la company en base
        Company companyFromDb = dao.findOne(id);
        assertNull(companyFromDb);
    }

    @Test
    public void testFindContactListByCompanyId() throws Exception {
        // Liste des contacts associés à : Microsoft
        int id = 2;
        List<GenericContact> contactList = dao.findContactListByCompanyId(id);
        assertNotNull(contactList);
        assertFalse(contactList.isEmpty());
        assertEquals(1,contactList.size());
    }
}