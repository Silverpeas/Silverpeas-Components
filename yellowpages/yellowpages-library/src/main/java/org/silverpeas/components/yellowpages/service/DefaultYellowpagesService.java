/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.yellowpages.service;

import org.silverpeas.core.contribution.templating.form.dao.ModelDAO;
import org.silverpeas.core.contribution.templating.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.templating.publication.PublicationTemplateManager;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contact.service.ContactService;
import org.silverpeas.core.contact.model.CompleteContact;
import org.silverpeas.core.contact.model.ContactDetail;
import org.silverpeas.core.contact.model.ContactFatherDetail;
import org.silverpeas.core.contact.model.ContactPK;
import com.stratelia.webactiv.node.control.NodeService;
import com.stratelia.webactiv.node.model.NodeDetail;
import com.stratelia.webactiv.node.model.NodePK;
import org.silverpeas.core.contact.model.Contact;
import org.silverpeas.components.yellowpages.dao.GroupDAO;
import org.silverpeas.components.yellowpages.model.TopicDetail;
import org.silverpeas.components.yellowpages.model.UserContact;
import org.silverpeas.components.yellowpages.model.YellowpagesRuntimeException;
import org.apache.commons.io.FilenameUtils;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.silverpeas.components.yellowpages.YellowpagesComponentSettings.areUserExtraDataRequired;

/**
 * This is the Yellowpages Service layer to manage the yellow page application.
 * @author Nicolas Eysseric
 */
@Singleton
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class DefaultYellowpagesService implements YellowpagesService {

  @Inject
  private OrganizationController organizationController;
  @Inject
  private NodeService nodeService;
  @Inject
  private ContactService contactService;

  public DefaultYellowpagesService() {
  }

  private OrganizationController getOrganisationController() {
    return this.organizationController;
  }

  private List<Integer> getRecursiveNbContact(NodeDetail nodeDetail) {
    List<Integer> nbContactsByTopic = new ArrayList<>();

    Collection<NodeDetail> childrenPKs = nodeDetail.getChildrenDetails();
    if (childrenPKs != null) {
      // get groups
      // add groups to nodeDetail.childrens
      List<String> groupIds = getGroupIds(nodeDetail.getNodePK());
      for (String groupId : groupIds) {
        Group group = getOrganisationController().getGroup(groupId);
        if (group != null) {
          NodeDetail nodeGroup = new NodeDetail();
          nodeGroup.getNodePK().setId("group_" + group.getId());
          nodeGroup.setName(group.getName());
          nodeGroup.setDescription(group.getDescription());
          childrenPKs.add(nodeGroup);
        }
      }

      int nbContacts;
      List<Integer> nbContactsBySubTopic;
      Iterator<Integer> itSub;
      int nbSubContact;
      for (NodeDetail child : childrenPKs) {
        NodePK childPK = child.getNodePK();
        if (!childPK.getId().startsWith("group_")) {
          String childPath = child.getPath();
          nbContacts = contactService.getNbPubByFatherPath(childPK, childPath);
          // traitement des sous-rubriques et des sous-groupes
          nbContactsBySubTopic = getRecursiveNbContact(nodeService.getDetail(childPK));
          itSub = nbContactsBySubTopic.iterator();
          while (itSub.hasNext()) {
            nbSubContact = itSub.next();
            nbContacts += nbSubContact;
          }
        } else { // groupe
          String groupId =
              childPK.getId().substring(childPK.getId().indexOf("_") + 1, childPK.getId().length());
          nbContacts = getOrganisationController().getAllSubUsersNumber(groupId);
        }
        nbContactsByTopic.add(nbContacts);
      }
    }
    return nbContactsByTopic;
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
        ContactPK contactPK = new ContactPK("unknown", pk);
        contactDetails = contactService.getOrphanContacts(contactPK);
      } else if (pk.isTrash()) {
        ContactPK contactPK = new ContactPK("unknown", pk);
        contactDetails = contactService.getUnavailableContactsByPublisherId(contactPK, userId, "1");
      } else {
        contactDetails = contactService.getDetailsByFatherPK(nodeDetail.getNodePK());
      }

      if (contactDetails != null) {
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

      List<Integer> nbContactsByTopic = getRecursiveNbContact(nodeDetail);

      return new TopicDetail(newPath, nodeDetail, contactDetails2userPubs(contactDetailsR),
          nbContactsByTopic);
    } catch (Exception re) {
      throw new YellowpagesRuntimeException("DefaultYellowpagesService.goTo()",
          SilverpeasRuntimeException.ERROR, "yellowpages.EX_GET_NODEBM_HOME_FAILED", re);
    }
  }

  @Override
  public List<NodeDetail> getTree(String instanceId) {
    List<NodeDetail> result = new ArrayList<>();
    try (Connection con = getConnection()) {
      List<NodeDetail> tree = nodeService.getSubTree(new NodePK("0", instanceId));
      // TODO :getting all groups linked in this component instance
      for (NodeDetail node : tree) {
        result.add(node);
        // pour chaque node, recuperer les groupes associes
        List<String> groupIds =
            (List<String>) GroupDAO.getGroupIds(con, node.getNodePK().getId(), instanceId);
        for (final String groupId : groupIds) {
          Group group = getOrganisationController().getGroup(groupId);
          result = addGroup(result, group, node.getLevel() + 1);
        }
      }
    } catch (Exception re) {
      throw new YellowpagesRuntimeException("DefaultYellowpagesService.getTree()",
          SilverpeasRuntimeException.ERROR, "yellowpages.EX_GET_TREE_FAILED", re);
    }
    return result;
  }

  public List<NodeDetail> addGroup(List<NodeDetail> tree, Group group, int level) {
    if (group != null) {
      NodeDetail nGroup = new NodeDetail();
      nGroup.setName(group.getName());
      nGroup.setDescription(group.getDescription());
      nGroup.getNodePK().setId("group_" + group.getId());
      nGroup.setLevel(level);
      tree.add(nGroup);

      Group[] subGroups = getOrganisationController().getAllSubGroups(group.getId());
      Group subGroup;
      for (final Group subGroup1 : subGroups) {
        subGroup = subGroup1;
        addGroup(tree, subGroup, level + 1);
      }
    }
    return tree;
  }

  /**
   * Add a subtopic to a topic - If a subtopic of same name already exists a NodePK with id=-1 is
   * returned else the new topic NodePK
   * @param father the topic Id of the future father
   * @param subTopic the NodeDetail of the new sub topic
   * @return If a subtopic of same name already exists a NodePK with id=-1 is returned else the new
   * topic NodePK
   * @see com.stratelia.webactiv.node.model.NodeDetail
   * @see com.stratelia.webactiv.node.model.NodePK
   */
  @Override
  public NodePK addToTopic(NodeDetail father, NodeDetail subTopic) {

    if (isSameTopicSameLevelOnCreation(subTopic)) {
      // a subtopic of same name already exists
      return new NodePK("-1");
    } else {
      try {
        // register form to current app
        String xmlFormName = subTopic.getModelId();
        if (StringUtil.isDefined(xmlFormName)) {
          registerTemplate(xmlFormName, father.getNodePK().getInstanceId());
        }
        return nodeService.createNode(subTopic, father);
      } catch (Exception re) {
        throw new YellowpagesRuntimeException("DefaultYellowpagesService.addToTopic()",
            SilverpeasRuntimeException.ERROR, "yellowpages.EX_GET_NODEBM_HOME_FAILED", re);
      }
    }
  }

  /**
   * When creates a new subTopic, Check if a subtopic of same name already exists
   * @param subTopic the NodeDetail of the new sub topic
   * @return true if a subtopic of same name already exists under the currentTopic else false
   * @see com.stratelia.webactiv.node.model.NodeDetail
   */
  private boolean isSameTopicSameLevelOnCreation(NodeDetail subTopic) {
    try {
      return nodeService.isSameNameSameLevelOnCreation(subTopic);
    } catch (Exception re) {
      throw new YellowpagesRuntimeException("DefaultYellowpagesService.isSameNameSameLevelOnCreation()",
          SilverpeasRuntimeException.ERROR, "yellowpages.EX_GET_NODEBM_HOME_FAILED", re);
    }
  }

  /**
   * When updates a subTopic, Check if another subtopic of same name already exists
   * @param subTopic the NodeDetail of the new sub topic
   * @return true if a subtopic of same name already exists under the currentTopic else false
   * @see com.stratelia.webactiv.node.model.NodeDetail
   */
  private boolean isSameTopicSameLevelOnUpdate(NodeDetail subTopic) {
    try {
      return nodeService.isSameNameSameLevelOnUpdate(subTopic);
    } catch (Exception re) {
      throw new YellowpagesRuntimeException("DefaultYellowpagesService.isSameTopicSameLevelOnUpdate()",
          SilverpeasRuntimeException.ERROR, "yellowpages.EX_GET_NODEBM_HOME_FAILED", re);
    }
  }

  /**
   * Update a subtopic to currentTopic and alert users - If a subtopic of same name already exists
   * a NodePK with id=-1 is returned else the new topic NodePK
   * @param topic the NodeDetail of the updated sub topic
   * @return If a subtopic of same name already exists a NodePK with id=-1 is returned else the new
   * topic NodePK
   * @see com.stratelia.webactiv.node.model.NodeDetail
   * @see com.stratelia.webactiv.node.model.NodePK
   */
  @Override
  public NodePK updateTopic(NodeDetail topic) {

    if (isSameTopicSameLevelOnUpdate(topic)) {
      // a subtopic of same name already exists
      return new NodePK("-1");
    } else {
      try {
        // register form to current app
        String xmlFormName = topic.getModelId();
        if (StringUtil.isDefined(xmlFormName)) {
          registerTemplate(xmlFormName, topic.getNodePK().getInstanceId());
        }
        nodeService.setDetail(topic);
      } catch (Exception re) {
        throw new YellowpagesRuntimeException("DefaultYellowpagesService.updateTopic()",
            SilverpeasRuntimeException.ERROR, "root.EX_UPDATE_TOPIC_FAILED", "topic = " + topic,
            re);
      }


      return topic.getNodePK();
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
    ContactPK contactPK = new ContactPK("unknown", pkToDelete);

    // Delete all entries in the table which link pub to topic
    try {
      contactService.removeAllIssue(pkToDelete, contactPK);
      unreferenceOrphanContacts(contactPK);
    } catch (Exception re) {
      throw new YellowpagesRuntimeException("DefaultYellowpagesService.deleteTopic()",
          SilverpeasRuntimeException.ERROR, "yellowpages.EX_DELETE_CONTACTS_FAILED",
          "pkToDelete=" + pkToDelete.toString(), re);
    }

    // Delete the topic
    try {
      nodeService.removeNode(pkToDelete);
    } catch (Exception re) {
      throw new YellowpagesRuntimeException("DefaultYellowpagesService.deleteTopic()",
          SilverpeasRuntimeException.ERROR, "root.EX_DELETE_TOPIC_FAILED",
          "pk = " + pkToDelete.toString(), re);
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
      throw new YellowpagesRuntimeException("DefaultYellowpagesService.getContactDetail()",
          SilverpeasRuntimeException.ERROR, "root.EX_GET_CONTACT_FAILED",
          "pk = " + contactPK.toString(), re);
    }
  }

  private void setContactAttributes(ContactDetail contactDetail, UserDetail userDetail,
      boolean filterExtraInfos) {
    contactDetail.setFirstName(userDetail.getFirstName());
    contactDetail.setLastName(userDetail.getLastName());
    contactDetail.setEmail(userDetail.geteMail());
    contactDetail.setUserExtraDataRequired(!filterExtraInfos || areUserExtraDataRequired());
  }

  @Override
  public Collection<ContactDetail> getContactDetailsByLastName(ContactPK pk, String query) {

    try {
      return contactService.getDetailsByLastName(pk, query);
    } catch (Exception e) {
      throw new YellowpagesRuntimeException("DefaultYellowpagesService.getContactDetailsByLastName()",
          SilverpeasRuntimeException.ERROR, "root.EX_GET_CONTACTS_FAILED", e);
    }
  }

  @Override
  public Collection<ContactDetail> getContactDetailsByLastNameOrFirstName(ContactPK pk,
      String query) {


    Collection<ContactDetail> contactDetails;
    try {
      contactDetails = contactService.getDetailsByLastNameOrFirstName(pk, query);
    } catch (Exception e) {
      throw new YellowpagesRuntimeException(
          "DefaultYellowpagesService.getContactDetailsByLastNameOrFirstName()",
          SilverpeasRuntimeException.ERROR, "root.EX_GET_CONTACTS_FAILED", e);
    }
    return contactDetails;
  }

  @Override
  public Collection<ContactDetail> getContactDetailsByLastNameAndFirstName(ContactPK pk,
      String lastName, String firstName) {

    try {
      return contactService.getDetailsByLastNameAndFirstName(pk, lastName, firstName);
    } catch (Exception e) {
      throw new YellowpagesRuntimeException(
          "DefaultYellowpagesService.getContactDetailsByLastNameAndFirstName()",
          SilverpeasRuntimeException.ERROR, "root.EX_GET_CONTACTS_FAILED", e);
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
      ContactPK pk = new ContactPK("unknown", nodePK);
      Collection<ContactFatherDetail> contactDetails =
          contactService.getDetailsByFatherPKs(nodePKsWithout12, pk, nodePK);
      if (contactDetails != null) {
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

      return contactDetailsR;
    } catch (Exception re) {
      throw new YellowpagesRuntimeException("DefaultYellowpagesService.getAllContactDetails()",
          SilverpeasRuntimeException.ERROR, "root.EX_GET_CONTACTS_FAILED", re);
    }
  }

  /**
   * Return list of all path to this contact - it's a Collection of NodeDetail collection
   * @param contactPK the id of the contact
   * @return a Collection of NodeDetail collection
   * @see com.stratelia.webactiv.node.model.NodeDetail
   */
  @Override
  public List<Collection<NodeDetail>> getPathList(ContactPK contactPK) {

    Collection<NodePK> fatherPKs;
    try {
      // get all nodePK whick contains this contact
      fatherPKs = contactService.getAllFatherPK(contactPK);
    } catch (Exception re) {
      throw new YellowpagesRuntimeException("DefaultYellowpagesService.getPathList()",
          SilverpeasRuntimeException.ERROR, "yellowpages.EX_GET_CONTACTBM_HOME_FAILED", re);
    }
    try {
      List<Collection<NodeDetail>> pathList = new ArrayList<>();
      if (fatherPKs != null) {
        // For each topic, get the path to it
        for (NodePK pk : fatherPKs) {
          Collection<NodeDetail> path = nodeService.getAnotherPath(pk);
          // add this path
          pathList.add(path);
        }
      }

      return pathList;
    } catch (Exception re) {
      throw new YellowpagesRuntimeException("DefaultYellowpagesService.getPathList()",
          SilverpeasRuntimeException.ERROR, "yellowpages.EX_GET_NODEBM_HOME_FAILED", re);
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
      throw new YellowpagesRuntimeException("DefaultYellowpagesService.createContact()",
          SilverpeasRuntimeException.ERROR, "yellowpages.EX_CREATE_CONTACT_FAILED", re);
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
      throw new YellowpagesRuntimeException("DefaultYellowpagesService.updateContact()",
          SilverpeasRuntimeException.ERROR, "yellowpages.EX_UPDATE_CONTACT_FAILED", re);
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
        throw new YellowpagesRuntimeException("DefaultYellowpagesService.deleteContact()",
            SilverpeasRuntimeException.ERROR, "yellowpages.EX_DELETE_CONTACT_FAILED", re);
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
      throw new YellowpagesRuntimeException("DefaultYellowpagesService.sendContactToBasket()",
          SilverpeasRuntimeException.ERROR, "yellowpages.EX_SEND_CONTACT_TO_BASKET_FAILED", re);
    }
  }

  @Override
  public void emptyDZByUserId(String instanceId, String userId) {
    ContactPK contactPK = new ContactPK(null, instanceId);
    try {
      // delete all current user orphan contacts
      contactService.deleteOrphanContactsByCreatorId(contactPK, userId);
    } catch (Exception re) {
      throw new YellowpagesRuntimeException("DefaultYellowpagesService.emptyDZByUserId()",
          SilverpeasRuntimeException.ERROR, "yellowpages.EX_DELETE_ORPHEAN_CONTACTS_FAILED", re);
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
      throw new YellowpagesRuntimeException("DefaultYellowpagesService.addContactToTopic()",
          SilverpeasRuntimeException.ERROR, "yellowpages.EX_ADD_CONTACT_TO_TOPIC_FAILED", re);
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
      throw new YellowpagesRuntimeException("DefaultYellowpagesService.deleteContactFromTopic()",
          SilverpeasRuntimeException.ERROR, "yellowpages.EX_DELETE_CONTACT_FROM_TOPIC_FAILED", re);
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
      throw new YellowpagesRuntimeException("DefaultYellowpagesService.createInfoModel()",
          SilverpeasRuntimeException.ERROR, "yellowpages.EX_CREATE_INFO_MODEL_DETAIL_FAILED", re);
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
      throw new YellowpagesRuntimeException("DefaultYellowpagesService.getCompleteContactInNode()",
          SilverpeasRuntimeException.ERROR, "yellowpages.EX_GET_CONTACT_FAILED", re);
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
        for (ContactDetail contactDetail : contacts) {
          if (contactDetail.getUserId() != null) {
            // contact de type user Silverpeas
            try {
              OrganizationController orga = getOrganisationController();
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
              SilverLogger.getLogger(this)
                  .error("contactDetail.getUserId() = " + contactDetail.getUserId(), e);
            }
          } else {
            contactDetailsR.add(contactDetail);
          }
        }
      }
    } catch (Exception re) {
      throw new YellowpagesRuntimeException("DefaultYellowpagesService.getContacts()",
          SilverpeasRuntimeException.ERROR, "yellowpages.EX_GET_ALL_CONTACTS_FAILED", re);
    }

    return contactDetails2userPubs(contactDetailsR);
  }

  @Override
  public Collection<NodePK> getContactFathers(ContactPK contactPK) {
    try {
      // fetch contact fathers
      return contactService.getAllFatherPK(contactPK);
    } catch (Exception re) {
      throw new YellowpagesRuntimeException("DefaultYellowpagesService.getContactFathers()",
          SilverpeasRuntimeException.ERROR, "yellowpages.EX_GET_CONTACT_FATHERS_FAILED", re);
    }
  }

  public void unreferenceOrphanContacts(ContactPK contactPK) {
    try {
      Collection<ContactDetail> orphanContacts = contactService.getOrphanContacts(contactPK);
      for (ContactDetail contactDetail : orphanContacts) {
        // add link between this contact and the basket topic
        contactService.addFather(contactDetail.getPK(), new NodePK(NodePK.BIN_NODE_ID, contactPK));
        deleteIndex(contactDetail.getPK());
      }
    } catch (Exception e) {
      throw new YellowpagesRuntimeException("DefaultYellowpagesService.unreferenceOrphanContacts()",
          SilverpeasRuntimeException.ERROR, "yellowpages.EX_UNREFERENCE_ORPHEAN_CONTACTS_FAILED",
          e);
    }
  }

  private void deleteIndex(ContactPK contactPK) {
    contactService.deleteIndex(contactPK);
  }

  @Override
  public List<String> getGroupIds(NodePK pk) {
    try (Connection con = getConnection()) {
      return (List<String>) GroupDAO.getGroupIds(con, pk.getId(), pk.getInstanceId());
    } catch (Exception e) {
      throw new YellowpagesRuntimeException("DefaultYellowpagesService.addGroup()",
          SilverpeasRuntimeException.ERROR, "yellowpages.EX_UNREFERENCE_ORPHEAN_CONTACTS_FAILED",
          e);
    }
  }

  @Override
  public void addGroup(String groupId, NodePK nodePK) {
    try (Connection con = getConnection()) {
      GroupDAO.addGroup(con, groupId, nodePK.getId(), nodePK.getInstanceId());
    } catch (Exception e) {
      throw new YellowpagesRuntimeException("DefaultYellowpagesService.addGroup()",
          SilverpeasRuntimeException.ERROR, "yellowpages.EX_UNREFERENCE_ORPHEAN_CONTACTS_FAILED",
          e);
    }
  }

  @Override
  public void removeGroup(String groupId) {
    Connection con = getConnection();
    try {
      GroupDAO.removeGroup(con, groupId);
    } catch (Exception e) {
      throw new YellowpagesRuntimeException("DefaultYellowpagesService.removeGroup()",
          SilverpeasRuntimeException.ERROR, "yellowpages.EX_REMOVE_GROUP_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void removeGroup(String groupId, NodePK nodePK) {
    try (Connection con = getConnection()) {
      GroupDAO.removeGroup(con, groupId, nodePK.getId(), nodePK.getInstanceId());
    } catch (Exception e) {
      throw new YellowpagesRuntimeException("DefaultYellowpagesService.removeGroup()",
          SilverpeasRuntimeException.ERROR, "yellowpages.EX_REMOVE_GROUP_FAILED", e);
    }
  }

  private Connection getConnection() {
    // initialisation de la connexion
    try {
      return DBUtil.openConnection();
    } catch (SQLException e) {
      // traitement des exceptions
      throw new YellowpagesRuntimeException("DefaultYellowpagesService.getConnection()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
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
      throw new YellowpagesRuntimeException("DefaultYellowpagesService.addModelUsed()",
          SilverpeasRuntimeException.ERROR, "kmelia.IMPOSSIBLE_D_AJOUTER_LES_MODELES", e);
    }
  }

  @Override
  public Collection<String> getModelUsed(String instanceId) {
    try (Connection con = getConnection()) {
      return ModelDAO.getModelUsed(con, instanceId);
    } catch (Exception e) {
      throw new YellowpagesRuntimeException("DefaultYellowpagesService.getModelUsed()",
          SilverpeasRuntimeException.ERROR, "kmelia.IMPOSSIBLE_DE_RECUPERER_LES_MODELES", e);
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
