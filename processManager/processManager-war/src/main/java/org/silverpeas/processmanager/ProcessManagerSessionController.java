/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
package org.silverpeas.processmanager;

import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.DataRecordUtil;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.field.DateField;
import org.silverpeas.core.contribution.content.form.field.MultipleUserField;
import org.silverpeas.core.contribution.content.form.field.UserField;
import org.silverpeas.core.contribution.content.form.form.HtmlForm;
import org.silverpeas.core.contribution.content.form.form.XmlForm;
import org.silverpeas.core.contribution.content.form.record.GenericFieldTemplate;
import org.silverpeas.core.contribution.content.form.record.GenericRecordTemplate;
import org.silverpeas.core.notification.message.MessageNotifier;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.workflow.api.UpdatableProcessInstanceManager;
import org.silverpeas.core.workflow.api.Workflow;
import org.silverpeas.core.workflow.api.WorkflowEngine;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.error.WorkflowError;
import org.silverpeas.core.workflow.api.event.GenericEvent;
import org.silverpeas.core.workflow.api.event.QuestionEvent;
import org.silverpeas.core.workflow.api.event.ResponseEvent;
import org.silverpeas.core.workflow.api.event.TaskDoneEvent;
import org.silverpeas.core.workflow.api.event.TaskSavedEvent;
import org.silverpeas.core.workflow.api.instance.Actor;
import org.silverpeas.core.workflow.api.instance.HistoryStep;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.api.instance.Question;
import org.silverpeas.core.workflow.api.instance.UpdatableProcessInstance;
import org.silverpeas.core.workflow.api.model.*;
import org.silverpeas.core.workflow.api.task.Task;
import org.silverpeas.core.workflow.api.user.User;
import org.silverpeas.core.workflow.api.user.UserInfo;
import org.silverpeas.core.workflow.api.user.UserSettings;
import org.silverpeas.core.workflow.engine.WorkflowHub;
import org.silverpeas.core.workflow.engine.datarecord.ProcessInstanceRowRecord;
import org.silverpeas.core.workflow.engine.instance.LockingUser;
import org.silverpeas.core.workflow.engine.model.ItemImpl;
import org.silverpeas.core.workflow.engine.user.UserSettingsService;
import org.silverpeas.processmanager.record.QuestionRecord;
import org.silverpeas.processmanager.record.QuestionTemplate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import static org.silverpeas.core.contribution.attachment.AttachmentService.VERSION_MODE;
import static org.silverpeas.core.workflow.util.WorkflowUtil.getItemByName;

/**
 * The ProcessManager Session controller
 */
public class ProcessManagerSessionController extends AbstractComponentSessionController {

  /**
   * Builds and init a new session controller
   * @param mainSessionCtrl
   * @param context
   * @throws ProcessManagerException
   */
  public ProcessManagerSessionController(MainSessionController mainSessionCtrl,
      ComponentContext context) throws ProcessManagerException {
    super(mainSessionCtrl, context, "org.silverpeas.processManager.multilang.processManagerBundle",
        "org.silverpeas.processManager.settings.processManagerIcons",
        "org.silverpeas.processManager.settings.processManagerSettings");
    // the peasId is the current component id.
    peasId = context.getCurrentComponentId();
    processModel = getProcessModel(peasId);


    // the current user is given by the main session controller.
    currentUser = getUser(mainSessionCtrl.getUserId());
    // the user roles are given by the context.
    userRoles = context.getCurrentProfile();
    if (userRoles == null || userRoles.length == 0) {
      throw new ProcessManagerException("ProcessManagerSessionController",
          "processManager.UNAUTHORIZED_USER");
    }
    currentRole = userRoles[0];

    // Reset the user rights for creation
    resetCreationRights();


    // Load user informations
    userSettings = UserSettingsService.get().get(mainSessionCtrl.getUserId(), peasId);
  }

  /**
   * Builds a ill session controller. Initialization is skipped and this session controller can
   * only * display the fatal exception. Used by the request router when a full session controller
   * can't be built.
   * @param mainSessionCtrl
   * @param context
   * @param fatal
   */
  public ProcessManagerSessionController(MainSessionController mainSessionCtrl,
      ComponentContext context, ProcessManagerException fatal) {
    super(mainSessionCtrl, context,
        "org.silverpeas.processManager.multilang.processManagerBundle",
        "org.silverpeas.processManager.settings.processManagerIcons",
        "org.silverpeas.processManager.settings.processManagerSettings");

    fatalException = fatal;
  }

  /**
   * Get the creation rights
   * @return true if user can do the "Creation" action
   */
  public boolean getCreationRights() {
    return creationRights;
  }

  /**
   * Compute the creation rights set creationRight to true if user can do the "Creation" action
   */
  public final void resetCreationRights() {
    creationRights = false;

    try {
      String[] roles = processModel.getCreationRoles();

      for (String role : roles) {
        if (role.equals(currentRole)) {
          creationRights = true;
        }
      }
    } catch (Exception e) {
      creationRights = false;
    }
  }

  /**
   * L'historique peut être filtré. Dans ce cas, les formulaires associés à chaque état sont
   * visibles uniquement si l'utilisateur courant était un working user ou un interested user.
   */
  public boolean filterHistory() {
    String parameterValue = getComponentParameterValue("filterHistory");
    return StringUtil.getBooleanValue(parameterValue);
  }

  public boolean isHistoryTabVisible() {
    String parameterValue = this.getComponentParameterValue("historyTabEnable");
    if (StringUtil.isDefined(parameterValue)) {
      return "yes".equalsIgnoreCase(parameterValue);
    }
    return true;
  }

  public boolean isCSVExportEnabled() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("exportCSV"));
  }

  /**
   * Print button on an action can be disabled. So it's return the visibility status of that
   * button.
   * @return true is print button is visible
   */
  public boolean isPrintButtonEnabled() {
    return StringUtil.getBooleanValue(getComponentParameterValue("printButtonEnabled"));
  }

  /**
   * Save button on an action can be disabled. So it's return the visibility status of that button.
   * @return true is save button is visible
   */
  public boolean isSaveButtonEnabled() {
    return StringUtil.getBooleanValue(getComponentParameterValue("saveButtonEnabled"));
  }

  /**
   * Returns the last fatal exception
   */
  public ProcessManagerException getFatalException() {
    return fatalException;
  }

  /**
   * Set current Process instance to null
   */
  public void resetCurrentProcessInstance() {
    this.setResumingInstance(false);
    currentProcessInstance = null;
  }

  /**
   * Updates the current process instance from the given id and returns the associated
   * ProcessInstance object. If the instanceId parameter is null, updates nothing and returns the
   * current ProcessInstance. Throws ProcessManagerException when the instanceId is unkwown and
   * when
   * the current user is not allowed to access the instance. Doesn't change the current process
   * instance when an error occures.
   */
  public ProcessInstance resetCurrentProcessInstance(String instanceId)
      throws ProcessManagerException {
    this.setResumingInstance(false);
    if (instanceId != null) {
      ProcessInstance instance;
      try {
        instance = Workflow.getProcessInstanceManager().getProcessInstance(instanceId);
      } catch (WorkflowException e) {
        throw new ProcessManagerException("ProcessManagerSessionControler",
            "processManager.UNKNOWN_PROCESS_INSTANCE", instanceId, e);
      }
      if (isAllowed(instance)) {
        currentProcessInstance = instance;
      } else {
        throw new ProcessManagerException("ProcessManagerSessionController",
            "processManager.NO_ACCESS_TO_PROCESS_INSTANCE");
      }
    }
    if (currentProcessInstance == null) {
      throw new ProcessManagerException("ProcessManagerSessionController",
          "processManager.NO_CURRENT_PROCESS_INSTANCE");
    }
    return currentProcessInstance;
  }

  /**
   * Get the current process instance
   */
  public ProcessInstance getCurrentProcessInstance() {
    return currentProcessInstance;
  }

  /**
   * Returns the current instance list rows template.
   */
  public RecordTemplate getProcessListHeaders() {
    if (currentListHeaders == null) {
      resetCurrentProcessListHeaders();
    }
    return currentListHeaders;
  }

  /**
   * Reset the current instance list rows template.
   */
  public void resetCurrentProcessListHeaders() {
    currentListHeaders = processModel.getRowTemplate(currentRole, getLanguage());
  }

  /**
   * Returns the current process instance list.
   */
  public List<DataRecord> getCurrentProcessList() throws ProcessManagerException {
    if (currentProcessList == null) {
      return resetCurrentProcessList(false);
    } else {
      return currentProcessList;
    }
  }

  /**
   * Updates the current process instance list with current filter and returns this list. Doesn't
   * change the current process instance when an error occurs.
   */
  public List<DataRecord> resetCurrentProcessList(boolean doAPause) throws ProcessManagerException {
    // Wait to display processes list up-to-date
    if (doAPause) {
      doAPause();
    }

    try {
      String[] groupIds = getOrganisationController().getAllGroupIdsOfUser(getUserId());
      List<ProcessInstance> processList = Workflow.getProcessInstanceManager()
          .getProcessInstances(peasId, currentUser, currentRole, getUserRoles(), groupIds);
      currentProcessList = getCurrentFilter().filter(processList, currentRole, getLanguage());
    } catch (WorkflowException e) {
      throw new ProcessManagerException("ProcessManagerSessionController",
          "processManager.GET_PROCESS_LIST_FAILED", peasId, e);
    }
    return currentProcessList;
  }

  /**
   * Get the role name of task referred by the todo with the given todo id
   */
  public String getRoleNameFromExternalTodoId(String externalTodoId)
      throws ProcessManagerException {
    try {
      return Workflow.getTaskManager().getRoleNameFromExternalTodoId(externalTodoId);
    } catch (WorkflowException e) {
      throw new ProcessManagerException("ProcessManagerSessionController",
          "processManager.GET_ROLENAME_FROM_TODO_FAILED", "externalTodoId : " + externalTodoId, e);
    }
  }

  /**
   * Get the process instance Id referred by the todo with the given todo id
   * @param externalTodoId external todo identifier
   * @return the process instance Id referred by the todo with the given todo id.
   * @throws ProcessManagerException
   */
  public String getProcessInstanceIdFromExternalTodoId(String externalTodoId)
      throws ProcessManagerException {
    try {
      return Workflow.getTaskManager().getProcessInstanceIdFromExternalTodoId(externalTodoId);
    } catch (WorkflowException e) {
      throw new ProcessManagerException("ProcessManagerSessionController",
          "processManager.GET_PROCESS_FROM_TODO_FAILED", "externalTodoId : " + externalTodoId, e);
    }
  }

  public boolean isUserAllowedOnActiveStates() {
    String[] states = currentProcessInstance.getActiveStates();
    if (states == null) {
      return false;
    }
    for (String state : states) {
      if (getActiveUsers(state).contains(getUserId())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Get the active states.
   * @return the active states.
   */
  public String[] getActiveStates() {
    String[] states = currentProcessInstance.getActiveStates();
    if (states == null) {
      return new String[0];
    }
    String[] stateLabels = new String[states.length];
    for (int i = 0; i < states.length; i++) {
      stateLabels[i] = getState(states[i]).getLabel(currentRole, getLanguage());
    }

    return stateLabels;
  }

  public String[] getActiveRoles() {
    String[] states = currentProcessInstance.getActiveStates();
    if (states == null) {
      return new String[0];
    }
    String[] roles = new String[states.length];
    for (int i = 0; i < states.length; i++) {
      try {
        State state = getState(states[i]);

        QualifiedUsers workingUsers = state.getWorkingUsers();

        RelatedUser[] relatedUsers = workingUsers.getRelatedUsers();
        StringBuilder role = new StringBuilder();
        if (relatedUsers != null) {
          for (RelatedUser relatedUser : relatedUsers) {
            if (role.length() > 0) {
              role.append(", ");
            }
            // Process participants
            Participant participant = relatedUser.getParticipant();
            String relation = relatedUser.getRelation();
            if (participant != null && relation == null) {
              role.append(participant.getLabel(currentRole, getLanguage()));
            } else if (participant != null && relation != null) {
              // userSettings of requester (not current user)
              String requesterId = getCreatorIdOfCurrentProcessInstance();
              if (requesterId != null) {
                UserInfo userInfo = getSettingsOfUser(requesterId).getUserInfo(relation);
                if (userInfo != null) {
                  role.append(getUserDetail(userInfo.getValue()).getDisplayedName());
                }
              }
            }

            // Process folder item
            Item item = relatedUser.getFolderItem();
            if (item != null) {
              Field field = currentProcessInstance.getField(item.getName());
              if (field instanceof UserField) {
                String userId = field.getStringValue();
                if (userId != null) {
                  UserDetail user = getUserDetail(userId);
                  if (user != null) {
                    role.append(user.getDisplayedName());
                  }
                }
              } else if (field instanceof MultipleUserField) {
                MultipleUserField multipleUserField = (MultipleUserField) field;
                String[] userIds = multipleUserField.getUserIds();
                for (String userId : userIds) {
                  if (userId != null) {
                    UserDetail user = getUserDetail(userId);
                    if (user != null) {
                      if (role.length() > 0) {
                        role.append(", ");
                      }
                      role.append(user.getDisplayedName());
                    }
                  }
                }
              }
            }
          }
        }

        UserInRole[] userInRoles = workingUsers.getUserInRoles();
        if (userInRoles != null) {
          for (UserInRole userInRole : userInRoles) {
            if (role.length() > 0) {
              role.append(", ");
            }
            role.append(processModel.getRole(userInRole.getRoleName())
                .getLabel(currentRole, getLanguage()));
          }
        }

        RelatedGroup[] relatedGroups = workingUsers.getRelatedGroups();
        if (relatedGroups != null) {
          for (RelatedGroup relatedGroup : relatedGroups) {
            if (relatedGroup != null) {
              if (role.length() > 0) {
                role.append(", ");
              }

              // Process folder item
              Item item = relatedGroup.getFolderItem();
              if (item != null) {
                String groupId = currentProcessInstance.getField(item.getName()).getStringValue();
                if (groupId != null) {
                  Group group = getOrganisationController().getGroup(groupId);
                  if (group != null) {
                    role.append(group.getName());
                  }
                }
              }
            }
          }
        }
        roles[i] = role.toString();
      } catch (WorkflowException ignored) {
        // ignore unknown state
        continue;
      }
    }
    return roles;
  }

  /**
   * Get the active user names
   */
  public boolean isActiveUser() {
    if (currentProcessInstance == null) {
      return false;
    }

    String[] states = currentProcessInstance.getActiveStates();
    if (states == null) {
      return false;
    }

    Actor[] users;
    for (final String state : states) {
      try {
        users = currentProcessInstance.getWorkingUsers(state);
        for (final Actor user : users) {
          if (getUserId().equals(user.getUser().getUserId())) {
            return true;
          }
        }
      } catch (WorkflowException ignored) {
        // ignore unknown state
        continue;
      }
    }

    return false;
  }

  private List<String> getActiveUsers(String stateName) {
    List<String> activeUsers = new ArrayList<String>();
    State state = getState(stateName);
    if (state != null) {
      activeUsers.addAll(getUsers(state.getWorkingUsers()));
      activeUsers.addAll(getUsers(state.getInterestedUsers()));
    }
    return activeUsers;
  }

  public List<String> getUsers(QualifiedUsers qualifiedUsers) {
    return getUsers(qualifiedUsers, false);
  }

  public List<String> getUsers(QualifiedUsers qualifiedUsers, boolean useCurrentRole) {
    List<String> users = new ArrayList<>();
    RelatedUser[] relatedUsers = qualifiedUsers.getRelatedUsers();
    RelatedUser relatedUser;
    List<String> roles = new ArrayList<>();
    for (final RelatedUser relatedUser1 : relatedUsers) {
      relatedUser = relatedUser1;
      // Process participants
      Participant participant = relatedUser.getParticipant();
      String relation = relatedUser.getRelation();
      if (participant != null && relation == null) {
        if (currentRole.equals(relatedUser.getRole())) {
          users.add(getUserId());
        }
      } else if (participant != null && relation != null) {
        String requesterId = getCreatorIdOfCurrentProcessInstance();
        if (requesterId != null) {
          UserInfo userInfo = getSettingsOfUser(requesterId).getUserInfo(relation);
          if (userInfo != null) {
            users.add(userInfo.getValue());
          }
        }
      }

      // Process folder item
      Item item = relatedUser.getFolderItem();
      if (item != null) {
        try {
          Field field = currentProcessInstance.getField(item.getName());
          String role = relatedUser.getRole();
          if (field instanceof UserField) {
            if (StringUtil.isDefined(role) && currentRole.equals(role)) {
              users.add(field.getStringValue());
            } else if (StringUtil.isNotDefined(role)) {
              users.add(field.getStringValue());
            }
          } else if (field instanceof MultipleUserField) {
            MultipleUserField multipleUserField = (MultipleUserField) field;
            if (StringUtil.isDefined(role) && currentRole.equals(role)) {
              users.addAll(Arrays.asList(multipleUserField.getUserIds()));
            } else if (StringUtil.isNotDefined(role)) {
              users.addAll(Arrays.asList(multipleUserField.getUserIds()));
            }
          }
        } catch (WorkflowException we) {
          // ignore it.
        }
      }
    }

    UserInRole[] userInRoles = qualifiedUsers.getUserInRoles();
    for (final UserInRole userInRole : userInRoles) {
      roles.add(userInRole.getRoleName());
    }

    if (useCurrentRole) {
      if (roles.contains(currentRole)) {
        roles.clear();
        roles.add(currentRole);
      } else {
        roles.clear();
      }
    }

    String[] userIds = getOrganisationController().getUsersIdsByRoleNames(getComponentId(), roles);
    for (final String userId : userIds) {
      users.add(userId);
    }

    // Process related groups
    RelatedGroup[] relatedGroups = qualifiedUsers.getRelatedGroups();
    if (relatedGroups != null) {
      for (RelatedGroup relatedGroup : relatedGroups) {
        if (relatedGroup != null) {
          // Process folder item
          Item item = relatedGroup.getFolderItem();
          if (item != null) {
            try {
              String groupId = currentProcessInstance.getField(item.getName()).getStringValue();
              UserDetail[] usersOfGroup = getOrganisationController().getAllUsersOfGroup(groupId);
              for (UserDetail userOfGroup : usersOfGroup) {
                users.add(userOfGroup.getId());
              }
            } catch (WorkflowException we) {// ignore it.
            }
          }
        }
      }
    }

    return users;
  }

  /**
   * Returns the workflow user having the given id.
   * @param userId the user identifier
   * @return the workflow user having the given id.
   * @throws ProcessManagerException
   */
  public User getUser(String userId) throws ProcessManagerException {
    try {
      return Workflow.getUserManager().getUser(userId);
    } catch (WorkflowException e) {
      throw new ProcessManagerException("ProcessManagerSessionControler",
          "processManager.UNKNOWN_USER", userId, e);
    }
  }

  /**
   * Returns the process model having the given id.
   * @param modelId the model identifier
   * @return the process model having the given id.
   * @throws ProcessManagerException
   */
  public final ProcessModel getProcessModel(String modelId) throws ProcessManagerException {
    try {
      return Workflow.getProcessModelManager().getProcessModel(modelId);
    } catch (WorkflowException e) {
      throw new ProcessManagerException("ProcessManagerSessionControler",
          "processManager.UNKNOWN_PROCESS_MODEL", modelId, e);
    }
  }

  /**
   * @return the current role name.
   */
  public String getCurrentRole() {
    return currentRole;
  }

  /**
   * Returns the current role name.
   * @param role a role
   * @throws ProcessManagerException
   */
  public void resetCurrentRole(String role) throws ProcessManagerException {
    if (role != null && role.length() > 0) {
      this.currentRole = role;
    }
    resetCreationRights();
    resetProcessFilter();
    resetCurrentProcessList(false);
    resetCurrentProcessListHeaders();
  }

  /**
   * Returns the user roles as a list of (name, label) pair.
   */
  public NamedValue[] getUserRoleLabels() {
    if (userRoleLabels == null) {
      String lang = getLanguage();
      Role[] roles = processModel.getRoles();

      List<NamedValue> labels = new ArrayList<>();
      NamedValue label;

      // quadratic search ! but it's ok : the list are about 3 or 4 length.
      for (final String userRole : userRoles) {
        if ("supervisor".equals(userRole)) {
          label = new NamedValue("supervisor", getString("processManager.supervisor"));
          labels.add(label);
        } else {
          for (final Role role : roles) {
            if (userRole.equals(role.getName())) {
              label = new NamedValue(userRole, role.getLabel(currentRole, lang));
              labels.add(label);
            }
          }
        }
      }

      Collections.sort(labels, NamedValue.ascendingValues);
      userRoleLabels = labels.toArray(new NamedValue[labels.size()]);
    }

    return userRoleLabels;
  }

  /**
   * Returns the form presenting the folder of the current process instance.
   */
  public Form getPresentationForm() throws ProcessManagerException {
    try {
      Form form = getPresentationForm("presentationForm");
      if (form != null) {
        form.setViewForm(true);
        return form;
      }

      XmlForm xmlForm = new XmlForm(
          processModel.getDataFolder().toRecordTemplate(currentRole, getLanguage(), true));
      xmlForm.setTitle(getString("processManager.folder"));
      xmlForm.setViewForm(true);
      return xmlForm;
    } catch (FormException | WorkflowException e) {
      throw new ProcessManagerException("SessionController",
          "processManager.PRESENTATION_FORM_UNAVAILABLE", e);
    }
  }

  public Item[] getFolderItems() throws ProcessManagerException {
    return processModel.getDataFolder().getItems();
  }

  /**
   * Returns the folder data of the current process instance.
   */
  public DataRecord getFolderRecord() throws ProcessManagerException {
    DataRecord data = null;
    try {
      if (currentProcessInstance != null) {
        data = currentProcessInstance.getFormRecord("presentationForm", currentRole, getLanguage());

        if (data == null) {
          data = currentProcessInstance.getFolder();
        }
      }
    } catch (WorkflowException e) {
      throw new ProcessManagerException("SessionController",
          "processManager.PRESENTATION_DATA_UNAVAILABLE", e);
    }

    if (data == null) {
      throw new ProcessManagerException("SessionController",
          "processManager.PRESENTATION_DATA_UNAVAILABLE");
    }

    return data;
  }

  /**
   * Returns the creation task.
   */
  public Task getCreationTask() throws ProcessManagerException {
    try {
      Task creationTask =
          Workflow.getTaskManager().getCreationTask(currentUser, currentRole, processModel);

      return creationTask;
    } catch (WorkflowException e) {
      throw new ProcessManagerException("SessionController",
          "processManager.CREATION_TASK_UNAVAILABLE", e);
    }
  }

  /**
   * Returns the form which starts a new instance.
   */
  public Form getCreationForm() throws ProcessManagerException {
    try {
      Action creation = processModel.getCreateAction(currentRole);
      return processModel.getPublicationForm(creation.getName(), currentRole, getLanguage());
    } catch (WorkflowException e) {
      throw new ProcessManagerException("SessionController", "processManager.NO_CREATION_FORM", e);
    }
  }

  /**
   * Returns the an empty creation record which will be filled with the creation form.
   */
  public DataRecord getEmptyCreationRecord() throws ProcessManagerException {
    try {
      Action creation = processModel.getCreateAction(currentRole);
      return processModel.getNewActionRecord(creation.getName(), currentRole, getLanguage(), null);
    } catch (WorkflowException e) {
      throw new ProcessManagerException("SessionController", "processManager.UNKNOWN_ACTION", e);
    }
  }

  /**
   * Returns the form to ask a question
   */
  public Form getQuestionForm(boolean readonly) throws ProcessManagerException {
    try {
      return new XmlForm(new QuestionTemplate(getLanguage(), readonly));
    } catch (FormException fe) {
      throw new ProcessManagerException("ProcessManagerSessionController",
          "processManager.GET_QUESTION_FORM_FAILED", fe);
    }
  }

  /**
   * Returns the an empty question record which will be filled with the question form.
   */
  public DataRecord getEmptyQuestionRecord() throws ProcessManagerException {
    return new QuestionRecord("");
  }

  /**
   * get the question with the given id
   * @param questionId question id
   * @return the found question
   */
  public Question getQuestion(String questionId) {
    Question[] questions = currentProcessInstance.getQuestions();
    Question foundQuestion = null;

    for (int i = 0; foundQuestion == null && i < questions.length; i++) {
      if (questions[i].getId().equals(questionId)) {
        foundQuestion = questions[i];
      }
    }

    return foundQuestion;
  }

  /**
   * Returns the an empty question record which will be filled with the question form.
   */
  public DataRecord getQuestionRecord(String questionId) throws ProcessManagerException {
    Question question = getQuestion(questionId);
    return new QuestionRecord(question.getQuestionText());
  }

  /**
   * Get assign template (for the re-affectations)
   */
  private GenericRecordTemplate getAssignTemplate() throws ProcessManagerException {
    try {
      String[] activeStates = currentProcessInstance.getActiveStates();
      GenericRecordTemplate rt = new GenericRecordTemplate();

      for (final String activeState : activeStates) {
        State state = getState(activeState);
        Actor[] actors = currentProcessInstance.getWorkingUsers(activeState);

        for (int j = 0; actors != null && j < actors.length; j++) {
          GenericFieldTemplate fieldTemplate = new GenericFieldTemplate(
              state.getName() + "_" + actors[j].getUserRoleName() + "_" + j, "user");
          fieldTemplate.addLabel(state.getLabel(currentRole, getLanguage()), getLanguage());
          fieldTemplate.setDisplayerName("user");
          fieldTemplate.setMandatory(true);
          fieldTemplate.addParameter("roles", actors[j].getUserRoleName());
          rt.addFieldTemplate(fieldTemplate);
        }
      }
      return rt;
    } catch (FormException | WorkflowException ex) {
      throw new ProcessManagerException("ProcessManagerSessionController",
          "processManager.GET_ASSIGN_TEMPLATE_FAILED", ex);
    }
  }

  /**
   * Get assign form (for the re-affectations)
   */
  public Form getAssignForm() throws ProcessManagerException {
    try {
      return new XmlForm(getAssignTemplate());
    } catch (FormException ex) {
      throw new ProcessManagerException("ProcessManagerSessionController",
          "processManager.GET_ASSIGN_FORM_FAILED", ex);
    }
  }

  /**
   * Get assign data (for the re-affectations)
   */
  public DataRecord getAssignRecord() {
    String[] activeStates = currentProcessInstance.getActiveStates();
    Actor[] actors = null;

    try {
      DataRecord data = getAssignTemplate().getEmptyRecord();
      for (int i = 0; activeStates != null && i < activeStates.length; i++) {
        actors = currentProcessInstance.getWorkingUsers(activeStates[i]);
        for (int j = 0; actors != null && j < actors.length; j++) {
          Field field =
              data.getField(activeStates[i] + "_" + actors[j].getUserRoleName() + "_" + j);
          String value = actors[j].getUser().getUserId();

          if (value != null) {
            field.setStringValue(value);
          }
        }
      }
      return data;
    } catch (WorkflowException | ProcessManagerException | FormException e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return null;
    }
  }

  /**
   * Get assign data (for the re-affectations)
   */
  public void reAssign(DataRecord data) throws ProcessManagerException {
    Actor[] oldUsers;
    List<Actor> oldActors = new ArrayList<>();
    List<Actor> newActors = new ArrayList<>();
    Map<String, String> changes = new HashMap<String, String>();

    try {
      WorkflowEngine wfEngine = Workflow.getWorkflowEngine();
      String[] activeStates = currentProcessInstance.getActiveStates();

      for (String activeState : activeStates) {
        // unassign old working users
        oldUsers = currentProcessInstance.getWorkingUsers(activeState);
        oldActors.addAll(Arrays.asList(oldUsers));
        RelatedUser[] relatedUsers =
            processModel.getState(activeState).getWorkingUsers().getRelatedUsers();

        // assign new working users
        for (int j = 0; j < oldUsers.length; j++) {
          String roleName = oldUsers[j].getUserRoleName();
          Field field = data.getField(activeState + "_" + roleName + "_" + j);
          String userId = field.getStringValue();
          User user = Workflow.getUserManager().getUser(userId);
          Actor newActor = Workflow.getProcessInstanceManager().createActor(
              user, roleName, oldUsers[j].getState());
          newActors.add(newActor);

          // detect folderItem where old userIds are stored
          for (RelatedUser relatedUser : relatedUsers) {
            Item item = relatedUser.getFolderItem();
            if (item != null && roleName.equals(relatedUser.getRole())) {
              addAnyChanges(userId, item, changes);
            }
          }
        }
      }

      wfEngine.reAssignActors((UpdatableProcessInstance) currentProcessInstance,
          oldActors.toArray(new Actor[oldActors.size()]),
          newActors.toArray(new Actor[newActors.size()]), currentUser);

      // update folder
      updateFolder(changes);
    } catch (WorkflowException | FormException we) {
      throw new ProcessManagerException("ProcessManagerSessionController",
          "processManager.RE_ASSIGN_FAILED", we);
    }
  }

  private void addAnyChanges(final String userId, final Item item,
      final Map<String, String> changes) {
    try {
      Field folderField = currentProcessInstance.getField(item.getName());
      if (folderField instanceof UserField) {
        changes.put(item.getName(), userId);
      }
    } catch (WorkflowException we) {
      // ignore it.
    }
  }

  /**
   * Create a new process instance with the filled form.
   * @param data the form data
   * @param isDraft true if form has just been saved as draft
   * @param firstTimeSaved true if form is saved as draft for the first time
   */
  public String createProcessInstance(DataRecord data, boolean isDraft, boolean firstTimeSaved)
      throws ProcessManagerException {
    try {
      Action creation = processModel.getCreateAction(currentRole);
      GenericEvent event =
          (isDraft) ? getCreationTask().buildTaskSavedEvent(creation.getName(), data) :
              getCreationTask().buildTaskDoneEvent(creation.getName(), data);

      // Is a validate or a "save as draft" action ?
      if (isDraft) {
        TaskSavedEvent tse = (TaskSavedEvent) event;
        tse.setFirstTimeSaved(firstTimeSaved);
        Workflow.getWorkflowEngine().process((TaskSavedEvent) event);
      } else {
        Workflow.getWorkflowEngine().process((TaskDoneEvent) event);
        feedbackUser("processManager.createProcess.feedback");
      }

      return event.getProcessInstance().getInstanceId();
    } catch (WorkflowException e) {
      throw new ProcessManagerException("SessionController",
          "processManager.CREATION_PROCESSING_FAILED", e);
    }
  }

  private void feedbackUser(String key) {
    MessageNotifier.addSuccess(getString(key)).setDisplayLiveTime(10000);
  }

  private void doAPause() {
    int duration = getSettings().getInteger("refresh.delay", 1000);
    if (duration > 0) {
      try {
        Thread.sleep(duration);
      } catch (InterruptedException ie) {
        SilverLogger.getLogger(this).error(ie.getLocalizedMessage(), ie);
      }
    }
  }

  /**
   * Get all tasks assigned for current user on current process instance
   */
  public Task[] getTasks() throws ProcessManagerException {
    try {
      return Workflow.getTaskManager().getTasks(currentUser, currentRole, currentProcessInstance);
    } catch (WorkflowException e) {
      throw new ProcessManagerException("SessionController", "processManager.GET_TASKS_FAILED", e);
    }
  }

  /**
   * Search for an hypothetic action of kind "delete", allowed for the current user with the given
   * role
   * @return an array of 2 String { action.name, state.name }, null if no action found
   */
  public String[] getDeleteAction() {
    Task[] tasks = null;
    State state = null;
    Action[] actions = null;

    try {
      tasks = getTasks();
      for (int i = 0; tasks != null && i < tasks.length; i++) {
        state = tasks[i].getState();
        actions = state.getAllowedActions();
        for (int j = 0; actions != null && j < actions.length; j++) {
          if ("delete".equals(actions[j].getKind())) {
            String[] result = new String[3];
            result[0] = actions[j].getName();
            result[1] = state.getName();
            result[2] = actions[j].getLabel(currentRole, getLanguage());
            return result;
          }
        }
      }

      return null;
    } catch (ProcessManagerException e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return null;
    }
  }

  /**
   * Returns the named task.
   */
  public Task getTask(String stateName) throws ProcessManagerException {
    Task[] tasks = getTasks();
    for (final Task task : tasks) {
      if (task.getState().getName().equals(stateName)) {
        return task;
      }
    }
    return null;
  }

  /**
   * Returns the named form (Read only). Throws an exception if the form is unknown.
   */
  public Form getPresentationForm(String name) throws ProcessManagerException {
    try {
      return processModel.getPresentationForm(name, currentRole, getLanguage());
    } catch (WorkflowException e) {
      throw new ProcessManagerException("SessionController", "processManager.UNKNOWN_ACTION", e);
    }
  }

  /**
   * Returns the form associated to the named action. Returns null if this action has no form.
   * Throws an exception if the action is unknown.
   */
  public Form getActionForm(String stateName, String actionName) throws ProcessManagerException {
    try {
      return processModel.getPublicationForm(actionName, currentRole, getLanguage());
    } catch (WorkflowException e) {
      throw new ProcessManagerException("SessionController", "processManager.UNKNOWN_ACTION", e);
    }
  }

  /**
   * Returns a new DataRecord filled whith the folder data and which will be be completed by the
   * action form.
   */
  public DataRecord getActionRecord(String stateName, String actionName)
      throws ProcessManagerException {
    try {
      return currentProcessInstance.getNewActionRecord(actionName);
    } catch (WorkflowException e) {
      throw new ProcessManagerException("SessionController", "processManager.UNKNOWN_ACTION", e);
    }
  }

  /**
   * Create a new history step instance with the filled form.
   */
  public void processAction(String stateName, String actionName, DataRecord data, boolean isDraft,
      boolean isFirstTimeSaved) throws ProcessManagerException {
    try {
      Task task;

      if (StringUtil.isDefined(stateName)) {
        task = getTask(stateName);
      } else {
        task = getCreationTask();
        task.setProcessInstance(currentProcessInstance);
      }

      // Is a validate or a "save as draft" action ?
      if (isDraft) {
        TaskSavedEvent tse = task.buildTaskSavedEvent(actionName, data);
        tse.setFirstTimeSaved(isFirstTimeSaved);
        Workflow.getWorkflowEngine().process(tse);
      } else {
        TaskDoneEvent event = task.buildTaskDoneEvent(actionName, data);
        event.setResumingAction(this.isResumingInstance);
        Workflow.getWorkflowEngine().process(event);
        feedbackUser("processManager.action.feedback");
      }
    } catch (WorkflowException e) {
      throw new ProcessManagerException("SessionController",
          "processManager.CREATION_PROCESSING_FAILED", e);
    }
  }

  /**
   * Returns the required step
   */
  public HistoryStep getStep(String stepId) {
    HistoryStep[] steps = currentProcessInstance.getHistorySteps();
    for (final HistoryStep step : steps) {
      if (step.getId().equals(stepId)) {
        return step;
      }
    }
    return null;
  }

  /**
   * Send the question as a QuestionEvent to the workflowEngine.
   */
  public void processQuestion(String stepId, String state, DataRecord data)
      throws ProcessManagerException {
    try {
      Task task = getTask(state);
      QuestionEvent event = task.buildQuestionEvent(stepId, data);
      Workflow.getWorkflowEngine().process(event);
    } catch (WorkflowException e) {
      throw new ProcessManagerException("SessionController",
          "processManager.CREATION_PROCESSING_FAILED", e);
    }
  }

  /**
   * Send the answer as a ResponseEvent to the workflowEngine.
   */
  public void processResponse(String questionId, DataRecord data) throws ProcessManagerException {
    try {
      Question question = getQuestion(questionId);
      Task task = getTask(question.getTargetState().getName());
      ResponseEvent event = task.buildResponseEvent(questionId, data);
      Workflow.getWorkflowEngine().process(event);
    } catch (WorkflowException e) {
      throw new ProcessManagerException("SessionController",
          "processManager.CREATION_PROCESSING_FAILED", e);
    }
  }

  /**
   * Get locking users list
   * @throws ProcessManagerException
   */
  public List<LockVO> getLockingUsers() throws ProcessManagerException {
    this.currentUserIsLockingUser = false;

    try {
      List<LockVO> lockingUsers = new ArrayList<>();
      String[] states = currentProcessInstance.getActiveStates();
      if (states != null) {
        for (String stateName : states) {
          LockingUser lockingUser = currentProcessInstance.getLockingUser(stateName);
          if (lockingUser != null) {
            User user = WorkflowHub.getUserManager().getUser(lockingUser.getUserId());
            boolean isDraftPending =
                (currentProcessInstance.getSavedStep(lockingUser.getUserId()) != null);
            lockingUsers.add(new LockVO(user, lockingUser.getLockDate(), lockingUser.getState(),
                !isDraftPending));
            if (lockingUser.getUserId().equals(getUserId())) {
              this.currentUserIsLockingUser = true;
            }
          }
        }
      }

      // special case : instance saved in creation step
      LockingUser lockingUser = currentProcessInstance.getLockingUser("");
      if (lockingUser != null) {
        User user = WorkflowHub.getUserManager().getUser(lockingUser.getUserId());
        boolean isDraftPending =
            (currentProcessInstance.getSavedStep(lockingUser.getUserId()) != null);
        lockingUsers.add(
            new LockVO(user, lockingUser.getLockDate(), lockingUser.getState(), !isDraftPending));
        if (lockingUser.getUserId().equals(getUserId())) {
          this.currentUserIsLockingUser = true;
        }
      }

      return lockingUsers;
    } catch (WorkflowException e) {
      throw new ProcessManagerException("SessionController",
          "processManager.GET_LOCKING_USERS_FAILED", e);
    }

  }

  /**
   * Lock the current instance for current user and given state
   * @param stateName state name
   */
  public void lock(String stateName) throws ProcessManagerException {
    try {
      State state = processModel.getState(stateName);
      ((UpdatableProcessInstanceManager) Workflow.getProcessInstanceManager())
          .lock(currentProcessInstance, state, currentUser);
    } catch (WorkflowException e) {
      throw new ProcessManagerException("SessionController", "processManager.LOCK_FAILED", e);
    }
  }

  /**
   * Un-Lock the current instance for given user and given state
   * @param userId user Id
   * @param stateName state name
   */
  public void unlock(String userId, String stateName) throws ProcessManagerException {
    try {
      State state = processModel.getState(stateName);
      User user = WorkflowHub.getUserManager().getUser(userId);
      ((UpdatableProcessInstanceManager) Workflow.getProcessInstanceManager())
          .unlock(currentProcessInstance, state, user);
    } catch (WorkflowException e) {
      throw new ProcessManagerException("SessionController", "processManager.LOCK_FAILED", e);
    }
  }

  /**
   * Un-Lock the current instance for current user and given state
   * @param stateName state name
   */
  public void unlock(String stateName) throws ProcessManagerException {
    try {
      State state = processModel.getState(stateName);
      ((UpdatableProcessInstanceManager) Workflow.getProcessInstanceManager())
          .unlock(currentProcessInstance, state, currentUser);
    } catch (WorkflowException e) {
      throw new ProcessManagerException("SessionController", "processManager.LOCK_FAILED", e);
    }
  }

  public List<StepVO> getSteps(String strEnlightedStep) {
    List<StepVO> stepsVO = new ArrayList<StepVO>();

    // get step from last recent to older
    HistoryStep[] steps = getSortedHistorySteps(false);

    strEnlightedStep = (strEnlightedStep == null) ? steps[0].getId() : strEnlightedStep;
    for (HistoryStep step : steps) {

      StepVO stepVO = new StepVO();

      stepVO.setStepId(step.getId());

      // Activity
      String activity = "";
      if (step.getResolvedState() != null) {
        State resolvedState = processModel.getState(step.getResolvedState());
        if (resolvedState != null) {
          activity = resolvedState.getLabel(currentRole, getLanguage());
        }
      }
      stepVO.setActivity(activity);

      // Actor Full Name
      stepVO.setActorFullName(getStepActor(step));

      // Action name
      stepVO.setActionName(getStepAction(step));

      // Step date
      stepVO.setStepDate(DateUtil.getOutputDate(step.getActionDate(), getLanguage()));

      // visibility
      stepVO.setVisible(isStepVisible(step));

      // Step data
      if ("all".equalsIgnoreCase(strEnlightedStep) || strEnlightedStep.equals(step.getId())) {
        try {
          // Form
          Form form = getStepForm(step);

          // Context
          PagesContext context =
              new PagesContext("dummy", "0", getLanguage(), false, getComponentId(), getUserId());
          context.setVersioningUsed(isVersionControlled());
          if (currentProcessInstance != null) {
            context.setObjectId("Step" + step.getId());
          }

          // DataRecord
          DataRecord data = getStepRecord(step);

          HistoryStepContent stepContent = new HistoryStepContent(form, context, data);
          stepVO.setContent(stepContent);
        } catch (ProcessManagerException e) {
          SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
        }
      }

      stepsVO.add(stepVO);
    }

    return stepsVO;
  }

  public HistoryStep[] getSortedHistorySteps(final boolean ascending) {
    HistoryStep[] steps = currentProcessInstance.getHistorySteps();

    // Invert history to get newest history at the beginning
    Arrays.sort(steps, new Comparator<HistoryStep>() {

      public int compare(HistoryStep o1, HistoryStep o2) {
        if (ascending) {
          return o1.getId().compareTo(o2.getId());
        } else {
          return o2.getId().compareTo(o1.getId());
        }
      }
    });

    return steps;
  }

  public boolean isStepVisible(HistoryStep step) {
    boolean visible = true;
    String stateName = null;
    if (filterHistory()) {
      visible = false;
      stateName = step.getResolvedState();
      if (stateName != null) {
        if (getActiveUsers(stateName).contains(getUserId())) {
          visible = true;
        }
      } else {
        // action kind=create
        try {
          Action createAction = processModel.getCreateAction(currentRole);
          QualifiedUsers qualifiedUsers = createAction.getAllowedUsers();
          if (getUsers(qualifiedUsers).contains(getUserId())) {
            visible = true;
          }
        } catch (WorkflowException we) {
          // no action of kind create
          visible = true;
        }
      }
    }
    return visible;
  }

  /**
   * Get the list of actors in History Step of current process instance
   * @return an array of string containing actors full name
   */
  public String getStepActor(HistoryStep step) {
    String actorFullName;
    try {
      actorFullName = step.getUser().getFullName();
    } catch (WorkflowException we) {
      actorFullName = "##";
    }
    return actorFullName;
  }

  /**
   * Get the list of actions in History Step of current process instance
   * @return an array of string containing actions names
   */
  public String getStepAction(HistoryStep step) {
    Action action = null;
    String actionName = null;

    try {
      if ("#question#".equals(step.getAction())) {
        actionName = getString("processManager.question");
      } else if ("#response#".equals(step.getAction())) {
        actionName = getString("processManager.response");
      } else if ("#reAssign#".equals(step.getAction())) {
        actionName = getString("processManager.reAffectation");
      } else {
        action = processModel.getAction(step.getAction());
        actionName = action.getLabel(currentRole, getLanguage());
      }
    } catch (WorkflowException we) {
      actionName = "##";
    }

    return actionName;
  }

  /**
   * Returns the form used to display the i-th step. Returns null if the step is unkwown.
   * @throws ProcessManagerException
   */
  public Form getStepForm(HistoryStep step) throws ProcessManagerException {
    try {
      if ("#question#".equals(step.getAction()) || "#response#".equals(step.getAction())) {
        return getQuestionForm(true);
      } else {
        return processModel.getPresentationForm(step.getAction(), currentRole, getLanguage());
      }
    } catch (WorkflowException e) {

      return null;
    }
  }

  /**
   * Returns the data filled during the given step. Returns null if the step is unkwown.
   */
  private DataRecord getStepRecord(HistoryStep step) {

    try {
      if ("#question#".equals(step.getAction())) {
        Question question = null;
        Question[] questions = currentProcessInstance.getQuestions();
        for (int j = 0; question == null && j < questions.length; j++) {
          if (step.getResolvedState().equals(questions[j].getFromState().getName())) {
            if (((questions[j].getQuestionDate().getTime() - step.getActionDate().getTime()) <
                30000) &&
                ((questions[j].getQuestionDate().getTime() - step.getActionDate().getTime()) > 0)) {
              question = questions[j];
            }
          }
        }

        if (question == null) {
          return null;
        } else {
          return new QuestionRecord(question.getQuestionText());
        }
      } else if ("#response#".equals(step.getAction())) {
        Question question = null;
        Question[] questions = currentProcessInstance.getQuestions();
        for (int j = 0; question == null && j < questions.length; j++) {
          if (step.getResolvedState().equals(questions[j].getTargetState().getName())) {
            if (((questions[j].getResponseDate().getTime() - step.getActionDate().getTime()) <
                30000) &&
                ((questions[j].getResponseDate().getTime() - step.getActionDate().getTime()) > 0)) {
              question = questions[j];
            }
          }
        }

        if (question == null) {
          return null;
        } else {
          return new QuestionRecord(question.getResponseText());
        }
      } else {
        return step.getActionRecord();
      }
    } catch (WorkflowException e) {

      return null;
    }
  }

  /**
   * Get step saved by given user id.
   * @throws ProcessManagerException
   */
  public HistoryStep getSavedStep() throws ProcessManagerException {
    try {
      return currentProcessInstance.getSavedStep(getUserId());
    } catch (WorkflowException e) {
      throw new ProcessManagerException("ProcessManagerSessionController",
          "processManager.GET_SAVED_STEP_FAILED", e);
    }
  }

  /**
   * Get step data record saved.
   * @throws ProcessManagerException
   * @throws ProcessManagerException
   */
  public DataRecord getSavedStepRecord(HistoryStep savedStep) throws ProcessManagerException {
    try {
      return savedStep.getActionRecord();
    } catch (WorkflowException e) {
      throw new ProcessManagerException("ProcessManagerSessionController",
          "processManager.GET_SAVED_STEP_DATARECORD_FAILED", e);
    }
  }

  /**
   * Returns the form defined to print
   */
  public Form getPrintForm() throws ProcessManagerException {
    try {
      org.silverpeas.core.workflow.api.model.Form form = processModel.getForm("printForm");
      if (form == null) {
        throw new ProcessManagerException("ProcessManagerSessionController",
            "processManager.NO_PRINTFORM_DEFINED_IN_MODEL");
      } else {
        HtmlForm htmlForm = new HtmlForm(
            processModel.getDataFolder().toRecordTemplate(currentRole, getLanguage(), true));

        htmlForm.setFileName(form.getHTMLFileName());
        return htmlForm;
      }
    } catch (Exception e) {
      throw new ProcessManagerException("ProcessManagerSessionController",
          "processManager.GET_PRINT_FORM_FAILED", e);
    }
  }

  /**
   * Returns the data of instance
   */
  public DataRecord getPrintRecord() throws ProcessManagerException {
    try {
      return currentProcessInstance.getAllDataRecord(currentRole, getLanguage());
    } catch (WorkflowException we) {
      throw new ProcessManagerException("ProcessManagerSessionController",
          "processManager.GET_PRINT_RECORD_FAILED", we);
    }
  }

  /**
   * Get the label of given action
   * @param actionName action name
   * @return action label
   */
  public String getActionLabel(String actionName) {
    try {
      Action action = processModel.getAction(actionName);
      if (action == null) {
        return actionName;
      } else {
        return action.getLabel(currentRole, getLanguage());
      }
    } catch (WorkflowException we) {
      return actionName;
    }
  }

  /**
   * Get the state with the given name
   * @param stateName state name
   * @return State object
   */
  public State getState(String stateName) {
    return processModel.getState(stateName);
  }

  /**
   * Get the named action
   * @param actionName action name
   * @return action
   */
  public Action getAction(String actionName) {
    try {
      return processModel.getAction(actionName);
    } catch (WorkflowException we) {
      return null;
    }
  }

  /**
   * Tests if there is some question for current user in current processInstance
   * @return true if there is one or more question
   */
  public boolean hasPendingQuestions() {
    try {
      Task[] tasks = getTasks();
      if (tasks == null || tasks.length == 0) {
        return false;
      }

      for (final Task task : tasks) {
        if (task.getPendingQuestions() != null && task.getPendingQuestions().length > 0) {
          return true;
        }
      }

      return false;
    } catch (ProcessManagerException pme) {
      return false;
    }
  }

  private String getCreatorIdOfCurrentProcessInstance() {
    HistoryStep[] steps = currentProcessInstance.getHistorySteps();
    if (steps.length > 0) {
      HistoryStep initialStep = steps[0];
      try {
        return initialStep.getUser().getUserId();
      } catch (WorkflowException e) {
        SilverLogger.getLogger(this)
            .error("can not get the creator of the current process instance", e);
      }
    }
    return null;
  }

  private UserSettings getSettingsOfUser(String userId) {
    return UserSettingsService.get().get(userId, peasId);
  }

  /**
   * Returns the form to fill user settings
   */
  public Form getUserSettingsForm() throws ProcessManagerException {
    try {
      DataFolder userInfos = processModel.getUserInfos();
      if (userInfos == null) {
        return null;
      }

      return new XmlForm(userInfos.toRecordTemplate(currentRole, getLanguage(), false));
    } catch (FormException | WorkflowException we) {
      throw new ProcessManagerException("ProcessManagerSessionController",
          "processManager.GET_USERSETTINGS_FORM_FAILED", we);
    }
  }

  /**
   * Returns the an empty date record which will be filled with the user settings form.
   */
  public DataRecord getEmptyUserSettingsRecord() throws ProcessManagerException {
    try {
      return processModel.getNewUserInfosRecord(currentRole, getLanguage());
    } catch (WorkflowException we) {
      throw new ProcessManagerException("ProcessManagerSessionController",
          "processManager.GET_EMPTY_USERSETTINGS_RECORD_FAILED", we);
    }
  }

  /**
   * Returns the an empty data record which will be filled with the user settings form.
   */
  public DataRecord getUserSettingsRecord() throws ProcessManagerException {
    try {
      DataRecord data = getEmptyUserSettingsRecord();
      userSettings.load(data,
          processModel.getUserInfos().toRecordTemplate(currentRole, getLanguage(), false));

      return data;
    } catch (WorkflowException we) {
      throw new ProcessManagerException("ProcessManagerSessionController",
          "processManager.GET_EMPTY_USERSETTINGS_RECORD_FAILED", we);
    }
  }

  /**
   * Save the user settings which have been filled with the user settings form.
   */
  public void saveUserSettings(DataRecord data) throws ProcessManagerException {
    try {
      RecordTemplate userSettingsTemplate =
          processModel.getUserInfos().toRecordTemplate(currentRole, getLanguage(), false);
      UserSettingsService.get().update(userSettings, data, userSettingsTemplate);
    } catch (WorkflowException we) {
      throw new ProcessManagerException("ProcessManagerSessionController",
          "processManager.SAVE_USERSETTINGS_FAILED", we);
    }
  }

  /**
   * Returns the current ProcessFilter.
   */
  public ProcessFilter getCurrentFilter() throws ProcessManagerException {
    if (currentProcessFilter == null) {
      currentProcessFilter = new ProcessFilter(processModel, currentRole, getLanguage());
    }
    return currentProcessFilter;
  }

  /**
   * Reset the current ProcessFilter.
   */
  public void resetProcessFilter() throws ProcessManagerException {
    ProcessFilter oldFilter = currentProcessFilter;
    currentProcessFilter = new ProcessFilter(processModel, currentRole, getLanguage());

    if (oldFilter != null) {
      currentProcessFilter.setCollapse(oldFilter.getCollapse());
      currentProcessFilter.copySharedCriteria(oldFilter);
    }
  }

  /**
   * Remove process instance with given id
   */
  public void removeProcess(String processId) throws ProcessManagerException {
    try {
      if (checkUserIsInstanceSupervisor(processId)) {
        UpdatableProcessInstanceManager pim =
            (UpdatableProcessInstanceManager) Workflow.getProcessInstanceManager();
        pim.removeProcessInstance(processId);
      } else {
        SilverLogger.getLogger(this)
            .warn("Security alert from userId {0} on processId {1}", getUserId(), processId);
      }
    } catch (WorkflowException we) {
      throw new ProcessManagerException("ProcessManagerSessionController",
          "processManager.REMOVE_PROCESS_FAILED", we);
    }
  }

  private boolean checkUserIsInstanceSupervisor(String processId) throws ProcessManagerException {
    if ("supervisor".equalsIgnoreCase(getCurrentRole())) {
      try {
        ProcessInstance processInstance =
            Workflow.getProcessInstanceManager().getProcessInstance(processId);
        List<User> users = processInstance.getUsersInRole("supervisor");
        if (users != null && !users.isEmpty()) {
          for (User user : users) {
            if (user.getUserId().equals(getUserId())) {
              return true;
            }
          }
        }
      } catch (WorkflowException e) {
        throw new ProcessManagerException("ProcessManagerSessionController",
            "checkUserIsInstanceSupervisor", e);
      }

    }
    return false;
  }

  /**
   * Get all the errors occured while processing the current process instance
   */
  public WorkflowError[] getProcessInstanceErrors(String processId) throws ProcessManagerException {
    try {
      return Workflow.getErrorManager().getErrorsOfInstance(processId);
    } catch (WorkflowException we) {
      throw new ProcessManagerException("ProcessManagerSessionController",
          "processManager.GET_PROCESS_ERRORS_FAILED", we);
    }
  }

  /**
   * Returns true if : the instance is built from the process model of this session. the user is
   * allowed to access to this instance with his current role.
   */
  private boolean isAllowed(ProcessInstance instance) {
    return true; // xoxox
  }

  /**
   * Returns true if : the user settings are correct
   */
  public boolean isUserSettingsOK() {
    return userSettings != null && userSettings.isValid();
  }

  /**
   * Returns true if : the model has any user settings.
   */
  public boolean hasUserSettings() {
    return processModel.getUserInfos() != null;
  }

  public boolean isVersionControlled() {
    return StringUtil.getBooleanValue(getComponentParameterValue(VERSION_MODE));
  }

  public boolean isAttachmentTabEnable() {
    String param = this.getComponentParameterValue("attachmentTabEnable");
    return param == null || (!("").equals(param) && !("no").equalsIgnoreCase(param));
  }

  public boolean isProcessIdVisible() {
    String param = this.getComponentParameterValue("processIdVisible");
    return "yes".equalsIgnoreCase(param);
  }

  public boolean isViewReturn() {
    // Retrieve global parameter
    boolean hideReturnGlobal = getSettings().getBoolean("hideReturn", false);
    boolean viewReturn = !hideReturnGlobal;

    if (viewReturn) {
      // Must see "back" buttons if instance don't hide it
      boolean hideReturnLocal =
          StringUtil.getBooleanValue(getComponentParameterValue("hideReturn"));
      viewReturn = !hideReturnLocal;
    }
    return viewReturn;
  }

  public String exportListAsCSV() throws ProcessManagerException {
    String fieldsToExport = getComponentParameterValue("fieldsToExport");
    List<StringBuilder> csvRows;
    if (StringUtil.isDefined(fieldsToExport)) {
      csvRows = exportDefinedItemsAsCSV();
    } else {
      csvRows = exportAllFolderAsCSV();
    }
    return writeCSVFile(csvRows);
  }

  private List<StringBuilder> exportAllFolderAsCSV() throws ProcessManagerException {
    try {
      List<DataRecord> processList = getCurrentProcessList();
      Item[] items = getFolderItems();
      RecordTemplate listHeaders = getProcessListHeaders();
      FieldTemplate[] headers = listHeaders.getFieldTemplates();

      List<String> csvCols = getCSVCols();

      ProcessInstanceRowRecord instance;
      StringBuilder csvRow = new StringBuilder();
      List<StringBuilder> csvRows = new ArrayList<>();
      boolean isProcessIdVisible = isProcessIdVisible();

      if (isProcessIdVisible) {
        addCSVValue(csvRow, "#");
      }

      addCSVValue(csvRow, "<>");

      String col;
      ItemImpl item;
      for (int i = 0; i < csvCols.size(); i++) {
        if (i == 0 || i == 1) {
          addCSVValue(csvRow, headers[i].getLabel(getLanguage()));
        } else {
          col = csvCols.get(i);
          item = (ItemImpl) getItemByName(items, col);
          addCSVValue(csvRow, item.getLabel(getCurrentRole(), getLanguage()));
        }
      }
      csvRows.add(csvRow);

      for (final DataRecord aProcessList : processList) {
        instance = (ProcessInstanceRowRecord) aProcessList;
        if (instance != null) {
          csvRow = new StringBuilder();
          if (isProcessIdVisible) {
            addCSVValue(csvRow, instance.getId());
          }

          if (instance.isInError()) {
            addCSVValue(csvRow, getString("processManager.inError"));
          } else if (instance.isLockedByAdmin()) {
            addCSVValue(csvRow, getString("processManager.lockedByAdmin"));
          } else if (instance.isInTimeout()) {
            addCSVValue(csvRow, getString("processManager.timeout"));
          } else {
            addCSVValue(csvRow, "");
          }

          // add title
          addCSVValue(csvRow, instance.getField(0).getValue(getLanguage()));

          // add state
          addCSVValue(csvRow, instance.getField(1).getValue(getLanguage()));

          String fieldString;
          for (int c = 2; c < csvCols.size(); c++) {
            String fieldName = csvCols.get(c);
            fieldString = getFieldValue(instance, items, fieldName);
            addCSVValue(csvRow, fieldString);
          }
          csvRows.add(csvRow);
        }
      }

      return csvRows;
    } catch (FormException e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return null;
    }
  }

  private List<StringBuilder> exportDefinedItemsAsCSV() throws ProcessManagerException {
    try {
      List<DataRecord> processList = getCurrentProcessList();
      Item[] items = getFolderItems();
      RecordTemplate listHeaders = getProcessListHeaders();
      FieldTemplate[] headers = listHeaders.getFieldTemplates();
      String fieldsToExport = getComponentParameterValue("fieldsToExport");

      List<String> csvCols = new ArrayList<>();
      StringTokenizer tokenizer = new StringTokenizer(fieldsToExport, ";");
      while (tokenizer.hasMoreTokens()) {
        csvCols.add(tokenizer.nextToken());
      }

      ProcessInstanceRowRecord instance;
      StringBuilder csvHeader = new StringBuilder();
      List<StringBuilder> csvRows = new ArrayList<>();
      boolean isProcessIdVisible = isProcessIdVisible();

      if (isProcessIdVisible) {
        addCSVValue(csvHeader, "#");
      }

      String col;
      ItemImpl item;
      // add state column
      addCSVValue(csvHeader, headers[1].getLabel(getLanguage()));
      // add state column
      addCSVValue(csvHeader, headers[0].getLabel(getLanguage()));
      for (String csvCol : csvCols) {
        col = csvCol;
        item = (ItemImpl) getItemByName(items, col);
        if (item != null) {
          addCSVValue(csvHeader, item.getLabel(getCurrentRole(), getLanguage()));
        } else {
          addCSVValue(csvHeader, col);
        }
      }
      csvRows.add(csvHeader);

      StringBuilder csvRow = new StringBuilder();
      for (final DataRecord aProcessList : processList) {
        instance = (ProcessInstanceRowRecord) aProcessList;
        if (instance != null) {
          csvRow = new StringBuilder();
          if (isProcessIdVisible) {
            addCSVValue(csvRow, instance.getId());
          }

          // add state
          addCSVValue(csvRow, instance.getField(1).getValue(getLanguage()));

          // add title
          addCSVValue(csvRow, instance.getField(0).getValue(getLanguage()));

          String fieldString;
          for (String csvCol : csvCols) {
            if (csvCol.startsWith("${")) {
              fieldString = DataRecordUtil.applySubstitution(csvCol, instance, "fr");
            } else {
              fieldString = getFieldValue(instance, items, csvCol);
            }
            addCSVValue(csvRow, fieldString);
          }
          csvRows.add(csvRow);
        }
      }

      return csvRows;
    } catch (FormException e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return null;
    }
  }

  private void updateFolder(final Map<String, String> changes)
      throws FormException, WorkflowException {
    if (!changes.isEmpty()) {
      GenericRecordTemplate rt = new GenericRecordTemplate();
      for (String fieldName : changes.keySet()) {
        GenericFieldTemplate fieldTemplate = new GenericFieldTemplate(fieldName, "user");
        rt.addFieldTemplate(fieldTemplate);
      }
      DataRecord folder = rt.getEmptyRecord();
      for (String fieldName : changes.keySet()) {
        folder.getField(fieldName).setStringValue(changes.get(fieldName));
      }
      currentProcessInstance.updateFolder(folder);
    }
  }

  private String getFieldValue(ProcessInstanceRowRecord instance, Item[] items, String fieldName) {
    String fieldString;
    try {
      Field field = instance.getFullProcessInstance().getField(fieldName);
      fieldString = field.getValue(getLanguage());
      if (!StringUtil.isDefined(fieldString) || !field.getTypeName().equals(DateField.TYPE)) {
        ItemImpl item = (ItemImpl) getItemByName(items, fieldName);
        if (item != null) {
          Map<String, String> keyValuePairs = item.getKeyValuePairs();
          if (keyValuePairs != null && keyValuePairs.size() > 0) {
            StringBuilder newValue = new StringBuilder();
            if (StringUtil.isDefined(fieldString)) {
              if (fieldString.contains("##")) {
                // Try to display a checkbox list
                StringTokenizer tokenizer = new StringTokenizer(fieldString, "##");
                String token;
                while (tokenizer.hasMoreTokens()) {
                  token = tokenizer.nextToken();
                  token = keyValuePairs.get(token);
                  newValue.append(token);
                  if (tokenizer.hasMoreTokens()) {
                    newValue.append(", ");
                  }
                }
              } else {
                newValue.append(keyValuePairs.get(fieldString));
              }
            }
            fieldString = newValue.toString();
          }
        }
      }
    } catch (WorkflowException we) {
      fieldString = "";
    }
    return fieldString;
  }

  private String writeCSVFile(List<StringBuilder> csvRows) {
    FileOutputStream fileOutput = null;
    String csvFilename = new Date().getTime() + ".csv";
    try {
      fileOutput = new FileOutputStream(FileRepositoryManager.getTemporaryPath() + csvFilename);

      StringBuilder csvRow;
      for (StringBuilder csvRow1 : csvRows) {
        csvRow = csvRow1;
        fileOutput.write(csvRow.toString().getBytes());
        fileOutput.write("\n".getBytes());
      }
    } catch (IOException e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      csvFilename = null;
    } finally {
      if (fileOutput != null) {
        try {
          fileOutput.flush();
          fileOutput.close();
        } catch (IOException e) {
          csvFilename = null;
          SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
        }
      }
    }
    return csvFilename;
  }

  private List<String> getCSVCols() throws ProcessManagerException, FormException {
    List<String> csvCols = new ArrayList<>();
    Item[] items = getFolderItems();
    RecordTemplate listHeaders = getProcessListHeaders();
    FieldTemplate[] headers = listHeaders.getFieldTemplates();

    for (final FieldTemplate header : headers) {
      csvCols.add(header.getFieldName());
    }


    for (final Item item : items) {
      if (!csvCols.contains(item.getName())) {
        csvCols.add(item.getName());
      }
    }

    return csvCols;
  }

  private void addCSVValue(StringBuilder row, String value) {
    row.append("\"");
    if (value != null) {
      row.append(value.replaceAll("\"", "\"\""));
    }
    row.append("\"").append(",");
  }

  /**
   * Is current user is part of the locking users for current process instance ?
   * @return true is current user is part of the locking users for current process instance
   */
  public boolean isCurrentUserIsLockingUser() {
    return currentUserIsLockingUser;
  }

  public void setResumingInstance(boolean isResumingInstance) {
    this.isResumingInstance = isResumingInstance;
  }

  public boolean isResumingInstance() {
    return isResumingInstance;
  }

  public String getTrace(String function, String extraParam) {
    StringBuilder trace = new StringBuilder();

    try {
      String processInstanceId = (currentProcessInstance == null) ? "N-C" : currentProcessInstance.
          getInstanceId();
      trace.append("userId=").append(getUserId()).append(",role=").append(getCurrentRole())
          .append(",function=").append(function).append(",processInstanceId=")
          .append(processInstanceId).
          append(",").append(extraParam);
    } catch (Exception e) {
      // a debug trace must not generate exception
      trace.append(e.getClass() + " : " + e.getMessage());
    }

    return trace.toString();
  }

  /**
   * Get the current tokenId. Token Id prevents users to use several windows with same session.
   * @return the current token id
   */
  public String getCurrentTokenId() {
    return currentTokenId;
  }

  /**
   * Set the current tokenId. Token Id prevents users to use several windows with same session.
   * @param newTokenId the current token id
   */
  public void setCurrentTokenId(String newTokenId) {
    this.currentTokenId = newTokenId;
  }

  /**
   * The session controller saves any fatal exception.
   */
  private ProcessManagerException fatalException = null;
  /**
   * All the process instance of this work session are built from a same process model : the
   * processModel.
   */
  private String peasId = null;
  private ProcessModel processModel = null;
  /**
   * The session saves a current User (workflow user)
   */
  private User currentUser = null;
  /**
   * The session saves a list of user roles.
   */
  private String[] userRoles = null;
  private NamedValue[] userRoleLabels = null;
  /**
   * The session saves a current User role.
   */
  private String currentRole = null;
  /**
   * The creation rights (true if user can create new instances)
   */
  private boolean creationRights = false;
  /**
   * The session saves a current process instance.
   */
  private ProcessInstance currentProcessInstance = null;
  /**
   * The session saves a current process filter.
   */
  private ProcessFilter currentProcessFilter = null;
  /**
   * The session saves a current process instance list rows template.
   */
  private RecordTemplate currentListHeaders = null;
  /**
   * The session saves a current process instance list.
   */
  private List<DataRecord> currentProcessList = null;
  /**
   * The user settings
   */
  private UserSettings userSettings = null;
  /**
   * Flag to indicate if current user is one of the locking users for current process instance
   */
  private boolean currentUserIsLockingUser;
  /**
   * Flag to indicate if user is currently resuming a saved instance
   */
  private boolean isResumingInstance = false;
  /**
   * Current token Id prevents users to use several windows with the same session.
   */
  private String currentTokenId = null;

}
