/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
package com.stratelia.webactiv.yellowpages.model;

import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.contact.model.CompleteContact;

/**
 * This object contains elements which are displayed in a yellowpages Topic
 * @author Nicolas Eysseric
 */
public class UserCompleteContact implements java.io.Serializable {

  private static final long serialVersionUID = 683024147291151734L;

  /**  */
  private UserDetail owner;

  /**  */
  private CompleteContact contact;

  /**
   * Construct an empty TopicDetail
   */
  public UserCompleteContact() {
    init(null, null);
  }

  /**
   * Create a new TopicDetail
   */
  public UserCompleteContact(UserDetail owner, CompleteContact contact) {
    init(owner, contact);
  }

  /**
   * Create a new TopicDetail
   */
  private void init(UserDetail owner, CompleteContact contact) {
    this.owner = owner;
    this.contact = contact;
  }

  /**
   * Get the path
   * @return the path
   */
  public UserDetail getOwner() {
    return this.owner;
  }

  /**
   * Get the Topic nodePK
   * @return the Topic nodePK
   * @see com.stratelia.webactiv.node.model.NodePK
   */
  public CompleteContact getContact() {
    return this.contact;
  }

  /**
   * Set the owner
   * @param ud the user detail to set
   */
  public void setOwner(UserDetail ud) {
    this.owner = ud;
  }

  /**
   * Set the complete contact
   * @param completeContact the contact to set
   */
  public void setContact(CompleteContact completeContact) {
    this.contact = completeContact;
  }
}