package com.stratelia.webactiv.yellowpages.model.dao;

import com.silverpeas.components.model.AbstractJndiCase;
import com.silverpeas.components.model.SilverpeasJndiCase;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.yellowpages.model.Constants.GenericContactTypeConstant;
import com.stratelia.webactiv.yellowpages.model.beans.Company;
import com.stratelia.webactiv.yellowpages.model.beans.CompanyPK;
import com.stratelia.webactiv.yellowpages.model.beans.GenericContact;
import com.stratelia.webactiv.yellowpages.model.dao.jdbc.JDBCCompanyRequester;
import org.dbunit.database.IDatabaseConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class CompanyDaoTest extends AbstractJndiCase {

    private JDBCCompanyRequester companyDAO = new JDBCCompanyRequester();

    Connection con;

    public CompanyDaoTest() {
    }

    @BeforeClass
    public static void generalSetUp() throws Exception {
        baseTest = new SilverpeasJndiCase("com/stratelia/webactiv/yellowpages/model/dao/company-dataset.xml", "create-database.ddl");
        baseTest.configureJNDIDatasource();
        IDatabaseConnection databaseConnection = baseTest.getDatabaseTester().getConnection();
        executeDDL(databaseConnection, baseTest.getDdlFile());
        baseTest.getDatabaseTester().closeConnection(databaseConnection);
    }

    @Before
    public void getConnection() throws Exception {
        IDatabaseConnection dbConnection = baseTest.getConnection();
        con = dbConnection.getConnection();
        DBUtil.getInstanceForTest(con);
    }

    @After
    public void closeConnection() throws SQLException {
        con.close();
    }

    @Test
    public void testSaveCompany() throws Exception {
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

        // Check GenericContact
        GenericContact genericCompanyFromDb = companyDAO.getGenericContactFromCompanyId(con, Integer.parseInt(companyFromDb.getPk().getId()));
        assertNotNull(genericCompanyFromDb);
        assertEquals(genericCompanyFromDb.getType(), GenericContactTypeConstant.COMPANY);
    }

    @Test
    public void testUpdateCompany() throws Exception {
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

    @Test
    public void testDeleteCompany() throws Exception {
        // Company à supprimer : Microsoft
        CompanyPK pk = new CompanyPK("11", null, "yellowpages6");
        Company company = companyDAO.getCompany(con, pk);
        assertNotNull(company);

        int result = companyDAO.deleteCompany(con, company);
        assertTrue(result > 0);
        // relecture de la company en base
        Company companyFromDb = companyDAO.getCompany(con, pk);
        // comparaison des deux objets
        assertNull(companyFromDb);
    }

    @Test
    public void testAddCompanyToContact() throws Exception {
        // ajout de "Boing" au contact "Barack Obama"
        CompanyPK companyPkWaltDisney = new CompanyPK("15", null, "yellowpages6");
        Company companyBoing = companyDAO.getCompany(con, companyPkWaltDisney);
        assertNotNull(companyBoing);

        // Contact à utiliser : "Georges Washington"
        int idContactObama = 17;

        companyDAO.addCompanyToContact(con, companyBoing, idContactObama);

        // Check dans la base
        assertTrue(companyDAO.isAlreadyInContactList(con, companyBoing, idContactObama));
    }

    @Test
    public void testRemoveCompanyFromContact() throws Exception {
        // enlever "Walt Disney" du contact "Barack Obama"
        CompanyPK companyPkWaltDisney = new CompanyPK("10", null, "yellowpages6");
        Company companyWaltDisney = companyDAO.getCompany(con, companyPkWaltDisney);
        assertNotNull(companyWaltDisney);

        // Contact à utiliser : "Barak Obama"
        int idContactObama = 17;

        companyDAO.removeCompanyFromContact(con, companyWaltDisney, idContactObama);

        // Check dans la base
        assertFalse(companyDAO.isAlreadyInContactList(con, companyWaltDisney, idContactObama));
    }

    @Test
    public void testFindCompanyListByContactId() throws Exception {
        // Contact à utiliser : "Barak Obama"
        int idContactObama = 17;

        List<Company> result = companyDAO.findCompanyListByContactId(con, idContactObama);
        assertNotNull(result);
        assertEquals(result.size(), 3);
    }

}
