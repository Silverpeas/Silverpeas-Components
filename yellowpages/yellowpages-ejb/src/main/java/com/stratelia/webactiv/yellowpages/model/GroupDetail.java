package com.stratelia.webactiv.yellowpages.model;

import java.util.ArrayList;
import java.util.List;

import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;

public class GroupDetail extends Group implements java.io.Serializable {

  /**
	 * 
	 */
  private static final long serialVersionUID = 1L;

  private int totalUsers = 0;
  private List users = new ArrayList();
  private List subGroups = new ArrayList();

  public GroupDetail(Group group) {
    super(group);
  }

  public void addUser(UserDetail user) {
    users.add(user);
  }

  public void addSubGroup(GroupDetail group) {
    subGroups.add(group);
  }

  public void addSubGroups(Group[] groups) {
    Group group = null;
    for (int g = 0; g < groups.length; g++) {
      group = groups[g];
      addSubGroup(new GroupDetail(group));
    }
  }

  public List getSubGroups() {
    return subGroups;
  }

  public List getUsers() {
    return users;
  }

  public boolean equals(Object o) {
    if (o instanceof GroupDetail) {
      GroupDetail anotherGroup = (GroupDetail) o;
      if (this.getId() != null)
        return this.getId().equals(anotherGroup.getId());
    }
    return false;
  }

  public int getTotalUsers() {
    return totalUsers;
  }

  public void setTotalUsers(int totalUsers) {
    this.totalUsers = totalUsers;
  }

}