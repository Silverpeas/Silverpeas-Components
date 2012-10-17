/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.yellowpages.model;

import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.util.ArrayList;
import java.util.List;

public class GroupDetail extends Group implements java.io.Serializable {

  private static final long serialVersionUID = 1L;

  private int totalUsers = 0;
  private List<UserDetail> users = new ArrayList<UserDetail>();
  private List<GroupDetail> subGroups = new ArrayList<GroupDetail>();

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
    Group group;
    for (int g = 0; g < groups.length; g++) {
      group = groups[g];
      addSubGroup(new GroupDetail(group));
    }
  }
  
  @Override
  public List<GroupDetail> getSubGroups() {
    return subGroups;
  }

  public List<UserDetail> getUsers() {
    return users;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof GroupDetail) {
      GroupDetail anotherGroup = (GroupDetail) o;
      if (this.getId() != null)
        return this.getId().equals(anotherGroup.getId());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 47 * hash + this.totalUsers;
    hash = 47 * hash + (this.users != null ? this.users.hashCode() : 0);
    hash = 47 * hash + (this.subGroups != null ? this.subGroups.hashCode() : 0);
    return hash;
  }

  public int getTotalUsers() {
    return totalUsers;
  }

  public void setTotalUsers(int totalUsers) {
    this.totalUsers = totalUsers;
  }

}