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

import java.util.Map;
import java.util.Properties;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.silverpeas.components.discussion.DiscussionSettingsTestSupport.configured;

@EnableSilverTestEnv(context = JEETestContext.class)
class DiscussionSettingsTest {
  @Test
  void distributedDefaultsMatchTheServerAndBrowserAndEverySettingIsDocumented() throws Exception {
    var properties = new Properties();
    try (var reader = Files.newBufferedReader(Path.of(System.getProperty("basedir"), "..",
        "discussion-configuration/src/main/config/properties/org/silverpeas/components/discussion/settings/DiscussionSettings.properties"))) {
      properties.load(reader);
    }
    try (var ignored = configured(Map.of())) {
      var values = DiscussionSettings.forClient();
      assertEquals(properties.size(), values.size());
      properties.forEach((key, value) -> assertEquals(Integer.parseInt((String) value), values.get(key)));
      assertEquals(30, DiscussionSettings.HISTORY_PAGE_SIZE.value());
      assertThrows(UnsupportedOperationException.class, () -> values.put("presence.refreshMs", 1));
    }
  }

  @Test
  void overridesAreReloadedAndInvalidValuesFallBackWithoutEscapingSafetyBounds() {
    try (var ignored = configured(Map.of("history.pageSize", " 7 ", "search.pageSize", "3", "presence.refreshMs", "45000"))) {
      assertEquals(7, DiscussionSettings.HISTORY_PAGE_SIZE.value());
      assertEquals(3, DiscussionSettings.SEARCH_PAGE_SIZE.value());
      assertEquals(45000, DiscussionSettings.forClient().get("presence.refreshMs"));
    }
    for (String invalid : new String[]{"", "abc", "0", "-1", "101", "2147483648"}) {
      try (var ignored = configured(Map.of("history.pageSize", invalid))) {
        assertEquals(30, DiscussionSettings.HISTORY_PAGE_SIZE.value());
      }
    }
    try (var ignored = configured(Map.of("event.leadMinutes", "0"))) {
      assertEquals(0, DiscussionSettings.EVENT_LEAD_MINUTES.value());
    }
  }

  @Test
  void relatedTimersCannotExpireTypingEarlyOrReduceTheRetryCeilingBelowTheInitialDelay() {
    try (var ignored = configured(Map.of("typing.intervalMs", "1000", "typing.throttleMs", "3000",
        "typing.idleMs", "6000", "typing.ttlMs", "4000", "live.retryInitialMs", "8000", "live.retryMaxMs", "1000"))) {
      assertEquals(3000, DiscussionSettings.typingIntervalMs());
      assertEquals(9000, DiscussionSettings.typingTtlMs());
      assertEquals(9000, DiscussionSettings.forClient().get("typing.ttlMs"));
      assertEquals(8000, DiscussionSettings.forClient().get("live.retryMaxMs"));
    }
  }
  @Test
  void pinnedExcerptUsesConfiguredUnicodeLengthWhilePreservingTheFullMessage() {
    var message = org.mockito.Mockito.mock(org.silverpeas.components.discussion.model.DiscussionMessage.class);
    org.mockito.Mockito.when(message.getBody()).thenReturn("😀".repeat(12));
    try (var ignored = configured(Map.of("pins.excerptLength", "5"))) {
      var pin = org.silverpeas.components.discussion.service.PinnedMessage.from(message);
      assertEquals("😀".repeat(5) + "…", pin.excerpt());
      assertEquals("😀".repeat(12), pin.text());
    }
  }

}
