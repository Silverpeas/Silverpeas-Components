/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.components.classifieds.control;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.components.classifieds.ClassifiedsComponentSettings;
import org.silverpeas.components.classifieds.model.ClassifiedDetail;
import org.silverpeas.components.classifieds.model.Subscribe;
import org.silverpeas.components.classifieds.notification.ClassifiedOwnerNotification;
import org.silverpeas.components.classifieds.service.ClassifiedService;
import org.silverpeas.components.classifieds.service.ClassifiedServiceProvider;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.comment.service.CommentService;
import org.silverpeas.core.comment.service.CommentServiceProvider;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.form.record.GenericFieldTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.webcomponent.WebMessager;
import org.silverpeas.core.web.util.ListIndex;
import org.silverpeas.core.web.util.viewgenerator.html.pagination.Pagination;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ClassifiedsSessionController extends AbstractComponentSessionController {

  private static final int DEFAULT_NBITEMS_PERPAGE = 20;
  private static final int SCOPE_ALL = 0;
  private static final int SCOPE_MINE = 1;
  private static final int SCOPE_TOVALIDATE = 2;
  private static final int SCOPE_SEARCH = 3;

  private int currentFirstItemIndex = 0;
  private int nbItemsPerPage = DEFAULT_NBITEMS_PERPAGE;
  private Map<String, String> fields1 = null;
  private Map<String, String> fields2 = null;
  private transient CommentService commentService = null;
  private transient MultiSilverpeasBundle resources = null;
  private transient ClassifiedService classifiedService;

  private transient SearchContext searchContext = null;
  private List<ClassifiedDetail> sessionClassifieds = null;
  transient Pagination pagination = null;
  private ListIndex currentIndex = new ListIndex(0);
  private int currentScope = SCOPE_ALL;

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   */
  public ClassifiedsSessionController(MainSessionController mainSessionCtrl,
          ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, ClassifiedsComponentSettings.MESSAGES_PATH,
            ClassifiedsComponentSettings.ICONS_PATH,
            ClassifiedsComponentSettings.SETTINGS_PATH);

    nbItemsPerPage = getResources().getSetting("nbElementsPerPage", DEFAULT_NBITEMS_PERPAGE);

    // affectation du formulaire
    String xmlFormName = getXMLFormName();
    String xmlFormShortName;
    if (StringUtil.isDefined(xmlFormName)) {
      xmlFormShortName =
              xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName.indexOf('.'));
      try {
        getPublicationTemplateManager().addDynamicPublicationTemplate(getComponentId() + ":"
                + xmlFormShortName, xmlFormName);
      } catch (PublicationTemplateException e) {
        throw new SilverpeasRuntimeException(e);
      }
    }
  }

  public ClassifiedsComponentSettings getInstanceSettings() {
    ClassifiedsComponentSettings instanceSettings = new ClassifiedsComponentSettings();
    instanceSettings.setCommentsEnabled(isParamEnabled(ClassifiedsComponentSettings.PARAM_COMMENTS));
    instanceSettings.setPhotosAllowed(isParamEnabled(ClassifiedsComponentSettings.PARAM_PHOTOS));
    instanceSettings.setPriceAllowed(isParamEnabled(ClassifiedsComponentSettings.PARAM_PRICE));
    return instanceSettings;
  }

  private boolean isParamEnabled(String paramName) {
    return !"no".equalsIgnoreCase(getComponentParameterValue(paramName));
  }

  public void setPagination(Pagination pagination) {
    this.pagination = pagination;
  }

  /**
   * Gets the resources associated with this session controller.
   * @return all of the resources (messages, settings, icons, ...)
   */
  public synchronized MultiSilverpeasBundle getResources() {
    if (resources == null) {
      resources = new MultiSilverpeasBundle(getMultilang(), getIcon(), getSettings(), getLanguage());
    }
    return resources;
  }

  /**
   * get classified corresponding to classifiedId
   * @param classifiedId : String
   * @return classified : ClassifiedDetail
   */
  public ClassifiedDetail getClassified(String classifiedId) {
    ClassifiedDetail classified = getClassifiedService().getContributionById(classifiedId);
    classified.setCreatorName(getUserDetail(classified.getCreatorId()).getDisplayedName());
    classified.setCreatorEmail(getUserDetail(classified.getCreatorId()).geteMail());
    if (StringUtil.isDefined(classified.getValidatorId())) {
      classified.setValidatorName(getUserDetail(classified.getValidatorId()).getDisplayedName());
    }

    return classified;
  }

  /**
   * get the number of classifieds for this instance
   * @return number : String
   */
  public String getNbTotalClassifieds() {
    String nb = getClassifiedService().getNbTotalClassifieds(getComponentId());
    currentIndex.setNbItems(Integer.parseInt(nb));
    return nb;
  }

  /**
   * search all classifieds corresponding to the query
   * @param query : QueryDescription
   */
  public void search(QueryDescription query) {
    setSessionClassifieds(getClassifieds(query));
    currentIndex.setNbItems(getSessionClassifieds().size());
  }

  private List<ClassifiedDetail> getClassifieds(QueryDescription query) {
    query.setSearchingUser(getUserId());
    query.addComponent(getComponentId());
    return getClassifiedService().search(query);
  }

  public List<ClassifiedDetail> getClassifieds(QueryDescription query, int nb) {
    setCurrentScope(SCOPE_ALL);
    List<ClassifiedDetail> classifieds = getClassifieds(query);
    List<ClassifiedDetail> result = new ArrayList<>();
    for (int i = 0; i < nb && i < classifieds.size(); i++) {
      ClassifiedDetail classified = classifieds.get(i);
      enrichClassified(classified);
      result.add(classified);
    }
    return result;
  }

  public List<ClassifiedDetail> getSessionClassifieds() {
    if (sessionClassifieds == null) {
      sessionClassifieds = getAllValidClassifieds();
    }
    return sessionClassifieds;
  }

  private void setSessionClassifieds(List<ClassifiedDetail> classifieds) {
    sessionClassifieds = classifieds;
  }

  public Collection<ClassifiedDetail> getPage() {
    pagination.init(sessionClassifieds.size(), getNbPerPage(), getCurrentFirstItemIndex());
    List<ClassifiedDetail> classifieds =
        sessionClassifieds.subList(pagination.getFirstItemIndex(), pagination.getLastItemIndex());

    // enrich displayed classifieds
    for (ClassifiedDetail classified : classifieds) {
      enrichClassified(classified);
    }

    return classifieds;
  }

  private void enrichClassified(ClassifiedDetail classified) {
    setImagesToClassified(classified);
    getClassifiedService()
        .setClassification(classified, getSearchFields1(), getSearchFields2(), getXMLFormName());
  }

  /**
   * get all classifieds for the current user and this instance
   * @return a collection of ClassifiedDetail
   */
  public List<ClassifiedDetail> getClassifiedsByUser() {
    setCurrentScope(SCOPE_MINE);
    setSessionClassifieds(
        getClassifiedService().getClassifiedsByUser(getComponentId(), getUserId()));
    return sessionClassifieds;
  }

  /**
   * get all classifieds to validate for this instance
   * @return a collection of ClassifiedDetail
   */
  public Collection<ClassifiedDetail> getClassifiedsToValidate() {
    setCurrentScope(SCOPE_TOVALIDATE);
    setSessionClassifieds(getClassifiedService().getClassifiedsToValidate(getComponentId()));
    return sessionClassifieds;
  }

  /**
   * take out draft mode the classified corresponding to classified
   * @param classifiedId : String
   * @param highestRole : ClassifiedsRole
   */
  public synchronized void draftOutClassified(String classifiedId, ClassifiedsRole highestRole) {
    getClassifiedService()
        .draftOutClassified(classifiedId, highestRole.getName(), isValidationEnabled());
    if (!isValidationEnabled() &&
        (highestRole == ClassifiedsRole.PUBLISHER || highestRole == ClassifiedsRole.MANAGER)) {
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
   */
  public synchronized void validateClassified(String classifiedId) {
    getClassifiedService().validateClassified(classifiedId, getUserId());

    // remove classified from validation list
    removeCurrentClassifiedFromSession();

    // alert subscribers
    sendSubscriptionsNotification(classifiedId);
  }

  private void removeCurrentClassifiedFromSession() {
    getSessionClassifieds().remove(currentIndex.getCurrentIndex());
    currentIndex.setNbItems(getSessionClassifieds().size());
    currentIndex.setCurrentIndex(currentIndex.getPreviousIndex());
  }

  /**
   * pass to status refused because the user corresponding to userId refused the classified
   * corresponding to classifiedId for the motive ResusalMotive
   * @param classifiedId : String
   * @param motive : String
   */
  public synchronized void refusedClassified(String classifiedId, String motive) {
    getClassifiedService().refusedClassified(classifiedId, getUserId(), motive);

    // remove classified from validation list
    removeCurrentClassifiedFromSession();
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
   * @param profile : ClassifiedsRole
   * @return classifiedId : String
   */
  public synchronized String createClassified(ClassifiedDetail classified,
      Collection<FileItem> listImage, ClassifiedsRole profile, boolean publish) {
    UserDetail user = getUserDetail();
    classified.setCreatorId(getUserId());
    classified.setCreationDate(new Date());
    classified.setCreatorName(user.getDisplayedName());
    classified.setCreatorEmail(user.geteMail());
    classified.setInstanceId(getComponentId());
    // status
    if (isDraftEnabled() && !publish) {
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
    ResourceReference pk = new ResourceReference(classifiedId, getComponentId());
    getCommentService().deleteAllCommentsOnResource(ClassifiedDetail.getResourceType(), pk);
  }

  /**
   * update the classified and send notification if isUpdate is true and, if isAdmin is true and
   * classified is in status valid
   * @param classified : ClassifiedDetail
   * @param isUpdate : boolean
   * @param isAdmin : boolean
   */
  public synchronized void updateClassified(ClassifiedDetail classified, boolean isUpdate,
          boolean isAdmin, boolean publish) {
    boolean notify = false;
    if (isUpdate) {
      classified.setUpdateDate(new Date());
      notify = setStatusOnUpdate(classified, isAdmin, publish);
    }
    getClassifiedService().updateClassified(classified, notify);

    // for newly created classifieds by admin : need to force notification
    if (!isUpdate && classified.isValid()) {
      sendSubscriptionsNotification(Integer.toString(classified.getClassifiedId()));
    }
  }

  private boolean setStatusOnUpdate(ClassifiedDetail classified, boolean isAdmin, boolean publish) {
    boolean notify = false;
    if (isDraftEnabled() && classified.isDraft() && !publish) {
      // do nothing
    } else if (!isAdmin && isValidationEnabled() && !classified.isToValidate()) {
      classified.setStatus(ClassifiedDetail.TO_VALIDATE);
      notify = true;
    } else if (isAdmin || !isValidationEnabled()) {
      classified.setStatus(ClassifiedDetail.VALID);
    }
    return notify;
  }

  /**
   * send a notification for subscribers when classified (corresponding to classifiedId) is modified
   * @param classifiedId : String
   */
  private void sendSubscriptionsNotification(String classifiedId) {
    try {
      ClassifiedDetail classified = getClassified(classifiedId);
      PublicationTemplate pubTemplate = getPublicationTemplate();
      if (pubTemplate != null) {
        RecordSet recordSet = pubTemplate.getRecordSet();
        DataRecord data = recordSet.getRecord(classifiedId);
        String field1 = data.getField(getSearchFields1()).getValue();
        String field2 = data.getField(getSearchFields2()).getValue();
        getClassifiedService().sendSubscriptionsNotification(field1, field2, classified);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error("Can't send subscriptions notifications", e);
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
      throw new SilverpeasRuntimeException(e);
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
    Map<String, String> fields = Collections.synchronizedMap(new HashMap<>());
    if (StringUtil.isDefined(listName)) {
      // création de la hashtable (key,value)
      try {
        PublicationTemplate pubTemplate = getPublicationTemplate();
        if (pubTemplate != null) {
          GenericFieldTemplate field = (GenericFieldTemplate) pubTemplate.getRecordTemplate().getFieldTemplate(listName);
          return field.getKeyValuePairs(getLanguage());
        }
      } catch (Exception e) {
        // ERREUR : le champ de recherche renseigné n'est pas une liste déroulante
        throw new SilverpeasRuntimeException("Field is not a list", e);
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
      Collection<Subscribe> subscribes =
          getClassifiedService().getSubscribesByUser(getComponentId(), getUserId());

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

      return subscribes;
  }

  public void setCurrentFirstItemIndex(String index) {
    if (StringUtil.isDefined(index)) {
      this.currentFirstItemIndex = Integer.parseInt(index);
    }
  }

  public int getCurrentFirstItemIndex() {
    return this.currentFirstItemIndex;
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
      classifiedService = ClassifiedServiceProvider.getClassifiedService();
    }
    return classifiedService;
  }

  /**
   * Gets a service providing operations on comments.
   * @return a DefaultCommentService instance.
   */
  private CommentService getCommentService() {
    if (commentService == null) {
      commentService = CommentServiceProvider.getCommentService();
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

      SimpleAttachment attachment = SimpleAttachment.builder(getLanguage())
          .setFilename(fileName)
          .setTitle("")
          .setDescription("")
          .setSize(size)
          .setContentType(mimeType)
          .setCreationData(getUserId(), creationDate)
          .build();
      SimpleDocument sd = new SimpleDocument(sdPK, classifiedId, 0, false, attachment);
      sd.setDocumentType(DocumentType.attachment);

      AttachmentServiceProvider.getAttachmentService()
          .createAttachment(sd, fileImage.getInputStream(), true);
    } catch (Exception e) {
      throw new SilverpeasRuntimeException("Can't save image", e);
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
    processIndex(classified);
    return classified;
  }

  private void setImagesToClassified(ClassifiedDetail classified) {
    if (classified != null) {
      ResourceReference classifiedForeignKey =
          new ResourceReference(classified.getId(), getComponentId());
      List<SimpleDocument> listSimpleDocument =
          AttachmentServiceProvider.getAttachmentService().listDocumentsByForeignKeyAndType(
              classifiedForeignKey, DocumentType.attachment, null);
      classified.setImages(listSimpleDocument);
    }
  }

  /**
   * update classified image
   * @param fileImage : FileItem
   * @param imageId : String
   * @param classifiedId : String
   */
  public void updateClassifiedImage(FileItem fileImage, String imageId, String classifiedId) {
    SimpleDocumentPK sdPK = new SimpleDocumentPK(imageId, getComponentId());
    SimpleDocument classifiedImage =
        AttachmentServiceProvider.getAttachmentService().searchDocumentById(sdPK, null);

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
      classifiedImage.setLastUpdateDate(updateDate);

      try {
        AttachmentServiceProvider.getAttachmentService()
            .updateAttachment(classifiedImage, fileImage.getInputStream(), true, false);
      } catch (Exception e) {
        throw new SilverpeasRuntimeException("Can't save image", e);
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
    SimpleDocumentPK sdPK = new SimpleDocumentPK(imageId, getComponentId());
    SimpleDocument classifiedImage =
        AttachmentServiceProvider.getAttachmentService().searchDocumentById(sdPK, null);

    if(classifiedImage != null) {
      //delete the actual picture file in the file server and database
      AttachmentServiceProvider.getAttachmentService().deleteAttachment(classifiedImage);
    } else {
      throw new SilverpeasRuntimeException("Image "+imageId+" does not exist");
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
   * @return a collection of ClassifiedDetail
   */
  public List<ClassifiedDetail> getAllValidClassifieds() {
    setCurrentScope(SCOPE_ALL);
    setSessionClassifieds(getClassifiedService().getAllValidClassifieds(getComponentId()));
    if (fields1 == null) {
      fields1 = createListField(getSearchFields1());
    }
    if (fields2 == null) {
      fields2 = createListField(getSearchFields2());
    }
    int nbElementsPerPage = getNbPerPage();
    return getClassifiedService().getAllValidClassifieds(getComponentId(), fields1, fields2,
        getSearchFields1(), getSearchFields2(), getCurrentFirstItemIndex(), nbElementsPerPage);
  }

  public int getNbPerPage() {
    return nbItemsPerPage;
  }

  public void setNbItemsPerPage(String nb) {
    if (StringUtil.isInteger(nb)) {
      nbItemsPerPage = Integer.parseInt(nb);
    }
  }

  /**
   * Gets the template of the publication based on the classified XML form.
   * @return the publication template for classifieds.
   * @throws PublicationTemplateException if an error occurs while getting the publication template.
   */
  public PublicationTemplate getPublicationTemplate() throws PublicationTemplateException {
    PublicationTemplate pubTemplate = null;
    String xmlFormName = getXMLFormName();
    if (StringUtil.isDefined(xmlFormName)) {
      String xmlFormShortName =
          xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName.indexOf('.'));
      pubTemplate =
          getPublicationTemplateManager().getPublicationTemplate(
              getComponentId() + ":" + xmlFormShortName, xmlFormName);
    }
    return pubTemplate;
  }

  public void setSearchContext(SearchContext context) {
    setCurrentScope(SCOPE_SEARCH);
    this.searchContext = context;
  }

  public SearchContext getSearchContext() {
    return searchContext;
  }

  public ListIndex getIndex() {
    return currentIndex;
  }

  private void processIndex(ClassifiedDetail classified) {
    currentIndex.setCurrentIndex(getSessionClassifieds().indexOf(classified));
    currentIndex.setNbItems(getSessionClassifieds().size());
  }

  public ClassifiedDetail getPrevious() {
    return getSessionClassifieds().get(currentIndex.getPreviousIndex());
  }

  public ClassifiedDetail getNext() {
    return getSessionClassifieds().get(currentIndex.getNextIndex());
  }

  public int getCurrentScope() {
    return currentScope;
  }

  private void setCurrentScope(int scope) {
    currentScope = scope;
  }

  public void checkScope(ClassifiedDetail classified) {
    if (classified.isToValidate()) {
      getClassifiedsToValidate();
      processIndex(classified);
    }
  }

  public ClassifiedDetail getCurrentClassified() {
    return getSessionClassifieds().get(currentIndex.getCurrentIndex());
  }

  public void notifyOwner(String message) {
    UserNotificationHelper.buildAndSend(new ClassifiedOwnerNotification(getCurrentClassified(),
        getUserId(), message));
    WebMessager.getInstance().addSuccess(getString("classifieds.notif.sent"));
  }
}