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
 */
package org.silverpeas.components.community.web;

import org.silverpeas.components.community.model.CommunityOfUsers;
import org.silverpeas.core.annotation.WebService;

import javax.transaction.Transactional;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.net.URI;

import static java.util.Optional.ofNullable;

/**
 * A REST-based Web resource representing the communities of users. A community is always related to
 * a resource in Silverpeas, and it is managed by a Community application instance. Only authorized
 * users can access these resources.
 */
@WebService
@Path(CommunityWebResource.RESOURCE_NAME + "/{componentInstanceId}")
public class CommunityOfUsersResource extends CommunityWebResource {

  @PathParam("componentInstanceId")
  private String componentInstanceId;

  @Override
  public String getComponentId() {
    return componentInstanceId;
  }

  /**
   * Gets the JSON representation of the community of users  managed by the component instance of
   * id
   * <code>componentInstanceId</code>. If it doesn't exist, a 404 HTTP code is returned.
   * @return the JSON representation of a community resource.
   * @see WebProcess#execute()
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CommunityOfUsersEntity getCommunityOfUsers() {
    return process(this::asWebEntity);
  }

  /**
   * Updates the community of users with the properties of the specified web entity representing the
   * new state of the community. Because the space for which the community has been defined is
   * permanent and because the memberships of the community is handled by another resource (referred
   * by a URI), only data on the home page and the charter can be modified.
   * @param entity the new state of the community of users.
   * @return the JSON representation of the updated state of the community of users.
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  public CommunityOfUsersEntity updateCommunityOfUsers(final CommunityOfUsersEntity entity) {
    return process(community -> {
      checkValidity(entity, community);
      ofNullable(entity.getCharterURL()).ifPresentOrElse(
          community::setCharterURL,
          community::unsetCharterURL);
      community.setHomePage(entity.getHomePage().getFirst(), entity.getHomePage().getSecond());
      community.save();
      return asWebEntity(community);
    });
  }

  private void checkValidity(final CommunityOfUsersEntity entity,
      final CommunityOfUsers community) {
    var uriBuilder = getCommunityUriBuilder();
    URI communityURI = uriBuilder.getCommunityURI(community);
    URI membershipsURI = uriBuilder.getCommunityMembershipsURI(community);
    if (!community.getComponentInstanceId().equals(entity.getId()) ||
        !community.getSpaceId().equals(entity.getSpaceId()) ||
        !communityURI.equals(entity.getURI()) ||
        !membershipsURI.equals(entity.getMemberships())) {
      throw new BadRequestException(
          "The community entity doesn't match the community to update");
    }
  }

  private CommunityOfUsersEntity asWebEntity(final CommunityOfUsers community) {
    return CommunityOfUsersEntity.builder().with(community)
        .with(getCommunityUriBuilder())
        .build();
  }
}