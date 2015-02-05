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
package com.stratelia.webactiv.quickinfo.control;

import com.silverpeas.pdc.model.PdcPosition;
import com.silverpeas.pdc.web.PdcClassificationEntity;
import com.silverpeas.subscribe.SubscriptionService;
import com.silverpeas.subscribe.SubscriptionServiceProvider;
import com.silverpeas.subscribe.service.ComponentSubscription;
import com.silverpeas.thumbnail.ThumbnailSettings;
import com.silverpeas.usernotification.builder.helper.UserNotificationHelper;
import com.stratelia.silverpeas.alertUser.AlertUser;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.publication.control.PublicationService;
import com.stratelia.webactiv.statistic.control.StatisticService;
import org.silverpeas.components.quickinfo.NewsByStatus;
import org.silverpeas.components.quickinfo.QuickInfoComponentSettings;
import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.components.quickinfo.model.QuickInfoService;
import org.silverpeas.components.quickinfo.model.QuickInfoServiceProvider;
import org.silverpeas.components.quickinfo.notification.NewsManualUserNotification;
import org.silverpeas.date.Period;
import org.silverpeas.util.DateUtil;
import org.silverpeas.util.Pair;
import org.silverpeas.util.ServiceProvider;
import org.silverpeas.util.StringUtil;
import org.silverpeas.util.exception.DecodingException;

import java.util.Iterator;
import java.util.List;

import static org.silverpeas.cache.service.CacheServiceProvider
    .getSessionVolatileResourceCacheService;

/**
 * @author squere
 */
public class QuickInfoSessionController extends AbstractComponentSessionController {

  private PublicationService publicationService = null;
  private QuickInfoComponentSettings instanceSettings = null;

  /**
   * Creates new QuickInfoSessionController
   * @param mainSessionCtrl
   * @param componentContext
   */
  public QuickInfoSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, QuickInfoComponentSettings.MESSAGES_PATH,
        QuickInfoComponentSettings.ICONS_PATH, QuickInfoComponentSettings.SETTINGS_PATH);
  }

  private PublicationService getPublicationService() {
    if (publicationService == null) {
      publicationService = ServiceProvider.getService(PublicationService.class);
    }
    return publicationService;
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
    return instanceSettings;
  }

  public NewsByStatus getQuickInfos() {
    NewsByStatus newsByStatus = getService().getAllNewsByStatus(getComponentId(), getUserId());
    // TODO - REMOVE THIS PART OF BELOW CODE IN WILDFLY VERSION
    // removing drafts which have never been saved by the contributor
    Iterator<News> drafts = newsByStatus.getDrafts().iterator();
    while (drafts.hasNext()) {
      News draft = drafts.next();
      if (!draft.hasBeenModified()) {
        getService().removeNews(draft.getId());
        drafts.remove();
      }
    }
    return newsByStatus;
  }

  public List<News> getVisibleQuickInfos() {
    return getService().getVisibleNews(getComponentId());
  }

  public News getNews(String id, boolean statVisit) {
    News news = getService().getNews(id);
    if (statVisit) {
      addVisit(news);
    }
    return news;
  }

  public News getNewsByForeignId(String foreignId) {
    News news = getService().getNewsByForeignId(foreignId);
    addVisit(news);
    return news;
  }

  private void addVisit(News news) {
    if (!news.isDraft()) {
      getStatisticService().addStat(getUserId(), news);
    }
  }

  /**
   * Publish a news represented by the given identifier.
   * @param id the identifier of the news that must be published.
   */
  public void publish(String id) {
    getService().publish(id, getUserId());
  }

  /**
   * Prepare an instance of a news that will exists only in memory if no action persistence will be
   * applied on.
   * @return an in memory instance of a news.
   */
  public News prepareEmptyNews() {
    Period period = Period.from(DateUtil.MINIMUM_DATE, DateUtil.MAXIMUM_DATE);
    News news = new News(getString("quickinfo.news.untitled"), null, period, false, false, false);
    news.setDraft();
    news.setComponentInstanceId(getComponentId());
    // Dummy identifiers
    news.setId("volatileId@" + getComponentId() + "@" + System.nanoTime());
    news.setPublicationId(
        getSessionVolatileResourceCacheService().newVolatileIntegerIdentifierAsString());
    news.getPublication().getPK().setId(news.getPublicationId());
    getSessionVolatileResourceCacheService().addComponentResource(news.getPublication());
    return news;
  }

  private QuickInfoService getService() {
    return QuickInfoServiceProvider.getQuickInfoService();
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
    getService().create(news);
  }

  /**
   * Updates all the data of a news.
   * @param id the identifier of the news (used to load previous data).
   * @param updatedNews the data to save.
   * @param pdcPositions the pdc positions.
   * @param forcePublish true to indicate a publish action, false otherwise.
   */
  public void update(String id, News updatedNews, String pdcPositions, boolean forcePublish) {
    News news = getNews(id, false);
    news.setTitle(updatedNews.getTitle());
    news.setDescription(updatedNews.getDescription());
    news.setContent(updatedNews.getContent());
    news.setVisibilityPeriod(updatedNews.getVisibilityPeriod());
    news.setUpdaterId(getUserId());
    news.setImportant(updatedNews.isImportant());
    news.setTicker(updatedNews.isTicker());
    news.setMandatory(updatedNews.isMandatory());
    news.markAsModified();
    if (forcePublish) {
      news.setPublished();
    }

    getService().update(news, getPositionsFromJSON(pdcPositions), forcePublish);
  }

  /**
   * Removes from persistence the news which the identifier is the one given.
   * If the given identifier corresponds to a volatile one, nothing is done.
   * @param id the identifier of the news to remove from the persistence.
   */
  public void remove(String id) {
    if (!isNewsIdentifierFromMemory(id)) {
      // Case of a news that exists
      getService().removeNews(id);
    }
  }

  public boolean isPdcUsed() {
    String value = getComponentParameterValue("usePdc");
    if (value != null) {
      return "yes".equals(value.toLowerCase());
    }
    return false;
  }

  @Override
  public void close() {
    if (publicationService != null) {
      publicationService = null;
    }
  }

  public ThumbnailSettings getThumbnailSettings() {
    int width = getSettings().getInteger("thumbnail.width", 200);
    int height = getSettings().getInteger("thumbnail.height", 200);
    ThumbnailSettings settings = ThumbnailSettings.getInstance(getComponentId(), width, height);
    return settings;
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

  public String notify(String newsId) {
    AlertUser sel = getAlertUser();
    sel.resetAll();

    // setting up browsebar
    sel.setHostSpaceName(getSpaceLabel());
    sel.setHostComponentId(getComponentId());
    Pair<String, String> hostComponentName = new Pair<>(getComponentLabel(), null);
    sel.setHostComponentName(hostComponentName);
    sel.setNotificationMetaData(UserNotificationHelper
        .build(new NewsManualUserNotification(getNews(newsId, false), getUserDetail())));

    SelectionUsersGroups sug = new SelectionUsersGroups();
    sug.setComponentId(getComponentId());
    sel.setSelectionUsersGroups(sug);

    return AlertUser.getAlertUserURL();
  }

  public void submitNewsOnHomepage(String id) {
    getService().submitNewsOnHomepage(id, getUserId());
  }

  /**
   * Classify the info letter publication on the PdC only if the positions parameter is filled
   * @param positions the string json positions
   */
  private List<PdcPosition> getPositionsFromJSON(String positions) {
    List<PdcPosition> pdcPositions = null;
    if (StringUtil.isDefined(positions)) {
      PdcClassificationEntity qiClassification = null;
      try {
        qiClassification = PdcClassificationEntity.fromJSON(positions);
      } catch (DecodingException e) {
        SilverTrace.error("quickInfo", "QuickInfoSessionController.classifyQuickInfo",
            "PdcClassificationEntity error", "Problem to read JSON", e);
      }
      if (qiClassification != null && !qiClassification.isUndefined()) {
        pdcPositions = qiClassification.getPdcPositions();
      }
    }
    return pdcPositions;
  }

  private StatisticService getStatisticService() {
    return ServiceProvider.getService(StatisticService.class);
  }

}