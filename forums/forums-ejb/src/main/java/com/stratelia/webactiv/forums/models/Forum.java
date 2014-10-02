/**
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
package com.stratelia.webactiv.forums.models;

import java.io.Serializable;

import com.silverpeas.notation.ejb.RatingServiceProvider;
import org.silverpeas.rating.Rateable;
import org.silverpeas.rating.ContributionRating;
import org.silverpeas.rating.ContributionRatingPK;

public class Forum implements Rateable, Serializable {

  public static final String RESOURCE_TYPE = "Forum";
  private static final long serialVersionUID = -2933341803291325081L;
  private int id;
  private String name;
  private String description;
  private boolean active;
  private int parentId;
  private String category;
  private String creationDate;
  private String instanceId;
  private ForumPK pk;
  private ContributionRating contributionRating;

  public Forum(int id, String name, String description, boolean active, int parentId, String category) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.active = active;
    this.parentId = parentId;
    this.category = category;
  }

  public Forum(int id, String name, String description, boolean active,
      int parentId, String category, String creationDate, String instanceId) {
    this(id, name, description, active, parentId, category);
    this.instanceId = instanceId;
    this.creationDate = creationDate;
    this.pk = new ForumPK(instanceId, String.valueOf(id));
  }

  public int getId() {
    return id;
  }

  public String getIdAsString() {
    return String.valueOf(id);
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public int getParentId() {
    return parentId;
  }

  public String getParentIdAsString() {
    return String.valueOf(parentId);
  }

  public void setParentId(int parentId) {
    this.parentId = parentId;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(String creationDate) {
    this.creationDate = creationDate;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public ForumPK getPk() {
    return pk;
  }

  public void setPk(ForumPK pk) {
    this.pk = pk;
  }

  public boolean isRoot() {
    return parentId == 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Forum forum = (Forum) o;

    if (active != forum.active) {
      return false;
    }
    if (id != forum.id) {
      return false;
    }
    if (parentId != forum.parentId) {
      return false;
    }
    if (category != null ? !category.equals(forum.category) : forum.category != null) {
      return false;
    }
    if (creationDate != null ? !creationDate.equals(
        forum.creationDate) : forum.creationDate != null) {
      return false;
    }
    if (description != null ? !description.equals(forum.description) : forum.description != null) {
      return false;
    }
    if (instanceId != null ? !instanceId.equals(forum.instanceId) : forum.instanceId != null) {
      return false;
    }
    if (name != null ? !name.equals(forum.name) : forum.name != null) {
      return false;
    }
    if (pk != null ? !pk.equals(forum.pk) : forum.pk != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = id;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (active ? 1 : 0);
    result = 31 * result + parentId;
    result = 31 * result + (category != null ? category.hashCode() : 0);
    result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
    result = 31 * result + (instanceId != null ? instanceId.hashCode() : 0);
    result = 31 * result + (pk != null ? pk.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Forum{" + "id=" + id + ", name=" + name + ", description=" + description + ", active=" + active + ", parentId=" + parentId + ", category=" + category + ", creationDate=" + creationDate + ", instanceId=" + instanceId + ", pk=" + pk + '}';
  }

  @Override
  public ContributionRating getRating() {
    if (contributionRating == null) {
      contributionRating = RatingServiceProvider.getRatingService()
          .getRating(new ContributionRatingPK(String.valueOf(id), getInstanceId(), RESOURCE_TYPE));
    }
    return contributionRating;
  }
}
