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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.silverpeas.classifieds.model.ClassifiedsRuntimeException;
import com.silverpeas.classifieds.model.Subscribe;
import com.silverpeas.comment.service.CommentService;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordSet;
import com.silverpeas.form.record.GenericFieldTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.model.CommentPK;
import com.silverpeas.comment.service.CommentServiceFactory;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import org.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.beans.admin.UserDetail;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.DocumentType;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.search.searchEngine.model.QueryDescription;

import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.viewGenerator.html.pagination.Pagination;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ClassifiedsSessionController extends AbstractComponentSessionController {

  private int currentPage = 0;
  private Map<String, String> fields1 = null;
  private Map<String, String> fields2 = null;
  private CommentService commentService = null;
  private ResourcesWrapper resources = null;
  private ClassifiedService classifiedService;
  
  private SearchContext searchContext = null;
  private List<ClassifiedDetail> sessionClassifieds = null;
  Pagination pagination = null;

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public ClassifiedsSessionController(MainSessionController mainSessionCtrl,
          ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
            "org.silverpeas.classifieds.multilang.classifiedsBundle",
            "org.silverpeas.classifieds.settings.classifiedsIcons",
            "org.silverpeas.classifieds.settings.classifiedsSettings");
    
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
        throw new ClassifiedsRuntimeException("ClassifiedsSessionController.super()",
                SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
  }
  
  public void setPagination(Pagination pagination) {
    this.pagination = pagination;
  }
  
  public Pagination getPagination() {
    return pagination;
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
  public void search(QueryDescription query) {
    sessionClassifieds = getClassifieds(query);
  }
  
  private List<ClassifiedDetail> getClassifieds(QueryDescription query) {
    query.setSearchingUser(getUserId());
    query.addComponent(getComponentId());
    return getClassifiedService().search(query);
  }
  
  public List<ClassifiedDetail> getClassifieds(QueryDescription query, int nb) {
    List<ClassifiedDetail> classifieds = getClassifieds(query);
    List<ClassifiedDetail> result = new ArrayList<ClassifiedDetail>();
    for (int i = 0; i < nb && i < classifieds.size(); i++) {
      ClassifiedDetail classified = classifieds.get(i);
      enrichClassified(classified);
      result.add(classified);
    }
    return result;
  }
  
  public Collection<ClassifiedDetail> getSessionClassifieds() {
    return sessionClassifieds;
  }
  
  public Collection<ClassifiedDetail> getPage(int itemIndex) {
    pagination.init(sessionClassifieds.size(), getSettings().getInteger("nbElementsPerPage", 10), itemIndex);
    List<ClassifiedDetail> classifieds = sessionClassifieds.subList(pagination.getFirstItemIndex(), pagination.getLastItemIndex());
    
    // enrich displayed classifieds
    for (ClassifiedDetail classified : classifieds) {
      enrichClassified(classified);
    }
    
    return classifieds;
  }
  
  private void enrichClassified(ClassifiedDetail classified) {
      setImagesToClassified(classified);
      getClassifiedService().setClassification(classified, getSearchFields1(), getSearchFields2(), getXMLFormName());
  }

  /**
   * get all classifieds for the current user and this instance
   * @return a collection of ClassifiedDetail
   */
  public Collection<ClassifiedDetail> getClassifiedsByUser() {
    return getClassifiedService().getClassifiedsByUser(getComponentId(), getUserId());
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
   * @throws PublicationTemplateException
   * @throws FormException
   */
  public synchronized void draftOutClassified(String classifiedId, ClassifiedsRole highestRole)
          throws PublicationTemplateException, FormException {
    getClassifiedService().draftOutClassified(classifiedId, highestRole.toString());
    if (highestRole == ClassifiedsRole.MANAGER) {
      sendSubscriptionsNotification(classifiedId);
    }
  }

  /**
   * pass the classified corresponding to classifiedId in draft mode
   * @param classifiedId : String
   */
  public synchronized void draftInClassified(String classifiedId) {
    getClassifiedService().draftInClassified(classifiedId);
  }

  /**
   * pass to status validate because the user corresponding to userId validated the classified
   * corresponding to classifiedId
   * @param classifiedId : String
   * @throws PublicationTemplateException
   * @throws FormException
   */
  public synchronized void validateClassified(String classifiedId) throws PublicationTemplateException, FormException {
    getClassifiedService().validateClassified(classifiedId, getUserId());
    sendSubscriptionsNotification(classifiedId);
  }

  /**
   * pass to status refused because the user corresponding to userId refused the classified
   * corresponding to classifiedId for the motive ResusalMotive
   * @param classifiedId : String
   * @param motive : String
   */
  public synchronized void refusedClassified(String classifiedId, String motive) {
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
  public synchronized String createClassified(ClassifiedDetail classified, Collection<FileItem> listImage, ClassifiedsRole profile) {
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
    String classifiedId = getClassifiedService().createClassified(classified);
    createClassifiedImages(listImage, classifiedId);
    return classifiedId;
  }

  /**
   * delete the classified corresponding to classifiedId
   * @param classifiedId : String
   */
  public void deleteClassified(String classifiedId) {
    //supprime la petite annonce et ses images
    getClassifiedService().deleteClassified(this.getComponentId(), classifiedId);
    
    //supprime les commentaires
    Collection<Comment> comments = getAllComments(classifiedId);
    for(Comment comment : comments) {
      getCommentService().deleteComment(comment.getCommentPK());
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
    PublicationTemplate pubTemplate = getPublicationTemplate();
    if (pubTemplate != null) {
      RecordSet recordSet = pubTemplate.getRecordSet();
      data = recordSet.getRecord(classifiedId);
    }
    String field1 = data.getField(getSearchFields1()).getValue();
    String field2 = data.getField(getSearchFields2()).getValue();

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
      try {
        PublicationTemplate pubTemplate = getPublicationTemplate();
        GenericFieldTemplate field = (GenericFieldTemplate) pubTemplate.getRecordTemplate().getFieldTemplate(listName);
        return field.getKeyValuePairs(getLanguage());
      } catch (Exception e) {
        // ERREUR : le champ de recherche renseigné n'est pas une liste déroulante
        throw new ClassifiedsRuntimeException("ClassifedsSessionController.createListField()",
                SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
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

      if (fields1 == null) {
        fields1 = createListField(getSearchFields1());
      }
      if (fields2 == null) {
        fields2 = createListField(getSearchFields2());
      }
      
      for (Subscribe subscribe : subscribes) {
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

  public void setCurrentPage(int currentPage) {
    this.currentPage = currentPage;
  }

  public int getCurrentPage() {
    return this.currentPage;
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
  
  /**
   * create classified image
   * @param fileImage : FileItem
   * @param classifiedId : String
   */
  public synchronized void createClassifiedImage(FileItem fileImage, String classifiedId) {
    
    try {
      // create SimpleDocumentPK with componentId
      SimpleDocumentPK sdPK = new SimpleDocumentPK(null, getComponentId());
  
      // create SimpleDocument Object
      Date creationDate = new Date();
      String fileName = FileUtil.getFilename(fileImage.getName());
      long size = fileImage.getSize();
      String mimeType = FileUtil.getMimeType(fileName);
      
      SimpleDocument sd = new SimpleDocument(sdPK, classifiedId, 0, false, new SimpleAttachment(fileName, getLanguage(), "", "", size 
              , mimeType, getUserId(), creationDate, null));
      sd.setDocumentType(DocumentType.attachment);
  
      AttachmentServiceFactory.getAttachmentService().createAttachment(sd, fileImage.getInputStream(), true);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifiedsSessionController.createClassifiedImage()",
            SilverpeasRuntimeException.ERROR, "classifieds.MSG_CLASSIFIED_IMAGE_NOT_CREATE", e);
    }
  }
  
  /**
   * create classified images
   * @param listImage : Collection de FileItem
   * @param classifiedId : String
   */
  private synchronized void createClassifiedImages(Collection<FileItem> listImage, String classifiedId) {
    for(FileItem fileImage : listImage) {
      createClassifiedImage(fileImage, classifiedId);
    }    
  }

  /**
   * get classified corresponding to classifiedId including images
   * @param classifiedId : String
   * @return classified : ClassifiedDetail
   */
  public ClassifiedDetail getClassifiedWithImages(String classifiedId) {
    ClassifiedDetail classified = getClassified(classifiedId);
    setImagesToClassified(classified);
    return classified;
  }
  
  private void setImagesToClassified(ClassifiedDetail classified) {
    if (classified != null) {
      try {
        WAPrimaryKey classifiedForeignKey =
            new SimpleDocumentPK(classified.getId(), getComponentId());
        List<SimpleDocument> listSimpleDocument =
            AttachmentServiceFactory.getAttachmentService().listDocumentsByForeignKeyAndType(
                classifiedForeignKey, DocumentType.attachment, null);
        classified.setImages(listSimpleDocument);
      } catch (Exception e) {
        throw new ClassifiedsRuntimeException(
            "ClassifiedsSessionController.setImagesToClassified()",
            SilverpeasRuntimeException.ERROR, "classifieds.MSG_ERR_GET_IMAGES", e);
      }
    }
  }
  
  /**
   * update classified image 
   * @param fileImage : FileItem
   * @param imageId : String
   * @param classifiedId : String
   */
  public void updateClassifiedImage(FileItem fileImage, String imageId, String classifiedId) {
    SimpleDocument classifiedImage = null;
    try {
      SimpleDocumentPK sdPK = new SimpleDocumentPK(imageId, getComponentId());
      classifiedImage = AttachmentServiceFactory.getAttachmentService().searchDocumentById(sdPK, null);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifiedsSessionController.updateClassifiedImage()",
        SilverpeasRuntimeException.ERROR, "classifieds.MSG_ERR_GET_IMAGE", e);
    }
    
    if(classifiedImage != null) {
      Date updateDate = new Date();
      String fileName = FileUtil.getFilename(fileImage.getName());
      long size = fileImage.getSize();
      String mimeType = FileUtil.getMimeType(fileName);
      
      classifiedImage.setDocumentType(DocumentType.attachment);
      classifiedImage.setFilename(fileName);
      classifiedImage.setLanguage(null);
      classifiedImage.setTitle("");
      classifiedImage.setDescription("");
      classifiedImage.setSize(size);
      classifiedImage.setContentType(mimeType);
      classifiedImage.setUpdatedBy(getUserId());
      classifiedImage.setUpdated(updateDate);
      
      try {
        AttachmentServiceFactory.getAttachmentService().updateAttachment(classifiedImage, fileImage.getInputStream(), true, false);
      } catch (Exception e) {
          throw new ClassifiedsRuntimeException("ClassifiedsSessionController.updateClassifiedImage()",
            SilverpeasRuntimeException.ERROR, "classifieds.MSG_CLASSIFIED_IMAGE_NOT_UPDATE", e);
      }
      
    } else {
      createClassifiedImage(fileImage, classifiedId);
    }
  }
  
  /**
   * delete classified image 
   * @param imageId : String
   */
  public void deleteClassifiedImage(String imageId) {
    SimpleDocument classifiedImage = null;
    try {
      SimpleDocumentPK sdPK = new SimpleDocumentPK(imageId, getComponentId());
      classifiedImage = AttachmentServiceFactory.getAttachmentService().searchDocumentById(sdPK, null);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifiedsSessionController.deleteClassifiedImage()",
        SilverpeasRuntimeException.ERROR, "classifieds.MSG_ERR_GET_IMAGE", e);
    }
    
    if(classifiedImage != null) {
      //delete the actual picture file in the file server and database
      AttachmentServiceFactory.getAttachmentService().deleteAttachment(classifiedImage);
    } else {
      throw new ClassifiedsRuntimeException("ClassifiedsSessionController.deleteClassifiedImage()",
          SilverpeasRuntimeException.ERROR, "classifieds.MSG_CLASSIFIED_IMAGE_NOT_DELETE", imageId+" does not exist");
    }
  }
  
  /**
   * return true if Home page displays classifieds organized by category
   * @return boolean
   */
  public boolean isHomePageDisplayCategorized() {
    String parameterHomePage = getComponentParameterValue("homePage");
    if(StringUtil.isDefined(parameterHomePage)) {
      return "0".equalsIgnoreCase(getComponentParameterValue("homePage"));
    }
    return true;
  }
  
  /**
   * get all valid classifieds
   * @param currentPage
   * @return a collection of ClassifiedDetail
   */
  public Collection<ClassifiedDetail> getAllValidClassifieds(int currentPage) {
    if (fields1 == null) {
      fields1 = createListField(getSearchFields1());
    }
    if (fields2 == null) {
      fields2 = createListField(getSearchFields2());
    }
    int nbElementsPerPage = Integer.parseInt(getResources().getSetting("nbElementsPerPage"));
    return getClassifiedService().getAllValidClassifieds(getComponentId(), fields1, fields2,
        getSearchFields1(), getSearchFields2(), currentPage, nbElementsPerPage);
  }
  
  /**
   * get the number of pages to display
   * @return number : String
   */
  public String getNbPages(String nbClassifieds) {
    int nbClassifiedsInt = Integer.parseInt(nbClassifieds);
    int nbElementsPerPage = Integer.parseInt(getResources().getSetting("nbElementsPerPage"));
    int nbPages = nbClassifiedsInt / nbElementsPerPage;
    if (nbClassifiedsInt % nbElementsPerPage != 0) {
      nbPages = nbPages + 1;
    }
    return Integer.toString(nbPages);
  }
  
  /**
   * Gets the template of the publication based on the classified XML form.
   * @param classifiedsSC the session controller.
   * @return the publication template for classifieds.
   * @throws PublicationTemplateException if an error occurs while getting the publication template.
   */
  public PublicationTemplate getPublicationTemplate() throws PublicationTemplateException {
    PublicationTemplate pubTemplate = null;
    String xmlFormName = getXMLFormName();
    if (StringUtil.isDefined(xmlFormName)) {
      String xmlFormShortName =
          xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
      pubTemplate =
          getPublicationTemplateManager().getPublicationTemplate(
              getComponentId() + ":" + xmlFormShortName, xmlFormName);
    }
    return pubTemplate;
  }
  
  public void setSearchContext(SearchContext context) {
    this.searchContext = context;
  }
  
  public SearchContext getSearchContext() {
    return searchContext;
  }
}