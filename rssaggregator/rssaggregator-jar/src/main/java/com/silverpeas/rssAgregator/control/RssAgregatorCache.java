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

import com.silverpeas.rssAgregator.model.SPChannel;
import com.silverpeas.rssAgregator.model.SPChannelPK;
import org.silverpeas.util.ResourceLocator;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * @author sv
 */
@Singleton
public class RssAgregatorCache {
  // cache refresh rate in millisecond
  private static long refreshRate = 0;
  // instance of RssAgregatorCache singleton
  private static RssAgregatorCache instance = null;
  // content of cache
  private Map<SPChannelPK, SPChannel> cache = new HashMap<>();
  // informations about cache refresh
  private Map<SPChannelPK, Long> cacheNextRefresh = new HashMap<>();

  /**
   * Default constructor
   */
  private RssAgregatorCache() {
    ResourceLocator res =
        new ResourceLocator("com.silverpeas.rssAgregator.settings.rssAgregatorSettings", "");
    String refreshRate = res.getString("refreshRate");
    RssAgregatorCache.refreshRate = (60 * 1000) * Long.valueOf(refreshRate);
  }

  /**
   * Get an instance of RssAgregatorCache
   */
  public static RssAgregatorCache getInstance() {
    if (instance == null) {
      instance = new RssAgregatorCache();
    }
    return instance;
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
    int refreshRate = spChannel.getRefreshRate() * 60 * 1000;
    cacheNextRefresh.put(key, currentTime + refreshRate);
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
