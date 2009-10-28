/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * Created on 25 oct. 2004
 *
 */
package com.silverpeas.projectManager.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.UtilException;

/**
 * @author neysseri
 *
 */
public class ProjectManagerDAO {

  // the date format used in database to represent a date
  private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
  private final static String PROJECTMANAGER_TASKS_TABLENAME = "SC_ProjectManager_Tasks";
  private final static String PROJECTMANAGER_RESOURCES_TABLENAME = "SC_ProjectManager_Resources";

  private static int getChrono(Connection con, String instanceId)
      throws SQLException {
    StringBuffer query = new StringBuffer(128);
    query.append("select max(chrono) ");
    query.append("from ").append(PROJECTMANAGER_TASKS_TABLENAME);
    query.append(" where instanceId = ? ");

    PreparedStatement stmt = null;
    ResultSet rs = null;
    int result = -1;

    try {
      stmt = con.prepareStatement(query.toString());
      stmt.setString(1, instanceId);
      rs = stmt.executeQuery();
      if (rs.next()) {
        result = rs.getInt(1);
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return result + 1;
  }

  public static int addTask(Connection con, TaskDetail task)
      throws SQLException, UtilException {
    SilverTrace.debug("projectManager", "ProjectManagerDAO.addTask()",
        "root.MSG_GEN_ENTER_METHOD", task.toString());
    StringBuffer insertStatement = new StringBuffer(128);
    insertStatement.append("insert into ").append(
        PROJECTMANAGER_TASKS_TABLENAME);
    insertStatement
        .append(" values ( ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ?, ?, ? , ? , ? )");
    PreparedStatement prepStmt = null;

    int id = -1;
    try {
      prepStmt = con.prepareStatement(insertStatement.toString());

      id = DBUtil.getNextId(PROJECTMANAGER_TASKS_TABLENAME, "id");
      prepStmt.setInt(1, id);
      prepStmt.setInt(2, task.getMereId());
      if (task.getMereId() == -1) // Project
        prepStmt.setInt(3, 0);
      else
        prepStmt.setInt(3, getChrono(con, task.getInstanceId()));
      prepStmt.setString(4, task.getNom());
      prepStmt.setString(5, task.getDescription());
      prepStmt.setInt(6, task.getOrganisateurId());
      prepStmt.setInt(7, task.getResponsableId());
      prepStmt.setFloat(8, task.getCharge());
      prepStmt.setFloat(9, task.getConsomme());
      prepStmt.setFloat(10, task.getRaf());
      prepStmt.setInt(11, task.getAvancement());
      prepStmt.setInt(12, task.getStatut());
      prepStmt.setString(13, date2DBDate(task.getDateDebut()));
      if (task.getDateFin() != null)
        prepStmt.setString(14, date2DBDate(task.getDateFin()));
      else
        prepStmt.setString(14, "9999/99/99");
      prepStmt.setString(15, task.getCodeProjet());
      prepStmt.setString(16, task.getDescriptionProjet());
      prepStmt.setInt(17, task.getEstDecomposee());
      prepStmt.setString(18, task.getInstanceId());
      prepStmt.setString(19, task.getPath() + id + "/");
      prepStmt.setInt(20, task.getPreviousTaskId());

      prepStmt.executeUpdate();

      // insertion des resources
      if (task.getResources() != null) {
        Iterator it = task.getResources().iterator();
        while (it.hasNext()) {
          TaskResourceDetail resource = (TaskResourceDetail) it.next();
          resource.setTaskId(id);
          resource.setInstanceId(task.getInstanceId());
          addResource(con, resource);
        }
      }
    } finally {
      DBUtil.close(prepStmt);
    }
    return id;
  }

  public static int addResource(Connection con, TaskResourceDetail resource)
      throws SQLException, UtilException {
    SilverTrace.debug("projectManager", "ProjectManagerDAO.addResources()",
        "root.MSG_GEN_ENTER_METHOD", resource.toString());
    StringBuffer insertStatement = new StringBuffer(128);
    insertStatement.append("insert into ").append(
        PROJECTMANAGER_RESOURCES_TABLENAME);
    insertStatement.append(" values ( ? , ? , ? , ? , ? )");
    PreparedStatement prepStmt = null;

    int id = -1;
    try {
      prepStmt = con.prepareStatement(insertStatement.toString());

      id = DBUtil.getNextId(PROJECTMANAGER_RESOURCES_TABLENAME, "id");
      prepStmt.setInt(1, id);
      prepStmt.setInt(2, resource.getTaskId());
      // prepStmt.setString(3, resource.getUserId());
      prepStmt.setInt(3, Integer.parseInt(resource.getUserId()));
      prepStmt.setInt(4, resource.getCharge());
      prepStmt.setString(5, resource.getInstanceId());

      prepStmt.executeUpdate();
    } finally {
    }
    return id;
  }

  public static void updateTask(Connection con, TaskDetail task)
      throws SQLException, UtilException {
    SilverTrace.debug("projectManager", "ProjectManagerDAO.updateAction()",
        "root.MSG_GEN_ENTER_METHOD", task.toString());

    StringBuffer updateQuery = new StringBuffer(128);
    updateQuery.append("update ").append(PROJECTMANAGER_TASKS_TABLENAME);
    updateQuery
        .append(" set nom = ? , description = ? , responsableId = ? , charge = ? , consomme = ? , raf = ? , avancement = ? , statut = ? , dateDebut = ? , dateFin = ? , previousId = ? ");
    updateQuery.append(" where id = ? ");

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(updateQuery.toString());
      prepStmt.setString(1, task.getNom());
      prepStmt.setString(2, task.getDescription());
      prepStmt.setInt(3, task.getResponsableId());
      prepStmt.setFloat(4, task.getCharge());
      prepStmt.setFloat(5, task.getConsomme());
      prepStmt.setFloat(6, task.getRaf());
      prepStmt.setInt(7, task.getAvancement());
      prepStmt.setInt(8, task.getStatut());
      prepStmt.setString(9, date2DBDate(task.getDateDebut()));
      if (task.getDateFin() != null)
        prepStmt.setString(10, date2DBDate(task.getDateFin()));
      else
        prepStmt.setString(10, "9999/99/99");
      prepStmt.setInt(11, task.getPreviousTaskId());

      prepStmt.setInt(12, task.getId());

      prepStmt.executeUpdate();

      // mise à jour des resources
      deleteAllResources(con, task.getId(), task.getInstanceId());

      if (task.getResources() != null) {
        Iterator it = task.getResources().iterator();
        while (it.hasNext()) {
          TaskResourceDetail resource = (TaskResourceDetail) it.next();
          resource.setTaskId(task.getId());
          resource.setInstanceId(task.getInstanceId());
          addResource(con, resource);
        }
      }
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void deleteAllResources(Connection con, int taskId,
      String instanceId) throws SQLException {
    StringBuffer deleteStatement = new StringBuffer(128);
    deleteStatement.append("delete from ").append(
        PROJECTMANAGER_RESOURCES_TABLENAME).append(
        " where taskId = ? and instanceId = ? ");
    PreparedStatement stmt = null;

    try {
      stmt = con.prepareStatement(deleteStatement.toString());
      stmt.setInt(1, taskId);
      stmt.setString(2, instanceId);
      stmt.executeUpdate();
    } finally {
      DBUtil.close(stmt);
    }
  }

  public static void actionEstDecomposee(Connection con, int id,
      int estDecomposee) throws SQLException {
    SilverTrace.debug("projectManager",
        "ProjectManagerDAO.actionEstDecomposee()", "root.MSG_GEN_ENTER_METHOD",
        "id = " + id + ", estDecomposee = " + estDecomposee);

    StringBuffer updateQuery = new StringBuffer(128);
    updateQuery.append("update ").append(PROJECTMANAGER_TASKS_TABLENAME);
    updateQuery.append(" set estDecomposee = ? ");
    updateQuery.append(" where id = ? ");

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(updateQuery.toString());
      prepStmt.setInt(1, estDecomposee);
      prepStmt.setInt(2, id);

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void removeTask(Connection con, int id) throws SQLException {
    StringBuffer deleteStatement = new StringBuffer(128);
    deleteStatement.append("delete from ").append(
        PROJECTMANAGER_TASKS_TABLENAME).append(" where id = ? ");
    PreparedStatement stmt = null;

    try {
      stmt = con.prepareStatement(deleteStatement.toString());
      stmt.setInt(1, id);
      stmt.executeUpdate();
    } finally {
      DBUtil.close(stmt);
    }
  }

  public static TaskDetail getTask(Connection con, String id)
      throws SQLException {
    return getTask(con, new Integer(id).intValue());
  }

  public static TaskDetail getTask(Connection con, int id) throws SQLException {
    TaskDetail task = null;
    StringBuffer query = new StringBuffer(128);
    query.append("select * ");
    query.append("from ").append(PROJECTMANAGER_TASKS_TABLENAME);
    query.append(" where id = ? ");

    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(query.toString());
      stmt.setInt(1, id);
      rs = stmt.executeQuery();
      if (rs.next()) {
        task = getTaskDetailFromResultset(rs);
        // récupération des resources
        task
            .setResources(getResources(con, task.getId(), task.getInstanceId()));
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return task;
  }

  public static Collection getResources(Connection con, int taskId,
      String instanceId) throws SQLException {
    Collection resources = new ArrayList();
    StringBuffer query = new StringBuffer(128);
    query.append("select * ");
    query.append("from ").append(PROJECTMANAGER_RESOURCES_TABLENAME);
    query.append(" where taskId = ?  and instanceId = ? ");

    SilverTrace.info("projectManager", "ProjectManagerDAO.getResources()",
        "root.MSG_GEN_PARAM_VALUE", query.toString());
    SilverTrace.info("projectManager", "ProjectManagerDAO.getResources()",
        "root.MSG_GEN_PARAM_VALUE", "taskId = " + taskId + " instanceId = "
            + instanceId);

    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(query.toString());
      stmt.setInt(1, taskId);
      stmt.setString(2, instanceId);
      rs = stmt.executeQuery();
      while (rs.next()) {
        TaskResourceDetail resource = getTaskResourceDetailFromResultset(rs);
        resources.add(resource);
      }
    } finally {
    }
    return resources;
  }

  public static List getAllTasks(Connection con, String instanceId,
      Filtre filtre) throws SQLException {
    List tasks = new ArrayList();
    StringBuffer query = new StringBuffer(128);
    query.append("select * ");
    query.append("from ").append(PROJECTMANAGER_TASKS_TABLENAME);
    query.append(" where instanceId = ? ");
    if (filtre != null) {
      String filtreSQL = getSQL(filtre);
      if (filtreSQL.length() > 0)
        query.append("and ").append(filtreSQL);
    }
    query.append("order by path ASC");

    SilverTrace.info("projectManager", "ProjectManagerDAO.getAllActions()",
        "root.MSG_GEN_PARAM_VALUE", query.toString());

    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(query.toString());
      stmt.setString(1, instanceId);
      rs = stmt.executeQuery();
      while (rs.next()) {
        TaskDetail task = getTaskDetailFromResultset(rs);
        task
            .setResources(getResources(con, task.getId(), task.getInstanceId()));
        tasks.add(task);
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return tasks;
  }

  public static List getTasks(Connection con, int actionId, Filtre filtre,
      String instanceId) throws SQLException {
    List tasks = new ArrayList();
    StringBuffer query = new StringBuffer(128);
    query.append("select * ");
    query.append("from ").append(PROJECTMANAGER_TASKS_TABLENAME);
    query.append(" where mereId = ? ");
    query.append(" and instanceId = ? ");
    if (filtre != null) {
      String filtreSQL = getSQL(filtre);
      if (filtreSQL.length() > 0)
        query.append("and ").append(filtreSQL);
    }
    query.append("order by dateDebut ASC");

    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(query.toString());
      stmt.setInt(1, actionId);
      stmt.setString(2, instanceId);
      rs = stmt.executeQuery();
      while (rs.next()) {
        TaskDetail task = getTaskDetailFromResultset(rs);
        task
            .setResources(getResources(con, task.getId(), task.getInstanceId()));
        tasks.add(task);
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return tasks;
  }

  public static List getNextTasks(Connection con, int taskId)
      throws SQLException {
    List tasks = new ArrayList();
    StringBuffer query = new StringBuffer(128);
    query.append("select * ");
    query.append("from ").append(PROJECTMANAGER_TASKS_TABLENAME);
    query.append(" where previousId = ? ");
    query.append("order by dateDebut ASC");

    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(query.toString());
      stmt.setInt(1, taskId);
      rs = stmt.executeQuery();
      while (rs.next()) {
        TaskDetail task = getTaskDetailFromResultset(rs);
        task
            .setResources(getResources(con, task.getId(), task.getInstanceId()));
        tasks.add(task);
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return tasks;
  }

  public static TaskDetail getMostDistantTask(Connection con,
      String instanceId, int taskId) throws SQLException {
    StringBuffer query = new StringBuffer(128);
    query.append("select * ");
    query.append("from ").append(PROJECTMANAGER_TASKS_TABLENAME);
    query.append(" where mereId = ? ");
    query.append(" order by dateFin DESC");

    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(query.toString());
      stmt.setInt(1, taskId);
      rs = stmt.executeQuery();
      if (rs.next()) {
        TaskDetail task = getTaskDetailFromResultset(rs);
        task
            .setResources(getResources(con, task.getId(), task.getInstanceId()));
        return task;
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return null;
  }

  /**
   * @param con
   *          a Connection to database
   * @param actionId
   *          the root of the tree
   * @return the tree - a List of TaskDetail
   * @throws SQLException
   */
  public static List getTree(Connection con, int actionId) throws SQLException {
    List tasks = new ArrayList();
    StringBuffer query = new StringBuffer(128);
    query.append("select * ");
    query.append("from ").append(PROJECTMANAGER_TASKS_TABLENAME);
    query.append(" where path like '%/").append(actionId).append("/%' ");
    query.append("order by path ASC");

    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(query.toString());
      rs = stmt.executeQuery();
      while (rs.next()) {
        TaskDetail task = getTaskDetailFromResultset(rs);
        task
            .setResources(getResources(con, task.getId(), task.getInstanceId()));
        tasks.add(task);
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return tasks;
  }

  public static List getTasksByMotherId(Connection con, String instanceId,
      int motherId, Filtre filtre) throws SQLException {
    List tasks = new ArrayList();
    StringBuffer query = new StringBuffer(128);
    query.append("select * ");
    query.append("from ").append(PROJECTMANAGER_TASKS_TABLENAME);
    query.append(" where instanceId = ? ");
    query.append(" and mereId = ? ");
    if (filtre != null) {
      String filtreSQL = getSQL(filtre);
      if (filtreSQL.length() > 0)
        query.append("and ").append(filtreSQL);
    }
    query.append("order by dateDebut ASC");

    SilverTrace.info("projectManager",
        "ProjectManagerDAO.getTasksByMotherId()", "root.MSG_GEN_ENTER_METHOD",
        query.toString());

    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(query.toString());
      stmt.setString(1, instanceId);
      stmt.setInt(2, motherId);

      rs = stmt.executeQuery();
      while (rs.next()) {
        TaskDetail task = getTaskDetailFromResultset(rs);
        task
            .setResources(getResources(con, task.getId(), task.getInstanceId()));
        tasks.add(task);
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return tasks;
  }

  public static List getTasksNotCancelledByMotherId(Connection con,
      String instanceId, int motherId, Filtre filtre) throws SQLException {
    List tasks = new ArrayList();
    StringBuffer query = new StringBuffer(128);
    query.append("select * ");
    query.append("from ").append(PROJECTMANAGER_TASKS_TABLENAME);
    query.append(" where instanceId = ? ");
    query.append(" and mereId = ? ");
    if (filtre != null) {
      String filtreSQL = getSQL(filtre);
      if (filtreSQL.length() > 0)
        query.append("and ").append(filtreSQL);
    }

    // si on demande toutes les taches quelque soit le statut,
    // il faut enlever de la liste les taches dont le statut est "Abandonné"
    // car elles n'apparaissent pas dans le diagramme de Gantt
    if (filtre == null || filtre.getStatut() == null
        || filtre.getStatut().equals("-1")) {
      query.append(" and statut != " + TaskDetail.CANCELLED);
    }

    query.append("order by dateDebut ASC");

    SilverTrace.info("projectManager",
        "ProjectManagerDAO.getTasksNotCancelledByMotherId()",
        "root.MSG_GEN_ENTER_METHOD", query.toString());

    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(query.toString());
      stmt.setString(1, instanceId);
      stmt.setInt(2, motherId);

      rs = stmt.executeQuery();
      while (rs.next()) {
        TaskDetail task = getTaskDetailFromResultset(rs);
        task
            .setResources(getResources(con, task.getId(), task.getInstanceId()));
        tasks.add(task);
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return tasks;
  }

  public static List getTasksByMotherIdAndPreviousId(Connection con,
      String instanceId, int motherId, int previousId) throws SQLException {
    List tasks = new ArrayList();
    StringBuffer query = new StringBuffer(128);
    query.append("select * ");
    query.append("from ").append(PROJECTMANAGER_TASKS_TABLENAME);
    query.append(" where mereId = ? ");
    query.append(" and previousId = ? ");
    query.append(" and instanceId = ? ");
    query.append("order by dateDebut ASC");

    SilverTrace.info("projectManager",
        "ProjectManagerDAO.getTasksByMotherIdAndPreviousId()",
        "root.MSG_GEN_ENTER_METHOD", query.toString());

    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(query.toString());
      stmt.setInt(1, motherId);
      stmt.setInt(2, previousId);
      stmt.setString(3, instanceId);

      rs = stmt.executeQuery();
      while (rs.next()) {
        TaskDetail task = getTaskDetailFromResultset(rs);
        task
            .setResources(getResources(con, task.getId(), task.getInstanceId()));
        tasks.add(task);
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return tasks;
  }

  public static int getOccupationByUser(Connection con, String userId,
      Date dateDeb, Date dateFin) throws SQLException {
    return getOccupationByUser(con, userId, dateDeb, dateFin, -1);
  }

  public static int getOccupationByUser(Connection con, String userId,
      Date dateDeb, Date dateFin, int excludedTaskId) throws SQLException {
    StringBuffer query = new StringBuffer(128);
    query.append("select sum(res.charge) as somme ");
    query.append("from ").append(PROJECTMANAGER_TASKS_TABLENAME).append(
        " task, ");
    query.append(PROJECTMANAGER_RESOURCES_TABLENAME).append(" res");
    query.append(" where res.taskId = task.id");
    if (excludedTaskId != -1)
      query.append(" and task.id <> ?");
    query
        .append(" and ((dateDebut <= ? or dateFin <= ?) and (dateDebut >= ? or dateFin >= ?)) and resourceId = ? ");
    query.append("Group by res.resourceId");

    SilverTrace.info("projectManager",
        "ProjectManagerDAO.getTasksByMotherId()", "root.MSG_GEN_ENTER_METHOD",
        query.toString());
    SilverTrace.info("projectManager",
        "ProjectManagerDAO.getTasksByMotherId()", "root.MSG_GEN_ENTER_METHOD",
        "dateDebut < " + dateDeb + " and dateFin > " + dateFin);

    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(query.toString());
      int i = 1;
      if (excludedTaskId != -1)
        stmt.setInt(i++, excludedTaskId);

      stmt.setString(i++, date2DBDate(dateFin));
      stmt.setString(i++, date2DBDate(dateFin));
      stmt.setString(i++, date2DBDate(dateDeb));
      stmt.setString(i++, date2DBDate(dateDeb));
      stmt.setString(i++, userId);

      rs = stmt.executeQuery();
      if (rs.next()) {
        int somme = rs.getInt("somme");
        return somme;
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return 0;
  }

  private static TaskDetail getTaskDetailFromResultset(ResultSet rs)
      throws SQLException {
    int id = rs.getInt("id");
    int mereId = rs.getInt("mereId");
    int chrono = rs.getInt("chrono");
    String nom = rs.getString("nom");
    String description = rs.getString("description");
    int organisateurId = rs.getInt("organisateurId");
    int responsableId = rs.getInt("responsableId");
    float charge = rs.getFloat("charge");
    float consomme = rs.getFloat("consomme");
    float raf = rs.getFloat("raf");
    int statut = rs.getInt("statut");
    Date dateDebut = dbDate2Date(rs.getString("dateDebut"), "dateDebut");
    Date dateFin = dbDate2Date(rs.getString("dateFin"), "dateFin");
    String codeProjet = rs.getString("codeProjet");
    String descriptionProjet = rs.getString("descriptionProjet");
    int estDecomposee = rs.getInt("estDecomposee");
    String instanceId = rs.getString("instanceId");
    String path = rs.getString("path");
    int previousId = rs.getInt("previousId");

    TaskDetail task = new TaskDetail(id, mereId, chrono, nom, description,
        organisateurId, responsableId, charge, consomme, raf, statut,
        dateDebut, dateFin, codeProjet, descriptionProjet, estDecomposee,
        instanceId, path);
    task.setPreviousTaskId(previousId);
    return task;
  }

  private static TaskResourceDetail getTaskResourceDetailFromResultset(
      ResultSet rs) throws SQLException {
    int id = rs.getInt("id");
    int taskId = rs.getInt("taskId");
    String userId = Integer.toString(rs.getInt("resourceId"));
    int charge = rs.getInt("charge");
    String instanceId = rs.getString("instanceId");

    TaskResourceDetail resourceDetail = new TaskResourceDetail(id, taskId,
        userId, charge, instanceId);

    return resourceDetail;
  }

  private static String getSQL(Filtre filtre) {
    StringBuffer sql = new StringBuffer();

    if (filtre.getActionFrom() != null && filtre.getActionFrom().length() > 0)
      sql.append(" chrono >= ").append(filtre.getActionFrom());

    if (filtre.getActionTo() != null && filtre.getActionTo().length() > 0) {
      if (sql.length() > 0)
        sql.append(" AND ");
      sql.append(" chrono <= ").append(filtre.getActionTo());
    }

    if (filtre.getCodeProjet() != null && filtre.getCodeProjet().length() > 0) {
      if (sql.length() > 0)
        sql.append(" AND ");
      sql.append(" codeProjet = ").append(filtre.getCodeProjet());
    }

    if (filtre.getDescProjet() != null && filtre.getDescProjet().length() > 0) {
      if (sql.length() > 0)
        sql.append(" AND ");
      sql.append(" descriptionProjet like '%").append(filtre.getDescProjet())
          .append("%' ");
    }

    if (filtre.getActionNom() != null && filtre.getActionNom().length() > 0) {
      if (sql.length() > 0)
        sql.append(" AND ");
      sql.append(" nom like '%").append(filtre.getActionNom()).append("%' ");
    }

    if (filtre.getStatut() != null && !filtre.getStatut().equals("-1")) {
      if (sql.length() > 0)
        sql.append(" AND ");
      sql.append(" statut = ").append(filtre.getStatut());
    }

    if (filtre.getDateDebutFrom() != null) {
      if (sql.length() > 0)
        sql.append(" AND ");
      sql.append(" dateDebut >= '").append(
          ProjectManagerDAO.date2DBDate(filtre.getDateDebutFrom()))
          .append("' ");
    }

    if (filtre.getDateDebutTo() != null) {
      if (sql.length() > 0)
        sql.append(" AND ");
      sql.append(" dateDebut <= '").append(
          ProjectManagerDAO.date2DBDate(filtre.getDateDebutTo())).append("' ");
    }

    if (filtre.getDateFinFrom() != null) {
      if (sql.length() > 0)
        sql.append(" AND ");
      sql.append(" dateFin >= '").append(
          ProjectManagerDAO.date2DBDate(filtre.getDateFinFrom())).append("' ");
    }

    if (filtre.getDateFinTo() != null) {
      if (sql.length() > 0)
        sql.append(" AND ");
      sql.append(" dateFin <= '").append(
          ProjectManagerDAO.date2DBDate(filtre.getDateFinTo())).append("' ");
    }

    if (filtre.getRetard() != null && !filtre.getRetard().equals("-1")) {
      if (sql.length() > 0)
        sql.append(" AND ");
      Date today = new Date();
      if (filtre.getRetard().equals("1")) {
        // les tasks en retard
        sql.append("( dateFin < '")
            .append(ProjectManagerDAO.date2DBDate(today)).append(
                "' AND avancement = 100 ) ");
      } else {
        // les tasks qui ne sont pas en retard
        sql.append("( dateFin >= '").append(
            ProjectManagerDAO.date2DBDate(today)).append(
            "' AND avancement = 100 ) ");
      }
    }

    if (filtre.getAvancement() != null && !filtre.getAvancement().equals("-1")) {
      if (sql.length() > 0)
        sql.append(" AND ");
      if (filtre.getAvancement().equals("1")) {
        // les tasks terminées
        sql.append(" avancement = 100 ");
      } else {
        // les tasks non terminées
        sql.append(" avancement < 100 ");
      }
    }

    if (filtre.getResponsableId() != null
        && filtre.getResponsableId().length() > 0) {
      if (sql.length() > 0)
        sql.append(" AND ");
      sql.append(" responsableId = ").append(filtre.getResponsableId());
    }

    return sql.toString();
  }

  public static String date2DBDate(Date date) {
    String dbDate = formatter.format(date);
    return dbDate;
  }

  private static Date dbDate2Date(String dbDate, String fieldName)
      throws SQLException {
    Date date = null;
    try {
      date = formatter.parse(dbDate);
    } catch (ParseException e) {
      throw new SQLException("ProjectManagerDAO : dbDate2Date(" + fieldName
          + ") : format unknown " + e.toString());
    }
    return date;
  }
}