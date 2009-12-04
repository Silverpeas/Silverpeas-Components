/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.forums.forumEntity.ejb;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;

import com.silverpeas.util.i18n.AbstractI18NBean;
import com.stratelia.silverpeas.silvertrace.*;
import com.stratelia.silverpeas.contentManager.*;

/**
 * This object contains the description of a forum
 * @author Marc Guillemin
 * @version 1.0
 */
public class ForumDetail extends AbstractI18NBean implements SilverContentInterface, Serializable {
  private ForumPK pk;
  private String name;
  private String description;
  private Date creationDate;
  private String creatorId;
  private String silverObjectId; // added for the components - PDC integration
  private String iconUrl;

  public ForumDetail(ForumPK pk, String name, String description,
      String creatorId, Date creationDate) {
    this.pk = pk;
    this.name = name;
    this.description = description;
    this.creatorId = creatorId;
    this.creationDate = creationDate;
  }

  public ForumPK getPK() {
    return pk;
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
    result = result + "  getPK().getDomain() = " + getPK().getDomain() + "\n";
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
    this.silverObjectId = new Integer(silverObjectId).toString();
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
    java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(
        "yyyy/MM/dd");
    String formattedDate = null;

    try {
      formattedDate = formatter.format(getCreationDate());
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

  public String getDescription(String language) {
    return getDescription();
  }

  public String getName(String language) {
    return getName();
  }

  public Iterator getLanguages() {
    return null;
  }
}