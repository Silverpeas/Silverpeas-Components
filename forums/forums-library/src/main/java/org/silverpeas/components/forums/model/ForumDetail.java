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
package org.silverpeas.components.forums.model;

import org.silverpeas.core.Identifiable;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.LocalizedContribution;
import org.silverpeas.core.i18n.AbstractBean;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * This object contains the description of a forum
 *
 * @author Marc Guillemin
 */
public class ForumDetail extends AbstractBean
    implements LocalizedContribution, Identifiable, Serializable {

  private static final long serialVersionUID = -5500661559879178630L;
  private static final String TYPE = Forum.RESOURCE_TYPE;
  private final ForumPK pk;
  private final Date creationDate;
  private final String creatorId;

  public ForumDetail(ForumPK pk, String name, String description, String creatorId,
      Date creationDate) {
    this.pk = pk;
    setName(name);
    setDescription(description);
    this.creatorId = creatorId;
    this.creationDate = creationDate;
  }

  public ForumPK getPK() {
    return pk;
  }

  public String getCreatorId() {
    return creatorId;
  }

  @Override
  public Date getCreationDate() {
    return creationDate;
  }

  @Override
  public Date getLastUpdateDate() {
    return getCreationDate();
  }

  @Override
  public User getCreator() {
    return User.getById(getCreatorId());
  }

  @Override
  public User getLastUpdater() {
    return getCreator();
  }

  public String toString() {
    String result = "ForumDetail {" + "\n";

    result = result + "  getPK().getId() = " + getPK().getId() + "\n";
    result = result + "  getPK().getComponent() = " + getPK().getComponentName() + "\n";
    result = result + "  getName() = " + getName() + "\n";
    result = result + "  getDescription() = " + getDescription() + "\n";
    result = result + "  getCreatorId() = " + getCreatorId() + "\n";
    result = result + "  getCreationDate() = " + getCreationDate() + "\n";
    result = result + "}";
    return result;
  }

  @Override
  public String getId() {
    return getPK().getId();
  }

  public String getInstanceId() {
    return getPK().getComponentName();
  }

  @Override
  public ContributionIdentifier getIdentifier() {
    return ContributionIdentifier.from(getInstanceId(), getId(), getContributionType());
  }

  @Override
  public String getTitle() {
    return getName();
  }

  @Override
  public String getContributionType() {
    return TYPE;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final ForumDetail that = (ForumDetail) o;
    return Objects.equals(pk, that.pk) && Objects.equals(creationDate, that.creationDate) &&
        Objects.equals(creatorId, that.creatorId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pk, creationDate, creatorId);
  }
}
