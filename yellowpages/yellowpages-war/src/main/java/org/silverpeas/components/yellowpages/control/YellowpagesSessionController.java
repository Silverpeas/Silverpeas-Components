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
package org.silverpeas.components.yellowpages.control;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.components.yellowpages.ImportReport;
import org.silverpeas.components.yellowpages.YellowpagesException;
import org.silverpeas.components.yellowpages.model.TopicDetail;
import org.silverpeas.components.yellowpages.model.UserContact;
import org.silverpeas.components.yellowpages.model.YellowPagesGroupDetail;
import org.silverpeas.components.yellowpages.model.YellowpagesRuntimeException;
import org.silverpeas.components.yellowpages.service.YellowpagesService;
import org.silverpeas.core.admin.component.model.CompoSpace;
import org.silverpeas.core.admin.component.model.GlobalContext;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserDetailsSearchCriteria;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.contact.model.CompleteContact;
import org.silverpeas.core.contact.model.ContactDetail;
import org.silverpeas.core.contact.model.ContactFatherDetail;
import org.silverpeas.core.contact.model.ContactPK;
import org.silverpeas.core.contribution.content.form.AbstractForm;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.form.TypeManager;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateImpl;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.exception.UtilTrappedException;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.csv.CSVReader;
import org.silverpeas.core.util.csv.Variant;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.selection.Selection;

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
import java.util.MissingResourceException;
import java.util.stream.Collectors;

import static org.silverpeas.components.yellowpages.YellowpagesComponentSettings.areUserExtraDataRequired;

public class YellowpagesSessionController extends AbstractComponentSessionController {

  private TopicDetail currentTopic = null;
  private CompleteContact currentContact = null;
  private String path = null;
  private String owner = "false";
  // admin || publisher || user
  private String profile;
  private List<YellowPagesGroupDetail> groupPath = new ArrayList<>();
  private boolean portletMode = false;
  private Collection<ContactFatherDetail> currentContacts = null;
  private Collection<UserFull> currentFullUsers = null;
  private Collection<CompleteContact> currentCompleteUsers = null;
  private String currentTypeSearch;
  private String currentSearchCriteria;
  public static final String GroupReferentielPrefix = "group_";
  private LocalizationBundle domainMultilang;

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
      SettingBundle domainProperty = ResourceLocator.getSettingBundle(domain.getPropFileName());
      domainMultilang =
          ResourceLocator.getLocalizationBundle(domainProperty.getString("property.ResourceFile"));
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

  public YellowpagesService getYellowpagesService() {
    return YellowpagesService.get();
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

  public YellowPagesGroupDetail getGroup(String groupId) {
    Group group = getOrganisationController().getGroup(groupId);
    YellowPagesGroupDetail groupDetail = new YellowPagesGroupDetail(group);

    // add sub groups
    Group[] subGroups = getOrganisationController().getAllSubGroups(groupId);
    Group subGroup;
    YellowPagesGroupDetail subGroupDetail;
    for (Group subGroup1 : subGroups) {
      subGroup = subGroup1;
      subGroupDetail = new YellowPagesGroupDetail(subGroup);
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

  private void processGroupPath(YellowPagesGroupDetail group) {
    if (groupPath.isEmpty()) {
      groupPath.add(group);
    } else {
      int index = groupPath.indexOf(group);

      if (index == -1) {
        groupPath.add(group);
      } else {
        groupPath = groupPath.subList(0, index + 1);
      }
    }
  }

  public List<YellowPagesGroupDetail> getGroupPath() {
    return groupPath;
  }

  public void clearGroupPath() {
    groupPath.clear();
  }

  public List<NodeDetail> getTree() {
    return getYellowpagesService().getTree(getComponentId());
  }

  public NodePK updateTopicHeader(NodeDetail nd) {

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
    getYellowpagesService().getContactDetail(getContactPK(contactId));
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
    String mContext = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
    String hostSpaceName = getSpaceLabel();
    Pair<String, String> hostComponentName = new Pair<>(getComponentLabel(), "");
    Pair<String, String>[] hostPath = new Pair[1];
    hostPath[0] = new Pair<>(getString("UserCreer"), "");
    String hostUrl = mContext + URLUtil.getURL(null, getComponentId()) + "saveUser";
    String cancelUrl = mContext + URLUtil.getURL(null, getComponentId()) + "ContactNew";

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

    return Selection.getSelectionURL();
  }

  /**
   * Put selected user into session using userPanel
   * @return true if a user is selected
   */
  public boolean setContactUserSelected() {
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
      return true;
    }
    return false;
  }

  public String initGroupPanel() {
    String mContext = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
    String hostSpaceName = getSpaceLabel();
    Pair<String, String> hostComponentName = new Pair<>(getComponentLabel(), "");
    Pair<String, String>[] hostPath = new Pair[1];
    hostPath[0] = new Pair<>(getString("GroupAdd"), "");
    String hostUrl = mContext + URLUtil.getURL(null, getComponentId()) + "AddGroup";
    String cancelUrl = mContext + URLUtil.getURL(null, getComponentId()) + "AddGroup";

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

    return Selection.getSelectionURL();
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
    YellowPagesGroupDetail groupDetail = getGroup(groupId);
    UserDetail[] userDetails = getOrganisationController().getFiltredDirectUsers(groupId, "");

    for (UserDetail userDetail : userDetails) {
      ContactFatherDetail contactFather = getContactFatherDetail(userDetail.getId(), groupDetail);
      if (contactFather != null) {
        users.add(contactFather);
      }
    }

    List<Group> subGroups = groupDetail.getSubGroups();
    for (Group subGroup : subGroups) {
      users.addAll(getAllUsersOfGroup(subGroup.getId()));
    }
    return users;
  }

  private ContactFatherDetail getContactFatherDetail(String userId, YellowPagesGroupDetail group) {
    ContactFatherDetail contactFather = null;
    UserDetail user = getOrganisationController().getUserDetail(userId);
    if (user != null) {
      ContactDetail cUser =
          new ContactDetail(new ContactPK("fromGroup", getComponentId()), user.getFirstName(),
              user.getLastName(), user.geteMail(), "", "", user.getId(), null, null);
      cUser.setUserExtraDataRequired(areUserExtraDataRequired());

      contactFather = new ContactFatherDetail(
          cUser,
          YellowpagesSessionController.GroupReferentielPrefix + group.getId(),
          group.getName());
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

  private String augmentQuery(String query) {
    String augmentedQuery = query;
    if (query.endsWith("*") || query.endsWith("%")) {
      augmentedQuery = query.substring(0, query.length() - 1);
    }
    return "%" + augmentedQuery + "%";
  }

  private List<ContactFatherDetail> searchByName(final String property, final String query) {
    List<ContactFatherDetail> result;

    List<ContactDetail> contacts = new ArrayList<>();
    final UserDetailsSearchCriteria criteria = new UserDetailsSearchCriteria();
    if ("LastName".equals(property)) {
      criteria.onLastName(augmentQuery(query));
      contacts = (List<ContactDetail>) getYellowpagesService().getContactDetailsByLastName(
          new ContactPK("useless", getComponentId()), criteria.getCriterionOnLastName());
    } else if ("FirstName".equals(property)) {
      criteria.onFirstName(augmentQuery(query));
      contacts = (List<ContactDetail>) getYellowpagesService()
          .getContactDetailsByLastNameAndFirstName(new ContactPK("useless", getComponentId()),
              "%", criteria.getCriterionOnFirstName());
    } else if ("LastNameFirstName".equals(property)) {
      // last name and/or first name
      int separator = query.indexOf(' ');
      if (separator == -1) {
        // only search on the last name or the first name
        criteria.onName(augmentQuery(query));
        contacts = (List<ContactDetail>) getYellowpagesService()
            .getContactDetailsByLastNameOrFirstName(new ContactPK("useless", getComponentId()),
                criteria.getCriterionOnName());
      } else {
        // search both on the last and first names
        criteria.onLastName(augmentQuery(query.substring(0, separator).trim()));
        criteria.onFirstName(augmentQuery(query.substring(separator).trim()));
        contacts = (List<ContactDetail>) getYellowpagesService()
            .getContactDetailsByLastNameAndFirstName(new ContactPK("useless", getComponentId()),
                criteria.getCriterionOnLastName(), criteria.getCriterionOnFirstName());
      }
    }
    List<UserDetail> users = getOrganisationController().searchUsers(criteria);

    // filter users who are in the component
    TopicDetail rootTopic = getTopic(NodePK.ROOT_NODE_ID);
    Collection<ContactFatherDetail> contactsUser = getAllContactDetails(rootTopic.getNodePK());

    result = contactsUser.stream().filter(contactFatherDetail -> {
      String userId = contactFatherDetail.getContactDetail().getUserId();
      return users.stream().anyMatch(userDetail -> userDetail.getId().equals(userId));
    }).collect(Collectors.toList());

    result.addAll(getListContactFather(contacts, false));

    return result;
  }

  private List<ContactFatherDetail> searchByAllProperties(String query)
      throws PublicationTemplateException, FormException {
    List<ContactFatherDetail> result = new ArrayList<>();
    // users currently in the session
    Collection<ContactFatherDetail> contactsUser = setCurrentFullCompleteUsers();
    // 1 - Look for user in Silverpeas groups
    List<UserFull> listFullUsers = searchFullUsers(query);
    // 2 - Look for contacts
    List<CompleteContact> listCompleteUsers = searchCompleteUsers(query);

    for (ContactFatherDetail contactFather: contactsUser) {
      findContact(contactFather, listFullUsers, listCompleteUsers, result);
    }

    return result;
  }

  private void findContact(final ContactFatherDetail contactFather,
      final List<UserFull> listFullUsers, final List<CompleteContact> listCompleteUsers,
      final List<ContactFatherDetail> result) {
    if (contactFather.getNodeId() != null && contactFather.getNodeId()
        .startsWith(YellowpagesSessionController.GroupReferentielPrefix) &&
        contactFather.getContactDetail().getUserId() != null) {
      // a contact that is a user that belongs to a group in Silverpeas
      for (UserFull userFull: listFullUsers) {
        if (contactFather.getContactDetail().getUserId().equals(userFull.getId())) {
          result.add(contactFather);
          break;
        }
      }
    } else {
      // a contact from an internal or external directory
      for (CompleteContact userComplete: listCompleteUsers) {
        if (contactFather.getContactDetail().getPK().getId()
            .equals(userComplete.getContactDetail().getPK().getId())) {
          result.add(contactFather);
          break;
        }
      }
    }
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
    List<ContactFatherDetail> result;
    String _query = query.trim();

    if (!"All".equals(typeSearch)) {
      // the search is on the name (first name and/or last name) of the users
      result = searchByName(typeSearch, _query);
    } else {
      // the search is on all the user properties
      result = searchByAllProperties(_query);
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
  public void setModelUsed(String[] models) {
    getYellowpagesService().setModelUsed(models, getComponentId());
  }

  public Collection<String> getModelUsed() {
    return getYellowpagesService().getModelUsed(getComponentId());
  }

  public void deleteBasketContent() throws FormException, PublicationTemplateException {

    TopicDetail td = getCurrentTopic();
    Collection<UserContact> pds = td.getContactDetails();

    for (UserContact userContact : pds) {

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
    for (String nameProperty : properties) {
      String nameHeader = null;
      try {
        if (nameProperty.startsWith("domain.")) {
          // on recherche une propriété du domaine
          String property = nameProperty.substring(7);
          nameHeader = getDomainMultilang().getString(property);
        } else {
          // on recherche une propriété classique
          nameHeader = getMultilang().getString("yellowpages.column." + nameProperty);
        }
      } catch (MissingResourceException ignore) {
      }
      arrayHeaders.add(nameHeader);
    }
    return arrayHeaders;
  }

  private LocalizationBundle getDomainMultilang() {
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
   * @throws YellowpagesException
   */
  public ImportReport importCSV(FileItem filePart, String modelId) throws YellowpagesException {


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

        int nbColumns = csvReader.getNbCols() + csvReader.getSpecificNbCols();

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
            for (int column = csvReader.getNbCols();
                 column < csvReader.getNbCols() + csvReader.getSpecificNbCols(); column++) {
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
