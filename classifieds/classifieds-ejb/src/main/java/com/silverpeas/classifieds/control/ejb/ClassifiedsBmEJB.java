/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.silverpeas.classifieds.control.ejb;

import static com.silverpeas.classifieds.ClassifiedUtil.getClassifiedUrl;
import static com.silverpeas.classifieds.ClassifiedUtil.getMessage;
import static com.silverpeas.classifieds.ClassifiedUtil.newTemplate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.silverpeas.classifieds.dao.ClassifiedsDAO;
import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.silverpeas.classifieds.model.ClassifiedsRuntimeException;
import com.silverpeas.classifieds.model.Subscribe;
import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.ui.DisplayI18NHelper;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.searchEngine.control.ejb.SearchEngineBm;
import com.stratelia.webactiv.searchEngine.control.ejb.SearchEngineBmHome;
import com.stratelia.webactiv.searchEngine.model.MatchingIndexEntry;
import com.stratelia.webactiv.searchEngine.model.QueryDescription;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;

/**
 * @author
 */
public class ClassifiedsBmEJB implements SessionBean, ClassifiedsBmBusinessSkeleton {
  private static final long serialVersionUID = 5737592996224214551L;

  @Override
  public String createClassified(ClassifiedDetail classified) {
    Connection con = initCon();
    try {
      String id = ClassifiedsDAO.createClassified(con, classified);
      classified.setClassifiedId(Integer.parseInt(id));
      createIndex(classified);
      if (classified.getStatus().equals(ClassifiedDetail.TO_VALIDATE)) {
        sendAlertToSupervisors(classified);
      }
      return id;
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifiedsBmEJB.createClassified()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_CLASSIFIED_NOT_CREATE", e);
    } finally {
      fermerCon(con);
    }
  }

  @Override
  public void deleteClassified(String classifiedId) {
    Connection con = initCon();
    try {
      ClassifiedDetail classified = getClassified(classifiedId);
      ClassifiedsDAO.deleteClassified(con, classifiedId);
      deleteIndex(classified);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifiedsBmEJB.deleteClassified()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_CLASSIFIED_NOT_DELETE", e);
    } finally {
      fermerCon(con);
    }
  }

  @Override
  public void deleteAllClassifieds(String instanceId) {
    Collection<ClassifiedDetail> classifieds = getAllClassifieds(instanceId);
    for (ClassifiedDetail classified : classifieds) {
      deleteClassified(Integer.toString(classified.getClassifiedId()));
    }
  }

  private void updateClassified(ClassifiedDetail classified) {
    updateClassified(classified, false);
  }

  @Override
  public void updateClassified(ClassifiedDetail classified, boolean notify) {
    Connection con = initCon();
    try {
      ClassifiedsDAO.updateClassified(con, classified);
      createIndex(classified);
      if (notify) {
        sendAlertToSupervisors(classified);
      }
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifiedsBmEJB.updateClassified()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_CLASSIFIED_NOT_UPDATE", e);
    } finally {
      fermerCon(con);
    }
  }

  @Override
  public ClassifiedDetail getClassified(String classifiedId) {
    Connection con = initCon();
    try {
      return ClassifiedsDAO.getClassified(con, classifiedId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifiedsBmEJB.getClassified()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_ERR_GET_CLASSIFIED", e);
    } finally {
      fermerCon(con);
    }
  }

  @Override
  public Collection<ClassifiedDetail> getAllClassifieds(String instanceId) {
    Connection con = initCon();
    try {
      return ClassifiedsDAO.getAllClassifieds(con, instanceId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifiedsBmEJB.getAllClassifieds()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_ERR_GET_CLASSIFIEDS", e);
    } finally {
      fermerCon(con);
    }
  }

  @Override
  public String getNbTotalClassifieds(String instanceId) {
    Connection con = initCon();
    try {
      return ClassifiedsDAO.getNbTotalClassifieds(con, instanceId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifiedsBmEJB.getNbTotalClassifieds()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_ERR_NB_CLASSIFIEDS", e);
    } finally {
      fermerCon(con);
    }
  }

  @Override
  public Collection<ClassifiedDetail> getClassifiedsByUser(String instanceId, String userId) {
    Connection con = initCon();
    try {
      return ClassifiedsDAO.getClassifiedsByUser(con, instanceId, userId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifiedsBmEJB.getClassifiedsByUser()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_ERR_GET_CLASSIFIEDS", e);
    } finally {
      fermerCon(con);
    }
  }

  @Override
  public Collection<ClassifiedDetail> getClassifiedsToValidate(String instanceId) {
    Connection con = initCon();
    try {
      return ClassifiedsDAO.getClassifiedsToValidate(con, instanceId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifiedsBmEJB.getClassifiedsToValidate()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_ERR_GET_CLASSIFIEDS", e);
    } finally {
      fermerCon(con);
    }
  }

  @Override
  public void validateClassified(String classifiedId, String userId) {
    SilverTrace.info("classified", "ClassifiedsBmEJB.validateClassified()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      ClassifiedDetail classified = getClassified(classifiedId);
      if (ClassifiedDetail.TO_VALIDATE.equalsIgnoreCase(classified.getStatus())) {
        classified.setValidatorId(userId);
        classified.setValidateDate(new Date());
        classified.setStatus(ClassifiedDetail.VALID);
      }
      updateClassified(classified);
      sendValidationNotification(classified.getCreatorId(), classified, null, userId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifiedsBmEJB.validateClassified()",
          SilverpeasRuntimeException.ERROR, "classifieds.EX_ERR_VALIDATE_CLASSIFIED", e);
    }
    SilverTrace.info("classified", "ClassifiedsBmEJB.validateClassified()",
        "root.MSG_GEN_EXIT_METHOD", "classifiedId = " + classifiedId);
  }

  @Override
  public void refusedClassified(String classifiedId, String userId, String refusalMotive) {
    SilverTrace.info("classified", "ClassifiedsBmEJB.refusedClassified()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      ClassifiedDetail classified = getClassified(classifiedId);
      classified.setStatus(ClassifiedDetail.REFUSED);
      updateClassified(classified);
      sendValidationNotification(classified.getCreatorId(), classified, refusalMotive, userId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifiedsBmEJB.unvalidateClassified()",
          SilverpeasRuntimeException.ERROR, "classifieds.EX_ERR_REFUSED_CLASSIFIED", e);
    }
  }

  private void sendValidationNotification(String userId, ClassifiedDetail classified,
      String refusalMotive, String userIdWhoRefuse) {
    try {
      if (userId != null) {

        Map<String, SilverpeasTemplate> templates = new HashMap<String, SilverpeasTemplate>();
        String subject = getValidationNotificationSubject(classified, DisplayI18NHelper.getDefaultLanguage());
        String templateName = "validated";
        if (!ClassifiedDetail.VALID.equals(classified.getStatus())) {
          templateName = "refused";
        }
        NotificationMetaData notifMetaData =
            new NotificationMetaData(NotificationParameters.NORMAL, subject, templates,
                templateName);

        for (String language : DisplayI18NHelper.getLanguages()) {
          SilverpeasTemplate template = newTemplate(classified);
          template.setAttribute("refusalMotive", refusalMotive);
          templates.put(language, template);
          notifMetaData.addLanguage(language,
              getValidationNotificationSubject(classified, language), "");
        }

        notifMetaData.addUserRecipient(new UserRecipient(userId));
        notifMetaData.setLink(getClassifiedUrl(classified));
        notifMetaData.setComponentId(classified.getInstanceId());
        notifyUsers(notifMetaData, userIdWhoRefuse);
      }
    } catch (Exception e) {
      SilverTrace.warn("classifieds", "classifieds.sendValidationNotification()",
          "classifieds.EX_ERR_ALERT_USERS", "userId = " + userId +
              ", classified = " + classified.getClassifiedId(), e);
    }
  }

  @Override
  public void sendSubscriptionsNotification(String field1, String field2,
      ClassifiedDetail classified) {
    // We alert subscribers only if classified is Valid
    Collection<String> users = getUsersBySubscribe(field1, field2);
    if (users != null) {
      try {
        Map<String, SilverpeasTemplate> templates = new HashMap<String, SilverpeasTemplate>();
        String subject = getMessage("classifieds.mailNewPublicationSubscription");
        NotificationMetaData notifMetaData =
            new NotificationMetaData(NotificationParameters.NORMAL, subject, templates,
                "subscription");

        for (String language : DisplayI18NHelper.getLanguages()) {
          SilverpeasTemplate template = newTemplate(classified);
          templates.put(language, template);
          notifMetaData.addLanguage(language, getMessage(
              "classifieds.mailNewPublicationSubscription", subject, language), "");
        }
        for (String user : users) {
          notifMetaData.addUserRecipient(new UserRecipient(user));
        }
        notifMetaData.setLink(getClassifiedUrl(classified));
        notifMetaData.setComponentId(classified.getInstanceId());
        notifyUsers(notifMetaData, classified.getCreatorId());
      } catch (Exception e) {
        SilverTrace.warn("classifieds", "ClassifiedsBmEJB.sendSubscriptionsNotification()",
            "classifieds.EX_ERR_ALERT_USERS", "", e);
      }
    }
  }

  private String getValidationNotificationSubject(ClassifiedDetail classified, final String language) {
    String subject = "";
    if (ClassifiedDetail.VALID.equals(classified.getStatus())) {
      subject = getMessage("classifieds.classifiedValidated", language);
    } else {
      subject = getMessage("classifieds.classifiedRefused", language);
    }
    return subject;
  }

  @Override
  public Collection<ClassifiedDetail> getAllClassifiedsToDelete(int nbDays) {
    Connection con = initCon();
    SilverTrace.info("classifieds", "classifiedsBmEJB.getAllClassifiedsToDelete()",
        "root.MSG_GEN_ENTER_METHOD", "nbDays = " + nbDays);
    try {
      return ClassifiedsDAO.getAllClassifiedsToDelete(con, nbDays);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifiedsBmEJB.getAllClassifiedsToDelete()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_ERR_GET_CLASSIFIEDS", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  private void notifyUsers(NotificationMetaData notifMetaData, String senderId) {
    Connection con = null;
    try {
      con = initCon();
      notifMetaData.setConnection(con);
      if (notifMetaData.getSender() == null || notifMetaData.getSender().length() == 0) {
        notifMetaData.setSender(senderId);
      }
      getNotificationSender(notifMetaData.getComponentId()).notifyUser(notifMetaData);
    } catch (NotificationManagerException e) {
      SilverTrace.warn("classifieds", "classifiedsBmEJB.notifyUsers()",
          "classifieds.EX_ERR_ALERT_USERS", e);
    } finally {
      fermerCon(con);
    }
  }

  private NotificationSender getNotificationSender(String componentId) {
    // must return a new instance each time
    // This is to resolve Serializable problems
    NotificationSender notifSender = new NotificationSender(componentId);
    return notifSender;
  }

  @Override
  public Collection<ClassifiedDetail> search(QueryDescription query) {
    List<ClassifiedDetail> classifieds = new ArrayList<ClassifiedDetail>();
    MatchingIndexEntry[] result = null;
    OrganizationController orga = new OrganizationController();
    try {
      SearchEngineBm searchEngineBm = getSearchEngineBm();
      searchEngineBm.search(query);
      result = searchEngineBm.getRange(0, searchEngineBm.getResultLength());

      // création des petites annonces à partir des resultats
      for (int i = 0; i < result.length; i++) {
        MatchingIndexEntry matchIndex = result[i];
        if (matchIndex.getObjectType().equals("Classified")) {
          ClassifiedDetail classified = getClassified(matchIndex.getObjectId());
          if (classified != null) {
            SilverTrace.info("classifieds", "ClassifiedsBmEJB.search()",
                "root.MSG_GEN_ENTER_METHOD", "classified = " + classified.getTitle());
            // ne l'ajouter que si elle est valide
            if (classified.getStatus().equals(ClassifiedDetail.VALID)) {
              // ajouter le nom du createur
              classified.setCreatorName(orga.getUserDetail(classified.getCreatorId())
                  .getDisplayedName());
              classifieds.add(classified);
            }
          }
        }
      }
      // pour ordonner les petites annonces de la plus récente vers la plus ancienne
      Collections.reverse(classifieds);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifiedsBmEJB.search()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_ADD_OBJECT", e);
    }
    return classifieds;
  }

  @Override
  public void indexClassifieds(String instanceId) {
    // parcourir toutes les petites annonnces
    Collection<ClassifiedDetail> classifieds = getAllClassifieds(instanceId);
    if (classifieds != null) {
      for (ClassifiedDetail classified : classifieds) {
        createIndex(classified);
      }
    }
  }

  public void createIndex(ClassifiedDetail classified) {
    FullIndexEntry indexEntry = null;
    if (classified != null) {
      indexEntry =
          new FullIndexEntry(classified.getInstanceId(), "Classified", Integer.toString(classified
              .getClassifiedId()));
      indexEntry.setTitle(classified.getTitle());
      indexEntry.setCreationDate(classified.getCreationDate());
      indexEntry.setCreationUser(classified.getCreatorId());

      // indéxation du contenu du formulaire XML
      OrganizationController orga = new OrganizationController();
      String xmlFormName =
          orga.getComponentParameterValue(classified.getInstanceId(), "XMLFormName");
      if (StringUtil.isDefined(xmlFormName)) {
        String xmlFormShortName =
            xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
        PublicationTemplate pubTemplate;
        try {
          pubTemplate = PublicationTemplateManager.getInstance()
              .getPublicationTemplate(classified.getInstanceId() + ":" + xmlFormShortName);
          RecordSet set = pubTemplate.getRecordSet();
          String classifiedId = Integer.toString(classified.getClassifiedId());
          set.indexRecord(classifiedId, xmlFormShortName, indexEntry);
          SilverTrace.info("classifieds", "ClassifiedsBmEJB.createIndex()",
              "root.MSG_GEN_ENTER_METHOD", "indexEntry = " + indexEntry.toString());
        } catch (Exception e) {
          throw new ClassifiedsRuntimeException("ClassifiedsBmEJB.createIndex()",
              SilverpeasRuntimeException.ERROR,
              "classifieds.EX_ERR_GET_SILVEROBJECTID", e);
        }
      }
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  public void deleteIndex(ClassifiedDetail classified) {
    SilverTrace.info("classifieds", "ClassifiedsBmEJB.deleteIndex()", "root.MSG_GEN_ENTER_METHOD",
        "ClassifiedId = " + classified.toString());
    IndexEntryPK indexEntry =
        new IndexEntryPK(classified.getInstanceId(), "Classified", Integer.toString(classified
            .getClassifiedId()));
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  public SearchEngineBm getSearchEngineBm() {
    SearchEngineBm searchEngineBm = null;
    try {
      SearchEngineBmHome searchEngineHome =
          (SearchEngineBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.SEARCHBM_EJBHOME,
              SearchEngineBmHome.class);
      searchEngineBm = searchEngineHome.create();
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifiedsBmEJB.getSearchEngineBm()",
          SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return searchEngineBm;
  }

  @Override
  public void draftOutClassified(String classifiedId, String profile) {
    ClassifiedDetail classified = getClassified(classifiedId);
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

  private void sendAlertToSupervisors(ClassifiedDetail classified) {
    if (ClassifiedDetail.TO_VALIDATE.equalsIgnoreCase(classified.getStatus())) {
      try {
        Map<String, SilverpeasTemplate> templates = new HashMap<String, SilverpeasTemplate>();
        String subject = getMessage("classifieds.supervisorNotifSubject");
        NotificationMetaData notifMetaData =
            new NotificationMetaData(NotificationParameters.NORMAL, subject, templates,
                "tovalidate");

        for (String language : DisplayI18NHelper.getLanguages()) {
          SilverpeasTemplate template = newTemplate(classified);
          templates.put(language, template);
          notifMetaData.addLanguage(language, getMessage(
              "classifieds.supervisorNotifSubject", subject, language), "");
        }

        List<String> roles = new ArrayList<String>();
        roles.add("admin");
        OrganizationController orga = new OrganizationController();
        String[] admins = orga.getUsersIdsByRoleNames(classified.getInstanceId(), roles);
        for (String admin : admins) {
          notifMetaData.addUserRecipient(new UserRecipient(admin));
        }
        notifMetaData.setLink(getClassifiedUrl(classified));
        notifMetaData.setComponentId(classified.getInstanceId());
        notifyUsers(notifMetaData, classified.getCreatorId());
      } catch (Exception e) {
        SilverTrace.warn("classifieds", "classifieds.sendAlertToSupervisors()",
            "classifieds.EX_ERR_ALERT_USERS", "userId = " +
                classified.getCreatorId() + ", classified = " + classified.getClassifiedId(), e);
      }
    }
  }

  @Override
  public void draftInClassified(String classifiedId) {
    ClassifiedDetail classified = getClassified(classifiedId);
    String status = classified.getStatus();
    if (status.equals(ClassifiedDetail.TO_VALIDATE) || status.equals(ClassifiedDetail.VALID)) {
      status = ClassifiedDetail.DRAFT;
    }
    classified.setStatus(status);
    updateClassified(classified);
  }

  @Override
  public void createSubscribe(Subscribe subscribe) {
    Connection con = initCon();
    try {
      if (checkSubscription(subscribe)) {
        String id = ClassifiedsDAO.createSubscribe(con, subscribe);
        subscribe.setSubscribeId(id);
      }
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifiedsBmEJB.createSubscribe()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_SUBSCRIBE_NOT_CREATE", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  @Override
  public void deleteSubscribe(String subscribeId) {
    Connection con = initCon();
    try {
      ClassifiedsDAO.deleteSubscribe(con, subscribeId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifiedsBmEJB.deleteSubscribe()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_SUBSCRIBE_NOT_DELETE", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
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
      throw new ClassifiedsRuntimeException("ClassifiedsBmEJB.checkSubscription()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_ERR_GET_SUBSCRIBE", e);
    }
  }

  @Override
  public Collection<Subscribe> getSubscribesByUser(String instanceId, String userId) {
    Connection con = initCon();
    try {
      return ClassifiedsDAO.getSubscribesByUser(con, instanceId, userId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifiedsBmEJB.getSubscribesByUser()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_ERR_GET_SUBSCRIBES", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  @Override
  public Collection<String> getUsersBySubscribe(String field1, String field2) {
    Connection con = initCon();
    try {
      return ClassifiedsDAO.getUsersBySubscribe(con, field1, field2);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifiedsBmEJB.getUsersBySubscribe()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_ERR_GET_SUBSCRIBES", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  public Collection<Subscribe> getAllSubscribes(String instanceId) {
    Connection con = initCon();
    try {
      return ClassifiedsDAO.getAllSubscribes(con, instanceId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifiedsBmEJB.getAllSubscribes()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_ERR_GET_SUBSCRIBES", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
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

  private void fermerCon(Connection con) {
    try {
      con.close();
    } catch (SQLException e) {
      // traitement des exceptions
      throw new ClassifiedsRuntimeException("ClassifiedsBmEJB.fermerCon()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_CLOSE_FAILED", e);
    }
  }

  private Connection initCon() {
    Connection con;
    // initialisation de la connexion
    try {
      con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    } catch (UtilException e) {
      // traitement des exceptions
      throw new ClassifiedsRuntimeException("ClassifiedsBmEJB.initCon()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
    return con;
  }

  public void ejbCreate() {
    // not implemented
  }

  @Override
  public void setSessionContext(SessionContext context) {
    // not implemented
  }

  @Override
  public void ejbRemove() {
    // not implemented
  }

  @Override
  public void ejbActivate() {
    // not implemented
  }

  @Override
  public void ejbPassivate() {
    // not implemented
  }
}