/*
 * Copyright (C) 2000 - 2015 Silverpeas
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

import com.silverpeas.subscribe.SubscriptionService;
import com.silverpeas.subscribe.SubscriptionServiceProvider;
import com.silverpeas.subscribe.service.ComponentSubscription;
import com.silverpeas.subscribe.service.ResourceSubscriptionProvider;
import com.silverpeas.usernotification.builder.helper.UserNotificationHelper;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.webactiv.beans.admin.ObjectType;
import com.stratelia.webactiv.node.control.NodeService;
import com.stratelia.webactiv.node.model.NodeDetail;
import com.stratelia.webactiv.node.model.NodeOrderComparator;
import com.stratelia.webactiv.node.model.NodePK;
import com.stratelia.webactiv.publication.control.PublicationService;
import com.stratelia.webactiv.publication.model.PublicationDetail;
import com.stratelia.webactiv.publication.model.PublicationPK;
import org.silverpeas.components.blog.BlogContentManager;
import org.silverpeas.components.blog.dao.PostDAO;
import org.silverpeas.components.blog.model.Archive;
import org.silverpeas.components.blog.model.BlogRuntimeException;
import org.silverpeas.components.blog.model.Category;
import org.silverpeas.components.blog.model.PostDetail;
import org.silverpeas.components.blog.notification.BlogUserNotification;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.model.CommentPK;
import org.silverpeas.core.comment.service.CommentService;
import org.silverpeas.core.index.indexing.model.IndexManager;
import org.silverpeas.core.index.search.SearchEngineProvider;
import org.silverpeas.core.index.search.model.MatchingIndexEntry;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.service.PdcClassificationService;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.pdc.subscription.service.PdcSubscriptionManager;
import org.silverpeas.util.DBUtil;
import org.silverpeas.util.DateUtil;
import org.silverpeas.util.ForeignPK;
import org.silverpeas.util.LocalizationBundle;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.util.ServiceProvider;
import org.silverpeas.util.SettingBundle;
import org.silverpeas.util.StringUtil;
import org.silverpeas.util.exception.SilverpeasRuntimeException;
import org.silverpeas.util.logging.SilverLogger;
import org.silverpeas.wysiwyg.control.WysiwygController;

import javax.inject.Inject;
import javax.inject.Singleton;
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
      throw new BlogRuntimeException(getClass().getSimpleName() + ".getDateEvent()",
          SilverpeasRuntimeException.ERROR, "blog.EX_GET_DATE_POST", e);
    }
  }

  @Override
  public String createPost(PostDetail post) {
    return createPost(post, null);
  }

  @Override
  public String createPost(final PostDetail post, PdcClassification classification) {
    try (Connection con = openConnection()) {
      // Create publication
      PublicationDetail pub = post.getPublication();
      pub.setStatus(PublicationDetail.DRAFT);
      PublicationPK pk = getPublicationService().createPublication(pub);

      // Create post
      PostDAO.createDateEvent(con, pk.getId(), post.getDateEvent(), pk.getInstanceId());
      if (StringUtil.isDefined(post.getCategoryId())) {
        setCategory(pk, post.getCategoryId());
      }

      // Create empty wysiwyg content
      WysiwygController
          .createUnindexedFileAndAttachment("", pk, pub.getCreatorId(), pub.getLanguage());

      // Create silver content
      createSilverContent(con, pub, pub.getCreatorId());

      // classify the publication on the PdC if its classification is defined
      if (classification != null && !classification.isEmpty()) {
        classification.ofContent(pk.getId());
        pdcClassificationService.classifyContent(pub, classification);
      }

      return pk.getId();
    } catch (Exception e) {
      throw new BlogRuntimeException("DefaultBlogService.createPost()",
          SilverpeasRuntimeException.ERROR, "blog.EX_CREATE_POST", e);
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
        if (organizationController.isComponentAvailable(fatherPK.getInstanceId(), userId)) {
          if (!node.haveRights() || organizationController
              .isObjectAvailable(node.getRightsDependsOn(), ObjectType.NODE,
                  fatherPK.getInstanceId(), userId)) {
            newSubscribers.add(userId);
          }
        }
      }

      if (!newSubscribers.isEmpty()) {
        UserNotificationHelper.buildAndSend(
            new BlogUserNotification(fatherPK.getInstanceId(), post, comment, type, senderId,
                newSubscribers));
      }
    }
  }

  @Override
  public void updatePost(PostDetail post) {
    try (Connection con = openConnection()) {
      PublicationPK pubPk = post.getPublication().getPK();
      PublicationDetail pub = post.getPublication();

      // Remove last category
      getPublicationService().removeAllFather(pubPk);

      // Save the publication
      getPublicationService().setDetail(pub);

      // Add the new category
      if (StringUtil.isDefined(post.getCategoryId())) {
        setCategory(pubPk, post.getCategoryId());
      }

      // Update event date
      PostDAO.updateDateEvent(con, pubPk.getId(), post.getDateEvent());

      // Save wysiwyg content
      if (pub.isValid()) {
        WysiwygController
            .updateFileAndAttachment(post.getContent(), pub.getInstanceId(), pubPk.getId(),
                pub.getUpdaterId(), pub.getLanguage());
      } else if (pub.isDraft()) {//DRAFT mode -> do not index
        WysiwygController
            .updateFileAndAttachment(post.getContent(), pub.getInstanceId(), pubPk.getId(),
                pub.getUpdaterId(), pub.getLanguage(), false);
      }

      // Send notification if subscription
      if (pub.isValid()) {
        sendSubscriptionsNotification(new NodePK("0", pub.getPK().getSpaceId(), pub.getPK().
            getInstanceId()), post, null, "update", pub.getUpdaterId());
      }

    } catch (SQLException e) {
      throw new BlogRuntimeException("DefaultBlogService.updatePost()",
          SilverpeasRuntimeException.ERROR, "blog.EX_UPDATE_POST", e);
    }
  }

  private void setCategory(PublicationPK pk, String categoryId) {
    NodePK nodePK = new NodePK(categoryId, pk.getInstanceId());
    getPublicationService().addFather(pk, nodePK);
  }

  @Override
  public void deletePost(String postId, String instanceId) {

    try (Connection con = openConnection()) {
      PublicationPK pubPK = new PublicationPK(postId, instanceId);
      // Delete link with categorie
      getPublicationService().removeAllFather(pubPK);
      // Delete date event
      PostDAO.deleteDateEvent(con, pubPK.getId());
      // Delete comments
      ForeignPK foreignPK = new ForeignPK(postId, instanceId);
      getCommentService().deleteAllCommentsOnPublication(PostDetail.getResourceType(), foreignPK);
      // Delete wysiwyg content
      WysiwygController.deleteFileAndAttachment(instanceId, postId);
      // Delete publication
      getPublicationService().removePublication(pubPK);
      // Delete silverContent
      getBlogContentManager().deleteSilverContent(con, pubPK);
    } catch (Exception e) {
      throw new BlogRuntimeException("DefaultBlogService.deletePost()",
          SilverpeasRuntimeException.ERROR, "blog.EX_DELETE_POST", e);
    }
  }

  private Connection openConnection() {
    Connection con;
    try {
      con = DBUtil.openConnection();
    } catch (SQLException e) {
      // traitement des exceptions
      throw new BlogRuntimeException("DefaultBlogService.openConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
    return con;
  }

  private PostDetail getPost(PublicationDetail publication) {
    try {
      Collection<NodePK> allCat = getPublicationService().getAllFatherPK(publication.getPK());
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
      throw new BlogRuntimeException("DefaultBlogService.getPost()",
          SilverpeasRuntimeException.ERROR, "blog.EX_CONSTRUCT_POST", e);
    }
  }

  @Override
  public Collection<PostDetail> getAllPosts(String instanceId) {
    PublicationPK pubPK = new PublicationPK("useless", instanceId);

    Collection<PostDetail> posts = new ArrayList<>();
    try(Connection con = openConnection()) {
      // rechercher les publications classée par date d'évènement
      Collection<String> lastEvents = PostDAO.getAllEvents(con, instanceId);
      Collection<PublicationDetail> publications =
          getPublicationService().getAllPublications(pubPK);
      for (String pubId : lastEvents) {
        for (PublicationDetail publication : publications) {
          if (publication.getPK().getId().equals(pubId)) {
            posts.add(getPost(publication));
          }
        }
      }
      return posts;
    } catch (Exception e) {
      throw new BlogRuntimeException("DefaultBlogService.getAllPosts()",
          SilverpeasRuntimeException.ERROR, "blog.EX_GET_ALL_POST", e);
    }
  }

  @Override
  public Collection<PostDetail> getAllValidPosts(String instanceId, int nbReturned) {
    PublicationPK pubPK = new PublicationPK("useless", instanceId);

    Collection<PostDetail> posts = new ArrayList<>();
    try (Connection con = openConnection()) {
      // rechercher les publications classées par date d'évènement
      Collection<String> lastEvents = PostDAO.getAllEvents(con, instanceId);
      Collection<PublicationDetail> publications =
          getPublicationService().getAllPublications(pubPK);
      for (String pubId : lastEvents) {
        for (PublicationDetail publication : publications) {
          if (publication.getPK().getId().equals(pubId) && PublicationDetail.VALID.
              equals(publication.getStatus()) && nbReturned > 0) {
            nbReturned--;
            posts.add(getPost(publication));
          }
        }
      }
      return posts;
    } catch (Exception e) {
      throw new BlogRuntimeException("DefaultBlogService.getAllValidPosts()",
          SilverpeasRuntimeException.ERROR, "blog.EX_GET_ALL_VALID_POST", e);
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
      throw new BlogRuntimeException("DefaultBlogService.getPostsByCategory()",
          SilverpeasRuntimeException.ERROR, "blog.EX_GET_ALL_POST_BY_CATEGORY", e);
    }
  }

  @Override
  public Collection<PostDetail> getPostsByDate(String date, String instanceId) {
    return getPostsByArchive(date, date, instanceId);
  }

  @Override
  public Collection<PostDetail> getPostsByArchive(String beginDate, String endDate,
      String instanceId) {


    PublicationPK pubPK = new PublicationPK("useless", instanceId);
    Collection<PostDetail> posts = new ArrayList<>();
    try (Connection con = openConnection()) {
      // rechercher les publications classée par date d'évènement
      Collection<String> lastEvents = PostDAO.getEventsByDates(con, instanceId, beginDate, endDate);

      Collection<PublicationDetail> publications =
          getPublicationService().getAllPublications(pubPK);
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
      throw new BlogRuntimeException("DefaultBlogService.getPostsByArchive()",
          SilverpeasRuntimeException.ERROR, "blog.EX_GET_ALL_POST_BY_ARCHIVE", e);
    }
  }

  @Override
  public Collection<PostDetail> getResultSearch(String word, String userId, String spaceId,
      String instanceId) {
    Collection<PostDetail> posts = new ArrayList<>();
    List<String> postIds = new ArrayList<>();

    QueryDescription query = new QueryDescription(word);
    query.setSearchingUser(userId);
    query.addSpaceComponentPair(spaceId, instanceId);

    try (Connection con = openConnection()) {
      List<MatchingIndexEntry> result = SearchEngineProvider.getSearchEngine().search(query).
          getEntries();


      // création des billets à partir des résultats
      // rechercher la liste des posts trié par date
      Collection<String> allEvents = PostDAO.getAllEvents(con, instanceId);
      for (final String pubId : allEvents) {
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
          SilverpeasRuntimeException.ERROR, "blog.EX_GET_ALL_POST_BY_SEARCH", e);
    }
    return posts;
  }

  @Override
  public String createCategory(Category category) {
    try {
      NodePK nodePK = getNodeBm().createNode(category, new NodeDetail());
      return nodePK.getId();
    } catch (Exception e) {
      throw new BlogRuntimeException("DefaultBlogService.createCategory()",
          SilverpeasRuntimeException.ERROR, "blog.EX_CREATE_CATEGORY", e);
    }
  }

  @Override
  public void updateCategory(Category category) {
    try {
      getNodeBm().setDetail(category);
    } catch (Exception e) {
      throw new BlogRuntimeException("DefaultBlogService.updateCategory()",
          SilverpeasRuntimeException.ERROR, "blog.EX_UPDATE_CATEGORY", e);
    }
  }

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
      throw new BlogRuntimeException("DefaultBlogService.deleteCategory()",
          SilverpeasRuntimeException.ERROR, "blog.EX_DELETE_CATEGORY", e);
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
      throw new BlogRuntimeException("DefaultBlogService.getAllArchives()",
          SilverpeasRuntimeException.ERROR, "blog.EX_GET_ALL_ARCHIVE", e);
    }
  }

  private Archive createArchive(Calendar calendar) {
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
    Collection<PublicationDetail> pubs;
    try {
      pubs = getPublicationService().getAllPublications(pubPK);
    } catch (Exception e) {
      throw new BlogRuntimeException("DefaultBlogService.indexPublications()",
          SilverpeasRuntimeException.ERROR, "blog.EX_GET_ALL_PUBLICATION", e);
    }

    if (pubs != null) {
      for (PublicationDetail pub : pubs) {
        try {
          indexPublication(pub.getPK());
        } catch (Exception e) {
          throw new BlogRuntimeException("DefaultBlogService.indexPublications()",
              SilverpeasRuntimeException.ERROR, "blog.EX_INDEX_PUBLICATION",
              "pubPK = " + pub.getPK().toString(), e);
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
  public void addSubscription(final String userId, final String instanceId) {
    getSubscribeService().subscribe(new ComponentSubscription(userId, instanceId));
  }

  @Override
  public void removeSubscription(final String userId, final String instanceId) {
    getSubscribeService().unsubscribe(new ComponentSubscription(userId, instanceId));
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
    if (PublicationDetail.DRAFT.equals(pubDetail.getStatus())) {
      pubDetail.setIndexOperation(IndexManager.NONE);
    }
    getPublicationService().setDetail(pubDetail);
    // envoie notification si abonnement
    if (pubDetail.getStatus().equals(PublicationDetail.VALID)) {
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

  private BlogContentManager getBlogContentManager() {
    return new BlogContentManager();
  }

  private int createSilverContent(Connection con, PublicationDetail pubDetail, String creatorId) {
    try {
      return getBlogContentManager().createSilverContent(con, pubDetail, creatorId);
    } catch (ContentManagerException e) {
      throw new BlogRuntimeException("DefaultBlogService.createSilverContent()",
          SilverpeasRuntimeException.ERROR, "blog.EX_CREATE_CONTENT_PDC", e);
    }
  }

  private void updateSilverContentVisibility(PublicationDetail pubDetail) {
    try {
      getBlogContentManager().updateSilverContentVisibility(pubDetail);
    } catch (Exception e) {
      throw new BlogRuntimeException("DefaultBlogService.updateSilverContentVisibility()",
          SilverpeasRuntimeException.ERROR, "blog.EX_UPDATE_CONTENT_PDC", e);
    }
  }

  @Override
  public void draftOutPost(PostDetail post) {

    PublicationDetail pub = post.getPublication();
    pub.setStatus(PublicationDetail.VALID);

    // update the publication
    getPublicationService().setDetail(pub);

    if (pub.getStatus().equals(PublicationDetail.VALID)) {

      // index wysiwyg content
      WysiwygController.updateFileAndAttachment(
          WysiwygController.load(pub.getInstanceId(), pub.getPK().getId(), pub.getLanguage()),
          pub.getInstanceId(), pub.getPK().getId(), pub.getUpdaterId(), pub.getLanguage());

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
      return getBlogContentManager().createSilverContent(con, pubDetail, creatorId);
    } catch (Exception e) {
      throw new BlogRuntimeException("DefaultBlogService.createSilverContent()",
          SilverpeasRuntimeException.ERROR, "blog.EX_CREATE_CONTENT_PDC", e);
    } finally {
      DBUtil.close(con);
    }
  }


  private int getSilverObjectId(PublicationPK pubPK) {

    int silverObjectId;
    PublicationDetail pubDetail;
    try {
      silverObjectId =
          getBlogContentManager().getSilverObjectId(pubPK.getId(), pubPK.getInstanceId());
      if (silverObjectId == -1) {
        pubDetail = getPublicationService().getDetail(pubPK);
        silverObjectId = createSilverContent(pubDetail, pubDetail.getCreatorId());
      }
    } catch (Exception e) {
      throw new BlogRuntimeException("DefaultBlogService.getSilverObjectId()",
          SilverpeasRuntimeException.ERROR, "blog.EX_GET_CONTENT_PDC", e);
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
