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

package com.silverpeas.processManager.servlets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.processManager.HistoryStepContent;
import com.silverpeas.processManager.ProcessFilter;
import com.silverpeas.processManager.ProcessManagerException;
import com.silverpeas.processManager.ProcessManagerSessionController;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.silverpeas.workflow.api.error.WorkflowError;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.instance.Question;
import com.silverpeas.workflow.api.model.AllowedAction;
import com.silverpeas.workflow.api.model.AllowedActions;
import com.silverpeas.workflow.api.model.Item;
import com.silverpeas.workflow.api.model.QualifiedUsers;
import com.silverpeas.workflow.api.model.State;
import com.silverpeas.workflow.api.task.Task;
import com.silverpeas.workflow.engine.model.ActionRefs;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import java.io.File;
import java.util.Iterator;

public class ProcessManagerRequestRouter extends ComponentRequestRouter {
  /**
   * Returns the name used by the ComponentRequestRequest to store the session controller in the
   * user session.
   */
  public String getSessionControlBeanName() {
    return "processManager";
  }

  /**
   * Return a new ProcessManagerSessionController wich will be used for each request made in the
   * given componentContext. Returns a ill session controler when the a fatal error occures. This
   * ill session controller can only display an error page.
   */
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    SilverTrace.info("kmelia", "ProcessManagerRequestRouter.createComponentSessionController()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return new ProcessManagerSessionController(
          mainSessionCtrl, componentContext);
    } catch (ProcessManagerException e) {
      return new ProcessManagerSessionController(
          mainSessionCtrl, componentContext, e);
    }
  }

  /**
   * Process the request and returns the response url.
   * @param function the user request name
   * @param request the user request params
   * @param session the user request context
   */
  public String getDestination(String function, ComponentSessionController sessionController,
      HttpServletRequest request) {
    SilverTrace.info("processManager", "ProcessManagerRequestRouter.getDestination()",
        "root.MSG_GEN_ENTER_METHOD", "function = " + function);
    ProcessManagerSessionController session = (ProcessManagerSessionController) sessionController;
    FunctionHandler handler = (FunctionHandler) getHandlerMap().get(function);
    Exception error = session.getFatalException();

    if (handler != null && error == null) {
      try {
        return handler.getDestination(function, session, request);
      } catch (ProcessManagerException e) {
        error = e;
      }
    }

    if (error != null) {
      request.setAttribute("javax.servlet.jsp.jspException", error);
    }

    if ("Main".equals(function) || "listProcess".equals(function)) {
      return "/admin/jsp/errorpageMain.jsp";
    } else {
      // return "/admin/jsp/errorpage.jsp"; //xoxox pb boucle.
      return "/admin/jsp/errorpageMain.jsp";
    }
  }

  /**
   * Init this servlet, before any request.
   */
  public void init(ServletConfig config)
      throws ServletException {
    super.init(config);
    if (handlerMap == null)
      initHandlers();
  }

  private Map getHandlerMap() {
    SilverTrace.info("processManager", "ProcessManagerRequestRouter.getHandlerMap()",
        "root.MSG_GEN_ENTER_METHOD");
    if (handlerMap == null)
      initHandlers();

    return handlerMap;
  }

  /**
   * Map the function name to the function handler
   */
  static private Map handlerMap = null;

  /**
   * Inits the function handler
   */
  synchronized private void initHandlers() {
    if (handlerMap != null)
      return;

    handlerMap = new HashMap();

    handlerMap.put("Main", listProcessHandler);
    handlerMap.put("listProcess", listProcessHandler);
    handlerMap.put("listSomeProcess", listSomeProcessHandler);
    handlerMap.put("changeRole", changeRoleHandler);
    handlerMap.put("filterProcess", filterProcessHandler);
    handlerMap.put("viewProcess", viewProcessHandler);
    handlerMap.put("viewHistory", viewHistoryHandler);
    handlerMap.put("createProcess", createProcessHandler);
    handlerMap.put("saveCreation", saveCreationHandler);
    handlerMap.put("listTasks", listTasksHandler);
    handlerMap.put("editAction", editActionHandler);
    handlerMap.put("saveAction", saveActionHandler);
    handlerMap.put("cancelAction", cancelActionHandler);
    handlerMap.put("editQuestion", editQuestionHandler);
    handlerMap.put("saveQuestion", saveQuestionHandler);
    handlerMap.put("editResponse", editResponseHandler);
    handlerMap.put("cancelResponse", cancelResponseHandler);
    handlerMap.put("saveResponse", saveResponseHandler);
    handlerMap.put("listQuestions", listQuestionsHandler);
    handlerMap.put("printProcessFrameset", printProcessFramesetHandler);
    handlerMap.put("printProcess", printProcessHandler);
    handlerMap.put("printButtons", printButtonsHandler);
    handlerMap.put("editUserSettings", editUserSettingsHandler);
    handlerMap.put("saveUserSettings", saveUserSettingsHandler);
    handlerMap.put("searchResult.jsp", searchResultHandler);
    handlerMap.put("searchResult", searchResultHandler);
    handlerMap.put("attachmentManager", attachmentManagerHandler);
    handlerMap.put("exportCSV", exportCSVHandler);

    // handlerMap.put("adminListProcess", adminListProcessHandler);
    handlerMap.put("adminRemoveProcess", adminRemoveProcessHandler);
    // handlerMap.put("adminViewProcess", adminViewProcessHandler);
    handlerMap.put("adminViewErrors", adminViewErrorsHandler);
    handlerMap.put("adminReAssign", adminReAssignHandler);
    handlerMap.put("adminDoReAssign", adminDoReAssignHandler);
  }

  /**
   * The listProcess handler for the supervisor.
   */
  /*
   * static private FunctionHandler adminListProcessHandler = new FunctionHandler() { public String
   * getDestination(String function, ProcessManagerSessionController session, HttpServletRequest
   * request) throws ProcessManagerException { Item[] items = session.getFolderItems();
   * request.setAttribute("FolderItems", items); RecordTemplate listHeaders =
   * session.getProcessListHeaders(); request.setAttribute("listHeaders", listHeaders); DataRecord[]
   * processList = null; if (request.getAttribute("dontreset") == null) { processList =
   * session.resetCurrentProcessList(); } else { processList = session.getCurrentProcessList(); }
   * request.setAttribute("processList", processList);
   * setProcessFilterAttributes(session,request,session.getCurrentFilter());
   * setSharedAttributes(session, request); return "/processManager/jsp/admin/listProcess.jsp"; } };
   */

  /**
   * The removeProcess handler for the supervisor.
   */
  static private FunctionHandler adminRemoveProcessHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {
      String processId = request.getParameter("processId");

      session.removeProcess(processId);

      return listProcessHandler.getDestination(function, session, request);
    }
  };

  /**
   * The viewProcess handler for the supervisor
   */
  /*
   * static private FunctionHandler adminViewProcessHandler = new FunctionHandler() { public String
   * getDestination(String function, ProcessManagerSessionController session, HttpServletRequest
   * request) throws ProcessManagerException { String processId = request.getParameter("processId");
   * String force = request.getParameter("force"); ProcessInstance process =
   * session.resetCurrentProcessInstance(processId); if ( (force==null || !force.equals("true")) &&
   * (session.hasPendingQuestions()) ) return listQuestionsHandler.getDestination(function, session,
   * request); com.silverpeas.form.Form form = session.getPresentationForm();
   * request.setAttribute("form", form); PagesContext context = getFormContext("presentation", "0",
   * session, true); request.setAttribute("context", context); String[] activeStates =
   * session.getActiveStates(); request.setAttribute("activeStates", activeStates); String[] roles =
   * session.getActiveRoles(); request.setAttribute("activeRoles", roles); DataRecord data =
   * session.getFolderRecord(); request.setAttribute("data", data); String[] deleteAction =
   * session.getDeleteAction(); if (deleteAction != null) request.setAttribute("deleteAction",
   * deleteAction); setSharedAttributes(session, request); return
   * "/processManager/jsp/admin/viewProcess.jsp"; } };
   */

  /**
   * The viewErrors handler for the supervisor
   */
  static private FunctionHandler adminViewErrorsHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {
      String processId = request.getParameter("processId");

      session.resetCurrentProcessInstance(processId);

      WorkflowError[] errors = session.getProcessInstanceErrors(processId);
      request.setAttribute("errors", errors);

      setSharedAttributes(session, request);
      return "/processManager/jsp/admin/viewErrors.jsp";
    }
  };

  /**
   * The reAssign handler for the supervisor
   */
  static private FunctionHandler adminReAssignHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {
      String processId = request.getParameter("processId");

      session.resetCurrentProcessInstance(processId);

      // Get the associated form
      com.silverpeas.form.Form form = session.getAssignForm();
      request.setAttribute("form", form);

      // Set the form context
      PagesContext context = getFormContext("assignForm", "0", session, true);
      request.setAttribute("context", context);

      // Get the form data
      DataRecord data = session.getAssignRecord();
      request.setAttribute("data", data);

      setSharedAttributes(session, request);
      return "/processManager/jsp/admin/reAssign.jsp";
    }
  };

  /**
   * The doReAssign handler for the supervisor Get the new users affected and creates tasks
   */
  static private FunctionHandler adminDoReAssignHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {
      // Get the associated form
      com.silverpeas.form.Form form = session.getAssignForm();
      request.setAttribute("form", form);

      // Set the form context
      PagesContext context = getFormContext("assignForm", "0", session, true);
      request.setAttribute("context", context);

      // Get the form data
      DataRecord data = session.getAssignRecord();
      request.setAttribute("data", data);

      try {
        List items = FileUploadUtil.parseRequest(request);
        form.update(items, data, context);
        session.reAssign(data);

        return listProcessHandler.getDestination(function, session, request);
      } catch (Exception e) {
        throw new ProcessManagerException("ProcessManagerRequestRouter",
            "processManager.ILL_CREATE_FORM", e);
      }
    }
  };

  /**
   * The listProcess handler. Used as the Main handler too.
   */
  static private FunctionHandler listProcessHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {
      if (session.hasUserSettings() && !session.isUserSettingsOK()) {
        return editUserSettingsHandler.getDestination(function, session, request);
      }

      if (session.hasUserSettings()) {
        request.setAttribute("hasUserSettings", "1");
      } else {
        request.setAttribute("hasUserSettings", "0");
      }

      request.setAttribute("isCSVExportEnabled", new Boolean(session.isCSVExportEnabled()));

      Item[] items = session.getFolderItems();
      request.setAttribute("FolderItems", items);

      RecordTemplate listHeaders = session.getProcessListHeaders();
      request.setAttribute("listHeaders", listHeaders);

      DataRecord[] processList = null;

      if (request.getAttribute("dontreset") == null) {
        processList = session.resetCurrentProcessList();
      } else {
        processList = session.getCurrentProcessList();
      }
      request.setAttribute("processList", processList);

      setProcessFilterAttributes(session, request, session.getCurrentFilter());
      setSharedAttributes(session, request);
      return "/processManager/jsp/listProcess.jsp";
    }
  };

  /**
   * The listProcess handler (modified in order to skip the list re-computation).
   */
  static private FunctionHandler listSomeProcessHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {
      request.setAttribute("dontreset", "no, dont");

      return listProcessHandler.getDestination(function, session, request);
    }
  };

  /**
   * The changeRole handler.
   */
  static private FunctionHandler changeRoleHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {
      String roleName = request.getParameter("role");
      session.resetCurrentRole(roleName);

      return listProcessHandler.getDestination(function, session, request);
    }
  };

  /**
   * The filterProcess handler.
   */
  static private FunctionHandler filterProcessHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {
      ProcessFilter filter = session.getCurrentFilter();
      updateProcessFilter(session, request, filter);

      return listProcessHandler.getDestination(function, session, request);
    }
  };

  /**
   * The attachmentManager handler
   */
  static private FunctionHandler attachmentManagerHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {
      String processId = request.getParameter("processId");

      if (processId != null)
        session.resetCurrentProcessInstance(processId);

      setSharedAttributes(session, request);
      return "/processManager/jsp/attachmentManager.jsp";
    }
  };

  /**
   * The viewProcess handler
   */
  static private FunctionHandler viewProcessHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {
      String processId = request.getParameter("processId");
      String force = request.getParameter("force");

      session.resetCurrentProcessInstance(processId);

      if ((force == null || !force.equals("true")) && (session.hasPendingQuestions()))
        return listQuestionsHandler.getDestination(function, session, request);

      com.silverpeas.form.Form form = session.getPresentationForm();
      request.setAttribute("form", form);

      PagesContext context = getFormContext("presentation", "0", session, true);
      request.setAttribute("context", context);

      String[] activeStates = session.getActiveStates();
      request.setAttribute("activeStates", activeStates);

      /*
       * String[] actors = session.getActiveUsers(); request.setAttribute("actors", actors);
       */

      String[] roles = session.getActiveRoles();
      request.setAttribute("activeRoles", roles);

      DataRecord data = session.getFolderRecord();
      request.setAttribute("data", data);

      String[] deleteAction = session.getDeleteAction();
      if (deleteAction != null)
        request.setAttribute("deleteAction", deleteAction);

      setSharedAttributes(session, request);
      return "/processManager/jsp/viewProcess.jsp";
    }
  };

  /**
   * The searchResult handler
   */
  static private FunctionHandler searchResultHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {
      String type = request.getParameter("Type");
      String todoId = request.getParameter("Id");

      // Accept only links coming from todo details
      if (type == null ||
          (!type.equals("com.stratelia.webactiv.calendar.backbone.TodoDetail") && !type
          .equals("ProcessInstance")))
        return listProcessHandler.getDestination(function, session, request);

      String processId = todoId;
      if (type.equals("com.stratelia.webactiv.calendar.backbone.TodoDetail")) {
        // from todo, todoId is in fact the externalId
        processId = session.getProcessInstanceIdFromExternalTodoId(todoId);

        String roleName = session.getRoleNameFromExternalTodoId(todoId);
        session.resetCurrentRole(roleName);
      }

      session.resetCurrentProcessInstance(processId);

      if (session.hasPendingQuestions())
        return listQuestionsHandler.getDestination(function, session, request);

      com.silverpeas.form.Form form = session.getPresentationForm();
      request.setAttribute("form", form);

      PagesContext context = getFormContext("presentation", "0", session, true);
      request.setAttribute("context", context);

      String[] activeStates = session.getActiveStates();
      request.setAttribute("activeStates", activeStates);

      /*
       * String[] actors = session.getActiveUsers(); request.setAttribute("actors", actors);
       */

      String[] roles = session.getActiveRoles();
      request.setAttribute("activeRoles", roles);

      DataRecord data = session.getFolderRecord();
      request.setAttribute("data", data);

      String[] deleteAction = session.getDeleteAction();
      if (deleteAction != null)
        request.setAttribute("deleteAction", deleteAction);

      setSharedAttributes(session, request);
      return "/processManager/jsp/viewProcess.jsp";
    }

  };

  /**
   * The viewHistory handler
   */
  static private FunctionHandler viewHistoryHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {
      String processId = request.getParameter("processId");

      session.resetCurrentProcessInstance(processId);

      String[] stepActivities = session.getStepActivities();
      request.setAttribute("stepActivities", stepActivities);

      String[] stepActors = session.getStepActors();
      request.setAttribute("stepActors", stepActors);

      String[] stepActions = session.getStepActions();
      request.setAttribute("stepActions", stepActions);

      String[] stepDates = session.getStepDates();
      request.setAttribute("stepDates", stepDates);

      String[] stepVisibles = session.getStepVisibles();
      request.setAttribute("stepVisibles", stepVisibles);

      String strEnlightedStep = request.getParameter("enlightedStep");
      request.setAttribute("enlightedStep", strEnlightedStep);

      if ("all".equalsIgnoreCase(strEnlightedStep)) {
        List stepContents = new ArrayList();
        for (int i = 0; i < stepVisibles.length; i++) {
          com.silverpeas.form.Form form = session.getStepForm(i);
          PagesContext context = getFormContext("dummy", "0", session);
          DataRecord data = session.getStepRecord(i);

          HistoryStepContent stepContent = new HistoryStepContent(form, context, data);
          stepContents.add(stepContent);
        }
        request.setAttribute("StepsContent", stepContents);
      } else {
        int enlightedStep = intValue(strEnlightedStep, -1);

        if (enlightedStep != -1) {
          com.silverpeas.form.Form form = session.getStepForm(enlightedStep);
          request.setAttribute("form", form);
          PagesContext context = getFormContext("dummy", "0", session);
          request.setAttribute("context", context);
          DataRecord data = session.getStepRecord(enlightedStep);
          request.setAttribute("data", data);
        }
      }

      setSharedAttributes(session, request);
      return "/processManager/jsp/viewHistory.jsp";
    }
  };

  /**
   * The createProcess handler
   */
  static private FunctionHandler createProcessHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {
      com.silverpeas.form.Form form = session.getCreationForm();
      request.setAttribute("form", form);

      PagesContext context = getFormContext("createForm", "0", session, true);
      request.setAttribute("context", context);

      DataRecord data = session.getEmptyCreationRecord();
      request.setAttribute("data", data);

      setSharedAttributes(session, request);
      return "/processManager/jsp/createProcess.jsp";
    }
  };

  /**
   * The saveCreation handler
   */
  static private FunctionHandler saveCreationHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {
      com.silverpeas.form.Form form = session.getCreationForm();
      PagesContext context = getFormContext("createForm", "0", session);
      DataRecord data = session.getEmptyCreationRecord();

      try {
        List items = FileUploadUtil.parseRequest(request);
        List attachmentIds = form.update(items, data, context);
        String instanceId = session.createProcessInstance(data);

        // Attachment's foreignkey must be set with the just created instanceId
        String attachmentId = null;
        AttachmentPK attachmentPK = null;
        DocumentPK documentPK = null;
        VersioningUtil versioningUtil = null;
        for (int a = 0; a < attachmentIds.size(); a++) {
          attachmentId = (String) attachmentIds.get(a);

          if (session.isVersionControlled()) {
            if (versioningUtil == null)
              versioningUtil = new VersioningUtil();

            documentPK =
                new DocumentPK(Integer.parseInt(attachmentId), "useless", session.getComponentId());
            versioningUtil.updateDocumentForeignKey(documentPK, instanceId);
          } else {
            attachmentPK = new AttachmentPK(attachmentId, "useless", session.getComponentId());
            AttachmentController.updateAttachmentForeignKey(attachmentPK, instanceId);
          }
        }

        return listProcessHandler.getDestination(function, session, request);
      } catch (Exception e) {
        throw new ProcessManagerException("ProcessManagerRequestRouter",
            "processManager.ILL_CREATE_FORM", e);
      }
    }
  };

  /**
   * The listTasks handler
   */
  static private FunctionHandler listTasksHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {
      String processId = request.getParameter("processId");

      ProcessInstance process = session.resetCurrentProcessInstance(processId);

      if (!process.getErrorStatus()) {
        Task[] tasks = session.getTasks();

        for (int i = 0; tasks != null && i < tasks.length; i++) {
          State state = tasks[i].getState();
          AllowedActions filteredActions = new ActionRefs();
          if (state.getAllowedActionsEx() != null) {
            Iterator<AllowedAction> actions = state.getAllowedActionsEx().iterateAllowedAction();
            while (actions.hasNext()) {
              AllowedAction action = actions.next();
              QualifiedUsers qualifiedUsers = action.getAction().getAllowedUsers();
              if (session.getUsers(qualifiedUsers, true).contains(session.getUserId())) {
                filteredActions.addAllowedAction(action);
              }
            }
          }
          state.setFilteredActions(filteredActions);
        }

        request.setAttribute("tasks", tasks);
        request.setAttribute("ViewReturn", new Boolean(session.isViewReturn()));
        request.setAttribute("Error", Boolean.FALSE);
      } else {
        request.setAttribute("Error", Boolean.TRUE);
      }

      setSharedAttributes(session, request);
      return "/processManager/jsp/listTasks.jsp";
    }
  };

  /**
   * The editAction handler
   */
  static private FunctionHandler editActionHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {
      // Set process instance
      String processId = request.getParameter("processId");
      session.resetCurrentProcessInstance(processId);

      // retrieve state name and action name
      String stateName = request.getParameter("state");
      String actionName = request.getParameter("action");

      request.setAttribute("state", session.getState(stateName));
      request.setAttribute("action", session.getAction(actionName));

      // Get the associated form
      com.silverpeas.form.Form form = session.getActionForm(stateName, actionName);
      request.setAttribute("form", form);

      // Set the form context
      PagesContext context = getFormContext("actionForm", "0", session, true);
      request.setAttribute("context", context);

      // Get the form data
      DataRecord data = session.getActionRecord(stateName, actionName);
      request.setAttribute("data", data);

      // lock the process instance
      session.lock(stateName);

      // Set global attributes
      setSharedAttributes(session, request);

      return "/processManager/jsp/editAction.jsp";
    }
  };

  /**
   * The saveAction handler
   */
  static private FunctionHandler saveActionHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {

      try {
        List items = FileUploadUtil.parseRequest(request);

        String stateName = FileUploadUtil.getParameter(items, "state");
        String actionName = FileUploadUtil.getParameter(items, "action");

        com.silverpeas.form.Form form = session.getActionForm(stateName, actionName);
        PagesContext context = getFormContext("actionForm", "0", session);
        DataRecord data = session.getActionRecord(stateName, actionName);

        if (form != null) {
          form.update(items, data, context);
        }
        session.processAction(stateName, actionName, data);

        return listProcessHandler.getDestination(function, session, request);
      } catch (Exception e) {
        throw new ProcessManagerException("ProcessManagerRequestRouter",
            "processManager.ILL_CREATE_FORM", e);
      }
    }
  };

  /**
   * The cancelAction handler
   */
  static private FunctionHandler cancelActionHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {
      String stateName = request.getParameter("state");

      // unlock the process instance
      session.unlock(stateName);

      return listTasksHandler.getDestination(function, session, request);
    }
  };

  /**
   * The cancelResponse handler
   */
  static private FunctionHandler cancelResponseHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {
      String stateName = request.getParameter("state");

      // unlock the process instance
      session.unlock(stateName);

      return viewProcessHandler.getDestination(function, session, request);
    }
  };

  /**
   * The editQuestion handler
   */
  static private FunctionHandler editQuestionHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {
      String stepId = request.getParameter("stepId");
      request.setAttribute("stepId", stepId);
      request.setAttribute("step", session.getStep(stepId));

      String state = request.getParameter("state");
      request.setAttribute("state", state);

      // Get the question form
      com.silverpeas.form.Form form = session.getQuestionForm(false);
      request.setAttribute("form", form);

      // Set the form context
      PagesContext context = getFormContext("questionForm", "0", session, true);
      request.setAttribute("context", context);

      // Get the form data
      DataRecord data = session.getEmptyQuestionRecord();
      request.setAttribute("data", data);

      // lock the process instance
      session.lock(state);

      setSharedAttributes(session, request);
      return "/processManager/jsp/editQuestion.jsp";
    }
  };

  /**
   * The saveQuestion handler
   */
  static private FunctionHandler saveQuestionHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {
      try {
        List items = FileUploadUtil.parseRequest(request);

        String stepId = FileUploadUtil.getParameter(items, "stepId");
        String state = FileUploadUtil.getParameter(items, "state");

        com.silverpeas.form.Form form = session.getQuestionForm(false);
        PagesContext context = getFormContext("questionForm", "0", session);
        DataRecord data = session.getEmptyQuestionRecord();

        form.update(items, data, context);
        session.processQuestion(stepId, state, data);

        return listProcessHandler.getDestination(function, session, request);
      } catch (Exception e) {
        throw new ProcessManagerException("ProcessManagerRequestRouter",
            "processManager.ILL_CREATE_FORM", e);
      }
    }
  };

  /**
   * The editResponse handler
   */
  static private FunctionHandler editResponseHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {
      String questionId = request.getParameter("questionId");
      request.setAttribute("question", session.getQuestion(questionId));

      // Get the question form (readonly)
      com.silverpeas.form.Form questionForm = session.getQuestionForm(true);
      request.setAttribute("questionForm", questionForm);

      // Get the response form (same as the question)
      com.silverpeas.form.Form responseForm = session.getQuestionForm(false);
      request.setAttribute("responseForm", responseForm);

      // Set the form context
      PagesContext context = getFormContext("responseForm", "0", session, true);
      request.setAttribute("context", context);

      // Get the question form data
      DataRecord questionData = session.getQuestionRecord(questionId);
      request.setAttribute("questionData", questionData);

      // Get the response form data
      DataRecord responseData = session.getEmptyQuestionRecord();
      request.setAttribute("responseData", responseData);

      // lock the process instance
      Question question = session.getQuestion(questionId);
      session.lock(question.getTargetState().getName());

      setSharedAttributes(session, request);
      return "/processManager/jsp/editResponse.jsp";
    }
  };

  /**
   * The saveResponse handler
   */
  static private FunctionHandler saveResponseHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {
      try {
        List items = FileUploadUtil.parseRequest(request);

        String questionId = FileUploadUtil.getParameter(items, "questionId");

        com.silverpeas.form.Form responseForm = session.getQuestionForm(false);
        PagesContext context = getFormContext("responseForm", "0", session);
        DataRecord responseData = session.getEmptyQuestionRecord();

        responseForm.update(items, responseData, context);
        session.processResponse(questionId, responseData);

        return listProcessHandler.getDestination(function, session, request);
      } catch (Exception e) {
        throw new ProcessManagerException("ProcessManagerRequestRouter",
            "processManager.ILL_CREATE_FORM", e);
      }
    }
  };

  /**
   * The editUserSetting handler
   */
  static private FunctionHandler editUserSettingsHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {
      // Get the user settings form
      com.silverpeas.form.Form form = session.getUserSettingsForm();
      request.setAttribute("form", form);

      // Set the form context
      PagesContext context = getFormContext("userSettingsForm", "0", session);
      request.setAttribute("context", context);

      // Get the form data
      DataRecord data = session.getUserSettingsRecord();
      request.setAttribute("data", data);

      setSharedAttributes(session, request);
      return "/processManager/jsp/editUserSettings.jsp";
    }
  };

  /**
   * The saveUserSetting handler
   */
  static private FunctionHandler saveUserSettingsHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {
      com.silverpeas.form.Form form = session.getUserSettingsForm();
      PagesContext context = getFormContext("userSettingsForm", "0", session);
      DataRecord data = session.getEmptyUserSettingsRecord();

      try {
        List items = FileUploadUtil.parseRequest(request);
        form.update(items, data, context);
        session.saveUserSettings(data);

        return listProcessHandler.getDestination(function, session, request);
      } catch (Exception e) {
        throw new ProcessManagerException("ProcessManagerRequestRouter",
            "processManager.ILL_USERSETTINGS_FORM", e);
      }
    }
  };

  /**
   * The listQuestions handler
   */
  static private FunctionHandler listQuestionsHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {
      String processId = request.getParameter("processId");

      session.resetCurrentProcessInstance(processId);

      // Get the question form (readonly)
      com.silverpeas.form.Form questionForm = session.getQuestionForm(true);
      request.setAttribute("form", questionForm);

      // Set the form context
      PagesContext context = getFormContext("responseForm", "0", session);
      request.setAttribute("context", context);

      // Get tasks list
      Task[] tasks = session.getTasks();
      request.setAttribute("tasks", tasks);

      setSharedAttributes(session, request);
      return "/processManager/jsp/listQuestions.jsp";
    }
  };

  /**
   * Builds the ProcessFilter from the http request parameters.
   */
  static private void updateProcessFilter(
      ProcessManagerSessionController session,
      HttpServletRequest request,
      ProcessFilter filter)
      throws ProcessManagerException {

    try {
      List items = FileUploadUtil.parseRequest(request);

      String collapse = FileUploadUtil.getParameter(items, "collapse");

      String oldC = filter.getCollapse();
      filter.setCollapse(collapse);

      // unless the filterPanel was not open.
      if ("false".equals(oldC)) {
        com.silverpeas.form.Form form = filter.getPresentationForm();
        PagesContext context = getFormContext("filter", "1", session);
        DataRecord data = filter.getCriteriaRecord();

        form.update(items, data, context);
        filter.setCriteriaRecord(data);
      }
    } catch (Exception e) {
      throw new ProcessManagerException("ProcessManagerRequestRouter",
          "processManager.ERR_ILL_FILTER_FORM", e);
    }
  }

  /**
   * Send the filter parameters
   */
  static private void setProcessFilterAttributes(
      ProcessManagerSessionController session,
      HttpServletRequest request,
      ProcessFilter filter)
      throws ProcessManagerException {
    String collapse = filter.getCollapse();
    request.setAttribute("collapse", collapse);

    com.silverpeas.form.Form form = filter.getPresentationForm();
    request.setAttribute("form", form);

    PagesContext context = getFormContext("filter", "1", session);
    request.setAttribute("context", context);

    DataRecord data = filter.getCriteriaRecord();
    request.setAttribute("data", data);
  }

  /**
   * The printProcessFrameset handler
   */
  static private FunctionHandler printProcessFramesetHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {
      setSharedAttributes(session, request);
      return "/processManager/jsp/printProcessFrameset.jsp";
    }
  };

  /**
   * The printProcess handler
   */
  static private FunctionHandler printProcessHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {
      // Get the print form
      com.silverpeas.form.Form form = session.getPrintForm(request);
      request.setAttribute("form", form);

      // Set the form context
      PagesContext context = getFormContext("printForm", "0", session);
      request.setAttribute("context", context);

      // Get the form data
      DataRecord data = session.getPrintRecord();
      request.setAttribute("data", data);

      setSharedAttributes(session, request);
      return "/processManager/jsp/printProcess.jsp";
    }
  };

  /**
   * The printButtons handler
   */
  static private FunctionHandler printButtonsHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {
      setSharedAttributes(session, request);
      return "/processManager/jsp/printButtons.jsp";
    }
  };

  static private FunctionHandler exportCSVHandler = new FunctionHandler()
      {
    public String getDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request)
        throws ProcessManagerException {
      String csvFilename = session.exportListAsCSV();

      request.setAttribute("CSVFilename", csvFilename);
      if (StringUtil.isDefined(csvFilename)) {
        File file = new File(FileRepositoryManager.getTemporaryPath() + csvFilename);
        request.setAttribute("CSVFileSize", Long.valueOf(file.length()));
        request.setAttribute("CSVFileURL", FileServerUtils.getUrlToTempDir(csvFilename,
            csvFilename, "text/csv"));
        file = null;
      }

      return "/processManager/jsp/downloadCSV.jsp";
    }
  };

  /**
   * Set attributes shared by all the processManager pages.
   */
  static private void setSharedAttributes(ProcessManagerSessionController session,
      HttpServletRequest request) {
    String canCreate = (session.getCreationRights()) ? "1" : "0";
    boolean isVersionControlled = session.isVersionControlled();
    String s_isVersionControlled = (isVersionControlled ? "1" : "0");

    request.setAttribute("isVersionControlled", s_isVersionControlled);
    request.setAttribute("language", session.getLanguage());
    request.setAttribute("roles", session.getUserRoleLabels());
    request.setAttribute("currentRole", session.getCurrentRole());
    request.setAttribute("canCreate", canCreate);
    request.setAttribute("process", session.getCurrentProcessInstance());
    request.setAttribute("isActiveUser", new Boolean(session.isActiveUser()));
    request.setAttribute("isAttachmentTabEnable", new Boolean(session.isAttachmentTabEnable()));
    request.setAttribute("isHistoryTabEnable", new Boolean(session.isHistoryTabVisible()));
    request.setAttribute("isProcessIdVisible", new Boolean(session.isProcessIdVisible()));
    request.setAttribute("isPrintButtonEnabled", new Boolean(session.isPrintButtonEnabled()));
  }

  /**
   * Read an int parameter.
   */
  static int intValue(String parameter, int defaultValue) {
    try {
      if (parameter != null)
        return (new Integer(parameter)).intValue();
      else
        return defaultValue;
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  static private PagesContext getFormContext(String formName,
      String formIndex,
      ProcessManagerSessionController session) {
    return getFormContext(formName, formIndex, session, false);
  }

  static private PagesContext getFormContext(String formName,
      String formIndex,
      ProcessManagerSessionController session,
      boolean printTitle) {
    PagesContext pagesContext =
        new PagesContext(formName, formIndex, session.getLanguage(), printTitle, session
        .getComponentId(), session.getUserId());

    if (session.getCurrentProcessInstance() != null) {
      String currentInstanceId = session.getCurrentProcessInstance().getInstanceId();
      pagesContext.setObjectId(currentInstanceId);
    }

    // versioning used ?
    pagesContext.setVersioningUsed(session.isVersionControlled());

    return pagesContext;
  }

}
