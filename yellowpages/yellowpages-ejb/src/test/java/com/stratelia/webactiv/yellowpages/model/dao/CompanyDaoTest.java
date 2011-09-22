package com.stratelia.webactiv.yellowpages.model.dao;

import com.silverpeas.components.model.AbstractJndiCase;
import com.silverpeas.components.model.SilverpeasJndiCase;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.yellowpages.model.beans.Company;
import com.stratelia.webactiv.yellowpages.model.beans.CompanyPK;
import com.stratelia.webactiv.yellowpages.model.dao.jdbc.JDBCCompanyRequester;
import org.dbunit.database.IDatabaseConnection;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.naming.NamingException;
import java.io.IOException;
import java.sql.Connection;
import java.util.Date;

import static org.junit.Assert.*;

public class CompanyDaoTest extends AbstractJndiCase {

    private JDBCCompanyRequester companyDAO = new JDBCCompanyRequester();

    public CompanyDaoTest() {
    }

    @BeforeClass
    public static void generalSetUp() throws IOException, NamingException, Exception {
        baseTest = new SilverpeasJndiCase("com/stratelia/webactiv/yellowpages/model/dao/company-dataset.xml", "create-database.ddl");
        baseTest.configureJNDIDatasource();
        IDatabaseConnection databaseConnection = baseTest.getDatabaseTester().getConnection();
        executeDDL(databaseConnection, baseTest.getDdlFile());
        baseTest.getDatabaseTester().closeConnection(databaseConnection);
    }

    @Test
    public void testSaveCompany() throws Exception {
        IDatabaseConnection dbConnection = baseTest.getConnection();
        Connection con = dbConnection.getConnection();
        DBUtil.getInstanceForTest(con);
        // Company à enregistrer
        CompanyPK pk = new CompanyPK("200", null, "yellowpages6");
        // CommentPK pk = new CommentPK(null, null, "kmelia18");
        // ForeignPK foreignKey = new ForeignPK("200", "kmelia18");
        //int ownerId = RandomGenerator.getRandomInt();
        Company company = new Company(pk, "Apple", "steve.jobs@apple.test", "+ 00000000", "+ 11111111", DateUtil.date2SQLDate(new Date()), "0");
        CompanyPK result = companyDAO.saveCompany(con, company);
        assertNotNull(result);
        // relecture de la company en base
        Company companyFromDb = companyDAO.getCompany(con, result);
        // comparaison des deux objets
        assertNotNull(companyFromDb);
        assertEquals(company.getName(), companyFromDb.getName());
        assertEquals(company.getEmail(), companyFromDb.getEmail());
        assertEquals(company.getPk().getId(), companyFromDb.getPk().getId());
    }

    @Test
    public void testUpdateCompany() throws Exception {
        IDatabaseConnection dbConnection = baseTest.getConnection();
        Connection con = dbConnection.getConnection();
        DBUtil.getInstanceForTest(con);
        // Company à modifier : Walt Disney
        CompanyPK pk = new CompanyPK("10", null, "yellowpages6");
        Company company = companyDAO.getCompany(con, pk);
        assertNotNull(company);
        company.setName("Pixar");
        company.setEmail("pixar@oosphere.com");
        int result = companyDAO.updateCompany(con, company);
        assertTrue(result > 0);
        // relecture de la company en base
        Company companyFromDb = companyDAO.getCompany(con, pk);
        // comparaison des deux objets
        assertNotNull(companyFromDb);
        assertEquals(company.getName(), companyFromDb.getName());
        assertEquals(company.getEmail(), companyFromDb.getEmail());
        assertEquals(company.getPk().getId(), companyFromDb.getPk().getId());
    }

//    @Test
    public void testDeleteCompany() {
        fail("Not yet implemented");
    }

//    @Test
    public void testFindCompanyListById() {
        fail("Not yet implemented");
    }

//    @Test
    public void testAddCompanyToContact() {
        fail("Not yet implemented");
    }

//    @Test
    public void testRemoveCompanyFromContact() {
        fail("Not yet implemented");
    }

}
