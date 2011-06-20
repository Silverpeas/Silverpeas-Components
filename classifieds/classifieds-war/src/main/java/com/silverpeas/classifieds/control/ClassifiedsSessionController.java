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
package com.silverpeas.classifieds.control;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.silverpeas.classifieds.control.ejb.ClassifiedsBm;
import com.silverpeas.classifieds.control.ejb.ClassifiedsBmHome;
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
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.searchEngine.model.QueryDescription;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ClassifiedsSessionController extends AbstractComponentSessionController {

  private int indexOfFirstItemToDisplay = 0;
  private Map<String, String> fields1 = createListField(getSearchFields1());
  private Map<String, String> fields2 = createListField(getSearchFields2());
  private CommentService commentService = null;
  private ResourcesWrapper resources = null;

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
  public synchronized  ResourcesWrapper getResources() {
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
    try {
      classified = getClassifiedsBm().getClassified(classifiedId);
      classified.setCreatorName(getUserDetail(classified.getCreatorId()).getDisplayedName());
      classified.setCreatorEmail(getUserDetail(classified.getCreatorId()).geteMail());
      if (StringUtil.isDefined(classified.getValidatorId())) {
        classified.setValidatorName((getUserDetail(classified.getValidatorId()).getDisplayedName()));
      }
    } catch (RemoteException e) {
      throw new ClassifiedsRuntimeException("ClassifedsSessionController.getClassified()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return classified;
  }

  /**
   * get all classifieds for this instance
   * @return a collection of ClassifiedDetail
   */
  public Collection<ClassifiedDetail> getAllClassifieds() {
    Collection<ClassifiedDetail> classifieds = new ArrayList<ClassifiedDetail>();
    try {
      classifieds = getClassifiedsBm().getAllClassifieds(getComponentId());
    } catch (RemoteException e) {
      throw new ClassifiedsRuntimeException("ClassifedsSessionController.getAllClassifieds()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return classifieds;
  }

  /**
   * get the number of classifieds for this instance
   * @return number : String
   */
  public String getNbTotalClassifieds() {
    try {
      return getClassifiedsBm().getNbTotalClassifieds(getComponentId());
    } catch (RemoteException e) {
      throw new ClassifiedsRuntimeException("ClassifedsSessionController.getAllClassifieds()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /**
   * search all classifieds corresponding to the query
   * @param query : QueryDescription
   * @return a collection of ClassifiedDetail
   */
  public Collection<ClassifiedDetail> search(QueryDescription query) {
    Collection<ClassifiedDetail> result = new ArrayList<ClassifiedDetail>();
    try {
      query.setSearchingUser(getUserId());
      query.addComponent(getComponentId());
      result = getClassifiedsBm().search(query);
      return result;
    } catch (RemoteException e) {
      throw new ClassifiedsRuntimeException("ClassifedsSessionController.search()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /**
   * get all classifieds for the current user and this instance
   * @return a collection of ClassifiedDetail
   */
  public Collection<ClassifiedDetail> getClassifiedsByUser() {
    Collection<ClassifiedDetail> classifieds = new ArrayList<ClassifiedDetail>();
    try {
      classifieds = getClassifiedsBm().getClassifiedsByUser(getComponentId(), getUserId());
    } catch (RemoteException e) {
      throw new ClassifiedsRuntimeException("ClassifedsSessionController.getClassifiedsByUser()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return classifieds;
  }

  /**
   * get all classifieds to validate for this instance
   * @return a collection of ClassifiedDetail
   */
  public Collection<ClassifiedDetail> getClassifiedsToValidate() {
    Collection<ClassifiedDetail> classifieds = new ArrayList<ClassifiedDetail>();
    try {
      classifieds = getClassifiedsBm().getClassifiedsToValidate(getComponentId());
    } catch (RemoteException e) {
      throw new ClassifiedsRuntimeException(
          "ClassifedsSessionController.getClassifiedsToValidate()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return classifieds;
  }

  /**
   * get all comments for the classified corresponding to classifiedId
   * @param classifiedId : String
   * @return
   */
  public Collection<Comment> getAllComments(String classifiedId) {
    CommentPK foreign_pk = new CommentPK(classifiedId, getComponentId());
    return getCommentService().getAllCommentsOnPublication(foreign_pk);
  }

  /**
   * add a comment (the message) to the classified corresponding to classifiedId
   * @param classifiedId : String
   * @param message : String
   */
  public synchronized void addComment(String classifiedId, String message) {
    CommentPK foreign_pk = new CommentPK(classifiedId, getComponentId());
    CommentPK pk = new CommentPK("X", getComponentId());
    Date dateToday = new Date();
    String date = DateUtil.date2SQLDate(dateToday);
    String owner = getUserDetail(getUserId()).getDisplayedName();
    SilverTrace.info("classifieds", "ClassifedsSessionController.addComment()",
        "root.MSG_GEN_PARAM_VALUE", "owner=" + owner);

    Comment comment =
        new Comment(pk, foreign_pk, Integer.parseInt(getUserId()), owner, message, date, date);
    getCommentService().createComment(comment);
    SilverTrace.info("classifieds", "ClassifedsSessionController.addComment()",
        "root.MSG_GEN_PARAM_VALUE", "owner comment=" + comment.getOwner());
  }

  /**
   * delete the comment corresponding to commentId
   * @param commentId : String
   */
  public void deleteComment(String commentId) {
    CommentPK pk = new CommentPK(commentId, "useless", getComponentId());
    getCommentService().deleteComment(pk);
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
    getClassifiedsBm().draftOutClassified(classifiedId, highestRole.toString());
    if (highestRole==ClassifiedsRole.MANAGER) {
      sendSubscriptionsNotification(classifiedId);
    }
  }

  /**
   * pass the classified corresponding to classifiedId in draft mode
   * @param classifiedId : String
   * @throws RemoteException
   */
  public synchronized void draftInClassified(String classifiedId) throws RemoteException {
    getClassifiedsBm().draftInClassified(classifiedId);
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
    getClassifiedsBm().validateClassified(classifiedId, getUserId());
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
    getClassifiedsBm().refusedClassified(classifiedId, getUserId(), motive);
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
    try {
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
        if (profile==ClassifiedsRole.MANAGER || !isValidationEnabled()) {
          classified.setStatus(ClassifiedDetail.VALID);
        } else {
          classified.setStatus(ClassifiedDetail.TO_VALIDATE);
        }
      }
      return getClassifiedsBm().createClassified(classified);
    } catch (RemoteException e) {
      throw new ClassifiedsRuntimeException("ClassifedsSessionController.createClassified()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /**
   * delete the classified corresponding to classifiedId
   * @param classifiedId : String
   */
  public void deleteClassified(String classifiedId) {
    try {
      getClassifiedsBm().deleteClassified(classifiedId);
      // supprimer les commentaires
      Collection<Comment> comments = getAllComments(classifiedId);
      Iterator<Comment> it = comments.iterator();
      while (it.hasNext()) {
        Comment comment = it.next();
        CommentPK commentPK = comment.getCommentPK();
        getCommentService().deleteComment(commentPK);
      }
    } catch (RemoteException e) {
      throw new ClassifiedsRuntimeException("ClassifedsSessionController.deleteClassified()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_DELETE_OBJECT", e);
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
          if (!isAdmin && isValidationEnabled() ) {
            classified.setStatus(ClassifiedDetail.TO_VALIDATE);
          }
          else {
            classified.setStatus(ClassifiedDetail.VALID);
          }
        }
      }
      getClassifiedsBm().updateClassified(classified, notify);

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
    try {
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

      getClassifiedsBm().sendSubscriptionsNotification(field1, field2, classified);
    } catch (RemoteException e) {
      throw new ClassifiedsRuntimeException(
          "ClassifedsSessionController.sendSubscriptionsNotification()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
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
      subscribe.setFieldName1(fields1.get(subscribe.getField1()));
      subscribe.setFieldName2(fields2.get(subscribe.getField2()));
      getClassifiedsBm().createSubscribe(subscribe);
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
    try {
      getClassifiedsBm().deleteSubscribe(subscribeId);
    } catch (RemoteException e) {
      throw new ClassifiedsRuntimeException("ClassifedsSessionController.deleteSubscribe()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /**
   * create a map of fields corresponding to listName
   * @param listName : String
   * @return a Hashtable of <String, String>
   */
  private Map<String, String> createListField(String listName) {
    Map<String, String> fields = Collections.synchronizedMap(new HashMap<String, String>());
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
        throw new ClassifiedsRuntimeException("ClassifedsSessionController.createListField()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
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
      subscribes = getClassifiedsBm().getSubscribesByUser(getComponentId(), getUserId());

      Iterator<Subscribe> it = subscribes.iterator();
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

  private ClassifiedsBm getClassifiedsBm() {
    ClassifiedsBm classifiedsBm = null;
    try {
      ClassifiedsBmHome classifiedsBmHome =
          (ClassifiedsBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.CLASSIFIEDSBM_EJBHOME,
          ClassifiedsBmHome.class);
      classifiedsBm = classifiedsBmHome.create();
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifedsSessionController.getClassifiedsBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return classifiedsBm;
  }

  /**
   * Gets a service providing operations on comments.
   * @return a DefaultCommentService instance.
   */
  public CommentService getCommentService() {
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
    return "yes".equalsIgnoreCase(getComponentParameterValue("comments"));
  }
}