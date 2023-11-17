/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.projectmanager.service;

import org.silverpeas.components.projectmanager.model.Filtre;
import org.silverpeas.components.projectmanager.model.HolidayDetail;
import org.silverpeas.components.projectmanager.model.ProjectManagerCalendarDAO;
import org.silverpeas.components.projectmanager.model.ProjectManagerDAO;
import org.silverpeas.components.projectmanager.model.ProjectManagerRuntimeException;
import org.silverpeas.components.projectmanager.model.TaskDetail;
import org.silverpeas.components.projectmanager.model.TaskPK;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.comment.service.CommentService;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.Attachments;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.io.upload.UploadedFile;
import org.silverpeas.core.notification.NotificationException;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationParameters;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.personalorganizer.model.TodoDetail;
import org.silverpeas.core.personalorganizer.service.SilverpeasCalendar;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.Link;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * CDI bean to manage the projectManager application
 */
@Service
@Transactional(Transactional.TxType.SUPPORTS)
public class DefaultProjectManagerService implements ProjectManagerService {

  @Inject
  private CommentService commentController;

  @Inject
  private SilverpeasCalendar silverpeasCalendar;

  /**
   * Gets a comment service.
   *
   * @return a DefaultCommentService instance.
   */
  private CommentService getCommentService() {
    return commentController;
  }

  @Override
  public List<TaskDetail> getProjects(String instanceId) {

    Connection con = getConnection();
    try {
      return ProjectManagerDAO.getTasksByMotherId(con, instanceId, -1);
    } catch (SQLException re) {
      throw new ProjectManagerRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public List<TaskDetail> getTasksByMotherId(String instanceId, int motherId) {
    Connection con = getConnection();
    try {
      return ProjectManagerDAO.getTasksByMotherId(con, instanceId, motherId);
    } catch (SQLException re) {
      throw new ProjectManagerRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public List<TaskDetail> getTasksByMotherIdAndPreviousId(String instanceId, int motherId,
      int previousId) {
    Connection con = getConnection();
    try {
      return ProjectManagerDAO.
          getTasksByMotherIdAndPreviousId(con, instanceId, motherId, previousId);
    } catch (SQLException re) {
      throw new ProjectManagerRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public List<TaskDetail> getAllTasks(String instanceId, Filtre filtre) {
    Connection con = getConnection();
    try {
      return ProjectManagerDAO.getAllTasks(con, instanceId, filtre);
    } catch (SQLException re) {
      throw new ProjectManagerRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public TaskDetail getTask(int id) {

    Connection con = getConnection();
    try {
      return ProjectManagerDAO.getTask(con, id);
    } catch (SQLException re) {
      throw new ProjectManagerRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public TaskDetail getTaskByTodoId(String todoId) {

    Connection con = getConnection();
    String actionId;
    try {
      TodoDetail todo = getTodo(todoId);
      actionId = todo.getExternalId();
      return ProjectManagerDAO.getTask(con, actionId);
    } catch (Exception re) {
      throw new ProjectManagerRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public TaskDetail getMostDistantTask(String instanceId, int taskId) {

    Connection con = getConnection();
    try {
      return ProjectManagerDAO.getMostDistantTask(con, taskId);
    } catch (Exception re) {
      throw new ProjectManagerRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  @Transactional
  public int addTask(TaskDetail task, final Collection<UploadedFile> uploadedFiles) {

    Connection con = getConnection();
    try {
      // insertion de la task en BdD
      if (task.getAvancement() == 100) {
        task.setStatut(TaskDetail.COMPLETE);
      }
      int id = ProjectManagerDAO.addTask(con, task);
      task.setId(id);

      if (task.getMereId() != -1) {
        // la tache mere est decomposée
        ProjectManagerDAO.actionEstDecomposee(con, task.getMereId(), 1);
      }

      // modification de sa tache mère s'il en existe une
      updateChargesMotherTask(con, task);
      // insertion de la tache correspondante dans le gestionnaire de taches du responsable
      addTodo(task);
      // attachments
      Attachments.from(uploadedFiles).attachTo(task.asContribution());
      // indexation de la task
      createIndex(task);
      if (task.getMereId() != -1) {
        // alerte du responsable
        alertResource(task, true);
      }
      return id;
    } catch (SQLException re) {
      throw new ProjectManagerRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  @Transactional
  public void removeTask(int id, String instanceId) {

    Connection con = getConnection();
    try {
      TaskDetail actionASupprimer = ProjectManagerDAO.getTask(con, id);
      // Supprime toutes les sous taches (à n'importe quel niveau)
      List<TaskDetail> tree = ProjectManagerDAO.getTree(con, id);
      TaskDetail task = null;
      for (TaskDetail taskDetail : tree) {
        task = taskDetail;
        removeTask(con, task.getId(), task.getInstanceId());
      }
      // La tâche mère a t-elle d'autres taches filles. Est-elle toujours décomposée ?
      List<TaskDetail> actionsSoeur = ProjectManagerDAO.getTree(con, actionASupprimer.getMereId());
      if (actionsSoeur.size() == 1) {
        // La task mère n'a qu'une sous task. Celle que l'on va supprimer.
        // Elle ne va donc plus être décomposée
        ProjectManagerDAO.actionEstDecomposee(con, actionASupprimer.getMereId(), 0);
      }
      // Cette tâche est-elle la tâche précédente d'autres tâches
      List<TaskDetail> nextTasks = ProjectManagerDAO.getNextTasks(con, id);
      TaskDetail nextTask;
      for (TaskDetail taskDetail : nextTasks) {
        nextTask = taskDetail;
        nextTask.setPreviousTaskId(-1);
        ProjectManagerDAO.updateTask(con, nextTask);
      }
      // modification de sa tache mère s'il en existe une
      Objects.requireNonNull(task);
      updateChargesMotherTask(con, task);
    } catch (Exception re) {
      throw new ProjectManagerRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  private void removeTask(Connection con, int id, String instanceId) throws SQLException {

    // suppression de la tâche en BdD
    ProjectManagerDAO.removeTask(con, id);
    // suppression de la tâche associée
    removeTodo(id, instanceId);
    // supprime les fichiers joint à la tâche
    TaskPK taskPK = new TaskPK(id, instanceId);
    ResourceReference ref = new ResourceReference(taskPK);
    List<SimpleDocument> attachments = AttachmentServiceProvider.getAttachmentService().
        listDocumentsByForeignKey(ref, null);
    for (SimpleDocument attachment : attachments) {
      AttachmentServiceProvider.getAttachmentService().deleteAttachment(attachment);
    }
    // supprime les commentaires de la tâche
    getCommentService().deleteAllCommentsOnResource(TaskDetail.getResourceType(), ref);
    // suppression de l'index
    removeIndex(id, instanceId);
  }

  @Override
  @Transactional
  public void updateTask(TaskDetail task, String userId) {

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

      //traitement des tâches suivantes
      updateNextTasks(task, userId, con, endDate, holidays, calendar);

      //traitement des sous-tâches
      updateSubTasks(task, userId, con, beginDate, holidays, calendar);

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
      if (task.getMereId() != -1 && !userId.equals(Integer.toString(task.getResponsableId()))) {
        alertResource(task, false);
      }
    } catch (SQLException re) {
      throw new ProjectManagerRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  private void updateSubTasks(final TaskDetail task, final String userId, final Connection con,
      final Date beginDate, final List<Date> holidays, final Calendar calendar)
      throws SQLException {
    // on traite maintenant les sous tâches
    List<TaskDetail> subTasks = ProjectManagerDAO.getTasksByMotherIdAndPreviousId(con, task.
        getInstanceId(), task.getId(), -1);


    // détecte les tâches qui doivent être décalées
    for (TaskDetail taskDetail : subTasks) {
      boolean isModifBeginDate = false;
      updateSubTask(userId, beginDate, holidays, calendar, taskDetail, isModifBeginDate);
    }
  }

  private void updateSubTask(final String userId, final Date beginDate, final List<Date> holidays,
      final Calendar calendar, final TaskDetail subTask, boolean isModifBeginDate) {
    Date beginDateSub = subTask.getDateDebut();
    Date saveBeginDate = beginDateSub;

    // vérifie si la date de début n'est pas un jour travaillé
    calendar.setTime(beginDateSub);
    beginDateSub = goAfterHoliday(holidays, calendar, subTask, beginDateSub);

    if (beginDate.after(beginDateSub)) {
      // La date de début de la tâche mêre est supérieure à la sous tâche cette tâche doit être
      // décalée
      // nouvelle date de début = date début mère
      beginDateSub = beginDate;
      subTask.setDateDebut(beginDate);
    }

    Date endDateSub = subTask.getDateFin();
    Date saveEndDate = endDateSub;

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
    if (isModifBeginDate) {
      updateTask(subTask, userId);
    }
  }

  private void updateNextTasks(final TaskDetail task, final String userId, final Connection con,
      final Date endDate, final List<Date> holidays, final Calendar calendar) throws SQLException {
    // on commence par récupérer les tâches suivantes
    List<TaskDetail> nextTasks = ProjectManagerDAO.getNextTasks(con, task.getId());

    // détecte les tâches qui doivent être décalées
    Calendar calendar2 = Calendar.getInstance();

    for (TaskDetail linkedTask : nextTasks) {
      Date beginDateLinked = linkedTask.getDateDebut();
      Date saveBeginDate = beginDateLinked;

      // vérifie si la date de début n'est pas
      // un jour travaillé
      calendar.setTime(beginDateLinked);
      beginDateLinked = goAfterHoliday(holidays, calendar, linkedTask, beginDateLinked);

      Date endDateLinked = linkedTask.getDateFin();
      Date saveEndDate = endDateLinked;

      if (endDate.equals(endDateLinked) || endDate.after(endDateLinked)) {
        beginDateLinked = moveTask(endDate, holidays, calendar, linkedTask);
      }

      // calcul de la nouvelle date de fin (date début + charge)
      endDateLinked = processEndDate(linkedTask, calendar, holidays);
      linkedTask.setDateFin(endDateLinked);

      // regarder si les dates sont modifiées
      updateTask(userId, linkedTask, beginDateLinked, saveBeginDate, endDateLinked, saveEndDate);

      // on traite maintenant la tâche mère de la tache liée
      TaskDetail motherTask = ProjectManagerDAO.getTask(con, task.getMereId());
      if (motherTask.getMereId() != -1) {
        // c'est une tache, pas le projet
        updateMotherTask(con, holidays, calendar, linkedTask, motherTask, calendar2);

      }
    }
  }

  private void updateMotherTask(final Connection con, final List<Date> holidays,
      final Calendar calendar, final TaskDetail linkedTask, final TaskDetail motherTask,
      final Calendar calendar2) throws SQLException {
    boolean updateMother;
    final Date endDateLinked;
    float charge;
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
      // La date de fin de la tâche fille est supérieure à celle de la mère cette tâche doit être
      // décalée
      // nouvelle date de fin de la mère = date fin fille
      motherTask.setDateFin(endDateLinked);
      updateMother = true;
    }

    if (updateMother) {
      // recalcule la charge
      calendar.setTime(motherTask.getDateDebut());
      calendar2.setTime(motherTask.getDateFin());
      charge = 0;
      while (calendar.before(calendar2) || calendar.equals(calendar2)) {
        charge++;
        calendar.add(Calendar.DATE, 1);
      }

      // recalcul les charges de la tache mère
      motherTask.setCharge(charge);

      // modification de la tâche mère en BdD
      ProjectManagerDAO.updateTask(con, motherTask);
    }
  }

  private void updateTask(final String userId, final TaskDetail linkedTask,
      final Date beginDateLinked, final Date saveBeginDate, final Date endDateLinked,
      final Date saveEndDate) {
    boolean isModifBeginDate = false;
    boolean isModifEndDate = false;
    if (!beginDateLinked.equals(saveBeginDate)) {
      isModifBeginDate = true;
    }
    if (!endDateLinked.equals(saveEndDate)) {
      isModifEndDate = true;
    }

    // si on est dans un cas de modif de date, faire la mise à jour
    // seulement si les dates changent
    if (isModifBeginDate || isModifEndDate) {
      updateTask(linkedTask, userId);
    }
  }

  private Date moveTask(final Date endDate, final List<Date> holidays, final Calendar calendar,
      final TaskDetail linkedTask) {
    final Date beginDateLinked;// La date de fin de la tâche précédente est supérieure ou égale à la
    // tâche liéée
    // cette tâche doit être décalée

    // calcul de la nouvelle date de début (= date fin + 1)
    beginDateLinked = getBeginDate(calendar, endDate, holidays);
    linkedTask.setDateDebut(beginDateLinked);
    return beginDateLinked;
  }

  private Date goAfterHoliday(final List<Date> holidays, final Calendar calendar,
      final TaskDetail linkedTask, Date beginDateLinked) {
    while (holidays.contains(beginDateLinked)) {
      calendar.add(Calendar.DATE, 1);
      beginDateLinked = calendar.getTime();
      linkedTask.setDateDebut(beginDateLinked);
    }
    return beginDateLinked;
  }

  private String getNotificationSubject(final LocalizationBundle message, boolean onCreation) {
    String subject;
    if (onCreation) {
      subject = message.getString("projectManager.NewTask");
    } else {
      subject = message.getString("projectManager.UpdateTask");
    }
    return subject;
  }

  private String getNotificationBody(final LocalizationBundle message, boolean onCreation,
      String taskName) {
    StringBuilder body = new StringBuilder();
    if (onCreation) {
      body.append(message.getString("projectManager.NewTaskNamed"))
          .append(" '")
          .append(taskName)
          .append("' ")
          .append(message.getString("projectManager.NewTaskAssigned"))
          .append("\n");
    } else {
      body.append(message.getString("projectManager.UpdateTaskNamed"))
          .append(" '")
          .append(taskName)
          .append("' ")
          .append(message.getString("projectManager.UpdateTaskAssigned"))
          .append("\n");
    }
    return body.toString();
  }

  private void alertResource(TaskDetail task, boolean onCreation) {
    NotificationSender notifSender = new NotificationSender(task.getInstanceId());

    String url = URLUtil.getURL("projectManager", null, task.getInstanceId())
        + "searchResult?Type=Task&Id=" + task.getId();

    LocalizationBundle message = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.projectManager.multilang.projectManagerBundle",
        DisplayI18NHelper.getDefaultLanguage());

    String subject = getNotificationSubject(message, onCreation);
    String body = getNotificationBody(message, onCreation, task.getNom());

    NotificationMetaData notifMetaData =
        new NotificationMetaData(NotificationParameters.PRIORITY_NORMAL,
        subject, body);

    for (String language : DisplayI18NHelper.getLanguages()) {
      message = ResourceLocator.getLocalizationBundle(
          "org.silverpeas.projectManager.multilang.projectManagerBundle", language);
      subject = getNotificationSubject(message, onCreation);
      body = getNotificationBody(message, onCreation, task.getNom());
      notifMetaData.addLanguage(language, subject, body);

      Link link = new Link(url, message.getString("projectManager.notifLinkLabel"));
      notifMetaData.setLink(link, language);
    }

    notifMetaData.setSender(Integer.toString(task.getOrganisateurId()));
    notifMetaData.addUserRecipient(new UserRecipient(String.valueOf((task.getResponsableId()))));

    try {
      notifSender.notifyUser(notifMetaData);
    } catch (NotificationException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
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
  public Date processEndDate(TaskDetail task) {
    return processEndDate(task, null, null);
  }

  private Date processEndDate(TaskDetail task, Calendar theCalendar, List<Date> theHolidays) {
    float toRound = 0.49F;
    int charge = Math.round(task.getCharge() + toRound) - 1;
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
      Date currentDate = calendar.getTime();
      if (!holidays.contains(currentDate)) {
        charge--;
      }
    }
    return calendar.getTime();
  }

  @Override
  public Date processEndDate(float fCharge, String instanceId, Date dateDebut) {
    float toRound = 0.49F;
    int charge = Math.round(fCharge + toRound) - 1;
    Calendar calendar = Calendar.getInstance();
    List<Date> holidays = getHolidayDates(instanceId);
    calendar.setTime(dateDebut);
    while (charge != 0) {
      calendar.add(Calendar.DATE, 1);
      Date currentDate = calendar.getTime();
      if (!holidays.contains(currentDate)) {
        charge--;
      }
    }
    return calendar.getTime();
  }

  @Override
  @Transactional
  public void calculateAllTasksDates(String instanceId, int projectId,
      String userId) {
    // récupère toutes les tâches de premier niveau sans précédence
    List<TaskDetail> tasks = getTasksByMotherIdAndPreviousId(instanceId, projectId, -1);

    // Récupération des jours non travaillés
    List<Date> holidays = getHolidayDates(instanceId);

    Calendar calendar = Calendar.getInstance();
    for (TaskDetail task : tasks) {
      boolean isModifBeginDate = false;
      Date beginDate = task.getDateDebut();
      Date saveBeginDate = beginDate;

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
      Date saveEndDate = task.getDateFin();
      Date endDate = processEndDate(task, calendar, holidays);
      if (!endDate.equals(saveEndDate)) {
        task.setDateFin(endDate);
        isModifBeginDate = true;
      }

      // modification de la tâche + autres tâches liées si besoin
      if (isModifBeginDate) {
        updateTask(task, userId);
      }
    }
  }

  private void updateChargesMotherTask(Connection con, TaskDetail task) {
    try {
      // la tache est une sous-tache -> on recalcule les montants de charges de
      // la tache mère
      TaskDetail motherTask = ProjectManagerDAO.getTask(con, task.getMereId());
      if (motherTask != null && motherTask.getMereId() != -1) { // c'est une
        // tache, pas le
        // projet
        List<TaskDetail> subTasks = ProjectManagerDAO.getTasksByMotherId(con, motherTask
            .getInstanceId(), motherTask.getId());
        float somConsomme = 0;
        float somRaf = 0;
        for (TaskDetail subTask : subTasks) {
          // calcul la somme des charges consommées et reste à faire
          somConsomme += subTask.getConsomme();
          somRaf += subTask.getRaf();
        }
        motherTask.setConsomme(somConsomme);
        motherTask.setRaf(somRaf);

        ProjectManagerDAO.updateTask(con, motherTask);
      }
    } catch (SQLException re) {
      throw new ProjectManagerRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public List<Date> getHolidayDates(String instanceId) {

    Connection con = getConnection();
    try {
      return ProjectManagerCalendarDAO.getHolidayDates(con, instanceId);
    } catch (Exception re) {
      throw new ProjectManagerRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public List<Date> getHolidayDates(String instanceId, Date beginDate, Date endDate) {

    Connection con = getConnection();
    try {
      return ProjectManagerCalendarDAO.getHolidayDates(con, instanceId, beginDate, endDate);
    } catch (SQLException re) {
      throw new ProjectManagerRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void addHolidayDate(HolidayDetail holiday) {

    Connection con = getConnection();
    try {
      ProjectManagerCalendarDAO.addHolidayDate(con, holiday);
    } catch (SQLException re) {
      throw new ProjectManagerRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  @Transactional
  public void addHolidayDates(List<HolidayDetail> holidayDates) {

    Connection con = getConnection();
    try {
      for (HolidayDetail holiday : holidayDates) {
        ProjectManagerCalendarDAO.addHolidayDate(con, holiday);
      }
    } catch (SQLException re) {
      throw new ProjectManagerRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  @Transactional
  public void removeHolidayDate(HolidayDetail holiday) {

    Connection con = getConnection();
    try {
      ProjectManagerCalendarDAO.removeHolidayDate(con, holiday);
    } catch (SQLException re) {
      throw new ProjectManagerRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  @Transactional
  public void removeHolidayDates(List<HolidayDetail> holidayDates) {

    Connection con = getConnection();
    try {
      for (HolidayDetail holiday : holidayDates) {
        ProjectManagerCalendarDAO.removeHolidayDate(con, holiday);
      }
    } catch (SQLException re) {
      throw new ProjectManagerRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public boolean isHolidayDate(HolidayDetail date) {
    Connection con = getConnection();
    try {
      return ProjectManagerCalendarDAO.isHolidayDate(con, date);
    } catch (SQLException re) {
      throw new ProjectManagerRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  private TodoDetail getTodo(String todoId) {
    return silverpeasCalendar.getTodoDetail(todoId);
  }

  private void addTodo(TaskDetail task) {

    TodoDetail todo = task.toTodoDetail();
    silverpeasCalendar.addToDo(todo);
  }

  private void removeTodo(int id, String instanceId) {

    silverpeasCalendar.removeToDoFromExternal("useless", instanceId, Integer.toString(id));
  }

  private void updateTodo(TaskDetail task) {

    silverpeasCalendar.removeToDoFromExternal("useless", task.getInstanceId(), Integer.
        toString(task.getId()));
    silverpeasCalendar.addToDo(task.toTodoDetail());
  }

  private void createIndex(TaskDetail task) {
    FullIndexEntry indexEntry =
        new FullIndexEntry(new IndexEntryKey(task.getInstanceId(), "Action",
            Integer.toString(task.getId())));
    indexEntry.setTitle(task.getNom());
    indexEntry.setPreview(task.getDescription());
    IndexEngineProxy.addIndexEntry(indexEntry);
  }

  private void removeIndex(int id, String instanceId) {

    IndexEntryKey indexEntry = new IndexEntryKey(instanceId, "Action", Integer.toString(id));
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  @Override
  public void index(String instanceId) {
    List<TaskDetail> tasks = getAllTasks(instanceId, null);
    for (TaskDetail task : tasks) {
      indexTask(task);
    }
  }

  private void indexTask(TaskDetail task) {
    // index task itself
    createIndex(task);
    TaskPK taskPK = new TaskPK(task.getId(), task.getInstanceId());
    ResourceReference ref = new ResourceReference(taskPK);
    AttachmentServiceProvider.getAttachmentService()
        .indexAllDocuments(ref, null, null);
    // index comments
    getCommentService().indexAllCommentsOnPublication(TaskDetail.getResourceType(), ref);
  }

  private Connection getConnection() {
    try {
      return DBUtil.openConnection();
    } catch (Exception e) {
      throw new ProjectManagerRuntimeException(e);
    }
  }

  @Override
  public int getOccupationByUser(String userId, Date dateDeb, Date dateFin) {
    Connection con = getConnection();
    try {
      return ProjectManagerDAO.getOccupationByUser(con, userId, dateDeb, dateFin);
    } catch (SQLException re) {
      throw new ProjectManagerRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public int getOccupationByUser(String userId, Date dateDeb, Date dateFin, int excludedTaskId) {
    Connection con = getConnection();
    try {
      return ProjectManagerDAO.getOccupationByUser(con, userId, dateDeb, dateFin, excludedTaskId);
    } catch (SQLException re) {
      throw new ProjectManagerRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }
}
