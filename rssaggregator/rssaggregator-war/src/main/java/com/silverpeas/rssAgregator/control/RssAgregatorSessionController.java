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

package com.silverpeas.rssAgregator.control;

import com.silverpeas.rssAgregator.model.RSSViewType;
import com.silverpeas.rssAgregator.model.RssAgregatorException;
import com.silverpeas.rssAgregator.model.SPChannel;
import com.silverpeas.rssAgregator.model.SPChannelPK;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import de.nava.informa.core.ParseException;
import de.nava.informa.impl.basic.Channel;
import de.nava.informa.impl.basic.ChannelBuilder;
import de.nava.informa.parsers.FeedParser;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.util.SettingBundle;
import org.silverpeas.util.exception.SilverpeasException;
import org.silverpeas.util.template.SilverpeasTemplate;
import org.silverpeas.util.template.SilverpeasTemplateFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
  // instance of RssAgregatorCache singleton
  private RssAgregatorCache cache = RssAgregatorCache.getInstance();
  private RssAgregatorBm rssBm = null;
  private ChannelBuilder channelBuilder = new ChannelBuilder();
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
    List<SPChannel> channelsFromDB = getRssBm().getChannels(getComponentId());
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
   * Extract rss files informations (channels and items). Return a list of Channel.
   */
  public List<SPChannel> getChannelsContent() throws RssAgregatorException {
    List<SPChannel> channelsFromDB = getRssBm().getChannels(getComponentId());
    ArrayList<SPChannel> channels = new ArrayList<>();
    Channel rssChannel = null;
    boolean oneChannelLoaded = false;

    for (SPChannel channel : channelsFromDB) {
      SPChannelPK channelPK = (SPChannelPK) channel.getPK();
      if (cache.isContentNeedToRefresh(channelPK)) {
        if (oneChannelLoaded) {
          channel = null;
        } else {
          SilverTrace
              .debug("rssAgregator", "RssAgregatorSessionController.getChannels", "Update cache",
                  "channelPK = " + channelPK.toString());
          try {
            rssChannel = getChannelFromUrl(channel.getUrl());
          } catch (Exception e) {
            SilverTrace.info("rssAgregator", "RssAgregatorSessionController.getChannelsContent()",
                "Update cache", "channelPK = " + channelPK.toString());
          } finally {
            channel._setChannel(rssChannel);
            cache.addChannelToCache(channel);
          }
        }
        oneChannelLoaded = true;
      } else {
        SilverTrace.debug("rssAgregator", "RssAgregatorSessionController.getChannels", "Use cache",
            "channelPK = " + channelPK.toString());
        channel = cache.getChannelFromCache(channelPK);
      }
      channels.add(channel);
    }
    return channels;
  }

  public SPChannel addChannel(SPChannel channel) throws RssAgregatorException {
    // add channel in database
    channel.setInstanceId(getComponentId());
    channel.setCreatorId(getUserId());
    channel.setCreationDate(getDateFormatter().format(new Date()));
    SPChannel newChannel = getRssBm().addChannel(channel);
    return newChannel;
  }

  public void updateChannel(SPChannel channel) throws RssAgregatorException {
    boolean urlHaveChanged = true;
    SilverTrace.debug("rssAgregator", "RssAgregatorSessionController.updateChannel",
        "root.MSG_GEN_PARAM_VALUE", "channelPK = " + channel.getPK().getId());
    if (currentChannel != null && currentChannel.getPK().getId().equals(channel.getPK().getId())) {
      SilverTrace.debug("rssAgregator", "RssAgregatorSessionController.updateChannel",
          "root.MSG_GEN_PARAM_VALUE", "channelPK = " + currentChannel.getPK().getId());
      urlHaveChanged = !currentChannel.getUrl().equals(channel.getUrl());
      if (urlHaveChanged) {
        currentChannel.setUrl(channel.getUrl());
      }
      currentChannel.setNbDisplayedItems(channel.getNbDisplayedItems());
      currentChannel.setRefreshRate(channel.getRefreshRate());
      currentChannel.setDisplayImage(channel.getDisplayImage());
    }

    getRssBm().updateChannel(currentChannel);

    Channel rssChannel;
    if (urlHaveChanged) {
      // L'url a change, il faut recharger le channel
      cache.removeChannelFromCache((SPChannelPK) channel.getPK());
    } else {
      // L'url n'a pas change, il n'est pas necessaire de recharger le channel
      rssChannel = cache.getChannelFromCache((SPChannelPK) currentChannel.getPK())._getChannel();
      currentChannel._setChannel(rssChannel);

      // add rss channel in cache
      cache.addChannelToCache(currentChannel);
    }
  }

  public void deleteChannel(String id) throws RssAgregatorException {
    SPChannelPK spChannelPK = new SPChannelPK(id, getComponentId());

    // remove channel from database
    getRssBm().deleteChannel(spChannelPK);

    // remove channel from cache
    cache.removeChannelFromCache(spChannelPK);
  }

  public SPChannel getChannel(String id) throws RssAgregatorException {
    currentChannel = getRssBm().getChannel(new SPChannelPK(id, getComponentId()));
    return currentChannel;
  }

  private Channel getChannelFromUrl(String sUrl) throws RssAgregatorException {
    SilverTrace.debug("rssAgregator", "RssAgregatorSessionController.getChannelFromUrl",
        "root.MSG_GEN_ENTER_METHOD", "sUrl = " + sUrl);
    Channel channel = null;
    try {
      if (sUrl != null && !sUrl.equals("")) {
        URL url = new URL(sUrl);
        channel = (Channel) FeedParser.parse(channelBuilder, url);
      }
    } catch (MalformedURLException e) {
      throw new RssAgregatorException("RssAgregatorSessionController.getChannelFromUrl",
          SilverpeasException.WARNING, "RssAgregator.EX_URL_IS_NOT_VALID", e);
    } catch (IOException e) {
      throw new RssAgregatorException("RssAgregatorSessionController.getChannelFromUrl",
          SilverpeasException.WARNING, "RssAgregator.EX_URL_IS_NOT_REATCHABLE", e);
    } catch (ParseException e) {
      throw new RssAgregatorException("RssAgregatorSessionController.getChannelFromUrl",
          SilverpeasException.WARNING, "RssAgregator.EX_RSS_BAD_FORMAT", e);
    }
    SilverTrace.debug("rssAgregator", "RssAgregatorSessionController.getChannelFromUrl",
        "root.MSG_GEN_EXIT_METHOD");
    return channel;
  }

  private RssAgregatorBm getRssBm() {
    if (rssBm == null) {
      rssBm = new RssAgregatorBmImpl();
    }
    return rssBm;
  }

  /**
   * @return
   */
  public SimpleDateFormat getDateFormatter() {
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
   * @param viewMode the view mode (separated, agregated).
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