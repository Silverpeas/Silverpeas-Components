package com.stratelia.webactiv.kmelia.model;

import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.publication.model.CompletePublication;

/**
 * This object contains elements which are displayed in a kmelia Topic
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class UserCompletePublication extends Object implements java.io.Serializable {

  /**  */
  private UserDetail owner;

  /**  */
  private CompletePublication publication;
  
  private boolean isAlias = false;  

/**
    * Construct an empty TopicDetail
    * @since 1.0
    */
  public UserCompletePublication(){
        init(null, null);
  }
  
  /**
    * Create a new TopicDetail
    * @since 1.0
    */
  public UserCompletePublication(UserDetail owner, CompletePublication publication) {
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
    * @see com.stratelia.webactiv.util.node.model.NodePK
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
  
  public String getId()
  {
	  return this.publication.getPublicationDetail().getPK().getId();
  }
  
  public boolean isAlias() {
	  return isAlias;
  }

  public void setAlias(boolean isAlias) {
	  this.isAlias = isAlias;
  }
}