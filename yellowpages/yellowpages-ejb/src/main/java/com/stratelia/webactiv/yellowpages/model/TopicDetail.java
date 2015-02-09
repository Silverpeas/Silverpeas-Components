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
package com.stratelia.webactiv.yellowpages.model;

import java.util.Collection;

import com.stratelia.webactiv.node.model.NodeDetail;
import com.stratelia.webactiv.node.model.NodePK;

/**
 * This object contains elements which are displayed in a yellowpages Topic
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class TopicDetail implements java.io.Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * A NodeDetail collection representing the path from leaf to root
   */
  private Collection<NodeDetail> path;

  /**
   * the informations of the Topic are in this object
   */
  private NodeDetail nodeDetail;

  /**
   * A NodeDetail collection representing the path from leaf to root
   */
  private Collection<UserContact> contactDetails;

  /**
   * A int collection which contains the number of contact containing under each
   * sub topics of the topics
   */
  private Collection<Integer> nbContactByTopic;

  /**
   * Construct an empty TopicDetail
   */
  public TopicDetail() {
    init(null, null, null, null);
  }

  /**
   * Create a new TopicDetail
   */
  public TopicDetail(Collection<NodeDetail> path, NodeDetail nodeDetail,
      Collection<UserContact> contactDetails, Collection<Integer> nbContactByTopic) {
    init(path, nodeDetail, contactDetails, nbContactByTopic);
  }

  /**
   * Create a new TopicDetail
   */
  private void init(Collection<NodeDetail> path, NodeDetail nodeDetail,
      Collection<UserContact> contactDetails, Collection<Integer> nbContactByTopic) {
    this.path = path;
    this.nodeDetail = nodeDetail;
    this.contactDetails = contactDetails;
    this.nbContactByTopic = nbContactByTopic;
  }

  /**
   * Get the path
   * @return the path
   */
  public Collection<NodeDetail> getPath() {
    return this.path;
  }

  /**
   * Get the Topic nodePK
   * @return the Topic nodePK
   * @see com.stratelia.webactiv.node.model.NodePK
   */
  public NodePK getNodePK() {
    return this.nodeDetail.getNodePK();
  }

  /**
   * Get the detail of this topic
   * @return the detail of this topic
   * @see com.stratelia.webactiv.node.model.NodeDetail
   */
  public NodeDetail getNodeDetail() {
    return this.nodeDetail;
  }

  /**
   * Get a ContactDetail collection containing all the contacts in this topic
   * @return a ContactDetail collection containing all the contacts in this
   * topic
   * @see com.stratelia.webactiv.contact.model.ContactDetail
   * @see java.util.Collection
   */
  public Collection<UserContact> getContactDetails() {
    return this.contactDetails;
  }

  /**
   * Get a int collection containing the number of contacts of each sub topics
   * @return a int collection containing the number of contacts of each sub
   * topics
   * @see java.util.Collection
   */
  public Collection<Integer> getNbContactByTopic() {
    return this.nbContactByTopic;
  }

  /**
   * Set the path
   * @param path a NodeDetail Collection
   */
  public void setPath(Collection<NodeDetail> path) {
    this.path = path;
  }

  /**
   * Set the detail of this topic
   * @param nd the topic NodeDetail
   */
  public void setNodeDetail(NodeDetail nd) {
    this.nodeDetail = nd;
  }

  /**
   * Set the contact details of each contact containing in this topic
   * @param pd a ContactDetail Collection
   */
  public void setContactDetails(Collection<UserContact> pd) {
    this.contactDetails = pd;
  }

  /**
   * Set the number of contacts in each sub topics
   * @param nbContactByTopic a int Collection
   */
  public void setNbContactByTopic(Collection<Integer> nbContactByTopic) {
    this.nbContactByTopic = nbContactByTopic;
  }
}