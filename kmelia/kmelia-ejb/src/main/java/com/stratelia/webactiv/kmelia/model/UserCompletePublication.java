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

package com.stratelia.webactiv.kmelia.model;

import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.publication.model.CompletePublication;

/**
 * This object contains elements which are displayed in a kmelia Topic
 * @author Nicolas Eysseric
 * @version 1.0
 * @deprecated this class is now deprecated. Please use instead the KmeliaPublication objects.
 */
public class UserCompletePublication extends Object implements java.io.Serializable {
  private static final long serialVersionUID = 3822837913809466706L;

  /**  */
  private UserDetail owner;

  /**  */
  private CompletePublication publication;

  private boolean isAlias = false;

  /**
   * Construct an empty TopicDetail
   * @since 1.0
   */
  public UserCompletePublication() {
    init(null, null);
  }

  /**
   * Create a new TopicDetail
   * @since 1.0
   */
  public UserCompletePublication(UserDetail owner,
      CompletePublication publication) {
    init(owner, publication);
  }

  /**
   * Create a new TopicDetail
   * @since 1.0
   */
  private void init(UserDetail owner, CompletePublication publication) {
    this.owner = owner;
    this.publication = publication;
  }

  /**
   * Get the path
   * @return the path
   * @since 1.0
   */
  public UserDetail getOwner() {
    return this.owner;
  }

  /**
   * Get the Topic nodePK
   * @return the Topic nodePK
   * @see com.stratelia.webactiv.node.model.NodePK
   * @since 1.0
   */
  public CompletePublication getPublication() {
    return this.publication;
  }

  /**
   * Set the path
   * @param path a NodeDetail Collection
   * @since 1.0
   */
  public void setOwner(UserDetail ud) {
    this.owner = ud;
  }

  /**
   * Set the detail of this topic
   * @param nd the topic NodeDetail
   * @since 1.0
   */
  public void setPublication(CompletePublication pub) {
    this.publication = pub;
  }

  public String getId() {
    return this.publication.getPublicationDetail().getPK().getId();
  }

  public boolean isAlias() {
    return isAlias;
  }

  public void setAlias(boolean isAlias) {
    this.isAlias = isAlias;
  }
}