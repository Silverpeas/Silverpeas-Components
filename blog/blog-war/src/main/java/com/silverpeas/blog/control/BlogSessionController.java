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
package com.silverpeas.blog.control;

import java.io.IOException;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;

import static com.silverpeas.pdc.model.PdcClassification.aPdcClassificationOfContent;

import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.silverpeas.node.web.NodeEntity;
import org.silverpeas.search.indexEngine.model.IndexManager;

import com.silverpeas.blog.model.Archive;
import com.silverpeas.blog.model.BlogRuntimeException;
import com.silverpeas.blog.model.Category;
import com.silverpeas.blog.model.PostDetail;
import com.silverpeas.blog.notification.BlogUserNotification;
import com.silverpeas.comment.CommentRuntimeException;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.model.CommentPK;
import com.silverpeas.comment.service.CommentService;
import com.silverpeas.comment.service.CommentServiceFactory;
import com.silverpeas.myLinks.ejb.MyLinksBm;
import com.silverpeas.myLinks.ejb.MyLinksBmHome;
import com.silverpeas.myLinks.model.LinkDetail;
import com.silverpeas.notification.builder.helper.UserNotificationHelper;
import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.model.PdcPosition;
import com.silverpeas.pdc.web.PdcClassificationEntity;
import com.stratelia.silverpeas.alertUser.AlertUser;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

public class BlogSessionController extends AbstractComponentSessionController {

  private Calendar currentBeginDate = Calendar.getInstance(); // format = yyyy/MM/ddd
  private Calendar currentEndDate = Calendar.getInstance(); // format = yyyy/MM/ddd
  private String serverURL = null;

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public BlogSessionController(MainSessionController mainSessionCtrl,
          ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, "org.silverpeas.blog.multilang.blogBundle",
            "org.silverpeas.blog.settings.blogIcons");
    AdminController admin = new AdminController("useless");
    Domain defaultDomain = admin.getDomain(getUserDetail().getDomainId());
    serverURL = defaultDomain.getSilverpeasServerURL();
  }

  public Collection<PostDetail> lastPosts() {
    // mettre à jour les variables currentBeginDate et currentEndDate
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    setMonthFirstDay(calendar);
    setMonthLastDay(calendar);

    // return getBlogBm().getLastPosts(getComponentId());
    return getBlogService().getAllPosts(getComponentId(), 10);
  }

  private void setMonthFirstDay(Calendar calendar) {
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    currentBeginDate.setTime(calendar.getTime());
  }

  private void setMonthLastDay(Calendar calendar) {
    int monthLastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    calendar.set(Calendar.DAY_OF_MONTH, monthLastDay);
    currentEndDate.setTime(calendar.getTime());
  }

  public Collection<PostDetail> postsByCategory(String categoryId) {
    // rechercher les billets de la catégorie
    if (categoryId.equals("0")) {
      // on veux arriver sur l'accueil
      return lastPosts();
    } else {
      return getBlogService().getPostsByCategory(categoryId, getComponentId());
    }
  }

  public Collection<PostDetail> postsByArchive(String theBeginDate, String theEndDate) {
    String beginDate = theBeginDate;
    String endDate = theEndDate;
    if (endDate == null || endDate.length() == 0 || "null".equals(endDate)) {
      beginDate = getCurrentBeginDateAsString();
      endDate = getCurrentEndDateAsString();
    } else {
      setCurrentBeginDate(beginDate);
      setCurrentEndDate(endDate);
    }
    return getBlogService().getPostsByArchive(beginDate, endDate, getComponentId());
  }

  public Collection<PostDetail> postsByDate(String date) {
    return getBlogService().getPostsByDate(date, getComponentId());
  }

  public PostDetail getPost(String postId) {
    // rechercher la publication associé au billet
    PostDetail post = getBlogService().getContentById(postId);

    // mettre à jour les dates de début et de fin en fonction de la date du post
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(post.getPublication().getCreationDate());
    setMonthFirstDay(calendar);
    setMonthLastDay(calendar);

    return post;
  }

  public synchronized String createPost(String title, String categoryId, Date dateEvent,
      PdcClassificationEntity classification) {
    PublicationDetail pub =
            new PublicationDetail("X", title, "", null, null, null, null, "1", null, null, "");
    pub.getPK().setComponentName(getComponentId());
    pub.setCreatorId(getUserId());
    pub.setCreatorName(getUserDetail(getUserId()).getDisplayedName());
    pub.setCreationDate(new Date());
    pub.setIndexOperation(IndexManager.NONE);
    
    PostDetail newPost = new PostDetail(pub, categoryId, dateEvent);
    
    // creating post
    if (classification.isUndefined()) {
      return getBlogService().createPost(newPost);
    } else {
      List<PdcPosition> pdcPositions = classification.getPdcPositions();
      PdcClassification withClassification = aPdcClassificationOfContent("unknown",
            getComponentId()).withPositions(pdcPositions);
      return getBlogService().createPost(newPost, withClassification);
    }
  }

  public synchronized void updatePost(String postId, String title, String categoryId) {
    updatePost(postId, title, categoryId, new Date());
  }

  public synchronized void updatePost(String postId, String title, String categoryId, Date dateEvent) {
    PostDetail post = getPost(postId);
    PublicationDetail pub = post.getPublication();
    pub.setName(title);
    pub.setUpdaterId(getUserId());

    if (PublicationDetail.DRAFT.equals(pub.getStatus())) {
      pub.setIndexOperation(IndexManager.NONE);
    }

    post.setCategoryId(categoryId);
    post.setDateEvent(dateEvent);

    // modification du billet
    getBlogService().updatePost(post);
  }

  public synchronized void draftOutPost(String postId) {
    PostDetail post = getPost(postId);
    getBlogService().draftOutPost(post);

  }

  public String initAlertUser(String postId) throws RemoteException {
    AlertUser sel = getAlertUser();
    // Initialisation de AlertUser
    sel.resetAll();
    sel.setHostSpaceName(getSpaceLabel()); // set nom de l'espace pour browsebar
    sel.setHostComponentId(getComponentId()); // set id du composant pour appel selectionPeas (extra
    // param permettant de filtrer les users ayant acces
    // au composant)
    PairObject hostComponentName = new PairObject(getComponentLabel(), null); // set nom du
    // composant pour
    // browsebar
    // (PairObject(nom_composant,
    // lien_vers_composant))
    // NB : seul le 1er
    // element est
    // actuellement
    // utilisé
    // (alertUserPeas est
    // toujours présenté
    // en popup => pas de
    // lien sur nom du
    // composant)
    sel.setHostComponentName(hostComponentName);
    SilverTrace.debug("blog", "BlogSessionController.initAlertUser()", "root.MSG_GEN_PARAM_VALUE",
            "name = " + hostComponentName + " componentId=" + getComponentId());
    sel.setNotificationMetaData(getAlertNotificationMetaData(postId)); // set NotificationMetaData
    // contenant les informations
    // à notifier
    // fin initialisation de AlertUser
    // l'url de nav vers alertUserPeas et demandée à AlertUser et retournée
    return AlertUser.getAlertUserURL();
  }

  private synchronized NotificationMetaData getAlertNotificationMetaData(String postId)
      throws RemoteException {
    return UserNotificationHelper.build(new BlogUserNotification(getComponentId(), getPost(postId), getUserDetail()));
  }

  public synchronized void deletePost(String postId) {
    getBlogService().deletePost(postId, getComponentId());
    // supprimer les commentaires
    Collection<Comment> comments = getAllComments(postId);
    for (Comment comment : comments) {
      CommentPK commentPK = comment.getCommentPK();
      getCommentService().deleteComment(commentPK);
    }
  }

  public Collection<Comment> getAllComments(String postId) {
    CommentPK foreign_pk = new CommentPK(postId, null, getComponentId());
    return getCommentService()
        .getAllCommentsOnPublication(PostDetail.getResourceType(), foreign_pk);
  }

  public Comment getComment(String commentId) {
    CommentPK commentPK = new CommentPK(commentId);
    return getCommentService().getComment(commentPK);
  }

  public Collection<NodeDetail> getAllCategories() {
    return getBlogService().getAllCategories(getComponentId());
  }

  public Category getCategory(String categoryId) {
    // rechercher la catégorie
    NodePK nodePK = new NodePK(categoryId, getComponentId());
    return getBlogService().getCategory(nodePK);
  }

  public synchronized void createCategory(Category category) {
    category.setCreationDate(DateUtil.date2SQLDate(new Date()));
    category.setCreatorId(getUserId());
    category.getNodePK().setComponentName(getComponentId());

    getBlogService().createCategory(category);
  }

  public synchronized void deleteCategory(String categoryId) {
    getBlogService().deleteCategory(categoryId, getComponentId());
  }

  public synchronized void updateCategory(Category category) {
    getBlogService().updateCategory(category);
  }

  public Collection<Archive> getAllArchives() {
    return getBlogService().getAllArchives(getComponentId());
  }

  public Collection<LinkDetail> getAllLinks() {
    try {
      return getMyLinksBm().getAllLinksByInstance(getComponentId());
    } catch (RemoteException e) {
      throw new BlogRuntimeException("BlogSessionController.getAllLinks()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public Collection<PostDetail> getResultSearch(String word) {
    SilverTrace.info("blog", "BlogSessionController.getResultSearch()",
            "root.MSG_GEN_PARAM_VALUE", "word =" + word);
    return getBlogService().getResultSearch(word, getUserId(), getSpaceId(), getComponentId());
  }

  public synchronized void addSubscription(String topicId) throws RemoteException {
    getBlogService().addSubscription(new NodePK(topicId, getSpaceId(), getComponentId()),
            getUserId());
  }

  private boolean isUseRss() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("rss"));
  }

  @Override
  public String getRSSUrl() {
    if (isUseRss()) {
      return super.getRSSUrl().replaceAll("&", "&amp;"); //replace to remove when all composants will be XHTML compliant
    }
    return null;
  }

  public Boolean isPdcUsed() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("usePdc"));
  }

  public Boolean isDraftVisible() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("draftVisible"));
  }

  /**
   * Gets a DefaultCommentService instance.
   * @return a DefaultCommentService instance.
   */
  protected CommentService getCommentService() {
    return CommentServiceFactory.getFactory().getCommentService();
  }

  public MyLinksBm getMyLinksBm() {
    MyLinksBm myLinksBm = null;
    {
      try {
        MyLinksBmHome myLinksHome =
                (MyLinksBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.MYLINKSBM_EJBHOME,
                MyLinksBmHome.class);
        myLinksBm = myLinksHome.create();
      } catch (Exception e) {
        throw new CommentRuntimeException("BlogSessionController.getMyLinksBm()",
                SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }

    return myLinksBm;
  }

  private BlogService getBlogService() {
    return BlogServiceFactory.getFactory().getBlogService();
  }

  public void setCurrentBeginDate(String beginDate) {
    try {
      this.currentBeginDate.setTime(DateUtil.parse(beginDate));
    } catch (ParseException e) {
      throw new BlogRuntimeException("BlogSessionController.setCurrentBeginDate()",
              SilverpeasRuntimeException.ERROR, "blog.DATE_FORMAT_ERROR", e);
    }
  }

  public void setCurrentEndDate(String endDate) {
    try {
      this.currentEndDate.setTime(DateUtil.parse(endDate));
    } catch (ParseException e) {
      throw new BlogRuntimeException("BlogSessionController.setCurrentEndDate()",
              SilverpeasRuntimeException.ERROR, "blog.DATE_FORMAT_ERROR", e);
    }
  }

  public String getCurrentBeginDateAsString() {
    return DateUtil.date2SQLDate(currentBeginDate.getTime());
  }

  public String getCurrentEndDateAsString() {
    return DateUtil.date2SQLDate(currentEndDate.getTime());
  }

  public Date getDateEvent(String pubId) throws RemoteException {
    return getBlogService().getDateEvent(pubId);
  }

  public void nextMonth() {
    currentBeginDate.add(Calendar.MONTH, 1);
    currentBeginDate.set(Calendar.DATE, 1);

    currentEndDate.add(Calendar.MONTH, 1);
    currentEndDate.set(Calendar.DAY_OF_MONTH, currentEndDate.getActualMaximum(Calendar.DAY_OF_MONTH));
  }

  public void previousMonth() {
    currentBeginDate.add(Calendar.MONTH, -1);
    currentBeginDate.set(Calendar.DATE, 1);

    currentEndDate.add(Calendar.MONTH, -1);
    currentEndDate.set(Calendar.DAY_OF_MONTH, currentEndDate.getActualMaximum(Calendar.DAY_OF_MONTH));
  }

  public String getServerURL() {
    return serverURL;
  }
  
  /**
  * Converts the list of Delegated News into its JSON representation.
  * @return a JSON representation of the list of Delegated News (as string)
  * @throws JAXBException
  */
    public String getListNodeJSON(Collection<NodeDetail> listNode)
        throws JAXBException {
      List<NodeEntity> listNodeEntity = new ArrayList<NodeEntity>();
      for (NodeDetail node : listNode) {
        NodeEntity nodeEntity =
            NodeEntity.fromNodeDetail(node, node.getNodePK().getId());
        listNodeEntity.add(nodeEntity);
      }
      return listAsJSON(listNodeEntity);
    }

    /**
  * Converts the list of Delegated News Entity into its JSON representation.
  * @param listNodeEntity
  * @return a JSON representation of the list of Delegated News Entity (as string)
  * @throws DelegatedNewsRuntimeException
  */
    private String listAsJSON(List<NodeEntity> listNodeEntity)
        throws BlogRuntimeException {
      NodeEntity[] entities =
          listNodeEntity.toArray(new NodeEntity[listNodeEntity.size()]);
      ObjectMapper mapper = new ObjectMapper();
      AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
      mapper.setAnnotationIntrospector(introspector);
      StringWriter writer = new StringWriter();
      try {
        mapper.writeValue(writer, entities);
      } catch (IOException ex) {
        throw new BlogRuntimeException("BlogSessionController.listAsJSON()",
            SilverpeasRuntimeException.ERROR,
            "root.EX_NO_MESSAGE", ex);
      }
      return writer.toString();
    }
}