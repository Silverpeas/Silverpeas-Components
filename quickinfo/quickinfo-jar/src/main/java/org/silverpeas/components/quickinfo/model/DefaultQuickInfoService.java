/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.quickinfo.model;

import com.silverpeas.SilverpeasComponentService;
import com.silverpeas.annotation.Service;
import com.silverpeas.comment.service.CommentService;
import com.silverpeas.comment.service.CommentUserNotificationService;
import com.silverpeas.delegatednews.service.DelegatedNewsService;
import com.silverpeas.delegatednews.service.ServicesFactory;
import com.silverpeas.notification.builder.helper.UserNotificationHelper;
import com.silverpeas.pdc.PdcServiceFactory;
import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.model.PdcPosition;
import com.silverpeas.pdc.service.PdcClassificationService;
import com.silverpeas.thumbnail.control.ThumbnailController;
import com.silverpeas.thumbnail.model.ThumbnailDetail;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.CompoSpace;
import com.stratelia.webactiv.quickinfo.QuickInfoContentManager;
import com.stratelia.webactiv.quickinfo.control.QuickInfoDateComparatorDesc;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.statistic.control.StatisticBm;
import org.silverpeas.attachment.AttachmentService;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.attachment.util.SimpleDocumentList;
import org.silverpeas.authentication.UserAuthenticationListener;
import org.silverpeas.authentication.UserAuthenticationListenerRegistration;
import org.silverpeas.components.quickinfo.NewsByStatus;
import org.silverpeas.components.quickinfo.QuickInfoComponentSettings;
import org.silverpeas.components.quickinfo.QuickInfoUserAuthenticationListener;
import org.silverpeas.components.quickinfo.notification.QuickInfoSubscriptionUserNotification;
import org.silverpeas.components.quickinfo.repository.NewsRepository;
import org.silverpeas.core.admin.OrganisationControllerFactory;
import org.silverpeas.persistence.Transaction;
import org.silverpeas.persistence.repository.OperationContext;
import org.silverpeas.search.indexEngine.model.IndexManager;
import org.silverpeas.wysiwyg.control.WysiwygController;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.silverpeas.pdc.model.PdcClassification.aPdcClassificationOfContent;

@Service
public class DefaultQuickInfoService implements QuickInfoService, SilverpeasComponentService<News> {

  @Inject
  private NewsRepository newsRepository;

  @Inject
  private CommentUserNotificationService commentUserNotificationService;

  @Inject
  private CommentService commentService;

  private UserAuthenticationListener userAuthenticationListener;

  @Override
  public News getContentById(String contentId) {
    return getNews(contentId);
  }

  @Override
  public List<News> getVisibleNews(String componentId) {
    SilverTrace.info("quickinfo", "DefaultQuickInfoService.getVisibleNews()",
        "root.MSG_GEN_ENTER_METHOD", "componentId = " + componentId);
    List<News> quickinfos = getAllNews(componentId);
    List<News> result = new ArrayList<News>();
    for (News news : quickinfos) {
      if (news.isVisible() && !news.isDraft()) {
        result.add(news);
      }
    }
    sortByDateDesc(result);
    return result;
  }

  @Override
  public List<News> getAllNews(String componentId) {
    List<News> allNews = newsRepository.getByComponentId(componentId);
    boolean delegateNewsEnabled = isDelegatedNewsActivated(componentId);
    for (News aNews : allNews) {
      PublicationDetail publication = getPublication(aNews);
      aNews.setPublication(publication);
      if (delegateNewsEnabled) {
        setDelegatedNews(aNews, publication);
      }
    }
    return allNews;
  }

  @Override
  public NewsByStatus getAllNewsByStatus(String componentId, String userId) {
    return new NewsByStatus(getAllNews(componentId), userId);
  }

  @Override
  public News getNews(String id) {
    News news = newsRepository.getById(id);
    PublicationDetail publication = getPublication(news);
    news.setPublication(publication);
    setDelegatedNews(news, publication);
    return news;
  }

  private void setDelegatedNews(News news, PublicationDetail publication) {
    news.setDelegatedNews(getDelegatedNewsService().getDelegatedNews(
        Integer.parseInt(publication.getId())));
  }

  @Override
  public News getNewsByForeignId(String foreignId) {
    News news = newsRepository.getByForeignId(foreignId);
    PublicationDetail publication = getPublication(news);
    news.setPublication(publication);
    return news;
  }

  @Override
  public void acknowledgeNews(String id, String userId) {
    News news = newsRepository.getById(id);
    if (news != null) {
      getStatisticService().addStat(userId, news);
    }
  }

  private PublicationDetail getPublication(News news) {
    return getPublicationBm().getDetail(news.getForeignPK());
  }

  @Override
  public ResourceLocator getComponentSettings() {
    return QuickInfoComponentSettings.getSettings();
  }

  @Override
  public ResourceLocator getComponentMessages(String language) {
    return QuickInfoComponentSettings.getMessagesIn(language);
  }

  @Override
  public News create(final News news) {
    ForeignPK volatileAttachmentSourcePK =
        new ForeignPK(news.getPublicationId(), news.getComponentInstanceId());

    // Creating publication
    final PublicationDetail publication = news.getPublication();
    publication.setIndexOperation(IndexManager.NONE);
    final PublicationPK pubPK = getPublicationBm().createPublication(publication);
    publication.setPk(pubPK);

    News savedNews = Transaction.performInOne(new Transaction.Process<News>() {
      @Override
      public News execute() {
        news.setId(null);
        news.setPublicationId(pubPK.getId());
        return newsRepository.save(OperationContext.fromUser(publication.getCreatorId()), news);
      }
    });

    // Attaching all documents linked to volatile news to the persisted news
    List<SimpleDocumentPK> movedDocumentPks = AttachmentServiceFactory.getAttachmentService()
        .moveAllDocuments(volatileAttachmentSourcePK, savedNews.getPublication().getPK());
    if (!movedDocumentPks.isEmpty()) {
      // Change images path in wysiwyg
      WysiwygController.wysiwygPlaceHaveChanged(news.getComponentInstanceId(),
          volatileAttachmentSourcePK.getId(), news.getComponentInstanceId(), savedNews.getId());
    }

    // Referring new content into taxonomy
    try {
      new QuickInfoContentManager()
          .createSilverContent(null, publication, publication.getCreatorId(), false);
    } catch (ContentManagerException e) {
      SilverTrace
          .error("quickinfo", "DefaultQuickInfoService.addNews()", "root.ContentManagerException",
              e);
    }

    return savedNews;
  }

  private void publish(final News news) {
    news.setPublished();
    news.setPublishDate(new Date());

    Transaction.performInOne(new Transaction.Process<News>() {
      @Override
      public News execute() {
        return newsRepository.save(OperationContext.fromUser(news.getPublishedBy()), news);
      }
    });

    PublicationDetail publication = news.getPublication();
    getPublicationBm().setDetail(publication, false);

    try {
      new QuickInfoContentManager().updateSilverContentVisibility(publication, true);
    } catch (ContentManagerException e) {
      SilverTrace.error("quickinfo", "DefaultQuickInfoService.publish()", "root.ContentManagerException", e);
    }

    if (news.isVisible()) {
      // Sending notifications to subscribers
      UserNotificationHelper.buildAndSend(new QuickInfoSubscriptionUserNotification(news, NotifAction.CREATE));
    }
  }

  @Override
  public void publish(String id, String userId) {
    News news = getNews(id);
    news.setPublishedBy(userId);
    publish(news);
  }

  @Override
  public void update(final News news, List<PdcPosition> positions, final boolean forcePublishing) {
    final PublicationDetail publication = news.getPublication();

    // saving WYSIWYG content
    WysiwygController.save(news.getContent(), news.getComponentInstanceId(),
        news.getPublicationId(), publication.getUpdaterId(), I18NHelper.defaultLanguage, false);

    // Updating the publication
    if (news.isDraft()) {
      publication.setIndexOperation(IndexManager.NONE);
    }
    getPublicationBm().setDetail(publication);

    Transaction.performInOne(new Transaction.Process<News>() {
      @Override
      public News execute() {
        news.setPublicationId(publication.getId());
        if (forcePublishing) {
          news.setPublishDate(new Date());
          news.setPublishedBy(news.getLastUpdatedBy());
        }
        return newsRepository.save(OperationContext.fromUser(news.getLastUpdatedBy()), news);
      }
    });

    // Updating visibility onto taxonomy
    try {
      new QuickInfoContentManager().updateSilverContentVisibility(publication, !news.isDraft());
    } catch (ContentManagerException e) {
      SilverTrace.error("quickinfo", "DefaultQuickInfoService.update()",
          "root.ContentManagerException", e);
    }

    // Classifying new content onto taxonomy
    classifyQuickInfo(publication, positions);

    if (!news.isDraft() && news.isVisible()) {
      // Sending notifications to subscribers
      NotifAction action = NotifAction.UPDATE;
      if (forcePublishing) {
        action = NotifAction.CREATE;
      }
      UserNotificationHelper.buildAndSend(new QuickInfoSubscriptionUserNotification(news, action));
    }

    if (isDelegatedNewsActivated(news.getComponentInstanceId())) {
      getDelegatedNewsService().updateDelegatedNews(publication.getId(), news,
          publication.getUpdaterId(), news.getVisibilityPeriod());
    }
  }

  @Override
  public void removeNews(final String id) {
    News news = getNews(id);

    PublicationPK foreignPK = news.getForeignPK();

    // Deleting publication
    getPublicationBm().removePublication(foreignPK);

    // De-reffering contribution in taxonomy
    Connection connection = DBUtil.makeConnection(JNDINames.SILVERPEAS_DATASOURCE);
    try {
      new QuickInfoContentManager().deleteSilverContent(connection, foreignPK);
    } catch (ContentManagerException e) {
      SilverTrace.error("quickinfo", "DefaultQuickInfoService.removeNews",
          "ContentManagerExceptino", e);
    } finally {
      DBUtil.close(connection);
    }

    // Deleting all attached files (WYSIWYG, WYSIWYG images...)
    AttachmentService attachmentService = AttachmentServiceFactory.getAttachmentService();
    SimpleDocumentList<SimpleDocument> docs =
        attachmentService.listAllDocumentsByForeignKey(foreignPK, null);
    for (SimpleDocument document : docs) {
      attachmentService.deleteAttachment(document);
    }

    // Deleting thumbnail
    ThumbnailDetail thumbnail =
        new ThumbnailDetail(foreignPK.getInstanceId(), Integer.parseInt(foreignPK.getId()),
            ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE);
    ThumbnailController.deleteThumbnail(thumbnail);

    // Deleting comments
    commentService.deleteAllCommentsOnPublication(News.CONTRIBUTION_TYPE, news.getPK());

    // deleting statistics
    getStatisticService().deleteStats(news);

    // deleting delegated news
    getDelegatedNewsService().deleteDelegatedNews(Integer.parseInt(foreignPK.getId()));

    // deleting news itself
    Transaction.performInOne(new Transaction.Process<Long>() {
      @Override
      public Long execute() {
        return Long.valueOf(newsRepository.deleteById(id));
      }
    });
  }

  @Override
  public List<News> getPlatformNews(String userId) {
    SilverTrace.info("quickinfo", "DefaultQuickInfoService.getPlatformNews()",
        "root.MSG_GEN_PARAM_VALUE", "Enter Get All Quick Info : User=" + userId);
    List<News> result = new ArrayList<News>();
    CompoSpace[] compoSpaces =
        OrganisationControllerFactory.getOrganisationController().getCompoForUser(userId,
            QuickInfoComponentSettings.COMPONENT_NAME);
    for (CompoSpace compoSpace : compoSpaces) {
      String componentId = compoSpace.getComponentId();
      try {
        result.addAll(getVisibleNews(componentId));
      } catch (Exception e) {
        SilverTrace.error("quickinfo", "DefaultQuickInfoService.getPlatformNews()",
            "quickinfo.CANT_GET_QUICKINFOS", componentId, e);
      }
    }
    return sortByDateDesc(result);
  }

  @Override
  public List<News> getNewsForTicker(String userId) {
    List<News> allNews = getPlatformNews(userId);
    List<News> forTicker = new ArrayList<News>();
    for (News news : allNews) {
      if (news.isTicker()) {
        forTicker.add(news);
      }
    }
    return forTicker;
  }

  @Override
  public List<News> getUnreadBlockingNews(String userId) {
    List<News> allNews = getPlatformNews(userId);
    List<News> result = new ArrayList<News>();
    for (News news : allNews) {
      if (news.isMandatory() && !getStatisticService().isRead(news, userId)) {
        result.add(news);
      }
    }
    return result;
  }

  public void submitNewsOnHomepage(String id, String userId) {
    News news = getNews(id);
    getDelegatedNewsService().submitNews(news.getPublicationId(), news, news.getUpdaterId(),
        news.getVisibilityPeriod(), userId);
  }

  private List<News> sortByDateDesc(List<News> listOfNews) {
    Comparator<News> comparator = QuickInfoDateComparatorDesc.comparator;
    Collections.sort(listOfNews, comparator);
    return listOfNews;
  }


  private PublicationBm getPublicationBm() {
    return EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME, PublicationBm.class);
  }

  /**
   * Classify the info letter publication on the PdC only if the positions parameter is filled
   *
   * @param publi the quickInfo PublicationDetail to classify
   * @param pdcPositions the string json positions
   */
  private void classifyQuickInfo(PublicationDetail publi, List<PdcPosition> pdcPositions) {
    if (pdcPositions != null && !pdcPositions.isEmpty()) {
      String qiId = publi.getPK().getId();
      PdcClassification classification = aPdcClassificationOfContent(qiId,
            publi.getInstanceId()).withPositions(pdcPositions);
      if (!classification.isEmpty()) {
        PdcClassificationService service =
              PdcServiceFactory.getFactory().getPdcClassificationService();
        classification.ofContent(qiId);
        service.classifyContent(publi, classification);
      }
    }
  }

  /**
   * Initializes the component by setting some transversal core services for their
   * use by the component instances. One of these services is the user comment notification.
   */
  @PostConstruct
  public void initialize() {
    commentUserNotificationService.register(QuickInfoComponentSettings.COMPONENT_NAME, this);
    userAuthenticationListener = new QuickInfoUserAuthenticationListener();
    UserAuthenticationListenerRegistration.register(userAuthenticationListener);
  }

  /**
   * Releases the uses of the transverse core services that were used by the instances of the
   * component.
   */
  @PreDestroy
  public void release() {
    commentUserNotificationService.unregister(QuickInfoComponentSettings.COMPONENT_NAME);
    UserAuthenticationListenerRegistration.unregister(userAuthenticationListener);
  }

  private StatisticBm getStatisticService() {
    return EJBUtilitaire.getEJBObjectRef(JNDINames.STATISTICBM_EJBHOME, StatisticBm.class);
  }

  private DelegatedNewsService getDelegatedNewsService() {
    return ServicesFactory.getDelegatedNewsService();
  }

  private boolean isDelegatedNewsActivated(String componentId) {
    String paramValue =
        OrganisationControllerFactory.getOrganisationController().getComponentParameterValue(
            componentId, QuickInfoComponentSettings.PARAM_DELEGATED);
    return StringUtil.getBooleanValue(paramValue);
  }

}
