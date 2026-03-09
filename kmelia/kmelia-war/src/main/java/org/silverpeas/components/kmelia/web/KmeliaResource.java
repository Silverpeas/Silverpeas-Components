/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia.web;

import jakarta.inject.Inject;
import org.silverpeas.components.kmelia.service.KmeliaService;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationRuntimeException;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.annotation.Authorized;
import org.silverpeas.core.webapi.publication.PublicationEntity;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.net.URI;

/**
 * A REST Web resource allowing to update data related to a publication.
 */
@WebService
@Path(KmeliaResource.PATH + "/{componentId}")
@Authorized
public class KmeliaResource extends RESTWebService {

  static final String PATH = "publications";

  @PathParam("componentId")
  private String componentId;

  @Inject
  private KmeliaService kmeliaService;

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }

  @Override
  public String getComponentId() {
    return componentId;
  }

  /**
   * Creates a publication corresponding to the given publication entity and whose parent node
   * matches the specified node ID.
   *
   * @param nodeId The ID of the publication's parent node.
   * @param publicationEntity The description of the publication to create.
   * @return HTTP response
   */
  @Path("/{nodeId}")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response savePublication(@PathParam("nodeId") String nodeId,
      PublicationEntity publicationEntity) {
    try {
      publicationEntity.setCreator(UserDetail.from(getUser()));
      PublicationDetail publication = publicationEntity.toPublicationDetail();

      NodePK nodePK = getNodePK(nodeId);

      String pubId = getKmeliaService().createPublicationIntoTopic(publication, nodePK);
      publication.getPK().setId(pubId);

      URI publicationURI = getUri().getRequestUriBuilder().path(publication.getPK().getId())
          .build();
      return Response.created(publicationURI).
          entity(asWebEntity(publication, identifiedBy(publicationURI))).build();
    } catch (PublicationRuntimeException ex) {
      throw new WebApplicationException(ex, Status.CONFLICT);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Updates the publication described by the given publication entity.
   *
   * @param publicationEntity The description of the publication to update.
   * @return a response containing the entity describing the updated publication.
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updatePublication(final PublicationEntity publicationEntity) {
    try {
      PublicationDetail publication = publicationEntity.toPublicationDetail();

      // Publication status is a mandatory data into the context of publication update.
      // As this data is not handled by this service, it is retrieved from the silverpeas data
      // before performing the update.
      publication.setStatus(getKmeliaService().getPublicationDetail(publication.getPK()).getStatus());

      // Now, the update can be performed
      getKmeliaService().updatePublication(publication);

      URI publicationURI = getUri().getRequestUriBuilder().path(publication.getPK().getId())
          .build();
      return Response.ok(publicationURI).
          entity(asWebEntity(publication, identifiedBy(publicationURI))).build();
    } catch (PublicationRuntimeException ex) {
      throw new WebApplicationException(ex, Status.CONFLICT);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  private NodePK getNodePK(String nodeId) {
    return new NodePK(nodeId, getComponentId());
  }

  private URI identifiedBy(URI uri) {
    return uri;
  }

  private PublicationEntity asWebEntity(final PublicationDetail publication, URI publicationURI) {
    return PublicationEntity.fromPublicationDetail(publication, publicationURI);
  }

  private KmeliaService getKmeliaService() {
    return kmeliaService;
  }

}
