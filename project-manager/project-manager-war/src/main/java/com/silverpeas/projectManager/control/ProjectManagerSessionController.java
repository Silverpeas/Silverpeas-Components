/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.projectManager.control;

import java.text.ParseException;
import java.util.*;

import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;

import com.silverpeas.projectManager.control.ejb.ProjectManagerBm;
import com.silverpeas.projectManager.model.*;
import com.silverpeas.projectManager.vo.DayVO;
import com.silverpeas.projectManager.vo.MonthVO;
import com.silverpeas.projectManager.vo.WeekVO;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;

import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.*;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

/**
 * This class contains all the business model for project manager component
 */
public class ProjectManagerSessionController extends AbstractComponentSessionController {

  /**
   * Project manager EJB
   */
  private ProjectManagerBm projectManagerBm = null;
  private TaskDetail currentTask = null;
  private Boolean projectDefined = null;
  private TaskDetail currentProject = null;
  // Current resource collection
  private Collection<TaskResourceDetail> currentResources = null;
  private boolean filtreActif = false;
  private Filtre filtre = null;
  private List<Integer> unfoldTasks = new ArrayList<Integer>();
  private Calendar calendar = null;
  public static int WORKING_DAY = 0;
  public static int HOLIDAY_DAY = 1;

  public ProjectManagerSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.projectManager.multilang.projectManagerBundle",
        "org.silverpeas.projectManager.settings.projectManagerIcons");
  }

  public TaskDetail getCurrentTask() {
    return currentTask;
  }

  public void addUnfoldTask(String id) {
    unfoldTasks.add(Integer.parseInt(id));
  }

  public void removeUnfoldTask(String id) {
    unfoldTasks.remove(Integer.valueOf(id));
  }

  public List<Integer> getUnfoldTasks() {
    return unfoldTasks;
  }

  private boolean isUnfoldTask(int actionId) {
    return unfoldTasks.contains(Integer.valueOf(actionId));
  }

  public List<TaskDetail> getAllTasks() {
    List<TaskDetail> tasks = getProjectManagerBm().getAllTasks(getComponentId(), getFiltre());
    for (TaskDetail task : tasks) {
      enrichirTask(task);
    }
    return tasks;
  }

  public List<TaskDetail> getTasks() {
    currentTask = null;
    List<TaskDetail> tasks = getProjectManagerBm().getTasksByMotherId(getComponentId(),
        getCurrentProject().getId(), getFiltre());
    List<TaskDetail> arbo = new ArrayList<TaskDetail>();
    for (TaskDetail task : tasks) {
      arbo = buildArbo(arbo, task, null, 0);
    }
    return arbo;
  }

  private List<TaskDetail> buildArbo(List<TaskDetail> arbo, TaskDetail task, TaskDetail actionMere,
      int level) {
    SilverTrace.info("projectManager", "ProjectManagerSessionController.buildArbo()",
        "root.MSG_GEN_ENTER_METHOD", "arbo.size()=" + arbo.size() + ", actionId = " + task.getId()
        + ", level = " + level);
    enrichirTask(task);

    // fichiers joint à la tâche
    task.setAttachments(getAttachments(Integer.toString(task.getId())));
    task.setLevel(level);
    if (SilverpeasRole.admin.isInRole(getRole())) {
      task.setDeletionAvailable(true);
      task.setUpdateAvailable(true);
    } else {
      if (actionMere != null && "responsable".equals(getRole())
          && actionMere.getResponsableId() == Integer.parseInt(getUserId())) {
        task.setDeletionAvailable(true);
        task.setUpdateAvailable(true);
      } else if ("responsable".equals(getRole())
          && task.getResponsableId() == Integer.parseInt(getUserId())) {
        task.setUpdateAvailable(true);
      }
    }

    if (isUnfoldTask(task.getId())) {
      task.setUnfold(true);
      arbo.add(task);

      // la tâche est dépliée
      List<TaskDetail> sousActions = getProjectManagerBm().getTasksByMotherId(
          getComponentId(), task.getId(), getFiltre());
      level++;
      for (TaskDetail sousAction : sousActions) {
        buildArbo(arbo, sousAction, task, level);
      }
    } else {
      task.setUnfold(false);
      arbo.add(task);
    }
    return arbo;
  }

  public List<TaskDetail> getTasks(String id) {
    List<TaskDetail> tasks = getProjectManagerBm().getTasksByMotherId(getComponentId(),
        Integer.parseInt(id), getFiltre());
    for (TaskDetail task : tasks) {
      enrichirTask(task);
    }
    return tasks;
  }

  /**
   * Retrieve the list of tasks which are not cancelled
   *
   * @param id the current root task identifier
   * @return the list of tasks which are not cancelled
   */
  public List<TaskDetail> getTasksNotCancelled(String id) {
    List<TaskDetail> tasks = getProjectManagerBm().getTasksNotCancelledByMotherId(
        getComponentId(), Integer.parseInt(id), getFiltre());
    for (TaskDetail task : tasks) {
      enrichirTask(task);
    }
    return tasks;
  }

  public List<TaskDetail> getPotentialPreviousTasks() {
    return getPotentialPreviousTasks(false);
  }

  public List<TaskDetail> getPotentialPreviousTasks(boolean onCreation) {
    int motherId = getCurrentProject().getId(); // par défaut, on est au niveau
    // du projet
    if (getCurrentTask() != null) {
      // On est au niveau d'une tache
      if (onCreation) {
        motherId = getCurrentTask().getId();
      } else {
        motherId = getCurrentTask().getMereId();
      }
    }
    List<TaskDetail> previousTasks = getProjectManagerBm().getTasksByMotherId(getComponentId(),
        motherId);
    // calcul de la date de debut de la nouvelle tache
    // par rapport à la date de fin de la tache precedente
    TaskDetail previousTask = null;
    for (int t = 0; t < previousTasks.size(); t++) {
      previousTask = previousTasks.get(t);
      previousTask.setCharge(previousTask.getCharge() + 1);
      previousTask.setUiDateDebutPlus1(date2UIDate(processEndDate(previousTask)));
      previousTask.setCharge(previousTask.getCharge() - 1);
    }
    return previousTasks;
  }

  /**
   * @param task la tache dont on veut calculer la date de fin
   * @return la date de fin (= dateDebut + Charge)
   */
  public Date processEndDate(TaskDetail task) {
    task.setInstanceId(getComponentId());
    return getProjectManagerBm().processEndDate(task);
  }

  public Date processEndDate(String taskId, String charge, Date dateDebut) {
    TaskDetail task = getTask(taskId);
    task.setCharge(charge);
    task.setDateDebut(dateDebut);
    return getProjectManagerBm().processEndDate(task);
  }

  public Date processEndDate(String charge, Date dateDebut, String instanceId) {
    return getProjectManagerBm().processEndDate(Float.parseFloat(charge), instanceId, dateDebut);
  }

  /**
   * Vérifie la date de début d'une tâche. Si la date de début est un jour non travaillé, la date de
   * début sera le prochain jour travaillé.
   *
   * @param task la tâche dont la date de début doit être vérifiée
   */
  public void checkBeginDate(TaskDetail task) {
    getCalendar().setTime(task.getDateDebut());
    // récupère les jours non travaillés
    List<Date> holidayDates = getProjectManagerBm().getHolidayDates(getComponentId());
    while (holidayDates.contains(getCalendar().getTime())) {
      getCalendar().add(Calendar.DATE, 1);
    }
    task.setDateDebut(getCalendar().getTime());
  }

  public void enrichirTask(TaskDetail task) {
    String responsableId = Integer.toString(task.getResponsableId());
    UserDetail responsable = getUserDetail(responsableId);
    task.setResponsableFullName(getUserFullName(responsable));

    String organisateurId = Integer.toString(task.getOrganisateurId());
    UserDetail cdp = getUserDetail(organisateurId);
    task.setOrganisateurFullName(getUserFullName(cdp));

    // dates au format de l'utilisateur
    task.setUiDateDebut(date2UIDate(task.getDateDebut()));
    task.setUiDateFin(date2UIDate(task.getDateFin()));

    // mettre les nom des user sur les resources
    Collection<TaskResourceDetail> resources = task.getResources();
    Iterator<TaskResourceDetail> it = resources.iterator();
    while (it.hasNext()) {
      TaskResourceDetail resource = it.next();
      String userId = resource.getUserId();
      UserDetail user = getUserDetail(userId);
      resource.setUserName(getUserFullName(user));
    }
    updateOccupation(task);
  }

  public TaskDetail getTask(String id) {
    return getTask(id, true);
  }

  public TaskDetail getTask(String id, boolean getAttachments) {
    currentTask = getProjectManagerBm().getTask(Integer.parseInt(id));
    enrichirTask(currentTask);
    // récupération du nom de la tâche précédente
    int previousTaskId = currentTask.getPreviousTaskId();
    if (previousTaskId != -1) {
      TaskDetail previousTask = getProjectManagerBm().getTask(previousTaskId);
      if (previousTask != null) {
        currentTask.setPreviousTaskName(previousTask.getNom());
      }
    }
    if (getAttachments) {
      // fichiers joint à la tâche
      currentTask.setAttachments(getAttachments(id));
    }
    return currentTask;
  }

  public TaskDetail getTaskByTodoId(String todoId) {
    currentTask = getProjectManagerBm().getTaskByTodoId(todoId);
    enrichirTask(currentTask);
    // fichiers joint à la tâche
    currentTask.setAttachments(getAttachments(String.valueOf(currentTask.getId())));
    return currentTask;
  }

  public TaskDetail getTaskMere(String mereId) {
    return getTaskMere(Integer.parseInt(mereId));
  }

  public TaskDetail getTaskMere(int mereId) {
    TaskDetail actionMere = getProjectManagerBm().getTask(mereId);
    if (actionMere != null) {
      // dates au format de l'utilisateur
      actionMere.setUiDateDebut(date2UIDate(actionMere.getDateDebut()));
      actionMere.setUiDateFin(date2UIDate(actionMere.getDateFin()));
    }
    return actionMere;
  }

  public List<SimpleDocument> getAttachments(String id) {
    ForeignPK foreignKey = new ForeignPK(id, getComponentId());
    return AttachmentServiceFactory.getAttachmentService().listDocumentsByForeignKey(foreignKey,
        null);
  }

  public int addTask(TaskDetail task) {
    task.setInstanceId(getComponentId());
    task.setOrganisateurId(Integer.parseInt(getUserId()));
    task.setEstDecomposee(0);
    // calcul la date de fin
    task.setDateFin(processEndDate(task));
    if (getCurrentTask() != null) {
      // ajout d'une sous tâche
      task.setMereId(getCurrentTask().getId());
      task.setPath(getCurrentTask().getPath());
    } else {
      if (getCurrentProject() != null) {
        // ajout d'une tâche au projet
        task.setMereId(getCurrentProject().getId());
        task.setPath(getCurrentProject().getPath());
      } else {
        task.setMereId(-1);
      }
    }
    // ajout des zones manquantes dans les resources
    Collection<TaskResourceDetail> resources = task.getResources();
    Iterator<TaskResourceDetail> it = resources.iterator();
    while (it.hasNext()) {
      TaskResourceDetail resourceDetail = it.next();
      resourceDetail.setInstanceId(getComponentId());
      resourceDetail.setTaskId(task.getId());
    }

    // reinitialise le filtre
    filtre = null;

    return getProjectManagerBm().addTask(task);
  }

  public void removeTask(String id) {
    SilverTrace.info("projectManager", "ProjectManagerSessionController.removeTask()",
        "root.MSG_GEN_ENTER_METHOD", "id=" + id);
    getProjectManagerBm().removeTask(Integer.parseInt(id), getComponentId());
    currentTask = null;
  }

  public void updateCurrentTask() {
    getProjectManagerBm().updateTask(getCurrentTask(), getUserId());
  }

  public String initUserPanel() {
    String urlContext = URLManager.getApplicationURL();
    String hostUrl = urlContext + URLManager.getURL(getSpaceId(), getComponentId())
        + "FromUserPanel";
    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName(getSpaceLabel());
    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(hostUrl);
    sel.setMultiSelect(false);
    sel.setPopupMode(false);
    sel.setSetSelectable(false);
    PairObject hostComponentName = new PairObject(getComponentLabel(), null);
    sel.setHostPath(null);
    sel.setHostComponentName(hostComponentName);
    sel.setFirstPage(Selection.FIRST_PAGE_SEARCH_ELEMENT);

    ArrayList<String> roles = new ArrayList<String>();
    roles.add("admin");
    roles.add("responsable");

    // Add extra params
    SelectionUsersGroups sug = new SelectionUsersGroups();
    sug.setComponentId(getComponentId());
    sug.setProfileNames(roles);
    sel.setExtraParams(sug);

    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  public String initUserSelect() {
    String urlContext = URLManager.getApplicationURL();
    String hostUrl = urlContext + URLManager.getURL(getSpaceId(), getComponentId())
        + "FromUserSelect";
    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName(getSpaceLabel());
    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(hostUrl);
    sel.setMultiSelect(true);
    sel.setPopupMode(false);
    sel.setSetSelectable(false);
    PairObject hostComponentName = new PairObject(getComponentLabel(), null);
    sel.setHostPath(null);
    sel.setHostComponentName(hostComponentName);
    sel.setFirstPage(Selection.FIRST_PAGE_SEARCH_ELEMENT);

    ArrayList<String> roles = new ArrayList<String>();
    roles.add("admin");
    roles.add("responsable");

    // Add extra params
    SelectionUsersGroups sug = new SelectionUsersGroups();
    sug.setComponentId(getComponentId());
    sug.setProfileNames(roles);
    sel.setExtraParams(sug);

    String[] users = new String[currentResources.size()];
    int i = 0;
    Iterator<TaskResourceDetail> it = currentResources.iterator();
    while (it.hasNext()) {
      TaskResourceDetail resource = it.next();
      users[i] = resource.getUserId();
      i++;
    }
    sel.setSelectedElements(users);

    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  public boolean isFiltreActif() {
    return filtreActif;
  }

  /**
   * @param b
   */
  public void setFiltreActif(boolean b) {
    filtreActif = b;
  }

  /**
   * @return
   */
  public Filtre getFiltre() {
    return filtre;
  }

  /**
   * @param filtre
   */
  public void setFiltre(Filtre filtre) {
    this.filtre = filtre;
  }

  /**
   * *************************************************************************
   */
  /**
   * GESTION du projet / **************************************************************************
   */
  public boolean isProjectDefined() {
    if (projectDefined == null) {
      List<TaskDetail> projects = getProjectManagerBm().getProjects(getComponentId());
      if (!projects.isEmpty()) {
        projectDefined = Boolean.TRUE;
        currentProject = projects.get(0);
      } else {
        projectDefined = Boolean.FALSE;
      }
    }
    return projectDefined.booleanValue();
  }

  /**
   * Create a new project
   *
   * @param project the new TaskDetail project
   */
  public void createProject(TaskDetail project) {
    project.setInstanceId(getComponentId());
    project.setOrganisateurId(getUserId());
    project.setMereId(-1);
    int currentProjectId = getProjectManagerBm().addTask(project);
    this.currentProject = getTask(Integer.toString(currentProjectId));
    projectDefined = Boolean.TRUE;
  }

  /**
   * @return
   */
  public TaskDetail getCurrentProject() {
    return currentProject;
  }

  public void setCurrentProject(TaskDetail currentProject) {
    this.currentProject = currentProject;
  }

  public Date getEndDateOfCurrentProjet() {
    TaskDetail mostDistantTask = getProjectManagerBm().getMostDistantTask(getComponentId(),
        getCurrentProject().getId());
    if (mostDistantTask != null) {
      return mostDistantTask.getDateFin();
    }
    return null;
  }

  public void updateCurrentProject() {
    getProjectManagerBm().updateTask(getCurrentProject(), getUserId());
  }

  /**
   * *************************************************************************
   */
  /**
   * Gestion des jours non travaillés /
   * **************************************************************************
   */
  /**
   * Change le statut de la date
   *
   * @param date la date
   * @param nextStatus le nouveau statut de la date
   */
  public void changeDateStatus(String date, String nextStatus) throws ParseException {
    int status = Integer.parseInt(nextStatus);
    HolidayDetail holiday = new HolidayDetail(uiDate2Date(date),
        getCurrentProject().getId(), getComponentId());
    if (status == WORKING_DAY) {
      // le jour devient un jour travaillé
      getProjectManagerBm().removeHolidayDate(holiday);
    } else {
      // le jour devient un jour non travaillé
      getProjectManagerBm().addHolidayDate(holiday);
    }
    // recalcule pour toutes les tâches du projet
    // toutes les dates de début et de fin
    calculateAllTasksDates();
  }

  public void changeDayOfWeekStatus(String year, String month, String day) throws ParseException {
    SilverTrace.info("projectManager", "ProjectManagerSessionController.changeDayOfWeekStatus()",
        "root.MSG_GEN_ENTER_METHOD", "year=" + year + ", month=" + month + ", day=" + day);
    int iMonth = Integer.parseInt(month);
    getCalendar().set(Calendar.YEAR, Integer.parseInt(year));
    getCalendar().set(Calendar.MONTH, iMonth);
    getCalendar().set(Calendar.DATE, 1);
    // on se place sur le premier jour du mois
    // correspondant au jour de la semaine passé en paramêtre
    while (getCalendar().get(Calendar.DAY_OF_WEEK) != Integer.parseInt(day)) {
      getCalendar().add(Calendar.DATE, 1);
    }
    Date date = getCalendar().getTime();
    SilverTrace.info("projectManager", "ProjectManagerSessionController.changeDayOfWeekStatus()",
        "root.MSG_GEN_PARAM_VALUE", "date=" + date);
    HolidayDetail holidayDate = new HolidayDetail(date, getCurrentProject().getId(),
        getComponentId());
    boolean isHoliday = getProjectManagerBm().isHolidayDate(holidayDate);
    List<HolidayDetail> holidayDates = new ArrayList<HolidayDetail>();
    while (getCalendar().get(Calendar.MONTH) == iMonth) {
      holidayDates.add(new HolidayDetail(getCalendar().getTime(), getCurrentProject().getId(),
          getComponentId()));
      getCalendar().add(Calendar.DATE, 7);
    }
    if (isHoliday) {
      getProjectManagerBm().removeHolidayDates(holidayDates);
    } else {
      getProjectManagerBm().addHolidayDates(holidayDates);
    }
    // recalcule pour toutes les tâches du projet
    // toutes les dates de début et de fin
    calculateAllTasksDates();
  }

  public List<Date> getHolidayDates() {
    return getProjectManagerBm().getHolidayDates(getComponentId());
  }

  public void calculateAllTasksDates() {
    getProjectManagerBm().calculateAllTasksDates(getComponentId(), getCurrentProject().getId(),
        getUserId());
  }

  /**
   * *************************************************************************
   */
  /**
   * Méthodes utilitaires /
   * **************************************************************************
   */
  public Date uiDate2Date(String uiDate) throws ParseException {
    if (StringUtil.isDefined(uiDate)) {
      return DateUtil.stringToDate(uiDate, getLanguage());
    }
    return null;
  }

  public String date2UIDate(Date date) {
    if (date != null) {
      return DateUtil.getInputDate(date, getLanguage());
    }
    return null;
  }

  public String getUserFullName(UserDetail user) {
    if (user != null) {
      return user.getFirstName() + " " + user.getLastName();
    }
    return getUserDetail().getFirstName() + " " + getUserDetail().getLastName();
  }

  public String getUserFullName() {
    return getUserFullName(null);
  }

  public Calendar getCalendar() {
    if (calendar == null) {
      calendar = Calendar.getInstance();
    }
    return calendar;
  }

  /**
   * @return an instance of ProjectManagerBm (Project Manager Business Model class)
   */
  private ProjectManagerBm getProjectManagerBm() {
    if (projectManagerBm == null) {
      try {
        projectManagerBm = EJBUtilitaire.getEJBObjectRef(JNDINames.PROJECTMANAGERBM_EJBHOME,
            ProjectManagerBm.class);
      } catch (Exception e) {
        throw new ProjectManagerRuntimeException(
            "ProjectManagerSessionController.getProjectManagerBm()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return projectManagerBm;
  }

  /**
   * @return String representation of highest user role
   */
  public String getRole() {
    String[] roles = getUserRoles();
    String higherRole = "lecteur";
    for (int i = 0; i < roles.length; i++) {
      String role = roles[i];
      // if admin, return it, we won't find a better profile
      if ("admin".equals(role)) {
        return role;
      }
      if ("responsable".equals(role)) {
        higherRole = role;
      }
    }
    return higherRole;
  }

  public void index() {
    getProjectManagerBm().index(getComponentId());
  }

  public void setCurrentResources(Collection<TaskResourceDetail> resources) {
    currentResources = resources;
  }

  public Collection<TaskResourceDetail> getCurrentResources() {
    return currentResources;
  }

  /**
   * @param task
   */
  public void updateOccupation(TaskDetail task) {
    try {
      Date dateDeb = task.getDateDebut();
      Date dateFin = task.getDateFin();

      Collection<TaskResourceDetail> resources = task.getResources();
      for (TaskResourceDetail resource : resources) {
        String userId = resource.getUserId();
        resource.setOccupation(getProjectManagerBm().getOccupationByUser(userId, dateDeb, dateFin));
      }
    } catch (Exception e) {
      throw new ProjectManagerRuntimeException("ProjectManagerSessionController.updateOccupation()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /**
   * @param taskId
   * @param userId
   * @return
   */
  public int checkOccupation(String taskId, String userId) {
    try {
      TaskDetail task = getTask(taskId);
      Date dateDeb = task.getDateDebut();
      Date dateFin = task.getDateFin();
      return getProjectManagerBm().getOccupationByUser(userId, dateDeb, dateFin, task.getId());
    } catch (Exception e) {
      throw new ProjectManagerRuntimeException(
          "ProjectManagerSessionController.updateOccupation()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /**
   * @param taskId
   * @param userId
   * @param beginDate
   * @param endDate
   * @return
   */
  public int checkOccupation(String taskId, String userId, Date beginDate, Date endDate) {
    try {
      return getProjectManagerBm().getOccupationByUser(userId, beginDate, endDate,
          Integer.parseInt(taskId));
    } catch (Exception e) {
      throw new ProjectManagerRuntimeException("ProjectManagerSessionController.updateOccupation()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /**
   * @param userId
   * @param beginDate
   * @param endDate
   * @return
   */
  public int checkOccupation(String userId, Date beginDate, Date endDate) {
    try {
      return getProjectManagerBm().getOccupationByUser(userId, beginDate, endDate);
    } catch (Exception e) {
      throw new ProjectManagerRuntimeException("ProjectManagerSessionController.updateOccupation()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /**
   * @param startDate the date from which we get the current month
   * @return the Month Value Object of the date's month parameter.
   */
  public MonthVO getMonthVO(Date startDate) {
    // Building Month ValueObject in order to prepare view
    Calendar curDayCal = new GregorianCalendar();
    curDayCal.setTime(startDate);
    ResourceLocator resource = new ResourceLocator("org.silverpeas.multilang.generalMultilang",
        this.getLanguage());
    int nbDaysDisplayed = curDayCal.getActualMaximum(Calendar.DAY_OF_MONTH);
    int currentWeek = -1;
    int numWeekInYear = curDayCal.get(Calendar.WEEK_OF_YEAR);
    int oldNumWeekInYear = numWeekInYear;
    List<WeekVO> weeks = new ArrayList<WeekVO>();
    List<DayVO> curDays = new ArrayList<DayVO>();
    for (int i = 1; i < nbDaysDisplayed + 1; i++) {
      curDayCal.set(Calendar.DAY_OF_MONTH, i);
      int numDayinWeek = curDayCal.get(Calendar.DAY_OF_WEEK);
      numWeekInYear = curDayCal.get(Calendar.WEEK_OF_YEAR);
      // GML.jour || GML.shortJour
      DayVO curDay = new DayVO(Integer.toString(i), resource.getString("GML.jour" + numDayinWeek,
          "?"), curDayCal.getTime());
      if (numWeekInYear != currentWeek && currentWeek != -1) {
        WeekVO week = new WeekVO(curDays, Integer.toString(oldNumWeekInYear));
        weeks.add(week);
        currentWeek = numWeekInYear;
        curDays = new ArrayList<DayVO>();
      } else {
        currentWeek = numWeekInYear;
      }
      curDays.add(curDay);
      if (i == nbDaysDisplayed) {
        WeekVO week = new WeekVO(curDays, Integer.toString(numWeekInYear));
        weeks.add(week);
      }
      oldNumWeekInYear = numWeekInYear;
    }
    MonthVO curMonth = new MonthVO(weeks, Integer.toString(curDayCal.get(Calendar.MONTH)),
        nbDaysDisplayed);
    return curMonth;
  }

  /**
   * @param curDate If null parameter we get the current month system.
   * @return a quarter which contains 3 month value object starting from start date month
   * @throws ParseException
   */
  public List<MonthVO> getQuarterMonth(Date curDate) throws ParseException {
    return getNbMonth(3, curDate);
  }

  /**
   * @param curDate If null parameter we get the current month system.
   * @return a quarter which contains 3 month value object starting from start date month
   * @throws ParseException
   */
  public List<MonthVO> getYearMonth(Date curDate) throws ParseException {
    return getNbMonth(12, curDate);
  }

  /**
   * @param nbMonth the number of month to return starting from curDate's month
   * @param curDate the current starting month date. If null parameter we get the current month
   * system.
   * @return a list
   * @throws ParseException
   */
  private List<MonthVO> getNbMonth(int nbMonth, Date curDate) {
    // Result list decaration
    List<MonthVO> months = new ArrayList<MonthVO>();
    if (nbMonth <= 0) {
      return months;
    }
    // Initialize date
    Calendar curCal = Calendar.getInstance();
    if (curDate != null) {
      curCal.setTime(curDate);
    }
    for (int cptMonth = 0; cptMonth < nbMonth; cptMonth++) {
      MonthVO curMonth = this.getMonthVO(curCal.getTime());
      months.add(curMonth);
      curCal.add(Calendar.MONTH, 1);
    }
    return months;
  }

  /**
   * Retrieve the most relevant date if the current date parameter is null.<br/>
   *
   * @param startDate the string representation of the date. If null we will retrieve the most
   * relevant date in order to display current tasks
   * @return the most relevant date
   * @throws ParseException
   */
  public Date getMostRelevantDate(String startDate) throws ParseException {
    Date curDate;
    if (startDate != null) {
      curDate = DateUtil.stringToDate(startDate, I18NHelper.defaultLanguage);
    } else {
      // Search for the most relevant date
      curDate = getProjectManagerRelevantDate();
    }
    return curDate;
  }

  /**
   * Here is a method which uses existing service to retrieve the most relevant date. We already
   * uses this algorithm :<br/>
   * <ol>
   * <li>if tasks exist in the current month we return current date</li>
   * <li>else if we display following months if contains tasks</li>
   * <li>else if we display preceding months if contains tasks</li>
   * <li>else if we dispay current month</li>
   * </ol>
   *
   * @return the most relevant date.
   */
  private Date getProjectManagerRelevantDate() {
    // Initialize date variable
    Calendar cal = Calendar.getInstance();
    Date relevantDate = cal.getTime();
    cal.set(Calendar.DAY_OF_MONTH, 1);
    Date firstDayMonth = cal.getTime();
    cal.add(Calendar.MONTH, 1);
    Date lastDayMonth = cal.getTime();

    // Initialize loop variable
    Date nextDate = null;
    Date precedingDate = null;
    boolean isAfterCurrentMonth = false;
    boolean isBeforeCurrentMonth = false;

    // Search for the most relevant date
    try {
      List<TaskDetail> tasks = this.getAllTasks();
      for (TaskDetail taskDetail : tasks) {
        if (taskDetail.getMereId() != -1) {
          Date beginDate = taskDetail.getDateDebut();
          Date endDate = taskDetail.getDateFin();
          if (beginDate.after(lastDayMonth)) {
            // Current task is after current month
            isAfterCurrentMonth = true;
            if (nextDate != null) {
              if (beginDate.before(nextDate)) {
                nextDate = beginDate;
              }
            } else {
              nextDate = beginDate;
            }
          } else if (endDate.before(firstDayMonth)) {
            isBeforeCurrentMonth = true;
            if (precedingDate != null) {
              if (endDate.after(precedingDate)) {
                precedingDate = beginDate;
              }
            } else {
              precedingDate = endDate;
            }
          } else {
            //It's the current month so we return it and stop the algorithm
            return firstDayMonth;
          }
        }
      }
    } catch (Exception e) {
      SilverTrace.warn(getComponentName(), ProjectManagerSessionController.class.getName(),
          "Problem to retrieve all the tasks", e);
    }
    if (isAfterCurrentMonth) {
      relevantDate = nextDate;
    } else if (isBeforeCurrentMonth) {
      relevantDate = precedingDate;
    }
    return relevantDate;
  }
}
