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

public class ChatRoomDetail extends AbstractI18NBean implements
    SilverContentInterface, Serializable {
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