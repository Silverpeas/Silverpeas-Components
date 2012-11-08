package com.stratelia.silverpeas.infoLetter.model;

import java.util.List;

import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;

public class InternalSubscribers {

  private List<UserDetail> users;
  private List<Group> groups;

  /**
   * @param users
   * @param groups
   */
  public InternalSubscribers(List<UserDetail> users, List<Group> groups) {
    super();
    this.users = users;
    this.groups = groups;
  }

  /**
   * @return the users
   */
  public List<UserDetail> getUsers() {
    return users;
  }
  /**
   * @param users the users to set
   */
  public void setUsers(List<UserDetail> users) {
    this.users = users;
  }
  /**
   * @return the groups
   */
  public List<Group> getGroups() {
    return groups;
  }
  /**
   * @param groups the groups to set
   */
  public void setGroups(List<Group> groups) {
    this.groups = groups;
  }
  
  
  
}
