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

import com.silverpeas.SilverpeasContent;
import com.silverpeas.accesscontrol.AccessController;
import com.silverpeas.accesscontrol.AccessControllerProvider;
import org.silverpeas.util.i18n.AbstractBean;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.forums.ForumsContentManager;
import com.stratelia.webactiv.util.DateUtil;

import java.io.Serializable;
import java.util.Date;

/**
 * This object contains the description of a forum
 * @author Marc Guillemin
 * @version 1.0
 */
public class ForumDetail extends AbstractBean
    implements SilverContentInterface, Serializable, SilverpeasContent {

  private static final long serialVersionUID = -5500661559879178630L;
  private static final String TYPE = "forum";  
  private ForumPK pk;
  private Date creationDate;
  private String creatorId;
  private String silverObjectId; // added for the components - PDC integration
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

  public String getCreatorId() {
    return creatorId;
  }

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

  public String getURL() {
    return "searchResult?Type=Forum&Id=" + getId();
  }

  public String getId() {
    return getPK().getId();
  }

  public String getInstanceId() {
    return getPK().getComponentName();
  }

  public String getDate() {
    String formattedDate = null;
    try {
      formattedDate = DateUtil.formatDate(getCreationDate());
    } catch (Exception e) {
      SilverTrace.warn("publication", "ForumDetail.getDate()",
          "root.MSG_GEN_ENTER_METHOD", "date to format = "
          + getCreationDate().toString());
    }
    return formattedDate;
  }

  public void setIconUrl(String iconUrl) {
    this.iconUrl = iconUrl;
  }

  public String getIconUrl() {
    return this.iconUrl;
  }

  public String getTitle() {
    return getName();
  }

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
    if (silverObjectId != null ? !silverObjectId.equals(
        that.silverObjectId) : that.silverObjectId != null) {
      return false;
    }

    return true;
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
  public String getComponentInstanceId() {
    return getPK().getComponentName();
  }

  @Override
  public String getContributionType() {
    return TYPE;
  }

  /**
   * Is the specified user can access this forum?
   * <p/>
   * A user can access a forum if it has enough rights to access the Forums instance in
   * which is managed this forum.
   * @param user a user in Silverpeas.
   * @return true if the user can access this forum, false otherwise.
   */
  @Override
  public boolean canBeAccessedBy(final UserDetail user) {
    AccessController<String> accessController =
        AccessControllerProvider.getAccessController("componentAccessController");
    return accessController.isUserAuthorized(user.getId(), getComponentInstanceId());
  }

  @Override
  public UserDetail getCreator() {
    return UserDetail.getById(this.getCreatorId());
  }

  @Override
  public String getSilverpeasContentId() {
    if (this.silverObjectId == null) {
      ForumsContentManager forumsContentMgr = new ForumsContentManager();
      int objectId = forumsContentMgr.getSilverObjectId(getId(), getComponentInstanceId());
      if (objectId >= 0) {
        this.silverObjectId = String.valueOf(objectId);
      }
    }
    return this.silverObjectId;
  }
}