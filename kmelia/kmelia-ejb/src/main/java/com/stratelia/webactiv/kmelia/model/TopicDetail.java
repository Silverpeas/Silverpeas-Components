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
package com.stratelia.webactiv.kmelia.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

/**
 * This object contains elements which are displayed in a kmelia Topic
 * 
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class TopicDetail extends Object implements java.io.Serializable {

  /** A NodeDetail collection representing the path from leaf to root */
  private Collection path;

  /** the informations of the Topic are in this object */
  private NodeDetail nodeDetail;

  /** A NodeDetail collection representing the path from leaf to root */
  private Collection publicationDetails;

  /**
   * A int collection which contains the number of publication containing under
   * each sub topics of the topics
   */
  private Collection nbPubByTopic;

  /**
   * Construct an empty TopicDetail
   * 
   * @since 1.0
   */
  public TopicDetail() {
    init(null, null, null, null);
  }

  /**
   * Create a new TopicDetail
   * 
   * @since 1.0
   */
  public TopicDetail(Collection path, NodeDetail nodeDetail,
      Collection pubDetails, Collection nbPubByTopic) {
    init(path, nodeDetail, pubDetails, nbPubByTopic);
  }

  /**
   * Create a new TopicDetail
   * 
   * @since 1.0
   */
  private void init(Collection path, NodeDetail nodeDetail,
      Collection pubDetails, Collection nbPubByTopic) {
    this.path = path;
    this.nodeDetail = nodeDetail;
    this.publicationDetails = pubDetails;
    this.nbPubByTopic = nbPubByTopic;
  }

  /**
   * Get the path
   * 
   * @return the path
   * @since 1.0
   */
  public Collection getPath() {
    return this.path;
  }

  /**
   * Get the Topic nodePK
   * 
   * @return the Topic nodePK
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @since 1.0
   */
  public NodePK getNodePK() {
    return this.nodeDetail.getNodePK();
  }

  /**
   * Get the detail of this topic
   * 
   * @return the detail of this topic
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  public NodeDetail getNodeDetail() {
    return this.nodeDetail;
  }

  /**
   * Get a PublicationDetail collection containing all the publications in this
   * topic
   * 
   * @return a PublicationDetail collection containing all the publications in
   *         this topic
   * @see com.stratelia.webactiv.util.publication.model.PublicationDetail
   * @see java.util.Collection
   * @since 1.0
   */
  public Collection getPublicationDetails() {
    return this.publicationDetails;
  }

  /**
   * Get a int collection containing the number of publications of each sub
   * topics
   * 
   * @return a int collection containing the number of publications of each sub
   *         topics
   * @see java.util.Collection
   * @since 1.0
   */
  public Collection getNbPubByTopic() {
    return this.nbPubByTopic;
  }

  /**
   * Set the path
   * 
   * @param path
   *          a NodeDetail Collection
   * @since 1.0
   */
  public void setPath(Collection path) {
    this.path = path;
  }

  /**
   * Set the detail of this topic
   * 
   * @param nd
   *          the topic NodeDetail
   * @since 1.0
   */
  public void setNodeDetail(NodeDetail nd) {
    this.nodeDetail = nd;
  }

  /**
   * Set the publication details of each publication containing in this topic
   * 
   * @param pd
   *          a PublicationDetail Collection
   * @since 1.0
   */
  public void setPublicationDetails(Collection pd) {
    this.publicationDetails = pd;
  }

  /**
   * Set the number of publications in each sub topics
   * 
   * @param nbPubByTopic
   *          a int Collection
   * @since 1.0
   */
  public void setNbPubByTopic(Collection nbPubByTopic) {
    this.nbPubByTopic = nbPubByTopic;
  }

  public List getValidPublications() {
    return getValidPublications(null);
  }

  public List getValidPublications(PublicationPK pubPKToExclude) {
    UserPublication userPub;
    PublicationDetail pub;
    List validPublications = new ArrayList();
    Iterator iterator = publicationDetails.iterator();
    while (iterator.hasNext()) {
      userPub = (UserPublication) iterator.next();
      pub = userPub.getPublication();
      if (pub.getStatus() != null && pub.getStatus().equals("Valid")) {
        if (pubPKToExclude == null)
          validPublications.add(userPub);
        else {
          if (!pub.getPK().equals(pubPKToExclude))
            validPublications.add(userPub);
        }
      }
    }
    return validPublications;
  }
}