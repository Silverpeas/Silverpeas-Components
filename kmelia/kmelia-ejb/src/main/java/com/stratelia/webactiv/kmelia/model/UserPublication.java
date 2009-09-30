package com.stratelia.webactiv.kmelia.model;

import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

/**
 * This object contains elements which are displayed in a kmelia Topic
 * 
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class UserPublication extends Object implements java.io.Serializable {

  /**  */
  private UserDetail owner;

  /**  */
  private PublicationDetail publicationDetail;

  /**
   * Construct an empty TopicDetail
   * 
   * @since 1.0
   */
  public UserPublication() {
    init(null, null);
  }

  /**
   * Create a new TopicDetail
   * 
   * @since 1.0
   */
  public UserPublication(UserDetail owner, PublicationDetail publicationDetail) {
    init(owner, publicationDetail);
  }

  /**
   * Create a new TopicDetail
   * 
   * @since 1.0
   */
  private void init(UserDetail owner, PublicationDetail publicationDetail) {
    this.owner = owner;
    this.publicationDetail = publicationDetail;
  }

  /**
   * Get the path
   * 
   * @return the path
   * @since 1.0
   */
  public UserDetail getOwner() {
    return this.owner;
  }

  /**
   * Get the Topic nodePK
   * 
   * @return the Topic nodePK
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @since 1.0
   */
  public PublicationDetail getPublication() {
    return this.publicationDetail;
  }

  /**
   * Set the path
   * 
   * @param path
   *          a NodeDetail Collection
   * @since 1.0
   */
  public void setOwner(UserDetail ud) {
    this.owner = ud;
  }

  /**
   * Set the detail of this topic
   * 
   * @param nd
   *          the topic NodeDetail
   * @since 1.0
   */
  public void setPublication(PublicationDetail pub) {
    this.publicationDetail = pub;
  }

  public boolean equals(Object o) {
    if (o == null)
      return false;
    if (o instanceof UserPublication) {
      UserPublication up = (UserPublication) o;
      return (up.getPublication().getId().equals(this.getPublication().getId()));
    }
    return false;
  }
}