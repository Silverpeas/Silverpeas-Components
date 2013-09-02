/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.forums.models;

import com.stratelia.webactiv.beans.admin.UserDetail;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * User: Yohann Chastagnier
 * Date: 17/06/13
 */
public class Moderator {

  private String userId;
  private int forumId;
  boolean byInheritance = false;

  /**
   * Intanciate a moderator object.
   * @param userId
   * @param forumId
   * @return
   */
  public static Moderator from(String userId, int forumId) {
    return new Moderator(userId, forumId);
  }

  /**
   * Default constructor
   * @param userId
   * @param forumId
   */
  private Moderator(String userId, int forumId) {
    this.userId = userId;
    this.forumId = forumId;
  }

  public String getUserId() {
    return userId;
  }

  public UserDetail getUser() {
    return UserDetail.getById(userId);
  }

  public int getForumId() {
    return forumId;
  }

  public void setByInheritance(final boolean byInheritance) {
    this.byInheritance = byInheritance;
  }

  public boolean isByInheritance() {
    return byInheritance;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj.getClass() != getClass()) {
      return false;
    }
    Moderator other = (Moderator) obj;
    EqualsBuilder builder = new EqualsBuilder();
    builder.append(getUserId(), other.getUserId());
    builder.append(getForumId(), other.getForumId());
    builder.append(isByInheritance(), other.isByInheritance());
    return builder.isEquals();
  }

  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();
    builder.append(getUserId());
    builder.append(getForumId());
    builder.append(isByInheritance());
    return builder.toHashCode();
  }
}
