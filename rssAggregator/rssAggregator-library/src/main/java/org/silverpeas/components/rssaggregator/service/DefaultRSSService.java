/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.rssaggregator.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.apache.http.HttpHeaders;
import org.silverpeas.components.rssaggregator.model.RSSItem;
import org.silverpeas.components.rssaggregator.model.RssAgregatorException;
import org.silverpeas.components.rssaggregator.model.SPChannel;
import org.silverpeas.components.rssaggregator.model.SPChannelPK;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.util.MimeTypes;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.inject.Inject;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.net.http.HttpResponse.BodyHandlers.ofInputStream;
import static org.silverpeas.core.util.HttpUtil.*;

@Service
public class DefaultRSSService implements RSSService {

  @Inject
  private RssAggregator rssAggregator;

  private final RssAggregatorCache cache = RssAggregatorCache.getInstance();

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
      SPChannelPK channelPK = new SPChannelPK(channel.getPK().getId(), channel.getPK());
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
        final HttpClient httpClient = channel.isSafeUrl() ? httpClientTrustingAnySslContext() : httpClient();
        final HttpResponse<InputStream> response = httpClient.send(toUrl(channelUrl)
            .header(HttpHeaders.ACCEPT, MimeTypes.RSS_MIME_TYPE)
            .build(), ofInputStream());
        try (final InputStream body = response.body()) {
          final SyndFeedInput input = new SyndFeedInput();
          final SyndFeed feed = input.build(new XmlReader(body));
          channel.setFeed(feed);
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this).silent(e);
        if (e instanceof InterruptedException) {
          Thread.currentThread().interrupt();
        }
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
