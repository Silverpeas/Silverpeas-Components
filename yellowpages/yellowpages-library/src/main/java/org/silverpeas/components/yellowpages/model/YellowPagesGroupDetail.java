/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.yellowpages.model;

import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.model.UserDetail;

import java.util.ArrayList;
import java.util.List;

public class YellowPagesGroupDetail extends GroupDetail implements java.io.Serializable {

  private static final long serialVersionUID = 1L;

  private List<UserDetail> users = new ArrayList<>();
  private List<Group> subGroups = new ArrayList<>();

  public YellowPagesGroupDetail(Group group) {
    super((GroupDetail) group);
  }

  public void addUser(UserDetail user) {
    users.add(user);
  }

  public void addSubGroup(YellowPagesGroupDetail group) {
    subGroups.add(group);
  }

  @Override
  public List<Group> getSubGroups() {
    return subGroups;
  }

  public List<UserDetail> getUsers() {
    return users;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof YellowPagesGroupDetail) {
      YellowPagesGroupDetail anotherGroup = (YellowPagesGroupDetail) o;
      if (this.getId() != null) {
        return this.getId().equals(anotherGroup.getId());
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = super.hashCode();
    hash = 47 * hash + (this.users != null ? this.users.hashCode() : 0);
    hash = 47 * hash + (this.subGroups != null ? this.subGroups.hashCode() : 0);
    return hash;
  }

  public int getTotalUsers() {
    return super.getTotalNbUsers();
  }

}