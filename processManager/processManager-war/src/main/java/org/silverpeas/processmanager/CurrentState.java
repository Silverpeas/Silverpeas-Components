package org.silverpeas.processmanager;

import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.workflow.api.instance.HistoryStep;
import org.silverpeas.core.workflow.api.model.Action;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.task.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CurrentState {

  private final State state;
  private String label;
  private String workingUsers;
  private List<Task> tasks;

  public CurrentState(State state) {
    this.state = state;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(final String label) {
    this.label = label;
  }

  public String getWorkingUsersAsString() {
    return workingUsers;
  }

  public void setWorkingUsersAsString(final String workingUsers) {
    this.workingUsers = workingUsers;
  }

  public Action[] getActions() {
    return state.getFilteredActions();
  }

  public String getName() {
    return state.getName();
  }

  public List<HistoryStep> getBackSteps() {
    if (CollectionUtil.isEmpty(tasks)) {
      return Collections.emptyList();
    }

    List<HistoryStep> steps = new ArrayList<>();
    for (Task task : tasks) {
      HistoryStep[] aSteps = task.getBackSteps();
      steps.addAll(CollectionUtil.asList(aSteps));
    }
    return steps;
  }

  public void setTasks(final List<Task> tasks) {
    this.tasks = tasks;
  }
}
