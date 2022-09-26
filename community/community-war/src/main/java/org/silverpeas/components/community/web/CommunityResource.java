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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.community.web;

import org.silverpeas.components.community.CommunityWebManager;
import org.silverpeas.components.community.model.CommunityOfUsers;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.annotation.Authorized;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.silverpeas.core.util.StringUtil.EMPTY;

/**
 * A REST-based Web resource representing the communities of users. A community is always related to
 * a resource in Silverpeas, and it is managed by a Community application instance. Only authorized
 * users can access these resources.
 */
@WebService
@Path(CommunityResource.RESOURCE_NAME + "/{componentInstanceId}")
@Authorized
public class CommunityResource extends RESTWebService {

  protected static final String RESOURCE_NAME = "community";

  @PathParam("componentInstanceId")
  private String componentInstanceId;

  @Override
  protected String getResourceBasePath() {
    return CommunityResource.RESOURCE_NAME;
  }

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
  public CommunityOfUsersEntity getCommunity() {
    final CommunityOfUsers resource = getCommunityOfUsers();
    return asWebEntity(resource);
  }

  /**
   * Gets the JSON representation of the members of a community of users  managed by the
   * component instance of id
   * <code>componentInstanceId</code>. If it doesn't exist, a 404 HTTP code is returned.
   * @return the JSON representation of a community resource.
   * @see WebProcess#execute()
   */
  @GET
  @Path("members")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getCommunityMembers() {
    // todo use the dedicated member entity when ready
    final CommunityOfUsers resource = getCommunityOfUsers();
    final String json = JSONCodec.encodeArray(a -> {
      CommunityWebManager.get()
          .getMembers(resource)
          .forEach(m -> a.addJSONObject(o -> o
              .put("lastname", m.getUser().getLastName())
              .put("firstname", m.getUser().getFirstName())
              .put("status", m.getStatus().name())));
      return a;
    });
    return Response.ok(json).build();
  }

  /**
   * Gets the JSON representation of current user membership to a community of users managed by the
   * component instance of id
   * <code>componentInstanceId</code>. If it doesn't exist, a 404 HTTP code is returned.
   * @return the JSON representation of a community resource.
   * @see WebProcess#execute()
   */
  @GET
  @Path("members/me")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getCommunityMembership() {
    // todo use the dedicated member entity when ready
    final CommunityOfUsers resource = getCommunityOfUsers();
    final String json = resource.getMembershipsProvider()
        .get(User.getCurrentRequester())
        .map(m -> JSONCodec.encodeObject(o -> o
            .put("lastname", m.getUser().getLastName())
            .put("firstname", m.getUser().getFirstName())
            .put("status", m.getStatus().name())))
        .orElse(EMPTY);
    return Response.ok(json).build();
  }

  private CommunityOfUsers getCommunityOfUsers() {
    return process(() ->
        CommunityOfUsers.getByComponentInstanceId(getComponentId())
            .orElseThrow(
                () -> new NotFoundException("No such component instance: " + componentInstanceId))
    ).execute();
  }

  private CommunityOfUsersEntity asWebEntity(final CommunityOfUsers resource) {
    return new CommunityOfUsersEntity(resource).identifiedBy(
        getUri().getBaseUriBuilder().path(RESOURCE_NAME).path(resource.getComponentInstanceId())
            .build());
  }
}