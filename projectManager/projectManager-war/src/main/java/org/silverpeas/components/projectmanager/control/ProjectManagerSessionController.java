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
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.projectmanager.control;

import org.silverpeas.components.projectmanager.model.Filtre;
import org.silverpeas.components.projectmanager.model.HolidayDetail;
import org.silverpeas.components.projectmanager.model.ProjectManagerRuntimeException;
import org.silverpeas.components.projectmanager.model.TaskDetail;
import org.silverpeas.components.projectmanager.model.TaskResourceDetail;
import org.silverpeas.components.projectmanager.service.ProjectManagerService;
import org.silverpeas.components.projectmanager.vo.DayVO;
import org.silverpeas.components.projectmanager.vo.MonthVO;
import org.silverpeas.components.projectmanager.vo.WeekVO;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.io.upload.UploadedFile;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.csv.CSVRow;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.export.ExportCSVBuilder;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.selection.SelectionUsersGroups;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.stream.Collectors;

/**
 * This class contains all the business model for project manager component
 */
public class ProjectManagerSessionController extends AbstractComponentSessionController {
  private static final long serialVersionUID = 1783287523929344966L;

  private static final String RESPONSABLE_ROLE = "responsable";
  private static final String ADMIN_ROLE = "admin";
  /**
   * Project manager EJB
   */
  private transient ProjectManagerService projectManagerService = null;
  private TaskDetail currentTask = null;
  private Boolean projectDefined = null;
  private TaskDetail currentProject = null;
  // Current resource collection
  private Collection<TaskResourceDetail> currentResources = null;
  private boolean filtreActif = false;
  private Filtre filtre = null;
  private List<Integer> unfoldTasks = new ArrayList<>();
  private Calendar calendar = null;
  private static final int WORKING_DAY = 0;

  public ProjectManagerSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.projectManager.multilang.projectManagerBundle",
        "org.silverpeas.projectManager.settings.projectManagerIcons",
        "org.silverpeas.projectManager.settings.projectManagerSettings");
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
    return unfoldTasks.contains(actionId);
  }

  public List<TaskDetail> getAllTasks() {
    List<TaskDetail> tasks = getProjectManagerService().getAllTasks(getComponentId(), getFiltre());
    for (TaskDetail task : tasks) {
      enrichirTask(task);
    }
    return tasks;
  }

  public List<TaskDetail> getTasks() {
    currentTask = null;
    final List<TaskDetail> tasks = getProjectManagerService()
        .getTasksByMotherId(getComponentId(), getCurrentProject().getId());
    final List<TaskDetail> taskTree = new ArrayList<>();
    for (final TaskDetail task : tasks) {
      buildTaskTreeWithoutFiltering(taskTree, task, null, 0);
    }
    return applyFilter(taskTree);
  }

  private List<TaskDetail> applyFilter(final List<TaskDetail> taskTree) {
    return getFiltre() != null
        ? taskTree.stream().filter(getFiltre()::matches).collect(Collectors.toList())
        : taskTree;
  }

  private void buildTaskTreeWithoutFiltering(List<TaskDetail> taskTree, TaskDetail task,
      TaskDetail parentTask, int level) {
    enrichirTask(task);
    task.setAttachments(getAttachments(Integer.toString(task.getId())));
    task.setLevel(level);
    if (SilverpeasRole.admin.isInRole(getRole())) {
      task.setDeletionAvailable(true);
      task.setUpdateAvailable(true);
    } else {
      if (parentTask != null && RESPONSABLE_ROLE.equals(getRole())
          && parentTask.getResponsableId() == Integer.parseInt(getUserId())) {
        task.setDeletionAvailable(true);
        task.setUpdateAvailable(true);
      } else if (RESPONSABLE_ROLE.equals(getRole())
          && task.getResponsableId() == Integer.parseInt(getUserId())) {
        task.setUpdateAvailable(true);
      }
    }
    if (isUnfoldTask(task.getId())) {
      task.setUnfold(true);
      taskTree.add(task);
      final List<TaskDetail> subTasks = getProjectManagerService().getTasksByMotherId(
          getComponentId(), task.getId());
      level++;
      for (TaskDetail subTask : subTasks) {
        buildTaskTreeWithoutFiltering(taskTree, subTask, task, level);
      }
    } else {
      task.setUnfold(false);
      taskTree.add(task);
    }
  }

  public List<TaskDetail> getTasks(String id) {
    final List<TaskDetail> tasks = getProjectManagerService()
        .getTasksByMotherId(getComponentId(), Integer.parseInt(id));
    for (final TaskDetail task : tasks) {
      enrichirTask(task);
    }
    return applyFilter(tasks);
  }

  public List<TaskDetail> getPotentialPreviousTasks() {
    return getPotentialPreviousTasks(false);
  }

  public List<TaskDetail> getPotentialPreviousTasks(boolean onCreation) {
    int motherId = getCurrentProject().getId(); // par défaut, on est au niveau du projet
    if (getCurrentTask() != null) {
      // On est au niveau d'une tache
      if (onCreation) {
        motherId = getCurrentTask().getId();
      } else {
        motherId = getCurrentTask().getMereId();
      }
    }
    List<TaskDetail> previousTasks = getProjectManagerService().getTasksByMotherId(getComponentId(),
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
    return getProjectManagerService().processEndDate(task);
  }

  public Date processEndDate(String taskId, String charge, Date dateDebut) {
    TaskDetail task = getTask(taskId);
    task.setCharge(charge);
    task.setDateDebut(dateDebut);
    return getProjectManagerService().processEndDate(task);
  }

  public Date processEndDate(String charge, Date dateDebut, String instanceId) {
    return getProjectManagerService().processEndDate(Float.parseFloat(charge), instanceId, dateDebut);
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
    List<Date> holidayDates = getProjectManagerService().getHolidayDates(getComponentId());
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
    currentTask = getProjectManagerService().getTask(Integer.parseInt(id));
    enrichirTask(currentTask);
    // récupération du nom de la tâche précédente
    int previousTaskId = currentTask.getPreviousTaskId();
    if (previousTaskId != -1) {
      TaskDetail previousTask = getProjectManagerService().getTask(previousTaskId);
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
    currentTask = getProjectManagerService().getTaskByTodoId(todoId);
    enrichirTask(currentTask);
    // fichiers joint à la tâche
    currentTask.setAttachments(getAttachments(String.valueOf(currentTask.getId())));
    return currentTask;
  }

  public TaskDetail getTaskMere(String mereId) {
    return getTaskMere(Integer.parseInt(mereId));
  }

  public TaskDetail getTaskMere(int mereId) {
    TaskDetail actionMere = getProjectManagerService().getTask(mereId);
    if (actionMere != null) {
      // dates au format de l'utilisateur
      actionMere.setUiDateDebut(date2UIDate(actionMere.getDateDebut()));
      actionMere.setUiDateFin(date2UIDate(actionMere.getDateFin()));
    }
    return actionMere;
  }

  private List<SimpleDocument> getAttachments(String id) {
    ResourceReference foreignKey = new ResourceReference(id, getComponentId());
    return AttachmentServiceProvider.getAttachmentService().listDocumentsByForeignKey(foreignKey,
        null);
  }

  public int addTask(TaskDetail task, Collection<UploadedFile> uploadedFiles) {
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

    return getProjectManagerService().addTask(task, uploadedFiles);
  }

  public void removeTask(String id) {

    getProjectManagerService().removeTask(Integer.parseInt(id), getComponentId());
    currentTask = null;
  }

  public void updateCurrentTask() {
    getProjectManagerService().updateTask(getCurrentTask(), getUserId());
  }

  public String initUserSelect() {
    String urlContext = URLUtil.getApplicationURL();
    String hostUrl = urlContext + URLUtil.getURL(getSpaceId(), getComponentId())
        + "FromUserSelect";
    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName(getSpaceLabel());
    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(hostUrl);
    sel.setMultiSelect(true);
    sel.setPopupMode(false);
    sel.setSetSelectable(false);
    Pair<String, String> hostComponentName = new Pair<>(getComponentLabel(), null);
    sel.setHostPath(null);
    sel.setHostComponentName(hostComponentName);

    ArrayList<String> roles = new ArrayList<>();
    roles.add(ADMIN_ROLE);
    roles.add(RESPONSABLE_ROLE);

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

    return Selection.getSelectionURL();
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
      List<TaskDetail> projects = getProjectManagerService().getProjects(getComponentId());
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
  public void createProject(TaskDetail project, Collection<UploadedFile> uploadedFiles) {
    project.setInstanceId(getComponentId());
    project.setOrganisateurId(getUserId());
    project.setMereId(-1);
    int currentProjectId = getProjectManagerService().addTask(project, uploadedFiles);
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
    TaskDetail mostDistantTask = getProjectManagerService().getMostDistantTask(getComponentId(),
        getCurrentProject().getId());
    if (mostDistantTask != null) {
      return mostDistantTask.getDateFin();
    }
    return null;
  }

  public void updateCurrentProject() {
    getProjectManagerService().updateTask(getCurrentProject(), getUserId());
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
      getProjectManagerService().removeHolidayDate(holiday);
    } else {
      // le jour devient un jour non travaillé
      getProjectManagerService().addHolidayDate(holiday);
    }
    // recalcule pour toutes les tâches du projet
    // toutes les dates de début et de fin
    calculateAllTasksDates();
  }

  public void changeDayOfWeekStatus(String year, String month, String day) {

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

    HolidayDetail holidayDate = new HolidayDetail(date, getCurrentProject().getId(),
        getComponentId());
    boolean isHoliday = getProjectManagerService().isHolidayDate(holidayDate);
    List<HolidayDetail> holidayDates = new ArrayList<>();
    while (getCalendar().get(Calendar.MONTH) == iMonth) {
      holidayDates.add(new HolidayDetail(getCalendar().getTime(), getCurrentProject().getId(),
          getComponentId()));
      getCalendar().add(Calendar.DATE, 7);
    }
    if (isHoliday) {
      getProjectManagerService().removeHolidayDates(holidayDates);
    } else {
      getProjectManagerService().addHolidayDates(holidayDates);
    }
    // recalcule pour toutes les tâches du projet
    // toutes les dates de début et de fin
    calculateAllTasksDates();
  }

  public List<Date> getHolidayDates() {
    return getProjectManagerService().getHolidayDates(getComponentId());
  }

  public void calculateAllTasksDates() {
    getProjectManagerService().calculateAllTasksDates(getComponentId(), getCurrentProject().getId(),
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
   * @return an instance of ProjectManagerService (Project Manager Business Model class)
   */
  private ProjectManagerService getProjectManagerService() {
    if (projectManagerService == null) {
      projectManagerService = ProjectManagerService.get();
    }
    return projectManagerService;
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
      if (ADMIN_ROLE.equals(role)) {
        return role;
      }
      if (RESPONSABLE_ROLE.equals(role)) {
        higherRole = role;
      }
    }
    return higherRole;
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
        resource.setOccupation(
            getProjectManagerService().getOccupationByUser(userId, dateDeb, dateFin));
      }
    } catch (Exception e) {
      throw new ProjectManagerRuntimeException(e);
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
      return getProjectManagerService().getOccupationByUser(userId, dateDeb, dateFin, task.getId());
    } catch (Exception e) {
      throw new ProjectManagerRuntimeException(e);
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
      return getProjectManagerService().getOccupationByUser(userId, beginDate, endDate,
          Integer.parseInt(taskId));
    } catch (Exception e) {
      throw new ProjectManagerRuntimeException(e);
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
      return getProjectManagerService().getOccupationByUser(userId, beginDate, endDate);
    } catch (Exception e) {
      throw new ProjectManagerRuntimeException(e);
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
    LocalizationBundle resource =
        ResourceLocator.getLocalizationBundle("org.silverpeas.multilang.generalMultilang",
        this.getLanguage());
    int nbDaysDisplayed = curDayCal.getActualMaximum(Calendar.DAY_OF_MONTH);
    int currentWeek = -1;
    int numWeekInYear = curDayCal.get(Calendar.WEEK_OF_YEAR);
    int oldNumWeekInYear = numWeekInYear;
    List<WeekVO> weeks = new ArrayList<>();
    List<DayVO> curDays = new ArrayList<>();
    for (int i = 1; i < nbDaysDisplayed + 1; i++) {
      curDayCal.set(Calendar.DAY_OF_MONTH, i);
      int numDayinWeek = curDayCal.get(Calendar.DAY_OF_WEEK);
      numWeekInYear = curDayCal.get(Calendar.WEEK_OF_YEAR);
      // GML.jour || GML.shortJour
      String translation;
      try {
        translation = resource.getString("GML.jour" + numDayinWeek);
      } catch (MissingResourceException ex) {
        translation = "?";
      }
      DayVO curDay = new DayVO(Integer.toString(i), translation, curDayCal.getTime());
      if (numWeekInYear != currentWeek && currentWeek != -1) {
        WeekVO week = new WeekVO(curDays, Integer.toString(oldNumWeekInYear));
        weeks.add(week);
        currentWeek = numWeekInYear;
        curDays = new ArrayList<>();
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
    return new MonthVO(weeks, Integer.toString(curDayCal.get(Calendar.MONTH)),
        nbDaysDisplayed);
  }

  /**
   * @param curDate If null parameter we get the current month system.
   * @return a quarter which contains 3 month value object starting from start date month
   * @throws ParseException
   */
  public List<MonthVO> getQuarterMonth(Date curDate) {
    return getNbMonth(3, curDate);
  }

  /**
   * @param curDate If null parameter we get the current month system.
   * @return a quarter which contains 3 month value object starting from start date month
   * @throws ParseException
   */
  public List<MonthVO> getYearMonth(Date curDate) {
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
    List<MonthVO> months = new ArrayList<>();
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
   * Retrieve the most relevant date if the current date parameter is null.<br>
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
   * uses this algorithm :<br>
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
      SilverLogger.getLogger(this).error(e);
    }
    if (isAfterCurrentMonth) {
      relevantDate = nextDate;
    } else if (isBeforeCurrentMonth) {
      relevantDate = precedingDate;
    }
    return relevantDate;
  }

  public ExportCSVBuilder export() {

    ExportCSVBuilder csvBuilder = new ExportCSVBuilder();

    CSVRow header = new CSVRow();
    header.addCell(getString("projectManager.TacheStatut"));
    header.addCell(getString("projectManager.TacheNumero"));
    header.addCell(getString("projectManager.TacheNom"));
    header.addCell(getString("projectManager.TacheResponsable"));
    header.addCell(getString("projectManager.TacheDebut"));
    header.addCell(getString("projectManager.TacheFin"));
    header.addCell(getString("projectManager.TacheCharge"));
    header.addCell(getString("projectManager.TacheConso"));
    header.addCell(getString("projectManager.TacheReste"));
    csvBuilder.setHeader(header);

    List<TaskDetail> allTasks = getAllTasks();
    for (TaskDetail task : allTasks) {
      CSVRow row = new CSVRow();
      if (task.getChrono() != 0) {
        row.addCell(getTaskStatusAsString(task));
        row.addCell(task.getChrono());
        row.addCell(task.getNom());
        row.addCell(task.getResponsableFullName());
        row.addCell(task.getUiDateDebut());
        row.addCell(task.getUiDateFin());
        row.addCell(task.getCharge());
        row.addCell(task.getConsomme());
        row.addCell(task.getRaf());
        csvBuilder.addLine(row);
      }
    }

    return csvBuilder;
  }

  private String getTaskStatusAsString(TaskDetail task) {
    String status;
    switch (task.getStatut()) {
      case 0:
        status = getString("projectManager.TacheStatutEnCours");
        break;
      case 1:
        status = getString("projectManager.TacheStatutGelee");
        break;
      case 2:
        status = getString("projectManager.TacheStatutAbandonnee");
        break;
      case 3:
        status = getString("projectManager.TacheStatutRealisee");
        break;
      case 4:
        status = getString("projectManager.TacheStatutEnAlerte");
        break;
      default:
        status = getString("projectManager.TacheAvancementND");
    }
    return status;
  }

}