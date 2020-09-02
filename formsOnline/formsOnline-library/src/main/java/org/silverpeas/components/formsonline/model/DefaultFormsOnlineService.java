/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
import org.apache.ecs.html.BR;
import org.apache.ecs.xhtml.div;
import org.silverpeas.components.formsonline.model.RequestsByStatus.MergeRuleByStates;
import org.silverpeas.components.formsonline.notification.FormsOnlineCanceledRequestUserNotification;
import org.silverpeas.components.formsonline.notification.FormsOnlinePendingValidationRequestUserNotification;
import org.silverpeas.components.formsonline.notification.FormsOnlineProcessedRequestUserNotification;
import org.silverpeas.components.formsonline.notification.FormsOnlineValidationRequestUserNotification;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.contribution.ContributionStatus;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentMailAttachedFile;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.form.field.FileField;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateImpl;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.mail.MailAddress;
import org.silverpeas.core.mail.MailSending;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.security.authorization.ForbiddenRuntimeException;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.MemoizedSupplier;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMultipart;
import javax.transaction.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;
import static org.silverpeas.components.formsonline.model.FormDetail.RECEIVERS_TYPE_FINAL;
import static org.silverpeas.components.formsonline.model.FormDetail.RECEIVERS_TYPE_INTERMEDIATE;
import static org.silverpeas.components.formsonline.model.FormInstanceValidationType.HIERARCHICAL;
import static org.silverpeas.components.formsonline.model.RequestValidationCriteria.withValidatorId;
import static org.silverpeas.components.formsonline.model.RequestsByStatus.MERGING_RULES_BY_STATES;
import static org.silverpeas.core.mail.MailContent.getHtmlBodyPartFromHtmlContent;
import static org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper.buildAndSend;
import static org.silverpeas.core.util.CollectionUtil.isEmpty;

@Singleton
public class DefaultFormsOnlineService implements FormsOnlineService {

  private static final String IN_COMPONENT_MSG_PART = " in component ";

  @Inject
  private OrganizationController organizationController;

  @Override
  public List<FormDetail> getAllForms(final String appId, final String userId,
      final boolean withSendInfo) throws FormsOnlineException {
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

  private boolean isSender(FormPK pk, String userId) throws FormsOnlineException {
    return isInLists(userId, getSendersAsUsers(pk), getSendersAsGroups(pk));
  }

  private List<User> getSendersAsUsers(FormPK pk) throws FormsOnlineException {
    List<String> userIds = getDAO().getSendersAsUsers(pk);
    User[] details = organizationController.getUserDetails(userIds.toArray(new String[0]));
    return CollectionUtil.asList(details);
  }

  private List<Group> getSendersAsGroups(FormPK pk) throws FormsOnlineException {
    List<String> groupIds = getDAO().getSendersAsGroups(pk);
    Group[] groups = organizationController.getGroups(groupIds.toArray(new String[0]));
    return CollectionUtil.asList(groups);
  }

  private List<User> getReceiversAsUsers(FormPK pk, String rightType) throws FormsOnlineException {
    List<String> userIds = getDAO().getReceiversAsUsers(pk, rightType);
    User[] details = organizationController.getUserDetails(userIds.toArray(new String[0]));
    return CollectionUtil.asList(details);
  }

  private List<Group> getReceiversAsGroups(FormPK pk, String rightType) throws FormsOnlineException {
    List<String> groupIds = getDAO().getReceiversAsGroups(pk, rightType);
    Group[] groups = organizationController.getGroups(groupIds.toArray(new String[0]));
    return CollectionUtil.asList(groups);
  }

  private boolean isValidator(FormPK pk, String userId, String rightType)
      throws FormsOnlineException {
    return isInLists(userId, getReceiversAsUsers(pk, rightType),
        getReceiversAsGroups(pk, rightType));
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
  public FormDetail loadForm(FormPK pk) throws FormsOnlineException {
    FormDetail form = getDAO().getForm(pk);
    setSendersAndReceivers(form);
    return form;
  }

  @Override
  @Transactional
  public FormDetail storeForm(FormDetail form, String[] senderUserIds, String[] senderGroupIds,
      String[] intermediateReceiverUserIds, String[] intermediateReceiverGroupIds,
      String[] receiverUserIds, String[] receiverGroupIds) throws FormsOnlineException {
    FormDetail theForm = form;
    if (form.getId() == -1) {
      theForm = getDAO().createForm(form);
    } else {
      getDAO().updateForm(theForm);
    }
    getDAO().updateSenders(theForm.getPK(), senderUserIds, senderGroupIds);
    getDAO()
        .updateReceivers(theForm.getPK(), intermediateReceiverUserIds, intermediateReceiverGroupIds,
            RECEIVERS_TYPE_INTERMEDIATE);
    getDAO().updateReceivers(theForm.getPK(), receiverUserIds, receiverGroupIds, RECEIVERS_TYPE_FINAL);
    setSendersAndReceivers(theForm);

    index(theForm);

    return theForm;
  }

  @Override
  @Transactional
  public boolean deleteForm(FormPK pk) throws FormsOnlineException {
    // delete all associated requests
    final SilverpeasList<FormInstance> requests = getDAO().getAllRequests(pk);
    boolean reallyDeleteForm = true;
    for (FormInstance request : requests) {
      try {
        FormsOnlineService.get().deleteRequest(request.getPK());
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(
            "Unable to delete request #" + request.getId() + IN_COMPONENT_MSG_PART +
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
  public void publishForm(FormPK pk) throws FormsOnlineException {
    FormDetail form = getDAO().getForm(pk);
    form.setState(FormDetail.STATE_PUBLISHED);
    getDAO().updateForm(form);
    index(form);
  }

  @Override
  @Transactional
  public void unpublishForm(FormPK pk) throws FormsOnlineException {
    FormDetail form = getDAO().getForm(pk);
    form.setState(FormDetail.STATE_UNPUBLISHED);
    getDAO().updateForm(form);
    index(form);
  }

  @Override
  public List<FormDetail> getAvailableFormsToSend(Collection<String> appIds, String userId)
      throws FormsOnlineException {
    String[] userGroupIds = organizationController.getAllGroupIdsOfUser(userId);
    return getDAO().getUserAvailableForms(appIds, userId, userGroupIds);
  }

  @Override
  public RequestsByStatus getAllUserRequests(String appId, String userId,
      final PaginationPage paginationPage)
      throws FormsOnlineException {
    RequestsByStatus requests = new RequestsByStatus(paginationPage);
    List<FormDetail> forms = getAllForms(appId, userId, false);
    for (FormDetail form : forms) {
      setSendersAndReceivers(form);
      for (final MergeRuleByStates rule : MERGING_RULES_BY_STATES) {
        final List<Integer> states = rule.getStates();
        final BiConsumer<RequestsByStatus, SilverpeasList<FormInstance>> merge = rule.getMerger();
        final SilverpeasList<FormInstance> result =
            getDAO().getSentFormInstances(form.getPK(), userId, states, paginationPage);
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
  public RequestsByStatus getValidatorRequests(RequestsFilter filter, String validatorId,
      final PaginationPage paginationPage) throws FormsOnlineException {
    final Map<String, Set<FormInstanceValidationType>> possibleValidatorValidationTypesByFormId =
        getValidatorFormIdsWithValidationTypes(filter.getComponentId(), validatorId, filter.getFormIds());
    final List<FormDetail> availableForms = getDAO().getForms(possibleValidatorValidationTypesByFormId.keySet());
    final Map<String, Set<FormInstanceValidationType>> possibleValidationTypesByFormId = getDAO()
        .getPossibleValidationTypesByFormId(possibleValidatorValidationTypesByFormId.keySet());
    final RequestsByStatus requests = new RequestsByStatus(paginationPage);
    final MemoizedSupplier<Set<String>> managedDomainUsersSupplier = new MemoizedSupplier<>(() -> {
      final String userDomainId = User.getById(validatorId).getDomainId();
      final User[] users = OrganizationController.get().getAllUsersInDomain(userDomainId);
      final Set<String> userIds = Stream.of(users).map(User::getId).collect(toSet());
      final HierarchicalValidatorCacheManager hvManager = new HierarchicalValidatorCacheManager();
      hvManager.cacheHierarchicalValidatorsOf(userIds);
      return userIds.stream()
          .map(u -> Pair.of(u, hvManager.getHierarchicalValidatorOf(u)))
          .filter(p -> validatorId.equals(p.getRight()))
          .map(Pair::getLeft)
          .collect(toSet());
    });
    for (final FormDetail form : availableForms) {
      setSendersAndReceivers(form);
      for (final MergeRuleByStates rule : MERGING_RULES_BY_STATES) {
        final List<Integer> states = rule.getStates().stream()
            .filter(s -> filter.getState() < FormInstance.STATE_DRAFT || filter.getState() == s)
            .collect(Collectors.toList());
        if (states.isEmpty()) {
          continue;
        }
        final BiConsumer<RequestsByStatus, SilverpeasList<FormInstance>> merge = rule.getMerger();
        final RequestValidationCriteria validationCriteria;
        if (!filter.isAllRequests()) {
          validationCriteria = withValidatorId(validatorId, managedDomainUsersSupplier);
          final String formId = form.getPK().getId();
          final Set<FormInstanceValidationType> possibleFormValidationTypes =
              possibleValidationTypesByFormId.get(formId);
          final Set<FormInstanceValidationType> possibleValidatorValidationTypes =
              possibleValidatorValidationTypesByFormId.get(formId);
          rule.getValidationCriteriaConfigurer().accept(
              Pair.of(possibleFormValidationTypes, possibleValidatorValidationTypes),
              validationCriteria);
        } else {
          validationCriteria = null;
        }
        final Optional<FormInstanceValidationType> pendingValidationTypeFilter = filter
            .getPendingValidationType();
        final SilverpeasList<FormInstance> result = getDAO().getReceivedRequests(form, states, validationCriteria,
                ofNullable(paginationPage).filter(p -> !pendingValidationTypeFilter.isPresent()).orElse(null));
        Stream<FormInstance> resultStream = result.stream();
        if (pendingValidationTypeFilter.isPresent()) {
          resultStream = resultStream
              .filter(r -> r.getValidations().getValidationOfType(pendingValidationTypeFilter.get()).isPresent());
        }
        resultStream = resultStream.map(l -> {
          l.setForm(form);
          return l;
        });
        merge.accept(requests, resultStream.collect(SilverpeasList.collector(result)));
      }
    }

    return requests;
  }

  @Override
  public Map<String, Set<FormInstanceValidationType>> getValidatorFormIdsWithValidationTypes(
      String appId, String validatorId, final Collection<String> formIds) throws FormsOnlineException {
    final String[] userGroupIds = organizationController.getAllGroupIdsOfUser(validatorId);
    final Map<String, Set<FormInstanceValidationType>> result = getDAO()
        .getValidatorFormIdsWithValidationTypes(appId, validatorId, userGroupIds, formIds);
    // get available form as boss
    final List<FormDetail> forms = getDAO().findAllForms(appId);
    final HierarchicalValidatorCacheManager hvManager = new HierarchicalValidatorCacheManager();
    for (FormDetail form : forms) {
      if (form.isHierarchicalValidation() && (isEmpty(formIds) || formIds.contains(form.getPK().getId()))) {
        final SilverpeasList<FormInstance> requests = getDAO().getAllRequests(form.getPK());
        final Set<String> creatorIds = requests.stream().map(FormInstance::getCreatorId).collect(toSet());
        hvManager.cacheHierarchicalValidatorsOf(creatorIds);
        creatorIds.stream()
            .map(hvManager::getHierarchicalValidatorOf)
            .filter(validatorId::equals)
            .findFirst()
            .ifPresent(b ->
                result.computeIfAbsent(Integer.toString(form.getId()), s -> new TreeSet<>())
                      .add(HIERARCHICAL));
      }
    }
    return result;
  }

  @Override
  public FormInstance loadRequest(RequestPK pk, String userId) throws FormsOnlineException {
    return loadRequest(pk, userId, false);
  }

  @Override
  public FormInstance loadRequest(RequestPK pk, String userId, boolean editionMode)
      throws FormsOnlineException {

    FormInstance request = getDAO().getRequest(pk);

    // recuperation de l'objet et du nom du formulaire
    FormDetail form = loadForm(request.getFormPK());
    request.setForm(form);
    String xmlFormName = form.getXmlFormName();
    String xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName.indexOf('.'));

    // creation du PublicationTemplate
    try {
      getPublicationTemplateManager()
          .addDynamicPublicationTemplate(pk.getInstanceId() + ":" + xmlFormShortName, xmlFormName);
      PublicationTemplateImpl pubTemplate = (PublicationTemplateImpl) getPublicationTemplateManager()
          .getPublicationTemplate(pk.getInstanceId() + ":" + xmlFormShortName, xmlFormName);

      // Retrieve Form and DataRecord
      Form customForm = editionMode ? pubTemplate.getUpdateForm() : pubTemplate.getViewForm();
      RecordSet recordSet = pubTemplate.getRecordSet();
      DataRecord data = recordSet.getRecord(pk.getId());
      customForm.setData(data);
      request.setFormWithData(customForm);
    } catch (PublicationTemplateException | FormException e) {
      throw new FormsOnlineException(
          "Can't load content of request #" + request.getId() + IN_COMPONENT_MSG_PART +
              pk.getInstanceId(), e);
    }

    // Check FormsOnline request states in order to display or hide comment
    setRequestStateAndValidationData(form, request, userId);

    return request;
  }

  private void setRequestStateAndValidationData(final FormDetail form, final FormInstance request,
      final String userId) throws FormsOnlineException {
    if (request.canBeValidated()) {
      // mise a jour du statut de l'instance
      if (request.getState() == FormInstance.STATE_UNREAD) {
        request.setState(FormInstance.STATE_READ);
        getDAO().saveRequestState(request);
      }
      final List<FormInstanceValidation> schema = request.getValidationsSchema();
      for (FormInstanceValidation validation : schema) {
        if (validation.isPendingValidation()) {
          boolean validationEnabled;
          if (validation.getValidationType().isHierarchical()) {
            validationEnabled = request.isHierarchicalValidator(userId);
          } else if (validation.getValidationType().isIntermediate()) {
            validationEnabled = isValidator(form.getPK(), userId, RECEIVERS_TYPE_INTERMEDIATE);
          } else {
            validationEnabled = isValidator(form.getPK(), userId, RECEIVERS_TYPE_FINAL);
          }
          request.setValidationEnabled(validationEnabled);
          break;
        }
      }
    }
  }

  @Override
  @Transactional
  public void saveRequest(FormPK pk, String userId, List<FileItem> items, boolean draft)
      throws FormsOnlineException {

    String requestId = FileUploadUtil.getParameter(items, "Id");
    FormInstance request;
    if (StringUtil.isNotDefined(requestId)) {
      request = createRequest(pk, userId, items, draft);
    } else {
      request = loadRequest(new RequestPK(requestId, pk.getInstanceId()), userId);
      updateRequest(request, items, draft);
    }

    if (!draft) {
      // Notify receivers
      notifyReceivers(request);
    }
  }

  private FormInstance createRequest(FormPK pk, String userId, List<FileItem> items,
      boolean draft) throws FormsOnlineException {
    FormInstance request = new FormInstance();
    request.setCreatorId(userId);
    request.setFormId(Integer.parseInt(pk.getId()));
    request.setInstanceId(pk.getInstanceId());
    if (draft) {
      request.setState(FormInstance.STATE_DRAFT);
    } else {
      request.setState(FormInstance.STATE_UNREAD);
    }
    request = getDAO().saveRequest(request);

    FormDetail formDetail = loadForm(pk);
    request.setForm(formDetail);

    // Retrieve data form (with DataRecord object)
    try {
      PublicationTemplate pub = getPublicationTemplate(request);
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
    } catch (Exception e) {
      throw new FormsOnlineException(
          "Can't create content of request #" + request.getId() + IN_COMPONENT_MSG_PART +
              pk.getInstanceId(), e);
    }

    return request;
  }

  private FormInstance updateRequest(FormInstance request, List<FileItem> items,
      boolean draft) throws FormsOnlineException {

    FormDetail formDetail = request.getForm();

    if (draft) {
      request.setState(FormInstance.STATE_DRAFT);
    } else {
      request.setState(FormInstance.STATE_UNREAD);
    }
    request = getDAO().saveRequest(request);

    request.setForm(formDetail);

    // Retrieve data form (with DataRecord object)
    try {
      PublicationTemplate pub = getPublicationTemplate(request);
      RecordSet set = pub.getRecordSet();
      Form form = pub.getUpdateForm();
      DataRecord data = set.getRecord(request.getId());

      // Save data form
      PagesContext aContext = new PagesContext("dummy", "0",
          UserDetail.getById(request.getCreatorId()).getUserPreferences().getLanguage(), false, request.getComponentInstanceId(), request.getCreatorId());
      aContext.setObjectId(String.valueOf(request.getId()));
      form.update(items, data, aContext);
      set.save(data);
    } catch (Exception e) {
      throw new FormsOnlineException(
          "Can't update content of request #" + request.getId() + IN_COMPONENT_MSG_PART +
              request.getComponentInstanceId(), e);
    }

    return request;
  }

  @Override
  @Transactional
  public void saveNextRequestValidationStep(RequestPK pk, String validatorId, String decision,
      String comment, boolean follower) throws FormsOnlineException {
    final FormInstance request = getDAO().getRequest(pk);
    final FormDetail form = loadForm(new FormPK(request.getFormId(), pk.getInstanceId()));
    request.setForm(form);
    final FormInstanceValidation validation = request.getPendingValidation();
    if ((validation.getValidationType().isHierarchical() && !request.isHierarchicalValidator(validatorId)) ||
        (validation.getValidationType().isIntermediate() && !form.isIntermediateValidator(validatorId)) ||
        (validation.getValidationType().isFinal() && !form.isFinalValidator(validatorId))) {
      throwForbiddenException(validatorId + " can not validate the request " + request.getId());
    }
    // update state
    if ("validate".equals(decision)) {
      if (validation.getValidationType().isFinal()) {
        request.setState(FormInstance.STATE_VALIDATED);
      }
      validation.setStatus(ContributionStatus.VALIDATED);
    } else {
      request.setState(FormInstance.STATE_REFUSED);
      validation.setStatus(ContributionStatus.REFUSED);
    }
    // validation infos
    validation.setDate(Date.from(Instant.now()));
    validation.setValidator(User.getById(validatorId));
    validation.setComment(comment);
    validation.setFollower(follower);
    request.getValidations().add(validation);
    // save modifications
    getDAO().saveRequest(request);
    // notify sender and all validators
    notifyValidation(request);
  }

  private void notifyValidation(FormInstance request) throws FormsOnlineException {
    FormInstanceValidation latestValidation = request.getValidations().getLatestValidation();
    FormInstanceValidation pendingValidation = request.getPendingValidation();

    NotifAction action = NotifAction.VALIDATE;
    if (request.getState() == FormInstance.STATE_REFUSED) {
      action = NotifAction.REFUSE;
    }

    // notify sender
    // TODO custom message
    buildAndSend(new FormsOnlineValidationRequestUserNotification(request, action));

    // notify the request processed
    final List<String> nextValidatorIds = new ArrayList<>();
    if (latestValidation.getValidationType().isFinal() ||
        pendingValidation.getValidationType().isFinal()) {
      nextValidatorIds.addAll(getAllReceivers(request.getForm().getPK(), RECEIVERS_TYPE_FINAL));
    } else if (pendingValidation.getValidationType().isIntermediate()) {
      nextValidatorIds.addAll(getAllReceivers(request.getForm().getPK(), RECEIVERS_TYPE_INTERMEDIATE));
    }
    buildAndSend(new FormsOnlineProcessedRequestUserNotification(request, action, nextValidatorIds));
  }

  @Override
  public void cancelRequest(final RequestPK pk) throws FormsOnlineException {
    final FormInstance request = getDAO().getRequest(pk);
    final FormDetail form = loadForm(new FormPK(request.getFormId(), pk.getInstanceId()));
    request.setForm(form);
    final User currentRequester = User.getCurrentRequester();
    if (!request.canBeCanceledBy(currentRequester)) {
      throwForbiddenException(currentRequester.getId() + " can not cancel the request " + request.getId());
    }
    request.setState(FormInstance.STATE_CANCELED);
    getDAO().saveRequestState(request);
    // notify sender and all validators
    notifyCancellation(request);
  }

  private void notifyCancellation(final FormInstance request) {
    buildAndSend(new FormsOnlineCanceledRequestUserNotification(request));
  }

  @Override
  @Transactional
  public void deleteRequest(RequestPK pk) throws FormsOnlineException {
    try {
      // delete form data
      final FormInstance request = getDAO().getRequest(pk);
      final FormDetail form = loadForm(new FormPK(request.getFormId(), pk.getInstanceId()));
      request.setForm(form);
      final User currentRequester = User.getCurrentRequester();
      if (!request.canBeDeletedBy(currentRequester)) {
        throwForbiddenException(currentRequester.getId() + " can not delete the request " + request.getId());
      }
      PublicationTemplate pubTemplate = getPublicationTemplate(request);
      RecordSet set = pubTemplate.getRecordSet();
      DataRecord data = set.getRecord(pk.getId());
      set.delete(data.getId());
    } catch (Exception e) {
      throw new FormsOnlineException("Can't delete request #"+pk.getId()+ IN_COMPONENT_MSG_PART +
          pk.getInstanceId(), e);
    }

    // delete instance metadata
    getDAO().deleteRequest(pk);
  }

  @Override
  @Transactional
  public void archiveRequest(RequestPK pk) throws FormsOnlineException {
    final FormInstance request = getDAO().getRequest(pk);
    final FormDetail form = loadForm(new FormPK(request.getFormId(), pk.getInstanceId()));
    request.setForm(form);
    final User currentRequester = User.getCurrentRequester();
    if (!request.canBeArchivedBy(currentRequester)) {
      throwForbiddenException(currentRequester.getId() + " can not archive the request " + request.getId());
    }
    request.setState(FormInstance.STATE_ARCHIVED);
    getDAO().saveRequestState(request);
  }

  private void notifyReceivers(FormInstance request) throws FormsOnlineException {
    final FormInstanceValidations validations = request.getValidations();
    final FormDetail form = request.getForm();
    if (validations.isEmpty()) {
      sendRequestByEmail(request);
    }
    List<String> userIds = new ArrayList<>();
    final FormInstanceValidation pendingValidation = request.getPendingValidation();
    if (pendingValidation != null) {
      if (pendingValidation.getValidationType().isHierarchical()) {
        // notify boss
        final String bossId = form.getHierarchicalValidatorOfCurrentUser();
        userIds.add(bossId);
      } else if (pendingValidation.getValidationType().isIntermediate()) {
        userIds = getAllReceivers(request.getForm().getPK(), RECEIVERS_TYPE_INTERMEDIATE);
      } else {
        userIds = getAllReceivers(request.getForm().getPK(), RECEIVERS_TYPE_FINAL);
      }
    }

    buildAndSend(new FormsOnlinePendingValidationRequestUserNotification(request, userIds));
  }

  private List<String> getAllReceivers(FormPK pk, String rightType) throws FormsOnlineException {
    List<String> userIds = getDAO().getReceiversAsUsers(pk, rightType);
    List<String> groupIds = getDAO().getReceiversAsGroups(pk, rightType);
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

  private PublicationTemplate getPublicationTemplate(FormInstance request)
      throws FormsOnlineException, PublicationTemplateException {
    FormPK formPK = new FormPK(request.getFormId(), request.getPK().getInstanceId());
    FormDetail form = getDAO().getForm(formPK);
    String xmlFormName = form.getXmlFormName();
    String xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName.indexOf('.'));
    return getPublicationTemplateManager().getPublicationTemplate(
        request.getPK().getInstanceId() + ":" + xmlFormShortName);
  }

  private void sendRequestByEmail(FormInstance request) throws FormsOnlineException {
    final FormDetail form = request.getForm();
    final Optional<String> requestExchangeReceiver = form.getRequestExchangeReceiver();
    if (requestExchangeReceiver.isPresent()) {
      final Pair<String, List<SimpleDocument>> contents = prepareMailContents(request, form);
      final String email = requestExchangeReceiver.get();
      try {
        final Multipart multipart = new MimeMultipart();
        // First HTML content
        multipart.addBodyPart(getHtmlBodyPartFromHtmlContent(contents.getLeft()));
        // Finally explicit attached files
        attachFilesToMail(multipart, contents.getRight());
        // Sending
        MailSending
            .from(MailAddress.eMail(User.getById(request.getCreatorId()).geteMail()))
            .to(MailAddress.eMail(email))
            .withSubject(form.getTitle())
            .withContent(multipart)
            .setReplyToRequired()
            .send();
      } catch (Exception e) {
        throw new FormsOnlineException("Can't send request #" + request.getPK().getId() + " to " + email, e);
      }
      if (form.isDeleteAfterRequestExchange()) {
        deleteRequest(request.getPK());
      }
    }
  }

  private Pair<String, List<SimpleDocument>> prepareMailContents(final FormInstance request,
      final FormDetail form) throws FormsOnlineException {
    try {
      final div content = new div();
      final List<SimpleDocument> docs = new ArrayList<>();
      final PublicationTemplate template = getPublicationTemplate(request);
      final RecordSet recordSet = template.getRecordSet();
      final FieldTemplate[] fields = template.getRecordTemplate().getFieldTemplates();
      final DataRecord dataRecord = recordSet.getRecord(request.getId());
      final Map<String, String> values = dataRecord.getValues(I18NHelper.defaultLanguage);
      for (final FieldTemplate field : fields) {
        final String value = values.get(field.getFieldName());
        content.addElement(field.getLabel(I18NHelper.defaultLanguage));
        content.addElement(" : ");
        content.addElement(value);
        content.addElement(new BR());
        if (StringUtil.isDefined(value) && field.getTypeName().equals(FileField.TYPE)) {
          final SimpleDocument doc = AttachmentServiceProvider.getAttachmentService()
              .searchDocumentById(
                  new SimpleDocumentPK(dataRecord.getField(field.getFieldName()).getValue(),
                      request.getComponentInstanceId()), null);
          if (doc != null) {
            docs.add(doc);
          }
        }
      }
      return Pair.of(content.toString(), docs);
    } catch (Exception e) {
      throw new FormsOnlineException("Can't load form '" + form.getXmlFormName() + "'", e);
    }
  }

  private void attachFilesToMail(Multipart mp, List<SimpleDocument> listAttachedFiles)
      throws MessagingException {
    for (SimpleDocument attachment : listAttachedFiles) {
      mp.addBodyPart(new SimpleDocumentMailAttachedFile(attachment).toBodyPart());
    }
  }

  private void throwForbiddenException(String method) {
    throw new ForbiddenRuntimeException(
        "User is not allowed to do the following operation: " + method);
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

  private void setSendersAndReceivers(FormDetail form) throws FormsOnlineException {
    if (form != null) {
      FormPK pk = form.getPK();
      form.setSendersAsUsers(getSendersAsUsers(pk));
      form.setSendersAsGroups(getSendersAsGroups(pk));
      form.setIntermediateReceiversAsUsers(getReceiversAsUsers(pk, RECEIVERS_TYPE_INTERMEDIATE));
      form.setIntermediateReceiversAsGroups(getReceiversAsGroups(pk, RECEIVERS_TYPE_INTERMEDIATE));
      form.setReceiversAsUsers(getReceiversAsUsers(pk, RECEIVERS_TYPE_FINAL));
      form.setReceiversAsGroups(getReceiversAsGroups(pk, RECEIVERS_TYPE_FINAL));
    }
  }

  /**
   * Permits to manage a cache in order to increase performances.
   */
  static class HierarchicalValidatorCacheManager {

    private final Map<String, String> cache = new HashMap<>();

    /**
     * Caches the hierarchical validators of users represented by given ids.
     * @param userIds set of string user ids.
     */
    void cacheHierarchicalValidatorsOf(final Set<String> userIds) {
      userIds.forEach(this::getHierarchicalValidatorOf);
    }

    /**
     * Gets from cached data the validator of given users.
     * <p>
     * If no data has been cached for the user, the data are retrieved.
     * </p>
     * @param userId a string user id.
     * @return the hierarchical validator of the user represented by the given id.
     */
    String getHierarchicalValidatorOf(final String userId) {
      return cache.computeIfAbsent(userId, i ->
          UserFull.getById(i).getValue("boss"));
    }
  }
}