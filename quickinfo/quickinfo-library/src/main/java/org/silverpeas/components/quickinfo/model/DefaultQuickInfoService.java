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

package org.silverpeas.components.quickinfo.model;

import org.silverpeas.components.delegatednews.service.DelegatedNewsService;
import org.silverpeas.components.delegatednews.service.DelegatedNewsServiceProvider;
import org.silverpeas.components.quickinfo.NewsByStatus;
import org.silverpeas.components.quickinfo.QuickInfoComponentSettings;
import org.silverpeas.components.quickinfo.notification.QuickInfoSubscriptionUserNotification;
import org.silverpeas.components.quickinfo.repository.NewsRepository;
import org.silverpeas.components.quickinfo.service.QuickInfoContentManager;
import org.silverpeas.components.quickinfo.service.QuickInfoDateComparatorDesc;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.component.model.CompoSpace;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.comment.service.CommentService;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.Attachments;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.util.SimpleDocumentList;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.index.indexing.model.IndexManager;
import org.silverpeas.core.io.media.image.thumbnail.control.ThumbnailController;
import org.silverpeas.core.io.media.image.thumbnail.model.ThumbnailDetail;
import org.silverpeas.core.io.upload.UploadedFile;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.model.PdcPosition;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.silverstatistics.access.service.StatisticService;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static org.silverpeas.core.pdc.pdc.model.PdcClassification.aPdcClassificationOfContent;
import static org.silverpeas.core.persistence.Transaction.performInOne;

@Singleton
public class DefaultQuickInfoService implements QuickInfoService {

  @Inject
  private NewsRepository newsRepository;

  @Inject
  private CommentService commentService;

  @Inject
  private QuickInfoContentManager quickInfoContentManager;

  @Override
  public News getContentById(String contentId) {
    return getNews(contentId);
  }

  @Override
  public List<News> getVisibleNews(String componentId) {
    List<News> quickinfos = getAllNews(componentId);
    List<News> result = new ArrayList<>();
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
      decorateNewsWithPublication(aNews, delegateNewsEnabled);
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
    decorateNewsWithPublication(news, true);
    return news;
  }

  @Override
  public News getNewsByForeignId(String foreignId) {
    News news = newsRepository.getByForeignId(foreignId);
    decorateNewsWithPublication(news, false);
    return news;
  }

  @Override
  public void acknowledgeNews(String id, String userId) {
    News news = newsRepository.getById(id);
    if (news != null) {
      getStatisticService().addStat(userId, news);
    }
  }

  @Override
  public SettingBundle getComponentSettings() {
    return QuickInfoComponentSettings.getSettings();
  }

  @Override
  public LocalizationBundle getComponentMessages(String language) {
    return QuickInfoComponentSettings.getMessagesIn(language);
  }

  @Override
  public boolean isRelatedTo(final String instanceId) {
    return instanceId.startsWith(QuickInfoComponentSettings.COMPONENT_NAME);
  }

  @Override
  public News create(final News news) {
    ResourceReference volatileAttachmentSourcePK =
        new ResourceReference(news.getPublicationId(), news.getComponentInstanceId());

    // Creating publication
    final PublicationDetail publication = news.getPublication();
    publication.setIndexOperation(IndexManager.NONE);
    final PublicationPK pubPK = getPublicationService().createPublication(publication);
    publication.setPk(pubPK);

    News savedNews = performInOne(() -> {
      news.setId(null);
      news.setPublicationId(pubPK.getId());
      news.createdBy(publication.getCreatorId());
      return newsRepository.save(news);
    });

    // Attaching all documents linked to volatile news to the persisted news
    List<SimpleDocumentPK> movedDocumentPks = AttachmentServiceProvider.getAttachmentService()
        .moveAllDocuments(volatileAttachmentSourcePK, savedNews.getPublication().getPK());
    if (!movedDocumentPks.isEmpty()) {
      // Change images path in wysiwyg
      WysiwygController.wysiwygPlaceHaveChanged(news.getComponentInstanceId(),
          volatileAttachmentSourcePK.getId(), news.getComponentInstanceId(), savedNews.getId());
    }

    // Referring new content into taxonomy
    try {
      quickInfoContentManager
          .createSilverContent(null, publication, publication.getCreatorId(), false);
    } catch (ContentManagerException e) {
      SilverLogger.getLogger(this).error(
          "can not create a silver-content for publication " + publication.getId() +
              " associated to the saved news " + savedNews.getId(), e);
    }

    return savedNews;
  }

  @Override
  public void publish(String id, String userId) {
    News news = getNews(id);
    news.setPublishedBy(userId);
    publish(news);
  }

  @Override
  public void update(final News news, List<PdcPosition> positions,
      Collection<UploadedFile> uploadedFiles, final boolean forcePublishing) {
    final PublicationDetail publication = news.getPublication();

    // saving WYSIWYG content
    WysiwygController.save(news.getContentToStore(), news.getComponentInstanceId(),
        news.getPublicationId(),
            publication.getUpdaterId(), I18NHelper.defaultLanguage, false);

    // Attach uploaded files
    Attachments.from(uploadedFiles).attachTo(news);

    // Updating the publication
    if (news.isDraft()) {
      publication.setIndexOperation(IndexManager.NONE);
    }
    getPublicationService().setDetail(publication);

    performInOne(() -> {
      news.setPublicationId(publication.getId());
      if (forcePublishing) {
        news.setPublishDate(new Date());
        news.setPublishedBy(news.getLastUpdaterId());
      }
      return newsRepository.save(news);
    });

    // Updating visibility onto taxonomy
    try {
      quickInfoContentManager.updateSilverContentVisibility(publication, !news.isDraft());
    } catch (ContentManagerException e) {
      SilverLogger.getLogger(this).error(
          "can not update the silver-content of the publication " + publication.getId() +
              " associated to the news " + news.getId(), e);
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
      getDelegatedNewsService()
          .updateDelegatedNews(publication.getId(), news, publication.getUpdaterId(),
              news.getVisibilityPeriod());
    }
  }

  @Override
  public void removeNews(final String id) {
    News news = getNews(id);

    PublicationPK foreignPK = news.getForeignPK();

    // Deleting publication
    getPublicationService().removePublication(foreignPK);

    // De-reffering contribution in taxonomy
    try (Connection connection = DBUtil.openConnection()) {
      quickInfoContentManager.deleteSilverContent(connection, foreignPK);
    } catch (ContentManagerException | SQLException e) {
      SilverLogger.getLogger(this).error(
          "can not delete the silver-content of the publication " + foreignPK.getId() +
              " associated to the news " + news.getId(), e);
    }


    // Deleting all attached files (WYSIWYG, WYSIWYG images...)
    AttachmentService attachmentService = AttachmentServiceProvider.getAttachmentService();
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
    performInOne(() -> newsRepository.deleteById(id));
  }

  @Override
  public List<News> getPlatformNews(String userId) {
    SilverLogger.getLogger(this).debug("Enter Get All Quick Info : User=" + userId);
    List<News> result = new ArrayList<>();
    CompoSpace[] compoSpaces = OrganizationControllerProvider.getOrganisationController()
        .getCompoForUser(userId, QuickInfoComponentSettings.COMPONENT_NAME);
    for (CompoSpace compoSpace : compoSpaces) {
      String componentId = compoSpace.getComponentId();
      try {
        result.addAll(getVisibleNews(componentId));
      } catch (Exception e) {
        SilverLogger.getLogger(this).error("can get visible news of " + componentId, e);
      }
    }
    return sortByDateDesc(result);
  }

  @Override
  public List<News> getNewsForTicker(String userId) {
    List<News> tickerNews = newsRepository.getTickerNews();
    if (tickerNews.isEmpty()) {
      return tickerNews;
    }

    List<News> forTicker = new ArrayList<>();
    for (News news : tickerNews) {
      decorateNewsWithPublication(news, false);
      if (!news.isDraft() && news.isVisible() && news.canBeAccessedBy(User.getById(userId))) {
        forTicker.add(news);
      }
    }
    return sortByDateDesc(forTicker);
  }

  @Override
  public List<News> getUnreadBlockingNews(String userId) {
    List<News> blockingNews = newsRepository.getBlockingNews();
    if (blockingNews.isEmpty()) {
      return blockingNews;
    }
    List<News> result = new ArrayList<>();
    for (News news : blockingNews) {
      decorateNewsWithPublication(news, false);
      if (!news.isDraft() && news.isVisible() && news.canBeAccessedBy(User.getById(userId)) &&
          !getStatisticService().isRead(news, userId)) {
        result.add(news);
      }
    }
    return sortByDateDesc(result);
  }

  public void submitNewsOnHomepage(String id, String userId) {
    News news = getNews(id);
    getDelegatedNewsService()
        .submitNews(news.getPublicationId(), news, news.getUpdaterId(), news.getVisibilityPeriod(),
            userId);
  }

  private void publish(final News news) {
    news.setPublished();
    news.setPublishDate(new Date());
    news.lastUpdatedBy(news.getPublishedBy());
    performInOne(() -> newsRepository.save(news));

    PublicationDetail publication = news.getPublication();
    getPublicationService().setDetail(publication, false);

    try {
      quickInfoContentManager.updateSilverContentVisibility(publication, true);
    } catch (ContentManagerException e) {
      SilverLogger.getLogger(this)
          .error("can not update the silver-content of the publication " + publication.getId() +
              " associated to the news " + news.getId(), e);
    }

    if (news.isVisible()) {
      // Sending notifications to subscribers
      UserNotificationHelper.buildAndSend(
          new QuickInfoSubscriptionUserNotification(news, NotifAction.CREATE));
    }
  }

  private List<News> sortByDateDesc(List<News> listOfNews) {
    Comparator<News> comparator = QuickInfoDateComparatorDesc.comparator;
    Collections.sort(listOfNews, comparator);
    return listOfNews;
  }

  private void setDelegatedNews(News news, PublicationDetail publication) {
    news.setDelegatedNews(
        getDelegatedNewsService().getDelegatedNews(Integer.parseInt(publication.getId())));
  }

  /**
   * Classify the info letter publication on the PdC only if the positions parameter is filled
   * @param publi the quickInfo PublicationDetail to classify
   * @param pdcPositions the string json positions
   */
  private void classifyQuickInfo(PublicationDetail publi, List<PdcPosition> pdcPositions) {
    if (pdcPositions != null && !pdcPositions.isEmpty()) {
      PdcClassification classification =
          aPdcClassificationOfContent(publi).withPositions(pdcPositions);
      classification.classifyContent(publi);
    }
  }

  private boolean isDelegatedNewsActivated(String componentId) {
    String paramValue = OrganizationControllerProvider.getOrganisationController()
        .getComponentParameterValue(componentId, QuickInfoComponentSettings.PARAM_DELEGATED);
    return StringUtil.getBooleanValue(paramValue);
  }

  private void decorateNewsWithPublication(News news, boolean delegated) {
    PublicationDetail publication = getPublicationService().getDetail(news.getForeignPK());
    news.setPublication(publication);
    if (delegated) {
      setDelegatedNews(news, publication);
    }
  }

  private PublicationService getPublicationService() {
    return ServiceProvider.getService(PublicationService.class);
  }

  private StatisticService getStatisticService() {
    return ServiceProvider.getService(StatisticService.class);
  }

  private DelegatedNewsService getDelegatedNewsService() {
    return DelegatedNewsServiceProvider.getDelegatedNewsService();
  }

}
