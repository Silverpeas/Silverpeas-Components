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

package org.silverpeas.components.rssaggregator.control;

import com.rometools.rome.feed.synd.SyndFeed;
import org.silverpeas.components.rssaggregator.model.RSSViewType;
import org.silverpeas.components.rssaggregator.model.RssAgregatorException;
import org.silverpeas.components.rssaggregator.model.SPChannel;
import org.silverpeas.components.rssaggregator.model.SPChannelPK;
import org.silverpeas.components.rssaggregator.service.DefaultRssAggregator;
import org.silverpeas.components.rssaggregator.service.RSSService;
import org.silverpeas.components.rssaggregator.service.RSSServiceProvider;
import org.silverpeas.components.rssaggregator.service.RssAggregator;
import org.silverpeas.components.rssaggregator.service.RssAggregatorCache;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Standard Session Controller Constructor
 * @author neysseri
 */
public class RssAgregatorSessionController extends AbstractComponentSessionController {
  // instance of RssAggregatorCache singleton
  private RssAggregatorCache cache = RssAggregatorCache.getInstance();
  private RssAggregator rssBm = null;
  private SPChannel currentChannel = null;
  private SimpleDateFormat dateFormatter = null;
  private static final String DEFAULT_VIEW_PARAMETER = "defaultView";
  private RSSViewType viewMode;

  /**
   * Default constructor
   */
  public RssAgregatorSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.rssAgregator.multilang.rssAgregatorBundle",
        "org.silverpeas.rssAgregator.settings.rssAgregatorIcons",
        "org.silverpeas.rssAgregator.settings.rssAgregatorSettings");
    String defaultView = getComponentParameterValue(DEFAULT_VIEW_PARAMETER);
    if (defaultView.isEmpty()) {
      viewMode = RSSViewType.SEPARATED;
    } else {
      viewMode = RSSViewType.valueOf(defaultView);
    }
  }

  /**
   * Extract rss files informations (channels and items). Return a list of Channel.
   */
  public List<SPChannel> getAvailableChannels() throws RssAgregatorException {
    List<SPChannel> channelsFromDB = getRssAggregator().getChannels(getComponentId());
    List<SPChannel> channels = new ArrayList<>();
    for (SPChannel channel : channelsFromDB) {
      SPChannelPK channelPK = (SPChannelPK) channel.getPK();
      if (cache.isContentNeedToRefresh(channelPK)) {
        channel = null;
      } else {
        channel = cache.getChannelFromCache(channelPK);
      }
      channels.add(channel);
    }
    return channels;
  }

  /**
   * Extract rss files information (channels and items). Return a list of Channel.
   */
  public List<SPChannel> getChannelsContent() throws RssAgregatorException {
    return getRssService().getAllChannels(getComponentId());
  }

  public SPChannel addChannel(SPChannel channel) throws RssAgregatorException {
    // add channel in database
    channel.setInstanceId(getComponentId());
    channel.setCreatorId(getUserId());
    channel.setCreationDate(getDateFormatter().format(new Date()));
    return getRssAggregator().addChannel(channel);
  }

  public void updateChannel(SPChannel channel) throws RssAgregatorException {
    boolean reloadChannel = false;
    if (currentChannel != null && currentChannel.getPK().getId().equals(channel.getPK().getId())) {
      reloadChannel = !currentChannel.getUrl().equals(channel.getUrl());
      currentChannel.setUrl(channel.getUrl());
      currentChannel.setNbDisplayedItems(channel.getNbDisplayedItems());
      currentChannel.setRefreshRate(channel.getRefreshRate());
      currentChannel.setDisplayImage(channel.getDisplayImage());
      reloadChannel = reloadChannel || (currentChannel.isSafeUrl() != channel.isSafeUrl());
      currentChannel.setSafeUrl(channel.isSafeUrl());
    }
    getRssAggregator().updateChannel(currentChannel);

    SyndFeed feed;
    if (reloadChannel) {
      // L'url a change, il faut recharger le channel
      cache.removeChannelFromCache((SPChannelPK) currentChannel.getPK());
    } else {
      // L'url n'a pas change, il n'est pas necessaire de recharger le channel
      feed = cache.getChannelFromCache((SPChannelPK) currentChannel.getPK()).getFeed();
      currentChannel.setFeed(feed);

      // add rss channel in cache
      cache.addChannelToCache(currentChannel);
    }
  }

  public void deleteChannel(String id) throws RssAgregatorException {
    SPChannelPK spChannelPK = new SPChannelPK(id, getComponentId());

    // remove channel from database
    getRssAggregator().deleteChannel(spChannelPK);

    // remove channel from cache
    cache.removeChannelFromCache(spChannelPK);
  }

  public SPChannel getChannel(String id) throws RssAgregatorException {
    final SPChannelPK channelPK = new SPChannelPK(id, getComponentId());
    currentChannel = cache.getChannelFromCache(channelPK);
    if (currentChannel == null) {
      currentChannel = getRssAggregator().getChannel(channelPK);
    }
    return currentChannel;
  }

  private RssAggregator getRssAggregator() {
    if (rssBm == null) {
      rssBm = new DefaultRssAggregator();
    }
    return rssBm;
  }

  private RSSService getRssService() {
    return RSSServiceProvider.getRSSService();
  }

  private SimpleDateFormat getDateFormatter() {
    if (dateFormatter == null) {
      dateFormatter = new SimpleDateFormat("yyyy/MM/dd");
    }
    return dateFormatter;
  }

  /**
   * @return HTML string content of RSS presentation
   */
  public String getRSSIntroductionContent() {
    SilverpeasTemplate rssTemplate = getNewTemplate();
    return rssTemplate.applyFileTemplate("introductionRSS_" + this.getLanguage());
  }

  /**
   * @return an RSS aggregator Silverpeas Template
   */
  private SilverpeasTemplate getNewTemplate() {
    SettingBundle rs = ResourceLocator.getSettingBundle(
        "org.silverpeas.rssAgregator.settings.rssAgregatorSettings");
    Properties templateConfiguration = new Properties();
    templateConfiguration
        .setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR, rs.getString("templatePath"));
    templateConfiguration
        .setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR, rs.getString("customersTemplatePath"));
    return SilverpeasTemplateFactory.createSilverpeasTemplate(templateConfiguration);
  }

  /**
   * Sets the current view mode of the RSS agregator rendering.
   * @param viewMode the view mode (separated, aggregated).
   */
  public void setViewMode(final RSSViewType viewMode) {
    this.viewMode = viewMode;
  }

  /**
   * @return the current viewMode
   */
  public RSSViewType getViewMode() {
    return this.viewMode;
  }

  /**
   * This method return the highest user profiles
   * @return profile which gives the higher access
   */
  public String getHighestRole() {
    String[] profiles = this.getUserRoles();
    String role = "user";
    for (String profile : profiles) {
      // if admin, return it, we won't find a better profile
      if ("admin".equals(profile)) {
        return profile;
      }
    }
    return role;
  }

}