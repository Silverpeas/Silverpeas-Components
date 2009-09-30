/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.rssAgregator.control;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.silverpeas.rssAgregator.model.RssAgregatorException;
import com.silverpeas.rssAgregator.model.SPChannel;
import com.silverpeas.rssAgregator.model.SPChannelPK;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasException;

import de.nava.informa.core.ParseException;
import de.nava.informa.impl.basic.Channel;
import de.nava.informa.impl.basic.ChannelBuilder;
import de.nava.informa.parsers.FeedParser;

/**
 * Standard Session Controller Constructeur
 * 
 * @author neysseri
 * @since 27/08/2004
 * 
 * @param mainSessionCtrl
 *          The user's profile
 * @param componentContext
 *          The component's profile
 * 
 * @see
 */
public class RssAgregatorSessionController extends
    AbstractComponentSessionController {
  // instance of RssAgregatorCache singleton
  private RssAgregatorCache cache = RssAgregatorCache.getInstance();
  private RssAgregatorBm rssBm = null;
  private ChannelBuilder channelBuilder = new ChannelBuilder();
  private SPChannel currentChannel = null;
  private SimpleDateFormat dateFormatter = null;

  /**
   * Default constructor
   */
  public RssAgregatorSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.silverpeas.rssAgregator.multilang.rssAgregatorBundle",
        "com.silverpeas.rssAgregator.settings.rssAgregatorIcons");
  }

  /**
   * Extract rss files informations (channels and items). Return a list of
   * Channel.
   */
  public List getAvailableChannels() throws RssAgregatorException {
    List channelsFromDB = getRssBm().getChannels(getComponentId());
    ArrayList channels = new ArrayList();
    SPChannel channel = null;
    SPChannelPK channelPK = null;

    for (int c = 0; c < channelsFromDB.size(); c++) {
      channel = (SPChannel) channelsFromDB.get(c);
      channelPK = (SPChannelPK) channel.getPK();
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
   * Extract rss files informations (channels and items). Return a list of
   * Channel.
   */
  public List getChannelsContent() throws RssAgregatorException {
    List channelsFromDB = getRssBm().getChannels(getComponentId());
    ArrayList channels = new ArrayList();
    SPChannel channel = null;
    Channel rssChannel = null;
    SPChannelPK channelPK = null;
    boolean oneChannelLoaded = false;

    for (int c = 0; c < channelsFromDB.size(); c++) {
      channel = (SPChannel) channelsFromDB.get(c);
      channelPK = (SPChannelPK) channel.getPK();
      if (cache.isContentNeedToRefresh(channelPK)) {
        if (oneChannelLoaded) {
          channel = null;
        } else {
          SilverTrace.debug("rssAgregator",
              "RssAgregatorSessionController.getChannels",
              "Mise à jour du cache", "channelPK = " + channelPK.toString());
          try {
            rssChannel = getChannelFromUrl(channel.getUrl());
          } catch (Exception e) {
            SilverTrace.info("rssAgregator",
                "RssAgregatorSessionController.getChannelsContent()",
                "Mise à jour du cache", "channelPK = " + channelPK.toString());
          } finally {
            channel._setChannel(rssChannel);
            cache.addChannelToCache(channel);
          }
        }
        oneChannelLoaded = true;
      } else {
        SilverTrace.debug("rssAgregator",
            "RssAgregatorSessionController.getChannels",
            "Utilisation du cache", "channelPK = " + channelPK.toString());
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
    SilverTrace.debug("rssAgregator",
        "RssAgregatorSessionController.updateChannel",
        "root.MSG_GEN_PARAM_VALUE", "channelPK = " + channel.getPK().getId());
    SilverTrace.debug("rssAgregator",
        "RssAgregatorSessionController.updateChannel",
        "root.MSG_GEN_PARAM_VALUE", "channelPK = "
            + currentChannel.getPK().getId());
    if (currentChannel != null
        && currentChannel.getPK().getId().equals(channel.getPK().getId())) {
      urlHaveChanged = !currentChannel.getUrl().equals(channel.getUrl());
      if (urlHaveChanged)
        currentChannel.setUrl(channel.getUrl());
      currentChannel.setNbDisplayedItems(channel.getNbDisplayedItems());
      currentChannel.setRefreshRate(channel.getRefreshRate());
      currentChannel.setDisplayImage(channel.getDisplayImage());
    }

    getRssBm().updateChannel(currentChannel);

    Channel rssChannel = null;
    if (urlHaveChanged) {
      // L'url a changé, il faut recharger le channel
      cache.removeChannelFromCache((SPChannelPK) channel.getPK());
    } else {
      // L'url n'a pas changé, il n'est pas necessaire de recharger le channel
      rssChannel = cache.getChannelFromCache(
          (SPChannelPK) currentChannel.getPK())._getChannel();
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
    currentChannel = getRssBm().getChannel(
        new SPChannelPK(id, getComponentId()));
    return currentChannel;
  }

  private Channel getChannelFromUrl(String sUrl) throws RssAgregatorException {
    SilverTrace.debug("rssAgregator",
        "RssAgregatorSessionController.getChannelFromUrl",
        "root.MSG_GEN_ENTER_METHOD", "sUrl = " + sUrl);
    Channel channel = null;
    try {
      if (sUrl != null && !sUrl.equals("")) {
        URL url = new URL(sUrl);
        channel = (Channel) FeedParser.parse(channelBuilder, url);
      }
    } catch (MalformedURLException e) {
      throw new RssAgregatorException(
          "RssAgregatorSessionController.getChannelFromUrl",
          SilverpeasException.WARNING, "RssAgregator.EX_URL_IS_NOT_VALID", e);
    } catch (IOException e) {
      throw new RssAgregatorException(
          "RssAgregatorSessionController.getChannelFromUrl",
          SilverpeasException.WARNING, "RssAgregator.EX_URL_IS_NOT_REATCHABLE",
          e);
    } catch (ParseException e) {
      throw new RssAgregatorException(
          "RssAgregatorSessionController.getChannelFromUrl",
          SilverpeasException.WARNING, "RssAgregator.EX_RSS_BAD_FORMAT", e);
    }
    SilverTrace.debug("rssAgregator",
        "RssAgregatorSessionController.getChannelFromUrl",
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

}