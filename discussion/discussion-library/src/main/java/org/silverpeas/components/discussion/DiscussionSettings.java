/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.discussion;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.silverpeas.kernel.bundle.ResourceLocator;

/** Operational settings with safe defaults and bounds independent of the database schema. */
public enum DiscussionSettings {
  HISTORY_PAGE_SIZE("history.pageSize", 30, 1, 100),
  SEARCH_PAGE_SIZE("search.pageSize", 20, 1, 100),
  PRESENCE_REFRESH_MS("presence.refreshMs", 30000, 1000, 3600000),
  READ_VISIBILITY_MS("read.visibilityMs", 1000, 100, 60000),
  READ_RETRY_MS("read.retryMs", 2000, 100, 60000),
  TYPING_INTERVAL_MS("typing.intervalMs", 2500, 100, 60000),
  TYPING_THROTTLE_MS("typing.throttleMs", 2000, 100, 60000),
  TYPING_IDLE_MS("typing.idleMs", 5000, 100, 60000),
  TYPING_TTL_MS("typing.ttlMs", 8000, 1000, 180000),
  TYPING_MAX_STATES("typing.maxStates", 10000, 100, 100000),
  LIVE_RETRY_INITIAL_MS("live.retryInitialMs", 1000, 100, 60000),
  LIVE_RETRY_MAX_MS("live.retryMaxMs", 30000, 100, 300000),
  PIN_EXCERPT_LENGTH("pins.excerptLength", 180, 1, 2000),
  QUOTE_EXCERPT_LENGTH("quote.excerptLength", 300, 1, 1000),
  EVENT_DURATION_MINUTES("event.durationMinutes", 60, 1, 10080),
  EVENT_LEAD_MINUTES("event.leadMinutes", 15, 0, 10080);

  public static final String BUNDLE = "org.silverpeas.components.discussion.settings.DiscussionSettings";
  private final String key;
  private final int fallback;
  private final int minimum;
  private final int maximum;

  DiscussionSettings(String key, int fallback, int minimum, int maximum) {
    this.key = key;
    this.fallback = fallback;
    this.minimum = minimum;
    this.maximum = maximum;
  }

  public int value() {
    try {
      int value = ResourceLocator.getSettingBundle(BUNDLE).getInteger(key, fallback);
      return value >= minimum && value <= maximum ? value : fallback;
    } catch (NumberFormatException invalid) {
      return fallback;
    }
  }

  public static int typingIntervalMs() {
    return Math.max(TYPING_INTERVAL_MS.value(), TYPING_THROTTLE_MS.value());
  }

  public static int typingTtlMs() {
    return Math.max(TYPING_TTL_MS.value(), TYPING_IDLE_MS.value() + typingIntervalMs());
  }

  /** The same validated values are used by the server and exposed to the browser. */
  public static Map<String, Integer> forClient() {
    var values = Arrays.stream(values()).collect(Collectors.toMap(setting -> setting.key, DiscussionSettings::value));
    values.put("typing.intervalMs", typingIntervalMs());
    values.put("typing.ttlMs", typingTtlMs());
    values.put("live.retryMaxMs", Math.max(LIVE_RETRY_INITIAL_MS.value(), LIVE_RETRY_MAX_MS.value()));
    return Map.copyOf(values);
  }
}
