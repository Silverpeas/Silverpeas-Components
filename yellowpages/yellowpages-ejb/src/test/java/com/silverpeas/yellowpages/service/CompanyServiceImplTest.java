package com.silverpeas.yellowpages.service;

import com.silverpeas.yellowpages.dao.CompanyDaoTest;
import com.silverpeas.yellowpages.model.Company;
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
        context = new ClassPathXmlApplicationContext("spring-company.xml");
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
        IDataSet dataSet = new FlatXmlDataSetBuilder().setColumnSensing(true).build(CompanyDaoTest.class.getClassLoader().getResourceAsStream("com/silverpeas/yellowpages/dao/company-dataset.xml"));
        IDatabaseConnection connection = new DatabaseConnection(ds.getConnection());
        DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
    }

    @Test
    public void testCreateCompany() throws Exception {
        String instanceId = "yellowpages6";
        String name = "Apple";
        String email = "steve.jobs@apple.test";
        String phone = "+ 00000000";
        String fax = "+ 11111111";

        Company result = service.createCompany(instanceId, name, email, phone, fax);
        Company companyFromDb = service.getCompany(result.getCompanyId());
        assertNotNull(result);
        assertNotNull(companyFromDb);
        assertEquals(name, companyFromDb.getName());
        assertEquals(email, companyFromDb.getEmail());
        assertEquals(phone, companyFromDb.getPhone());
        assertEquals(fax, companyFromDb.getFax());
    }

    @Test
    public void testUpdateExistingCompany() throws Exception {
        // Recup de la company 6 = Boeing
        int id = 6;
        String name = "Boeing";
        Company result = service.getCompany(id);
        assertNotNull(result);
        assertEquals(name, result.getName());

        // Modif du nom de la company
        String newName = "Boeing Corporated";
        Company newResult = service.saveCompany(result.getCompanyId(), newName, result.getEmail(), result.getPhone(), result.getFax());
        assertNotNull(newResult);
        assertEquals(result.getCompanyId(), newResult.getCompanyId());
        assertEquals(result.getEmail(), newResult.getEmail());
        assertEquals(result.getPhone(), newResult.getPhone());
        assertEquals(result.getFax(), newResult.getFax());
        // Test du changement de nom
        assertEquals(newName, newResult.getName());
    }

    @Test
    public void testDeleteCompany() throws Exception {
        // Recup de la compagnie "RIM"
        int companyRIM = 7;
        service.deleteCompany(companyRIM);

        Company result = service.getCompany(7);
        assertNull(result);
    }

    @Test
    public void testGetCompany() throws Exception {
        // Recup de la company 5 = General electric
        int id = 5;
        String name = "General Electric";
        Company result = service.getCompany(id);
        assertNotNull(result);
        assertEquals(name, result.getName());
    }

    @Test
    public void testAddCompanyToContact() throws Exception {
        String name = "American Air Lines";
        String email = "aal@test.com";
        String instanceId = "yellowpages6";
        String phone = "911";
        String fax = "911";
        Company newCompany = service.createCompany(instanceId, name, email, phone, fax);

        int contactId = 14; // Abraham Lincoln

        service.addContactToCompany(newCompany.getCompanyId(), contactId);

        List<Company> list = service.findCompanyListByContactId(contactId);
        assertNotNull(list);
        assertTrue(list.size() == 1);
        assertEquals(name, list.get(0).getName());
        assertEquals(email, list.get(0).getEmail());
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
