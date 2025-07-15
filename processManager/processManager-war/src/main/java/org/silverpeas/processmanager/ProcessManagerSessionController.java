/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.processmanager;

import org.silverpeas.core.SilverpeasExceptionMessages;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.content.form.*;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.field.DateField;
import org.silverpeas.core.contribution.content.form.field.MultipleUserField;
import org.silverpeas.core.contribution.content.form.field.UserField;
import org.silverpeas.core.contribution.content.form.form.XmlForm;
import org.silverpeas.core.contribution.content.form.record.GenericFieldTemplate;
import org.silverpeas.core.contribution.content.form.record.GenericRecordTemplate;
import org.silverpeas.core.contribution.content.form.record.Parameter;
import org.silverpeas.core.notification.message.MessageNotifier;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.MapUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.csv.CSVRow;
import org.silverpeas.core.web.export.ExportCSVBuilder;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.webcomponent.WebMessager;
import org.silverpeas.core.workflow.api.*;
import org.silverpeas.core.workflow.api.error.WorkflowError;
import org.silverpeas.core.workflow.api.event.*;
import org.silverpeas.core.workflow.api.instance.*;
import org.silverpeas.core.workflow.api.model.*;
import org.silverpeas.core.workflow.api.model.Participant;
import org.silverpeas.core.workflow.api.task.Task;
import org.silverpeas.core.workflow.api.user.*;
import org.silverpeas.core.workflow.engine.WorkflowHub;
import org.silverpeas.core.workflow.engine.datarecord.ProcessInstanceRowRecord;
import org.silverpeas.core.workflow.engine.instance.LockingUser;
import org.silverpeas.core.workflow.engine.model.ActionRefs;
import org.silverpeas.core.workflow.engine.model.ItemImpl;
import org.silverpeas.core.workflow.engine.user.UserSettingsService;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.Mutable;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.processmanager.record.QuestionRecord;
import org.silverpeas.processmanager.record.QuestionTemplate;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.silverpeas.core.SilverpeasExceptionMessages.failureOnGetting;
import static org.silverpeas.core.cache.service.CacheAccessorProvider.getThreadCacheAccessor;
import static org.silverpeas.core.contribution.attachment.AttachmentService.VERSION_MODE;
import static org.silverpeas.core.util.CollectionUtil.asList;
import static org.silverpeas.core.workflow.util.WorkflowUtil.getItemByName;
import static org.silverpeas.kernel.util.StringUtil.isDefined;
import static org.silverpeas.processmanager.ProcessManagerException.PROCESS_INSTANCE_CREATION_FAILURE;

/**
 * The ProcessManager Session controller
 */
public class ProcessManagerSessionController extends AbstractComponentSessionController {

  private static final String PROCESS_MANAGER_SESSION_CONTROLLER =
      "ProcessManagerSessionController";
  private static final String SUPERVISOR_ROLE = "supervisor";
  private static final String QUESTION_ACTION = "#question#";
  private static final String RESPONSE_ACTION = "#response#";
  private static final String RE_ASSIGN_ACTION = "#reAssign#";
  private static final String USER_SETTINGS = "user settings";

  /**
   * Builds and init a new session controller for the process manager instance.
   *
   * @param mainSessionCtrl the main session controller of the current user in Silverpeas.
   * @param context the context of the user navigation in the process manager instance.
   * @throws ProcessManagerException if the initialization of the controller failed.
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
      throw new ProcessManagerException(PROCESS_MANAGER_SESSION_CONTROLLER,
          "processManager.UNAUTHORIZED_USER");
    }
    currentRole = userRoles[0];

    // Reset the user rights for creation
    resetCreationRights();

    // Load user information
    userSettings = UserSettingsService.get().get(mainSessionCtrl.getUserId(), peasId);
  }

  /**
   * Builds a ill session controller. Initialization is skipped and this session controller can only
   * display the fatal exception. Used by the request router when a full session controller can't be
   * built.
   *
   * @param mainSessionCtrl the main session controller of the user in Silverpeas.
   * @param context the user navigation context in the process manager instance.
   * @param fatal the exception to display in case of failure.
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
   *
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
          break;
        }
      }
    } catch (Exception e) {
      // nothing to do
    }
  }

  /**
   * Is the history can be filtered? In this case, the forms mapped to each state are visible only
   * if the current user was a working or an interested user.
   *
   * @return true if the history can be filtered.
   */
  public boolean isHistoryCanBeFiltered() {
    String parameterValue = getComponentParameterValue("filterHistory");
    return StringUtil.getBooleanValue(parameterValue);
  }

  public boolean isHistoryTabVisible() {
    String parameterValue = this.getComponentParameterValue("historyTabEnable");
    if (isDefined(parameterValue)) {
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
   *
   * @return true is print button is visible
   */
  public boolean isPrintButtonEnabled() {
    return StringUtil.getBooleanValue(getComponentParameterValue("printButtonEnabled"));
  }

  /**
   * Save button on an action can be disabled. So it's return the visibility status of that button.
   *
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
   * current ProcessInstance. Throws ProcessManagerException when the instanceId is unknown and when
   * the current user is not allowed to access the instance. Doesn't change the current process
   * instance when an error occurs.
   */
  public void resetCurrentProcessInstance(String instanceId)
      throws ProcessManagerException {
    this.setResumingInstance(false);
    if (instanceId != null) {
      ProcessInstance instance;
      try {
        instance = Workflow.getProcessInstanceManager().getProcessInstance(instanceId);
      } catch (WorkflowException e) {
        throw new ProcessManagerException("Unknown process instance " + instanceId, e);
      }
      currentProcessInstance = instance;

    }
    if (currentProcessInstance == null) {
      throw new ProcessManagerException("No process instance");
    }
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
    currentListHeaders = processModel.getRowTemplate(currentRole, getLanguage(),
        isProcessIdVisible());
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
      User activeUser = getActiveUser();
      String[] groupIds = getOrganisationController().getAllGroupIdsOfUser(activeUser.getUserId());
      List<ProcessInstance> processList = Workflow.getProcessInstanceManager()
          .getProcessInstances(peasId, activeUser, currentRole, getUserRoles(), groupIds);
      currentProcessList = getCurrentFilter().filter(processList, currentRole, getLanguage());
    } catch (WorkflowException e) {
      throw new ProcessManagerException(failureOnGetting("process list of workflow", peasId), e);
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
      throw new ProcessManagerException(failureOnGetting("role name from todo", externalTodoId), e);
    }
  }

  /**
   * Get the process instance Id referred by the todo with the given todo id
   *
   * @param externalTodoId external todo identifier
   * @return the process instance Id referred by the todo with the given todo id.
   * @throws ProcessManagerException if an error occurs.
   */
  public String getProcessInstanceIdFromExternalTodoId(String externalTodoId)
      throws ProcessManagerException {
    try {
      return Workflow.getTaskManager().getProcessInstanceIdFromExternalTodoId(externalTodoId);
    } catch (WorkflowException e) {
      throw new ProcessManagerException(failureOnGetting("process from todo", externalTodoId), e);
    }
  }

  public boolean isUserAllowedOnActiveStates() throws ProcessManagerException {
    String[] states = currentProcessInstance.getActiveStates();
    if (states == null) {
      return false;
    }
    for (String state : states) {
      if (getActiveUsers(state).contains(getActiveUser().getUserId())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Get the active states.
   *
   * @return the active states.
   */
  public List<CurrentState> getActiveStates() throws ProcessManagerException {
    String[] stateNames = currentProcessInstance.getActiveStates();
    if (stateNames == null) {
      return emptyList();
    }

    Map<String, List<Task>> tasksByStates = getTasksByStates();

    List<CurrentState> currentStates = new ArrayList<>();
    for (String stateName : stateNames) {
      State state = getState(currentProcessInstance, stateName);
      filterActions(state);
      CurrentState currentState = new CurrentState(state);
      currentState.setLabel(state.getLabel(currentRole, getLanguage()));
      currentState.setWorkingUsersAsString(getActiveRoles(state));

      List<Task> tasks = tasksByStates.get(stateName);
      currentState.setTasks(tasks);

      currentStates.add(currentState);
    }

    return currentStates;
  }

  private Map<String, List<Task>> getTasksByStates() throws ProcessManagerException {
    Map<String, List<Task>> result = new HashMap<>();
    Task[] tasks = getTasks();
    for (Task task : tasks) {
      State state = task.getState();
      MapUtil.putAddList(result, state.getName(), task);
    }
    return result;
  }

  private String getActiveRoles(State state) {

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
        } else if (participant != null) {
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
          try {
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
          } catch (Exception e) {
            SilverLogger.getLogger(this).warn(e);
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
          try {
            if (item != null) {
              String groupId = currentProcessInstance.getField(item.getName()).getStringValue();
              if (groupId != null) {
                Group group = getOrganisationController().getGroup(groupId);
                if (group != null) {
                  role.append(group.getName());
                }
              }
            }
          } catch (Exception e) {
            SilverLogger.getLogger(this).warn(e);
          }
        }
      }
    }
    return role.toString();
  }

  /**
   * Get the active user names
   */
  private boolean isActiveUser() {
    if (currentProcessInstance == null) {
      return false;
    }
    final String[] states = currentProcessInstance.getActiveStates();
    return states != null && Stream.of(states)
        .flatMap(s -> {
          try {
            return Stream.of(currentProcessInstance.getWorkingUsers(s));
          } catch (WorkflowException ignored) {
            // ignore unknown state
          }
          return Stream.empty();
        })
        .anyMatch(u -> getActiveUser().getUserId().equals(u.getUser().getUserId()));
  }

  private List<String> getActiveUsers(String stateName) throws ProcessManagerException {
    List<String> activeUsers = new ArrayList<>();
    State state = getState(currentProcessInstance, stateName);
    if (state != null) {
      activeUsers.addAll(getUsers(state.getWorkingUsers()));
      activeUsers.addAll(getUsers(state.getInterestedUsers()));
    }
    return activeUsers;
  }

  private List<String> getUsers(QualifiedUsers qualifiedUsers) {
    return getUsers(qualifiedUsers, false);
  }

  private List<String> getUsers(QualifiedUsers qualifiedUsers, boolean useCurrentRole) {
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
          users.add(getActiveUser().getUserId());
        }
      } else if (participant != null) {
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
            if ((isDefined(role) && currentRole.equals(role)) ||
                StringUtil.isNotDefined(role)) {
              users.add(field.getStringValue());
            }
          } else if (field instanceof MultipleUserField) {
            MultipleUserField multipleUserField = (MultipleUserField) field;
            if ((isDefined(role) && currentRole.equals(role)) ||
                StringUtil.isNotDefined(role)) {
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
    final boolean lookAlsoForRemoved = getCurrentReplacement() != null;
    final String[] userIds = getOrganisationController().getUsersIdsByRoleNames(getComponentId(),
        roles, lookAlsoForRemoved);
    users.addAll(Arrays.asList(userIds));

    // Process related groups
    RelatedGroup[] relatedGroups = qualifiedUsers.getRelatedGroups();
    if (relatedGroups != null) {
      for (RelatedGroup relatedGroup : relatedGroups) {
        if (relatedGroup != null) {
          String role = relatedGroup.getRole();
          if (currentRole.equals(role) || StringUtil.isNotDefined(role)) {
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
    }

    return users;
  }

  /**
   * Returns the workflow user having the given id.
   *
   * @param userId the user identifier
   * @return the workflow user having the given id.
   * @throws ProcessManagerException if an error occurs.
   */
  public User getUser(String userId) throws ProcessManagerException {
    try {
      return Workflow.getUserManager().getUser(userId);
    } catch (WorkflowException e) {
      throw new ProcessManagerException(failureOnGetting("user", userId), e);
    }
  }

  /**
   * Returns the process model having the given id.
   *
   * @param modelId the model identifier
   * @return the process model having the given id.
   * @throws ProcessManagerException if an error occurs.
   */
  public final ProcessModel getProcessModel(String modelId) throws ProcessManagerException {
    try {
      return Workflow.getProcessModelManager().getProcessModel(modelId);
    } catch (WorkflowException e) {
      throw new ProcessManagerException(failureOnGetting("process model", modelId), e);
    }
  }

  /**
   * @return the current role name.
   */
  public String getCurrentRole() {
    return currentRole;
  }

  /**
   * @return the current replacement if any.
   */
  @SuppressWarnings("rawtypes")
  public Replacement getCurrentReplacement() {
    return currentReplacement;
  }

  public <T extends Replacement<T>> void resetCurrentRoleAsSubstitute(String role,
      String incumbentId)
      throws ProcessManagerException {
    final Mutable<String> finalRole = Mutable.of(role);
    final Optional<T> replacement = getCurrentUserReplacement(incumbentId, role);
    replacement.ifPresent(r -> finalRole.set(getRoleNameForSubstitute(r, role)));
    if (replacement.isEmpty()) {
      WebMessager.getInstance().addWarning(getMultilang()
              .getString("processManager.replacements.errors.noMoreValid"),
          getUser(incumbentId).getFullName());
    }
    resetCurrentRole(finalRole.get());
  }

  /**
   * Returns the current role name.
   *
   * @param role a role
   * @throws ProcessManagerException if an error occurs.
   */
  public void resetCurrentRole(String role) throws ProcessManagerException {
    final Mutable<String> roleToPlay = Mutable.empty();
    if (role != null && !role.isEmpty()) {
      final String[] roleCtx = role.split(":");
      if (roleCtx.length == 2) {
        Replacement.get(roleCtx[0]).ifPresent(r -> {
          this.currentReplacement = r;
          roleToPlay.set(roleCtx[1]);
        });
      } else {
        this.currentReplacement = null;
        roleToPlay.set(role);
      }
    }
    resetUserRole(roleToPlay.orElse(null));
    resetCreationRights();
    resetProcessFilter();
    resetCurrentProcessList(false);
    resetCurrentProcessListHeaders();
  }

  private void resetUserRole(String role) {
    if (StringUtil.isDefined(role)) {
      if (List.of(getUserRoles()).contains(role)) {
        this.currentRole = role;
      }
    }
  }

  /**
   * Returns the user roles as a list of (name, label) pair.
   */
  public NamedValue[] getUserRoleLabels() {
    return getThreadCacheAccessor()
        .getCache()
        .computeIfAbsent("ProcessManagerSC.getUserRoleLabels" + getComponentId(),
            NamedValue[].class, () -> {
              final String lang = getLanguage();
              final Role[] roles = processModel.getRoles();
              final List<NamedValue> labels = new ArrayList<>();

              final List<Replacement<?>> replacements = getCurrentUserReplacements();
              for (final Replacement<?> replacement : replacements) {
                final List<String> incumbentRoles =
                    getSubstituteRolesOf(replacement.getIncumbent());
                for (final String roleName : incumbentRoles) {
                  getRoleLabel(replacement, roles, roleName, lang)
                      .filter(n -> labels.stream()
                          .noneMatch(l -> Objects.equals(n.getValue(), l.getValue())))
                      .ifPresent(labels::add);
                }
              }

              // quadratic search ! but it's ok : the list are about 3 or 4 length.
              for (final String userRole : userRoles) {
                getRoleLabel(null, roles, userRole, lang).ifPresent(labels::add);
              }

              labels.sort(NamedValue.ascendingValues);
              return labels.toArray(new NamedValue[0]);
            });
  }

  /**
   * Returns the component instance roles as a list of (name, label) pair.
   */
  public NamedValue[] getComponentInstanceRoleLabels() {
    String lang = getLanguage();
    Role[] roles = processModel.getRoles();
    List<NamedValue> labels = new ArrayList<>();
    for (final Role role : roles) {
      getRoleLabel(null, roles, role.getName(), lang).ifPresent(labels::add);
    }
    labels.sort(NamedValue.ascendingValues);
    return labels.toArray(new NamedValue[0]);
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
      throw new ProcessManagerException(
          failureOnGetting("presentation form of model", processModel.getModelId()), e);
    }
  }

  public Item[] getFolderItems() {
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
      throw new ProcessManagerException(failureOnGetting("presentation form data of process",
          currentProcessInstance.getInstanceId()), e);
    }

    if (data == null) {
      String instanceId =
          currentProcessInstance == null ? "unknown" : currentProcessInstance.getInstanceId();
      throw new ProcessManagerException(failureOnGetting("presentation form data of process",
          instanceId));
    }

    return data;
  }

  /**
   * Returns the creation task.
   */
  private Task getCreationTask() throws ProcessManagerException {
    try {
      return Workflow.getTaskManager().getCreationTask(getActiveUser(), currentRole, processModel);
    } catch (WorkflowException e) {
      throw new ProcessManagerException(
          failureOnGetting("creation task of model", processModel.getModelId()), e);
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
      throw new ProcessManagerException(
          failureOnGetting("creation form of model", processModel.getModelId()), e);
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
      throw new ProcessManagerException(
          failureOnGetting("empty creation record of model", processModel.getModelId()), e);
    }
  }

  /**
   * Returns the form to ask a question
   */
  public Form getQuestionForm(boolean readonly) throws ProcessManagerException {
    try {
      return new XmlForm(new QuestionTemplate(getLanguage(), readonly));
    } catch (FormException fe) {
      throw new ProcessManagerException(failureOnGetting("question", "form"), fe);
    }
  }

  /**
   * Returns the an empty question record which will be filled with the question form.
   */
  public DataRecord getEmptyQuestionRecord() {
    return new QuestionRecord("");
  }

  /**
   * get the question with the given id
   *
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
  public DataRecord getQuestionRecord(String questionId) {
    Question question = getQuestion(questionId);
    return new QuestionRecord(question.getQuestionText());
  }

  /**
   * Get assign template (for the re-affectations)
   */
  private GenericRecordTemplate getAssignTemplate(ProcessInstance processInstance)
      throws ProcessManagerException {
    try {
      String[] activeStates = processInstance.getActiveStates();
      GenericRecordTemplate rt = new GenericRecordTemplate();

      for (final String activeState : activeStates) {
        State state = getState(processInstance, activeState);
        Actor[] actors = processInstance.getWorkingUsers(activeState);

        for (int j = 0; actors != null && j < actors.length; j++) {
          GenericFieldTemplate fieldTemplate = new GenericFieldTemplate(
              processStateName(state.getName()) + "_" + actors[j].getUserRoleName() + "_" + j,
              "user");
          fieldTemplate.addLabel(state.getLabel(currentRole, getLanguage()), getLanguage());
          fieldTemplate.setDisplayerName("user");
          fieldTemplate.setMandatory(true);
          fieldTemplate.addParameter("roles", actors[j].getUserRoleName());
          rt.addFieldTemplate(fieldTemplate);
        }
      }
      return rt;
    } catch (FormException | WorkflowException ex) {
      throw new ProcessManagerException(failureOnGetting("assign", "template"), ex);
    }
  }

  /**
   * Deletion of problematic characters in state name (spaces)
   *
   * @param name name of a state process
   * @return the normalized name
   */
  private String processStateName(String name) {
    return name.replace(" ", "");
  }

  /**
   * Gets a form with the assignation of users to roles for all the active states (for the
   * re-affectations of some roles in the active states)
   *
   * @param processInstance the instance of the process for which the assignation form is asked.
   * @return a form with for active states a field with the roles required by the state.
   * @throws ProcessManagerException if the build of the form fails.
   */
  public Form getAssignFormOfActiveStates(ProcessInstance processInstance)
      throws ProcessManagerException {
    try {
      return new XmlForm(getAssignTemplate(processInstance));
    } catch (FormException ex) {
      throw new ProcessManagerException(failureOnGetting("assign", "form"), ex);
    }
  }

  /**
   * Gets the assignation data of the users working on the active states (for the re-affectations of
   * some roles in the active states).
   *
   * @param processInstance the instance of the process for which the assignation data is asked.
   * @return a data record with all the assignation of the working users to the different roles of
   * each active states. Each field of the record is a mapping between a role in an active state to
   * a working user.
   */
  public DataRecord getAssignRecordOfActiveStates(ProcessInstance processInstance) {
    String[] activeStates = processInstance.getActiveStates();
    Actor[] actors;

    try {
      DataRecord data = getAssignTemplate(processInstance).getEmptyRecord();
      for (int i = 0; activeStates != null && i < activeStates.length; i++) {
        actors = processInstance.getWorkingUsers(activeStates[i]);
        for (int j = 0; actors != null && j < actors.length; j++) {
          Field field =
              data.getField(
                  processStateName(activeStates[i]) + "_" + actors[j].getUserRoleName() + "_" + j);
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
   * Reassign the roles in the active states of the specified process instance to the users defined
   * in the given assignation data.
   *
   * @param processInstance the instance of the process in which the reassignation will be operated
   * @param data the record data in which are defined the assignation of the working users to the
   * different roles of each active states. Each field of the record should be a mapping between a
   * role in an active state to a working user.
   * @throws ProcessManagerException if the reassignation failed.
   */
  public void reassignInActiveStates(ProcessInstance processInstance, DataRecord data)
      throws ProcessManagerException {
    Predicate<Actor> allActors = a -> true;

    Mutable<Integer> inc = Mutable.of(0);
    FuncActor substitute = a -> {
      try {
        String stateName = a.getState().getName();
        String roleName = a.getUserRoleName();
        Field field = data.getField(processStateName(stateName) + "_" + roleName + "_" + inc.get());
        String userId = field.getStringValue();
        User user = Workflow.getUserManager().getUser(userId);
        inc.set(inc.get() + 1);
        return Workflow.getProcessInstanceManager().createActor(user, roleName, a.getState());
      } catch (WorkflowException | FormException e) {
        throw new SilverpeasRuntimeException(e);
      }
    };

    String[] activeStates = processInstance.getActiveStates();
    reassignRoles(processInstance, activeStates, allActors, substitute);
  }

  @FunctionalInterface
  private interface FuncActor extends Function<Actor, Actor> {
    @Override
    Actor apply(Actor actor) throws SilverpeasRuntimeException;
  }

  /**
   * Reassign the roles played by the given actors to the new ones in the specified states of the
   * given process instance. An actor is a user that plays a given role in a given state of a
   * process instance.
   *
   * @param processInstance the process instance in which the reassignation has to be done.
   * @param states the name of the states of the process instance concerned by the reassignation.
   * @param actorsToReplace a predicate for filtering the actors of each state that has to be
   * replaced.
   * @param substitutes a function providing for a given existing actor his substitute.
   * @throws ProcessManagerException if the reassignation of roles fails.
   */
  private void reassignRoles(final ProcessInstance processInstance, final String[] states,
      Predicate<Actor> actorsToReplace, FuncActor substitutes) throws ProcessManagerException {
    try {
      // changes to apply on the folder items of type "user"
      Map<String, String> changes = new HashMap<>();
      // the actors to replace
      List<Actor> previousActors = new ArrayList<>();
      // users related to the states
      List<RelatedUser> relatedUsers = new ArrayList<>();

      for (String stateName : states) {
        // get actors and related users in the given process state
        Actor[] actors = processInstance.getWorkingUsers(stateName);
        relatedUsers.addAll(Stream.of(
            processInstance.getProcessModel().getState(stateName).getWorkingUsers()
                .getRelatedUsers()).collect(Collectors.toList()));

        // filter only the actors to replace
        previousActors.addAll(Stream.of(actors)
            .filter(actorsToReplace)
            .collect(Collectors.toList()));
      }

      // for each actor to replace get their substitute
      List<Actor> newActors = previousActors.stream()
          .map(substitutes)
          .collect(Collectors.toList());

      // now define the change of users to do for each folder item of type "user" among the
      // related users of the given process state
      newActors
          .forEach(a ->
              relatedUsers.stream()
                  .filter(u -> a.getUserRoleName().equals(u.getRole()))
                  .map(RelatedUser::getFolderItem)
                  .filter(Objects::nonNull)
                  .forEach(
                      i -> addAnyChanges(processInstance, a.getUser().getUserId(), i, changes))
          );

      // apply the reassignation
      WorkflowEngine engine = Workflow.getWorkflowEngine();
      engine.reAssignActors((UpdatableProcessInstance) processInstance,
          previousActors.toArray(new Actor[0]),
          newActors.toArray(new Actor[0]),
          currentUser);

      // update the folder items with the specified computed changes
      updateFolder(processInstance, changes);
    } catch (WorkflowException | FormException | SilverpeasRuntimeException e) {
      throw new ProcessManagerException(
          "Fail to reassign roles in states of process " + processInstance.getInstanceId(), e);
    }
  }

  /**
   * Reassign in all the states of the specified process instance the roles of the given user to the
   * specified substitute.
   *
   * @param processInstance the instance of the process in which the reassignation will be operated
   * @param user the user playing one or several roles in the states of the process instance.
   * @param substitute the user to whom the roles in the states will be reassigned.
   * @throws ProcessManagerException if the reassignation fails.
   */
  private void reassignInAssignedStates(ProcessInstance processInstance, final User user,
      final User substitute) throws ProcessManagerException {

    Predicate<Actor> actorToReplace = a -> a.getUser().getUserId().equals(user.getUserId());
    FuncActor newActor = a -> Workflow.getProcessInstanceManager().createActor(
        substitute, a.getUserRoleName(), a.getState());

    String[] assignedStates = processInstance.getAllAssignedStates(user);
    reassignRoles(processInstance, assignedStates, actorToReplace, newActor);
  }

  /**
   * Replaces the specified user by the other one in all the states of the current process
   * instances. All the roles assigned to the user in all the state in each process instance are
   * reassigned to the specified substitute. So, the constrain is the substitute must have the
   * ability to play the same roles than the given user. Otherwise an exception is thrown.
   *
   * @param userId the unique identifier of the user to replace.
   * @param substituteId the unique identifier of the user that will substitute the above user.
   * @throws ProcessManagerException if an error occurs while performing the substitution.
   */
  public void replaceInAllAssignedStates(final String userId, final String substituteId)
      throws ProcessManagerException {
    final ReassignmentReport report = new ReassignmentReport(this, userId, substituteId);
    try {
      report.start();
      Administration admin = Administration.get();
      var userProfiles = admin.getAllProfiles(userId, getComponentId()).stream()
          .filter(p -> !p.getName().equals(SilverpeasRole.SUPERVISOR.getName()))
          .map(ProfileInst::getId)
          .collect(Collectors.toSet());
      var substituteProfiles = admin.getAllProfiles(substituteId, getComponentId()).stream()
          .map(ProfileInst::getId)
          .collect(Collectors.toSet());
      if (!substituteProfiles.containsAll(userProfiles)) {
        throw new ProcessManagerException(
            "The substitute " + substituteId + " doesn't play all the roles of user " + userId);
      }

      ProcessInstanceManager processInstanceManager = Workflow.getProcessInstanceManager();
      User user = Workflow.getUserManager().getUser(userId);
      User substitute = Workflow.getUserManager().getUser(substituteId);

      List<DataRecord> processList = getCurrentProcessList();
      for (final DataRecord processData : processList) {
        ProcessInstanceRowRecord processRecord = (ProcessInstanceRowRecord) processData;
        ProcessInstance process =
            processInstanceManager.getProcessInstance(processRecord.getId());
        reassignInAssignedStates(process, user, substitute);
      }
      report.end();
    } catch (WorkflowException e) {
      report.end(e);
      throw new ProcessManagerException(e);
    }
  }

  private void addAnyChanges(ProcessInstance processInstance, final String userId,
      final Item item,
      final Map<String, String> changes) {
    try {
      Field folderField = processInstance.getField(item.getName());
      if (folderField instanceof UserField) {
        changes.put(item.getName(), userId);
      }
    } catch (WorkflowException we) {
      // ignore it.
    }
  }

  /**
   * Create a new process instance with the filled form.
   *
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
      setSubstituteToEvent(event);

      // Is a validate or a "save as draft" action ?
      if (isDraft) {
        TaskSavedEvent tse = (TaskSavedEvent) event;
        tse.setFirstTimeSaved(firstTimeSaved);
        Workflow.getWorkflowEngine().process((TaskSavedEvent) event);
      } else {
        Workflow.getWorkflowEngine().process((TaskDoneEvent) event);
        feedbackUser("processManager.createProcess.feedback",
            URLUtil.getSimpleURL(URLUtil.URL_COMPONENT, getComponentId()));
      }

      return event.getProcessInstance().getInstanceId();
    } catch (WorkflowException e) {
      throw new ProcessManagerException(PROCESS_INSTANCE_CREATION_FAILURE, e);
    }
  }

  private void setSubstituteToEvent(GenericEvent event) {
    if (getCurrentReplacement() != null) {
      event.setSubstitute(getCurrentReplacement().getSubstitute());
    }
  }


  private void feedbackUser(String key, String param) {
    MessageNotifier.addSuccess(getMultilang().getString(key), param).setDisplayLiveTime(10000);
  }

  private void doAPause() {
    int duration = getSettings().getInteger("refresh.delay", 1000);
    if (duration > 0) {
      try {
        Thread.sleep(duration);
      } catch (InterruptedException ie) {
        SilverLogger.getLogger(this).error(ie.getLocalizedMessage(), ie);
        Thread.currentThread().interrupt();
      }
    }
  }

  /**
   * Get all tasks assigned for current user on current process instance
   */
  public Task[] getTasks() throws ProcessManagerException {
    try {
      return Workflow.getTaskManager()
          .getTasks(getActiveUser(), currentRole, currentProcessInstance);
    } catch (WorkflowException e) {
      throw new ProcessManagerException(
          failureOnGetting("tasks of process", currentProcessInstance), e);
    }
  }

  private void filterActions(State state) {
    final AllowedActions filteredActions = new ActionRefs();
    state.setFilteredActions(filteredActions);
    // First, check if current role is allowed to deal with process in this state
    List<String> grantedUserIds = getUsers(state.getWorkingUsers(), true);
    if (!grantedUserIds.contains(getActiveUser().getUserId())) {
      return;
    }
    // Then, check if current role is allowed to deal with each action declared in this state
    if (state.getAllowedActionsEx() != null) {
      final Iterator<AllowedAction> actions = state.getAllowedActionsEx().iterateAllowedAction();
      while (actions.hasNext()) {
        final AllowedAction action = actions.next();
        final QualifiedUsers qualifiedUsers = action.getAction().getAllowedUsers();
        grantedUserIds = getUsers(qualifiedUsers, true);
        if (grantedUserIds.contains(getActiveUser().getUserId())) {
          filteredActions.addAllowedAction(action);
        }
      }
    }
  }

  /**
   * Search for an hypothetical action of kind "delete", allowed for the current user with the given
   * role
   *
   * @return an array of 3 Strings { action.name, state.name, action.label }, an empty array if no
   * action found
   */
  public String[] getDeleteAction() {
    Task[] tasks;
    State state;
    Action[] actions;

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

      return new String[0];
    } catch (ProcessManagerException e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return new String[0];
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
      throw new ProcessManagerException(
          failureOnGetting("presentation form of model", processModel.getModelId()), e);
    }
  }

  /**
   * Returns the form associated to the named action. Returns null if this action has no form.
   * Throws an exception if the action is unknown.
   */
  public Form getActionForm(String actionName) throws ProcessManagerException {
    try {
      return processModel.getPublicationForm(actionName, currentRole, getLanguage());
    } catch (WorkflowException e) {
      throw new ProcessManagerException(
          failureOnGetting("action form of model", processModel.getModelId()), e);
    }
  }

  /**
   * Returns a new DataRecord filled with the folder data and which will be be completed by the
   * action form.
   */
  public DataRecord getActionRecord(String actionName)
      throws ProcessManagerException {
    try {
      return currentProcessInstance.getNewActionRecord(actionName, getLanguage());
    } catch (WorkflowException e) {
      throw new ProcessManagerException(failureOnGetting("action", actionName), e);
    }
  }

  /**
   * Create a new history step instance with the filled form.
   */
  public void processAction(String stateName, String actionName, DataRecord data, boolean isDraft,
      boolean isFirstTimeSaved) throws ProcessManagerException {
    try {
      Task task;

      if (isDefined(stateName)) {
        task = getTask(stateName);
      } else {
        task = getCreationTask();
        task.setProcessInstance(currentProcessInstance);
      }

      // Is a validate or a "save as draft" action ?
      if (isDraft) {
        TaskSavedEvent tse = task.buildTaskSavedEvent(actionName, data);
        tse.setFirstTimeSaved(isFirstTimeSaved);
        setSubstituteToEvent(tse);
        Workflow.getWorkflowEngine().process(tse);
      } else {
        TaskDoneEvent event = task.buildTaskDoneEvent(actionName, data);
        event.setResumingAction(this.isResumingInstance);
        setSubstituteToEvent(event);
        Workflow.getWorkflowEngine().process(event);
        feedbackUser("processManager.action.feedback",
            URLUtil.getSimpleURL(URLUtil.URL_COMPONENT, getComponentId()));
      }
    } catch (WorkflowException e) {
      throw new ProcessManagerException("Fail to process action " + actionName, e);
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
      setSubstituteToEvent(event);
      Workflow.getWorkflowEngine().process(event);
    } catch (WorkflowException e) {
      throw new ProcessManagerException("Fail to process question", e);
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
      setSubstituteToEvent(event);
      Workflow.getWorkflowEngine().process(event);
    } catch (WorkflowException e) {
      throw new ProcessManagerException("Fail to process response", e);
    }
  }

  /**
   * Get locking users list
   *
   * @throws ProcessManagerException if an error occurs.
   */
  public List<LockVO> getLockingUsers() throws ProcessManagerException {
    this.currentUserIsLockingUser = false;
    try {
      final List<LockVO> lockingUsers = new ArrayList<>();
      final String[] activeStates = currentProcessInstance.getActiveStates();
      final List<String> states =
          activeStates != null ? asList(activeStates) : new ArrayList<>(1);
      // special case : instance saved in creation step
      states.add("");
      for (final String stateName : states) {
        LockingUser lockingUser = currentProcessInstance.getLockingUser(stateName);
        if (lockingUser != null) {
          final User user = WorkflowHub.getUserManager().getUser(lockingUser.getUserId());
          final HistoryStep savedStep =
              currentProcessInstance.getSavedStep(lockingUser.getUserId());
          if (savedStep == null || currentRole.equals(savedStep.getUserRoleName())) {
            final boolean isDraftPending = savedStep != null;
            lockingUsers.add(new LockVO(user, lockingUser.getLockDate(), lockingUser.getState(),
                !isDraftPending));
            if (lockingUser.getUserId().equals(getActiveUser().getUserId())) {
              this.currentUserIsLockingUser = true;
            }
          }
        }
      }
      return lockingUsers;
    } catch (WorkflowException e) {
      throw new ProcessManagerException(failureOnGetting("locking", "users"), e);
    }

  }

  /**
   * Lock the current instance for current user and given state
   *
   * @param stateName state name
   */
  public void lock(String stateName) throws ProcessManagerException {
    try {
      State state = processModel.getState(stateName);
      ((UpdatableProcessInstanceManager) Workflow.getProcessInstanceManager())
          .lock(currentProcessInstance, state, getActiveUser());
    } catch (WorkflowException e) {
      throw new ProcessManagerException("Locking failure", e);
    }
  }

  /**
   * Un-Lock the current instance for given user and given state
   *
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
      throw new ProcessManagerException("Unlocking failure", e);
    }
  }

  /**
   * Un-Lock the current instance for current user and given state
   *
   * @param stateName state name
   */
  public void unlock(String stateName) throws ProcessManagerException {
    try {
      State state = processModel.getState(stateName);
      ((UpdatableProcessInstanceManager) Workflow.getProcessInstanceManager())
          .unlock(currentProcessInstance, state, getActiveUser());
    } catch (WorkflowException e) {
      throw new ProcessManagerException("Unlocking failure", e);
    }
  }

  public List<StepVO> getSteps(String strEnlightedStep) throws ProcessManagerException {
    List<StepVO> stepsVO = new ArrayList<>();

    // get step from last recent to older
    HistoryStep[] steps = getSortedHistorySteps(false);

    strEnlightedStep = (strEnlightedStep == null) ? steps[0].getId() : strEnlightedStep;
    for (HistoryStep step : steps) {

      StepVO stepVO = new StepVO();

      stepVO.setStepId(step.getId());

      // Activity
      String activity = getStepActivity(step);
      stepVO.setActivity(activity);

      // Actor Full Name
      stepVO.setActorFullName(getStepActor(step));
      stepVO.setSubstituteFullName(getStepSubstitute(step));

      // Action name
      stepVO.setActionName(getStepAction(step));

      // Step date
      stepVO.setStepDate(DateUtil.getOutputDateAndHour(step.getActionDate(), getLanguage()));

      // visibility
      stepVO.setVisible(isStepVisible(step));

      // Step data
      if ("all".equalsIgnoreCase(strEnlightedStep) || strEnlightedStep.equals(step.getId())) {
        try {
          // Form
          Form form = getStepForm(step);

          // Context
          PagesContext context = new PagesContext("dummy", "0", getLanguage(), false,
              getComponentId(), getUserId());
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

  private String getStepActivity(final HistoryStep step) {
    String activity = "";
    if (step.getResolvedState() != null) {
      State resolvedState = processModel.getState(step.getResolvedState());
      if (resolvedState != null) {
        activity = resolvedState.getLabel(currentRole, getLanguage());
      }
    }
    return activity;
  }

  public HistoryStep[] getSortedHistorySteps(final boolean ascending) {
    HistoryStep[] steps = currentProcessInstance.getHistorySteps();

    // Invert history to get newest history at the beginning
    Arrays.sort(steps, (o1, o2) -> {
      final Integer id1 = Integer.parseInt(o1.getId());
      final Integer id2 = Integer.parseInt(o2.getId());
      if (ascending) {
        return id1.compareTo(id2);
      } else {
        return id2.compareTo(id1);
      }
    });

    return steps;
  }

  private boolean isStepVisible(HistoryStep step) throws ProcessManagerException {
    boolean visible = true;
    if (isHistoryCanBeFiltered()) {
      visible = false;
      String stateName = step.getResolvedState();
      if (stateName != null) {
        if (getActiveUsers(stateName).contains(getActiveUser().getUserId())) {
          visible = true;
        }
      } else {
        // action kind=create
        try {
          Action createAction = processModel.getCreateAction(currentRole);
          QualifiedUsers qualifiedUsers = createAction.getAllowedUsers();
          if (getUsers(qualifiedUsers).contains(getActiveUser().getUserId())) {
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
   *
   * @return an array of string containing actors full name
   */
  public String getStepActor(HistoryStep step) {
    String actorFullName = "##";
    try {
      final User user = step.getUser();
      if (user != null) {
        actorFullName = user.getFullName();
      }
    } catch (WorkflowException we) {
      SilverLogger.getLogger(this).warn(we);
    }
    return actorFullName;
  }

  public String getStepSubstitute(HistoryStep step) {
    String substituteId = step.getSubstituteId();
    if (isDefined(substituteId)) {
      return org.silverpeas.core.admin.user.model.User.getById(substituteId).getDisplayedName();
    }
    return null;
  }

  /**
   * Get the list of actions in History Step of current process instance
   *
   * @return an array of string containing actions names
   */
  public String getStepAction(HistoryStep step) {
    Action action;
    String actionName;

    try {
      if (QUESTION_ACTION.equals(step.getAction())) {
        actionName = getString("processManager.question");
      } else if (RESPONSE_ACTION.equals(step.getAction())) {
        actionName = getString("processManager.response");
      } else if (RE_ASSIGN_ACTION.equals(step.getAction())) {
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
   * Returns the form used to display the i-th step. Returns null if the step is unknown.
   *
   * @throws ProcessManagerException if an error occurs
   */
  public Form getStepForm(HistoryStep step) throws ProcessManagerException {
    try {
      if (QUESTION_ACTION.equals(step.getAction()) || RESPONSE_ACTION.equals(step.getAction())) {
        return getQuestionForm(true);
      } else {
        return processModel.getPresentationForm(step.getAction(), currentRole, getLanguage());
      }
    } catch (WorkflowException e) {

      return null;
    }
  }

  /**
   * Returns the data filled during the given step. Returns null if the step is unknown.
   */
  private DataRecord getStepRecord(HistoryStep step) {
    try {
      final Date actionDate = step.getActionDate();
      if (QUESTION_ACTION.equals(step.getAction())) {
        final Optional<Question> question =
            Arrays.stream(currentProcessInstance.getQuestions()).filter(q -> {
              if (step.getResolvedState().equals(q.getFromState().getName())) {
                final Date questionDate = q.getQuestionDate();
                final long elapsedTimeBetweenActionAndQuestion =
                    questionDate.getTime() - actionDate.getTime();
                return 0 < elapsedTimeBetweenActionAndQuestion &&
                    elapsedTimeBetweenActionAndQuestion < 30000;
              }
              return false;
            }).findFirst();
        return question.<DataRecord>map(q -> new QuestionRecord(q.getQuestionText()))
            .orElse(null);
      } else if (RESPONSE_ACTION.equals(step.getAction())) {
        final Optional<Question> question =
            Arrays.stream(currentProcessInstance.getQuestions()).filter(q -> {
              if (step.getResolvedState().equals(q.getTargetState().getName())) {
                final Date responseDate = q.getResponseDate();
                final long elapsedTimeBetweenActionAndResponse =
                    responseDate != null ? responseDate.getTime() - actionDate.getTime() : -1;
                return 0 < elapsedTimeBetweenActionAndResponse &&
                    elapsedTimeBetweenActionAndResponse < 30000;
              }
              return false;
            }).findFirst();
        return question.<DataRecord>map(q -> new QuestionRecord(q.getResponseText()))
            .orElse(null);
      } else {
        return step.getActionRecord();
      }
    } catch (WorkflowException e) {
      SilverLogger.getLogger(this).silent(e);
      return null;
    }
  }

  /**
   * Get step saved by given user id.
   *
   * @throws ProcessManagerException if an error occurs.
   */
  public HistoryStep getSavedStep() throws ProcessManagerException {
    try {
      return currentProcessInstance.getSavedStep(getActiveUser().getUserId());
    } catch (WorkflowException e) {
      throw new ProcessManagerException(failureOnGetting("saved", "step"), e);
    }
  }

  /**
   * Get step data record saved.
   *
   * @throws ProcessManagerException if an error occurs.
   */
  public DataRecord getSavedStepRecord(HistoryStep savedStep) throws ProcessManagerException {
    try {
      return savedStep.getActionRecord();
    } catch (WorkflowException e) {
      throw new ProcessManagerException(failureOnGetting("saved", "step record"), e);
    }
  }

  /**
   * Get the state with the given name in the specified process instance.
   *
   * @param processInstance the process in which the state is defined
   * @param stateName state name
   * @return the {@link State} instance corresponding to the asked name.
   * @throws ProcessManagerException if the state cannot be got.
   */
  public State getState(ProcessInstance processInstance, String stateName)
      throws ProcessManagerException {
    try {
      return processInstance.getProcessModel().getState(stateName);
    } catch (WorkflowException e) {
      throw new ProcessManagerException(e);
    }
  }

  /**
   * Get the named action
   *
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
   *
   * @return true if there is one or more question
   */
  public boolean hasPendingQuestions() {
    try {
      Task[] tasks = getTasks();
      if (tasks == null) {
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
      throw new ProcessManagerException(failureOnGetting(USER_SETTINGS, "form"), we);
    }
  }

  /**
   * Returns the an empty date record which will be filled with the user settings form.
   */
  public DataRecord getEmptyUserSettingsRecord() throws ProcessManagerException {
    try {
      return processModel.getNewUserInfosRecord(currentRole, getLanguage());
    } catch (WorkflowException we) {
      throw new ProcessManagerException(failureOnGetting(USER_SETTINGS, "form"), we);
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
      throw new ProcessManagerException(failureOnGetting(USER_SETTINGS, "record"), we);
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
      throw new ProcessManagerException("Fail to save user settings", we);
    }
  }

  /**
   * Returns the current ProcessFilter.
   */
  public ProcessFilter getCurrentFilter() throws ProcessManagerException {
    if (currentProcessFilter == null) {
      currentProcessFilter = new ProcessFilter(processModel, currentRole, getLanguage(),
          isProcessIdVisible());
    }
    return currentProcessFilter;
  }

  /**
   * Reset the current ProcessFilter.
   */
  private void resetProcessFilter() throws ProcessManagerException {
    ProcessFilter oldFilter = currentProcessFilter;
    currentProcessFilter = new ProcessFilter(processModel, currentRole, getLanguage(),
        isProcessIdVisible());

    if (oldFilter != null) {
      currentProcessFilter.setCollapse(oldFilter.isCollapse());
      currentProcessFilter.copySharedCriteria(oldFilter);
    }
  }

  public void clearFilter() {
    currentProcessFilter = null;
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
      throw new ProcessManagerException(
          SilverpeasExceptionMessages.failureOnRemoving("process", processId), we);
    }
  }

  private boolean checkUserIsInstanceSupervisor(String processId) throws ProcessManagerException {
    if (SUPERVISOR_ROLE.equalsIgnoreCase(getCurrentRole())) {
      try {
        ProcessInstance processInstance =
            Workflow.getProcessInstanceManager().getProcessInstance(processId);
        List<User> users = processInstance.getUsersInRole(SUPERVISOR_ROLE);
        if (users != null && !users.isEmpty()) {
          for (User user : users) {
            if (user.getUserId().equals(getUserId())) {
              return true;
            }
          }
        }
      } catch (WorkflowException e) {
        throw new ProcessManagerException(e);
      }

    }
    return false;
  }

  /**
   * Get all the errors occurred while processing the current process instance
   */
  public WorkflowError[] getProcessInstanceErrors(String processId) {
    return Workflow.getErrorManager().getErrorsOfInstance(processId);
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

  public boolean isAttachmentTabEnabled() {
    final String param = this.getComponentParameterValue("attachmentTabEnable");
    final boolean decodedParam = !("").equals(param) && !("no").equalsIgnoreCase(param);
    return decodedParam && isActiveUser();
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

  public ExportCSVBuilder exportListAsCSV() throws ProcessManagerException {
    String fieldsToExport = getComponentParameterValue("fieldsToExport");
    ExportCSVBuilder csvBuilder;
    if (isDefined(fieldsToExport)) {
      csvBuilder = exportDefinedItemsAsCSV();
    } else {
      csvBuilder = exportAllFolderAsCSV();
    }
    return csvBuilder;
  }

  private ExportCSVBuilder exportAllFolderAsCSV() throws ProcessManagerException {
    try {
      Item[] items = getFolderItems();
      RecordTemplate listHeaders = getProcessListHeaders();
      FieldTemplate[] headers = listHeaders.getFieldTemplates();

      List<String> csvCols = getCSVCols();

      ExportCSVBuilder csvBuilder = new ExportCSVBuilder();
      boolean processIdVisible = isProcessIdVisible();

      setProcessHeaderToCSVBuilder(csvBuilder, items, processIdVisible, csvCols, headers);

      setProcessListToCSVBuilder(csvBuilder, items, processIdVisible, csvCols);

      return csvBuilder;
    } catch (FormException e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return null;
    }
  }

  private ExportCSVBuilder exportDefinedItemsAsCSV() throws ProcessManagerException {
    try {
      Item[] items = getFolderItems();
      RecordTemplate listHeaders = getProcessListHeaders();
      FieldTemplate[] headers = listHeaders.getFieldTemplates();
      String fieldsToExport = getComponentParameterValue("fieldsToExport");

      List<String> csvCols = new ArrayList<>();
      StringTokenizer tokenizer = new StringTokenizer(fieldsToExport, ";");
      while (tokenizer.hasMoreTokens()) {
        String fieldName = tokenizer.nextToken();
        ItemImpl item = (ItemImpl) getItemByName(items, fieldName);
        if (item != null) {
          csvCols.add(fieldName);
        }
      }

      ExportCSVBuilder csvBuilder = new ExportCSVBuilder();
      boolean processIdVisible = isProcessIdVisible();

      setProcessHeaderToCSVBuilder(csvBuilder, items, processIdVisible, csvCols, headers);

      setProcessListToCSVBuilder(csvBuilder, items, processIdVisible, csvCols);

      return csvBuilder;
    } catch (FormException e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return null;
    }
  }

  private void setProcessHeaderToCSVBuilder(ExportCSVBuilder csvBuilder, Item[] items,
      boolean isProcessIdVisible, List<String> csvCols, FieldTemplate[] headers) {
    CSVRow csvHeader = new CSVRow();

    //Add status (error/timeout/locked) column
    csvHeader.addCell(getString("processManager.status"));
    int indexFrom = 0;
    if (isProcessIdVisible) {
      // add processId column
      csvHeader.addCell(headers[indexFrom].getLabel(getLanguage()));
      indexFrom++;
    }
    // add title column
    csvHeader.addCell(headers[indexFrom++].getLabel(getLanguage()));

    // add state column
    csvHeader.addCell(headers[indexFrom].getLabel(getLanguage()));

    for (String csvCol : csvCols) {
      ItemImpl item = (ItemImpl) getItemByName(items, csvCol);
      if (item != null) {
        csvHeader.addCell(item.getLabel(getCurrentRole(), getLanguage()));
      }
    }
    csvBuilder.setHeader(csvHeader);
  }

  private void setProcessListToCSVBuilder(ExportCSVBuilder csvBuilder, Item[] items,
      boolean isProcessIdVisible, List<String> csvCols) throws ProcessManagerException {
    List<DataRecord> processList = getCurrentProcessList();
    for (final DataRecord aProcessList : processList) {
      ProcessInstanceRowRecord instance = (ProcessInstanceRowRecord) aProcessList;
      if (instance != null) {
        try {
          CSVRow csvRow = new CSVRow();

          // add internal status
          csvRow.addCell(getLabelOfProcessInternalStatus(instance));

          int indexFrom = 0;
          if (isProcessIdVisible) {
            // add process Id
            csvRow.addCell(instance.getField(indexFrom++).getValue(getLanguage()));
          }

          // add title
          csvRow.addCell(instance.getField(indexFrom++).getValue(getLanguage()));

          // add state
          csvRow.addCell(instance.getField(indexFrom).getValue(getLanguage()));

          for (String csvCol : csvCols) {
            csvRow.addCell(getComputedFieldValue(csvCol, instance, items));
          }
          csvBuilder.addLine(csvRow);
        } catch (FormException e) {
          SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
        }
      }
    }
  }

  private String getComputedFieldValue(String fieldName, ProcessInstanceRowRecord instance,
      Item[] items) {
    if (fieldName.startsWith("${")) {
      return DataRecordUtil.applySubstitution(fieldName, instance, "fr");
    }
    return getFieldValue(instance, items, fieldName);
  }

  private String getLabelOfProcessInternalStatus(ProcessInstanceRowRecord instance) {
    if (instance.isInError()) {
      return getString("processManager.inError");
    } else if (instance.isLockedByAdmin()) {
      return getString("processManager.lockedByAdmin");
    } else if (instance.isInTimeout()) {
      return getString("processManager.timeout");
    }
    return "";
  }

  private void updateFolder(final ProcessInstance processInstance,
      final Map<String, String> changes)
      throws FormException, WorkflowException {
    if (!changes.isEmpty()) {
      GenericRecordTemplate rt = new GenericRecordTemplate();
      for (String fieldName : changes.keySet()) {
        GenericFieldTemplate fieldTemplate = new GenericFieldTemplate(fieldName, "user");
        rt.addFieldTemplate(fieldTemplate);
      }
      DataRecord folder = rt.getEmptyRecord();
      for (Map.Entry<String, String> fieldEntry : changes.entrySet()) {
        folder.getField(fieldEntry.getKey()).setStringValue(fieldEntry.getValue());
      }
      processInstance.updateFolder(folder);
    }
  }

  private String getFieldValue(ProcessInstanceRowRecord instance, Item[] items, String fieldName) {
    String encodedFieldValue;
    try {
      Field field = instance.getFullProcessInstance().getField(fieldName);
      encodedFieldValue = field.getValue(getLanguage());
      if (!isDefined(encodedFieldValue) || !field.getTypeName().equals(DateField.TYPE)) {
        ItemImpl item = (ItemImpl) getItemByName(items, fieldName);
        if (item != null) {
          Map<String, String> keyValuePairs = item.getKeyValuePairs();
          if (keyValuePairs != null && !keyValuePairs.isEmpty() && isDefined(encodedFieldValue)) {
            encodedFieldValue = Parameter.decode(encodedFieldValue).stream()
                .map(keyValuePairs::get)
                .collect(Collectors.joining(", "));
          }
        }
      }
    } catch (WorkflowException we) {
      encodedFieldValue = "";
    }
    return encodedFieldValue;
  }

  private List<String> getCSVCols() throws FormException {
    List<String> csvCols = new ArrayList<>();
    Item[] items = getFolderItems();
    RecordTemplate listHeaders = getProcessListHeaders();
    FieldTemplate[] headers = listHeaders.getFieldTemplates();

    for (final FieldTemplate header : headers) {
      String fieldName = header.getFieldName();
      if (!fieldName.equalsIgnoreCase("title") && !fieldName.equalsIgnoreCase("instance.state") && !fieldName.equalsIgnoreCase("instance.id")) {
        csvCols.add(fieldName);
      }
    }

    for (final Item item : items) {
      if (!csvCols.contains(item.getName())) {
        csvCols.add(item.getName());
      }
    }

    return csvCols;
  }

  /**
   * Is current user is part of the locking users for current process instance ?
   *
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

  /**
   * Get the current tokenId. Token Id prevents users to use several windows with same session.
   *
   * @return the current token id
   */
  public String getCurrentTokenId() {
    return currentTokenId;
  }

  /**
   * Set the current tokenId. Token Id prevents users to use several windows with same session.
   *
   * @param newTokenId the current token id
   */
  public void setCurrentTokenId(String newTokenId) {
    this.currentTokenId = newTokenId;
  }

  public int getNbEntriesAboutQuestions() {
    int nbEntries = 0;
    try {
      Task[] tasks = currentProcessInstance != null ? getTasks() : new Task[0];
      for (Task task : tasks) {
        nbEntries = task.getPendingQuestions().length;
        nbEntries += task.getSentQuestions().length;
        nbEntries += task.getRelevantQuestions().length;
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
    return nbEntries;
  }

  /**
   * Gets current replacements at date of day in which the current user can play as a substitute in
   * the workflow.
   *
   * @return a list of possible replacements.
   */
  private List<Replacement<?>> getCurrentUserReplacements() {
    return Replacement.getAllBy(currentUser, peasId)
        .stream()
        .filterCurrentAt(LocalDate.now())
        .filterOnAtLeastOneRole(userRoles)
        .collect(Collectors.toList());
  }

  /**
   * Gets current replacement at date of day in which the current user can play as a substitute
   * according to the given incumbent and the given role.
   *
   * @param incumbentId the identifier of the incumbent the current user can replace.
   * @param role the role the current user and the incumbent must have.
   * @return the optional current replacement.
   */
  private <T extends Replacement<T>> Optional<T> getCurrentUserReplacement(String incumbentId,
      String role) {
    ReplacementList<T> replacements = Replacement.getAllBy(currentUser, peasId);
    return replacements.stream()
        .filterOnIncumbent(incumbentId)
        .filterCurrentAt(LocalDate.now())
        .filterOnAtLeastOneRole(role)
        .findFirst();
  }

  /**
   * Gets the roles of the specified user in the underlying workflow the current user can play as a
   * substitute of him. The current user can play the roles of the specified user only and only if
   * he has the same role in the workflow.
   *
   * @param user a user of the workflow.
   * @return a list of roles.
   */
  private List<String> getSubstituteRolesOf(final User user) {
    final List<String> listOfUserRoles = Stream.of(userRoles)
        .filter(r -> !SUPERVISOR_ROLE.equals(r))
        .collect(Collectors.toList());
    final String[] roles = getOrganisationController().getUserProfiles(user.getUserId(), peasId);
    return Stream.of(roles)
        .filter(listOfUserRoles::contains)
        .collect(Collectors.toList());
  }

  /**
   * Gets among the roles available in the current process, the label of the specified role in the
   * given language.
   *
   * @param replacement a possible replacement for which the role is looking for. Null if the search
   * isn't done in the context of a replacement.
   * @param roles the roles the current process supports.
   * @param roleName the name of a role in the supported roles of the current process.
   * @param lang an ISO-601 code of the language in which the label should be.
   * @return optionally a {@link NamedValue} with as name the name of the role and as value the
   * label of the role. If no role with the specified name is present in the given list of roles,
   * then nothing is returned.
   */
  private Optional<NamedValue> getRoleLabel(final Replacement<?> replacement,
      final Role[] roles,
      final String roleName, final String lang) {
    final List<String> creationRoles = getCreationRoles();
    NamedValue label = null;
    final Function<String, NamedValue> getRoleNamedValue = l -> {
      String rName = roleName;
      String rLabel = l;
      if (replacement != null) {
        rLabel = getMultilang().getStringWithParams("processManager.substituteRoleLabel", l,
            replacement.getIncumbent().getFullName());
        rName = getRoleNameForSubstitute(replacement, roleName);
      }
      return new NamedValue(rName, rLabel, creationRoles.contains(rName));
    };
    if (SUPERVISOR_ROLE.equals(roleName)) {
      label = getRoleNamedValue.apply(getString("processManager.supervisor"));
    } else {
      for (final Role role : roles) {
        if (roleName.equals(role.getName())) {
          label = getRoleNamedValue.apply(role.getLabel(currentRole, lang));
          break;
        }
      }
    }
    return ofNullable(label);
  }

  private String getRoleNameForSubstitute(final Replacement<?> replacement,
      final String roleName) {
    return replacement.getId() + ":" + roleName;
  }

  private List<String> getCreationRoles() {
    final List<String> creationRoles;
    try {
      creationRoles = Stream.of(processModel.getCreationRoles()).collect(Collectors.toList());
    } catch (WorkflowException e) {
      throw new SilverpeasRuntimeException(e);
    }
    return creationRoles;
  }

  /**
   * Gets the current active user. It takes into account any enabled replacement. It such a
   * replacement exists, then the replaced user is returned as the current user is being him.
   *
   * @return the current active user: either the current user behind the session or a replaced user
   * from the enabled replacement.
   */
  private User getActiveUser() {
    User activeUser = currentUser;
    if (currentReplacement != null) {
      activeUser = currentReplacement.getIncumbent();
      // verifying the role, technical security in case an HTTP request is performed manually
      final List<String> incumbentRoles = getSubstituteRolesOf(activeUser);
      if (!incumbentRoles.contains(currentRole)) {
        throw new WebApplicationException(Response.Status.FORBIDDEN);
      }
    }
    return activeUser;
  }

  /**
   * Gets current and next replacements at date of day in which the current user can be replaced in
   * the workflow.
   *
   * @return a list of possible replacements.
   */
  @SuppressWarnings("rawtypes")
  public List<Replacement> getCurrentAndNextUserReplacementsAsIncumbent() {
    return Replacement.getAllOf(currentUser, peasId)
        .stream()
        .filterCurrentAndNextAt(LocalDate.now())
        .filterOnAtLeastOneRole(
            SUPERVISOR_ROLE.equals(currentRole) ? userRoles : new String[]{currentRole})
        .collect(Collectors.toList());
  }

  public String getCurrentRoleLabel() {
    return processModel.getRole(currentRole).getLabel(currentRole, getLanguage());
  }

  /**
   * The session controller saves any fatal exception.
   */
  private ProcessManagerException fatalException = null;
  /**
   * The identifier of the current component instance.
   */
  private String peasId = null;
  /**
   * All the process instance of this work session are built from a same process model: the
   * processModel.
   */
  private ProcessModel processModel = null;
  /**
   * The session saves a current User (workflow user)
   */
  private User currentUser = null;
  /**
   * The session saves a list of user roles.
   */
  private String[] userRoles = null;
  /**
   * The current enabled replacement if any. By default none.
   */
  private Replacement<?> currentReplacement = null;
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
