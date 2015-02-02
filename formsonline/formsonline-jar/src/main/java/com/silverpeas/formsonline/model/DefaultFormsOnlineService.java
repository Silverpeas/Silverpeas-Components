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
package com.silverpeas.formsonline.model;

import com.silverpeas.ApplicationService;
import com.silverpeas.annotation.Service;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.usernotification.builder.helper.UserNotificationHelper;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.core.admin.OrganizationController;
import org.silverpeas.util.ResourceLocator;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.components.formsonline.notification
    .FormsOnlinePendingValidationRequestUserNotification;
import org.silverpeas.components.formsonline.notification
    .FormsOnlineProcessedRequestUserNotification;
import org.silverpeas.components.formsonline.notification
    .FormsOnlineValidationRequestUserNotification;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class DefaultFormsOnlineService
    implements FormsOnlineService, ApplicationService<FormInstance> {

  @Inject
  private OrganizationController organizationController;

  @Override
  public List<FormDetail> getAllForms(final String appId, final String userId,
      final boolean withSendInfo) throws FormsOnlineDatabaseException {
    List<FormDetail> forms = getDAO().findAllForms(appId);
    if (withSendInfo) {
      for (FormDetail form : forms) {
        form.setSendable(isSender(form.getPK(), userId));
      }
    }
    return forms;
  }

  private boolean isSender(FormPK pk, String userId) throws FormsOnlineDatabaseException {
    return isInLists(userId, getSendersAsUsers(pk), getSendersAsGroups(pk));
  }

  private List<UserDetail> getSendersAsUsers(FormPK pk) throws FormsOnlineDatabaseException {
    List<String> userIds = getDAO().getSendersAsUsers(pk);
    UserDetail[] details = organizationController.getUserDetails(userIds.toArray(new String[0]));
    return Arrays.asList(details);
  }

  private List<Group> getSendersAsGroups(FormPK pk) throws FormsOnlineDatabaseException {
    List<String> groupIds = getDAO().getSendersAsGroups(pk);
    Group[] groups = organizationController.getGroups(groupIds.toArray(new String[0]));
    return Arrays.asList(groups);
  }

  private List<UserDetail> getReceiversAsUsers(FormPK pk) throws FormsOnlineDatabaseException {
    List<String> userIds = getDAO().getReceiversAsUsers(pk);
    UserDetail[] details = organizationController.getUserDetails(userIds.toArray(new String[0]));
    return Arrays.asList(details);
  }

  private List<Group> getReceiversAsGroups(FormPK pk) throws FormsOnlineDatabaseException {
    List<String> groupIds = getDAO().getReceiversAsGroups(pk);
    Group[] groups = organizationController.getGroups(groupIds.toArray(new String[0]));
    return Arrays.asList(groups);
  }

  private boolean isValidator(FormPK pk, String userId) throws FormsOnlineDatabaseException {
    return isInLists(userId, getReceiversAsUsers(pk), getReceiversAsGroups(pk));
  }

  private boolean isInLists(String userId, List<? extends UserDetail> users, List<Group> groups) {
    boolean inList = isInList(userId, users);
    if (!inList) {
      for (Group group : groups) {
        inList = isInList(userId, group.getAllUsers());
        if (inList) {
          return true;
        }
      }
    }
    return inList;
  }

  private boolean isInList(String userId, List<? extends UserDetail> users) {
    for (UserDetail user : users) {
      if (user.getId().equals(userId)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public FormDetail loadForm(FormPK pk) throws FormsOnlineDatabaseException {
    FormDetail form = getDAO().getForm(pk);
    form.setSendersAsUsers(getSendersAsUsers(pk));
    form.setSendersAsGroups(getSendersAsGroups(pk));
    form.setReceiversAsUsers(getReceiversAsUsers(pk));
    form.setReceiversAsGroups(getReceiversAsGroups(pk));
    return form;
  }

  @Override
  public FormDetail storeForm(FormDetail form, String[] senderUserIds, String[] senderGroupIds,
      String[] receiverUserIds, String[] receiverGroupIds) throws FormsOnlineDatabaseException {
    if (form.getId() == -1) {
      form = getDAO().createForm(form);
    } else {
      getDAO().updateForm(form);
    }
    getDAO().updateSenders(form.getPK(), senderUserIds, senderGroupIds);
    getDAO().updateReceivers(form.getPK(), receiverUserIds, receiverGroupIds);
    form.setSendersAsUsers(getSendersAsUsers(form.getPK()));
    form.setSendersAsGroups(getSendersAsGroups(form.getPK()));
    form.setReceiversAsUsers(getReceiversAsUsers(form.getPK()));
    form.setReceiversAsGroups(getReceiversAsGroups(form.getPK()));
    return form;
  }

  public void deleteForm(FormPK pk) throws FormsOnlineDatabaseException {
    getDAO().deleteForm(pk);
  }

  public void publishForm(FormPK pk) throws FormsOnlineDatabaseException {
    FormDetail form = getDAO().getForm(pk);
    form.setState(FormDetail.STATE_PUBLISHED);
    getDAO().updateForm(form);
  }

  public void unpublishForm(FormPK pk) throws FormsOnlineDatabaseException {
    FormDetail form = getDAO().getForm(pk);
    form.setState(FormDetail.STATE_UNPUBLISHED);
    getDAO().updateForm(form);
  }

  public List<FormDetail> getAvailableFormsToSend(String appId, String userId)
      throws FormsOnlineDatabaseException {
    String[] userGroupIds = organizationController.getAllGroupIdsOfUser(userId);
    return getDAO().getUserAvailableForms(appId, userId, userGroupIds);
  }

  public RequestsByStatus getAllUserRequests(String appId, String userId)
      throws FormsOnlineDatabaseException {
    RequestsByStatus allRequests = new RequestsByStatus();
    List<FormDetail> forms = getAllForms(appId, userId, false);
    for (FormDetail form : forms) {
      List<FormInstance> requests = getUserRequestsByForm(form.getPK(), userId);
      allRequests.add(requests, form);
    }
    return allRequests;
  }

  public List<FormInstance> getUserRequestsByForm(FormPK pk, String userId)
      throws FormsOnlineDatabaseException {
    return getDAO().getSentFormInstances(pk, userId);
  }

  public RequestsByStatus getAllValidatorRequests(String appId, boolean allRequests, String userId)
      throws FormsOnlineDatabaseException {
    List<String> formIds = getAvailableFormIdsAsReceiver(appId, userId);
    List<FormDetail> availableForms = getDAO().getForms(formIds);
    RequestsByStatus requests = new RequestsByStatus();
    for (FormDetail form : availableForms) {
      List<FormInstance> requestsByForm =
          getDAO().getReceivedRequests(form.getPK(), allRequests, userId);
      for (FormInstance request : requestsByForm) {
        request.setForm(form);
        requests.add(request);
      }
    }
    return requests;
  }

  public List<String> getAvailableFormIdsAsReceiver(String appId, String userId)
      throws FormsOnlineDatabaseException {
    String[] userGroupIds = organizationController.getAllGroupIdsOfUser(userId);
    return getDAO().getAvailableFormIdsAsReceiver(appId, userId, userGroupIds);
  }

  public FormInstance loadRequest(RequestPK pk, String userId)
      throws FormsOnlineDatabaseException, PublicationTemplateException, FormException {

    FormInstance request = getDAO().getRequest(pk);

    // recuperation de l'objet et du nom du formulaire
    FormDetail form = loadForm(request.getFormPK());
    request.setForm(form);
    String xmlFormName = form.getXmlFormName();
    String xmlFormShortName =
        xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName.indexOf('.'));

    // creation du PublicationTemplate
    getPublicationTemplateManager()
        .addDynamicPublicationTemplate(pk.getInstanceId() + ":" + xmlFormShortName, xmlFormName);
    PublicationTemplateImpl pubTemplate =
        (PublicationTemplateImpl) getPublicationTemplateManager().getPublicationTemplate(
            pk.getInstanceId() + ":" + xmlFormShortName, xmlFormName);

    // Retrieve Form and DataRecord
    Form formView = pubTemplate.getViewForm();
    RecordSet recordSet = pubTemplate.getRecordSet();
    DataRecord data = recordSet.getRecord(pk.getId());
    formView.setData(data);
    request.setFormWithData(formView);

    // Check FormsOnline request states in order to display or hide comment
    if (request.isCanBeValidated() && isValidator(form.getPK(), userId)) {
      // mise a jour du statut de l'instance
      if (request.getState() == FormInstance.STATE_UNREAD) {
        request.setState(FormInstance.STATE_READ);
        getDAO().updateRequest(request);
      }
      request.setValidationEnabled(true);
    }

    return request;
  }

  public void saveRequest(FormPK pk, String userId, List<FileItem> items)
      throws FormsOnlineDatabaseException, PublicationTemplateException, FormException {
    FormInstance request = new FormInstance();
    request.setCreatorId(userId);
    request.setFormId(Integer.parseInt(pk.getId()));
    request.setInstanceId(pk.getInstanceId());
    request.setState(FormInstance.STATE_UNREAD);
    request = getDAO().createInstance(request);

    FormDetail formDetail = getDAO().getForm(pk);
    request.setForm(formDetail);

    // Mise a jour du formulaire pour indiquer qu'il a ete utilise
    if (!formDetail.isAlreadyUsed()) {
      formDetail.setAlreadyUsed(true);
      getDAO().updateForm(formDetail);
    }

    String xmlFormName = formDetail.getXmlFormName();
    String xmlFormShortName =
        xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName.indexOf('.'));

    // Retrieve data form (with DataRecord object)
    PublicationTemplate pub = getPublicationTemplateManager().getPublicationTemplate(
        pk.getInstanceId() + ":" + xmlFormShortName);
    RecordSet set = pub.getRecordSet();
    Form form = pub.getUpdateForm();
    DataRecord data = set.getEmptyRecord();
    data.setId(String.valueOf(request.getId()));

    // Save data form
    PagesContext aContext = new PagesContext("dummy", "0",
        UserDetail.getById(userId).getUserPreferences().getLanguage(), false, pk.getInstanceId(),
        userId);
    aContext.setObjectId(String.valueOf(request.getId()));
    form.update(items, data, aContext);
    set.save(data);

    // Notify receivers
    notifyReceivers(request);
  }

  public void setValidationStatus(RequestPK pk, String userId, String decision, String comments)
      throws FormsOnlineDatabaseException {
    FormInstance request = getDAO().getRequest(pk);
    FormDetail form = getDAO().getForm(new FormPK(request.getFormId(), pk.getInstanceId()));
    request.setForm(form);

    // update state
    if ("validate".equals(decision)) {
      request.setState(FormInstance.STATE_VALIDATED);
    } else {
      request.setState(FormInstance.STATE_REFUSED);
    }

    // validation infos
    request.setValidationDate(new Date());
    request.setValidatorId(userId);
    request.setComments(comments);

    // save modifications
    getDAO().updateRequest(request);

    // notify sender and all validators
    notifyValidation(request);
  }

  private void notifyValidation(FormInstance request) throws FormsOnlineDatabaseException {
    NotifAction action = NotifAction.REFUSE;
    if (request.getState() == FormInstance.STATE_VALIDATED) {
      action = NotifAction.VALIDATE;
    }

    // notify sender
    UserNotificationHelper.buildAndSend(
        new FormsOnlineValidationRequestUserNotification(request, action));

    // notify all validators
    List<String> userIds = getAllReceivers(request.getForm().getPK());
    UserNotificationHelper
        .buildAndSend(new FormsOnlineProcessedRequestUserNotification(request, action, userIds));
  }

  public void deleteRequest(RequestPK pk)
      throws FormsOnlineDatabaseException, FormException, PublicationTemplateException {
    // delete form data
    FormInstance instance = getDAO().getRequest(pk);
    FormPK formPK = new FormPK(instance.getFormId(), pk.getInstanceId());
    FormDetail form = getDAO().getForm(formPK);
    String xmlFormName = form.getXmlFormName();
    String xmlFormShortName =
        xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName.indexOf('.'));
    PublicationTemplate pubTemplate = getPublicationTemplateManager().getPublicationTemplate(
        pk.getInstanceId() + ":" + xmlFormShortName);
    RecordSet set = pubTemplate.getRecordSet();
    DataRecord data = set.getRecord(pk.getId());
    set.delete(data);

    // delete instance metadata
    getDAO().deleteRequest(pk);
  }

  public void archiveRequest(RequestPK pk) throws FormsOnlineDatabaseException {
    FormInstance request = getDAO().getRequest(pk);
    request.setState(FormInstance.STATE_ARCHIVED);
    getDAO().updateRequest(request);
  }

  private void notifyReceivers(FormInstance request)
      throws FormsOnlineDatabaseException {
    List<String> userIds = getAllReceivers(request.getForm().getPK());

    UserNotificationHelper
        .buildAndSend(new FormsOnlinePendingValidationRequestUserNotification(request, userIds));
  }

  private List<String> getAllReceivers(FormPK pk) throws FormsOnlineDatabaseException {
    List<String> userIds = getDAO().getReceiversAsUsers(pk);
    List<String> groupIds = getDAO().getReceiversAsGroups(pk);
    for (String groupId : groupIds) {
      List<UserDetail> users = (List<UserDetail>) Group.getById(groupId).getAllUsers();
      for (UserDetail user : users) {
        userIds.add(user.getId());
      }
    }
    return userIds;
  }

  private PublicationTemplateManager getPublicationTemplateManager() {
    return PublicationTemplateManager.getInstance();
  }

  private NotificationSender getNotificationSender(String appId) {
    return new NotificationSender(appId);
  }

  private FormsOnlineDAO getDAO() {
    return new FormsOnlineDAOJdbc();
  }

  @Override
  public FormInstance getContentById(final String contentId) {
    return null;
  }

  @Override
  public ResourceLocator getComponentSettings() {
    return null;
  }

  @Override
  public ResourceLocator getComponentMessages(final String language) {
    return null;
  }

  /**
   * Is this service related to the specified component instance. The service is related to the
   * specified instance if it is a service defined by the application from which the instance
   * was spawned.
   * @param instanceId the unique instance identifier of the component.
   * @return true if the instance is spawn from the application to which the service is related.
   * False otherwise.
   */
  @Override
  public boolean isRelatedTo(final String instanceId) {
    return instanceId.startsWith("formsOnline");
  }

}