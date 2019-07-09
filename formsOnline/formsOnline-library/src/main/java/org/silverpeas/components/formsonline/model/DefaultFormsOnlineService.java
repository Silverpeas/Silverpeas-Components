/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.components.formsonline.model;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.components.formsonline.notification
    .FormsOnlinePendingValidationRequestUserNotification;
import org.silverpeas.components.formsonline.notification
    .FormsOnlineProcessedRequestUserNotification;
import org.silverpeas.components.formsonline.notification
    .FormsOnlineValidationRequestUserNotification;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateImpl;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.persistence.datasource.repository.PaginationCriterion;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Singleton
public class DefaultFormsOnlineService implements FormsOnlineService {

  @Inject
  private OrganizationController organizationController;

  @Override
  public List<FormDetail> getAllForms(final String appId, final String userId,
      final boolean withSendInfo) throws FormsOnlineDatabaseException {
    List<FormDetail> forms = getDAO().findAllForms(appId);
    Map<Integer, Integer> numbersOfRequests = getDAO().getNumberOfRequestsByForm(appId);
    for (FormDetail form : forms) {
      Integer numberOfRequests = numbersOfRequests.get(form.getId());
      if (numberOfRequests != null) {
        form.setNbRequests(numberOfRequests);
      }
      if (withSendInfo) {
        form.setSendable(isSender(form.getPK(), userId));
      }
    }
    return forms;
  }

  private boolean isSender(FormPK pk, String userId) throws FormsOnlineDatabaseException {
    return isInLists(userId, getSendersAsUsers(pk), getSendersAsGroups(pk));
  }

  private List<User> getSendersAsUsers(FormPK pk) throws FormsOnlineDatabaseException {
    List<String> userIds = getDAO().getSendersAsUsers(pk);
    User[] details = organizationController.getUserDetails(userIds.toArray(new String[0]));
    return CollectionUtil.asList(details);
  }

  private List<Group> getSendersAsGroups(FormPK pk) throws FormsOnlineDatabaseException {
    List<String> groupIds = getDAO().getSendersAsGroups(pk);
    Group[] groups = organizationController.getGroups(groupIds.toArray(new String[0]));
    return CollectionUtil.asList(groups);
  }

  private List<User> getReceiversAsUsers(FormPK pk) throws FormsOnlineDatabaseException {
    List<String> userIds = getDAO().getReceiversAsUsers(pk);
    User[] details = organizationController.getUserDetails(userIds.toArray(new String[0]));
    return CollectionUtil.asList(details);
  }

  private List<Group> getReceiversAsGroups(FormPK pk) throws FormsOnlineDatabaseException {
    List<String> groupIds = getDAO().getReceiversAsGroups(pk);
    Group[] groups = organizationController.getGroups(groupIds.toArray(new String[0]));
    return CollectionUtil.asList(groups);
  }

  private boolean isValidator(FormPK pk, String userId) throws FormsOnlineDatabaseException {
    return isInLists(userId, getReceiversAsUsers(pk), getReceiversAsGroups(pk));
  }

  private boolean isInLists(String userId, List<? extends User> users, List<Group> groups) {
    boolean inList = isInList(userId, users);
    if (!inList) {
      for (Group group : groups) {
        inList = group != null && isInList(userId, group.getAllUsers());
        if (inList) {
          return true;
        }
      }
    }
    return inList;
  }

  private boolean isInList(String userId, List<? extends User> users) {
    for (User user : users) {
      if (user != null && user.getId().equals(userId)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public FormDetail loadForm(FormPK pk) throws FormsOnlineDatabaseException {
    FormDetail form = getDAO().getForm(pk);
    if (form != null) {
      form.setSendersAsUsers(getSendersAsUsers(pk));
      form.setSendersAsGroups(getSendersAsGroups(pk));
      form.setReceiversAsUsers(getReceiversAsUsers(pk));
      form.setReceiversAsGroups(getReceiversAsGroups(pk));
    }
    return form;
  }

  @Override
  @Transactional
  public FormDetail storeForm(FormDetail form, String[] senderUserIds, String[] senderGroupIds,
      String[] receiverUserIds, String[] receiverGroupIds) throws FormsOnlineDatabaseException {
    FormDetail theForm = form;
    if (form.getId() == -1) {
      theForm = getDAO().createForm(form);
    } else {
      getDAO().updateForm(theForm);
    }
    getDAO().updateSenders(theForm.getPK(), senderUserIds, senderGroupIds);
    getDAO().updateReceivers(theForm.getPK(), receiverUserIds, receiverGroupIds);
    theForm.setSendersAsUsers(getSendersAsUsers(theForm.getPK()));
    theForm.setSendersAsGroups(getSendersAsGroups(theForm.getPK()));
    theForm.setReceiversAsUsers(getReceiversAsUsers(theForm.getPK()));
    theForm.setReceiversAsGroups(getReceiversAsGroups(theForm.getPK()));

    index(theForm);

    return theForm;
  }

  @Override
  @Transactional
  public boolean deleteForm(FormPK pk) throws FormsOnlineDatabaseException {
    // delete all associated requests
    final SilverpeasList<FormInstance> requests = getDAO().getAllRequests(pk);
    boolean reallyDeleteForm = true;
    for (FormInstance request : requests) {
      try {
        FormsOnlineService.get().deleteRequest(request.getPK());
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(
            "Unable to delete request #" + request.getId() + " in component " +
                request.getComponentInstanceId(), e);
        reallyDeleteForm = false;
      }
    }
    if (reallyDeleteForm) {
      // delete form itself
      getDAO().deleteForm(pk);
      removeIndex(pk);
    }
    return reallyDeleteForm;
  }

  @Override
  @Transactional
  public void publishForm(FormPK pk) throws FormsOnlineDatabaseException {
    FormDetail form = getDAO().getForm(pk);
    form.setState(FormDetail.STATE_PUBLISHED);
    getDAO().updateForm(form);
    index(form);
  }

  @Override
  @Transactional
  public void unpublishForm(FormPK pk) throws FormsOnlineDatabaseException {
    FormDetail form = getDAO().getForm(pk);
    form.setState(FormDetail.STATE_UNPUBLISHED);
    getDAO().updateForm(form);
    index(form);
  }

  @Override
  public List<FormDetail> getAvailableFormsToSend(String appId, String userId)
      throws FormsOnlineDatabaseException {
    String[] userGroupIds = organizationController.getAllGroupIdsOfUser(userId);
    return getDAO().getUserAvailableForms(appId, userId, userGroupIds);
  }

  @Override
  public RequestsByStatus getAllUserRequests(String appId, String userId,
      final PaginationPage paginationPage)
      throws FormsOnlineDatabaseException {
    RequestsByStatus requests = new RequestsByStatus(paginationPage);
    List<FormDetail> forms = getAllForms(appId, userId, false);
    for (FormDetail form : forms) {
      for (Pair<List<Integer>, BiConsumer<RequestsByStatus, SilverpeasList<FormInstance>>>
          mergingRuleByStates : RequestsByStatus.MERGING_RULES_BY_STATES) {
        final List<Integer> states = mergingRuleByStates.getLeft();
        final BiConsumer<RequestsByStatus, SilverpeasList<FormInstance>> merge =
            mergingRuleByStates.getRight();
        final PaginationCriterion paginationCriterion =
            paginationPage != null ? paginationPage.asCriterion() : null;
        final SilverpeasList<FormInstance> result =
            getDAO().getSentFormInstances(form.getPK(), userId, states, paginationCriterion);
        merge.accept(requests, result.stream()
                                     .map(l -> {
                                       l.setForm(form);
                                       return l;
                                     })
                                     .collect(SilverpeasList.collector(result)));
      }
    }
    return requests;
  }

  @Override
  public RequestsByStatus getValidatorRequests(RequestsFilter filter, String userId,
      final PaginationPage paginationPage) throws FormsOnlineDatabaseException {
    final List<String> formIds = getAvailableFormIdsAsReceiver(filter.getComponentId(), userId);

    // limit requests to specified forms
    if (!filter.getFormIds().isEmpty()) {
      formIds.retainAll(filter.getFormIds());
    }
    final List<FormDetail> availableForms = getDAO().getForms(formIds);
    RequestsByStatus requests = new RequestsByStatus(paginationPage);
    for (FormDetail form : availableForms) {
      for (Pair<List<Integer>, BiConsumer<RequestsByStatus, SilverpeasList<FormInstance>>>
          mergingRuleByStates : RequestsByStatus.MERGING_RULES_BY_STATES) {
        final List<Integer> states = mergingRuleByStates.getLeft();
        final BiConsumer<RequestsByStatus, SilverpeasList<FormInstance>> merge =
            mergingRuleByStates.getRight();
        final PaginationCriterion paginationCriterion =
            paginationPage != null ? paginationPage.asCriterion() : null;
        final SilverpeasList<FormInstance> result = getDAO()
            .getReceivedRequests(form.getPK(), filter.isAllRequests(), userId, states,
                paginationCriterion);
        merge.accept(requests, result.stream()
                                     .map(l -> {
                                       l.setForm(form);
                                       return l;
                                     })
                                     .collect(SilverpeasList.collector(result)));
      }
    }
    return requests;
  }

  @Override
  public List<String> getAvailableFormIdsAsReceiver(String appId, String userId)
      throws FormsOnlineDatabaseException {
    String[] userGroupIds = organizationController.getAllGroupIdsOfUser(userId);
    return getDAO().getAvailableFormIdsAsReceiver(appId, userId, userGroupIds);
  }

  @Override
  public FormInstance loadRequest(RequestPK pk, String userId)
      throws FormsOnlineDatabaseException, PublicationTemplateException, FormException {

    FormInstance request = getDAO().getRequest(pk);

    // recuperation de l'objet et du nom du formulaire
    FormDetail form = loadForm(request.getFormPK());
    request.setForm(form);
    String xmlFormName = form.getXmlFormName();
    String xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName.indexOf('.'));

    // creation du PublicationTemplate
    getPublicationTemplateManager()
        .addDynamicPublicationTemplate(pk.getInstanceId() + ":" + xmlFormShortName, xmlFormName);
    PublicationTemplateImpl pubTemplate = (PublicationTemplateImpl) getPublicationTemplateManager()
        .getPublicationTemplate(pk.getInstanceId() + ":" + xmlFormShortName, xmlFormName);

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

  @Override
  @Transactional
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

    String xmlFormName = formDetail.getXmlFormName();
    String xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName.indexOf('.'));

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

  @Override
  @Transactional
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

  @Override
  @Transactional
  public void deleteRequest(RequestPK pk)
      throws FormsOnlineDatabaseException, FormException, PublicationTemplateException {
    // delete form data
    FormInstance instance = getDAO().getRequest(pk);
    FormPK formPK = new FormPK(instance.getFormId(), pk.getInstanceId());
    FormDetail form = getDAO().getForm(formPK);
    String xmlFormName = form.getXmlFormName();
    String xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName.indexOf('.'));
    PublicationTemplate pubTemplate = getPublicationTemplateManager().getPublicationTemplate(
        pk.getInstanceId() + ":" + xmlFormShortName);
    RecordSet set = pubTemplate.getRecordSet();
    DataRecord data = set.getRecord(pk.getId());
    set.delete(data.getId());

    // delete instance metadata
    getDAO().deleteRequest(pk);
  }

  @Override
  @Transactional
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
      Group group = Group.getById(groupId);
      if (group != null) {
        List<User> users = group.getAllUsers();
        for (User user : users) {
          userIds.add(user.getId());
        }
      }
    }
    return userIds;
  }

  private PublicationTemplateManager getPublicationTemplateManager() {
    return PublicationTemplateManager.getInstance();
  }

  private FormsOnlineDAO getDAO() {
    return new FormsOnlineDAOJdbc();
  }

  @Override
  public FormInstance getContentById(final String contentId) {
    return null;
  }

  @Override
  public SettingBundle getComponentSettings() {
    return null;
  }

  @Override
  public LocalizationBundle getComponentMessages(final String language) {
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

  private void index(FormDetail form) {
    IndexEntryKey key = getIndexEntryKey(form.getPK());
    if (form.isPublished()) {
      FullIndexEntry fie = new FullIndexEntry(key);
      fie.setTitle(form.getTitle());
      fie.setPreview(form.getDescription());
      fie.setCreationDate(form.getCreationDate());
      fie.setCreationUser(form.getCreatorId());
      IndexEngineProxy.addIndexEntry(fie);
    } else {
      IndexEngineProxy.removeIndexEntry(key);
    }
  }

  private void removeIndex(FormPK pk) {
    IndexEngineProxy.removeIndexEntry(getIndexEntryKey(pk));
  }

  private IndexEntryKey getIndexEntryKey(FormPK pk) {
   return new IndexEntryKey(pk.getInstanceId(), "FormOnline", pk.getId());
  }

  public void index(String componentId) {
    try {
      List<FormDetail> forms = getAllForms(componentId, "useless", false);
      for (FormDetail form : forms) {
        index(form);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

}