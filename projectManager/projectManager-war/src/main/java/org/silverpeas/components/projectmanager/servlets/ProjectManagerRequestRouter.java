/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.components.projectmanager.servlets;

import org.silverpeas.components.projectmanager.control.ProjectManagerSessionController;
import org.silverpeas.components.projectmanager.model.Filtre;
import org.silverpeas.components.projectmanager.model.TaskDetail;
import org.silverpeas.components.projectmanager.model.TaskResourceDetail;
import org.silverpeas.components.projectmanager.vo.MonthVO;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.web.export.ExportCSVBuilder;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.selection.SelectionUsersGroups;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ProjectManagerRequestRouter extends ComponentRequestRouter<ProjectManagerSessionController> {

  private static final long serialVersionUID = 5878086083042945518L;
  private static final String COMPONENT_NAME = "projectManager";

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   *
   * @return
   */
  @Override
  public String getSessionControlBeanName() {
    return COMPONENT_NAME;
  }

  @Override
  public ProjectManagerSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new ProjectManagerSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   *
   * @param function The entering request function (ex : "Main")
   * @param projectManagerSC The component Session Control, build and initialised.
   * @param request
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, ProjectManagerSessionController projectManagerSC,
      HttpRequest request) {
    String destination = "";
    String rootDestination = "/projectManager/jsp/";

    String role = projectManagerSC.getRole();
    request.setAttribute("Role", role);
    request.setAttribute("AppIcons", projectManagerSC.getIcon());

    try {
      if (function.startsWith("Main")) {
        if (projectManagerSC.isProjectDefined()) {
          List<TaskDetail> tasks = projectManagerSC.getTasks();
          Filtre filtre = projectManagerSC.getFiltre();
          boolean filtreActif = projectManagerSC.isFiltreActif();
          request.setAttribute("Tasks", tasks);
          request.setAttribute("FiltreActif", filtreActif);
          request.setAttribute("Filtre", filtre);
          request.setAttribute("UserId", projectManagerSC.getUserId());
          destination = rootDestination + "tasksList.jsp";
        } else {
          // le projet n'a pas encore été défini.
          if (SilverpeasRole.ADMIN.isInRole(role)) {
            String orgaFullName = projectManagerSC.getUserFullName();
            request.setAttribute("Organisateur", orgaFullName);
            destination = rootDestination + "projectDefinition.jsp";
          } else {
            destination = rootDestination + "projectNotDefined.jsp";
          }
        }
      } else if ("ToProject".equals(function)) {
        if (!projectManagerSC.isProjectDefined()) {
          destination = getDestination("Main", projectManagerSC, request);
        } else {
          TaskDetail project = projectManagerSC.getCurrentProject();
          project.setDateFin(projectManagerSC.getEndDateOfCurrentProjet());
          projectManagerSC.enrichirTask(project);
          request.setAttribute("Project", project);
          destination = rootDestination + "projectView.jsp";
        }
      } else if ("CreateProject".equals(function)) {
        TaskDetail project = request2Project(request, projectManagerSC);
        projectManagerSC.createProject(project, request.getUploadedFiles());
        destination = getDestination("Main", projectManagerSC, request);
      } else if ("ToUpdateProject".equals(function)) {
        TaskDetail project = projectManagerSC.getCurrentProject();
        project.setDateFin(projectManagerSC.getEndDateOfCurrentProjet());

        projectManagerSC.enrichirTask(project);
        request.setAttribute("Project", project);

        destination = rootDestination + "projectDefinition.jsp";
      } else if ("UpdateProject".equals(function)) {
        updateCurrentProject(request, projectManagerSC);
        destination = getDestination("Main", projectManagerSC, request);
      } else if (function.startsWith("Filter")) {
        projectManagerSC.setFiltreActif(function.equals("FilterShow"));
        destination = getDestination("Main", projectManagerSC, request);
      } else if ("ToFilterTasks".equals(function)) {
        Filtre filtre = new Filtre();
        filtre.setActionFrom(request.getParameter("TaskFrom"));
        filtre.setActionTo(request.getParameter("TaskTo"));
        filtre.setActionNom(request.getParameter("TaskNom"));
        filtre.setStatut(request.getParameter("Statut"));
        filtre.setRetard(request.getParameter("Retard"));
        filtre.setAvancement(request.getParameter("Avancement"));
        filtre.setResponsableId(request.getParameter("ResponsableId"));
        filtre.setResponsableName(request.getParameter("ResponsableName"));
        filtre.setDateDebutFrom(projectManagerSC.uiDate2Date(request.getParameter("DateDebutFrom")));
        filtre.setDateDebutTo(projectManagerSC.uiDate2Date(request.getParameter("DateDebutTo")));
        filtre.setDateFinFrom(projectManagerSC.uiDate2Date(request.getParameter("DateFinFrom")));
        filtre.setDateFinTo(projectManagerSC.uiDate2Date(request.getParameter("DateFinTo")));
        filtre.setDateDebutFromUI(request.getParameter("DateDebutFrom"));
        filtre.setDateDebutToUI(request.getParameter("DateDebutTo"));
        filtre.setDateFinFromUI(request.getParameter("DateFinFrom"));
        filtre.setDateFinToUI(request.getParameter("DateFinTo"));
        projectManagerSC.setFiltre(filtre);
        destination = getDestination("Main", projectManagerSC, request);
      } else if ("ViewTask".equals(function)) {
        String id = request.getParameter("Id");
        TaskDetail task = projectManagerSC.getTask(id, false);
        request.setAttribute("Task", task);
        request.setAttribute("AbleToAddSubTask", isOrganiteurOrResponsable(projectManagerSC, task));
        destination = rootDestination + "taskView.jsp";
      } else if ("UnfoldTask".equals(function)) {
        String id = request.getParameter("Id");
        projectManagerSC.addUnfoldTask(id);
        destination = getDestination("Main", projectManagerSC, request);
      } else if ("CollapseTask".equals(function)) {
        String id = request.getParameter("Id");
        projectManagerSC.removeUnfoldTask(id);
        destination = getDestination("Main", projectManagerSC, request);
      } else if ("ToAddTask".equals(function)) {
        TaskDetail currentTask = projectManagerSC.getCurrentTask();
        if (projectManagerSC.isProjectDefined()) {
          TaskDetail currentProject = projectManagerSC.getCurrentProject();
          request.setAttribute("DateDebProject", projectManagerSC.date2UIDate(currentProject
              .getDateDebut()));
        }
        request.setAttribute("Organisateur", projectManagerSC.getUserFullName());
        request.setAttribute("CurrentTask", currentTask);
        request.setAttribute("PreviousTasks", projectManagerSC.getPotentialPreviousTasks(true));
        destination = rootDestination + "taskAdd.jsp";
      } else if ("AddTask".equals(function)) {
        TaskDetail task = request2TaskDetail(request, projectManagerSC);
        projectManagerSC.addTask(task, request.getUploadedFiles());
        destination = getDestination("Main", projectManagerSC, request);
      } else if ("ProcessEndDate".equals(function)) {
        // Création ou modification ?
        String action = request.getParameter("Action");
        // memorise les données saisies
        TaskDetail task = request2TaskDetail(request, projectManagerSC);
        // vérifie la cohérence de la date de début
        projectManagerSC.checkBeginDate(task);
        // calcul la date de fin
        task.setDateFin(projectManagerSC.processEndDate(task));
        projectManagerSC.enrichirTask(task);
        request.setAttribute("TaskInProgress", task);
        destination = getDestination(action, projectManagerSC, request);
      } else if ("ToUpdateTask".equals(function)) {
        if (projectManagerSC.isProjectDefined()) {
          TaskDetail currentProject = projectManagerSC.getCurrentProject();
          request.setAttribute("DateDebProject", projectManagerSC.date2UIDate(currentProject
              .getDateDebut()));
        }
        String id = request.getParameter("Id");
        TaskDetail task = projectManagerSC.getTask(id, false);
        if (task.getMereId() != -1) {
          TaskDetail actionMere = projectManagerSC.getTaskMere(task.getMereId());
          request.setAttribute("ActionMere", actionMere);
        }
        List<TaskDetail> previousTasks = projectManagerSC.getPotentialPreviousTasks();
        // ajout des pourcentages d'occupation des ressources
        projectManagerSC.updateOccupation(task);
        request.setAttribute("Task", task);
        request.setAttribute("PreviousTasks", previousTasks);
        destination = rootDestination + "taskUpdate.jsp";
      } else if ("UpdateTask".equals(function)) {
        updateActionCourante(request, projectManagerSC);
        destination = getDestination("Main", projectManagerSC, request);
      } else if ("RemoveTask".equals(function)) {
        String id = request.getParameter("Id");
        projectManagerSC.removeTask(id);
        destination = getDestination("Main", projectManagerSC, request);
      } else if ("ToSelectResources".equals(function)) {
        // récupération de la liste des resources en cours
        Collection<TaskResourceDetail> currentResources = request2Resources(request);
        projectManagerSC.setCurrentResources(currentResources);
        destination = projectManagerSC.initUserSelect();
      } else if ("FromUserSelect".equals(function)) {
        // récupération des valeurs de userPanel par userPanelPeas
        Selection sel = projectManagerSC.getSelection();
        // Get users selected in User Panel
        String[] userIds = SelectionUsersGroups.getDistinctUserIds(sel.getSelectedElements(), null);
        if (userIds.length != 0) {
          UserDetail[] userDetails = SelectionUsersGroups.getUserDetails(userIds);
          // récupération de la liste des resources en cours
          Collection<TaskResourceDetail> currentResources = projectManagerSC.getCurrentResources();
          // création des resources à partir des users
          Collection<TaskResourceDetail> resources = new ArrayList<>();
          for (UserDetail userDetail : userDetails) {
            TaskResourceDetail resourceDetail = new TaskResourceDetail();
            resourceDetail.setUserId(userDetail.getId());
            resourceDetail.setUserName(projectManagerSC.getUserFullName(userDetail));
            // si la resource existant déjà, on conserve la charge
            if (currentResources != null) {
              boolean trouve = false;
              Iterator<TaskResourceDetail> it = currentResources.iterator();
              while (it.hasNext() && !trouve) {
                TaskResourceDetail res = it.next();
                // si la resource existe déjà, on conserve la charge
                if (res.getUserId().equals(userDetail.getId())) {
                  resourceDetail.setCharge(res.getCharge());
                  trouve = true;
                }
              }
            }
            resources.add(resourceDetail);
          }
          request.setAttribute("Resources", resources);
        }
        destination = rootDestination + "refreshFromUserSelect.jsp";
      } else if ("ToGantt".equals(function)) {
        // retrieve all data to display GANT diagram.
        String id = request.getParameter("Id");
        String viewMode = request.getParameter("viewMode");
        if (!StringUtil.isDefined(viewMode)) {
          viewMode = "quarter";
        }
        String startDate = request.getParameter("StartDate");
        TaskDetail actionMere = null;
        if (id == null || id.length() == 0 || "-1".equals(id)) {
          id = Integer.toString(projectManagerSC.getCurrentProject().getId());
        } else {
          actionMere = projectManagerSC.getTaskMere(id);
        }
        Date curDate = projectManagerSC.getMostRelevantDate(startDate);
        request.setAttribute("StartDate", curDate);
        // Prepare date navigation
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(curDate);
        calendar.add(Calendar.MONTH, -1);
        request.setAttribute("BeforeMonth", calendar.getTime());
        calendar.add(Calendar.MONTH, 2);
        request.setAttribute("AfterMonth", calendar.getTime());
        // Compute view destination
        String viewDestination = "gantt.jsp";
        if ("month".equalsIgnoreCase(viewMode)) {
          MonthVO curMonth = projectManagerSC.getMonthVO(curDate);
          request.setAttribute("MonthVO", curMonth);
        } else if ("quarter".equalsIgnoreCase(viewMode)) {
          request.setAttribute("MonthsVO", projectManagerSC.getQuarterMonth(curDate));
          viewDestination = "gantt_months.jsp";
          //Prepare date navigation
          calendar.setTime(curDate);
          calendar.add(Calendar.MONTH, -3);
          request.setAttribute("BeforeQuarter", calendar.getTime());
          calendar.add(Calendar.MONTH, 6);
          request.setAttribute("AfterQuarter", calendar.getTime());
        } else if ("year".equalsIgnoreCase(viewMode)) {
          request.setAttribute("MonthsVO", projectManagerSC.getYearMonth(curDate));
          viewDestination = "gantt_months.jsp";
          //Prepare date navigation
          calendar.setTime(curDate);
          calendar.add(Calendar.YEAR, -1);
          request.setAttribute("BeforeYear", calendar.getTime());
          calendar.add(Calendar.YEAR, 2);
          request.setAttribute("AfterYear", calendar.getTime());
        }
        // Get all the tasks
        List<TaskDetail> tasks = projectManagerSC.getTasks(id);
        TaskDetail oldestAction = getOldestTask(tasks);
        // Le diagramme de Gantt doit faire apparaitre
        // les jours non travaillés
        request.setAttribute("Holidays", projectManagerSC.getHolidayDates());
        request.setAttribute("Tasks", tasks);
        request.setAttribute("ActionMere", actionMere);
        request.setAttribute("OldestAction", oldestAction);
        request.setAttribute("ViewMode", viewMode);
        // redirect to gant JSP view
        destination = rootDestination + viewDestination;
      } else if ("ToCalendar".equals(function)) {
        TaskDetail project = projectManagerSC.getCurrentProject();
        Date beginDate = project.getDateDebut();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(beginDate);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        beginDate = calendar.getTime();
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date endDate = calendar.getTime();
        request.setAttribute("BeginDate", beginDate);
        request.setAttribute("EndDate", endDate);
        request.setAttribute("HolidayDates", projectManagerSC.getHolidayDates());
        destination = rootDestination + "calendar.jsp";
      } else if ("ChangeDateStatus".equals(function)) {
        String date = request.getParameter("Date");
        String status = request.getParameter("Status");
        projectManagerSC.changeDateStatus(date, status);
        destination = getDestination("ToCalendar", projectManagerSC, request);
      } else if ("ChangeDayOfWeekStatus".equals(function)) {
        String year = request.getParameter("Year");
        String month = request.getParameter("Month");
        String day = request.getParameter("DayOfWeek");
        projectManagerSC.changeDayOfWeekStatus(year, month, day);
        destination = getDestination("ToCalendar", projectManagerSC, request);
      } else if ("Export".equals(function)) {
        ExportCSVBuilder exportCSV = projectManagerSC.export();

        destination = exportCSV.setupRequest(request);
      } else if (function.startsWith("searchResult")) {
        String id = request.getParameter("Id");
        String type = request.getParameter("Type");
        if (type.indexOf("TodoDetail") != -1) {
          destination = getDestination("ToUpdateTask", projectManagerSC, request);
        } else if (type.startsWith("Comment")) {
          if ("-1".equals(id)) {
            destination = getDestination("ToComments", projectManagerSC, request);
          } else {
            projectManagerSC.getTask(id);
            destination = getDestination("ToTaskComments", projectManagerSC, request);
          }
        } else {
          destination = getDestination("ViewTask", projectManagerSC, request);
        }
      } else if ("portlet".equals(function)) {
        if (projectManagerSC.isProjectDefined()) {
          request.setAttribute("Tasks", projectManagerSC.getTasks());
          request.setAttribute("Role", "lecteur");
          destination = rootDestination + "portlet.jsp";
        } else {
          // le projet n'a pas encore été défini.
          destination = rootDestination + "projectNotDefined.jsp";
        }
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    return destination;
  }

  private void updateActionCourante(HttpServletRequest request,
      ProjectManagerSessionController projectManagerSC) throws ParseException {
    TaskDetail task = projectManagerSC.getCurrentTask();
    if ("admin".equals(projectManagerSC.getRole())) {
      task.setResponsableId(Integer.parseInt(request.getParameter("ResponsableId")));
      task.setNom(request.getParameter("Nom"));
      task.setCharge(StringUtil.asFloat(request.getParameter("Charge")));
      task.setConsomme(StringUtil.asFloat(request.getParameter("Consomme")));
      task.setRaf(StringUtil.asFloat(request.getParameter("Raf")));
      task.setStatut(Integer.valueOf(request.getParameter("Statut")));
      task.setDescription(request.getParameter("Description"));
      task.setDateDebut(projectManagerSC.uiDate2Date(request.getParameter("DateDebut")));
      task.setDateFin(projectManagerSC.uiDate2Date(request.getParameter("DateFin")));
      task.setPreviousTaskId(Integer.parseInt(request.getParameter("PreviousId")));

      task.setResources(request2Resources(request));
    } else if ("responsable".equals(projectManagerSC.getRole())) {
      task.setConsomme(StringUtil.asFloat(request.getParameter("Consomme")));
      task.setRaf(StringUtil.asFloat(request.getParameter("Raf")));
      task.setStatut(Integer.valueOf(request.getParameter("Statut")));
      task.setResources(request2Resources(request));
    }

    projectManagerSC.updateCurrentTask();
  }

  private TaskDetail request2TaskDetail(HttpServletRequest request,
      ProjectManagerSessionController projectManagerSC) throws ParseException {
    TaskDetail task = new TaskDetail();

    String id = request.getParameter("Id");
    if (id != null) {
      task.setId(Integer.parseInt(id));
    }

    task.setCodeProjet(request.getParameter("ProjetCode"));
    task.setDescriptionProjet(request.getParameter("ProjetDescription"));

    String responsableId = request.getParameter("ResponsableId");
    if (StringUtil.isDefined(responsableId)) {
      task.setResponsableId(Integer.valueOf(responsableId));
    }

    task.setNom(request.getParameter("Nom"));
    task.setCharge(StringUtil.asFloat(request.getParameter("Charge")));
    task.setConsomme(StringUtil.asFloat(request.getParameter("Consomme")));
    task.setRaf(StringUtil.asFloat(request.getParameter("Raf")));
    task.setStatut(Integer.parseInt(request.getParameter("Statut")));
    task.setDescription(request.getParameter("Description"));
    task.setDateDebut(projectManagerSC.uiDate2Date(request.getParameter("DateDebut")));
    task.setDateFin(projectManagerSC.uiDate2Date(request.getParameter("DateFin")));
    task.setPreviousTaskId(Integer.parseInt(request.getParameter("PreviousId")));
    // liste des resources
    task.setResources(request2Resources(request));

    return task;
  }

  private Collection<TaskResourceDetail> request2Resources(HttpServletRequest request) {
    Collection<TaskResourceDetail> resources = new ArrayList<>();
    String allResources = request.getParameter("allResources");
    if (StringUtil.isDefined(allResources)) {
      String[] tabResources = allResources.split(",");
      for (int i = 0; i < tabResources.length; i++) {
        TaskResourceDetail resource = new TaskResourceDetail();
        String[] user = tabResources[i].split("_");
        String userId = user[0];
        String charge = user[1];
        resource.setCharge(charge);
        resource.setUserId(userId);
        resources.add(resource);

      }
    }
    return resources;
  }

  private TaskDetail request2Project(HttpServletRequest request,
      ProjectManagerSessionController projectManagerSC) throws ParseException {
    TaskDetail task = new TaskDetail();
    String id = request.getParameter("Id");
    if (id != null) {
      task.setId(Integer.parseInt(id));
    }
    task.setNom(request.getParameter("Nom"));
    task.setDescription(request.getParameter("Description"));
    task.setDateDebut(projectManagerSC.uiDate2Date(request.getParameter("DateDebut")));
    return task;
  }

  private void updateCurrentProject(HttpServletRequest request,
      ProjectManagerSessionController projectManagerSC) throws ParseException {
    TaskDetail project = projectManagerSC.getCurrentProject();
    project.setNom(request.getParameter("Nom"));
    project.setDescription(request.getParameter("Description"));
    project.setDateDebut(projectManagerSC.uiDate2Date(request.getParameter("DateDebut")));
    projectManagerSC.setCurrentProject(project);
    projectManagerSC.updateCurrentProject();
  }

  /**
   * @param tasks
   * @return the oldest task from a list of TaskDetail
   */
  private TaskDetail getOldestTask(List<TaskDetail> tasks) {
    TaskDetail oldest = null;
    TaskDetail task = null;
    for (int a = 0; a < tasks.size(); a++) {
      task = tasks.get(a);
      if (a == 0) {
        oldest = task;
      } else {
        if (task.getDateDebut().before(oldest.getDateDebut())) {
          oldest = task;
        }
      }
    }
    return oldest;
  }

  private Boolean isOrganiteurOrResponsable(ProjectManagerSessionController projectManagerSC,
      TaskDetail task) {
    String role = projectManagerSC.getRole();
    return "admin".equals(role) || ("responsable".equals(role) && Integer.
        parseInt(projectManagerSC.getUserId()) == task.getResponsableId());
  }
}