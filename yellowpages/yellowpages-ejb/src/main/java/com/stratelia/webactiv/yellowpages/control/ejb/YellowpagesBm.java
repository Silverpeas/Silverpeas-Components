/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.yellowpages.control.ejb;

import java.util.Collection;
import java.util.List;

import javax.ejb.Local;

import com.stratelia.webactiv.util.contact.model.CompleteContact;
import com.stratelia.webactiv.util.contact.model.Contact;
import com.stratelia.webactiv.util.contact.model.ContactDetail;
import com.stratelia.webactiv.util.contact.model.ContactFatherDetail;
import com.stratelia.webactiv.util.contact.model.ContactPK;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.yellowpages.model.TopicDetail;
import com.stratelia.webactiv.yellowpages.model.UserContact;

@Local
public interface YellowpagesBm {

  /**
   * Return a the detail of a topic
   *
   * @param id the id of the topic
   * @return a TopicDetail
   * @see com.stratelia.webactiv.yellowpages.model.TopicDetail
   * @since 1.0
   */
  public TopicDetail goTo(NodePK pk, String userId);

  public List<NodeDetail> getTree(String instanceId);

  /**
   * Add a subtopic to a topic - If a subtopic of same name already exists a NodePK with id=-1 is
   * returned else the new topic NodePK
   *
   * @param father the father
   * @param subTopic the NodeDetail of the new sub topic
   * @return If a subtopic of same name already exists a NodePK with id=-1 is returned else the new
   * topic NodePK
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @since 1.0
   */
  public NodePK addToTopic(NodeDetail father, NodeDetail subtopic);

  /**
   * Update a subtopic to currentTopic and alert users - If a subtopic of same name already exists a
   * NodePK with id=-1 is returned else the new topic NodePK
   *
   * @param topic the NodeDetail of the updated sub topic
   * @param alertType Alert all users, only publishers or nobody of the topic creation alertType =
   * "All"|"Publisher"|"None"
   * @return If a subtopic of same name already exists a NodePK with id=-1 is returned else the new
   * topic NodePK
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @since 1.0
   */
  public NodePK updateTopic(NodeDetail topic);

  /**
   * Return a subtopic to currentTopic
   *
   * @param subTopicId the id of the researched topic
   * @return the detail of the specified topic
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  public NodeDetail getSubTopicDetail(NodePK pk);

  /**
   * Delete a topic and all descendants. Delete all links between descendants and contacts. This
   * contacts will be visible in the Declassified zone. Delete All subscriptions and favorites on
   * this topics and all descendants
   *
   * @param topicId the id of the topic to delete
   * @since 1.0
   */
  public void deleteTopic(NodePK pkToDelete);

  public void emptyDZByUserId(String instanceId, String userId);

  /**
   * Return the detail of a contact (only the Header)
   *
   * @param pubId the id of the contact
   * @return a ContactDetail
   * @see com.stratelia.webactiv.util.contact.model.ContactDetail
   * @since 1.0
   */
  public ContactDetail getContactDetail(ContactPK pk);

  /**
   * Return list of all path to this contact - it's a Collection of NodeDetail collection
   *
   * @param pubId the id of the contact
   * @return a Collection of NodeDetail collection
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  public List<Collection<NodeDetail>> getPathList(ContactPK contactPK);

  /**
   * Create a new Contact (only the header - parameters) to the current Topic
   *
   * @param pubDetail a ContactDetail
   * @return the id of the new contact
   * @see com.stratelia.webactiv.util.contact.model.ContactDetail
   * @since 1.0
   */
  public String createContact(Contact contact, NodePK nodePK);

  /**
   * Update a contact (only the header - parameters)
   *
   * @param pubDetail a ContactDetail
   * @see com.stratelia.webactiv.util.contact.model.Contact
   * @since 1.0
   */
  public void updateContact(Contact contact);

  /**
   * Delete a contact If this contact is in the basket or in the DZ, it's deleted from the database
   * Else it only send to the basket
   *
   * @param pubId the id of the contact to delete
   * @return a TopicDetail
   * @see com.stratelia.webactiv.yellowpages.model.TopicDetail
   * @since 1.0
   */
  public void deleteContact(ContactPK contactPK, NodePK nodePK);

  /**
   * Add a contact to a topic and send email alerts to topic subscribers
   *
   * @param pubId the id of the contact
   * @param fatherId the id of the topic
   * @since 1.0
   */
  public void addContactToTopic(ContactPK contactPK, String fatherId);

  /**
   * Delete a path between contact and topic
   *
   * @param pubId the id of the contact
   * @param fatherId the id of the topic
   * @since 1.0
   */
  public void deleteContactFromTopic(ContactPK contactPK, String fatherId);

  /**
   * Create model info attached to a contact
   *
   * @param pubId the id of the contact
   * @param modelId the id of the selected model
   * @since 1.0
   */
  public void createInfoModel(ContactPK contactPK, String modelId);

  /**
   * Return all info of a contact and add a reading statistic
   *
   * @param ContactId the id of a contact
   * @param nodeId the id of the node
   * @return a CompleteContact
   * @see com.stratelia.webactiv.util.contact.model.CompleteContact
   */
  public CompleteContact getCompleteContactInNode(ContactPK contactPK, String nodeId);
  
  public CompleteContact getCompleteContact(ContactPK contactPK);

  /**
   * Return a collection of ContactDetail throught a collection of contact ids
   *
   * @param contactIds a collection of contact ids
   * @return a collection of ContactDetail
   * @see com.stratelia.webactiv.util.contact.model.ContactDetail
   * @since 1.0
   */
  public Collection<UserContact> getContacts(Collection<String> contactIds, String instanceId);

  public Collection<ContactDetail> getContactDetailsByLastName(ContactPK pk, String query);

  public Collection<ContactDetail> getContactDetailsByLastNameOrFirstName(ContactPK pk,
      String query);

  public Collection<ContactDetail> getContactDetailsByLastNameAndFirstName(ContactPK pk,
      String lastName, String firstName);

  public Collection<NodePK> getContactFathers(ContactPK contactPK);

  public Collection<ContactFatherDetail> getAllContactDetails(NodePK nodePK);

  public List<String> getGroupIds(NodePK pk);

  public void addGroup(String groupId, NodePK nodePK);

  public void removeGroup(String groupId, NodePK nodePK);

  public void setModelUsed(String[] models, String instanceId);

  public Collection<String> getModelUsed(String instanceId);
  
  public void index(String instanceId);
}