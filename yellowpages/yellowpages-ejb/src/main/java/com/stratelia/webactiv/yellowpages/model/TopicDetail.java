package com.stratelia.webactiv.yellowpages.model;

import java.util.Collection;

import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

/**
 * This object contains elements which are displayed in a yellowpages Topic
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
  private Collection contactDetails;

  /**
   * A int collection which contains the number of contact containing under each
   * sub topics of the topics
   */
  private Collection nbContactByTopic;

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
      Collection contactDetails, Collection nbContactByTopic) {
    init(path, nodeDetail, contactDetails, nbContactByTopic);
  }

  /**
   * Create a new TopicDetail
   * 
   * @since 1.0
   */
  private void init(Collection path, NodeDetail nodeDetail,
      Collection contactDetails, Collection nbContactByTopic) {
    this.path = path;
    this.nodeDetail = nodeDetail;
    this.contactDetails = contactDetails;
    this.nbContactByTopic = nbContactByTopic;
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
   * Get a ContactDetail collection containing all the contacts in this topic
   * 
   * @return a ContactDetail collection containing all the contacts in this
   *         topic
   * @see com.stratelia.webactiv.util.contact.model.ContactDetail
   * @see java.util.Collection
   * @since 1.0
   */
  public Collection getContactDetails() {
    return this.contactDetails;
  }

  /**
   * Get a int collection containing the number of contacts of each sub topics
   * 
   * @return a int collection containing the number of contacts of each sub
   *         topics
   * @see java.util.Collection
   * @since 1.0
   */
  public Collection getNbContactByTopic() {
    return this.nbContactByTopic;
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
   * Set the contact details of each contact containing in this topic
   * 
   * @param pd
   *          a ContactDetail Collection
   * @since 1.0
   */
  public void setContactDetails(Collection pd) {
    this.contactDetails = pd;
  }

  /**
   * Set the number of contacts in each sub topics
   * 
   * @param nbContactByTopic
   *          a int Collection
   * @since 1.0
   */
  public void setNbContactByTopic(Collection nbContactByTopic) {
    this.nbContactByTopic = nbContactByTopic;
  }
}