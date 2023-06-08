/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.components.quickinfo.control;

import org.silverpeas.components.quickinfo.NewsByStatus;
import org.silverpeas.components.quickinfo.QuickInfoComponentSettings;
import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.components.quickinfo.model.QuickInfoService;
import org.silverpeas.components.quickinfo.model.QuickInfoServiceProvider;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.exception.DecodingException;
import org.silverpeas.core.io.media.image.thumbnail.ThumbnailSettings;
import org.silverpeas.core.io.upload.UploadedFile;
import org.silverpeas.core.pdc.pdc.model.PdcPosition;
import org.silverpeas.core.silverstatistics.access.service.StatisticService;
import org.silverpeas.core.subscription.SubscriptionService;
import org.silverpeas.core.subscription.SubscriptionServiceProvider;
import org.silverpeas.core.subscription.service.ComponentSubscription;
import org.silverpeas.core.subscription.service.ComponentSubscriptionResource;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.subscription.SubscriptionContext;
import org.silverpeas.core.web.util.ListIndex;
import org.silverpeas.core.webapi.pdc.PdcClassificationEntity;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static java.util.Optional.ofNullable;
import static org.silverpeas.core.cache.service.VolatileIdentifierProvider.newVolatileIntegerIdentifierOn;

/**
 * @author squere
 */
public class QuickInfoSessionController extends AbstractComponentSessionController {

  private QuickInfoComponentSettings instanceSettings;

  private List<News> mainList;
  private List<News> draftList;
  private List<News> notYetVisibleList;
  private List<News> noMoreVisibleList;
  private List<News> currentList;
  private final ListIndex currentIndex = new ListIndex(0);

  public QuickInfoSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, QuickInfoComponentSettings.MESSAGES_PATH,
        QuickInfoComponentSettings.ICONS_PATH, QuickInfoComponentSettings.SETTINGS_PATH);
  }

  public QuickInfoComponentSettings getInstanceSettings() {
    if (instanceSettings == null) {
      ComponentInstLight app = getOrganisationController().getComponentInstLight(getComponentId());
      instanceSettings = new QuickInfoComponentSettings(app.getDescription(getLanguage()));
    }
    instanceSettings.setCommentsEnabled(StringUtil
        .getBooleanValue(getComponentParameterValue(QuickInfoComponentSettings.PARAM_COMMENTS)));
    instanceSettings.setTaxonomyEnabled(StringUtil
        .getBooleanValue(getComponentParameterValue(QuickInfoComponentSettings.PARAM_TAXONOMY)));
    instanceSettings.setNotificationAllowed(!getUserDetail().isAnonymous());
    instanceSettings
        .setBroadcastModes(getComponentParameterValue(QuickInfoComponentSettings.PARAM_BROADCAST));
    instanceSettings.setDelegatedNewsEnabled(StringUtil
        .getBooleanValue(getComponentParameterValue(QuickInfoComponentSettings.PARAM_DELEGATED)));

    String userView = getComponentParameterValue(QuickInfoComponentSettings.PARAM_USERVIEW);
    if (QuickInfoComponentSettings.VALUE_USERVIEW_LISTING.equals(userView)) {
      instanceSettings.setMosaicViewForUsers(false);
    }

    return instanceSettings;
  }

  public NewsByStatus getQuickInfos() {
    NewsByStatus newsByStatus =
        getQuickInfoService().getAllNewsByStatus(getComponentId(), getUserId());
    setQuickInfos(newsByStatus);
    return newsByStatus;
  }

  public List<News> getVisibleQuickInfos() {
    mainList = getQuickInfoService().getVisibleNews(getComponentId());
    return mainList;
  }

  private void setQuickInfos(NewsByStatus newsByStatus) {
    mainList = newsByStatus.getVisibles();
    draftList = newsByStatus.getDrafts();
    notYetVisibleList = newsByStatus.getNotYetVisibles();
    noMoreVisibleList = newsByStatus.getNoMoreVisibles();
  }

  public News getNews(String id, boolean statVisit) {
    News news = getQuickInfoService().getNews(id);
    if (statVisit) {
      addVisit(news);
    }
    processIndex(news);
    return news;
  }

  public News getNewsByForeignId(String foreignId) {
    News news = getQuickInfoService().getNewsByForeignId(foreignId);
    addVisit(news);
    processIndex(news);
    return news;
  }

  /**
   * Publish a news represented by the given identifier.
   * @param id the identifier of the news that must be published.
   */
  public void publish(String id) {
    getQuickInfoService().publish(id, getUserId());
    loadNewsLists(true);
  }

  /**
   * Prepare an instance of a news that will exists only in memory if no action persistence will be
   * applied on.
   * @return an in memory instance of a news.
   */
  public News prepareEmptyNews() {
    News news = new News(getString("quickinfo.news.untitled"), null, null, false, false, false);
    news.setDraft();
    news.setComponentInstanceId(getComponentId());
    // Dummy identifiers
    news.setId("volatileId@" + getComponentId() + "@" + System.nanoTime());
    news.setPublicationId(newVolatileIntegerIdentifierOn(getComponentId()));
    news.getPublication().getPK().setId(news.getPublicationId());
    return news;
  }

  /**
   * Indicates if the given news identifier is one that indicates the news is in memory only.
   * @param newsId the news identifier to verify.
   * @return true if the news identifier is one for memory use, false otherwise (id is one of
   * persisted news).
   */
  public boolean isNewsIdentifierFromMemory(String newsId) {
    return StringUtil.isNotDefined(newsId) || newsId.startsWith("volatileId@" + getComponentId());
  }

  /**
   * Creates into persistence the news with minimal data.
   * @param news the news to persist.
   */
  public void create(News news) {
    news.setDraft();
    news.setComponentInstanceId(getComponentId());
    news.setCreatorId(getUserId());
    getQuickInfoService().create(news);
  }

  /**
   * Updates all the data of a news.
   * @param id the identifier of the news (used to load previous data).
   * @param updatedNews the data to save.
   * @param pdcPositions the pdc positions.
   * @param uploadedFiles the files uploaded in the aim to be attached to the news.
   * @param forcePublish true to indicate a publish action, false otherwise.
   */
  public void update(String id, News updatedNews, String pdcPositions,
      Collection<UploadedFile> uploadedFiles, boolean forcePublish) {
    News news = getNews(id, false);
    news.setTitle(updatedNews.getTitle());
    news.setDescription(updatedNews.getDescription());
    news.setContentToStore(updatedNews.getContentToStore());
    news.setVisibilityPeriod(updatedNews.getVisibility().getSpecificPeriod().orElse(null));
    news.setUpdaterId(getUserId());
    news.setImportant(updatedNews.isImportant());
    news.setTicker(updatedNews.isTicker());
    news.setMandatory(updatedNews.isMandatory());
    news.markAsModified();
    if (forcePublish) {
      news.setPublished();
    }

    getQuickInfoService().update(news, getPositionsFromJSON(pdcPositions), uploadedFiles,
        forcePublish);

    loadNewsLists(true);
  }

  public ThumbnailSettings getThumbnailSettings() {
    int width = getSettings().getInteger("thumbnail.width", 200);
    int height = getSettings().getInteger("thumbnail.height", 200);
    return ThumbnailSettings.getInstance(getComponentId(), width, height);
  }

  public Boolean isSubscriberUser() {
    Boolean subscriber = null;
    if (!getUserDetail().isAccessGuest()) {
      SubscriptionService subscriptionService = SubscriptionServiceProvider.getSubscribeService();
      subscriber = subscriptionService
          .existsSubscription(new ComponentSubscription(getUserId(), getComponentId()));
    }
    return subscriber;
  }

  public String manageSubscriptions() {
    SubscriptionContext subscriptionContext = getSubscriptionContext();
    subscriptionContext.initialize(ComponentSubscriptionResource.from(getComponentId()));
    return subscriptionContext.getDestinationUrl();
  }

  public void submitNewsOnHomepage(String id) {
    getQuickInfoService().submitNewsOnHomepage(id, getUserId());
  }

  /**
   * Classify the info letter publication on the PdC. If the position parameter is filled it
   * means that the content must be unclassified.
   * @param positions the string json positions.
   */
  private List<PdcPosition> getPositionsFromJSON(String positions) {
    return ofNullable(positions)
        .filter(StringUtil::isDefined)
        .map(p -> {
          try {
            return PdcClassificationEntity.fromJSON(positions);
          } catch (DecodingException e) {
            SilverLogger.getLogger(this).error(e);
          }
          return null;
        })
        .filter(Objects::nonNull)
        .orElseGet(PdcClassificationEntity::undefinedClassification)
        .getPdcPositions();
  }

  private void addVisit(News news) {
    if (!news.isDraft()) {
      getStatisticService().addStat(getUserId(), news);
    }
  }

  private QuickInfoService getQuickInfoService() {
    return QuickInfoServiceProvider.getQuickInfoService();
  }

  private StatisticService getStatisticService() {
    return StatisticService.get();
  }

  public News getPrevious() {
    return currentList.get(currentIndex.getPreviousIndex());
  }

  public News getNext() {
    return currentList.get(currentIndex.getNextIndex());
  }

  public ListIndex getIndex() {
    return currentIndex;
  }

  private void processIndex(News news) {
    loadNewsLists(false);

    if (!news.isDraft() && news.isVisible()) {
      currentList = mainList;
    } else if (news.isDraft()) {
      currentList = draftList;
    } else if (news.isNotYetVisible()) {
      currentList = notYetVisibleList;
    } else if (news.isNoMoreVisible()) {
      currentList = noMoreVisibleList;
    }
    if (currentList != null) {
      currentIndex.setCurrentIndex(currentList.indexOf(news));
      currentIndex.setNbItems(currentList.size());
    }
  }

  /**
   * Loads the handled lists.
   * @param reload if true forces to load again the lists, if false loading the lists if it has not
   * been yet done.
   */
  private void loadNewsLists(boolean reload) {
    if (mainList == null || reload) {
      if (getHighestSilverpeasUserRole().isGreaterThanOrEquals(SilverpeasRole.PUBLISHER)) {
        getQuickInfos();
      } else {
        getVisibleQuickInfos();
      }
    }
  }
}