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

import java.util.Date;

import com.silverpeas.workflow.api.user.User;

/**
 * A LockVO represents a lock let by a user.
 * @author Ludovic Bertin
 */
public class LockVO {
  private User user = null;
  private Date lockDate = null;
  private boolean removableBySupervisor = false;
  private String state = null;

  public LockVO(User user, Date lockDate, String state, boolean removableBySupervisor) {
    super();
    this.user = user;
    this.lockDate = lockDate;
    this.state = state;
    this.removableBySupervisor = removableBySupervisor;
  }

  /**
   * @return the user
   */
  public User getUser() {
    return user;
  }

  /**
   * @param user the user to set
   */
  public void setUser(User user) {
    this.user = user;
  }

  /**
   * @return the lockDate
   */
  public Date getLockDate() {
    return lockDate;
  }

  /**
   * @param lockDate the lockDate to set
   */
  public void setLockDate(Date lockDate) {
    this.lockDate = lockDate;
  }

  /**
   * @return the removableBySupervisor
   */
  public boolean isRemovableBySupervisor() {
    return removableBySupervisor;
  }

  /**
   * @param removableBySupervisor the removableBySupervisor to set
   */
  public void setRemovableBySupervisor(boolean removableBySupervisor) {
    this.removableBySupervisor = removableBySupervisor;
  }

  /**
   * @return the state
   */
  public String getState() {
    return state;
  }

  /**
   * @param state the state to set
   */
  public void setState(String state) {
    this.state = state;
  }

}
