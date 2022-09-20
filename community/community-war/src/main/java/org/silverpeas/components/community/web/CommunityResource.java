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

import org.silverpeas.components.community.model.Community;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.annotation.Authorized;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * A REST-based Web resource representing the Community contributions. Only authorized
 * users can access these resources.
 * TODO update the code to your context.
 */
@WebService
@Path("community/{componentInstanceId}")
@Authorized
public class CommunityResource extends RESTWebService {

  @PathParam("componentInstanceId")
  private String componentInstanceId;

  @Override
  protected String getResourceBasePath() {
    return "community";
  }

  @Override
  public String getComponentId() {
    return componentInstanceId;
  }

  /**
   * Gets the JSON representation of a list of Community contributions managed by the
   * component instance of id <code>componentInstanceId</code>.
   * If it doesn't exist, a 404 HTTP code is returned.
   * @return the JSON representation of a list of Community resources.
   * @see WebProcess#execute()
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<CommunityEntity> getAllCommunity() {
    final List<Community> resources = process(() ->
        Community.getAllByComponentInstanceId(getComponentId())
    ).execute();
    return asWebEntities(resources);
  }

  /**
   * Gets the JSON representation of a Community represented by the given identifier.
   * If it doesn't exist, a 404 HTTP code is returned.
   * @param id the identifier of the aimed Community.
   * @return the JSON representation of the asked Community instance.
   * @see WebProcess#execute()
   */
  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public CommunityEntity getById(@PathParam("id") String id) {
    final Community resource = process(() -> getCommunity(id)).execute();
    return asWebEntity(resource);
  }

  /**
   * Creates a new Community from its JSON representation and returns it once created.
   * If the user isn't authenticated, a 401 HTTP code is returned. If the user isn't authorized to
   * create a Community, a 403 is returned. If a problem occurs when processing the
   * request, a 503 HTTP code is returned.
   * @param entity the entity decoded from the embodied JSON representation of a Community
   * @return the response of the creation with the JSON representation of the created
   * Community.
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createCommunity(CommunityEntity entity) {
    Community newCommunity = entity.asCommunityFor(getComponentId());
    Community createdCommunity = process(newCommunity::save).execute();
    CommunityEntity createdEntity = asWebEntity(createdCommunity);
    return Response.created(createdEntity.getURI()).entity(createdEntity).build();
  }

  /**
   * Updates the Community identified by the specified identifier from the JSON
   * representation embodied in the incoming request and returns it once updated. If the
   * Community entity doesn't match with the targeted one, a 400 HTTP code is returned.
   * If the Community doesn't exist, a 404 HTTP code is returned. If the user isn't
   * authenticated, a 401 HTTP code is returned. If the user isn't authorized to update the
   * Community, a 403 is returned. If a problem
   * occurs when processing the request, a 503 HTTP code is returned.
   * @param id the identifier of the Community to update
   * @param entity the Web entity from which the Community has to be updated
   * @return the response of the update with the JSON representation of the updated
   * Community.
   */
  @PUT
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CommunityEntity updateCommunity(@PathParam("id") String id,
        CommunityEntity entity) {
    final Community resource = getCommunity(id);
    entity.update(resource);
    Community updatedCommunity = process(resource::save).execute();
    return asWebEntity(updatedCommunity);
  }

  /**
   * Deletes the Community identified by the specified identifier.
   * If the Community doesn't exist, a 404 HTTP code is returned. If the user isn't
   * authenticated, a 401 HTTP code is returned. If the user isn't authorized to delete the
   * Community, a 403 is returned. If a problem occurs when processing the request, a
   * 503 HTTP code is returned.
   * @param id the identifier of the Community to delete
   */
  @DELETE
  @Path("{id}")
  public void deleteCommunity(@PathParam("id") String id) {
    final Community resource = getCommunity(id);
    process(() -> {
      resource.delete();
      return null;
    }).execute();
  }

  private Community getCommunity(final String id) {
    final Community resource = Community.getById(id);
    if (resource == null || !resource.getComponentInstanceId().equals(getComponentId())) {
      SilverLogger.getLogger(this).error("Unknown Community at {0}", getUri().getPath());
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    return resource;
  }

  private CommunityEntity asWebEntity(final Community resource) {
    return new CommunityEntity(resource)
        .identifiedBy(getUri().getAbsolutePathBuilder().path(resource.getId()).build());
  }

  private List<CommunityEntity> asWebEntities(
      final List<Community> resources) {
    return resources.stream().map(this::asWebEntity).collect(SilverpeasList.collector(resources));
  }
}