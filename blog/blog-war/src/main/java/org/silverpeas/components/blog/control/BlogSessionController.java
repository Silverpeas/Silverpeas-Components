/*
 * Copyright (C) 2000 - 2018 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.blog.control;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.silverpeas.components.blog.access.BlogPostWriteAccessControl;
import org.silverpeas.components.blog.model.Archive;
import org.silverpeas.components.blog.model.BlogRuntimeException;
import org.silverpeas.components.blog.model.Category;
import org.silverpeas.components.blog.model.PostDetail;
import org.silverpeas.components.blog.notification.BlogUserAlertNotification;
import org.silverpeas.components.blog.service.BlogService;
import org.silverpeas.components.blog.service.BlogServiceFactory;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.model.CommentPK;
import org.silverpeas.core.comment.service.CommentService;
import org.silverpeas.core.comment.service.CommentServiceProvider;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.exception.EncodingException;
import org.silverpeas.core.index.indexing.model.IndexManager;
import org.silverpeas.core.mylinks.model.LinkDetail;
import org.silverpeas.core.mylinks.service.MyLinksService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.model.PdcPosition;
import org.silverpeas.core.subscription.service.ComponentSubscriptionResource;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.UtilException;
import org.silverpeas.core.util.file.FileFolderManager;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.util.AlertUser;
import org.silverpeas.core.web.subscription.SubscriptionContext;
import org.silverpeas.core.webapi.node.NodeEntity;
import org.silverpeas.core.webapi.pdc.PdcClassificationEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.AccessControlException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.silverpeas.core.pdc.pdc.model.PdcClassification.aPdcClassificationOfContent;

public final class BlogSessionController extends AbstractComponentSessionController {

  private static final int DEFAULT_POST_COUNT = 10;
  private static final String STYLES_CSS = "styles.css";
  private static final String THE_WALLPAPER_DELETION_FAILED = "The wallpaper {0} deletion failed!";
  private static final String BANNER_PNG = "banner.png";
  private static final String BANNER_GIF = "banner.gif";
  private static final String BANNER_JPG = "banner.jpg";
  private Calendar currentBeginDate = Calendar.getInstance();
  private Calendar currentEndDate = Calendar.getInstance();
  private String serverURL = null;
  private WallPaper wallPaper = null;
  private StyleSheet styleSheet = null;
  private static final String FORBIDEN_ACCESS_MSG = "blog.error.access";

  private BlogPostWriteAccessControl accessController = BlogPostWriteAccessControl.get();

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   */
  public BlogSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, "org.silverpeas.blog.multilang.blogBundle",
        "org.silverpeas.blog.settings.blogIcons");
    AdminController admin = ServiceProvider.getService(AdminController.class);
    Domain defaultDomain = admin.getDomain(getUserDetail().getDomainId());
    serverURL = defaultDomain.getSilverpeasServerURL();
    setWallPaper();
    setStyleSheet();
  }

  public void checkWriteAccessOnBlogPost() {
    if (!accessController.isUserAuthorized(getUserId(), getComponentId())) {
      String errorMsg = getMultilang().getString(FORBIDEN_ACCESS_MSG);
      throw new AccessControlException(errorMsg);
    }
  }

  public Collection<PostDetail> lastPosts() {
    // mettre à jour les variables currentBeginDate et currentEndDate
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    setMonthFirstDay(calendar);
    setMonthLastDay(calendar);

    return getBlogService().getAllPosts(getComponentId());
  }

  public Collection<PostDetail> lastValidPosts() {
    // mettre à jour les variables currentBeginDate et currentEndDate
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    setMonthFirstDay(calendar);
    setMonthLastDay(calendar);

    return getBlogService().getAllValidPosts(getComponentId(), DEFAULT_POST_COUNT);
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
    if ("0".equals(categoryId)) {
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
    checkWriteAccessOnBlogPost();
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
      PdcClassification withClassification =
          aPdcClassificationOfContent(newPost).withPositions(pdcPositions);
      return getBlogService().createPost(newPost, withClassification);
    }
  }

  public synchronized void updatePost(String postId, String title, String content,
      String categoryId, Date dateEvent) {
    checkWriteAccessOnBlogPost();
    PostDetail post = getPost(postId);

    PublicationDetail pub = post.getPublication();
    pub.setName(title);
    pub.setUpdaterId(getUserId());

    if (pub.isDraft()) {
      pub.setIndexOperation(IndexManager.NONE);
    }

    post.setCategoryId(categoryId);
    post.setDateEvent(dateEvent);
    post.setContent(content);

    // save the post
    getBlogService().updatePost(post);
  }

  public synchronized void draftOutPost(String postId) {
    checkWriteAccessOnBlogPost();
    PostDetail post = getPost(postId);
    getBlogService().draftOutPost(post);

  }

  public synchronized void updatePostAndDraftOut(String postId, String title, String content,
      String categoryId, Date dateEvent) {
    //update post
    updatePost(postId, title, content, categoryId, dateEvent);

    //draft out poste
    draftOutPost(postId);
  }

  public String initAlertUser(String postId) {
    AlertUser sel = getAlertUser();
    // Initialisation de AlertUser
    sel.resetAll();
    // set nom de l'espace pour browsebar
    sel.setHostSpaceName(getSpaceLabel());
    // set id du composant pour appel selectionPeas (extra param permettant de filtrer les users
    // ayant acces au composant)
    sel.setHostComponentId(getComponentId());
    Pair<String, String> hostComponentName = new Pair<>(getComponentLabel(), null);
    // set nom du
    // composant pour browsebar (PairObject(nom_composant, lien_vers_composant))
    // NB : seul le 1er element est actuellement utilisé (alertUserPeas est toujours présenté en
    // popup => pas de lien sur nom du composant)
    sel.setHostComponentName(hostComponentName);
    // set NotificationMetaData contenant les informations à notifier fin initialisation de
    // AlertUser l'url de nav vers alertUserPeas et demandée à AlertUser et retournée
    sel.setNotificationMetaData(getAlertNotificationMetaData(postId));
    return AlertUser.getAlertUserURL();
  }

  private synchronized NotificationMetaData getAlertNotificationMetaData(String postId) {
    return UserNotificationHelper
        .build(new BlogUserAlertNotification(getPost(postId), getUserDetail()));
  }

  public synchronized void deletePost(String postId) {
    checkWriteAccessOnBlogPost();
    getBlogService().deletePost(postId, getComponentId());
    // supprimer les commentaires
    Collection<Comment> comments = getAllComments(postId);
    for (Comment comment : comments) {
      CommentPK commentPK = comment.getCommentPK();
      getCommentService().deleteComment(commentPK);
    }
  }

  public Collection<Comment> getAllComments(String postId) {
    CommentPK foreignPk = new CommentPK(postId, null, getComponentId());
    return getCommentService().getAllCommentsOnPublication(PostDetail.getResourceType(), foreignPk);
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
    return getMyLinksBm().getAllLinksByInstance(getComponentId());
  }

  public Collection<PostDetail> getResultSearch(String word) {

    return getBlogService().getResultSearch(word, getUserId(), getComponentId());
  }

  public synchronized boolean isUserSubscribed() {
    return getBlogService().isSubscribed(getUserId(), getComponentId());
  }

  public String manageSubscriptions() {
    SubscriptionContext subscriptionContext = getSubscriptionContext();
    subscriptionContext.initialize(ComponentSubscriptionResource.from(getComponentId()));
    return subscriptionContext.getDestinationUrl();
  }

  private boolean isUseRss() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("rss"));
  }

  @Override
  public String getRSSUrl() {
    if (isUseRss()) {
      //replace to remove when all composants will be XHTML compliant
      return super.getRSSUrl()
          .replaceAll("&", "&amp;");
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
    return CommentServiceProvider.getCommentService();
  }

  public MyLinksService getMyLinksBm() {
    return ServiceProvider.getService(MyLinksService.class);
  }

  private BlogService getBlogService() {
    return BlogServiceFactory.getBlogService();
  }

  public void setCurrentBeginDate(String beginDate) {
    try {
      this.currentBeginDate.setTime(DateUtil.parse(beginDate));
    } catch (ParseException e) {
      throw new BlogRuntimeException(e);
    }
  }

  public void setCurrentEndDate(String endDate) {
    try {
      this.currentEndDate.setTime(DateUtil.parse(endDate));
    } catch (ParseException e) {
      throw new BlogRuntimeException(e);
    }
  }

  public String getCurrentBeginDateAsString() {
    return DateUtil.date2SQLDate(currentBeginDate.getTime());
  }

  public String getCurrentEndDateAsString() {
    return DateUtil.date2SQLDate(currentEndDate.getTime());
  }

  public Date getDateEvent(String pubId) {
    return getBlogService().getDateEvent(pubId);
  }

  public void nextMonth() {
    currentBeginDate.add(Calendar.MONTH, 1);
    currentBeginDate.set(Calendar.DATE, 1);

    currentEndDate.add(Calendar.MONTH, 1);
    currentEndDate.
        set(Calendar.DAY_OF_MONTH, currentEndDate.getActualMaximum(Calendar.DAY_OF_MONTH));
  }

  public void previousMonth() {
    currentBeginDate.add(Calendar.MONTH, -1);
    currentBeginDate.set(Calendar.DATE, 1);

    currentEndDate.add(Calendar.MONTH, -1);
    currentEndDate.
        set(Calendar.DAY_OF_MONTH, currentEndDate.getActualMaximum(Calendar.DAY_OF_MONTH));
  }

  public String getServerURL() {
    return serverURL;
  }

  /**
   * Converts the list of Delegated News into its JSON representation.
   * @return a JSON representation of the list of Delegated News (as string)
   */
  public String getListNodeJSON(Collection<NodeDetail> listNode) {
    List<NodeEntity> listNodeEntity = new ArrayList<>();
    for (NodeDetail node : listNode) {
      NodeEntity nodeEntity = NodeEntity.fromNodeDetail(node, node.getNodePK().getId());
      listNodeEntity.add(nodeEntity);
    }
    return listAsJSON(listNodeEntity);
  }

  /**
   * Converts the list of Delegated News Entity into its JSON representation.
   * @param listNodeEntity list of NodeEntity
   * @return a JSON representation of the list of Delegated News Entity (as string)
   * @throws BlogRuntimeException
   */
  private String listAsJSON(List<NodeEntity> listNodeEntity) {
    NodeEntity[] entities = listNodeEntity.toArray(new NodeEntity[listNodeEntity.size()]);
    try {
      return JSONCodec.encode(entities);
    } catch (EncodingException ex) {
      throw new BlogRuntimeException(ex);
    }
  }

  /**
   * Set the name, URL and size of the wallpaper file.
   */
  public void setWallPaper() {
    String path = FileRepositoryManager.getAbsolutePath(this.getComponentId());

    List<File> files;
    try {
      files = (List<File>) FileFolderManager.getAllFile(path);
    } catch (UtilException e) {
      SilverLogger.getLogger(this).silent(e);
      files = new ArrayList<>();
    }

    for (File file : files) {
      if (BANNER_GIF.equals(file.getName()) || BANNER_JPG.equals(file.getName()) ||
          BANNER_PNG.equals(file.getName())) {
        this.wallPaper = new WallPaper();
        this.wallPaper.setName(file.getName());
        this.wallPaper.setUrl(FileServerUtils
            .getOnlineURL(this.getComponentId(), file.getName(), file.getName(),
                FileUtil.getMimeType(file.getName()), ""));
        this.wallPaper.setSize(FileRepositoryManager.formatFileSize(file.length()));
        break;
      }
    }
  }

  /**
   * Get the wallpaper object.
   * @return the wallpaper object
   */
  public WallPaper getWallPaper() {
    return this.wallPaper;
  }

  /**
   * Save the banner file.
   * @throws BlogRuntimeException
   */
  public void saveWallPaperFile(FileItem fileItemWallPaper) {
    //extension
    String extension = FileRepositoryManager.getFileExtension(fileItemWallPaper.getName());
    if (extension != null && "jpeg".equalsIgnoreCase(extension)) {
      extension = "jpg";
    }

    if (extension == null ||
        (!"gif".equalsIgnoreCase(extension) && !"jpg".equalsIgnoreCase(extension) && !"png".
            equalsIgnoreCase(extension))) {
      throw new BlogRuntimeException(
          fileItemWallPaper.getName() + " wallpaper format isn't supported");
    }

    //path to create the file
    String path = FileRepositoryManager.getAbsolutePath(this.getComponentId());

    //remove all wallpapers to ensure it is unique
    removeWallPaperFile();

    try {
      String nameFile = "banner." + extension.toLowerCase();
      File fileWallPaper = new File(path, nameFile);

      //create the file
      File rootFolder = fileWallPaper.getParentFile();
      if (!rootFolder.exists()) {
        rootFolder.mkdirs();
      }
      fileItemWallPaper.write(fileWallPaper);

      //save the information
      this.wallPaper = new WallPaper();
      this.wallPaper.setName(nameFile);
      this.wallPaper.setUrl(FileServerUtils
          .getOnlineURL(this.getComponentId(), nameFile, nameFile, FileUtil.getMimeType(nameFile),
              ""));
      this.wallPaper.setSize(FileRepositoryManager.formatFileSize(fileWallPaper.length()));
    } catch (Exception ex) {
      throw new BlogRuntimeException(ex);
    }
  }

  /**
   * Remove the actual wallpaper file.
   */
  public void removeWallPaperFile() {
    String path = FileRepositoryManager.getAbsolutePath(this.getComponentId());
    File banner = new File(path, BANNER_GIF);
    try {
      Files.deleteIfExists(banner.toPath());
    } catch (IOException e) {
      SilverLogger.getLogger(this).warn(THE_WALLPAPER_DELETION_FAILED, BANNER_GIF);
    }

    banner = new File(path, BANNER_JPG);
    try {
      Files.deleteIfExists(banner.toPath());
    } catch (IOException e) {
      SilverLogger.getLogger(this).warn(THE_WALLPAPER_DELETION_FAILED, BANNER_JPG);
    }

    banner = new File(path, BANNER_PNG);
    try {
      Files.deleteIfExists(banner.toPath());
    } catch (IOException e) {
      SilverLogger.getLogger(this).warn(THE_WALLPAPER_DELETION_FAILED, BANNER_PNG);
    }

    this.wallPaper = null;
  }

  /**
   * Set the name, URL, size and content of the style sheet file.
   */
  public void setStyleSheet() {
    String path = FileRepositoryManager.getAbsolutePath(this.getComponentId());

    List<File> files;
    try {
      files = (List<File>) FileFolderManager.getAllFile(path);
    } catch (UtilException e) {
      SilverLogger.getLogger(this).silent(e);
      files = new ArrayList<>();
    }

    for (File file : files) {
      if (STYLES_CSS.equals(file.getName())) {
        this.styleSheet = new StyleSheet();
        this.styleSheet.setName(file.getName());
        this.styleSheet.setUrl(FileServerUtils
            .getOnlineURL(this.getComponentId(), file.getName(), file.getName(),
                FileUtil.getMimeType(file.getName()), ""));
        this.styleSheet.setSize(FileRepositoryManager.formatFileSize(file.length()));
        try {
          this.styleSheet.setContent(FileUtils.readFileToString(file, "UTF-8"));
        } catch (IOException e) {
          SilverLogger.getLogger(this).warn(e);
          this.styleSheet.setContent(null);
        }
        break;
      }
    }
  }

  /**
   * Get the style sheet object.
   * @return style sheet object
   */
  public StyleSheet getStyleSheet() {
    return this.styleSheet;
  }

  /**
   * Save the stylesheet file.
   * @throws BlogRuntimeException
   */
  public void saveStyleSheetFile(FileItem fileItemStyleSheet) {
    //extension
    String extension = FileRepositoryManager.getFileExtension(fileItemStyleSheet.getName());
    if (!"css".equalsIgnoreCase(extension)) {
      throw new BlogRuntimeException(
          fileItemStyleSheet.getName() + " isn't a supported stylesheet");
    }

    //path to create the file
    String path = FileRepositoryManager.getAbsolutePath(this.getComponentId());

    //remove all stylesheet to ensure it is unique
    removeStyleSheetFile();

    try {
      String nameFile = STYLES_CSS;
      File fileStyleSheet = new File(path, nameFile);

      //create the file
      File rootFolder = fileStyleSheet.getParentFile();
      if (!rootFolder.exists()) {
        rootFolder.mkdirs();
      }
      fileItemStyleSheet.write(fileStyleSheet);

      //save the information
      this.styleSheet = new StyleSheet();
      this.styleSheet.setName(nameFile);
      this.styleSheet.setUrl(FileServerUtils
          .getOnlineURL(this.getComponentId(), nameFile, nameFile, FileUtil.getMimeType(nameFile),
              ""));
      this.styleSheet.setSize(FileRepositoryManager.formatFileSize(fileStyleSheet.length()));
      setStylesheetContent(fileStyleSheet);

    } catch (Exception ex) {
      throw new BlogRuntimeException(ex);
    }
  }

  private void setStylesheetContent(final File fileStyleSheet) {
    try {
      this.styleSheet.setContent(FileUtils.readFileToString(fileStyleSheet, "UTF-8"));
    } catch (IOException e) {
      SilverLogger.getLogger(this).warn(e);
      this.styleSheet.setContent(null);
    }
  }

  /**
   * Remove the actual style sheet file.
   */
  public void removeStyleSheetFile() {
    String path = FileRepositoryManager.getAbsolutePath(this.getComponentId());
    File styles = new File(path, STYLES_CSS);
    try {
      Files.deleteIfExists(styles.toPath());
    } catch (IOException e) {
      SilverLogger.getLogger(this).warn(e);
    }

    this.styleSheet = null;
  }
}
