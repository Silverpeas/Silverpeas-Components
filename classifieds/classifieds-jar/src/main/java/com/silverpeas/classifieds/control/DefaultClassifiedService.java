/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.classifieds.control;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FilenameUtils;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.DocumentType;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.search.SearchEngineFactory;

import com.silverpeas.classifieds.dao.ClassifiedsDAO;
import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.silverpeas.classifieds.model.ClassifiedsRuntimeException;
import com.silverpeas.classifieds.model.Subscribe;
import com.silverpeas.classifieds.notification.ClassifiedSubscriptionUserNotification;
import com.silverpeas.classifieds.notification.ClassifiedSupervisorUserNotification;
import com.silverpeas.classifieds.notification.ClassifiedValidationUserNotification;
import com.silverpeas.comment.service.notification.CommentUserNotificationService;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.RecordSet;
import com.silverpeas.notification.builder.helper.UserNotificationHelper;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import org.silverpeas.search.searchEngine.model.MatchingIndexEntry;
import org.silverpeas.search.searchEngine.model.QueryDescription;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;
import org.silverpeas.search.indexEngine.model.IndexEngineProxy;
import org.silverpeas.search.indexEngine.model.IndexEntryPK;

/**
 * Services provided by the Classified Silverpeas component.
 */
@Named("classifiedService")
public class DefaultClassifiedService implements ClassifiedService {

  public static final String COMPONENT_NAME = "classifieds";
  private static final String MESSAGES_PATH = "com.silverpeas.classifieds.multilang.classifiedsBundle";
  private static final String SETTINGS_PATH = "com.silverpeas.classifieds.settings.classifiedsSettings";
  private static final ResourceLocator settings = new ResourceLocator(SETTINGS_PATH, "");

  @Inject
  private CommentUserNotificationService commentUserNotificationService;

  /**
   * Initializes this service by registering itself among Silverpeas core services as interested
   * by events.
   */
  @PostConstruct
  public void initialize() {
    commentUserNotificationService.register(COMPONENT_NAME, this);
  }

  /**
   * Releases all the resources required by this service. For instance, it unregisters from the
   * Silverpeas core services.
   */
  @PreDestroy
  public void release() {
    commentUserNotificationService.unregister(COMPONENT_NAME);
  }

  @Override
  public ClassifiedDetail getContentById(String classifiedId) {
    Connection con = openConnection();
    try {
      return ClassifiedsDAO.getClassified(con, classifiedId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("DefaultClassifiedService.getContentById()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_ERR_GET_CLASSIFIED", e);
    } finally {
      closeConnection(con);
    }
  }

  @Override
  public ResourceLocator getComponentSettings() {
    return settings;
  }

  @Override
  public ResourceLocator getComponentMessages(String language) {
    return new ResourceLocator(MESSAGES_PATH, language);
  }

  @Override
  public String createClassified(ClassifiedDetail classified) {
    Connection con = openConnection();
    try {
      String id = ClassifiedsDAO.createClassified(con, classified);
      classified.setClassifiedId(Integer.parseInt(id));
      createIndex(classified);
      if (classified.getStatus().equals(ClassifiedDetail.TO_VALIDATE)) {
        sendAlertToSupervisors(classified);
      }
      return id;
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("DefaultClassifiedService.createClassified()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_CLASSIFIED_NOT_CREATE", e);
    } finally {
      closeConnection(con);
    }
  }
  
  @Override
  public void deleteClassified(String instanceId, String classifiedId) {
    deleteClassified(instanceId, classifiedId, getTemplate(instanceId));
  }

  private void deleteClassified(String instanceId, String classifiedId, PublicationTemplate template) {
    
    ClassifiedDetail classified = getContentById(classifiedId);
    
    // remove form content
    try {
      RecordSet set = template.getRecordSet();
      set.delete(classifiedId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("DefaultClassifiedService.deleteClassified()",
          SilverpeasRuntimeException.ERROR, "classifieds.CANT_DELETE_FORM_CONTENT", e);
    }
    
    // remove attached files
    try {
      WAPrimaryKey classifiedForeignKey = new SimpleDocumentPK(classifiedId, instanceId);
      List<SimpleDocument> images = AttachmentServiceFactory.getAttachmentService().listDocumentsByForeignKeyAndType(classifiedForeignKey, DocumentType.attachment, null);
      for(SimpleDocument classifiedImage : images) {
        //delete the picture file in the file server and database
        AttachmentServiceFactory.getAttachmentService().deleteAttachment(classifiedImage);
      }
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("DefaultClassifiedService.deleteClassified()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_CLASSIFIED_IMAGES_NOT_DELETE", e);
    }
    
    // remove classified itself
    Connection con = openConnection();
    try {
      ClassifiedsDAO.deleteClassified(con, classifiedId);
    } catch (SQLException e) {
      throw new ClassifiedsRuntimeException("DefaultClassifiedService.deleteClassified()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_CLASSIFIED_NOT_DELETE", e);
    } finally {
      closeConnection(con);
    }
      
    // remove index
    deleteIndex(classified);
  }

  @Override
  public void unpublishClassified(String classifiedId) {
    Connection con = openConnection();
    try {
      ClassifiedDetail classified = getContentById(classifiedId);
      classified.setStatus(ClassifiedDetail.UNPUBLISHED);
      classified.setUpdateDate(new Date());
      updateClassified(classified);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("DefaultClassifiedService.unpublishClassified()",
          SilverpeasRuntimeException.ERROR, "classifieds.EX_ERR_REFUSED_CLASSIFIED", e);
    } finally {
      closeConnection(con);
    }
  }

  @Override
  public void deleteAllClassifieds(String instanceId) {
    PublicationTemplate template = getTemplate(instanceId);
    Collection<ClassifiedDetail> classifieds = getAllClassifieds(instanceId);
    for (ClassifiedDetail classified : classifieds) {
      deleteClassified(instanceId, Integer.toString(classified.getClassifiedId()), template);
    }
  }

  private void updateClassified(ClassifiedDetail classified) {
    updateClassified(classified, false);
  }

  @Override
  public void updateClassified(ClassifiedDetail classified, boolean notify) {
    Connection con = openConnection();
    try {
      ClassifiedsDAO.updateClassified(con, classified);
      createIndex(classified);
      if (notify) {
        sendAlertToSupervisors(classified);
      }
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("DefaultClassifiedService.updateClassified()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_CLASSIFIED_NOT_UPDATE", e);
    } finally {
      closeConnection(con);
    }
  }

  @Override
  public Collection<ClassifiedDetail> getAllClassifieds(String instanceId) {
    Connection con = openConnection();
    try {
      return ClassifiedsDAO.getAllClassifieds(con, instanceId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("DefaultClassifiedService.getAllClassifieds()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_ERR_GET_CLASSIFIEDS", e);
    } finally {
      closeConnection(con);
    }
  }

  @Override
  public String getNbTotalClassifieds(String instanceId) {
    Connection con = openConnection();
    try {
      return ClassifiedsDAO.getNbTotalClassifieds(con, instanceId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("DefaultClassifiedService.getNbTotalClassifieds()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_ERR_NB_CLASSIFIEDS", e);
    } finally {
      closeConnection(con);
    }
  }

  @Override
  public Collection<ClassifiedDetail> getClassifiedsByUser(String instanceId, String userId) {
    Connection con = openConnection();
    try {
      OrganizationController orga = new OrganizationController();
      Collection<ClassifiedDetail> listClassified =  ClassifiedsDAO.getClassifiedsByUser(con, instanceId, userId);
      for(ClassifiedDetail classified : listClassified) {
        //ajouter le nom du createur
        classified.setCreatorName(orga.getUserDetail(classified.getCreatorId())
            .getDisplayedName());     
      }
      return listClassified;
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("DefaultClassifiedService.getClassifiedsByUser()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_ERR_GET_CLASSIFIEDS", e);
    } finally {
      closeConnection(con);
    }
  }

  @Override
  public Collection<ClassifiedDetail> getClassifiedsToValidate(String instanceId) {
    Connection con = openConnection();
    try {
      OrganizationController orga = new OrganizationController();
      Collection<ClassifiedDetail> listClassified = ClassifiedsDAO.getClassifiedsWithStatus(con, instanceId, ClassifiedDetail.TO_VALIDATE, 0, -1);
      for(ClassifiedDetail classified : listClassified) {
        //ajouter le nom du createur
        classified.setCreatorName(orga.getUserDetail(classified.getCreatorId())
            .getDisplayedName());     
      }
      return listClassified;
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("DefaultClassifiedService.getClassifiedsToValidate()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_ERR_GET_CLASSIFIEDS", e);
    } finally {
      closeConnection(con);
    }
  }

  @Override
  public Collection<ClassifiedDetail> getUnpublishedClassifieds(String instanceId, String userId) {
    Connection con = openConnection();
    try {
      return ClassifiedsDAO.getUnpublishedClassifieds(con, instanceId, userId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("DefaultClassifiedService.getUnpublishedClassifieds()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_ERR_GET_CLASSIFIEDS", e);
    } finally {
      closeConnection(con);
    }
  }

  @Override
  public void validateClassified(String classifiedId, String userId) {
    SilverTrace.info("classified", "DefaultClassifiedService.validateClassified()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      ClassifiedDetail classified = getContentById(classifiedId);
      if (ClassifiedDetail.TO_VALIDATE.equalsIgnoreCase(classified.getStatus())) {
        classified.setValidatorId(userId);
        classified.setValidateDate(new Date());
        classified.setStatus(ClassifiedDetail.VALID);
      }
      updateClassified(classified);
      sendValidationNotification(classified.getCreatorId(), classified, null, userId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("DefaultClassifiedService.validateClassified()",
          SilverpeasRuntimeException.ERROR, "classifieds.EX_ERR_VALIDATE_CLASSIFIED", e);
    }
    SilverTrace.info("classified", "DefaultClassifiedService.validateClassified()",
        "root.MSG_GEN_EXIT_METHOD", "classifiedId = " + classifiedId);
  }

  @Override
  public void refusedClassified(String classifiedId, String userId, String refusalMotive) {
    SilverTrace.info("classified", "DefaultClassifiedService.refusedClassified()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      ClassifiedDetail classified = getContentById(classifiedId);
      classified.setStatus(ClassifiedDetail.REFUSED);
      updateClassified(classified);
      sendValidationNotification(classified.getCreatorId(), classified, refusalMotive, userId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("DefaultClassifiedService.refusedClassified()",
          SilverpeasRuntimeException.ERROR, "classifieds.EX_ERR_REFUSED_CLASSIFIED", e);
    }
  }

  private void sendValidationNotification(final String userId, final ClassifiedDetail classified,
      final String refusalMotive, final String userIdWhoRefuse) {
    try {

      UserNotificationHelper.buildAndSend(new ClassifiedValidationUserNotification(classified, userIdWhoRefuse,
          refusalMotive, userId));

    } catch (Exception e) {
      SilverTrace.warn("classifieds", "classifieds.sendValidationNotification()",
          "classifieds.EX_ERR_ALERT_USERS", "userId = " + userId +
              ", classified = " + classified.getClassifiedId(), e);
    }
  }

  public void sendSubscriptionsNotification(final String field1, final String field2, final ClassifiedDetail classified) {
    try {

      UserNotificationHelper.buildAndSend(new ClassifiedSubscriptionUserNotification(classified, getUsersBySubscribe(
          field1, field2)));

    } catch (Exception e) {
      SilverTrace.warn("classifieds", "DefaultClassifiedService.sendSubscriptionsNotification()",
          "classifieds.EX_ERR_ALERT_USERS", "", e);
    }
  }

  @Override
  public Collection<ClassifiedDetail> getAllClassifiedsToUnpublish(int nbDays, String instanceId) {
    Connection con = openConnection();
    SilverTrace.info("classifieds", "DefaultClassifiedService.getAllClassifiedsToUnpublish()",
        "root.MSG_GEN_ENTER_METHOD", "nbDays = " + nbDays);
    try {
      return ClassifiedsDAO.getAllClassifiedsToUnpublish(con, nbDays, instanceId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("DefaultClassifiedService.getAllClassifiedsToUnpublish()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_ERR_GET_CLASSIFIEDS", e);
    } finally {
      // fermer la connexion
      closeConnection(con);
    }
  }

  @Override
  public List<ClassifiedDetail> search(QueryDescription query) {
    List<ClassifiedDetail> classifieds = new ArrayList<ClassifiedDetail>();
    try {
      List<MatchingIndexEntry> result = SearchEngineFactory.getSearchEngine().search(query).getEntries();
      //création des petites annonces à partir des resultats
      for (MatchingIndexEntry matchIndex : result) {
        if ("Classified".equals(matchIndex.getObjectType())) {
          //ne retourne que les petites annonces valides
          ClassifiedDetail classified = this.getContentById(matchIndex.getObjectId());
          if(classified != null && ClassifiedDetail.VALID.equals(classified.getStatus())) {
            classifieds.add(classified);
            SilverTrace.info("classifieds", "DefaultClassifiedService.search()",
                "root.MSG_GEN_PARAM_VALUE", "classified = " + classified.getTitle());
          }
        }
      }
      //ordonnancement des petites annonces de la plus récente vers la plus ancienne
      Collections.reverse(classifieds);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("DefaultClassifiedService.search()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_ADD_OBJECT", e);
    }
    return classifieds;
  }

  @Override
  public void indexClassifieds(String instanceId) {
    // parcourir toutes les petites annonnces
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
    FullIndexEntry indexEntry = null;
    if (classified != null) {
      indexEntry =
          new FullIndexEntry(classified.getInstanceId(), "Classified", Integer.toString(classified
              .getClassifiedId()));
      indexEntry.setTitle(classified.getTitle());
      indexEntry.setPreView(classified.getDescription());
      indexEntry.setCreationDate(classified.getCreationDate());
      indexEntry.setCreationUser(classified.getCreatorId());
      indexEntry.setLastModificationDate(classified.getUpdateDate());

      // indexation du contenu du formulaire XML
      String xmlFormShortName = FilenameUtils.getBaseName(template.getFileName());
      try {
        RecordSet set = template.getRecordSet();
        String classifiedId = Integer.toString(classified.getClassifiedId());
        set.indexRecord(classifiedId, xmlFormShortName, indexEntry);
      } catch (Exception e) {
        throw new ClassifiedsRuntimeException("DefaultClassifiedService.createIndex()",
            SilverpeasRuntimeException.ERROR,
            "classifieds.EX_ERR_GET_SILVEROBJECTID", e);
      }
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }
  
  private PublicationTemplate getTemplate(String instanceId) {
    try {
      OrganisationController orga = new OrganizationController();
      String xmlFormName = orga.getComponentParameterValue(instanceId, "XMLFormName");
      if (StringUtil.isDefined(xmlFormName)) {
        String xmlFormShortName =
            xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
        return PublicationTemplateManager.getInstance().getPublicationTemplate(
            instanceId + ":" + xmlFormShortName);
      }
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("DefaultClassifiedService.getTemplate()",
          SilverpeasRuntimeException.ERROR, "classifieds.CANT_GET_FORM_CONTENT", e);
    }
    throw new ClassifiedsRuntimeException("DefaultClassifiedService.getTemplate()",
        SilverpeasRuntimeException.ERROR, "classifieds.FORM_NOT_DEFINED");
  }

  public void deleteIndex(ClassifiedDetail classified) {
    SilverTrace.info("classifieds", "DefaultClassifiedService.deleteIndex()", "root.MSG_GEN_ENTER_METHOD",
        "ClassifiedId = " + classified.toString());
    IndexEntryPK indexEntry =
        new IndexEntryPK(classified.getInstanceId(), "Classified", Integer.toString(classified
            .getClassifiedId()));
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  @Override
  public void draftOutClassified(String classifiedId, String profile) {
    ClassifiedDetail classified = getContentById(classifiedId);
    String status = classified.getStatus();
    if (status.equals(ClassifiedDetail.DRAFT)) {
      status = ClassifiedDetail.TO_VALIDATE;
      if ("admin".equals(profile)) {
        status = ClassifiedDetail.VALID;
      }
      classified.setStatus(status);
      sendAlertToSupervisors(classified);
    }
    classified.setStatus(status);
    updateClassified(classified);
  }

  private void sendAlertToSupervisors(final ClassifiedDetail classified) {
    if (ClassifiedDetail.TO_VALIDATE.equalsIgnoreCase(classified.getStatus())) {
      try {

        UserNotificationHelper.buildAndSend(new ClassifiedSupervisorUserNotification(classified));

      } catch (Exception e) {
        SilverTrace.warn("classifieds", "classifieds.sendAlertToSupervisors()",
            "classifieds.EX_ERR_ALERT_USERS", "userId = " +
                classified.getCreatorId() + ", classified = " + classified.getClassifiedId(), e);
      }
    }
  }

  @Override
  public void draftInClassified(String classifiedId) {
    ClassifiedDetail classified = getContentById(classifiedId);
    String status = classified.getStatus();
    if (status.equals(ClassifiedDetail.TO_VALIDATE) || status.equals(ClassifiedDetail.VALID)) {
      status = ClassifiedDetail.DRAFT;
    }
    classified.setStatus(status);
    updateClassified(classified);
  }

  @Override
  public void createSubscribe(Subscribe subscribe) {
    Connection con = openConnection();
    try {
      if (checkSubscription(subscribe)) {
        String id = ClassifiedsDAO.createSubscribe(con, subscribe);
        subscribe.setSubscribeId(id);
      }
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("DefaultClassifiedService.createSubscribe()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_SUBSCRIBE_NOT_CREATE", e);
    } finally {
      // fermer la connexion
      closeConnection(con);
    }
  }

  @Override
  public void deleteSubscribe(String subscribeId) {
    Connection con = openConnection();
    try {
      ClassifiedsDAO.deleteSubscribe(con, subscribeId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("DefaultClassifiedService.deleteSubscribe()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_SUBSCRIBE_NOT_DELETE", e);
    } finally {
      // fermer la connexion
      closeConnection(con);
    }
  }

  public boolean checkSubscription(Subscribe subscribe) {
    try {
      Collection<Subscribe> subscriptions =
          getSubscribesByUser(subscribe.getInstanceId(), subscribe.getUserId());
      for (Subscribe sub : subscriptions) {
        if (sub.getField1().equals(subscribe.getField1()) &&
            sub.getField2().equals(subscribe.getField2())) {
          return false;
        }
      }
      return true;

    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("DefaultClassifiedService.checkSubscription()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_ERR_GET_SUBSCRIBE", e);
    }
  }

  @Override
  public Collection<Subscribe> getSubscribesByUser(String instanceId, String userId) {
    Connection con = openConnection();
    try {
      return ClassifiedsDAO.getSubscribesByUser(con, instanceId, userId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("DefaultClassifiedService.getSubscribesByUser()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_ERR_GET_SUBSCRIBES", e);
    } finally {
      // fermer la connexion
      closeConnection(con);
    }
  }

  @Override
  public Collection<String> getUsersBySubscribe(String field1, String field2) {
    Connection con = openConnection();
    try {
      return ClassifiedsDAO.getUsersBySubscribe(con, field1, field2);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("DefaultClassifiedService.getUsersBySubscribe()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_ERR_GET_SUBSCRIBES", e);
    } finally {
      // fermer la connexion
      closeConnection(con);
    }
  }

  public Collection<Subscribe> getAllSubscribes(String instanceId) {
    Connection con = openConnection();
    try {
      return ClassifiedsDAO.getAllSubscribes(con, instanceId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("DefaultClassifiedService.getAllSubscribes()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_ERR_GET_SUBSCRIBES", e);
    } finally {
      // fermer la connexion
      closeConnection(con);
    }
  }

  @Override
  public void deleteAllSubscribes(String instanceId) {
    Collection<Subscribe> subscribes = getAllSubscribes(instanceId);
    Iterator<Subscribe> it = subscribes.iterator();
    while (it.hasNext()) {
      Subscribe subscribe = it.next();
      deleteSubscribe(subscribe.getSubscribeId());
    }
  }
  
  @Override
  public Collection<ClassifiedDetail> getAllValidClassifieds(String instanceId, 
          Map<String, String> mapFields1, Map<String, String> mapFields2, 
          String searchField1, String searchField2, 
          int currentPage, int elementsPerPage) {
    Connection con = openConnection();
    try {
      OrganizationController orga = new OrganizationController();
      List<ClassifiedDetail> listClassified = ClassifiedsDAO.getClassifiedsWithStatus(con, instanceId, ClassifiedDetail.VALID, currentPage, elementsPerPage);
      
      for(ClassifiedDetail classified : listClassified) {
        String classifiedId = Integer.toString(classified.getClassifiedId());
        
        //Ajout du nom du createur
        classified.setCreatorName(orga.getUserDetail(classified.getCreatorId())
            .getDisplayedName());   
        
        //Ajout des champs de recherche
        String xmlFormName =
            orga.getComponentParameterValue(classified.getInstanceId(), "XMLFormName");
        setClassification(classified, searchField1, searchField2, xmlFormName);
        
        //Ajout des images
        try {
          WAPrimaryKey classifiedForeignKey = new SimpleDocumentPK(classifiedId, classified.getInstanceId());
          List<SimpleDocument> listSimpleDocument = AttachmentServiceFactory.getAttachmentService().listDocumentsByForeignKeyAndType(classifiedForeignKey, DocumentType.attachment, null);
          classified.setImages(listSimpleDocument);
        } catch (Exception e) {
          throw new ClassifiedsRuntimeException("DefaultClassifiedService.getAllValidClassifieds()",
              SilverpeasRuntimeException.ERROR, "classifieds.MSG_ERR_GET_IMAGES", e);
        }
      }
      
      // pour ordonner les petites annonces de la plus récente vers la plus ancienne
      Collections.reverse(listClassified);
      
      return listClassified;
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("DefaultClassifiedService.getAllValidClassifieds()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_ERR_GET_CLASSIFIEDS", e);
    } finally {
      closeConnection(con);
    }
  }
  
  public void setClassification(ClassifiedDetail classified, String searchField1, String searchField2, String xmlFormName) {
    //Ajout des champs de recherche
    if (StringUtil.isDefined(xmlFormName)) {
      String xmlFormShortName =
          xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
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
        throw new ClassifiedsRuntimeException("DefaultClassifiedService.setClassification()",
            SilverpeasRuntimeException.ERROR,
            "classifieds.MSG_ERR_GET_CLASSIFIED_TEMPLATE", "classifiedId = "+classified.getId(), e);
      }
    }
  }

  private void closeConnection(Connection con) {
    try {
      con.close();
    } catch (SQLException e) {
      // traitement des exceptions
      throw new ClassifiedsRuntimeException("DefaultClassifiedService.closeConnection()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_CLOSE_FAILED", e);
    }
  }

  private Connection openConnection() {
    Connection con;
    // initialisation de la connexion
    try {
      con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    } catch (UtilException e) {
      // traitement des exceptions
      throw new ClassifiedsRuntimeException("DefaultClassifiedService.openConnection()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
    return con;
  }
}
