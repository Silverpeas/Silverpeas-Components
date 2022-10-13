/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.yellowpages.service;

import org.silverpeas.core.admin.user.constant.GroupState;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.contact.model.CompleteContact;
import org.silverpeas.core.contact.model.ContactDetail;
import org.silverpeas.core.contact.model.ContactFatherDetail;
import org.silverpeas.core.contact.model.ContactPK;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.contact.model.Contact;
import org.silverpeas.components.yellowpages.model.TopicDetail;
import org.silverpeas.components.yellowpages.model.UserContact;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Collection;
import java.util.List;

public interface YellowpagesService {

  static YellowpagesService get() {
    return ServiceProvider.getService(YellowpagesService.class);
  }

  /**
   * Return a the detail of a topic
   * @param pk the id of the topic
   * @return a TopicDetail
   * @see TopicDetail
   * @since 1.0
   */
  TopicDetail goTo(NodePK pk, String userId);

  List<NodeDetail> getTree(String instanceId);

  /**
   * Add a subtopic to a topic - If a subtopic of same name already exists a NodePK with id=-1 is
   * returned else the new topic NodePK
   * @param father the father
   * @param subtopic the NodeDetail of the new sub topic
   * @return If a subtopic of same name already exists a NodePK with id=-1 is returned else the new
   * topic NodePK
   * @see NodeDetail
   * @see NodePK
   * @since 1.0
   */
  NodePK addToTopic(NodeDetail father, NodeDetail subtopic);

  /**
   * Update a subtopic to currentTopic and alert users - If a subtopic of same name already exists
   * a
   * NodePK with id=-1 is returned else the new topic NodePK
   * @param topic the NodeDetail of the updated sub topic
   * @return If a subtopic of same name already exists a NodePK with id=-1 is returned else the new
   * topic NodePK
   * @see NodeDetail
   * @see NodePK
   * @since 1.0
   */
  NodePK updateTopic(NodeDetail topic);

  /**
   * Return a subtopic to currentTopic
   * @param pk the id of the researched topic
   * @return the detail of the specified topic
   * @see NodeDetail
   * @since 1.0
   */
  NodeDetail getSubTopicDetail(NodePK pk);

  /**
   * Delete a topic and all descendants. Delete all links between descendants and contacts. This
   * contacts will be visible in the Declassified zone. Delete All subscriptions and favorites on
   * this topics and all descendants
   * @param pkToDelete the id of the topic to delete
   * @since 1.0
   */
  void deleteTopic(NodePK pkToDelete);

  void emptyDZByUserId(String instanceId, String userId);

  /**
   * Return the detail of a contact (only the Header)
   * @param contactPK the id of the contact
   * @return a ContactDetail
   * @see ContactDetail
   * @since 1.0
   */
  ContactDetail getContactDetail(ContactPK contactPK);

  /**
   * Return list of all path to this contact - it's a Collection of NodeDetail collection
   * @param contactPK the id of the contact
   * @return a Collection of NodeDetail collection
   * @see NodeDetail
   * @since 1.0
   */
  List<Collection<NodeDetail>> getPathList(ContactPK contactPK);

  /**
   * Create a new Contact (only the header - parameters) to the current Topic
   * @param contact a contact
   * @return the id of the new contact
   * @see ContactDetail
   * @since 1.0
   */
  String createContact(Contact contact, NodePK nodePK);

  /**
   * Update a contact (only the header - parameters)
   * @param contact a contact
   * @see Contact
   * @since 1.0
   */
  void updateContact(Contact contact);

  /**
   * Delete a contact If this contact is in the basket or in the DZ, it's deleted from the database
   * Else it only send to the basket
   * @param contactPK the id of the contact to delete
   * @see TopicDetail
   * @since 1.0
   */
  void deleteContact(ContactPK contactPK, NodePK nodePK);

  /**
   * Add a contact to a topic and send email alerts to topic subscribers
   * @param contactPK the id of the contact
   * @param fatherId the id of the topic
   * @since 1.0
   */
  void addContactToTopic(ContactPK contactPK, String fatherId);

  /**
   * Delete a path between contact and topic
   * @param contactPK the id of the contact
   * @param fatherId the id of the topic
   * @since 1.0
   */
  void deleteContactFromTopic(ContactPK contactPK, String fatherId);

  /**
   * Create model info attached to a contact
   * @param contactPK the id of the contact
   * @param modelId the id of the selected model
   * @since 1.0
   */
  void createInfoModel(ContactPK contactPK, String modelId);

  /**
   * Return all info of a contact and add a reading statistic
   * @param contactPK the id of a contact
   * @param nodeId the id of the node
   * @return a CompleteContact
   * @see CompleteContact
   */
  CompleteContact getCompleteContactInNode(ContactPK contactPK, String nodeId);

  CompleteContact getCompleteContact(ContactPK contactPK);

  /**
   * Return a collection of ContactDetail through a collection of contact ids
   * @param contactIds a collection of contact ids
   * @return a collection of ContactDetail
   * @see ContactDetail
   * @since 1.0
   */
  Collection<UserContact> getContacts(Collection<String> contactIds, String instanceId);

  Collection<ContactDetail> getContactDetailsByLastName(ContactPK pk, String query);

  Collection<ContactDetail> getContactDetailsByLastNameOrFirstName(ContactPK pk, String query);

  Collection<ContactDetail> getContactDetailsByLastNameAndFirstName(ContactPK pk, String lastName,
      String firstName);

  Collection<NodePK> getContactFathers(ContactPK contactPK);

  Collection<ContactFatherDetail> getAllContactDetails(NodePK nodePK);

  /**
   * Gets {@link GroupState#VALID} {@link Group} hosted by the given node.
   * @param pk the reference to a node.
   * @return a list of {@link GroupState#VALID} {@link Group} instance.
   */
  List<Group> getGroups(NodePK pk);

  void addGroup(String groupId, NodePK nodePK);

  void removeGroup(String groupId);

  void removeGroup(String groupId, NodePK nodePK);

  void setModelUsed(String[] models, String instanceId);

  Collection<String> getModelUsed(String instanceId);

  void index(String instanceId);
}