/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.yellowpages.control;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.RemoveException;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.form.AbstractForm;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.form.TypeManager;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.csv.CSVReader;
import com.silverpeas.util.csv.Variant;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.webactiv.beans.admin.CompoSpace;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.searchEngine.control.ejb.SearchEngineBm;
import com.stratelia.webactiv.searchEngine.control.ejb.SearchEngineBmHome;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.contact.model.CompleteContact;
import com.stratelia.webactiv.util.contact.model.ContactDetail;
import com.stratelia.webactiv.util.contact.model.ContactFatherDetail;
import com.stratelia.webactiv.util.contact.model.ContactPK;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilTrappedException;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.yellowpages.YellowpagesException;

import com.stratelia.webactiv.yellowpages.control.ejb.YellowpagesBm;
import com.stratelia.webactiv.yellowpages.control.ejb.YellowpagesBmHome;
import com.stratelia.webactiv.yellowpages.model.GroupDetail;
import com.stratelia.webactiv.yellowpages.model.TopicDetail;
import com.stratelia.webactiv.yellowpages.model.UserCompleteContact;
import com.stratelia.webactiv.yellowpages.model.UserContact;
import com.stratelia.webactiv.yellowpages.model.YellowpagesRuntimeException;

public class YellowpagesSessionController extends
    AbstractComponentSessionController {

  private YellowpagesBm kscEjb = null;
  private SearchEngineBm searchEngineEjb = null;
  private TopicDetail currentTopic = null;
  private UserCompleteContact currentContact = null;
  private String path = null;
  private String owner = "false";
  private String profile; // admin || publisher || user
  private List<GroupDetail> groupPath = new ArrayList<GroupDetail>();
  private boolean portletMode = false;
  private Collection<ContactFatherDetail> currentContacts = null;
  private Collection<UserFull> currentFullUsers = null; // liste de UserFull
  private Collection<UserCompleteContact> currentCompleteUsers = null;
  private String currentTypeSearch;
  private String currentSearchCriteria;
  public static String GroupReferentielPrefix = "group_";
  private ResourceLocator domainMultilang;
  
  /** Creates new sessionClientController */
  public YellowpagesSessionController(MainSessionController mainSessionCtrl,
      ComponentContext context) {
    super(mainSessionCtrl, context,
        "com.stratelia.webactiv.yellowpages.multilang.yellowpagesBundle",
        "com.stratelia.webactiv.yellowpages.settings.yellowpagesIcons",
        "com.stratelia.webactiv.yellowpages.settings.yellowpagesSettings");
    // super(mainSessionCtrl, context,
    // "com.stratelia.webactiv.yellowpages.multilang.yellowpagesBundle");
    initEJB();
    setProfile();
    String domainId = getSettings().getString("columns.domainId");
    if (StringUtil.isDefined(domainId)) {
      Domain domain = getOrganizationController().getDomain(domainId);
      ResourceLocator domainProperty = new ResourceLocator(domain.getPropFileName(), "");
      domainMultilang = new ResourceLocator(domainProperty.getString("property.ResourceFile"), "");
    }
  }

  private void initEJB() {
    // 1 - Remove all data store by this SessionController (includes EJB)
    kscEjb = null;
    removeSessionTopic();
    removeSessionPublication();
    removeSessionPath();
    removeSessionOwner();

    // 2 - Init EJB used by this SessionController
    try {
      setYellowpagesBm();
      setPrefixTableName(getSpaceId());
      setCurrentUser(getUserDetail());
      setComponentId(getComponentId());
    } catch (Exception e) {
      throw new YellowpagesRuntimeException(
          "YellowpagesSessionController.initEJB()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  private void setYellowpagesBm() {
    if (kscEjb == null) {
      try {
        YellowpagesBmHome kscEjbHome = (YellowpagesBmHome) EJBUtilitaire
            .getEJBObjectRef(JNDINames.YELLOWPAGESBM_EJBHOME,
            YellowpagesBmHome.class);
        kscEjb = kscEjbHome.create();
      } catch (Exception e) {
        throw new YellowpagesRuntimeException(
            "YellowpagesSessionController.setYellowpagesBm()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
            e);
      }
    }
  }

  public synchronized void setPrefixTableName(String prefixTableName)
      throws RemoteException {
    kscEjb.setPrefixTableName(prefixTableName);
  }

  /************************************************************************************************/
  // Current Space operations
  /************************************************************************************************/
  public synchronized void setSpaceId(String prefixTableName)
      throws RemoteException {
    try {
      kscEjb.setPrefixTableName(prefixTableName);
    } catch (NoSuchObjectException nsoe) {
      initEJB();
      setSpaceId(prefixTableName);
    }
  }

  /************************************************************************************************/
  // Current Component operations
  /************************************************************************************************/
  public synchronized void setComponentId(String componentId)
      throws RemoteException {
    try {
      kscEjb.setComponentId(componentId);
    } catch (NoSuchObjectException nsoe) {
      initEJB();
      setComponentId(componentId);
    }
  }

  /************************************************************************************************/
  // Current User operations
  /************************************************************************************************/
  public synchronized void setCurrentUser(UserDetail user)
      throws RemoteException {
    try {
      kscEjb.setActor(user);
    } catch (NoSuchObjectException nsoe) {
      initEJB();
      setCurrentUser(user);
    }
  }

  private String getFlag(String[] profiles) {
    String flag = "user";
    for (int i = 0; i < profiles.length; i++) {
      // if admin, return it, we won't find a better profile
      if (profiles[i].equals("admin")) {
        return profiles[i];
      }
      if (profiles[i].equals("publisher")) {
        flag = profiles[i];
      }
    }
    return flag;
  }

  public void setProfile() {
    profile = getFlag(getUserRoles());
  }

  public String getProfile() {
    return profile;
  }

  public void setCurrentTopic(TopicDetail currentTopic) {
    this.currentTopic = currentTopic;
  }

  public TopicDetail getCurrentTopic() {
    return this.currentTopic;
  }

  public void setCurrentContact(UserCompleteContact currentContact) {
    this.currentContact = currentContact;
  }

  public UserCompleteContact getCurrentContact() {
    return this.currentContact;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getPath() {
    return this.path;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getOwner() {
    return this.owner;
  }

  public synchronized YellowpagesBm getKSCEJB() {
    return kscEjb;
  }

  public SearchEngineBm getSearchEngine() {
    if (this.searchEngineEjb == null) {
      try {
        SearchEngineBmHome home = (SearchEngineBmHome) EJBUtilitaire
            .getEJBObjectRef(JNDINames.SEARCHBM_EJBHOME,
            SearchEngineBmHome.class);
        this.searchEngineEjb = home.create();
      } catch (Exception e) {
        throw new EJBException(e.getMessage());
      }
    }
    return this.searchEngineEjb;
  }

  /**************************************************************************************/
  /* YELLOWPAGES - Gestion des th�mes */
  /**************************************************************************************/
  public synchronized TopicDetail getTopic(String id) throws RemoteException {
    try {
      return kscEjb.goTo(id);
    } catch (NoSuchObjectException nsoe) {
      initEJB();
      return getTopic(id);
    }
  }

  public GroupDetail getGroup(String groupId) throws RemoteException {
    Group group = getOrganizationController().getGroup(groupId);
    GroupDetail groupDetail = new GroupDetail(group);

    // add sub groups
    Group[] subGroups = getOrganizationController().getAllSubGroups(groupId);
    Group subGroup = null;
    GroupDetail subGroupDetail = null;
    for (int g = 0; g < subGroups.length; g++) {
      subGroup = subGroups[g];
      subGroupDetail = new GroupDetail(subGroup);
      subGroupDetail.setTotalUsers(getOrganizationController()
          .getAllSubUsersNumber(subGroup.getId()));
      groupDetail.addSubGroup(subGroupDetail);
    }

    // add users
    String[] userIds = group.getUserIds();
    String userId = null;
    UserDetail user = null;
    for (int u = 0; u < userIds.length; u++) {
      userId = userIds[u];
      user = getOrganizationController().getUserDetail(userId);
      if (user != null) {
        groupDetail.addUser(user);
      }
    }

    processGroupPath(groupDetail);

    return groupDetail;
  }

  private void processGroupPath(GroupDetail group) {
    if (groupPath.size() == 0) {
      groupPath.add(group);
    } else {
      int index = groupPath.indexOf(group);
      SilverTrace.info("yellowpages",
          "YellowpagesSessionController.processGroupPath()",
          "root.MSG_GEN_PARAM_VALUE", "index = " + index);
      if (index == -1) {
        groupPath.add(group);
      } else {
        groupPath = groupPath.subList(0, index + 1);
      }
    }
  }

  public List<GroupDetail> getGroupPath() {
    return groupPath;
  }

  public void clearGroupPath() {
    groupPath.clear();
  }

  public synchronized List<NodeDetail> getTree() throws RemoteException {
    try {
      return kscEjb.getTree();
    } catch (NoSuchObjectException nsoe) {
      initEJB();
      return getTree();
    }
  }

  public synchronized NodePK updateTopicHeader(NodeDetail nd)
      throws RemoteException {
    SilverTrace.info("yellowpages",
        "YellowpagesSessionController.updateTopicHeader()",
        "root.MSG_GEN_PARAM_VALUE", "id = " + nd.getNodePK().getId());
    try {
      return kscEjb.updateTopic(nd);
    } catch (NoSuchObjectException nsoe) {
      initEJB();
      return updateTopicHeader(nd);
    }
  }

  public synchronized NodeDetail getSubTopicDetail(String subTopicId)
      throws RemoteException {
    try {
      return kscEjb.getSubTopicDetail(subTopicId);
    } catch (NoSuchObjectException nsoe) {
      initEJB();
      return getSubTopicDetail(subTopicId);
    }
  }

  public synchronized NodePK addSubTopic(NodeDetail nd) throws RemoteException {
    try {
      return kscEjb.addSubTopic(nd);
    } catch (NoSuchObjectException nsoe) {
      initEJB();
      return addSubTopic(nd);
    }
  }

  public synchronized void deleteTopic(String topicId) throws RemoteException {
    SilverTrace.info("yellowpages",
        "YellowpagesSessionController.deleteTopic()",
        "root.MSG_GEN_PARAM_VALUE", "topicId = " + topicId);

    try {
      kscEjb.deleteTopic(topicId);
      resetCurrentFullCompleteUsers();
    } catch (NoSuchObjectException nsoe) {
      initEJB();
      deleteTopic(topicId);
    }
  }

  public synchronized void emptyBasketByUserId() throws RemoteException {
    try {
      kscEjb.emptyBasketByUserId();
    } catch (NoSuchObjectException nsoe) {
      initEJB();
      emptyBasketByUserId();
    }
  }

  public synchronized void emptyPublisherDZ() throws RemoteException {
    try {
      kscEjb.emptyDZByUserId();
    } catch (NoSuchObjectException nsoe) {
      initEJB();
      emptyPublisherDZ();
    }
  }

  /**************************************************************************************/
  /* Yellowpages - Gestion des contacts */
  /**************************************************************************************/
  public synchronized ContactDetail getContactDetail(String contactId)
      throws RemoteException {
    try {
      return kscEjb.getContactDetail(contactId);
    } catch (NoSuchObjectException nsoe) {
      initEJB();
      return getContactDetail(contactId);
    }
  }

  private synchronized void resetCurrentFullCompleteUsers() {
    this.currentFullUsers = null;
    this.currentCompleteUsers = null;
  }

  private synchronized Collection<ContactFatherDetail> setCurrentFullCompleteUsers()
      throws RemoteException {// tous les contacts � la racine
    try {
      // racine
      TopicDetail rootTopic = getTopic("0");
      Collection<ContactFatherDetail> contacts = getAllContactDetails(rootTopic.getNodePK());

      if (this.currentFullUsers == null || this.currentCompleteUsers == null) {

        Iterator<ContactFatherDetail> itContact = contacts.iterator();
        ContactFatherDetail contact;
        UserFull userFull;
        ArrayList<UserFull> listUserFull = new ArrayList<UserFull>();
        UserCompleteContact userComplete;
        ArrayList<UserCompleteContact> listUserComplete = new ArrayList<UserCompleteContact>();
        while (itContact.hasNext()) {
          contact = (ContactFatherDetail) itContact.next();
          if (contact.getNodeId() != null
              && contact.getNodeId().startsWith(
              YellowpagesSessionController.GroupReferentielPrefix)
              && contact.getContactDetail().getUserId() != null) {// contact de
            // type user
            // appartenant
            // � un
            // groupe
            // Silverpeas
            userFull = this.getOrganizationController().getUserFull(
                contact.getContactDetail().getUserId());
            if (userFull != null) {
              listUserFull.add(userFull);
            }
          } else {// contacts annuaire interne et externe
            userComplete = getCompleteContactInNode(contact.getContactDetail()
                .getPK().getId(), contact.getNodeId());
            listUserComplete.add(userComplete);
          }
        }

        this.currentFullUsers = listUserFull;
        this.currentCompleteUsers = listUserComplete;

      }

      return contacts;
    } catch (NoSuchObjectException nsoe) {
      initEJB();
      return setCurrentFullCompleteUsers();
    }
  }

  public synchronized Collection<ContactFatherDetail> getAllContactDetails(NodePK fatherPK)
      throws RemoteException {
    try {
      Collection<ContactFatherDetail> contacts = kscEjb.getAllContactDetails(fatherPK);
      if (contacts != null) {
        // contacts.addAll(getAllUsers(fatherPK.getId()));

        // get users of groups contained in subtree
        List<NodeDetail> tree = getNodeBm().getSubTree(
            new NodePK(fatherPK.getId(), getComponentId()));
        for (int t = 0; tree != null && t < tree.size(); t++) {
          NodeDetail node = tree.get(t);
          contacts.addAll(getAllUsers(node.getNodePK().getId()));
        }
      } else {
        contacts = new ArrayList<ContactFatherDetail>(getAllUsers(fatherPK.getId()));
      }

      return contacts;
    } catch (NoSuchObjectException nsoe) {
      initEJB();
      return getAllContactDetails(fatherPK);
    }
  }

  public synchronized Collection<NodeDetail> getPathList(String contactId)
      throws RemoteException {
    try {
      return kscEjb.getPathList(contactId);
    } catch (NoSuchObjectException nsoe) {
      initEJB();
      return getPathList(contactId);
    }
  }

  public synchronized String createContact(ContactDetail contactDetail)
      throws RemoteException {
    try {
      String contactId = kscEjb.createContact(contactDetail);
      resetCurrentFullCompleteUsers();
      return contactId;
    } catch (NoSuchObjectException nsoe) {
      initEJB();
      return createContact(contactDetail);
    }
  }

  public synchronized void updateContact(ContactDetail contactDetail)
      throws RemoteException {
    try {
      kscEjb.updateContact(contactDetail);
      resetCurrentFullCompleteUsers();
    } catch (NoSuchObjectException nsoe) {
      initEJB();
      updateContact(contactDetail);
    }
  }

  public synchronized void deleteContact(String contactId)
      throws RemoteException, PublicationTemplateException, FormException {
    try {
      // delete donnees formulaires XML
      UserCompleteContact userCompleteContact = getCompleteContact(contactId);
      String modelId = userCompleteContact.getContact().getModelId();
      if (StringUtil.isDefined(modelId) && modelId.endsWith(".xml")) {
        String xmlFormName = modelId;
        String xmlFormShortName = xmlFormName.substring(xmlFormName
            .indexOf("/") + 1, xmlFormName.indexOf("."));

        // recuperation des donnees du formulaire (via le DataRecord)
        PublicationTemplate pubTemplate = PublicationTemplateManager
            .getPublicationTemplate(getComponentId() + ":" + xmlFormShortName);

        RecordSet recordSet = pubTemplate.getRecordSet();
        DataRecord data = recordSet.getRecord(contactId);
        recordSet.delete(data);
      }

      // delete contact
      kscEjb.deleteContact(contactId);
      resetCurrentFullCompleteUsers();
    } catch (NoSuchObjectException nsoe) {
      initEJB();
      deleteContact(contactId);
    }
  }

  public synchronized void addContactToTopic(String contactId, String fatherId)
      throws RemoteException {
    try {
      kscEjb.addContactToTopic(contactId, fatherId);
      resetCurrentFullCompleteUsers();
    } catch (NoSuchObjectException nsoe) {
      initEJB();
      addContactToTopic(contactId, fatherId);
    }
  }

  public synchronized void deleteContactFromTopic(String contactId,
      String fatherId) throws RemoteException {
    try {
      kscEjb.deleteContactFromTopic(contactId, fatherId);
    } catch (NoSuchObjectException nsoe) {
      initEJB();
      deleteContactFromTopic(contactId, fatherId);
    }
  }

  public synchronized void createInfoModel(String contactId, String modelId)
      throws RemoteException {
    try {
      kscEjb.createInfoModel(contactId, modelId);
      resetCurrentFullCompleteUsers();
    } catch (NoSuchObjectException nsoe) {
      initEJB();
      createInfoModel(contactId, modelId);
    }
  }

  public synchronized UserCompleteContact getCompleteContact(String contactId)
      throws RemoteException {
    try {
      return kscEjb.getCompleteContact(contactId);
    } catch (NoSuchObjectException nsoe) {
      initEJB();
      return getCompleteContact(contactId);
    }
  }

  public synchronized UserCompleteContact getCompleteContactInNode(
      String contactId, String nodeId) throws RemoteException {
    try {
      return kscEjb.getCompleteContactInNode(contactId, nodeId);
    } catch (NoSuchObjectException nsoe) {
      initEJB();
      return getCompleteContactInNode(contactId, nodeId);
    }
  }

  public synchronized TopicDetail getContactFather(String contactId)
      throws RemoteException {
    try {
      return kscEjb.getContactFather(contactId);
    } catch (NoSuchObjectException nsoe) {
      initEJB();
      return getContactFather(contactId);
    }
  }

  public synchronized Collection<NodePK> getContactFathers(String contactId)
      throws RemoteException {
    try {
      return kscEjb.getContactFathers(contactId);
    } catch (NoSuchObjectException nsoe) {
      initEJB();
      return getContactFathers(contactId);
    }
  }

  public synchronized void deleteContactFathers(String contactId)
      throws RemoteException {
    String fatherId;
    try {
      Collection<NodePK> fathers = kscEjb.getContactFathers(contactId);
      if (fathers != null) {
        Iterator<NodePK> it = fathers.iterator();
        while (it.hasNext()) {
          fatherId = ((NodePK) it.next()).getId();
          deleteContactFromTopic(contactId, fatherId);
        }
      }
      resetCurrentFullCompleteUsers();
    } catch (NoSuchObjectException nsoe) {
      initEJB();
      deleteContactFathers(contactId);
    }
  }

  public synchronized boolean isDescendant(String descId, String nodeId)
      throws RemoteException {
    try {
      return kscEjb.isDescendant(descId, nodeId);
    } catch (NoSuchObjectException nsoe) {
      initEJB();
      return isDescendant(descId, nodeId);
    }
  }

  /**************************************************************************************/
  /* Yellowpages - Gestion des Liens */
  /**************************************************************************************/
  // return a ContactDetail collection
  public synchronized Collection<UserContact> getContacts(Collection<String> targetIds)
      throws RemoteException {
    try {
      return kscEjb.getContacts(targetIds);
    } catch (NoSuchObjectException nsoe) {
      initEJB();
      return getContacts(targetIds);
    }
  }

  /**
   * methods for Users
   */
  public UserDetail[] getUserList() {
    return getOrganizationController().getAllUsers();
  }

  public UserDetail getUserDetail(String userId) {
    return getOrganizationController().getUserDetail(userId);
  }

  /**
   * get others instances of yellowpages
   */
  public CompoSpace[] getYellowPagesInstances() {
    CompoSpace[] compoSpaces = getOrganizationController().getCompoForUser(
        getUserId(), "yellowpages");
    return compoSpaces;
  }

  public void removeSessionTopic() {
    setCurrentTopic(null);
  }

  public void removeSessionPublication() {
    setCurrentContact(null);
  }

  public void removeSessionPath() {
    setPath(null);
  }

  public void removeSessionOwner() {
    setOwner("false");
  }

  /**************************************************************************************/
  /* Yellowpages - Gestion du UserPanel */
  /**************************************************************************************/
  /**
   * Param�tre le userPannel => tous les users, s�lection d'un seul user
   * @param
   * @return
   * @throws
   * @see
   */
  public String initUserPanel() {
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");
    String hostSpaceName = getSpaceLabel();
    PairObject hostComponentName = new PairObject(getComponentLabel(), "");
    PairObject[] hostPath = new PairObject[1];
    hostPath[0] = new PairObject(getString("UserCreer"), "");
    String hostUrl = m_context + URLManager.getURL(null, getComponentId())
        + "saveUser";
    String cancelUrl = m_context + URLManager.getURL(null, getComponentId())
        + "/saveUser";

    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName(hostSpaceName);
    sel.setHostComponentName(hostComponentName);
    sel.setHostPath(hostPath);

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);

    // Contraintes
    sel.setMultiSelect(false);
    sel.setPopupMode(true);
    sel.setSetSelectable(false);

    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  /**
   * Met en session le contact s�lectionn� via le userPanel
   * @param
   * @throws
   * @see setCurrentContact
   */
  public void setContactUserSelected() {
    String selUser = getSelection().getFirstSelectedElement();
    if ((selUser != null) && (selUser.length() > 0)) {
      UserDetail selectedUser = getOrganizationController().getUserDetail(
          selUser);
      String firstName = selectedUser.getFirstName();
      String lastName = selectedUser.getLastName();
      String email = selectedUser.geteMail();
      String userId = selectedUser.getId();

      String phone = null;
      String fax = null;
      UserFull userFull = getOrganizationController().getUserFull(userId);
      if (userFull != null) {
        phone = userFull.getValue("phone", "");
        fax = userFull.getValue("fax", "");
      }

      ContactDetail contactDetail = new ContactDetail("X", firstName, lastName,
          email, phone, fax, userId, null, null);
      UserCompleteContact userContactComplete = new UserCompleteContact(null,
          new CompleteContact(contactDetail, null));
      setCurrentContact(userContactComplete);
    } // fin if
  }

  public String initGroupPanel() {
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");
    String hostSpaceName = getSpaceLabel();
    PairObject hostComponentName = new PairObject(getComponentLabel(), "");
    PairObject[] hostPath = new PairObject[1];
    hostPath[0] = new PairObject(getString("GroupAdd"), "");
    String hostUrl = m_context + URLManager.getURL(null, getComponentId())
        + "AddGroup";
    String cancelUrl = m_context + URLManager.getURL(null, getComponentId())
        + "AddGroup";

    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName(hostSpaceName);
    sel.setHostComponentName(hostComponentName);
    sel.setHostPath(hostPath);

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);

    // Contraintes
    sel.setMultiSelect(false);
    sel.setPopupMode(true);
    sel.setSetSelectable(true);
    sel.setElementSelectable(false);

    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  public void setGroupSelected() throws RemoteException {
    String sel = getSelection().getFirstSelectedSet();
    if (sel != null && sel.length() > 0) {
      addGroup(sel);
    }
  }

  public List<ContactFatherDetail> getAllUsers(String nodeId) throws RemoteException {
    List<ContactFatherDetail> users = new ArrayList<ContactFatherDetail>();

    List<String> groupIds = kscEjb.getGroupIds(nodeId);

    String groupId = null;
    for (int g = 0; g < groupIds.size(); g++) {
      groupId = (String) groupIds.get(g);
      users.addAll(getAllUsersOfGroup(groupId));
    }

    return users;
  }

  public List<UserDetail> getAllUserDetails(String nodeId) throws RemoteException {
    List<UserDetail> users = new ArrayList<UserDetail>();

    List<String> groupIds = kscEjb.getGroupIds(nodeId);

    String groupId = null;
    for (int g = 0; g < groupIds.size(); g++) {
      groupId = (String) groupIds.get(g);

      UserDetail[] userDetails = getOrganizationController()
          .getAllUsersOfGroup(groupId);

      users.addAll(Arrays.asList(userDetails));
    }

    return users;
  }

  public List<ContactFatherDetail> getAllUsersOfGroup(String groupId) throws RemoteException {
    List<ContactFatherDetail> users = new ArrayList<ContactFatherDetail>();

    GroupDetail groupDetail = getGroup(groupId);
    UserDetail[] userDetails = getOrganizationController()
        .getFiltredDirectUsers(groupId, "");

    UserDetail userDetail = null;
    for (int u = 0; u < userDetails.length; u++) {
      userDetail = userDetails[u];
      ContactFatherDetail contactFather = getContactFatherDetail(userDetail
          .getId(), groupDetail);
      if (contactFather != null) {
        users.add(contactFather);
      }
    }

    List<GroupDetail> sousGroupes = groupDetail.getSubGroups();
    GroupDetail sousGroupe;
    for (int i = 0; i < sousGroupes.size(); i++) {
      sousGroupe = (GroupDetail) sousGroupes.get(i);
      users.addAll(getAllUsersOfGroup(sousGroupe.getId()));
    }

    return users;
  }

  private ContactFatherDetail getContactFatherDetail(String userId,
      GroupDetail group) {
    ContactFatherDetail contactFather = null;
    if (this.getSettings().getString("columns").contains("domain.")) {
      UserFull user = getOrganizationController().getUserFull(userId);
      if (user != null) {
        ContactDetail cUser = new ContactDetail(new ContactPK("fromGroup",
            null, getComponentId()), user.getFirstName(), user.getLastName(),
            user.geteMail(), user.getValue("phone"), user.getValue("fax"), user
            .getId(), null, null);
        cUser.setUserFull(user);

        contactFather = new ContactFatherDetail(
            cUser,
            YellowpagesSessionController.GroupReferentielPrefix + group.getId(),
            group.getName());
      }
    } else {
      UserDetail user = getOrganizationController().getUserDetail(userId);
      if (user != null) {
        ContactDetail cUser = new ContactDetail(new ContactPK("fromGroup",
            null, getComponentId()), user.getFirstName(), user.getLastName(),
            user.geteMail(), "", "", user.getId(), null, null);

        contactFather = new ContactFatherDetail(
            cUser,
            YellowpagesSessionController.GroupReferentielPrefix + group.getId(),
            group.getName());
      }
    }
    return contactFather;
  }

  public void addGroup(String groupId) throws RemoteException {
    SilverTrace.info("yellowpages", "YellowpagesSessionController.addGroup()",
        "root.MSG_GEN_ENTER_METHOD", "groupId = " + groupId);
    kscEjb.addGroup(groupId);
  }

  public void removeGroup(String groupId) throws RemoteException {
    kscEjb.removeGroup(groupId);
    resetCurrentFullCompleteUsers();
  }

  public void resetCurrentTypeSearchCriteria() {
    this.currentTypeSearch = null;
    this.currentSearchCriteria = null;
  }

  public void setCurrentTypeSearch(String typeSearch) {
    this.currentTypeSearch = typeSearch;
  }

  public void setCurrentSearchCriteria(String searchCriteria) {
    this.currentSearchCriteria = searchCriteria;
  }

  public String getCurrentTypeSearch() {
    return this.currentTypeSearch;
  }

  public String getCurrentSearchCriteria() {
    return this.currentSearchCriteria;
  }

  /**
   * @param query
   * @return list of UserFull
   */
  private List<UserFull> searchFullUsers(String query) {
    query = query.toLowerCase();
    Iterator<UserFull> it = this.currentFullUsers.iterator();
    List<UserFull> result = new ArrayList<UserFull>();
    UserFull userFull;
    String[] infosPropertiesNames;
    String infoValue;
    while (it.hasNext()) {
      userFull = (UserFull) it.next();
      if (StringUtil.isDefined(userFull.getFirstName())
          && userFull.getFirstName().toLowerCase().indexOf(query) != -1) {
        result.add(userFull);
      } else if (StringUtil.isDefined(userFull.getLastName())
          && userFull.getLastName().toLowerCase().indexOf(query) != -1) {
        result.add(userFull);
      } else if (StringUtil.isDefined(userFull.geteMail())
          && userFull.geteMail().toLowerCase().indexOf(query) != -1) {
        result.add(userFull);
      } else {
        infosPropertiesNames = userFull.getPropertiesNames();
        for (int i = 0; i < infosPropertiesNames.length; i++) {
          infoValue = userFull.getValue(infosPropertiesNames[i]);
          if (infoValue.toLowerCase().indexOf(query) != -1) {
            result.add(userFull);
            break;
          }
        }
      }
    }
    return result;
  }

  /**
   * @param query
   * @return list of UserCompleteContact
   * @throws PublicationTemplateException
   * @throws FormException
   */
  private List<UserCompleteContact> searchCompleteUsers(String query)
      throws PublicationTemplateException, FormException {
    query = query.toLowerCase();
    Iterator<UserCompleteContact> it = this.currentCompleteUsers.iterator();
    List<UserCompleteContact> result = new ArrayList<UserCompleteContact>();
    UserCompleteContact userComplete;

    String xmlFormName;
    String xmlFormShortName;
    PublicationTemplate pubTemplate;
    RecordSet recordSet;
    DataRecord data;
    String fieldName;
    Field field;
    String value;
    while (it.hasNext()) {
      userComplete = (UserCompleteContact) it.next();
      ContactDetail detail = userComplete.getContact().getContactDetail();
      if (StringUtil.isDefined(detail.getFirstName())
          && detail.getFirstName().toLowerCase().indexOf(query) != -1) {
        result.add(userComplete);
      } else if (StringUtil.isDefined(detail.getLastName())
          && detail.getLastName().toLowerCase().indexOf(query) != -1) {
        result.add(userComplete);
      } else if (StringUtil.isDefined(userComplete.getContact()
          .getContactDetail().getEmail())) {
        if (userComplete.getContact().getContactDetail().getEmail()
            .toLowerCase().indexOf(query) != -1) {
          result.add(userComplete);
        }
      } else if (StringUtil.isDefined(userComplete.getContact()
          .getContactDetail().getPhone())) {
        if (userComplete.getContact().getContactDetail().getPhone()
            .toLowerCase().indexOf(query) != -1) {
          result.add(userComplete);
        }
      } else if (StringUtil.isDefined(userComplete.getContact()
          .getContactDetail().getFax())) {
        if (userComplete.getContact().getContactDetail().getFax().toLowerCase()
            .indexOf(query) != -1) {
          result.add(userComplete);
        }
      } else if (userComplete.getContact().getModelId().endsWith(".xml")) {
        // Recherche sur les infos XML
        xmlFormName = userComplete.getContact().getModelId();
        xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf("/") + 1,
            xmlFormName.indexOf("."));

        // recuperation des donn�es du formulaire (via le DataRecord)
        pubTemplate = PublicationTemplateManager
            .getPublicationTemplate(getComponentId() + ":" + xmlFormShortName);

        recordSet = pubTemplate.getRecordSet();
        data = recordSet.getRecord(userComplete.getContact().getContactDetail()
            .getPK().getId());
        if (data != null) {
          for (int i = 0; i < data.getFieldNames().length; i++) {
            fieldName = data.getFieldNames()[i];
            field = data.getField(fieldName);
            value = field.getStringValue();
            if (value != null && value.toLowerCase().indexOf(query) != -1) {
              result.add(userComplete);
              break;
            }
          }
        }
      }
    }
    return result;
  }

  /**
   * @param typeSearch
   * @param query
   * @return list of ContactFatherDetail
   * @throws RemoteException
   * @throws FormException
   * @throws PublicationTemplateException
   */
  public List<ContactFatherDetail> search(String typeSearch, String query) throws RemoteException,
      PublicationTemplateException, FormException {
    List<ContactFatherDetail> result = new ArrayList<ContactFatherDetail>();

    query = query.trim();

    if (!"All".equals(typeSearch)) {// typeSearch = LastName || FirstName ||
      // LastNameFirstName
      // Recherche sur nom et/ou prénom

      String nom = null;
      String prenom = null;
      int indexEspace = -1;

      if ("LastName".equals(typeSearch)) {
        nom = query;
      } else if ("FirstName".equals(typeSearch)) {
        nom = "*";
        prenom = query;
      } else if ("LastNameFirstName".equals(typeSearch)) {// nom et/ou prénom
        indexEspace = query.indexOf(" ");
        if (indexEspace == -1) { // seulement recherche sur le nom, on cherchera
          // sur le prénom aprés
          nom = query;
        } else { // recherche sur le nom et le prénom
          nom = query.substring(0, indexEspace);
          prenom = query.substring(indexEspace);
          prenom = prenom.trim();
        }
      }

      if (nom.endsWith("*") || nom.endsWith("%")) {
        nom = nom.substring(0, nom.length() - 1);
      }
      nom = "%" + nom + "%";

      if (prenom != null) {
        if (prenom.endsWith("*") || prenom.endsWith("%")) {
          prenom = prenom.substring(0, prenom.length() - 1);
        }
        prenom = "%" + prenom + "%";
      }

      // 1 - Look for user in Silverpeas groups
      UserDetail modelUser = new UserDetail();
      modelUser.setLastName(nom);
      if (prenom != null) {
        modelUser.setFirstName(prenom);
      }

      UserDetail[] users = getOrganizationController().searchUsers(modelUser,
          true);

      if ("LastNameFirstName".equals(typeSearch) && indexEspace == -1) {// ajout
        // recherche
        // sur
        // le
        // prénom
        modelUser.setLastName(null);
        if (query.endsWith("*") || query.endsWith("%")) {
          query = query.substring(0, query.length() - 1);
        }
        query = "%" + query + "%";
        modelUser.setFirstName(query);

        UserDetail[] usersFirstName = getOrganizationController().searchUsers(
            modelUser, true);

        UserDetail[] temp = new UserDetail[users.length + usersFirstName.length];

        int i = 0;
        for (i = 0; i < users.length; i++) {
          temp[i] = users[i];
        }
        int j = i;
        for (int k = 0; k < usersFirstName.length; k++) {
          temp[j] = usersFirstName[k];
          j++;
        }

        users = temp;
      }

      // filter users who are in the component
      TopicDetail rootTopic = getTopic("0");
      Collection<ContactFatherDetail> contactsUser = getAllContactDetails(rootTopic.getNodePK());
      Iterator<ContactFatherDetail> iterator = contactsUser.iterator();
      ContactFatherDetail contactFather;
      ContactDetail contact;
      String userId;
      UserDetail userDetail = null;
      while (iterator.hasNext()) {
        contactFather = (ContactFatherDetail) iterator.next();
        contact = contactFather.getContactDetail();
        userId = contact.getUserId();
        if (userId != null) {
          for (int u = 0; u < users.length; u++) {
            userDetail = users[u];
            if (userId.equals(userDetail.getId())) {
              result.add(contactFather);
              break;
            }
          }
        }
      }

      // 2 - Look for contacts
      List<ContactDetail> contacts = new ArrayList<ContactDetail>();
      if ("LastName".equals(typeSearch)) {
        contacts = (List<ContactDetail>) kscEjb.getContactDetailsByLastName(new ContactPK(
            "useless", "useless", getComponentId()), nom);
      } else if ("FirstName".equals(typeSearch)) {
        contacts = (List<ContactDetail>) kscEjb.getContactDetailsByLastNameAndFirstName(
            new ContactPK("useless", "useless", getComponentId()), nom, prenom);
      } else if ("LastNameFirstName".equals(typeSearch)) {
        if (prenom == null) {// nom ou pr�nom
          contacts = (List<ContactDetail>) kscEjb.getContactDetailsByLastNameOrFirstName(
              new ContactPK("useless", "useless", getComponentId()), nom);
        } else {// nom et pr�nom
          contacts = (List<ContactDetail>) kscEjb.getContactDetailsByLastNameAndFirstName(
              new ContactPK("useless", "useless", getComponentId()), nom,
              prenom);
        }
      }

      result.addAll(getListContactFather(contacts, false));

    } else {// typeSearch = All
      // Recherche sur tous les champs

      // initialise les listes en session
      Collection<ContactFatherDetail> contactsUser = setCurrentFullCompleteUsers();

      // 1 - Look for user in Silverpeas groups
      List<UserFull> listFullUsers = searchFullUsers(query);

      // 2 - Look for contacts
      List<UserCompleteContact> listCompleteUsers = searchCompleteUsers(query);

      Iterator<ContactFatherDetail> iterator = contactsUser.iterator();
      ContactFatherDetail contactFather;
      Iterator<UserFull> itUserFull;
      UserFull userFull;
      Iterator<UserCompleteContact> itUserComplete;
      UserCompleteContact userComplete;
      while (iterator.hasNext()) {
        contactFather = (ContactFatherDetail) iterator.next();
        if (contactFather.getNodeId() != null
            && contactFather.getNodeId().startsWith(
            YellowpagesSessionController.GroupReferentielPrefix)
            && contactFather.getContactDetail().getUserId() != null) {// contact
          // de type
          // user
          // appartenant
          // à un
          // groupe
          // Silverpeas
          itUserFull = listFullUsers.iterator();
          while (itUserFull.hasNext()) {
            userFull = (UserFull) itUserFull.next();
            if (contactFather.getContactDetail().getUserId().equals(
                userFull.getId())) {
              result.add(contactFather);
              break;
            }
          }
        } else {// contacts annuaire interne et externe
          itUserComplete = listCompleteUsers.iterator();
          while (itUserComplete.hasNext()) {
            userComplete = (UserCompleteContact) itUserComplete.next();
            if (contactFather.getContactDetail().getPK().getId().equals(
                userComplete.getContact().getContactDetail().getPK().getId())) {
              result.add(contactFather);
              break;
            }
          }
        }
      }
    }

    return result;
  }

  public List<ContactFatherDetail> getListContactFather(List<ContactDetail> contacts,
      boolean retourneUserReferentiel) throws RemoteException { // en
    // param�tre
    // une liste de
    // ContactDetail
    List<ContactFatherDetail> result = new ArrayList<ContactFatherDetail>();
    ContactFatherDetail contactFather;
    if (contacts != null) {
      ContactDetail contact;
      Iterator<NodePK> iteratorFathers;
      NodePK nodePK;
      String nodeName;

      for (int c = 0; c < contacts.size(); c++) {
        contact = (ContactDetail) contacts.get(c);

        if (retourneUserReferentiel
            || (!retourneUserReferentiel && contact.getUserId() == null)) {
          iteratorFathers = getContactFathers(contact.getPK().getId())
              .iterator();
          while (iteratorFathers.hasNext()) {
            nodePK = (NodePK) iteratorFathers.next();
            if (!nodePK.getId().equals("1") && !nodePK.getId().equals("2")) {
              nodeName = getSubTopicDetail(nodePK.getId()).getName();

              contactFather = new ContactFatherDetail(contact, nodePK.getId(),
                  nodeName);
              result.add(contactFather);
            }
          }
        }
      }
    }

    return result;
  }

  public void close() {
    try {
      if (kscEjb != null) {
        kscEjb.remove();
      }
    } catch (RemoteException e) {
      SilverTrace.error("yellowpagesSession",
          "YellowpagesSessionController.close", "", e);
    } catch (RemoveException e) {
      SilverTrace.error("yellowpagesSession",
          "YellowpagesSessionController.close", "", e);
    }

    try {
      if (searchEngineEjb != null) {
        searchEngineEjb.remove();
      }
    } catch (RemoteException e) {
      SilverTrace.error("yellowpagesSession",
          "YellowpagesSessionController.close", "", e);
    } catch (RemoveException e) {
      SilverTrace.error("yellowpagesSession",
          "YellowpagesSessionController.close", "", e);
    }
  }

  public Collection<ContactFatherDetail> getCurrentContacts() {
    return currentContacts;
  }

  public void setCurrentContacts(Collection<ContactFatherDetail> currentContacts) {
    this.currentContacts = currentContacts;
  }

  public boolean isPortletMode() {
    return portletMode;
  }

  public void setPortletMode(boolean portletMode) {
    this.portletMode = portletMode;
  }

  public NodeBm getNodeBm() {
    try {
      NodeBmHome nodeBmHome = (NodeBmHome) EJBUtilitaire.getEJBObjectRef(
          JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
      return nodeBmHome.create();
    } catch (Exception re) {
      throw new YellowpagesRuntimeException(
          "YellowpagesSessionController.getNodeBm()",
          SilverpeasRuntimeException.ERROR,
          "yellowpages.EX_GET_NODEBM_HOME_FAILED", re);
    }
  }

  public int getNbContactPerPage() {
    return new Integer(getSettings().getString("nbContactPerPage")).intValue();
  }

  /**
   * @param models
   */
  public void addModelUsed(String[] models) {
    try {
      getKSCEJB().addModelUsed(models, getComponentId());
    } catch (RemoteException e) {
      throw new YellowpagesRuntimeException(
          "YellowpagesSessionController.addModelUsed()",
          SilverpeasRuntimeException.ERROR, "kmelia.MSG_ERR_GENERAL", e);
    }
  }

  public Collection<String> getModelUsed() {
    Collection<String> result = null;
    try {
      result = getKSCEJB().getModelUsed(getComponentId());
    } catch (RemoteException e) {
      throw new YellowpagesRuntimeException(
          "YellowpagesSessionController.getModelUsed()",
          SilverpeasRuntimeException.ERROR, "kmelia.MSG_ERR_GENERAL", e);
    }
    return result;
  }

  public synchronized void deleteBasketContent() throws RemoteException,
      FormException, PublicationTemplateException {
    SilverTrace.info("yellowpages",
        "YellowpagesSessionControl.deleteBasketContent",
        "root.MSG_ENTER_METHOD");
    TopicDetail td = getCurrentTopic();
    Collection<UserContact> pds = td.getContactDetails();
    Iterator<UserContact> ipds = pds.iterator();

    SilverTrace.info("yellowpages",
        "YellowpagesSessionControl.deleteBasketContent",
        "root.MSG_PARAM_VALUE", "NbContacts=" + pds.size());
    while (ipds.hasNext()) {
      UserContact userContact = (UserContact) ipds.next();
      String contactId = userContact.getContact().getPK().getId();
      SilverTrace.info("yellowpages",
          "YellowpagesSessionControl.deleteBasketContent",
          "root.MSG_PARAM_VALUE", "Deleting Contact #" + contactId);
      deleteContact(contactId);
    }
  }

  public List<String> getProperties() {
    List<String> properties = new ArrayList<String>();
    String columns = getSettings().getString("columns");
    String[] nameColumns = columns.split(",");
    for (String nameProperty : nameColumns) {
      if (!nameProperty.startsWith("password")) {
        properties.add(nameProperty);
      }
    }
    return properties;
  }

  public List<String> getArrayHeaders() {
    List<String> arrayHeaders = new ArrayList<String>();
    List<String> properties = getProperties();
    String nameHeader = "";
    for (String nameProperty : properties) {
      if (nameProperty.startsWith("domain.")) {
        // on recherche une propriété du domaine
        String property = nameProperty.substring(7);
        nameHeader = getDomainMultilang().getString(property);
      } else {
        // on recherche une propriété classique
        nameHeader = getMultilang().getString("yellowpages.column." + nameProperty);
      }
      arrayHeaders.add(nameHeader);
    }
    return arrayHeaders;
  }

  private ResourceLocator getDomainMultilang() {
    return domainMultilang;
  }

  public String exportAsCSV() throws RemoteException {
    List<StringBuffer> csvRows = exportAllDataAsCSV();

    return writeCSVFile(csvRows);
  }

  private List<StringBuffer> exportAllDataAsCSV() throws RemoteException {
    Collection<ContactFatherDetail> contacts =
        getAllContactDetails(currentTopic.getNodePK());

    StringBuffer csvRow = new StringBuffer();
    List<StringBuffer> csvRows = new ArrayList<StringBuffer>();

    // Can't export all columns because data are heterogenous
    csvRow = getCSVCols();
    // csvRows.add(csvRow);

    for (ContactFatherDetail contactFatherDetail : contacts) {
      ContactDetail contact = contactFatherDetail.getContactDetail();
      if (contact != null) {
        csvRow = new StringBuffer();
        addCSVValue(csvRow, contact.getLastName());
        addCSVValue(csvRow, contact.getFirstName());
        addCSVValue(csvRow, contact.getEmail());
        addCSVValue(csvRow, contact.getPhone());
        addCSVValue(csvRow, contact.getFax());

        // adding userFull data
        UserFull userFull = contact.getUserFull();
        if (userFull != null) {
          String[] properties = userFull.getPropertiesNames();
          for (String property : properties) {
            if (!property.startsWith("password")) {
              addCSVValue(csvRow, userFull.getValue(property));
            }
          }
        }

        // adding xml data
        String modelId = "unknown";
        try {
          modelId =
              getNodeBm().getDetail(new NodePK(contactFatherDetail.getNodeId(), getComponentId()))
              .getModelId();
          if (StringUtil.isDefined(modelId) && modelId.endsWith(".xml")) {
            String xmlFormName = modelId;
            String xmlFormShortName = xmlFormName.substring(0, xmlFormName.indexOf("."));
            PublicationTemplateImpl pubTemplate =
                (PublicationTemplateImpl) PublicationTemplateManager
                .getPublicationTemplate(getComponentId() + ":"
                + xmlFormShortName, xmlFormName);

            // get template and data
            AbstractForm formView = (AbstractForm) pubTemplate.getViewForm();
            RecordSet recordSet = pubTemplate.getRecordSet();
            DataRecord data = recordSet.getRecord(contact.getPK().getId());

            List<FieldTemplate> fields = formView.getFieldTemplates();
            for (FieldTemplate fieldTemplate : fields) {
              String fieldType = fieldTemplate.getTypeName();
              StringWriter sw = new StringWriter();
              PrintWriter out = new PrintWriter(sw, true);

              Field field = data.getField(fieldTemplate.getFieldName());
              if (field != null) {
                if (!fieldType.equals(Field.TYPE_FILE)) {
                  FieldDisplayer fieldDisplayer = TypeManager.getDisplayer(fieldType, "simpletext");
                  if (fieldDisplayer != null) {
                    fieldDisplayer.display(out, field, fieldTemplate, new PagesContext());
                  }
                  String fieldValue = sw.getBuffer().toString();
                  // removing ending carriage return appended by out.println() of fieldDisplayer
                  if (fieldValue.endsWith("\r\n")) {
                    fieldValue = fieldValue.substring(0, fieldValue.length() - 2);
                  }
                  addCSVValue(csvRow, fieldValue);
                }
              }
            }
          }
        } catch (Exception e) {
          SilverTrace.warn("yellowpages",
              "YellowpagesSessionController.exportAllDataAsCSV",
              "yellowpages.EX_GET_USER_DETAIL_FAILED", "modelId = " + modelId + ", contactId = " +
              contact.getPK().getId());
        }
        //Remove final ","
        csvRow.deleteCharAt(csvRow.lastIndexOf(","));
        csvRows.add(csvRow);
      }
    }

    return csvRows;
  }

  private StringBuffer getCSVCols() {
    StringBuffer csvRow = new StringBuffer();
    addCSVValue(csvRow, getString("yellowpages.column.lastname"));
    addCSVValue(csvRow, getString("yellowpages.column.firstname"));
    addCSVValue(csvRow, getString("yellowpages.column.email"));
    addCSVValue(csvRow, getString("yellowpages.column.phone"));

    return csvRow;
  }

  private void addCSVValue(StringBuffer row, String value) {
    row.append("\"");
    if (value != null)
      row.append(value.replaceAll("\"", "\"\""));
    row.append("\"").append(",");
  }

  private String writeCSVFile(List<StringBuffer> csvRows) {
    FileOutputStream fileOutput = null;
    String csvFilename = new Date().getTime() + ".csv";
    try {
      fileOutput = new FileOutputStream(FileRepositoryManager
          .getTemporaryPath()
          + csvFilename);

      StringBuffer csvRow;
      for (int r = 0; r < csvRows.size(); r++) {
        csvRow = csvRows.get(r);
        fileOutput.write(csvRow.toString().getBytes());
        fileOutput.write("\n".getBytes());
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      csvFilename = null;
      e.printStackTrace();
    } finally {
      if (fileOutput != null) {
        try {
          fileOutput.flush();
          fileOutput.close();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          csvFilename = null;
          e.printStackTrace();
        }
      }
    }
    return csvFilename;
  }
  
  /**
   * Import Csv file
   * @param filePart
   * @param modelId
   * @return HashMap<isError, messages>
   */
  public HashMap<Boolean,String> importCSV(FileItem filePart, String modelId) throws YellowpagesException {
    SilverTrace.info("yellowpages",
        "YellowpagesSessionController.importCSV()",
        "root.MSG_GEN_ENTER_METHOD");
    HashMap<Boolean, String> result = new HashMap<Boolean, String>();
    InputStream is;
    int nbContactsAdded = 0;
    Variant[][] csvHeaderValues;
    try {
      is = filePart.getInputStream();
      CSVReader csvReader = new CSVReader(getLanguage());
      csvReader.setColumnNumberControlEnabled(false);
      csvReader.setExtraColumnsControlEnabled(false);
      csvReader.initCSVFormat("com.stratelia.webactiv.yellowpages.settings.yellowpagesSettings", "User",",");

      try {
        csvHeaderValues = csvReader.parseStream(is);
        
        StringBuffer listErrors = new StringBuffer("");
        ContactDetail contactDetail;
        int nbColumns = csvReader.getM_nbCols()+csvReader.getM_specificNbCols();
        boolean processExtraColumns = false;
        
        for (int line = 0; line < csvHeaderValues.length; line++) {
          String[] CSVRow = new String[csvHeaderValues[line].length];
          //Read all columns
          for (int column = 0; column < nbColumns; column++) {
            String value = formatStringSeparator(csvHeaderValues[line][column].getValueString());
            CSVRow[column] = value;
          }
          //Header columns (firstName, lastName, email, phone, fax)
          contactDetail = new ContactDetail("X", CSVRow[0], CSVRow[1], CSVRow[2], CSVRow[3], CSVRow[4], null, null, null);
          
          //Extra columns from xml form ?
          if (StringUtil.isDefined(modelId))
            processExtraColumns = true;
          try {
            String contactId = createContact(contactDetail);
            //XML form columns
            if (processExtraColumns)
            {
              String xmlFormShortName = modelId.substring(modelId
                  .indexOf("/") + 1, modelId.indexOf("."));

              // récupération des données du formulaire (via le DataRecord)
              PublicationTemplate pubTemplate = PublicationTemplateManager
                  .getPublicationTemplate(getComponentId() + ":"
                  + xmlFormShortName);
              DataRecord record = pubTemplate.getRecordSet().getEmptyRecord();
              record.setId(contactId);
              
              int fieldIndex = 0;
              for (int column = csvReader.getM_nbCols(); column < csvReader.getM_nbCols()+csvReader.getM_specificNbCols(); column++) {
                String value = formatStringSeparator(CSVRow[column]);
                if (StringUtil.isDefined(value))
                  record.getField(fieldIndex).setObjectValue(value);
                fieldIndex++;
              }
              // Update
              pubTemplate.getRecordSet().save(record);

              // sauvegarde du contact et du model
              createInfoModel(contactId, modelId);
              UserCompleteContact userContactComplete = new UserCompleteContact(null, new CompleteContact(contactDetail, xmlFormShortName));
              setCurrentContact(userContactComplete);
            }
          } catch (Exception re)
          {
            SilverTrace.error("yellowpagesSession",
                "YellowpagesSessionController.importCSV", "yellowpages.EX_CSV_FILE", re);
          }
          if (listErrors.length() > 0) {
            result.put(new Boolean(true), listErrors.toString());
          }
          else
            nbContactsAdded++;

        }
      } catch (UtilTrappedException ute) {
        SilverTrace.error("yellowpagesSession",
            "YellowpagesSessionController.importCSV", "yellowpages.EX_CSV_FILE", ute);
        result.put(new Boolean(true), ute.getExtraInfos());
      }
      result.put(new Boolean(false), new Integer(nbContactsAdded).toString());
      
    } catch (IOException e) {
      SilverTrace.error("yellowpagesSession",
          "YellowpagesSessionController.importCSV", "yellowpages.EX_CSV_FILE", e);
      result.put(new Boolean(true), e.getMessage());
    }

    SilverTrace.info("yellowpages",
        "YellowpagesSessionController.importCSV()",
        "root.MSG_GEN_EXIT_METHOD");

    return result;
  }
  
  /**
   * Remove start & end " and replace double " by single "
   * @param value
   * @return
   */
  private String formatStringSeparator(String value) {
    String newValue = value;
    if (StringUtil.isDefined(value) && value.startsWith("\"") && value.endsWith("\"")) {
      newValue = value.substring(1,value.length()-1);
      newValue = newValue.replaceAll("\"\"", "\"");
    }
    return newValue;
  }


}
