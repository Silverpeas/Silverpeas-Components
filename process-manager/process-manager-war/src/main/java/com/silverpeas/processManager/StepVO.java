package com.silverpeas.processManager;

import java.util.Date;

public class StepVO {
  String stepId = null;
  String activity = null;
  String actorFullName = null;
  String actionName = null;
  String stepDate = null;
  boolean visible = false;
  HistoryStepContent content = null;

  /**
   * @return the stepId
   */
  public String getStepId() {
    return stepId;
  }

  /**
   * @param stepId the stepId to set
   */
  public void setStepId(String stepId) {
    this.stepId = stepId;
  }

  /**
   * @return the activity
   */
  public String getActivity() {
    return activity;
  }

  /**
   * @return the content
   */
  public HistoryStepContent getContent() {
    return content;
  }

  /**
   * @param content the content to set
   */
  public void setContent(HistoryStepContent content) {
    this.content = content;
  }

  /**
   * @param activity the activity to set
   */
  public void setActivity(String activity) {
    this.activity = activity;
  }

  /**
   * @return the actorFullName
   */
  public String getActorFullName() {
    return actorFullName;
  }

  /**
   * @param actorFullName the actorFullName to set
   */
  public void setActorFullName(String actorFullName) {
    this.actorFullName = actorFullName;
  }

  /**
   * @return the actionName
   */
  public String getActionName() {
    return actionName;
  }

  /**
   * @param actionName the actionName to set
   */
  public void setActionName(String actionName) {
    this.actionName = actionName;
  }

  /**
   * @return the stepDate
   */
  public String getStepDate() {
    return stepDate;
  }

  /**
   * @param stepDate the stepDate to set
   */
  public void setStepDate(String stepDate) {
    this.stepDate = stepDate;
  }

  /**
   * @return the visible
   */
  public boolean isVisible() {
    return visible;
  }

  /**
   * @param visible the visible to set
   */
  public void setVisible(boolean visible) {
    this.visible = visible;
  }

}
