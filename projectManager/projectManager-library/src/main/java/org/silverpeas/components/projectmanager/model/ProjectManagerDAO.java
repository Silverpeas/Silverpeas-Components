/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * Created on 25 oct. 2004
 *
 */
package org.silverpeas.components.projectmanager.model;

import org.silverpeas.core.exception.UtilException;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.util.DateUtil;

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
import java.util.function.UnaryOperator;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * @author neysseri
 */
public class ProjectManagerDAO {

  private static final String PROJECTMANAGER_TASKS_TABLENAME = "sc_projectmanager_tasks";
  private static final String PROJECTMANAGER_RESOURCES_TABLENAME = "sc_projectmanager_resources";

  /**
   * Hidden constructor.
   */
  private ProjectManagerDAO() {
  }

  private static int getChrono(Connection con, String instanceId)
      throws SQLException {
    StringBuilder query = new StringBuilder();
    query.append("SELECT MAX(chrono) FROM ").append(PROJECTMANAGER_TASKS_TABLENAME).append(
        " WHERE instanceId = ? ");

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
      throws SQLException {
    //insertStatement query
    StringBuilder insertStatement = new StringBuilder();
    insertStatement.append("INSERT INTO ").append(PROJECTMANAGER_TASKS_TABLENAME);
    insertStatement
        .append("(id, mereid, chrono, nom, description, organisateurid, responsableid, ");
    insertStatement.append("charge, consomme, raf, avancement, statut, datedebut, datefin, ");
    insertStatement.append("codeprojet, descriptionprojet, estdecomposee, instanceid, path, ");
    insertStatement.append("previousid) VALUES ( ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ");
    insertStatement.append("? , ? , ? , ?, ?, ? , ? , ? )");

    //execute the query
    int id = executeQueryAddTask(con, task, insertStatement);

    // insertion des resources
    if (task.getResources() != null) {
      Collection<TaskResourceDetail> tasks = task.getResources();
      for (TaskResourceDetail resource : tasks) {
        resource.setTaskId(id);
        resource.setInstanceId(task.getInstanceId());
        addResource(con, resource);
      }
    }
    return id;
  }

  private static int executeQueryAddTask(Connection con, TaskDetail task, StringBuilder insertStatement)
      throws SQLException {

    PreparedStatement prepStmt = null;
    int id = -1;
    try {
      prepStmt = con.prepareStatement(insertStatement.toString());

      id = DBUtil.getNextId(PROJECTMANAGER_TASKS_TABLENAME, "id");
      prepStmt.setInt(1, id);
      prepStmt.setInt(2, task.getMereId());
      if (task.getMereId() == -1) {
        prepStmt.setInt(3, 0);
      } else {
        prepStmt.setInt(3, getChrono(con, task.getInstanceId()));
      }
      prepStmt.setString(4, task.getNom());
      prepStmt.setString(5, task.getDescription());
      prepStmt.setInt(6, task.getOrganisateurId());
      prepStmt.setInt(7, task.getResponsableId());
      prepStmt.setFloat(8, task.getCharge());
      prepStmt.setFloat(9, task.getConsomme());
      prepStmt.setFloat(10, task.getRaf());
      prepStmt.setInt(11, task.getAvancement());
      prepStmt.setInt(12, task.getStatut());
      prepStmt.setString(13, DateUtil.date2SQLDate(task.getDateDebut()));
      if (task.getDateFin() != null) {
        prepStmt.setString(14, DateUtil.date2SQLDate(task.getDateFin()));
      } else {
        prepStmt.setString(14, "9999/99/99");
      }
      prepStmt.setString(15, task.getCodeProjet());
      prepStmt.setString(16, task.getDescriptionProjet());
      prepStmt.setInt(17, task.getEstDecomposee());
      prepStmt.setString(18, task.getInstanceId());
      prepStmt.setString(19, task.getPath() + id + "/");
      prepStmt.setInt(20, task.getPreviousTaskId());

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
    return id;
  }

  public static int addResource(Connection con, TaskResourceDetail resource)
      throws SQLException {
    StringBuilder insertStatement = new StringBuilder();
    insertStatement.append("INSERT INTO ").append(PROJECTMANAGER_RESOURCES_TABLENAME);
    insertStatement
        .append("(id , taskid, resourceid, charge, instanceid) VALUES ( ? , ? , ? , ? , ? )");
    PreparedStatement prepStmt = null;
    int id = -1;
    try {
      prepStmt = con.prepareStatement(insertStatement.toString());
      id = DBUtil.getNextId(PROJECTMANAGER_RESOURCES_TABLENAME, "id");
      prepStmt.setInt(1, id);
      prepStmt.setInt(2, resource.getTaskId());
      prepStmt.setInt(3, Integer.parseInt(resource.getUserId()));
      prepStmt.setInt(4, resource.getCharge());
      prepStmt.setString(5, resource.getInstanceId());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
    return id;
  }

  public static void updateTask(Connection con, TaskDetail task)
      throws SQLException, UtilException {
    StringBuilder updateQuery = new StringBuilder();
    updateQuery.append("UPDATE ").append(PROJECTMANAGER_TASKS_TABLENAME);
    updateQuery.append(" SET nom = ? , description = ? , responsableId = ? , charge = ? , ");
    updateQuery.append("consomme = ? , raf = ? , avancement = ? , statut = ? , dateDebut = ? , ");
    updateQuery.append("dateFin = ? , previousId = ? WHERE id = ? ");

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
      prepStmt.setString(9, DateUtil.date2SQLDate(task.getDateDebut()));
      if (task.getDateFin() != null) {
        prepStmt.setString(10, DateUtil.date2SQLDate(task.getDateFin()));
      } else {
        prepStmt.setString(10, "9999/99/99");
      }
      prepStmt.setInt(11, task.getPreviousTaskId());

      prepStmt.setInt(12, task.getId());

      prepStmt.executeUpdate();

      // mise à jour des resources
      deleteAllResources(con, task.getId(), task.getInstanceId());

      if (task.getResources() != null) {
        Iterator<TaskResourceDetail> it = task.getResources().iterator();
        while (it.hasNext()) {
          TaskResourceDetail resource = it.next();
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
    StringBuilder deleteStatement = new StringBuilder();
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
    StringBuilder updateQuery = new StringBuilder();
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
    StringBuilder deleteStatement = new StringBuilder();
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

  /**
   * Deletes all the tasks and their associated resources in the specified project manager.
   * @param con a connection to the data source into which are stored the tasks and the resources.
   * @param instanceId the unique identifier of a ProjectManager instance.
   * @throws SQLException if an error occurs while deleting the tasks and the associated resources.
   */
  public static void removeAllTasks(Connection con, String instanceId) throws SQLException {
    final String tasks = "delete from " + PROJECTMANAGER_TASKS_TABLENAME + " where instanceId = ?";
    final String resources =
        "delete from " + PROJECTMANAGER_RESOURCES_TABLENAME + " where instanceId = ?";
    try(PreparedStatement deletion = con.prepareStatement(resources)) {
      deletion.setString(1, instanceId);
      deletion.execute();
    }
    try (PreparedStatement deletion = con.prepareStatement(tasks)) {
      deletion.setString(1, instanceId);
      deletion.execute();
    }
  }

  public static TaskDetail getTask(Connection con, String id)
      throws SQLException {
    return getTask(con, Integer.parseInt(id));
  }

  public static TaskDetail getTask(Connection con, int id) throws SQLException {
    TaskDetail task = null;
    StringBuilder query = new StringBuilder();
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
        task.setResources(getResources(con, task.getId(), task.getInstanceId()));
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return task;
  }

  public static List<TaskResourceDetail> getResources(Connection con, int taskId,
      String instanceId) throws SQLException {
    List<TaskResourceDetail> resources = new ArrayList<>();
    StringBuilder query = new StringBuilder();
    query.append("SELECT id, taskId, resourceId, charge, instanceId FROM ");
    query.append(PROJECTMANAGER_RESOURCES_TABLENAME);
    query.append(" WHERE taskId = ? AND instanceId = ? ");

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
      DBUtil.close(rs, stmt);
    }
    return resources;
  }

  public static List<TaskDetail> getAllTasks(Connection con, String instanceId,
      Filtre filtre) throws SQLException {
    List<TaskDetail> tasks = new ArrayList<>();
    StringBuilder query = new StringBuilder();
    query.append("SELECT * FROM ").append(PROJECTMANAGER_TASKS_TABLENAME);
    query.append(" WHERE instanceId = ? ");
    if (filtre != null) {
      String filtreSQL = getSQL(filtre);
      if (filtreSQL.length() > 0) {
        query.append("AND ").append(filtreSQL);
      }
    }
    query.append(" ORDER BY path ASC");

    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(query.toString());
      stmt.setString(1, instanceId);
      rs = stmt.executeQuery();
      while (rs.next()) {
        TaskDetail task = getTaskDetailFromResultset(rs);
        task.setResources(getResources(con, task.getId(), task.getInstanceId()));
        tasks.add(task);
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return tasks;
  }

  public static List<TaskDetail> getNextTasks(Connection con, int taskId)
      throws SQLException {
    return listTasksSortedByStartDate(con, q -> q.where("previousId = ?", taskId));
  }

  public static TaskDetail getMostDistantTask(Connection con, int taskId) throws SQLException {
    StringBuilder query = new StringBuilder();
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
        task.setResources(getResources(con, task.getId(), task.getInstanceId()));
        return task;
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return null;
  }

  /**
   * @param con a Connection to database
   * @param actionId the root of the tree
   * @return the tree - a List of TaskDetail
   * @throws SQLException
   */
  public static List<TaskDetail> getTree(Connection con, int actionId) throws SQLException {
    List<TaskDetail> tasks = new ArrayList<>();
    StringBuilder query = new StringBuilder();
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
        task.setResources(getResources(con, task.getId(), task.getInstanceId()));
        tasks.add(task);
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return tasks;
  }

  /**
   * Gets the tasks by mother identifier if no filter, or filtered tasks on filter if any (so
   * not on mother id in that case).
   * @param con the current database connection.
   * @param instanceId the identifier of the component instance.
   * @param motherId the identifier of the parent task.
   * @return a list of {@link TaskDetail} instances.
   * @throws SQLException on database error.
   */
  public static List<TaskDetail> getTasksByMotherId(Connection con, String instanceId, int motherId)
      throws SQLException {
    return listInstanceTasksSortedByStartDate(con, instanceId, q -> q.and("mereId = ?", motherId));
  }

  /**
   * Centralizing the task listing.
   * @param con the current database connection.
   * @param instanceId the identifier of the component instance.
   * @param sqlQueryOp the operator permitting to precise the query.
   * @return a list of {@link TaskDetail} instances.
   * @throws SQLException on database error.
   */
  private static List<TaskDetail> listInstanceTasksSortedByStartDate(Connection con, String instanceId,
      UnaryOperator<JdbcSqlQuery> sqlQueryOp) throws SQLException {
    return listTasksSortedByStartDate(con, q -> sqlQueryOp.apply(q.where("instanceId = ?", instanceId)));
  }

  /**
   * Centralizing the task listing.
   * @param con the current database connection.
   * @param sqlQueryOp the operator permitting to precise the query. First clause MUST be a WHERE
   * one.
   * @return a list of {@link TaskDetail} instances.
   * @throws SQLException on database error.
   */
  private static List<TaskDetail> listTasksSortedByStartDate(Connection con,
      UnaryOperator<JdbcSqlQuery> sqlQueryOp) throws SQLException {
    return sqlQueryOp.apply(JdbcSqlQuery.select("*")
        .from(PROJECTMANAGER_TASKS_TABLENAME))
        .orderBy("dateDebut ASC, id ASC")
        .executeWith(con, rs -> {
          final TaskDetail task = getTaskDetailFromResultset(rs);
          task.setResources(getResources(con, task.getId(), task.getInstanceId()));
          return task;
        });
  }

  public static List<TaskDetail> getTasksByMotherIdAndPreviousId(Connection con,
      String instanceId, int motherId, int previousId) throws SQLException {
    return listInstanceTasksSortedByStartDate(con, instanceId, q -> q.and("mereId = ?", motherId).and("previousId = ?", previousId));
  }

  public static int getOccupationByUser(Connection con, String userId,
      Date dateDeb, Date dateFin) throws SQLException {
    return getOccupationByUser(con, userId, dateDeb, dateFin, -1);
  }

  public static int getOccupationByUser(Connection con, String userId,
      Date dateDeb, Date dateFin, int excludedTaskId) throws SQLException {
    StringBuilder query = new StringBuilder();
    query.append("SELECT SUM(res.charge) AS somme FROM ").append(PROJECTMANAGER_TASKS_TABLENAME);
    query.append(" task, ").append(PROJECTMANAGER_RESOURCES_TABLENAME).append(" res");
    query.append(" WHERE res.taskId = task.id");
    query
        .append(" AND ((dateDebut <= ? OR dateFin <= ?) AND (dateDebut >= ? OR dateFin >= ?)) AND resourceId = ? ");
    if (excludedTaskId > -1) {
      query.append(" AND task.id <> ? ");
    }
    query.append("GROUP BY res.resourceId");




    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(query.toString());
      stmt.setString(1, DateUtil.date2SQLDate(dateFin));
      stmt.setString(2, DateUtil.date2SQLDate(dateFin));
      stmt.setString(3, DateUtil.date2SQLDate(dateDeb));
      stmt.setString(4, DateUtil.date2SQLDate(dateDeb));
      stmt.setInt(5, Integer.parseInt(userId));
      if (excludedTaskId != -1) {
        stmt.setInt(6, excludedTaskId);
      }

      rs = stmt.executeQuery();
      if (rs.next()) {
        return rs.getInt("somme");
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

    return new TaskResourceDetail(id, taskId, userId, charge, instanceId);
  }

  private static String getSQL(Filtre filtre) {
    StringBuilder sql = new StringBuilder();

    if (isNotEmpty(filtre.getActionFrom())) {
      sql.append(" chrono >= ").append(filtre.getActionFrom());
    }

    if (isNotEmpty(filtre.getActionTo())) {
      andClause(sql);
      sql.append(" chrono <= ").append(filtre.getActionTo());
    }

    if (isNotEmpty(filtre.getCodeProjet())) {
      andClause(sql);
      sql.append(" codeProjet = ").append(filtre.getCodeProjet());
    }

    if (isNotEmpty(filtre.getDescProjet())) {
      andClause(sql);
      sql.append(" descriptionProjet like '%").append(filtre.getDescProjet()).append("%' ");
    }

    if (isNotEmpty(filtre.getActionNom())) {
      andClause(sql);
      sql.append(" nom like '%").append(filtre.getActionNom()).append("%' ");
    }

    if (isNotEmpty(filtre.getStatut()) && !"-1".equals(filtre.getStatut())) {
      andClause(sql);
      sql.append(" statut = ").append(filtre.getStatut());
    }

    if (filtre.getDateDebutFrom() != null) {
      andClause(sql);
      sql.append(" dateDebut >= '").append(
          DateUtil.date2SQLDate(filtre.getDateDebutFrom())).append("' ");
    }

    if (filtre.getDateDebutTo() != null) {
      andClause(sql);
      sql.append(" dateDebut <= '").append(
          DateUtil.date2SQLDate(filtre.getDateDebutTo())).append("' ");
    }

    if (filtre.getDateFinFrom() != null) {
      andClause(sql);
      sql.append(" dateFin >= '").append(
          DateUtil.date2SQLDate(filtre.getDateFinFrom())).append("' ");
    }

    if (filtre.getDateFinTo() != null) {
      andClause(sql);
      sql.append(" dateFin <= '").append(
          DateUtil.date2SQLDate(filtre.getDateFinTo())).append("' ");
    }

    if (filtre.getRetard() != null && !"-1".equals(filtre.getRetard())) {
      andClause(sql);
      Date today = new Date();
      if ("1".equals(filtre.getRetard())) {
        // les tasks en retard
        sql.append("( dateFin < '").append(DateUtil.date2SQLDate(today)).append(
            "' AND avancement = 100 ) ");
      } else {
        // les tasks qui ne sont pas en retard
        sql.append("( dateFin >= '").append(DateUtil.date2SQLDate(today)).append(
            "' AND avancement = 100 ) ");
      }
    }

    if (filtre.getAvancement() != null && !"-1".equals(filtre.getAvancement())) {
      andClause(sql);
      if ("1".equals(filtre.getAvancement())) {
        // les tasks terminées
        sql.append(" avancement = 100 ");
      } else {
        // les tasks non terminées
        sql.append(" avancement < 100 ");
      }
    }

    if (isNotEmpty(filtre.getResponsableId())) {
      andClause(sql);
      sql.append(" responsableId = ").append(filtre.getResponsableId());
    }

    return sql.toString();
  }

  /**
   * Adds an AND clause (SQL) the given sql is not empty.
   * @param sql the sql builder.
   */
  private static void andClause(final StringBuilder sql) {
    if (sql.length() > 0) {
      sql.append(" AND ");
    }
  }

  public static Date dbDate2Date(String dbDate, String fieldName)
      throws SQLException {
    Date date = null;
    try {
      // the date format used in database to represent a date
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
      date = formatter.parse(dbDate);
    } catch (ParseException e) {
      throw new SQLException("ProjectManagerDAO : dbDate2Date(" + fieldName
          + ") : format unknown " + e.toString(), e);
    }
    return date;
  }
}
