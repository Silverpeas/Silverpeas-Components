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
import org.silverpeas.components.quickinfo.notification.NewsEventNotifier;
import org.silverpeas.components.quickinfo.notification.QuickInfoDelayedVisibilityUserNotificationReminder;
import org.silverpeas.components.quickinfo.notification.QuickInfoSubscriptionUserNotification;
import org.silverpeas.components.quickinfo.repository.NewsRepository;
import org.silverpeas.components.quickinfo.service.QuickInfoContentManager;
import org.silverpeas.components.quickinfo.service.QuickInfoDateComparatorDesc;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.Attachments;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.index.indexing.model.IndexManager;
import org.silverpeas.core.io.upload.UploadedFile;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.model.PdcPosition;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.reminder.Reminder;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.silverstatistics.access.service.StatisticService;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.silverpeas.components.quickinfo.notification.QuickInfoDelayedVisibilityUserNotificationReminder.QUICKINFO_DELAYED_VISIBILITY_USER_NOTIFICATION;
import static org.silverpeas.core.pdc.pdc.model.PdcClassification.aPdcClassificationOfContent;
import static org.silverpeas.core.persistence.Transaction.performInOne;

@Service
@Named("quickInfoService")
public class DefaultQuickInfoService implements QuickInfoService {

  private static final String ASSOCIATED_TO_THE_NEWS_MSG = " associated to the news ";
  private static final Predicate<News> VISIBLE_PREDICATE = n -> !n.isDraft() && n.isVisible();

  @Inject
  private NewsRepository newsRepository;
  @Inject
  private QuickInfoContentManager quickInfoContentManager;
  @Inject
  private NewsEventNotifier notifier;

  @Override
  public Optional<News> getContributionById(ContributionIdentifier contributionId) {
    return Optional.of(getNews(contributionId.getLocalId()));
  }

  @Override
  public List<News> getVisibleNews(String componentId) {
    return getAllNews(componentId).stream()
        .filter(VISIBLE_PREDICATE)
        .sorted(QuickInfoDateComparatorDesc.comparator)
        .collect(Collectors.toList());
  }

  @Override
  public List<News> getAllNews(String componentId) {
    final List<News> allNews = newsRepository.getByComponentId(componentId);
    final boolean delegateNewsEnabled = isDelegatedNewsActivated(componentId);
    decorateNews(allNews, delegateNewsEnabled);
    return allNews;
  }

  @Override
  public NewsByStatus getAllNewsByStatus(String componentId, String userId) {
    return new NewsByStatus(getAllNews(componentId), userId);
  }

  @Override
  public News getNews(String id) {
    final News news = newsRepository.getById(id);
    decorateNews(singletonList(news), true);
    return news;
  }

  @Override
  public News getNewsByForeignId(String foreignId) {
    final News news = newsRepository.getByForeignId(foreignId);
    decorateNews(singletonList(news), true);
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
        .moveAllDocuments(volatileAttachmentSourcePK,
            savedNews.getPublication().getPK().toResourceReference());
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
    final News before = Transaction.performInNew(() -> getNews(news.getId()));

    final PublicationDetail publication = news.getPublication();

    // saving WYSIWYG content
    WysiwygController.save(news.getContentToStore(), news.getComponentInstanceId(),
        news.getPublicationId(),
            publication.getUpdaterId(), I18NHelper.DEFAULT_LANGUAGE, false);

    // Attach uploaded files
    Attachments.from(uploadedFiles).attachTo(news.getPublication());

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
              ASSOCIATED_TO_THE_NEWS_MSG + news.getId(), e);
    }

    // Classifying new content onto taxonomy
    classifyQuickInfo(publication, positions);

    notifier.notifyEventOn(ResourceEvent.Type.UPDATE, before, news);

    // Sending notifications to subscribers
    sendSubscriptionsNotification(news, forcePublishing ? NotifAction.CREATE: NotifAction.UPDATE);
  }

  @Override
  public void removeNews(final String id) {
    final News news = getNews(id);

    final PublicationPK foreignPK = news.getForeignPK();

    // Deleting publication
    getPublicationService().removePublication(foreignPK);

    // De-reffering contribution in taxonomy
    try (final Connection connection = DBUtil.openConnection()) {
      quickInfoContentManager.deleteSilverContent(connection, foreignPK);
    } catch (ContentManagerException | SQLException e) {
      SilverLogger.getLogger(this).error(
          "can not delete the silver-content of the publication " + foreignPK.getId() +
              ASSOCIATED_TO_THE_NEWS_MSG + news.getId(), e);
    }

    // TODO: the statistic deletion should be done by using the CDI notification for a better decoupling

    // deleting statistics
    getStatisticService().deleteStats(news);

    // deleting news itself
    performInOne(() -> newsRepository.deleteById(id));

    notifier.notifyEventOn(ResourceEvent.Type.DELETION, news);
  }

  @Override
  public List<News> getPlatformNews(String userId) {
    SilverLogger.getLogger(this).debug("Enter Get All Quick Info : User=" + userId);
    final String[] allowedComponentIds = OrganizationController.get()
        .getComponentIdsForUser(userId, QuickInfoComponentSettings.COMPONENT_NAME);
    int limit = QuickInfoComponentSettings.getSettings().getInteger("news.all.limit", 30);
    //noinspection SimplifyStreamApiCallChains
    return newsRepository.getByComponentIds(asList(allowedComponentIds))
        .stream()
        .filter(n -> n.getPublishDate() != null)
        .map(n -> {
          decorateNews(singletonList(n), false);
          return n;
        })
        .filter(VISIBLE_PREDICATE)
        .limit(limit)
        .collect(Collectors.toList());
  }

  @Override
  public List<News> getNewsForTicker(String userId) {
    final List<News> tickerNews = filterAuthorized(newsRepository.getTickerNews(), userId)
        .collect(Collectors.toList());
    if (tickerNews.isEmpty()) {
      return tickerNews;
    }
    decorateNews(tickerNews, false);
    return tickerNews.stream()
        .filter(VISIBLE_PREDICATE)
        .sorted(QuickInfoDateComparatorDesc.comparator)
        .collect(Collectors.toList());
  }

  @Override
  public List<News> getUnreadBlockingNews(String userId) {
    final List<News> blockingNews = filterAuthorized(newsRepository.getBlockingNews(), userId)
        .collect(Collectors.toList());
    if (blockingNews.isEmpty()) {
      return blockingNews;
    }
    decorateNews(blockingNews, false);
    final List<News> visibleNews = blockingNews.stream()
        .filter(VISIBLE_PREDICATE)
        .collect(Collectors.toList());
    final Set<News> readNews = getStatisticService()
        .filterRead(visibleNews, userId)
        .collect(Collectors.toSet());
    return visibleNews.stream()
        .filter(n -> !readNews.contains(n))
        .sorted(QuickInfoDateComparatorDesc.comparator)
        .collect(Collectors.toList());
  }

  private Stream<News> filterAuthorized(final List<News> news, final String userId) {
    final Set<String> allInstanceIds = news.stream()
        .map(News::getComponentInstanceId)
        .collect(Collectors.toSet());
    final Set<String> authorizedInstanceIds = ComponentAccessControl.get()
        .filterAuthorizedByUser(allInstanceIds, userId)
        .collect(Collectors.toSet());
    return news.stream()
        .filter(n -> authorizedInstanceIds.contains(n.getComponentInstanceId()));
  }

  @Override
  public void submitNewsOnHomepage(String id, String userId) {
    News news = getNews(id);
    news.setId(news.getPublicationId());
    getDelegatedNewsService().submitNews(news,
        news.getVisibility().getSpecificPeriod().orElse(null), userId);
  }

  @Override
  public void performReminder(final Reminder reminder) {
    if (QUICKINFO_DELAYED_VISIBILITY_USER_NOTIFICATION.asString().equals(reminder.getProcessName())) {
      getContributionById(reminder.getContributionId())
          .ifPresent(n -> sendSubscriptionsNotification(n, NotifAction.CREATE));
    }
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
              ASSOCIATED_TO_THE_NEWS_MSG + news.getId(), e);
    }
    sendSubscriptionsNotification(news, NotifAction.CREATE);
  }

  private void sendSubscriptionsNotification(final News news, final NotifAction notifAction) {
    if (!news.isDraft()) {
      if (news.isVisible()) {
        new QuickInfoSubscriptionUserNotification(news, notifAction).build().send();
      } else {
        QuickInfoDelayedVisibilityUserNotificationReminder.get().setAbout(news);
      }
    }
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

  private void decorateNews(final List<News> news, final boolean delegated) {
    final Map<String, News> mapping = mapByPublicationId(news);
    getPublicationService().getByIds(mapping.keySet()).forEach(p -> {
      News current = mapping.get(p.getId());
      current.setPublication(p);
    });
    if (delegated) {
      getDelegatedNewsService().getDelegatedNews(mapping.keySet()).forEach(d -> {
        News current = mapping.get(d.getId());
        current.setDelegatedNews(d);
      });
    }
  }

  private Map<String, News> mapByPublicationId(final List<News> news) {
    final Map<String, News> mapping = new HashMap<>(news.size());
    news.stream().filter(Objects::nonNull).forEach(n -> mapping.put(n.getPublicationId(), n));
    return mapping;
  }


  private PublicationService getPublicationService() {
    return PublicationService.get();
  }

  private StatisticService getStatisticService() {
    return StatisticService.get();
  }

  private DelegatedNewsService getDelegatedNewsService() {
    return DelegatedNewsServiceProvider.getDelegatedNewsService();
  }

}
