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
package com.stratelia.webactiv.yellowpages.control;

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
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.CompoSpace;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.contact.model.CompleteContact;
import com.stratelia.webactiv.contact.model.ContactDetail;
import com.stratelia.webactiv.contact.model.ContactFatherDetail;
import com.stratelia.webactiv.contact.model.ContactPK;
import com.stratelia.webactiv.node.control.NodeService;
import com.stratelia.webactiv.node.model.NodeDetail;
import com.stratelia.webactiv.node.model.NodePK;
import com.stratelia.webactiv.yellowpages.ImportReport;
import com.stratelia.webactiv.yellowpages.YellowpagesException;
import com.stratelia.webactiv.yellowpages.control.ejb.YellowpagesBm;
import com.stratelia.webactiv.yellowpages.model.GroupDetail;
import com.stratelia.webactiv.yellowpages.model.TopicDetail;
import com.stratelia.webactiv.yellowpages.model.UserContact;
import com.stratelia.webactiv.yellowpages.model.YellowpagesRuntimeException;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.util.FileRepositoryManager;
import org.silverpeas.util.GeneralPropertiesManager;
import org.silverpeas.util.GlobalContext;
import org.silverpeas.util.Pair;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.util.StringUtil;
import org.silverpeas.util.csv.CSVReader;
import org.silverpeas.util.csv.Variant;
import org.silverpeas.util.exception.SilverpeasRuntimeException;
import org.silverpeas.util.exception.UtilTrappedException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class YellowpagesSessionController extends AbstractComponentSessionController {

  private TopicDetail currentTopic = null;
  private CompleteContact currentContact = null;
  private String path = null;
  private String owner = "false";
  // admin || publisher || user
  private String profile;
  private List<GroupDetail> groupPath = new ArrayList<>();
  private boolean portletMode = false;
  private Collection<ContactFatherDetail> currentContacts = null;
  private Collection<UserFull> currentFullUsers = null;
  private Collection<CompleteContact> currentCompleteUsers = null;
  private String currentTypeSearch;
  private String currentSearchCriteria;
  public static final String GroupReferentielPrefix = "group_";
  private ResourceLocator domainMultilang;

  /**
   * Creates new sessionClientController
   * @param mainSessionCtrl
   * @param context
   */
  public YellowpagesSessionController(MainSessionController mainSessionCtrl,
      ComponentContext context) {
    super(mainSessionCtrl, context, "org.silverpeas.yellowpages.multilang.yellowpagesBundle",
        "org.silverpeas.yellowpages.settings.yellowpagesIcons",
        "org.silverpeas.yellowpages.settings.yellowpagesSettings");
    init();
    setProfile();
    String domainId = getSettings().getString("columns.domainId");
    if (StringUtil.isDefined(domainId)) {
      Domain domain = getOrganisationController().getDomain(domainId);
      ResourceLocator domainProperty = new ResourceLocator(domain.getPropFileName(), "");
      domainMultilang = new ResourceLocator(domainProperty.getString("property.ResourceFile"), "");
    }
  }

  private void init() {
    // 1 - Remove all data store by this SessionController
    removeSessionTopic();
    removeSessionPublication();
    removeSessionPath();
    removeSessionOwner();
  }

  // Current User operations

  private String getFlag(String[] profiles) {
    String flag = SilverpeasRole.user.toString();
    for (String profile1 : profiles) {
      if (SilverpeasRole.admin.isInRole(profile1)) {
        return profile1;
      }
      if (SilverpeasRole.publisher.isInRole(profile1)) {
        flag = profile1;
      }
    }
    return flag;
  }

  public final void setProfile() {
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

  public void setCurrentContact(CompleteContact currentContact) {
    this.currentContact = currentContact;
  }

  public CompleteContact getCurrentContact() {
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

  public YellowpagesBm getYellowpagesService() {
    return YellowpagesBm.get();
  }

  public TopicDetail getTopic(String id) {
    TopicDetail topic = getYellowpagesService().goTo(getNodePK(id), getUserId());
    List<NodeDetail> thePath = (List<NodeDetail>) getNodeBm().getAnotherPath(getNodePK(id));
    Collections.reverse(thePath);
    topic.setPath(thePath);
    return topic;
  }

  private NodePK getNodePK(String id) {
    return new NodePK(id, getComponentId());
  }

  private ContactPK getContactPK(String id) {
    return new ContactPK(id, getComponentId());
  }

  public GroupDetail getGroup(String groupId) {
    Group group = getOrganisationController().getGroup(groupId);
    GroupDetail groupDetail = new GroupDetail(group);

    // add sub groups
    Group[] subGroups = getOrganisationController().getAllSubGroups(groupId);
    Group subGroup;
    GroupDetail subGroupDetail;
    for (Group subGroup1 : subGroups) {
      subGroup = subGroup1;
      subGroupDetail = new GroupDetail(subGroup);
      subGroupDetail.setTotalUsers(getOrganisationController().
          getAllSubUsersNumber(subGroup.getId()));
      groupDetail.addSubGroup(subGroupDetail);
    }

    // add users
    String[] userIds = group.getUserIds();
    String userId;
    UserDetail user;
    for (String userId1 : userIds) {
      userId = userId1;
      user = getOrganisationController().getUserDetail(userId);
      if (user != null) {
        groupDetail.addUser(user);
      }
    }

    processGroupPath(groupDetail);

    return groupDetail;
  }

  private void processGroupPath(GroupDetail group) {
    if (groupPath.isEmpty()) {
      groupPath.add(group);
    } else {
      int index = groupPath.indexOf(group);
      SilverTrace.info("yellowpages", "YellowpagesSessionController.processGroupPath()",
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

  public List<NodeDetail> getTree() {
    return getYellowpagesService().getTree(getComponentId());
  }

  public NodePK updateTopicHeader(NodeDetail nd) {
    SilverTrace.info("yellowpages", "YellowpagesSessionController.updateTopicHeader()",
        "root.MSG_GEN_PARAM_VALUE", "id = " + nd.getNodePK().getId());
    nd.setFatherPK(getCurrentTopic().getNodePK());
    nd.getNodePK().setComponentName(getComponentId());
    return getYellowpagesService().updateTopic(nd);
  }

  public NodeDetail getSubTopicDetail(String subTopicId) {
    return getYellowpagesService().getSubTopicDetail(getNodePK(subTopicId));
  }

  public NodePK addSubTopic(NodeDetail nd) {
    nd.getNodePK().setComponentName(getComponentId());
    nd.setCreatorId(getUserId());
    return getYellowpagesService().addToTopic(getCurrentTopic().getNodeDetail(), nd);
  }

  public void deleteTopic(String topicId) {
    SilverTrace.info("yellowpages", "YellowpagesSessionController.deleteTopic()",
        "root.MSG_GEN_PARAM_VALUE", "topicId = " + topicId);

    getYellowpagesService().deleteTopic(getNodePK(topicId));
    resetCurrentFullCompleteUsers();
  }

  public void emptyPublisherDZ() {
    getYellowpagesService().emptyDZByUserId(getComponentId(), getUserId());
  }

  /* Yellowpages - Gestion des contacts */

  /**
   * @param contactId the contact identifier
   * @return the contact detail identified by given parameter
   */
  public ContactDetail getContactDetail(String contactId) {
    return getYellowpagesService().getContactDetail(getContactPK(contactId));
  }

  private void resetCurrentFullCompleteUsers() {
    this.currentFullUsers = null;
    this.currentCompleteUsers = null;
  }

  private Collection<ContactFatherDetail> setCurrentFullCompleteUsers() {
    // racine
    TopicDetail rootTopic = getTopic(NodePK.ROOT_NODE_ID);
    Collection<ContactFatherDetail> contacts = getAllContactDetails(rootTopic.getNodePK());
    if (this.currentFullUsers == null || this.currentCompleteUsers == null) {
      List<UserFull> listUserFull = new ArrayList<>();
      List<CompleteContact> listUserComplete = new ArrayList<>();
      for (ContactFatherDetail contact : contacts) {
        if (contact.getNodeId() != null && contact.getNodeId().startsWith(GroupReferentielPrefix) &&
            contact.getContactDetail().getUserId() != null) {// contact de
          // type user appartenanta un groupe Silverpeas
          UserFull userFull = getOrganisationController().getUserFull(contact.getContactDetail().
              getUserId());
          if (userFull != null) {
            listUserFull.add(userFull);
          }
        } else {
          // contacts annuaire interne et externe
          CompleteContact userComplete = getCompleteContactInNode(contact.getContactDetail().
              getPK().getId(), contact.getNodeId());
          listUserComplete.add(userComplete);
        }
      }
      this.currentFullUsers = listUserFull;
      this.currentCompleteUsers = listUserComplete;
    }
    return contacts;
  }

  public Collection<ContactFatherDetail> getAllContactDetails(NodePK fatherPK) {
    Collection<ContactFatherDetail> contacts =
        getYellowpagesService().getAllContactDetails(fatherPK);
    if (contacts != null) {
      // get users of groups contained in subtree
      List<NodeDetail> tree =
          getNodeBm().getSubTree(new NodePK(fatherPK.getId(), getComponentId()));
      for (int t = 0; tree != null && t < tree.size(); t++) {
        NodeDetail node = tree.get(t);
        contacts.addAll(getAllUsers(node.getNodePK().getId()));
      }
    } else {
      contacts = new ArrayList<>(getAllUsers(fatherPK.getId()));
    }
    return contacts;
  }

  public List<Collection<NodeDetail>> getPathList(String contactId) {
    return getYellowpagesService().getPathList(getContactPK(contactId));
  }

  public String createContact(CompleteContact contact) {
    setTechnicalData(contact);
    String contactId =
        getYellowpagesService().createContact(contact, getCurrentTopic().getNodePK());
    resetCurrentFullCompleteUsers();
    return contactId;
  }

  public void updateContact(CompleteContact contact) {
    contact.getPK().setComponentName(getComponentId());
    setTechnicalData(contact);
    getYellowpagesService().updateContact(contact);
    resetCurrentFullCompleteUsers();
  }

  private void setTechnicalData(CompleteContact contact) {
    contact.setCreationDate(new Date());
    contact.setCreatorId(getUserId());
    contact.setCreatorLanguage(getLanguage());
  }

  public void deleteContact(String contactId) {
    getYellowpagesService().deleteContact(getContactPK(contactId), getCurrentTopic().getNodePK());
    resetCurrentFullCompleteUsers();
  }

  public void addContactToTopic(String contactId, String fatherId) {
    if (StringUtil.isDefined(fatherId)) {
      getYellowpagesService().addContactToTopic(getContactPK(contactId), fatherId);
      resetCurrentFullCompleteUsers();
    }
  }

  public void deleteContactFromTopic(String contactId, String fatherId) {
    getYellowpagesService().deleteContactFromTopic(getContactPK(contactId), fatherId);
  }

  public void createInfoModel(String contactId, String modelId) {
    getYellowpagesService().createInfoModel(getContactPK(contactId), modelId);
    resetCurrentFullCompleteUsers();
  }

  public CompleteContact getCompleteContact(String contactId) {
    return getYellowpagesService().getCompleteContact(getContactPK(contactId));
  }

  public CompleteContact getCompleteContactInNode(String contactId, String nodeId) {
    return getYellowpagesService().getCompleteContactInNode(getContactPK(contactId), nodeId);
  }

  public Collection<NodePK> getContactFathers(String contactId) {
    return getYellowpagesService().getContactFathers(getContactPK(contactId));
  }

  public void deleteContactFathers(String contactId) {
    Collection<NodePK> fathers = getYellowpagesService().getContactFathers(getContactPK(contactId));
    if (fathers != null) {
      for (NodePK pk : fathers) {
        deleteContactFromTopic(contactId, pk.getId());
      }
    }
    resetCurrentFullCompleteUsers();
  }

  public Collection<UserContact> getContacts(Collection<String> targetIds) {
    return getYellowpagesService().getContacts(targetIds, getComponentId());
  }

  /**
   * methods for Users
   * @return
   */
  public UserDetail[] getUserList() {
    return getOrganisationController().getAllUsers();
  }

  /**
   * get others instances of yellowpages
   * @return
   */
  public CompoSpace[] getYellowPagesInstances() {
    CompoSpace[] compoSpaces =
        getOrganisationController().getCompoForUser(getUserId(), "yellowpages");
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

  /* Yellowpages - Manage UserPanel */

  /**
   * @param
   * @return
   */
  public String initUserPanel() {
    String mContext = GeneralPropertiesManager.getString("ApplicationURL");
    String hostSpaceName = getSpaceLabel();
    Pair<String, String> hostComponentName = new Pair<>(getComponentLabel(), "");
    Pair<String, String>[] hostPath = new Pair[1];
    hostPath[0] = new Pair<>(getString("UserCreer"), "");
    String hostUrl = mContext + URLManager.getURL(null, getComponentId()) + "saveUser";
    String cancelUrl = mContext + URLManager.getURL(null, getComponentId()) + "/saveUser";

    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName(hostSpaceName);
    sel.setHostComponentName(hostComponentName);
    sel.setHostPath(hostPath);

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);

    // Contraintes
    sel.setMultiSelect(false);
    sel.setPopupMode(false);
    sel.setSetSelectable(false);

    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  /**
   * Put selected user into session using userPanel
   */
  public void setContactUserSelected() {
    String selUser = getSelection().getFirstSelectedElement();
    if (StringUtil.isDefined(selUser)) {
      UserDetail selectedUser = getOrganisationController().getUserDetail(selUser);
      String firstName = selectedUser.getFirstName();
      String lastName = selectedUser.getLastName();
      String email = selectedUser.geteMail();
      String userId = selectedUser.getId();

      String phone = null;
      String fax = null;
      UserFull userFull = getOrganisationController().getUserFull(userId);
      if (userFull != null) {
        phone = userFull.getValue("phone", "");
        fax = userFull.getValue("fax", "");
      }

      ContactDetail contactDetail =
          new ContactDetail("X", firstName, lastName, email, phone, fax, userId, null, null);
      CompleteContact contactComplete = new CompleteContact(contactDetail, null);
      setCurrentContact(contactComplete);
    } // fin if
  }

  public String initGroupPanel() {
    String mContext = GeneralPropertiesManager.getString("ApplicationURL");
    String hostSpaceName = getSpaceLabel();
    Pair<String, String> hostComponentName = new Pair<>(getComponentLabel(), "");
    Pair<String, String>[] hostPath = new Pair[1];
    hostPath[0] = new Pair<>(getString("GroupAdd"), "");
    String hostUrl = mContext + URLManager.getURL(null, getComponentId()) + "AddGroup";
    String cancelUrl = mContext + URLManager.getURL(null, getComponentId()) + "AddGroup";

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

  public void setGroupSelected() {
    String sel = getSelection().getFirstSelectedSet();
    if (sel != null && sel.length() > 0) {
      addGroup(sel);
    }
  }

  public List<ContactFatherDetail> getAllUsers(String nodeId) {
    List<ContactFatherDetail> users = new ArrayList<>();

    List<String> groupIds = getYellowpagesService().getGroupIds(getNodePK(nodeId));
    for (String groupId : groupIds) {
      users.addAll(getAllUsersOfGroup(groupId));
    }
    return users;
  }

  public List<UserDetail> getAllUserDetails(String nodeId) {
    List<UserDetail> users = new ArrayList<>();
    List<String> groupIds = getYellowpagesService().getGroupIds(getNodePK(nodeId));

    for (String groupId : groupIds) {
      UserDetail[] userDetails = getOrganisationController().getAllUsersOfGroup(groupId);
      users.addAll(Arrays.asList(userDetails));
    }

    return users;
  }

  /**
   * Make a recursice call to get all users of group identified by the given parameter
   * @param groupId the group identifier
   * @return the list of contact father detail of the group
   */
  public List<ContactFatherDetail> getAllUsersOfGroup(String groupId) {
    List<ContactFatherDetail> users = new ArrayList<>();
    GroupDetail groupDetail = getGroup(groupId);
    UserDetail[] userDetails = getOrganisationController().getFiltredDirectUsers(groupId, "");

    for (UserDetail userDetail : userDetails) {
      ContactFatherDetail contactFather = getContactFatherDetail(userDetail.getId(), groupDetail);
      if (contactFather != null) {
        users.add(contactFather);
      }
    }

    List<GroupDetail> subGroups = groupDetail.getSubGroups();
    for (GroupDetail subGroup : subGroups) {
      users.addAll(getAllUsersOfGroup(subGroup.getId()));
    }
    return users;
  }

  private ContactFatherDetail getContactFatherDetail(String userId, GroupDetail group) {
    ContactFatherDetail contactFather = null;
    if (this.getSettings().getString("columns").contains("domain.")) {
      UserFull user = getOrganisationController().getUserFull(userId);
      if (user != null) {
        ContactDetail cUser =
            new ContactDetail(new ContactPK("fromGroup", getComponentId()), user.getFirstName(),
                user.getLastName(), user.geteMail(), user.getValue("phone"), user.getValue("fax"),
                user.getId(), null, null);
        cUser.setUserFull(user);

        contactFather = new ContactFatherDetail(cUser,
            YellowpagesSessionController.GroupReferentielPrefix + group.getId(), group.getName());
      }
    } else {
      UserDetail user = getOrganisationController().getUserDetail(userId);
      if (user != null) {
        ContactDetail cUser =
            new ContactDetail(new ContactPK("fromGroup", getComponentId()), user.getFirstName(),
                user.getLastName(), user.geteMail(), "", "", user.getId(), null, null);

        contactFather = new ContactFatherDetail(cUser,
            YellowpagesSessionController.GroupReferentielPrefix + group.getId(), group.getName());
      }
    }
    return contactFather;
  }

  public void addGroup(String groupId) {
    SilverTrace
        .info("yellowpages", "YellowpagesSessionController.addGroup()", "root.MSG_GEN_ENTER_METHOD",
            "groupId = " + groupId);
    getYellowpagesService().addGroup(groupId, getCurrentTopic().getNodePK());
  }

  public void removeGroup(String groupId) {
    getYellowpagesService().removeGroup(groupId, getCurrentTopic().getNodePK());
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
    String searchQuery = query.toLowerCase();
    Iterator<UserFull> it = this.currentFullUsers.iterator();
    List<UserFull> result = new ArrayList<>();
    UserFull userFull;
    String[] infosPropertiesNames;
    String infoValue;
    while (it.hasNext()) {
      userFull = it.next();
      if (StringUtil.isDefined(userFull.getFirstName()) &&
          userFull.getFirstName().toLowerCase().contains(searchQuery)) {
        result.add(userFull);
      } else if (StringUtil.isDefined(userFull.getLastName()) &&
          userFull.getLastName().toLowerCase().contains(searchQuery)) {
        result.add(userFull);
      } else if (StringUtil.isDefined(userFull.geteMail()) &&
          userFull.geteMail().toLowerCase().contains(searchQuery)) {
        result.add(userFull);
      } else {
        infosPropertiesNames = userFull.getPropertiesNames();
        for (String infosPropertiesName : infosPropertiesNames) {
          infoValue = userFull.getValue(infosPropertiesName);
          if (infoValue.toLowerCase().contains(searchQuery)) {
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
   * @return list of CompleteContact
   * @throws PublicationTemplateException
   * @throws FormException
   */
  private List<CompleteContact> searchCompleteUsers(String query)
      throws PublicationTemplateException, FormException {
    String searchQuery = query.toLowerCase(Locale.ROOT);
    Iterator<CompleteContact> it = this.currentCompleteUsers.iterator();
    List<CompleteContact> result = new ArrayList<>();
    CompleteContact userComplete;

    String xmlFormName;
    String xmlFormShortName;
    PublicationTemplate pubTemplate;
    RecordSet recordSet;
    DataRecord data;
    String fieldName;
    Field field;
    String value;
    while (it.hasNext()) {
      userComplete = it.next();
      ContactDetail detail = userComplete.getContactDetail();
      if (StringUtil.isDefined(detail.getFirstName()) &&
          detail.getFirstName().toLowerCase().contains(searchQuery)) {
        result.add(userComplete);
      } else if (StringUtil.isDefined(detail.getLastName()) &&
          detail.getLastName().toLowerCase().contains(searchQuery)) {
        result.add(userComplete);
      } else if (StringUtil.isDefined(userComplete.getContactDetail().getEmail())) {
        if (userComplete.getContactDetail().getEmail().toLowerCase().contains(searchQuery)) {
          result.add(userComplete);
        }
      } else if (StringUtil.isDefined(userComplete.getContactDetail().getPhone())) {
        if (userComplete.getContactDetail().getPhone().toLowerCase().contains(searchQuery)) {
          result.add(userComplete);
        }
      } else if (StringUtil.isDefined(userComplete.getContactDetail().getFax())) {
        if (userComplete.getContactDetail().getFax().toLowerCase().contains(searchQuery)) {
          result.add(userComplete);
        }
      } else if (userComplete.getModelId().endsWith(".xml")) {
        // Recherche sur les infos XML
        xmlFormName = userComplete.getModelId();
        xmlFormShortName =
            xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName.indexOf('.'));

        // recuperation des donnees du formulaire (via le DataRecord)
        pubTemplate = getPublicationTemplateManager()
            .getPublicationTemplate(getComponentId() + ":" + xmlFormShortName);

        recordSet = pubTemplate.getRecordSet();
        data = recordSet.getRecord(userComplete.getContactDetail().getPK().getId());
        if (data != null) {
          for (int i = 0; i < data.getFieldNames().length; i++) {
            fieldName = data.getFieldNames()[i];
            field = data.getField(fieldName);
            value = field.getStringValue();
            if (value != null && value.toLowerCase().contains(searchQuery)) {
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
   * @throws FormException
   * @throws PublicationTemplateException
   * @
   */
  public List<ContactFatherDetail> search(String typeSearch, String query)
      throws PublicationTemplateException, FormException {
    List<ContactFatherDetail> result = new ArrayList<>();
    query = query.trim();

    if (!"All".equals(typeSearch)) {
      // typeSearch = LastName || FirstName || LastNameFirstName
      // Recherche sur nom et/ou prénom

      String nom = null;
      String prenom = null;
      int indexEspace = -1;

      if ("LastName".equals(typeSearch)) {
        nom = query;
      } else if ("FirstName".equals(typeSearch)) {
        nom = "*";
        prenom = query;
      } else if ("LastNameFirstName".equals(typeSearch)) {
        // nom et/ou prénom
        indexEspace = query.indexOf(' ');
        if (indexEspace == -1) {
          // seulement recherche sur le nom, on cherchera sur le prénom aprés
          nom = query;
        } else {
          // recherche sur le nom et le prénom
          nom = query.substring(0, indexEspace);
          prenom = query.substring(indexEspace);
          prenom = prenom.trim();
        }
      }

      assert nom != null;
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

      UserDetail[] users = getOrganisationController().searchUsers(modelUser, true);

      if ("LastNameFirstName".equals(typeSearch) && indexEspace == -1) {
        // Add research on firstname
        modelUser.setLastName(null);
        if (query.endsWith("*") || query.endsWith("%")) {
          query = query.substring(0, query.length() - 1);
        }
        query = "%" + query + "%";
        modelUser.setFirstName(query);

        UserDetail[] usersFirstName = getOrganisationController().searchUsers(modelUser, true);
        UserDetail[] temp = new UserDetail[users.length + usersFirstName.length];

        int i;
        for (i = 0; i < users.length; i++) {
          temp[i] = users[i];
        }
        int j = i;
        for (UserDetail anUsersFirstName : usersFirstName) {
          temp[j] = anUsersFirstName;
          j++;
        }
        users = temp;
      }

      // filter users who are in the component
      TopicDetail rootTopic = getTopic(NodePK.ROOT_NODE_ID);
      Collection<ContactFatherDetail> contactsUser = getAllContactDetails(rootTopic.getNodePK());
      Iterator<ContactFatherDetail> iterator = contactsUser.iterator();
      ContactFatherDetail contactFather;
      ContactDetail contact;
      String userId;
      UserDetail userDetail;
      while (iterator.hasNext()) {
        contactFather = iterator.next();
        contact = contactFather.getContactDetail();
        userId = contact.getUserId();
        if (userId != null) {
          for (UserDetail user : users) {
            userDetail = user;
            if (userId.equals(userDetail.getId())) {
              result.add(contactFather);
              break;
            }
          }
        }
      }

      // 2 - Look for contacts
      List<ContactDetail> contacts = new ArrayList<>();
      if ("LastName".equals(typeSearch)) {
        contacts = (List<ContactDetail>) getYellowpagesService()
            .getContactDetailsByLastName(new ContactPK("useless", getComponentId()), nom);
      } else if ("FirstName".equals(typeSearch)) {
        contacts = (List<ContactDetail>) getYellowpagesService()
            .getContactDetailsByLastNameAndFirstName(new ContactPK("useless", getComponentId()),
                nom, prenom);
      } else if ("LastNameFirstName".equals(typeSearch)) {
        if (prenom == null) {
          // nom ou prenom
          contacts = (List<ContactDetail>) getYellowpagesService()
              .getContactDetailsByLastNameOrFirstName(new ContactPK("useless", getComponentId()),
                  nom);
        } else {
          // nom et prenom
          contacts = (List<ContactDetail>) getYellowpagesService()
              .getContactDetailsByLastNameAndFirstName(new ContactPK("useless", getComponentId()),
                  nom, prenom);
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
      List<CompleteContact> listCompleteUsers = searchCompleteUsers(query);

      Iterator<ContactFatherDetail> iterator = contactsUser.iterator();
      ContactFatherDetail contactFather;
      Iterator<UserFull> itUserFull;
      UserFull userFull;
      Iterator<CompleteContact> itUserComplete;
      CompleteContact userComplete;
      while (iterator.hasNext()) {
        contactFather = iterator.next();
        if (contactFather.getNodeId() != null && contactFather.getNodeId()
            .startsWith(YellowpagesSessionController.GroupReferentielPrefix) &&
            contactFather.getContactDetail().getUserId() != null) {
          // contact de type user appartenant à un groupe Silverpeas
          itUserFull = listFullUsers.iterator();
          while (itUserFull.hasNext()) {
            userFull = itUserFull.next();
            if (contactFather.getContactDetail().getUserId().equals(userFull.getId())) {
              result.add(contactFather);
              break;
            }
          }
        } else {
          // contacts annuaire interne et externe
          itUserComplete = listCompleteUsers.iterator();
          while (itUserComplete.hasNext()) {
            userComplete = itUserComplete.next();
            if (contactFather.getContactDetail().getPK().getId()
                .equals(userComplete.getContactDetail().getPK().getId())) {
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
      boolean retourneUserReferentiel) {
    List<ContactFatherDetail> result = new ArrayList<>();
    if (contacts != null) {
      for (ContactDetail contact : contacts) {
        if (retourneUserReferentiel || (!retourneUserReferentiel && contact.getUserId() == null)) {
          Collection<NodePK> fathers = getContactFathers(contact.getPK().getId());
          for (NodePK nodePK : fathers) {
            if (!"1".equals(nodePK.getId()) && !"2".equals(nodePK.getId())) {
              String nodeName = getSubTopicDetail(nodePK.getId()).getName();
              ContactFatherDetail contactFather =
                  new ContactFatherDetail(contact, nodePK.getId(), nodeName);
              result.add(contactFather);
            }
          }
        }
      }
    }
    return result;
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

  public NodeService getNodeBm() {
    try {
      return NodeService.get();
    } catch (Exception re) {
      throw new YellowpagesRuntimeException("YellowpagesSessionController.getNodeService()",
          SilverpeasRuntimeException.ERROR, "yellowpages.EX_GET_NODEBM_HOME_FAILED", re);
    }
  }

  public int getNbContactPerPage() {
    return Integer.parseInt(getSettings().getString("nbContactPerPage", "20"));
  }

  /**
   * @param models
   */
  public void addModelUsed(String[] models) {
    getYellowpagesService().addModelUsed(models, getComponentId());
  }

  public Collection<String> getModelUsed() {
    return getYellowpagesService().getModelUsed(getComponentId());
  }

  public void deleteBasketContent() throws FormException, PublicationTemplateException {
    SilverTrace.info("yellowpages", "YellowpagesSessionControl.deleteBasketContent",
        "root.MSG_ENTER_METHOD");
    TopicDetail td = getCurrentTopic();
    Collection<UserContact> pds = td.getContactDetails();
    SilverTrace.info("yellowpages", "YellowpagesSessionControl.deleteBasketContent",
        "root.MSG_PARAM_VALUE", "NbContacts=" + pds.size());
    for (UserContact userContact : pds) {
      SilverTrace.info("yellowpages", "YellowpagesSessionControl.deleteBasketContent",
          "root.MSG_PARAM_VALUE", "Deleting Contact #" + userContact.getContact().getPK().getId());
      deleteContact(userContact.getContact().getPK().getId());
    }
  }

  public List<String> getProperties() {
    List<String> properties = new ArrayList<>();
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
    List<String> arrayHeaders = new ArrayList<>();
    List<String> properties = getProperties();
    String nameHeader;
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

  public String exportAsCSV() {
    List<StringBuilder> csvRows = exportAllDataAsCSV();

    return writeCSVFile(csvRows);
  }

  private List<StringBuilder> exportAllDataAsCSV() {
    Collection<ContactFatherDetail> contacts = getAllContactDetails(currentTopic.getNodePK());
    List<StringBuilder> csvRows = new ArrayList<>();
    // Can't export all columns because data are heterogenous
    StringBuilder csvRow = getCSVCols();

    for (ContactFatherDetail contactFatherDetail : contacts) {
      ContactDetail contact = contactFatherDetail.getContactDetail();
      if (contact != null) {
        csvRow = new StringBuilder();
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
            String xmlFormShortName = xmlFormName.substring(0, xmlFormName.indexOf('.'));
            PublicationTemplateImpl pubTemplate =
                (PublicationTemplateImpl) getPublicationTemplateManager()
                    .getPublicationTemplate(getComponentId() + ":" + xmlFormShortName, xmlFormName);

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
                  FieldDisplayer fieldDisplayer =
                      TypeManager.getInstance().getDisplayer(fieldType, "simpletext");
                  if (fieldDisplayer != null) {
                    fieldDisplayer.display(out, field, fieldTemplate, new PagesContext());
                  }
                  String fieldValue = sw.getBuffer().toString();
                  // removing ending carriage return appended by out.println() of displayers
                  addCSVValue(csvRow, fieldValue.replaceFirst("\\s+$", ""));
                }
              }
            }
          }
        } catch (Exception e) {
          SilverTrace.warn("yellowpages", "YellowpagesSessionController.exportAllDataAsCSV",
              "yellowpages.EX_GET_USER_DETAIL_FAILED",
              "modelId = " + modelId + ", contactId = " + contact.getPK().getId());
        }
        // Remove final ","
        csvRow.deleteCharAt(csvRow.lastIndexOf(","));
        csvRows.add(csvRow);
      }
    }

    return csvRows;
  }

  private StringBuilder getCSVCols() {
    StringBuilder csvRow = new StringBuilder();
    addCSVValue(csvRow, getString("yellowpages.column.lastname"));
    addCSVValue(csvRow, getString("yellowpages.column.firstname"));
    addCSVValue(csvRow, getString("yellowpages.column.email"));
    addCSVValue(csvRow, getString("yellowpages.column.phone"));

    return csvRow;
  }

  private void addCSVValue(StringBuilder row, String value) {
    row.append("\"");
    if (value != null) {
      row.append(value.replaceAll("\"", "\"\""));
    }
    row.append("\"").append(",");
  }

  private String writeCSVFile(List<StringBuilder> csvRows) {
    String csvFilename = new Date().getTime() + ".csv";
    try (FileOutputStream fileOutput = new FileOutputStream(
        FileRepositoryManager.getTemporaryPath() + csvFilename)) {

      StringBuilder csvRow;
      for (StringBuilder csvRow1 : csvRows) {
        csvRow = csvRow1;
        fileOutput.write(csvRow.toString().getBytes());
        fileOutput.write("\n".getBytes());
      }
    } catch (Exception e) {
      SilverTrace.error("yellowpages", "YellowpagesSessionController.writeCSVFile()",
          "Problem to write csv file", e);
      csvFilename = null;
    }
    return csvFilename;
  }

  /**
   * Import Csv file
   * @param filePart
   * @param modelId
   * @return ImportReport
   * @throws com.stratelia.webactiv.yellowpages.YellowpagesException
   */
  public ImportReport importCSV(FileItem filePart, String modelId) throws YellowpagesException {
    SilverTrace.info("yellowpages", "YellowpagesSessionController.importCSV()",
        "root.MSG_GEN_ENTER_METHOD");

    ImportReport report = new ImportReport();
    try {
      InputStream is = filePart.getInputStream();
      CSVReader csvReader = new CSVReader(getLanguage());
      csvReader.setColumnNumberControlEnabled(false);
      csvReader.setExtraColumnsControlEnabled(false);
      csvReader
          .initCSVFormat("org.silverpeas.yellowpages.settings.yellowpagesSettings", "User", ",");

      try {
        Variant[][] csvHeaderValues = csvReader.parseStream(is);

        int nbColumns = csvReader.getM_nbCols() + csvReader.getM_specificNbCols();

        int currentLine = 1;
        int nbContactsAdded = 0;
        for (Variant[] csvHeaderValue : csvHeaderValues) {
          String[] CSVRow = new String[csvHeaderValue.length];
          // Read all columns
          for (int column = 0; column < nbColumns; column++) {
            CSVRow[column] = formatStringSeparator(csvHeaderValue[column].getValueString());
          }
          // Header columns (lastName, firstName, email, phone, fax)
          ContactDetail contactDetail =
              new ContactDetail("X", CSVRow[1], CSVRow[0], CSVRow[2], CSVRow[3], CSVRow[4], null,
                  null, null);
          CompleteContact contact = new CompleteContact(contactDetail, modelId);
          try {
            List<String> values = new ArrayList<>();
            for (int column = csvReader.getM_nbCols();
                 column < csvReader.getM_nbCols() + csvReader.getM_specificNbCols(); column++) {
              String value = formatStringSeparator(CSVRow[column]);
              values.add(value);
            }
            contact.setFormValues(values);

            String contactId = createContact(contact);
            setCurrentContact(contact);
            nbContactsAdded++;
          } catch (Exception re) {
            report.addError("Erreur à la ligne #" + currentLine);
            SilverTrace.error("yellowpages", "YellowpagesSessionController.importCSV",
                "yellowpages.EX_CSV_FILE", re);
          }
          currentLine++;
        }
        report.setNbAdded(nbContactsAdded);
      } catch (UtilTrappedException ute) {
        SilverTrace.error("yellowpages", "YellowpagesSessionController.importCSV",
            "yellowpages.EX_CSV_FILE", ute);
        report.addError(ute.getExtraInfos());
      }
    } catch (IOException e) {
      SilverTrace
          .error("yellowpages", "YellowpagesSessionController.importCSV", "yellowpages.EX_CSV_FILE",
              e);
      report.addError(e.getMessage());
    }

    SilverTrace.info("yellowpages", "YellowpagesSessionController.importCSV()",
        "root.MSG_GEN_EXIT_METHOD");

    return report;
  }

  /**
   * Remove start & end " and replace double " by single "
   * @param value
   * @return
   */
  private String formatStringSeparator(String value) {
    String newValue = value;
    if (StringUtil.isDefined(value) && value.startsWith("\"") && value.endsWith("\"")) {
      newValue = value.substring(1, value.length() - 1);
      newValue = newValue.replaceAll("\"\"", "\"");
    }
    return newValue;
  }

  /**
   * Gets an instance of PublicationTemplateManager.
   * @return an instance of PublicationTemplateManager.
   */
  private PublicationTemplateManager getPublicationTemplateManager() {
    return PublicationTemplateManager.getInstance();
  }

  public boolean useForm() {
    String modelId = getCurrentTopic().getNodeDetail().getModelId();
    return StringUtil.isDefined(modelId) && !"0".equals(modelId);
  }

  public List<PublicationTemplate> getForms() {
    List<PublicationTemplate> templates = new ArrayList<>();
    try {
      GlobalContext theContext = new GlobalContext(getSpaceId(), getComponentId());
      templates = getPublicationTemplateManager().getPublicationTemplates(theContext);
    } catch (PublicationTemplateException e) {
      SilverTrace
          .error("yellowpages", "YellowpagesSessionController.getForms()", "root.CANT_GET_FORMS",
              e);
    }
    return templates;
  }
}
