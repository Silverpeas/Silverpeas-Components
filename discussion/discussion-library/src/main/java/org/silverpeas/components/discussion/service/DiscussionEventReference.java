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
import java.util.Optional;
import java.util.regex.Pattern;
import org.silverpeas.core.contribution.model.ContributionIdentifier;

/** Recognizes native event permalinks in existing announcements without changing stored messages. */
public final class DiscussionEventReference {
  private static final Pattern URL = Pattern.compile("https?://[^\\s<>]+", Pattern.CASE_INSENSITIVE);
  private static final Pattern EVENT = Pattern.compile("almanach[0-9]+:CalendarEvent:[a-zA-Z0-9-]{1,40}");

  private DiscussionEventReference() {
    // Utility class.
  }

  public static Optional<ContributionIdentifier> find(String body, URI application) {
    var found = URL.matcher(body == null ? "" : body).results()
        .map(match -> parse(match.group(), application)).flatMap(Optional::stream).distinct().toList();
    // Several distinct events must never result in an arbitrary event being deleted.
    return found.size() == 1 ? Optional.of(found.getFirst()) : Optional.empty();
  }

  private static Optional<ContributionIdentifier> parse(String value, URI application) {
    try {
      var url = URI.create(value.replaceFirst("[.,!?;:)]+$", ""));
      String prefix = application.getPath().replaceFirst("/+$", "") + "/Contribution/";
      if (url.getUserInfo() != null || url.getHost() == null
          || !url.getHost().equalsIgnoreCase(application.getHost()) || url.getPort() != application.getPort()
          || !url.getScheme().equalsIgnoreCase(application.getScheme()) || !url.getPath().startsWith(prefix)) {
        return Optional.empty();
      }
      String decoded = new String(Base64.getDecoder().decode(url.getPath().substring(prefix.length())), StandardCharsets.UTF_8);
      return EVENT.matcher(decoded).matches() ? Optional.of(ContributionIdentifier.decode(decoded)) : Optional.empty();
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }
}
