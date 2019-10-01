/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.components.blog.service;

import org.silverpeas.components.blog.BlogContentManager;
import org.silverpeas.components.blog.dao.PostDAO;
import org.silverpeas.components.blog.model.Archive;
import org.silverpeas.components.blog.model.BlogRuntimeException;
import org.silverpeas.components.blog.model.Category;
import org.silverpeas.components.blog.model.PostDetail;
import org.silverpeas.components.blog.notification.BlogUserSubscriptionNotification;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.ProfiledObjectId;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.model.CommentPK;
import org.silverpeas.core.comment.service.CommentService;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.index.indexing.model.IndexManager;
import org.silverpeas.core.index.search.SearchEngineProvider;
import org.silverpeas.core.index.search.model.MatchingIndexEntry;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodeOrderComparator;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.service.PdcClassificationService;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.pdc.subscription.service.PdcSubscriptionManager;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.subscription.SubscriptionService;
import org.silverpeas.core.subscription.SubscriptionServiceProvider;
import org.silverpeas.core.subscription.service.ComponentSubscription;
import org.silverpeas.core.subscription.service.ResourceSubscriptionProvider;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static org.silverpeas.core.SilverpeasExceptionMessages.*;

/**
 * Default implementation of the services provided by the Blog component. It is managed by the
 * underlying IoC container. At initialization by the IoC container, it registers itself among
 * different services for which it is interested.
 */
@Singleton
public class DefaultBlogService implements BlogService {

  private static final String MESSAGES_PATH = "org.silverpeas.blog.multilang.blogBundle";
  private static final String SETTINGS_PATH = "org.silverpeas.blog.settings.blogSettings";
  private static final SettingBundle settings = ResourceLocator.getSettingBundle(SETTINGS_PATH);
  private static final String POST = "post";
  private static final String USELESS = "useless";
  @Inject
  private CommentService commentService;
  @Inject
  private OrganizationController organizationController;
  @Inject
  private PdcManager pdcManager;
  @Inject
  private PdcClassificationService pdcClassificationService;
  @Inject
  private PdcSubscriptionManager pdcSubscriptionManager;
  @Inject
  private BlogContentManager blogContentManager;

  @Override
  public PostDetail getContentById(String contentId) {
    PublicationDetail publication = getPublicationService().getDetail(new PublicationPK(contentId));
    return getPost(publication);
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
    return instanceId.startsWith("blog");
  }

  @Override
  public Date getDateEvent(String pubId) {
    try (Connection con = openConnection()) {
      return PostDAO.getDateEvent(con, pubId);
    } catch (SQLException e) {
      throw new BlogRuntimeException(failureOnGetting(POST, pubId), e);
    }
  }

  @Transactional
  @Override
  public String createPost(PostDetail post) {
    return createPost(post, null);
  }

  @Transactional
  @Override
  public String createPost(final PostDetail post, PdcClassification classification) {
    try (Connection con = openConnection()) {
      // Create publication
      PublicationDetail pub = post.getPublication();
      pub.setStatus(PublicationDetail.DRAFT_STATUS);
      PublicationPK pk = getPublicationService().createPublication(pub);

      // Create post
      PostDAO.createDateEvent(con, pk.getId(), post.getDateEvent(), pk.getInstanceId());
      if (StringUtil.isDefined(post.getCategoryId())) {
        setCategory(pk, post.getCategoryId());
      }

      // Create empty wysiwyg content
      WysiwygController
          .createUnindexedFileAndAttachment("", new ResourceReference(pk),
              pub.getCreatorId(), pub.getLanguage());

      // Create silver content
      createSilverContent(con, pub, pub.getCreatorId());

      // classify the publication on the PdC if its classification is defined
      if (classification != null) {
        classification.classifyContent(pub);
      }

      return pk.getId();
    } catch (Exception e) {
      throw new BlogRuntimeException(failureOnAdding(POST, post.getId()), e);
    }
  }

  @Override
  public void sendSubscriptionsNotification(final NodePK fatherPK, final PostDetail post,
      final Comment comment, final String type, final String senderId) {
    Collection<String> subscriberIds =
        ResourceSubscriptionProvider.getSubscribersOfComponent(fatherPK.getInstanceId())
            .getAllUserIds();
    if (subscriberIds != null && !subscriberIds.isEmpty()) {
      // get only subscribers who have sufficient rights to read pubDetail
      NodeDetail node = getNodeBm().getHeader(fatherPK);
      final List<String> newSubscribers = new ArrayList<>(subscriberIds.size());
      for (String userId : subscriberIds) {
        if (organizationController.isComponentAvailableToUser(fatherPK.getInstanceId(), userId) &&
            (!node.haveRights() || organizationController.isObjectAvailableToUser(
                ProfiledObjectId.fromNode(node.getRightsDependsOn()), fatherPK.getInstanceId(),
                userId))) {
          newSubscribers.add(userId);
        }
      }

      if (!newSubscribers.isEmpty()) {
        UserNotificationHelper.buildAndSend(
            new BlogUserSubscriptionNotification(post, comment, type, senderId, newSubscribers));
      }
    }
  }

  @Transactional
  @Override
  public void updatePost(PostDetail post) {
    try (Connection con = openConnection()) {
      PublicationPK pubPk = post.getPublication().getPK();
      PublicationDetail pub = post.getPublication();

      // Remove last category
      getPublicationService().removeAllFathers(pubPk);

      // Save the publication
      getPublicationService().setDetail(pub);

      // Add the new category
      if (StringUtil.isDefined(post.getCategoryId())) {
        setCategory(pubPk, post.getCategoryId());
      }

      // Update event date
      PostDAO.updateDateEvent(con, pubPk.getId(), post.getDateEvent());

      // Save wysiwyg content and do not index it (cause it is already indexed as publication content)
      WysiwygController
          .updateFileAndAttachment(post.getContent(), pub.getInstanceId(), pubPk.getId(),
              pub.getUpdaterId(), pub.getLanguage(), false);

      // Send notification if subscription
      if (pub.isValid()) {
        sendSubscriptionsNotification(new NodePK("0", pub.getPK().getSpaceId(), pub.getPK().
            getInstanceId()), post, null, "update", pub.getUpdaterId());
      }

    } catch (SQLException e) {
      throw new BlogRuntimeException(failureOnUpdate(POST, post.getId()), e);
    }
  }

  private void setCategory(PublicationPK pk, String categoryId) {
    NodePK nodePK = new NodePK(categoryId, pk.getInstanceId());
    getPublicationService().addFather(pk, nodePK);
  }

  @Transactional
  @Override
  public void deletePost(String postId, String instanceId) {

    try (Connection con = openConnection()) {
      PublicationPK pubPK = new PublicationPK(postId, instanceId);
      // Delete link with categorie
      getPublicationService().removeAllFathers(pubPK);
      // Delete date event
      PostDAO.deleteDateEvent(con, pubPK.getId());
      // Delete comments
      ResourceReference resourceReference = new ResourceReference(postId, instanceId);
      getCommentService().deleteAllCommentsOnPublication(PostDetail.getResourceType(),
          resourceReference);
      // Delete wysiwyg content
      WysiwygController.deleteFileAndAttachment(instanceId, postId);
      // Delete publication
      getPublicationService().removePublication(pubPK);
      // Delete silverContent
      blogContentManager.deleteSilverContent(con, pubPK);
    } catch (Exception e) {
      throw new BlogRuntimeException(failureOnDeleting(POST, postId), e);
    }
  }

  private Connection openConnection() {
    Connection con;
    try {
      con = DBUtil.openConnection();
    } catch (SQLException e) {
      // traitement des exceptions
      throw new BlogRuntimeException(failureOnOpeningConnectionTo("datasource"), e);
    }
    return con;
  }

  private PostDetail getPost(PublicationDetail publication) {
    try {
      Collection<NodePK> allCat = getPublicationService().getAllFatherPKInSamePublicationComponentInstance(publication.getPK());
      // la collection des catégories contient en fait une seule catégorie, la récupérer
      Category cat = null;
      if (!allCat.isEmpty()) {
        Iterator<NodePK> it = allCat.iterator();
        NodePK nodePK = it.next();
        cat = getCategory(nodePK);
      }
      // rechercher le nombre de commentaire
      CommentPK foreignPk = new CommentPK(publication.getPK().getId(), null, publication.getPK().
          getInstanceId());
      List<Comment> comments =
          getCommentService().getAllCommentsOnPublication(PostDetail.getResourceType(), foreignPk);

      // recherche de la date d'evenement
      Date dateEvent;
      try (Connection con = openConnection()) {
        dateEvent = PostDAO.getDateEvent(con, publication.getPK().getId());
      }

      PostDetail post = new PostDetail(publication, cat, comments.size(), dateEvent);
      post.setCreatorName(publication.getCreator().getDisplayedName());

      return post;
    } catch (Exception e) {
      throw new BlogRuntimeException(
          failureOnGetting(POST + " associated to publication", publication.getId()), e);
    }
  }

  @Override
  public Collection<PostDetail> getAllPosts(String instanceId) {
    Collection<PostDetail> posts = new ArrayList<>();
    try(Connection con = openConnection()) {
      // rechercher les publications classée par date d'évènement
      Collection<String> lastEvents = PostDAO.getAllEvents(con, instanceId);
      Collection<PublicationDetail> publications =
          getPublicationService().getAllPublications(instanceId);
      for (String pubId : lastEvents) {
        for (PublicationDetail publication : publications) {
          if (publication.getPK().getId().equals(pubId)) {
            posts.add(getPost(publication));
          }
        }
      }
      return posts;
    } catch (Exception e) {
      throw new BlogRuntimeException(failureOnGetting("all posts of blog", instanceId), e);
    }
  }

  @Override
  public Collection<PostDetail> getAllValidPosts(String instanceId, int nbReturned) {
    Collection<PostDetail> posts = new ArrayList<>();
    int count = nbReturned;
    try (Connection con = openConnection()) {
      // rechercher les publications classées par date d'évènement
      Collection<String> lastEvents = PostDAO.getAllEvents(con, instanceId);
      Collection<PublicationDetail> publications =
          getPublicationService().getAllPublications(instanceId);
      for (String pubId : lastEvents) {
        for (PublicationDetail publication : publications) {
          if (publication.getPK().getId().equals(pubId) && PublicationDetail.VALID_STATUS.
              equals(publication.getStatus()) && count > 0) {
            count--;
            posts.add(getPost(publication));
          }
        }
      }
      return posts;
    } catch (Exception e) {
      throw new BlogRuntimeException(failureOnGetting("All validated posts for blog", instanceId),
          e);
    }
  }

  @Override
  public Collection<PostDetail> getPostsByCategory(String categoryId, String instanceId) {

    NodePK pk = new NodePK(categoryId, null, instanceId);
    Collection<PostDetail> posts = new ArrayList<>();
    try (Connection con = openConnection()) {
      // rechercher les publications classée
      Collection<String> lastEvents = PostDAO.getAllEvents(con, instanceId);

      Collection<PublicationPK> publications = getPublicationService().getPubPKsInFatherPK(pk);
      PublicationPK[] allPubs = publications.toArray(new PublicationPK[publications.size()]);
      for (String pubId : lastEvents) {
        int j;
        for (int i = 0; i < allPubs.length; i++) {
          j = allPubs.length - i - 1;
          PublicationPK pubPK = allPubs[j];
          if (pubPK.getId().equals(pubId)) {
            posts.add(getContentById(pubId));
          }
        }
      }

      return posts;
    } catch (Exception e) {
      throw new BlogRuntimeException(failureOnGetting("all posts in category", categoryId), e);
    }
  }

  @Override
  public Collection<PostDetail> getPostsByDate(String date, String instanceId) {
    return getPostsByArchive(date, date, instanceId);
  }

  @Override
  public Collection<PostDetail> getPostsByArchive(String beginDate, String endDate,
      String instanceId) {
    Collection<PostDetail> posts = new ArrayList<>();
    try (Connection con = openConnection()) {
      // rechercher les publications classée par date d'évènement
      Collection<String> lastEvents = PostDAO.getEventsByDates(con, instanceId, beginDate, endDate);

      Collection<PublicationDetail> publications =
          getPublicationService().getAllPublications(instanceId);
      for (String pubId : lastEvents) {
        // pour chaque publication, créer le post correspondant
        for (PublicationDetail publication : publications) {
          if (publication.getPK().getId().equals(pubId)) {
            posts.add(getPost(publication));
          }
        }
      }

      return posts;
    } catch (Exception e) {
      throw new BlogRuntimeException(
          failureOnGetting("all posts archived between", beginDate + " and " + endDate), e);
    }
  }

  @Override
  public Collection<PostDetail> getResultSearch(String word, String userId, String instanceId) {
    Collection<PostDetail> posts = new ArrayList<>();
    List<String> postIds = new ArrayList<>();

    QueryDescription query = new QueryDescription(word);
    query.setSearchingUser(userId);
    query.addComponent(instanceId);

    try (Connection con = openConnection()) {
      List<MatchingIndexEntry> result = SearchEngineProvider.getSearchEngine().search(query).
          getEntries();


      // création des billets à partir des résultats
      // rechercher la liste des posts trié par date
      Collection<String> allEvents = PostDAO.getAllEvents(con, instanceId);
      for (final String pubId : allEvents) {
        for (MatchingIndexEntry matchIndex : result) {
          String objectId = matchIndex.getObjectId();
          if (pubId.equals(objectId) && !postIds.contains(objectId)) {
            PostDetail post = getContentById(objectId);
            postIds.add(objectId);
            posts.add(post);
          }
        }
      }

    } catch (Exception e) {
      throw new BlogRuntimeException(e);
    }
    return posts;
  }

  @Transactional
  @Override
  public String createCategory(Category category) {
    try {
      NodePK nodePK = getNodeBm().createNode(category, new NodeDetail());
      return nodePK.getId();
    } catch (Exception e) {
      throw new BlogRuntimeException(e);
    }
  }

  @Transactional
  @Override
  public void updateCategory(Category category) {
    try {
      getNodeBm().setDetail(category);
    } catch (Exception e) {
      throw new BlogRuntimeException(e);
    }
  }

  @Transactional
  @Override
  public void deleteCategory(String id, String instanceId) {
    try {
      NodePK nodePk = new NodePK(id, instanceId);

      // recherche des billets sur cette catégorie
      Collection<PostDetail> posts = getPostsByCategory(id, instanceId);
      for (PostDetail post : posts) {
        getPublicationService().removeFather(post.getPublication().getPK(), nodePk);
      }
      // suppression de la catégorie
      getNodeBm().removeNode(nodePk);
    } catch (Exception e) {
      throw new BlogRuntimeException(e);
    }
  }

  @Override
  public Category getCategory(NodePK pk) {
    return new Category(getNodeBm().getDetail(pk));
  }

  @Override
  public Collection<NodeDetail> getAllCategories(String instanceId) {
    NodePK nodePK = new NodePK(NodePK.ROOT_NODE_ID, instanceId);
    List<NodeDetail> result = new ArrayList<>(getNodeBm().getChildrenDetails(nodePK));
    Collections.sort(result, new NodeOrderComparator());
    return result;
  }

  @Override
  public Collection<Archive> getAllArchives(String instanceId) {
    try (Connection con = openConnection()) {
      Archive archive;
      Collection<Archive> archives = new ArrayList<>();
      Calendar calendar = Calendar.getInstance(Locale.FRENCH);

      // rechercher tous les posts par date d'évènements
      Collection<Date> lastEvents = PostDAO.getAllDateEvents(con, instanceId);
      for (final Date dateEvent : lastEvents) {
        calendar.setTime(dateEvent);
        // pour chaque date regarder si l'archive existe
        archive = createArchive(calendar);
        if (!archives.contains(archive)) {
          archives.add(archive);
        }
      }
      return archives;
    } catch (Exception e) {
      throw new BlogRuntimeException(failureOnGetting("All archives of blog", instanceId), e);
    }
  }

  private Archive createArchive(Calendar calendar) {
    Date beginDate = getMonthFirstDay(calendar);
    Date endDate = getMonthLastDay(calendar);
    // regarder s'il y a des évenements sur cette période
    Archive archive =
        new Archive(USELESS, DateUtil.date2SQLDate(beginDate), DateUtil.date2SQLDate(endDate));
    archive.setYear(java.lang.Integer.toString(calendar.get(Calendar.YEAR)));
    archive.setMonthId(java.lang.Integer.toString(calendar.get(Calendar.MONTH)));
    return archive;
  }

  @Override
  public void indexBlog(String componentId) {
    indexTopics(new NodePK(USELESS, componentId));
    indexPublications(componentId);
  }

  private void indexPublications(String componentId) {
    Collection<PublicationDetail> pubs;
    try {
      pubs = getPublicationService().getAllPublications(componentId);
    } catch (Exception e) {
      throw new BlogRuntimeException(
          failureOnGetting("[INDEXING] all publications in blog", componentId), e);
    }

    if (pubs != null) {
      for (PublicationDetail pub : pubs) {
        try {
          indexPublication(pub.getPK());
        } catch (Exception e) {
          throw new BlogRuntimeException(failureOnIndexing("publication", pub.getPK().toString()), e);
        }
      }
    }
  }

  private void indexPublication(PublicationPK pubPK) {
    // index publication itself
    getPublicationService().createIndex(pubPK);
    // index external elements
    indexExternalElementsOfPublication(pubPK);
  }

  private void indexTopics(NodePK nodePK) {
    Collection<NodeDetail> nodes = getNodeBm().getAllNodes(nodePK);
    if (nodes != null) {
      for (NodeDetail node : nodes) {
        if (!node.getNodePK().isRoot() && !node.getNodePK().isTrash()) {
          getNodeBm().createIndex(node);
        }
      }
    }
  }

  @Override
  public boolean isSubscribed(final String userId, final String instanceId) {
    return getSubscribeService().existsSubscription(new ComponentSubscription(userId, instanceId));
  }

  private void indexExternalElementsOfPublication(PublicationPK pubPK) {
    try {
      // index comments
      getCommentService().indexAllCommentsOnPublication(PostDetail.getResourceType(), pubPK);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error("Comment index failure for publication {0}",
          new String[]{pubPK.toString()}, e);
    }
  }

  @Override
  public void externalElementsOfPublicationHaveChanged(PublicationPK pubPK, String userId) {
    PublicationDetail pubDetail = getPublicationService().getDetail(pubPK);
    pubDetail.setUpdaterId(userId);
    if (PublicationDetail.DRAFT_STATUS.equals(pubDetail.getStatus())) {
      pubDetail.setIndexOperation(IndexManager.NONE);
    }
    getPublicationService().setDetail(pubDetail);
    // envoie notification si abonnement
    if (pubDetail.getStatus().equals(PublicationDetail.VALID_STATUS)) {
      PostDetail post = getPost(pubDetail);
      sendSubscriptionsNotification(new NodePK("0", pubPK.getSpaceId(), pubPK.getInstanceId()),
          post, null, "update", pubDetail.getUpdaterId());
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

  private int createSilverContent(Connection con, PublicationDetail pubDetail, String creatorId) {
    try {
      return blogContentManager.createSilverContent(con, pubDetail, creatorId);
    } catch (ContentManagerException e) {
      throw new BlogRuntimeException(e);
    }
  }

  private void updateSilverContentVisibility(PublicationDetail pubDetail) {
    try {
      blogContentManager.updateSilverContentVisibility(pubDetail);
    } catch (Exception e) {
      throw new BlogRuntimeException(e);
    }
  }

  @Override
  public void draftOutPost(PostDetail post) {

    PublicationDetail pub = post.getPublication();
    pub.setStatus(PublicationDetail.VALID_STATUS);

    // update the publication
    getPublicationService().setDetail(pub);

    if (pub.getStatus().equals(PublicationDetail.VALID_STATUS)) {

      // update visibility attribute on PDC
      updateSilverContentVisibility(pub);

      // send notification if instance subscription
      sendSubscriptionsNotification(new NodePK("0", pub.getPK().getSpaceId(), pub.getPK().
          getInstanceId()), post, null, "create", pub.getUpdaterId());

      // send notification if PDC subscription
      try {
        int silverObjectId = getSilverObjectId(pub.getPK());
        List<ClassifyPosition> positions = pdcManager.getPositions(silverObjectId, pub.getPK().
            getInstanceId());
        if (positions != null) {
          for (ClassifyPosition position : positions) {
            pdcSubscriptionManager
                .checkSubscriptions(position.getValues(), pub.getPK().getInstanceId(),
                    silverObjectId);
          }
        }
      } catch (PdcException e) {
        SilverLogger.getLogger(this).error("PdC subscriber notification failure", e);
      }
    }
  }

  private int createSilverContent(PublicationDetail pubDetail, String creatorId) {
    Connection con = null;
    try {
      con = openConnection();
      return blogContentManager.createSilverContent(con, pubDetail, creatorId);
    } catch (Exception e) {
      throw new BlogRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
  }


  private int getSilverObjectId(PublicationPK pubPK) {

    int silverObjectId;
    PublicationDetail pubDetail;
    try {
      silverObjectId =
          blogContentManager.getSilverContentId(pubPK.getId(), pubPK.getInstanceId());
      if (silverObjectId == -1) {
        pubDetail = getPublicationService().getDetail(pubPK);
        silverObjectId = createSilverContent(pubDetail, pubDetail.getCreatorId());
      }
    } catch (Exception e) {
      throw new BlogRuntimeException(e);
    }
    return silverObjectId;
  }

  private SubscriptionService getSubscribeService() {
    return SubscriptionServiceProvider.getSubscribeService();
  }

  private PublicationService getPublicationService() {
    return ServiceProvider.getService(PublicationService.class);
  }

  private NodeService getNodeBm() {
    return NodeService.get();
  }

  /**
   * Gets a DefaultCommentService instance.
   * @return a DefaultCommentService instance.
   */
  protected CommentService getCommentService() {
    return commentService;
  }
}
