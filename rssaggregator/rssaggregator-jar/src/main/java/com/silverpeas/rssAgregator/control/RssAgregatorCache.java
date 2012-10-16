/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
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

import java.util.HashMap;
import java.util.Map;

import com.silverpeas.rssAgregator.model.SPChannel;
import com.silverpeas.rssAgregator.model.SPChannelPK;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * @author sv
 * @since 17/12/2003
 * @version 1.0
 */
public class RssAgregatorCache {
  // cache refresh rate in millisecond
  public static long REFRESH_RATE = 0;
  // instance of RssAgregatorCache singleton
  private static RssAgregatorCache instance = null;
  // content of cache
  private Map<SPChannelPK, SPChannel> cache = new HashMap<SPChannelPK, SPChannel>();
  // informations about cache refresh
  private Map<SPChannelPK, Long> cacheNextRefresh = new HashMap<SPChannelPK, Long>();

  /**
   * Default constructor
   */
  private RssAgregatorCache() {
    ResourceLocator res =
        new ResourceLocator("com.silverpeas.rssAgregator.settings.rssAgregatorSettings", "");
    String refreshRate = res.getString("refreshRate");
    REFRESH_RATE = (60 * 1000) * Long.valueOf(refreshRate).longValue();
  }

  /**
   * Get an instance of RssAgregatorCache
   */
  public static final RssAgregatorCache getInstance() {
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
    int refreshRate = spChannel.getRefreshRate() * 60 * 1000; // refresh rate in
    // ms
    cacheNextRefresh.put(key, Long.valueOf(currentTime + refreshRate));
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
