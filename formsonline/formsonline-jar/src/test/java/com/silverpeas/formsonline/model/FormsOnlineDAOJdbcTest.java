package com.silverpeas.formsonline.model;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.silverpeas.formsonline.model.mock.FormsOnlineDAOJdbcMock;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;


/**
 *
 * @author ebonnet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-formsonline-embbed-datasource.xml"})
public class FormsOnlineDAOJdbcTest {
  
  private FormsOnlineDAO dao;
  
  @Inject
  private DataSource dataSource;

  public FormsOnlineDAOJdbcTest() {
  }
  
  @Before
  public void generalSetUp() throws Exception { 
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(
        FormsOnlineDAOJdbcTest.class.getClassLoader().getResourceAsStream(
        "com/silverpeas/formsonline/model/forms-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
    // Use the dao mock for database connection purpose
    dao = new FormsOnlineDAOJdbcMock(dataSource.getConnection());
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }
  
  @Before
  public void setUp() {
    // Bootstrap database in order to keep the same connection all test long !!!
    try {
      DBUtil.getInstanceForTest(dataSource.getConnection());
    } catch (SQLException e) {
    }
  }
  
  @After
  public void tearDown() {
  }

  /**
   * Test of createForm method, of class FormsOnlineDAOJdbc.
   */
  @Test
  public void testCreateForm() throws Exception {
    FormDetail formDetail = new FormDetail();
    formDetail.setInstanceId("formsOnline15");
    formDetail.setCreatorId("0");
    formDetail.setName("Demande de construction de salle...");
    formDetail.setState(FormInstance.STATE_UNREAD);
    formDetail.setTitle("Titre");
    formDetail.setXmlFormName("descriptif_salle.xml");
    FormDetail result = dao.createForm(formDetail);
    assertEquals(1002, result.getId());
  }

  /**
   * Test of getForm method, of class FormsOnlineDAOJdbc.
   */
  @Test
  public void testGetForm() throws Exception {
    String instanceId = "formsOnline100";
    int formId = 1000;
    FormDetail expResult = getFormDetailExpectedResult();
    
    FormDetail result = dao.getForm(new FormPK(formId, instanceId));
    assertEquals(expResult.equals(result), true);
  }

  private FormDetail getFormDetailExpectedResult() throws ParseException {
    FormDetail expResult = new FormDetail();
    expResult.setId(1000);
    expResult.setCreatorId("0");
    expResult.setAlreadyUsed(true);
    expResult.setCreationDate(DateUtil.parse("2012/01/09"));
    expResult.setDescription("Formulaire de description d'une salle");
    expResult.setInstanceId("formsOnline100");
    expResult.setName("Référencement des salles");
    expResult.setState(FormInstance.STATE_UNREAD);
    expResult.setTitle("Titre de mon formulaire en ligne");
    expResult.setXmlFormName("descriptif_salle.xml");
    return expResult;
  }
  
  
  /**
   * Test of findAllForms method, of class FormsOnlineDAOJdbc.
   */
  @Test
  public void testFindAllForms() throws Exception {
    String instanceId = "formsOnline100";
    List<FormDetail> result = dao.findAllForms(instanceId);
    assertEquals(result.size(), 2);
  }
  
  /**
   * Test of updateForm method, of class FormsOnlineDAOJdbc.
   */
  @Test
  public void testUpdateForm() throws Exception {
    FormDetail curForm = getFormDetailExpectedResult();
    curForm.setState(FormInstance.STATE_READ);
    dao.updateForm(curForm);
    // Need to create another connection because the last one was closed inside updateForm method
    dao = new FormsOnlineDAOJdbcMock(dataSource.getConnection());
    FormDetail updatedForm = dao.getForm(curForm.getPK());
    assertEquals(curForm.equals(updatedForm), true);
  }

}
