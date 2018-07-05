/*
 * Copyright (C) 2000 - 2018 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.processmanager.servlets;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.model.UnlockContext;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.workflow.api.error.WorkflowError;
import org.silverpeas.core.workflow.api.instance.HistoryStep;
import org.silverpeas.core.workflow.api.instance.Question;
import org.silverpeas.core.workflow.api.model.Item;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.task.Task;
import org.silverpeas.core.workflow.engine.model.StateImpl;
import org.silverpeas.processmanager.CurrentState;
import org.silverpeas.processmanager.LockVO;
import org.silverpeas.processmanager.ProcessFilter;
import org.silverpeas.processmanager.ProcessManagerException;
import org.silverpeas.processmanager.ProcessManagerSessionController;
import org.silverpeas.processmanager.StepVO;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class ProcessManagerRequestRouter
    extends ComponentRequestRouter<ProcessManagerSessionController> {

  private static final long serialVersionUID = -4758787807784357891L;

  /**
   * Map the function name to the function handler
   */
  private static Map<String, FunctionHandler> handlerMap = null;

  /**
   * The removeProcess handler for the supervisor.
   */
  private static FunctionHandler adminRemoveProcessHandler = new SessionSafeFunctionHandler() {
    @Override
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      String processId = request.getParameter("processId");
      session.removeProcess(processId);
      return listProcessHandler.getDestination(function, session, request);
    }
  };

  /**
   * The viewErrors handler for the supervisor
   */
  private static FunctionHandler adminViewErrorsHandler = new SessionSafeFunctionHandler() {
    @Override
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
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
  private static FunctionHandler adminReAssignHandler = new SessionSafeFunctionHandler() {
    @Override
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      String processId = request.getParameter("processId");
      session.resetCurrentProcessInstance(processId);
      // Get the associated form
      Form form = session.getAssignForm();
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
  private static FunctionHandler adminDoReAssignHandler = new SessionSafeFunctionHandler() {
    @Override
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      // Get the associated form
      Form form = session.getAssignForm();
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
  private static FunctionHandler listProcessHandler = new SessionSafeFunctionHandler() {
    @Override
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
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
      List<DataRecord> processList;
      if (request.getAttribute("dontreset") == null) {
        String from = (String) request.getAttribute("From");
        boolean doAPause = "Creation".equals(from) || "Action".equals(from);
        processList = session.resetCurrentProcessList(doAPause);
      } else {
        processList = session.getCurrentProcessList();
      }
      request.setAttribute("processList", processList);
      setProcessFilterAttributes(session, request);
      setSharedAttributes(session, request);
      return "/processManager/jsp/listProcess.jsp";
    }
  };

  /**
   * The listProcess handler (modified in order to skip the list re-computation).
   */
  private static FunctionHandler listSomeProcessHandler = new SessionSafeFunctionHandler() {
    @Override
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      request.setAttribute("dontreset", "no, dont");
      return listProcessHandler.getDestination(function, session, request);
    }
  };

  /**
   * The changeRole handler.
   */
  private static FunctionHandler changeRoleHandler = new SessionSafeFunctionHandler() {
    @Override
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      String roleName = request.getParameter("role");
      session.resetCurrentRole(roleName);
      return listProcessHandler.getDestination(function, session, request);
    }
  };

  /**
   * The filterProcess handler.
   */
  private static FunctionHandler filterProcessHandler = new SessionSafeFunctionHandler() {
    @Override
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      ProcessFilter filter = session.getCurrentFilter();
      updateProcessFilter(session, filter, items);
      return listProcessHandler.getDestination(function, session, request);
    }
  };

  private static FunctionHandler clearFilterHandler = new SessionSafeFunctionHandler() {
    @Override
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      session.clearFilter();
      return listProcessHandler.getDestination(function, session, request);
    }
  };

  /**
   * The attachmentManager handler
   */
  private static FunctionHandler attachmentManagerHandler = new SessionSafeFunctionHandler() {
    @Override
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      String processId = request.getParameter("processId");
      if (processId != null) {
        session.resetCurrentProcessInstance(processId);
      }
      setSharedAttributes(session, request);
      return "/processManager/jsp/attachmentManager.jsp";
    }
  };

  /**
   * The removeLock handler
   */
  private static FunctionHandler removeLockHandler = new SessionSafeFunctionHandler() {
    @Override
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      String processId = request.getParameter("processId");
      String stateName = request.getParameter("stateName");
      String userId = request.getParameter("userId");
      session.resetCurrentProcessInstance(processId);
      session.unlock(userId, stateName);

      return viewProcessHandler.getDestination(function, session, request);
    }
  };

  /**
   * The viewProcess handler
   */
  private static FunctionHandler viewProcessHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      String processId = request.getParameter("processId");
      String force = request.getParameter("force");

      session.resetCurrentProcessInstance(processId);

      if ((force == null || !force.equals("true")) && session.hasPendingQuestions()) {
        return listQuestionsHandler.getDestination(function, session, request);
      }

      List<LockVO> locks = session.getLockingUsers();

      // check if an action must be resumed
      if (!locks.isEmpty()) {
        // Detects special case where user has killed his navigator while filling an action form
        HistoryStep savedStep = session.getSavedStep();
        if (savedStep != null) {
          return resumeActionHandler.getDestination(function, session, request);
        }
      }

      Form form = session.getPresentationForm();
      request.setAttribute("form", form);

      PagesContext context = getFormContext("presentation", "0", session, true);
      request.setAttribute("context", context);

      List<CurrentState> activeStates = session.getActiveStates();
      request.setAttribute("activeStates", activeStates);

      DataRecord data = session.getFolderRecord();
      request.setAttribute("data", data);

      String[] deleteAction = session.getDeleteAction();
      if (deleteAction != null && deleteAction.length > 0) {
        request.setAttribute("deleteAction", deleteAction);
      }

      if (CollectionUtil.isNotEmpty(locks)) {
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
  private static FunctionHandler searchResultHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      String type = request.getParameter("Type");
      String todoId = request.getParameter("Id");

      // Accept only links coming from todo details
      if (type == null || (!type.equals("TodoDetail") &&
          !type.equals("ProcessInstance"))) {
        return listProcessHandler.getDestination(function, session, request);
      }

      String processId = todoId;
      if (type.equals("TodoDetail")) {
        // from todo, todoId is in fact the externalId
        processId = session.getProcessInstanceIdFromExternalTodoId(todoId);

        String roleName = session.getRoleNameFromExternalTodoId(todoId);
        session.resetCurrentRole(roleName);
      } else {
        String roleName = request.getParameter("role");
        if (roleName != null) {
          session.resetCurrentRole(roleName);
        }
      }

      try {
        session.resetCurrentProcessInstance(processId);
      } catch (ProcessManagerException e) {
        return "/admin/jsp/documentNotFound.jsp";
      }

      if (!session.isUserAllowedOnActiveStates()) {
        // user is not allowed to act on or view current process instance
        // redirect him on home page
        return listProcessHandler.getDestination(function, session, request);
      }

      if (session.hasPendingQuestions()) {
        return listQuestionsHandler.getDestination(function, session, request);
      }

      return viewProcessHandler.getDestination(function, session, request);
    }
  };

  /**
   * The viewHistory handler
   */
  private static FunctionHandler viewHistoryHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      String processId = request.getParameter("processId");

      session.resetCurrentProcessInstance(processId);

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
  private static FunctionHandler createProcessHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {

      session.resetCurrentProcessInstance();

      Form form = session.getCreationForm();
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
  private static FunctionHandler saveCreationHandler = new SessionSafeFunctionHandler() {
    @Override
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      Form form = session.getCreationForm();
      PagesContext context = getFormContext("createForm", "0", session);
      DataRecord data = session.getEmptyCreationRecord();

      try {
        // Session Safe : reset Token Id
        resetTokenId(session, request);

        // use random id as temporary object id
        context.setObjectId(UUID.randomUUID().toString());
        List<String> attachmentIds = form.update(items, data, context, false);

        boolean isDraft = StringUtil.getBooleanValue(FileUploadUtil.getParameter(items, "isDraft"));
        boolean isFirstTimeSaved =
            StringUtil.getBooleanValue(FileUploadUtil.getParameter(items, "isFirstTimeSaved"));

        String instanceId = session.createProcessInstance(data, isDraft, isFirstTimeSaved);

        // launch update again to have a correct object id in wysiwyg
        context.setObjectId(instanceId);
        form.updateWysiwyg(items, data, context);

        // Attachment's foreignkey must be set with the just created instanceId
        for (String attachmentId : attachmentIds) {

          SimpleDocumentPK pk = new SimpleDocumentPK(attachmentId, session.getComponentId());
          SimpleDocument document = AttachmentServiceProvider.getAttachmentService().
              searchDocumentById(pk, null);
          document.setForeignId(instanceId);
          AttachmentServiceProvider.getAttachmentService()
              .lock(attachmentId, session.getUserId(), null);
          AttachmentServiceProvider.getAttachmentService().updateAttachment(document, false, false);
          AttachmentServiceProvider.getAttachmentService()
              .unlock(new UnlockContext(attachmentId, session.getUserId(), null));
        }

        request.setAttribute("From", "Creation");

        return listProcessHandler.getDestination(function, session, request);
      } catch (FormException e) {
        throw new ProcessManagerException("ProcessManagerRequestRouter",
            "processManager.ILL_CREATE_FORM", e);
      }
    }
  };

  /**
   * The resumeAction handler
   */
  private static FunctionHandler resumeActionHandler = new SessionSafeFunctionHandler() {
    @Override
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
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
      Form form = session.getActionForm(actionName);
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
  private static FunctionHandler editActionHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {

      // Set process instance
      String processId = request.getParameter("processId");
      session.resetCurrentProcessInstance(processId);

      // retrieve state name and action name
      String stateName = request.getParameter("state");
      String actionName = request.getParameter("action");

      // Get the associated form
      Form form = session.getActionForm(actionName);
      if (form == null) {
        // no form associated to this action, process action directly
        DataRecord data = session.getActionRecord(actionName);

        // lock the process instance
        session.lock(stateName);

        session.processAction(stateName, actionName, data, false, true);

        request.setAttribute("From", "Action");

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
        DataRecord data = session.getActionRecord(actionName);
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
  private static FunctionHandler saveActionHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {

      try {
        // Session Safe : reset Token Id
        resetTokenId(session, request);

        String stateName = FileUploadUtil.getParameter(items, "state");
        String actionName = FileUploadUtil.getParameter(items, "action");

        boolean isDraft = StringUtil.getBooleanValue(FileUploadUtil.getParameter(items, "isDraft"));
        boolean isFirstTimeSaved =
            StringUtil.getBooleanValue(FileUploadUtil.getParameter(items, "isFirstTimeSaved"));

        Form form = session.getActionForm(actionName);
        PagesContext context = getFormContext("actionForm", "0", session);
        DataRecord data = session.getActionRecord(actionName);

        if (form != null) {
          form.update(items, data, context);
        }
        session.processAction(stateName, actionName, data, isDraft, isFirstTimeSaved);

        request.setAttribute("From", "Action");

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
  private static FunctionHandler cancelActionHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      String stateName = request.getParameter("state");

      // special case : a cancel occured when resuming a precedent action (saved in draft)
      if (session.isResumingInstance()) {
        return listProcessHandler.getDestination(function, session, request);
      }

      // unlock the process instance
      session.unlock(stateName);

      return viewProcessHandler.getDestination(function, session, request);
    }
  };

  /**
   * The cancelResponse handler
   */
  private static FunctionHandler cancelResponseHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      String stateName = request.getParameter("state");

      // unlock the process instance
      session.unlock(stateName);

      return viewProcessHandler.getDestination(function, session, request);
    }
  };

  /**
   * The editQuestion handler
   */
  private static FunctionHandler editQuestionHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      String stepId = request.getParameter("stepId");

      request.setAttribute("stepId", stepId);
      request.setAttribute("step", session.getStep(stepId));

      String state = request.getParameter("state");
      request.setAttribute("state", state);

      // Get the question form
      Form form = session.getQuestionForm(false);
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
  private static FunctionHandler saveQuestionHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      try {
        // Session Safe : reset Token Id
        resetTokenId(session, request);

        String stepId = FileUploadUtil.getParameter(items, "stepId");
        String state = FileUploadUtil.getParameter(items, "state");

        Form form = session.getQuestionForm(false);
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
  private static FunctionHandler editResponseHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      String questionId = request.getParameter("questionId");

      request.setAttribute("question", session.getQuestion(questionId));

      // Get the question form (readonly)
      Form questionForm = session.getQuestionForm(true);
      request.setAttribute("questionForm", questionForm);

      // Get the response form (same as the question)
      Form responseForm = session.getQuestionForm(false);
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
  private static FunctionHandler saveResponseHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      try {
        // Session Safe : reset Token Id
        resetTokenId(session, request);

        String questionId = FileUploadUtil.getParameter(items, "questionId");

        Form responseForm = session.getQuestionForm(false);
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
  private static FunctionHandler manageReplacementsHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      setSharedAttributes(session, request);
      return "/processManager/jsp/replacements.jsp";
    }
  };
  /**
   * The editUserSetting handler
   */
  private static FunctionHandler editUserSettingsHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      // Get the user settings form
      Form form = session.getUserSettingsForm();
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
  private static FunctionHandler saveUserSettingsHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      Form form = session.getUserSettingsForm();
      PagesContext context = getFormContext("userSettingsForm", "0", session);
      DataRecord data = session.getEmptyUserSettingsRecord();

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
  private static FunctionHandler listQuestionsHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      String processId = request.getParameter("processId");

      session.resetCurrentProcessInstance(processId);

      // Get the question form (readonly)
      Form questionForm = session.getQuestionForm(true);
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
  private static void updateProcessFilter(ProcessManagerSessionController session,
      ProcessFilter filter, List<FileItem> items) throws ProcessManagerException {
    try {
      Form form = filter.getPresentationForm();
      PagesContext context = getFormContext("filter", "1", session);
      DataRecord data = filter.getCriteriaRecord();

      form.update(items, data, context);
      filter.setCriteriaRecord(data);

      boolean isEmpty = true;
      Map<String, String> values = data.getValues(null);
      for (String value : values.values()) {
        isEmpty = isEmpty && !StringUtil.isDefined(value);
      }
      filter.setCollapse(!isEmpty);
    } catch (Exception e) {
      throw new ProcessManagerException("ProcessManagerRequestRouter",
          "processManager.ILL_FILTER_FORM", e);
    }
  }

  /**
   * Send the filter parameters
   */
  private static void setProcessFilterAttributes(ProcessManagerSessionController session,
      HttpServletRequest request) throws ProcessManagerException {
    ProcessFilter filter = session.getCurrentFilter();
    request.setAttribute("collapse", filter.isCollapse());

    Form form = filter.getPresentationForm();
    request.setAttribute("form", form);

    PagesContext context = getFormContext("filter", "1", session);
    request.setAttribute("context", context);

    DataRecord data = filter.getCriteriaRecord();
    request.setAttribute("data", data);
  }

  private static FunctionHandler exportCSVHandler = new SessionSafeFunctionHandler() {
    protected String computeDestination(String function, ProcessManagerSessionController session,
        HttpServletRequest request, List<FileItem> items) throws ProcessManagerException {
      String csvFilename = session.exportListAsCSV();

      request.setAttribute("CSVFilename", csvFilename);
      if (StringUtil.isDefined(csvFilename)) {
        File file = new File(FileRepositoryManager.getTemporaryPath() + csvFilename);
        request.setAttribute("CSVFileSize", file.length());
        request.setAttribute("CSVFileURL", FileServerUtils.getUrlToTempDir(csvFilename));
      }

      return "/processManager/jsp/downloadCSV.jsp";
    }
  };

  /**
   * Inits the function handler
   */
  synchronized private void initHandlers() {
    if (handlerMap != null) {
      return;
    }
    handlerMap = new HashMap<>(35);
    handlerMap.put("Main", listProcessHandler);
    handlerMap.put("listProcess", listProcessHandler);
    handlerMap.put("listSomeProcess", listSomeProcessHandler);
    handlerMap.put("changeRole", changeRoleHandler);
    handlerMap.put("filterProcess", filterProcessHandler);
    handlerMap.put("clearFilter", clearFilterHandler);
    handlerMap.put("viewProcess", viewProcessHandler);
    handlerMap.put("removeLock", removeLockHandler);
    handlerMap.put("viewHistory", viewHistoryHandler);
    handlerMap.put("createProcess", createProcessHandler);
    handlerMap.put("saveCreation", saveCreationHandler);
    handlerMap.put("editAction", editActionHandler);
    handlerMap.put("saveAction", saveActionHandler);
    handlerMap.put("cancelAction", cancelActionHandler);
    handlerMap.put("editQuestion", editQuestionHandler);
    handlerMap.put("saveQuestion", saveQuestionHandler);
    handlerMap.put("editResponse", editResponseHandler);
    handlerMap.put("cancelResponse", cancelResponseHandler);
    handlerMap.put("saveResponse", saveResponseHandler);
    handlerMap.put("listQuestions", listQuestionsHandler);
    handlerMap.put("manageReplacements", manageReplacementsHandler);
    handlerMap.put("editUserSettings", editUserSettingsHandler);
    handlerMap.put("saveUserSettings", saveUserSettingsHandler);
    handlerMap.put("searchResult.jsp", searchResultHandler);
    handlerMap.put("searchResult", searchResultHandler);
    handlerMap.put("attachmentManager", attachmentManagerHandler);
    handlerMap.put("exportCSV", exportCSVHandler);
    handlerMap.put("adminRemoveProcess", adminRemoveProcessHandler);
    handlerMap.put("adminViewErrors", adminViewErrorsHandler);
    handlerMap.put("adminReAssign", adminReAssignHandler);
    handlerMap.put("adminDoReAssign", adminDoReAssignHandler);
  }

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
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   */
  @Override
  public ProcessManagerSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {

    try {
      return new ProcessManagerSessionController(mainSessionCtrl, componentContext);
    } catch (ProcessManagerException e) {
      return new ProcessManagerSessionController(mainSessionCtrl, componentContext, e);
    }
  }

  /**
   * Process the request and returns the response url.
   * @param function the user request name
   * @param sessionController the user request context
   * @param request the user request params
   */
  @Override
  public String getDestination(String function, ProcessManagerSessionController sessionController,
      HttpRequest request) {

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

    if (handlerMap == null) {
      initHandlers();
    }
    return handlerMap;
  }

  /**
   * Set attributes shared by all the processManager pages.
   */
  private static void setSharedAttributes(ProcessManagerSessionController session,
      HttpServletRequest request) {
    final String canCreate = session.getCreationRights() ? "1" : "0";
    final boolean isVersionControlled = session.isVersionControlled();
    final String isVersionControlledAsString = isVersionControlled ? "1" : "0";

    request.setAttribute("isVersionControlled", isVersionControlledAsString);
    request.setAttribute("language", session.getLanguage());
    request.setAttribute("roles", session.getUserRoleLabels());
    request.setAttribute("jsRoles", JSONCodec.encodeObject(o -> {
      Stream.of(session.getUserRoleLabels()).forEach(l -> o.putJSONObject(l.getName(),
          r -> r.put("label", l.getValue()).put("creationOne", l.isCreationOne())));
      return o;
    }));
    request.setAttribute("replacements", session.getUserReplacements());
    request.setAttribute("currentRole", session.getCurrentRole());
    request.setAttribute("currentReplacement", session.getCurrentReplacement());
    request.setAttribute("canCreate", canCreate);
    request.setAttribute("process", session.getCurrentProcessInstance());
    request.setAttribute("isActiveUser", session.isActiveUser());
    request.setAttribute("isAttachmentTabEnable", session.isAttachmentTabEnable());
    request.setAttribute("isHistoryTabEnable", session.isHistoryTabVisible());
    request.setAttribute("isProcessIdVisible", session.isProcessIdVisible());
    request.setAttribute("isPrintButtonEnabled", session.isPrintButtonEnabled());
    request.setAttribute("isSaveButtonEnabled", session.isSaveButtonEnabled());
    request.setAttribute("isReturnEnabled", session.isViewReturn());
    request.setAttribute("NbEntriesAboutQuestions", session.getNbEntriesAboutQuestions());
  }

  private static PagesContext getFormContext(String formName, String formIndex,
      ProcessManagerSessionController session) {
    return getFormContext(formName, formIndex, session, false);
  }

  private static PagesContext getFormContext(String formName, String formIndex,
      ProcessManagerSessionController session, boolean printTitle) {
    PagesContext pagesContext =
        new PagesContext(formName, formIndex, session.getLanguage(), printTitle,
            session.getComponentId(), session.getUserId());

    if (session.getCurrentProcessInstance() != null) {
      String currentInstanceId = session.getCurrentProcessInstance().getInstanceId();
      pagesContext.setObjectId(currentInstanceId);
    }
    // versioning used ?
    pagesContext.setVersioningUsed(session.isVersionControlled());
    return pagesContext;
  }
}
