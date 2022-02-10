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
package org.silverpeas.components.rssaggregator.service;

import org.silverpeas.components.rssaggregator.model.SPChannel;
import org.silverpeas.components.rssaggregator.model.SPChannelPK;
import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sv
 */
@Singleton
public class RssAggregatorCache {
  // content of cache
  private Map<SPChannelPK, SPChannel> cache = new ConcurrentHashMap<>();
  // informations about cache refresh
  private Map<SPChannelPK, Long> cacheNextRefresh = new ConcurrentHashMap<>();

  /**
   * Default constructor
   */
  private RssAggregatorCache() {
  }

  /**
   * Get an instance of RssAggregatorCache
   */
  public static RssAggregatorCache getInstance() {
    return ServiceProvider.getService(RssAggregatorCache.class);
  }

  /**
   * Get a cached content. If content is not cached, return null.
   */
  public SPChannel getChannelFromCache(SPChannelPK key) {
    return cache.get(key);
  }

  /**
   * Add or replace a content in the cache
   */
  public void addChannelToCache(SPChannel spChannel) {
    SPChannelPK key = (SPChannelPK) spChannel.getPK();

    // Store channel in cache
    cache.put(key, spChannel);

    // Store time of content informations storage
    long currentTime = System.currentTimeMillis();
    // refresh rate in ms
    final int secondsInminute = 60;
    final int milliInSecond = 1000;
    int channelRefreshRate = spChannel.getRefreshRate() * milliInSecond * secondsInminute;
    cacheNextRefresh.put(key, currentTime + channelRefreshRate);
  }

  /**
   * @param key of the channel to remove from the cache
   */
  public void removeChannelFromCache(SPChannelPK key) {
    cache.remove(key);
    cacheNextRefresh.remove(key);
  }

  /**
   * @return true if a cached content need to be updated or a content is not cached.
   */
  public boolean isContentNeedToRefresh(SPChannelPK key) {
    if (cache.get(key) == null) {
      // content is not cached
      return true;
    } else {
      // verify if the content has been refreshed at the refresh rate
      long timeOfNextRefresh = cacheNextRefresh.get(key);
      long currentTime = System.currentTimeMillis();
      return currentTime > timeOfNextRefresh;
    }
  }
}
