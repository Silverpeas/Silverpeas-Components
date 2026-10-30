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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/** Recognizes local Silverpeas publication links without following or requesting any URL. */
final class DiscussionPublicationLinks {
  private static final Pattern LINKS = Pattern.compile("https?://[^\\s<>\"']+|(?<![\\w/:])/[\\w][^\\s<>\"']*", Pattern.CASE_INSENSITIVE);
  private static final Pattern ID = Pattern.compile("[0-9]{1,18}");

  private DiscussionPublicationLinks() {
  }

  static List<String> ids(String body, URI application) {
    return LINKS.matcher(body).results().map(match -> identify(match.group(), application))
        .filter(id -> !id.isEmpty()).distinct().toList();
  }

  private static String identify(String raw, URI application) {
    try {
      URI link = application.resolve(raw.replaceFirst("[),.;!?]+$", ""));
      if (!local(link, application)) {
        return "";
      }
      String root = application.getPath().replaceFirst("/$", "");
      String path = link.getPath();
      String prefix = root + "/Publication/";
      if (path.startsWith(prefix)) {
        String id = path.substring(prefix.length());
        return ID.matcher(id).matches() ? id : "";
      }
      if (!path.matches(Pattern.quote(root) + "/R(?:kmelia|toolbox|kmax)/[^/]+/(?:ViewPublication|searchResult)")) {
        return "";
      }
      String id = parameter(link, "PubId");
      if (path.endsWith("/searchResult")) {
        id = "Publication".equalsIgnoreCase(parameter(link, "Type")) ? parameter(link, "Id") : "";
      }
      return ID.matcher(id).matches() ? id : "";
    } catch (IllegalArgumentException invalidLink) {
      return "";
    }
  }

  private static String parameter(URI link, String name) {
    return Arrays.stream(java.util.Objects.toString(link.getRawQuery(), "").split("&"))
        .map(part -> part.split("=", 2)).filter(pair -> pair.length == 2 && pair[0].equalsIgnoreCase(name))
        .map(pair -> URLDecoder.decode(pair[1], StandardCharsets.UTF_8)).findFirst().orElse("");
  }

  private static boolean local(URI link, URI application) {
    return application.getScheme() != null && application.getHost() != null && link.getUserInfo() == null
        && application.getScheme().equalsIgnoreCase(link.getScheme())
        && application.getHost().equalsIgnoreCase(link.getHost())
        && port(link) == port(application);
  }

  private static int port(URI uri) {
    if (uri.getPort() >= 0) {
      return uri.getPort();
    }
    return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
  }
}
