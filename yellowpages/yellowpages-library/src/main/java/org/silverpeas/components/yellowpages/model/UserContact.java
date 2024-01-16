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

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contact.model.ContactDetail;
import org.silverpeas.core.node.model.NodePK;

/**
 * This object contains elements which are displayed in a yellowpages Topic
 * @author Nicolas Eysseric
 */
public class UserContact implements java.io.Serializable {

  private static final long serialVersionUID = 7209945723910986590L;

  /**  */
  private UserDetail owner;

  /**  */
  private ContactDetail contactDetail;

  /**
   * Construct an empty TopicDetail
   */
  public UserContact() {
    init(null, null);
  }

  /**
   * Create a new TopicDetail
   */
  public UserContact(UserDetail owner, ContactDetail contactDetail) {
    init(owner, contactDetail);
  }

  /**
   * Create a new TopicDetail
   */
  private void init(UserDetail owner, ContactDetail contactDetail) {
    this.owner = owner;
    this.contactDetail = contactDetail;
  }

  /**
   * Get the owner
   * @return the owner
   */
  public UserDetail getOwner() {
    return this.owner;
  }

  /**
   * Get the Topic nodePK
   * @return the Topic nodePK
   * @see NodePK
   */
  public ContactDetail getContact() {
    return this.contactDetail;
  }

  /**
   * Set the owner
   * @param ud the user detail to set
   */
  public void setOwner(UserDetail ud) {
    this.owner = ud;
  }

  /**
   * Set the contact
   * @param contact the contact to set
   * @since 1.0
   */
  public void setContact(ContactDetail contact) {
    this.contactDetail = contact;
  }
}