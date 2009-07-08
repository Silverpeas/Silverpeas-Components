package com.stratelia.webactiv.yellowpages.model;

import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.contact.model.ContactDetail;

/**
 * This object contains elements which are displayed in a yellowpages Topic
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class UserContact extends Object implements java.io.Serializable {

  /**  */
  private UserDetail owner;

  /**  */
  private ContactDetail contactDetail;

  /**
    * Construct an empty TopicDetail
    * @since 1.0
    */
  public UserContact(){
        init(null, null);
  }
  
  /**
    * Create a new TopicDetail
    * @since 1.0
    */
  public UserContact(UserDetail owner, ContactDetail contactDetail) {
        init(owner, contactDetail);
  }

  /**
    * Create a new TopicDetail
    * @since 1.0
    */
  private void init(UserDetail owner, ContactDetail contactDetail) {
        this.owner = owner;
        this.contactDetail = contactDetail;
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
  public ContactDetail getContact() {
        return this.contactDetail;
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
  public void setContact(ContactDetail pub) {
        this.contactDetail = pub;
  }
}