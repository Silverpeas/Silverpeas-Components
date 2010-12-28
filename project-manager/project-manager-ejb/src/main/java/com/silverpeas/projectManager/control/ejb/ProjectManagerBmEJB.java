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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.projectManager.control.ejb;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.silverpeas.projectManager.model.Filtre;
import com.silverpeas.projectManager.model.HolidayDetail;
import com.silverpeas.projectManager.model.ProjectManagerCalendarDAO;
import com.silverpeas.projectManager.model.ProjectManagerDAO;
import com.silverpeas.projectManager.model.ProjectManagerRuntimeException;
import com.silverpeas.projectManager.model.TaskDetail;
import com.silverpeas.projectManager.model.TaskPK;
import com.silverpeas.comment.service.CommentService;
import com.silverpeas.comment.service.CommentServiceFactory;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.calendar.backbone.TodoBackboneAccess;
import com.stratelia.webactiv.calendar.backbone.TodoDetail;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;

/**
 * @author
 */
public class ProjectManagerBmEJB implements ProjectManagerBmBusinessSkeleton, SessionBean {
  private static final long serialVersionUID = -1707326539051696107L;

  private String dbName = JNDINames.SILVERPEAS_DATASOURCE;
  private CommentService commentController = null;

  /**
   * Gets a comment service.
   * @return a CommentService instance.
   */
  private CommentService getCommentService() {
    if (commentController == null) {
      commentController = CommentServiceFactory.getFactory().getCommentService();
    }
    return commentController;
  }

  @Override
  public List<TaskDetail> getProjects(String instanceId) throws RemoteException {
    SilverTrace.info("projectManager", "ProjectManagerBmEJB.getProjects()",
        "root.MSG_GEN_ENTER_METHOD", "instanceId=" + instanceId);
    Connection con = getConnection();
    List<TaskDetail> projects = null;
    try {
      projects = ProjectManagerDAO.getTasksByMotherId(con, instanceId, -1, null);
    } catch (Exception re) {
      throw new ProjectManagerRuntimeException("ProjectManagerBmEJB.getProjects()",
          SilverpeasRuntimeException.ERROR, "projectManager.GETTING_PROJECTS_FAILED",
          "instanceId = " + instanceId, re);
    } finally {
      freeConnection(con);
    }
    return projects;
  }

  @Override
  public List<TaskDetail> getTasksByMotherId(String instanceId, int motherId)
      throws RemoteException {
    SilverTrace.info("projectManager",
        "ProjectManagerBmEJB.getTasksByMotherId()",
        "root.MSG_GEN_ENTER_METHOD", "instanceId=" + instanceId);
    return getTasksByMotherId(instanceId, motherId, null);
  }

  @Override
  public List<TaskDetail> getTasksByMotherId(String instanceId, int motherId, Filtre filtre)
      throws RemoteException {
    SilverTrace.info("projectManager",
        "ProjectManagerBmEJB.getTasksByMotherId()",
        "root.MSG_GEN_ENTER_METHOD", "instanceId=" + instanceId);
    Connection con = getConnection();
    List<TaskDetail> tasks = null;
    try {
      tasks = ProjectManagerDAO.getTasksByMotherId(con, instanceId, motherId, filtre);
    } catch (Exception re) {
      throw new ProjectManagerRuntimeException(
          "ProjectManagerBmEJB.getTasksByMotherId()",
          SilverpeasRuntimeException.ERROR,
          "projectManager.GETTING_TASKS_FAILED", "instanceId = " + instanceId,
          re);
    } finally {
      freeConnection(con);
    }
    return tasks;
  }

  @Override
  public List<TaskDetail> getTasksNotCancelledByMotherId(String instanceId, int motherId,
      Filtre filtre) throws RemoteException {
    SilverTrace.info("projectManager",
        "ProjectManagerBmEJB.getTasksNotCancelledByMotherId()",
        "root.MSG_GEN_ENTER_METHOD", "instanceId=" + instanceId);
    Connection con = getConnection();
    List<TaskDetail> tasks = null;
    try {
      tasks = ProjectManagerDAO.getTasksNotCancelledByMotherId(con, instanceId, motherId, filtre);
    } catch (Exception re) {
      throw new ProjectManagerRuntimeException(
          "ProjectManagerBmEJB.getTasksByMotherId()",
          SilverpeasRuntimeException.ERROR,
          "projectManager.GETTING_TASKS_FAILED", "instanceId = " + instanceId,
          re);
    } finally {
      freeConnection(con);
    }
    return tasks;
  }

  @Override
  public List<TaskDetail> getTasksByMotherIdAndPreviousId(String instanceId, int motherId,
      int previousId) throws RemoteException {
    SilverTrace.info("projectManager",
        "ProjectManagerBmEJB.getTasksByMotherIdAndPreviousId()",
        "root.MSG_GEN_ENTER_METHOD", "instanceId=" + instanceId + ", motherId="
            + motherId + ", previousId=" + previousId);
    Connection con = getConnection();
    List<TaskDetail> tasks = null;
    try {
      tasks =
          ProjectManagerDAO.getTasksByMotherIdAndPreviousId(con, instanceId, motherId, previousId);
    } catch (Exception re) {
      throw new ProjectManagerRuntimeException(
          "ProjectManagerBmEJB.getTasksByMotherIdAndPreviousId()",
          SilverpeasRuntimeException.ERROR,
          "projectManager.GETTING_TASKS_FAILED", "instanceId = " + instanceId,
          re);
    } finally {
      freeConnection(con);
    }
    return tasks;
  }

  @Override
  public List<TaskDetail> getAllTasks(String instanceId, Filtre filtre)
      throws RemoteException {
    SilverTrace.info("projectManager", "ProjectManagerBmEJB.getAllTasks()",
        "root.MSG_GEN_ENTER_METHOD", "instanceId=" + instanceId);
    Connection con = getConnection();
    List<TaskDetail> tasks = null;
    try {
      tasks = ProjectManagerDAO.getAllTasks(con, instanceId, filtre);
    } catch (Exception re) {
      throw new ProjectManagerRuntimeException("ProjectManagerBmEJB.getAllTasks()",
          SilverpeasRuntimeException.ERROR, "projectManager.GETTING_TASKS_FAILED", "instanceId = " +
              instanceId, re);
    } finally {
      freeConnection(con);
    }
    return tasks;
  }

  @Override
  public TaskDetail getTask(int id) throws RemoteException {
    SilverTrace.info("projectManager", "ProjectManagerBmEJB.getTask()",
        "root.MSG_GEN_ENTER_METHOD", "id = " + id);
    Connection con = getConnection();
    TaskDetail task = null;
    try {
      task = ProjectManagerDAO.getTask(con, id);
    } catch (Exception re) {
      throw new ProjectManagerRuntimeException("ProjectManagerBmEJB.getTask()",
          SilverpeasRuntimeException.ERROR,
          "projectManager.GETTING_TASK_FAILED", "id = " + id, re);
    } finally {
      freeConnection(con);
    }
    return task;
  }

  @Override
  public TaskDetail getTaskByTodoId(String todoId) throws RemoteException {
    SilverTrace.info("projectManager", "ProjectManagerBmEJB.getTaskByTodoId()",
        "root.MSG_GEN_ENTER_METHOD", "todoId = " + todoId);
    Connection con = getConnection();
    TaskDetail task = null;
    String actionId = null;
    try {
      // get todo
      TodoDetail todo = getTodo(todoId);

      // get task associated to it
      actionId = todo.getExternalId();
      task = ProjectManagerDAO.getTask(con, actionId);
    } catch (Exception re) {
      throw new ProjectManagerRuntimeException(
          "ProjectManagerBmEJB.getTaskByTodoId()",
          SilverpeasRuntimeException.ERROR,
          "projectManager.GETTING_TASK_FAILED", "actionId = " + actionId, re);
    } finally {
      freeConnection(con);
    }
    return task;
  }

  @Override
  public TaskDetail getMostDistantTask(String instanceId, int taskId)
      throws RemoteException {
    SilverTrace.info("projectManager",
        "ProjectManagerBmEJB.getMostDistantTask()",
        "root.MSG_GEN_ENTER_METHOD", "taskId = " + taskId);
    Connection con = getConnection();
    TaskDetail task = null;
    try {
      task = ProjectManagerDAO.getMostDistantTask(con, instanceId, taskId);
    } catch (Exception re) {
      throw new ProjectManagerRuntimeException(
          "ProjectManagerBmEJB.getMostDistantTask()",
          SilverpeasRuntimeException.ERROR,
          "projectManager.GETTING_TASK_FAILED", "taskId = " + taskId, re);
    } finally {
      freeConnection(con);
    }
    return task;
  }

  @Override
  public int addTask(TaskDetail task) throws RemoteException {
    SilverTrace.info("projectManager", "ProjectManagerBmEJB.addTask()",
        "root.MSG_GEN_ENTER_METHOD", "task=" + task.toString());
    Connection con = getConnection();
    int id = -1;
    try {
      // insertion de la task en BdD
      if (task.getAvancement() == 100) {
        task.setStatut(TaskDetail.COMPLETE);
      }
      id = ProjectManagerDAO.addTask(con, task);
      task.setId(id);

      if (task.getMereId() != -1) {
        // la tache mere est decomposée
        ProjectManagerDAO.actionEstDecomposee(con, task.getMereId(), 1);
      }

      // modification de sa tache mère s'il en existe une
      updateChargesMotherTask(con, task);

      // insertion de la tache correspondante dans le gestionnaire de taches du
      // responsable
      addTodo(task);

      // indexation de la task
      createIndex(task);

      if (task.getMereId() != -1) {
        // alerte du responsable
        alertResource(task, true);
      }
    } catch (Exception re) {
      throw new ProjectManagerRuntimeException("ProjectManagerBmEJB.addTask()",
          SilverpeasRuntimeException.ERROR,
          "projectManager.CREATING_TASK_FAILED", "task = " + task.toString(),
          re);
    } finally {
      freeConnection(con);
    }
    return id;
  }

  @Override
  public void removeTask(int id, String instanceId) throws RemoteException {
    SilverTrace.info("projectManager", "ProjectManagerBmEJB.removeTask()",
        "root.MSG_GEN_ENTER_METHOD", "id = " + id + ", instanceId="
            + instanceId);
    Connection con = getConnection();
    try {
      TaskDetail actionASupprimer = ProjectManagerDAO.getTask(con, id);

      // Supprime toutes les sous taches (à n'importe quel niveau)
      List<TaskDetail> tree = ProjectManagerDAO.getTree(con, id);
      TaskDetail task = null;
      for (int t = 0; t < tree.size(); t++) {
        task = tree.get(t);
        removeTask(con, task.getId(), task.getInstanceId());
      }

      // La tâche mère a t-elle d'autres taches filles. Est-elle toujours
      // décomposée ?
      List<TaskDetail> actionsSoeur = ProjectManagerDAO.getTree(con, actionASupprimer
          .getMereId());
      if (actionsSoeur.size() == 1) {
        // La task mère n'a qu'une sous task. Celle que l'on va supprimer.
        // Elle ne va donc plus être décomposée
        ProjectManagerDAO.actionEstDecomposee(con,
            actionASupprimer.getMereId(), 0);
      }

      // Cette tâche est-elle la tâche précédente d'autres tâches
      List<TaskDetail> nextTasks = ProjectManagerDAO.getNextTasks(con, id);
      TaskDetail nextTask = null;
      for (int n = 0; n < nextTasks.size(); n++) {
        nextTask = nextTasks.get(n);
        nextTask.setPreviousTaskId(-1);
        ProjectManagerDAO.updateTask(con, nextTask);
      }

      // modification de sa tache mère s'il en existe une
      updateChargesMotherTask(con, task);
    } catch (Exception re) {
      throw new ProjectManagerRuntimeException(
          "ProjectManagerBmEJB.removeTask()", SilverpeasRuntimeException.ERROR,
          "projectManager.DELETING_TASK_FAILED", "id = " + id, re);
    } finally {
      freeConnection(con);
    }
  }

  private void removeTask(Connection con, int id, String instanceId)
      throws Exception {
    SilverTrace.info("projectManager",
        "ProjectManagerBmEJB.removeTask(Connection)",
        "root.MSG_GEN_ENTER_METHOD", "id = " + id + ", instanceId="
            + instanceId);

    // suppression de la tâche en BdD
    ProjectManagerDAO.removeTask(con, id);

    // suppression de la tâche associée
    removeTodo(id, instanceId);

    // supprime les fichiers joint à la tâche
    TaskPK taskPK = new TaskPK(id, instanceId);
    AttachmentController.deleteAttachmentByCustomerPK(taskPK);

    // supprime les commentaires de la tâche
    getCommentService().deleteAllCommentsOnPublication(taskPK);

    // suppression de l'index
    removeIndex(id, instanceId);
  }

  @Override
  public void updateTask(TaskDetail task, String userId) throws RemoteException {
    SilverTrace.info("projectManager", "ProjectManagerBmEJB.updateTask()",
        "root.MSG_GEN_ENTER_METHOD", "taskId=" + task.getId());
    Connection con = getConnection();
    try {
      Date beginDate = task.getDateDebut();
      Date endDate = task.getDateFin();

      // Récupération des jours non travaillés
      List<Date> holidays = getHolidayDates(task.getInstanceId());

      Calendar calendar = Calendar.getInstance();

      // quelles sont les tâches liées à la tâche modifiée ? Ce sont :
      // - soit des tâches suivantes (ie tâches qui ont comme précédence la
      // tâche modifiée) - niveau N
      // - soit des sous tâches (sans précédence) de la tâche modifiée - niveau
      // N-1

      // on commence par récupérer les tâches suivantes
      List<TaskDetail> nextTasks = ProjectManagerDAO.getNextTasks(con, task.getId());

      // détecte les tâches qui doivent être décalées
      TaskDetail linkedTask = null;
      TaskDetail motherTask = null;

      Date beginDateLinked = null;
      Date endDateLinked = null;

      float charge = 0;
      Calendar calendar2 = Calendar.getInstance();
      boolean updateMother = false;

      boolean isModifBeginDate = false;
      boolean isModifEndDate = false;
      Date saveBeginDate = null;
      Date saveEndDate = null;

      for (int t = 0; t < nextTasks.size(); t++) {
        isModifBeginDate = false;
        isModifEndDate = false;

        linkedTask = nextTasks.get(t);

        beginDateLinked = linkedTask.getDateDebut();
        saveBeginDate = beginDateLinked;

        // vérifie si la date de début n'est pas
        // un jour travaillé
        calendar.setTime(beginDateLinked);
        while (holidays.contains(beginDateLinked)) {
          calendar.add(Calendar.DATE, 1);
          beginDateLinked = calendar.getTime();
          linkedTask.setDateDebut(beginDateLinked);
        }

        endDateLinked = linkedTask.getDateFin();
        saveEndDate = endDateLinked;

        if (endDate.equals(endDateLinked) || endDate.after(endDateLinked)) {
          // La date de fin de la tâche précédente est supérieure ou égale à la
          // tâche liéée
          // cette tâche doit être décalée

          // calcul de la nouvelle date de début (= date fin + 1)
          beginDateLinked = getBeginDate(calendar, endDate, holidays);
          linkedTask.setDateDebut(beginDateLinked);
        }

        // calcul de la nouvelle date de fin (date début + charge)
        endDateLinked = processEndDate(linkedTask, calendar, holidays);
        linkedTask.setDateFin(endDateLinked);

        // regarder si les dates sont modifiées
        if (!beginDateLinked.equals(saveBeginDate)) {
          isModifBeginDate = true;
        }
        if (!endDateLinked.equals(saveEndDate)) {
          isModifBeginDate = true;
        }

        // si on est dans un cas de modif de date, faire la mise à jour
        // seulement si les dates changent
        if (isModifBeginDate || isModifEndDate) {
          updateTask(linkedTask, userId);
        }

        // on traite maintenant la tâche mère de la tache liée
        motherTask = ProjectManagerDAO.getTask(con, task.getMereId());
        if (motherTask.getMereId() != -1) {// c'est une tache, pas le projet
          updateMother = false;
          endDateLinked = linkedTask.getDateFin();

          // vérifie si la date de fin n'est pas
          // un jour travaillé
          calendar.setTime(motherTask.getDateFin());
          while (holidays.contains(motherTask.getDateFin())) {
            calendar.add(Calendar.DATE, 1);
            motherTask.setDateFin(calendar.getTime());
            updateMother = true;
          }

          if (endDateLinked.after(motherTask.getDateFin())) {
            // La date de fin de la tâche fille est supérieure à celle de la
            // mère
            // cette tâche doit être décalée

            // nouvelle date de fin de la mère = date fin fille
            motherTask.setDateFin(endDateLinked);
            updateMother = true;
          }

          if (updateMother) {
            // recalcule la charge
            calendar.setTime(motherTask.getDateDebut());
            calendar2.setTime(motherTask.getDateFin());
            charge = 0;
            while (true) {
              if (calendar.before(calendar2) || calendar.equals(calendar2)) {
                charge++;
              } else {
                break;
              }
              calendar.add(Calendar.DATE, 1);
            }

            // recalcul les charges de la tache mère
            motherTask.setCharge(charge);

            // modification de la tâche mère en BdD
            ProjectManagerDAO.updateTask(con, motherTask);
          }
        }
      }

      // on traite maintenant les sous tâches
      List<TaskDetail> subTasks = ProjectManagerDAO.getTasksByMotherIdAndPreviousId(con,
          task.getInstanceId(), task.getId(), -1);

      Date beginDateSub = null;
      Date endDateSub = null;
      saveBeginDate = null;
      saveEndDate = null;

      // détecte les tâches qui doivent être décalées
      TaskDetail subTask = null;
      for (int t = 0; t < subTasks.size(); t++) {
        isModifBeginDate = false;
        isModifEndDate = false;

        subTask = subTasks.get(t);

        beginDateSub = subTask.getDateDebut();
        saveBeginDate = beginDateSub;

        // vérifie si la date de début n'est pas un jour travaillé
        calendar.setTime(beginDateSub);
        while (holidays.contains(beginDateSub)) {
          calendar.add(Calendar.DATE, 1);
          beginDateSub = calendar.getTime();
          subTask.setDateDebut(beginDateSub);
        }

        if (beginDate.after(beginDateSub)) {
          // La date de début de la tâche mêre est supérieure à la sous tâche
          // cette tâche doit être décalée

          // nouvelle date de début = date début mère
          beginDateSub = beginDate;
          subTask.setDateDebut(beginDate);
        }

        endDateSub = subTask.getDateFin();
        saveEndDate = endDateSub;

        // calcul la date de fin
        endDateSub = processEndDate(subTask, calendar, holidays);
        subTask.setDateFin(endDateSub);

        // regarder si les dates sont modifiées
        if (!beginDateSub.equals(saveBeginDate)) {
          isModifBeginDate = true;
        }
        if (!endDateSub.equals(saveEndDate)) {
          isModifBeginDate = true;
        }

        // si on est dans un cas de modif de date, faire la mise à jour
        // seulement si les dates changent
        if (isModifBeginDate || isModifEndDate) {
          updateTask(subTask, userId);
        }
      }

      // modification de la tâche en BdD
      if (task.getAvancement() == 100) {
        task.setStatut(TaskDetail.COMPLETE);
      }
      ProjectManagerDAO.updateTask(con, task);

      // modification de sa tache mère s'il en existe une
      updateChargesMotherTask(con, task);

      // modification de la tache associée
      updateTodo(task);

      // indexation de la tâche
      createIndex(task);

      // notifie le responsable
      if (task.getMereId() != -1
          && !userId.equals(new Integer(task.getResponsableId()).toString())) {
        alertResource(task, false);
      }
    } catch (Exception re) {
      throw new ProjectManagerRuntimeException(
          "ProjectManagerBmEJB.updateTask()", SilverpeasRuntimeException.ERROR,
          "projectManager.UPDATING_TASK_FAILED", "task = " + task.toString(),
          re);
    } finally {
      freeConnection(con);
    }
  }

  private void alertResource(TaskDetail task, boolean onCreation) {
    NotificationSender notifSender = new NotificationSender(task
        .getInstanceId());

    String subject = "";
    StringBuilder body = new StringBuilder(128);
    if (onCreation) {
      subject = "Nouvelle tâche";
      body.append("Une nouvelle tâche nommée \"").append(task.getNom()).append(
          "\" vient de vous être affecté.\n");
    } else {
      subject = "Modification d'une tâche";
      body.append("La tâche \"").append(task.getNom()).append(
          "\" dont vous êtes responsable vient d'être modifiée.\n");
    }

    NotificationMetaData notifMetaData = new NotificationMetaData(
        NotificationParameters.NORMAL, subject, body.toString());
    notifMetaData.setSender(new Integer(task.getOrganisateurId()).toString());
    notifMetaData.addUserRecipient(new Integer(task.getResponsableId())
        .toString());

    String url = URLManager
        .getURL("projectManager", null, task.getInstanceId())
        + "searchResult?Type=Task&Id=" + task.getId();
    notifMetaData.setLink(url);

    try {
      notifSender.notifyUser(notifMetaData);
    } catch (Exception e) {
      SilverTrace.warn("projectManager", "ProjectManagerBmEJB.alertResource()",
          "projectManager.EX_CANT_SEND_NOTIFICATIONS", "taskId = "
              + task.getId(), e);
    }
  }

  private Date getBeginDate(Calendar calendar, Date endDate, List<Date> holidays) {
    calendar.setTime(endDate);
    calendar.add(Calendar.DATE, 1);

    while (holidays.contains(calendar.getTime())) {
      calendar.add(Calendar.DATE, 1);
    }

    return calendar.getTime();
  }

  @Override
  public Date processEndDate(TaskDetail task) throws RemoteException {
    return processEndDate(task, null, null);
  }

  private Date processEndDate(TaskDetail task, Calendar theCalendar, List<Date> theHolidays)
      throws RemoteException {
    float toRound = new Float(0.49).floatValue();
    int charge = Math.round(task.getCharge() + toRound) - 1;
    Date currentDate = null;
    Calendar calendar = theCalendar;
    List<Date> holidays = theHolidays;

    if (calendar == null) {
      calendar = Calendar.getInstance();
    }

    if (holidays == null) {
      // Récupération des jours non travaillés
      holidays = getHolidayDates(task.getInstanceId());
    }

    calendar.setTime(task.getDateDebut());

    while (charge != 0) {
      calendar.add(Calendar.DATE, 1);
      currentDate = calendar.getTime();
      if (!holidays.contains(currentDate)) {
        charge--;
      }
    }
    return calendar.getTime();
  }

  @Override
  public Date processEndDate(float fCharge, String instanceId, Date dateDebut)
      throws RemoteException {
    float toRound = new Float(0.49).floatValue();
    int charge = Math.round(fCharge + toRound) - 1;
    Date currentDate = null;

    Calendar calendar = Calendar.getInstance();

    List<Date> holidays = getHolidayDates(instanceId);

    calendar.setTime(dateDebut);

    while (charge != 0) {
      calendar.add(Calendar.DATE, 1);
      currentDate = calendar.getTime();
      if (!holidays.contains(currentDate)) {
        charge--;
      }
    }
    return calendar.getTime();
  }

  @Override
  public void calculateAllTasksDates(String instanceId, int projectId,
      String userId) throws RemoteException {
    // récupère toutes les tâches de premier niveau sans précédence
    List<TaskDetail> tasks = getTasksByMotherIdAndPreviousId(instanceId, projectId, -1);

    // Récupération des jours non travaillés
    List<Date> holidays = getHolidayDates(instanceId);

    Calendar calendar = Calendar.getInstance();

    TaskDetail task = null;
    Date beginDate = null;
    Date endDate = null;
    boolean isModifBeginDate = false;
    boolean isModifEndDate = false;
    Date saveBeginDate = null;
    Date saveEndDate = null;

    for (int t = 0; t < tasks.size(); t++) {
      isModifBeginDate = false;
      isModifEndDate = false;
      task = tasks.get(t);
      beginDate = task.getDateDebut();
      saveBeginDate = beginDate;

      // vérifie si la date de début n'est pas un jour travaillé
      while (holidays.contains(beginDate)) {
        calendar.setTime(beginDate);
        calendar.add(Calendar.DATE, 1);
        beginDate = calendar.getTime();
      }
      // mise à jour de la date de début si elle est modifiée
      if (!beginDate.equals(saveBeginDate)) {
        task.setDateDebut(beginDate);
        isModifBeginDate = true;
      }

      // calcul la date de fin et mise à jour si elle est modifiée
      saveEndDate = task.getDateFin();
      endDate = processEndDate(task, calendar, holidays);
      if (!endDate.equals(saveEndDate)) {
        task.setDateFin(endDate);
        isModifBeginDate = true;
      }

      // modification de la tâche + autres tâches liées si besoin
      if (isModifBeginDate || isModifEndDate) {
        updateTask(task, userId);
      }
    }
  }

  private void updateChargesMotherTask(Connection con, TaskDetail task)
      throws RemoteException {
    try {
      // la tache est une sous-tache -> on recalcule les montants de charges de
      // la tache mère
      TaskDetail motherTask = ProjectManagerDAO.getTask(con, task.getMereId());
      if (motherTask != null && motherTask.getMereId() != -1) {// c'est une
        // tache, pas le
        // projet
        List<TaskDetail> subTasks = ProjectManagerDAO.getTasksByMotherId(con, motherTask
            .getInstanceId(), motherTask.getId(), null);
        TaskDetail subTask = null;
        float somConsomme = 0;
        float somRaf = 0;

        for (int t = 0; t < subTasks.size(); t++) {
          subTask = subTasks.get(t);

          // calcul la somme des charges consommées et reste à faire
          somConsomme += subTask.getConsomme();
          somRaf += subTask.getRaf();
        }
        motherTask.setConsomme(somConsomme);
        motherTask.setRaf(somRaf);

        ProjectManagerDAO.updateTask(con, motherTask);
      }
    } catch (Exception re) {
      throw new ProjectManagerRuntimeException(
          "ProjectManagerBmEJB.updateChargesMotherTask()",
          SilverpeasRuntimeException.ERROR,
          "projectManager.UPDATING_TASK_FAILED", "task = " + task.toString(),
          re);
    } finally {
      freeConnection(con);
    }
  }

  /*
   * Gestion du calendrier des jours non travaillés /
   * ********************************************************************************
   */

  @Override
  public List<Date> getHolidayDates(String instanceId) throws RemoteException {
    SilverTrace.info("projectManager", "ProjectManagerBmEJB.getHolidayDates()",
        "root.MSG_GEN_ENTER_METHOD", "instanceId=" + instanceId);
    Connection con = getConnection();
    try {
      return ProjectManagerCalendarDAO.getHolidayDates(con, instanceId);
    } catch (Exception re) {
      throw new ProjectManagerRuntimeException("ProjectManagerBmEJB.getHolidayDates()",
          SilverpeasRuntimeException.ERROR, "projectManager.GETTING_HOLIDAYDATES_FAILED",
          "instanceId = " + instanceId, re);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public List<Date> getHolidayDates(String instanceId, Date beginDate, Date endDate)
      throws RemoteException {
    SilverTrace.info("projectManager", "ProjectManagerBmEJB.getHolidayDates()",
        "root.MSG_GEN_ENTER_METHOD", "instanceId=" + instanceId);
    Connection con = getConnection();
    try {
      return ProjectManagerCalendarDAO.getHolidayDates(con, instanceId,
          beginDate, endDate);
    } catch (Exception re) {
      throw new ProjectManagerRuntimeException(
          "ProjectManagerBmEJB.getHolidayDates()",
          SilverpeasRuntimeException.ERROR,
          "projectManager.GETTING_HOLIDAYDATES_FAILED", "instanceId = "
              + instanceId, re);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public void addHolidayDate(HolidayDetail holiday) throws RemoteException {
    SilverTrace.info("projectManager", "ProjectManagerBmEJB.addHolidayDate()",
        "root.MSG_GEN_ENTER_METHOD", "holidayDate="
            + holiday.getDate().toString());
    Connection con = getConnection();
    try {
      ProjectManagerCalendarDAO.addHolidayDate(con, holiday);
    } catch (Exception re) {
      throw new ProjectManagerRuntimeException(
          "ProjectManagerBmEJB.addHolidayDate()",
          SilverpeasRuntimeException.ERROR,
          "projectManager.ADDING_HOLIDAYDATE_FAILED", "date = "
              + holiday.getDate().toString(), re);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public void addHolidayDates(List<HolidayDetail> holidayDates) throws RemoteException {
    SilverTrace.info("projectManager", "ProjectManagerBmEJB.addHolidayDates()",
        "root.MSG_GEN_ENTER_METHOD", "holidayDates.size()="
            + holidayDates.size());
    Connection con = getConnection();
    try {
      for (HolidayDetail holiday : holidayDates) {
        ProjectManagerCalendarDAO.addHolidayDate(con, holiday);
      }
    } catch (Exception re) {
      throw new ProjectManagerRuntimeException(
          "ProjectManagerBmEJB.addHolidayDates()",
          SilverpeasRuntimeException.ERROR,
          "projectManager.ADDING_HOLIDAYDATES_FAILED", re);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public void removeHolidayDate(HolidayDetail holiday) throws RemoteException {
    SilverTrace.info("projectManager",
        "ProjectManagerBmEJB.removeHolidayDate()", "root.MSG_GEN_ENTER_METHOD",
        "holidayDate=" + holiday.getDate().toString());
    Connection con = getConnection();
    try {
      ProjectManagerCalendarDAO.removeHolidayDate(con, holiday);
    } catch (Exception re) {
      throw new ProjectManagerRuntimeException(
          "ProjectManagerBmEJB.removeHolidayDate()",
          SilverpeasRuntimeException.ERROR,
          "projectManager.REMOVING_HOLIDAYDATE_FAILED", "date = "
              + holiday.getDate().toString(), re);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public void removeHolidayDates(List<HolidayDetail> holidayDates) throws RemoteException {
    SilverTrace.info("projectManager",
        "ProjectManagerBmEJB.removeHolidayDates()",
        "root.MSG_GEN_ENTER_METHOD", "holidayDates.size()="
            + holidayDates.size());
    Connection con = getConnection();
    try {
      for (HolidayDetail holiday : holidayDates) {
        ProjectManagerCalendarDAO.removeHolidayDate(con, holiday);
      }
    } catch (Exception re) {
      throw new ProjectManagerRuntimeException(
          "ProjectManagerBmEJB.removeHolidayDates()",
          SilverpeasRuntimeException.ERROR,
          "projectManager.REMOVING_HOLIDAYDATES_FAILED", re);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public boolean isHolidayDate(HolidayDetail date) throws RemoteException {
    Connection con = getConnection();
    try {
      return ProjectManagerCalendarDAO.isHolidayDate(con, date);
    } catch (Exception re) {
      throw new ProjectManagerRuntimeException(
          "ProjectManagerBmEJB.isHolidayDate()",
          SilverpeasRuntimeException.ERROR,
          "projectManager.GETTING_HOLIDAYDATE_FAILED", re);
    } finally {
      freeConnection(con);
    }
  }

  /**********************************************************************************/
  /**
   * Gestion des todos /
   **********************************************************************************/
  private TodoDetail getTodo(String todoId) {
    SilverTrace.info("projectManager", "ProjectManagerBmEJB.getTodo()",
        "root.MSG_GEN_ENTER_METHOD", "todoId=" + todoId);
    TodoBackboneAccess todoBBA = new TodoBackboneAccess();
    return todoBBA.getEntry(todoId);
  }

  private void addTodo(TaskDetail task) {
    SilverTrace.info("projectManager", "ProjectManagerBmEJB.addTodo()",
        "root.MSG_GEN_ENTER_METHOD", "actionId=" + task.getId());
    TodoBackboneAccess todoBBA = new TodoBackboneAccess();
    TodoDetail todo = task.toTodoDetail();
    todoBBA.addEntry(todo);
  }

  private void removeTodo(int id, String instanceId) {
    SilverTrace.info("projectManager", "ProjectManagerBmEJB.removeTodo()",
        "root.MSG_GEN_ENTER_METHOD", "id = " + id + ", instanceId="
            + instanceId);
    TodoBackboneAccess todoBBA = new TodoBackboneAccess();
    todoBBA.removeEntriesFromExternal("useless", instanceId, new Integer(id)
        .toString());
  }

  private void updateTodo(TaskDetail task) {
    SilverTrace.info("projectManager", "ProjectManagerBmEJB.updateTodo()",
        "root.MSG_GEN_ENTER_METHOD", "actionId=" + task.getId());
    TodoBackboneAccess todoBBA = new TodoBackboneAccess();
    todoBBA.removeEntriesFromExternal("useless", task.getInstanceId(),
        new Integer(task.getId()).toString());
    todoBBA.addEntry(task.toTodoDetail());
  }

  /**********************************************************************************/
  /**
   * Gestion des index /
   **********************************************************************************/
  private void createIndex(TaskDetail task) {
    SilverTrace.info("projectManager", "ProjectManagerBmEJB.createIndex()",
        "root.MSG_GEN_ENTER_METHOD", "actionId=" + task.getId());

    FullIndexEntry indexEntry = null;
    // Index the Composed Task
    indexEntry = new FullIndexEntry(task.getInstanceId(), "Action",
        new Integer(task.getId()).toString());
    indexEntry.setTitle(task.getNom());
    indexEntry.setPreView(task.getDescription());
    IndexEngineProxy.addIndexEntry(indexEntry);
  }

  private void removeIndex(int id, String instanceId) {
    SilverTrace.info("projectManager", "ProjectManagerBmEJB.removeIndex()",
        "root.MSG_GEN_ENTER_METHOD", "actionId=" + id);

    IndexEntryPK indexEntry = new IndexEntryPK(instanceId, "Action",
        new Integer(id).toString());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  @Override
  public void index(String instanceId) throws RemoteException {
    List<TaskDetail> tasks = getAllTasks(instanceId, null);
    for (TaskDetail task : tasks) {
      indexTask(task);
    }
  }

  private void indexTask(TaskDetail task) throws RemoteException {
    // index task itself
    createIndex(task);

    TaskPK taskPK = new TaskPK(task.getId(), task.getInstanceId());

    // index attachments
    AttachmentController.attachmentIndexer(taskPK);

    // index comments
    getCommentService().indexAllCommentsOnPublication(taskPK);
  }

  private Connection getConnection() {
    try {
      Connection con = DBUtil.makeConnection(dbName);

      return con;
    } catch (Exception e) {
      throw new ProjectManagerRuntimeException(
          "ProjectManagerBmEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  /**
   * Method declaration
   * @param con
   * @see
   */
  private void freeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        SilverTrace.error("publication", "PublicationEJB.freeConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  @Override
  public int getOccupationByUser(String userId, Date dateDeb, Date dateFin) {
    Connection con = getConnection();
    try {
      return ProjectManagerDAO.getOccupationByUser(con, userId, dateDeb,
          dateFin);
    } catch (Exception re) {
      throw new ProjectManagerRuntimeException(
          "ProjectManagerBmEJB.isHolidayDate()",
          SilverpeasRuntimeException.ERROR,
          "projectManager.GETTING_HOLIDAYDATE_FAILED", re);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public int getOccupationByUser(String userId, Date dateDeb, Date dateFin,
      int excludedTaskId) {
    Connection con = getConnection();
    try {
      return ProjectManagerDAO.getOccupationByUser(con, userId, dateDeb,
          dateFin, excludedTaskId);
    } catch (Exception re) {
      throw new ProjectManagerRuntimeException(
          "ProjectManagerBmEJB.isHolidayDate()",
          SilverpeasRuntimeException.ERROR,
          "projectManager.GETTING_HOLIDAYDATE_FAILED", re);
    } finally {
      freeConnection(con);
    }
  }

  public void ejbCreate() {
    // not implemented
  }

  @Override
  public void setSessionContext(SessionContext context) {
    // not implemented
  }

  @Override
  public void ejbRemove() {
    // not implemented
  }

  @Override
  public void ejbActivate() {
    // not implemented
  }

  @Override
  public void ejbPassivate() {
    // not implemented
  }
}
