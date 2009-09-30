package com.stratelia.webactiv.yellowpages.control.ejb;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;

import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.contact.model.ContactDetail;
import com.stratelia.webactiv.util.contact.model.ContactPK;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.yellowpages.model.TopicDetail;
import com.stratelia.webactiv.yellowpages.model.UserCompleteContact;

/**
 * This is the Yellowpages EJB-tier controller of the MVC. It is implemented as
 * a session EJB. It controls all the activities that happen in a client
 * session. It also provides mechanisms to access other session EJBs.
 * 
 * @author Nicolas Eysseric
 */
public interface YellowpagesBmSkeleton {

  /**************************************************************************************/
  /* Interface - Gestion des thèmes */
  /**************************************************************************************/
  /**
   * Set the space Id where the user is logged on
   * 
   * @param space
   *          the space name
   * @since 1.0
   */
  public void setPrefixTableName(String prefixTableName) throws RemoteException;

  public void setComponentId(String componentId) throws RemoteException;

  /**
   * Set the current User ActorDetail
   * 
   * @param userDetail
   *          the UserDetail of the current User
   * @since 1.0
   */
  public void setActor(UserDetail userDetail) throws RemoteException;

  /**************************************************************************************/
  /* Interface - Gestion des thèmes */
  /**************************************************************************************/
  /**
   * Return a the detail of a topic
   * 
   * @param id
   *          the id of the topic
   * @return a TopicDetail
   * @see com.stratelia.webactiv.yellowpages.model.TopicDetail
   * @exception java.rmi.RemoteException
   * @exception javax.ejb.FinderException
   * @exception javax.ejb.CreateException
   * @exception javax.ejb.NamingException
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public TopicDetail goTo(String id) throws RemoteException;

  public List getTree() throws RemoteException;

  /**
   * Add a subtopic to a topic - If a subtopic of same name already exists a
   * NodePK with id=-1 is returned else the new topic NodePK
   * 
   * @param fatherId
   *          the topic Id of the future father
   * @param subTopic
   *          the NodeDetail of the new sub topic
   * @return If a subtopic of same name already exists a NodePK with id=-1 is
   *         returned else the new topic NodePK
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @exception java.rmi.RemoteException
   * @exception javax.ejb.FinderException
   * @exception javax.ejb.CreateException
   * @exception javax.ejb.NamingException
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public NodePK addToTopic(String id, NodeDetail subtopic)
      throws RemoteException;

  /**
   * Add a subtopic to currentTopic and alert users - If a subtopic of same name
   * already exists a NodePK with id=-1 is returned else the new topic NodePK
   * 
   * @param subTopic
   *          the NodeDetail of the new sub topic
   * @param alertType
   *          Alert all users, only publishers or nobody of the topic creation
   *          alertType = "All"|"Publisher"|"None"
   * @return If a subtopic of same name already exists a NodePK with id=-1 is
   *         returned else the new topic NodePK
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @exception java.rmi.RemoteException
   * @exception javax.ejb.FinderException
   * @exception javax.ejb.CreateException
   * @exception javax.ejb.NamingException
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public NodePK addSubTopic(NodeDetail subtopic) throws RemoteException;

  /**
   * Update a subtopic to currentTopic and alert users - If a subtopic of same
   * name already exists a NodePK with id=-1 is returned else the new topic
   * NodePK
   * 
   * @param topic
   *          the NodeDetail of the updated sub topic
   * @param alertType
   *          Alert all users, only publishers or nobody of the topic creation
   *          alertType = "All"|"Publisher"|"None"
   * @return If a subtopic of same name already exists a NodePK with id=-1 is
   *         returned else the new topic NodePK
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @exception java.rmi.RemoteException
   * @exception javax.ejb.FinderException
   * @exception javax.ejb.CreateException
   * @exception javax.ejb.NamingException
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public NodePK updateTopic(NodeDetail topic) throws RemoteException;

  /**
   * Return a subtopic to currentTopic
   * 
   * @param subTopicId
   *          the id of the researched topic
   * @return the detail of the specified topic
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @exception java.rmi.RemoteException
   * @exception javax.ejb.FinderException
   * @exception javax.ejb.CreateException
   * @exception javax.ejb.NamingException
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public NodeDetail getSubTopicDetail(String subTopicId) throws RemoteException;

  /**
   * Delete a topic and all descendants. Delete all links between descendants
   * and contacts. This contacts will be visible in the Declassified zone.
   * Delete All subscriptions and favorites on this topics and all descendants
   * 
   * @param topicId
   *          the id of the topic to delete
   * @exception java.rmi.RemoteException
   * @exception javax.ejb.FinderException
   * @exception javax.ejb.CreateException
   * @exception javax.ejb.NamingException
   * @exception javax.ejb.RemoveException
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public void deleteTopic(String topicId) throws RemoteException;

  public void emptyDZByUserId() throws RemoteException;

  public void emptyBasketByUserId() throws RemoteException;

  /**************************************************************************************/
  /* Interface - Gestion des contacts */
  /**************************************************************************************/
  /**
   * Return the detail of a contact (only the Header)
   * 
   * @param pubId
   *          the id of the contact
   * @return a ContactDetail
   * @see com.stratelia.webactiv.util.contact.model.ContactDetail
   * @exception java.rmi.RemoteException
   * @exception javax.ejb.FinderException
   * @exception javax.ejb.CreateException
   * @exception javax.ejb.NamingException
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public ContactDetail getContactDetail(String pubId) throws RemoteException;

  /**
   * Return list of all path to this contact - it's a Collection of NodeDetail
   * collection
   * 
   * @param pubId
   *          the id of the contact
   * @return a Collection of NodeDetail collection
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @exception java.rmi.RemoteException
   * @exception javax.ejb.FinderException
   * @exception javax.ejb.CreateException
   * @exception javax.ejb.NamingException
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public Collection getPathList(String pubId) throws RemoteException;

  /**
   * Create a new Contact (only the header - parameters) to the current Topic
   * 
   * @param pubDetail
   *          a ContactDetail
   * @return the id of the new contact
   * @see com.stratelia.webactiv.util.contact.model.ContactDetail
   * @exception java.rmi.RemoteException
   * @exception javax.ejb.FinderException
   * @exception javax.ejb.CreateException
   * @exception javax.ejb.NamingException
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public String createContact(ContactDetail pubDetail) throws RemoteException;

  /**
   * Update a contact (only the header - parameters)
   * 
   * @param pubDetail
   *          a ContactDetail
   * @see com.stratelia.webactiv.util.contact.model.ContactDetail
   * @exception java.rmi.RemoteException
   * @exception javax.ejb.FinderException
   * @exception javax.ejb.CreateException
   * @exception javax.ejb.NamingException
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public void updateContact(ContactDetail detail) throws RemoteException;

  /**
   * Delete a contact If this contact is in the basket or in the DZ, it's
   * deleted from the database Else it only send to the basket
   * 
   * @param pubId
   *          the id of the contact to delete
   * @return a TopicDetail
   * @see com.stratelia.webactiv.yellowpages.model.TopicDetail
   * @exception java.rmi.RemoteException
   * @exception javax.ejb.FinderException
   * @exception javax.ejb.RemoveException
   * @exception javax.ejb.CreateException
   * @exception javax.ejb.NamingException
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public void deleteContact(String pubId) throws RemoteException;

  /**
   * Add a contact to a topic and send email alerts to topic subscribers
   * 
   * @param pubId
   *          the id of the contact
   * @param fatherId
   *          the id of the topic
   * @exception java.rmi.RemoteException
   * @exception javax.ejb.FinderException
   * @exception javax.ejb.CreateException
   * @exception javax.ejb.NamingException
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public void addContactToTopic(String pubId, String fatherId)
      throws RemoteException;

  /**
   * Delete a path between contact and topic
   * 
   * @param pubId
   *          the id of the contact
   * @param fatherId
   *          the id of the topic
   * @exception java.rmi.RemoteException
   * @exception javax.ejb.FinderException
   * @exception javax.ejb.CreateException
   * @exception javax.ejb.NamingException
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public void deleteContactFromTopic(String pubId, String fatherId)
      throws RemoteException;

  /**
   * Create model info attached to a contact
   * 
   * @param pubId
   *          the id of the contact
   * @param modelId
   *          the id of the selected model
   * @exception java.rmi.RemoteException
   * @exception javax.ejb.FinderException
   * @exception javax.ejb.CreateException
   * @exception javax.ejb.NamingException
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public void createInfoModel(String pubId, String modelId)
      throws RemoteException;

  /**
   * Return all info of a contact and add a reading statistic
   * 
   * @param pubId
   *          the id of a contact
   * @return a CompleteContact
   * @see com.stratelia.webactiv.util.contact.model.CompleteContact
   * @exception java.rmi.RemoteException
   * @exception javax.ejb.FinderException
   * @exception javax.ejb.CreateException
   * @exception javax.ejb.NamingException
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public UserCompleteContact getCompleteContact(String pubId)
      throws RemoteException;

  /**
   * Return all info of a contact and add a reading statistic
   * 
   * @param ContactId
   *          the id of a contact
   * @param nodeId
   *          the id of the node
   * @return a CompleteContact
   * @see com.stratelia.webactiv.util.contact.model.CompleteContact
   * @exception javax.ejb.FinderException
   * @exception javax.ejb.CreateException
   * @exception javax.ejb.NamingException
   * @exception java.sql.SQLException
   */
  public UserCompleteContact getCompleteContactInNode(String ContactId,
      String nodeId) throws RemoteException;

  public TopicDetail getContactFather(String pubId) throws RemoteException;

  /**
   * Return a collection of ContactDetail throught a collection of contact ids
   * 
   * @param contactIds
   *          a collection of contact ids
   * @return a collection of ContactDetail
   * @see com.stratelia.webactiv.util.contact.model.ContactDetail
   * @exception java.rmi.RemoteException
   * @exception javax.ejb.FinderException
   * @exception javax.ejb.CreateException
   * @exception javax.ejb.NamingException
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public Collection getContacts(Collection contactIds) throws RemoteException;

  public Collection getContactDetailsByLastName(ContactPK pk, String query)
      throws RemoteException;

  public Collection getContactDetailsByLastNameOrFirstName(ContactPK pk,
      String query) throws RemoteException;

  public Collection getContactDetailsByLastNameAndFirstName(ContactPK pk,
      String lastName, String firstName) throws RemoteException;

  public Collection getContactFathers(String pubId) throws RemoteException;

  public Collection getAllContactDetails(NodePK nodePK) throws RemoteException;

  public boolean isDescendant(String descId, String nodeId)
      throws RemoteException;

  public List getGroupIds(String nodeId) throws RemoteException;

  public void addGroup(String groupId) throws RemoteException;

  public void removeGroup(String groupId) throws RemoteException;

  public void addModelUsed(String[] models, String instanceId)
      throws RemoteException;

  public Collection getModelUsed(String instanceId) throws RemoteException;

}