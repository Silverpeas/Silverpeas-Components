/*
 * Copyright (C) 2000 - 2024 Silverpeas
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

import org.apache.commons.io.FilenameUtils;
import org.silverpeas.components.yellowpages.dao.GroupDAO;
import org.silverpeas.components.yellowpages.model.TopicDetail;
import org.silverpeas.components.yellowpages.model.UserContact;
import org.silverpeas.components.yellowpages.model.YellowpagesRuntimeException;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contact.model.CompleteContact;
import org.silverpeas.core.contact.model.Contact;
import org.silverpeas.core.contact.model.ContactDetail;
import org.silverpeas.core.contact.model.ContactFatherDetail;
import org.silverpeas.core.contact.model.ContactPK;
import org.silverpeas.core.contact.service.ContactService;
import org.silverpeas.core.contribution.template.form.dao.ModelDAO;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;
import static org.silverpeas.components.yellowpages.YellowpagesComponentSettings.areUserExtraDataRequired;

/**
 * This is the Yellowpages Service layer to manage the yellow page application.
 * @author Nicolas Eysseric
 */
@Service
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class DefaultYellowpagesService implements YellowpagesService {

  private static final String GROUP_PREFIX = "group_";
  private static final String NO_ID = "unknown";
  @Inject
  private OrganizationController organizationController;
  @Inject
  private NodeService nodeService;
  @Inject
  private ContactService contactService;

  private OrganizationController getOrganisationController() {
    return this.organizationController;
  }

  private List<Integer> getRecursiveNbContact(NodeDetail nodeDetail) {
    List<Integer> nbContactsByTopic = new ArrayList<>();

    Collection<NodeDetail> childrenPKs = nodeDetail.getChildrenDetails();
    if (childrenPKs != null) {
      // get groups
      // add groups to nodeDetail.childrens
      addNodeDetailGroups(nodeDetail, childrenPKs);

      int nbContacts;
      List<Integer> nbContactsBySubTopic;
      for (NodeDetail child : childrenPKs) {
        NodePK childPK = child.getNodePK();
        if (!childPK.getId().startsWith(GROUP_PREFIX)) {
          String childPath = child.getPath();
          nbContacts = contactService.getNbPubByFatherPath(childPK, childPath);
          // traitement des sous-rubriques et des sous-groupes
          nbContactsBySubTopic = getRecursiveNbContact(nodeService.getDetail(childPK));
          for (Integer nbSubContact : nbContactsBySubTopic) {
            nbContacts += nbSubContact;
          }
        } else { // groupe
          String groupId =
              childPK.getId().substring(childPK.getId().indexOf('_') + 1);
          nbContacts = getOrganisationController().getAllSubUsersNumber(groupId);
        }
        nbContactsByTopic.add(nbContacts);
      }
    }
    return nbContactsByTopic;
  }

  private void addNodeDetailGroups(final NodeDetail nodeDetail,
      final Collection<NodeDetail> childrenPKs) {
    getGroups(nodeDetail.getNodePK()).forEach(g -> {
      NodeDetail nodeGroup = new NodeDetail();
      nodeGroup.getNodePK().setId(GROUP_PREFIX + g.getId());
      nodeGroup.setName(g.getName());
      nodeGroup.setDescription(g.getDescription());
      childrenPKs.add(nodeGroup);
    });
  }

  /**
   * Return a the detail of a topic
   * @param pk the id of the topic
   * @return a TopicDetail
   * @see TopicDetail
   */
  @Override
  public TopicDetail goTo(NodePK pk, String userId) {

    Collection<NodeDetail> newPath = new ArrayList<>();
    List<ContactDetail> contactDetailsR = new ArrayList<>();
    try {
      NodeDetail nodeDetail = nodeService.getDetail(pk);
      Collection<ContactDetail> contactDetails;
      if (pk.isUnclassed()) {
        ContactPK contactPK = new ContactPK(NO_ID, pk);
        contactDetails = contactService.getOrphanContacts(contactPK);
      } else if (pk.isTrash()) {
        ContactPK contactPK = new ContactPK(NO_ID, pk);
        contactDetails = contactService.getUnavailableContactsByPublisherId(contactPK, userId, "1");
      } else {
        contactDetails = contactService.getDetailsByFatherPK(nodeDetail.getNodePK());
      }

      if (contactDetails != null) {
        fillContactDetails(contactDetails, contactDetailsR);
      }

      List<Integer> nbContactsByTopic = getRecursiveNbContact(nodeDetail);

      return new TopicDetail(newPath, nodeDetail, contactDetails2userPubs(contactDetailsR),
          nbContactsByTopic);
    } catch (Exception re) {
      throw new YellowpagesRuntimeException(re);
    }
  }

  private void fillContactDetails(final Collection<ContactDetail> contactDetails,
      final List<ContactDetail> contactDetailsR) {
    OrganizationController orga = getOrganisationController();
    for (ContactDetail contactDetail : contactDetails) {
      if (contactDetail.getUserId() != null) {// contact de type user Silverpeas
        try {
          UserDetail userDetail = orga.getUserDetail(contactDetail.getUserId());
          if (userDetail != null) {
            setContactAttributes(contactDetail, userDetail, true);
            contactDetailsR.add(contactDetail);
          } else {
            contactDetail.setUserId(null);
            updateContact(contactDetail);
            sendContactToBasket(contactDetail.getPK());
          }
        } catch (Exception e) {
          SilverLogger.getLogger(this).error("contactDetail = " + contactDetail, e);
        }
      } else {
        contactDetailsR.add(contactDetail);
      }
    }
  }

  @Override
  public List<NodeDetail> getTree(String instanceId) {
    final List<NodeDetail> result = new ArrayList<>();
    List<NodeDetail> tree = nodeService.getSubTree(new NodePK("0", instanceId));
    // TODO :getting all groups linked in this component instance
    for (NodeDetail node : tree) {
      result.add(node);
      // pour chaque node, recuperer les groupes associes
      getGroups(node.getNodePK()).forEach(g -> addGroup(result, g, node.getLevel() + 1));
    }
    return result;
  }

  public void addGroup(List<NodeDetail> tree, Group group, int level) {
    if (group != null) {
      NodeDetail nGroup = new NodeDetail();
      nGroup.setName(group.getName());
      nGroup.setDescription(group.getDescription());
      nGroup.getNodePK().setId(GROUP_PREFIX + group.getId());
      nGroup.setLevel(level);
      tree.add(nGroup);

      Group[] subGroups = getOrganisationController().getAllSubGroups(group.getId());
      Group subGroup;
      for (final Group subGroup1 : subGroups) {
        subGroup = subGroup1;
        addGroup(tree, subGroup, level + 1);
      }
    }
  }

  @Override
  public void addToTopic(NodeDetail father, NodeDetail subTopic) {
    if (!isSameTopicSameLevelOnCreation(subTopic)) {
      try {
        // register form to current app
        String xmlFormName = subTopic.getModelId();
        if (StringUtil.isDefined(xmlFormName)) {
          registerTemplate(xmlFormName, father.getNodePK().getInstanceId());
        }
        nodeService.createNode(subTopic, father);
      } catch (Exception re) {
        throw new YellowpagesRuntimeException(re);
      }
    }
  }

  /**
   * When creates a new subTopic, Check if a subtopic of same name already exists
   * @param subTopic the NodeDetail of the new sub topic
   * @return true if a subtopic of same name already exists under the currentTopic else false
   * @see NodeDetail
   */
  private boolean isSameTopicSameLevelOnCreation(NodeDetail subTopic) {
    try {
      return nodeService.isSameNameSameLevelOnCreation(subTopic);
    } catch (Exception re) {
      throw new YellowpagesRuntimeException(re);
    }
  }

  /**
   * When updates a subTopic, Check if another subtopic of same name already exists
   * @param subTopic the NodeDetail of the new sub topic
   * @return true if a subtopic of same name already exists under the currentTopic else false
   * @see NodeDetail
   */
  private boolean isSameTopicSameLevelOnUpdate(NodeDetail subTopic) {
    try {
      return nodeService.isSameNameSameLevelOnUpdate(subTopic);
    } catch (Exception re) {
      throw new YellowpagesRuntimeException(re);
    }
  }

  @Override
  public void updateTopic(NodeDetail topic) {
    if (!isSameTopicSameLevelOnUpdate(topic)) {
      try {
        // register form to current app
        String xmlFormName = topic.getModelId();
        if (StringUtil.isDefined(xmlFormName)) {
          registerTemplate(xmlFormName, topic.getNodePK().getInstanceId());
        }
        nodeService.setDetail(topic);
      } catch (Exception re) {
        throw new YellowpagesRuntimeException(re);
      }
    }
  }

  @Override
  public NodeDetail getSubTopicDetail(NodePK pk) {
    return nodeService.getDetail(pk);
  }

  /**
   * Delete a topic and all descendants. Delete all links between descendants and contacts. This
   * contacts will be visible in the Declassified zone. Delete All subscriptions and favorites on
   * this topics and all descendants
   * @param pkToDelete the id of the topic to delete
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void deleteTopic(NodePK pkToDelete) {


    // Fictive contact to obtain the correct tableName
    ContactPK contactPK = new ContactPK(NO_ID, pkToDelete);

    // Delete all entries in the table which link pub to topic
    try {
      contactService.removeAllIssue(pkToDelete, contactPK);
      unreferenceOrphanContacts(contactPK);
    } catch (Exception re) {
      throw new YellowpagesRuntimeException(re);
    }

    // Delete the topic
    try {
      nodeService.deleteNode(pkToDelete);
    } catch (Exception re) {
      throw new YellowpagesRuntimeException(re);
    }

  }

  /**
   * Interface - Gestion des contacts
   */

  private Collection<UserContact> contactDetails2userPubs(
      Collection<ContactDetail> contactDetails) {
    Iterator<ContactDetail> iterator = contactDetails.iterator();
    String[] users = new String[contactDetails.size()];
    int i = 0;
    while (iterator.hasNext()) {
      users[i] = iterator.next().getCreatorId();
      i++;
    }
    OrganizationController orga = getOrganisationController();
    UserDetail[] userDetails = orga.getUserDetails(users);
    ArrayList<UserContact> list = new ArrayList<>(contactDetails.size());
    iterator = contactDetails.iterator();
    i = 0;
    while (iterator.hasNext()) {
      UserContact uPub = new UserContact(userDetails[i], iterator.next());
      i++;
      list.add(uPub);
    }
    return list;
  }

  /**
   * Return the detail of a contact (only the Header)
   * @param contactPK the id of the contact
   * @return a ContactDetail
   * @see ContactDetail
   */
  @Override
  public ContactDetail getContactDetail(ContactPK contactPK) {
    try {
      ContactDetail contactDetail = contactService.getDetail(contactPK);
      // contact de type user Silverpeas
      if (contactDetail.getUserId() != null) {
        OrganizationController orga = getOrganisationController();
        UserDetail userDetail = orga.getUserDetail(contactDetail.getUserId());
        if (userDetail != null) {
          setContactAttributes(contactDetail, userDetail, false);
        } else {
          contactDetail.setUserId(null);
          updateContact(contactDetail);
          sendContactToBasket(contactDetail.getPK());
        }
      }
      return contactDetail;
    } catch (Exception re) {
      throw new YellowpagesRuntimeException(re);
    }
  }

  private void setContactAttributes(ContactDetail contactDetail, UserDetail userDetail,
      boolean filterExtraInfos) {
    contactDetail.setFirstName(userDetail.getFirstName());
    contactDetail.setLastName(userDetail.getLastName());
    contactDetail.setEmail(userDetail.getEmailAddress());
    contactDetail.setUserExtraDataRequired(!filterExtraInfos || areUserExtraDataRequired());
  }

  @Override
  public Collection<ContactDetail> getContactDetailsByLastName(ContactPK pk, String query) {

    try {
      return contactService.getDetailsByLastName(pk, query);
    } catch (Exception e) {
      throw new YellowpagesRuntimeException(e);
    }
  }

  @Override
  public Collection<ContactDetail> getContactDetailsByLastNameOrFirstName(ContactPK pk,
      String query) {


    Collection<ContactDetail> contactDetails;
    try {
      contactDetails = contactService.getDetailsByLastNameOrFirstName(pk, query);
    } catch (Exception e) {
      throw new YellowpagesRuntimeException(e);
    }
    return contactDetails;
  }

  @Override
  public Collection<ContactDetail> getContactDetailsByLastNameAndFirstName(ContactPK pk,
      String lastName, String firstName) {

    try {
      return contactService.getDetailsByLastNameAndFirstName(pk, lastName, firstName);
    } catch (Exception e) {
      throw new YellowpagesRuntimeException(e);
    }
  }

  @Override
  public Collection<ContactFatherDetail> getAllContactDetails(NodePK nodePK) {

    ArrayList<NodePK> nodePKsWithout12 = new ArrayList<>();
    ArrayList<ContactFatherDetail> contactDetailsR = new ArrayList<>();
    try {
      Collection<NodePK> nodePKs = nodeService.getDescendantPKs(nodePK);
      nodePKsWithout12.add(nodePK);
      for (NodePK pk : nodePKs) {
        if (!pk.isTrash() && !pk.isUnclassed()) {
          nodePKsWithout12.add(pk);
        }
      }
      ContactPK pk = new ContactPK(NO_ID, nodePK);
      Collection<ContactFatherDetail> contactDetails =
          contactService.getDetailsByFatherPKs(nodePKsWithout12, pk, nodePK);
      if (contactDetails != null) {
        fillContactFatherDetails(contactDetails, contactDetailsR);
      }

      return contactDetailsR;
    } catch (Exception re) {
      throw new YellowpagesRuntimeException(re);
    }
  }

  private void fillContactFatherDetails(final Collection<ContactFatherDetail> contactDetails,
      final ArrayList<ContactFatherDetail> contactDetailsR) {
    for (ContactFatherDetail contactFatherDetail : contactDetails) {
      ContactDetail contactDetail = contactFatherDetail.getContactDetail();
      // contact de type user Silverpeas
      if (contactDetail.getUserId() != null) {
        try {
          OrganizationController orga = getOrganisationController();
          UserDetail userDetail = orga.getUserDetail(contactDetail.getUserId());
          if (userDetail != null) {
            setContactAttributes(contactDetail, userDetail, true);
            contactDetailsR.add(contactFatherDetail);
          } else {
            contactDetail.setUserId(null);
            updateContact(contactDetail);
            sendContactToBasket(contactDetail.getPK());
          }
        } catch (Exception e) {
          SilverLogger.getLogger(this).error("contactDetail = " + contactDetail, e);
        }
      } else {
        contactDetailsR.add(contactFatherDetail);
      }
    }
  }

  /**
   * Return list of all path to this contact - it's a Collection of NodeDetail collection
   * @param contactPK the id of the contact
   * @return a Collection of NodeDetail collection
   * @see NodeDetail
   */
  @Override
  public List<Collection<NodeDetail>> getPathList(ContactPK contactPK) {

    Collection<NodePK> fatherPKs;
    try {
      // get all nodePK whick contains this contact
      fatherPKs = contactService.getAllFatherPK(contactPK);
    } catch (Exception re) {
      throw new YellowpagesRuntimeException(re);
    }
    try {
      List<Collection<NodeDetail>> pathList = new ArrayList<>();
      if (fatherPKs != null) {
        // For each topic, get the path to it
        for (NodePK pk : fatherPKs) {
          Collection<NodeDetail> path = nodeService.getPath(pk);
          // add this path
          pathList.add(path);
        }
      }

      return pathList;
    } catch (Exception re) {
      throw new YellowpagesRuntimeException(re);
    }
  }

  /**
   * Create a new Contact (only the header - parameters) to the current Topic
   * @param contact a contact
   * @return the id of the new contact
   * @see Contact
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public String createContact(Contact contact, NodePK nodePK) {

    ContactPK contactPK;
    contact.getPK().setComponentName(nodePK.getInstanceId());

    try {
      // create the contact
      contactPK = contactService.createContact(contact);
      contact.getPK().setId(contactPK.getId());
      // add this contact to the current topic
      addContactToTopic(contactPK, nodePK.getId());
    } catch (Exception re) {
      throw new YellowpagesRuntimeException(re);
    }

    return contactPK.getId();
  }

  /**
   * Update a contact (only the header - parameters)
   * @param contactDetail a ContactDetail
   * @see Contact
   */
  @Override
  public void updateContact(Contact contactDetail) {

    try {
      contactService.setDetail(contactDetail);
      ContactPK contactPK = contactDetail.getPK();
      String fatherId = "2";
      Collection<NodePK> fathers = contactService.getAllFatherPK(contactPK);
      Iterator<NodePK> it = fathers.iterator();
      if (it.hasNext()) {
        fatherId = (it.next()).getId();
      }

      if ("2".equals(fatherId) || "1".equals(fatherId)) {
        deleteIndex(contactPK);
      }

    } catch (Exception re) {
      throw new YellowpagesRuntimeException(re);
    }

  }

  /**
   * Delete a contact If this contact is in the basket or in the DZ, it's deleted from the database
   * Else it only send to the basket
   * @param contactPK the id of the contact to delete
   * @see TopicDetail
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void deleteContact(ContactPK contactPK, NodePK nodePK) {

    // if the contact is in the basket or in the DZ
    // this contact is deleted from the database
    if (nodePK.isTrash() || nodePK.isUnclassed()) {
      try {
        // delete link between this contact and the basket A VOIR POUR LA DZ !!!!!!!!!
        contactService.removeFather(contactPK, new NodePK(NodePK.BIN_NODE_ID, nodePK));
        // delete the contact
        contactService.removeContact(contactPK);
      } catch (Exception re) {
        throw new YellowpagesRuntimeException(re);
      }
    } else {
      // the contact is in another topic than basket or DZ
      // this contact is not deleted from the database but only send to the basket
      sendContactToBasket(contactPK);
    }

  }

  /**
   * Send the contact in the basket topic
   * @param contactPK the id of the contact
   * @see TopicDetail
   */
  private void sendContactToBasket(ContactPK contactPK) {
    try {
      // remove all links between this contact and topics
      contactService.removeAllFather(contactPK);
      // add link between this contact and the basket topic
      contactService.addFather(contactPK, new NodePK(NodePK.BIN_NODE_ID, contactPK));
      deleteIndex(contactPK);
    } catch (Exception re) {
      throw new YellowpagesRuntimeException(re);
    }
  }

  @Override
  public void emptyDZByUserId(String instanceId, String userId) {
    ContactPK contactPK = new ContactPK(null, instanceId);
    try {
      // delete all current user orphan contacts
      contactService.deleteOrphanContactsByCreatorId(contactPK, userId);
    } catch (Exception re) {
      throw new YellowpagesRuntimeException(re);
    }
  }

  /**
   * Add a contact to a topic and send email alerts to topic subscribers
   * @param contactPK the id of the contact
   * @param fatherId the id of the topic
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void addContactToTopic(ContactPK contactPK, String fatherId) {
    NodePK fatherPK = new NodePK(fatherId, contactPK);
    // add contact to topic
    try {
      Collection<NodePK> fathers = contactService.getAllFatherPK(contactPK);
      if (fathers.size() == 1) {
        Iterator<NodePK> iterator = fathers.iterator();
        if (iterator.hasNext()) {
          NodePK pk = iterator.next();
          if (pk.isTrash()) {
            contactService.removeFather(contactPK, pk);
          }
        }
      }
      contactService.addFather(contactPK, fatherPK);
      // reindexe le contact si pas dans la corbeille
      if (!fatherPK.isTrash()) {
        contactService.index(contactPK);
      }
    } catch (Exception re) {
      throw new YellowpagesRuntimeException(re);
    }
  }

  /**
   * Delete a path between contact and topic
   * @param contactPK the id of the contact
   * @param fatherId the id of the topic
   */
  @Override
  public void deleteContactFromTopic(ContactPK contactPK, String fatherId) {
    NodePK fatherPK = new NodePK(fatherId, contactPK);
    try {
      contactService.removeFather(contactPK, fatherPK);
    } catch (Exception re) {
      throw new YellowpagesRuntimeException(re);
    }

  }

  /**
   * Create model info attached to a contact
   * @param contactPK the id of the contact
   * @param modelId the id of the selected model
   */
  @Override
  public void createInfoModel(ContactPK contactPK, String modelId) {
    try {
      contactService.createInfoModel(contactPK, modelId);
    } catch (Exception re) {
      throw new YellowpagesRuntimeException(re);
    }
  }

  /**
   * Return all info of a contact
   * @param contactPK the id of a contact
   * @param nodeId the id of the node
   * @return a CompleteContact
   * @see CompleteContact
   */
  @Override
  public CompleteContact getCompleteContactInNode(ContactPK contactPK, String nodeId) {

    CompleteContact completeContact;
    try {
      NodePK nodePK = new NodePK(nodeId, contactPK.getInstanceId());
      NodeDetail nodeDetail = nodeService.getDetail(nodePK);
      String modelId = nodeDetail.getModelId();
      completeContact = contactService.getCompleteContact(contactPK, modelId);
    } catch (Exception re) {
      throw new YellowpagesRuntimeException(re);
    }
    checkContactAsUser(completeContact);

    return completeContact;
  }

  @Override
  public CompleteContact getCompleteContact(ContactPK contactPK) {
    CompleteContact contact = contactService.getCompleteContact(contactPK);
    checkContactAsUser(contact);
    return contact;
  }

  private void checkContactAsUser(CompleteContact completeContact) {
    ContactDetail contactDetail = completeContact.getContactDetail();
    if (contactDetail.getUserId() != null) {
      // contact de type user Silverpeas
      try {
        OrganizationController orga = getOrganisationController();
        UserDetail userDetail = orga.getUserDetail(contactDetail.getUserId());
        if (userDetail != null) {
          setContactAttributes(contactDetail, userDetail, false);
        } else {
          contactDetail.setUserId(null);
          updateContact(contactDetail);
          sendContactToBasket(contactDetail.getPK());
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this).error("contactPK = " + contactDetail.getPK().toString(), e);
      }
    }
  }

  /**
   * Return a collection of ContactDetail throught a collection of contact ids
   * @param contactIds a collection of contact ids
   * @return a collection of ContactDetail
   * @see ContactDetail
   */
  @Override
  public Collection<UserContact> getContacts(Collection<String> contactIds, String instanceId) {

    List<ContactPK> contactPKs = new ArrayList<>();
    List<ContactDetail> contactDetailsR = new ArrayList<>();
    for (String contactId : contactIds) {
      ContactPK contactPK = new ContactPK(contactId, instanceId);
      contactPKs.add(contactPK);
    }
    try {
      Collection<ContactDetail> contacts = contactService.getContacts(contactPKs);
      if (contacts != null) {
        fillContactDetails(contacts, contactDetailsR);
      }
    } catch (Exception re) {
      throw new YellowpagesRuntimeException(re);
    }

    return contactDetails2userPubs(contactDetailsR);
  }

  @Override
  public Collection<NodePK> getContactFathers(ContactPK contactPK) {
    try {
      // fetch contact fathers
      return contactService.getAllFatherPK(contactPK);
    } catch (Exception re) {
      throw new YellowpagesRuntimeException(re);
    }
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public void unreferenceOrphanContacts(ContactPK contactPK) {
    try {
      Collection<ContactDetail> orphanContacts = contactService.getOrphanContacts(contactPK);
      for (ContactDetail contactDetail : orphanContacts) {
        // add link between this contact and the basket topic
        contactService.addFather(contactDetail.getPK(), new NodePK(NodePK.BIN_NODE_ID, contactPK));
        deleteIndex(contactDetail.getPK());
      }
    } catch (Exception e) {
      throw new YellowpagesRuntimeException(e);
    }
  }

  private void deleteIndex(ContactPK contactPK) {
    contactService.deleteIndex(contactPK);
  }

  @Override
  public List<Group> getGroups(NodePK pk) {
    try (Connection con = getConnection()) {
      return GroupDAO.getGroupIds(con, pk.getId(), pk.getInstanceId()).stream()
          .map(getOrganisationController()::getGroup)
          .filter(Objects::nonNull)
          .map(Group.class::cast)
          .filter(not(Group::isRemovedState))
          .collect(Collectors.toList());
    } catch (Exception e) {
      throw new YellowpagesRuntimeException(e);
    }
  }

  @Override
  public void addGroup(String groupId, NodePK nodePK) {
    try (Connection con = getConnection()) {
      GroupDAO.addGroup(con, groupId, nodePK.getId(), nodePK.getInstanceId());
    } catch (Exception e) {
      throw new YellowpagesRuntimeException(e);
    }
  }

  @Override
  public void removeGroup(String groupId) {
    Connection con = getConnection();
    try {
      GroupDAO.removeGroup(con, groupId);
    } catch (Exception e) {
      throw new YellowpagesRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void removeGroup(String groupId, NodePK nodePK) {
    try (Connection con = getConnection()) {
      GroupDAO.removeGroup(con, groupId, nodePK.getId(), nodePK.getInstanceId());
    } catch (Exception e) {
      throw new YellowpagesRuntimeException(e);
    }
  }

  private Connection getConnection() {
    // initialisation de la connexion
    try {
      return DBUtil.openConnection();
    } catch (SQLException e) {
      // traitement des exceptions
      throw new YellowpagesRuntimeException(e);
    }
  }

  @Override
  public void setModelUsed(String[] models, String instanceId) {
    try (Connection con = getConnection()) {
      ModelDAO.deleteModel(con, instanceId);
      if (models != null) {
        for (String modelId : models) {
          ModelDAO.addModel(con, instanceId, modelId);
        }
      }
    } catch (Exception e) {
      throw new YellowpagesRuntimeException(e);
    }
  }

  @Override
  public Collection<String> getModelUsed(String instanceId) {
    try (Connection con = getConnection()) {
      return ModelDAO.getModelUsed(con, instanceId);
    } catch (Exception e) {
      throw new YellowpagesRuntimeException(e);
    }
  }

  public void index(String instanceId) {
    indexFolder(new NodePK(NodePK.ROOT_NODE_ID, instanceId));
  }

  private void indexFolder(NodePK pk) {
    NodeDetail node = nodeService.getDetail(pk);
    if (!pk.isRoot() && !pk.isTrash() && !pk.isUnclassed()) {
      nodeService.createIndex(node);
    }

    if (!pk.isTrash() && !pk.isUnclassed()) {
      // treatment of the publications of current topic
      indexContacts(pk);

      // treatment of the nodes of current topic
      Collection<NodeDetail> subTopics = node.getChildrenDetails();
      for (NodeDetail subTopic : subTopics) {
        indexFolder(subTopic.getNodePK());
      }
    }
  }

  private void indexContacts(NodePK pk) {
    Collection<ContactDetail> contacts = contactService.getDetailsByFatherPK(pk);
    for (ContactDetail contact : contacts) {
      contactService.index(contact.getPK());
    }
  }

  private void registerTemplate(String xmlFormName, String instanceId)
      throws PublicationTemplateException {
    // register form to current app
    if (StringUtil.isDefined(xmlFormName)) {
      String key = instanceId + ":" + FilenameUtils.getBaseName(xmlFormName);
      PublicationTemplateManager templateManager = PublicationTemplateManager.getInstance();
      templateManager.addDynamicPublicationTemplate(key, xmlFormName);
    }
  }
}
