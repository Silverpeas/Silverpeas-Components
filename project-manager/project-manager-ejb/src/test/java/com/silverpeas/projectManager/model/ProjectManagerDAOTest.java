/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.projectManager.model;

import java.io.IOException;
import javax.naming.NamingException;
import org.junit.Before;
import org.junit.BeforeClass;
import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;
import com.silverpeas.components.model.AbstractTestDao;
import com.stratelia.webactiv.util.DateUtil;

import static com.silverpeas.jcrutil.RandomGenerator.*;
import java.sql.Connection;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import static org.junit.Assert.*;

/**
 *
 * @author ehugonnet
 */
public class ProjectManagerDAOTest extends AbstractTestDao {

  public ProjectManagerDAOTest() {
  }

  @Override
  protected String getDatasetFileName() {
    return "test-projectmanager-dao-dataset.xml";
  }

  /**
   * Test of addTask method, of class ProjectManagerDAO.
   * @throws Exception
   */
  public void testAddTask() throws Exception {
    String instanceId = "projectManager100";
    TaskDetail task = new TaskDetail();
    task.setCharge(getRandomFloat());
    task.setChrono(0);
    task.setCodeProjet(getRandomString());
    task.setConsomme(getRandomFloat());
    task.setDateDebut(getOutdatedCalendar().getTime());
    task.setDateFin(getFuturCalendar().getTime());
    task.setDescription(getRandomString());
    task.setDescriptionProjet(getRandomString());
    task.setEstDecomposee(getRandomInt());
    task.setInstanceId(instanceId);
    task.setMereId(-1);
    task.setNom(getRandomString());
    task.setOrganisateurId(String.valueOf(getRandomInt()));
    task.setPreviousTaskId(-1);
    task.setRaf(getRandomFloat());
    task.setResponsableId(getRandomInt());
    task.setStatut(TaskDetail.IN_ALERT);
    Connection con = getConnection().getConnection();
    int taskId = ProjectManagerDAO.addTask(con, task);
    task.setId(taskId);
    task.setPath("/" + taskId + "/");
    TaskDetail result = ProjectManagerDAO.getTask(con, taskId);
    assertTasksAreEqual(task, result);
  }

  /**
   * Test of addResource method, of class ProjectManagerDAO.
   * @throws Exception
   */
  public void testAddResource() throws Exception {
    Connection con = getConnection().getConnection();
    int taskId = 1000;
    String instanceId = "projectManager100";
    List<TaskResourceDetail> resources = ProjectManagerDAO.getResources(con, taskId, instanceId);
    assertNotNull(resources);
    assertEquals("2 resources expected", 2, resources.size());
    TaskResourceDetail resource = new TaskResourceDetail();
    resource.setCharge(20);
    resource.setTaskId(taskId);
    resource.setInstanceId(instanceId);
    resource.setUserId("16");
    int resourceId = ProjectManagerDAO.addResource(con, resource);
    resource.setId(resourceId);
    resources = ProjectManagerDAO.getResources(con, taskId, instanceId);
    assertNotNull(resources);
    assertEquals("We are expecting 3 resources", 3, resources.size());
    for (TaskResourceDetail dbResource : resources) {
      if (dbResource.getId() == resourceId) {
        assertResourcesAreEqual(resource, dbResource);
      }
    }

  }

  /**
   * Test of updateTask method, of class ProjectManagerDAO.
   * @throws Exception
   */
  public void testUpdateTask() throws Exception {
    Connection con = getConnection().getConnection();
    int taskId = 1000;
    TaskDetail detail = ProjectManagerDAO.getTask(con, taskId);
    assertNotNull(detail);
    assertEquals("Id is incorrect", taskId, detail.getId());
    assertEquals("Avancement is incorrect", Math.round((2.8 / 10.5) * 100), detail.getAvancement());
    assertEquals("Charge is incorrect", 10.5d, detail.getCharge(), 0.01d);
    assertEquals("Chrono is incorrect", 10, detail.getChrono());
    assertEquals("Project code is incorrect", "Silverpeas", detail.getCodeProjet());
    assertEquals("Consomme is incorrect", 2.8d, detail.getConsomme(), 0.01d);
    assertEquals("Raf is incorrect", 7.7d, detail.getRaf(), 0.01d);
    assertEquals("Date de debut is incorrect", ProjectManagerDAO.dbDate2Date("2010/05/20", ""),
        detail.getDateDebut());
    assertEquals("Date de fin is incorrect", ProjectManagerDAO.dbDate2Date("2100/06/15", ""),
        detail.getDateFin());
    assertEquals("Description is incorrect", "Tache de test sans resource",
        detail.getDescription());
    assertEquals("Project description is incorrect", "Silverpeas est un portail collaboratif",
        detail.getDescriptionProjet());
    assertEquals("Est decomposee is incorrect", 0, detail.getEstDecomposee());
    assertEquals("InstanceId is incorrect", "projectManager100", detail.getInstanceId());
    assertEquals("Level is incorrect", 0, detail.getLevel());
    assertEquals("MotherId is incorrect", -1, detail.getMereId());
    assertEquals("Nom is incorrect", "Tache de Test 1", detail.getNom());
    assertEquals("Organisateur id is incorrect", 5, detail.getOrganisateurId());
    assertEquals("Path is incorrect", "/1000/", detail.getPath());
    assertEquals("Previous task is incorrect", -1, detail.getPreviousTaskId());
    assertNotNull("Null ressources", detail.getResources());
    assertEquals("2 ressources are expected", 2, detail.getResources().size());
    assertEquals("Responsable id is incorrect", 6, detail.getResponsableId());
    assertEquals("Status is incorrect", TaskDetail.IN_PROGRESS, detail.getStatut());

    detail.setCharge(getRandomFloat());
    detail.setChrono(getRandomInt(100));
    detail.setCodeProjet(getRandomString());
    detail.setConsomme(getRandomFloat());
    detail.setDateDebut(getOutdatedCalendar().getTime());
    detail.setDateDebut(getFuturCalendar().getTime());
    detail.setDescription(getRandomString());
    detail.setDescriptionProjet(getRandomString());
    detail.setEstDecomposee(0);
    detail.setLevel(getRandomInt(5));
    detail.setMereId(-1);
    detail.setNom(getRandomString());
    detail.setOrganisateurId(getRandomInt(100));
    detail.setRaf(getRandomFloat());
    detail.setResponsableId(getRandomInt(100));
    detail.setStatut(getRandomInt(TaskDetail.NOT_STARTED));
    ProjectManagerDAO.updateTask(con, detail);
    TaskDetail result = ProjectManagerDAO.getTask(con, taskId);
    assertEquals("Id is incorrect", taskId, result.getId());
    assertEquals("Charge is incorrect", detail.getCharge(), result.getCharge(), 0.01d);
    assertEquals("Chrono should not be updated", 10, result.getChrono());
    assertEquals("Project code should not be updated", "Silverpeas", result.getCodeProjet());
    assertEquals("Consomme is incorrect", detail.getConsomme(), result.getConsomme(), 0.01d);
    assertEquals("Raf is incorrect", detail.getRaf(), result.getRaf(), 0.01d);
    assertEquals("Date de debut is incorrect", DateUtil.date2SQLDate(detail.getDateDebut()),
        DateUtil.date2SQLDate(result.getDateDebut()));
    assertEquals("Date de fin is incorrect", DateUtil.date2SQLDate(detail.getDateFin()),
        DateUtil.date2SQLDate(result.getDateFin()));
    assertEquals("Description is incorrect", detail.getDescription(), result.getDescription());
    assertEquals("Project description should not be updated",
        "Silverpeas est un portail collaboratif",
        result.getDescriptionProjet());
    assertEquals("Est decomposee is incorrect", detail.getEstDecomposee(), result.getEstDecomposee());
    assertEquals("InstanceId is incorrect", "projectManager100", result.getInstanceId());
    assertEquals("Level  should not be updated", 0, result.getLevel());
    assertEquals("MotherId is incorrect", detail.getMereId(), result.getMereId());
    assertEquals("Nom is incorrect", detail.getNom(), result.getNom());
    assertEquals("Organisateur id  should not be updated", 5, result.getOrganisateurId());
    assertEquals("Path is incorrect", detail.getPath(), result.getPath());
    assertEquals("Previous task is incorrect", detail.getPreviousTaskId(),
        result.getPreviousTaskId());
    assertNotNull("Null ressources", result.getResources());
    assertEquals("2 ressources are expected", 2, result.getResources().size());
    assertEquals("Responsable id is incorrect", detail.getResponsableId(), result.getResponsableId());
    assertEquals("Status is incorrect", detail.getStatut(), result.getStatut());

  }

  /**
   * Test of deleteAllResources method, of class ProjectManagerDAO.
   * @throws Exception
   */
  public void testDeleteAllResources() throws Exception {
    Connection con = getConnection().getConnection();
    int taskId = 1010;
    String instanceId = "projectManager100";
    Collection<TaskResourceDetail> result = ProjectManagerDAO.getResources(con, taskId, instanceId);
    assertNotNull(result);
    assertEquals("We are expecting 3 resources", 3, result.size());
    Map<String, TaskResourceDetail> expectedResult = new HashMap<String, TaskResourceDetail>(3);
    expectedResult.put("510", new TaskResourceDetail(510, 1010, "3", 3, "projectManager100"));
    expectedResult.put("511", new TaskResourceDetail(511, 1010, "5", 7, "projectManager100"));
    expectedResult.put("512", new TaskResourceDetail(512, 1010, "8", 3, "projectManager100"));
    for (TaskResourceDetail resource : result) {
      TaskResourceDetail expResult = expectedResult.get(String.valueOf(resource.getId()));
      assertResourcesAreEqual(expResult, resource);
    }
    ProjectManagerDAO.deleteAllResources(con, taskId, instanceId);
    result = ProjectManagerDAO.getResources(con, taskId, instanceId);
    assertNotNull(result);
    assertEquals("We are expecting no more resources", 0, result.size());
  }

  /**
   * Test of actionEstDecomposee method, of class ProjectManagerDAO.
   * @throws Exception
   */
  public void testActionEstDecomposee() throws Exception {
    Connection con = getConnection().getConnection();
    int taskId = 1000;
    TaskDetail result = ProjectManagerDAO.getTask(con, taskId);
    assertNotNull(result);
    assertEquals("Id is incorrect", taskId, result.getId());
    assertEquals("Est decomposee is incorrect", 0, result.getEstDecomposee());
    ProjectManagerDAO.actionEstDecomposee(con, taskId, 1);
    result = ProjectManagerDAO.getTask(con, taskId);
    assertNotNull(result);
    assertEquals("Id is incorrect", taskId, result.getId());
    assertEquals("Est decomposee is incorrect", 1, result.getEstDecomposee());
    ProjectManagerDAO.actionEstDecomposee(con, taskId, 0);
    result = ProjectManagerDAO.getTask(con, taskId);
    assertNotNull(result);
    assertEquals("Id is incorrect", taskId, result.getId());
    assertEquals("Est decomposee is incorrect", 0, result.getEstDecomposee());
  }

  /**
   * Test of removeTask method, of class ProjectManagerDAO.
   * @throws Exception
   */
  public void testRemoveTask() throws Exception {
    Connection con = getConnection().getConnection();
    int id = 1000;
    TaskDetail result = ProjectManagerDAO.getTask(con, id);
    assertNotNull(result);
    ProjectManagerDAO.removeTask(con, id);
    result = ProjectManagerDAO.getTask(con, id);
    assertNull(result);
  }

  /**
   * Test of getTask method, of class ProjectManagerDAO.
   * @throws Exception
   */
  public void testGetTaskByStringId() throws Exception {
    Connection con = getConnection().getConnection();
    String id = "1000";
    TaskDetail result = ProjectManagerDAO.getTask(con, id);
    assertNotNull(result);
    assertEquals("Id is incorrect", 1000, result.getId());
    assertEquals("Avancement is incorrect", Math.round((2.8 / 10.5) * 100), result.getAvancement());
    assertEquals("Charge is incorrect", 10.5d, result.getCharge(), 0.01d);
    assertEquals("Chrono is incorrect", 10, result.getChrono());
    assertEquals("Project code is incorrect", "Silverpeas", result.getCodeProjet());
    assertEquals("Consomme is incorrect", 2.8d, result.getConsomme(), 0.01d);
    assertEquals("Raf is incorrect", 7.7d, result.getRaf(), 0.01d);
    assertEquals("Date de debut is incorrect", ProjectManagerDAO.dbDate2Date("2010/05/20", ""),
        result.getDateDebut());
    assertEquals("Date de fin is incorrect", ProjectManagerDAO.dbDate2Date("2100/06/15", ""),
        result.getDateFin());
    assertEquals("Description is incorrect", "Tache de test sans resource",
        result.getDescription());
    assertEquals("Project description is incorrect", "Silverpeas est un portail collaboratif",
        result.getDescriptionProjet());
    assertEquals("Est decomposee is incorrect", 0, result.getEstDecomposee());
    assertEquals("InstanceId is incorrect", "projectManager100", result.getInstanceId());
    assertEquals("Level is incorrect", 0, result.getLevel());
    assertEquals("MotherId is incorrect", -1, result.getMereId());
    assertEquals("Nom is incorrect", "Tache de Test 1", result.getNom());
    assertEquals("Organisateur id is incorrect", 5, result.getOrganisateurId());
    assertEquals("Path is incorrect", "/1000/", result.getPath());
    assertEquals("Previous task is incorrect", -1, result.getPreviousTaskId());
    assertNotNull("Null ressources", result.getResources());
    assertEquals("2 ressources are expected", 2, result.getResources().size());
    assertEquals("Responsable id is incorrect", 6, result.getResponsableId());
    assertEquals("Status is incorrect", TaskDetail.IN_PROGRESS, result.getStatut());
  }

  /**
   * Test of getTask method, of class ProjectManagerDAO.
   * @throws Exception
   */
  public void testGetTaskById() throws Exception {
    Connection con = getConnection().getConnection();
    int id = 1000;
    TaskDetail result = ProjectManagerDAO.getTask(con, id);
    assertNotNull(result);
    assertEquals("Id is incorrect", id, result.getId());
    assertEquals("Avancement is incorrect", Math.round((2.8 / 10.5) * 100), result.getAvancement());
    assertEquals("Charge is incorrect", 10.5d, result.getCharge(), 0.01d);
    assertEquals("Chrono is incorrect", 10, result.getChrono());
    assertEquals("Project code is incorrect", "Silverpeas", result.getCodeProjet());
    assertEquals("Consomme is incorrect", 2.8d, result.getConsomme(), 0.01d);
    assertEquals("Raf is incorrect", 7.7d, result.getRaf(), 0.01d);
    assertEquals("Date de debut is incorrect", ProjectManagerDAO.dbDate2Date("2010/05/20", ""),
        result.getDateDebut());
    assertEquals("Date de fin is incorrect", ProjectManagerDAO.dbDate2Date("2100/06/15", ""),
        result.getDateFin());
    assertEquals("Description is incorrect", "Tache de test sans resource",
        result.getDescription());
    assertEquals("Project description is incorrect", "Silverpeas est un portail collaboratif",
        result.getDescriptionProjet());
    assertEquals("Est decomposee is incorrect", 0, result.getEstDecomposee());
    assertEquals("InstanceId is incorrect", "projectManager100", result.getInstanceId());
    assertEquals("Level is incorrect", 0, result.getLevel());
    assertEquals("MotherId is incorrect", -1, result.getMereId());
    assertEquals("Nom is incorrect", "Tache de Test 1", result.getNom());
    assertEquals("Organisateur id is incorrect", 5, result.getOrganisateurId());
    assertEquals("Path is incorrect", "/1000/", result.getPath());
    assertEquals("Previous task is incorrect", -1, result.getPreviousTaskId());
    assertNotNull("Null ressources", result.getResources());
    assertEquals("2 ressources are expected", 2, result.getResources().size());
    assertEquals("Responsable id is incorrect", 6, result.getResponsableId());
    assertEquals("Status is incorrect", TaskDetail.IN_PROGRESS, result.getStatut());
  }

  /**
   * Test of getResources method, of class ProjectManagerDAO.
   * @throws Exception
   */
  public void testGetResources() throws Exception {
    Connection con = getConnection().getConnection();
    int taskId = 1010;
    String instanceId = "projectManager100";
    Collection<TaskResourceDetail> result = ProjectManagerDAO.getResources(con, taskId, instanceId);
    assertNotNull(result);
    assertEquals("We are expecting 3 resources", 3, result.size());
    Map<String, TaskResourceDetail> expectedResult = new HashMap<String, TaskResourceDetail>(3);
    expectedResult.put("510", new TaskResourceDetail(510, 1010, "3", 3, "projectManager100"));
    expectedResult.put("511", new TaskResourceDetail(511, 1010, "5", 7, "projectManager100"));
    expectedResult.put("512", new TaskResourceDetail(512, 1010, "8", 3, "projectManager100"));
    for (TaskResourceDetail resource : result) {
      TaskResourceDetail expResult = expectedResult.get(String.valueOf(resource.getId()));
      assertResourcesAreEqual(expResult, resource);
    }

  }

  /**
   * Test of getAllTasks method, of class ProjectManagerDAO.
   */
  /*
  public void testGetAllTasks() throws Exception {
  System.out.println("getAllTasks");
  Connection con = null;
  String instanceId = "";
  Filtre filtre = null;
  List expResult = null;
  List result = ProjectManagerDAO.getAllTasks(con, instanceId, filtre);
  assertEquals(
  expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail(
  "The test case is a prototype.");


  }*/
  /**
   * Test of getTasks method, of class ProjectManagerDAO.
   */
  /*
  public void testGetTasks() throws Exception {
  System.out.println("getTasks");
  Connection con = null;


  int actionId = 0;
  Filtre filtre = null;
  String instanceId = "";
  List expResult = null;
  List result = ProjectManagerDAO.getTasks(con, actionId, filtre, instanceId);
  assertEquals(
  expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail(
  "The test case is a prototype.");


  }*/
  /**
   * Test of getNextTasks method, of class ProjectManagerDAO.
   */
  /*
  public void testGetNextTasks() throws Exception {
  System.out.println("getNextTasks");
  Connection con = null;


  int taskId = 0;
  List expResult = null;
  List result = ProjectManagerDAO.getNextTasks(con, taskId);
  assertEquals(
  expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail(
  "The test case is a prototype.");


  }*/
  /**
   * Test of getMostDistantTask method, of class ProjectManagerDAO.
   */
  /*
  public void testGetMostDistantTask() throws Exception {
  System.out.println("getMostDistantTask");
  Connection con = null;
  String instanceId = "";


  int taskId = 0;
  TaskDetail expResult = null;
  TaskDetail result = ProjectManagerDAO.getMostDistantTask(con, instanceId, taskId);
  assertEquals(
  expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail(
  "The test case is a prototype.");


  }*/
  /**
   * Test of getTree method, of class ProjectManagerDAO.
   */
  /*
  public void testGetTree() throws Exception {
  System.out.println("getTree");
  Connection con = null;


  int actionId = 0;
  List expResult = null;
  List result = ProjectManagerDAO.getTree(con, actionId);
  assertEquals(
  expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail(
  "The test case is a prototype.");


  }*/
  /**
   * Test of getTasksByMotherId method, of class ProjectManagerDAO.
   */
  /*
  public void testGetTasksByMotherId() throws Exception {
  System.out.println("getTasksByMotherId");
  Connection con = null;
  String instanceId = "";


  int motherId = 0;
  Filtre filtre = null;
  List expResult = null;
  List result = ProjectManagerDAO.getTasksByMotherId(con, instanceId, motherId, filtre);
  assertEquals(
  expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail(
  "The test case is a prototype.");
  }*/
  /**
   * Test of getTasksNotCancelledByMotherId method, of class ProjectManagerDAO.
   */
  /*
  public void testGetTasksNotCancelledByMotherId() throws Exception {
  System.out.println("getTasksNotCancelledByMotherId");
  Connection con = null;
  String instanceId = "";


  int motherId = 0;
  Filtre filtre = null;
  List expResult = null;
  List result = ProjectManagerDAO.getTasksNotCancelledByMotherId(con, instanceId, motherId, filtre);
  assertEquals(
  expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail(
  "The test case is a prototype.");
  }*/
  /**
   * Test of getTasksByMotherIdAndPreviousId method, of class ProjectManagerDAO.
   */
  /*
  public void testGetTasksByMotherIdAndPreviousId() throws Exception {
  System.out.println("getTasksByMotherIdAndPreviousId");
  Connection con = null;
  String instanceId = "";


  int motherId = 0;


  int previousId = 0;
  List expResult = null;
  List result = ProjectManagerDAO.getTasksByMotherIdAndPreviousId(con, instanceId, motherId, previousId);
  assertEquals(
  expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail(
  "The test case is a prototype.");


  }*/
  /**
   * Test of getOccupationByUser method, of class ProjectManagerDAO.
   */
  public void testGetOccupationByUser() throws Exception {
    Connection con = getConnection().getConnection();
    String userId = "3";
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.YEAR, 2009);
    calend.set(Calendar.MONTH, Calendar.APRIL);
    calend.set(Calendar.DAY_OF_MONTH, 10);
    Date dateDeb = calend.getTime();
    calend.set(Calendar.YEAR, 2010);
    calend.set(Calendar.MONTH, Calendar.SEPTEMBER);
    calend.set(Calendar.DAY_OF_MONTH, 15);
    Date dateFin = calend.getTime();
    int expResult = 13;
    int result = ProjectManagerDAO.getOccupationByUser(con, userId, dateDeb, dateFin);
    assertEquals(expResult, result);
    userId = "5";
    expResult = 24;
    result = ProjectManagerDAO.getOccupationByUser(con, userId, dateDeb, dateFin);
    assertEquals(expResult, result);
  }

  /**
   * Test of getOccupationByUser method, of class ProjectManagerDAO.
   */
  public void testGetOccupationByUserExludingTask() throws Exception {
    Connection con = getConnection().getConnection();
    String userId = "3";
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.YEAR, 2009);
    calend.set(Calendar.MONTH, Calendar.APRIL);
    calend.set(Calendar.DAY_OF_MONTH, 10);
    Date dateDeb = calend.getTime();
    calend.set(Calendar.YEAR, 2010);
    calend.set(Calendar.MONTH, Calendar.SEPTEMBER);
    calend.set(Calendar.DAY_OF_MONTH, 15);
    Date dateFin = calend.getTime();
    int expResult = 3;
    int excludedTaskId = 1001;
    int result =
        ProjectManagerDAO.getOccupationByUser(con, userId, dateDeb, dateFin, excludedTaskId);
    assertEquals(expResult, result);
    userId = "5";
    expResult = 14;
    result = ProjectManagerDAO.getOccupationByUser(con, userId, dateDeb, dateFin, excludedTaskId);
    assertEquals(expResult, result);
  }

  /**
   * Test of date2DBDate method, of class ProjectManagerDAO.
   */
  public void assertResourcesAreEqual(TaskResourceDetail expected, TaskResourceDetail result) {
    assertEquals("Id should be the same", expected.getId(), result.getId());
    assertEquals("Charge should be the same", expected.getCharge(), result.getCharge());
    assertEquals("InstanceId should be the same", expected.getInstanceId(), result.getInstanceId());
    assertEquals("Occupation should be the same", expected.getOccupation(), result.getOccupation());
    assertEquals("TaskId should be the same", expected.getTaskId(), result.getTaskId());
    assertEquals("UserId should be the same", expected.getUserId(), result.getUserId());
  }

  public void assertTasksAreEqual(TaskDetail expected, TaskDetail result) {
    assertEquals("Id should be the same", expected.getId(), result.getId());
    assertEquals("Avancement should be the same", expected.getAvancement(), result.getAvancement());
    assertEquals("Charge should be the same", expected.getCharge(), result.getCharge(), 0.01d);
    assertEquals("Chrono should be the same", expected.getChrono(), result.getChrono());
    assertEquals("Code projet should be the same", expected.getCodeProjet(), result.getCodeProjet());
    assertEquals("Consomme should be the same", expected.getConsomme(), result.getConsomme(), 0.01d);
    assertEquals("Date debut should be the same", DateUtil.date2SQLDate(expected.getDateDebut()),
        DateUtil.date2SQLDate(result.getDateDebut()));
    assertEquals("Date fin should be the same", DateUtil.date2SQLDate(expected.getDateFin()),
        DateUtil.date2SQLDate(result.getDateFin()));
    assertEquals("Description should be the same", expected.getDescription(),
        result.getDescription());
    assertEquals("Description projet should be the same", expected.getDescriptionProjet(),
        result.getDescriptionProjet());
    assertEquals("EstDecomposee should be the same", expected.getEstDecomposee(), result.
        getEstDecomposee());
    assertEquals("InstanceId should be the same", expected.getInstanceId(), result.getInstanceId());
    assertEquals("Mother Id should be the same", expected.getMereId(), result.getMereId());
    assertEquals("Nom should be the same", expected.getNom(), result.getNom());
    assertEquals("OrganisateurId should be the same", expected.getOrganisateurId(),
        result.getOrganisateurId());
    assertEquals("Path should be the same", expected.getPath(), result.getPath());
    assertEquals("Previous taskId should be the same", expected.getPreviousTaskId(), result.
        getPreviousTaskId());
    assertEquals("ResponsableId should be the same", expected.getResponsableId(), result.
        getResponsableId());
    assertEquals("Raf should be the same", expected.getRaf(), result.getRaf(), 0.01d);
    assertEquals("Statut should be the same", expected.getStatut(), result.getStatut());
  }

  @Override
  protected String getTableCreationFileName() {
    return "create-database.sql";
  }
}
