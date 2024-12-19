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
import java.util.Objects;

public class YellowPagesGroupDetail extends GroupDetail implements java.io.Serializable {

  private static final long serialVersionUID = 1L;

  private final List<UserDetail> users = new ArrayList<>();
  private final List<GroupDetail> subGroups = new ArrayList<>();

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
  public List<GroupDetail> getSubGroups() {
    return subGroups;
  }

  @Override
  public List<UserDetail> getUsers() {
    return users;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof YellowPagesGroupDetail) {
      YellowPagesGroupDetail anotherGroup = (YellowPagesGroupDetail) o;
      return getId() != null ? Objects.equals(getId(), anotherGroup.getId()) : super.equals(o);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getId() != null ? Objects.hash(getId()) : super.hashCode();
  }

  public int getTotalUsers() {
    return super.getTotalUsersCount();
  }

}