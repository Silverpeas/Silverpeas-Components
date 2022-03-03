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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.forums.model;

import org.silverpeas.components.forums.ForumsContentManager;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentInterface;
import org.silverpeas.core.i18n.AbstractBean;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.Serializable;
import java.util.Date;

/**
 * This object contains the description of a forum
 * @author Marc Guillemin
 */
public class ForumDetail extends AbstractBean
    implements SilverContentInterface, Serializable {

  private static final long serialVersionUID = -5500661559879178630L;
  private static final String TYPE = "forum";
  private ForumPK pk;
  private Date creationDate;
  private String creatorId;
  // added for the components - PDC integration
  private String silverObjectId;
  private String iconUrl;

  public ForumDetail(ForumPK pk, String name, String description,
      String creatorId, Date creationDate) {
    this.pk = pk;
    setName(name);
    setDescription(description);
    this.creatorId = creatorId;
    this.creationDate = creationDate;
  }

  public ForumPK getPK() {
    return pk;
  }

  public void setCreatorId(String creatorId) {
    this.creatorId = creatorId;
  }

  @Override
  public String getCreatorId() {
    return creatorId;
  }

  @Override
  public Date getCreationDate() {
    return creationDate;
  }

  public String toString() {
    String result = "ForumDetail {" + "\n";

    result = result + "  getPK().getId() = " + getPK().getId() + "\n";
    result = result + "  getPK().getComponent() = "
        + getPK().getComponentName() + "\n";
    result = result + "  getName() = " + getName() + "\n";
    result = result + "  getDescription() = " + getDescription() + "\n";
    result = result + "  getCreatorId() = " + getCreatorId() + "\n";
    result = result + "  getCreationDate() = " + getCreationDate() + "\n";
    result = result + "  getSilverObjectId()  = " + getSilverObjectId() + "\n";
    result = result + "}";
    return result;
  }

  public void setSilverObjectId(String silverObjectId) {
    this.silverObjectId = silverObjectId;
  }

  public void setSilverObjectId(int silverObjectId) {
    this.silverObjectId = Integer.toString(silverObjectId);
  }

  public String getSilverObjectId() {
    return this.silverObjectId;
  }

  // methods to be implemented by SilverContentInterface

  @Override
  public String getURL() {
    return "searchResult?Type=Forum&Id=" + getId();
  }

  @Override
  public String getId() {
    return getPK().getId();
  }

  @Override
  public String getInstanceId() {
    return getPK().getComponentName();
  }

  @Override
  public String getDate() {
    String formattedDate = null;
    try {
      formattedDate = DateUtil.formatDate(getCreationDate());
    } catch (Exception e) {
      SilverLogger.getLogger(this).warn(e);
    }
    return formattedDate;
  }

  public void setIconUrl(String iconUrl) {
    this.iconUrl = iconUrl;
  }

  @Override
  public String getIconUrl() {
    return this.iconUrl;
  }

  @Override
  public String getTitle() {
    return getName();
  }

  @Override
  public String getSilverCreationDate() {
    return getDate();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ForumDetail that = (ForumDetail) o;

    if (creationDate != null ? !creationDate.equals(
        that.creationDate) : that.creationDate != null) {
      return false;
    }
    if (creatorId != null ? !creatorId.equals(that.creatorId) : that.creatorId != null) {
      return false;
    }
    if (getDescription() != null ? !getDescription().equals(that.getDescription()) :
        that.getDescription() != null) {
      return false;
    }
    if (iconUrl != null ? !iconUrl.equals(that.iconUrl) : that.iconUrl != null) {
      return false;
    }
    if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) {
      return false;
    }
    if (pk != null ? !pk.equals(that.pk) : that.pk != null) {
      return false;
    }
    return silverObjectId != null ? silverObjectId.equals(that.silverObjectId) : that.silverObjectId == null;
  }

  @Override
  public int hashCode() {
    int result = pk != null ? pk.hashCode() : 0;
    result = 31 * result + (getName() != null ? getName().hashCode() : 0);
    result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
    result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
    result = 31 * result + (creatorId != null ? creatorId.hashCode() : 0);
    result = 31 * result + (silverObjectId != null ? silverObjectId.hashCode() : 0);
    result = 31 * result + (iconUrl != null ? iconUrl.hashCode() : 0);
    return result;
  }

  @Override
  public String getContributionType() {
    return TYPE;
  }

  @Override
  public String getSilverpeasContentId() {
    if (this.silverObjectId == null) {
      ForumsContentManager contentManager = ServiceProvider.getService(ForumsContentManager.class);
      int objectId = contentManager.getSilverContentId(getId(), getComponentInstanceId());
      if (objectId >= 0) {
        this.silverObjectId = String.valueOf(objectId);
      }
    }
    return this.silverObjectId;
  }
}