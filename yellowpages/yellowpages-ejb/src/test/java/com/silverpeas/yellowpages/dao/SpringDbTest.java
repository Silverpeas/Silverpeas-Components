package com.silverpeas.yellowpages.dao;

import org.dbunit.DataSourceDatabaseTester;
import org.dbunit.IDatabaseTester;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.sql.DataSource;

/**
 * Convenient super class for tests using the Spring-configured datasource initialized with DbUnit
 */
@ContextConfiguration(locations= "/spring-company.xml")
public abstract class SpringDbTest extends AbstractJUnit4SpringContextTests {
    private static final FlatXmlDataSetBuilder DATASET_BUILDER = new FlatXmlDataSetBuilder();
    private static final ReplacementDataSet DATA_SET;
    private static final String DATASET_PATH = "/com/silverpeas/yellowpages/dao/company-dataset.xml";

    static{
        try {
            DATA_SET = new ReplacementDataSet(DATASET_BUILDER.build(
                    SpringDbTest.class.getResourceAsStream(
                            DATASET_PATH)));
        } catch (DataSetException e) {
            throw new AssertionError();
        }
        DATA_SET.addReplacementObject("[NULL]", null);
    }

    private IDatabaseTester databaseTester;

    @Before
    public void beforeTest() throws Exception {
        databaseTester.onSetup();
    }

    @After
    public void afterTest() throws Exception{
        databaseTester.onTearDown();
    }

    @Autowired
    public void setDataSource(DataSource dataSource) throws Exception {
        this.databaseTester = initDatabaseTester(dataSource);
    }

    public static IDatabaseTester initDatabaseTester(DataSource dataSource) throws Exception {
        IDatabaseTester databaseTester = new DataSourceDatabaseTester(dataSource){
            public IDatabaseConnection getConnection() throws Exception
            {
                IDatabaseConnection res = super.getConnection();
                res.getConfig().setProperty("http://www.dbunit.org/properties/datatypeFactory",new H2DataTypeFactory());
                return res;
            }
        };
        databaseTester.setTearDownOperation(DatabaseOperation.DELETE_ALL);
        databaseTester.setSetUpOperation(DatabaseOperation.INSERT);
        databaseTester.setDataSet(DATA_SET);
        return databaseTester;
    }

}
