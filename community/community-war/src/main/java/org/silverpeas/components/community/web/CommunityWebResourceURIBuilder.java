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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.components.community.web;

import org.silverpeas.components.community.model.CommunityMembership;
import org.silverpeas.components.community.model.CommunityOfUsers;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.web.WebResourceUri;
import org.silverpeas.core.webapi.profile.ProfileResourceBaseURIs;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * A builder of URI of web resources handled by the REST-based web services in the community
 * application.
 * @author mmoquillon
 */
class CommunityWebResourceURIBuilder {

  private final URI baseUri;
  private final URI currentUri;

  CommunityWebResourceURIBuilder(final WebResourceUri uri) {
    this.baseUri = uri.getBaseUri();
    this.currentUri = uri.getAbsolutePath();
  }

  /**
   * Gets the current URI targeted by the incoming request relating the current requested resource.
   * @return the URI of the current requested resource.
   */
  public URI getCurrentResourceURI() {
    return currentUri;
  }

  public URI getCommunityURI(final CommunityOfUsers community) {
    return UriBuilder.fromUri(baseUri)
        .path(CommunityWebResource.RESOURCE_NAME)
        .path(community.getComponentInstanceId())
        .build();
  }

  public URI getUserURI(final User user) {
    return UriBuilder.fromUri(baseUri)
        .path(ProfileResourceBaseURIs.uriOfUser(user.getId()).toString())
        .build();
  }

  public URI getCommunityMembershipsURI(final CommunityOfUsers community) {
    return UriBuilder.fromUri(baseUri)
        .path(CommunityWebResource.RESOURCE_NAME)
        .path(community.getComponentInstanceId())
        .path(CommunityMembershipResource.RESOURCE_NAME)
        .build();
  }

  public URI getMembershipURI(final CommunityMembership membership) {
    return UriBuilder.fromUri(baseUri)
        .path(CommunityWebResource.RESOURCE_NAME)
        .path(membership.getCommunity().getComponentInstanceId())
        .path(CommunityMembershipResource.RESOURCE_NAME)
        .path("all")
        .path(membership.getId())
        .build();
  }
}
