/*
 * Copyright (C) 2000 - 2014 Silverpeas
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

package com.silverpeas.processManager;

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
