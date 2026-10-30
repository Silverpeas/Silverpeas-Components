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
package org.silverpeas.components.discussion.web;

import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DiscussionPublicationLinksTest {
  private static final URI APP = URI.create("https://silverpeas.test/silverpeas");

  @Test
  void recognizesNativePermalinksAndDeduplicatesTheyReferToTheSamePublication() {
    assertEquals(List.of("12", "34"), DiscussionPublicationLinks.ids(
        "Voir https://silverpeas.test/silverpeas/Publication/12?ComponentId=kmelia2 et "
        + "/silverpeas/Publication/12. Puis (https://silverpeas.test/silverpeas/Publication/34).", APP));
  }

  @Test
  void recognizesNativeNavigationUrlsAndEncodedIdentifiers() {
    assertEquals(List.of("12", "34"), DiscussionPublicationLinks.ids(
        "/silverpeas/Rkmelia/kmelia1/ViewPublication?PubId=%31%32 "
        + "https://silverpeas.test/silverpeas/Rtoolbox/toolbox1/searchResult?Type=Publication&Id=34", APP));
    assertTrue(DiscussionPublicationLinks.ids(
        "/silverpeas/Rkmelia/kmelia1/searchResult?Type=Document&Id=12", APP).isEmpty());
  }

  @Test
  void rejectsExternalOriginsInvalidPathsProtocolsAndUnknownPublicationFormats() {
    for (String url : List.of("https://evil.test/silverpeas/Publication/12", "https://silverpeas.test.evil/Publication/12",
        "http://silverpeas.test/silverpeas/Publication/12", "https://silverpeas.test:8443/silverpeas/Publication/12",
        "https://user@silverpeas.test/silverpeas/Publication/12", "/other/Publication/12",
        "/silverpeas/Publication/not-a-number", "javascript:/silverpeas/Publication/12", "https://[invalid")) {
      assertTrue(DiscussionPublicationLinks.ids(url, APP).isEmpty(), url);
    }
  }
}
