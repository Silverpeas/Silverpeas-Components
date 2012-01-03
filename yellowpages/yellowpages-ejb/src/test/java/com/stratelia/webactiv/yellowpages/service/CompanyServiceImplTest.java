package com.stratelia.webactiv.yellowpages.service;

import com.stratelia.webactiv.yellowpages.dao.CompanyDaoTest;
import com.stratelia.webactiv.yellowpages.model.Company;
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
import java.util.List;

import static org.junit.Assert.*;

public class CompanyServiceImplTest {

    private static CompanyService service;
    private static DataSource ds;
    private static ClassPathXmlApplicationContext context;

    @BeforeClass
    public static void setUpClass() throws Exception {
        context = new ClassPathXmlApplicationContext(
                "spring-company.xml");
        service = (CompanyService) context.getBean("companyService");
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
    public void testSaveCompany() throws Exception {
        String instanceId = "yellowpages6";
        String name = "Apple";
        String email = "steve.jobs@apple.test";
        String phone = "+ 00000000";
        String fax = "+ 11111111";

        Company result = service.saveCompany(instanceId, name, email, phone, fax);
        Company companyFromDb = service.getCompany(result.getCompanyId());
        assertNotNull(result);
        assertNotNull(companyFromDb);
        assertEquals(name, companyFromDb.getName());
        assertEquals(email, companyFromDb.getEmail());
        assertEquals(phone, companyFromDb.getPhone());
        assertEquals(fax, companyFromDb.getFax());
    }

    @Test
    public void testDeleteCompany() throws Exception {
        // Recup de la compagnie "RIM"
        int companyRIM = 6;
        service.deleteCompany(companyRIM);

        Company result = service.getCompany(6);
        assertNull(result);
    }

    @Test
    public void testGetCompany() throws Exception {
        // Recup de la company 4 = General electric
        int id = 4;
        String name = "General Electric";
        Company result = service.getCompany(id);
        assertNotNull(result);
        assertEquals(result.getName(), name);
    }

    @Test
    public void testAddCompanyToContact() throws Exception {
        String name = "American Air Linges";
        String email = "ael@test.com";
        String instanceId = "yellowpages6";
        String phone = "911";
        String fax = "911";
        Company newCompany = service.saveCompany(instanceId,name,email,phone,fax);

        int contactId = 14; // Abraham Lincoln

        service.addContactToCompany(newCompany.getCompanyId(), contactId);

        List<Company> list = service.findCompanyListByContactId(contactId);
        assertNotNull(list);
        assertTrue(list.size() == 1);
        assertEquals(list.get(0).getName(), name);
        assertEquals(list.get(0).getEmail(), email);
    }

    @Test
    public void testFindCompanyListByContactId() throws Exception {
        int contactId = 17; // Barak Obama
        List<Company> list = service.findCompanyListByContactId(contactId);
        assertNotNull(list);
        // nombre d'élements avec ENABLED = true
        assertTrue(list.size() == 2);
    }
}
