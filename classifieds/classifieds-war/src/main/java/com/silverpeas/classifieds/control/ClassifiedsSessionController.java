/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.silverpeas.classifieds.model.ClassifiedsRuntimeException;
import com.silverpeas.classifieds.model.Subscribe;
import com.silverpeas.comment.service.CommentService;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.StringUtil;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.model.CommentPK;
import com.silverpeas.comment.service.CommentServiceFactory;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.silverpeas.wysiwyg.WysiwygException;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.search.searchEngine.model.QueryDescription;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ClassifiedsSessionController extends AbstractComponentSessionController {

  private int indexOfFirstItemToDisplay = 0;
  //private Map<String, String> fields1 = createListField(getSearchFields1());
  //private Map<String, String> fields2 = createListField(getSearchFields2());
  private Map<String, String> fields1 = null;
  private Map<String, String> fields2 = null;
  private CommentService commentService = null;
  private ResourcesWrapper resources = null;
  private ClassifiedService classifiedService;

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public ClassifiedsSessionController(MainSessionController mainSessionCtrl,
          ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
            "com.silverpeas.classifieds.multilang.classifiedsBundle",
            "com.silverpeas.classifieds.settings.classifiedsIcons");

    // affectation du formulaire
    String xmlFormName = getXMLFormName();
    String xmlFormShortName = null;
    if (StringUtil.isDefined(xmlFormName)) {
      xmlFormShortName =
              xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
      try {
        getPublicationTemplateManager().addDynamicPublicationTemplate(getComponentId() + ":"
                + xmlFormShortName, xmlFormName);
      } catch (PublicationTemplateException e) {
        throw new ClassifiedsRuntimeException("GallerySessionController.super()",
                SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
  }

  /**
   * Gets the resources associated with this session controller.
   * @return all of the resources (messages, settings, icons, ...)
   */
  public synchronized ResourcesWrapper getResources() {
    if (resources == null) {
      resources = new ResourcesWrapper(getMultilang(), getIcon(), getSettings(), getLanguage());
    }
    return resources;
  }

  /**
   * get classified corresponding to classifiedId
   * @param classifiedId : String
   * @return classified : ClassifiedDetail
   */
  public ClassifiedDetail getClassified(String classifiedId) {
    ClassifiedDetail classified = new ClassifiedDetail();
    classified = getClassifiedService().getContentById(classifiedId);
    classified.setCreatorName(getUserDetail(classified.getCreatorId()).getDisplayedName());
    classified.setCreatorEmail(getUserDetail(classified.getCreatorId()).geteMail());
    if (StringUtil.isDefined(classified.getValidatorId())) {
      classified.setValidatorName((getUserDetail(classified.getValidatorId()).getDisplayedName()));
    }

    return classified;
  }

  /**
   * get all classifieds for this instance
   * @return a collection of ClassifiedDetail
   */
  public Collection<ClassifiedDetail> getAllClassifieds() {
    Collection<ClassifiedDetail> classifieds = new ArrayList<ClassifiedDetail>();
    classifieds = getClassifiedService().getAllClassifieds(getComponentId());
    return classifieds;
  }

  /**
   * get the number of classifieds for this instance
   * @return number : String
   */
  public String getNbTotalClassifieds() {
    return getClassifiedService().getNbTotalClassifieds(getComponentId());
  }

  /**
   * search all classifieds corresponding to the query
   * @param query : QueryDescription
   * @return a collection of ClassifiedDetail
   */
  public Collection<ClassifiedDetail> search(QueryDescription query) {
    Collection<ClassifiedDetail> result = new ArrayList<ClassifiedDetail>();
    query.setSearchingUser(getUserId());
    query.addComponent(getComponentId());
    result = getClassifiedService().search(query);
    return result;
  }

  /**
   * get all classifieds for the current user and this instance
   * @return a collection of ClassifiedDetail
   */
  public Collection<ClassifiedDetail> getClassifiedsByUser() {
    Collection<ClassifiedDetail> classifieds = new ArrayList<ClassifiedDetail>();
    classifieds = getClassifiedService().getClassifiedsByUser(getComponentId(), getUserId());
    return classifieds;
  }

  /**
   * get all classifieds to validate for this instance
   * @return a collection of ClassifiedDetail
   */
  public Collection<ClassifiedDetail> getClassifiedsToValidate() {
    Collection<ClassifiedDetail> classifieds = new ArrayList<ClassifiedDetail>();
    classifieds = getClassifiedService().getClassifiedsToValidate(getComponentId());
    return classifieds;
  }

  /**
   * get all comments for the classified corresponding to classifiedId
   * @param classifiedId : String
   * @return
   */
  private Collection<Comment> getAllComments(String classifiedId) {
    CommentPK foreign_pk = new CommentPK(classifiedId, getComponentId());
    return getCommentService().getAllCommentsOnPublication(ClassifiedDetail.getResourceType(),
        foreign_pk);
  }

  /**
   * take out draft mode the classified corresponding to classified
   * @param classifiedId : String
   * @param profile : String
   * @throws RemoteException
   * @throws PublicationTemplateException
   * @throws FormException
   */
  public synchronized void draftOutClassified(String classifiedId, ClassifiedsRole highestRole)
          throws RemoteException, PublicationTemplateException, FormException {
    getClassifiedService().draftOutClassified(classifiedId, highestRole.toString());
    if (highestRole == ClassifiedsRole.MANAGER) {
      sendSubscriptionsNotification(classifiedId);
    }
  }

  /**
   * pass the classified corresponding to classifiedId in draft mode
   * @param classifiedId : String
   * @throws RemoteException
   */
  public synchronized void draftInClassified(String classifiedId) throws RemoteException {
    getClassifiedService().draftInClassified(classifiedId);
  }

  /**
   * pass to status validate because the user corresponding to userId validated the classified
   * corresponding to classifiedId
   * @param classifiedId : String
   * @throws RemoteException
   * @throws PublicationTemplateException
   * @throws FormException
   */
  public synchronized void validateClassified(String classifiedId) throws RemoteException,
          PublicationTemplateException, FormException {
    getClassifiedService().validateClassified(classifiedId, getUserId());
    sendSubscriptionsNotification(classifiedId);
  }

  /**
   * pass to status refused because the user corresponding to userId refused the classified
   * corresponding to classifiedId for the motive ResusalMotive
   * @param classifiedId : String
   * @param motive : String
   * @throws RemoteException
   */
  public synchronized void refusedClassified(String classifiedId, String motive)
          throws RemoteException {
    getClassifiedService().refusedClassified(classifiedId, getUserId(), motive);
  }

  /**
   * get the name of xmlForm
   * @return name : String
   */
  public String getXMLFormName() {
    return getComponentParameterValue("XMLFormName");
  }

  /**
   * get the name of search field1
   * @return search field1 name : String
   */
  public String getSearchFields1() {
    return getComponentParameterValue("searchFields1");
  }

  /**
   * get the name of search field2
   * @return search field2 name : String
   */
  public String getSearchFields2() {
    return getComponentParameterValue("searchFields2");
  }

  /**
   * create classified
   * @param classified : classifiedDetail
   * @param profile : String
   * @return classifiedId : String
   */
  public synchronized String createClassified(ClassifiedDetail classified, ClassifiedsRole profile) {
    UserDetail user = getUserDetail();
    classified.setCreatorId(getUserId());
    classified.setCreationDate(new Date());
    classified.setCreatorName(user.getDisplayedName());
    classified.setCreatorEmail(user.geteMail());
    classified.setInstanceId(getComponentId());
    // status
    if (isDraftEnabled()) {
      classified.setStatus(ClassifiedDetail.DRAFT);
    } else {
      if (profile == ClassifiedsRole.MANAGER || !isValidationEnabled()) {
        classified.setStatus(ClassifiedDetail.VALID);
      } else {
        classified.setStatus(ClassifiedDetail.TO_VALIDATE);
      }
    }
    return getClassifiedService().createClassified(classified);
  }

  /**
   * delete the classified corresponding to classifiedId
   * @param classifiedId : String
   */
  public void deleteClassified(String classifiedId) {
    getClassifiedService().deleteClassified(classifiedId);
    // supprimer les commentaires
    Collection<Comment> comments = getAllComments(classifiedId);
    Iterator<Comment> it = comments.iterator();
    while (it.hasNext()) {
      Comment comment = it.next();
      CommentPK commentPK = comment.getCommentPK();
      getCommentService().deleteComment(commentPK);
    }
  }

  /**
   * update the classified and send notification if isUpdate is true and, if isAdmin is true and
   * classified is in status valid
   * @param classified : ClassifiedDetail
   * @param isUpdate : boolean
   * @param isAdmin : boolean
   */
  public synchronized void updateClassified(ClassifiedDetail classified, boolean isUpdate,
          boolean isAdmin) {
    try {
      boolean notify = false;
      if (isUpdate) {
        classified.setUpdateDate(new Date());
        // That's a real update
        if (isDraftEnabled()) {
          if (classified.getStatus().equals(ClassifiedDetail.VALID)) {
            notify = true;
          }
          if (!isAdmin && isValidationEnabled() && classified.getStatus().equals(
                  ClassifiedDetail.VALID)) {
            classified.setStatus(ClassifiedDetail.TO_VALIDATE);
          }
        }

        // special case : status is UNPUBLISHED, user requested classified republication
        if (classified.getStatus().equals(ClassifiedDetail.UNPUBLISHED)) {
          if (!isAdmin && isValidationEnabled()) {
            classified.setStatus(ClassifiedDetail.TO_VALIDATE);
          } else {
            classified.setStatus(ClassifiedDetail.VALID);
          }
        }
      }
      getClassifiedService().updateClassified(classified, notify);

      // for newly created classifieds by admin : need to force notification
      if (!isUpdate && classified.getStatus().equals(ClassifiedDetail.VALID)) {
        sendSubscriptionsNotification(Integer.toString(classified.getClassifiedId()));
      }

    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifedsSessionController.updateClassified()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /**
   * send a notification for subscribers when classified (corresponding to classifiedId) is modified
   * @param classifiedId : String
   * @throws PublicationTemplateException
   * @throws FormException
   */
  public void sendSubscriptionsNotification(String classifiedId)
          throws PublicationTemplateException, FormException {
    ClassifiedDetail classified = getClassified(classifiedId);
    DataRecord data = null;
    String xmlFormName = getXMLFormName();
    if (StringUtil.isDefined(xmlFormName)) {
      String xmlFormShortName =
              xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
      PublicationTemplateImpl pubTemplate =
              (PublicationTemplateImpl) getPublicationTemplateManager().getPublicationTemplate(
              getComponentId() + ":" + xmlFormShortName, xmlFormName);
      if (pubTemplate != null) {
        RecordSet recordSet = pubTemplate.getRecordSet();
        data = recordSet.getRecord(classifiedId);
      }
    }
    String field1 = (data.getField(getSearchFields1())).getValue();
    String field2 = (data.getField(getSearchFields2())).getValue();

    getClassifiedService().sendSubscriptionsNotification(field1, field2, classified);
  }

  /**
   * create a subscription
   * @param subscribe : Subscribe
   */
  public synchronized void createSubscribe(Subscribe subscribe) {
    try {
      subscribe.setUserId(getUserId());
      subscribe.setInstanceId(getComponentId());
      // ajouter les libellés des zones du formulaire
      if (fields1 == null) {
        fields1 = createListField(getSearchFields1());
      }
      if (fields2 == null) {
        fields2 = createListField(getSearchFields2());
      }
      subscribe.setFieldName1(fields1.get(subscribe.getField1()));
      subscribe.setFieldName2(fields2.get(subscribe.getField2()));
      getClassifiedService().createSubscribe(subscribe);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifedsSessionController.createSubscribe()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /**
   * delete the subscription corresponding to subscribeId
   * @param subscribeId : String
   */
  public void deleteSubscribe(String subscribeId) {
    getClassifiedService().deleteSubscribe(subscribeId);
  }

  /**
   * create a map of fields corresponding to listName
   * @param listName : String
   * @return a Hashtable of <String, String>
   */
  private Map<String, String> createListField(String listName) {
    Map<String, String> fields = Collections.synchronizedMap(new HashMap<String, String>());
    if (StringUtil.isDefined(listName)) {
      // création de la hashtable (key,value)
      String xmlFormName = getXMLFormName();
      if (StringUtil.isDefined(xmlFormName)) {
        String xmlFormShortName =
                xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
        PublicationTemplateImpl pubTemplate;
        try {
          pubTemplate =
                  (PublicationTemplateImpl) getPublicationTemplateManager().getPublicationTemplate(
                  getComponentId() + ":" + xmlFormShortName, xmlFormName);
          String key =
                  pubTemplate.getRecordTemplate().getFieldTemplate(listName).getParameters(
                  getLanguage()).get("keys");
          String value =
                  pubTemplate.getRecordTemplate().getFieldTemplate(listName).getParameters(
                  getLanguage()).get("values");
          String[] keys = key.split("##");
          String[] values = value.split("##");
          for (int i = 0; i < keys.length; i++) {
            fields.put(keys[i], values[i]);
          }
        } catch (Exception e) {
          // ERREUR : le champ de recherche renseigné n'est pas une liste déroulante
          throw new ClassifiedsRuntimeException("ClassifedsSessionController.createListField()",
                  SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
        }
      }
    } else {
      // ERREUR : le champs de recherche n'est pas renseigné
    }
    return fields;
  }

  /**
   * get all subscriptions for the current user and this instance
   * @return a collection of Subscribe
   */
  public Collection<Subscribe> getSubscribesByUser() {
    Collection<Subscribe> subscribes = new ArrayList<Subscribe>();
    try {
      subscribes = getClassifiedService().getSubscribesByUser(getComponentId(), getUserId());

      Iterator<Subscribe> it = subscribes.iterator();
      if (fields1 == null) {
        fields1 = createListField(getSearchFields1());
      }
      if (fields2 == null) {
        fields2 = createListField(getSearchFields2());
      }
      while (it.hasNext()) {
        Subscribe subscribe = it.next();
        // ajout des libellés
        subscribe.setFieldName1(fields1.get(subscribe.getField1()));
        subscribe.setFieldName2(fields2.get(subscribe.getField2()));
      }
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifedsSessionController.getSubscribesByUser()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return subscribes;
  }

  public void setIndexOfFirstItemToDisplay(String index) {
    this.indexOfFirstItemToDisplay = new Integer(index).intValue();
  }

  public int getIndexOfFirstItemToDisplay() {
    return indexOfFirstItemToDisplay;
  }

  /**
   * return true if draft mode is enabled
   * @return boolean
   */
  public boolean isDraftEnabled() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("draft"));
  }

  public boolean isValidationEnabled() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("validation"));
  }

  private ClassifiedService getClassifiedService() {
    if (classifiedService == null) {
      classifiedService = ClassifiedServiceFactory.getFactory().getClassifiedService();
    }
    return classifiedService;
  }

  /**
   * Gets a service providing operations on comments.
   * @return a DefaultCommentService instance.
   */
  private CommentService getCommentService() {
    if (commentService == null) {
      commentService = CommentServiceFactory.getFactory().getCommentService();
    }
    return commentService;
  }

  /**
   * Gets an instance of PublicationTemplateManager.
   * @return an instance of PublicationTemplateManager.
   */
  private PublicationTemplateManager getPublicationTemplateManager() {
    return PublicationTemplateManager.getInstance();
  }

  /**
   * return true if comments feature is enabled
   * @return boolean
   */
  public boolean isCommentsEnabled() {
    return !"no".equalsIgnoreCase(getComponentParameterValue("comments"));
  }

  /**
   * return true if wysiwyg header feature is enabled
   * @return boolean
   */
  public boolean isWysiwygHeaderEnabled() {
    return "yes".equals(getComponentParameterValue("wysiwygHeader").toLowerCase());
  }

  /**
   * return wysiwyg header html code
   *
   * @return wysiwyg header html code
   */
  public String getWysiwygHeader() {
    if (isWysiwygHeaderEnabled()) {
        return WysiwygController.load(getComponentId(), "Node_0", getLanguage());
    }
    return "";
  }
}