/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
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
package com.silverpeas.formsonline.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.util.GlobalContext;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.formsonline.model.FormDetail;
import com.silverpeas.formsonline.model.FormInstance;
import com.silverpeas.formsonline.model.FormsOnlineDAO;
import com.silverpeas.formsonline.model.FormsOnlineDAOJdbc;
import com.silverpeas.formsonline.model.FormsOnlineDatabaseException;
import com.silverpeas.formsonline.model.FormsOnlineRuntimeException;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import org.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.notificationManager.GroupRecipient;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.util.PairObject;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.util.GeneralPropertiesManager;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.util.exception.SilverpeasRuntimeException;

public class FormsOnlineSessionController extends AbstractComponentSessionController {

  private FormsOnlineDAO dao = new FormsOnlineDAOJdbc();
  private FormDetail currentForm;
  protected Selection m_Selection = null;
  private SelectionUsersGroups sugSenders = null;
  private SelectionUsersGroups sugReceivers = null;
  private FormDetail choosenForm;
  private NotificationSender notifSender;
  private ResourceLocator messagesEn =
      new ResourceLocator("com.silverpeas.formsonline.multilang.formsOnlineBundle", "en");
  private ResourceLocator messagesFr =
      new ResourceLocator("com.silverpeas.formsonline.multilang.formsOnlineBundle", "fr");

  /**
   * Standard Session Controller Constructeur
   *
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public FormsOnlineSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.silverpeas.formsonline.multilang.formsOnlineBundle",
        "com.silverpeas.formsonline.settings.formsOnlineIcons",
        "com.silverpeas.formsonline.settings.formsOnlineSettings");
    m_Selection = getSelection();
  }

  public List<FormDetail> getAllForms() throws FormsOnlineDatabaseException {
    return dao.findAllForms(getComponentId());
  }

  public void setCurrentForm(FormDetail form) {
    this.currentForm = form;
  }

  public FormDetail getCurrentForm() {
    return this.currentForm;
  }

  public void updateCurrentForm() throws FormsOnlineDatabaseException {
    if (currentForm.getId() == -1) {
      currentForm.setInstanceId(getComponentId());
      currentForm = dao.createForm(currentForm);
    } else {
      dao.updateForm(currentForm);
    }
  }

  public FormDetail loadForm(int formId) throws FormsOnlineDatabaseException {
    this.currentForm = dao.getForm(getComponentId(), formId);
    return currentForm;
  }

  public void deleteForm(int formId) throws FormsOnlineDatabaseException {
    dao.deleteForm(getComponentId(), formId);
  }

  // initialisation de Selection pour nav vers SelectionPeas
  private String initSelection(SelectionUsersGroups sug, String goFunction,
      String[] userIds, String[] groupIds) {
    String url = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL")
        + URLManager.getURL(getSpaceId(), getComponentId());
    String goUrl = url + goFunction;
    String cancelUrl = url + "SendersReceivers";

    m_Selection.resetAll();

    m_Selection.setGoBackURL(goUrl);
    m_Selection.setCancelURL(cancelUrl);

    m_Selection.setSelectedElements(userIds);
    m_Selection.setSelectedSets(groupIds);

    // bien que le up s'affiche en popup, le mecanisme de fermeture est
    // assure par le composant=> il est donc necessaire d'indiquer
    // a l'UserPanelPeas de ne pas s'occuper de cette fermeture!
    m_Selection.setHostPath(null);
    PairObject hostComponentName = new PairObject(getComponentLabel(), null);
    m_Selection.setHostComponentName(hostComponentName);
    m_Selection.setHostSpaceName(getSpaceLabel());
    m_Selection.setFirstPage(Selection.FIRST_PAGE_BROWSE);

    // Add extra params
    m_Selection.setExtraParams(sug);
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  public String initSelectionSenders() throws FormsOnlineDatabaseException {
    ArrayList<String> profiles = new ArrayList<String>();
    profiles.add("SenderReceiver");
    sugSenders = new SelectionUsersGroups();
    sugSenders.setComponentId(getComponentId());
    sugSenders.setProfileNames(profiles);

    String[] userIds = (String[]) (dao.getSendersAsUsers(currentForm.getId(), getComponentId())).
        toArray(new String[0]);
    String[] groupIds = (String[]) (dao.getSendersAsGroups(currentForm.getId(), getComponentId())).
        toArray(new String[0]);

    return initSelection(sugSenders, "UpdateSenders", userIds, groupIds);
  }

  public String initSelectionReceivers() throws FormsOnlineDatabaseException {
    ArrayList<String> profiles = new ArrayList<String>();
    profiles.add("SenderReceiver");
    sugReceivers = new SelectionUsersGroups();
    sugReceivers.setComponentId(getComponentId());
    sugReceivers.setProfileNames(profiles);

    String[] userIds = (String[]) (dao.getReceiversAsUsers(currentForm.getId(), getComponentId())).
        toArray(new String[0]);
    String[] groupIds =
        (String[]) (dao.getReceiversAsGroups(currentForm.getId(), getComponentId())).
        toArray(new String[0]);

    return initSelection(sugReceivers, "UpdateReceivers", userIds, groupIds);
  }

  public List<UserDetail> getSendersAsUsers() throws FormsOnlineDatabaseException {
    List<String> userIds = dao.getSendersAsUsers(currentForm.getId(), getComponentId());
    UserDetail[] details = getOrganisationController().
        getUserDetails(userIds.toArray(new String[0]));
    return Arrays.asList(details);
  }

  public List<Group> getSendersAsGroups() throws FormsOnlineDatabaseException {
    List<String> groupIds = dao.getSendersAsGroups(currentForm.getId(), getComponentId());
    Group[] groups = getOrganisationController().getGroups(groupIds.toArray(new String[0]));
    return Arrays.asList(groups);
  }

  public List<UserDetail> getReceiversAsUsers() throws FormsOnlineDatabaseException {
    List<String> userIds = dao.getReceiversAsUsers(currentForm.getId(), getComponentId());
    UserDetail[] details = getOrganisationController().
        getUserDetails(userIds.toArray(new String[0]));
    return Arrays.asList(details);
  }

  public List<Group> getReceiversAsGroups() throws FormsOnlineDatabaseException {
    List<String> groupIds = dao.getReceiversAsGroups(currentForm.getId(), getComponentId());
    Group[] groups = getOrganisationController().getGroups(groupIds.toArray(new String[0]));
    return Arrays.asList(groups);
  }

  public void updateSenders() throws FormsOnlineDatabaseException {
    dao.updateSenders(currentForm.getId(), getComponentId(), m_Selection.getSelectedElements(),
        m_Selection.getSelectedSets());
  }

  public void updateReceivers() throws FormsOnlineDatabaseException {
    dao.updateReceivers(currentForm.getId(), getComponentId(), m_Selection.getSelectedElements(),
        m_Selection.getSelectedSets());
  }

  public void publishForm(String formId) throws NumberFormatException,
      FormsOnlineDatabaseException {
    FormDetail form = dao.getForm(getComponentId(), Integer.parseInt(formId));
    form.setState(FormDetail.STATE_PUBLISHED);
    dao.updateForm(form);
  }

  public void unpublishForm(String formId) throws NumberFormatException,
      FormsOnlineDatabaseException {
    FormDetail form = dao.getForm(getComponentId(), Integer.parseInt(formId));
    form.setState(FormDetail.STATE_UNPUBLISHED);
    dao.updateForm(form);
  }

  public List<FormDetail> getAvailableFormsToSend() throws FormsOnlineDatabaseException {
    String userId = getUserId();
    String[] userGroupIds = getOrganisationController().getAllGroupIdsOfUser(userId);
    return dao.getUserAvailableForms(getComponentId(), userId, userGroupIds);
  }

  public List<FormInstance> getFormInstances(int choosenFormId)
      throws FormsOnlineDatabaseException {
    if (choosenFormId == -1) {
      return new ArrayList<FormInstance>();
    }

    return dao.getSentFormInstances(getComponentId(), choosenFormId, getUserId());
  }

  public void setChoosenForm(FormDetail choosenForm) {
    this.choosenForm = choosenForm;
  }

  public FormDetail getChoosenForm() {
    return choosenForm;
  }

  public void saveNewInstance(List<FileItem> items)
      throws FormsOnlineDatabaseException, PublicationTemplateException,
      FormException {
    FormInstance instance = new FormInstance();
    instance.setCreatorId(getUserId());
    instance.setFormId(choosenForm.getId());
    instance.setInstanceId(getComponentId());
    instance.setState(FormInstance.STATE_UNREAD);
    instance = dao.createInstance(instance);

    String xmlFormName = choosenForm.getXmlFormName();
    String xmlFormShortName =
        xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName.indexOf('.'));

    // Retrieve data form (with DataRecord object)
    PublicationTemplate pub = getPublicationTemplateManager().getPublicationTemplate(
        getComponentId() + ":" + xmlFormShortName);
    RecordSet set = pub.getRecordSet();
    Form form = pub.getUpdateForm();
    DataRecord data = set.getEmptyRecord();
    data.setId(String.valueOf(instance.getId()));

    // Save data form
    PagesContext aContext = new PagesContext("newInstanceForm", "0", getLanguage(), false,
        getComponentId(), getUserId());
    aContext.setObjectId(String.valueOf(instance.getId()));
    form.update(items, data, aContext);
    set.save(data);

    // Notify receivers
    notifyReceivers(choosenForm.getId(), instance.getId());
  }

  /**
   * getNotificationSender
   */
  public NotificationSender getNotificationSender() {
    if (notifSender == null) {
      notifSender = new NotificationSender(getComponentId());
    }
    return notifSender;
  }

  /**
   * notifyReceivers
   *
   * @throws FormsOnlineDatabaseException
   */
  private void notifyReceivers(int formId, int formInstanceId) throws FormsOnlineDatabaseException {

    FormDetail form = dao.getForm(getComponentId(), formId);
    String emetteur = getUserDetail().getDisplayedName();

    // french notifications
    String subject = messagesFr.getString("formsOnline.msgFormToValid");
    String messageText = emetteur + " " + messagesFr.getString(
        "formsOnline.msgUserHasSentAForm") + "  \n \n";

    // english notifications
    String subject_en = messagesEn.getString("formsOnline.msgFormToValid");
    String messageText_en = emetteur + " " + messagesEn.getString(
        "formsOnline.msgUserHasSentAForm") + "  \n \n";

    NotificationMetaData notifMetaData = new NotificationMetaData(
        NotificationParameters.NORMAL, subject, messageText);
    notifMetaData.setSender(getUserId());
    List<String> userIds = dao.getReceiversAsUsers(formId, getComponentId());
    for (String user : userIds) {
      notifMetaData.addUserRecipient(new UserRecipient(user));
    }
    List<String> groupIds = dao.getReceiversAsGroups(formId, getComponentId());
    for (String group : groupIds) {
      notifMetaData.addGroupRecipient(new GroupRecipient(group));
    }
    notifMetaData.setSource(getSpaceLabel() + " - " + form.getName());
    notifMetaData.setLink("/RformsOnline/" + getComponentId()
        + "/ValidFormInstance?formInstanceId=" + formInstanceId);

    notifMetaData.addLanguage("en", subject_en, messageText_en);
    notifMetaData.addLanguage("fr", subject, messageText);
    try {
      getNotificationSender().notifyUser(notifMetaData);
    } catch (NotificationManagerException e) {
      SilverTrace.error("formManager", "FormManagerSessionController.notifyReceivers()",
          "root.MSG_GEN_PARAM_VALUE", "formInstanceId = " + formInstanceId,
          new FormsOnlineRuntimeException(
          "com.silverpeas.formsonline.control.FormsOnlineSessionController",
          SilverpeasRuntimeException.ERROR, ""));
    }
  }

  /**
   * notifySender
   *
   * @throws FormsOnlineDatabaseException
   */
  private void notifySender(FormInstance formInstance) throws FormsOnlineDatabaseException {

    FormDetail form = dao.getForm(getComponentId(), formInstance.getFormId());

    // Subject
    String subject;
    String subject_en;
    if (formInstance.getState() == FormInstance.STATE_VALIDATED) {
      subject = messagesFr.getString("formsOnline.msgFormValidated");
      subject_en = messagesEn.getString("formsOnline.msgFormValidated");
    } else {
      subject = messagesFr.getString("formsOnline.msgFormRefused");
      subject_en = messagesEn.getString("formsOnline.msgFormRefused");
    }

    // sender
    String sender = getUserDetail().getDisplayedName();

    // message
    StringBuffer messageText = new StringBuffer(sender).append(" ");
    StringBuffer messageText_en = new StringBuffer(sender).append(" ");
    if (formInstance.getState() == FormInstance.STATE_VALIDATED) {
      messageText.append(messagesFr.getString("formsOnline.msgHasValidatedYourForm"));
      messageText_en.append(messagesEn.getString("formsOnline.msgHasValidatedYourForm"));
    } else {
      messageText.append(messagesFr.getString("formsOnline.msgHasRefusedYourForm"));
      messageText_en.append(messagesEn.getString("formsOnline.msgHasValidatedYourForm"));
      if (StringUtil.isDefined(formInstance.getComments())) {
        messageText.append(" ").append(messagesFr.getString("formsOnline.notif.comment"))
            .append(formInstance.getComments());
        messageText_en.append(" ").append(messagesEn.getString("formsOnline.notif.comment"))
            .append(formInstance.getComments());
      }
    }

    NotificationMetaData notifMetaData = new NotificationMetaData(
        NotificationParameters.NORMAL, subject, messageText.toString());
    notifMetaData.setSender(getUserId());
    notifMetaData.addUserRecipient(new UserRecipient(formInstance.getCreatorId()));
    notifMetaData.setSource(getSpaceLabel() + " - " + form.getName());
    notifMetaData.setLink("/RformsOnline/" + getComponentId()
        + "/ViewFormInstance?formInstanceId=" + formInstance.getId());
    notifMetaData.addLanguage("en", subject_en, messageText_en.toString());
    notifMetaData.addLanguage("fr", subject, messageText.toString());

    try {
      getNotificationSender().notifyUser(notifMetaData);
    } catch (NotificationManagerException e) {
      SilverTrace.error("formManager", "FormManagerSessionController.notifySender()",
          "root.MSG_GEN_PARAM_VALUE", "formInstanceId = " + formInstance.getId(),
          new FormsOnlineRuntimeException(
          "com.silverpeas.formsonline.control.FormsOnlineSessionController",
          SilverpeasRuntimeException.ERROR, ""));
    }
  }

  public List<String> getAvailableFormIdsAsReceiver() throws FormsOnlineDatabaseException {
    String userId = getUserId();
    String[] userGroupIds = getOrganisationController().getAllGroupIdsOfUser(userId);
    return dao.getAvailableFormIdsAsReceiver(getComponentId(), userId, userGroupIds);
  }

  public List<FormInstance> getAvailableFormInstancesReceived(int formId)
      throws FormsOnlineDatabaseException {
    return dao.getReceivedFormInstances(getComponentId(), getUserId(), formId);
  }

  public FormInstance loadFormInstance(int formInstanceId) throws FormsOnlineDatabaseException {
    return dao.getFormInstance(getComponentId(), formInstanceId);
  }

  public List<FormDetail> getForms(List<String> formIds) throws FormsOnlineDatabaseException {
    return dao.getForms(formIds);
  }

  public void updateValidationStatus(int formInstanceId, String decision,
      String comments) throws FormsOnlineDatabaseException {
    FormInstance instance = loadFormInstance(formInstanceId);

    // update state
    if ("validate".equals(decision)) {
      instance.setState(FormInstance.STATE_VALIDATED);
    } else {
      instance.setState(FormInstance.STATE_REFUSED);
    }

    // validation infos
    instance.setValidationDate(new Date());
    instance.setValidatorId(getUserId());
    instance.setComments(comments);

    // save modifications
    dao.updateFormInstance(instance);

    // notify sender
    notifySender(instance);
  }

  public void updateFormInstance(FormInstance formInstance) throws FormsOnlineDatabaseException {
    dao.updateFormInstance(formInstance);
  }

  public void archiveFormInstances(String[] formInstanceIds) throws FormsOnlineDatabaseException {
    for (int i = 0; i < formInstanceIds.length; i++) {
      FormInstance instance =
          dao.getFormInstance(getComponentId(), Integer.parseInt(formInstanceIds[i]));
      instance.setState(FormInstance.STATE_ARCHIVED);
      dao.updateFormInstance(instance);
    }
  }

  public void deleteFormInstances(String[] formInstanceIds) throws FormsOnlineDatabaseException,
      FormException, PublicationTemplateException {
    for (int i = 0; i < formInstanceIds.length; i++) {
      // delete form data
      FormInstance instance =
          dao.getFormInstance(getComponentId(), Integer.parseInt(formInstanceIds[i]));
      FormDetail form = dao.getForm(getComponentId(), instance.getFormId());
      String xmlFormName = form.getXmlFormName();
      String xmlFormShortName =
          xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName.indexOf('.'));
      PublicationTemplate pubTemplate = getPublicationTemplateManager().getPublicationTemplate(
          getComponentId() + ":" + xmlFormShortName);
      RecordSet set = pubTemplate.getRecordSet();
      DataRecord data = set.getRecord(formInstanceIds[i]);
      set.delete(data);

      // delete instance metadata
      dao.deleteFormInstance(getComponentId(), instance.getId());
    }
  }

  public void filter(List<FormInstance> receivedForInstances, String filteredState) {
    if (filteredState != null && filteredState.length() > 0) {
      int state = Integer.parseInt(filteredState);
      Iterator<FormInstance> it = receivedForInstances.iterator();
      while (it.hasNext()) {
        FormInstance instance = it.next();
        if (instance.getState() != state) {
          it.remove();
        }
      }
    }
  }
  
  /**
   * Gets an instance of PublicationTemplateManager.
   * @return an instance of PublicationTemplateManager.
   */
  private PublicationTemplateManager getPublicationTemplateManager() {
    return PublicationTemplateManager.getInstance();
  }
  
  public List<PublicationTemplate> getTemplates() {
    List<PublicationTemplate> templates = new ArrayList<PublicationTemplate>();
    try {
      GlobalContext aContext = new GlobalContext(getSpaceId(), getComponentId());
      templates = getPublicationTemplateManager().getPublicationTemplates(aContext);
    } catch (PublicationTemplateException e) {
      SilverTrace.error("formManager", "FormsOnlineSessionController.getForms()", "root.CANT_GET_FORMS", e);
    }
    return templates;
  }
}
