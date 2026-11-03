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
package org.silverpeas.components.discussion.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DiscussionEventReferenceTest {
  private static final URI APPLICATION = URI.create("https://silverpeas.test/silverpeas");

  @Test
  void existingAnnouncementsResolveTheNativeEventWithoutChangingTheirBody() {
    String link = link("almanach22:CalendarEvent:event1");
    var reference = DiscussionEventReference.find("📅 Réunion\n4 nov. 2026\n" + link, APPLICATION).orElseThrow();
    assertEquals("almanach22", reference.getComponentInstanceId());
    assertEquals("event1", reference.getLocalId());
    assertTrue(DiscussionEventReference.find("(" + link + ").", APPLICATION).isPresent());
    assertTrue(DiscussionEventReference.find(link + "\n" + link, APPLICATION).isPresent());
  }

  @Test
  void foreignCredentialsMalformedReferencesAndSeveralEventsNeverSelectAnArbitraryEvent() {
    String link = link("almanach22:CalendarEvent:event1");
    assertTrue(DiscussionEventReference.find(link.replace("silverpeas.test", "external.test"), APPLICATION).isEmpty());
    assertTrue(DiscussionEventReference.find(link.replace("https://", "https://user:password@"), APPLICATION).isEmpty());
    assertTrue(DiscussionEventReference.find(link.replace("/silverpeas/", "/another/"), APPLICATION).isEmpty());
    assertTrue(DiscussionEventReference.find(link("kmelia22:Publication:event1"), APPLICATION).isEmpty());
    assertTrue(DiscussionEventReference.find("https://silverpeas.test/silverpeas/Contribution/invalid%zz", APPLICATION).isEmpty());
    assertTrue(DiscussionEventReference.find(link + "\n" + link("almanach22:CalendarEvent:event2"), APPLICATION).isEmpty());
    assertTrue(DiscussionEventReference.find(null, APPLICATION).isEmpty());
  }

  private String link(String value) {
    return APPLICATION + "/Contribution/" + Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
  }
}
