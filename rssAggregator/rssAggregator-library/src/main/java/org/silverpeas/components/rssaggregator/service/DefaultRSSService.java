/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.components.rssaggregator.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.silverpeas.components.rssaggregator.model.RSSItem;
import org.silverpeas.components.rssaggregator.model.RssAgregatorException;
import org.silverpeas.components.rssaggregator.model.SPChannel;
import org.silverpeas.components.rssaggregator.model.SPChannelPK;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.util.MimeTypes;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.silverpeas.core.util.HttpUtil.httpClient;
import static org.silverpeas.core.util.HttpUtil.httpClientTrustingAnySslContext;

@Service
public class DefaultRSSService implements RSSService {

  @Inject
  private RssAggregator rssAggregator;

  private RssAggregatorCache cache = RssAggregatorCache.getInstance();

  @Override
  public List<RSSItem> getApplicationItems(String applicationId, boolean aggregateContent)
      throws RssAgregatorException {
    List<SPChannel> channels = getAllChannels(applicationId);
    return buildRSSItemList(channels, aggregateContent);
  }

  @Override
  public List<SPChannel> getAllChannels(String applicationId) throws RssAgregatorException {
    List<SPChannel> channelsFromDB = this.rssAggregator.getChannels(applicationId);
    List<SPChannel> channels = new ArrayList<>();
    for (SPChannel channel : channelsFromDB) {
      SPChannelPK channelPK = (SPChannelPK) channel.getPK();
      if (cache.isContentNeedToRefresh(channelPK)) {
        try {
          applyFeedTo(channel);
        } catch (Exception e) {
          SilverLogger.getLogger(this).error("Syndication feed fetching error with channel " +
              channelPK + " at " + channel.getUrl(), e);
        } finally {
          cache.addChannelToCache(channel);
        }
      } else {
        channel = cache.getChannelFromCache(channelPK);
      }
      channels.add(channel);
    }
    return channels;
  }

  /**
   * Applies {@link SyndFeed} from {@link SPChannel} data.
   * @param channel the channel with all necessary data to perform connexion.
   * @throws RssAgregatorException if MalformedURL or Parse or IO problem occur
   */
  private void applyFeedTo(final SPChannel channel) throws RssAgregatorException {
    final String channelUrl = channel.getUrl();
    if (StringUtil.isDefined(channelUrl)) {
      try {
        final HttpGet httpGet = new HttpGet(channelUrl);
        httpGet.addHeader(HttpHeaders.ACCEPT, MimeTypes.RSS_MIME_TYPE);
        try (CloseableHttpClient httpClient = channel.isSafeUrl() ? httpClientTrustingAnySslContext() : httpClient();
             CloseableHttpResponse response = httpClient.execute(httpGet)) {
          final SyndFeedInput input = new SyndFeedInput();
          final SyndFeed feed = input.build(new XmlReader(response.getEntity().getContent()));
          channel.setFeed(feed);
        }
      } catch (IOException | FeedException | GeneralSecurityException e) {
        throw new RssAgregatorException(e.getMessage(), e);
      }
    }
  }

  /**
   * @param channels the list of channels
   * @return the list of RSS items read from channels.
   */
  private List<RSSItem> buildRSSItemList(List<SPChannel> channels, boolean agregateContent) {
    List<RSSItem> items = new ArrayList<>();
    for (SPChannel spChannel : channels) {
      SyndFeed feed = spChannel.getFeed();
      if (feed != null) {
        List<SyndEntry> feedEntries = feed.getEntries();
        // Get the number of displayed items
        int itemsCount = spChannel.getNbDisplayedItems();
        for (SyndEntry feedEntry : feedEntries) {
          // Limit the number of items
          if (feedEntries.indexOf(feedEntry) + 1 > itemsCount) {
            break;
          }
          items.add(new RSSItem(feedEntry, feed, spChannel));
        }
      }
    }
    // Sort list of items in agregate content mode
    if (agregateContent) {
      Collections.sort(items);
    }
    return items;
  }

}
