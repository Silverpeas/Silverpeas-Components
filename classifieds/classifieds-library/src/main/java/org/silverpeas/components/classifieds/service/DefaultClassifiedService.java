/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.classifieds.service;

import org.apache.commons.io.FilenameUtils;
import org.silverpeas.components.classifieds.dao.ClassifiedsDAO;
import org.silverpeas.components.classifieds.model.ClassifiedDetail;
import org.silverpeas.components.classifieds.model.ClassifiedsRuntimeException;
import org.silverpeas.components.classifieds.model.Subscribe;
import org.silverpeas.components.classifieds.notification.ClassifiedSubscriptionUserNotification;
import org.silverpeas.components.classifieds.notification.ClassifiedSupervisorUserNotification;
import org.silverpeas.components.classifieds.notification.ClassifiedValidationUserNotification;
import org.silverpeas.core.NotFoundException;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.index.search.SearchEngineProvider;
import org.silverpeas.core.index.search.model.MatchingIndexEntry;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.silverpeas.core.SilverpeasExceptionMessages.*;

/**
 * Services provided by the Classified Silverpeas component.
 */
@Service
@Named("classifiedsService")
public class DefaultClassifiedService implements ClassifiedService {

  private static final String MESSAGES_PATH =
      "org.silverpeas.classifieds.multilang.classifiedsBundle";
  private static final String SETTINGS_PATH =
      "org.silverpeas.classifieds.settings.classifiedsSettings";
  private static final SettingBundle settings = ResourceLocator.getSettingBundle(SETTINGS_PATH);
  public static final String CLASSIFIED = "classified";
  public static final String CLASSIFIEDS_IN_APPLICATION = "classifieds in application";
  public static final String CLASSIFIED_TYPE = "Classified";
  private static final String NO_SUCH_CLASSIFIED = "No such classified ";

  @Inject
  private OrganizationController organizationController;

  @Override
  public Optional<ClassifiedDetail> getContributionById(ContributionIdentifier classifiedId) {
    try (Connection con = openConnection()) {
      return Optional.ofNullable(ClassifiedsDAO.getClassified(con, classifiedId.getLocalId()));
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException(failureOnGetting(CLASSIFIED, classifiedId), e);
    }
  }

  @Override
  public SettingBundle getComponentSettings() {
    return settings;
  }

  @Override
  public LocalizationBundle getComponentMessages(String language) {
    return ResourceLocator.getLocalizationBundle(MESSAGES_PATH, language);
  }

  @Override
  public boolean isRelatedTo(final String instanceId) {
    return instanceId.startsWith(CLASSIFIED);
  }

  @Override
  public String createClassified(ClassifiedDetail classified) {
    try (Connection con = openConnection()) {
      String id = ClassifiedsDAO.createClassified(con, classified);
      classified.setClassifiedId(Integer.parseInt(id));
      createIndex(classified);
      if (classified.isToValidate()) {
        sendAlertToSupervisors(classified);
      }
      return id;
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException(failureOnAdding(CLASSIFIED, ""), e);
    }
  }

  @Override
  public void deleteClassified(final ContributionIdentifier classifiedId) {
    deleteClassified(classifiedId, getTemplate(classifiedId.getComponentInstanceId()));
  }

  private void deleteClassified(ContributionIdentifier classifiedId, PublicationTemplate template) {

    ClassifiedDetail classified = getContributionById(classifiedId).orElseThrow(
        () -> new NotFoundException(NO_SUCH_CLASSIFIED + classifiedId.asString()));

    // remove form content
    try {
      RecordSet set = template.getRecordSet();
      set.delete(classifiedId.getLocalId());
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException(
          failureOnDeleting("form of the classified", classifiedId.getLocalId()), e);
    }

    // remove attached files
    try {
      ResourceReference classifiedForeignKey = new ResourceReference(classifiedId);
      List<SimpleDocument> images = AttachmentServiceProvider.getAttachmentService()
          .listDocumentsByForeignKeyAndType(classifiedForeignKey, DocumentType.attachment, null);
      for (SimpleDocument classifiedImage : images) {
        //delete the picture file in the file server and database
        AttachmentServiceProvider.getAttachmentService()
            .deleteAttachment(classifiedImage);
      }
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException(
          failureOnDeleting("images of the classified", classifiedId), e);
    }

    // remove classified itself
    try (Connection con = openConnection()) {
      ClassifiedsDAO.deleteClassified(con, classifiedId.getLocalId());
    } catch (SQLException e) {
      throw new ClassifiedsRuntimeException(failureOnDeleting(CLASSIFIED, classifiedId), e);
    }

    // remove index
    deleteIndex(classified);
  }

  @Override
  public void unpublishClassified(final ContributionIdentifier classifiedId) {
    try {
      ClassifiedDetail classified = getContributionById(classifiedId).orElseThrow(
          () -> new NotFoundException(NO_SUCH_CLASSIFIED + classifiedId.asString()));
      classified.setStatus(ClassifiedDetail.UNPUBLISHED);
      classified.setUpdateDate(new Date());
      updateClassified(classified);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException(failureOnUpdate(CLASSIFIED, classifiedId), e);
    }
  }

  @Override
  public void deleteAllClassifieds(String instanceId) {
    PublicationTemplate template = getTemplate(instanceId);
    Collection<ClassifiedDetail> classifieds = getAllClassifieds(instanceId);
    for (ClassifiedDetail classified : classifieds) {
      deleteClassified(classified.getIdentifier(), template);
    }
  }

  private void updateClassified(ClassifiedDetail classified) {
    updateClassified(classified, false);
  }

  @Override
  public void updateClassified(ClassifiedDetail classified, boolean notify) {
    try (Connection con = openConnection()) {
      ClassifiedsDAO.updateClassified(con, classified);
      createIndex(classified);
      if (notify) {
        sendAlertToSupervisors(classified);
      }
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException(failureOnUpdate(CLASSIFIED, classified), e);
    }
  }

  @Override
  public Collection<ClassifiedDetail> getAllClassifieds(String instanceId) {
    try (Connection con = openConnection()) {
      return ClassifiedsDAO.getAllClassifieds(con, instanceId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException(
          failureOnGetting("all classifieds in application", instanceId), e);
    }
  }

  @Override
  public String getNbTotalClassifieds(String instanceId) {
    try (Connection con = openConnection()) {
      return ClassifiedsDAO.getNbTotalClassifieds(con, instanceId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException(failureOnGetting(CLASSIFIED, "count"), e);
    }
  }

  @Override
  public List<ClassifiedDetail> getClassifiedsByUser(String instanceId, String userId) {
    try (Connection con = openConnection()) {
      return ClassifiedsDAO.getClassifiedsByUser(con, instanceId, userId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException(failureOnGetting("classifieds of the user", userId), e);
    }
  }

  @Override
  public List<ClassifiedDetail> getClassifiedsToValidate(String instanceId) {
    try (Connection con = openConnection()) {
      return ClassifiedsDAO.getClassifiedsWithStatus(con, instanceId, ClassifiedDetail.TO_VALIDATE,
          0, -1);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException(
          failureOnGetting("classifieds to validate in application", instanceId), e);
    }
  }

  @Override
  public void validateClassified(ContributionIdentifier classifiedId, String userId) {
    try {
      ClassifiedDetail classified = getContributionById(classifiedId).orElseThrow(
          () -> new NotFoundException(NO_SUCH_CLASSIFIED + classifiedId.asString()));
      if (classified.isToValidate()) {
        classified.setValidatorId(userId);
        classified.setValidateDate(new Date());
        classified.setStatus(ClassifiedDetail.VALID);
      }
      updateClassified(classified);
      sendValidationNotification(classified.getCreatorId(), classified, null, userId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException(failureOnValidating(CLASSIFIED, classifiedId), e);
    }

  }

  @Override
  public void refusedClassified(ContributionIdentifier classifiedId, String userId,
      String refusalMotive) {
    try {
      ClassifiedDetail classified = getContributionById(classifiedId).orElseThrow(
          () -> new NotFoundException(NO_SUCH_CLASSIFIED + classifiedId.asString()));
      classified.setStatus(ClassifiedDetail.REFUSED);
      updateClassified(classified);
      sendValidationNotification(classified.getCreatorId(), classified, refusalMotive, userId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException(failureOnValidating(CLASSIFIED, classifiedId), e);
    }
  }

  private void sendValidationNotification(final String userId, final ClassifiedDetail classified,
      final String refusalMotive, final String userIdWhoRefuse) {
    try {
      UserNotificationHelper.buildAndSend(
          new ClassifiedValidationUserNotification(classified, userIdWhoRefuse, refusalMotive,
              userId));
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .error(e);
    }
  }

  @Override
  public void sendSubscriptionsNotification(final String field1, final String field2,
      final ClassifiedDetail classified) {
    try {
      UserNotificationHelper.buildAndSend(new ClassifiedSubscriptionUserNotification(classified,
          getUsersBySubscribe(field1, field2)));
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .error(e);
    }
  }

  @Override
  public Collection<ClassifiedDetail> getAllClassifiedsToUnpublish(int nbDays, String instanceId) {
    try (Connection con = openConnection()) {
      return ClassifiedsDAO.getAllClassifiedsToUnpublish(con, nbDays, instanceId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException(
          failureOnGetting("all classifieds to unpublish in application", instanceId), e);
    }
  }

  @Override
  public List<ClassifiedDetail> search(QueryDescription query) {
    List<ClassifiedDetail> classifieds = new ArrayList<>();
    try {
      List<MatchingIndexEntry> result = SearchEngineProvider.getSearchEngine()
          .search(query)
          .getEntries();
      //classified creation from the results
      for (MatchingIndexEntry matchIndex : result) {
        if (CLASSIFIED_TYPE.equals(matchIndex.getObjectType())) {
          //return only valid classifieds
          ContributionIdentifier classifiedId =
              ContributionIdentifier.from("", matchIndex.getObjectId(),
                  ClassifiedDetail.getResourceType());
          getContributionById(classifiedId)
              .filter(ClassifiedDetail::isValid)
              .ifPresent(classifieds::add);
        }
      }
      // sort the classifieds from the newer to the older
      Collections.reverse(classifieds);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException(e.getMessage(), e);
    } return classifieds;
  }

  @Override
  public void indexClassifieds(String instanceId) {
    // browse all the classifieds
    Collection<ClassifiedDetail> classifieds = getAllClassifieds(instanceId);
    if (classifieds != null) {
      PublicationTemplate template = getTemplate(instanceId);
      for (ClassifiedDetail classified : classifieds) {
        createIndex(classified, template);
      }
    }
  }

  private void createIndex(ClassifiedDetail classified) {
    if (classified != null) {
      createIndex(classified, getTemplate(classified.getInstanceId()));
    }
  }

  private void createIndex(ClassifiedDetail classified, PublicationTemplate template) {
    FullIndexEntry indexEntry;
    if (classified != null) {
      indexEntry = new FullIndexEntry(classified.getInstanceId(), CLASSIFIED_TYPE,
          Integer.toString(classified.getClassifiedId()));
      indexEntry.setTitle(classified.getTitle());
      indexEntry.setPreview(classified.getDescription());
      indexEntry.setCreationDate(classified.getCreationDate());
      indexEntry.setCreationUser(classified.getCreatorId());
      indexEntry.setLastModificationDate(classified.getUpdateDate());

      // indexation of the XML form's content
      String xmlFormShortName = FilenameUtils.getBaseName(template.getFileName());
      try {
        RecordSet set = template.getRecordSet();
        String classifiedId = Integer.toString(classified.getClassifiedId());
        set.indexRecord(classifiedId, xmlFormShortName, indexEntry);
      } catch (Exception e) {
        throw new ClassifiedsRuntimeException(failureOnIndexing(CLASSIFIED, classified.getId()), e);
      }
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  private PublicationTemplate getTemplate(String instanceId) {
    try {
      String xmlFormName =
          organizationController.getComponentParameterValue(instanceId, "XMLFormName");
      if (StringUtil.isDefined(xmlFormName)) {
        String xmlFormShortName =
            xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName.indexOf('.'));
        return PublicationTemplateManager.getInstance()
            .getPublicationTemplate(instanceId + ":" + xmlFormShortName);
      }
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException(
          failureOnGetting("classified form for application", instanceId), e);
    }
    throw new ClassifiedsRuntimeException(
        failureOnGetting("classified form for application", instanceId));
  }

  public void deleteIndex(ClassifiedDetail classified) {
    IndexEntryKey indexEntry = new IndexEntryKey(classified.getInstanceId(), CLASSIFIED_TYPE,
        Integer.toString(classified.getClassifiedId()));
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  @Override
  public void draftOutClassified(ContributionIdentifier classifiedId, String profile, boolean isValidationEnabled) {
    ClassifiedDetail classified = getContributionById(classifiedId)
        .orElseThrow(() -> new NotFoundException(NO_SUCH_CLASSIFIED + classifiedId.asString()));
    String status = classified.getStatus();
    if (classified.isDraft()) {
      if ("admin".equals(profile) || !isValidationEnabled) {
        status = ClassifiedDetail.VALID;
      } else {
        status = ClassifiedDetail.TO_VALIDATE;
      }
      classified.setStatus(status);
      sendAlertToSupervisors(classified);
    }
    classified.setStatus(status);
    updateClassified(classified);
  }

  private void sendAlertToSupervisors(final ClassifiedDetail classified) {
    if (classified.isToValidate()) {
      try {
        UserNotificationHelper.buildAndSend(new ClassifiedSupervisorUserNotification(classified));
      } catch (Exception e) {
        SilverLogger.getLogger(this)
            .error(e);
      }
    }
  }

  @Override
  public void draftInClassified(ContributionIdentifier classifiedId) {
    ClassifiedDetail classified = getContributionById(classifiedId)
        .orElseThrow(() -> new NotFoundException(NO_SUCH_CLASSIFIED + classifiedId.asString()));
    String status = classified.getStatus();
    if (classified.isToValidate() || classified.isValid()) {
      status = ClassifiedDetail.DRAFT;
    }
    classified.setStatus(status);
    updateClassified(classified);
  }

  @Override
  public void createSubscribe(Subscribe subscribe) {
    try (Connection con = openConnection()) {
      if (checkSubscription(subscribe)) {
        String id = ClassifiedsDAO.createSubscribe(con, subscribe);
        subscribe.setSubscribeId(id);
      }
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException(
          failureOnSubscribing(CLASSIFIEDS_IN_APPLICATION, subscribe.getInstanceId()), e);
    }
  }

  @Override
  public void deleteSubscribe(String subscribeId) {
    try (Connection con = openConnection()) {
      ClassifiedsDAO.deleteSubscribe(con, subscribeId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException(failureOnDeleting("subscription", subscribeId), e);
    }
  }

  public boolean checkSubscription(Subscribe subscribe) {
    try {
      Collection<Subscribe> subscriptions =
          getSubscribesByUser(subscribe.getInstanceId(), subscribe.getUserId());
      for (Subscribe sub : subscriptions) {
        if (sub.getField1()
            .equals(subscribe.getField1()) && sub.getField2()
            .equals(subscribe.getField2())) {
          return false;
        }
      }
      return true;

    } catch (Exception e) {
      throw new ClassifiedsRuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public Collection<Subscribe> getSubscribesByUser(String instanceId, String userId) {
    try (Connection con = openConnection()) {
      return ClassifiedsDAO.getSubscribesByUser(con, instanceId, userId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException(failureOnGetting("subscriptions of the user", userId),
          e);
    }
  }

  @Override
  public Collection<String> getUsersBySubscribe(String field1, String field2) {
    try (Connection con = openConnection()) {
      return ClassifiedsDAO.getUsersBySubscribe(con, field1, field2);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException(failureOnGetting("subscriptions", ""), e);
    }
  }

  public Collection<Subscribe> getAllSubscribes(String instanceId) {
    try (Connection con = openConnection()) {
      return ClassifiedsDAO.getAllSubscribes(con, instanceId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException(
          failureOnGetting("subscriptions for application", instanceId), e);
    }
  }

  @Override
  public void deleteAllSubscribes(String instanceId) {
    Collection<Subscribe> subscribes = getAllSubscribes(instanceId);
    for (final Subscribe subscribe : subscribes) {
      deleteSubscribe(subscribe.getSubscribeId());
    }
  }

  @Override
  public List<ClassifiedDetail> getAllValidClassifieds(String instanceId) {
    try (Connection con = openConnection()) {
      return ClassifiedsDAO.getClassifiedsWithStatus(con, instanceId, ClassifiedDetail.VALID, -1,
          -1);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException(
          failureOnGetting("valid classifieds in application", instanceId), e);
    }
  }

  @Override
  public List<ClassifiedDetail> getAllValidClassifieds(String instanceId,
      Map<String, String> mapFields1, Map<String, String> mapFields2, String searchField1,
      String searchField2, int firstItemIndex, int elementsPerPage) {
    try (Connection con = openConnection()) {
      List<ClassifiedDetail> listClassified =
          ClassifiedsDAO.getClassifiedsWithStatus(con, instanceId, ClassifiedDetail.VALID,
              firstItemIndex, elementsPerPage);

      // add the search fields
      String xmlFormName =
          organizationController.getComponentParameterValue(instanceId, "XMLFormName");

      for (ClassifiedDetail classified : listClassified) {
        // add the creator name
        classified.setCreatorName(UserDetail.getById(classified.getCreatorId())
            .getDisplayedName());

        setClassification(classified, searchField1, searchField2, xmlFormName);

        // add the images
        addImages(instanceId, classified);
      }
      return listClassified;
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException(
          failureOnGetting("valid classifieds in application", instanceId), e);
    }
  }

  private void addImages(final String instanceId, final ClassifiedDetail classified) {
    try {
      String classifiedId = Integer.toString(classified.getClassifiedId());
      ResourceReference classifiedForeignKey = new ResourceReference(classifiedId, instanceId);
      List<SimpleDocument> listSimpleDocument = AttachmentServiceProvider.getAttachmentService()
          .listDocumentsByForeignKeyAndType(classifiedForeignKey, DocumentType.attachment, null);
      classified.setImages(listSimpleDocument);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException(
          failureOnGetting("images of valid classifieds in application", instanceId), e);
    }
  }

  @Override
  public void setClassification(ClassifiedDetail classified, String searchField1,
      String searchField2, String xmlFormName) {
    // add of the search fields
    if (StringUtil.isDefined(xmlFormName)) {
      String xmlFormShortName =
          xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName.indexOf('.'));
      try {
        PublicationTemplate pubTemplate = PublicationTemplateManager.getInstance()
            .getPublicationTemplate(classified.getInstanceId() + ":" + xmlFormShortName);
        if (pubTemplate != null) {
          RecordSet recordSet = pubTemplate.getRecordSet();
          DataRecord data = recordSet.getRecord(classified.getId());
          Map<String, String> values = data.getValues("fr");

          Field field1 = data.getField(searchField1);
          String searchValueId1 = field1.getValue();
          String searchValue1 = values.get(searchField1);

          Field field2 = data.getField(searchField2);
          String searchValueId2 = field2.getValue();
          String searchValue2 = values.get(searchField2);

          classified.setSearchValueId1(searchValueId1);
          classified.setSearchValueId2(searchValueId2);
          classified.setSearchValue1(searchValue1);
          classified.setSearchValue2(searchValue2);
        }
      } catch (Exception e) {
        throw new ClassifiedsRuntimeException(failureOnGetting(CLASSIFIED, classified.getId()), e);
      }
    }
  }


  private Connection openConnection() {
    Connection con;
    try {
      con = DBUtil.openConnection();
    } catch (SQLException e) {
      throw new ClassifiedsRuntimeException(e.getMessage(), e);
    }
    return con;
  }
}
