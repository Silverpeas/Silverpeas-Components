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
package com.silverpeas.blog.control;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.silverpeas.search.SearchEngineFactory;

import com.silverpeas.blog.BlogContentManager;
import com.silverpeas.blog.dao.PostDAO;
import com.silverpeas.blog.model.Archive;
import com.silverpeas.blog.model.BlogRuntimeException;
import com.silverpeas.blog.model.Category;
import com.silverpeas.blog.model.PostDetail;
import com.silverpeas.blog.notification.BlogUserNotification;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.model.CommentPK;
import com.silverpeas.comment.service.CommentService;
import com.silverpeas.comment.service.notification.CommentUserNotificationService;
import com.silverpeas.notification.builder.helper.UserNotificationHelper;
import com.silverpeas.pdc.PdcServiceFactory;
import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.service.PdcClassificationService;
import com.silverpeas.subscribe.Subscription;
import com.silverpeas.subscribe.SubscriptionService;
import com.silverpeas.subscribe.SubscriptionServiceFactory;
import com.silverpeas.subscribe.service.NodeSubscription;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.beans.admin.ObjectType;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import org.silverpeas.search.searchEngine.model.MatchingIndexEntry;
import org.silverpeas.search.searchEngine.model.QueryDescription;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;
import org.silverpeas.search.indexEngine.model.IndexManager;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.publication.model.PublicationRuntimeException;

/**
 * Default implementation of the services provided by the Blog component. It is managed by the underlying IoC container.
 * At initialization by the IoC container, it registers itself among different services for which it is interested.
 */
@Named("blogService")
public class DefaultBlogService implements BlogService {

  public static final String COMPONENT_NAME = "blog";
  private static final String MESSAGES_PATH = "org.silverpeas.blog.multilang.blogBundle";
  private static final String SETTINGS_PATH = "org.silverpeas.blog.settings.blogSettings";
  private static final ResourceLocator settings = new ResourceLocator(SETTINGS_PATH, "");

  @Inject
  private CommentUserNotificationService commentUserNotificationService;

  @Inject
  private CommentService commentService;

  /**
   * Initializes this service by registering itself among Silverpeas core services as interested by events.
   */
  @PostConstruct
  public void initialize() {
    commentUserNotificationService.register(COMPONENT_NAME, this);
  }

  /**
   * Releases all the resources required by this service. For instance, it unregisters from the Silverpeas core
   * services.
   */
  @PreDestroy
  public void release() {
    commentUserNotificationService.unregister(COMPONENT_NAME);
  }

  @Override
  public PostDetail getContentById(String contentId) {
    PublicationDetail publication;
    try {
      publication = getPublicationBm().getDetail(new PublicationPK(contentId));
    } catch (RemoteException e) {
      throw new BlogRuntimeException(getClass().getSimpleName() + ".getContentById()",
          SilverpeasRuntimeException.ERROR, "root.EX_RECORD_NOT_FOUND", "id = " + contentId, e);
    }
    return getPost(publication);
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
  public Date getDateEvent(String pubId) {
    Connection con = openConnection();
    try {
      return PostDAO.getDateEvent(con, pubId);
    } catch (Exception e) {
      throw new BlogRuntimeException(getClass().getSimpleName() + ".getDateEvent()",
          SilverpeasRuntimeException.ERROR, "post.MSG_POST_NOT_CREATE", e);
    } finally {
      closeConnection(con);
    }
  }

  @Override
  public String createPost(PostDetail post) {
    return createPost(post, null);
  }
  
  @Override
  public String createPost(final PostDetail post, PdcClassification classification) {
    Connection con = openConnection();
    try {
      PublicationDetail pub = post.getPublication();
      pub.setStatus(PublicationDetail.DRAFT);
      PublicationPK pk = getPublicationBm().createPublication(pub);
      PostDAO.createDateEvent(con, pk.getId(), post.getDateEvent(), pk.getInstanceId());
      if (StringUtil.isDefined(post.getCategoryId())) {
        setCategory(pk, post.getCategoryId());
      }
      createSilverContent(con, pub, pub.getCreatorId());
      
      // classify the publication on the PdC if its classification is defined
      if (classification != null && !classification.isEmpty()) {
        PdcClassificationService service = PdcServiceFactory.getFactory().
                getPdcClassificationService();
        classification.ofContent(pk.getId());
        service.classifyContent(pub, classification);
      }
      
      return pk.getId();
    } catch (Exception e) {
      throw new BlogRuntimeException("BlogBmEJB.createPost()", SilverpeasRuntimeException.ERROR,
          "post.MSG_POST_NOT_CREATE", e);
    } finally {
      closeConnection(con);
    }
  }

  @Override
  public void sendSubscriptionsNotification(final NodePK fatherPK, final PostDetail post, final Comment comment,
      final String type, final String senderId) {
    // send email alerts
    try {
      Collection<String> subscriberIds = getSubscribeBm().getSubscribers(fatherPK);
      OrganizationController orgaController = new OrganizationController();
      if (subscriberIds != null && !subscriberIds.isEmpty()) {
        // get only subscribers who have sufficient rights to read pubDetail
        NodeDetail node = getNodeBm().getHeader(fatherPK);
        final List<String> newSubscribers = new ArrayList<String>(subscriberIds.size());
        for (String userId : subscriberIds) {
          if (orgaController.isComponentAvailable(fatherPK.getInstanceId(), userId)) {
            if (!node.haveRights()
                || orgaController.isObjectAvailable(node.getRightsDependsOn(), ObjectType.NODE,
                    fatherPK.getInstanceId(), userId)) {
              newSubscribers.add(userId);
            }
          }
        }

        if (!newSubscribers.isEmpty()) {
          UserNotificationHelper.buildAndSend(new BlogUserNotification(fatherPK.getInstanceId(), post, comment, type,
              senderId, newSubscribers));
        }
      }
    } catch (Exception e) {
      SilverTrace.warn("blog", "BlogBmEJB.sendSubscriptionsNotification()",
          "blog.EX_IMPOSSIBLE_DALERTER_LES_UTILISATEURS", "fatherId = " + fatherPK.getId()
              + ", pubId = " + post.getPublication().getPK().getId(), e);
    }
  }

  @Override
  public void updatePost(PostDetail post) {
    Connection con = openConnection();
    try {
      // Suppression de l'ancienne category
      getPublicationBm().removeAllFather(post.getPublication().getPK());

      // Modification de la publi
      getPublicationBm().setDetail(post.getPublication());

      // Ajout de la nouvelle category
      if (StringUtil.isDefined(post.getCategoryId())) {
        setCategory(post.getPublication().getPK(), post.getCategoryId());
      }

      // modification de la date d'évènement
      PostDAO.updateDateEvent(con, post.getPublication().getPK().getId(), post.getDateEvent());

      // envoie notification si abonnement
      PublicationDetail pub = post.getPublication();
      if (pub.getStatus().equals(PublicationDetail.VALID)) {
        sendSubscriptionsNotification(new NodePK("0", pub.getPK().getSpaceId(), pub.getPK().
            getInstanceId()), post, null, "update", pub.getUpdaterId());
      }

    } catch (Exception e) {
      throw new BlogRuntimeException("BlogBmEJB.updatePost()", SilverpeasRuntimeException.ERROR,
          "post.MSG_POST_NOT_UPDATE", e);
    } finally {
      closeConnection(con);
    }
  }

  private void setCategory(PublicationPK pk, String categoryId) {
    NodePK nodePK = new NodePK(categoryId, pk.getInstanceId());
    try {
      getPublicationBm().addFather(pk, nodePK);
    } catch (RemoteException e) {
      throw new BlogRuntimeException("BlogBmEJB.setCategory()", SilverpeasRuntimeException.ERROR,
          "post.MSG_CANT_SET_CATEGORY", e);
    }
  }

  @Override
  public void deletePost(String postId, String instanceId) {
    Connection con = openConnection();
    try {
      PublicationPK pubPK = new PublicationPK(postId, instanceId);

      // Supprime la liaison avec la categorie
      getPublicationBm().removeAllFather(pubPK);

      // supprimer la date d'evenement
      PostDAO.deleteDateEvent(con, pubPK.getId());

      // Supprime les commentaires
      ForeignPK foreignPK = new ForeignPK(postId, instanceId);
      getCommentService().deleteAllCommentsOnPublication(PostDetail.getResourceType(), foreignPK);

      // Supprime le contenu Wysiwyg
      WysiwygController.deleteFileAndAttachment(instanceId, postId);

      // Supprime la publication
      getPublicationBm().removePublication(pubPK);

      // supprimer le silverContent
      getBlogContentManager().deleteSilverContent(con, pubPK);
    } catch (Exception e) {
      throw new BlogRuntimeException("BlogBmEJB.deletePost()", SilverpeasRuntimeException.ERROR,
          "post.MSG_POST_NOT_DELETE", e);
    } finally {
      // fermer la connexion
      closeConnection(con);
    }
  }

  private Connection openConnection() {
    Connection con;
    // initialisation de la connexion
    try {
      con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    } catch (UtilException e) {
      // traitement des exceptions
      throw new BlogRuntimeException("blogBmEJB.initCon()", SilverpeasException.ERROR,
          "root.EX_CONNECTION_OPEN_FAILED", e);
    }
    return con;
  }

  private void closeConnection(Connection con) {
    DBUtil.close(con);
  }

  private PostDetail getPost(PublicationDetail publication) {
    try {
      Collection<NodePK> allCat = getPublicationBm().getAllFatherPK(publication.getPK());
      // la collection des catégories contient en fait une seule catégorie, la récupérer
      Category cat = null;
      if (!allCat.isEmpty()) {
        Iterator<NodePK> it = allCat.iterator();
        NodePK nodePK = it.next();
        cat = getCategory(nodePK);
      }
      // rechercher le nombre de commentaire
      CommentPK foreign_pk = new CommentPK(publication.getPK().getId(), null, publication.getPK().getInstanceId());
      List<Comment> comments =
          getCommentService().getAllCommentsOnPublication(PostDetail.getResourceType(), foreign_pk);

      // recherche de la date d'evenement
      Connection con = openConnection();
      Date dateEvent;
      try {
        dateEvent = com.silverpeas.blog.dao.PostDAO.getDateEvent(con, publication.getPK().getId());
      } finally {
        closeConnection(con);
      }

      PostDetail post = new PostDetail(publication, cat, comments.size(), dateEvent);
      post.setCreatorName(publication.getCreator().getDisplayedName());

      return post;
    } catch (Exception e) {
      throw new BlogRuntimeException("BlogBmEJB.getPost()", SilverpeasRuntimeException.ERROR,
          "post.MSG_POST_NOT_CREATE", e);
    }
  }

  @Override
  public Collection<PostDetail> getAllPosts(String instanceId, int nbReturned) {
    PublicationPK pubPK = new PublicationPK("useless", instanceId);
    Connection con = openConnection();

    Collection<PostDetail> posts = new ArrayList<PostDetail>();
    try {
      // rechercher les publications classée par date d'évènement
      Collection<String> lastEvents = PostDAO.getLastEvents(con, instanceId, nbReturned);
      Collection<PublicationDetail> publications =
          getPublicationBm().getAllPublications(pubPK);
      for (String pubId : lastEvents) {
        for (PublicationDetail publication : publications) {
          if (publication.getPK().getId().equals(pubId)) {
            posts.add(getPost(publication));
          }
        }
      }
      return posts;
    } catch (Exception e) {
      throw new BlogRuntimeException("BlogBmEJB.getAllPosts()", SilverpeasRuntimeException.ERROR,
          "post.MSG_POST_NOT_CREATE", e);
    } finally {
      closeConnection(con);
    }
  }

  @Override
  public Collection<PostDetail> getLastPosts(String instanceId) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    Date beginDate = getMonthFirstDay(calendar);
    Date endDate = getMonthLastDay(calendar);
    return getPostsByArchive(DateUtil.date2SQLDate(beginDate), DateUtil.date2SQLDate(endDate),
        instanceId);
  }

  @Override
  public Collection<PostDetail> getPostsByCategory(String categoryId, String instanceId) {
    SilverTrace.info("blog", "BlogBmEJB.getPostsByCategory()", "root.MSG_GEN_PARAM_VALUE",
        "categoryId =" + categoryId);
    Connection con = openConnection();

    NodePK pk = new NodePK(categoryId, null, instanceId);
    Collection<PostDetail> posts = new ArrayList<PostDetail>();
    try {
      // rechercher les publications classée
      Collection<String> lastEvents = PostDAO.getAllEvents(con, instanceId);

      Collection<PublicationPK> publications = getPublicationBm().getPubPKsInFatherPK(pk);
      SilverTrace.info("blog", "BlogBmEJB.getPostsByCategory()", "root.MSG_GEN_PARAM_VALUE",
          "nb publications =" + publications.size());

      PublicationPK[] allPubs = publications.toArray(new PublicationPK[publications.size()]);
      SilverTrace.info("blog", "BlogBmEJB.getPostsByCategory()", "root.MSG_GEN_PARAM_VALUE",
          "allPubs =" + allPubs.length);
      for (String pubId : lastEvents) {
        int j;
        for (int i = 0; i < allPubs.length; i++) {
          j = allPubs.length - i - 1;
          SilverTrace.info("blog", "BlogBmEJB.getPostsByCategory()", "root.MSG_GEN_PARAM_VALUE",
              "i =" + i + " j = " + j);
          PublicationPK pubPK = allPubs[j];
          SilverTrace.info("blog", "BlogBmEJB.getPostsByCategory()", "root.MSG_GEN_PARAM_VALUE",
              "pubPK =" + pubPK.getId());
          if (pubPK.getId().equals(pubId)) {
            posts.add(getContentById(pubId));
          }
        }
      }

      return posts;
    } catch (Exception e) {
      throw new BlogRuntimeException("BlogBmEJB.getPostsByCategory()",
          SilverpeasRuntimeException.ERROR, "post.MSG_POST_NOT_CREATE", e);
    } finally {
      closeConnection(con);
    }
  }

  @Override
  public Collection<PostDetail> getPostsByDate(String date, String instanceId) {
    return getPostsByArchive(date, date, instanceId);
  }

  @Override
  public Collection<PostDetail> getPostsByArchive(String beginDate, String endDate,
      String instanceId) {
    SilverTrace.info("blog", "BlogBmEJB.getPostsByArchive()", "root.MSG_GEN_PARAM_VALUE",
        "dates =" + beginDate + "-" + endDate);

    PublicationPK pubPK = new PublicationPK("useless", instanceId);
    Connection con = openConnection();
    Collection<PostDetail> posts = new ArrayList<PostDetail>();
    try {
      // rechercher les publications classée par date d'évènement
      Collection<String> lastEvents = PostDAO.getEventsByDates(con, instanceId, beginDate, endDate);

      // Collection<PublicationDetail> publications =
      // getPublicationBm().getDetailBetweenDate(beginDate, endDate, instanceId);
      Collection<PublicationDetail> publications =
          getPublicationBm().getPublicationsByStatus("Valid", pubPK);
      for (String pubId : lastEvents) {
        // pour chaque publication, créer le post correspondant
        SilverTrace.info("blog", "BlogBmEJB.getPostsByArchive()", "root.MSG_GEN_PARAM_VALUE",
            "publications =" + publications.toString());
        for (PublicationDetail publication : publications) {
          if (publication.getPK().getId().equals(pubId)) {
            posts.add(getPost(publication));
          }
        }
      }
      SilverTrace.info("blog", "BlogBmEJB.getPostsByArchive()", "root.MSG_GEN_PARAM_VALUE",
          "posts =" + posts.toString());
      return posts;
    } catch (Exception e) {
      throw new BlogRuntimeException("BlogBmEJB.getPostsByArchive()",
          SilverpeasRuntimeException.ERROR, "post.MSG_POST_NOT_CREATE", e);
    } finally {
      closeConnection(con);
    }
  }

  @Override
  public Collection<PostDetail> getResultSearch(String word, String userId, String spaceId,
      String instanceId) {
    Collection<PostDetail> posts = new ArrayList<PostDetail>();
    List<String> postIds = new ArrayList<String>();
    SilverTrace.info("blog", "BlogBmEJB.getResultSearch()", "root.MSG_GEN_PARAM_VALUE", "word ="
        + word + " userId = " + userId + " instanceId = " + instanceId);
    QueryDescription query = new QueryDescription(word);
    query.setSearchingUser(userId);
    query.addSpaceComponentPair(spaceId, instanceId);
    SilverTrace.info("blog", "BlogBmEJB.getResultSearch()", "root.MSG_GEN_PARAM_VALUE", "query ="
        + query.getQuery());
    Connection con = openConnection();
    try {
      List<MatchingIndexEntry> result = SearchEngineFactory.getSearchEngine().search(query).getEntries();
      SilverTrace.info("blog", "BlogBmEJB.getResultSearch()", "root.MSG_GEN_PARAM_VALUE",
          "result =" + result.size());

      // création des billets à partir des résultats
      // rechercher la liste des posts trié par date
      Collection<String> allEvents = PostDAO.getAllEvents(con, instanceId);
      for (String pubId : allEvents) {
        for (MatchingIndexEntry matchIndex : result) {
          String objectType = matchIndex.getObjectType();
          String objectId = matchIndex.getObjectId();
          if ("Publication".equals(objectType) || objectType.startsWith("Attachment")) {
            if (pubId.equals(objectId) && !postIds.contains(objectId)) {
              PostDetail post = getContentById(objectId);
              postIds.add(objectId);
              posts.add(post);
            }
          }
        }
      }

    } catch (Exception e) {
      throw new BlogRuntimeException("BlogSessionController.getResultSearch()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_ADD_OBJECT", e);
    } finally {
      closeConnection(con);
    }
    return posts;
  }

  @Override
  public String createCategory(Category category) {
    try {
      NodePK nodePK = getNodeBm().createNode((NodeDetail) category, new NodeDetail());
      return nodePK.getId();
    } catch (Exception e) {
      throw new BlogRuntimeException("BlogBmEJB.createCategory()",
          SilverpeasRuntimeException.ERROR, "post.MSG_CATEGORY_NOT_CREATE", e);
    }
  }

  @Override
  public void updateCategory(Category category) {
    try {
      getNodeBm().setDetail(category);
    } catch (Exception e) {
      throw new BlogRuntimeException("BlogBmEJB.updateCategory()",
          SilverpeasRuntimeException.ERROR, "post.MSG_CATEGORY_NOT_UPDATE", e);
    }
  }

  @Override
  public void deleteCategory(String id, String instanceId) {
    try {
      NodePK nodePk = new NodePK(id, instanceId);

      // recherche des billets sur cette catégorie
      Collection<PostDetail> posts = getPostsByCategory(id, instanceId);
      for (PostDetail post : posts) {
        getPublicationBm().removeFather(post.getPublication().getPK(), nodePk);
      }
      // suppression de la catégorie
      getNodeBm().removeNode(nodePk);
    } catch (Exception e) {
      throw new BlogRuntimeException("BlogBmEJB.deleteCategory()",
          SilverpeasRuntimeException.ERROR, "post.MSG_CATEGORY_NOT_DELETE", e);
    }
  }

  @Override
  public Category getCategory(NodePK pk) {
    try {
      return new Category(getNodeBm().getDetail(pk));
    } catch (Exception e) {
      throw new BlogRuntimeException("BlogBmEJB.getCategory()", SilverpeasRuntimeException.ERROR,
          "post.MSG_CATEGORY_NOT_EXIST", e);
    }
  }

  @Override
  public Collection<NodeDetail> getAllCategories(String instanceId) {
    try {
      NodePK nodePK = new NodePK("0", instanceId);
      return getNodeBm().getChildrenDetails(nodePK, "nodeName");
    } catch (Exception e) {
      throw new BlogRuntimeException("BlogBmEJB.getAllCategories()",
          SilverpeasRuntimeException.ERROR, "post.MSG_CATEGORIES_NOT_EXIST", e);
    }
  }

  @Override
  public Collection<Archive> getAllArchives(String instanceId) {
    Connection con = openConnection();
    try {
      Archive archive;
      Collection<Archive> archives = new ArrayList<Archive>();
      Calendar calendar = Calendar.getInstance(Locale.FRENCH);

      // rechercher tous les posts par date d'évènements
      Collection<Date> lastEvents = PostDAO.getAllDateEvents(con, instanceId);

      for (Date dateEvent : lastEvents) {
        calendar.setTime(dateEvent);
        // pour chaque date regarder si l'archive existe
        archive = createArchive(calendar, instanceId);
        if (!archives.contains(archive)) {
          archives.add(archive);
        }
      }
      return archives;
    } catch (Exception e) {
      throw new BlogRuntimeException("BlogBmEJB.getAllArchives()",
          SilverpeasRuntimeException.ERROR, "post.MSG_CATEGORIES_NOT_EXIST", e);
    } finally {
      closeConnection(con);
    }
  }

  private Archive createArchive(Calendar calendar, String instanceId) {
    Date beginDate = getMonthFirstDay(calendar);
    Date endDate = getMonthLastDay(calendar);
    // regarder s'il y a des évenements sur cette période

    Archive archive =
        new Archive("useless", DateUtil.date2SQLDate(beginDate), DateUtil.date2SQLDate(endDate));
    archive.setYear(java.lang.Integer.toString(calendar.get(Calendar.YEAR)));
    archive.setMonthId(java.lang.Integer.toString(calendar.get(Calendar.MONTH)));
    return archive;
  }

  @Override
  public void indexBlog(String componentId) {
    indexTopics(new NodePK("useless", componentId));
    indexPublications(new PublicationPK("useless", componentId));
  }

  private void indexPublications(PublicationPK pubPK) {
    Collection<PublicationDetail> pubs = null;
    try {
      pubs = getPublicationBm().getAllPublications(pubPK);
    } catch (Exception e) {
      throw new BlogRuntimeException("BlogBmEJB.indexPublications()",
          SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DINDEXER_LES_PUBLICATIONS", e);
    }

    if (pubs != null) {
      for (PublicationDetail pub : pubs) {
        try {
          indexPublication(pub.getPK());
        } catch (Exception e) {
          throw new BlogRuntimeException("BlogBmEJB.indexPublications()",
              SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DINDEXER_LA_PUBLICATION",
              "pubPK = " + pub.getPK().toString(), e);
        }
      }
    }
  }

  private void indexPublication(PublicationPK pubPK) throws RemoteException {
    // index publication itself
    getPublicationBm().createIndex(pubPK);

    // index external elements
    indexExternalElementsOfPublication(pubPK);
  }

  private void indexTopics(NodePK nodePK) {
    Collection<NodeDetail> nodes = null;
    try {
      nodes = getNodeBm().getAllNodes(nodePK);
      if (nodes != null) {
        for (NodeDetail node : nodes) {
          if (!node.getNodePK().isRoot() && !node.getNodePK().isTrash()) {
            getNodeBm().createIndex(node);
          }
        }
      }
    } catch (Exception e) {
      throw new BlogRuntimeException("BlogBmEJB.indexTopics()", SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DINDEXER_LES_THEMES", e);
    }
  }

  @Override
  public void addSubscription(NodePK topicPK, String userId) {
    SilverTrace.info("blog", "BlogBmEJB.addSubscription()", "root.MSG_GEN_ENTER_METHOD");
    if (!checkSubscription(topicPK, userId)) {
      return;
    }
    getSubscribeBm().subscribe(new NodeSubscription(userId, topicPK));
    SilverTrace.info("blog", "BlogBmEJB.addSubscription()", "root.MSG_GEN_EXIT_METHOD");
  }

  private boolean checkSubscription(NodePK topicPK, String userId) {
    try {
      Collection<? extends Subscription> subscriptions =
          getSubscribeBm().getUserSubscriptionsByComponent(userId, topicPK.getInstanceId());
      for (Subscription subscription : subscriptions) {
        if (topicPK.getId().equals(subscription.getTopic().getId())) {
          return false;
        }
      }
      return true;
    } catch (Exception e) {
      throw new BlogRuntimeException("BlogBmEJB.checkSubscription()",
          SilverpeasRuntimeException.ERROR, "blog.EX_IMPOSSIBLE_DOBTENIR_LES_ABONNEMENTS", e);
    }
  }

  private void indexExternalElementsOfPublication(PublicationPK pubPK) {
    try {
      // index comments
      getCommentService().indexAllCommentsOnPublication(PostDetail.getResourceType(), pubPK);
    } catch (Exception e) {
      SilverTrace.error("blog", "BlogBmEJB.indexExternalElementsOfPublication",
          "Indexing comments failed", "pubPK = " + pubPK.toString(), e);
    }
  }

  @Override
  public void externalElementsOfPublicationHaveChanged(PublicationPK pubPK, String userId) {
    try {
      PublicationDetail pubDetail = getPublicationBm().getDetail(pubPK);
      pubDetail.setUpdaterId(userId);

      if (PublicationDetail.DRAFT.equals(pubDetail.getStatus())) {
        pubDetail.setIndexOperation(IndexManager.NONE);
      }

      getPublicationBm().setDetail(pubDetail);

      // envoie notification si abonnement
      if (pubDetail.getStatus().equals(PublicationDetail.VALID)) {
        PostDetail post = getPost(pubDetail);
        sendSubscriptionsNotification(new NodePK("0", pubPK.getSpaceId(), pubPK.getInstanceId()),
            post, null, "update", pubDetail.getUpdaterId());
      }
    } catch (RemoteException e) {
      SilverTrace.error("blog", getClass().getSimpleName()
          + ".externalElementsOfPublicationHaveChanged", "root.EX_NO_MESSAGE", e);
    }
  }

  private Date getMonthFirstDay(Calendar calendar) {
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    return calendar.getTime();
  }

  private Date getMonthLastDay(Calendar calendar) {
    int monthLastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    calendar.set(Calendar.DAY_OF_MONTH, monthLastDay);
    return calendar.getTime();
  }

  private BlogContentManager getBlogContentManager() {
    return new BlogContentManager();
  }

  private int createSilverContent(Connection con, PublicationDetail pubDetail, String creatorId) {
    SilverTrace.info("blog", "BlogBmEJB.createSilverContent()", "root.MSG_GEN_ENTER_METHOD",
        "pubId = " + pubDetail.getPK().getId());
    try {
      return getBlogContentManager().createSilverContent(con, pubDetail, creatorId);
    } catch (Exception e) {
      throw new BlogRuntimeException("BlogBmEJB.createSilverContent()",
          SilverpeasRuntimeException.ERROR, "blog.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  @Override
  public void draftOutPost(PostDetail post) {
    try {
      PublicationDetail pub = post.getPublication();
      pub.setStatus(PublicationDetail.VALID);
      // Modification de la publi
      getPublicationBm().setDetail(pub);

      // envoie notification si abonnement
      if (pub.getStatus().equals(PublicationDetail.VALID)) {
        sendSubscriptionsNotification(new NodePK("0", pub.getPK().getSpaceId(), pub.getPK().
            getInstanceId()), post, null, "create", pub.getUpdaterId());
      }
    } catch (Exception e) {
      throw new BlogRuntimeException("BlogBmEJB.draftOutPost()",
          SilverpeasRuntimeException.ERROR, "blog.EX_CAN_DRAFT_OUT", e);
    }
  }

  private SubscriptionService getSubscribeBm() {
    return SubscriptionServiceFactory.getFactory().getSubscribeService();
  }

  private PublicationBm getPublicationBm() {
    PublicationBm publicationBm = null;
    try {
      PublicationBmHome publicationBmHome = EJBUtilitaire.getEJBObjectRef(
          JNDINames.PUBLICATIONBM_EJBHOME, PublicationBmHome.class);
      publicationBm = publicationBmHome.create();
    } catch (Exception e) {
      throw new PublicationRuntimeException("BlogBmEJB.getPublicationBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return publicationBm;
  }

  private NodeBm getNodeBm() {
    NodeBm nodeBm = null;
    try {
      NodeBmHome nodeBmHome = EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME,
          NodeBmHome.class);
      nodeBm = nodeBmHome.create();
    } catch (Exception e) {
      throw new PublicationRuntimeException("BlogBmEJB.getNodeBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return nodeBm;
  }

  /**
   * Gets a DefaultCommentService instance.
   * @return a DefaultCommentService instance.
   */
  protected CommentService getCommentService() {
    return commentService;
  }

}
