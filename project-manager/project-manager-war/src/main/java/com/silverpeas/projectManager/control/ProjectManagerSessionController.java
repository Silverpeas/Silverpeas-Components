package com.silverpeas.projectManager.control;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.projectManager.control.ejb.ProjectManagerBm;
import com.silverpeas.projectManager.control.ejb.ProjectManagerBmHome;
import com.silverpeas.projectManager.model.Filtre;
import com.silverpeas.projectManager.model.HolidayDetail;
import com.silverpeas.projectManager.model.ProjectManagerRuntimeException;
import com.silverpeas.projectManager.model.TaskDetail;
import com.silverpeas.projectManager.model.TaskResourceDetail;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class ProjectManagerSessionController extends
    AbstractComponentSessionController {
  private ProjectManagerBm projectManagerBm = null;
  private TaskDetail currentTask = null;
  private Boolean projectDefined = null;
  private TaskDetail currentProject = null;
  // pour conserver les resources courantes
  private Collection currentResources = null;

  boolean filtreActif = false;
  Filtre filtre = null;

  List unfoldTasks = new ArrayList();
  Calendar calendar = null;

  public static int WORKING_DAY = 0;
  public static int HOLIDAY_DAY = 1;

  public ProjectManagerSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.silverpeas.projectManager.multilang.projectManagerBundle",
        "com.silverpeas.projectManager.settings.projectManagerIcons");
  }

  public TaskDetail getCurrentTask() {
    return currentTask;
  }

  /*********************************************************************/
  /********** Gestion de l'arborescence des taches ********************/
  /*********************************************************************/
  public void addUnfoldTask(String id) {
    unfoldTasks.add(new Integer(id));
  }

  public void removeUnfoldTask(String id) {
    unfoldTasks.remove(new Integer(id));
  }

  public List getUnfoldTasks() {
    return unfoldTasks;
  }

  private boolean isUnfoldTask(int actionId) {
    return unfoldTasks.contains(new Integer(actionId));
  }

  /*********************************************************************/

  public List getAllTasks() throws RemoteException {
    List tasks = getProjectManagerBm().getAllTasks(getComponentId(),
        getFiltre());

    TaskDetail task = null;
    for (int a = 0; a < tasks.size(); a++) {
      task = (TaskDetail) tasks.get(a);

      enrichirTask(task);
    }
    return tasks;
  }

  public List getTasks() throws RemoteException {
    currentTask = null;

    List tasks = getProjectManagerBm().getTasksByMotherId(getComponentId(),
        getCurrentProject().getId(), getFiltre());

    List arbo = new ArrayList();

    TaskDetail task = null;
    for (int a = 0; a < tasks.size(); a++) {
      task = (TaskDetail) tasks.get(a);

      arbo = buildArbo(arbo, task, null, 0);
    }
    return arbo;
  }

  private List buildArbo(List arbo, TaskDetail task, TaskDetail actionMere,
      int level) throws RemoteException {
    SilverTrace.info("projectManager",
        "ProjectManagerSessionController.buildArbo()",
        "root.MSG_GEN_ENTER_METHOD", "arbo.size()=" + arbo.size()
            + ", actionId = " + task.getId() + ", level = " + level);

    enrichirTask(task);

    // fichiers joint à la tâche
    task.setAttachments(getAttachments(new Integer(task.getId()).toString()));

    task.setLevel(level);

    if (getRole().equals("admin")) {
      task.setDeletionAvailable(true);
      task.setUpdateAvailable(true);
    } else {
      if (actionMere != null
          && getRole().equals("responsable")
          && actionMere.getResponsableId() == new Integer(getUserId())
              .intValue()) {
        task.setDeletionAvailable(true);
        task.setUpdateAvailable(true);
      } else if (getRole().equals("responsable")
          && task.getResponsableId() == new Integer(getUserId()).intValue()) {
        task.setUpdateAvailable(true);
      }
    }

    if (isUnfoldTask(task.getId())) {
      task.setUnfold(true);
      arbo.add(task);

      // la tâche est dépliée
      List sousActions = getProjectManagerBm().getTasksByMotherId(
          getComponentId(), task.getId(), getFiltre());
      level++;
      TaskDetail sousAction = null;
      for (int a = 0; a < sousActions.size(); a++) {
        sousAction = (TaskDetail) sousActions.get(a);
        buildArbo(arbo, sousAction, task, level);
      }
    } else {
      task.setUnfold(false);
      arbo.add(task);
    }
    return arbo;
  }

  public List getTasks(String id) throws RemoteException {
    List tasks = getProjectManagerBm().getTasksByMotherId(getComponentId(),
        new Integer(id).intValue(), getFiltre());
    TaskDetail task = null;
    for (int a = 0; a < tasks.size(); a++) {
      task = (TaskDetail) tasks.get(a);
      enrichirTask(task);
    }
    return tasks;
  }

  public List getTasksNotCancelled(String id) throws RemoteException {
    List tasks = getProjectManagerBm().getTasksNotCancelledByMotherId(
        getComponentId(), new Integer(id).intValue(), getFiltre());
    TaskDetail task = null;
    for (int a = 0; a < tasks.size(); a++) {
      task = (TaskDetail) tasks.get(a);
      enrichirTask(task);
    }
    return tasks;
  }

  public List getPotentialPreviousTasks() throws RemoteException {
    return getPotentialPreviousTasks(false);
  }

  public List getPotentialPreviousTasks(boolean onCreation)
      throws RemoteException {
    List previousTasks = null;
    int motherId = getCurrentProject().getId(); // par défaut, on est au niveau
                                                // du projet
    if (getCurrentTask() != null) {
      // On est au niveau d'une tache
      if (onCreation)
        motherId = getCurrentTask().getId();
      else
        motherId = getCurrentTask().getMereId();
    }
    previousTasks = getProjectManagerBm().getTasksByMotherId(getComponentId(),
        motherId);

    // calcul de la date de debut de la nouvelle tache
    // par rapport à la date de fin de la tache precedente
    TaskDetail previousTask = null;
    for (int t = 0; t < previousTasks.size(); t++) {
      previousTask = (TaskDetail) previousTasks.get(t);

      previousTask.setCharge(previousTask.getCharge() + 1);
      previousTask
          .setUiDateDebutPlus1(date2UIDate(processEndDate(previousTask)));
      previousTask.setCharge(previousTask.getCharge() - 1);
    }
    return previousTasks;
  }

  /**
   * @param task
   *          la tache dont on veut calculer la date de fin
   * @return la date de fin (= dateDebut + Charge)
   */
  public Date processEndDate(TaskDetail task) throws RemoteException {
    task.setInstanceId(getComponentId());
    return getProjectManagerBm().processEndDate(task);
  }

  public Date processEndDate(String taskId, String charge, Date dateDebut)
      throws RemoteException {
    TaskDetail task = getTask(taskId);
    task.setCharge(charge);
    task.setDateDebut(dateDebut);
    return getProjectManagerBm().processEndDate(task);
  }

  public Date processEndDate(String charge, Date dateDebut, String instanceId)
      throws RemoteException {
    return getProjectManagerBm().processEndDate(Float.parseFloat(charge),
        instanceId, dateDebut);
  }

  /**
   * Vérifie la date de début d'une tâche. Si la date de début est un jour non
   * travaillé, la date de début sera le prochain jour travaillé.
   * 
   * @param task
   *          la tâche dont la date de début doit être vérifiée
   * @throws RemoteException
   */
  public void checkBeginDate(TaskDetail task) throws RemoteException {
    getCalendar().setTime(task.getDateDebut());

    // récupère les jours non travaillés
    List holidayDates = getProjectManagerBm().getHolidayDates(getComponentId());

    while (holidayDates.contains(getCalendar().getTime())) {
      getCalendar().add(Calendar.DATE, 1);
    }
    task.setDateDebut(getCalendar().getTime());
  }

  public void enrichirTask(TaskDetail task) {
    String responsableId = new Integer(task.getResponsableId()).toString();
    UserDetail responsable = getUserDetail(responsableId);
    task.setResponsableFullName(getUserFullName(responsable));

    String organisateurId = new Integer(task.getOrganisateurId()).toString();
    UserDetail cdp = getUserDetail(organisateurId);
    task.setOrganisateurFullName(getUserFullName(cdp));

    // dates au format de l'utilisateur
    task.setUiDateDebut(date2UIDate(task.getDateDebut()));
    task.setUiDateFin(date2UIDate(task.getDateFin()));

    // mettre les nom des user sur les resources
    Collection resources = task.getResources();
    Iterator it = resources.iterator();
    while (it.hasNext()) {
      TaskResourceDetail resource = (TaskResourceDetail) it.next();
      String userId = new Integer(resource.getUserId()).toString();
      UserDetail user = getUserDetail(userId);
      resource.setUserName(getUserFullName(user));
    }

    updateOccupation(task);
  }

  public TaskDetail getTask(String id) throws RemoteException {
    return getTask(id, true);
  }

  public TaskDetail getTask(String id, boolean getAttachments)
      throws RemoteException {
    currentTask = getProjectManagerBm().getTask(new Integer(id).intValue());

    enrichirTask(currentTask);

    // récupération du nom de la tâche précédente
    int previousTaskId = currentTask.getPreviousTaskId();
    if (previousTaskId != -1) {
      TaskDetail previousTask = getProjectManagerBm().getTask(previousTaskId);
      if (previousTask != null)
        currentTask.setPreviousTaskName(previousTask.getNom());
    }

    if (getAttachments) {
      // fichiers joint à la tâche
      currentTask.setAttachments(getAttachments(id));
    }

    return currentTask;
  }

  public TaskDetail getTaskByTodoId(String todoId) throws RemoteException {
    currentTask = getProjectManagerBm().getTaskByTodoId(todoId);

    enrichirTask(currentTask);

    // fichiers joint à la tâche
    currentTask.setAttachments(getAttachments(currentTask.getId()));

    return currentTask;
  }

  public TaskDetail getTaskMere(String mereId) throws RemoteException {
    return getTaskMere(new Integer(mereId).intValue());
  }

  public TaskDetail getTaskMere(int mereId) throws RemoteException {
    TaskDetail actionMere = getProjectManagerBm().getTask(mereId);
    if (actionMere != null) {
      // dates au format de l'utilisateur
      actionMere.setUiDateDebut(date2UIDate(actionMere.getDateDebut()));
      actionMere.setUiDateFin(date2UIDate(actionMere.getDateFin()));
    }
    return actionMere;
  }

  public Collection getAttachments(int id) {
    return getAttachments(new Integer(id).toString());
  }

  private Collection getAttachments(String id) {
    AttachmentPK foreignKey = new AttachmentPK(id, "useless", getComponentId());
    return AttachmentController
        .searchAttachmentByPKAndContext(foreignKey, null);
  }

  public int addTask(TaskDetail task) throws RemoteException {
    task.setInstanceId(getComponentId());
    task.setOrganisateurId(new Integer(getUserId()).intValue());
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
    Collection resources = task.getResources();
    Iterator it = resources.iterator();
    while (it.hasNext()) {
      TaskResourceDetail resourceDetail = (TaskResourceDetail) it.next();
      resourceDetail.setInstanceId(getComponentId());
      resourceDetail.setTaskId(task.getId());
    }

    // reinitialise le filtre
    filtre = null;

    return getProjectManagerBm().addTask(task);
  }

  public void removeTask(String id) throws RemoteException {
    SilverTrace.info("projectManager",
        "ProjectManagerSessionController.removeTask()",
        "root.MSG_GEN_ENTER_METHOD", "id=" + id);

    getProjectManagerBm().removeTask(new Integer(id).intValue(),
        getComponentId());

    currentTask = null;
  }

  public void updateCurrentTask() throws RemoteException {
    getProjectManagerBm().updateTask(getCurrentTask(), getUserId());
  }

  /******************************************************************************************************************/
  /**
   * UserPanel methods /
   ******************************************************************************************************************/
  public String initUserPanel() throws RemoteException {
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");
    // String hostUrl = m_context+"/RprojectManager/jsp/FromUserPanel";
    String hostUrl = m_context
        + URLManager.getURL(getSpaceId(), getComponentId()) + "FromUserPanel";

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

    ArrayList roles = new ArrayList();
    roles.add("admin");
    roles.add("responsable");

    // Add extra params
    SelectionUsersGroups sug = new SelectionUsersGroups();
    sug.setComponentId(getComponentId());
    sug.setProfileNames(roles);
    sel.setExtraParams(sug);

    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  public String initUserSelect() throws RemoteException {
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");
    // String hostUrl = m_context+"/RprojectManager/jsp/FromUserSelect";
    String hostUrl = m_context
        + URLManager.getURL(getSpaceId(), getComponentId()) + "FromUserSelect";

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

    ArrayList roles = new ArrayList();
    roles.add("admin");
    roles.add("responsable");

    // Add extra params
    SelectionUsersGroups sug = new SelectionUsersGroups();
    sug.setComponentId(getComponentId());
    sug.setProfileNames(roles);
    sel.setExtraParams(sug);

    String[] users = new String[currentResources.size()];
    int i = 0;
    Iterator it = currentResources.iterator();
    while (it.hasNext()) {
      TaskResourceDetail resource = (TaskResourceDetail) it.next();
      users[i] = resource.getUserId();
      i++;
    }
    sel.setSelectedElements(users);

    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  /****************************************************************************/
  /**
   * GESTION du Filtre /
   ****************************************************************************/
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

  /****************************************************************************/
  /**
   * GESTION du projet /
   ****************************************************************************/
  public boolean isProjectDefined() throws RemoteException {
    if (projectDefined == null) {
      List projects = getProjectManagerBm().getProjects(getComponentId());
      if (projects.size() > 0) {
        projectDefined = new Boolean(true);
        currentProject = (TaskDetail) projects.get(0);
      } else
        projectDefined = new Boolean(false);
    }
    return projectDefined.booleanValue();
  }

  public void createProject(TaskDetail project) throws RemoteException {
    project.setInstanceId(getComponentId());
    project.setOrganisateurId(getUserId());
    project.setMereId(-1);

    int currentProjectId = getProjectManagerBm().addTask(project);

    this.currentProject = getTask(new Integer(currentProjectId).toString());

    projectDefined = new Boolean(true);
  }

  /**
   * @return
   */
  public TaskDetail getCurrentProject() {
    return currentProject;
  }

  public Date getEndDateOfCurrentProjet() throws RemoteException {
    TaskDetail mostDistantTask = getProjectManagerBm().getMostDistantTask(
        getComponentId(), getCurrentProject().getId());
    if (mostDistantTask != null)
      return mostDistantTask.getDateFin();
    else
      return null;
  }

  public void updateCurrentProject() throws RemoteException {
    getProjectManagerBm().updateTask(getCurrentProject(), getUserId());
  }

  /****************************************************************************/
  /**
   * Gestion des jours non travaillés /
   ****************************************************************************/

  /**
   * Change le statut de la date
   * 
   * @param date
   *          la date
   * @param nextStatus
   *          le nouveau statut de la date
   */
  public void changeDateStatus(String date, String nextStatus)
      throws RemoteException, ParseException {
    int status = new Integer(nextStatus).intValue();
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

  public void changeDayOfWeekStatus(String year, String month, String day)
      throws RemoteException, ParseException {
    SilverTrace.info("projectManager",
        "ProjectManagerSessionController.changeDayOfWeekStatus()",
        "root.MSG_GEN_ENTER_METHOD", "year=" + year + ", month=" + month
            + ", day=" + day);

    int iMonth = new Integer(month).intValue();

    getCalendar().set(Calendar.YEAR, new Integer(year).intValue());
    getCalendar().set(Calendar.MONTH, iMonth);
    getCalendar().set(Calendar.DATE, 1);

    // on se place sur le premier jour du mois
    // correspondant au jour de la semaine passé en paramêtre
    while (getCalendar().get(Calendar.DAY_OF_WEEK) != new Integer(day)
        .intValue()) {
      getCalendar().add(Calendar.DATE, 1);
    }

    Date date = getCalendar().getTime();

    SilverTrace.info("projectManager",
        "ProjectManagerSessionController.changeDayOfWeekStatus()",
        "root.MSG_GEN_PARAM_VALUE", "date=" + date.toString());

    HolidayDetail holidayDate = new HolidayDetail(date, getCurrentProject()
        .getId(), getComponentId());
    boolean isHoliday = getProjectManagerBm().isHolidayDate(holidayDate);

    List holidayDates = new ArrayList();
    while (getCalendar().get(Calendar.MONTH) == iMonth) {
      holidayDates.add(new HolidayDetail(getCalendar().getTime(),
          getCurrentProject().getId(), getComponentId()));
      getCalendar().add(Calendar.DATE, 7);
    }
    if (isHoliday)
      getProjectManagerBm().removeHolidayDates(holidayDates);
    else
      getProjectManagerBm().addHolidayDates(holidayDates);

    // recalcule pour toutes les tâches du projet
    // toutes les dates de début et de fin
    calculateAllTasksDates();
  }

  public List getHolidayDates() throws RemoteException {
    return getProjectManagerBm().getHolidayDates(getComponentId());
  }

  public void calculateAllTasksDates() throws RemoteException {
    getProjectManagerBm().calculateAllTasksDates(getComponentId(),
        getCurrentProject().getId(), getUserId());
  }

  /****************************************************************************/
  /**
   * Méthodes utilitaires /
   ****************************************************************************/
  public Date uiDate2Date(String uiDate) throws ParseException {
    if (uiDate != null && uiDate.length() > 0)
      return DateUtil.stringToDate(uiDate, getLanguage());
    else
      return null;
  }

  public String date2UIDate(Date date) {
    if (date != null)
      return DateUtil.getInputDate(date, getLanguage());
    else
      return null;
  }

  public String getUserFullName(UserDetail user) {
    if (user != null)
      return user.getFirstName() + " " + user.getLastName();
    else
      return getUserDetail().getFirstName() + " "
          + getUserDetail().getLastName();
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

  private ProjectManagerBm getProjectManagerBm() {
    if (projectManagerBm == null) {
      try {
        ProjectManagerBmHome projectManagerBmHome = (ProjectManagerBmHome) EJBUtilitaire
            .getEJBObjectRef(JNDINames.PROJECTMANAGERBM_EJBHOME,
                ProjectManagerBmHome.class);
        projectManagerBm = projectManagerBmHome.create();
      } catch (Exception e) {
        throw new ProjectManagerRuntimeException(
            "ProjectManagerSessionController.getProjectManagerBm()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
            e);
      }
    }
    return projectManagerBm;
  }

  public String getRole() {
    String[] roles = getUserRoles();
    String role = null;
    String higherRole = "lecteur";
    for (int i = 0; i < roles.length; i++) {
      role = roles[i];
      // if admin, return it, we won't find a better profile
      if (role.equals("admin"))
        return role;
      if (role.equals("responsable"))
        higherRole = role;
    }
    return higherRole;
  }

  /*
   * public SimpleDateFormat getUiFormatter() { return uiFormatter; }
   */

  public void index() throws RemoteException {
    getProjectManagerBm().index(getComponentId());
  }

  public void setCurrentResources(Collection resources) {
    currentResources = resources;
  }

  public Collection getCurrentResources() {
    return currentResources;
  }

  public void updateOccupation(TaskDetail task) {
    try {
      Date dateDeb = task.getDateDebut();
      Date dateFin = task.getDateFin();

      Collection resources = task.getResources();
      Iterator it = resources.iterator();
      while (it.hasNext()) {
        TaskResourceDetail resource = (TaskResourceDetail) it.next();
        String userId = resource.getUserId();
        resource.setOccupation(getProjectManagerBm().getOccupationByUser(
            userId, dateDeb, dateFin));
      }
    } catch (Exception e) {
      throw new ProjectManagerRuntimeException(
          "ProjectManagerSessionController.updateOccupation()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public int checkOccupation(String taskId, String userId) {
    try {
      TaskDetail task = getTask(taskId);
      Date dateDeb = task.getDateDebut();
      Date dateFin = task.getDateFin();

      return getProjectManagerBm().getOccupationByUser(userId, dateDeb,
          dateFin, task.getId());
    } catch (Exception e) {
      throw new ProjectManagerRuntimeException(
          "ProjectManagerSessionController.updateOccupation()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public int checkOccupation(String taskId, String userId, Date beginDate,
      Date endDate) {
    try {
      return getProjectManagerBm().getOccupationByUser(userId, beginDate,
          endDate, Integer.parseInt(taskId));
    } catch (Exception e) {
      throw new ProjectManagerRuntimeException(
          "ProjectManagerSessionController.updateOccupation()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public int checkOccupation(String userId, Date beginDate, Date endDate) {
    try {
      return getProjectManagerBm().getOccupationByUser(userId, beginDate,
          endDate);
    } catch (Exception e) {
      throw new ProjectManagerRuntimeException(
          "ProjectManagerSessionController.updateOccupation()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }
}