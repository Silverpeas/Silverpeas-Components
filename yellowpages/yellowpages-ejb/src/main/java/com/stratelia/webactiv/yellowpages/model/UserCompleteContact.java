package com.stratelia.webactiv.yellowpages.model;

import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.contact.model.CompleteContact;

/**
 * This object contains elements which are displayed in a yellowpages Topic
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class UserCompleteContact extends Object implements java.io.Serializable {

  /**  */
  private UserDetail owner;

  /**  */
  private CompleteContact contact;

  /**
    * Construct an empty TopicDetail
    * @since 1.0
    */
  public UserCompleteContact(){
        init(null, null);
  }
  
  /**
    * Create a new TopicDetail
    * @since 1.0
    */
  public UserCompleteContact(UserDetail owner, CompleteContact contact) {
        init(owner, contact);
  }

  /**
    * Create a new TopicDetail
    * @since 1.0
    */
  private void init(UserDetail owner, CompleteContact contact) {
        this.owner = owner;
        this.contact = contact;
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
  public CompleteContact getContact() {
        return this.contact;
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
  public void setContact(CompleteContact pub) {
        this.contact = pub;
  }
}