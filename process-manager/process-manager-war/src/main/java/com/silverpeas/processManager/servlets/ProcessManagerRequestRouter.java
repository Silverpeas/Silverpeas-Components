/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.silverpeas.processManager.servlets;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.processManager.*;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.silverpeas.workflow.api.error.WorkflowError;
import com.silverpeas.workflow.api.instance.HistoryStep;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.instance.Question;
import com.silverpeas.workflow.api.model.*;
import com.silverpeas.workflow.api.task.Task;
import com.silverpeas.workflow.engine.model.ActionRefs;
import com.silverpeas.workflow.engine.model.StateImpl;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.attachment.model.UnlockContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProcessManagerRequestRouter
    extends ComponentRequestRouter<ProcessManagerSessionController> {

  private static final long serialVersionUID = -4758787807784357891L;

  /**
   * Returns the name used by the ComponentRequestRequest to store the session controller in the
   * user session.
   */
  @Override
  public String getSessionControlBeanName() {
    return "processManager";
  }

  /**
   * Return a new ProcessManagerSessionController wich will be used for each request made in the
   * given componentContext. Returns a ill session controler when the a fatal error occures. This
   * ill session controller can only display an error page.
   *
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   */
  @Override
  public ProcessManagerSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    SilverTrace.info("kmelia", "ProcessManagerRequestRouter.createComponentSessionController()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return new ProcessManagerSessionController(mainSessionCtrl, componentContext);
    } catch (ProcessManagerException e) {
      return new ProcessManagerSessionController(mainSessionCtrl, componentContext, e);
    }
  }

  /**
   * Process the request and returns the response url.
   *
   * @param function the user request name
   * @param request the user request params
   * @param sessionController the user request context
   */
  @Override
  public String getDestination(String function, ProcessManagerSessionController sessionController,
      HttpServletRequest request) {
    SilverTrace.info("processManager", "ProcessManagerRequestRouter.getDestination()",
        "root.MSG_GEN_ENTER_METHOD", "function = " + function);
    FunctionHandler handler = getHandlerMap().get(function);
    Exception error = sessionController.getFatalException();
    if (handler != null && error == null) {
      try {
        return handler.getDestination(function, sessionController, request);
      } catch (ProcessManagerException e) {
        error = e;
      }
    }
    if (error != null) {
      request.setAttribute("javax.servlet.jsp.jspException", error);
    }
    if ("Main".equals(function) || "listProcess".equals(function)) {
      return "/admin/jsp/errorpageMain.jsp";
    }
    return "/admin/jsp/errorpageMain.jsp";
  }

  /**
   * Init this servlet, before any request.
   */
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    if (handlerMap == null) {
      initHandlers();
    }
  }

  private Map<String, FunctionHandler> getHandlerMap() {
    SilverTrace.info("processManager", "ProcessManagerRequestRouter.getHandlerMap()",
        "root.MSG_GEN_ENTER_METHOD");
    if (handlerMap == null) {
      initHandlers();
    }
    return handlerMap;
  }
  /**
   * Map the function name to the function handler
   */
  static private Map<String, FunctionHandler> handlerMap = null;

  /**
   * Inits the function handler
   */
  synchronized private void initHandlers() {
    if (handlerMap != null) {
      return;
    }
    handlerMap = new HashMap<String, FunctionHandler>(35);
    handlerMap.put("Main", listProcessHandler);
    handlerMap.put("listProcess", listProcessHandler);
    handlerMap.put("listSomeProcess", listSomeProcessHandler);
    handlerMap.put("changeRole", changeRoleHandler);
    handlerMap.put("filterProcess", filterProcessHandler);
    handlerMap.put("viewProcess", viewProcessHandler);
    handlerMap.put("removeLock", removeLockHandler);
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
    handlerMap.put("ToWysiwygWelcome", toWelcomeWysiwyg);
    handlerMap.put("FromWysiwygWelcome", listProcessHandler);
    handlerMap.put("adminRemoveProcess", adminRemoveProcessHandler);
    handlerMap.put("adminViewErrors", adminViewErrorsHandler);
    handlerMap.put("adminReAssign", adminReAssignHandler);
    handlerMap.put("adminDoReAssign", adminDoReAssignHandler);
  }
  /**
   * The removeProcess handler for the supervisor.
   */
  static private FunctionHandler adminRemoveProcessHandler = new SessionSafeFunctionHandler() {
    @Override
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      String processId = request.getParameter("processId");
      SilverTrace.debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
          "root.MSG_GEN_ENTER_METHOD", session.getTrace("adminRemoveProcess", "processId="
          + processId));
      session.removeProcess(processId);
      return listProcessHandler.getDestination(function, session, request);
    }
  };
  /**
   * The viewErrors handler for the supervisor
   */
  static private FunctionHandler adminViewErrorsHandler = new SessionSafeFunctionHandler() {
    @Override
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      String processId = request.getParameter("processId");
      session.resetCurrentProcessInstance(processId);
      SilverTrace.
          debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
          "root.MSG_GEN_ENTER_METHOD", session.getTrace("adminViewErrors", "processId=" + processId));
      WorkflowError[] errors = session.getProcessInstanceErrors(processId);
      request.setAttribute("errors", errors);
      setSharedAttributes(session, request);
      return "/processManager/jsp/admin/viewErrors.jsp";
    }
  };
  /**
   * The reAssign handler for the supervisor
   */
  static private FunctionHandler adminReAssignHandler = new SessionSafeFunctionHandler() {
    @Override
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      String processId = request.getParameter("processId");
      session.resetCurrentProcessInstance(processId);
      SilverTrace.debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
          "root.MSG_GEN_ENTER_METHOD", session.getTrace("adminReAssign", "processId=" + processId));
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
  static private FunctionHandler adminDoReAssignHandler = new SessionSafeFunctionHandler() {
    @Override
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      SilverTrace.debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
          "root.MSG_GEN_ENTER_METHOD", session.getTrace("adminDoReAssign", ""));
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
        form.update(items, data, context);
        session.reAssign(data);
        return listProcessHandler.getDestination(function, session, request);
      } catch (FormException e) {
        throw new ProcessManagerException("ProcessManagerRequestRouter",
            "processManager.ILL_CREATE_FORM", e);
      }
    }
  };
  /**
   * The listProcess handler. Used as the Main handler too.
   */
  static private FunctionHandler listProcessHandler = new SessionSafeFunctionHandler() {
    @Override
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      SilverTrace.debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
          "root.MSG_GEN_ENTER_METHOD", session.getTrace("listProcess", ""));
      if (session.hasUserSettings() && !session.isUserSettingsOK()) {
        return editUserSettingsHandler.getDestination(function, session, request);
      }
      if (session.hasUserSettings()) {
        request.setAttribute("hasUserSettings", "1");
      } else {
        request.setAttribute("hasUserSettings", "0");
      }
      request.setAttribute("isCSVExportEnabled", session.isCSVExportEnabled());
      Item[] folderItems = session.getFolderItems();
      request.setAttribute("FolderItems", folderItems);
      RecordTemplate listHeaders = session.getProcessListHeaders();
      request.setAttribute("listHeaders", listHeaders);
      DataRecord[] processList;
      if (request.getAttribute("dontreset") == null) {
        processList = session.resetCurrentProcessList();
      } else {
        processList = session.getCurrentProcessList();
      }
      request.setAttribute("processList", processList);
      String welcomeMessage = WysiwygController.load(session.getComponentId(),
          session.getComponentId(), session.getLanguage());
      request.setAttribute("WelcomeMessage", welcomeMessage);
      setProcessFilterAttributes(session, request, session.getCurrentFilter());
      setSharedAttributes(session, request);
      return "/processManager/jsp/listProcess.jsp";
    }
  };
  /**
   * The listProcess handler (modified in order to skip the list re-computation).
   */
  static private FunctionHandler listSomeProcessHandler = new SessionSafeFunctionHandler() {
    @Override
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      request.setAttribute("dontreset", "no, dont");
      SilverTrace.debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
          "root.MSG_GEN_ENTER_METHOD", session.getTrace("listSomeProcess", ""));
      return listProcessHandler.getDestination(function, session, request);
    }
  };
  /**
   * The changeRole handler.
   */
  static private FunctionHandler changeRoleHandler = new SessionSafeFunctionHandler() {
    @Override
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      String roleName = request.getParameter("role");
      session.resetCurrentRole(roleName);
      SilverTrace.debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
          "root.MSG_GEN_ENTER_METHOD", session.getTrace("changeRole", "new role=" + roleName));
      return listProcessHandler.getDestination(function, session, request);
    }
  };
  /**
   * The filterProcess handler.
   */
  static private FunctionHandler filterProcessHandler = new SessionSafeFunctionHandler() {
    @Override
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      SilverTrace.debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
          "root.MSG_GEN_ENTER_METHOD", session.getTrace("filterProcess", ""));
      ProcessFilter filter = session.getCurrentFilter();
      updateProcessFilter(session, request, filter, items);
      return listProcessHandler.getDestination(function, session, request);
    }
  };
  /**
   * The attachmentManager handler
   */
  static private FunctionHandler attachmentManagerHandler = new SessionSafeFunctionHandler() {
    @Override
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items)
        throws ProcessManagerException {
      String processId = request.getParameter("processId");
      if (processId != null) {
        session.resetCurrentProcessInstance(processId);
      }
      SilverTrace.debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
          "root.MSG_GEN_ENTER_METHOD", session.getTrace("attachmentManager", ""));
      setSharedAttributes(session, request);
      return "/processManager/jsp/attachmentManager.jsp";
    }
  };
  /**
   * The removeLock handler
   */
  static private FunctionHandler removeLockHandler = new SessionSafeFunctionHandler() {
    @Override
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      String processId = request.getParameter("processId");
      String stateName = request.getParameter("stateName");
      String userId = request.getParameter("userId");
      session.resetCurrentProcessInstance(processId);
      SilverTrace.debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
          "root.MSG_GEN_ENTER_METHOD", session.getTrace("removeLock",
          "stateName=" + stateName + ",lock_userId=" + userId));

      session.unlock(userId, stateName);

      return viewProcessHandler.getDestination(function, session, request);
    }
  };
  /**
   * The viewProcess handler
   */
  static private FunctionHandler viewProcessHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items)
        throws ProcessManagerException {
      String processId = request.getParameter("processId");
      String force = request.getParameter("force");

      session.resetCurrentProcessInstance(processId);

      SilverTrace.debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
          "root.MSG_GEN_ENTER_METHOD", session.getTrace("viewProcess", "force=" + force));

      if ((force == null || !force.equals("true")) && (session.hasPendingQuestions())) {
        return listQuestionsHandler.getDestination(function, session, request);
      }

      com.silverpeas.form.Form form = session.getPresentationForm();
      request.setAttribute("form", form);

      PagesContext context = getFormContext("presentation", "0", session, true);
      request.setAttribute("context", context);

      String[] activeStates = session.getActiveStates();
      request.setAttribute("activeStates", activeStates);

      String[] roles = session.getActiveRoles();
      request.setAttribute("activeRoles", roles);

      DataRecord data = session.getFolderRecord();
      request.setAttribute("data", data);

      String[] deleteAction = session.getDeleteAction();
      if (deleteAction != null) {
        request.setAttribute("deleteAction", deleteAction);
      }

      List<LockVO> locks = session.getLockingUsers();
      if (locks != null) {
        request.setAttribute("locks", locks);
        request.setAttribute("isCurrentUserIsLockingUser", session.isCurrentUserIsLockingUser());
      } else {
        request.setAttribute("isCurrentUserIsLockingUser", false);
      }


      setSharedAttributes(session, request);
      return "/processManager/jsp/viewProcess.jsp";
    }
  };
  /**
   * The searchResult handler
   */
  static private FunctionHandler searchResultHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items)
        throws ProcessManagerException {
      String type = request.getParameter("Type");
      String todoId = request.getParameter("Id");

      SilverTrace.debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
          "root.MSG_GEN_ENTER_METHOD",
          session.getTrace("searchResult", "Type=" + type + ", Id=" + todoId));

      // Accept only links coming from todo details
      if (type == null || (!type.equals("com.stratelia.webactiv.calendar.backbone.TodoDetail")
          && !type
          .equals("ProcessInstance"))) {
        return listProcessHandler.getDestination(function, session, request);
      }

      String processId = todoId;
      if (type.equals("com.stratelia.webactiv.calendar.backbone.TodoDetail")) {
        // from todo, todoId is in fact the externalId
        processId = session.getProcessInstanceIdFromExternalTodoId(todoId);

        String roleName = session.getRoleNameFromExternalTodoId(todoId);
        session.resetCurrentRole(roleName);
      } else {
        String roleName = request.getParameter("role");
        SilverTrace.debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
            "root.MSG_GEN_ENTER_METHOD",
            session.getTrace("searchResult", "Changing role to :" + roleName));
        SilverTrace.debug("processManager",
            "ProcessManagerRequestRouter.getDestination",
            "root.MSG_GEN_PARAM_VALUE", "From notification, role=" + roleName);
        if (roleName != null) {
          session.resetCurrentRole(roleName);
        }
      }

      session.resetCurrentProcessInstance(processId);
      if (!session.isUserAllowedOnActiveStates()) {
        // user is not allowed to act on or view current process instance
        // redirect him on home page
        return listProcessHandler.getDestination(function, session, request);
      }

      if (session.hasPendingQuestions()) {
        return listQuestionsHandler.getDestination(function, session, request);
      }

      com.silverpeas.form.Form form = session.getPresentationForm();
      request.setAttribute("form", form);

      PagesContext context = getFormContext("presentation", "0", session, true);
      request.setAttribute("context", context);

      String[] activeStates = session.getActiveStates();
      request.setAttribute("activeStates", activeStates);

      String[] roles = session.getActiveRoles();
      request.setAttribute("activeRoles", roles);

      DataRecord data = session.getFolderRecord();
      request.setAttribute("data", data);

      String[] deleteAction = session.getDeleteAction();
      if (deleteAction != null) {
        request.setAttribute("deleteAction", deleteAction);
      }

      List<LockVO> locks = session.getLockingUsers();
      if (locks != null) {
        request.setAttribute("locks", locks);
        request.setAttribute("isCurrentUserIsLockingUser", session.isCurrentUserIsLockingUser());
      } else {
        request.setAttribute("isCurrentUserIsLockingUser", false);
      }

      setSharedAttributes(session, request);
      return "/processManager/jsp/viewProcess.jsp";
    }
  };
  /**
   * The viewHistory handler
   */
  static private FunctionHandler viewHistoryHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items)
        throws ProcessManagerException {
      String processId = request.getParameter("processId");

      session.resetCurrentProcessInstance(processId);

      SilverTrace.debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
          "root.MSG_GEN_ENTER_METHOD", session.getTrace("viewHistory", ""));

      String strEnlightedStep = request.getParameter("enlightedStep");
      request.setAttribute("enlightedStep", strEnlightedStep);

      List<StepVO> steps = session.getSteps(strEnlightedStep);
      request.setAttribute("steps", steps);

      setSharedAttributes(session, request);
      return "/processManager/jsp/viewHistory.jsp";
    }
  };
  /**
   * The createProcess handler
   */
  static private FunctionHandler createProcessHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items)
        throws ProcessManagerException {

      session.resetCurrentProcessInstance();

      SilverTrace.debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
          "root.MSG_GEN_ENTER_METHOD", session.getTrace("createProcess", ""));

      com.silverpeas.form.Form form = session.getCreationForm();
      request.setAttribute("form", form);

      PagesContext context = getFormContext("createForm", "0", session, true);
      request.setAttribute("context", context);

      DataRecord data = session.getEmptyCreationRecord();
      request.setAttribute("data", data);

      request.setAttribute("isFirstTimeSaved", "yes");

      setSharedAttributes(session, request);

      // Session Safe : Generate token Id
      generateTokenId(session, request);

      return "/processManager/jsp/createProcess.jsp";
    }
  };
  /**
   * The saveCreation handler
   */
  static private FunctionHandler saveCreationHandler = new SessionSafeFunctionHandler() {
    @Override
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      com.silverpeas.form.Form form = session.getCreationForm();
      PagesContext context = getFormContext("createForm", "0", session);
      DataRecord data = session.getEmptyCreationRecord();

      try {
        // Session Safe : reset Token Id
        resetTokenId(session, request);

        // use random id as temporary object id
        context.setObjectId(UUID.randomUUID().toString());
        List<String> attachmentIds = form.update(items, data, context, false);

        boolean isDraft = StringUtil.getBooleanValue(FileUploadUtil.getParameter(items, "isDraft"));
        boolean isFirstTimeSaved = StringUtil.getBooleanValue(FileUploadUtil.getParameter(items,
            "isFirstTimeSaved"));

        SilverTrace.debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
            "root.MSG_GEN_ENTER_METHOD", session.getTrace("saveCreation",
            "isDraft=" + isDraft + ", isFirstTimeSaved=" + isFirstTimeSaved));
        String instanceId = session.createProcessInstance(data, isDraft, isFirstTimeSaved);
        
        // launch update again to have a correct object id in wysiwyg
        context.setObjectId(instanceId);
        form.updateWysiwyg(items, data, context);
        
        // Attachment's foreignkey must be set with the just created instanceId
        for (String attachmentId : attachmentIds) {

          SimpleDocumentPK pk = new SimpleDocumentPK(attachmentId, session.getComponentId());
          SimpleDocument document = AttachmentServiceFactory.getAttachmentService().
              searchDocumentById(pk, null);
          document.setForeignId(instanceId);
          AttachmentServiceFactory.getAttachmentService().lock(attachmentId, session.getUserId(),
              null);
          AttachmentServiceFactory.getAttachmentService().updateAttachment(document, false, false);
          AttachmentServiceFactory.getAttachmentService().unlock(new UnlockContext(attachmentId,
              session.getUserId(), null));
        }

        return listProcessHandler.getDestination(function, session, request);
      } catch (FormException e) {
        throw new ProcessManagerException("ProcessManagerRequestRouter",
            "processManager.ILL_CREATE_FORM", e);
      }
    }
  };
  /**
   * The listTasks handler
   */
  static private FunctionHandler listTasksHandler = new SessionSafeFunctionHandler() {
    @Override
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      String processId = request.getParameter("processId");

      ProcessInstance process = session.resetCurrentProcessInstance(processId);

      SilverTrace.debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
          "root.MSG_GEN_ENTER_METHOD", session.getTrace("listTasks", ""));

      // checking locking users
      List<LockVO> locks = session.getLockingUsers();
      if ((!locks.isEmpty()) && (!session.isCurrentUserIsLockingUser())) {
        return listProcessHandler.getDestination(function, session, request);
      }

      // check if an action must be resumed
      if (!locks.isEmpty()) {

        // Detects special case where user has killed his navigator while filling an action form
        HistoryStep savedStep = session.getSavedStep();
        if (savedStep != null) {
          return resumeActionHandler.getDestination(function, session, request);
        }
      }

      if (!process.getErrorStatus()) {
        Task[] tasks = session.getTasks();

        SilverTrace.debug("processManager", "ProcessManagerRequestRouter.getDestination",
            "root.MSG_GEN_PARAM_VALUE", "gettings tasks list, nb found : " + ((tasks == null) ? 0
            : tasks.length));

        for (int i = 0; tasks != null && i < tasks.length; i++) {
          SilverTrace.debug("processManager", "ProcessManagerRequestRouter.getDestination",
              "root.MSG_GEN_PARAM_VALUE", "filtering task actions, task no : " + i);
          State state = tasks[i].getState();
          AllowedActions filteredActions = new ActionRefs();
          if (state.getAllowedActionsEx() != null) {
            Iterator<AllowedAction> actions = state.getAllowedActionsEx().iterateAllowedAction();
            while (actions.hasNext()) {
              AllowedAction action = actions.next();
              QualifiedUsers qualifiedUsers = action.getAction().getAllowedUsers();

              List<String> grantedUserIds = session.getUsers(qualifiedUsers, true);
              SilverTrace.debug("processManager", "ProcessManagerRequestRouter.getDestination",
                  "root.MSG_GEN_PARAM_VALUE", "granted user ids for action " + action.getAction().
                  getName() + " : " + grantedUserIds);
              if (grantedUserIds.contains(session.getUserId())) {
                filteredActions.addAllowedAction(action);
              }
            }
          } else {
            SilverTrace.debug("processManager", "ProcessManagerRequestRouter.getDestination",
                "root.MSG_GEN_PARAM_VALUE", "no action found for task no : " + i);
          }
          state.setFilteredActions(filteredActions);
        }

        request.setAttribute("tasks", tasks);
        request.setAttribute("ViewReturn", session.isViewReturn());
        request.setAttribute("Error", Boolean.FALSE);
      } else {
        request.setAttribute("Error", Boolean.TRUE);
      }
      setSharedAttributes(session, request);
      return "/processManager/jsp/listTasks.jsp";
    }
  };
  /**
   * The resumeAction handler
   */
  static private FunctionHandler resumeActionHandler = new SessionSafeFunctionHandler() {
    @Override
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      SilverTrace.debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
          "root.MSG_GEN_ENTER_METHOD", session.getTrace("resumeAction", ""));
      // retrieve state name and action name
      HistoryStep savedStep = session.getSavedStep();
      String stateName = savedStep.getResolvedState();
      String actionName = savedStep.getAction();

      // Assume that user switch to same role as saved action
      String roleName = savedStep.getUserRoleName();
      session.resetCurrentRole(roleName);
      State state = (stateName == null) ? new StateImpl("") : session.getState(stateName);

      request.setAttribute("state", state);
      request.setAttribute("action", session.getAction(actionName));

      // Get the associated form
      com.silverpeas.form.Form form = session.getActionForm(stateName, actionName);
      request.setAttribute("form", form);

      // Set the form context
      PagesContext context = getFormContext("actionForm", "0", session, true);
      request.setAttribute("context", context);

      // Get the form data
      DataRecord data = session.getSavedStepRecord(savedStep);
      request.setAttribute("data", data);

      // Set flag to indicate action record has already been saved as draft
      request.setAttribute("isFirstTimeSaved", "no");

      // Set flag to indicate instance is in resuming mode
      session.setResumingInstance(true);

      // Set global attributes
      setSharedAttributes(session, request);
      // Session Safe : Generate token Id
      generateTokenId(session, request);
      return "/processManager/jsp/editAction.jsp";
    }
  };
  /**
   * The editAction handler
   */
  static private FunctionHandler editActionHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items)
        throws ProcessManagerException {

      // Set process instance
      String processId = request.getParameter("processId");
      session.resetCurrentProcessInstance(processId);

      // retrieve state name and action name
      String stateName = request.getParameter("state");
      String actionName = request.getParameter("action");

      SilverTrace.debug(
          "processManagerTrace",
          "ProcessManagerRequestRouter.getDestination()",
          "root.MSG_GEN_ENTER_METHOD",
          session.getTrace("editAction", "stateName=" + stateName + ", actionName=" + actionName));

      // Get the associated form
      com.silverpeas.form.Form form = session.getActionForm(stateName, actionName);
      if (form == null) {
        // no form associated to this action, process action directly
        DataRecord data = session.getActionRecord(stateName, actionName);

        // lock the process instance
        session.lock(stateName);

        session.processAction(stateName, actionName, data, false, true);

        return listProcessHandler.getDestination(function, session, request);
      } else {
        // a form is associated to this action, display it to process action
        request.setAttribute("state", session.getState(stateName));
        request.setAttribute("action", session.getAction(actionName));
        request.setAttribute("form", form);

        // Set the form context
        PagesContext context = getFormContext("actionForm", "0", session, true);
        request.setAttribute("context", context);

        // Get the form data
        DataRecord data = session.getActionRecord(stateName, actionName);
        request.setAttribute("data", data);

        // Set flag to indicate action record has never been saved as draft for this step
        request.setAttribute("isFirstTimeSaved", "yes");

        // lock the process instance
        session.lock(stateName);

        // Set global attributes
        setSharedAttributes(session, request);

        // Session Safe : Generate token Id
        generateTokenId(session, request);

        return "/processManager/jsp/editAction.jsp";
      }
    }
  };
  /**
   * The saveAction handler
   */
  static private FunctionHandler saveActionHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items)
        throws ProcessManagerException {

      try {
        // Session Safe : reset Token Id
        resetTokenId(session, request);

        String stateName = FileUploadUtil.getParameter(items, "state");
        String actionName = FileUploadUtil.getParameter(items, "action");

        boolean isDraft = StringUtil.getBooleanValue(FileUploadUtil.getParameter(items, "isDraft"));
        boolean isFirstTimeSaved =
            StringUtil.getBooleanValue(FileUploadUtil.getParameter(items, "isFirstTimeSaved"));

        SilverTrace.debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
            "root.MSG_GEN_ENTER_METHOD", session.getTrace("saveAction",
            "stateName=" + stateName + ", actionName=" + actionName + ", isDraft=" + isDraft
            + ", isFirstTimeSaved=" + isFirstTimeSaved));

        com.silverpeas.form.Form form = session.getActionForm(stateName, actionName);
        PagesContext context = getFormContext("actionForm", "0", session);
        DataRecord data = session.getActionRecord(stateName, actionName);

        if (form != null) {
          form.update(items, data, context);
        }
        session.processAction(stateName, actionName, data, isDraft, isFirstTimeSaved);

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
  static private FunctionHandler cancelActionHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items)
        throws ProcessManagerException {
      String stateName = request.getParameter("state");

      SilverTrace.debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
          "root.MSG_GEN_ENTER_METHOD", session.getTrace("cancelAction", "stateName=" + stateName));

      // special case : a cancel occured when resuming a precedent action (saved in draft)
      if (session.isResumingInstance()) {
        return listProcessHandler.getDestination(function, session, request);
      }

      // unlock the process instance
      session.unlock(stateName);

      return listTasksHandler.getDestination(function, session, request);
    }
  };
  /**
   * The cancelResponse handler
   */
  static private FunctionHandler cancelResponseHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items)
        throws ProcessManagerException {
      String stateName = request.getParameter("state");

      SilverTrace.debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
          "root.MSG_GEN_ENTER_METHOD",
          session.getTrace("cancelResponse", "stateName=" + stateName));

      // unlock the process instance
      session.unlock(stateName);

      return viewProcessHandler.getDestination(function, session, request);
    }
  };
  /**
   * The editQuestion handler
   */
  static private FunctionHandler editQuestionHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items)
        throws ProcessManagerException {
      String stepId = request.getParameter("stepId");

      SilverTrace.debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
          "root.MSG_GEN_ENTER_METHOD", session.getTrace("editQuestion", "stepId=" + stepId));

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

      // Session Safe : Generate token Id
      generateTokenId(session, request);

      return "/processManager/jsp/editQuestion.jsp";
    }
  };
  /**
   * The saveQuestion handler
   */
  static private FunctionHandler saveQuestionHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items)
        throws ProcessManagerException {
      try {
        // Session Safe : reset Token Id
        resetTokenId(session, request);

        String stepId = FileUploadUtil.getParameter(items, "stepId");
        String state = FileUploadUtil.getParameter(items, "state");

        SilverTrace.debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
            "root.MSG_GEN_ENTER_METHOD",
            session.getTrace("saveQuestion", "stepId=" + stepId + ", state=" + state));

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
  static private FunctionHandler editResponseHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items)
        throws ProcessManagerException {
      String questionId = request.getParameter("questionId");

      SilverTrace.debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
          "root.MSG_GEN_ENTER_METHOD",
          session.getTrace("editResponse", "questionId=" + questionId));

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

      // Session Safe : Generate token Id
      generateTokenId(session, request);

      return "/processManager/jsp/editResponse.jsp";
    }
  };
  /**
   * The saveResponse handler
   */
  static private FunctionHandler saveResponseHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items)
        throws ProcessManagerException {
      try {
        // Session Safe : reset Token Id
        resetTokenId(session, request);

        String questionId = FileUploadUtil.getParameter(items, "questionId");

        SilverTrace.debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
            "root.MSG_GEN_ENTER_METHOD",
            session.getTrace("saveResponse", "questionId=" + questionId));

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
  static private FunctionHandler editUserSettingsHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items)
        throws ProcessManagerException {
      SilverTrace.debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
          "root.MSG_GEN_ENTER_METHOD", session.getTrace("editUserSettings", ""));

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
  static private FunctionHandler saveUserSettingsHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items)
        throws ProcessManagerException {
      com.silverpeas.form.Form form = session.getUserSettingsForm();
      PagesContext context = getFormContext("userSettingsForm", "0", session);
      DataRecord data = session.getEmptyUserSettingsRecord();

      SilverTrace.debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
          "root.MSG_GEN_ENTER_METHOD", session.getTrace("saveUserSettings", ""));

      try {
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
  static private FunctionHandler listQuestionsHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items)
        throws ProcessManagerException {
      String processId = request.getParameter("processId");

      session.resetCurrentProcessInstance(processId);

      SilverTrace.debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
          "root.MSG_GEN_ENTER_METHOD", session.getTrace("listQuestions", ""));

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
      ProcessFilter filter, List<FileItem> items)
      throws ProcessManagerException {

    try {
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
          "processManager.ILL_FILTER_FORM", e);
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
  static private FunctionHandler printProcessFramesetHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items)
        throws ProcessManagerException {
      setSharedAttributes(session, request);
      return "/processManager/jsp/printProcessFrameset.jsp";
    }
  };
  /**
   * The printProcess handler
   */
  static private FunctionHandler printProcessHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items)
        throws ProcessManagerException {
      SilverTrace.debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
          "root.MSG_GEN_ENTER_METHOD", session.getTrace("printProcess", ""));

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
  static private FunctionHandler printButtonsHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items)
        throws ProcessManagerException {
      setSharedAttributes(session, request);
      return "/processManager/jsp/printButtons.jsp";
    }
  };
  static private FunctionHandler toWelcomeWysiwyg = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items)
        throws ProcessManagerException {

      StringBuilder destination = new StringBuilder();

      try {
        String returnURL = URLEncoder.encode(
            URLManager.getApplicationURL()
            + URLManager.getURL(null, session.getComponentId())
            + "FromWysiwygWelcome", "UTF-8");
        destination.append("/wysiwyg/jsp/htmlEditor.jsp?");
        destination.append("SpaceName=").append(
            URLEncoder.encode(session.getSpaceLabel(), "UTF-8"));
        destination.append("&ComponentId=").append(session.getComponentId());
        destination.append("&ComponentName=").append(
            URLEncoder.encode(session.getComponentLabel(), "UTF-8"));
        destination.append("&BrowseInfo=")
            .append(session.getString("processManager.welcomeWysiwyg"));
        destination.append("&Language=").append(session.getLanguage());
        destination.append("&ObjectId=").append(session.getComponentId());
        destination.append("&ReturnUrl=").append(returnURL);
      } catch (UnsupportedEncodingException e) {
        throw new ProcessManagerException("processManager", "processManager.CANT_GO_TO_WYSIWYG", e);
      }

      return destination.toString();
    }
  };
  static private FunctionHandler exportCSVHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function,
        ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items)
        throws ProcessManagerException {
      String csvFilename = session.exportListAsCSV();

      SilverTrace.debug("processManagerTrace", "ProcessManagerRequestRouter.getDestination()",
          "root.MSG_GEN_ENTER_METHOD", session.getTrace("exportCSV", ""));

      request.setAttribute("CSVFilename", csvFilename);
      if (StringUtil.isDefined(csvFilename)) {
        File file = new File(FileRepositoryManager.getTemporaryPath() + csvFilename);
        request.setAttribute("CSVFileSize", Long.valueOf(file.length()));
        request.setAttribute("CSVFileURL", FileServerUtils.getUrlToTempDir(csvFilename));
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
    request.setAttribute("isSaveButtonEnabled", new Boolean(session.isSaveButtonEnabled()));
    request.setAttribute("isReturnEnabled", new Boolean(session.isViewReturn()));
  }

  /**
   * Read an int parameter.
   */
  static int intValue(String parameter, int defaultValue) {
    try {
      if (parameter != null) {
        return (new Integer(parameter)).intValue();
      } else {
        return defaultValue;
      }
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
