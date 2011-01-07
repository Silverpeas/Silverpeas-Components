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
package com.silverpeas.projectManager.servlets;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.silverpeas.projectManager.control.ProjectManagerSessionController;
import com.silverpeas.projectManager.model.Filtre;
import com.silverpeas.projectManager.model.TaskDetail;
import com.silverpeas.projectManager.model.TaskResourceDetail;
import com.silverpeas.projectManager.vo.MonthVO;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;

public class ProjectManagerRequestRouter extends ComponentRequestRouter {

  private static final long serialVersionUID = 5878086083042945518L;
  private static final String COMPONENT_NAME = "projectManager";

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   * @return
   */
  @Override
  public String getSessionControlBeanName() {
    return COMPONENT_NAME;
  }

  @Override
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new ProjectManagerSessionController(mainSessionCtrl,
        componentContext);
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main")
   * @param componentSC The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request) {
    String destination = "";
    String rootDestination = "/projectManager/jsp/";

    ProjectManagerSessionController projectManagerSC =
        (ProjectManagerSessionController) componentSC;
    SilverTrace.info(COMPONENT_NAME, "ProjectManagerRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "User=" + projectManagerSC.getUserId() + " Function=" +
            function);

    try {
      if (function.startsWith("Main")) {
        String role = projectManagerSC.getRole();
        if (projectManagerSC.isProjectDefined()) {
          List<TaskDetail> tasks = projectManagerSC.getTasks();
          Filtre filtre = projectManagerSC.getFiltre();

          boolean filtreActif = projectManagerSC.isFiltreActif();

          request.setAttribute("Tasks", tasks);
          request.setAttribute("Role", role);
          request.setAttribute("FiltreActif", Boolean.valueOf(filtreActif));
          request.setAttribute("Filtre", filtre);
          request.setAttribute("UserId", projectManagerSC.getUserId());

          destination = rootDestination + "tasksList.jsp";
        } else {
          // le projet n'a pas encore été défini.
          if (role.equals("admin")) {
            String orgaFullName = projectManagerSC.getUserFullName();

            request.setAttribute("Organisateur", orgaFullName);

            destination = rootDestination + "projectDefinition.jsp";
          } else {
            destination = rootDestination + "projectNotDefined.jsp";
          }
        }
      } else if (function.equals("ToProject")) {
        TaskDetail project = projectManagerSC.getCurrentProject();
        project.setDateFin(projectManagerSC.getEndDateOfCurrentProjet());

        projectManagerSC.enrichirTask(project);
        request.setAttribute("Project", project);

        if (projectManagerSC.getRole().equals("admin")) {
          destination = rootDestination + "projectDefinition.jsp";
        } else {
          request.setAttribute("Attachments", projectManagerSC.getAttachments(project.getId()));
          destination = rootDestination + "projectView.jsp";
        }
      } else if (function.equals("CreateProject")) {
        TaskDetail project = request2Project(request, projectManagerSC);

        projectManagerSC.createProject(project);

        destination = getDestination("Main", componentSC, request);
      } else if (function.equals("UpdateProject")) {
        updateCurrentProject(request, projectManagerSC);

        destination = getDestination("Main", componentSC, request);
      } else if (function.startsWith("Filter")) {
        projectManagerSC.setFiltreActif(function.equals("FilterShow"));
        destination = getDestination("Main", componentSC, request);
      } else if (function.equals("ToFilterTasks")) {
        Filtre filtre = new Filtre(request);

        filtre.setActionFrom(request.getParameter("TaskFrom"));
        filtre.setActionTo(request.getParameter("TaskTo"));
        filtre.setActionNom(request.getParameter("TaskNom"));
        filtre.setStatut(request.getParameter("Statut"));
        filtre.setRetard(request.getParameter("Retard"));
        filtre.setAvancement(request.getParameter("Avancement"));
        filtre.setResponsableId(request.getParameter("ResponsableId"));
        filtre.setResponsableName(request.getParameter("ResponsableName"));
        filtre
            .setDateDebutFrom(projectManagerSC.uiDate2Date(request.getParameter("DateDebutFrom")));
        filtre.setDateDebutTo(projectManagerSC.uiDate2Date(request.getParameter("DateDebutTo")));
        filtre.setDateFinFrom(projectManagerSC.uiDate2Date(request.getParameter("DateFinFrom")));
        filtre.setDateFinTo(projectManagerSC.uiDate2Date(request.getParameter("DateFinTo")));
        filtre.setDateDebutFromUI(request.getParameter("DateDebutFrom"));
        filtre.setDateDebutToUI(request.getParameter("DateDebutTo"));
        filtre.setDateFinFromUI(request.getParameter("DateFinFrom"));
        filtre.setDateFinToUI(request.getParameter("DateFinTo"));

        projectManagerSC.setFiltre(filtre);

        destination = getDestination("Main", componentSC, request);
      } else if (function.equals("ViewTask")) {
        String id = request.getParameter("Id");

        TaskDetail task = projectManagerSC.getTask(id, false);

        request.setAttribute("Task", task);
        request.setAttribute("AbleToAddSubTask", isOrganiteurOrResponsable(
            projectManagerSC, task));
        request.setAttribute("Role", projectManagerSC.getRole());

        destination = rootDestination + "taskView.jsp";
      } else if (function.equals("UnfoldTask")) {
        String id = request.getParameter("Id");

        projectManagerSC.addUnfoldTask(id);

        destination = getDestination("Main", componentSC, request);
      } else if (function.equals("CollapseTask")) {
        String id = request.getParameter("Id");

        projectManagerSC.removeUnfoldTask(id);

        destination = getDestination("Main", componentSC, request);
      } else if (function.equals("ToAddTask")) {
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
      } else if (function.equals("AddTask")) {
        TaskDetail task = request2TaskDetail(request, projectManagerSC);

        projectManagerSC.addTask(task);

        destination = getDestination("Main", componentSC, request);
      } else if (function.equals("ProcessEndDate")) {
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

        destination = getDestination(action, componentSC, request);
      } else if (function.equals("ToUpdateTask")) {
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
        request.setAttribute("Role", projectManagerSC.getRole());
        request.setAttribute("PreviousTasks", previousTasks);

        destination = rootDestination + "taskUpdate.jsp";
      } else if (function.equals("UpdateTask")) {
        updateActionCourante(request, projectManagerSC);

        destination = getDestination("Main", componentSC, request);
      } else if (function.equals("RemoveTask")) {
        String id = request.getParameter("Id");
        projectManagerSC.removeTask(id);

        destination = getDestination("Main", componentSC, request);
      } else if (function.equals("ToChooseDate")) {
        String inputId = request.getParameter("InputId");
        String jsFunction = request.getParameter("JSFunction");

        List<Date> holidays = projectManagerSC.getHolidayDates();

        HttpSession session = request.getSession(true);
        session.setAttribute("Silverpeas_NonSelectableDays", holidays);

        destination = URLManager.getURL(URLManager.CMP_AGENDA)
            + "calendar.jsp?JSCallback=" + jsFunction
            + "&indiceForm=0&indiceElem=" + inputId;
      } else if (function.equals("ToUserPanel")) {
        try {
          destination = projectManagerSC.initUserPanel();
        } catch (Exception e) {
          SilverTrace.warn(COMPONENT_NAME,
              "ProjectManagerRequestRouter.getDestination()",
              "root.EX_USERPANEL_FAILED", "function = " + function, e);
        }
      } else if (function.equals("FromUserPanel")) {
        // récupération des valeurs de userPanel par userPanelPeas
        SilverTrace.debug(COMPONENT_NAME, "ProjectManagerRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "FromUserPanel:");
        Selection sel = projectManagerSC.getSelection();
        // Get user selected in User Panel
        String[] userIds = SelectionUsersGroups.getDistinctUserIds(sel.getSelectedElements(), null);
        SilverTrace.debug(COMPONENT_NAME, "ProjectManagerRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "userIds:" + userIds);
        if (userIds.length != 0) {
          SilverTrace.debug(COMPONENT_NAME, "ProjectManagerRequestRouter.getDestination()",
              "root.MSG_GEN_PARAM_VALUE", "userIds.length():" + userIds.length);

          UserDetail[] userDetails = SelectionUsersGroups.getUserDetails(userIds);
          SilverTrace.debug(COMPONENT_NAME, "ProjectManagerRequestRouter.getDestination()",
              "root.MSG_GEN_PARAM_VALUE", "userDetails:" + userDetails);
          if (userDetails != null) {
            SilverTrace.debug(COMPONENT_NAME, "ProjectManagerRequestRouter.getDestination()",
                "root.MSG_GEN_PARAM_VALUE", "userDetails[0].getId():"
                    + userDetails[0].getId().toString());
            UserDetail userDetail = userDetails[0];

            request.setAttribute("ResponsableFullName", projectManagerSC
                .getUserFullName(userDetail));
            request.setAttribute("ResponsableId", userDetail.getId());
          }
        }
        destination = rootDestination + "refreshFromUserPanel.jsp";
      } else if (function.equals("ToSelectResources")) {
        // récupération de la liste des resources en cours
        Collection<TaskResourceDetail> currentResources = request2Resources(request);
        projectManagerSC.setCurrentResources(currentResources);
        try {
          destination = projectManagerSC.initUserSelect();
        } catch (Exception e) {
          SilverTrace.warn(COMPONENT_NAME, "ProjectManagerRequestRouter.getDestination()",
              "root.EX_USERPANEL_FAILED", "function = " + function, e);
        }
      } else if (function.equals("FromUserSelect")) {
        // récupération des valeurs de userPanel par userPanelPeas
        SilverTrace.debug(COMPONENT_NAME, "ProjectManagerRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "FromUserSelect:");
        Selection sel = projectManagerSC.getSelection();
        // Get users selected in User Panel
        String[] userIds = SelectionUsersGroups.getDistinctUserIds(sel.getSelectedElements(), null);
        SilverTrace.debug(COMPONENT_NAME, "ProjectManagerRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "userIds:" + userIds);
        if (userIds.length != 0) {
          SilverTrace.debug(COMPONENT_NAME, "ProjectManagerRequestRouter.getDestination()",
              "root.MSG_GEN_PARAM_VALUE", "userIds.length():" + userIds.length);

          UserDetail[] userDetails = SelectionUsersGroups.getUserDetails(userIds);
          SilverTrace.debug(COMPONENT_NAME, "ProjectManagerRequestRouter.getDestination()",
              "root.MSG_GEN_PARAM_VALUE", "userDetails:" + userDetails.toString());
          // récupération de la liste des resources en cours
          Collection<TaskResourceDetail> currentResources = projectManagerSC.getCurrentResources();
          // création des resources à partir des users
          Collection<TaskResourceDetail> resources = new ArrayList<TaskResourceDetail>();
          for (int i = 0; i < userDetails.length; i++) {
            SilverTrace.debug(COMPONENT_NAME, "ProjectManagerRequestRouter.getDestination()",
                "root.MSG_GEN_PARAM_VALUE", "userDetails[" + i + "].getId():" +
                    userDetails[i].getId());
            UserDetail userDetail = userDetails[i];
            TaskResourceDetail resourceDetail = new TaskResourceDetail();
            resourceDetail.setUserId(userDetail.getId());
            resourceDetail.setUserName(projectManagerSC.getUserFullName(userDetail));
            // si la resource existant déjà, on conserve la charge
            if (currentResources != null) {
              SilverTrace.debug(COMPONENT_NAME, "ProjectManagerRequestRouter.getDestination()",
                  "root.MSG_GEN_PARAM_VALUE", "currentResource = "
                      + currentResources.size());
              boolean trouve = false;
              Iterator<TaskResourceDetail> it = currentResources.iterator();
              while (it.hasNext() && !trouve) {
                TaskResourceDetail res = it.next();
                SilverTrace.debug(COMPONENT_NAME, "ProjectManagerRequestRouter.getDestination()",
                    "root.MSG_GEN_PARAM_VALUE", "currentResource userId:" + res.getUserId() +
                        " / " + res.getUserName());
                // si la resource existe déjà, on conserve la charge
                if (res.getUserId().equals(userDetail.getId())) {
                  resourceDetail.setCharge(res.getCharge());
                  trouve = true;
                  SilverTrace.debug(COMPONENT_NAME, "ProjectManagerRequestRouter.getDestination()",
                      "root.MSG_GEN_PARAM_VALUE", "currentResource charge:" + res.getCharge());
                }
              }
            }
            resources.add(resourceDetail);
          }
          request.setAttribute("Resources", resources);
        }
        destination = rootDestination + "refreshFromUserSelect.jsp";
      } else if (function.equals("ToAttachments")) {
        TaskDetail project = projectManagerSC.getCurrentProject();
        String url = projectManagerSC.getComponentUrl() + function;

        request.setAttribute("Task", project);
        request.setAttribute("URL", url);
        request.setAttribute("Role", projectManagerSC.getRole());

        destination = rootDestination + "attachments.jsp";
      } else if (function.equals("ToTaskAttachments")) {
        TaskDetail task = projectManagerSC.getCurrentTask();
        String url = projectManagerSC.getComponentUrl() + function;

        request.setAttribute("Task", task);
        request.setAttribute("URL", url);

        destination = rootDestination + "taskAttachments.jsp";
      } else if (function.equals("ToTaskComments")) {
        request.setAttribute("Task", projectManagerSC.getCurrentTask());
        request.setAttribute("URL", projectManagerSC.getComponentUrl()
            + function);
        request.setAttribute("UserId", projectManagerSC.getUserId());
        request.setAttribute("AbleToAddAttachments", isOrganiteurOrResponsable(
            projectManagerSC, projectManagerSC.getCurrentTask()));

        destination = rootDestination + "taskComments.jsp";
      } else if (function.equals("ToComments")) {
        String url = projectManagerSC.getComponentUrl() + function;

        request.setAttribute("Role", projectManagerSC.getRole());
        request.setAttribute("URL", url);
        request.setAttribute("UserId", projectManagerSC.getUserId());
        request.setAttribute("InstanceId", projectManagerSC.getComponentId());

        destination = rootDestination + "comments.jsp";
      } else if (function.equals("ToGantt")) {
        // TODO retrieve all data to display GANT diagram.
        String id = request.getParameter("Id");
        String viewMode = request.getParameter("viewMode");
        if (!StringUtil.isDefined(viewMode)) {
          viewMode = "month";
        }
        String startDate = request.getParameter("StartDate");

        TaskDetail actionMere = null;
        if (id == null || id.length() == 0 || id.equals("-1")) {
          id = Integer.toString(projectManagerSC.getCurrentProject().getId());
        } else {
          actionMere = projectManagerSC.getTaskMere(id);
        }

        Date curDate;
        if (startDate != null) {
          curDate = projectManagerSC.uiDate2Date(startDate);
        } else {
          curDate = Calendar.getInstance().getTime();
        }
        request.setAttribute("StartDate", curDate);
        
        String viewDestination = "gantt.jsp";

        if ("month".equalsIgnoreCase(viewMode)) {
          MonthVO curMonth = projectManagerSC.getMonthVO(curDate);
          request.setAttribute("MonthVO", curMonth);
        } else if ("quarter".equalsIgnoreCase(viewMode)) {
          request.setAttribute("MonthsVO", projectManagerSC.getQuarterMonth(curDate));
          viewDestination = "gantt_months.jsp";
        } else if ("year".equalsIgnoreCase(viewMode)) {
          request.setAttribute("MonthsVO", projectManagerSC.getYearMonth(curDate));
          viewDestination = "gantt_months.jsp";
        }

        List<TaskDetail> tasks = projectManagerSC.getTasksNotCancelled(id);
        TaskDetail oldestAction = getOldestTask(tasks);

        // Le diagramme de Gantt doit faire apparaitre
        // les jours non travaillés
        request.setAttribute("Holidays", projectManagerSC.getHolidayDates());
        request.setAttribute("Tasks", tasks);
        request.setAttribute("ActionMere", actionMere);
        request.setAttribute("OldestAction", oldestAction);
        request.setAttribute("Role", projectManagerSC.getRole());
        request.setAttribute("ViewMode", viewMode);

        // redirect to gant JSP view
        destination = rootDestination + viewDestination;
      } else if (function.equals("ToCalendar")) {
        TaskDetail project = projectManagerSC.getCurrentProject();
        Date beginDate = project.getDateDebut();
        // Date endDate = project.getDateFin();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(beginDate);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        beginDate = calendar.getTime();

        // calendar.setTime(endDate);
        // calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)+1);
        // calendar.set(Calendar.DAY_OF_MONTH, 1);
        // calendar.add(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date endDate = calendar.getTime();

        request.setAttribute("BeginDate", beginDate);
        request.setAttribute("EndDate", endDate);
        request.setAttribute("HolidayDates", projectManagerSC.getHolidayDates());

        destination = rootDestination + "calendar.jsp";
      } else if (function.equals("ChangeDateStatus")) {
        String date = request.getParameter("Date");
        String status = request.getParameter("Status");

        projectManagerSC.changeDateStatus(date, status);

        destination = getDestination("ToCalendar", componentSC, request);
      } else if (function.equals("ChangeDayOfWeekStatus")) {
        String year = request.getParameter("Year");
        String month = request.getParameter("Month");
        String day = request.getParameter("DayOfWeek");

        projectManagerSC.changeDayOfWeekStatus(year, month, day);

        destination = getDestination("ToCalendar", componentSC, request);
      } else if (function.equals("Export")) {
        List<TaskDetail> tasks = projectManagerSC.getAllTasks();
        request.setAttribute("Tasks", tasks);
        destination = rootDestination + "export.jsp";
      } else if (function.startsWith("searchResult")) {
        String id = request.getParameter("Id");
        String type = request.getParameter("Type");

        if (type.indexOf("TodoDetail") != -1) {
          destination = getDestination("ToUpdateTask", componentSC, request);
        } else if (type.startsWith("Comment")) {
          if (id.equals("-1")) {
            destination = getDestination("ToComments", componentSC, request);
          } else {
            projectManagerSC.getTask(id);
            destination = getDestination("ToTaskComments", componentSC, request);
          }
        } else {
          destination = getDestination("ViewTask", componentSC, request);
        }
      } else if (function.equals("portlet")) {
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

    SilverTrace.info(COMPONENT_NAME, "ProjectManagerRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
    return destination;
  }

  private void updateActionCourante(HttpServletRequest request,
      ProjectManagerSessionController projectManagerSC) throws RemoteException,
      ParseException {
    TaskDetail task = projectManagerSC.getCurrentTask();
    SilverTrace.debug(COMPONENT_NAME, "ProjectManagerRequestRouter.updateActionCourante()",
        "root.MSG_GEN_PARAM_VALUE", "Consomme = " + request.getParameter("Consomme") + " Raf = "
            + request.getParameter("Raf") + " statut =  " + request.getParameter("Statut")
            + " all resources = " + request.getParameter("allResources"));
    if ("admin".equals(projectManagerSC.getRole())) {
      task.setResponsableId(Integer.parseInt(request.getParameter("ResponsableId")));
      task.setNom(request.getParameter("Nom"));
      task.setCharge(StringUtil.convertFloat(request.getParameter("Charge")));
      task.setConsomme(StringUtil.convertFloat(request.getParameter("Consomme")));
      task.setRaf(StringUtil.convertFloat(request.getParameter("Raf")));
      task.setStatut(Integer.valueOf(request.getParameter("Statut")));
      task.setDescription(request.getParameter("Description"));
      task.setDateDebut(projectManagerSC.uiDate2Date(request.getParameter("DateDebut")));
      task.setDateFin(projectManagerSC.uiDate2Date(request.getParameter("DateFin")));
      task.setPreviousTaskId(Integer.parseInt(request.getParameter("PreviousId")));

      task.setResources(request2Resources(request));
    } else if ("responsable".equals(projectManagerSC.getRole())) {
      task.setConsomme(StringUtil.convertFloat(request.getParameter("Consomme")));
      task.setRaf(StringUtil.convertFloat(request.getParameter("Raf")));
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
    task.setCharge(StringUtil.convertFloat(request.getParameter("Charge")));
    task.setConsomme(StringUtil.convertFloat(request.getParameter("Consomme")));
    task.setRaf(StringUtil.convertFloat(request.getParameter("Raf")));
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
    Collection<TaskResourceDetail> resources = new ArrayList<TaskResourceDetail>();
    String allResources = request.getParameter("allResources");
    if (allResources != null && !allResources.equals("")) {
      String[] tabResources = allResources.split(",");
      for (int i = 0; i < tabResources.length; i++) {
        TaskResourceDetail resource = new TaskResourceDetail();
        String[] user = tabResources[i].split("_");
        String userId = user[0];
        String charge = user[1];
        resource.setCharge(charge);
        resource.setUserId(userId);
        resources.add(resource);
        SilverTrace.info(COMPONENT_NAME,
            "ProjectManagerRequestRouter.request2Resources()",
            "root.MSG_GEN_PARAM_VALUE", "Resource=" + resource.toString());
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
      ProjectManagerSessionController projectManagerSC) throws RemoteException,
      ParseException {
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
      task = (TaskDetail) tasks.get(a);
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
    return Boolean.valueOf("admin".equals(role) ||
        ("responsable".equals(role) && Integer.parseInt(projectManagerSC.getUserId()) == task
            .getResponsableId()));
  }
}
