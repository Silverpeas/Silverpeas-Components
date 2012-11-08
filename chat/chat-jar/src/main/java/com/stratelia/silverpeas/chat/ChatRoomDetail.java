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
 * FLOSS exception.  You should have received a copy of the text describing
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

/*
 * ChatRoomDetail.java
 *
 * Created on 22/02/2002 by Ludo
 */

package com.stratelia.silverpeas.chat;

import java.io.Serializable;
import java.util.Iterator;

import com.silverpeas.util.i18n.AbstractI18NBean;
import com.stratelia.silverpeas.contentManager.*;

public class ChatRoomDetail extends AbstractI18NBean implements SilverContentInterface,
    Serializable {
  private String instanceId = null;
  private String id = null;
  private String name = null;
  private String comment = null;
  private String iconUrl = "chatSmall.gif";
  private String creatorId = null;

  // constructor
  public ChatRoomDetail(String instanceId, String id, String name,
      String comment, String creatorId) {
    this.instanceId = instanceId;
    this.id = id;
    this.name = name;
    this.comment = comment;
    this.creatorId = creatorId;
  }

  // methods to be implemented by SilverContentInterface

  public String getName() {
    return name;
  }

  public String getDescription() {
    return comment;
  }

  public String getURL() {
    return "searchResult?Id=" + getId();
  }

  public String getId() {
    return id;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public String getDate() {
    return null;
  }

  public String getCreatorId() {
    return creatorId;
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
    return null;
  }

  /**
   * @param string
   */
  public void setId(String string) {
    id = string;
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