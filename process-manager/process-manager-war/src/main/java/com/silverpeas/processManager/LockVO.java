package com.silverpeas.processManager;

import java.util.Date;

import com.silverpeas.workflow.api.user.User;

/**
 * A LockVO represents a lock let by a user.
 *
 * @author Ludovic Bertin
 *
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
